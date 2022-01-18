import {Injectable} from '@angular/core';
import {Location} from '@angular/common';
import {Router} from '@angular/router';
import {BehaviorSubject, Observable, ReplaySubject, Subject, Subscription} from 'rxjs';
import * as SockJS from 'sockjs-client';
import * as Stomp from 'webstomp-client';
import {AuthServerProvider} from '../../core/auth/auth-jwt.service';
import {environment} from '../../../environments/environment';
import {Notification, NotificationType} from '../../models/notification.model';
import {CallState} from '../../models/call-state.model';
import {UserState} from '../../models/user-state.model';
import {IRoom} from "../../models/room.model";
import {UserSocket} from "../../models/user-socket.model";
import {IUser} from "../../models/user.model";

@Injectable({providedIn: 'root'})
export class WebsocketService {
  private stompClient: Stomp.Client | null = null;
  private routerSubscription: Subscription | null = null;
  private connectionSubject: ReplaySubject<void> = new ReplaySubject(1);
  private connectionSubscription: Subscription | null = null;
  private stompSubscription: Stomp.Subscription | null = null;
  private userStateSubject$: Subject<{ user: string, content: string, state: UserState }> = new Subject();
  readonly userState$: Observable<{ user: string, content: string, state: UserState }> = this.userStateSubject$.asObservable();
  private callStateSubject$: Subject<{ room: string, user: IUser, content: string, state: CallState }> = new Subject();
  readonly callState$: Observable<{ room: string, user: IUser, content: string, state: CallState }> = this.callStateSubject$.asObservable();
  private notificationSubject$: Subject<string> = new Subject();
  readonly notification$: Observable<string> = this.notificationSubject$.asObservable();
  private userSocketListSubject$: Subject<UserSocket[]> = new Subject<UserSocket[]>();
  readonly userSocketList$: Observable<UserSocket[]> = this.userSocketListSubject$.asObservable();
  private messageSubject$: Subject<{ user: IUser, message: string, room: number }> = new Subject();
  readonly message$: Observable<{ user: IUser, message: string, room: number }> = this.messageSubject$.asObservable();

  constructor(
    private router: Router,
    private authServerProvider: AuthServerProvider,
    private location: Location,
  ) {
  }

  connect(): void {
    if (this.stompClient?.connected) {
      return;
    }
    let url = environment.wsEndpoint;
    const authToken = this.authServerProvider.getToken();
    if (authToken) {
      url += '?access_token=' + authToken;
    }
    const socket: WebSocket = new SockJS(url);
    this.stompClient = Stomp.over(socket, {protocols: ['v12.stomp']});
    this.stompClient.debug = () => {};
    const headers: Stomp.ConnectionHeaders = {};
    this.stompClient.connect(headers, () => {
      this.connectionSubject.next();
    });
  }

  disconnect(): void {
    this.unsubscribe();
    this.connectionSubject = new ReplaySubject(1);
    if (this.routerSubscription) {
      this.routerSubscription.unsubscribe();
      this.routerSubscription = null;
    }
    if (this.stompClient) {
      if (this.stompClient.connected) {
        this.stompClient.disconnect();
      }
      this.stompClient = null;
    }
  }

  subscribeToTopic(loggedUser: string): void {
    if (this.connectionSubscription) {
      return;
    }
    this.connectionSubscription = this.connectionSubject.subscribe(() => {
      if (this.stompClient) {
        this.stompSubscription = this.stompClient.subscribe(
          '/topic/public',
          (data: Stomp.Message) => {
            const payload: Notification = JSON.parse(data.body);
            this.handleNotification(payload);
          });
        this.stompSubscription = this.stompClient.subscribe(
          '/user/' + loggedUser + '/queue/messages',
          (data: Stomp.Message) => {
            const payload: Notification = JSON.parse(data.body);
            this.handleNotification(payload);
          });
      }
    });
  }

  unsubscribe(): void {
    if (this.stompSubscription) {
      this.stompSubscription.unsubscribe();
      this.stompSubscription = null;
    }
    if (this.connectionSubscription) {
      this.connectionSubscription.unsubscribe();
      this.connectionSubscription = null;
    }
  }

  sendNotification(destination: string, notification: Notification): void {
    if (this.stompClient) {
      this.stompClient.send(
        destination,
        JSON.stringify(notification),
        {}
      );
    }
  }

  sendRequestToFetchOnlineUsers(): void {
    if (this.connectionSubscription && this.stompClient) {
      this.stompClient.send(
        '/online-users',
        '',
        {}
      );
    }
  }

  sendRequestToUpdateUserStatus(state: UserState): void {
    if (this.connectionSubscription && this.stompClient) {
      this.stompClient.send(
        '/update-user-state',
        state,
        {}
      );
    }
  }

  private handleNotification(notification: Notification): void {
    let {type, content, metadata} = notification;
    typeof metadata === 'string' ? metadata = JSON.parse(metadata) : null;
    const data = metadata ? new Map(Object.entries(metadata)) : null;
    const room = data?.get("ROOM");
    const user = data?.get("USER");
    switch (type) {
      case NotificationType.USER_STATE:
        const state = data?.get("STATE");
        !content ? content = '' : null;
        this.userStateSubject$.next({user, content, state: state});
        break;
      case NotificationType.INCOMING_CALL:
        !content ? content = '' : null;
        this.callStateSubject$.next({room, content, user: JSON.parse(user) ,state: CallState.INCOMING_CALL});
        break;
      case NotificationType.INCOMING_MESSAGE:
        const message = data?.get("MESSAGE");
        this.messageSubject$.next({user: JSON.parse(user), message, room});
        content ? this.notificationSubject$.next(content) : null;
        break;
      case NotificationType.ACCEPTED_CALL:
        !content ? content = '' : null;
        this.callStateSubject$.next({room, content, user: JSON.parse(user) , state: CallState.ACCEPTED_CALL});
        break;
      case NotificationType.REJECTED_CALL:
        !content ? content = '' : null;
        this.callStateSubject$.next({room, content, user: JSON.parse(user) , state: CallState.REJECTED_CALL});
        break;
      case NotificationType.CANCELLED_CALL:
        !content ? content = '' : null;
        this.callStateSubject$.next({room, content, user: JSON.parse(user) , state: CallState.CANCELLED_CALL});
        break;
      case NotificationType.ONLINE_USERS:
        const resultStr = data?.get("USERS");
        const users = resultStr ? resultStr.split(",") : [];
        this.userSocketListSubject$.next(JSON.parse(users));
        break;
      default:
    }
  }
}

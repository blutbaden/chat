import {Component, EventEmitter, OnDestroy, OnInit, Output} from '@angular/core';
import {WebsocketService} from '../../../services/websocket.service';
import {UserState} from '../../../../models/user-state.model';
import {concatMap, map, mergeMap} from "rxjs/operators";
import {HttpResponse} from "@angular/common/http";
import {GroupService} from "../../../../pages/group/group.service";
import {IUser} from "../../../../models/user.model";
import {RoomService} from "../../room/room.service";
import {EMPTY, Observable, Subscription} from "rxjs";
import {IRoom} from "../../../../models/room.model";
import {UserSocket} from "../../../../models/user-socket.model";
import {ConversationService} from "../../../services/conversation.service";
import {ChatCallNotificationService} from "../../../services/chat-call-notification.service";
import {UserStateService} from "../../../services/user-state.service";

@Component({
  selector: 'app-chat-list-user',
  templateUrl: './chat-list-user.component.html',
  styleUrls: ['./chat-list-user.component.scss']
})
export class ChatListUserComponent implements OnInit, OnDestroy {

  usersInfo: { user: IUser, state: UserState, count: number }[] = [];

  query?: string;

  @Output() selectedUser = new EventEmitter<{ user: IUser, state: UserState }>();
  @Output() selectedRoom = new EventEmitter<IRoom>();

  isLoading: boolean = false;

  userSubscription: Subscription | null = null;
  userStateSubscription: Subscription | null = null;
  roomSubscription: Subscription | null = null;
  messageSubscription: Subscription | null = null;

  constructor(
    private websocketService: WebsocketService,
    private groupService: GroupService,
    private roomService: RoomService,
    private conversationService: ConversationService,
    private chatCallNotificationService: ChatCallNotificationService,
    private userStateService: UserStateService
  ) {
  }

  public get userState(): typeof UserState {
    return UserState;
  }

  ngOnInit(): void {
    this.messageSubscription = this.websocketService.message$.subscribe(({user, message, room}) => {
      const index = this.usersInfo.findIndex(item => item.user.login == user.login);
      if (index !== -1) {
        this.usersInfo[index].count++;
      }
    });
    this.websocketService.sendRequestToFetchOnlineUsers();
    this.getUsers();
    this.subscribeToUserStateEvent();
  }

  onCallUser(user: IUser): void {
    const room$ = this.roomService.getPrivateRoomByLoggedUserAndSelectedUser(user)
      .pipe(
        map(res => res.body),
      );
    this.roomSubscription = room$.subscribe((room) => {
      room ? this.chatCallNotificationService.makeCall(room, user.login!) : null;
    });
  }

  onSelectUser(item: { user: IUser, state: UserState }): void {
    const room$ = this.roomService.getPrivateRoomByLoggedUserAndSelectedUser(item.user)
      .pipe(
        map((res: HttpResponse<IRoom>) => res.body),
        mergeMap((room: IRoom | null) => this.updateRoomState(room))
      );
    this.roomSubscription = room$.subscribe((room) => {
      this.selectedUser.next(item);
      room ? this.selectedRoom.next(room) : null;
    });
  }

  updateRoomState(room: IRoom | null): Observable<IRoom> {
    if (room) {
      return this.conversationService.updateConversationStateByRoom(room?.id!).pipe(
        map(() => room)
      );
    }
    return EMPTY;
  }

  getUsername(user: IUser): string | undefined {
    return user ? user.login : undefined;
  }

  getStateClass(type: 'bg' | 'text', state: UserState) {
    return this.userStateService.getStateClass(type, state);
  }

  ngOnDestroy(): void {
    this.userStateSubscription?.unsubscribe();
    this.userSubscription?.unsubscribe();
    this.roomSubscription?.unsubscribe();
    this.messageSubscription?.unsubscribe();
  }

  private getUsers(): void {
    this.isLoading = true;
    const users$ = this.websocketService.userSocketList$.pipe(
      // get count delivered messages by receiver is logged user and grouped by sender (user) => /count
      concatMap((userSockets: UserSocket[]) => this.getCurrentUserConversationsCountByStateGroupedBySender().pipe(
        map((count: Map<number, number>) => {
          return {
            userSockets: userSockets,
            count: count
          };
        }),
        // get distinct users joined to same groups as logged user /users
        concatMap((result: { userSockets: UserSocket[], count: Map<number, number> }) => this.groupService.getUsersJoinedToSameGroupsAsLoggedUser().pipe(
          map((res: HttpResponse<IUser[]>) => {
            const users = res.body || [];
            const {userSockets, count} = result;
            return users.map(user => {
              let index = userSockets.findIndex(userSocket => userSocket.username === user.login);
              // return mixed result (sender & count)
              return {
                user: user,
                state: index === -1 ? UserState.OFFLINE : userSockets[index].state!,
                count: count?.get(user.id!) || 0
              };
            });
          }),
        )),
      )),
    );
    this.userSubscription = users$.subscribe((users) => {
      this.isLoading = false;
      this.usersInfo = users;
    });
  }

  private getCurrentUserConversationsCountByStateGroupedBySender(): Observable<Map<number, number>> {
    return this.conversationService.getCurrentUserConversationsCountByStateGroupedBySender().pipe(
      map((res: HttpResponse<any[]>) => {
        let data = res.body ?? [];
        return data.reduce(function (map, item) {
          if (map.has(item[1])) map.get(item[0]).push(item[1]);
          else map.set(item[0], item[1]);
          return map;
        }, new Map<number, number>());
      }),
    );
  }

  private subscribeToUserStateEvent() {
    this.userStateSubscription = this.websocketService.userState$.subscribe((data: { user: string, content: string, state: UserState }) => {
      let index = this.usersInfo.findIndex(item => {
        const {login} = item.user;
        return login === data.user;
      });
      index !== -1 ? this.usersInfo[index].state = data.state : null;
    });
  }
}

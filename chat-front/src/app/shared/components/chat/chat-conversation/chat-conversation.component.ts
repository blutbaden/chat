import {Component, ElementRef, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ChatCallNotificationService} from '../../../services/chat-call-notification.service';
import {NotificationType} from "../../../../models/notification.model";
import {WebsocketService} from "../../../services/websocket.service";
import {IRoom} from "../../../../models/room.model";
import {EMPTY, Observable, Subscription} from "rxjs";
import {finalize, map} from "rxjs/operators";
import {IConversation} from "../../../../models/conversation.model";
import {IUser} from "../../../../models/user.model";
import {ConversationService} from "../../../services/conversation.service";
import {UserState} from "../../../../models/user-state.model";
import {UserStateService} from "../../../services/user-state.service";

@Component({
  selector: 'app-chat-conversation',
  templateUrl: './chat-conversation.component.html',
  styleUrls: ['./chat-conversation.component.scss']
})
export class ChatConversationComponent implements OnInit, OnDestroy {

  @Input() room?: IRoom;
  @Input() type?: 'GROUP' | 'PRIVATE';
  @Input() sender?: IUser;
  @Input() receiver?: { user: IUser, state: UserState };
  @Input() enableEmoji: boolean = false;
  @Input() enableRecordingMessage: boolean = false;

  @ViewChild('scroll') scroll?: ElementRef;

  scrollTop?: number;
  message: string = '';
  size: number = 5;
  page: number = 0;
  totalPages: number = 0;
  conversations: { user: IUser, message: string, timestamp: Date }[] = [];
  isLoading: boolean = false;

  private subscription: Subscription | null = null;
  private conversationsSubscription: Subscription | null = null;
  private userStateSubscription: Subscription | null = null;

  constructor(
    private chatCallNotificationService: ChatCallNotificationService,
    private websocketService: WebsocketService,
    private conversationService: ConversationService,
    private userStateService: UserStateService
  ) {
  }

  ngOnInit(): void {
    this.subscription = this.websocketService.message$.subscribe(({user, message, room}) => {
      if (this.room?.id === Number(room) && user !== this.sender?.login) {
        this.updateRoomStateAndPushMessage(user.login!, message, Number(room));
      }
    });
    this.conversationsSubscription = this.loadMessages()
      .subscribe(
        (res) => this.conversations = res,
      );
    if(this.type === 'PRIVATE') { // subscribe to user state event
      this.subscribeToUserStateEvent();
    }
  }

  updateRoomStateAndPushMessage(user: string, message: string, roomId: number): void {
    this.conversationService.updateConversationStateByRoom(roomId).subscribe(
      () => {
        this.conversations.push({user: {login: user}, message, timestamp: new Date()});
        setTimeout(() => {
          this.scrollTop = this.scroll!.nativeElement.scrollHeight + 100;
        }, 50);
      }
    );
  }

  private subscribeToUserStateEvent() {
    this.userStateSubscription = this.websocketService.userState$.subscribe((data: { user: string, content: string, state: UserState }) => {
      const {user} = this.receiver!;
      if(this.receiver && user && user.login) {
        if(user.login === data.user) {
          this.receiver.state = data.state;
        }
      }
    });
  }

  getConversationHeaderName(): string | null | undefined {
    const {name} = this.room!;
    if(this.type === 'GROUP') {
      return name ? name : 'Room'
    }else {
      const {user} = this.receiver!;
      return user.login ? user.login : 'Unknown';
    }
  }

  makeCall(): void {
    this.room ? this.chatCallNotificationService.makeCall(this.room, this.getConversationHeaderName()!) : null;
  }

  loadMessages(): Observable<any[]> {
    if (this.room) {
      this.isLoading = true;
      const {id} = this.room;
      return this.conversationService.loadAllMessagesByRoom(id!, {page: this.page, size: this.size}).pipe(
        map((res: any) => {
          const conversations: IConversation[] = res.body?.content;
          this.totalPages = res.body?.totalPages || 1;
          return conversations.reverse()
            .map((conversation) => {
              return {
                user: conversation.sender,
                message: conversation.content,
                timestamp: conversation.createdDate,
              }
            });
        }),
        finalize(() => {
          this.isLoading = false;
          this.page++;
          setTimeout(() => {
            this.scrollTop = this.scroll!.nativeElement.scrollHeight + 100;
          }, 50);
        })
      );
    }
    return EMPTY;
  }

  getStateClass(type: 'bg' | 'text'): string {
    const {state} = this.receiver!;
    if(state) {
      return this.userStateService.getStateClass(type, state);
    }
    return 'dark';
  }

  sendMessage(): void {
    if (!(this.message && this.message.trim().length)) {
      return;
    }
    if (!(this.room && this.room.id)) {
      return;
    }
    const {id} = this.room;
    let metadata = new Map<string, string>();
    metadata.set("MESSAGE", this.message);
    metadata.set("ROOM", String(id));
    this.sendNotification(metadata);
  }

  sendNotification(metadata: any): void {
    if (this.sender) {
      this.chatCallNotificationService.sendNotificationTo(metadata, null, NotificationType.INCOMING_MESSAGE);
      this.conversations.push({user: this.sender, message: this.message, timestamp: new Date()});
      setTimeout(() => {
        this.scrollTop = this.scroll!.nativeElement.scrollHeight + 100;
      }, 50);
    }
    this.message = '';
  }

  isMessageSentByLoggedUser(user: IUser): boolean {
    return this.sender?.login === user?.login;
  }

  onScrollUp() {
    if (this.page >= this.totalPages) {
      return;
    }
    this.conversationsSubscription = this.loadMessages()
      .subscribe((res) => {
        this.conversations = [...res, ...this.conversations];
      });
  }

  isCallNotAllowed(): boolean {
    if(this.type === 'PRIVATE') {
      const {state} = this.receiver!;
      if(this.receiver && state) {
        if(state == UserState.ONLINE || state == UserState.AWAY) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
    this.conversationsSubscription?.unsubscribe();
    this.userStateSubscription?.unsubscribe();
  }
}

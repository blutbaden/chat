import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {GroupService} from "../../../../pages/group/group.service";
import {HttpResponse} from "@angular/common/http";
import {IGroup} from "../../../../models/group.model";
import {finalize, map} from "rxjs/operators";
import {RoomService} from "../../room/room.service";
import {IRoom} from "../../../../models/room.model";
import {Observable, Subscription} from "rxjs";
import {ConversationService} from "../../../services/conversation.service";
import {WebsocketService} from "../../../services/websocket.service";
import {RoomUpdateDialogComponent} from "../../room/update/room-update-dialog.component";
import {NgbModal, NgbPanelChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {IUser} from "../../../../models/user.model";

@Component({
  selector: 'app-chat-list-group',
  templateUrl: './chat-list-group.component.html',
  styleUrls: ['./chat-list-group.component.scss']
})
export class ChatListGroupComponent implements OnInit, OnDestroy {

  @Input('loggedUser') loggedUser?: IUser;
  @Output() selectedGroup = new EventEmitter<any>();
  @Output() selectedRoom = new EventEmitter<any>();

  query?: string;
  isLoadingRooms: boolean = false;

  groups$: Observable<IGroup[]> | undefined;
  roomsData: { room: IRoom, count: number }[] | undefined;

  unreadConversationCountsByRoom: Map<number, number> = new Map<number, number>();

  messageSubscription: Subscription | null = null;
  roomSubscription: Subscription | null = null;

  constructor(
    private groupService: GroupService,
    private roomService: RoomService,
    private conversationService: ConversationService,
    private websocketService: WebsocketService,
    private modalService: NgbModal,
  ) {
  }

  ngOnInit(): void {
    // get all groups joined by logged user
    this.getGroups();
    // get count delivered messages by receiver is logged user and grouped by sender (user) => /count
    this.getCurrentUserConversationsCountByStateGroupedByRoom().subscribe(result => {
      this.unreadConversationCountsByRoom = result;
    });
    this.messageSubscription = this.websocketService.message$.subscribe(({user, message, room}) => {
      if(this.roomsData) {
        const index = this.roomsData.findIndex(item => item.room.id == room);
        if (index !== -1) {
          this.roomsData[index].count++;
        }
      }
    });
  }

  createRoom(group: IGroup): void {
    const modalRef = this.modalService.open(RoomUpdateDialogComponent, {size: 'lg', backdrop: 'static'});
    modalRef.componentInstance.group = group;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed.subscribe(reason => {
      if (reason === 'created') {}
    });
  }

  handleRoomAction(group: IGroup, room?: IRoom): void {
    const modalRef = this.modalService.open(RoomUpdateDialogComponent, {size: 'lg', backdrop: 'static'});
    modalRef.componentInstance.group = group;
    if(room && room.id) {
      modalRef.componentInstance.roomId = room.id;
    }
    modalRef.closed.subscribe((reason: string) => {
      if (reason === 'created') {}
      if (reason === 'updated') {}
    });
  }

  onPanelChange(event: NgbPanelChangeEvent) {
    if(event.nextState) {
      this.isLoadingRooms = true;
      this.roomsData = undefined;
    }
  }


  deleteRoom(room: IRoom): void {
    if(room && room.id) {
      this.roomService.delete(room.id!).subscribe();
    }
  }

  isRoomCreatedByCurrentUser(room: IRoom): boolean {
    if(room && room.createdBy) {
      return room.createdBy === this.loggedUser?.login;
    }
    return false;
  }

  onShowPanel(group: IGroup) {
    // get rooms
    if (group && group.id) {
      this.isLoadingRooms = true;
      this.selectedGroup.next(group);
      this.roomSubscription = this.roomService.findRoomsInGroupJoinedByLoggedUser(group?.id).pipe(
        map((res: HttpResponse<IRoom[]>) => {
          return (res.body ?? []).map((room: IRoom) => {
            return {
              room: room,
              count: this.unreadConversationCountsByRoom?.get(room.id!) || 0
            }
          });
        }),
        finalize(() => this.isLoadingRooms = false)
      ).subscribe(data => {
        this.roomsData = data;
      });
    }
  }

  onSelectRoom(room: IRoom) {
    this.conversationService.updateConversationStateByRoom(room.id!).subscribe(
      () => this.selectedRoom.next(room)
    );
  }

  private getGroups(): void {
    this.groups$ = this.groupService.findByLoggedUser().pipe(
      map((res: HttpResponse<IGroup[]>) => {
        return res.body ?? [];
      }),
    );
  }

  private getCurrentUserConversationsCountByStateGroupedByRoom(): Observable<Map<number, number>> {
    return this.conversationService.getCurrentUserConversationsCountByStateGroupedByRoom().pipe(
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

  isCreatedByCurrentUser(room: IRoom): boolean {
    if(room && room.createdBy) {
      return room.createdBy === this.loggedUser?.login;
    }
    return false;
  }

  ngOnDestroy(): void {
    this.messageSubscription?.unsubscribe();
    this.roomSubscription?.unsubscribe();
  }
}

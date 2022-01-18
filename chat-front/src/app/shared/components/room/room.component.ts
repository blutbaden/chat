import {Component, Input, OnInit} from '@angular/core';
import {HttpResponse} from '@angular/common/http';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {IRoom} from "../../../models/room.model";
import {RoomService} from "./room.service";
import {RoomDeleteDialogComponent} from "./delete/room-delete-dialog.component";
import {RoomUpdateDialogComponent} from "./update/room-update-dialog.component";
import {IGroup} from "../../../models/group.model";

@Component({
  selector: 'app-room',
  templateUrl: './room.component.html',
})
export class RoomComponent implements OnInit {

  @Input('group') group?: IGroup;

  rooms?: IRoom[] = [];
  isLoading = false;

  constructor(
    protected roomService: RoomService,
      protected modalService: NgbModal
  ) {
  }

  loadAll(): void {
    this.isLoading = true;
    if(this.group && this.group.id) {
      this.roomService.findRoomsInGroupJoinedByLoggedUser(this.group?.id).subscribe(
        (res: HttpResponse<IRoom[]>) => {
          this.isLoading = false;
          this.rooms = res.body ?? [];
        },
        () => {
          this.isLoading = false;
        }
      );
    }
  }

  ngOnInit(): void {
    this.loadAll();
  }

  trackId(index: number, item: IRoom): number {
    return item.id!;
  }

  create(): void {
    const modalRef = this.modalService.open(RoomUpdateDialogComponent, {size: 'lg', backdrop: 'static'});
    modalRef.componentInstance.group = this.group;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed.subscribe(reason => {
      if (reason === 'created') {
        this.loadAll();
      }
    });
  }

  update(roomId: number): void {
    const modalRef = this.modalService.open(RoomUpdateDialogComponent, {size: 'lg', backdrop: 'static'});
    modalRef.componentInstance.group = this.group;
    modalRef.componentInstance.roomId = roomId;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed.subscribe(reason => {
      if (reason === 'updated') {
        this.loadAll();
      }
    });
  }

  delete(room: IRoom): void {
    const modalRef = this.modalService.open(RoomDeleteDialogComponent, {size: 'lg', backdrop: 'static'});
    modalRef.componentInstance.room = room;
    // unsubscribe not needed because closed completes on modal close
    modalRef.closed.subscribe(reason => {
      if (reason === 'deleted') {
        this.loadAll();
      }
    });
  }
}

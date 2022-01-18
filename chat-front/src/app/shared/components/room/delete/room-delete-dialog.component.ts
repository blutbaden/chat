import {Component} from '@angular/core';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {IRoom} from "../../../../models/room.model";
import {RoomService} from "../room.service";

@Component({
  templateUrl: './room-delete-dialog.component.html',
})
export class RoomDeleteDialogComponent {
  room?: IRoom;

  constructor(
    protected roomService: RoomService,
    protected activeModal: NgbActiveModal
  ) {
  }

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.roomService.delete(id).subscribe(() => {
      this.activeModal.close('deleted');
    });
  }
}

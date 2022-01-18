import { Component } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import {IGroup} from "../../../models/group.model";
import {GroupService} from "../group.service";


@Component({
  templateUrl: './group-delete-dialog.component.html',
})
export class GroupDeleteDialogComponent {
  group?: IGroup;

  constructor(protected groupService: GroupService, protected activeModal: NgbActiveModal) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(group: IGroup): void {
    const {id} = group;
    if(id) {
      this.groupService.delete(id).subscribe(() => {
        this.activeModal.close('deleted');
      });
    }
  }
}

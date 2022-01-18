import {Component, OnInit} from '@angular/core';
import {HttpResponse} from '@angular/common/http';
import {FormBuilder} from '@angular/forms';
import {ActivatedRoute} from '@angular/router';
import {EMPTY, mergeMap, Observable, of} from 'rxjs';
import {finalize, map} from 'rxjs/operators';
import {IUser} from "../../../../models/user.model";
import {RoomService} from "../room.service";
import {UserService} from "../../../services/user.service";
import {IRoom, Room} from "../../../../models/room.model";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {IGroup} from "../../../../models/group.model";
import {GroupService} from "../../../../pages/group/group.service";

@Component({
  selector: 'jhi-room-update',
  templateUrl: './room-update-dialog.component.html',
})
export class RoomUpdateDialogComponent implements OnInit {
  isSaving = false;

  group?: IGroup;

  roomId?: number;

  usersSharedCollection: IUser[] = [];

  editForm = this.fb.group({
    id: [],
    name: [],
    isActivated: [],
    allowImageMessage: [],
    allowVoiceMessage: [],
    allowStickerMessage: [],
    users: [],
  });

  constructor(
    protected roomService: RoomService,
    protected userService: UserService,
    protected groupService: GroupService,
    protected activatedRoute: ActivatedRoute,
    protected fb: FormBuilder,
    protected activeModal: NgbActiveModal
  ) {
  }

  ngOnInit(): void {
    if(this.roomId) {
      this.getRoom(this.roomId).pipe().subscribe(room => {
        this.updateForm(room);
      });
    }
    this.loadRelationshipsOptions();
  }

  getRoom(id: number): Observable<Room> {
    if (id) {
      return this.roomService.find(id).pipe(
        mergeMap((room: HttpResponse<Room>) => {
          if (room.body) {
            return of(room.body);
          } else {
            return EMPTY;
          }
        })
      );
    }
    return EMPTY;
  }

  cancel(): void {
    this.activeModal.dismiss();
  }

  save(): void {
    this.isSaving = true;
    const room = this.createFromForm();
    if (room.id) {
      this.subscribeToSaveResponse(this.roomService.update(room));
    } else {
      this.subscribeToSaveResponse(this.roomService.create(room));
    }
  }

  trackUserById(index: number, item: IUser): number {
    return item.id!;
  }

  getSelectedUser(option: IUser, selectedVals?: IUser[]): IUser {
    if (selectedVals) {
      for (const selectedVal of selectedVals) {
        if (option.id === selectedVal.id) {
          return selectedVal;
        }
      }
    }
    return option;
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IRoom>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe(
      () => this.onSaveSuccess(),
      () => this.onSaveError()
    );
  }

  protected onSaveSuccess(): void {
    this.activeModal.close('updated');
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(room: IRoom): void {
    this.editForm.patchValue({
      id: room.id,
      name: room.name,
      isActivated: room.isActivated,
      allowImageMessage: room.allowImageMessage,
      allowVoiceMessage: room.allowVoiceMessage,
      allowStickerMessage: room.allowStickerMessage,
      users: room.users,
      group: room.group,
    });

    this.usersSharedCollection = this.userService.addUserToCollectionIfMissing(this.usersSharedCollection, ...(room.users ?? []));
  }

  protected loadRelationshipsOptions(): void {
    if(this.group && this.group.id) {
      this.groupService
        .getUsersByGroup(this.group.id)
        .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
        .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing(users, ...(this.editForm.get('users')!.value ?? []))))
        .subscribe((users: IUser[]) => (this.usersSharedCollection = users));
    }
  }

  protected createFromForm(): IRoom {
    return {
      ...new Room(),
      id: this.editForm.get(['id'])!.value,
      name: this.editForm.get(['name'])!.value,
      isActivated: this.editForm.get(['isActivated'])!.value,
      allowImageMessage: this.editForm.get(['allowImageMessage'])!.value,
      allowVoiceMessage: this.editForm.get(['allowVoiceMessage'])!.value,
      allowStickerMessage: this.editForm.get(['allowStickerMessage'])!.value,
      users: this.editForm.get(['users'])!.value,
      group: this.group
    };
  }
}

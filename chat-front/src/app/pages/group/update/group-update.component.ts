import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import {EMPTY, mergeMap, Observable} from 'rxjs';
import { finalize, map } from 'rxjs/operators';
import {IUser} from "../../../models/user.model";
import {GroupService} from "../group.service";
import {Group, IGroup} from "../../../models/group.model";
import {UserService} from "../../../shared/services/user.service";
import {RoomService} from "../../../shared/components/room/room.service";
import {IRoom} from "../../../models/room.model";

@Component({
  selector: 'jhi-group-update',
  templateUrl: './group-update.component.html',
})
export class GroupUpdateComponent implements OnInit {
  isSaving = false;

  usersSharedCollection: IUser[] = [];

  group?: IGroup;

  editForm = this.fb.group({
    id: [],
    name: [],
    isActivated: [],
    users: [],
  });
  active = 1;

  constructor(
    protected groupService: GroupService,
    protected roomService: RoomService,
    protected userService: UserService,
    protected activatedRoute: ActivatedRoute,
    protected fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ group }) => {
      this.group = group;
      this.updateForm(group);
      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const group = this.createFromForm();
    if (group.id !== undefined) {
      this.subscribeToSaveResponse(this.groupService.update(group));
    } else {
      this.groupService.create(group)
        .pipe(
          map(res => res.body),
          mergeMap(group => {
            if(group) {
              const room: IRoom = {group: {id: group.id}}
              return this.roomService.createFirstRoomInGroup(room);
            }
            return EMPTY;
          }),
          finalize(() => this.onSaveFinalize())).subscribe(
        (res) => this.previousState(),
        () => this.onSaveError()
      );
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

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IGroup>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe(
      () => this.onSaveSuccess(),
      () => this.onSaveError()
    );
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(group: IGroup): void {
    this.editForm.patchValue({
      id: group.id,
      name: group.name,
      isActivated: group.isActivated,
      users: group.users,
    });

    this.usersSharedCollection = this.userService.addUserToCollectionIfMissing(this.usersSharedCollection, ...(group.users ?? []));
  }

  protected loadRelationshipsOptions(): void {
    this.userService
      .query()
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing(users, ...(this.editForm.get('users')!.value ?? []))))
      .subscribe((users: IUser[]) => {
        this.usersSharedCollection = users
      });
  }

  protected createFromForm(): IGroup {
    return {
      ...new Group(),
      id: this.editForm.get(['id'])!.value,
      name: this.editForm.get(['name'])!.value,
      isActivated: this.editForm.get(['isActivated'])!.value,
      users: this.editForm.get(['users'])!.value,
    };
  }
}

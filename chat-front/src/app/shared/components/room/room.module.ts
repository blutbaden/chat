import { NgModule } from '@angular/core';
import { RoomDeleteDialogComponent } from './delete/room-delete-dialog.component';
import {CommonModule} from "@angular/common";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {RoomComponent} from "./room.component";
import {RoomUpdateDialogComponent} from "./update/room-update-dialog.component";

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
  ],
  declarations: [
    RoomComponent,
    RoomUpdateDialogComponent,
    RoomDeleteDialogComponent
  ],
  entryComponents: [RoomDeleteDialogComponent],
  exports: [
    RoomComponent
  ]
})
export class RoomModule {}

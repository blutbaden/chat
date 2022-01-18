import {NgModule} from '@angular/core';
import {GroupDeleteDialogComponent} from './delete/group-delete-dialog.component';
import {SharedModule} from "../../shared/shared.module";
import {GroupComponent} from "./group.component";
import {RouterModule} from "@angular/router";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {GroupUpdateComponent} from "./update/group-update.component";
import {GroupRoutingModule} from "./group-routing.module";
import {CommonModule} from "@angular/common";
import {NgbNavModule} from "@ng-bootstrap/ng-bootstrap";

@NgModule({
  imports: [
    SharedModule,
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    GroupRoutingModule,
    NgbNavModule
  ],
  declarations: [
    GroupComponent,
    GroupUpdateComponent,
    GroupDeleteDialogComponent
  ],
  entryComponents: [GroupDeleteDialogComponent],
})
export class GroupModule {
}

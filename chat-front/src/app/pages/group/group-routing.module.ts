import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import {GroupComponent} from "./group.component";
import {GroupUpdateComponent} from "./update/group-update.component";
import {GroupRoutingResolveService} from "./group-routing-resolve.service";

const groupRoute: Routes = [
  {
    path: '',
    component: GroupComponent,
  },
  {
    path: 'new',
    component: GroupUpdateComponent,
    resolve: {
      group: GroupRoutingResolveService,
    },
  },
  {
    path: ':id/edit',
    component: GroupUpdateComponent,
    resolve: {
      group: GroupRoutingResolveService,
    },
  },
];

@NgModule({
  imports: [RouterModule.forChild(groupRoute)],
  exports: [RouterModule],
})
export class GroupRoutingModule {}

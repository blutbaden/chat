import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {RegisterComponent} from './pages/register/register.component';
import {LoginComponent} from './pages/login/login.component';
import {HomeComponent} from './pages/home/home.component';
import {UserRouteAccessService} from './core/auth/user-route-access.service';
import {GroupModule} from "./pages/group/group.module";

const routes: Routes = [
  {
    path: 'home',
    component: HomeComponent,
    canActivate: [UserRouteAccessService]
  },
  {
    path: 'login',
    component: LoginComponent
  },
  {
    path: 'register',
    component: RegisterComponent
  },
  {
    path: 'group',
    loadChildren: () => import('./pages/group/group.module').then(m => m.GroupModule),
    canActivate: [UserRouteAccessService]
  },
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}

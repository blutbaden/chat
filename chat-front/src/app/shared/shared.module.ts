import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {HasAnyAuthorityDirective} from './directives/has-any-authority.directive';
import {RoomModule} from "./components/room/room.module";
import {CustomSpinnerComponent} from './components/custom-spinner/custom-spinner.component';


@NgModule({
  imports: [
    FormsModule,
    CommonModule,
    ReactiveFormsModule,
  ],
  declarations: [
    HasAnyAuthorityDirective,
    CustomSpinnerComponent,
  ],
  exports: [
    RoomModule,
    HasAnyAuthorityDirective,
    CustomSpinnerComponent,
  ],
})
export class SharedModule {
}

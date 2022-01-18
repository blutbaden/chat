import {NgModule} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {NgbAccordionModule, NgbCollapseModule, NgbDropdownModule, NgbNavModule} from '@ng-bootstrap/ng-bootstrap';
import {ChatConversationComponent} from './chat-conversation/chat-conversation.component';
import {ChatListUserComponent} from './chat-list-user/chat-list-user.component';
import {ChatComponent} from './chat.component';
import {ChatCallModalComponent} from './chat-call-modal/chat-call-modal.component';
import {ChatListGroupComponent} from './chat-list-group/chat-list-group.component';
import {AvatarModule} from 'ngx-avatar';
import {SearchFilterPipe} from "../../pipes/search-filter.pipe";
import {ChatEmojiComponent} from './chat-emoji/chat-emoji.component';
import {SharedModule} from "../../shared.module";
import {InfiniteScrollModule} from "ngx-infinite-scroll";
import {DateAgoPipe} from "../../pipes/date-ago.pipe";
import { ChatUserStateComponent } from './chat-user-state/chat-user-state.component';

@NgModule({
  imports: [
    FormsModule,
    CommonModule,
    ReactiveFormsModule,
    NgbNavModule,
    AvatarModule,
    NgbAccordionModule,
    NgbCollapseModule,
    InfiniteScrollModule,
    SharedModule,
    NgbDropdownModule,
  ],
  declarations: [
    ChatConversationComponent,
    ChatListUserComponent,
    ChatComponent,
    ChatCallModalComponent,
    ChatListGroupComponent,
    SearchFilterPipe,
    DateAgoPipe,
    ChatEmojiComponent,
    ChatUserStateComponent,
  ],
  exports: [
    ChatComponent,
    ChatUserStateComponent
  ],
})
export class ChatModule {
}

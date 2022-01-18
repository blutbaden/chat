import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {UserState} from "../../../../models/user-state.model";
import {UserStateService} from "../../../services/user-state.service";

@Component({
  selector: 'app-chat-user-state',
  templateUrl: './chat-user-state.component.html',
  styleUrls: ['./chat-user-state.component.scss']
})
export class ChatUserStateComponent implements OnInit {

  @Input('userImage') userImage: string = '../../../../../assets/img/default-user.jpg';
  @Output() stateUpdated = new EventEmitter<UserState>();

  state: UserState = UserState.ONLINE;

  constructor(
    private userStateService: UserStateService
  ) {
  }

  public get userState(): typeof UserState {
    return UserState
  }

  ngOnInit(): void {
    const state = this.userStateService.getStoredState();
    if (state) {
      this.state = state;
      this.userStateService.updateState(state);
    }
    this.userStateService.stateChanged$.subscribe(state => {
      this.state = state;
    });
  }

  onChangeState(state: UserState): void {
    this.state = state;
    this.stateUpdated.next(state);
    this.userStateService.updateState(state);
  }

  getCurrentStateClass() {
    let defaultClass = '';
    switch (this.state) {
      case UserState.ONLINE:
        defaultClass = 'bg-success';
        break;
      case UserState.OFFLINE:
        defaultClass = 'bg-secondary';
        break;
      case UserState.BUSY:
        defaultClass = 'bg-danger';
        break;
      case UserState.AWAY:
        defaultClass = 'bg-warning';
        break;
      default:
        defaultClass = 'bg-secondary';
    }
    return defaultClass;
  }

}

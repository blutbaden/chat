import {Component, OnDestroy, OnInit} from '@angular/core';
import {WebsocketService} from '../../shared/services/websocket.service';
import {UserState} from "../../models/user-state.model";
import {UserStateService} from "../../shared/services/user-state.service";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit, OnDestroy {

  state: UserState = UserState.ONLINE;

  constructor(
    private websocketService: WebsocketService,
    private userStateService: UserStateService
  ) {
  }

  public get userState(): typeof UserState {
    return UserState
  }

  ngOnInit(): void {
  }

  connect(): void {
    this.websocketService.connect();
  }

  disconnect(): void {
    this.websocketService.disconnect();
  }

  changeStatus(): void {
    this.userStateService.updateState(this.state);
  }

  ngOnDestroy(): void {
    this.websocketService.disconnect();
  }
}

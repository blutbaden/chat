import {Component, OnInit} from '@angular/core';
import {AccountService} from './core/auth/account.service';
import {AuthServerProvider} from './core/auth/auth-jwt.service';
import {ToastrService} from "ngx-toastr";
import {WebsocketService} from "./shared/services/websocket.service";
import {map, mergeMap} from "rxjs/operators";
import {HttpResponse} from "@angular/common/http";
import {IRoom} from "./models/room.model";
import {RoomService} from "./shared/components/room/room.service";
import {Observable} from "rxjs";
import {UserState} from "./models/user-state.model";
import {DEFAULT_INTERRUPTSOURCES, Idle} from "@ng-idle/core";
import {Keepalive} from "@ng-idle/keepalive";
import {UserStateService} from "./shared/services/user-state.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  showChatPanel: boolean = false;
  loggedUser?: string;

  constructor(
    private accountService: AccountService,
    private authServerProvider: AuthServerProvider,
    private toastrService: ToastrService,
    private websocketService: WebsocketService,
    private roomService: RoomService,
    private idle: Idle,
    private userStateService: UserStateService
  ) {
    idle.setIdle(300); // how long can they be inactive before considered idle, in seconds
    idle.setTimeout(300); // how long can they be idle before considered timed out, in seconds
    idle.setInterrupts(DEFAULT_INTERRUPTSOURCES);
    idle.onIdleStart.subscribe(() => { // do something when the user becomes idle
      this.userStateService.updateState(UserState.AWAY);
    });
    idle.onIdleEnd.subscribe(() => { // do something when the user is no longer idle
      // get last state from storage
      const storedState = this.userStateService.getStoredState();
      this.userStateService.updateState(storedState);
    });
  }

  ngOnInit(): void {
    this.accountService
      .getAuthenticationState()
      .pipe(
        map(account => {
          return account?.login
        }),
      ).subscribe((loggedUser: string | undefined) => {
      this.idle.watch();
      this.websocketService.subscribeToTopic(loggedUser!);
    });
    this.websocketService.notification$.subscribe((content: string) => {
      this.toastrService.show(content);
    })
  }

  isAuthenticated(): boolean {
    return this.accountService.isAuthenticated();
  }

  logout(): void {
    this.authServerProvider.logout()
      .subscribe({
        complete: () => {
          this.accountService.authenticate(null);
          this.websocketService.disconnect();
        }
      });
  }

  /*getPublicRoomsByCurrentUser(username: string | undefined): Observable<any> {
    this.loggedUser = username;
    return this.roomService.findPublicByLoggedUser().pipe(
      map((res: HttpResponse<IRoom[]>) => {
        return {username, rooms: res.body ?? []}
      })
    );
  }*/
}

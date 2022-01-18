import {Injectable, OnDestroy} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {takeUntil} from "rxjs/operators";
import {UserState} from "../../models/user-state.model";
import {WebsocketService} from "./websocket.service";

@Injectable({
  providedIn: 'root'
})

export class UserStateService implements OnDestroy {

  constructor(private websocketService: WebsocketService) {
  }

  private destroy$: Subject<void> = new Subject();

  private stateChangedSubject$: Subject<UserState> = new Subject();
  stateChanged$: Observable<UserState> = this.stateChangedSubject$.pipe(takeUntil(this.destroy$));

  updateState(state: UserState): void {
    if(state !== UserState.AWAY) {
      sessionStorage.setItem("userState", state);
      localStorage.setItem("userState", state);
    }
    this.stateChangedSubject$.next(state);
    setTimeout(() => {
      this.websocketService.sendRequestToUpdateUserStatus(state);
    }, 500);
  }

  getStoredState(): UserState {
    const stateInLocalStorage: string | null = localStorage.getItem('userState');
    const stateInSessionStorage: string | null = sessionStorage.getItem('userState');
    return stateInLocalStorage as UserState ?? stateInSessionStorage as UserState ?? UserState.ONLINE;
  }

  getStateClass(type: 'bg' | 'text', state: UserState): string {
    let defaultClass = '';
    switch (state) {
      case UserState.ONLINE:
        defaultClass = `${type}-success`;
        break;
      case UserState.OFFLINE:
        defaultClass = `${type}-secondary`;
        break;
      case UserState.BUSY:
        defaultClass = `${type}-danger`;
        break;
      case UserState.AWAY:
        defaultClass = `${type}-warning`;
        break;
      default:
        defaultClass = `${type}-secondary`;
    }
    return defaultClass;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

}

import {Injectable, OnDestroy} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {takeUntil} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})

export class TimerService implements OnDestroy {

  maxTimeInSeconds: number = 30;
  timeLeftInSeconds: number = 0;
  intervalInSeconds: number = 1;

  timer: any;

  private destroy$: Subject<void> = new Subject();

  private timerEndSubject$: Subject<boolean> = new Subject();
  timerEnd$: Observable<boolean> = this.timerEndSubject$.pipe(takeUntil(this.destroy$));


  startTimer() {
    this.timer = setInterval(() => {
      if (this.timeLeftInSeconds < this.maxTimeInSeconds) {
        this.timeLeftInSeconds++;
      } else {
        this.resetTimer();
        this.timerEndSubject$.next(true);
      }
    }, this.intervalInSeconds * 1000);
  }

  resetTimer() {
    clearInterval(this.timer);
    this.timeLeftInSeconds = 0;
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

}

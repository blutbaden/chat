import {Injectable, OnDestroy} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ModalService implements OnDestroy {

  private destroy$: Subject<void> = new Subject();

  // ref is the reference of modal

  private showModalSubject$: Subject<{ show: boolean, ref: string, metadata?: Map<string, any> }> = new Subject();
  showModal$: Observable<{ show: boolean, ref: string, metadata?: Map<string, any> }> = this.showModalSubject$.pipe(takeUntil(this.destroy$));

  constructor() {
  }

  showModal(show: boolean, ref: string, metadata?: Map<string, any>): void {
      this.showModalSubject$.next({show, ref, metadata});
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

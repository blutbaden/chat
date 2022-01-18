import {Injectable} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {WebsocketService} from './websocket.service';
import {CallState} from '../../models/call-state.model';
import {NotificationType} from '../../models/notification.model';
import {IRoom} from "../../models/room.model";
import {TimerService} from "./timer.service";
import {ModalService} from "./modal.service";

const CALL_MODAL_REF = "CALL_MODAL";

@Injectable({
  providedIn: 'root'
})
export class ChatCallNotificationService {

  private onCallActionSubject$: Subject<{ show: boolean, state: CallState }> = new Subject();
  readonly onCallAction$: Observable<{ show: boolean, state: CallState }> = this.onCallActionSubject$.asObservable();

  constructor(
    private websocketService: WebsocketService,
    private modalService: ModalService,
    private timerService: TimerService,
  ) {
  }

  makeCall(room: IRoom, name: string): void {
    const {id} = room!;
    let metadata = new Map<string, any>();
    metadata.set("STATE", CallState.OUTGOING_CALL);
    metadata.set("NAME", name);
    if (room && id) {
      this.modalService.showModal(true, CALL_MODAL_REF, metadata);
      this.interceptCall(CallState.INCOMING_CALL, String(id));
      this.timerService.startTimer();
    }
  }

  interceptCall(callState: string, room: string): void {
    let metadata = new Map<string, string>();
    metadata.set("ROOM", room);
    switch (callState) {
      case CallState.CANCELLED_CALL:
        this.sendNotificationTo(metadata, null, NotificationType.CANCELLED_CALL);
        break;
      case CallState.ACCEPTED_CALL:
        this.sendNotificationTo(metadata, null, NotificationType.ACCEPTED_CALL);
        break;
      case CallState.REJECTED_CALL:
        this.sendNotificationTo(metadata, null, NotificationType.REJECTED_CALL);
        break;
      case CallState.INCOMING_CALL:
        this.sendNotificationTo(metadata, null, NotificationType.INCOMING_CALL);
    }
  }

  sendNotificationTo(metadata: Map<string, string>, content: string | null, type: NotificationType): void {
    if (metadata) {
      let jsonObject: any = {};
      metadata.forEach((value, key) => {
        jsonObject[key] = value
      });
      this.websocketService.sendNotification(
        '/chat',
        {
          content: content,
          type,
          metadata: JSON.stringify(jsonObject),
        }
      );
    }
  }
}

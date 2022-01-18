import {Component, OnInit} from '@angular/core';
import {ChatCallNotificationService} from '../../services/chat-call-notification.service';
import {CallState} from '../../../models/call-state.model';
import {WebsocketService} from '../../services/websocket.service';
import {ModalService} from "../../services/modal.service";
import {JitsiService} from "../../services/jitsi.service";
import {AccountService} from "../../../core/auth/account.service";
import {Account} from "../../../core/auth/account.model";
import {map} from "rxjs/operators";
import {Observable} from "rxjs";
import {IRoom} from "../../../models/room.model";
import {IUser} from "../../../models/user.model";
import {TimerService} from "../../services/timer.service";
import {UserState} from "../../../models/user-state.model";

const CALL_MODAL_REF = "CALL_MODAL";

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss']
})
export class ChatComponent implements OnInit {

  isConversation = false;
  activeTab = 1;
  selectedRoom?: IRoom;
  account?: Account | null;
  sender?: IUser;
  receiver?: { user: IUser, state: UserState };
  roomId?: string;
  audio: HTMLAudioElement;

  constructor(
    private chatCallNotificationService: ChatCallNotificationService,
    private websocketService: WebsocketService,
    private modalService: ModalService,
    private jitsiService: JitsiService,
    private accountService: AccountService,
    private timerService: TimerService
  ) {
    this.audio = new Audio();
  }

  public get callState(): typeof CallState {
    return CallState;
  }

  ngOnInit(): void {
    this.getCurrentUser().subscribe(user => this.sender = user);
    this.jitsiService.participantHangUp$.subscribe(() => this.modalService.showModal(false, CALL_MODAL_REF));
    this.websocketService.callState$.subscribe(
      (data: { room: string, user: IUser, content: string, state: CallState }) => {
        let metadata = new Map<string, any>();
        metadata.set("STATE", data.state);
        metadata.set("NAME", data.user.login);
        switch (data.state) {
          case CallState.INCOMING_CALL:
            this.modalService.showModal(true, CALL_MODAL_REF, metadata);
            this.roomId = data.room;
            this.playAudio(this.audio);
            break;
          case CallState.ACCEPTED_CALL:
            metadata.set("CALL_ACCEPTED", true);
            this.modalService.showModal(true, CALL_MODAL_REF, metadata);
            this.timerService.resetTimer();
            const {login} = this.sender!;
            login ? setTimeout(() => this.jitsiService.createMeet(login, data.room), 100) : null;
            break;
          default:
            this.stopAudio(this.audio);
            this.timerService.resetTimer();
            this.modalService.showModal(false, CALL_MODAL_REF);
        }
      }
    );
    this.timerService.timerEnd$.subscribe(() => {
      if (this.roomId) { // if timer is ended then cancel call
        this.chatCallNotificationService.interceptCall(CallState.CANCELLED_CALL, this.roomId);
        this.modalService.showModal(false, CALL_MODAL_REF);
      }
    });
  }

  getCurrentUser(): Observable<any> {
    return this.accountService.getAuthenticationState().pipe(
      map((account) => {
        return {
          id: null,
          login: account?.login
        };
      }));
  }

  playAudio(audio: HTMLAudioElement): void {
    audio.loop = true;
    audio.src = "../../../../assets/audio/phone.wav";
    audio.load();
    audio.play();
  }

  stopAudio(audio: HTMLAudioElement): void {
    audio.pause();
    audio.currentTime = 0;
  }

  onSelectRoom(room: IRoom) {
    this.isConversation = true;
    this.selectedRoom = room;
    const {id} = this.selectedRoom!;
    this.roomId = String(id);
  }

  handleCallAction(callState: string): void {
    this.stopAudio(this.audio);
    this.timerService.resetTimer();
    if (this.roomId) {
      const {login} = this.sender!;
      this.chatCallNotificationService.interceptCall(callState, this.roomId);
      login && (callState === CallState.ACCEPTED_CALL) ?
        this.jitsiService.createMeet(login, this.roomId) : null;
    }
  }
}

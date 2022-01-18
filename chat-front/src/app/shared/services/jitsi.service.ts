import {Injectable} from '@angular/core';
import {environment} from '../../../environments/environment';
import {Observable, Subject} from 'rxjs';
import '../../../assets/vendor/jitsi/external_api.js';
import {ModalService} from "./modal.service";

/**
 * Import for native js libraries
 */
declare var JitsiMeetExternalAPI: any;

@Injectable({
  providedIn: 'root'
})

export class JitsiService {

  api: any;

  modalRef = "CALL_MODAL";

  options = {
    roomName: null,
    height: 500,
    configOverwrite: {
      prejoinPageEnabled: false
    },
    interfaceConfigOverwrite: {
      // overwrite interface properties
    },
    featureFlags: {
      closeCaptionsEnabled: false,
      calendarEnabled: false,
      callIntegration: true,
      chatEnabled: false,
      inviteEnabled: true,
      iosRecordingEnabled: false,
      pipEnabled: false,
      welcomePageEnabled: false
    },
    parentNode: null,
    userInfo: {
      displayName: null
    }
  };

  private participantLeftSubject$: Subject<any> = new Subject();
  readonly participantLeft$: Observable<any> = this.participantLeftSubject$.asObservable();

  private participantJoinedSubject$: Subject<any> = new Subject();
  readonly participantJoined$: Observable<any> = this.participantJoinedSubject$.asObservable();

  private participantHangUpSubject$: Subject<boolean> = new Subject();
  readonly participantHangUp$: Observable<boolean> = this.participantHangUpSubject$.asObservable();

  constructor(
    protected modalService: ModalService
  ) {
  }


  createMeet = (user: string, room: string) => {
    try {
      this.api = new JitsiMeetExternalAPI(environment.jitsiDomain, this.setMeetOptions(user, room));
      this.api.addEventListeners({
        participantLeft: this.handleParticipantLeft,
        participantJoined: this.handleParticipantJoined,
        videoConferenceJoined: this.handleVideoConferenceJoined,
        videoConferenceLeft: this.handleVideoConferenceLeft,
      });
    } catch (e) {
      let metadata = new Map<string, any>();
      metadata.set("CALL_ERROR", true);
      this.modalService.showModal(true, this.modalRef, metadata);
    }
  }

  private setMeetOptions = (user: any, room: any, jitsiNodeId: string = 'jitsi-iframe') => {
    this.options['roomName'] = room;
    this.options['parentNode'] = document.querySelector(`#${jitsiNodeId}`) as any;
    this.options.userInfo['displayName'] = user;
    return this.options;
  }

  private handleVideoConferenceJoined = async (participant: any) => {
    this.participantJoinedSubject$.next(participant);
  }

  private handleVideoConferenceLeft = () => {
    this.participantHangUpSubject$.next(true);
  }


  private getParticipants = () => {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        resolve(this.api.getParticipantsInfo()); // get all participants
      }, 500);
    });
  }

  private handleParticipantLeft = async (participant: any) => {
    this.participantLeftSubject$.next(participant);
    await this.getParticipants().then((data: any) => {
      // end the meet
      if(data && data.length < 2) {
        this.executeCommand('hangup');
      }
    });
  }

  private handleParticipantJoined = async (participant: any) => {
    this.participantJoinedSubject$.next(participant);
  }

  // custom events
  private executeCommand = (command: string) => {
    this.api.executeCommand(command);
  }

}

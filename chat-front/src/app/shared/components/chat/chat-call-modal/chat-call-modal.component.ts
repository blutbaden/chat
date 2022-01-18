import {Component, EventEmitter, OnInit, Output, TemplateRef, ViewChild} from '@angular/core';
import {ModalDismissReasons, NgbModal, NgbModalOptions} from '@ng-bootstrap/ng-bootstrap';
import {CallState} from '../../../../models/call-state.model';
import {ModalService} from '../../../services/modal.service';

@Component({
  selector: 'app-chat-call-modal',
  templateUrl: './chat-call-modal.component.html',
  styleUrls: ['./chat-call-modal.component.scss']
})
export class ChatCallModalComponent implements OnInit {

  @Output() callCanceled = new EventEmitter<boolean>();
  @Output() callRejected = new EventEmitter<boolean>();
  @Output() callAccepted = new EventEmitter<boolean>();

  @ViewChild('callPendingModal') callPendingModal?: TemplateRef<NgbModal>;
  @ViewChild('callAcceptedModal') callAcceptedModal?: TemplateRef<NgbModal>;
  @ViewChild('callErrorModal') callErrorModal?: TemplateRef<NgbModal>;

  modalRef = "CALL_MODAL";
  closeResult?: string;
  state?: string;
  name?: string = 'Unknown';
  isError: boolean = false;

  modalOption: NgbModalOptions = {
    size: "sm",
    backdrop: 'static',
    keyboard: false,
    ariaLabelledBy: 'modal-basic-title',
    centered: true,
  };

  constructor(
    private modalService: ModalService,
    private ngbModal: NgbModal
  ) {
  }

  public get callState(): typeof CallState {
    return CallState;
  }

  ngOnInit(): void {
    this.modalService.showModal$.subscribe(({show, metadata, ref}) => {
      if(ref === this.modalRef) {
        this.state = metadata?.get("STATE");
        this.name = metadata?.get("NAME");
        this.isError = metadata?.get("CALL_ERROR") as boolean;
        const isCallAccepted = metadata?.get("CALL_ACCEPTED") as boolean;
        this.handleModal(show, isCallAccepted);
      }
    });
  }

  private handleModal(show: boolean, isCallAccepted: boolean) {
    if (show) {
      if (this.isError) {
        this.showCallErrorModal();
      } else {
        if (isCallAccepted) {
          this.showCallModal();
        } else {
          // close others modal if opened
          this.hideModal();
          this.modalOption['size'] = 'sm'
          this.showModal(this.callPendingModal!);
        }
      }
    } else {
      this.hideModal();
    }
  }

  private showCallModal(): void {
    // close others modal if opened
    this.hideModal();
    this.modalOption['size'] = 'xl';
    // show accepted call modal
    this.showModal(this.callAcceptedModal!);
  }

  private showCallErrorModal(): void {
    this.hideModal();
    this.modalOption['size'] = 'lg';
    // show error call modal
    this.showModal(this.callErrorModal!);
  }

  acceptCall() {
    this.showCallModal();
    setTimeout(() => this.callAccepted.next(true), 100);
  }

  rejectCall(): void {
    this.hideModal();
    this.callRejected.next(true);
  }

  cancelCall(): void {
    this.hideModal();
    this.callCanceled.next(true);
  }

  private showModal(modal: TemplateRef<NgbModal>): void {
    this.ngbModal.open(modal, this.modalOption).result.then((result) => {
      this.closeResult = `Closed with: ${result}`;
    }, (reason) => {
      this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });
  }

  hideModal(): void {
    this.modalOption['size'] = 'sm';
    this.ngbModal.dismissAll();
  }

  private getDismissReason(reason: any): string {
    if (reason === ModalDismissReasons.ESC) {
      return 'by pressing ESC';
    } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      return 'by clicking on a backdrop';
    } else {
      return `with: ${reason}`;
    }
  }
}

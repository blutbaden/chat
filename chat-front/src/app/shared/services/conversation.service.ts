import {Injectable} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {WebsocketService} from './websocket.service';
import {CallState} from '../../models/call-state.model';
import {NotificationType} from '../../models/notification.model';
import {Pagination} from "../../core/request/request.model";
import {createRequestOption} from "../../core/request/request-util";
import {SERVER_API_URL} from "../../config/app.constants";
import {HttpClient, HttpResponse} from "@angular/common/http";
import {IConversation} from "../../models/conversation.model";

@Injectable({
  providedIn: 'root'
})
export class ConversationService {

  protected resourceUrl = SERVER_API_URL + 'api/conversations';

  constructor(
    protected http: HttpClient
  ) {
  }

  loadAllMessagesByRoom(roomId: number, req?: Pagination): Observable<HttpResponse<IConversation[]>> {
    const options = createRequestOption(req);
    return this.http.get<IConversation[]>(this.resourceUrl + '/room/' + roomId, {params: options, observe: 'response'});
  }

  getCurrentUserConversationsCountByStateGroupedByRoom(): Observable<HttpResponse<any[]>> {
    return this.http.get<any[]>(this.resourceUrl + '/logged/delivered/by-room/count', {observe: 'response'});
  }

  getCurrentUserConversationsCountByStateGroupedBySender(): Observable<HttpResponse<any[]>> {
    return this.http.get<any[]>(this.resourceUrl + '/logged/delivered/by-sender/count', {observe: 'response'});
  }

  getDeliveredMessagesCountByRoom(idRoom: number): Observable<HttpResponse<number>> {
    return this.http.get<number>(this.resourceUrl + '/logged/delivered/room/' + idRoom + '/count', {observe: 'response'});
  }

  updateConversationStateByRoom(idRoom: number): Observable<void> {
    return this.http.put<void>(this.resourceUrl + '/logged/room/' + idRoom, {observe: 'response'});
  }
}

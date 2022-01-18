import {Injectable} from '@angular/core';
import {HttpClient, HttpResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
import {getRoomIdentifier, IRoom} from "../../../models/room.model";
import {SERVER_API_URL} from "../../../config/app.constants";
import {createRequestOption} from "../../../core/request/request-util";
import {isPresent} from "../../../core/util/operators";
import {IUser} from "../../../models/user.model";

export type EntityResponseType = HttpResponse<IRoom>;
export type EntityArrayResponseType = HttpResponse<IRoom[]>;

@Injectable({providedIn: 'root'})
export class RoomService {
  protected resourceUrl = SERVER_API_URL + 'api/rooms';

  constructor(protected http: HttpClient) {
  }

  create(room: IRoom): Observable<EntityResponseType> {
    return this.http.post<IRoom>(this.resourceUrl, room, {observe: 'response'});
  }

  createFirstRoomInGroup(room: IRoom): Observable<EntityResponseType> {
    return this.http.post<IRoom>(this.resourceUrl + '/group', room, {observe: 'response'});
  }

  update(room: IRoom): Observable<EntityResponseType> {
    return this.http.put<IRoom>(`${this.resourceUrl}/${getRoomIdentifier(room) as number}`, room, {observe: 'response'});
  }

  findByGroup(groupId: number): Observable<EntityArrayResponseType> {
    return this.http.get<IRoom[]>(this.resourceUrl + '/group/' + groupId, {observe: 'response'});
  }

  findRoomsInGroupJoinedByLoggedUser(groupId: number): Observable<EntityArrayResponseType> {
    return this.http.get<IRoom[]>(this.resourceUrl + '/logged/group/' + groupId, {observe: 'response'});
  }

  findByLoggedUser(): Observable<EntityArrayResponseType> {
    return this.http.get<IRoom[]>(this.resourceUrl + '/logged', {observe: 'response'});
  }

  findPublicByLoggedUser(): Observable<EntityArrayResponseType> {
    return this.http.get<IRoom[]>(this.resourceUrl + '/logged/public', {observe: 'response'});
  }

  partialUpdate(room: IRoom): Observable<EntityResponseType> {
    return this.http.patch<IRoom>(`${this.resourceUrl}/${getRoomIdentifier(room) as number}`, room, {observe: 'response'});
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IRoom>(`${this.resourceUrl}/${id}`, {observe: 'response'});
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IRoom[]>(this.resourceUrl, {params: options, observe: 'response'});
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, {observe: 'response'});
  }

  addRoomToCollectionIfMissing(roomCollection: IRoom[], ...roomsToCheck: (IRoom | null | undefined)[]): IRoom[] {
    const rooms: IRoom[] = roomsToCheck.filter(isPresent);
    if (rooms.length > 0) {
      const roomCollectionIdentifiers = roomCollection.map(roomItem => getRoomIdentifier(roomItem)!);
      const roomsToAdd = rooms.filter(roomItem => {
        const roomIdentifier = getRoomIdentifier(roomItem);
        if (roomIdentifier == null || roomCollectionIdentifiers.includes(roomIdentifier)) {
          return false;
        }
        roomCollectionIdentifiers.push(roomIdentifier);
        return true;
      });
      return [...roomsToAdd, ...roomCollection];
    }
    return roomCollection;
  }

  getPrivateRoomByLoggedUserAndSelectedUser(user: IUser) {
    return this.http.get<IRoom>(this.resourceUrl + '/logged/private/user/' + user.id, {observe: 'response'});
  }
}

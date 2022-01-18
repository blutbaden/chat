import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import {getGroupIdentifier, IGroup} from "../../models/group.model";
import {ApplicationConfigService} from "../../core/config/application-config.service";
import {createRequestOption} from "../../core/request/request-util";
import {isPresent} from "../../core/util/operators";
import {SERVER_API_URL} from "../../config/app.constants";
import {IUser} from "../../models/user.model";

export type EntityResponseType = HttpResponse<IGroup>;
export type EntityArrayResponseType = HttpResponse<IGroup[]>;

@Injectable({ providedIn: 'root' })
export class GroupService {
  protected resourceUrl = SERVER_API_URL + 'api/groups';

  constructor(protected http: HttpClient, protected applicationConfigService: ApplicationConfigService) {}

  create(group: IGroup): Observable<EntityResponseType> {
    return this.http.post<IGroup>(this.resourceUrl, group, { observe: 'response' });
  }

  update(group: IGroup): Observable<EntityResponseType> {
    return this.http.put<IGroup>(`${this.resourceUrl}/${getGroupIdentifier(group) as number}`, group, { observe: 'response' });
  }

  partialUpdate(group: IGroup): Observable<EntityResponseType> {
    return this.http.patch<IGroup>(`${this.resourceUrl}/${getGroupIdentifier(group) as number}`, group, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IGroup>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IGroup[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  findByLoggedUser(): Observable<EntityArrayResponseType> {
    return this.http.get<IGroup[]>(this.resourceUrl + '/logged', { observe: 'response' });
  }

  getUsersJoinedToSameGroupsAsLoggedUser(): Observable<HttpResponse<IUser[]>> {
    return this.http.get<IUser[]>(this.resourceUrl + '/logged/users', { observe: 'response' });
  }

  getUsersByGroup(id: number): Observable<HttpResponse<IUser[]>> {
    return this.http.get<IUser[]>(this.resourceUrl + `/${id}/users`, {observe: 'response'});
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  addGroupToCollectionIfMissing(groupCollection: IGroup[], ...groupsToCheck: (IGroup | null | undefined)[]): IGroup[] {
    const groups: IGroup[] = groupsToCheck.filter(isPresent);
    if (groups.length > 0) {
      const groupCollectionIdentifiers = groupCollection.map(groupItem => getGroupIdentifier(groupItem)!);
      const groupsToAdd = groups.filter(groupItem => {
        const groupIdentifier = getGroupIdentifier(groupItem);
        if (groupIdentifier == null || groupCollectionIdentifiers.includes(groupIdentifier)) {
          return false;
        }
        groupCollectionIdentifiers.push(groupIdentifier);
        return true;
      });
      return [...groupsToAdd, ...groupCollection];
    }
    return groupCollection;
  }
}

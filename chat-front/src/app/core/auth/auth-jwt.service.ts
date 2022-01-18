import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';

import {ApplicationConfigService} from '../config/application-config.service';
import {Login} from '../../models/login.model';
import {SERVER_API_URL} from '../../config/app.constants';
import {UserState} from "../../models/user-state.model";

type JwtToken = {
    id_token: string;
};

@Injectable({providedIn: 'root'})
export class AuthServerProvider {
    constructor(
        private http: HttpClient,
        private applicationConfigService: ApplicationConfigService
    ) {
    }

    getToken(): string {
        const tokenInLocalStorage: string | null = localStorage.getItem('authenticationToken');
        const tokenInSessionStorage: string | null = sessionStorage.getItem('authenticationToken');
        return tokenInLocalStorage ?? tokenInSessionStorage ?? '';
    }

    login(credentials: Login): Observable<void> {
        return this.http
            .post<JwtToken>(SERVER_API_URL + 'api/authenticate', credentials)
            .pipe(map(response => this.authenticateSuccess(response, credentials.rememberMe)));
    }

    logout(): Observable<void> {
        return new Observable(observer => {
            localStorage.removeItem('authenticationToken');
            localStorage.removeItem('userState');
            sessionStorage.removeItem('authenticationToken');
            sessionStorage.removeItem('userState');
            observer.complete();
        });
    }

    private authenticateSuccess(response: JwtToken, rememberMe: boolean): void {
        const jwt = response.id_token;
        if (rememberMe) {
            localStorage.setItem('authenticationToken', jwt);
            sessionStorage.removeItem('authenticationToken');
            localStorage.setItem('userState', UserState.ONLINE);
            sessionStorage.removeItem('userState');
        } else {
            sessionStorage.setItem('authenticationToken', jwt);
            localStorage.removeItem('authenticationToken');
            sessionStorage.setItem('userState', UserState.ONLINE);
            localStorage.removeItem('userState');
        }
    }
}

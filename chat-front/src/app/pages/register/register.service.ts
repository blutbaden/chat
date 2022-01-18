import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {ApplicationConfigService} from '../../core/config/application-config.service';
import {Registration} from '../../models/registration.mode';
import {SERVER_API_URL} from '../../config/app.constants';

@Injectable({ providedIn: 'root' })
export class RegisterService {
    constructor(private http: HttpClient, private applicationConfigService: ApplicationConfigService) {}

    save(registration: Registration): Observable<{}> {
        return this.http.post(SERVER_API_URL + 'api/register', registration);
    }
}

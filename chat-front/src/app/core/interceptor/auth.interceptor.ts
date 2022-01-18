import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';

import { ApplicationConfigService } from '../config/application-config.service';
import {SERVER_API_URL} from '../../config/app.constants';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(
    private applicationConfigService: ApplicationConfigService
  ) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const serverApiUrl = SERVER_API_URL + '';
    if (!request.url || (request.url.startsWith('http') && !(serverApiUrl && request.url.startsWith(serverApiUrl)))) {
      return next.handle(request);
    }

    const token: string | null =
      localStorage.getItem('authenticationToken') ?? sessionStorage.getItem('authenticationToken');
    if (token) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`,
        },
      });
    }
    return next.handle(request);
  }
}

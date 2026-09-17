import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuthService } from './auth.service';
import { getBackendUrl } from '../config/backend-config';

@Injectable()
export class TokenInterceptor implements HttpInterceptor {

  constructor(private authService: AuthService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Prefixa a URL com a URL do backend (variável de ambiente)
    const backendUrl = getBackendUrl();
    let url = req.url;
    if (backendUrl && !req.url.startsWith('http')) {
      url = backendUrl + req.url;
    }

    let request = req;
    if (url !== req.url) {
      request = req.clone({ url });
    }

    const token = this.authService.getToken();
    if (token && request) {
      request = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401 && !req.url.includes('/auth/login')) {
          this.authService.logout();
        }
        return throwError(() => error);
      })
    );
  }
}

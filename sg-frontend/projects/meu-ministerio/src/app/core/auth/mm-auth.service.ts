import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { Router } from '@angular/router';
import {
  LoginRequest, LoginResponse, AlterarSenhaRequest, ApiMessageResponse
} from '../models/mm.models';

@Injectable({ providedIn: 'root' })
export class MmAuthService {
  // Chaves próprias do app MeuMinisterio (não conflita com o app admin)
  private readonly TOKEN_KEY = 'mm_token';
  private readonly USER_KEY = 'mm_user';

  private userSubject = new BehaviorSubject<LoginResponse | null>(null);
  user$ = this.userSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    this.loadStoredUser();
  }

  login(request: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/auth/login', request).pipe(
      tap(response => {
        this.storeUser(response);
        this.userSubject.next(response);
      })
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.userSubject.next(null);
    this.router.navigateByUrl('/login', { replaceUrl: true });
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getCurrentUser(): LoginResponse | null {
    return this.userSubject.value;
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;
    try {
      const payloadPart = token.split('.')[1];
      const normalized = payloadPart.replace(/-/g, '+').replace(/_/g, '/');
      const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=');
      const payload = JSON.parse(atob(padded));
      const exp = payload.exp * 1000;
      return Date.now() < exp;
    } catch {
      return false;
    }
  }

  precisaTrocarSenha(): boolean {
    const user = this.getCurrentUser();
    return user?.senhaTemporaria === true;
  }

  marcarSenhaAlterada(): void {
    const user = this.getCurrentUser();
    if (user) {
      user.senhaTemporaria = false;
      localStorage.setItem(this.USER_KEY, JSON.stringify(user));
      this.userSubject.next(user);
    }
  }

  alterarSenha(request: AlterarSenhaRequest): Observable<ApiMessageResponse> {
    return this.http.put<ApiMessageResponse>('/auth/alterar-senha', request);
  }

  // ===== Private =====

  private storeUser(response: LoginResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(response));
  }

  private loadStoredUser(): void {
    const stored = localStorage.getItem(this.USER_KEY);
    if (stored) {
      try {
        this.userSubject.next(JSON.parse(stored));
      } catch {
        localStorage.removeItem(this.USER_KEY);
      }
    }
  }
}

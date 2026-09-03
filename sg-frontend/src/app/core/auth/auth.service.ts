import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse, AlterarSenhaRequest, EsqueciSenhaRequest, RedefinirSenhaRequest, ApiMessageResponse, PerfilResponse, AtualizarPerfilRequest } from '../models/sge.models';
import { Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly TOKEN_KEY = 'sge_token';
  private readonly USER_KEY = 'sge_user';

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
    // Usa navigateByUrl + replaceUrl para forçar saída do admin layout
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
      // jjwt (backend) emite tokens em base64url; atob() espera base64 padrão.
      // Normaliza: - → +, _ → /, e completa o padding para decodificar corretamente.
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

  getNivel(): string | null {
    const user = this.getCurrentUser();
    return user?.nivel ?? null;
  }

  getDashboardRoute(): string {
    // Se tiver senha temporária, força ir para o perfil alterar senha
    if (this.precisaTrocarSenha()) {
      return '/perfil';
    }
    switch (this.getNivel()) {
      case 'PASTOR_AUXILIAR': return '/dashboard/pastor';
      case 'TESOUREIRO': return '/dashboard/tesoureiro';
      case 'SECRETARIO': return '/dashboard/secretario';
      default: return '/dashboard';
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

  // ===== Perfil =====

  getPerfil(): Observable<PerfilResponse> {
    return this.http.get<PerfilResponse>('/auth/me');
  }

  atualizarPerfil(request: AtualizarPerfilRequest): Observable<PerfilResponse> {
    return this.http.put<PerfilResponse>('/auth/me', request);
  }

  // ===== Password Management =====

  alterarSenha(request: AlterarSenhaRequest): Observable<ApiMessageResponse> {
    return this.http.put<ApiMessageResponse>('/auth/alterar-senha', request);
  }

  esqueciSenha(request: EsqueciSenhaRequest): Observable<ApiMessageResponse> {
    return this.http.post<ApiMessageResponse>('/auth/esqueci-senha', request);
  }

  redefinirSenha(request: RedefinirSenhaRequest): Observable<ApiMessageResponse> {
    return this.http.post<ApiMessageResponse>('/auth/redefinir-senha', request);
  }

  // ===== Private Helpers =====

  private storeUser(response: LoginResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify(response));
  }

  private loadStoredUser(): void {
    const stored = localStorage.getItem(this.USER_KEY);
    if (stored) {
      try {
        const user = JSON.parse(stored);
        this.userSubject.next(user);
      } catch {
        localStorage.removeItem(this.USER_KEY);
      }
    }
  }
}

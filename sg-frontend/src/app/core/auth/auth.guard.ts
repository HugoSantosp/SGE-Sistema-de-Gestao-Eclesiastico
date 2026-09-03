import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class AuthGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree {
    if (!this.authService.isLoggedIn()) {
      return this.router.parseUrl('/login');
    }

    // Usuário com senha temporária só pode acessar /perfil
    if (this.authService.precisaTrocarSenha()) {
      if (state.url !== '/perfil') {
        return this.router.parseUrl('/perfil');
      }
    }

    return true;
  }
}

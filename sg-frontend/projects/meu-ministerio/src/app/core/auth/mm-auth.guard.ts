import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { MmAuthService } from './mm-auth.service';

@Injectable({ providedIn: 'root' })
export class MmAuthGuard implements CanActivate {

  constructor(
    private authService: MmAuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree {
    if (!this.authService.isLoggedIn()) {
      return this.router.parseUrl('/login');
    }

    // Usuário com senha temporária só pode acessar /trocar-senha
    if (this.authService.precisaTrocarSenha() && state.url !== '/trocar-senha') {
      return this.router.parseUrl('/trocar-senha');
    }

    return true;
  }
}

import { Injectable } from '@angular/core';
import { CanActivate, Router, UrlTree, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    const allowedRoles = route.data['roles'] as string[];

    if (!allowedRoles || allowedRoles.length === 0) {
      return true; // no role restriction
    }

    const userNivel = this.authService.getNivel();

    if (!userNivel) {
      return this.router.parseUrl('/login');
    }

    if (allowedRoles.includes(userNivel)) {
      return true;
    }

    // Redirect to user's own dashboard instead of infinite loop
    return this.router.parseUrl(this.authService.getDashboardRoute());
  }
}

import { Component, Output, EventEmitter } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styles: [':host { display: contents; }']
})
export class HeaderComponent {
  @Output() toggleSidebar = new EventEmitter<void>();
  pageTitle = 'Dashboard';
  user: any;

  constructor(
    private router: Router,
    private authService: AuthService
  ) {
    this.authService.user$.subscribe(u => this.user = u);
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      const url = this.router.url.split('/').filter(s => s);
      this.pageTitle = url.length > 0
        ? url.map(s => s.charAt(0).toUpperCase() + s.slice(1)).join(' > ')
        : 'Dashboard';
    });
  }

  onToggle(): void {
    this.toggleSidebar.emit();
  }

  logout(): void {
    this.authService.logout();
  }
}

import { Injectable } from '@angular/core';
import { Router, ActivatedRouteSnapshot, CanActivate, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { USER_ROLES } from '../dtos/auth-request';
import {UiConfigService} from "../services/ui-config.service";

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService,
              private router: Router,
              private uiConfigService: UiConfigService) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree {
    // If the user is not logged in, redirect to login page
    if (!this.authService.isLoggedIn()) {
      console.warn('User not logged in, redirecting to /login');
      return this.router.createUrlTree(['/login']);
    }

    // Extract role-based checks from the route data
    const adminCheck = route.data['adminCheck'];
    const cleanerCheck = route.data['cleanerCheck'];

    // Get the route path
    const path = route.routeConfig?.path;

    // If the route is 'check-in/:id' and digitalCheckIn is disabled, block access
    if (path === 'check-in/:id' && !this.uiConfigService.digitalCheckInIsEnabled()) {
      console.warn('Digital Check-In is disabled, redirecting to home.');
      return this.router.createUrlTree(['']);
    }

    // If the route is 'room-cleaning' and roomCleaning is disabled, block access
    if (path === 'room-cleaning' && !this.uiConfigService.roomCleaningIsEnabled()) {
      console.warn('Room Cleaning is disabled, redirecting to home.');
      return this.router.createUrlTree(['']);
    }

    // If admin check is required, verify user is an admin
    if (adminCheck && !this.isAdmin()) {
      console.warn('Admin access required. Redirecting to home.');
      return this.router.createUrlTree(['']);
    }

    // If cleaner check is required, verify user is either a cleaner or admin
    if (cleanerCheck && !this.isCleanerOrAdmin()) {
      console.warn('Cleaner/Admin access required. Redirecting to home.');
      return this.router.createUrlTree(['']);
    }

    // Allow access
    return true;
  }

  private isAdmin(): boolean {
    return this.authService.getUserRole() === USER_ROLES.ADMIN;
  }

  private isCleanerOrAdmin(): boolean {
    const role = this.authService.getUserRole();
    return role === USER_ROLES.CLEANING_STAFF || role === USER_ROLES.ADMIN;
  }
}

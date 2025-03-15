import {Injectable} from '@angular/core';
import {AuthRequest, USER_ROLES} from '../dtos/auth-request';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {tap} from 'rxjs/operators';
import {jwtDecode} from 'jwt-decode';
import {Globals} from '../global/globals';
import {CheckInService} from "./check-in.service";
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private authBaseUri: string = this.globals.backendUri + '/authentication';

  constructor(private httpClient: HttpClient, private globals: Globals, private checkInService: CheckInService, private snackBar: MatSnackBar) {
  }

  /**
   * Login in the user. If it was successful, a valid JWT token will be stored
   *
   * @param authRequest User data
   */
  loginUser(authRequest: AuthRequest): Observable<string> {
    return this.httpClient.post(this.authBaseUri, authRequest, {responseType: 'text'})
      .pipe(
        tap((authResponse: string) => {
          this.setToken(authResponse);
          this.checkInService.resetCheckedIn();
          this.snackBar.open('logged in!', 'Close', { duration: 1000 });
        })
      );
  }


  /**
   * Check if a valid JWT token is saved in the localStorage
   */
  isLoggedIn() {
    return !!this.getToken() && (this.getTokenExpirationDate(this.getToken()).valueOf() > new Date().valueOf());
  }

  logoutUser() {
    console.log('Logout');
    localStorage.removeItem('authToken');
          this.snackBar.open('logged out!', 'Close', { duration: 1000 });
  }

  getToken() {
    return localStorage.getItem('authToken');
  }

  /**
   * Returns the user role based on the current token
   */
  getUserRole() {
    if (this.getToken() != null) {
      const decoded: any = jwtDecode(this.getToken());
      const authInfo: string[] = decoded.rol;
      if (authInfo.includes('ROLE_ADMIN')) {
        return USER_ROLES.ADMIN;
      } else if (authInfo.includes('ROLE_RECEPTIONIST')) {
        return USER_ROLES.RECEPTIONIST;
      } else if (authInfo.includes('ROLE_CLEANING_STAFF')) {
        return USER_ROLES.CLEANING_STAFF;
      } else if (authInfo.includes('ROLE_GUEST')) {
        return USER_ROLES.GUEST;
      }
    }
    return 'UNDEFINED';
  }

  getUserEmail() {
    if (this.getToken() != null) {
      return jwtDecode(this.getToken()).sub;
    }
  }

  private setToken(authResponse: string) {
    localStorage.setItem('authToken', authResponse);
  }

  private getTokenExpirationDate(token: string): Date {

    const decoded: any = jwtDecode(token);
    if (decoded.exp === undefined) {
      return null;
    }

    const date = new Date(0);
    date.setUTCSeconds(decoded.exp);
    return date;
  }

}

import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'SE PR Group Phase';

  constructor() {
    this.clearAuthTokensOnAppStart();
  }

  private clearAuthTokensOnAppStart(): void {
    const appInitializedKey = 'appInitialized';

    // Check if the app has already been initialized in this session
    const isAppInitialized = sessionStorage.getItem(appInitializedKey);

    if (!isAppInitialized) {
      console.log('First app start detected. Clearing auth tokens...');
      localStorage.removeItem('authToken'); // Clear tokens
      sessionStorage.removeItem('authToken');

      // Mark the app as initialized for this browser session
      sessionStorage.setItem(appInitializedKey, 'true');
      console.log('Auth tokens cleared and app marked as initialized.');
    } else {
      console.log('App already initialized, skipping token clearance.');
    }
  }
}

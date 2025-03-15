import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class Globals {
  readonly backendUri: string = this.findBackendUrl();
  readonly stripePublishableKey: string = 'pk_test_51QgrpZ4IfNp467UDKM50oyjspnrghAzsdZXWxR1v6pmxbggRvO8XbUW02FIEdkjyk1ThhKKQEySfsy8Z27czxUcE00Z5nMXxT8';

  private findBackendUrl(): string {
    if (window.location.port === '4200') { // local `ng serve`, backend at localhost:8080
      return 'http://localhost:8080/api/v1';
    } else {
      // assume deployed somewhere and backend is available at same host/port as frontend
      return window.location.protocol + '//' + window.location.host + window.location.pathname + 'api/v1';
    }
  }
}



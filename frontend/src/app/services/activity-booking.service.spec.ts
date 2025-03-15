import { TestBed } from '@angular/core/testing';

import { ActivityBookingService } from './activity-booking.service';

describe('ActivityBookingService', () => {
  let service: ActivityBookingService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ActivityBookingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

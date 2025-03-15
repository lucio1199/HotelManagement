import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ActivityTimeslotListComponent } from './activity-timeslot-list.component';

describe('ActivityTimeslotListComponent', () => {
  let component: ActivityTimeslotListComponent;
  let fixture: ComponentFixture<ActivityTimeslotListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ActivityTimeslotListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ActivityTimeslotListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

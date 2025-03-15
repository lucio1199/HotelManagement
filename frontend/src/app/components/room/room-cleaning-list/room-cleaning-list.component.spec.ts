import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RoomCleaningListComponent } from './room-cleaning-list.component';

describe('RoomCleaningListComponent', () => {
  let component: RoomCleaningListComponent;
  let fixture: ComponentFixture<RoomCleaningListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RoomCleaningListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RoomCleaningListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

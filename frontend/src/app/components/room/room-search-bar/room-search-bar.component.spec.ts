import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RoomSearchBarComponent } from './room-search-bar.component';

describe('RoomSearchBarComponent', () => {
  let component: RoomSearchBarComponent;
  let fixture: ComponentFixture<RoomSearchBarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RoomSearchBarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RoomSearchBarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

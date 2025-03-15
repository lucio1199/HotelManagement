import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RoomCreateEditComponent } from './room-create-edit.component';

describe('RoomCreateComponent', () => {
  let component: RoomCreateEditComponent;
  let fixture: ComponentFixture<RoomCreateEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RoomCreateEditComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RoomCreateEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyRoomComponent } from './my-room.component';

describe('MyRoomComponent', () => {
  let component: MyRoomComponent;
  let fixture: ComponentFixture<MyRoomComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyRoomComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MyRoomComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

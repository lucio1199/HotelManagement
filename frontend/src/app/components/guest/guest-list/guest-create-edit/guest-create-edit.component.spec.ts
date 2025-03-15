import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GuestCreateEditComponent } from './guest-create-edit.component';

describe('GuestCreateEditComponent', () => {
  let component: GuestCreateEditComponent;
  let fixture: ComponentFixture<GuestCreateEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GuestCreateEditComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GuestCreateEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

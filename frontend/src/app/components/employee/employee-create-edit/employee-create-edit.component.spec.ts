import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmployeeCreateEditComponent } from './employee-create-edit.component';

describe('EmployeeCreateEditComponent', () => {
  let component: EmployeeCreateEditComponent;
  let fixture: ComponentFixture<EmployeeCreateEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EmployeeCreateEditComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EmployeeCreateEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

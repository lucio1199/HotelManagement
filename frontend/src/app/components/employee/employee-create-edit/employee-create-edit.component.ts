import {Component, OnInit} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule, ValidationErrors,
  ValidatorFn,
  Validators
} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatCardModule} from "@angular/material/card";
import {CommonModule} from "@angular/common";
import {MatIconModule} from "@angular/material/icon";
import {EmployeeService} from "../../../services/employee.service";
import {ActivatedRoute, Router} from "@angular/router";
import {EmployeeCreateDto, EmployeeDetailDto, EmployeeUpdateDto, RoleType} from "../../../dtos/employee";
import {MatOption} from "@angular/material/autocomplete";
import {MatSelect} from "@angular/material/select";
import { MatSnackBar } from '@angular/material/snack-bar';
import { Location } from '@angular/common';

export enum EmployeeCreateEditMode {
  create,
  edit
}

@Component({
  selector: 'app-employee-create-edit',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatCardModule,
    CommonModule,
    MatIconModule,
    MatOption,
    MatSelect
  ],
  templateUrl: './employee-create-edit.component.html',
  styleUrls: ['./employee-create-edit.component.scss']
})
export class EmployeeCreateEditComponent implements OnInit {
  mode: EmployeeCreateEditMode = EmployeeCreateEditMode.create;
  employeeId: number | null = null;
  employeeForm!: FormGroup;
  roleTypes = Object.values(RoleType).filter(role => role !== RoleType.ROLE_GUEST);
  hidePassword = true;
  isRoleTypeEditable: boolean = true;

  constructor(
      private service: EmployeeService,
      private router: Router,
      private route: ActivatedRoute,
      private formBuilder: FormBuilder,
      private snackBar: MatSnackBar,
      private location: Location
  ) {}

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.mode = data['mode'];

      if (this.mode === EmployeeCreateEditMode.create) {
        this.setupCreateForm();
      } else {
        this.setupEditForm();
      }

      if (this.mode === EmployeeCreateEditMode.edit) {
        this.loadEmployeeData();
      }
    });
  }

  private setupCreateForm(): void {
    // In create mode, exclude admin and guest roles.
    this.roleTypes = Object.values(RoleType)
        .filter(role => role !== RoleType.ROLE_GUEST && role !== RoleType.ROLE_ADMIN);

    this.employeeForm = this.formBuilder.group({
      firstName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      lastName: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
      phoneNumber: ['', [Validators.minLength(7), Validators.maxLength(15)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      roleType: [this.roleTypes[0], [Validators.required]]
    });
  }

  private setupEditForm(): void {
    // In edit mode, all fields are optional, but validated if present.
    this.employeeForm = this.formBuilder.group({
      firstName: ['', this.conditionalValidator([Validators.minLength(2), Validators.maxLength(100)])],
      lastName: ['', this.conditionalValidator([Validators.minLength(2), Validators.maxLength(100)])],
      phoneNumber: ['', this.conditionalValidator([Validators.minLength(7), Validators.maxLength(15)])],
      email: ['', this.conditionalValidator([Validators.email])],
      password: ['', this.conditionalValidator([Validators.minLength(6)])],
      roleType: ['']
    });
  }

  /**
   * Only apply the given validators if the control has a value.
   */
  private conditionalValidator(validators: ValidatorFn[]): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;
      if (value && value.trim() !== '') {
        return Validators.compose(validators)?.(control) || null;
      }
      return null;
    };
  }

  goBack(): void {
    this.location.back();
  }

  onSubmit() {
    if (this.employeeForm.invalid) {
      return;
    }

    const cleanedFormValues = this.cleanFormValues(this.employeeForm.value);

    if (this.mode === EmployeeCreateEditMode.create) {
      const employeeDto: EmployeeCreateDto = {
        firstName: cleanedFormValues.firstName,
        lastName: cleanedFormValues.lastName,
        phoneNumber: cleanedFormValues.phoneNumber,
        roleType: cleanedFormValues.roleType,
        email: cleanedFormValues.email,
        password: cleanedFormValues.password
      };
      this.createEmployee(employeeDto);
    } else {
      const employeeUpdateDto: EmployeeUpdateDto = {
        firstName: cleanedFormValues.firstName,
        lastName: cleanedFormValues.lastName,
        phoneNumber: cleanedFormValues.phoneNumber,
        roleType: cleanedFormValues.roleType,
        email: cleanedFormValues.email,
        password: cleanedFormValues.password
      };
      this.updateEmployee(employeeUpdateDto);
    }
  }

  private cleanFormValues(values: any): any {
    const cleanedValues: any = {};
    for (const key of Object.keys(values)) {
      const value = values[key];
      cleanedValues[key] = (typeof value === 'string' && value.trim() === '') ? null : value;
    }
    return cleanedValues;
  }

  private createEmployee(employeeDto: EmployeeCreateDto) {
    this.service.createEmployee(employeeDto).subscribe({
      next: (createdEmployee) => {
        this.router.navigate(['/employees']);
        this.snackBar.open(`Employee ${createdEmployee.firstName} ${createdEmployee.lastName} created successfully`, 'Close', { duration: 3000 });
      },
      error: error => {
              console.error('Error creating employee', error);
        this.snackBar.open("Error creating employee: " + error.error.errors, 'Close', { duration: 3000 });
      }
    });
  }

  private updateEmployee(employeeUpdateDto: EmployeeUpdateDto) {
    this.service.updateEmployee(this.employeeId, employeeUpdateDto).subscribe({
      next: () => {
        this.router.navigate(['/employees']);
        this.snackBar.open("Employee updated successfully", 'Close', { duration: 3000 });
      },
      error: error => {
        console.error('Error updating employee', error);
        this.snackBar.open("Error updating employee: " + error.error.errors, 'Close', { duration: 3000 });
      }
    });
  }

  private loadEmployeeData() {
    this.route.params.subscribe(params => {
      const id = +params['id'];
      this.employeeId = id;
      this.service.findOneEmployee(id).subscribe({
        next: (employee: EmployeeDetailDto) => {
          // Set initial form values
          this.employeeForm.setValue({
            firstName: employee.firstName,
            lastName: employee.lastName,
            phoneNumber: employee.phoneNumber || '',
            roleType: employee.roleType,
            email: employee.email,
            password: ''
          });

          // Adjust roleTypes based on the employee's current role
          if (employee.roleType === RoleType.ROLE_ADMIN) {
            // Show admin but disable editing the role
            this.isRoleTypeEditable = false;
            this.employeeForm.get('roleType')?.disable();
          } else {
            // Not an admin, so remove admin from the selectable roles
            // to prevent setting admin when updating
            this.roleTypes = this.roleTypes.filter(r => r !== RoleType.ROLE_ADMIN);
            this.isRoleTypeEditable = true;
          }
        },
        error: error => {
          console.error('Error loading employee', error);
          this.snackBar.open("Error loading employee data" + error.error.errors, 'Close', { duration: 3000 });
        }
      });
    });
  }

  public get modeText() {
    return this.mode === EmployeeCreateEditMode.create ? "Create" : "Update";
  }

  protected readonly RoleType = RoleType;
  protected readonly EmployeeCreateEditMode = EmployeeCreateEditMode;
}

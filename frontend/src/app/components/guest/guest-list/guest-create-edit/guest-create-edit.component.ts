import {Component, OnInit} from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule, ValidationErrors,
  Validators
} from "@angular/forms";
import {MatButtonModule} from "@angular/material/button";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatCardModule} from "@angular/material/card";
import {CommonModule} from "@angular/common";
import {MatIconModule} from "@angular/material/icon";
import {MatOption} from "@angular/material/autocomplete";
import {MatSelect} from "@angular/material/select";
import {MatSnackBar} from "@angular/material/snack-bar";
import {ActivatedRoute, Router} from "@angular/router";
import {GuestService} from "../../../../services/guest.service";
import {GuestCreateDto, GuestDetailDto, GuestUpdateDto} from "../../../../dtos/guest";
import {Gender, GENDERS} from "../../../../models/gender.model";
import { Nationality, NATIONALITIES } from '../../../../models/nationality.model'; // Adjust path as necessary



export enum GuestCreateEditMode {
  create,
  edit
}
@Component({
  selector: 'app-guest-create-edit',
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
  templateUrl: './guest-create-edit.component.html',
  styleUrl: './guest-create-edit.component.scss'
})
export class GuestCreateEditComponent implements OnInit {
  mode: GuestCreateEditMode = GuestCreateEditMode.create;
  guestEmail: string | null = null;
  guestForm!: FormGroup;
  hidePassword = true;
  genders: Gender[] = GENDERS;
  nationalities: Nationality[] = NATIONALITIES;
  guest: GuestDetailDto = {
    firstName: '',
    lastName: '',
    email: '',
    dateOfBirth: null,
    placeOfBirth: '',
    gender: '',
    nationality: '',
    address: '',
    passportNumber: '',
    phoneNumber: '',
    password: '',
  }

  constructor(
    private service: GuestService,
    private router: Router,
    private route: ActivatedRoute,
    private formBuilder: FormBuilder,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.mode = data['mode'];
      this.initializeForm();
      if (this.mode === GuestCreateEditMode.edit) {
        this.loadGuestData();
      }
    });
  }

  onSubmit(): void {
    if (this.guestForm.invalid) {
      return;
    }

    const cleanedValues = this.cleanFormValues(this.guestForm.value);

    if (this.mode === GuestCreateEditMode.create) {
      const createDto: GuestCreateDto = { ...cleanedValues };
      this.createGuest(createDto);
    } else {
      const updateDto: GuestUpdateDto = { ...cleanedValues };
      this.updateGuest(updateDto);
    }
  }

  private initializeForm(): void {
    this.guestForm = this.formBuilder.group({
      firstName: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(64)]],
      lastName: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(64)]],
      email: ['', [Validators.required, Validators.email]],
      dateOfBirth: ['', [Validators.minLength(3), Validators.maxLength(64), this.minimumAgeValidator(18)]],
      placeOfBirth: ['', [Validators.minLength(3), Validators.maxLength(128)]],
      gender: [''],
      nationality: [''],
      address: ['', [Validators.minLength(3), Validators.maxLength(256)]],
      passportNumber: ['', [Validators.minLength(7), Validators.maxLength(15)]],
      phoneNumber: ['', [Validators.minLength(6), Validators.maxLength(9)]],
      password: this.mode === GuestCreateEditMode.create
        ? ['', [Validators.required, Validators.minLength(6)]]
        : ['']
    });
  }

  private loadGuestData(): void {
    this.route.params.subscribe(params => {
      this.guestEmail = params['email'];
      this.service.getGuest(this.guestEmail).subscribe({
        next: (guest: GuestDetailDto) => {
          console.log('guestData: ', guest);
          this.guestForm.patchValue({
            firstName: guest.firstName,
            lastName: guest.lastName,
            email: guest.email,
            dateOfBirth: guest.dateOfBirth,
            placeOfBirth: guest.placeOfBirth,
            gender: guest.gender,
            nationality: guest.nationality,
            address: guest.address,
            passportNumber: guest.passportNumber,
            phoneNumber: guest.phoneNumber
          });
        },
        error: error => {
          console.error('Error loading guest', error);
          this.snackBar.open('Error loading guest', 'Ok', { duration: 3000 });
        }
      });
    });
  }

  private createGuest(guestDto: GuestCreateDto): void {
    this.service.createGuest(guestDto).subscribe({
      next: () => {
        this.router.navigate(['guests']);
        this.snackBar.open('Guest created successfully', 'Ok', { duration: 3000 });
      },
      error: error => {
        this.printErrors(error);
      }
    });
  }

  private updateGuest(guestDto: GuestUpdateDto): void {
    if (!this.guestEmail) return;

    this.service.updateGuest(this.guestEmail, guestDto).subscribe({
      next: () => {
        this.router.navigate(['guests']);
        this.snackBar.open('Guest updated successfully', 'Ok', { duration: 3000 });
      },
      error: error => {
        console.error('Error updating guest', error);
        this.snackBar.open('Error updating guest: ' + error, 'Ok', { duration: 3000 });
      }
    });
  }

  private cleanFormValues(values: any): any {
    return Object.keys(values).reduce((acc, key) => {
      const value = values[key];
      acc[key] = typeof value === 'string' && value.trim() === '' ? null : value;
      return acc;
    }, {});
  }

  goBack(): void {
    this.router.navigate(['guests']);
  }

  public get modeText(): string {
    return this.mode === GuestCreateEditMode.create ? 'Create' : 'Update';
  }
  minimumAgeValidator(minAge: number) {
    return (control: AbstractControl): ValidationErrors | null => {
      const dateOfBirth = control.value;
      if (!dateOfBirth) {
        return null; // If no date is entered, validation passes (can combine with required later)
      }

      const today = new Date();
      const birthDate = new Date(dateOfBirth);
      const age = today.getFullYear() - birthDate.getFullYear();
      const monthDiff = today.getMonth() - birthDate.getMonth();
      const dayDiff = today.getDate() - birthDate.getDate();

      // Adjust age if the current date is before the birthday
      if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) {
        return age - 1 >= minAge ? null : {minimumAge: {requiredAge: minAge, actualAge: age - 1}};
      }
      return age >= minAge ? null : {minimumAge: {requiredAge: minAge, actualAge: age}};
    };
  }

  printErrors(error: any) {
    console.log(error);
    let errorMessage = '';
    if ((error.status === 422 || error.status === 409) && error.error) {
      const backendError = error.error;
      if (backendError.errors && typeof backendError.errors === 'object') {
        const fieldErrors = Object.entries(backendError.errors)
          .map(([field, errors]) => `${Array.isArray(errors) ? errors.join(', ') : errors}`)
          .join(' ');
        errorMessage += fieldErrors;
      }
    } else {
      errorMessage += error.message || 'An unknown error occurred.';
    }

    this.snackBar.open(errorMessage, 'Close', { duration: 50000 });

  }

  protected readonly GuestCreateEditMode = GuestCreateEditMode;
}



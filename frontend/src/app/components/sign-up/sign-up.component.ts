import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {Router, RouterModule} from "@angular/router";
import {AuthService} from "../../services/auth.service";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatCardModule} from "@angular/material/card";
import {MatIconModule} from "@angular/material/icon";
import {CommonModule} from "@angular/common";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";
import {GuestSignupDto, SimpleGuestDto} from "../../dtos/guest";
import {GuestService} from "../../services/guest.service";
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
  selector: 'app-sign-up',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatFormFieldModule,
    MatCardModule,
    MatIconModule,
    MatInputModule,
    MatButtonModule
  ],
  templateUrl: './sign-up.component.html',
  styleUrl: './sign-up.component.scss'
})
export class SignUpComponent implements OnInit {
  signupForm: FormGroup;
  submitted = false;
  errorMessage = '';

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private authService: AuthService,
    private guestService: GuestService,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.initializeForm();
  }

  private initializeForm(): void {
    this.signupForm = this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    }, {
      validator: this.passwordMatchValidator
    });
  }

  // Custom validator to check if passwords match
  private passwordMatchValidator(group: FormGroup): any {
    const password = group.get('password');
    const confirmPassword = group.get('confirmPassword');

    if (!password || !confirmPassword) {
      return null;
    }

    if (confirmPassword.errors && !confirmPassword.errors['passwordMismatch']) {
      return null;
    }

    if (password.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    } else {
      confirmPassword.setErrors(null);
      return null;
    }
  }

  // For easy access to form fields
  get f() {
    return this.signupForm.controls;
  }

  registerUser(): void {
    this.submitted = true;

    if (this.signupForm.invalid) {
      return;
    }

    const guestSignupDto: GuestSignupDto = {
      email: this.f['email'].value,
      password: this.f['password'].value
    };

    this.guestService.signup(guestSignupDto).subscribe({
      next: (response: SimpleGuestDto) => {
        console.log('Registration successful');
        this.snackBar.open('Registration successful', 'Close', { duration: 3000 });
        this.router.navigate(['/login']);
      },
      error: (error) => {
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

        this.snackBar.open(errorMessage, 'Close', { duration : 5000 });
      }
    });
  }

  vanishError(): void {
    this.errorMessage = '';
  }

  hasError(controlName: string, errorName: string): boolean {
    return this.signupForm.get(controlName)?.errors?.[errorName]
      && this.signupForm.get(controlName)?.touched;
  }

  resetForm(): void {
    this.submitted = false;
    this.errorMessage = '';
    this.signupForm.reset();
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}

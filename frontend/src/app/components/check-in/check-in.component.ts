import { Component, OnInit } from '@angular/core';
import { CheckInService } from '../../services/check-in.service';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BookingDetailDto } from '../../dtos/booking';
import { CheckInDto } from '../../dtos/check-in';
import { ManuallyAddToRoomDto } from '../../dtos/check-in';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { GENDERS, Gender } from '../../models/gender.model';
import { NATIONALITIES, Nationality } from '../../models/nationality.model';
import { AuthService } from '../../services/auth.service';
import { GuestService } from '../../services/guest.service'; // Import GuestService
import { MatDialog } from "@angular/material/dialog";
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog.component';
import { DialogMode } from "../confirm-dialog/dialog-mode.enum";
import { MAT_DATE_FORMATS, DateAdapter, MAT_DATE_LOCALE } from '@angular/material/core';
import { MomentDateAdapter } from '@angular/material-moment-adapter';
import { DatePipe } from '@angular/common';

export const MY_FORMATS = {
  parse: {
    dateInput: 'DD.MM.YYYY',
  },
  display: {
    dateInput: 'DD.MM.YYYY',
    monthYearLabel: 'MMMM YYYY',
    dateA11yLabel: 'LL',
    monthYearA11yLabel: 'MMMM YYYY',
  },
};

@Component({
  selector: 'app-check-in',
  templateUrl: './check-in.component.html',
  styleUrls: ['./check-in.component.scss'],
  providers: [
      { provide: DateAdapter, useClass: MomentDateAdapter, deps: [MAT_DATE_LOCALE] },
      { provide: MAT_DATE_FORMATS, useValue: MY_FORMATS },
  ],
})
export class CheckInComponent implements OnInit {
  passport: File | null = null;
  booking: BookingDetailDto | null = null;
  price: number | null = null;
  tax: number | null = null;
  passportName: string | null = null;
  bookingId: string | null = null;
  checkInForm: UntypedFormGroup;
  gender: string | null = null;
  genders: Gender[] = GENDERS;
  nationality: string | null = null;
  nationalities: Nationality[] = NATIONALITIES;
  isManualCheckIn: boolean = false;
  guestEmail: string | null = null;
  submitted = false;
  error = false;
  errorMessage = '';
  _currentYear = new Date().getFullYear();
  minDate = new Date(this._currentYear - 125, new Date().getMonth(), new Date().getDay());
  maxDate = new Date(this._currentYear - 18, new Date().getMonth(), new Date().getDay());
  ownerEmail: string | null = null;
  isGuestAddToRoom: boolean = false;
  isManualAddToRoom: boolean = false;

  constructor(
    private checkInService: CheckInService,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar,
    private fb: UntypedFormBuilder,
    private authService: AuthService,
    private guestService: GuestService, // Inject GuestService
    private dialog: MatDialog,
    private datePipe: DatePipe
  ) {}

  ngOnInit(): void {
    this.guestEmail = this.route.snapshot.paramMap.get('email');
    this.ownerEmail = this.route.snapshot.paramMap.get('ownerEmail');
    this.bookingId = this.route.snapshot.paramMap.get('id');
    if (this.ownerEmail && this.ownerEmail === 'manual') {
      this.isManualAddToRoom = true;
    }
    this.isManualCheckIn = this.guestEmail !== null;
    this.isGuestAddToRoom = this.ownerEmail !== null;
    this.fetchBookingDetails();
    console.log("isManualCheckIn: " + this.isManualCheckIn);
    console.log("isManualAddToRoom: " + this.isManualAddToRoom);
    console.log("isGuestAddToRoom: " + this.isGuestAddToRoom);

    if (this.isGuestAddToRoom) {
      this.maxDate = new Date();
    }

    if (this.isManualCheckIn && !this.isGuestAddToRoom) {
      this.loadGuestData(this.guestEmail); // Load guest data if manual check-in
    } else if (this.isManualAddToRoom && this.isGuestAddToRoom) {
      this.loadGuestData(this.guestEmail); // Load guest data if manual add to room
    } else if (!this.isManualCheckIn && !this.isManualAddToRoom && !this.isGuestAddToRoom) {
      this.loadGuestData(this.authService.getUserEmail()); // Load guest data if not manual check-in
    }
    this.checkInForm = this.fb.group({
      firstname: ['', Validators.required],
      lastname: ['', Validators.required],
      dateofbirth: ['', Validators.required],
      placeofbirth: ['', Validators.required],
      address: ['', Validators.required],
      phonenumber: ['', Validators.required],
      passportnumber: ['', Validators.required],
      nationality: ['', Validators.required],
      gender: ['', Validators.required],
    });
  }

  fetchBookingDetails(): void {
    if (this.isGuestAddToRoom && !this.isManualAddToRoom) {
      this.checkInService.getBookingById(+this.bookingId).subscribe(
        (booking) => {
          this.booking = booking;
          const startDate = new Date(booking.startDate);
          const endDate = new Date(booking.endDate);
          const timeDifference = endDate.getTime() - startDate.getTime();
          const dayDifference = timeDifference / (1000 * 60 * 60 * 24);

          this.tax = parseFloat((booking.price * dayDifference * 0.1).toFixed(2));
          this.price = parseFloat((booking.price * dayDifference + this.tax).toFixed(2));
        },
        (error) => {
          this.router.navigate(['/']);
          this.snackBar.open('The requested room booking does not exist', 'Close', { duration: 3000 });
        }
      );
    } else if (this.isGuestAddToRoom && this.isManualAddToRoom) {
      this.checkInService.getBookingByIdAndEmail(+this.bookingId, this.authService.getUserEmail()).subscribe(
        (booking) => {
          this.booking = booking;
          // Calculate the booking duration in days
          const startDate = new Date(booking.startDate);
          const endDate = new Date(booking.endDate);
          const timeDifference = endDate.getTime() - startDate.getTime(); // Difference in milliseconds
          const dayDifference = timeDifference / (1000 * 60 * 60 * 24); // Convert milliseconds to days

          // Calculate the total price based on the number of days
          this.price = booking.price * dayDifference;
        },
        (error) => {
          this.router.navigate(['/']);
          this.snackBar.open('The requested booking does not exist', 'Close', { duration: 3000 });
        }
      );
    } else if (this.isManualCheckIn) {
      this.checkInService.getBookingByIdAndEmail(+this.bookingId, this.guestEmail).subscribe(
        (booking) => {
          this.booking = booking;
          const startDate = new Date(booking.startDate);
          const endDate = new Date(booking.endDate);
          const timeDifference = endDate.getTime() - startDate.getTime();
          const dayDifference = timeDifference / (1000 * 60 * 60 * 24);

          this.tax = parseFloat((booking.price * dayDifference * 0.1).toFixed(2));
          this.price = parseFloat((booking.price * dayDifference + this.tax).toFixed(2));
        },
        (error) => {
          this.router.navigate(['/']);
          this.snackBar.open('The requested check-in does not exist', 'Close', { duration: 3000 });
        }
      );
    } else {
      this.checkInService.getBookingById(+this.bookingId).subscribe(
        (booking) => {
          this.booking = booking;
          const startDate = new Date(booking.startDate);
          const endDate = new Date(booking.endDate);
          const timeDifference = endDate.getTime() - startDate.getTime();
          const dayDifference = timeDifference / (1000 * 60 * 60 * 24);

          this.tax = parseFloat((booking.price * dayDifference * 0.1).toFixed(2));
          this.price = parseFloat((booking.price * dayDifference + this.tax).toFixed(2));
        },
        (error) => {
          this.router.navigate(['/']);
          this.snackBar.open('The requested check-in does not exist', 'Close', { duration: 3000 });
        }
      );
    }
  }

  loadGuestData(email): void {
    this.guestService.getGuest(email).subscribe(
      (guestData) => {
        this.checkInForm.patchValue({
          firstname: guestData.firstName || '',
          lastname: guestData.lastName || '',
          dateofbirth: guestData.dateOfBirth || '',
          placeofbirth: guestData.placeOfBirth || '',
          address: guestData.address || '',
          phonenumber: guestData.phoneNumber || '',
          passportnumber: guestData.passportNumber || '',
          nationality: guestData.nationality || '',
          gender: guestData.gender || '',
        });
      },
      (error) => {
        console.error('Error loading guest data', error);
      }
    );
  }

  onFileChange(event: any): void {
    const files = (event.target as HTMLInputElement).files;
    const MAX_FILE_SIZE_MB = 10; // Max file size in MB
    const allowedTypes = ['application/pdf'];

    if (files) {
      Array.from(files).forEach(file => {
        // Check MIME type
        if (!allowedTypes.includes(file.type)) {
          this.snackBar.open(
            `Invalid file type: ${file.type}. Allowed type is PDF.`,
            'Close',
            { duration: 3000, panelClass: 'snack-error' }
          );
          return;
        } else {
          // Check file size
          const fileSizeInMB = file.size / (1024 * 1024);
          if (fileSizeInMB > MAX_FILE_SIZE_MB) {
            this.snackBar.open(
              `File size exceeds the 10 MB limit: ${file.name} (${fileSizeInMB.toFixed(2)} MB).`,
              'Close',
              { duration: 3000, panelClass: 'snack-error' }
            );
            return;
          } else {
            if (event.target.files.length > 0) {
              this.passport = event.target.files[0];
              this.passportName = this.passport.name;
            }
          }
        }
      });
    }
  }

  confirmDelete(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      height: '200px',
      width: '500px',
      data: { name: " passport",
              mode: DialogMode.Deletion,
              message: "Are you sure you want to delete your"
            },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.onDeleteFile();
      }
    });
  }

  onDeleteFile(): void {
    this.passport = null;
    this.passportName = null;
    const fileInput = document.querySelector('#passportInput') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
  }

  onSubmit(): void {
    if (!this.passport && (!this.isManualCheckIn || !this.isManualAddToRoom)) {
      this.snackBar.open('Please upload your passport to proceed.', 'Close', { duration: 3000 });
      return;
    }
    if (!this.passport && (this.isManualCheckIn || this.isManualAddToRoom)) {
      this.snackBar.open('Please upload a passport to proceed.', 'Close', { duration: 3000 });
      return;
    }
    if (!this.checkInForm.valid && !this.isManualCheckIn) {
      this.snackBar.open('Please input your information to proceed.', 'Close', { duration: 3000 });
      return;
    }
    if (!this.checkInForm.valid && this.isManualCheckIn) {
      this.snackBar.open('Please input the guest information to proceed.', 'Close', { duration: 3000 });
      return;
    }

    // Get raw date value from form
    const rawDateOfBirth = this.checkInForm.get('dateofbirth')?.value;
    // Format date using DatePipe
    const formattedDateOfBirth = this.datePipe.transform(rawDateOfBirth, 'YYYY-MM-dd') ?? '';

    const checkIn: CheckInDto = {
      bookingId: this.bookingId,
      firstName: this.checkInForm.get('firstname')?.value,
      lastName: this.checkInForm.get('lastname')?.value,
      dateOfBirth: formattedDateOfBirth,
      placeOfBirth: this.checkInForm.get('placeofbirth')?.value,
      gender: this.checkInForm.get('gender')?.value,
      nationality: this.checkInForm.get('nationality')?.value,
      address: this.checkInForm.get('address')?.value,
      passportNumber: this.checkInForm.get('passportnumber')?.value,
      phoneNumber: this.checkInForm.get('phonenumber')?.value,
      passport: this.passport,
    };

    const addToRoomDto: ManuallyAddToRoomDto = {
      bookingId: this.bookingId,
      firstName: this.checkInForm.get('firstname')?.value,
      lastName: this.checkInForm.get('lastname')?.value,
      dateOfBirth: formattedDateOfBirth,
      placeOfBirth: this.checkInForm.get('placeofbirth')?.value,
      gender: this.checkInForm.get('gender')?.value,
      nationality: this.checkInForm.get('nationality')?.value,
      address: this.checkInForm.get('address')?.value,
      passportNumber: this.checkInForm.get('passportnumber')?.value,
      phoneNumber: this.checkInForm.get('phonenumber')?.value,
      passport: this.passport,
      email: this.guestEmail
    };

    if (this.isGuestAddToRoom && this.isManualAddToRoom) {
      this.checkInService.manuallyAddGuestToRoom(addToRoomDto).subscribe({
        next: () => {
         this.router.navigate(['/bookings/managerbookings']);
         this.snackBar.open(`The guest was added to the room successfully!`, 'Close', { duration: 3000 });
        },
        error: (error) => {
          console.log(error);
          let errorMessage = '';
          if ((error.status === 422 || error.status === 409 || error.status === 404) && error.error) {
            const backendError = error.error;
            if (backendError.errors && typeof backendError.errors === 'object') {
            const fieldErrors = Object.entries(backendError.errors)
              .map(([field, errors]) => `${Array.isArray(errors) ? errors.join(', ') : errors}`)
              .join(' ');
            errorMessage += fieldErrors;
            }
          } else {
           errorMessage += 'An unknown error occurred.';
          }
          this.snackBar.open(errorMessage, 'Close', { duration: 50000 });
        }
      });
    } else if (this.isManualCheckIn) {
      this.checkInService.manualCheckIn(checkIn, this.guestEmail).subscribe(
        () => {
          this.router.navigate(['bookings/managerbookings']);
          this.snackBar.open('Check-in successful!', 'Close', { duration: 3000 });
        },
        (error) => {
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
            errorMessage += 'An unknown error occurred.';
          }

          this.snackBar.open(errorMessage, 'Close', { duration: 50000 });
        }
      );
    } else {
      this.checkInService.checkIn(checkIn).subscribe(
        () => {
          // Navigate to the MyRoom page with queryParams indicating a fresh check-in
          this.router.navigate(['/my-room'], { queryParams: { fromCheckIn: 'true' } });
          this.snackBar.open('Check-in successful!', 'Close', { duration: 3000 });
        },
        (error) => {
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
            errorMessage += 'An unknown error occurred.';
          }

          this.snackBar.open(errorMessage, 'Close', { duration: 50000 });
        }
      );
    }
  }
}

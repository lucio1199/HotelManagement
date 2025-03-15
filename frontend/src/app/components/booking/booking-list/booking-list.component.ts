import { Component, OnInit } from '@angular/core';
import { BookingService } from "../../../services/booking.service";
import { CheckInService } from "../../../services/check-in.service";
import { CheckOutDto } from "../../../dtos/check-in";
import { DetailedBookingDto } from "../../../dtos/booking";
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormsModule, ReactiveFormsModule} from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from "@angular/router";
import { RouterModule } from '@angular/router';
import { CommonModule, DatePipe} from '@angular/common';
import { MatButton } from "@angular/material/button";
import { MatCard } from "@angular/material/card";
import { CurrencyPipe } from "@angular/common";
import {
  MatCell, MatCellDef,
  MatColumnDef,
  MatFooterRow, MatFooterRowDef,
  MatHeaderCell, MatHeaderCellDef,
  MatHeaderRow, MatHeaderRowDef,
  MatRow, MatRowDef,
  MatTable, MatTextColumn
} from "@angular/material/table";
import {MatIcon} from "@angular/material/icon";
import {MatExpansionPanel, MatExpansionPanelTitle} from "@angular/material/expansion";
import { MatExpansionModule } from '@angular/material/expansion';
import {MatDialog} from "@angular/material/dialog";
import {ConfirmDialogComponent} from '../../confirm-dialog/confirm-dialog.component';
import {DialogMode} from "../../confirm-dialog/dialog-mode.enum";
import { FormGroup, FormControl, FormBuilder } from '@angular/forms';
import {MatFormField} from "@angular/material/form-field";
import {
  MatDatepicker,
  MatDatepickerInput,
  MatDatepickerToggle,
  MatDateRangeInput,
  MatDateRangePicker, MatEndDate, MatStartDate
} from "@angular/material/datepicker";
import { MatFormFieldModule } from '@angular/material/form-field';
import {MatInput} from "@angular/material/input";
import {MatOption, MatSelect} from "@angular/material/select";
import {MatSlideToggle, MatSlideToggleChange} from "@angular/material/slide-toggle";
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { BookingDetailsDialogComponent } from '../booking-details-dialog/booking-details-dialog.component';
import {GuestListDto} from '../../../dtos/guest';
import {MatPaginator, PageEvent} from "@angular/material/paginator";
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export function dateValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;

    if (!value) {
      return null;
    }

    const inputDate = new Date(value);
    const minDate = new Date(1990, 0, 1);
    const maxDate = new Date();
    maxDate.setFullYear(maxDate.getFullYear() + 1);

    if (isNaN(inputDate.getTime())) {
      return { invalidDate: true };
    }

    if (inputDate < minDate) {
      return { before1990: true };
    }

    if (inputDate > maxDate) {
      return { after2027: true };
    }
    return null;
  };
}


@Component({
  selector: 'app-my-bookings',
  templateUrl: './booking-list.component.html',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    RouterModule,
    MatButton,
    MatCard,
    MatTable,
    MatColumnDef,
    MatProgressBarModule,
    MatHeaderCell,
    MatCell,
    MatHeaderRow,
    MatRow,
    MatHeaderRowDef,
    MatRowDef,
    MatCellDef,
    MatFormFieldModule,
    MatHeaderCellDef,
    MatIcon,
    CommonModule,
    DatePipe,
    MatExpansionModule,
    MatFormField,
    MatDatepickerInput,
    MatDatepickerToggle,
    MatDatepicker,
    MatInput,
    FormsModule,
    MatPaginator
  ],
  styleUrls: ['./booking-list.component.scss']
})
export class BookingListComponent implements OnInit {

  bookings: DetailedBookingDto[] = [];
  displayedColumns: string[] = ['bookingNumber', 'bookingDetails', 'startDate', 'endDate', 'lastname', 'totalnights', 'checkIn', 'statusCheckIn', 'paidStatus',  'transactionId', 'statusBooking'];
  successMessage: string | null = null;
  today: Date = new Date();
  checkInStatuses: { [bookingId: number]: boolean } = {};
  searchForm: FormGroup;
  maxDate: Date;
  totalBookings: number = 0;
  pageSize: number = 10;
  pageIndex: number = 0;
  selectedRow: any;

  constructor(
    private bookingService: BookingService,
    private snackBar: MatSnackBar,
    private router: Router,
    private route: ActivatedRoute,
    private checkInService: CheckInService,
    private dialog: MatDialog,
    private fb: FormBuilder
  ) { }

  ngOnInit(): void {
    this.maxDate = new Date();
    this.maxDate.setFullYear(this.maxDate.getFullYear() + 100);
    this.searchForm = this.fb.group({
      start: [null, [dateValidator()]],
      end: [null, [dateValidator()]],
      firstName: ['', [this.nameValidator]],
      lastName: ['', [this.nameValidator]],
      roomName: ['', [this.nameValidator]],
      phoneNumber: ['', [this.phoneNumberValidator]],
      dateOfBirth: [null, [this.dateOfBirthValidator]],
      bookingNumber: ['', [this.bookingNumberValidator]],
      email: ['', [this.emailValidator]]
    });
    this.loadBookings();
  }

  openBookingDetailsDialog(booking: DetailedBookingDto): void {
    this.checkInService.getAllGuests(booking.id).subscribe({
      next: (guestList) => {
        const guests: GuestListDto[] = guestList;
        this.dialog.open(BookingDetailsDialogComponent, {
          data: {
            booking: booking,
            guests: guests
          },
        });
      },
      error: (error) => {
        this.dialog.open(BookingDetailsDialogComponent, {
          data: {
            booking: booking,
            guests: null
          },
        });
      }
    });
  }

  loadBookings(): void {
    this.bookingService.getPagedBookings(this.pageIndex, this.pageSize).subscribe(
      (pagedData) => {
        this.bookings = pagedData.content;

        this.bookings.sort((a, b) => {
          if (a.status === 'ACTIVE' && b.status !== 'ACTIVE') {
            return -1;
          }
          if (a.status !== 'ACTIVE' && b.status === 'ACTIVE') {
            return 1;
          }
          return new Date(a.startDate).getTime() - new Date(b.startDate).getTime();
        });

        this.totalBookings = pagedData.totalElements;

        this.bookings.forEach(booking => {
          booking.isActive = this.checkIfActive(booking.startDate, booking.endDate);
            if (booking.status === 'COMPLETED') {
              this.checkInStatuses[booking.id] = true;
            } else {
              this.loadCheckInStatuses(booking.email, booking);
            }
        });
      },
      error => {
        console.error("Error retrieving bookings:", error);
      }
    );
  }


  onPageChanged(event: PageEvent): void {
    this.pageSize = event.pageSize;
    this.pageIndex = event.pageIndex;
    this.loadBookings();
  }


  emailValidator(control: FormControl): { [key: string]: boolean } | null {
    const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
    if (control.value) {
      if (control.value.length > 50) {
        return { 'emailTooLong': true };
      }
      if (!emailPattern.test(control.value)) {
        return { 'invalidEmail': true };
      }
    }
    return null;
  }


  dateOfBirthValidator(control: FormControl): { [key: string]: boolean } | null {
    const dob = new Date(control.value);
    if (isNaN(dob.getTime())) {
      return { 'invalidDate': true };
    }
    const today = new Date();
    if (dob > today) {
      return { 'dateInFuture': true };
    }
    return null;
  }

  phoneNumberValidator(control: FormControl): { [key: string]: boolean } | null {
    const phonePattern = /^\+?[0-9]{1,20}$/;
    if (control.value) {
      if (control.value.length > 20) {
        return { 'phoneNumberTooLong': true };
      }
      if (!phonePattern.test(control.value)) {
        return { 'invalidPhoneNumber': true };
      }
    }
    return null;
  }

  nameValidator(control: FormControl): { [key: string]: boolean } | null {
    const namePattern = /^[a-zA-ZäöüÄÖÜ]+(?: [a-zA-ZäöüÄÖÜ]+)*$/;
    if (control.value) {
      if (control.value.length > 20) {
        return { 'maxLengthExceeded': true };
      }
      if (!namePattern.test(control.value)) {
        return { 'invalidName': true };
      }
    }
    return null;
  }

  bookingNumberValidator(control: FormControl): { [key: string]: boolean } | null {
    const bookingPattern = /^BOOK-[A-Za-z0-9]{1,12}$/;
    if (control.value) {
      const value = control.value;
      if (value.length > 17) {
        return { 'bookingNumberTooLong': true };
      }
      if (!bookingPattern.test(value)) {
        return { 'invalidBookingNumber': true };
      }
    }
    return null;
  }

  onSearch(): void {
    const searchCriteria = this.searchForm.value;

    if (searchCriteria.start && searchCriteria.end) {
      const startDate = new Date(searchCriteria.start);
      const endDate = new Date(searchCriteria.end);
      if (startDate > endDate) {
        this.snackBar.open('The start date must not be after the end date', 'Close', { duration: 3000 });
        return;
      }
    }

    this.bookingService.getBookingsByGuests().subscribe(
      data => {
        const filteredBookings = data.filter(booking => {
          const startMatch = !searchCriteria.start || new Date(booking.startDate) >= new Date(searchCriteria.start);
          const endMatch = !searchCriteria.end || new Date(booking.endDate) <= new Date(searchCriteria.end);
          const firstNameMatch = !searchCriteria.firstName || booking.firstName.toLowerCase().includes(searchCriteria.firstName.toLowerCase());
          const lastNameMatch = !searchCriteria.lastName || booking.lastName.toLowerCase().includes(searchCriteria.lastName.toLowerCase());
          const roomNameMatch = !searchCriteria.roomName || booking.roomName.toLowerCase().includes(searchCriteria.roomName.toLowerCase());
          const phoneNumberMatch = !searchCriteria.phoneNumber || booking.phoneNumber.includes(searchCriteria.phoneNumber);
          const dateOfBirthMatch = !searchCriteria.dateOfBirth || new Date(booking.dateOfBirth).toLocaleDateString() === new Date(searchCriteria.dateOfBirth).toLocaleDateString();
          const bookingNumberMatch = !searchCriteria.bookingNumber || booking.bookingNumber.startsWith(searchCriteria.bookingNumber);
          const emailMatch = !searchCriteria.email || booking.email.toLowerCase().includes(searchCriteria.email.toLowerCase());

          return startMatch && endMatch && firstNameMatch && lastNameMatch && roomNameMatch && phoneNumberMatch && dateOfBirthMatch && bookingNumberMatch && emailMatch;

        });


        this.bookings = filteredBookings;
        this.totalBookings = filteredBookings.length;
      },
      error => {
        console.error("Error in search:", error);
        this.snackBar.open("The search failed. Please try again.", "Close", { duration: 3000 });
      }
    );
  }



  loadCheckInStatuses(email: string, booking: DetailedBookingDto): void {
    this.checkInService.getCheckedInStatus(email).subscribe(
      statuses => {
        const relevantStatus = statuses.find(status => +status.bookingId === booking.id);

        if (!!relevantStatus) {
          if (relevantStatus.email === "invalid") {
            this.checkInStatuses[booking.id] = null;
          } else {
            this.checkInStatuses[booking.id] = true;
          }
        } else {
          this.checkInStatuses[booking.id] = false;
        }
      },
      error => {
        console.error(`Failed to load check-in status for bookingId ${booking.id}:`, error);
        this.snackBar.open("Failed to load check-in status", "Close", { duration: 3000 });
        this.checkInStatuses[booking.id] = false;
      }
    );
  }

  checkIfActive(startDate: string, endDate: string): boolean {
    const now = new Date();
    return new Date(startDate) <= now && now <= new Date(endDate);
  }


  isBookingStarted(startDate: string, endDate: string): boolean {
    const today = new Date();
    const start = new Date(startDate);
    start.setHours(0, 0, 0, 0);
    const end = new Date(endDate);
    end.setHours(23, 59, 59, 999);

    return today >= start && today <= end;
  }

  getTodayDate(): string {
    const today = new Date();
    return today.toLocaleDateString();
  }

  objectKeys(obj: any): string[] {
    return Object.keys(obj);
  }

  manualCheckIn(email: string, bookingId: number): void {
    console.log("manual check in with email: " + email + " and bookingId: " + bookingId);
    this.router.navigate(['/manual-check-in', email, bookingId]);
  }

  confirmManualCheckOut(firstName: string, lastName: string, roomName: string, email: string, bookingId: number): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      height: '200px',
      width: '500px',
      data: { name: "check-out",
              mode: DialogMode.Checkout,
              message: "This will also end the stay for all guests that were added to the room. Are you sure you want the owner " + firstName + " " + lastName + ", staying in '" + roomName + "', " + " to "
            },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.manualCheckOut(email, bookingId);
      }
    });
  }

  manualCheckOut(email: string, bookingId: number): void {
    const checkOut: CheckOutDto = {
      bookingId: bookingId.toString(),
      email: email
    };

    this.checkInService.manualCheckOut(checkOut).subscribe(
      () => {
        this.checkInStatuses[bookingId] = null; // Mark as checked-out
        this.snackBar.open("Guest checked out successfully.", "Close", { duration: 3000 });
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
          errorMessage += error.message || 'An unknown error occurred.';
        }

        this.snackBar.open(errorMessage, 'Close', { duration: 50000 });
      }
    );
  }

  markAsPaid(bookingId: number): void {
    this.bookingService.markAsPaidManually(bookingId).subscribe({
      next: () => {
        this.snackBar.open('Booking marked as paid successfully.', 'Close', {duration: 3000});
        this.loadBookings();
      },
      error: (err) => {
        console.error(err);
        const errorMessage = err.status === 409 ? err.error.message : 'Failed to mark booking as paid.';
        this.snackBar.open(errorMessage, 'Close', {duration: 3000});
      }
    });
  }

  onRowClick(row: any): void {
    this.selectedRow = row;
  }
}


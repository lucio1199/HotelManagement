import { Component, OnInit } from '@angular/core';
import { BookingService } from "../../../services/booking.service";
import { BookingDetailDto } from "../../../dtos/booking";
import { CheckInStatusDto } from "../../../dtos/check-in";
import { MatSnackBar } from '@angular/material/snack-bar';
import {ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import { RouterModule } from '@angular/router';
import {CommonModule} from '@angular/common';
import {MatButton} from "@angular/material/button";
import {MatCard} from "@angular/material/card";
import {CurrencyPipe} from "@angular/common";
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
import {CheckInService} from "../../../services/check-in.service";
import {AuthService} from "../../../services/auth.service";
import {MatDialog} from "@angular/material/dialog";
import {ConfirmDialogComponent} from '../../confirm-dialog/confirm-dialog.component';
import {DialogMode} from "../../confirm-dialog/dialog-mode.enum";
import {UiConfigService} from "../../../services/ui-config.service";
import {PaymentService} from "../../../services/payment.service";
import {PaymentRequestDto} from "../../../dtos/payment-request";
import {MatTooltip} from "@angular/material/tooltip";
import {ActivityBookingService} from "../../../services/activity-booking.service";
import {ActivityBookingDto} from "../../../dtos/activity";

@Component({
  selector: 'app-my-bookings',
  templateUrl: './booking-detail.component.html',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    RouterModule,
    MatButton,
    RouterLink,
    MatCard,
    CurrencyPipe,
    MatTable,
    MatColumnDef,
    MatHeaderCell,
    MatCell,
    MatHeaderRow,
    MatRow,
    MatFooterRow,
    MatHeaderRowDef,
    MatRowDef,
    MatCellDef,
    MatHeaderCellDef,
    MatFooterRowDef,
    MatTextColumn,
    MatIcon,
    CommonModule,
    MatTooltip
  ],
  styleUrls: ['./booking-detail.component.scss']
})
export class BookingDetailComponent implements OnInit {

  bookings: BookingDetailDto[] = [];
  activityBookings: ActivityBookingDto[] = [];
  columnsToDisplay = ['bookingNumber', 'bookingDate', 'roomName', 'startDate', 'endDate', 'bookingStatus', 'actions', 'paidStatus', 'pdf'];
  activityColumnsToDisplay = ['activityName', 'bookingDate', 'startTime', 'endTime', 'date' , 'participants', 'paid', 'actions'];
  successMessage: string | null = null;
  checkedInStatus: CheckInStatusDto[] = [];
  isLoading = false;

  constructor(
    private bookingService: BookingService,
    private snackBar: MatSnackBar,
    private router: Router,
    private route: ActivatedRoute,
    protected checkInService: CheckInService,
    private authService: AuthService,
    protected uiConfigService: UiConfigService,
    private dialog: MatDialog,
    private paymentService: PaymentService,
    private activityBookingService: ActivityBookingService
  ) {
  }

  ngOnInit(): void {
    this.loadMyBookings();
    this.loadMyActivityBookings();
    this.loadCheckedInStatus();

    this.route.paramMap.subscribe(params => {
      const bookingId = params.get('bookingId');
      const currentRoute = this.route.snapshot.url.map(segment => segment.path).join('/');

      if (currentRoute.includes('success') && !currentRoute.includes('activity') && bookingId) {
        console.log(`Success payment route detected for booking ID: ${bookingId}`);
        this.snackBar.open('Payment successful. Thank you!', 'Close', { duration: 3000 });
        this.updateBookingPaymentStatus(+bookingId);
      } else if (currentRoute.includes('my-bookings/cancel') && !currentRoute.includes('activity')) {
        this.snackBar.open('Booking canceled', 'Close', { duration: 3000 });
        console.log('Payment was canceled.');
        this.snackBar.open('Payment was canceled. Your booking remains unpaid.', 'Close', {duration: 3000});
      } else if (currentRoute.includes('success') && bookingId && currentRoute.includes('activity')) {
        console.log(`Success payment route detected for activityBooking ID: ${bookingId}`);
        this.updateActivityBookingPaymentStatus(+bookingId);
        this.snackBar.open('Payment successful. Thank you!', 'Close', { duration: 3000 });
      } else if (currentRoute.includes('cancel') && currentRoute.includes('activity')) {
        this.snackBar.open('Booking canceled', 'Close', { duration: 3000 });
        this.updateActivityBookingPaymentStatus(+bookingId);
      }
    });
  }

  loadMyBookings(): void {
    this.bookingService.getBookingsByUser().subscribe(
      (bookings: BookingDetailDto[]) => {
        this.bookings = bookings.sort((a, b) => {
          const dateA = new Date(a.startDate);
          const dateB = new Date(b.startDate);
          return dateA.getTime() - dateB.getTime();
        });
      },
      (error) => {
        console.error('Error loading bookings', error);
        this.snackBar.open('Error loading bookings', 'Close', {duration: 3000});
      }
    );
  }

  loadMyActivityBookings(): void {
    this.activityBookingService.getBookingByUser().subscribe(
      (bookings: ActivityBookingDto[]) => {
        console.log('Activity bookings:', bookings);
        this.activityBookings = bookings
        console.log('Activity bookings:', this.activityBookings);
      },
      (error) => {
        console.error('Error loading bookings', error);
        this.snackBar.open('Error loading bookings', 'Close', {duration: 3000});
      }
    );
  }

  loadCheckedInStatus(): void {
    this.checkInService.getDigitalCheckedInStatus().subscribe(
      (statusList: CheckInStatusDto[]) => {
        this.checkedInStatus = statusList;
      },
      (error) => {
        console.error('Error loading check-in status', error);
        this.snackBar.open('Error loading check-in status', 'Close', {duration: 3000});
      }
    );
  }

  checkIn(bookingId: number): void {
    console.log("check in, bookingId: " + bookingId);
    this.router.navigate(['/check-in', bookingId]);
  }

  isBookingStarted(startDate: string, endDate: string): boolean {
    const today = new Date();
    const start = new Date(startDate);
    start.setHours(0, 0, 0, 0);
    const end = new Date(endDate);
    end.setHours(23, 59, 59, 999);

    return today >= start && today <= end;
  }

  confirmCheckOut(booking: BookingDetailDto): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      height: '200px',
      width: '500px',
      data: {
        name: "Check-out",
        mode: DialogMode.Checkout,
        message: "This action cannot be undone! Are you sure you want to"
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.checkOut(booking);
      }
    });
  }

  checkOut(booking: BookingDetailDto): void {
    this.checkInService.checkOut({
      bookingId: booking.id.toString(),
      email: this.authService.getUserEmail()
    }).subscribe(
      () => {
        this.successMessage = 'Check-out successful';
        location.reload();
      },
      (error) => {
        console.error('Error checking out', error);
        this.snackBar.open('Error checking out', 'Close', {duration: 3000});
      }
    );
  }

  checkedIn(booking: BookingDetailDto): boolean | null {
    const found = this.checkedInStatus.find(status => +status.bookingId === booking.id);
    if (found) {
      if (this.checkedInStatus.find(status => +status.bookingId === booking.id && status.email === "invalid")) {
        return null;
      }
      return true;
    }
    return false;
  }

  cancelBooking(bookingId: number): void {
    this.bookingService.cancelBooking(bookingId).subscribe(
      () => {
        this.snackBar.open('Booking canceled successfully', 'Close', {duration: 3000});

        this.bookings = this.bookings.map(booking =>
          booking.id === bookingId ? {...booking, status: 'CANCELLED'} : booking
        );
      },
      (error) => {
        if (error.status === 409) {
          const fullMessage = error.error?.message;
          const firstPart = fullMessage.split('.')[0];
          this.snackBar.open(firstPart, "Close", {duration: 3000});
        } else {
          this.snackBar.open('Error canceling booking', 'Close', {duration: 3000});
        }
      }
    );
  }

  confirmCancelBooking(booking: BookingDetailDto): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      height: '200px',
      width: '500px',
      data: {
        name: "Cancel Booking",
        mode: DialogMode.Cancellation,
        message: `Do you really want to cancel your booking for the room "${booking.roomName}"?`,
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.cancelBooking(booking.id);
      }
    });
  }

  downloadBookingPdf(bookingId: number, type: string): void {
    this.bookingService.downloadPdf(bookingId, type).subscribe(
      (response: Blob) => {
        const blob = response;
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = `${type}`;
        a.click();
        window.URL.revokeObjectURL(downloadUrl);
      },
      (error) => {
        console.error('Error downloading PDF', error);
        this.snackBar.open('Error downloading PDF', 'Close', {duration: 3000});
      }
    );
  }

  private updateBookingPaymentStatus(bookingId: number): void {
    console.log('Marking booking as paid');
    this.bookingService.markAsPaid(bookingId).subscribe({
      next: () => {
        console.log('Booking marked as paid')
        this.loadMyBookings();
      },
      error: (err) => console.error('Error marking booking as paid', err),
    });
  }

  private updateActivityBookingPaymentStatus(bookingId: number): void {
    console.log('Marking activityBooking as paid');
    this.activityBookingService.markAsPaid(bookingId).subscribe({
      next: () => {
        console.log('Booking marked as paid')
        this.loadMyActivityBookings()
      },
      error: (err) => console.error('Error marking booking as paid', err),
    });
  }



  payNow(booking: BookingDetailDto): void {
    console.log(`Initiating payment for booking ID: ${booking.id}`);
    const paymentRequest: PaymentRequestDto = {
      bookingId: booking.id,
      roomId: booking.roomId,
    };
    this.isLoading = true;
    this.paymentService.checkout(paymentRequest).subscribe({
      next: (session) => {
        console.log('Redirecting to Stripe checkout:', session.url);
        this.paymentService.redirectToCheckout(session.url);
      },
      error: (error) => {
        this.isLoading = false;
        if (error.status === 409) {
          const fullMessage = error.error?.message || "Payment conflict occurred.";
          const firstPart = fullMessage.split('.')[0];
          this.snackBar.open(firstPart, "Close", {duration: 3000});
        } else {
          this.snackBar.open("Payment failed. Please try again.", "Close", {duration: 3000});
        }
      }
    });
  }

  formatTime(time: string): string {
    if (!time) return '-';

    const date = new Date(`1970-01-01T${time}Z`);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }
}

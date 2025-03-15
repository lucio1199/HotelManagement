import { Component, Inject } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialogActions,
  MatDialogClose,
  MatDialogContent,
  MatDialogRef,
  MatDialogTitle
} from '@angular/material/dialog';
import { CurrencyPipe, NgIf } from "@angular/common";
import { MatButtonModule } from "@angular/material/button";
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { MatError } from '@angular/material/form-field';
import { MatLabel } from '@angular/material/form-field';
import { Router } from '@angular/router';
import { AuthService } from "../../../services/auth.service";
import {GuestListComponent} from '../../guest-list/guest-list.component';

@Component({
  selector: 'app-booking-details-dialog',
  templateUrl: './booking-details-dialog.component.html',
  styleUrls: ['./booking-details-dialog.scss'],
  imports: [
    MatDialogActions,
    MatDialogContent,
    CurrencyPipe,
    MatDialogClose,
    MatButtonModule,
    MatDialogTitle,
    FormsModule,
    MatFormFieldModule,
    MatInputModule,
    NgIf,
    GuestListComponent
  ],
  standalone: true
})
export class BookingDetailsDialogComponent {
  guestEmail: string | null = null;

  constructor(
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private router: Router,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private dialogRef: MatDialogRef<BookingDetailsDialogComponent>
  ) {}

  onClose(): void {
    this.dialogRef.close();
  }

  addGuestToRoom(): void {
    if (!this.guestEmail || !this.isValidEmail(this.guestEmail)) {
      this.snackBar.open('Invalid email address.', 'Close', { duration: 3000 });
      return;
    }
    // Check if the email already exists in the guests list
    if (this.data.guests) {
      const isEmailAlreadyInRoom = this.data.guests.some(
        (guest) => guest.email.toLowerCase() === this.guestEmail!.toLowerCase()
      );

      if (isEmailAlreadyInRoom) {
        this.snackBar.open('This guest is already added to the room.', 'Close', { duration: 3000 });
        return;
      }
    }
    if (this.guestEmail == this.authService.getUserEmail()) {
      this.snackBar.open('Cannot add yourself to the room.', 'Close', { duration: 3000 });
      return;
    }
    this.onClose();
    this.router.navigate(['/add-to-room', this.guestEmail, "manual", true, this.data.booking.id]);
  }

  isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }
}

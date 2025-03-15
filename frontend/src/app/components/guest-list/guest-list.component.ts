import {Component, Input, OnInit, Optional, ViewChild} from '@angular/core';
import { GuestListDto } from '../../dtos/guest';
import { CommonModule } from '@angular/common';
import { DocumentService } from '../../services/document.service';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CheckInService } from '../../services/check-in.service';
import { MatDialogRef } from '@angular/material/dialog';
import { BookingDetailsDialogComponent } from '../booking/booking-details-dialog/booking-details-dialog.component';
import {MatDialog} from "@angular/material/dialog";
import {ConfirmDialogComponent} from '../confirm-dialog/confirm-dialog.component';
import {DialogMode} from "../confirm-dialog/dialog-mode.enum";

@Component({
  selector: 'app-guest-list',
  templateUrl: './guest-list.component.html',
  styleUrls: ['./guest-list.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule
  ]
})
export class GuestListComponent implements OnInit {
  @Input() guests: GuestListDto[] = [];
  @Input() isManagement: boolean = false;
  @Input() bookingId: number | null = null;
  displayedColumns: string[] = ['firstName', 'lastName', 'email'];

  constructor(private documentService: DocumentService,
              private checkInService: CheckInService,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              @Optional() private dialogRef: MatDialogRef<BookingDetailsDialogComponent>) {}

  ngOnInit(): void {
    if (this.isManagement) {
      this.displayedColumns = ['firstName', 'lastName', 'email', 'passport', 'actions'];
    }
  }

  showPassport(email: string): void {
    if (this.bookingId) {
      this.documentService.getPassportByBookingIdAndEmail(this.bookingId, email)
        .subscribe({
          next: (data: Blob) => {
            const url = URL.createObjectURL(data);
            window.open(url, '_blank');
            setTimeout(() => URL.revokeObjectURL(url), 100);
          },
          error: (error) => {
            console.error('Error loading passport:', error);
            this.snackBar.open('Could not load passport.', 'Close', { duration: 5000 });
          }
        });
    } else {
      console.error('Booking ID is not set');
    }
  }

  confirmRemove(email: string): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      height: '200px',
      width: '500px',
      data: { name: "remove this guest from the room",
              mode: DialogMode.Deletion,
              message: "This action cannot be undone! Are you sure you want to"
            },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.remove(email);
      }
    });
  }

  remove(email: string): void {
    if (this.bookingId) {
      this.checkInService.remove(this.bookingId, email)
        .subscribe({
          next: () => {
            this.snackBar.open('Successfully removed the guest from the room.', 'Close', { duration: 5000 });
            if (this.dialogRef) {
              this.dialogRef.close();
            }

          },
          error: (error) => {
            console.error(error);
            let errorMessage = '';

            if (error.status === 404 && error.error) {
              const backendError = error.error;

              if (backendError.errors && typeof backendError.errors === 'object') {
                const fieldErrors = Object.entries(backendError.errors)
                  .map(([field, errors]) => Array.isArray(errors) ? errors.join(', ') : errors)
                  .join(' ');
                errorMessage += fieldErrors;
              } else if (backendError.message) {
                errorMessage += backendError.message;
              }
            } else {
              errorMessage += error.message || 'An unknown error occurred.';
            }

            this.snackBar.open(errorMessage, 'Close', { duration: 5000 });
          }
        });
    } else {
      console.error('Booking ID is not set');
    }
  }
}

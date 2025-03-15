import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { BookingService } from "../../../services/booking.service";
import { BookingCreateDto } from "../../../dtos/booking";
import { ActivatedRoute, Router } from "@angular/router";
import { CommonModule } from '@angular/common';
import { MatSnackBar } from '@angular/material/snack-bar';
import {MatCard} from "@angular/material/card";
import {MatIcon} from "@angular/material/icon";
import {MatDialog} from "@angular/material/dialog";
import {ConfirmDialogComponent} from "../../confirm-dialog/confirm-dialog.component";
import {DialogMode} from "../../confirm-dialog/dialog-mode.enum";
import { Location } from '@angular/common';
import {RoomDetailDto} from "../../../dtos/room";
import {RoomService} from "../../../services/room.service";
import {MatButton} from "@angular/material/button";
import {PaymentService} from "../../../services/payment.service";
import {PaymentRequestDto} from "../../../dtos/payment-request";

@Component({
  selector: 'app-booking-create',
  templateUrl: './booking-create.component.html',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    MatCard,
    MatIcon,
    MatButton
  ],
  styleUrls: ['./booking-create.component.scss']
})
export class BookingCreateComponent implements OnInit {
  public bookingForm: FormGroup;
  public errorMessage: string;

  paymentRequest: PaymentRequestDto;

  public roomDetails!: RoomDetailDto;
  public isLoading = false;
  public totalAmount: number = 0;
  public priceWithoutTax: number = 0;
  public taxAmount: number = 0;
  public taxId: string = '10%';

  constructor(
    private fb: FormBuilder,
    private bookingService: BookingService,
    private roomService: RoomService,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    private location: Location,
    private paymentService: PaymentService
  ) { }

  ngOnInit(): void {
    console.log('Route params:', this.route.snapshot.paramMap.keys);
    console.log('ID found in route:', this.route.snapshot.paramMap.get('id'));
    console.log('Start Date:', this.route.snapshot.queryParamMap.get('startDate'));
    console.log('End Date:', this.route.snapshot.queryParamMap.get('endDate'));

    this.initializeForm();

    this.paymentRequest = {
      bookingId: 0,
      roomId: 0,
    }
    this.route.queryParams.subscribe(params => {
      const roomId = params['roomId'];
      const startDate = params['startDate'];
      const endDate = params['endDate'];

      console.log('Query Params:', params);

      if (roomId) {
        this.bookingForm.patchValue({ roomId: roomId });
        this.paymentRequest.roomId = roomId;
      }
      if (startDate) {
        const formattedStartDate = new Date(startDate).toISOString().split('T')[0];
        this.bookingForm.patchValue({ startDate: formattedStartDate });
      }

      if (endDate) {
        const formattedEndDate = new Date(endDate).toISOString().split('T')[0];
        this.bookingForm.patchValue({ endDate: formattedEndDate });
      }
    });
    this.getRoomDetails();

    this.bookingForm.get('paymentMethod')?.valueChanges.subscribe((value: string) => {
      if (value === 'PayInAdvance') {
        this.bookingForm.patchValue({ payedInAdvance: true });
      } else if (value === 'PayCash') {
        this.bookingForm.patchValue({ payedInAdvance: false });
      }
    });
    this.getRoomDetails();
  }

  private initializeForm() {
    this.bookingForm = this.fb.group({
      roomId: ['', [Validators.required]],
      startDate: ['', [Validators.required]],
      endDate: ['', [Validators.required]],
      paymentMethod: ['', [Validators.required]],
    });
    this.bookingForm.get('startDate')?.valueChanges.subscribe(() => {
      this.calculateTotalAmount();
    });
    this.bookingForm.get('endDate')?.valueChanges.subscribe(() => {
      this.calculateTotalAmount();
    });
  }

  private calculateTotalAmount() {
    const startDate = this.bookingForm.get('startDate')?.value;
    const endDate = this.bookingForm.get('endDate')?.value;
    const pricePerNight = this.roomDetails?.price ?? 0;

    if (startDate && endDate) {
      const start = new Date(startDate);
      const end = new Date(endDate);

      const nights = (end.getTime() - start.getTime()) / (1000 * 3600 * 24);
      this.priceWithoutTax = pricePerNight * nights;
      this.taxAmount = this.priceWithoutTax * 0.10;
      this.totalAmount = this.priceWithoutTax + this.taxAmount;
    } else {
      this.priceWithoutTax = 0;
      this.totalAmount = 0;
      this.taxAmount = 0;
    }
    this.priceWithoutTax = parseFloat((this.priceWithoutTax).toFixed(2));
    this.taxAmount = parseFloat((this.taxAmount).toFixed(2));
    this.totalAmount = parseFloat((this.totalAmount).toFixed(2));
  }

  private getRoomDetails(): void {
    const roomId = this.route.snapshot.paramMap.get('id');
    if (!roomId) {
      this.errorMessage = 'Room ID is missing in the URL.';
      console.error('Room ID is missing.');
      return;
    }

    console.log('Room ID from URL:', roomId);

    this.roomService.findOne(+roomId).subscribe({
      next: (room) => {
        this.roomDetails = room;
        this.bookingForm.patchValue({
          roomId: +roomId,
          startDate: this.route.snapshot.queryParamMap.get('startDate'),
          endDate: this.route.snapshot.queryParamMap.get('endDate')
        });
        this.roomDetails.mainImage = this.getImageSrc(room.mainImage);
        console.log('Room details loaded:', this.roomDetails);
        this.calculateTotalAmount();
      },
      error: (error) => {
        console.error('Error loading room details:', error);
        this.errorMessage = 'Could not load room details.';
      }
    });
  }



  private getImageSrc(base64Image: string): string {
    return 'data:image/jpeg;base64,' + base64Image;
  }

  public onSubmit(): void {
    const roomName = this.route.snapshot.queryParamMap.get('roomName');
    console.log('Room Name passed to dialog:', roomName);
    if (this.bookingForm.valid) {
      const booking: BookingCreateDto = this.bookingForm.value;

      const dialogRef = this.dialog.open(ConfirmDialogComponent, {
        height: '200px',
        width: '400px',
        data: {
          name: roomName,
          mode: DialogMode.Confirmation,
          message: 'Do you really want to book the'
        }
      });

      dialogRef.afterClosed().subscribe((confirmed) => {
        if (confirmed) {
          this.createBooking(booking);
        } else {
          console.log('Booking not confirmed');
        }
      });

    } else {
      this.snackBar.open('Please fill out the form correctly.', '', { duration: 3000 });
    }
  }

  private createBooking(booking: BookingCreateDto) {
    this.isLoading = true;
    this.bookingService.createBooking(booking).subscribe({
      next: (booking ) => {
        if (this.bookingForm.get('paymentMethod')?.value === 'PayInAdvance') {
          this.payForBooking(booking.id);
        } else {

          this.snackBar.open('Booking created successfully', '', {duration: 3000});
          this.router.navigate(['/bookings/my-bookings']);
        }
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Error creating booking:', error);
        this.snackBar.open('An error occurred. Please try again.', '', { duration: 5000 });
        console.error(error);

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

        this.snackBar.open(errorMessage, 'Close', { duration: 5000 });
      }
    });
  }

  payForBooking(bookingId: number): void {
    this.paymentRequest.roomId = this.roomDetails.id;
    this.paymentRequest.bookingId = bookingId;
    this.paymentService.checkout(this.paymentRequest).subscribe({
      next: (session) => {
        this.paymentService.redirectToCheckout(session.url);
      },
      error: (error) => {
        if (error.status === 409) {
          const fullMessage = error.error?.message || "Payment conflict occurred.";
          const firstPart = fullMessage.split('.')[0];
          this.snackBar.open(firstPart, "Close", { duration: 3000 });
        } else {
          this.snackBar.open("Payment failed. Please try again.", "Close", { duration: 3000 });
        }
      }
    });
  }

  goBack(): void {
    this.location.back();
  }
}

import {Component, OnInit, ViewChild} from '@angular/core';
import {MatCard} from "@angular/material/card";
import {ActivityDetailDto, ActivitySlotDto, ActivitySlotSearchDto} from "../../../dtos/activity";
import {ActivatedRoute} from "@angular/router";
import {ActivityService} from "../../../services/activity.service";
import {MatSnackBar} from "@angular/material/snack-bar";
import {
  MatCell, MatCellDef,
  MatColumnDef,
  MatHeaderCell, MatHeaderCellDef,
  MatHeaderRow, MatHeaderRowDef, MatRow, MatRowDef,
  MatTable,
  MatTableDataSource
} from "@angular/material/table";
import {MatPaginator, MatPaginatorModule} from "@angular/material/paginator";
import {CurrencyPipe, DatePipe, formatDate, NgIf} from "@angular/common";
import {MatButton} from "@angular/material/button";
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from "@angular/forms";
import {MatError, MatFormField, MatLabel} from "@angular/material/form-field";
import {
  MatDatepickerModule,
} from "@angular/material/datepicker";
import {MatInput, MatInputModule} from "@angular/material/input";
import {MatNativeDateModule} from "@angular/material/core";
import {MatRadioButton} from "@angular/material/radio";
import {ActivityPaymentRequestDto} from "../../../dtos/payment-request";
import {PaymentService} from "../../../services/payment.service";
import {ActivityBookingService} from "../../../services/activity-booking.service";

@Component({
  selector: 'app-activity-timeslot-list',
  standalone: true,
  imports: [
    MatCard,
    MatPaginatorModule,
    DatePipe,
    MatTable,
    MatHeaderCell,
    MatCell,
    MatColumnDef,
    MatHeaderRow,
    MatRow,
    MatButton,
    MatCellDef,
    MatHeaderCellDef,
    MatHeaderRowDef,
    MatRowDef,
    CurrencyPipe,
    ReactiveFormsModule,
    MatFormField,
    MatInput,
    MatLabel,
    NgIf,
    MatError,
    MatDatepickerModule,
    MatInputModule,
    MatNativeDateModule,
    MatRadioButton,
  ],
  templateUrl: './activity-timeslot-list.component.html',
  styleUrl: './activity-timeslot-list.component.scss'
})
export class ActivityTimeslotListComponent implements OnInit{

  dataSource = new MatTableDataSource<ActivitySlotDto>();
  displayedColumns: string[] = ['date', 'startTime', 'endTime', 'capacity', 'occupied', 'select'];
  totalTimeslots = 0;
  pageSize = 10;
  pageIndex = 0;
  activityId!: number;
  isLoading = false;

  activity: ActivityDetailDto | null = null;

  searchForm: FormGroup;

  calculatedPrice: number | null = null;
  pricePerParticipant: number;

  selectedTimeslot: ActivitySlotDto | null = null;
  selectedDate: string = '-';
  selectedStartTime: string = '-';
  selectedEndTime: string = '-';
  selectedParticipants: number = 1;

  paymentRequest: ActivityPaymentRequestDto = {
    activityId: 0,
    activityBookingId: 0,
  };

  date: Date | null = null;
  participants: number = 1;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private route: ActivatedRoute,
    private service: ActivityService,
    private snackBar: MatSnackBar,
    private formBuilder: FormBuilder,
    private paymentService: PaymentService,
    private activityBookingService: ActivityBookingService
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const rawDate = params['date'] || null;
      this.date = rawDate ? new Date(rawDate) : null;

      this.participants = +params['participants'] || 1;

      if (this.participants) {
        this.updateCalculatedPrice(this.participants);
      }

      console.log('Parsed Date:', this.date, 'Participants:', this.participants);
    });

    this.initializeForm();

    this.activityId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadActivity(this.activityId);
    this.loadTimeslots();

    this.searchForm.valueChanges.subscribe((formValues) => {
      console.log('Form changed:', formValues);
      this.updateCalculatedPrice( formValues.participants);
      this.onSearchSubmit();
    });
    this.onSearchSubmit();
  }

  initializeForm(): void {
    this.searchForm = this.formBuilder.group({
      date: [this.date, [this.futureDateValidator]],
      participants: [this.participants, [Validators.min(1), Validators.max(1000)]]
    });
  }

  loadTimeslots(): void {
    this.service.getTimeSlots(this.activityId, this.pageIndex, this.pageSize).subscribe({
      next: (response) => {
        this.dataSource.data = response.content;
        this.totalTimeslots = response.totalElements;
      },
      error: (err) => {
        console.error('Error loading timeslots:', err);
        this.snackBar.open('Failed to load timeslots.', 'Close', { duration: 3000 });
      },
    });
  }



  loadActivity(activityId: number): void {
    this.service.findOne(activityId).subscribe({
      next: (activity) => {
        this.activity = activity;
        this.pricePerParticipant = activity.price;
        console.log('Activity details loaded:', activity);
      },
      error: (err) => {
        console.error('Error loading activity details:', err);
        this.snackBar.open('Failed to load activity details.', 'Close', { duration: 3000 });
      }
    });
  }

  onPageChange(event: { pageIndex: number; pageSize: number }): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadTimeslots();
  }



  convertToTime(time: string): Date | null {
    if (!time) return null;
    return new Date(`1970-01-01T${time}`);
  }

  onSearchSubmit(): void {
    this.selectedParticipants = this.searchForm.get('participants')?.value || 1;
    const formValues = this.searchForm.value;

    if (!formValues.date && !formValues.participants) {
      this.pageIndex = 0;
      this.loadTimeslots();
      return;
    }

    const searchParams: ActivitySlotSearchDto = {
      date: formValues.date || undefined,
      participants: formValues.participants || undefined,
    };

    this.service.searchTimeSlots(this.activityId, searchParams, this.pageIndex, this.pageSize).subscribe({
      next: (response) => {
        this.dataSource.data = response.content;
        this.totalTimeslots = response.totalElements;
      },
      error: (err) => {
        console.error('Error filtering timeslots:', err);
        this.snackBar.open('Error applying filters. Please try again.', 'Close', { duration: 3000 });
      },
    });
  }


  futureDateValidator(control: AbstractControl): ValidationErrors | null {
    const selectedDate = control.value;
    if (!selectedDate) {
      return null;
    }

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (new Date(selectedDate) < today) {
      return { pastDate: true };
    }

    return null;
  }

  updateCalculatedPrice(participants: number | null): void {
    if (participants && this.pricePerParticipant > 0) {
      this.calculatedPrice = participants * this.pricePerParticipant;
    } else {
      this.calculatedPrice = null;
    }
  }

  selectTimeslot(timeslot: ActivitySlotDto): void {
    this.selectedTimeslot = timeslot;

    const participants = this.searchForm.get('participants')?.value || 1;

    this.selectedDate = timeslot.date
      ? formatDate(new Date(timeslot.date), 'fullDate', 'en-US')
      : '-';
    this.selectedStartTime = timeslot.startTime
      ? new Date(`1970-01-01T${timeslot.startTime}`).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
      : '-';

    this.selectedEndTime = timeslot.endTime
      ? new Date(`1970-01-01T${timeslot.endTime}`).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })
      : '-';
    this.selectedParticipants = participants.toString();
    this.calculatedPrice = this.selectedParticipants * this.pricePerParticipant;
  }

  bookNow(): void {
    if (!this.selectedTimeslot) {
      this.snackBar.open('Please select a timeslot first.', 'Close', { duration: 3000 });
      return;
    }

    const participants = this.searchForm.get('participants')?.value || 1;

    if (this.selectedTimeslot.capacity - this.selectedTimeslot.occupied < participants) {
      this.snackBar.open(
        `Not enough spots available. Adjust participant count.`,
        'Close',
        { duration: 3000 }
      );
      return;
    }

    this.isLoading = true;

    const bookingData = {
      activityId: this.activityId,
      activitySlotId: this.selectedTimeslot.id,
      bookingDate: new Date(),
      participants: this.selectedParticipants,
    };

    this.activityBookingService.bookActivitySlot(bookingData).subscribe({
      next: (booking) => {
        this.payForBooking(booking.id);
        this.loadTimeslots();
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
        this.isLoading = false;
      },
    });
  }

  payForBooking(bookingId: number): void {

    this.paymentRequest.activityId = this.activity.id;
    this.paymentRequest.activityBookingId = bookingId;
    console.log('Payment request:', this.paymentRequest);
    this.paymentService.activityBookingCheckout(this.paymentRequest).subscribe({
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
}


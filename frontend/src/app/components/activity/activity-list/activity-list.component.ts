import {Component, OnInit, ViewChild} from '@angular/core';
import { ActivityService } from "../../../services/activity.service";
import { MatDialog } from '@angular/material/dialog';
import { AuthService } from "../../../services/auth.service";
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivityListDto } from "../../../dtos/activity";
import {MatIcon} from "@angular/material/icon";
import {MatCard, MatCardModule} from "@angular/material/card";
import {
  MatCell, MatCellDef,
  MatColumnDef,
  MatFooterRow, MatFooterRowDef,
  MatHeaderCell, MatHeaderCellDef,
  MatHeaderRow, MatHeaderRowDef,
  MatRow, MatRowDef,
  MatTable, MatTableDataSource, MatTextColumn
} from "@angular/material/table";
import {CurrencyPipe, NgIf} from "@angular/common";
import { CommonModule } from '@angular/common';
import {MatButton, MatIconButton} from "@angular/material/button";
import {ConfirmDialogComponent} from '../../confirm-dialog/confirm-dialog.component';
import {
  FormBuilder,
  FormControl,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';
import {RoomSearchBarComponent} from "../../room/room-search-bar/room-search-bar.component";
import { MatInputModule } from '@angular/material/input';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatFormFieldModule} from '@angular/material/form-field';
import {USER_ROLES} from "../../../dtos/auth-request";
import {MatPaginator} from "@angular/material/paginator";
import {DialogMode} from "../../confirm-dialog/dialog-mode.enum";

@Component({
  selector: 'app-activity-list',
  standalone: true,
  imports: [
    MatIcon,
    MatCard,
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
    CurrencyPipe,
    CommonModule,
    RouterLink,
    MatButton,
    MatIconButton,
    MatFormFieldModule,
    MatDatepickerModule,
    FormsModule,
    MatInputModule,
    ReactiveFormsModule,
    RoomSearchBarComponent,
    NgIf,
    MatCardModule,
    MatPaginator,
  ],
  templateUrl: './activity-list.component.html',
  styleUrl: './activity-list.component.scss'
})
export class ActivityListComponent implements OnInit {
  activities: ActivityListDto[] = [];
  recommendedActivity: ActivityListDto = null;
  columnsToDisplay = ['mainImage', 'name', 'price', 'timeslots', 'actions'];
  successMessage: string | null = null;

  pageIndex: number = 0;
  pageSize: number = 10;
  totalActivities: number = 0;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(private service: ActivityService,
              private dialog: MatDialog,
              protected authService: AuthService,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar,
              private formBuilder: FormBuilder
  ) {
  }

  guestSearchForm = this.formBuilder.group(
    {
      name: [null, [Validators.maxLength(100)]],
      date: [null, [this.futureDateValidator]],
      persons: [null, [Validators.min(1), Validators.max(100)]],
      minPrice: [null, [Validators.min(0), Validators.max(100000)]],
      maxPrice: [null, [Validators.min(0), Validators.max(100000)]],
    },
  );


  onGuestSubmit(): void {
    if (this.guestSearchForm.invalid) {
      this.snackBar.open('Please fill in all required fields.', 'Close', {
        duration: 3000,
      });
      return;
    }

    const formValues = this.guestSearchForm.value;

    const searchParams = {
      name: formValues.name,
      date: formValues.date,
      persons: formValues.persons,
      minPrice: formValues.minPrice,
      maxPrice: formValues.maxPrice,
    };
    console.log(searchParams);

    this.service.searchActivitiesPaginated(searchParams, this.pageIndex, this.pageSize).subscribe({
      next: (response) => {
        this.activities = response.content;
        this.totalActivities = response.totalElements;
        this.activities.forEach((activity) => {
          activity.mainImage = this.getImageSrc(activity.mainImage);
        });
        console.log('Filtered activities loaded successfully:', this.activities);
      },
      error: (err) => {
        console.error('Error loading filtered activities:', err);
        this.snackBar.open('Error loading activities', 'Close', {
          duration: 3000,
        });
      },
    });
  }


  ngOnInit(): void {
    this.loadActivities();
  }

 protected readonly USER_ROLES = USER_ROLES;

  loadActivities() : void {
    if(this.authService.isLoggedIn() && this.authService.getUserRole() === USER_ROLES.GUEST){
      this.service.getRecommended().subscribe({
        next: (activity) => {
        this.recommendedActivity = activity;
          this.recommendedActivity.mainImage = this.getImageSrc(activity.mainImage);
          console.log('Recommended Activity loaded successfully:', this.recommendedActivity);
        },
        error: (err) => {
          console.error('Error loading recommended Activity:', err);
          this.snackBar.open('Error loading recommended Activity', 'Close', {
            duration: 3000,
          });
        }
      });
    }
    this.service.getAllPaginated(this.pageIndex, this.pageSize).subscribe({
      next: (response) => {
        this.activities = response.content;
        this.totalActivities = response.totalElements;
        this.activities.forEach((activity) => {
          activity.mainImage = this.getImageSrc(activity.mainImage);
        });
        console.log('Paginated activities loaded successfully:', this.activities);
      },
      error: (err) => {
        console.error('Error loading activities:', err);
        this.snackBar.open('Error loading activities', 'Close', {
          duration: 3000,
        });
      }
    });
  }

    confirmDelete(activity: ActivityListDto): void {
      const dialogRef = this.dialog.open(ConfirmDialogComponent, {
        height: '200px',
        width: '500px',
        data: { name: activity.name,
                mode: DialogMode.Deletion,
                message: "Are you sure you want to delete"
              },
      });

      dialogRef.afterClosed().subscribe((confirmed) => {
        if (confirmed) {
          this.deleteActivity(activity);
        }
      });
    }

    deleteActivity(activity: ActivityListDto): void {
      this.service.delete(activity.id).subscribe({
        next: () => {
          console.log(`Activity deleted: ${activity.name}`);
          this.loadActivities(); // Refresh list
        },
        error: (error) => {
          console.log('Error deleting activity.');
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

          this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
        },
      });
    }

  getImageSrc(base64Image: string): string {
    return 'data:image/jpeg;base64,' + base64Image;
  }

  formatDayOfWeek(dayOfWeek: string): string {
    if (!dayOfWeek) return '';
    return dayOfWeek.charAt(0).toUpperCase() + dayOfWeek.slice(1).toLowerCase();
  }

  formatTime(time: string): string {
    if (!time) return '';
    const match = time.match(/^(\d{2}:\d{2})/);
    return match ? match[1] : time;
  }

  formatSpecificDate(date: string): string {
    const parsedDate = new Date(date);
    return parsedDate.toLocaleDateString('en-US', {
      day: '2-digit',
      month: 'short',
      year: 'numeric'
    });
  }

  futureDateValidator(control: FormControl): { [key: string]: any } | null {
    const selectedDate = control.value;
    if (!selectedDate) return null;
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return selectedDate < today ? { pastDate: true } : null;
  }

  onPageChange(event: any): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadActivities();
  }
}

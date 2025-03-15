import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {MatButton} from "@angular/material/button";
import {ActivatedRoute, Router, RouterLink} from "@angular/router";
import {MatCard} from "@angular/material/card";
import {RoomAdminSearchDto, RoomListDto, RoomSearchDto} from "../../../dtos/room";
import {CurrencyPipe, NgIf} from "@angular/common";
import {RoomService} from "../../../services/room.service";
import {MatDialog} from "@angular/material/dialog";
import {ConfirmDialogComponent} from '../../confirm-dialog/confirm-dialog.component';
import {DialogMode} from "../../confirm-dialog/dialog-mode.enum";
import {
  MatCell, MatCellDef,
  MatColumnDef,
  MatFooterRow, MatFooterRowDef,
  MatHeaderCell, MatHeaderCellDef,
  MatHeaderRow, MatHeaderRowDef,
  MatRow, MatRowDef,
  MatTable, MatTableDataSource, MatTextColumn
} from "@angular/material/table";
import {MatIcon} from "@angular/material/icon";
import { CommonModule } from '@angular/common';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {provideNativeDateAdapter} from '@angular/material/core';
import {MatDatepickerModule} from '@angular/material/datepicker';
import { MatInputModule } from '@angular/material/input';
import {MatFormFieldModule} from '@angular/material/form-field';
import {RoomSearchBarComponent} from "../room-search-bar/room-search-bar.component";
import {AuthService} from "../../../services/auth.service";
import {USER_ROLES} from "../../../dtos/auth-request";
import { MatSnackBar } from '@angular/material/snack-bar';
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import {MatPaginator} from "@angular/material/paginator";

export function nameOrDescriptionValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null;
    }

    const regex = /^[a-zA-Z]+(\s[a-zA-Z]+)*$/;

    const valid = regex.test(control.value);
    if (!valid) {
      return { invalidNameOrDescription: 'Only letters and a single space between words are allowed' };
    }

    const minLength = 1;
    const maxLength = 60;

    if (control.value.length < minLength) {
      return { minLength: `At least ${minLength} characters required` };
    } else if (control.value.length > maxLength) {
      return { maxLength: `Maximum ${maxLength} characters required` };
    }

    return null;
  };
}

@Component({
  selector: 'app-room-list',
  standalone: true,
  providers: [provideNativeDateAdapter()],
  imports: [
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
    MatHeaderRowDef,
    MatRowDef,
    MatCellDef,
    MatHeaderCellDef,
    CommonModule,
    MatIcon,
    MatFormFieldModule,
    MatDatepickerModule,
    FormsModule,
    MatInputModule,
    ReactiveFormsModule,
    NgIf,
    MatPaginator
  ],
  templateUrl: './room-list.component.html',
  styleUrls: ['./room-list.component.scss']
})
export class RoomListComponent implements OnInit{
  rooms: RoomListDto[] = [];
  noRoomsMessage: string = '';

  fromHome: boolean = false;

  columnsToDisplay = ['mainImage', 'name', 'capacity', 'price', 'actions'];
  successMessage: string | null = null;

  adminSearchForm = new FormGroup({
    name: new FormControl<string | null>(null, [
      nameOrDescriptionValidator()
    ]),
    description: new FormControl<string | null>(null, [
      nameOrDescriptionValidator()
    ]),
    minCapacity: new FormControl<number | null>(null, [
      Validators.min(1),
      Validators.max(10),
      Validators.pattern('^[0-9]*$')
    ]),
    maxCapacity: new FormControl<number | null>(null, [
      Validators.min(1),
      Validators.max(10),
      Validators.pattern('^[0-9]*$')
    ]),
    minPrice: new FormControl<number | null>(null, [
      Validators.min(1),
      Validators.max(10000),
      Validators.pattern('^[0-9]*$')
    ]),
    maxPrice: new FormControl<number | null>(null, [
      Validators.min(1),
      Validators.max(10000),
      Validators.pattern('^[0-9]*$')
    ])
  }, this.priceValidator);

  guestSearchForm = new FormGroup({
      start: new FormControl<Date | null>(null, [Validators.required]),
      end: new FormControl<Date | null>(null, [Validators.required]),
      persons: new FormControl<number | null>(null, [Validators.required, Validators.min(1), Validators.max(6), Validators.pattern('^[0-9]+$')]),
      minPrice: new FormControl<number | null>(null, [
        Validators.min(1),
        Validators.max(10000),
        Validators.pattern('^[0-9]+$')
      ]),
      maxPrice: new FormControl<number | null>(null, [
        Validators.min(1),
        Validators.max(10000),
        Validators.pattern('^[0-9]+$')
      ]),
    }, this.priceValidator);


  pageIndex: number = 0;
  pageSize: number = 10;
  totalRooms: number = 0;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  @ViewChild(RoomSearchBarComponent) searchBar: RoomSearchBarComponent

  constructor(private service: RoomService,
              private dialog: MatDialog,
              protected authService: AuthService,
              private route: ActivatedRoute,
              private router: Router,
              private snackBar: MatSnackBar
  ) {
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['message']) {
        this.successMessage = params['message'];
      }

      if (params['startDate'] && params['endDate'] && params['persons']) {
        this.fromHome = true;
      } else {
        if (this.authService.getUserRole() !== USER_ROLES.ADMIN) {
          this.router.navigate(['']);
        }
      }

      this.guestSearchForm.patchValue({
        start: params['startDate'],
        end: params['endDate'],
        persons: params['persons'],
        minPrice: params['minPrice'],
        maxPrice: params['maxPrice']
      });
    });

    this.guestSearchForm.valueChanges.subscribe(() => {
      if (this.guestSearchForm.invalid) {
        this.rooms = [];
        this.noRoomsMessage = '';
        this.snackBar.open('No available rooms for the selected inputs, please change your inputs!', 'Close', { duration: 3000 });
      }
    });

    if (this.fromHome) {
      this.onGuestSubmit();
    } else {
      this.onAdminSubmit();
    }

  }


  validateDateRange() {
    const startControl = this.guestSearchForm.get('start');
    const endControl = this.guestSearchForm.get('end');

    const startDate = startControl?.value ? new Date(startControl.value) : null;
    const endDate = endControl?.value ? new Date(endControl.value) : null;

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const oneYearFromToday = new Date();
    oneYearFromToday.setFullYear(today.getFullYear() + 1);

    if (startDate) {
      if (startDate < today) {
        startControl?.setErrors({ dateInvalid: 'Date cannot be in the past' });
      } else if (startDate > oneYearFromToday) {
        startControl?.setErrors({ dateExceedsOneYear: 'Date cannot exceed one year from today' });
      } else {
        startControl?.setErrors(null);
      }
    }

    if (startDate && endDate) {
      if (startDate.getTime() === endDate.getTime()) {
        endControl?.setErrors({ dateEqual: 'End date must be at least 1 day after start date' });
      } else if (endDate < startDate) {
        endControl?.setErrors({ dateRangeInvalid: 'End date must be after start date.' });
      } else if (endDate > oneYearFromToday) {
        endControl?.setErrors({ dateExceedsOneYear: 'Date cannot exceed one year from today' });
      } else {
        endControl?.setErrors(null);
      }
    }
  }


  priceValidator(formGroup: FormGroup) {
    const minPrice = formGroup.get('minPrice')?.value;
    const maxPrice = formGroup.get('maxPrice')?.value;

    if (minPrice !== null && (minPrice < 1 || minPrice > 10000)) {
      formGroup.get('minPrice')?.setErrors({ minPriceInvalid: 'Min price must be between 1 and 10000.' });
    } else {
      formGroup.get('minPrice')?.setErrors(null);
    }

    if (maxPrice !== null && (maxPrice < 1 || maxPrice > 10000)) {
      formGroup.get('maxPrice')?.setErrors({ maxPriceInvalid: 'Max price must be between 1 and 10000.' });
    } else {
      formGroup.get('maxPrice')?.setErrors(null);
    }

    if (minPrice && maxPrice) {
      if (minPrice === maxPrice) {
        formGroup.get('maxPrice')?.setErrors({ priceEqual: 'Min price cannot be equal to Max price' });
      } else if (minPrice > maxPrice) {
        formGroup.get('maxPrice')?.setErrors({ priceInvalid: 'Max price must be greater than Min price' });
      }
    }

    return null;
  }

  onGuestSubmit() {
    const searchValues = this.guestSearchForm.value;

    if (searchValues.minPrice !== null && searchValues.minPrice !== undefined && isNaN(searchValues.minPrice)) {
      this.guestSearchForm.get('minPrice')?.setErrors({ invalidNumber: 'Min Price must be a valid number' });
    }

    if (searchValues.maxPrice !== null && searchValues.maxPrice !== undefined && isNaN(searchValues.maxPrice)) {
      this.guestSearchForm.get('maxPrice')?.setErrors({ invalidNumber: 'Max Price must be a valid number' });
    }

    if (this.guestSearchForm.invalid) {
      this.guestSearchForm.markAllAsTouched();
      return;
    }

    const searchDto: RoomSearchDto = {
      startDate: searchValues.start || null,
      endDate: searchValues.end || null,
      persons: searchValues.persons || 0,
      minPrice: searchValues.minPrice || null,
      maxPrice: searchValues.maxPrice || null,
    };

    this.rooms = [];
    this.noRoomsMessage = '';

    this.router.navigate(['/rooms'], {
      queryParams: {
        startDate: searchValues.start instanceof Date ? searchValues.start.toLocaleDateString('en-CA') : searchValues.start,
        endDate: searchValues.end instanceof Date ? searchValues.end.toLocaleDateString('en-CA') : searchValues.end,
        persons: searchValues.persons,
        minPrice: searchValues.minPrice || undefined,
        maxPrice: searchValues.maxPrice || undefined,
      },
    });

    this.service.searchRooms(searchDto, this.pageIndex, this.pageSize).subscribe({
      next: rooms => {
        this.rooms = rooms.content;
        this.totalRooms = rooms.totalElements;
        if (this.rooms.length === 0) {
          this.noRoomsMessage = 'No available rooms for the selected inputs, please change your inputs!';
          this.snackBar.open(this.noRoomsMessage, 'Close', { duration: 3000 });
        } else {
          this.noRoomsMessage = '';
        }
        this.rooms.forEach(room => {
          room.mainImage = this.getImageSrc(room.mainImage);
        });
      },
      error: () => {
        this.rooms = [];
        this.snackBar.open('An error occurred while searching for rooms', 'Close', { duration: 3000 });
      },
    });
  }

  onAdminSubmit() {
    const searchValues = this.adminSearchForm.value;

    if (searchValues.minPrice !== null && searchValues.minPrice !== undefined && isNaN(searchValues.minPrice)) {
      this.adminSearchForm.get('minPrice')?.setErrors({ invalidNumber: 'Min Price must be a valid number' });
    }

    if (searchValues.maxPrice !== null && searchValues.maxPrice !== undefined && isNaN(searchValues.maxPrice)) {
      this.adminSearchForm.get('maxPrice')?.setErrors({ invalidNumber: 'Max Price must be a valid number' });
    }

    if (this.adminSearchForm.invalid) {
      this.adminSearchForm.markAllAsTouched();
      return;
    }

    const searchDto: RoomAdminSearchDto = {
      name: searchValues.name || '',
      description: searchValues.description || '',
      minCapacity: searchValues.minCapacity || 0,
      maxCapacity: searchValues.maxCapacity || 0,
      minPrice: searchValues.minPrice || 0,
      maxPrice: searchValues.maxPrice || 0,
    };

    this.service.managerSearch(searchDto, this.pageIndex, this.pageSize).subscribe({
      next: rooms => {
        this.rooms = rooms.content;
        this.totalRooms = rooms.totalElements;
        if (this.rooms.length === 0) {
          this.noRoomsMessage = 'No rooms found for the selected inputs, please change your inputs';
          this.snackBar.open(this.noRoomsMessage, 'Close', { duration: 3000 });
        } else {
          this.noRoomsMessage = '';
        }

        this.rooms.forEach(room => {
          room.mainImage = this.getImageSrc(room.mainImage);
        });
      },
      error: (error) => {
         console.log(error);
         this.rooms = [];
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

        this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
        this.router.navigate(['/']);
      },
    });
  }

  getImageSrc(base64Image: string): string {
    return 'data:image/jpeg;base64,' + base64Image;
  }

  confirmDelete(room: RoomListDto): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      height: '200px',
      width: '500px',
      data: { name: room.name,
              mode: DialogMode.Deletion,
              message: "Are you sure you want to delete"
            },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.deleteRoom(room);
      }
    });
  }

  deleteRoom(room: RoomListDto): void {
    this.service.delete(room.id).subscribe({
      next: () => {
        console.log(`Room deleted: ${room.name}`);
        this.onAdminSubmit(); // Refresh list
      },
      error: (error) => {
        console.log(error);
        let errorMessage = '';
        if ((error.status === 422 || error.status === 409 )&& error.error) {
          const backendError = error.error;
          errorMessage += backendError.message.split('.')[0];
        } else {
          errorMessage += error.message || 'An unknown error occurred.';
        }

        this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
      },
    });
  }
  onPageChange(event: any): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    if (this.fromHome) {
      this.onGuestSubmit();
    } else {
      this.onAdminSubmit();
    }
  }

  protected readonly USER_ROLES = USER_ROLES;


  protected readonly name = name;
}

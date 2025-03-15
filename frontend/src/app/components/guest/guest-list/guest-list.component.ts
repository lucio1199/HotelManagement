import {Component, OnInit, ViewChild} from '@angular/core';
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import {MatTableModule} from "@angular/material/table";
import {MatIconModule} from "@angular/material/icon";
import {CommonModule} from "@angular/common";
import {ActivatedRoute, RouterLink} from "@angular/router";
import {MatDialog} from "@angular/material/dialog";
import {GuestService} from "../../../services/guest.service";
import {ConfirmDialogComponent} from "../../confirm-dialog/confirm-dialog.component";
import {DialogMode} from "../../confirm-dialog/dialog-mode.enum";
import {GuestListDto, GuestSearchDto} from "../../../dtos/guest";
import {MatSnackBar} from "@angular/material/snack-bar";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatInput} from "@angular/material/input";
import {MatPaginator} from "@angular/material/paginator";

@Component({
  selector: 'app-guest-list',
  standalone: true,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatTableModule,
    MatIconModule,
    CommonModule,
    RouterLink,
    FormsModule,
    MatFormField,
    MatInput,
    MatLabel,
    ReactiveFormsModule,
    MatPaginator,
  ],
  templateUrl: './guest-list.component.html',
  styleUrl: './guest-list.component.scss'
})
export class GuestListComponent implements OnInit{

  guests: GuestListDto[] = [];
  filteredGuests: GuestListDto[] = [];
  columnsToDisplay = ['firstName', 'lastName', 'email', 'actions'];
  successMessage: string | null = null;
  searchForm = new FormGroup({
    firstName: new FormControl<string | null>(null),
    lastName: new FormControl<string | null>(null),
    email: new FormControl<string | null>(null),
  });

  pageIndex: number = 0;
  pageSize: number = 10;
  totalGuests: number = 0;
  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private service: GuestService,
    private dialog: MatDialog,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.getGuests();

    this.route.queryParams.subscribe((params) => {
      if (params['message']) {
        this.successMessage = params['message'];
      }
    });
    console.log(this.guests)
  }

  getGuests(): void {
    this.service.getAllGuests(this.pageIndex, this.pageSize).subscribe({
      next: (guests) => {
        console.log('guest.content:', guests.content)
        this.guests = guests.content;
        this.totalGuests = guests.totalElements;
      },
      error: (error) => {
        console.error('Error loading employees', error);
        this.snackBar.open('Error loading guests', 'Ok', {
          duration: 3000});
      },
    });
  }

  confirmDelete(guest: GuestListDto): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      height: '200px',
      width: '500px',
      data: { name: guest.firstName + ' ' + guest.lastName,
        mode: DialogMode.Deletion,
        message: "Are you sure you want to delete"
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.deleteGuest(guest);
      }
    });
  }


  deleteGuest(guest: GuestListDto): void {
    this.service.deleteGuest(guest).subscribe({
      next: () => {
        console.log(`guest deleted: ${guest.firstName} ${guest.lastName}`);
        this.snackBar.open('Guest deleted', 'Ok', {
          duration: 3000});
        this.getGuests();
      },
      error: (err) => {
        console.error('Error deleting guest', err);
        if (err.status === 422 && err.error?.errors) {
          const validationErrors = err.error.errors;
          validationErrors.forEach((error: string) => {
            this.snackBar.open('Error deleting guest: ' + error, 'Ok', { duration: 3000 });

          });
        } else {
          this.snackBar.open('Error saving configuration' + err.message, 'Ok', {duration: 3000});
        }
        },
    });
  }

  onSubmit(): void {
    const searchValues = this.searchForm.value;
    const searchDto: GuestSearchDto = {
      firstName: searchValues.firstName || null,
      lastName: searchValues.lastName || null,
      email: searchValues.email || null,
    }

    this.service.searchGuests(searchDto, this.pageIndex, this.pageSize).subscribe({
      next: (guests) => {
        this.guests = guests.content;
        this.totalGuests = guests.totalElements;
      },
      error: (error) => {
        console.error('Error loading guests', error);
        this.snackBar.open('Error loading guests', 'Ok', {duration: 3000});
      },
    });
  }

  onPageChange(event: any): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.getGuests();
  }
}

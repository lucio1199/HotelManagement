import {Component, OnInit} from '@angular/core';
import {MatButtonModule} from "@angular/material/button";
import {MatCardModule} from "@angular/material/card";
import {MatTableModule} from "@angular/material/table";
import {MatIconModule} from "@angular/material/icon";
import {CommonModule} from "@angular/common";
import {EmployeeListDto, RoleType} from "../../../dtos/employee";
import {EmployeeService} from "../../../services/employee.service";
import {MatDialog} from "@angular/material/dialog";
import {ActivatedRoute, RouterLink} from "@angular/router";
import {ConfirmDialogComponent} from "../../confirm-dialog/confirm-dialog.component";
import {DialogMode} from "../../confirm-dialog/dialog-mode.enum";
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [
    MatButtonModule,
    MatCardModule,
    MatTableModule,
    MatIconModule,
    CommonModule,
    RouterLink,
  ],
  templateUrl: './employee-list.component.html',
  styleUrls: ['./employee-list.component.scss'],
})
export class EmployeeListComponent implements OnInit {
  employees: EmployeeListDto[] = [];
  columnsToDisplay = ['firstName', 'lastName', 'phoneNumber', 'roleType', 'actions']; // Include roleType
  successMessage: string | null = null;

  constructor(
    private service: EmployeeService,
    private dialog: MatDialog,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.getEmployees();

    this.route.queryParams.subscribe((params) => {
      if (params['message']) {
        this.successMessage = params['message'];
      }
    });

    console.log(this.employees)
  }

  getEmployees(): void {
    this.service.getAllEmployees().subscribe({
      next: (employees) => {
        this.employees = employees;
        console.log(employees);
      },
      error: (error) => {
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

        this.snackBar.open(errorMessage, 'Close', { duration: 5000 });
      },
    });
  }

  employeeIsNotAdmin(employee: EmployeeListDto) {
    return employee.roleType !== RoleType.ROLE_ADMIN;
  }

  confirmDelete(employee: EmployeeListDto): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      height: '200px',
      width: '500px',
      data: {
        name: `${employee.firstName} ${employee.lastName}`,
        mode: DialogMode.Deletion,
        message: 'Are you sure you want to delete this employee?',
      },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.deleteEmployee(employee);
      }
    });
  }

  deleteEmployee(employee: EmployeeListDto): void {
    this.service.deleteEmployee(employee.id).subscribe({
      next: () => {
        console.log(`Employee deleted: ${employee.firstName} ${employee.lastName}`);
        this.getEmployees();
      },
      error: (error) => {
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

        this.snackBar.open(errorMessage, 'Close', { duration : 5000});
      },
    });
  }
}


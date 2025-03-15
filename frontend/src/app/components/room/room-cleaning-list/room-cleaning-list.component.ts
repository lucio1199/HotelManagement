import {Component, OnInit, ViewChild} from '@angular/core';
import { MatButton } from "@angular/material/button";
import { RouterLink } from "@angular/router";
import { MatCard } from "@angular/material/card";
import { RoomListCleanDto, OccupancyDto } from "../../../dtos/room";
import { CurrencyPipe, DatePipe, CommonModule } from "@angular/common";
import { RoomService } from "../../../services/room.service";
import { MatDialog } from "@angular/material/dialog";
import { ConfirmDialogComponent } from '../../../components/confirm-dialog/confirm-dialog.component';
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
import { MatIcon } from "@angular/material/icon";
import { MatSnackBar } from '@angular/material/snack-bar';
import { CheckInService } from "../../../services/check-in.service";
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import {MatPaginator} from "@angular/material/paginator";
@Component({
  selector: 'app-room-cleaning-list',
  standalone: true,
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
    MatFooterRow,
    MatHeaderRowDef,
    MatRowDef,
    MatCellDef,
    MatHeaderCellDef,
    MatFooterRowDef,
    MatTextColumn,
    MatIcon,
    CommonModule,
    MatSlideToggleModule,
    MatPaginator,
  ],
  templateUrl: './room-cleaning-list.component.html',
  styleUrl: './room-cleaning-list.component.scss',
  providers: [DatePipe]
})
export class RoomCleaningListComponent implements OnInit {
  rooms: RoomListCleanDto[] = [];
  columnsToDisplay = ['mainImage', 'name', 'occupancy', 'status', 'lastCleanedAt', 'actions'];
  highlightedRooms: Map<number, boolean> = new Map();
  occupiedIds: number[] = [];
  occupiedStatus: string[] = [];

  showOnlyFreeRooms: boolean = false;
  cleaningProcessMap: Map<number, boolean> = new Map();
  reloadRooms:boolean = false;

  pageIndex: number = 0;
  pageSize: number = 10;
  totalRooms: number = 0;


  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(private service: RoomService,
              private dialog: MatDialog,
              private datePipe: DatePipe,
              private snackBar: MatSnackBar,
              private checkInService: CheckInService) {
  }

  ngOnInit(): void {
    this.getRooms();
    this.loadCleaningProcessMap();
    this.checkIfRoomCleaningTimesStillRelevant();
  }

  getRooms() {
    this.occupiedStatus = [];
    this.service.getAllForClean(this.pageIndex, this.pageSize).subscribe({
      next: (rooms) => {
        this.totalRooms = rooms.totalElements;
        this.rooms = rooms.content.map(room => ({
          ...room,
          mainImage: this.getImageSrc(room.mainImage)
        }));
        console.log(rooms);
        if (this.showOnlyFreeRooms) {
          this.rooms = this.rooms.filter(room =>
            !this.occupiedIds.includes(room.id)
          );
          console.log(new Date().toLocaleString());
          console.log("Filtered rooms:", this.rooms);
        }
         this.checkIfRoomCleaningTimesStillRelevant();
         this.rooms.forEach(room => {
         this.getOccupancyStatus(room.id);
         });
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

  checkIfRoomCleaningTimesStillRelevant(){
    const now = new Date();
    this.rooms.forEach((room) => {
      if(new Date(room.cleaningTimeTo) < now && room.cleaningTimeTo != null){
        this.deleteCleaningTime(room);
        };
      });
  }

  loadCleaningProcessMap(): void {
    const storedData = localStorage.getItem('cleaningProcessMap');
    if (storedData) {
      this.cleaningProcessMap = new Map(JSON.parse(storedData));
    }
  }

  saveCleaningProcessMap(): void {
    localStorage.setItem('cleaningProcessMap', JSON.stringify(Array.from(this.cleaningProcessMap.entries())));
  }

  getImageSrc(base64Image: string): string {
    return 'data:image/jpeg;base64,' + base64Image;
  }

  formatDate(date: string): string {
    const roomDate = new Date(date);
    const currentDate = new Date();

    currentDate.setHours(0, 0, 0, 0);
    roomDate.setHours(0, 0, 0, 0);

    const diffTime = currentDate.getTime() - roomDate.getTime();
    const oneDay = 24 * 60 * 60 * 1000;

    if (diffTime < oneDay) {
      return `Today at ${this.datePipe.transform(date, 'HH:mm')}`;
    } else if (diffTime < 2 * oneDay) {
      return `Yesterday at ${this.datePipe.transform(date, 'HH:mm')}`;
    } else{
        return `${diffTime/oneDay} days ago at ${this.datePipe.transform(date, 'HH:mm')}` || '';

    }
  }

  confirmStartClean(room: RoomListCleanDto): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      height: '200px',
      width: '500px',
      data: { name: room.name,
              mode: DialogMode.Confirmation,
              message: "Are you sure you want to start cleaning"
            },
    });
    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.startClean(room);
        this.snackBar.open('Room cleaning started successfully!', 'Close', { duration: 3000 });
      }
    });
  }

  confirmEndClean(room: RoomListCleanDto): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      height: '200px',
      width: '500px',
      data: { name: room.name,
              mode: DialogMode.Confirmation,
              message: "Are you sure you want to finish cleaning"
            },
    });
    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.cleanNow(room);
        this.snackBar.open('Room cleaning completed successfully!', 'Close', { duration: 3000 });
      }
    });
  }

  startClean(room: RoomListCleanDto){
      this.cleaningProcessMap.set(room.id, true);
      this.saveCleaningProcessMap();
      this.getRooms();
  }

  cleanNow(room: RoomListCleanDto): void {
    console.log("Room with id " + room.id + " is being marked as clean");
    this.cleaningProcessMap.set(room.id, false);
    this.saveCleaningProcessMap();
    this.service.updateRoomLastCleanedAt(room.id).subscribe({
      next: (updatedRoom) => {
        console.log('Updated room:', updatedRoom);
        this.reloadRooms = true;
        this.deleteCleaningTime(room);
        this.highlightedRooms.set(room.id, true);
        setTimeout(() => this.highlightedRooms.set(room.id, false), 1000);
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

  deleteCleaningTime(room: RoomListCleanDto): void {
    this.service.deleteRoomCleaningTime(room.id).subscribe({
      next: () => {
        console.log(`Room cleaning time deleted: ${room.name}`);
        if(this.reloadRooms){
          this.reloadRooms = false;
          this.getRooms();
        }
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

        this.snackBar.open(errorMessage, 'Close', { duration : 5000 });
      },
    });
  }

  getOccupancyStatus(roomId: number): void {
    this.service.getOccupancyStatus(roomId).subscribe(
      (status: OccupancyDto) => {
        console.log("getOccupancyStatus(" + roomId + ")");
        console.log("result: " + status.status);
        this.occupiedStatus.push(status.status);
        if(status.status == 'occupied'){
          this.occupiedIds.push(status.roomId);
          }
      },
      (error) => {
        this.snackBar.open('Cannot fetch Occupancy for Room with id:' + roomId + ': ' + error.message, 'Close', { duration: 5000 });

      }
    );
  }
  onToggleChange(isChecked: boolean): void {
    this.showOnlyFreeRooms = isChecked;
    this.getRooms();
  }

  onPageChange(event: any): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.getRooms()
  }

}

import {Component, OnInit} from '@angular/core';
import {CheckInService} from '../../services/check-in.service';
import {Router, ActivatedRoute} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';
import {BookingDetailDto} from '../../dtos/booking';
import {CheckOutDto} from '../../dtos/check-in'
import {InviteToRoomDto} from '../../dtos/invite'
import {MatDialog} from "@angular/material/dialog";
import {ConfirmDialogComponent} from '../confirm-dialog/confirm-dialog.component';
import {DialogMode} from "../confirm-dialog/dialog-mode.enum";
import {myRoomDto, RoomDetailDto, RoomListDto} from '../../dtos/room';
import {RoomService } from '../../services/room.service';
import {UiConfigService} from "../../services/ui-config.service";
import {KeyService} from "../../services/key.service";
import {AuthService} from "../../services/auth.service";
import {GuestListDto} from '../../dtos/guest';
import {GuestListComponent} from '../guest-list/guest-list.component';

@Component({
  selector: 'app-my-room',
  templateUrl: './my-room.component.html',
  styleUrls: ['./my-room.component.scss']
})
export class MyRoomComponent implements OnInit {
  rooms: myRoomDto[] = [];
  selectedRoomIndex: number = 0;
  selectedRoom: myRoomDto | null = null
  booking: BookingDetailDto | null = null;
  canCheckOut: boolean = false;
  choseTodaysCleaningTimeMap: Map<number, boolean> = new Map();
  timeOptions: string[] = [];
  keyStatus: string[] = [];
  nukiEnabled: boolean = false;
  digitalCheckInEnabled: boolean = false;
  guestEmail: string | null = null;
  guests: GuestListDto[] = [];
  isOwner: boolean = false;

  constructor(
    private checkInService: CheckInService,
    private roomService: RoomService,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar,
    private dialog: MatDialog,
    protected uiConfigService: UiConfigService,
    private keyService: KeyService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.fetchNukiModuleStatus();
    this.fetchDigitalCheckInModuleStatus();
    this.fetchRoomDetails();
    this.checkInService.resetCheckedIn();
    this.generateTimeOptions();
  }

  fetchNukiModuleStatus(): void {
    this.uiConfigService.nukiIsEnabled().subscribe({
     next: (result) => {
       this.nukiEnabled = result;
     },
     error: (error) => {
       console.error(`Failed to get nuki module status`, error);
       this.snackBar.open(
         'Failed to get module status.',
         'Close',
         { duration: 5000 }
       );
     }
   });
  }

  fetchDigitalCheckInModuleStatus(): void {
    this.uiConfigService.digitalCheckInIsEnabled().subscribe({
     next: (result) => {
       this.digitalCheckInEnabled = result;
     },
     error: (error) => {
       console.error(`Failed to get module status`, error);
       this.snackBar.open(
         'Failed to get digital check-in module status.',
         'Close',
         { duration: 5000 }
       );
     }
   });
  }

  generateTimeOptions(): void {
    const end = 24 * 60;
    const interval = 20;
    const now = new Date();
    const currentMinutes = now.getHours() * 60 + now.getMinutes();
    const currentMinutesFloorToInterval = Math.floor(currentMinutes/interval)*interval;

    this.timeOptions = [];

    for (let minutes = currentMinutesFloorToInterval + interval; minutes < end; minutes += interval) {
      const hours = Math.floor(minutes / 60)
        .toString()
        .padStart(2, '0');
      const mins = (minutes % 60).toString().padStart(2, '0');
      this.timeOptions.push(`${hours}:${mins}`);
    }
  }

  fetchRoomDetails(): void {
    this.checkInService.getGuestRooms().subscribe({
      next: (rooms) => {
        console.log('rooms:', rooms);
        this.rooms = rooms;
        this.initializeTodaysCleaningTimeMap();
        if (this.rooms.length > 0) {
          // Check for navigation data or URL state
          const isFromCheckIn = this.route.snapshot.queryParams['fromCheckIn'] === 'true';

          if (isFromCheckIn) {
            // If the page was opened from a check-in, set the last room as the selected one
            this.selectedRoomIndex = this.rooms.length - 1;
            this.selectedRoom = this.rooms[this.selectedRoomIndex];
          } else {
            // Default behavior
            this.selectedRoom = this.rooms[0];
            this.selectedRoomIndex = 0;
          }
          this.rooms.forEach((room) => {
            room.mainImage = this.getImageSrc(room.mainImage);
            this.loadCleaningTimes(room);

            // Fetch key status for each room
            this.keyService.getStatus(room.id).subscribe({
              next: (status) => {
                room.keyStatus = status.status;
              },
              error: (error) => {
                room.keyStatus = 'Unknown';
              }
            });
          });

          this.checkInService.getGuestBooking(this.selectedRoom.id).subscribe({
            next: (booking) => {
              this.booking = booking;
              console.log("booking: " + booking.id);
            },
            error: () => {
              console.log("Could not retrieve booking, because guest is not the owner.");
            }
          });

          this.fetchOwnerStatus(this.selectedRoom.id);
          this.fetchGuestsForRoom(this.selectedRoom.id);
        }
      },
      error: () => {
        this.router.navigate(['/']);
        this.snackBar.open('You are not checked in.', 'Close', { duration: 3000 });
      }
    });
  }

  fetchGuestsForRoom(roomId: number): void {
    this.checkInService.getGuests(roomId).subscribe({
      next: (guestList) => {
        this.guests = guestList;

      },
      error: (error) => {
      }
    });
  }

  fetchOwnerStatus(id: number): void {
    this.checkInService.isOwner(id).subscribe({
     next: (result) => {
       this.isOwner = result;
     },
     error: (error) => {
       this.isOwner = false;
       console.error(`Failed to get owner status`, error);
       this.snackBar.open(
         'Failed to get owner status.',
         'Close',
         { duration: 5000 }
       );
     }
   });
  }

  confirmOpenDoor(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      height: '200px',
      width: '500px',
      data: { name: "Do you want to open the door",
              mode: DialogMode.Confirmation,
              message: "Are you sure?"
            },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        console.log("open door");
        this.openDoor();
      }
    });
  }

  openDoor(): void {
    if (!this.selectedRoom) {
      this.snackBar.open('No room selected.', 'Close', { duration: 3000 });
      return;
    }

    // Attempt to unlock the selected room
    this.keyService.unlock(this.selectedRoom.id).subscribe({
      next: () => {
        this.snackBar.open('Door opened successfully!', 'Close', { duration: 3000 });
      },
      error: (error) => {
        console.error(`Failed to unlock door for room ${this.selectedRoom?.id}:`, error);
        this.snackBar.open(
          'Failed to open the door. Please try again or contact support.',
          'Close',
          { duration: 10000 }
        );
      }
    });
  }

  getImageSrc(base64Image: string): string {
    return 'data:image/jpeg;base64,' + base64Image;
  }

  onRoomTabChange(index: number): void {
    this.selectedRoomIndex = index;
    this.selectedRoom = this.rooms[index];
    this.loadCleaningTimes(this.selectedRoom);
    if (this.selectedRoom) {
      this.fetchGuestsForRoom(this.selectedRoom.id); // Fetch guests when room changes
      this.fetchOwnerStatus(this.selectedRoom.id);
    }
  }

  markAsReadyToClean(room: myRoomDto): void {

    if (!this.selectedRoom.cleaningFrom || !this.selectedRoom.cleaningTo) {
      this.snackBar.open('Please provide both "From" and "To" times.', 'Close', { duration: 3000 });
      return;
    }

    if(this.selectedRoom.cleaningFrom > this.selectedRoom.cleaningTo) {
        this.snackBar.open('Please make sure that "From" is earlier than "To".', 'Close', { duration: 3000 });
        return;
    }

    if(this.convertToMinutes(this.selectedRoom.cleaningTo)-this.convertToMinutes(this.selectedRoom.cleaningFrom) < 20) {
        this.snackBar.open('Please make sure that "From" and "To" are at least 20min apart.', 'Close', { duration: 3000 });
        return;
    }

    const today = new Date().toLocaleDateString('en-CA');
    const now = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    if(this.selectedRoom.cleaningFrom < now){
      this.snackBar.open('Please choose a later time, it is already ' + now + ".", 'Close', { duration: 3000 });
      return;
    }


    this.roomService.updateRoomCleaningTime(this.selectedRoom.id, this.selectedRoom.cleaningFrom, this.selectedRoom.cleaningTo)
      .subscribe({
        next: (response: RoomListDto) => {
          localStorage.setItem('cleaningFrom'+this.selectedRoom.id.toString(), this.selectedRoom.cleaningFrom);
          localStorage.setItem('cleaningTo'+this.selectedRoom.id.toString(), this.selectedRoom.cleaningTo);
          localStorage.setItem('cleaningDate'+this.selectedRoom.id.toString(), today);
          console.log('cleaning time successfully updated:', response);
          this.loadCleaningTimes(room);
          this.snackBar.open(
            `Room marked as ready to clean from ${this.selectedRoom.cleaningFrom} to ${this.selectedRoom.cleaningTo}.`,
            'Close',
            { duration: 3000 }
          );
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
        }
      });
  }

  loadCleaningTimes(room: myRoomDto): void {
    if (!this.selectedRoom || !this.selectedRoom.id) {
      console.warn("Room or Room ID is missing, cannot load cleaning times.");
      return;
    }
    const storedFrom = localStorage.getItem('cleaningFrom' + room.id.toString());
    const storedTo = localStorage.getItem('cleaningTo' + room.id.toString());
    const storedDate = localStorage.getItem('cleaningDate' + room.id.toString());
    if (storedFrom && storedTo && storedDate) {
      const today = new Date().toLocaleDateString('en-CA');
      if (today === storedDate) {
        room.cleaningFrom = storedFrom;
        room.cleaningTo = storedTo;
        this.setTodaysCleaningTimeForRoom(room.id);
      }
    }
  }

  initializeTodaysCleaningTimeMap() {
    this.rooms.forEach((room, index) => {
      this.choseTodaysCleaningTimeMap.set(room.id, false);
    });
  }

  setTodaysCleaningTimeForRoom(roomId: number) {
    this.choseTodaysCleaningTimeMap.set(roomId, true);
  }

  getTodaysCleaningTimeForRoom(roomId: number): boolean | undefined {
    return this.choseTodaysCleaningTimeMap.get(roomId);
  }


  convertToMinutes(time: string): number {
    const parts: string[] = time.split(":");
    return Number(parts[0]) * 60 + Number(parts[1]);
  }

  confirmCheckOut(room: myRoomDto): void {
    var message: string = "";
    if (this.guests === undefined || this.guests.length === 0) {
      message = "This action cannot be undone! Are you sure you want to";
    } else {
      message = "This action cannot be undone! This will also end the stay for every added guest! Are you sure you want to";
    }

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      height: '200px',
      width: '500px',
      data: { name: "Check-out",
              mode: DialogMode.Checkout,
              message: message
            },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        console.log("check out: " + this.selectedRoom.id);
        this.checkInService.getGuestBooking(this.selectedRoom.id).subscribe({
          next: (booking) => {
            this.booking = booking;
            console.log("check out: " + booking.id);
            this.checkOut(this.booking.id);
          },
          error: () => {
            this.snackBar.open('Could not retrieve booking.', 'Close', { duration: 3000 });
          }
        });
      }
    });
  }

  checkOut(bookingId: number): void {
    const checkOut: CheckOutDto = {
      bookingId: bookingId.toString(),
      email: null
    };

    this.checkInService.checkOut(checkOut).subscribe(
      () => {
        this.router.navigate(['']);
        this.snackBar.open("Checked out successfully.", "Close", { duration: 3000 });
        this.checkInService.resetCheckedIn();
      },
      (error) => {
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

        this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
      }
    );
  }

  confirmAddGuestToRoom(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      height: '200px',
      width: '500px',
      data: { name: "add this guest to your room",
              mode: DialogMode.Confirmation,
              message: "This action cannot be undone! Are you sure you want to"
            },
    });

    dialogRef.afterClosed().subscribe((confirmed) => {
      if (confirmed) {
        this.addGuestToRoom();
      }
    });
  }

  addGuestToRoom(): void {
    if (this.digitalCheckInEnabled) {
      if (!this.guestEmail || !this.isValidEmail(this.guestEmail)) {
        this.snackBar.open('Invalid email address.', 'Close', { duration: 3000 });
        return;
      }
      // Check if the email already exists in the guests list
      const isEmailAlreadyInRoom = this.guests.some(
        (guest) => guest.email.toLowerCase() === this.guestEmail!.toLowerCase()
      );

      if (isEmailAlreadyInRoom) {
        this.snackBar.open('This guest is already added to the room.', 'Close', { duration: 3000 });
        return;
      }

      if (this.guestEmail == this.authService.getUserEmail()) {
        this.snackBar.open('Cannot add yourself to the room.', 'Close', { duration: 3000 });
        return;
      }
      const invite: InviteToRoomDto = {
        bookingId: this.booking.id.toString(),
        email: this.guestEmail,
        ownerEmail: this.authService.getUserEmail()
      };

      console.log("invite.id: " + invite.bookingId);
      console.log("invite.email: " + invite.email);
      console.log("invite.ownerEmail: " + invite.ownerEmail);

      this.checkInService.inviteToRoom(invite).subscribe(
        () => {
          this.snackBar.open("Invited to room successfully.", "Close", { duration: 3000 });
          this.guestEmail = null;
        },
        (error) => {
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
          } else if (error.status === 404 && error.error) {
            errorMessage += error.error;
          } else {
            errorMessage += error.message || 'An unknown error occurred.';
          }

          this.snackBar.open(errorMessage, 'Close', { duration: 3000 });
        }
      );
    }
  }

  isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }
}

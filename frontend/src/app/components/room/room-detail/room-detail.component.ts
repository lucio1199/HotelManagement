import { Component, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { RoomDetailDto } from "../../../dtos/room";
import { RoomService } from "../../../services/room.service";
import {ActivatedRoute, RouterLink} from '@angular/router';
import { CommonModule } from '@angular/common';
import {Router} from "@angular/router";
import { Location } from '@angular/common';
import {AuthService} from '../../../services/auth.service';
import {USER_ROLES} from "../../../dtos/auth-request";
import {NgIf} from "@angular/common";
import {CheckInService} from "../../../services/check-in.service";

@Component({
  selector: 'app-room-detail',
  standalone: true,
  imports: [MatCardModule, MatButtonModule, MatIconModule, CommonModule, RouterLink],
  templateUrl: './room-detail.component.html',
  styleUrl: './room-detail.component.scss'
})
export class RoomDetailComponent implements OnInit {
  room!: RoomDetailDto;
  currentImageIndex: number = 0;
  startDate: string | null = null;
  endDate: string | null = null;

  constructor(
    private service: RoomService,
    private route: ActivatedRoute,
    private router: Router,
    private location: Location,
    public authService: AuthService,
    protected checkInService: CheckInService

  ) {}

  ngOnInit(): void {
    this.startDate = this.route.snapshot.queryParamMap.get('startDate');
    this.endDate = this.route.snapshot.queryParamMap.get('endDate');

    if (this.startDate) {
      const localStartDate = new Date(this.startDate).toLocaleDateString('en-CA');
      this.startDate = localStartDate;
    }

    if (this.endDate) {
      const localEndDate = new Date(this.endDate).toLocaleDateString('en-CA');
      this.endDate = localEndDate;
    }

    console.log('Start Date:', this.startDate);
    console.log('End Date:', this.endDate);

    this.getRoom();
    this.checkInService.resetCheckedIn();
  }

  protected readonly USER_ROLES = USER_ROLES;

  getRoom(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.service.findOne(+id).subscribe({
        next: (room) => {
          this.room = room;
          room.mainImage = this.getImageSrc(room.mainImage);

          room.additionalImages = room.additionalImages.map(this.getImageSrc);
          console.log('Room details loaded:', room);
        },
        error: (error) => {
          console.error('Error loading room details', error);
        }
      });
    }
  }

  getImageSrc(base64Image: string): string {
    return 'data:image/jpeg;base64,' + base64Image;
  }

  get currentImage(): string {
    return this.currentImageIndex === 0
      ? this.room?.mainImage
      : this.room?.additionalImages[this.currentImageIndex - 1];
  }

  nextImage(): void {
    if (this.currentImageIndex < this.room?.additionalImages.length) {
      this.currentImageIndex++;
    } else {
      this.currentImageIndex = 0;
    }
  }

  previousImage(): void {
    if (this.currentImageIndex > 0) {
      this.currentImageIndex--;
    } else {
      this.currentImageIndex = this.room?.additionalImages.length;
    }
  }

  goBack(): void {
    this.location.back();
  }
}

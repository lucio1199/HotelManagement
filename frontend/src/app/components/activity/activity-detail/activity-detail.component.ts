import { Component, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { ActivityDetailDto } from "../../../dtos/activity";
import { ActivityService } from "../../../services/activity.service";
import {ActivatedRoute, RouterLink} from '@angular/router';
import { CommonModule } from '@angular/common';
import {Router} from "@angular/router";
import { Location } from '@angular/common';
import {AuthService} from '../../../services/auth.service';
import {USER_ROLES} from "../../../dtos/auth-request";
import {NgIf} from "@angular/common";


@Component({
  selector: 'app-activity-detail',
  standalone: true,
  imports: [MatCardModule, MatButtonModule, MatIconModule, CommonModule, RouterLink
  ],
  templateUrl: './activity-detail.component.html',
  styleUrl: './activity-detail.component.scss'
})
export class ActivityDetailComponent implements OnInit {
  activity!: ActivityDetailDto;
  currentImageIndex: number = 0;

  constructor(
    private service: ActivityService,
    private route: ActivatedRoute,
    private router: Router,
    private location: Location,
    public authService: AuthService,
  ) {}


 ngOnInit(): void {
    this.getActivity();
  }

  protected readonly USER_ROLES = USER_ROLES;

  getActivity(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.service.findOne(+id).subscribe({
        next: (activity) => {
          this.activity = activity;
          activity.mainImage = this.getImageSrc(activity.mainImage);

          activity.additionalImages = activity.additionalImages.map(this.getImageSrc);
          console.log('Activity details loaded:', activity);
        },
        error: (error) => {
          console.error('Error loading activity details', error);
        }
      });
    }
  }

  getImageSrc(base64Image: string): string {
    return 'data:image/jpeg;base64,' + base64Image;
  }

  get currentImage(): string {
    return this.currentImageIndex === 0
      ? this.activity?.mainImage
      : this.activity?.additionalImages[this.currentImageIndex - 1];
  }

  nextImage(): void {
    if (this.currentImageIndex < this.activity?.additionalImages.length) {
      this.currentImageIndex++;
    } else {
      this.currentImageIndex = 0;
    }
  }

  previousImage(): void {
    if (this.currentImageIndex > 0) {
      this.currentImageIndex--;
    } else {
      this.currentImageIndex = this.activity?.additionalImages.length;
    }
  }

  formatDayOfWeek(dayOfWeek: string): string {
    if (!dayOfWeek) return '';
    return dayOfWeek.charAt(0).toUpperCase() + dayOfWeek.slice(1).toLowerCase();
  }

  formatTime(time: string): string {
    if (!time) return '';
    const match = time.match(/^(\d{2}:\d{2})/); // Sucht nach "HH:mm" im String
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

  get displayedTimeslots(): any[] {
    if (this.activity?.timeslots.length === 7) {
      return [{ dayOfWeek: 'Daily', startTime: this.activity.timeslots[0].startTime, endTime: this.activity.timeslots[0].endTime }];
    }
    return this.activity?.timeslots || [];
  }


  goBack(): void {
    this.location.back();
  }

  goToTimeslots(): void {
    this.router.navigate(['/activities/timeslots/', this.activity.id]); // Navigate to the timeslots page
  }
}

import { Component, OnInit } from '@angular/core';
import { UiConfigService } from '../../services/ui-config.service';
import { UiConfigHomepageDto } from '../../dtos/ui-config';
import { MatSnackBar } from '@angular/material/snack-bar';
import {DomSanitizer, SafeResourceUrl} from "@angular/platform-browser";
import {USER_ROLES} from "../../dtos/auth-request";
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import {MatCard} from "@angular/material/card";
import {MatFormField, MatSuffix} from "@angular/material/form-field";
import {MatIcon} from "@angular/material/icon";
import {MatInput} from "@angular/material/input";
import {MatButton, MatIconButton} from "@angular/material/button";
import {MatLabel} from "@angular/material/form-field";
import {NgIf} from "@angular/common";
import {Router, RouterLink} from "@angular/router";
import { MatFormFieldModule } from '@angular/material/form-field';


@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  imports: [
    MatDatepickerModule,
    MatNativeDateModule,
    MatCard,
    MatFormField,
    ReactiveFormsModule,
    MatIcon,
    MatInput,
    MatButton,
    MatIconButton,
    MatLabel,
    MatSuffix,
    NgIf,
    MatFormFieldModule,
    RouterLink
  ],
  standalone: true
})
export class HomeComponent implements OnInit {
  uiConfig: UiConfigHomepageDto | null = null;
  convertedImages: string[] = [];
  currentImageIndex: number = 0;
  googleMapsUrl!: SafeResourceUrl;

  searchForm = new FormGroup({
    startDate: new FormControl<Date | null>(null, [Validators.required, this.dateValidator]),
    endDate: new FormControl<Date | null>(null, [Validators.required, this.dateValidator]),
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



  constructor(
    private uiConfigService: UiConfigService,
    private snackBar: MatSnackBar,
    private sanitizer: DomSanitizer,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUiConfig();
    this.searchForm.get('startDate')?.valueChanges.subscribe(() => this.validateDateRange());
    this.searchForm.get('endDate')?.valueChanges.subscribe(() => this.validateDateRange());
  }

  loadUiConfig(): void {
    this.uiConfigService.getUiConfigHomepage().subscribe({
      next: (config) => {
        this.uiConfig = config;
        this.convertImages(config.images);
        this.setGoogleMapsUrl(config.address);
      },
      error: (err) => {
        console.error('Error loading homepage configuration:', err);
        this.showSnackBar('Error loading homepage configuration', 'Close');
      },
    });
  }

  convertImages(images: string[]): void {
    this.convertedImages = images.map((base64Image) => {
      return 'data:image/jpeg;base64,' + base64Image;
    });
  }

  get currentImage(): string {
    return this.convertedImages[this.currentImageIndex];
  }

  nextImage(): void {
    if (this.currentImageIndex < this.convertedImages.length - 1) {
      this.currentImageIndex++;
    } else {
      this.currentImageIndex = 0; // Loop back to the first image
    }
  }

  previousImage(): void {
    if (this.currentImageIndex > 0) {
      this.currentImageIndex--;
    } else {
      this.currentImageIndex = this.convertedImages.length - 1; // Loop back to the last image
    }
  }

  setGoogleMapsUrl(address: string): void {
    const baseUrl = 'https://www.google.com/maps/embed/v1/place';
    const apiKey = 'AIzaSyCuks2egWFhOFmOWDn27tpWw_nZgDLreDw'; // Replace with your Google Maps API Key
    this.googleMapsUrl = this.sanitizer.bypassSecurityTrustResourceUrl(
      `${baseUrl}?q=${encodeURIComponent(address)}&key=${apiKey}`
    );
  }

  showSnackBar(message: string, action: string): void {
    this.snackBar.open(message, action, {
      duration: 3000, // Duration in milliseconds
      horizontalPosition: 'right', // Align to the right
      verticalPosition: 'top', // Align to the top
      panelClass: ['custom-snackbar'] // Optional: Add custom styling
    });
  }

    protected readonly USER_ROLES = USER_ROLES;

  onSubmit() {
    const searchValues = this.searchForm.value;

    if (searchValues.minPrice !== null && isNaN(searchValues.minPrice)) {
      this.searchForm.get('minPrice')?.setErrors({ invalidNumber: 'Min Price must be a valid number' });
    }

    if (searchValues.maxPrice !== null && isNaN(searchValues.maxPrice)) {
      this.searchForm.get('maxPrice')?.setErrors({ invalidNumber: 'Max Price must be a valid number' });
    }

    if (this.searchForm.valid) {
    this.router.navigate(['/rooms'], {
      queryParams: {
        startDate: searchValues.startDate?.toLocaleDateString('en-CA'),
        endDate: searchValues.endDate?.toLocaleDateString('en-CA'),
        persons: searchValues.persons,
        minPrice: searchValues.minPrice,
        maxPrice: searchValues.maxPrice
      }
    });
  }
}

  dateValidator(control: FormControl) {
    const today = new Date();

    today.setHours(0, 0, 0, 0);

    const value = control.value;

    if (value) {
      if (value < today) {
        return { dateInvalid: 'Date cannot be in the past' };
      }

      const oneYearLater = new Date(today);
      oneYearLater.setFullYear(today.getFullYear() + 1);

      if (value > oneYearLater) {
        return { dateInvalid: 'Date cannot exceed one year from today' };
      }
    }

    return null;
  }

  validateDateRange() {
    const startDate = this.searchForm.get('startDate')?.value;
    const endDate = this.searchForm.get('endDate')?.value;

    if (startDate && endDate) {
      startDate.setHours(0, 0, 0, 0);
      endDate.setHours(0, 0, 0, 0);

      const differenceInTime = endDate.getTime() - startDate.getTime();
      const differenceInDays = differenceInTime / (1000 * 3600 * 24);

      if (differenceInDays < 1) {
        this.searchForm.get('endDate')?.setErrors({ dateRangeInvalid: 'End date must be at least 1 day after start date' });
      } else {
        this.searchForm.get('endDate')?.setErrors(null);
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
}

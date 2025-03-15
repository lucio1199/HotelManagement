import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors} from '@angular/forms';
import { UiConfigService } from '../../services/ui-config.service';
import { UiConfig } from '../../dtos/ui-config';
import {MatCard} from "@angular/material/card";
import {MatFormField, MatLabel} from "@angular/material/form-field";
import {MatCheckbox} from "@angular/material/checkbox";
import {MatButton, MatIconButton} from "@angular/material/button";
import {NgForOf, NgIf} from "@angular/common";
import {MatIcon} from "@angular/material/icon";
import {MatInput} from "@angular/material/input";
import {ActivatedRoute} from "@angular/router";
import { MatDialog } from '@angular/material/dialog';
import {ConfirmDialogComponent} from "../confirm-dialog/confirm-dialog.component";
import {DialogMode} from "../confirm-dialog/dialog-mode.enum";
import {MatSnackBar} from "@angular/material/snack-bar";
import { Location } from '@angular/common';
import { MatError } from '@angular/material/form-field';

@Component({
  selector: 'app-ui-config',
  imports: [
    MatCard,
    ReactiveFormsModule,
    MatFormField,
    MatCheckbox,
    MatButton,
    MatIconButton,
    NgForOf,
    MatIcon,
    MatLabel,
    MatInput,
    MatError,
    NgIf
  ],
  templateUrl: './ui-config.component.html',
  standalone: true,
  styleUrls: ['./ui-config.component.scss']
})
export class UiConfigComponent implements OnInit {
  uiConfigForm!: FormGroup;
  uiConfig: UiConfig = {
    id: 1,
    hotelName: '',
    descriptionShort: '',
    description: '',
    address: '',
    roomCleaning: false,
    digitalCheckIn: false,
    activities: false,
    communication: false,
    nuki: false,
    halfBoard: false,
    priceHalfBoard: 0,
    images: []
  }
  uploadedImages: string[] = [];

  constructor(
    private fb: FormBuilder,
    private uiConfigService: UiConfigService,
    private route: ActivatedRoute,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private location: Location
  ) {}

  ngOnInit(): void {
    this.uiConfigForm = this.fb.group({
      hotelName: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      descriptionShort: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(1000)]],
      address: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100), this.addressValidator]],
      roomCleaning: [false],
      digitalCheckIn: [false],
      activities: [false],
      communication: [false],
      nuki: [false],
      halfBoard: [false],
      priceHalfBoard: [{ value: '', disabled: false }, [Validators.min(0)]]
    });
    this.loadConfig();
  }

  loadConfig(): void {
    this.route.params.subscribe(params => {

      this.uiConfigService.getUiConfig().subscribe({
        next: uiConfig => {
          this.uiConfigForm.patchValue(uiConfig);
          this.uiConfig = {
            ...uiConfig,
            images: []
          };
          if (uiConfig.images) {
            uiConfig.images.forEach((imageContent) => {
              const binary = atob(imageContent);
              const array = []
              for (let i = 0; i < binary.length; i++) {
                array.push(binary.charCodeAt(i));
              }
              const blob = new Blob([new Uint8Array(array)], {type: 'image/jpeg'});
              this.uploadedImages.push(URL.createObjectURL(blob));
              this.uiConfig.images.push(new File([blob], 'image.jpg', {type: 'image/jpeg'}));
            });
          }
        },
        error: (err) => {
          console.error('Error loading configuration:', err);
          this.snackBar.open('Error loading configuration', 'Close', { duration: 3000 });

        },
      });
    });
  }

  onHalfBoardToggle(event: any): void {
    const priceControl = this.uiConfigForm.get('priceHalfBoard');
    if (event.checked) {
      priceControl?.enable();
    } else {
      priceControl?.disable();
      priceControl?.setValue('');
    }
  }

  onFileSelected(event: Event): void {
    const files = (event.target as HTMLInputElement).files;
    const MAX_FILE_SIZE_MB = 1; // Max file size in MB
    const allowedTypes = ['image/jpeg', 'image/png'];

    if (files) {
      Array.from(files).forEach(file => {
        // Check MIME type
        if (!allowedTypes.includes(file.type)) {
          this.snackBar.open(
            `Invalid file type: ${file.type}. Allowed types are JPEG and PNG.`,
            'Close',
            { duration: 3000, panelClass: 'snack-error' }
          );
          return;
        }

        // Check file size
        const fileSizeInMB = file.size / (1024 * 1024);
        if (fileSizeInMB > MAX_FILE_SIZE_MB) {
          this.snackBar.open(
            `File size exceeds the 1 MB limit: ${file.name} (${fileSizeInMB.toFixed(2)} MB).`,
            'Close',
            { duration: 3000, panelClass: 'snack-error' }
          );
          return;
        }

        // Read and display the file
        const reader = new FileReader();
        reader.onload = () => {
          this.uiConfig.images.push(file); // Push original file for upload
          this.uploadedImages.push(reader.result as string); // Display preview
        };
        reader.readAsDataURL(file);
      });
    }
  }




  removeUploadedImage(index: number): void {
    this.uploadedImages.splice(index, 1);
    this.uiConfig.images.splice(index, 1);
  }

  goBack(): void {
    this.location.back();
  }

  addressValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value as string;
    if (!value || (!value.includes('Austria') && !value.includes('Österreich'))) {
      return { invalidAddress: 'Address must be in Austria.' };
    }
    return null;
  }

  onSubmit(): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '400px',
      data: {
        name: '',
        mode: DialogMode.Confirmation,
        message: 'Do you want to save the changes'
      }
    });
    dialogRef.afterClosed().subscribe(confirmed => {
      if (confirmed) {

        this.uiConfig.hotelName = this.uiConfigForm.get('hotelName')?.value;
        this.uiConfig.descriptionShort = this.uiConfigForm.get('descriptionShort')?.value;
        this.uiConfig.description = this.uiConfigForm.get('description')?.value;
        this.uiConfig.address = this.uiConfigForm.get('address')?.value;
        this.uiConfig.roomCleaning = this.uiConfigForm.get('roomCleaning')?.value;
        this.uiConfig.digitalCheckIn = this.uiConfigForm.get('digitalCheckIn')?.value;
        this.uiConfig.activities = this.uiConfigForm.get('activities')?.value;
        this.uiConfig.communication = this.uiConfigForm.get('communication')?.value;
        this.uiConfig.nuki = this.uiConfigForm.get('nuki')?.value
        this.uiConfig.halfBoard = this.uiConfigForm.get('halfBoard')?.value;
        this.uiConfig.priceHalfBoard = this.uiConfigForm.get('priceHalfBoard')?.value;

        this.uiConfigService.updateUiConfig(this.uiConfig).subscribe({
          next: () => {
            console.log('Configuration saved successfully!');
            this.snackBar.open('Configuration updated successfully!', 'Close', { duration: 3000 });

          },
          error: err => {
            console.error('Error saving configuration:', err);
            if (err.status === 422 && err.error?.errors) {
              const validationErrors = err.error.errors;
              validationErrors.forEach((error: string) => {
                this.snackBar.open('Validation Error' + error, 'Close', { duration: 3000 });

              });
            } else {
              this.snackBar.open('Error saving configuration' + err.message, 'Close', { duration: 3000 });

            }
          },
        });
      }
    });
  }
}

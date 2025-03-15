import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatButton, MatIconButton} from "@angular/material/button";
import {ActivityService} from "../../../services/activity.service";
import {Activity, DayOfWeek, ActivityTimeslotInfo} from "../../../dtos/activity";
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from "@angular/material/form-field";
import {MatCard} from "@angular/material/card";
import {CommonModule} from "@angular/common";
import {MatIcon} from "@angular/material/icon";
import {ActivatedRoute, Router} from "@angular/router";
import { MatSnackBar } from '@angular/material/snack-bar';
import { Location } from '@angular/common';
import {ToastrService} from "ngx-toastr";
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import {MatCheckboxModule} from "@angular/material/checkbox";
import {Category} from "../../../dtos/activity";

export enum ActivityCreateEditMode {
  create,
  edit
}

@Component({
  selector: 'app-activity-create-edit',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormsModule,
    MatButton,
    MatFormFieldModule,
    MatInputModule,
    MatCard,
    CommonModule,
    MatIcon,
    MatIconButton,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatCheckboxModule
  ],
  templateUrl: './activity-create-edit.component.html',
  styleUrl: './activity-create-edit.component.scss'
})
export class ActivityCreateEditComponent implements OnInit{
  mode: ActivityCreateEditMode = ActivityCreateEditMode.create;
    selectedTimeSlotType: string | null = null;
    selectedButton: string | null = null;

  activity: Activity = {
    id: null,
    name: '',
    description: '',
    capacity: 0,
    mainImage: null,
    price: 0,
    additionalImages: [],
    timeslotsInfos: [],
    categories: ''
  };

  timeslots: ActivityTimeslotInfo[] = [];
  daysOfWeek: DayOfWeek[] = [
    DayOfWeek.monday, DayOfWeek.tuesday, DayOfWeek.wednesday,
    DayOfWeek.thursday, DayOfWeek.friday, DayOfWeek.saturday, DayOfWeek.sunday
  ];

  timeOptions: string[] = [];
  weeklyDays: string[] = [];
  weeklyFroms: any[] = [];
  weeklyTos: any[] = [];

  uploadedImage: string | ArrayBuffer;
  additionalImages: string[] = [];

  activityForm! : FormGroup;
  categories: Category[] = [];

    private atLeastOneCategoryValidator(): ValidatorFn {
      return (formGroup: AbstractControl): ValidationErrors | null => {
        const controls = (formGroup as FormGroup).controls;
        const isAtLeastOneSelected = Object.values(controls).some(control => control.value === true);
        return isAtLeastOneSelected ? null : { noCategorySelected: true };
      };
    }

  constructor(private service: ActivityService,
              private router: Router,
              private route: ActivatedRoute,
              private formBuilder: FormBuilder,
              private snackBar: MatSnackBar,
              private location: Location,
              private toastr: ToastrService
) {
  }
  selectTimeSlotType(type: string): void {
    this.selectedTimeSlotType = type;
    this.selectedButton = type;

    if (type === 'weekly') {
      if (!this.activityForm.contains('dayOfWeek')) {
        //this.activityForm.addControl('dayOfWeek'), new FormControl('', [Validators.required]));
      }
    } else {
      if (this.activityForm.contains('dayOfWeek')) {
        this.activityForm.removeControl('dayOfWeek');
      }
    }
  }

  addWeeklyDay(): void {
    // Add a new empty entry to the array for the new weekly day
    this.weeklyDays.push(null);
    this.weeklyFroms.push('00:00');
    this.weeklyTos.push('00:00');
  }

  removeWeeklyDay(index: number): void {
    if (this.weeklyDays.length > 1) {
      this.weeklyDays.splice(index, 1);
      this.weeklyFroms.splice(index, 1);
      this.weeklyTos.splice(index, 1);
    }
  }
  onDaySelectionChange(index: number): void {
    // Handle the event when a day is selected

  }

    convertTimeToMinutes(time: Date): number {
      const [hours, minutes] = time.toString().split(':').map(num => parseInt(num, 10));
      return (hours * 60) + minutes;
    }

    onTimeSelectionChange(index: number, field: 'from' | 'to', value: string): void {
      if (field === 'from') {
        this.weeklyFroms[index] = value;
      } else if (field === 'to') {
        this.weeklyTos[index] = value;
      }
    }

  generateTimeOptions(): void {
    const end = 24 * 60;
    const interval = 20;
    const currentMinutesFloorToInterval = 0;

    this.timeOptions = [];

    for (let minutes = currentMinutesFloorToInterval; minutes < end; minutes += interval) {
      const hours = Math.floor(minutes / 60)
        .toString()
        .padStart(2, '0');
      const mins = (minutes % 60).toString().padStart(2, '0');
      this.timeOptions.push(`${hours}:${mins}`);
    }
  }

  onSubmit() {
  console.log("aufgerufen");
    if(this.activityForm.invalid){
      return;
    }
    if(this.selectedTimeSlotType == 'specificDate'){
      // Convert to GMT+0 (UTC)
      const date = new Date(this.activityForm.get('specificDate').value) || null;
      date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
      const startTime = this.convertTimeToMinutes(this.activityForm.get('fromTime')?.value);
      const endTime = this.convertTimeToMinutes(this.activityForm.get('toTime')?.value);

      if (startTime >= endTime) {
        this.snackBar.open('"To" must be later than "From"', 'Close', { duration: 3000 });
        return;
      } else if(this.activityForm.get('specificDate').value == null){
        this.snackBar.open("Date cannot be empty", 'Close', { duration: 3000 });
        return;
      } else if (date.getTime() + startTime * 60000 < Date.now()) {
        this.snackBar.open("This Time has already passed", 'Close', { duration: 3000 });
        return;
      }

      const timeslot: ActivityTimeslotInfo = {
        id: null,
        dayOfWeek: this.activityForm.get('dayOfWeek')?.value || null,
        specificDate: date,
        startTime: this.activityForm.get('fromTime')?.value,
        endTime: this.activityForm.get('toTime')?.value
    };
    this.timeslots.push(timeslot);

    } else if(this.selectedTimeSlotType == 'daily'){
      const timeslots: ActivityTimeslotInfo[] = [];
      for (let i = 0; i < 7; i++) {
        timeslots.push({
          id: null,
          dayOfWeek: this.daysOfWeek[i].toUpperCase() as DayOfWeek,
          specificDate: null,
          startTime: this.activityForm.get('fromTime')?.value,
          endTime: this.activityForm.get('toTime')?.value
        });
      }
      this.timeslots = timeslots;
      const startTime = this.convertTimeToMinutes(timeslots[0].startTime);
      const endTime = this.convertTimeToMinutes(timeslots[0].endTime);

      if (startTime >= endTime) {
        this.snackBar.open('"To" must be later than "From"', 'Close', { duration: 3000 });
        return;
      }
    } else if(this.selectedTimeSlotType == 'weekly'){
      const timeslots: ActivityTimeslotInfo[] = [];
      for (let i = 0; i < this.weeklyDays.length; i++) {
        if( this.weeklyDays[i] == null) {
          this.snackBar.open('Day cannot stay empty', 'Close', { duration: 3000 });
          return;
        }
        const day = this.weeklyDays[i].toUpperCase() as DayOfWeek;
        if (timeslots.some(timeslot => timeslot.dayOfWeek === day)) {
          this.snackBar.open('Cannot add ' + this.weeklyDays[i] + ' multiple times', 'Close', { duration: 3000 });
          return;
        }
        timeslots.push({
          id: null,
          dayOfWeek: this.weeklyDays[i].toUpperCase() as DayOfWeek,
          specificDate: null,
          startTime: this.weeklyFroms[i],
          endTime: this.weeklyTos[i]
        });
        const start = this.convertTimeToMinutes(timeslots[i].startTime);
        const end = this.convertTimeToMinutes(timeslots[i].endTime);
        if (start >= end) {
          this.snackBar.open(this.weeklyDays[i]+': "To" must be later than "From"', 'Close', { duration: 3000 });
          return;
        }
      }
      this.timeslots = timeslots;
    } else {
      this.snackBar.open('Choose a Timeslot', 'Close', { duration: 3000 });
      return;
    }
    this.activity.name = this.activityForm.get('name').value;
    this.activity.description = this.activityForm.get('description').value;
    this.activity.capacity = this.activityForm.get('capacity').value;
    this.activity.price = this.activityForm.get('price').value;
    this.activity.timeslotsInfos = this.timeslots;
    const selectedCategories = Object.keys(this.activityForm.get('categories')?.value)
      .filter(category => this.activityForm.get('categories')?.value[category] === true);

    this.activity.categories = selectedCategories.join(', ');
    console.log("categories: " + this.activity.categories);
    if (this.mode === ActivityCreateEditMode.create) {
      this.service.createActivity(this.activity).subscribe({
        next: () => {
          this.router.navigate(['/activities']);
          this.snackBar.open('Activity created successfully!', 'Close', { duration: 3000 });

        },
        error: error => {
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
            errorMessage += 'An unknown error occurred.';
          }

          this.snackBar.open(errorMessage, 'Close', { duration: 50000 });
        }
      });
    } else {
      this.service.updateActivity(this.activity.id, this.activity).subscribe({
        next: () => {
          this.router.navigate(['/activities']);
          this.snackBar.open('Activity updated successfully!', 'Close', { duration: 3000 });
        },
        error: error => {
          console.error('Error updating activity', error);
          this.snackBar.open("Error updating activity: " + error.error.errors, 'Close', { duration: 3000 });
        }
      });
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if(!input.files || input.files.length === 0) {
      return;
    }
    this.activity.mainImage = input.files[0];
    const reader = new FileReader();
    reader.onload = () => {
      this.uploadedImage = reader.result;
    };
    reader.readAsDataURL(input.files[0]);
  }

  removeImage() {
    this.uploadedImage = null;
    this.activity.mainImage = null;
  }

  removeAdditionalImage(i: number) {
    this.additionalImages.splice(i, 1);
    this.activity.additionalImages.splice(i, 1);
  }

  onAdditionalImageSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      Array.from(input.files).forEach((file) => {
        const reader = new FileReader();
        reader.onload = () => {
          this.activity.additionalImages.push(file);
          this.additionalImages.push(reader.result as string);
        };
        reader.readAsDataURL(file);
      });
      input.value = '';
    }
  }

  ngOnInit(): void {
    this.categories = Object.values(Category); // Enum-Werte extrahieren

    const categoryControls = this.categories.reduce((controls, category) => {
      controls[category] = new FormControl(false);
      return controls;
    }, {});
    this.activityForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
      description: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(1000)]],
      capacity: [0, [Validators.required, Validators.min(1)]],
      price: [0, [Validators.required, Validators.min(1)]],
      specificDate: [null],
      fromTime: ['00:00'],
      toTime: ['00:00'],
      categories: new FormGroup(categoryControls, [this.atLeastOneCategoryValidator()])
    })

    this.route.data.subscribe(data => {
      this.mode = data['mode'];
    });

    if (this.mode === ActivityCreateEditMode.edit) {
      this.loadActivityData();
    }
    this.generateTimeOptions();

    this.weeklyDays = [null];
    this.weeklyFroms = ['00:00'];
    this.weeklyTos = ['00:00'];

  }

  formatTime(time: string): string {
    if (!time) return '';
    const match = time.match(/^(\d{2}:\d{2})/); // Sucht nach "HH:mm" im String
    return match ? match[1] : time;
  }

  formatDayOfWeek(dayOfWeek: string): string {
    if (!dayOfWeek) return '';
    return dayOfWeek.charAt(0).toUpperCase() + dayOfWeek.slice(1).toLowerCase();
  }

  loadActivityData(){
    this.route.params.subscribe(params => {
      this.service.findOne(params['id']).subscribe({
        next: activity => {
          this.activity.id = activity.id;
          this.activity.name = activity.name;
          this.activity.description = activity.description;
          this.activity.capacity = activity.capacity;
          this.activity.price = activity.price;
          this.activity.categories = activity.categories;

          // Set pre-checked categories based on the activity data
          if (this.activity.categories) {
            const categoriesArray = this.activity.categories.split(', ');
            this.categories.forEach(category => {
              if (categoriesArray.includes(category)) {
                this.activityForm.get('categories')?.get(category)?.setValue(true);
              }
            });
            console.log(this.activityForm.controls.categories);
          }

          if (activity.mainImage) {
            const binary = atob(activity.mainImage);
            const array = [];
            for (let i = 0; i < binary.length; i++) {
              array.push(binary.charCodeAt(i));
            }
            const blob = new Blob([new Uint8Array(array)], { type: 'image/jpeg' });
            this.uploadedImage = URL.createObjectURL(blob);
            this.activity.mainImage = new File([blob], 'image.jpg', { type: 'image/jpeg' });
          }
          if (activity.additionalImages) {
            activity.additionalImages.forEach((imageContent) => {
              const binary = atob(imageContent);
              const array = [];
              for (let i = 0; i < binary.length; i++) {
                array.push(binary.charCodeAt(i));
              }
              const blob = new Blob([new Uint8Array(array)], { type: 'image/jpeg' });
              this.additionalImages.push(URL.createObjectURL(blob));
              this.activity.additionalImages.push(new File([blob], 'image.jpg', { type: 'image/jpeg' }));
            });
          }
          if (activity.timeslots.length > 0 && activity.timeslots.length < 7) {
            this.selectedTimeSlotType = 'weekly';
            this.selectedButton = 'weekly';
            this.weeklyFroms.length = 0;
            this.weeklyTos.length = 0;
            this.weeklyDays.length = 0;
            activity.timeslots.forEach(timeslot => {
              this.weeklyFroms.push(this.formatTime(timeslot.startTime.toString()));
              this.weeklyTos.push(this.formatTime(timeslot.endTime.toString()));
              this.weeklyDays.push(this.formatDayOfWeek(timeslot.dayOfWeek));
            });

            this.activityForm.patchValue({
              name: activity.name,
              description: activity.description,
              capacity: activity.capacity,
              price: activity.price,
              specificDate: null,  // weekly mode does not use specificDate
              fromTime: this.weeklyFroms[0],  // Default to the first day's "from" value
              toTime: this.weeklyTos[0],      // Default to the first day's "to" value
            });
          }

          if(activity.timeslots[0].specificDate != null){
          this.selectedTimeSlotType = 'specificDate';
          this.selectedButton = 'specificDate';
          } else if(activity.timeslots.length == 7){
                    this.selectedTimeSlotType = 'daily';
                    this.selectedButton = 'daily';
          }
          const startTime = activity.timeslots[0].startTime
          this.activityForm.setValue({
            name: activity.name,
            description: activity.description,
            capacity: activity.capacity,
            price: activity.price,
            specificDate: activity.timeslots[0].specificDate,
            fromTime: this.formatTime(activity.timeslots[0].startTime.toString()),
            toTime: this.formatTime(activity.timeslots[0].endTime.toString()),
            categories: this.activityForm.controls.categories.value
          });


        },
        error: error => {
          console.error('Error loading activity', error);
        }
      });
    });
    console.log(this.activityForm);
  }

  public get modeText() {
    switch (this.mode) {
      case ActivityCreateEditMode.create:
        return "Create";
      case ActivityCreateEditMode.edit:
        return "Update";
    }
  }

  goBack(): void {
    this.location.back();
  }
}

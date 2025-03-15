import { Component } from '@angular/core';
import {FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatButton} from "@angular/material/button";
import {
    MatDatepickerToggle,
    MatDateRangeInput,
    MatDateRangePicker,
    MatEndDate,
    MatStartDate
} from "@angular/material/datepicker";
import {MatFormField, MatLabel, MatSuffix} from "@angular/material/form-field";
import {MatInput} from "@angular/material/input";
import {MatCard} from "@angular/material/card";
import {RoomSearchDto} from "../../../dtos/room";
import {Router, RouterLink} from "@angular/router";

@Component({
  selector: 'app-room-search-bar',
  standalone: true,
  imports: [
    FormsModule,
    MatButton,
    MatDateRangeInput,
    MatDateRangePicker,
    MatDatepickerToggle,
    MatEndDate,
    MatFormField,
    MatInput,
    MatLabel,
    MatStartDate,
    MatSuffix,
    ReactiveFormsModule,
    MatCard
  ],
  templateUrl: './room-search-bar.component.html',
  styleUrl: './room-search-bar.component.scss'
})
export class RoomSearchBarComponent {

  searchForm = new FormGroup({
    start: new FormControl<Date | null>(null, Validators.required),
    end: new FormControl<Date | null>(null, Validators.required),
    persons: new FormControl<number | null>(null, Validators.required),
    minPrice: new FormControl<number | null> (null),
    maxPrice: new FormControl<number | null>(null),
  });

  constructor(private router: Router) {
  }

  onSubmit() {
    const navigationExtras = {
      queryParams: this.roomSearchDto
    };
    this.router.navigate([], navigationExtras);
  }

  set startDate(date: Date | null) {
    this.searchForm.get('start')?.setValue(date);
  }

  set endDate(date: Date | null) {
    this.searchForm.get('end')?.setValue(date);
  }

  set persons(value: number | null) {
    this.searchForm.get('persons')?.setValue(value);
  }

  set minPrice(value: number | null) {
    this.searchForm.get('minPrice')?.setValue(value);
  }

  set maxPrice(value: number | null) {
    this.searchForm.get('maxPrice')?.setValue(value);
  }

  get roomSearchDto() {
    return this.searchForm.value as RoomSearchDto;
  }


}

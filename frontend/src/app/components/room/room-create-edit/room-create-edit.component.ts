import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatButton, MatIconButton} from "@angular/material/button";
import {RoomService} from "../../../services/room.service";
import {Room} from "../../../dtos/room";
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from "@angular/material/form-field";
import {MatCard} from "@angular/material/card";
import {CommonModule} from "@angular/common";
import {MatIcon} from "@angular/material/icon";
import {ActivatedRoute, Router} from "@angular/router";
import { MatSnackBar } from '@angular/material/snack-bar';
import { Location } from '@angular/common';
import {ToastrService} from "ngx-toastr";
import {UiConfigService} from "../../../services/ui-config.service";

export enum RoomCreateEditMode {
  create,
  edit
}

@Component({
  selector: 'app-room-create',
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
  ],
  templateUrl: './room-create-edit.component.html',
  styleUrl: './room-create-edit.component.scss'
})

export class RoomCreateEditComponent implements OnInit{
  mode: RoomCreateEditMode = RoomCreateEditMode.create;
  room: Room = {
    id: null,
    name: '',
    description: '',
    capacity: 0,
    mainImage: null,
    price: 0,
    additionalImages: [],
    smartLockId: null
  };
  uploadedImage: string | ArrayBuffer;
  additionalImages: string[] = [];
  nukiEnabled: boolean = false;

  roomForm! : FormGroup;

  constructor(private service: RoomService,
              private router: Router,
              private route: ActivatedRoute,
              private formBuilder: FormBuilder,
              private snackBar: MatSnackBar,
              private location: Location,
              private toastr: ToastrService,
              private uiConfigService: UiConfigService
) {
  }

  onSubmit() {
    if(this.roomForm.invalid){
      return;
    }

    this.room.name = this.roomForm.get('name').value;
    this.room.description = this.roomForm.get('description').value;
    this.room.capacity = this.roomForm.get('capacity').value;
    this.room.smartLockId = this.roomForm.get('smartLockId').value;
    this.room.price = this.roomForm.get('price').value;

    if (this.mode === RoomCreateEditMode.create) {
      this.service.createRoom(this.room).subscribe({
        next: () => {
          this.router.navigate(['/rooms']);
          this.snackBar.open('Room created successfully!', 'Close', { duration: 3000 });

        },
        error: error => {
        this.snackBar.open("Error creating room: " + error.error.errors, 'Close', { duration: 3000 });
        }
      });
    } else {
      this.service.updateRoom(this.room.id, this.room).subscribe({
        next: () => {
          this.router.navigate(['/rooms']);
          this.snackBar.open('Room updated successfully!', 'Close', { duration: 3000 });
        },
        error: error => {
          console.error('Error updating room', error);
          this.snackBar.open("Error updating room: " + error.error.errors, 'Close', { duration: 3000 });
        }
      });
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if(!input.files || input.files.length === 0) {
      return;
    }
    this.room.mainImage = input.files[0];
    const reader = new FileReader();
    reader.onload = () => {
      this.uploadedImage = reader.result;
    };
    reader.readAsDataURL(input.files[0]);
  }

  removeImage() {
    this.uploadedImage = null;
    this.room.mainImage = null;
  }


  removeAdditionalImage(i: number) {
    this.additionalImages.splice(i, 1);
    this.room.additionalImages.splice(i, 1);
  }


  onAdditionalImageSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      Array.from(input.files).forEach((file) => {
        const reader = new FileReader();
        reader.onload = () => {
          this.room.additionalImages.push(file);
          this.additionalImages.push(reader.result as string);
        };
        reader.readAsDataURL(file);
      });
      input.value = '';
    }
  }

  ngOnInit(): void {
    this.fetchModuleStatus();

    const nameRegex = /^[a-zA-Z ]+$/;
    const noConsecutiveSpaces = /^(?!.* {2,})/;

    this.roomForm = this.formBuilder.group({
      name: [
        '',
        [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(40),
          Validators.pattern(nameRegex),
          Validators.pattern(noConsecutiveSpaces),
        ],
      ],
      description: [
        '',
        [
          Validators.required,
          Validators.minLength(3),
          Validators.maxLength(100),
        ],
      ],
      price: [
        null,
        [
          Validators.required,
          Validators.min(1),
          Validators.max(10000),
          Validators.pattern(/^\d+$/),
        ],
      ],
      capacity: [
        null,
        [
          Validators.required,
          Validators.min(1),
          Validators.max(6),
          Validators.pattern(/^\d+$/),
        ],
      ],
      smartLockId: [
        null,
        [
          Validators.minLength(1),
        ],
      ],
    });

    this.route.data.subscribe((data) => {
      this.mode = data['mode'];
    });

    if (this.mode === RoomCreateEditMode.edit) {
      this.loadRoomData();
    }
  }

  fetchModuleStatus(): void {
    this.uiConfigService.nukiIsEnabled().subscribe({
     next: (result) => {
       this.nukiEnabled = result;
     },
     error: (error) => {
       console.error(`Failed to get module status`, error);
       this.snackBar.open(
         'Failed to get module status.',
         'Close',
         { duration: 5000 }
       );
     }
   });
  }

  loadRoomData() {
    this.route.params.subscribe(params => {
      this.service.findOne(params['id']).subscribe({
        next: room => {
          this.room.id = room.id;
          this.room.name = room.name;
          this.room.description = room.description;
          this.room.capacity = room.capacity;
          this.room.smartLockId = room.smartLockId;
          this.room.price = room.price;
          if (room.mainImage) {
            const binary = atob(room.mainImage);
            const array = [];
            for (let i = 0; i < binary.length; i++) {
              array.push(binary.charCodeAt(i));
            }
            const blob = new Blob([new Uint8Array(array)], { type: 'image/jpeg' });
            this.uploadedImage = URL.createObjectURL(blob);
            this.room.mainImage = new File([blob], 'image.jpg', { type: 'image/jpeg' });
          }
          if (room.additionalImages) {
            room.additionalImages.forEach(imageContent => {
              const binary = atob(imageContent);
              const array = [];
              for (let i = 0; i < binary.length; i++) {
                array.push(binary.charCodeAt(i));
              }
              const blob = new Blob([new Uint8Array(array)], { type: 'image/jpeg' });
              this.additionalImages.push(URL.createObjectURL(blob));
              this.room.additionalImages.push(new File([blob], 'image.jpg', { type: 'image/jpeg' }));
            });
          }
          this.roomForm.setValue({
            name: room.name,
            description: room.description,
            capacity: room.capacity,
            smartLockId: room.smartLockId,
            price: room.price
          });
        },
        error: error => {
          console.error('Error loading room', error);
        }
      });
    });
  }

  public get modeText() {
    switch (this.mode) {
      case RoomCreateEditMode.create:
        return "Create";
      case RoomCreateEditMode.edit:
        return "Update";
    }
  }

  goBack(): void {
    this.location.back();
  }
}

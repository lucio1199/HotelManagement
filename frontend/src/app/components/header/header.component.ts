import {Component, OnInit} from '@angular/core';
import {AuthService} from '../../services/auth.service';
import {USER_ROLES} from "../../dtos/auth-request";
import {NgIf} from "@angular/common";
import {CheckInService} from "../../services/check-in.service";
import {UiConfigService} from "../../services/ui-config.service";

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit {

  constructor(public authService: AuthService,
              protected checkInService: CheckInService,
              protected uiConfigService: UiConfigService) { }

  ngOnInit() {
    this.checkInService.resetCheckedIn();
  }

  protected readonly USER_ROLES = USER_ROLES;
}

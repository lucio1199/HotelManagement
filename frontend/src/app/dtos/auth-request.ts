export class AuthRequest {
  constructor(
    public email: string,
    public password: string
  ) {}
}

export enum USER_ROLES {
  ADMIN,
  RECEPTIONIST,
  CLEANING_STAFF,
  GUEST
}

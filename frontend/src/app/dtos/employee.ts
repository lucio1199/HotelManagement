export enum RoleType {
  ROLE_GUEST = 'ROLE_GUEST',
  ROLE_ADMIN = 'ROLE_ADMIN',
  ROLE_CLEANING_STAFF = 'ROLE_CLEANING_STAFF',
  ROLE_RECEPTIONIST = 'ROLE_RECEPTIONIST'
}

export interface EmployeeCreateDto {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  roleType: RoleType;
}

export interface EmployeeUpdateDto {
  firstName?: string;
  lastName?: string;
  phoneNumber?: string;
  roleType?: RoleType;
  email?: string;
  password?: string;
}

export interface EmployeeDetailDto {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  roleType: RoleType;
}

export interface EmployeeListDto {
  id: number;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  roleType: RoleType;
}

export class Employee {
  id: number;
  firstName: string;
  lastName: string;
  phoneNumber: string;
  roleType: RoleType;
}


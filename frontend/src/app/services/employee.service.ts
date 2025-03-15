import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Globals} from "../global/globals";
import {EmployeeCreateDto, EmployeeDetailDto, EmployeeListDto, EmployeeUpdateDto} from "../dtos/employee";
import {Observable} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {

  private employeeBaseUri: string = this.globals.backendUri + '/employee';

  constructor(private httpClient: HttpClient, private globals: Globals) { }

  /**
   * Creates a new employee in the backend.
   *
   * @param employeeCreateDto to create
   * @returns the created employee
   */
  createEmployee(employeeCreateDto: EmployeeCreateDto): Observable<EmployeeListDto> {
    return this.httpClient.post<EmployeeListDto>(this.employeeBaseUri, employeeCreateDto);
  }
  /**
   * Retrieves all employees from the backend.
   *
   * @returns a list of employees
   */
  getAllEmployees(): Observable<EmployeeListDto[]> {
    return this.httpClient.get<EmployeeListDto[]>(this.employeeBaseUri);
  }

  /**
   * Finds an employee by ID.
   *
   * @param id of the employee to retrieve
   * @returns the employee details
   */
  findOneEmployee(id: number): Observable<EmployeeDetailDto> {
    return this.httpClient.get<EmployeeDetailDto>(`${this.employeeBaseUri}/${id}`);
  }

  /**
   * Deletes an employee by ID in the backend.
   *
   * @param id of the employee to delete
   * @returns an observable for the delete operation
   */
  deleteEmployee(id: number): Observable<void> {
    return this.httpClient.delete<void>(`${this.employeeBaseUri}/${id}`);
  }

  /**
   * Updates an existing employee in the backend.
   *
   * @param id the ID of the employee to update
   * @param employeeUpdateDto the data to update
   * @returns the updated employee details
   */
  updateEmployee(id: number, employeeUpdateDto: EmployeeUpdateDto): Observable<EmployeeDetailDto> {
    return this.httpClient.put<EmployeeDetailDto>(`${this.employeeBaseUri}/${id}`, employeeUpdateDto);
  }
}

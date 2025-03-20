# 🏨 InnControl

## Project Description

This project is a hotel management platform that allows users to manage customers, create bookings, manage rooms, generate invoices and manage activities. Customers can rent rooms or choose activities. A feature for cleaning staff is implemented to monitor the cleaning processes. The frontend is built with Angular, and the backend is powered by Spring Boot.

## Technologies & Tools

### Frontend:

Angular (TypeScript)

Angular Material

Forms, Routing, Services

### Backend:

Spring Boot (Java)

Spring Data JPA

H2

RESTful API

## Setup & Installation

### Backend

Navigate to the backend folder of the project and start the backend

`mvn spring-boot:run`

Start the backend with test data (recommended)

`mvn spring-boot:run -Dspring-boot.run.profiles=generateData`

### Frontend

Navigate to the frontend folder of the project and execute `npm install`. Based on the *package.json* file, npm will download all required node_modules to run an Angular application.
Afterwards, execute `npm install -g @angular/cli` to install the Angular CLI globally.

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

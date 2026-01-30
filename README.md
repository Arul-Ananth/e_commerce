# E-Commerce Web Application

This is a sample **full-stack e-commerce web application** built using **React**, **Spring Boot**, and **Nginx**.  
The project demonstrates how a frontend SPA and a backend REST API are integrated using Nginx as a reverse proxy, similar to real-world production setups.

---

## Tech Stack

- Frontend: React
- Backend: Spring Boot (Java)
- Web Server / Reverse Proxy: Nginx
- Database: SQL-based (see `DatabaseInit.sql`)

---

## Project Structure

```
.
├── backend-services/    # Spring Boot backend
├── frontend/            # React frontend
├── ngix/                # Nginx configuration
├── DatabaseInit.sql     # Database initialization script
```
---

## Running the Application (Development)

### Run Backend (Spring Boot)

```bash
cd backend-services
mvn spring-boot:run
```

Backend will start at:

http://localhost:8080

---

### Run Frontend (React)

```bash
cd frontend
npm install
npm start
```

Frontend will start at:

http://localhost:3000

---

## Production Setup Using Nginx

In a production setup:
- React is built into static files
- Nginx serves the frontend
- API requests are forwarded to Spring Boot

### Build React App

```bash
cd frontend
npm run build
```

Nginx is used to:
- Serve React build files
- Proxy `/api` requests to the Spring Boot backend

---

## Database Setup

The repository includes a SQL file to initialize the database:

```
DatabaseInit.sql
```

Run this script in your database before starting the backend and update the database configuration accordingly.

---

## Purpose of This Project

This project is created for:
- Learning full-stack web development
- Understanding React and Spring Boot integration
- Demonstrating Nginx reverse proxy usage
- Academic and portfolio purposes

---

## Notes

- Authentication and payment features are not implemented
- UI is kept simple to focus on architecture
- This is a demo project and not production-ready


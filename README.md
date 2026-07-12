# 🚗 Car Dealership Inventory System

A full-stack car dealership inventory management application with role-based access control, JWT authentication, and real-time inventory operations.

**🌐 Live Demo:** [https://car-dealership-inventory-system-eight.vercel.app](https://car-dealership-inventory-system-eight.vercel.app)

---
## 👤 Default Admin Account

On first server startup, an admin account is automatically seeded:

| Field | Value |
|---|---|
| **Username** | `het` |
| **Email** | `het@dealership.com` |
| **Password** | '2572006@Het' |
| **Role** | `ADMIN` |

## 📋 Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Features](#features)
- [Project Structure](#project-structure)
- [API Reference](#api-reference)
- [Database Schema](#database-schema)
- [Environment Variables](#environment-variables)
- [Local Development](#local-development)
- [Deployment](#deployment)
- [Vehicle Categories](#vehicle-categories)
- [Role Permissions](#role-permissions)

---

## Overview

This system allows a car dealership to manage its vehicle inventory. Users can browse and purchase vehicles, while admins have full control — including adding, updating, restocking, and deleting vehicles. Authentication is secured with JWT tokens, and all data is persisted in a **Neon PostgreSQL** cloud database.

---

## 🛠 Tech Stack

### Backend
| Technology | Details |
|---|---|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.4.1 |
| **Security** | Spring Security + JWT (jjwt 0.12.7) |
| **ORM** | Spring Data JPA (Hibernate) |
| **Database** | PostgreSQL (Neon Cloud) |
| **Build Tool** | Maven |
| **Hosting** | Render |

### Frontend
| Technology | Details |
|---|---|
| **Framework** | React (Vite) |
| **Styling** | Tailwind CSS |
| **HTTP Client** | Fetch API |
| **Hosting** | Vercel |

---

## ✨ Features

### User Features
- 🔐 Register & login with email + password
- 🔑 Token-based authentication (JWT)
- 🚘 Browse all available vehicles
- 🔍 Search vehicles by make, model, category, or price range
- 🛒 Purchase vehicles (reduces inventory quantity)

### Admin Features
- ➕ Add new vehicles to inventory
- ✏️ Update vehicle details (make, model, category, price, quantity)
- 🗑️ Delete vehicles from inventory
- 📦 Restock vehicles (increase quantity)
- 💰 Price must be **minimum $100,000** per vehicle

### System Features
- ✅ Unique email enforcement at both app and database level
- 🔒 Stateless JWT sessions (no server-side sessions)
- 🌐 CORS configured for Vercel deployments + localhost
- 🌱 Auto-seeded admin account on first startup
- 🛡️ Role-based access control (`ADMIN` / `USER`)

---

## 📁 Project Structure

```
Car-Dealership-Inventory-System/
├── frontend/                         # React + Vite frontend
│   └── src/
│       ├── App.jsx                   # Main application component
│       ├── api.js                    # API service layer
│       └── index.css                 # Global styles
│
├── src/main/java/com/Car/Dealership/Inventory/System/
│   ├── config/
│   │   ├── AdminSeeder.java          # Seeds default admin on startup
│   │   └── SecurityConfig.java      # JWT filter chain & CORS config
│   ├── controller/
│   │   ├── AuthController.java       # POST /api/auth/register & /login
│   │   └── VehicleController.java   # Vehicle CRUD & inventory endpoints
│   ├── dto/
│   │   ├── LoginRequest.java         # Login request body
│   │   └── RestockRequest.java       # Restock request body
│   ├── entity/
│   │   ├── User.java                 # User JPA entity
│   │   ├── Vehicle.java              # Vehicle JPA entity
│   │   ├── Category.java             # Vehicle category enum
│   │   └── Role.java                 # User role enum (ADMIN/USER)
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── InvalidCredentialsException.java
│   │   ├── UserAlreadyExistsException.java
│   │   └── VehicleNotFoundException.java
│   ├── filter/
│   │   └── JwtAuthFilter.java        # JWT request validation filter
│   ├── repository/
│   │   ├── UserRepository.java       # User DB queries
│   │   └── VehicleRepository.java   # Vehicle DB queries + search
│   ├── service/
│   │   ├── AuthService.java          # Auth service interface
│   │   ├── VehicleService.java       # Vehicle service
│   │   └── impl/
│   │       ├── AuthServiceImpl.java
│   │       └── UserDetailsServiceImpl.java
│   └── util/
│       └── JwtUtil.java              # JWT generation & validation
│
├── .env                              # Local environment variables
├── Dockerfile                        # Docker support
└── pom.xml                           # Maven dependencies
```

---

## 📡 API Reference

### Base URL
- **Local:** `http://localhost:8080`
- **Production:** Render backend URL

> All endpoints under `/api/vehicles` require a valid JWT in the `Authorization: Bearer <token>` header.

---

### 🔓 Auth Endpoints (Public)

#### `POST /api/auth/register`
Register a new user account.

**Request Body:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "yourpassword",
  "role": "USER"
}
```

**Response `201 Created`:**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "role": "USER"
}
```

> `role` can be `"USER"` or `"ADMIN"`. Email must be unique.

---

#### `POST /api/auth/login`
Login with email/username and password.

**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "yourpassword"
}
```
> You can also pass `"username"` instead of `"email"`.

**Response `200 OK`:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "john_doe",
  "email": "john@example.com",
  "role": "USER"
}
```

---

### 🔒 Vehicle Endpoints (Protected — requires JWT)

#### `POST /api/vehicles`
Add a new vehicle.

**Headers:** `Authorization: Bearer <token>`

**Request Body:**
```json
{
  "make": "Ferrari",
  "model": "F8 Tributo",
  "category": "SPORTS_CAR",
  "price": 280000.00,
  "quantity": 3
}
```
> ⚠️ Price must be **≥ $100,000**.

**Response `201 Created`:** Returns the created vehicle object.

---

#### `GET /api/vehicles`
Retrieve all vehicles in inventory.

**Response `200 OK`:**
```json
[
  {
    "id": 1,
    "make": "Ferrari",
    "model": "F8 Tributo",
    "category": "SPORTS_CAR",
    "price": 280000.00,
    "quantity": 3
  }
]
```

---

#### `GET /api/vehicles/search`
Search vehicles with optional filters.

**Query Parameters:**

| Parameter | Type | Description |
|---|---|---|
| `make` | string | Filter by manufacturer |
| `model` | string | Filter by model name |
| `category` | string | Filter by category (e.g. `SEDAN`, `SUV`) |
| `minPrice` | decimal | Minimum price filter |
| `maxPrice` | decimal | Maximum price filter |

**Example:**
```
GET /api/vehicles/search?make=Ferrari&minPrice=100000&maxPrice=500000
```

**Response `200 OK`:** Array of matching vehicle objects.

---

#### `PUT /api/vehicles/{id}`
Update an existing vehicle's details.

**Request Body:** Same fields as `POST /api/vehicles`.

**Response `200 OK`:** Returns the updated vehicle object.

---

#### `DELETE /api/vehicles/{id}` ⛔ Admin Only
Delete a vehicle from inventory.

**Response `204 No Content`**

---

#### `POST /api/vehicles/{id}/purchase`
Purchase a vehicle (decreases quantity by 1).

**Response `200 OK`:** Returns updated vehicle with decremented quantity.

> Returns error if quantity is 0 (out of stock).

---

#### `POST /api/vehicles/{id}/restock` ⛔ Admin Only
Restock a vehicle (increases quantity).

**Request Body:**
```json
{
  "quantity": 5
}
```

**Response `200 OK`:** Returns updated vehicle with incremented quantity.

---

## 🗄 Database Schema

### `users` table
| Column | Type | Constraints |
|---|---|---|
| `id` | BIGINT | PK, Auto-increment |
| `username` | VARCHAR | NOT NULL |
| `email` | VARCHAR | NOT NULL, UNIQUE |
| `password` | VARCHAR | NOT NULL (BCrypt hashed) |
| `role` | VARCHAR | NOT NULL (`ADMIN` / `USER`) |

### `vehicles` table
| Column | Type | Constraints |
|---|---|---|
| `id` | BIGINT | PK, Auto-increment |
| `make` | VARCHAR | NOT NULL |
| `model` | VARCHAR | NOT NULL |
| `category` | VARCHAR | NOT NULL (enum) |
| `price` | DECIMAL | NOT NULL, min 100,000 |
| `quantity` | INT | NOT NULL, min 0 |

---

## ⚙️ Environment Variables

### Backend (`.env` / Render Environment)

| Variable | Description | Example |
|---|---|---|
| `DB_URL` | Neon PostgreSQL JDBC URL | `jdbc:postgresql://...neon.tech/neondb?sslmode=require` |
| `DB_USERNAME` | Database username | `neondb_owner` |
| `DB_PASSWORD` | Database password | `your_db_password` |
| `JWT_SECRET` | HS256 secret key (min 256 bits) | `YourSuperSecretKey...` |
| `JWT_EXPIRATION` | JWT expiry in milliseconds | `86400000` (24 hours) |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins | `https://your-app.vercel.app` |

### Frontend (Vercel Environment)

| Variable | Description |
|---|---|
| `VITE_API_URL` | Backend base URL (Render) |

---

## 💻 Local Development

### Prerequisites
- Java 21+
- Node.js 18+
- Maven 3.9+
- A PostgreSQL database (or [Neon](https://neon.tech) free tier)

### Backend Setup

```bash
# Clone the repository
git clone https://github.com/HetGajera257/Car-Dealership-Inventory-System.git
cd Car-Dealership-Inventory-System

# Create .env file with your database credentials (see Environment Variables above)

# Run the Spring Boot application
./mvnw spring-boot:run
```
Backend runs at `http://localhost:8080`

### Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```
Frontend runs at `http://localhost:5173`

---

## 🚀 Deployment

### Backend — Render
1. Connect your GitHub repository to [Render](https://render.com)
2. Set **Build Command:** `./mvnw clean package -DskipTests`
3. Set **Start Command:** `java -jar target/Car-Dealership-Inventory-System-0.0.1-SNAPSHOT.jar`
4. Add all environment variables from the table above in Render's dashboard
5. Set `CORS_ALLOWED_ORIGINS` to your Vercel frontend URL

### Frontend — Vercel
1. Import the repository into [Vercel](https://vercel.com)
2. Set **Root Directory** to `frontend`
3. Set `VITE_API_URL` environment variable to your Render backend URL
4. Deploy — Vercel auto-deploys on every push to `main`

---

## 🏷 Vehicle Categories

| Display Name | API Value |
|---|---|
| Sedan | `SEDAN` |
| SUV | `SUV` |
| Hatchback | `HATCHBACK` |
| Coupe | `COUPE` |
| Convertible | `CONVERTIBLE` |
| Wagon | `WAGON` |
| Pickup Truck | `PICKUP_TRUCK` |
| Minivan | `MINIVAN` |
| Sports Car | `SPORTS_CAR` |
| Luxury Car | `LUXURY_CAR` |
| Electric Vehicle | `ELECTRIC_VEHICLE` |
| Hybrid Vehicle | `HYBRID_VEHICLE` |
| Crossover | `CROSSOVER` |
| Off-Road Vehicle | `OFF_ROAD_VEHICLE` |
| Commercial Vehicle | `COMMERCIAL_VEHICLE` |
| Van | `VAN` |

---

## 🔐 Role Permissions

| Action | USER | ADMIN |
|---|---|---|
| Register / Login | ✅ | ✅ |
| View all vehicles | ✅ | ✅ |
| Search vehicles | ✅ | ✅ |
| Purchase a vehicle | ✅ | ✅ |
| Add a vehicle | ❌ | ✅ |
| Update a vehicle | ❌ | ✅ |
| Delete a vehicle | ❌ | ✅ |
| Restock a vehicle | ❌ | ✅ |

---



---

## 📄 License

This project was built as part of a backend API assessment. All rights reserved © Het Gajera.

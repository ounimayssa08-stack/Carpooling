<div align="center">

# 🚗 CarPooling Desktop

> *Share the road. Split the cost. Connect the journey.*

A sleek JavaFX desktop application for managing carpooling — from booking rides to tracking payments, all in one place.

[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=flat-square)](https://openjfx.io/)
[![SQLite](https://img.shields.io/badge/SQLite-3.45-lightblue?style=flat-square&logo=sqlite)](https://sqlite.org/)
[![Maven](https://img.shields.io/badge/Maven-Build-red?style=flat-square&logo=apachemaven)](https://maven.apache.org/)

</div>

---

## ✨ Features

| Module | Highlights |
|---|---|
| 🔐 **Auth** | Register, login, account lock after failed attempts |
| 🗺️ **Trips** | Create, start, cancel & complete trips |
| 📋 **Reservations** | Book seats, accept/refuse requests, real-time seat tracking |
| 🚙 **Vehicles** | Manage driver vehicles |
| 💳 **Payments** | Authorize, capture & refund payments per reservation |
| ⭐ **Reviews** | Rate drivers after completed trips |
| 🛡️ **Admin Dashboard** | Full oversight — users, trips, and system stats |

---

## 🧱 Tech Stack

- **Language** — Java 17
- **UI Framework** — JavaFX 21 + FXML
- **Database** — SQLite (embedded, zero config)
- **Build Tool** — Apache Maven
- **Architecture** — MVC · Service Layer · Repository Pattern

---

## 🚀 Getting Started

**Prerequisites:** Java 17+, Maven 3.6+

```bash
# Clone the repo
git clone https://github.com/your-username/carpooling.git
cd carpooling/carpooling

# Build & run
mvn javafx:run
```

That's it. No external database setup required — SQLite is embedded.

---

## 👥 User Roles

```
ADMIN      → Full system control
DRIVER     → Create trips, manage vehicles, handle reservations
PASSENGER  → Browse trips, make reservations, leave reviews
```

---

## 📁 Project Structure

```
src/main/java/com/carpooling/
├── controller/     # JavaFX controllers (UI logic)
├── model/          # Domain entities (User, Trip, Reservation…)
├── service/        # Business rules & validations
├── repository/     # SQLite data access layer
├── enums/          # Status & role enumerations
└── util/           # Shared helpers & DB connection
```

---

<div align="center">
  Made with ☕ and JavaFX
</div>

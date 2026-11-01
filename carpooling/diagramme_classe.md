# Diagramme de Classe UML - Carpooling App

```mermaid
classDiagram
    direction LR

    %% Classes de Données (Models)
    class User {
        +int id
        +String name
        +String email
        +Role role
        +UserStatus status
        +int failedAttempts
    }

    class Trip {
        +int id
        +int driverId
        +int vehicleId
        +String departure
        +String destination
        +LocalDateTime tripDate
        +int availableSeats
        +double price
        +TripStatus status
    }

    class Reservation {
        +int id
        +int userId
        +int tripId
        +int seatsReserved
        +ReservationStatus status
    }

    class Vehicle {
        +int id
        +int driverId
        +String model
        +String plate
        +int seats
    }

    class Payment {
        +int id
        +int reservationId
        +double amount
        +PaymentStatus status
    }

    class Review {
        +int id
        +int driverId
        +int userId
        +int rating
        +String comment
    }

    %% Énumérations
    class Role <<enumeration>> {
        PASSENGER
        DRIVER
        ADMIN
    }

    class TripStatus <<enumeration>> {
        EN_ATTENTE
        COMPLET
        EN_COURS
        TERMINE
        ANNULE
    }

    class ReservationStatus <<enumeration>> {
        EN_ATTENTE
        CONFIRME
        REFUSE
        ANNULE
    }

    class PaymentStatus <<enumeration>> {
        AUTHORIZED
        CAPTURED
        REFUNDED
    }

    class UserStatus <<enumeration>> {
        ACTIVE
        SUSPENDED
        BLOCKED
    }

    %% Services (Logique métier)
    class AuthService {
        +register(name, email, pass, role)
        +login(email, pass)
        +logout()
    }

    class TripService {
        +createTrip(driverId, vehicleId, ...)
        +cancelTrip(tripId, driverId)
        +startTrip(tripId, driverId)
        +endTrip(tripId, driverId)
    }

    class ReservationService {
        +reserve(userId, tripId, seats)
        +acceptReservation(resId, driverId)
        +refuseReservation(resId, driverId)
        +cancelReservation(resId, userId)
    }

    %% Relations
    User "1" -- "*" Trip : conduit
    User "1" -- "*" Reservation : reserve
    Trip "1" -- "*" Reservation : contient
    User "1" -- "1" Role : est
    Vehicle "*" -- "1" User : appartient à
    Reservation "1" -- "1" Payment : possède
    Review "*" -- "1" User : évalué par

    %% Couleurs
    style User fill:#e1f5fe,stroke:#01579b
    style Trip fill:#e1f5fe,stroke:#01579b
    style Reservation fill:#e1f5fe,stroke:#01579b
    style Vehicle fill:#e1f5fe,stroke:#01579b
    
    style Role fill:#f3e5f5,stroke:#4a148c
    style TripStatus fill:#f3e5f5,stroke:#4a148c
    style ReservationStatus fill:#f3e5f5,stroke:#4a148c
    
    style AuthService fill:#e8f5e9,stroke:#1b5e20
    style TripService fill:#e8f5e9,stroke:#1b5e20
    style ReservationService fill:#e8f5e9,stroke:#1b5e20
```

## Description du Diagramme

Ce diagramme illustre l'architecture complète du projet de covoiturage. 

1.  **Le Cœur du Système** : L'entité `User` est centrale, pouvant être un `PASSENGER`, un `DRIVER` ou un `ADMIN`.
2.  **Gestion des Trajets** : Un conducteur (`DRIVER`) crée un `Trip` avec un `Vehicle` spécifique.
3.  **Processus de Réservation** : Un passager effectue une `Reservation`. Celle-ci passe par différents états (`EN_ATTENTE`, `CONFIRME`) gérés par le `ReservationService`.
4.  **Paiement et Évaluation** : Une fois confirmée, une réservation génère un `Payment`. Après le trajet, l'utilisateur peut laisser une `Review`.
5.  **Services** : Ils encapsulent toute la logique de validation (ex: vérifier s'il reste des places avant de confirmer).

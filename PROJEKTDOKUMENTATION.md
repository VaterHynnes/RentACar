# RentACar - Autovermietung Backend

## Projektdokumentation

**Version:** 1.0.0  
**Datum:** 2025-11-23  
**Projekt:** RentACar - Backend-System für Autovermietung  
**Architektur:** Domain-Driven Design (DDD)  
**Framework:** Spring Boot 3.2.0

---

## Inhaltsverzeichnis

1. [Projektübersicht](#1-projektübersicht)
2. [Architektur](#2-architektur)
3. [Technologie-Stack](#3-technologie-stack)
4. [Installation & Setup](#4-installation--setup)
5. [Projektstruktur](#5-projektstruktur)
6. [Funktionale Anforderungen](#6-funktionale-anforderungen)
7. [Nicht-funktionale Anforderungen](#7-nicht-funktionale-anforderungen)
8. [API-Dokumentation](#8-api-dokumentation)
9. [Sicherheit](#9-sicherheit)
10. [Datenbank](#10-datenbank)
11. [Testing](#11-testing)
12. [Deployment](#12-deployment)
13. [Wartung & Erweiterung](#13-wartung--erweiterung)
14. [Anhang](#14-anhang)

---

## 1. Projektübersicht

### 1.1 Beschreibung

RentACar ist ein modernes Backend-System für eine Autovermietung, das nach den Prinzipien des **Domain-Driven Design (DDD)** entwickelt wurde. Das System ermöglicht die Verwaltung von Fahrzeugen, Kunden, Buchungen und Vermietungen mit umfassenden Sicherheits- und Compliance-Features.

### 1.2 Hauptfunktionen

- **Fahrzeugverwaltung**: Verwaltung von Fahrzeugtypen, Status und Standorten
- **Kundenverwaltung**: Registrierung, Datenverwaltung mit DSGVO-konformer Verschlüsselung
- **Buchungsverwaltung**: Fahrzeugsuche, Buchungserstellung mit Verfügbarkeitsprüfung
- **Vermietungsprozess**: Check-out, Check-in, Schadensberichte, Zusatzkosten

### 1.3 Zielgruppe

- **Entwickler**: Backend-Entwicklung, API-Integration
- **Administratoren**: Systemverwaltung, Konfiguration
- **Projektmanager**: Projektübersicht, Anforderungen

---

## 2. Architektur

### 2.1 Domain-Driven Design (DDD)

Das Projekt folgt strikt den Prinzipien des Domain-Driven Design mit klarer Trennung in **Bounded Contexts** und **Schichten**.

#### 2.1.1 Bounded Contexts

1. **Vehicle Context** - Fahrzeugverwaltung
   - Verwaltung von Fahrzeugen, Typen, Status
   - Standortverwaltung

2. **Customer Context** - Kundenverwaltung
   - Kundenregistrierung und -verwaltung
   - DSGVO-konforme Datenverschlüsselung

3. **Booking Context** - Buchungsverwaltung
   - Fahrzeugsuche und Verfügbarkeitsprüfung
   - Buchungserstellung, Bestätigung, Stornierung
   - Preisberechnung

4. **Rental Context** - Vermietungsprozess
   - Check-out und Check-in
   - Schadensberichte
   - Zusatzkostenberechnung

#### 2.1.2 Schichtenarchitektur

Jeder Bounded Context ist in folgende Schichten unterteilt:

```
┌─────────────────────────────────────┐
│         Web Layer (REST API)        │  ← Controllers
├─────────────────────────────────────┤
│      Application Layer (Use Cases)  │  ← Application Services
├─────────────────────────────────────┤
│        Domain Layer (Business)      │  ← Aggregates, Entities, Value Objects
├─────────────────────────────────────┤
│    Infrastructure Layer (Technical) │  ← JPA, Repositories, External Services
└─────────────────────────────────────┘
```

**Domain Layer:**
- Aggregates (Vehicle, Customer, Booking, Rental)
- Entities
- Value Objects (LicensePlate, EncryptedString)
- Domain Services (AvailabilityService, PriceCalculationService)
- Repository Interfaces

**Application Layer:**
- Application Services (BookingService, CustomerService, etc.)
- Use Case Orchestrierung
- DTOs (geplant)

**Infrastructure Layer:**
- JPA Entities
- Repository Implementations
- External Services (EncryptionService)
- Database Configuration

**Web Layer:**
- REST Controllers
- Request/Response Mapping

### 2.2 Shared Kernel

Gemeinsame Komponenten, die von allen Bounded Contexts genutzt werden:

- `BaseEntity`: Basis-Entity mit ID, Timestamps, Version
- `AuditLog`: Audit-Logging für sicherheitsrelevante Aktionen
- `AuditService`: Service für Audit-Logging
- `Security`: User, Roles, Security Configuration

---

## 3. Technologie-Stack

### 3.1 Core Technologies

| Technologie | Version | Verwendung |
|------------|---------|------------|
| Java | 17 | Programmiersprache |
| Spring Boot | 3.2.0 | Framework |
| Spring Data JPA | 3.2.0 | Datenzugriff |
| Spring Security | 6.1.1 | Sicherheit |
| H2 Database | 2.2.224 | In-Memory Datenbank |
| Gradle | 8.5 | Build-Tool |

### 3.2 Dependencies

**Spring Boot Starters:**
- `spring-boot-starter-web` - REST API
- `spring-boot-starter-data-jpa` - JPA/Hibernate
- `spring-boot-starter-security` - Spring Security
- `spring-boot-starter-validation` - Bean Validation

**Security & Encryption:**
- `jasypt-spring-boot-starter` (3.0.5) - Verschlüsselung
- `jjwt` (0.12.3) - JWT Support (optional)

**Utilities:**
- `lombok` - Code-Reduktion
- `h2` - In-Memory Datenbank

**Testing:**
- `junit-jupiter` (5.10.1) - Unit Testing
- `spring-boot-starter-test` - Integration Testing
- `spring-security-test` - Security Testing

### 3.3 Build-Konfiguration

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
}

java {
    sourceCompatibility = '17'
    targetCompatibility = '17'
}
```

---

## 4. Installation & Setup

### 4.1 Voraussetzungen

- **JDK 17** oder höher
- **Gradle 8.5** oder höher (Wrapper enthalten)
- **IDE** (IntelliJ IDEA, Eclipse, VS Code empfohlen)

### 4.2 Projekt-Setup

#### 4.2.1 Repository klonen/öffnen

```bash
cd RENTCARPROJEKT
```

#### 4.2.2 Dependencies herunterladen

```bash
./gradlew build
```

#### 4.2.3 Anwendung starten

```bash
./gradlew bootRun
```

Die Anwendung startet auf `http://localhost:8080`

### 4.3 Konfiguration

#### 4.3.1 application.properties

```properties
# Application Configuration
spring.application.name=rentacar

# H2 Database Configuration
spring.datasource.url=jdbc:h2:mem:rentacardb
spring.datasource.username=sa
spring.datasource.password=

# H2 Console (for development)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Encryption Configuration (Jasypt)
jasypt.encryptor.algorithm=PBEWithMD5AndDES
jasypt.encryptor.password=${JASYPT_ENCRYPTOR_PASSWORD:rentacar-secret-key}
```

#### 4.3.2 Umgebungsvariablen

Für Produktion sollte das Verschlüsselungspasswort als Umgebungsvariable gesetzt werden:

```bash
export JASYPT_ENCRYPTOR_PASSWORD=your-secure-password
```

### 4.4 H2-Konsole Zugriff

1. Öffne: `http://localhost:8080/h2-console`
2. Verbindungseinstellungen:
   - **JDBC URL**: `jdbc:h2:mem:rentacardb`
   - **Username**: `sa`
   - **Password**: (leer lassen)
3. Klicke auf "Connect"

---

## 5. Projektstruktur

```
RENTCARPROJEKT/
├── src/
│   ├── main/
│   │   ├── java/de/rentacar/
│   │   │   ├── RentACarApplication.java
│   │   │   ├── shared/                    # Shared Kernel
│   │   │   │   ├── domain/
│   │   │   │   │   ├── BaseEntity.java
│   │   │   │   │   ├── AuditLog.java
│   │   │   │   │   └── AuditService.java
│   │   │   │   ├── infrastructure/
│   │   │   │   │   ├── AuditLogRepository.java
│   │   │   │   │   └── DataInitializer.java
│   │   │   │   └── security/
│   │   │   │       ├── Role.java
│   │   │   │       ├── User.java
│   │   │   │       ├── UserRepository.java
│   │   │   │       ├── CustomUserDetailsService.java
│   │   │   │       └── SecurityConfig.java
│   │   │   ├── vehicle/                   # Vehicle Bounded Context
│   │   │   │   ├── domain/
│   │   │   │   │   ├── Vehicle.java
│   │   │   │   │   ├── VehicleType.java
│   │   │   │   │   ├── VehicleStatus.java
│   │   │   │   │   ├── LicensePlate.java
│   │   │   │   │   └── VehicleRepository.java
│   │   │   │   ├── application/
│   │   │   │   │   └── VehicleManagementService.java
│   │   │   │   ├── infrastructure/
│   │   │   │   │   ├── VehicleJpaRepository.java
│   │   │   │   │   └── VehicleRepositoryImpl.java
│   │   │   │   └── web/
│   │   │   │       └── VehicleController.java
│   │   │   ├── customer/                  # Customer Bounded Context
│   │   │   │   ├── domain/
│   │   │   │   │   ├── Customer.java
│   │   │   │   │   ├── EncryptedString.java
│   │   │   │   │   └── CustomerRepository.java
│   │   │   │   ├── application/
│   │   │   │   │   └── CustomerService.java
│   │   │   │   ├── infrastructure/
│   │   │   │   │   ├── CustomerJpaRepository.java
│   │   │   │   │   ├── CustomerRepositoryImpl.java
│   │   │   │   │   └── EncryptionService.java
│   │   │   │   └── web/
│   │   │   │       └── CustomerController.java
│   │   │   ├── booking/                   # Booking Bounded Context
│   │   │   │   ├── domain/
│   │   │   │   │   ├── Booking.java
│   │   │   │   │   ├── BookingStatus.java
│   │   │   │   │   ├── BookingRepository.java
│   │   │   │   │   ├── AvailabilityService.java
│   │   │   │   │   └── PriceCalculationService.java
│   │   │   │   ├── application/
│   │   │   │   │   └── BookingService.java
│   │   │   │   ├── infrastructure/
│   │   │   │   │   ├── BookingJpaRepository.java
│   │   │   │   │   └── BookingRepositoryImpl.java
│   │   │   │   └── web/
│   │   │   │       └── BookingController.java
│   │   │   └── rental/                    # Rental Bounded Context
│   │   │       ├── domain/
│   │   │       │   ├── Rental.java
│   │   │       │   ├── RentalStatus.java
│   │   │       │   ├── DamageReport.java
│   │   │       │   └── RentalRepository.java
│   │   │       ├── application/
│   │   │       │   └── RentalService.java
│   │   │       ├── infrastructure/
│   │   │       │   ├── RentalJpaRepository.java
│   │   │       │   └── RentalRepositoryImpl.java
│   │   │       └── web/
│   │   │           └── RentalController.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/de/rentacar/
│           └── booking/
│               ├── application/
│               │   └── BookingServiceTest.java
│               └── domain/
│                   ├── BookingTest.java
│                   ├── AvailabilityServiceTest.java
│                   └── PriceCalculationServiceTest.java
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
└── README.md
```

---

## 6. Funktionale Anforderungen

### 6.1 Fahrzeugverwaltung

**Anforderungen:**
- Verwaltung von Fahrzeugtypen (Kleinwagen, Kompaktklasse, Mittelklasse, Oberklasse, SUV, Van, Sportwagen)
- Eigenschaften: Kennzeichen, Marke, Modell, Kilometerstand, Standort (Filiale), Status
- Status: Verfügbar, Vermietet, Wartung, Außer Betrieb

**Use Cases:**
- Mitarbeiter können Fahrzeuge hinzufügen
- Mitarbeiter können Fahrzeuge bearbeiten
- Mitarbeiter können Fahrzeuge außer Betrieb setzen
- Alle können Fahrzeuge suchen (mit Filter)

**Implementierung:**
- `VehicleManagementService`: Application Service für Fahrzeugverwaltung
- `VehicleController`: REST-Endpunkte
- `Vehicle`: Aggregate Root mit Domain-Methoden

### 6.2 Kundenverwaltung

**Anforderungen:**
- Speicherung von Kundendaten (Name, Adresse, Führerscheinnummer, Kontaktdaten)
- DSGVO-konforme Verschlüsselung sensibler Daten
- Kunden können sich registrieren
- Kunden können ihre Daten ändern
- Anzeige der Buchungshistorie pro Kunde

**Use Cases:**
- Kundenregistrierung (öffentlich)
- Kundendaten aktualisieren
- Kunde nach ID/Benutzername abrufen
- Buchungshistorie abrufen

**Implementierung:**
- `CustomerService`: Application Service
- `EncryptionService`: Verschlüsselung sensibler Daten
- `CustomerController`: REST-Endpunkte

### 6.3 Buchungsverwaltung

**Anforderungen:**
- Kunden können Fahrzeuge suchen (Zeitraum, Typ, Standort)
- **Robuste Verfügbarkeitsprüfung** zur Verhinderung von Überbuchungen
- Buchung enthält: Kunde, Fahrzeug, Abhol-/Rückgabedatum, Orte, Gesamtpreis
- Kunden können bis 24h vor Abholung stornieren
- Preisberechnung basierend auf Kategorie und Dauer

**Use Cases:**
- Verfügbare Fahrzeuge suchen
- Buchung erstellen (mit Verfügbarkeitsprüfung)
- Buchung bestätigen
- Buchung stornieren (bis 24h vor Abholung)
- Buchungshistorie abrufen

**Implementierung:**
- `BookingService`: Application Service
- `AvailabilityService`: Domain Service für Verfügbarkeitsprüfung
- `PriceCalculationService`: Domain Service für Preisberechnung
- `BookingController`: REST-Endpunkte

**Wichtig:** Die Verfügbarkeitsprüfung verhindert Überbuchungen durch Prüfung auf überlappende bestätigte Buchungen.

### 6.4 Vermietungsprozess

**Anforderungen:**
- Mitarbeiter führen Check-out durch (Übergabe, Kilometerstand, Zustand)
- Mitarbeiter führen Check-in durch (Rückgabe, Kilometerstand, Schadensprüfung)
- Erstellung von Schadensberichten
- Berechnung von Zusatzkosten (Verspätung, Schäden, etc.)

**Use Cases:**
- Check-out durchführen
- Check-in durchführen
- Schadensbericht erstellen
- Zusatzkosten berechnen (automatisch bei Verspätung)

**Implementierung:**
- `RentalService`: Application Service
- `RentalController`: REST-Endpunkte
- `Rental`: Aggregate Root mit Domain-Methoden

---

## 7. Nicht-funktionale Anforderungen

### 7.1 Sicherheit (NFR3, NFR4, NFR5)

#### 7.1.1 Rollenbasierte Zugriffskontrolle (RBAC)

**Rollen:**
- `ROLE_CUSTOMER` - Kunde
  - Kann Fahrzeuge suchen
  - Kann Buchungen erstellen und stornieren
  - Kann eigene Daten ändern
  - Kann Buchungshistorie abrufen

- `ROLE_EMPLOYEE` - Mitarbeiter
  - Alle Kunden-Rechte
  - Kann Fahrzeuge verwalten
  - Kann Vermietungen durchführen (Check-out/Check-in)
  - Kann Schadensberichte erstellen

- `ROLE_ADMIN` - Administrator
  - Vollzugriff auf alle Endpunkte

**Implementierung:**
- `SecurityConfig`: Spring Security Konfiguration
- `CustomUserDetailsService`: User-Loading für Spring Security
- Method-Level Security mit `@PreAuthorize` (optional)

#### 7.1.2 Verschlüsselung (DSGVO-konform)

**Verschlüsselte Daten:**
- E-Mail-Adresse
- Telefonnummer
- Adresse
- Führerscheinnummer

**Implementierung:**
- `EncryptionService`: Jasypt-basierte Verschlüsselung
- `EncryptedString`: Value Object für verschlüsselte Strings
- Algorithmus: PBEWithMD5AndDES (für Produktion: AES-256 empfohlen)

#### 7.1.3 Audit-Logging

**Protokollierte Aktionen:**
- Kundenregistrierung
- Kundendatenänderung
- Buchungserstellung, -bestätigung, -stornierung
- Fahrzeugverwaltung
- Vermietungsprozesse

**Audit-Log Einträge enthalten:**
- Username
- Aktion
- Resource Type
- Resource ID
- Details
- IP-Adresse
- Timestamp

**Implementierung:**
- `AuditLog`: Entity für Audit-Einträge
- `AuditService`: Service für Audit-Logging

### 7.2 Performance (NFR1)

**Anforderungen:**
- Fahrzeugsuche muss effizient sein (Ziel: < 2 Sekunden)

**Optimierungen:**
- Lazy Loading für Entity-Relationships
- Read-Only Transactions für Queries
- Optimierte Queries mit JOIN FETCH (geplant)
- Indizes auf häufig abgefragten Spalten (empfohlen)

### 7.3 Wartbarkeit (NFR8)

**Anforderungen:**
- Unit-Tests mit 80%iger Code-Abdeckung

**Aktueller Stand:**
- Unit-Tests für kritische Domain Services vorhanden
- Integration Tests (geplant)

---

## 8. API-Dokumentation

### 8.1 Base URL

```
http://localhost:8080/api
```

### 8.2 Authentifizierung

Die API verwendet **HTTP Basic Authentication**.

**Header:**
```
Authorization: Basic base64(username:password)
```

**Test-User:**
- Admin: `admin` / `admin123`
- Mitarbeiter: `employee` / `employee123`
- Kunde: `customer` / `customer123`

### 8.3 Endpunkte

#### 8.3.1 Öffentliche Endpunkte

##### POST /api/customers/register

Kundenregistrierung (keine Authentifizierung erforderlich)

**Request Body:**
```json
{
  "username": "string",
  "password": "string",
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "phone": "string",
  "address": "string",
  "driverLicenseNumber": "string"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "username": "string",
  "firstName": "string",
  "lastName": "string",
  "email": "[ENCRYPTED]",
  "phone": "[ENCRYPTED]",
  "address": "[ENCRYPTED]",
  "driverLicenseNumber": "[ENCRYPTED]"
}
```

#### 8.3.2 Kunden-Endpunkte

Benötigen: `ROLE_CUSTOMER`, `ROLE_EMPLOYEE` oder `ROLE_ADMIN`

##### GET /api/bookings/search

Verfügbare Fahrzeuge suchen

**Query Parameters:**
- `vehicleType`: KLEINWAGEN | KOMPAKTKLASSE | MITTELKLASSE | OBERKLASSE | SUV | VAN | SPORTWAGEN
- `location`: string
- `startDate`: yyyy-MM-dd
- `endDate`: yyyy-MM-dd

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "licensePlate": {
      "value": "B-AB 1234"
    },
    "brand": "BMW",
    "model": "320d",
    "type": "MITTELKLASSE",
    "mileage": 50000,
    "location": "Berlin",
    "status": "VERFÜGBAR",
    "dailyPrice": 60.0
  }
]
```

##### POST /api/bookings

Buchung erstellen

**Request Body:**
```json
{
  "customerId": 1,
  "vehicleId": 1,
  "pickupDate": "2025-12-01",
  "returnDate": "2025-12-07",
  "pickupLocation": "Berlin",
  "returnLocation": "Berlin"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "customerId": 1,
  "vehicle": { ... },
  "pickupDate": "2025-12-01",
  "returnDate": "2025-12-07",
  "pickupLocation": "Berlin",
  "returnLocation": "Berlin",
  "status": "ANFRAGE",
  "totalPrice": 420.00
}
```

##### PUT /api/bookings/{id}/confirm

Buchung bestätigen

**Response:** `204 No Content`

##### PUT /api/bookings/{id}/cancel

Buchung stornieren (bis 24h vor Abholung)

**Response:** `204 No Content`

##### GET /api/bookings/customer/{customerId}

Buchungshistorie abrufen

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "customerId": 1,
    "vehicle": { ... },
    "pickupDate": "2025-12-01",
    "returnDate": "2025-12-07",
    "status": "BESTÄTIGT",
    "totalPrice": 420.00
  }
]
```

##### PUT /api/customers/{id}

Kundendaten aktualisieren

**Request Body:**
```json
{
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "phone": "string",
  "address": "string"
}
```

**Response:** `200 OK`

#### 8.3.3 Mitarbeiter-Endpunkte

Benötigen: `ROLE_EMPLOYEE` oder `ROLE_ADMIN`

##### GET /api/vehicles

Alle Fahrzeuge abrufen

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "licensePlate": { "value": "B-AB 1234" },
    "brand": "BMW",
    "model": "320d",
    "type": "MITTELKLASSE",
    "mileage": 50000,
    "location": "Berlin",
    "status": "VERFÜGBAR",
    "dailyPrice": 60.0
  }
]
```

##### POST /api/vehicles

Fahrzeug hinzufügen

**Request Body:**
```json
{
  "licensePlate": "B-AB 1234",
  "brand": "BMW",
  "model": "320d",
  "type": "MITTELKLASSE",
  "mileage": 50000,
  "location": "Berlin",
  "dailyPrice": 60.0
}
```

**Response:** `201 Created`

##### PUT /api/vehicles/{id}

Fahrzeug bearbeiten

**Request Body:**
```json
{
  "brand": "BMW",
  "model": "320d",
  "type": "MITTELKLASSE",
  "location": "Berlin",
  "dailyPrice": 65.0
}
```

**Response:** `200 OK`

##### PUT /api/vehicles/{id}/out-of-service

Fahrzeug außer Betrieb setzen

**Response:** `204 No Content`

##### POST /api/rentals/checkout

Check-out durchführen

**Request Body:**
```json
{
  "bookingId": 1,
  "mileage": 50000,
  "condition": "Gut"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "bookingId": 1,
  "vehicleId": 1,
  "customerId": 1,
  "plannedPickupDate": "2025-12-01",
  "plannedReturnDate": "2025-12-07",
  "pickupMileage": 50000,
  "status": "AUSGEGEBEN"
}
```

##### POST /api/rentals/{id}/checkin

Check-in durchführen

**Request Body:**
```json
{
  "mileage": 50100,
  "condition": "Gut"
}
```

**Response:** `200 OK`

##### POST /api/rentals/{id}/damage

Schadensbericht erstellen

**Request Body:**
```json
{
  "description": "Kratzer an der Tür",
  "repairCost": 250.00,
  "notes": "Kunde hat Versicherung"
}
```

**Response:** `201 Created`

### 8.4 Fehlerbehandlung

**Standard Error Response:**
```json
{
  "timestamp": "2025-11-23T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Fehlerbeschreibung",
  "path": "/api/bookings"
}
```

**Häufige Status Codes:**
- `200 OK` - Erfolgreich
- `201 Created` - Ressource erstellt
- `204 No Content` - Erfolgreich, kein Inhalt
- `400 Bad Request` - Ungültige Anfrage
- `401 Unauthorized` - Nicht authentifiziert
- `403 Forbidden` - Keine Berechtigung
- `404 Not Found` - Ressource nicht gefunden
- `500 Internal Server Error` - Serverfehler

---

## 9. Sicherheit

### 9.1 Authentifizierung

**Methode:** HTTP Basic Authentication

**Konfiguration:**
- Stateless Sessions
- BCrypt Password Hashing
- Custom UserDetailsService

### 9.2 Autorisierung

**Rollenbasierte Zugriffskontrolle (RBAC):**

| Endpunkt | CUSTOMER | EMPLOYEE | ADMIN |
|----------|----------|----------|-------|
| POST /api/customers/register | ✅ | ✅ | ✅ |
| GET /api/bookings/search | ✅ | ✅ | ✅ |
| POST /api/bookings | ✅ | ✅ | ✅ |
| PUT /api/bookings/{id}/cancel | ✅ | ✅ | ✅ |
| GET /api/vehicles | ❌ | ✅ | ✅ |
| POST /api/vehicles | ❌ | ✅ | ✅ |
| POST /api/rentals/checkout | ❌ | ✅ | ✅ |
| POST /api/rentals/{id}/checkin | ❌ | ✅ | ✅ |

### 9.3 Verschlüsselung

**Verschlüsselte Kundendaten:**
- E-Mail
- Telefonnummer
- Adresse
- Führerscheinnummer

**Algorithmus:** PBEWithMD5AndDES (für Produktion: AES-256 empfohlen)

**Konfiguration:**
```properties
jasypt.encryptor.algorithm=PBEWithMD5AndDES
jasypt.encryptor.password=${JASYPT_ENCRYPTOR_PASSWORD:rentacar-secret-key}
```

### 9.4 Audit-Logging

Alle sicherheitsrelevanten Aktionen werden protokolliert:

- Kundenregistrierung
- Kundendatenänderung
- Buchungserstellung, -bestätigung, -stornierung
- Fahrzeugverwaltung
- Vermietungsprozesse

**Audit-Log Tabelle:**
- `username` - Benutzername
- `action` - Durchgeführte Aktion
- `resourceType` - Typ der Ressource
- `resourceId` - ID der Ressource
- `details` - Zusätzliche Details
- `ipAddress` - IP-Adresse
- `timestamp` - Zeitstempel

### 9.5 Security Best Practices

✅ **Implementiert:**
- BCrypt Password Hashing
- RBAC
- Verschlüsselung sensibler Daten
- Audit-Logging
- Stateless Sessions

⚠️ **Für Produktion empfohlen:**
- HTTPS erzwingen
- Security Headers (CSP, HSTS, etc.)
- Rate Limiting
- Modernerer Verschlüsselungsalgorithmus (AES-256)
- CSRF Protection (für Web-Interfaces)

---

## 10. Datenbank

### 10.1 Datenbank-Schema

#### 10.1.1 Tabellen

**vehicles**
- `id` (PK)
- `license_plate` (UNIQUE)
- `brand`
- `model`
- `type` (ENUM)
- `mileage`
- `location`
- `status` (ENUM)
- `daily_price`
- `created_at`, `updated_at`, `version`

**customers**
- `id` (PK)
- `username` (UNIQUE)
- `password` (hashed)
- `first_name`
- `last_name`
- `encrypted_email`
- `encrypted_phone`
- `encrypted_address`
- `encrypted_license_number`
- `created_at`, `updated_at`, `version`

**bookings**
- `id` (PK)
- `customer_id` (FK)
- `vehicle_id` (FK)
- `pickup_date`
- `return_date`
- `pickup_location`
- `return_location`
- `status` (ENUM)
- `total_price`
- `cancellation_date`
- `created_at`, `updated_at`, `version`

**rentals**
- `id` (PK)
- `booking_id`
- `vehicle_id`
- `customer_id`
- `planned_pickup_date`
- `planned_return_date`
- `actual_pickup_time`
- `actual_return_time`
- `pickup_mileage`
- `return_mileage`
- `pickup_condition`
- `return_condition`
- `status` (ENUM)
- `additional_costs`
- `additional_costs_description`
- `created_at`, `updated_at`, `version`

**damage_reports**
- `id` (PK)
- `rental_id`
- `description`
- `repair_cost`
- `notes`
- `created_at`, `updated_at`, `version`

**users**
- `id` (PK)
- `username` (UNIQUE)
- `password` (hashed)
- `enabled`
- `created_at`, `updated_at`, `version`

**user_roles**
- `user_id` (FK)
- `role` (ENUM)

**audit_logs**
- `id` (PK)
- `username`
- `action`
- `resource_type`
- `resource_id`
- `details`
- `ip_address`
- `timestamp`
- `created_at`, `updated_at`, `version`

### 10.2 Indizes (Empfohlen)

```sql
CREATE INDEX idx_vehicle_search ON vehicles(type, location, status);
CREATE INDEX idx_booking_overlap ON bookings(vehicle_id, status, pickup_date, return_date);
CREATE INDEX idx_booking_customer ON bookings(customer_id);
CREATE INDEX idx_rental_booking ON rentals(booking_id);
```

### 10.3 Datenbank-Migration

Aktuell: `spring.jpa.hibernate.ddl-auto=create-drop` (nur für Entwicklung)

**Für Produktion:**
- Flyway oder Liquibase für Migrationen
- `spring.jpa.hibernate.ddl-auto=validate`

---

## 11. Testing

### 11.1 Unit-Tests

**Aktuelle Test-Abdeckung:**
- `BookingServiceTest` - Application Service Tests
- `AvailabilityServiceTest` - Domain Service Tests
- `BookingTest` - Aggregate Tests
- `PriceCalculationServiceTest` - Domain Service Tests

**Test-Framework:**
- JUnit 5
- Mockito

**Ausführen:**
```bash
./gradlew test
```

### 11.2 Integration Tests

**Geplant:**
- End-to-End API Tests
- Repository Integration Tests
- Security Integration Tests

### 11.3 Test-Skripte

**PowerShell-Skripte:**
- `test-api.ps1` - Funktionale API-Tests
- `test-security.ps1` - Sicherheitstests

**Ausführen:**
```powershell
.\test-api.ps1
.\test-security.ps1
```

### 11.4 Code Coverage

**Ziel:** 80% Code-Abdeckung

**Aktueller Stand:**
- Kritische Domain Services: ✅ Getestet
- Application Services: ⚠️ Teilweise getestet
- Controllers: ❌ Nicht getestet

---

## 12. Deployment

### 12.1 Build

**JAR erstellen:**
```bash
./gradlew clean build
```

**JAR-Datei:**
```
build/libs/rentacar-1.0-SNAPSHOT.jar
```

### 12.2 Ausführung

**Lokale Ausführung:**
```bash
java -jar build/libs/rentacar-1.0-SNAPSHOT.jar
```

**Mit Profil:**
```bash
java -jar -Dspring.profiles.active=prod rentacar-1.0-SNAPSHOT.jar
```

### 12.3 Produktions-Konfiguration

**application-prod.properties:**
```properties
# Database (z.B. PostgreSQL)
spring.datasource.url=jdbc:postgresql://localhost:5432/rentacar
spring.datasource.username=rentacar
spring.datasource.password=${DB_PASSWORD}

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# H2 Console deaktivieren
spring.h2.console.enabled=false

# Logging
logging.level.de.rentacar=INFO
logging.level.org.springframework.security=WARN

# Server
server.port=8080
server.ssl.enabled=true
```

### 12.4 Docker (Optional)

**Dockerfile:**
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/rentacar-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Docker Compose:**
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - JASYPT_ENCRYPTOR_PASSWORD=${JASYPT_PASSWORD}
```

---

## 13. Wartung & Erweiterung

### 13.1 Code-Qualität

**Code Review:**
- Siehe `CODE-REVIEW.md` für detaillierte Code-Review

**Hauptverbesserungen:**
1. Verschlüsselungsalgorithmus modernisieren
2. Security Headers hinzufügen
3. Pagination implementieren
4. Custom Exceptions einführen
5. DTOs einführen

### 13.2 Erweiterungsmöglichkeiten

**Geplante Features:**
- API Versioning
- DTOs für API-Responses
- Integration Tests
- Value Objects (DateRange, Location)
- Pagination für Listen-Endpunkte
- Caching (Redis)
- JWT Authentication
- Rate Limiting
- API Documentation (Swagger/OpenAPI)

### 13.3 Monitoring

**Empfohlen:**
- Spring Boot Actuator für Health Checks
- Logging (Logback/SLF4J)
- Metrics (Micrometer)
- Distributed Tracing (Zipkin/Jaeger)

### 13.4 Wartung

**Regelmäßige Aufgaben:**
- Dependency Updates
- Security Patches
- Performance Monitoring
- Log Review
- Audit-Log Review

---

## 14. Anhang

### 14.1 Glossar

- **Aggregate**: Cluster von Domain-Objekten, die als Einheit behandelt werden
- **Bounded Context**: Explizite Grenze, innerhalb derer ein Domain-Modell definiert ist
- **Domain Service**: Service, der Domain-Logik enthält, die nicht zu einem einzelnen Aggregate gehört
- **Value Object**: Objekt, das durch seine Attribute definiert ist, nicht durch Identität
- **Repository**: Abstraktion für Datenzugriff
- **Application Service**: Orchestriert Use Cases

### 14.2 Referenzen

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Domain-Driven Design](https://domainlanguage.com/ddd/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [JPA Specification](https://jakarta.ee/specifications/persistence/)

### 14.3 Kontakt & Support

**Projekt-Repository:**
- Lokales Repository: `C:\Uni\Projekt\RENTCARPROJEKT`

**Dokumentation:**
- `README.md` - Projektübersicht
- `API-TESTING.md` - API-Test-Dokumentation
- `CODE-REVIEW.md` - Code-Review
- `TEST-ERGEBNISSE.md` - Test-Ergebnisse

### 14.4 Changelog

**Version 1.0.0 (2025-11-23)**
- Initiale Implementierung
- DDD-Architektur
- RBAC-Sicherheit
- DSGVO-konforme Verschlüsselung
- Audit-Logging
- Unit-Tests

---

## 15. Lizenz & Copyright

Dieses Projekt wurde für akademische Zwecke erstellt.

---

**Ende der Dokumentation**



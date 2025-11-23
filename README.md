# RentACar - Autovermietung Backend

Ein modernes Backend-System für eine Autovermietung, implementiert mit **Domain-Driven Design (DDD)** und **Spring Boot**.

## Technologie-Stack

- **Java 17**
- **Gradle** (Build-Tool)
- **Spring Boot 3.2.0**
  - Spring Web (REST API)
  - Spring Data JPA
  - Spring Security (RBAC)
- **H2 Database** (In-Memory)
- **JUnit 5** (Testing)
- **Lombok** (Code-Reduktion)
- **Jasypt** (Verschlüsselung für DSGVO-Konformität)

## Architektur

Das Projekt folgt strikt den Prinzipien des **Domain-Driven Design (DDD)** mit klarer Trennung in:

### Bounded Contexts

1. **Vehicle Context** - Fahrzeugverwaltung
2. **Customer Context** - Kundenverwaltung
3. **Booking Context** - Buchungsverwaltung
4. **Rental Context** - Vermietungsprozess

### Schichtenarchitektur

Jeder Bounded Context ist in folgende Schichten unterteilt:

- **Domain Layer**: Aggregates, Entities, Value Objects, Domain Services, Repository Interfaces
- **Application Layer**: Application Services (Use Cases), DTOs
- **Infrastructure Layer**: JPA Entities, Repository Implementations, External Services
- **Web Layer**: REST Controllers

## Funktionale Anforderungen

### 1. Fahrzeugverwaltung
- Verwaltung von Fahrzeugtypen (Kleinwagen, SUV, etc.)
- Eigenschaften: Kennzeichen, Marke, Modell, Kilometerstand, Standort, Status
- Mitarbeiter können Fahrzeuge hinzufügen, bearbeiten und außer Betrieb setzen

### 2. Kundenverwaltung
- Speicherung von Kundendaten (Name, Adresse, Führerscheinnummer, Kontaktdaten)
- Kunden können sich registrieren und ihre Daten ändern
- Anzeige der Buchungshistorie pro Kunde
- **DSGVO-konforme Verschlüsselung** aller sensiblen Daten

### 3. Buchungsverwaltung
- Kunden können Fahrzeuge suchen (Zeitraum, Typ, Standort)
- **Robuste Verfügbarkeitsprüfung** zur Verhinderung von Überbuchungen
- Buchung enthält: Kunde, Fahrzeug, Abhol-/Rückgabedatum, Orte, Gesamtpreis
- Kunden können bis 24h vor Abholung stornieren
- Preisberechnung basierend auf Kategorie und Dauer

### 4. Vermietungsprozess
- Mitarbeiter führen Check-out (Übergabe, Kilometerstand, Zustand) durch
- Mitarbeiter führen Check-in (Rückgabe, Kilometerstand, Schadensprüfung) durch
- Erstellung von Schadensberichten
- Berechnung von Zusatzkosten (Verspätung, Schäden, etc.)

## Nicht-funktionale Anforderungen

### Sicherheit (NFR3, NFR4, NFR5)
- **RBAC (Rollenbasierte Zugriffskontrolle)**:
  - `ROLE_CUSTOMER` - Kunde
  - `ROLE_EMPLOYEE` - Mitarbeiter
  - `ROLE_ADMIN` - Administrator
- **Verschlüsselung**: Kundendaten werden verschlüsselt gespeichert (DSGVO-konform)
- **Audit-Log**: Alle sicherheitsrelevanten Aktionen werden protokolliert

### Performance (NFR1)
- Fahrzeugsuche optimiert für < 2 Sekunden

### Wartbarkeit (NFR8)
- Unit-Tests mit hoher Code-Abdeckung (Ziel: 80%)
- Klare DDD-Struktur für einfache Wartung

## Projektstruktur

```
src/main/java/de/rentacar/
├── RentACarApplication.java          # Spring Boot Application
├── shared/                           # Shared Kernel
│   ├── domain/                       # BaseEntity, AuditLog, AuditService
│   ├── infrastructure/               # AuditLogRepository
│   └── security/                     # Security Config, User, Roles
├── vehicle/                          # Vehicle Bounded Context
│   ├── domain/                       # Vehicle, VehicleType, VehicleStatus, LicensePlate
│   ├── application/                  # VehicleManagementService
│   ├── infrastructure/               # VehicleRepositoryImpl, VehicleJpaRepository
│   └── web/                          # VehicleController
├── customer/                         # Customer Bounded Context
│   ├── domain/                       # Customer, EncryptedString
│   ├── application/                  # CustomerService
│   ├── infrastructure/               # CustomerRepositoryImpl, EncryptionService
│   └── web/                          # CustomerController
├── booking/                          # Booking Bounded Context
│   ├── domain/                       # Booking, BookingStatus, AvailabilityService, PriceCalculationService
│   ├── application/                  # BookingService
│   ├── infrastructure/               # BookingRepositoryImpl, BookingJpaRepository
│   └── web/                          # BookingController
└── rental/                           # Rental Bounded Context
    ├── domain/                       # Rental, RentalStatus, DamageReport
    ├── application/                  # RentalService
    ├── infrastructure/               # RentalRepositoryImpl, RentalJpaRepository
    └── web/                          # RentalController
```

## API-Endpunkte

### Öffentliche Endpunkte
- `POST /api/customers/register` - Kundenregistrierung

### Kunden-Endpunkte (ROLE_CUSTOMER, ROLE_EMPLOYEE, ROLE_ADMIN)
- `GET /api/bookings/search` - Fahrzeuge suchen
- `POST /api/bookings` - Buchung erstellen
- `PUT /api/bookings/{id}/cancel` - Buchung stornieren
- `GET /api/bookings/customer/{customerId}` - Buchungshistorie
- `PUT /api/customers/{id}` - Kundendaten aktualisieren

### Mitarbeiter-Endpunkte (ROLE_EMPLOYEE, ROLE_ADMIN)
- `POST /api/vehicles` - Fahrzeug hinzufügen
- `PUT /api/vehicles/{id}` - Fahrzeug bearbeiten
- `PUT /api/vehicles/{id}/out-of-service` - Fahrzeug außer Betrieb setzen
- `POST /api/rentals/checkout` - Check-out durchführen
- `POST /api/rentals/{id}/checkin` - Check-in durchführen
- `POST /api/rentals/{id}/damage` - Schadensbericht erstellen

## Konfiguration

### H2 Database
Die H2-Konsole ist unter `http://localhost:8080/h2-console` verfügbar:
- JDBC URL: `jdbc:h2:mem:rentacardb`
- Username: `sa`
- Password: (leer)

### Verschlüsselung
Die Verschlüsselung für Kundendaten wird über Jasypt konfiguriert. Das Passwort kann über die Umgebungsvariable `JASYPT_ENCRYPTOR_PASSWORD` gesetzt werden (Standard: `rentacar-secret-key`).

## Tests ausführen

```bash
./gradlew test
```

## Anwendung starten

```bash
./gradlew bootRun
```

Die Anwendung läuft dann auf `http://localhost:8080`.

## Wichtige Implementierungsdetails

### Verfügbarkeitsprüfung (Überbuchungsverhinderung)
Die `AvailabilityService` prüft vor jeder Buchungserstellung und -bestätigung, ob das Fahrzeug im angegebenen Zeitraum verfügbar ist. Dies verhindert Überbuchungen durch Prüfung auf überlappende bestätigte Buchungen.

### Verschlüsselung
Alle sensiblen Kundendaten (E-Mail, Telefon, Adresse, Führerscheinnummer) werden mit Jasypt verschlüsselt gespeichert, um DSGVO-Konformität zu gewährleisten.

### Audit-Logging
Alle sicherheitsrelevanten Aktionen (Buchungserstellung, -bestätigung, -stornierung, Fahrzeugverwaltung, etc.) werden im Audit-Log protokolliert.

## Lizenz

Dieses Projekt wurde für akademische Zwecke erstellt.


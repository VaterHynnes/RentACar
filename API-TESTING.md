# API Testing Guide für RentACar

## Schnellstart

Führe das PowerShell-Testskript aus:
```powershell
.\test-api.ps1
```

## Manuelle Tests mit PowerShell

### 1. Kundenregistrierung (öffentlich, keine Auth nötig)

```powershell
$body = @{
    username = "testuser1"
    password = "test123"
    firstName = "Max"
    lastName = "Mustermann"
    email = "max.mustermann@example.com"
    phone = "0123456789"
    address = "Musterstraße 1, 12345 Berlin"
    driverLicenseNumber = "B123456789"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/customers/register" `
    -Method Post `
    -Body $body `
    -ContentType "application/json"
```

### 2. Authentifizierung (Basic Auth)

```powershell
$username = "customer"
$password = "customer123"
$base64AuthInfo = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${username}:${password}"))
$headers = @{
    Authorization = "Basic $base64AuthInfo"
}
```

### 3. Verfügbare Fahrzeuge suchen

```powershell
$tomorrow = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")
$nextWeek = (Get-Date).AddDays(7).ToString("yyyy-MM-dd")

Invoke-RestMethod -Uri "http://localhost:8080/api/bookings/search?vehicleType=MITTELKLASSE&location=Berlin&startDate=$tomorrow&endDate=$nextWeek" `
    -Method Get `
    -Headers $headers
```

### 4. Buchung erstellen

```powershell
$bookingBody = @{
    customerId = 1
    vehicleId = 1
    pickupDate = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")
    returnDate = (Get-Date).AddDays(7).ToString("yyyy-MM-dd")
    pickupLocation = "Berlin"
    returnLocation = "Berlin"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/bookings" `
    -Method Post `
    -Body $bookingBody `
    -ContentType "application/json" `
    -Headers $headers
```

### 5. Fahrzeuge verwalten (als Mitarbeiter)

```powershell
# Mitarbeiter-Auth
$username = "employee"
$password = "employee123"
$base64AuthInfo = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${username}:${password}"))
$headers = @{
    Authorization = "Basic $base64AuthInfo"
}

# Alle Fahrzeuge abrufen
Invoke-RestMethod -Uri "http://localhost:8080/api/vehicles" -Method Get -Headers $headers

# Neues Fahrzeug hinzufügen
$vehicleBody = @{
    licensePlate = "B-TEST 9999"
    brand = "Audi"
    model = "A4"
    type = "MITTELKLASSE"
    mileage = 10000
    location = "Berlin"
    dailyPrice = 70.0
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/vehicles" `
    -Method Post `
    -Body $vehicleBody `
    -ContentType "application/json" `
    -Headers $headers
```

## Tests mit cURL (falls installiert)

### Kundenregistrierung
```bash
curl -X POST http://localhost:8080/api/customers/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser1",
    "password": "test123",
    "firstName": "Max",
    "lastName": "Mustermann",
    "email": "max.mustermann@example.com",
    "phone": "0123456789",
    "address": "Musterstraße 1, 12345 Berlin",
    "driverLicenseNumber": "B123456789"
  }'
```

### Mit Authentifizierung
```bash
curl -X GET http://localhost:8080/api/vehicles \
  -u customer:customer123
```

## Tests mit Postman/Insomnia

1. **Base URL**: `http://localhost:8080`
2. **Authentication**: Basic Auth
   - Username: `customer` / `employee` / `admin`
   - Password: `customer123` / `employee123` / `admin123`

### Wichtige Endpunkte

#### Öffentlich (keine Auth)
- `POST /api/customers/register` - Kundenregistrierung

#### Kunden-Endpunkte (ROLE_CUSTOMER, ROLE_EMPLOYEE, ROLE_ADMIN)
- `GET /api/bookings/search` - Fahrzeuge suchen
- `POST /api/bookings` - Buchung erstellen
- `PUT /api/bookings/{id}/cancel` - Buchung stornieren
- `GET /api/bookings/customer/{customerId}` - Buchungshistorie
- `PUT /api/customers/{id}` - Kundendaten aktualisieren

#### Mitarbeiter-Endpunkte (ROLE_EMPLOYEE, ROLE_ADMIN)
- `GET /api/vehicles` - Alle Fahrzeuge abrufen
- `POST /api/vehicles` - Fahrzeug hinzufügen
- `PUT /api/vehicles/{id}` - Fahrzeug bearbeiten
- `PUT /api/vehicles/{id}/out-of-service` - Fahrzeug außer Betrieb setzen
- `POST /api/rentals/checkout` - Check-out durchführen
- `POST /api/rentals/{id}/checkin` - Check-in durchführen
- `POST /api/rentals/{id}/damage` - Schadensbericht erstellen

## H2-Konsole

1. Öffne: `http://localhost:8080/h2-console`
2. Verbindungseinstellungen:
   - **JDBC URL**: `jdbc:h2:mem:rentacardb`
   - **Username**: `sa`
   - **Password**: (leer lassen)
3. Klicke auf "Connect"

## Test-User

Die folgenden Test-User werden beim Start automatisch erstellt:

| Rolle | Username | Password | Berechtigungen |
|-------|----------|----------|----------------|
| Administrator | `admin` | `admin123` | Vollzugriff |
| Mitarbeiter | `employee` | `employee123` | Fahrzeug- und Vermietungsverwaltung |
| Kunde | `customer` | `customer123` | Buchungen, eigene Daten |

## Test-Fahrzeuge

Beim Start werden automatisch 4 Test-Fahrzeuge erstellt:
- BMW 320d (Mittelklasse) - Berlin
- Mercedes C220 (Mittelklasse) - München
- VW Golf (Kompaktklasse) - Hamburg
- Audi Q5 (SUV) - Berlin

## Beispiel-Workflow

1. **Als Kunde einloggen** und verfügbare Fahrzeuge suchen
2. **Buchung erstellen** für ein Fahrzeug
3. **Als Mitarbeiter einloggen** und die Buchung bestätigen
4. **Check-out durchführen** (Fahrzeug übergeben)
5. **Check-in durchführen** (Fahrzeug zurücknehmen)


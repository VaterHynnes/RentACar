# RentACar API Test Script
# PowerShell Script zum Testen der REST API

$baseUrl = "http://localhost:8080"

Write-Host "=== RentACar API Tests ===" -ForegroundColor Green
Write-Host ""

# 1. Test: Kundenregistrierung (öffentlicher Endpunkt)
Write-Host "1. Test: Kundenregistrierung" -ForegroundColor Yellow
$timestamp = [DateTimeOffset]::Now.ToUnixTimeSeconds()
$uniqueUsername = "testuser_$timestamp"
$registerBody = @{
    username = $uniqueUsername
    password = "test123"
    firstName = "Max"
    lastName = "Mustermann"
    email = "max.mustermann@example.com"
    phone = "0123456789"
    address = "Musterstraße 1, 12345 Berlin"
    driverLicenseNumber = "B123456789"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/customers/register" `
        -Method Post `
        -Body $registerBody `
        -ContentType "application/json"
    Write-Host "[OK] Kunde erfolgreich registriert: $($response.username)" -ForegroundColor Green
    Write-Host "     ID: $($response.id), Name: $($response.firstName) $($response.lastName)" -ForegroundColor Cyan
} catch {
    if ($_.Exception.Response.StatusCode.value__ -eq 400) {
        Write-Host "[INFO] Benutzername bereits vergeben (erwartet bei wiederholten Tests)" -ForegroundColor Yellow
    } else {
        Write-Host "[FEHLER] Status: $($_.Exception.Response.StatusCode.value__) - $($_.Exception.Message)" -ForegroundColor Red
    }
}
Write-Host ""

# 2. Test: Fahrzeuge abrufen (benötigt Authentifizierung - wird fehlschlagen)
Write-Host "2. Test: Fahrzeuge abrufen (ohne Auth - sollte fehlschlagen)" -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/vehicles" -Method Get
    Write-Host "[OK] Fahrzeuge abgerufen: $($response.Count) gefunden" -ForegroundColor Green
} catch {
    Write-Host "[ERWARTET] Fehler (keine Authentifizierung): $($_.Exception.Message)" -ForegroundColor Yellow
}
Write-Host ""

# 3. Test: Verfügbare Fahrzeuge suchen (benötigt Auth)
Write-Host "3. Test: Verfügbare Fahrzeuge suchen" -ForegroundColor Yellow
$tomorrow = (Get-Date).AddDays(1).ToString("yyyy-MM-dd")
$nextWeek = (Get-Date).AddDays(7).ToString("yyyy-MM-dd")
$searchUrl = "$baseUrl/api/bookings/search?vehicleType=MITTELKLASSE&location=Berlin&startDate=$tomorrow&endDate=$nextWeek"

try {
    # Erstelle Basic Auth Header für Test-User
    $username = "customer"
    $password = "customer123"
    $base64AuthInfo = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${username}:${password}"))
    $headers = @{
        Authorization = "Basic $base64AuthInfo"
    }
    
    $response = Invoke-RestMethod -Uri $searchUrl -Method Get -Headers $headers
    Write-Host "[OK] Verfuegbare Fahrzeuge gefunden: $($response.Count)" -ForegroundColor Green
    if ($response.Count -gt 0) {
        $response | ForEach-Object {
            Write-Host "  - $($_.brand) $($_.model) ($($_.licensePlate.value)) - $($_.dailyPrice) EUR/Tag" -ForegroundColor Cyan
        }
    }
} catch {
    Write-Host "[FEHLER] $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 4. Test: Alle Fahrzeuge abrufen (als Mitarbeiter)
Write-Host "4. Test: Alle Fahrzeuge abrufen (als Mitarbeiter)" -ForegroundColor Yellow
try {
    $username = "employee"
    $password = "employee123"
    $base64AuthInfo = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${username}:${password}"))
    $headers = @{
        Authorization = "Basic $base64AuthInfo"
    }
    
    $response = Invoke-RestMethod -Uri "$baseUrl/api/vehicles" -Method Get -Headers $headers
    Write-Host "[OK] Alle Fahrzeuge abgerufen: $($response.Count) gefunden" -ForegroundColor Green
    $response | ForEach-Object {
        Write-Host "  - $($_.brand) $($_.model) ($($_.licensePlate.value)) - Status: $($_.status)" -ForegroundColor Cyan
    }
} catch {
    Write-Host "[FEHLER] $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 5. Test: Health Check (falls Actuator aktiviert wäre)
Write-Host "5. Test: Server Status" -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/vehicles" -Method Get -UseBasicParsing -ErrorAction Stop
    Write-Host "Server laeuft und antwortet (Status: $($response.StatusCode))" -ForegroundColor Green
} catch {
    $statusCode = $null
    if ($_.Exception.Response) {
        $statusCode = $_.Exception.Response.StatusCode.value__
    }
    if ($statusCode -eq 401 -or $statusCode -eq 403) {
        Write-Host "Server laeuft (401/403 ist erwartet ohne Auth)" -ForegroundColor Green
    } else {
        Write-Host "Server antwortet nicht: $($_.Exception.Message)" -ForegroundColor Red
    }
}
Write-Host ""

Write-Host "=== Tests abgeschlossen ===" -ForegroundColor Green
Write-Host ""
Write-Host "Verfügbare Test-User:" -ForegroundColor Cyan
Write-Host "  - Admin: admin / admin123" -ForegroundColor White
Write-Host "  - Mitarbeiter: employee / employee123" -ForegroundColor White
Write-Host "  - Kunde: customer / customer123" -ForegroundColor White
Write-Host ""
Write-Host "H2-Konsole: $baseUrl/h2-console" -ForegroundColor Cyan
Write-Host "  JDBC URL: jdbc:h2:mem:rentacardb" -ForegroundColor White
Write-Host "  Username: sa" -ForegroundColor White
Write-Host "  Password: (leer)" -ForegroundColor White


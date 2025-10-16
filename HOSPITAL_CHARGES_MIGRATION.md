# Hospital Charges Migration Guide

## Overview
This migration utility updates existing Hospital records in the MongoDB database to set the `hospitalCharges` field based on hospital type.

## Migration Rules
- **Government Hospitals**: `hospitalCharges` = 0 (free)
- **Private Hospitals**: `hospitalCharges` = 5000.00 (default charge)
- **Already Set**: Hospitals with existing charges are skipped

## How to Run the Migration

> **Important:** Before running the migration, ensure you have restarted the application to load the new `MigrationController` and `HospitalMigrationService` classes.

### Option 1: Using Browser (Recommended)

#### Prerequisites
- Application must be running on http://localhost:8080
- Admin user credentials available
- Application has been restarted after adding migration code

#### Steps

1. **Login to the application**
   - Navigate to http://localhost:8080/login
   - Enter admin credentials:
     - Email: `admin@healthcare.com`
     - Password: `admin123`

2. **Trigger Migration via Browser Developer Tools**
   - Open browser Developer Tools (F12)
   - Go to Console tab
   - Run this JavaScript:
   ```javascript
   fetch('/api/admin/migration/hospital-charges', {
     method: 'POST',
     credentials: 'same-origin'
   })
   .then(response => response.json())
   .then(data => console.log('Migration Result:', data))
   .catch(error => console.error('Error:', error));
   ```

3. **View Results**
   - Check the console output for migration statistics

#### Example Response
```json
{
  "success": true,
  "message": "Migration completed: 3 total, 3 updated, 0 skipped, 0 errors",
  "totalRecords": 3,
  "updatedRecords": 3,
  "skippedRecords": 0,
  "errorRecords": 0
}
```

### Option 2: Using cURL (Linux/Mac/Git Bash)

```bash
# Step 1: Login via form and capture session cookie
curl -c cookies.txt -X POST http://localhost:8080/login \
  -d "email=admin@healthcare.com" \
  -d "password=admin123" \
  -L

# Step 2: Run migration using the session cookie
curl -b cookies.txt -X POST http://localhost:8080/api/admin/migration/hospital-charges

# Step 3: Check status
curl -b cookies.txt -X GET http://localhost:8080/api/admin/migration/status
```

### Option 3: Using PowerShell

```powershell
# Step 1: Login via form and save session
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$loginBody = @{
    email = "admin@healthcare.com"
    password = "admin123"
}

try {
    $loginResponse = Invoke-WebRequest -Uri "http://localhost:8080/login" `
        -Method POST `
        -Body $loginBody `
        -WebSession $session `
        -MaximumRedirection 0 `
        -ErrorAction SilentlyContinue
} catch {
    # Expected to redirect, ignore the error
}

Write-Host "Session established. Running migration..." -ForegroundColor Green

# Step 2: Run migration using the session
$migrationResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/admin/migration/hospital-charges" `
    -Method POST `
    -WebSession $session

# Step 3: Display result
Write-Host "`nMigration Result:" -ForegroundColor Cyan
$migrationResponse.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10

# Step 4: Check status
Write-Host "`nChecking migration service status..." -ForegroundColor Green
$statusResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/admin/migration/status" `
    -Method GET `
    -WebSession $session

Write-Host "`nService Status:" -ForegroundColor Cyan
$statusResponse.Content | ConvertFrom-Json | ConvertTo-Json -Depth 10
```

**PowerShell One-Liner:**
```powershell
$s = New-Object Microsoft.PowerShell.Commands.WebRequestSession; try { Invoke-WebRequest -Uri "http://localhost:8080/login" -Method POST -Body @{email="admin@healthcare.com";password="admin123"} -WebSession $s -MaximumRedirection 0 -ErrorAction SilentlyContinue } catch {}; (Invoke-WebRequest -Uri "http://localhost:8080/api/admin/migration/hospital-charges" -Method POST -WebSession $s).Content | ConvertFrom-Json | ConvertTo-Json
```

### Option 4: Using Postman

1. Create a new request collection
2. Add login request with admin credentials
3. Save session cookies
4. POST to `/api/admin/migration/hospital-charges`
5. View migration results

## What Happens During Migration

The migration service will:
1. Fetch all Hospital records from MongoDB
2. For each hospital:
   - **Check if already migrated** (skip if hospitalCharges already set properly)
   - **Government hospitals**: Set charges to 0
   - **Private hospitals**: Set charges to 5000.00
   - **Unknown type**: Set charges to 0 (safe default)
   - Update the `updatedAt` timestamp
   - Save to database
3. Return statistics:
   - Total records processed
   - Number updated
   - Number skipped
   - Number of errors

## Migration Logs

Check application logs for detailed migration information:

```
🔄 Starting hospital charges migration...
   ✓ Set government hospital 'General Hospital' charges to 0
   ✓ Set private hospital 'City Medical Center' charges to 5000.00
   ✓ Set government hospital 'National Hospital' charges to 0
✅ Hospital charges migration completed!
   Total hospitals: 3
   Updated: 3
   Skipped (already set): 0
   Errors: 0
```

## Safety Features

- **Idempotent**: Can be run multiple times safely
- **Skips already migrated records**: Won't overwrite existing valid charges
- **Transaction support**: Uses `@Transactional` for data consistency
- **Error handling**: Continues processing even if individual records fail
- **Admin-only**: Requires ADMIN role authentication

## Configuration

To change the default private hospital charge:

Edit `HospitalMigrationService.java`:
```java
private static final BigDecimal DEFAULT_PRIVATE_HOSPITAL_CHARGE = new BigDecimal("5000.00");
```

## Troubleshooting

### Unauthorized Error
- Ensure you're logged in as an admin user
- Check session cookies are being sent with the request

### Migration Shows All Skipped
- Records already have hospitalCharges set
- This is expected behavior if migration was already run

### Partial Success
- Check logs for specific error messages
- Individual record failures won't stop the entire migration
- Failed records will be counted in `errorRecords`

## Reverting Changes

To revert or re-run migration:
1. Manually update MongoDB records:
   ```javascript
   db.hospitals.updateMany(
     { type: "GOVERNMENT" },
     { $set: { hospitalCharges: NumberDecimal("0") } }
   )
   
   db.hospitals.updateMany(
     { type: "PRIVATE" },
     { $set: { hospitalCharges: NumberDecimal("5000.00") } }
   )
   ```

2. Or set charges to `null` to re-trigger migration:
   ```javascript
   db.hospitals.updateMany({}, { $unset: { hospitalCharges: "" } })
   ```

## Security

- Endpoint is protected with admin role check
- Session-based authentication required
- No direct database access needed
- All operations logged

## Next Steps

After running the migration:
1. Verify all hospitals have correct charges set
2. Test appointment booking with hospital charges
3. Update any reports/analytics that use hospital charges
4. Consider adding hospital charge editing UI for admins

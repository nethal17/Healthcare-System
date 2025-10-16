# Quick Start: Hospital Charges Migration

## What Was Added
✅ `HospitalMigrationService.java` - Service to update hospital charges
✅ `MigrationController.java` - REST endpoint to trigger migration
✅ `HOSPITAL_CHARGES_MIGRATION.md` - Complete documentation

## Quick Setup

### 1. Restart the Application
The application needs to be restarted to load the new migration code.

**In VS Code:**
- Stop the running application (if any)
- Run: `HealthCareSystemApplication` 
- Wait for "Started HealthCareSystemApplication" in the console

**Or via Maven:**
```powershell
.\mvnw.cmd spring-boot:run
```

### 2. Run the Migration

**Easiest Method - Browser Console:**

1. Open http://localhost:8080/login
2. Login with:
   - Email: `admin@healthcare.com`
   - Password: `admin123`
3. Open Developer Tools (F12) → Console tab
4. Paste and run:
```javascript
fetch('/api/admin/migration/hospital-charges', {
  method: 'POST',
  credentials: 'same-origin'
})
.then(response => response.json())
.then(data => console.log('Migration Result:', data))
.catch(error => console.error('Error:', error));
```

**Expected Output:**
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

## What the Migration Does

- ✅ Sets `hospitalCharges = 0` for all GOVERNMENT hospitals
- ✅ Sets `hospitalCharges = 5000.00` for all PRIVATE hospitals  
- ✅ Skips hospitals that already have charges set
- ✅ Updates the `updatedAt` timestamp
- ✅ Logs detailed progress to console

## Verify Results

Check application logs for:
```
🔄 Starting hospital charges migration...
   ✓ Set government hospital 'General Hospital' charges to 0
   ✓ Set private hospital 'City Medical Center' charges to 5000.00
   ✓ Set government hospital 'National Hospital' charges to 0
✅ Hospital charges migration completed!
```

## Troubleshooting

### "404 Not Found" Error
- **Solution:** Restart the application to load the new controller

### "Unauthorized" Error  
- **Solution:** Make sure you're logged in as admin before running the migration

### Migration Shows "0 updated, 3 skipped"
- **Solution:** Migration was already run successfully! Charges are already set.

## See Full Documentation
For more options (PowerShell, cURL, Postman), see: `HOSPITAL_CHARGES_MIGRATION.md`

# Visual Guide: Reserved Slots Feature

## 🎨 How Reserved Slots Appear to Users

### Time Slot Grid Layout

```
┌─────────────────────────────────────────────────────────────┐
│  Legend:                                                     │
│  ⬜ Available   🟧 Being Reserved   🟦 Your Selection       │
└─────────────────────────────────────────────────────────────┘

Morning Slots (9:00 AM - 12:30 PM)
┌─────────┬─────────┬─────────┬─────────┬─────────┐
│ ⬜ 9:00  │ ⬜ 9:30  │ 🟧 10:00 │ ⬜ 10:30 │ ⬜ 11:00 │
│   AM    │   AM    │   AM 🕐 │   AM    │   AM    │
└─────────┴─────────┴─────────┴─────────┴─────────┘
┌─────────┬─────────┬─────────┐
│ ⬜ 11:30 │ ⬜ 12:00 │ ⬜ 12:30 │
│   AM    │   PM    │   PM    │
└─────────┴─────────┴─────────┘

Afternoon Slots (2:00 PM - 5:00 PM)
┌─────────┬─────────┬─────────┬─────────┬─────────┐
│ 🟧 2:00  │ ⬜ 2:30  │ ⬜ 3:00  │ 🟧 3:30  │ ⬜ 4:00  │
│   PM 🕐 │   PM    │   PM    │   PM 🕐 │   PM    │
└─────────┴─────────┴─────────┴─────────┴─────────┘
┌─────────┬─────────┐
│ ⬜ 4:30  │ ⬜ 5:00  │
│   PM    │   PM    │
└─────────┴─────────┴─────────┘

Legend:
⬜ = Available (click to select)
🟧 = Being Reserved (disabled, another user is booking)
🟦 = Your Selection (after you click)
🕐 = Hourglass icon indicating temporary reservation
```

---

## 🔄 Concurrent Booking Flow Diagram

```
Time: 14:30:00 - User A and User B both want 10:00 AM slot
═══════════════════════════════════════════════════════════════

User A's Screen              Backend/DB              User B's Screen
─────────────────            ──────────              ─────────────────

Shows: ⬜ 10:00 AM
                                                     Shows: ⬜ 10:00 AM

Clicks 10:00 AM
     │
     ├──────────────────────>
                            Reservation Service
                            checks DB for active
                            reservations at 10:00
                            
                            ✓ No conflicts
                            Creates reservation:
                            {
                              doctorId: "123"
                              slotDateTime: 10:00
                              patientId: "A"
                              status: ACTIVE
                            }
                            
     <──────────────────────
                            Success!
Shows: 🟦 10:00 AM                                    [1 second later]
(blue, selected)                                      Clicks 10:00 AM
                                                           │
                                                           ├─────────>
                                                      Reservation Service
                                                      checks DB for active
                                                      reservations at 10:00
                                                      
                                                      ✗ CONFLICT FOUND!
                                                      User A already reserved
                                                      
                                                      Returns error
                                                           <─────────
                                                      Slot unavailable!
                                                      
                                                      Shows: 🟧 10:00 AM 🕐
                                                      (orange, disabled)
                                                      Error: "This slot is
                                                      being reserved"

Confirms booking
     │
     ├──────────────────────>
                            AppointmentService
                            1. Checks reservation valid
                            2. Checks no appointment exists
                            3. Creates appointment:
                            {
                              doctorId: "123"
                              appointmentDateTime: 10:00
                              status: SCHEDULED
                            }
                            4. Marks reservation CONFIRMED
                            
     <──────────────────────
                            Success!
Redirects to success page
                                                      Refreshes page
                                                           │
                                                           ├─────────>
                                                      Gets available slots
                                                      10:00 AM has appointment
                                                      
                                                      ✗ Not in list
                                                           <─────────
                                                      
                                                      10:00 AM removed
                                                      (fully booked)
```

---

## 🛡️ Race Condition Prevention: 3-Layer Protection

```
Layer 1: Application Logic
┌─────────────────────────────────────────────┐
│  @Transactional                             │
│  public Appointment bookAppointment(...) {  │
│    // Check existing appointments          │
│    if (slotAlreadyBooked) {                │
│      throw Exception                        │
│    }                                        │
│  }                                          │
└─────────────────────────────────────────────┘
                    ↓
          Prevents most conflicts
                    ↓

Layer 2: Database Unique Index
┌─────────────────────────────────────────────┐
│  @CompoundIndex(                            │
│    def = "{'doctorId': 1,                   │
│           'appointmentDateTime': 1,         │
│           'status': 1}",                    │
│    unique = true,                           │
│    partialFilter = "{'status': 'SCHEDULED'}"│
│  )                                          │
└─────────────────────────────────────────────┘
                    ↓
      Catches simultaneous writes
                    ↓

Layer 3: Reservation Lock
┌─────────────────────────────────────────────┐
│  5-minute temporary reservation             │
│  - Only one ACTIVE per doctor+slot          │
│  - Prevents UI conflicts                    │
│  - Auto-expires if not confirmed            │
└─────────────────────────────────────────────┘
```

---

## 🎯 User Experience Scenarios

### Scenario 1: Normal Booking (Happy Path)
```
1. User sees all available slots (white)
2. User clicks 10:00 AM → turns blue
3. Backend creates reservation → other users see orange
4. User confirms → appointment created
5. Slot disappears for everyone (booked)

Result: ✅ Successful booking
```

### Scenario 2: Concurrent Booking Attempt
```
1. User A clicks 10:00 AM → turns blue for them
2. User B's screen shows 10:00 AM as orange 🕐
3. User B tries to click → button is disabled
4. User B must select different slot

Result: ✅ Prevented confusion, no error
```

### Scenario 3: Race Condition at Backend
```
1. User A and B click simultaneously (rare)
2. Both get reservation request to backend
3. First request succeeds, creates reservation
4. Second request fails → "slot already reserved"
5. Second user sees error, must select new slot

Result: ✅ Backend caught race condition
```

### Scenario 4: Database Race Condition
```
1. User A confirms booking
2. User B bypasses UI checks (hacker?)
3. Both try to create appointment simultaneously
4. MongoDB unique index rejects duplicate
5. Second user gets "duplicate key" error
6. Caught by error handler → friendly message

Result: ✅ Database prevented double booking
```

### Scenario 5: Reservation Expiry
```
1. User A clicks 10:00 AM (reserved, blue)
2. User A gets distracted for 5+ minutes
3. Scheduled cleanup task runs
4. Marks reservation as EXPIRED
5. User B refreshes → sees 10:00 AM as white again
6. User A tries to confirm → "reservation expired"

Result: ✅ Slot freed up, no deadlock
```

---

## 🎨 CSS Color Scheme

```css
/* Available Slot */
.time-slot {
  background: white;
  border: 2px solid #E5E7EB; /* gray-200 */
  color: #111827; /* gray-900 */
}
.time-slot:hover {
  border-color: #2563EB; /* blue-600 */
  background: #EFF6FF; /* blue-50 */
}

/* Reserved Slot (by others) */
.reserved-slot {
  background: #FFF7ED; /* orange-50 */
  border: 2px solid #FDBA74; /* orange-300 */
  color: #C2410C; /* orange-700 */
  cursor: not-allowed;
  opacity: 0.75;
}

/* Selected Slot (by you) */
.selected-slot {
  background: #2563EB; /* blue-600 */
  border: 2px solid #2563EB; /* blue-600 */
  color: white;
}
```

---

## 📊 State Diagram: Slot Lifecycle

```
                    ┌─────────────┐
                    │  AVAILABLE  │
                    │   (White)   │
                    └──────┬──────┘
                           │
                   User clicks slot
                           │
                           ↓
                  ┌─────────────────┐
                  │    RESERVED      │
                  │    (Orange)      │ ← Other users see this
                  │  by current user │
                  └────┬─────────┬───┘
                       │         │
         User confirms │         │ User cancels / 5 min expires
                       │         │
                       ↓         ↓
              ┌─────────────┐  ┌─────────────┐
              │   BOOKED    │  │  AVAILABLE  │
              │  (Hidden)   │  │   (White)   │
              └─────────────┘  └─────────────┘
              Appointment      Returns to
              created          available pool
```

---

## 🔍 Debugging: What to Check

### If slots appear "stuck" as reserved:
1. Check scheduled cleanup task is running:
   ```
   Look for log: "Cleaned up X expired reservations"
   ```

2. Manually check MongoDB:
   ```javascript
   db.time_slot_reservations.find({status: "ACTIVE"})
   ```

3. Check createdAt timestamps (should be < 5 min old)

### If double bookings occur:
1. Verify indexes exist:
   ```javascript
   db.appointments.getIndexes()
   // Should see unique_scheduled_appointment_idx
   ```

2. Check MongoDB version (needs 4.0+ for partial indexes)

3. Look for duplicate key errors in logs

### If UI doesn't show reserved slots:
1. Check controller returns both lists:
   ```java
   model.addAttribute("reservedSlots", reservedSlots);
   ```

2. Verify Thymeleaf template loops over reserved slots:
   ```html
   <div th:each="slot : ${morningReservedSlots}">
   ```

3. Check browser console for JavaScript errors

---

## 📱 Mobile Responsive Behavior

```
Desktop (5 columns):
┌────┬────┬────┬────┬────┐
│9:00│9:30│10:0│10:3│11:0│
└────┴────┴────┴────┴────┘

Tablet (4 columns):
┌────┬────┬────┬────┐
│9:00│9:30│10:0│10:3│
└────┴────┴────┴────┘
┌────┐
│11:0│
└────┘

Mobile (3 columns):
┌────┬────┬────┐
│9:00│9:30│10:0│
└────┴────┴────┘
┌────┬────┐
│10:3│11:0│
└────┴────┘

Classes: grid-cols-3 sm:grid-cols-4 md:grid-cols-5
```

---

## ✅ Testing Commands

### Test 1: Verify indexes created
```javascript
// MongoDB Shell
use healthcare_db
db.appointments.getIndexes()
db.time_slot_reservations.getIndexes()
```

### Test 2: Simulate concurrent booking
```bash
# Terminal 1
curl -X POST http://localhost:8080/appointments/reserve-slot \
  -d "doctorId=123&date=2025-10-17&time=10:00"

# Terminal 2 (immediately)
curl -X POST http://localhost:8080/appointments/reserve-slot \
  -d "doctorId=123&date=2025-10-17&time=10:00"

# Expected: Second request fails with "already reserved"
```

### Test 3: Check cleanup task
```bash
# Wait 6 minutes after creating reservation
# Check MongoDB
db.time_slot_reservations.find({status: "EXPIRED"})
# Should show expired reservations
```

---

**Implementation Complete! 🎉**

The system now has:
- ✅ Visual indicators for all slot states
- ✅ Three layers of race condition protection
- ✅ Clear user feedback
- ✅ Robust error handling
- ✅ Database integrity constraints

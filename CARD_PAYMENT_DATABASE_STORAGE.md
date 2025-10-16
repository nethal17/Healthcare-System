# Card Payment Database Storage Implementation

## Overview
This implementation creates a complete payment tracking system that stores all payment information in a MongoDB database collection called "payments". When a user completes a card payment using Stripe, the payment details are automatically saved to the database.

## Database Schema

### Payment Collection (MongoDB)
**Collection Name**: `payments`

#### Fields Stored:

| Field Name | Type | Description | Example |
|------------|------|-------------|---------|
| `id` | String | Unique payment ID (MongoDB ObjectId) | "65a1b2c3d4e5f6g7h8i9j0k1" |
| **User Information** | | | |
| `patientId` | String | User ID (references Patient collection) | "65a1b2c3d4e5f6g7h8i9j0k2" |
| `patientName` | String | User's full name | "John Doe" |
| **Hospital Information** | | | |
| `hospitalId` | String | Hospital ID (references Hospital collection) | "65a1b2c3d4e5f6g7h8i9j0k3" |
| `hospitalName` | String | Hospital name | "City General Hospital" |
| **Doctor Information** | | | |
| `doctorId` | String | Doctor ID (references Doctor collection) | "65a1b2c3d4e5f6g7h8i9j0k4" |
| `doctorName` | String | Doctor's name | "Dr. Sarah Smith" |
| `doctorSpecialization` | String | Doctor's specialization | "Cardiology" |
| **Payment Information** | | | |
| `amount` | BigDecimal | Payment amount | 2500.00 |
| `paymentMethod` | Enum | Payment method (CASH, CARD, INSURANCE) | CARD |
| `status` | Enum | Payment status (PENDING, COMPLETED, FAILED, REFUNDED) | COMPLETED |
| `transactionId` | String | Stripe session/transaction ID | "cs_test_a1B2c3D4e5F6g7H8" |
| **Additional Fields** | | | |
| `appointmentId` | String | Associated appointment ID | "65a1b2c3d4e5f6g7h8i9j0k5" |
| `insuranceProvider` | String | Insurance company (for INSURANCE payments) | "Blue Cross" |
| `insurancePolicyNumber` | String | Policy number (for INSURANCE payments) | "BC123456789" |
| `paymentDate` | LocalDateTime | Payment created date and time | "2025-10-16T14:30:00" |
| `createdAt` | LocalDateTime | Record creation timestamp | "2025-10-16T14:30:00" |
| `updatedAt` | LocalDateTime | Record last update timestamp | "2025-10-16T14:30:00" |

## Implementation Components

### 1. Payment Model (`Payment.java`)
**Location**: `src/main/java/com/example/health_care_system/model/Payment.java`

**Purpose**: Defines the payment data structure

**Key Features**:
- MongoDB document mapping
- All required fields for payment tracking
- Enums for payment method and status
- Timestamp tracking

### 2. Payment Repository (`PaymentRepository.java`)
**Location**: `src/main/java/com/example/health_care_system/repository/PaymentRepository.java`

**Purpose**: Database access layer for payments

**Query Methods**:
```java
- findByPatientId(String patientId)
- findByAppointmentId(String appointmentId)
- findByHospitalId(String hospitalId)
- findByDoctorId(String doctorId)
- findByPaymentMethod(PaymentMethod method)
- findByStatus(PaymentStatus status)
- findByTransactionId(String transactionId)
- findByPatientIdAndStatus(String patientId, PaymentStatus status)
```

### 3. Payment Service (`PaymentService.java`)
**Location**: `src/main/java/com/example/health_care_system/service/PaymentService.java`

**Purpose**: Business logic for payment operations

**Key Methods**:

#### `createCardPayment()`
Creates a payment record for successful card transactions
```java
public Payment createCardPayment(
    String appointmentId,
    String transactionId,  // Stripe session ID
    BigDecimal amount
)
```

#### `createCashPayment()`
Creates a payment record for cash payments
```java
public Payment createCashPayment(
    String appointmentId,
    BigDecimal amount
)
```

#### `createInsurancePayment()`
Creates a payment record for insurance payments
```java
public Payment createInsurancePayment(
    String appointmentId,
    BigDecimal amount,
    String insuranceProvider,
    String policyNumber
)
```

### 4. Updated Controllers

#### AppointmentController Updates
**New Endpoints**:

**a) Payment Success Handler**
```java
@GetMapping("/appointments/payment/success")
```
- Called by Stripe after successful payment
- Receives Stripe session ID
- Creates payment record in database
- Redirects to success page

**b) Payment Cancel Handler**
```java
@GetMapping("/appointments/payment/cancel")
```
- Called when user cancels Stripe payment
- Returns user to payment page

#### PaymentController Updates
- Creates payment records for insurance submissions
- Stores insurance payment information

### 5. Updated Stripe Integration

#### StripeService Changes
**Updated Success URL**:
```java
.setSuccessUrl("http://localhost:8081/appointments/payment/success?session_id={CHECKOUT_SESSION_ID}")
.setCancelUrl("http://localhost:8081/appointments/payment/cancel")
```

The `{CHECKOUT_SESSION_ID}` is a Stripe placeholder that gets replaced with the actual session ID.

#### card-payment.html Updates
- Stores appointment ID in sessionStorage before redirecting
- Preserves payment context across Stripe redirect

## Payment Flow

### Card Payment Flow:

```
1. User selects Card Payment
   ↓
2. Redirected to card-payment.html
   ↓
3. User clicks "Pay with Stripe"
   ↓
4. JavaScript stores appointmentId in session
   ↓
5. Backend creates Stripe checkout session
   ↓
6. User redirected to Stripe payment page
   ↓
7. User completes payment on Stripe
   ↓
8. Stripe redirects back to:
   /appointments/payment/success?session_id=cs_xxx
   ↓
9. Controller receives session_id
   ↓
10. PaymentService.createCardPayment() called
    - Retrieves appointment from database
    - Retrieves patient information
    - Retrieves doctor information
    - Retrieves hospital information
    - Creates Payment record with:
      * User ID and Name
      * Hospital ID and Name
      * Doctor ID, Name, and Specialization
      * Payment Amount
      * Transaction ID (Stripe session ID)
      * Payment Date and Time
      * Status: COMPLETED
   ↓
11. Payment saved to database
   ↓
12. User redirected to success page
```

### Database Entry Example:

```json
{
  "_id": "65a1b2c3d4e5f6g7h8i9j0k1",
  "appointmentId": "65a1b2c3d4e5f6g7h8i9j0k5",
  "patientId": "65a1b2c3d4e5f6g7h8i9j0k2",
  "patientName": "John Doe",
  "hospitalId": "65a1b2c3d4e5f6g7h8i9j0k3",
  "hospitalName": "City General Hospital",
  "doctorId": "65a1b2c3d4e5f6g7h8i9j0k4",
  "doctorName": "Dr. Sarah Smith",
  "doctorSpecialization": "Cardiology",
  "amount": 2500.00,
  "paymentMethod": "CARD",
  "status": "COMPLETED",
  "transactionId": "cs_test_a1B2c3D4e5F6g7H8i9J0k1L2",
  "paymentDate": "2025-10-16T14:30:00",
  "createdAt": "2025-10-16T14:30:00",
  "updatedAt": "2025-10-16T14:30:00"
}
```

## Payment Status Meanings

| Status | Description | When Used |
|--------|-------------|-----------|
| `PENDING` | Payment not yet completed | Cash and Insurance payments (paid later) |
| `COMPLETED` | Payment successfully processed | Card payments after Stripe confirmation |
| `FAILED` | Payment attempt failed | Card payment declined |
| `REFUNDED` | Payment was refunded | Appointment cancelled with refund |

## Data Retrieval

### Get Payment by Appointment
```java
Optional<Payment> payment = paymentService.getPaymentByAppointmentId(appointmentId);
```

### Get All Payments for a Patient
```java
List<Payment> payments = paymentService.getPaymentsByPatientId(patientId);
```

### Get Payment by Transaction ID
```java
Optional<Payment> payment = paymentService.getPaymentByTransactionId(transactionId);
```

### Get Hospital's Payments
```java
List<Payment> payments = paymentService.getPaymentsByHospitalId(hospitalId);
```

### Get Doctor's Payments
```java
List<Payment> payments = paymentService.getPaymentsByDoctorId(doctorId);
```

## Files Created/Modified

### New Files:
1. ✅ `PaymentService.java` - Payment business logic

### Modified Files:
1. ✅ `Payment.java` - Added required fields
2. ✅ `PaymentRepository.java` - Added query methods
3. ✅ `AppointmentController.java` - Added payment success/cancel handlers
4. ✅ `PaymentController.java` - Added payment record creation for insurance
5. ✅ `StripeService.java` - Updated success URL
6. ✅ `card-payment.html` - Store appointment ID in session

## Testing Checklist

### Card Payment Testing:
- [ ] Select private hospital with charges
- [ ] Complete appointment booking to card payment page
- [ ] Click "Pay with Stripe"
- [ ] Complete payment on Stripe test mode
- [ ] Verify redirect back to success page
- [ ] **Check database for payment record**:
  - [ ] User ID stored correctly
  - [ ] User name stored correctly
  - [ ] Hospital ID stored correctly
  - [ ] Hospital name stored correctly
  - [ ] Doctor ID stored correctly
  - [ ] Doctor name stored correctly
  - [ ] Doctor specialization stored correctly
  - [ ] Payment amount correct
  - [ ] Transaction ID from Stripe stored
  - [ ] Payment date and time recorded
  - [ ] Status is COMPLETED

### Cash Payment Testing:
- [ ] Select cash payment
- [ ] Complete booking
- [ ] **Check database for payment record**:
  - [ ] All fields populated
  - [ ] Status is PENDING

### Insurance Payment Testing:
- [ ] Select insurance payment
- [ ] Fill insurance details
- [ ] Submit form
- [ ] **Check database for payment record**:
  - [ ] All fields populated
  - [ ] Insurance provider stored
  - [ ] Policy number stored
  - [ ] Status is PENDING

## Database Queries

### View All Payments (MongoDB Shell):
```javascript
db.payments.find().pretty()
```

### View Card Payments Only:
```javascript
db.payments.find({ paymentMethod: "CARD" }).pretty()
```

### View Payments for Specific User:
```javascript
db.payments.find({ patientId: "USER_ID_HERE" }).pretty()
```

### View Completed Payments:
```javascript
db.payments.find({ status: "COMPLETED" }).pretty()
```

### View Payments by Date:
```javascript
db.payments.find({
  paymentDate: {
    $gte: ISODate("2025-10-16T00:00:00Z"),
    $lt: ISODate("2025-10-17T00:00:00Z")
  }
}).pretty()
```

## Benefits

1. **Complete Audit Trail**: Every payment is tracked with full details
2. **User Information**: Know who made the payment
3. **Hospital Analytics**: Track revenue by hospital
4. **Doctor Analytics**: Track revenue by doctor
5. **Payment Status Tracking**: Monitor pending/completed payments
6. **Transaction IDs**: Link to Stripe transactions for reconciliation
7. **Timestamp Tracking**: Know exactly when payments occurred
8. **Reporting Ready**: All data available for financial reports

## Future Enhancements

1. **Payment Reports**
   - Daily/Monthly revenue reports
   - Doctor-wise payment summaries
   - Hospital-wise payment summaries

2. **Payment History Page**
   - User can view their payment history
   - Download payment receipts

3. **Refund Processing**
   - Handle appointment cancellations
   - Process refunds through Stripe

4. **Payment Notifications**
   - Email confirmations
   - SMS notifications

5. **Analytics Dashboard**
   - Revenue charts
   - Payment method distribution
   - Success rate tracking

---

## Configuration Required

### Stripe API Key
Ensure `application.properties` has:
```properties
stripe.secretKey=your_stripe_secret_key_here
```

### Test Mode
For testing, use Stripe test keys and test card numbers:
- Card: 4242 4242 4242 4242
- Expiry: Any future date
- CVC: Any 3 digits

---

**Implementation Complete!** 🎉

All card payments made through Stripe will now be automatically stored in the database with complete user, hospital, doctor, and payment information.

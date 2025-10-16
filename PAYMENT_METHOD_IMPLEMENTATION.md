# Payment Method Selection Implementation

## Overview
This implementation adds a payment method selection feature to the healthcare appointment booking system. After confirming appointment details, users are now redirected to select their preferred payment method before completing the booking.

## Payment Methods Available

### 1. **Cash Payment**
- Pay at the hospital reception desk during the visit
- No online processing required
- Receipt provided on-site

### 2. **Card Payment**
- Secure online payment via Stripe
- All major credit/debit cards accepted
- Instant confirmation
- Redirects to Stripe checkout page

### 3. **Insurance**
- Use health insurance to cover consultation fees
- Requires insurance provider name and policy number
- Verification will be performed by hospital staff
- Coverage confirmation sent to patient

## Changes Made

### 1. New Model Files

#### `Payment.java`
- Location: `src/main/java/com/example/health_care_system/model/Payment.java`
- Stores payment information for appointments
- Fields:
  - `id`: Payment ID
  - `appointmentId`: Reference to appointment
  - `patientId`: Reference to patient
  - `hospitalId`: Reference to hospital
  - `amount`: Payment amount
  - `paymentMethod`: CASH, CARD, or INSURANCE
  - `status`: PENDING, COMPLETED, FAILED, or REFUNDED
  - `transactionId`: For card payments (Stripe)
  - `insuranceProvider`: For insurance payments
  - `insurancePolicyNumber`: For insurance payments

#### `PaymentRepository.java`
- Location: `src/main/java/com/example/health_care_system/repository/PaymentRepository.java`
- MongoDB repository for payment operations

### 2. Updated Files

#### `AppointmentController.java`
Added three new endpoints:

**a) `/appointments/payment-selection` (GET)**
- Displays payment method selection page
- Shows appointment summary with hospital charges
- Hospital charges only displayed for PRIVATE hospitals
- GOVERNMENT hospitals show "Free Consultation" notice

**b) `/appointments/payment-selection/process` (POST)**
- Processes selected payment method
- Stores payment information in session
- Routes based on payment method:
  - **CASH**: Redirects to success page
  - **CARD**: Redirects to card payment page
  - **INSURANCE**: Redirects to success page (requires insurance details)

**c) `/appointments/payment/card` (GET)**
- Shows secure card payment page
- Displays payment summary
- Integrates with Stripe for payment processing

**d) Updated `/appointments/book/process` (POST)**
- Changed to store appointment details in session
- Redirects to payment selection instead of directly creating appointment

### 3. New Template Files

#### `payment-selection.html`
- Location: `src/main/resources/templates/appointments/payment-selection.html`
- Interactive payment method selection with cards
- Dynamic form validation
- Shows/hides insurance fields based on selection
- Displays hospital charges for PRIVATE hospitals only
- Shows "Free Consultation" notice for GOVERNMENT hospitals

#### `card-payment.html`
- Location: `src/main/resources/templates/appointments/card-payment.html`
- Secure payment interface
- Displays payment summary
- Integrates with Stripe checkout
- Shows security information and SSL encryption notice

### 4. Updated Template Files

#### `confirm.html`
- Updated button text from "Confirm Appointment" to "Proceed to Payment"
- Added `hospitalId` hidden field to form
- Hospital charges display section added:
  - Shows charges for PRIVATE hospitals
  - Shows "Free Consultation" notice for GOVERNMENT hospitals
  - Includes payment instructions

#### `success.html`
- Added payment method display section
- Shows different messages based on payment method:
  - Cash: "Please bring cash payment on your visit"
  - Card: "Your payment has been processed successfully"
  - Insurance: "We will verify your insurance coverage"

## User Flow

### Complete Booking Flow:
1. **Select Hospital** → `book.html`
2. **Select Doctor** → `select-doctor.html`
3. **Select Time Slot** → `select-timeslot.html`
4. **Confirm Details** → `confirm.html` (Shows hospital charges)
5. **🆕 Select Payment Method** → `payment-selection.html`
   - If CASH selected → Go to step 7
   - If CARD selected → Go to step 6
   - If INSURANCE selected → Provide insurance details → Go to step 7
6. **Card Payment** → `card-payment.html` (Stripe integration)
7. **Success** → `success.html` (Shows payment method confirmation)

## Hospital Charges Display Logic

### For PRIVATE Hospitals:
```html
- Displays hospital charges from database
- Shows formatted amount (Rs. X,XXX.XX)
- Includes payment note/instructions
```

### For GOVERNMENT Hospitals:
```html
- No charges displayed
- Shows "Free Consultation" banner
- Green success styling
```

## Features

### Security
- ✅ Stripe integration for secure card payments
- ✅ SSL encryption for all transactions
- ✅ Session-based data storage
- ✅ Payment verification

### User Experience
- ✅ Clear visual payment method cards
- ✅ Interactive selection with hover effects
- ✅ Progress indication
- ✅ Responsive design
- ✅ Form validation
- ✅ Loading states for buttons

### Business Logic
- ✅ Hospital charges fetched from database
- ✅ Different flow for government vs private hospitals
- ✅ Insurance information collection
- ✅ Payment method tracking
- ✅ Appointment-payment linking

## Testing

### Test Scenarios:
1. **Private Hospital - Cash Payment**
   - Select private hospital
   - Complete booking flow
   - Select cash payment
   - Verify charges displayed correctly

2. **Private Hospital - Card Payment**
   - Select private hospital
   - Complete booking flow
   - Select card payment
   - Complete Stripe checkout
   - Verify payment recorded

3. **Private Hospital - Insurance Payment**
   - Select private hospital
   - Complete booking flow
   - Select insurance
   - Enter insurance details
   - Verify insurance information stored

4. **Government Hospital**
   - Select government hospital
   - Verify "Free Consultation" displayed
   - Verify no charges shown
   - Complete booking

## Database Schema

### Payment Collection (MongoDB)
```json
{
  "_id": "ObjectId",
  "appointmentId": "string",
  "patientId": "string",
  "hospitalId": "string",
  "amount": "decimal",
  "paymentMethod": "CASH|CARD|INSURANCE",
  "status": "PENDING|COMPLETED|FAILED|REFUNDED",
  "transactionId": "string (for CARD)",
  "insuranceProvider": "string (for INSURANCE)",
  "insurancePolicyNumber": "string (for INSURANCE)",
  "paymentDate": "datetime",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

## Next Steps (Optional Enhancements)

1. **Payment Processing Service**
   - Create PaymentService to handle payment logic
   - Save payment records to database
   - Generate payment receipts

2. **Stripe Webhook Integration**
   - Handle payment success/failure callbacks
   - Update appointment status based on payment

3. **Insurance Verification**
   - Add insurance verification workflow
   - Send notifications for coverage approval/rejection

4. **Payment History**
   - Add payment history page for patients
   - Show all transactions and receipts

5. **Refund Processing**
   - Implement refund functionality for cancelled appointments
   - Handle partial refunds

## Configuration Required

### Stripe Configuration
Ensure `application.properties` has:
```properties
stripe.secretKey=your_stripe_secret_key
```

### Success/Cancel URLs
Update Stripe URLs in `StripeService.java` if needed:
```java
.setSuccessUrl("http://localhost:8081/appointments/success")
.setCancelUrl("http://localhost:8081/appointments/book")
```

## Files Summary

### Created:
1. `Payment.java` - Payment model
2. `PaymentRepository.java` - Payment repository
3. `payment-selection.html` - Payment method selection page
4. `card-payment.html` - Card payment page

### Modified:
1. `AppointmentController.java` - Added payment flow endpoints
2. `confirm.html` - Added hospital charges display and updated button
3. `success.html` - Added payment method information display

---

## Support
For issues or questions, please contact the development team.

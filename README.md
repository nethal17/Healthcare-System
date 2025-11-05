# Healthcare System

A comprehensive web-based healthcare management system designed for urban hospitals in Sri Lanka. This application streamlines patient registration, appointment scheduling medical record management, payment processing, and administrative analytics.

## Features

### Patient Management
- **User Registration & Authentication**: Secure session-based authentication with BCrypt password encryption
- **Role-Based Access Control**: Separate interfaces for Patients, Doctors, Staff, and Administrators
- **Profile Management**: Users can update personal information and change passwords
- **Digital Health Cards**: QR code-enabled health cards for quick patient identification

### Appointment System
- **Online Booking**: Patients can book appointments with available time slots
- **Time Slot Management**: Configurable working hours (9 AM - 5 PM) with lunch breaks
- **Reservation System**: 15-minute temporary reservations to prevent double-booking
- **Check-In Management**: Staff can mark patient arrivals and handle no-shows
- **Email Notifications**: Automated appointment confirmations and reminders

### Payment Processing
- **Multiple Payment Methods**: 
  - Online payments via Stripe integration
  - Cash payments at hospital
  - Insurance claims processing
- **Strategy Pattern Implementation**: Flexible payment processing architecture
- **Payment Tracking**: Complete payment history and status monitoring
- **Insurance Verification**: Staff can approve/reject insurance claims

### Medical Records
- **Electronic Health Records**: Secure storage and management of patient medical history
- **Concern Submission**: Patients can submit medical concerns for doctor review
- **Doctor Response System**: Two-way communication between patients and doctors
- **PDF Generation**: Downloadable medical records and insurance confirmations

### Analytics & Reporting
- **Admin Dashboard**: Comprehensive analytics for hospital management
- **Revenue Reports**: Financial tracking and revenue analysis
- **Appointment Statistics**: Booking trends, no-show rates, and capacity utilization
- **User Analytics**: Registration trends and user demographics
- **Exportable Reports**: PDF generation for analytics reports

## Technology Stack

### Backend
- **Java 17**
- **Spring Boot 3.5.6**
  - Spring Web
  - Spring Data MongoDB
  - Spring Mail
  - Spring Security (BCrypt)
  - Spring Validation
- **MongoDB**: NoSQL database for flexible data storage
- **Lombok**: Reducing boilerplate code

### Frontend
- **Thymeleaf**: Server-side templating engine
- **HTML/CSS/JavaScript**
- **Bootstrap** (implied from web interface)

### Third-Party Integrations
- **Stripe API**: Payment gateway integration
- **Google ZXing**: QR code generation
- **iText7**: PDF document generation
- **Spring Mail**: SMTP email service

### Development Tools
- **Maven**: Dependency management and build automation
- **JaCoCo**: Code coverage analysis
- **Mockito**: Unit testing framework
- **Spring Boot DevTools**: Hot reload during development

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MongoDB Atlas account (or local MongoDB instance)
- Stripe account for payment processing
- SMTP email server credentials

## Installation & Setup

### 1. Clone the Repository
```bash
git clone https://github.com/nethal17/Healthcare-System.git
cd Healthcare-System
```

### 2. Configure Environment Variables

Create a `.env` file in the root directory with the following variables:

```properties
# MongoDB Configuration
MONGO_DATABASE=your_database_name
MONGO_USER=your_mongodb_username
MONGO_PASS=your_mongodb_password
MONGO_CLUSTER=your_cluster.mongodb.net

# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASS=your_email_app_password

# Application Configuration
BASE_URL=http://localhost:8080
PORT=8080

# Stripe Configuration
STRIPE_SECRET_KEY=sk_test_your_stripe_secret_key
```

### 3. Build the Project
```bash
./mvnw clean install
```

### 4. Run the Application
```bash
./mvnw spring-boot:run
```

The application will be available at `http://localhost:8080`

## Testing

### Run All Tests
```bash
./mvnw test
```

### Generate Coverage Report
```bash
./mvnw test jacoco:report
```

View the coverage report at `target/site/jacoco/index.html`

## Project Structure

```
Healthcare-System/
├── src/
│   ├── main/
│   │   ├── java/com/example/health_care_system/
│   │   │   ├── controller/          # MVC Controllers
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── AppointmentController.java
│   │   │   │   ├── PaymentController.java
│   │   │   │   ├── HealthCardController.java
│   │   │   │   └── ...
│   │   │   ├── service/             # Business Logic
│   │   │   │   ├── AppointmentService.java
│   │   │   │   ├── PaymentService.java
│   │   │   │   ├── StripeService.java
│   │   │   │   ├── EmailService.java
│   │   │   │   └── ...
│   │   │   ├── repository/          # MongoDB Repositories
│   │   │   ├── model/               # Domain Models
│   │   │   │   ├── User.java
│   │   │   │   ├── Patient.java
│   │   │   │   ├── Doctor.java
│   │   │   │   ├── Appointment.java
│   │   │   │   ├── Payment.java
│   │   │   │   └── ...
│   │   │   ├── strategy/            # Payment Strategy Pattern
│   │   │   ├── factory/             # Factory Pattern
│   │   │   └── exception/           # Custom Exceptions
│   │   └── resources/
│   │       ├── application.properties
│   │       └── templates/           # Thymeleaf Templates
│   └── test/                        # Unit & Integration Tests
├── pom.xml
└── README.md
```

## Design Patterns Used

- **Strategy Pattern**: Flexible payment method handling (Cash, Card, Insurance)
- **Factory Pattern**: User object creation based on roles
- **Repository Pattern**: Data access abstraction with MongoDB
- **MVC Pattern**: Clear separation of concerns

## Security Features

- BCrypt password hashing
- Session-based authentication
- HTTP-only cookies
- CSRF protection (Spring Security)
- Role-based access control
- Secure payment processing via Stripe

## Configuration

### Appointment Settings
- **Working Hours**: 9:00 AM - 5:00 PM
- **Lunch Break**: 1:00 PM - 2:00 PM
- **Slot Duration**: 30 minutes
- **Advance Booking**: Minimum 1 hour ahead
- **Booking Window**: Up to 7 days in advance
- **Reservation Timeout**: 15 minutes

### Payment Settings
- **Consultation Fee**: LKR 2,500.00
- **Currency**: Sri Lankan Rupees (LKR)
- **Payment Gateway**: Stripe

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## API Endpoints

### Authentication
- `GET /login` - Login page
- `POST /login` - Process login
- `GET /register` - Registration page
- `POST /register` - Process registration
- `GET /logout` - Logout

### Appointments
- `GET /appointments` - View appointments
- `POST /appointments/book` - Book appointment
- `POST /appointments/cancel` - Cancel appointment

### Payments
- `GET /appointments/payment` - Payment page
- `POST /appointments/payment/process` - Process payment
- `GET /appointments/payment/success` - Payment success
- `GET /appointments/payment/cancel` - Payment cancelled

### Health Cards
- `GET /health-card` - View health card
- `GET /health-card/download` - Download health card PDF

### Admin
- `GET /admin/users` - User management
- `GET /admin/analytics` - Analytics dashboard
- `GET /admin/analytics/export-pdf` - Export analytics report

## Known Issues

- None currently documented

## Contact

**Developers**: Nethal Fernando, Naduli Weerasinghe, Yasindu Gamae, Ricky Perera
**Repository**: [Healthcare-System](https://github.com/nethal17/Healthcare-System)

## 📄 License

This project is part of an academic/portfolio project.

## 🙏 Acknowledgments

- Spring Boot Framework
- MongoDB Atlas
- Stripe Payment Platform
- iText PDF Library
- Google ZXing QR Code Library

---

**Built with ❤️ for improving healthcare accessibility in Sri Lanka**

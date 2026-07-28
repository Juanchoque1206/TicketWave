# documentation architecture
https://claude.ai/code/artifact/09551682-3206-4805-acde-fa2df787baab

# TicketWave

An event-driven ticket management platform built with Spring Boot. TicketWave enables customers to search events, purchase tickets, and manage bookings for concerts, sports events, conferences, and local venues.

## Architecture

This project follows a **layered (n-tier) architecture** with clear separation of concerns:

| Layer | Responsibility |
|-------|---------------|
| **Presentation** | REST controllers handling HTTP requests/responses |
| **Service** | Business logic, validation, and orchestration |
| **Repository** | Data access via Spring Data JPA |
| **Common** | Shared DTOs, entities, enums, exceptions, and configuration |

Asynchronous communication between modules is handled via **Apache Kafka**, making the system event-driven for operations like payment processing, ticket issuance, fraud detection, and notifications.

## Tech Stack

- **Java 21** / **Spring Boot 4.1.0**
- **PostgreSQL** - Primary database
- **Apache Kafka** - Event streaming and async messaging
- **Redis** - Caching and seat reservation holds
- **Spring Security + JWT** - Authentication and authorization
- **Spring Data JPA** - ORM and data access
- **Maven** - Build tool

## Modules

| Module | Description |
|--------|-------------|
| `user` | User registration, authentication (JWT), and profile management |
| `event` | Event lifecycle management, pricing, and availability |
| `venue` | Venue and seating configuration (sections, seats) |
| `ticket` | Ticket issuance, digital delivery, and QR code generation |
| `order` | Order processing, state transitions, and expiration handling |
| `payment` | Payment processing, gateway integration, and refunds |
| `promotion` | Promotional codes with fixed or percentage discounts |
| `notification` | Multi-channel notifications (email, SMS, push) via Kafka |
| `fraud` | Risk scoring, fraud detection, and alert management |

## Kafka Topics

| Topic | Purpose |
|-------|---------|
| `ticketwave.order.created` | Triggers payment processing and notifications |
| `ticketwave.order.completed` | Triggers completion notifications |
| `ticketwave.payment.completed` | Triggers ticket issuance and confirmation |
| `ticketwave.refund.processed` | Notifies about processed refunds |
| `ticketwave.event.changed` | Notifies users about event modifications |
| `ticketwave.event.cancelled` | Triggers refunds and cancellation notifications |
| `ticketwave.fraud.check` | Triggers fraud detection analysis |
| `ticketwave.fraud.alert` | Alerts about suspicious activities |

## API Endpoints

| Path | Description | Access |
|------|-------------|--------|
| `/api/auth/**` | Register, login, token refresh | Public |
| `/api/events/**` | Event CRUD, pricing, availability | Public read / Organizer write |
| `/api/search/**` | Public event search (city, artist, venue, date) | Public |
| `/api/orders/**` | Create, retrieve, cancel orders | Authenticated |
| `/api/payments/**` | Initiate payments, webhooks | Authenticated / Public webhooks |
| `/api/tickets/**` | Retrieve tickets, QR codes | Authenticated |
| `/api/venues/**` | Venue and seating management | Public read / Organizer write |
| `/api/promotions/**` | Promotion management | Organizer / Admin |
| `/api/admin/fraud/**` | Fraud alerts and resolution | Admin |

## Prerequisites

- Java 21
- PostgreSQL (running on port 5432)
- Apache Kafka (running on port 9092)
- Redis (running on port 6379)

## Getting Started

1. **Clone the repository:**

   ```bash
   git clone <repository-url>
   cd TicketWave
   ```

2. **Set up PostgreSQL:**

   Create a database named `ticketwave`:

   ```sql
   CREATE DATABASE ticketwave;
   ```

3. **Start Kafka and Redis** using your preferred method (Docker, local install, etc.).

4. **Configure the application:**

   Update `src/main/resources/application.properties` if your services run on non-default ports or require different credentials.

5. **Build and run:**

   ```bash
   ./mvnw spring-boot:run
   ```

   On Windows:

   ```cmd
   mvnw.cmd spring-boot:run
   ```

   The application starts on `http://localhost:8080`.

## Security

- JWT-based stateless authentication
- BCrypt password hashing
- Role-based access control: `CUSTOMER`, `ORGANIZER`, `ADMIN`

## Key Features

- **Seat reservation holds** - Redis-backed temporary seat locks with configurable expiration (default: 15 minutes)
- **Order expiration** - Automatic cancellation of unpaid pending orders via scheduled tasks
- **Fraud detection** - Configurable risk scoring with thresholds for alerting and blocking
- **Multi-channel notifications** - Email, SMS, and push notifications driven by Kafka events
- **Promotion system** - National and venue-scoped discounts with usage limits and time-bound validity


## Kafka container
docker run -d --name kafka -p 9092:9092 -e KAFKA_NODE_ID=1 -e KAFKA_PROCESS_ROLES=broker,controller -e KAFKA_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 -e CLUSTER_ID=MkU3OEVBNTcwNTJENDM2Qk apache/kafka:latest
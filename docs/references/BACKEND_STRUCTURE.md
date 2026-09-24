# Final Folder and File Structure

```text
bus-management-system/
│
├── .github/
│   └── workflows/
│       └── ci.yml
│
├── docker/
│   └── postgres/
│       └── init/
│
├── docs/
│   ├── architecture/
│   │   ├── system-architecture.md
│   │   ├── er-diagram.md
│   │   ├── booking-flow.md
│   │   └── payment-flow.md
│   │
│   ├── api/
│   │   └── api-overview.md
│   │
│   └── database/
│       └── database-schema.md
│
├── src/
│   │
│   ├── main/
│   │   │
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── yourname/
│   │   │           └── busmanagement/
│   │   │               │
│   │   │               ├── BusManagementApplication.java
│   │   │               │
│   │   │               ├── common/
│   │   │               │   │
│   │   │               │   ├── audit/
│   │   │               │   │   ├── AuditableEntity.java
│   │   │               │   │   ├── AuditorAwareImpl.java
│   │   │               │   │   └── JpaAuditConfig.java
│   │   │               │   │
│   │   │               │   ├── config/
│   │   │               │   │   ├── ApplicationProperties.java
│   │   │               │   │   ├── JacksonConfig.java
│   │   │               │   │   └── OpenApiConfig.java
│   │   │               │   │
│   │   │               │   ├── exception/
│   │   │               │   │   ├── ApiException.java
│   │   │               │   │   ├── ResourceNotFoundException.java
│   │   │               │   │   ├── BusinessRuleException.java
│   │   │               │   │   ├── UnauthorizedOperationException.java
│   │   │               │   │   ├── InvalidTripException.java
│   │   │               │   │   ├── TripConflictException.java
│   │   │               │   │   ├── DriverUnavailableException.java
│   │   │               │   │   ├── SeatNotAvailableException.java
│   │   │               │   │   ├── BookingExpiredException.java
│   │   │               │   │   ├── InvalidBookingStateException.java
│   │   │               │   │   ├── PaymentException.java
│   │   │               │   │   ├── RefundException.java
│   │   │               │   │   ├── TicketAlreadyUsedException.java
│   │   │               │   │   └── GlobalExceptionHandler.java
│   │   │               │   │
│   │   │               │   ├── response/
│   │   │               │   │   ├── ApiErrorResponse.java
│   │   │               │   │   ├── FieldValidationError.java
│   │   │               │   │   └── PageResponse.java
│   │   │               │   │
│   │   │               │   ├── security/
│   │   │               │   │   ├── SecurityConfig.java
│   │   │               │   │   ├── JwtProperties.java
│   │   │               │   │   ├── JwtService.java
│   │   │               │   │   ├── CustomUserDetailsService.java
│   │   │               │   │   ├── CurrentUser.java
│   │   │               │   │   └── SecurityUtils.java
│   │   │               │   │
│   │   │               │   └── util/
│   │   │               │       ├── BusinessIdGenerator.java
│   │   │               │       ├── DateTimeUtils.java
│   │   │               │       └── MoneyUtils.java
│   │   │               │
│   │   │               ├── auth/
│   │   │               │   ├── controller/
│   │   │               │   │   └── AuthController.java
│   │   │               │   │
│   │   │               │   ├── dto/
│   │   │               │   │   ├── request/
│   │   │               │   │   │   ├── RegisterRequest.java
│   │   │               │   │   │   ├── LoginRequest.java
│   │   │               │   │   │   ├── RefreshTokenRequest.java
│   │   │               │   │   │   └── LogoutRequest.java
│   │   │               │   │   │
│   │   │               │   │   └── response/
│   │   │               │   │       └── AuthResponse.java
│   │   │               │   │
│   │   │               │   ├── service/
│   │   │               │   │   ├── AuthService.java
│   │   │               │   │   └── RefreshTokenService.java
│   │   │               │   │
│   │   │               │   └── token/
│   │   │               │       ├── RefreshToken.java
│   │   │               │       └── RefreshTokenRepository.java
│   │   │               │
│   │   │               ├── user/
│   │   │               │   ├── controller/
│   │   │               │   │   ├── UserController.java
│   │   │               │   │   └── AdminUserController.java
│   │   │               │   │
│   │   │               │   ├── dto/
│   │   │               │   │   ├── request/
│   │   │               │   │   │   ├── UpdateProfileRequest.java
│   │   │               │   │   │   ├── ChangePasswordRequest.java
│   │   │               │   │   │   └── UpdateUserStatusRequest.java
│   │   │               │   │   │
│   │   │               │   │   └── response/
│   │   │               │   │       └── UserResponse.java
│   │   │               │   │
│   │   │               │   ├── entity/
│   │   │               │   │   ├── User.java
│   │   │               │   │   ├── UserRole.java
│   │   │               │   │   ├── Role.java
│   │   │               │   │   └── UserStatus.java
│   │   │               │   │
│   │   │               │   ├── repository/
│   │   │               │   │   └── UserRepository.java
│   │   │               │   │
│   │   │               │   ├── mapper/
│   │   │               │   │   └── UserMapper.java
│   │   │               │   │
│   │   │               │   └── service/
│   │   │               │       └── UserService.java
│   │   │               │
│   │   │               ├── station/
│   │   │               │   ├── controller/
│   │   │               │   │   ├── StationController.java
│   │   │               │   │   └── AdminStationController.java
│   │   │               │   │
│   │   │               │   ├── dto/
│   │   │               │   │   ├── request/
│   │   │               │   │   │   └── StationRequest.java
│   │   │               │   │   └── response/
│   │   │               │   │       └── StationResponse.java
│   │   │               │   │
│   │   │               │   ├── entity/
│   │   │               │   │   └── Station.java
│   │   │               │   │
│   │   │               │   ├── repository/
│   │   │               │   │   └── StationRepository.java
│   │   │               │   │
│   │   │               │   ├── mapper/
│   │   │               │   │   └── StationMapper.java
│   │   │               │   │
│   │   │               │   └── service/
│   │   │               │       └── StationService.java
│   │   │               │
│   │   │               ├── route/
│   │   │               │   ├── controller/
│   │   │               │   │   ├── RouteController.java
│   │   │               │   │   └── AdminRouteController.java
│   │   │               │   │
│   │   │               │   ├── dto/
│   │   │               │   │   ├── request/
│   │   │               │   │   │   ├── RouteRequest.java
│   │   │               │   │   │   ├── RouteStopRequest.java
│   │   │               │   │   │   └── ReplaceRouteStopsRequest.java
│   │   │               │   │   │
│   │   │               │   │   └── response/
│   │   │               │   │       ├── RouteResponse.java
│   │   │               │   │       └── RouteStopResponse.java
│   │   │               │   │
│   │   │               │   ├── entity/
│   │   │               │   │   ├── Route.java
│   │   │               │   │   └── RouteStop.java
│   │   │               │   │
│   │   │               │   ├── repository/
│   │   │               │   │   ├── RouteRepository.java
│   │   │               │   │   └── RouteStopRepository.java
│   │   │               │   │
│   │   │               │   ├── mapper/
│   │   │               │   │   └── RouteMapper.java
│   │   │               │   │
│   │   │               │   └── service/
│   │   │               │       ├── RouteService.java
│   │   │               │       └── RouteStopService.java
│   │   │               │
│   │   │               ├── fleet/
│   │   │               │   │
│   │   │               │   ├── seatlayout/
│   │   │               │   │   ├── controller/
│   │   │               │   │   │   └── SeatLayoutController.java
│   │   │               │   │   ├── dto/
│   │   │               │   │   │   ├── request/
│   │   │               │   │   │   │   ├── SeatLayoutRequest.java
│   │   │               │   │   │   │   └── SeatRequest.java
│   │   │               │   │   │   └── response/
│   │   │               │   │   │       ├── SeatLayoutResponse.java
│   │   │               │   │   │       └── SeatResponse.java
│   │   │               │   │   ├── entity/
│   │   │               │   │   │   ├── SeatLayout.java
│   │   │               │   │   │   ├── Seat.java
│   │   │               │   │   │   ├── SeatType.java
│   │   │               │   │   │   └── DeckType.java
│   │   │               │   │   ├── repository/
│   │   │               │   │   │   ├── SeatLayoutRepository.java
│   │   │               │   │   │   └── SeatRepository.java
│   │   │               │   │   ├── mapper/
│   │   │               │   │   │   └── SeatLayoutMapper.java
│   │   │               │   │   └── service/
│   │   │               │   │       └── SeatLayoutService.java
│   │   │               │   │
│   │   │               │   ├── bus/
│   │   │               │   │   ├── controller/
│   │   │               │   │   │   └── BusController.java
│   │   │               │   │   ├── dto/
│   │   │               │   │   │   ├── request/
│   │   │               │   │   │   │   ├── CreateBusRequest.java
│   │   │               │   │   │   │   └── UpdateBusStatusRequest.java
│   │   │               │   │   │   └── response/
│   │   │               │   │   │       └── BusResponse.java
│   │   │               │   │   ├── entity/
│   │   │               │   │   │   ├── Bus.java
│   │   │               │   │   │   ├── BusType.java
│   │   │               │   │   │   └── BusStatus.java
│   │   │               │   │   ├── repository/
│   │   │               │   │   │   └── BusRepository.java
│   │   │               │   │   ├── mapper/
│   │   │               │   │   │   └── BusMapper.java
│   │   │               │   │   └── service/
│   │   │               │   │       └── BusService.java
│   │   │               │   │
│   │   │               │   ├── driver/
│   │   │               │   │   ├── controller/
│   │   │               │   │   │   └── DriverController.java
│   │   │               │   │   ├── dto/
│   │   │               │   │   │   ├── request/
│   │   │               │   │   │   │   ├── DriverRequest.java
│   │   │               │   │   │   │   └── UpdateDriverStatusRequest.java
│   │   │               │   │   │   └── response/
│   │   │               │   │   │       └── DriverResponse.java
│   │   │               │   │   ├── entity/
│   │   │               │   │   │   ├── Driver.java
│   │   │               │   │   │   └── DriverStatus.java
│   │   │               │   │   ├── repository/
│   │   │               │   │   │   └── DriverRepository.java
│   │   │               │   │   ├── mapper/
│   │   │               │   │   │   └── DriverMapper.java
│   │   │               │   │   └── service/
│   │   │               │   │       └── DriverService.java
│   │   │               │   │
│   │   │               │   └── maintenance/
│   │   │               │       ├── controller/
│   │   │               │       │   └── BusMaintenanceController.java
│   │   │               │       ├── dto/
│   │   │               │       ├── entity/
│   │   │               │       │   ├── BusMaintenance.java
│   │   │               │       │   └── MaintenanceStatus.java
│   │   │               │       ├── repository/
│   │   │               │       │   └── BusMaintenanceRepository.java
│   │   │               │       └── service/
│   │   │               │           └── BusMaintenanceService.java
│   │   │               │
│   │   │               ├── trip/
│   │   │               │   │
│   │   │               │   ├── controller/
│   │   │               │   │   ├── TripController.java
│   │   │               │   │   └── TripManagementController.java
│   │   │               │   │
│   │   │               │   ├── dto/
│   │   │               │   │   ├── request/
│   │   │               │   │   │   ├── CreateTripRequest.java
│   │   │               │   │   │   ├── UpdateTripRequest.java
│   │   │               │   │   │   ├── TripSearchRequest.java
│   │   │               │   │   │   └── UpdateTripStatusRequest.java
│   │   │               │   │   └── response/
│   │   │               │   │       ├── TripResponse.java
│   │   │               │   │       └── TripSearchResponse.java
│   │   │               │   │
│   │   │               │   ├── entity/
│   │   │               │   │   ├── Trip.java
│   │   │               │   │   └── TripStatus.java
│   │   │               │   │
│   │   │               │   ├── repository/
│   │   │               │   │   └── TripRepository.java
│   │   │               │   │
│   │   │               │   ├── specification/
│   │   │               │   │   └── TripSpecification.java
│   │   │               │   │
│   │   │               │   ├── mapper/
│   │   │               │   │   └── TripMapper.java
│   │   │               │   │
│   │   │               │   ├── service/
│   │   │               │   │   ├── TripService.java
│   │   │               │   │   └── TripSearchService.java
│   │   │               │   │
│   │   │               │   ├── driver/
│   │   │               │   │   ├── entity/
│   │   │               │   │   │   ├── TripDriverAssignment.java
│   │   │               │   │   │   └── DriverRole.java
│   │   │               │   │   ├── repository/
│   │   │               │   │   │   └── TripDriverAssignmentRepository.java
│   │   │               │   │   └── service/
│   │   │               │   │       └── TripDriverAssignmentService.java
│   │   │               │   │
│   │   │               │   ├── fare/
│   │   │               │   │   ├── dto/
│   │   │               │   │   │   ├── request/
│   │   │               │   │   │   │   └── TripFareRequest.java
│   │   │               │   │   │   └── response/
│   │   │               │   │   │       └── TripFareResponse.java
│   │   │               │   │   ├── entity/
│   │   │               │   │   │   └── TripFare.java
│   │   │               │   │   ├── repository/
│   │   │               │   │   │   └── TripFareRepository.java
│   │   │               │   │   └── service/
│   │   │               │   │       ├── TripFareService.java
│   │   │               │   │       └── FareCalculationService.java
│   │   │               │   │
│   │   │               │   └── seat/
│   │   │               │       ├── controller/
│   │   │               │       │   └── TripSeatController.java
│   │   │               │       ├── dto/
│   │   │               │       │   └── response/
│   │   │               │       │       └── TripSeatAvailabilityResponse.java
│   │   │               │       ├── entity/
│   │   │               │       │   └── TripSeat.java
│   │   │               │       ├── repository/
│   │   │               │       │   └── TripSeatRepository.java
│   │   │               │       └── service/
│   │   │               │           ├── TripSeatService.java
│   │   │               │           └── SeatAvailabilityService.java
│   │   │               │
│   │   │               ├── booking/
│   │   │               │   ├── controller/
│   │   │               │   │   └── BookingController.java
│   │   │               │   │
│   │   │               │   ├── dto/
│   │   │               │   │   ├── request/
│   │   │               │   │   │   ├── CreateBookingRequest.java
│   │   │               │   │   │   ├── BookingPassengerRequest.java
│   │   │               │   │   │   └── CancelPassengerRequest.java
│   │   │               │   │   └── response/
│   │   │               │   │       ├── BookingResponse.java
│   │   │               │   │       └── BookingPassengerResponse.java
│   │   │               │   │
│   │   │               │   ├── entity/
│   │   │               │   │   ├── Booking.java
│   │   │               │   │   └── BookingStatus.java
│   │   │               │   │
│   │   │               │   ├── repository/
│   │   │               │   │   └── BookingRepository.java
│   │   │               │   │
│   │   │               │   ├── mapper/
│   │   │               │   │   └── BookingMapper.java
│   │   │               │   │
│   │   │               │   ├── service/
│   │   │               │   │   ├── BookingService.java
│   │   │               │   │   ├── SeatReservationService.java
│   │   │               │   │   ├── BookingExpirationService.java
│   │   │               │   │   └── CancellationService.java
│   │   │               │   │
│   │   │               │   ├── passenger/
│   │   │               │   │   ├── entity/
│   │   │               │   │   │   ├── BookingPassenger.java
│   │   │               │   │   │   └── PassengerStatus.java
│   │   │               │   │   └── repository/
│   │   │               │   │       └── BookingPassengerRepository.java
│   │   │               │   │
│   │   │               │   └── allocation/
│   │   │               │       ├── entity/
│   │   │               │       │   ├── SeatAllocation.java
│   │   │               │       │   └── SeatAllocationStatus.java
│   │   │               │       └── repository/
│   │   │               │           └── SeatAllocationRepository.java
│   │   │               │
│   │   │               ├── payment/
│   │   │               │   ├── controller/
│   │   │               │   │   └── PaymentController.java
│   │   │               │   │
│   │   │               │   ├── dto/
│   │   │               │   │   ├── request/
│   │   │               │   │   │   └── CreatePaymentRequest.java
│   │   │               │   │   └── response/
│   │   │               │   │       └── PaymentResponse.java
│   │   │               │   │
│   │   │               │   ├── entity/
│   │   │               │   │   ├── Payment.java
│   │   │               │   │   ├── PaymentStatus.java
│   │   │               │   │   └── PaymentMethod.java
│   │   │               │   │
│   │   │               │   ├── repository/
│   │   │               │   │   └── PaymentRepository.java
│   │   │               │   │
│   │   │               │   ├── service/
│   │   │               │   │   └── PaymentService.java
│   │   │               │   │
│   │   │               │   ├── gateway/
│   │   │               │   │   ├── PaymentGateway.java
│   │   │               │   │   ├── MockPaymentGateway.java
│   │   │               │   │   ├── PaymentInitiationResult.java
│   │   │               │   │   ├── PaymentVerificationResult.java
│   │   │               │   │   └── RefundGatewayResult.java
│   │   │               │   │
│   │   │               │   └── refund/
│   │   │               │       ├── entity/
│   │   │               │       │   ├── Refund.java
│   │   │               │       │   └── RefundStatus.java
│   │   │               │       ├── repository/
│   │   │               │       │   └── RefundRepository.java
│   │   │               │       └── service/
│   │   │               │           ├── RefundService.java
│   │   │               │           └── CancellationPolicyService.java
│   │   │               │
│   │   │               ├── ticket/
│   │   │               │   ├── controller/
│   │   │               │   │   ├── TicketController.java
│   │   │               │   │   └── TicketCheckInController.java
│   │   │               │   ├── dto/
│   │   │               │   │   └── response/
│   │   │               │   │       └── TicketResponse.java
│   │   │               │   ├── entity/
│   │   │               │   │   ├── Ticket.java
│   │   │               │   │   └── TicketStatus.java
│   │   │               │   ├── repository/
│   │   │               │   │   └── TicketRepository.java
│   │   │               │   └── service/
│   │   │               │       ├── TicketService.java
│   │   │               │       ├── CheckInService.java
│   │   │               │       └── QrTokenService.java
│   │   │               │
│   │   │               ├── reporting/
│   │   │               │   ├── controller/
│   │   │               │   │   └── ReportingController.java
│   │   │               │   ├── dto/
│   │   │               │   │   └── response/
│   │   │               │   │       ├── DashboardSummaryResponse.java
│   │   │               │   │       ├── RevenueReportResponse.java
│   │   │               │   │       └── OccupancyReportResponse.java
│   │   │               │   └── service/
│   │   │               │       └── ReportingService.java
│   │   │               │
│   │   │               ├── notification/
│   │   │               │   ├── entity/
│   │   │               │   │   ├── NotificationLog.java
│   │   │               │   │   ├── NotificationStatus.java
│   │   │               │   │   └── NotificationChannel.java
│   │   │               │   ├── repository/
│   │   │               │   │   └── NotificationLogRepository.java
│   │   │               │   └── service/
│   │   │               │       └── NotificationService.java
│   │   │               │
│   │   │               └── audit/
│   │   │                   ├── entity/
│   │   │                   │   └── AuditLog.java
│   │   │                   ├── repository/
│   │   │                   │   └── AuditLogRepository.java
│   │   │                   └── service/
│   │   │                       └── AuditLogService.java
│   │   │
│   │   └── resources/
│   │       │
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-test.yml
│   │       ├── application-prod.yml
│   │       │
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V1__create_users.sql
│   │       │       ├── V2__create_refresh_tokens.sql
│   │       │       ├── V3__create_stations.sql
│   │       │       ├── V4__create_routes.sql
│   │       │       ├── V5__create_route_stops.sql
│   │       │       ├── V6__create_seat_layouts.sql
│   │       │       ├── V7__create_seats.sql
│   │       │       ├── V8__create_buses.sql
│   │       │       ├── V9__create_drivers.sql
│   │       │       ├── V10__create_trips.sql
│   │       │       ├── V11__create_trip_driver_assignments.sql
│   │       │       ├── V12__create_trip_fares.sql
│   │       │       ├── V13__create_trip_seats.sql
│   │       │       ├── V14__create_bookings.sql
│   │       │       ├── V15__create_booking_passengers.sql
│   │       │       ├── V16__create_seat_allocations.sql
│   │       │       ├── V17__create_payments.sql
│   │       │       ├── V18__create_refunds.sql
│   │       │       ├── V19__create_tickets.sql
│   │       │       └── V20__add_core_indexes.sql
│   │       │
│   │       └── logback-spring.xml
│   │
│   └── test/
│       └── java/
│           └── com/
│               └── yourname/
│                   └── busmanagement/
│                       │
│                       ├── auth/
│                       │   ├── AuthServiceTest.java
│                       │   └── AuthIntegrationTest.java
│                       │
│                       ├── route/
│                       │   ├── RouteServiceTest.java
│                       │   └── RouteIntegrationTest.java
│                       │
│                       ├── fleet/
│                       │   ├── BusServiceTest.java
│                       │   └── DriverServiceTest.java
│                       │
│                       ├── trip/
│                       │   ├── TripServiceTest.java
│                       │   ├── TripSearchIntegrationTest.java
│                       │   └── FareCalculationServiceTest.java
│                       │
│                       ├── booking/
│                       │   ├── SeatAvailabilityServiceTest.java
│                       │   ├── BookingServiceTest.java
│                       │   ├── BookingIntegrationTest.java
│                       │   ├── BookingExpirationTest.java
│                       │   └── ConcurrentBookingIntegrationTest.java
│                       │
│                       ├── payment/
│                       │   ├── PaymentServiceTest.java
│                       │   └── PaymentIdempotencyIntegrationTest.java
│                       │
│                       ├── ticket/
│                       │   ├── TicketServiceTest.java
│                       │   └── CheckInIntegrationTest.java
│                       │
│                       └── support/
│                           ├── PostgreSqlContainerConfig.java
│                           ├── IntegrationTestBase.java
│                           └── TestDataFactory.java
│
├── .dockerignore
├── .gitignore
├── .env.example
├── docker-compose.yml
├── Dockerfile
├── mvnw
├── mvnw.cmd
├── pom.xml
├── README.md
└── LICENSE
```

# Key Structure Rules

## Package by feature

Use:

```text
booking/
payment/
trip/
route/
fleet/
```

instead of one global structure such as:

```text
controller/
service/
repository/
entity/
```

This keeps each business module isolated.

## Entities stay inside their domain

Examples:

```text
Route.java
→ route/entity/

Trip.java
→ trip/entity/

Booking.java
→ booking/entity/

Payment.java
→ payment/entity/
```

## Request and response DTOs stay separate

Use:

```text
dto/
├── request/
└── response/
```

Do not reuse entity objects as API DTOs.

## Enums stay near their entities

Example:

```text
booking/entity/
├── Booking.java
└── BookingStatus.java
```

and:

```text
payment/entity/
├── Payment.java
├── PaymentStatus.java
└── PaymentMethod.java
```

## Repository location

Repository belongs to the module owning the entity.

Example:

```text
trip/seat/entity/TripSeat.java

trip/seat/repository/TripSeatRepository.java
```

## Business logic location

Controllers must remain thin.

Use:

```text
Controller
    ↓
Service
    ↓
Repository
```

Do not put fare calculation, locking, payment processing, or state-transition logic inside controllers.

# Critical Booking Files

The most important implementation area should eventually contain:

```text
booking/
├── controller/
│   └── BookingController.java
│
├── entity/
│   ├── Booking.java
│   └── BookingStatus.java
│
├── passenger/
│   ├── entity/
│   │   ├── BookingPassenger.java
│   │   └── PassengerStatus.java
│   └── repository/
│       └── BookingPassengerRepository.java
│
├── allocation/
│   ├── entity/
│   │   ├── SeatAllocation.java
│   │   └── SeatAllocationStatus.java
│   └── repository/
│       └── SeatAllocationRepository.java
│
├── repository/
│   └── BookingRepository.java
│
└── service/
    ├── BookingService.java
    ├── SeatReservationService.java
    ├── BookingExpirationService.java
    └── CancellationService.java
```

`SeatReservationService` should own:

```text
TripSeat locking

segment overlap validation

seat availability recheck

allocation creation

multi-seat atomic reservation
```

`BookingService` should coordinate the overall use case.

# Critical Trip Seat Files

```text
trip/seat/
├── controller/
│   └── TripSeatController.java
├── entity/
│   └── TripSeat.java
├── repository/
│   └── TripSeatRepository.java
└── service/
    ├── TripSeatService.java
    └── SeatAvailabilityService.java
```

`TripSeatRepository` is where the pessimistic locking query belongs.

Conceptually:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

for requested TripSeat rows.

# Critical Payment Files

```text
payment/
├── controller/
│   └── PaymentController.java
│
├── entity/
│   ├── Payment.java
│   ├── PaymentStatus.java
│   └── PaymentMethod.java
│
├── repository/
│   └── PaymentRepository.java
│
├── service/
│   └── PaymentService.java
│
├── gateway/
│   ├── PaymentGateway.java
│   ├── MockPaymentGateway.java
│   ├── PaymentInitiationResult.java
│   ├── PaymentVerificationResult.java
│   └── RefundGatewayResult.java
│
└── refund/
    ├── entity/
    │   ├── Refund.java
    │   └── RefundStatus.java
    ├── repository/
    │   └── RefundRepository.java
    └── service/
        ├── RefundService.java
        └── CancellationPolicyService.java
```

A real payment provider can later be added as:

```text
payment/gateway/
└── SslCommerzPaymentGateway.java
```

without changing the booking architecture.

# Critical Test Structure

The highest-value test file is:

```text
booking/
└── ConcurrentBookingIntegrationTest.java
```

It should eventually verify:

```text
20 concurrent requests
        ↓
same TripSeat
same segment
        ↓
1 successful booking
19 conflicts
```

Use PostgreSQL Testcontainers for this test.

# Resource Structure

```text
resources/
├── application.yml
├── application-dev.yml
├── application-test.yml
├── application-prod.yml
├── db/
│   └── migration/
└── logback-spring.xml
```

`application.yml` should contain shared configuration.

Development-specific settings:

```text
application-dev.yml
```

Testing:

```text
application-test.yml
```

Production:

```text
application-prod.yml
```

# Root Files

```text
README.md
```

Contains:

- project overview
- features
- stack
- architecture
- ER diagram
- booking flow
- concurrency strategy
- API documentation
- local setup
- Docker instructions
- test instructions

```text
.env.example
```

Example only:

```text
DB_URL=
DB_USERNAME=
DB_PASSWORD=

JWT_PRIVATE_KEY=
JWT_PUBLIC_KEY=

PAYMENT_API_KEY=
```

Never commit `.env`.

```text
docker-compose.yml
```

Runs at minimum:

```text
PostgreSQL
Spring Boot application
```

```text
Dockerfile
```

Use multi-stage build.

```text
.github/workflows/ci.yml
```

Runs:

```text
checkout
↓
setup Java
↓
mvn verify
↓
tests
↓
package
↓
Docker build
```

# Incremental Folder Creation Rule

Do not create the entire tree on the first day.

Create folders as the project grows.

Recommended sequence:

```text
01 common
02 auth + user
03 station
04 route
05 fleet/seatlayout
06 fleet/bus
07 fleet/driver
08 trip
09 trip/driver
10 trip/fare
11 trip/seat
12 booking
13 booking/passenger
14 booking/allocation
15 payment
16 payment/refund
17 ticket
18 reporting
19 audit
20 notification
```

The repository should evolve with the implementation rather than beginning with dozens of empty classes.

# Final Core Package View

For Version 1.0, the essential package hierarchy is:

```text
busmanagement/
├── common
├── auth
├── user
├── station
├── route
├── fleet
│   ├── seatlayout
│   ├── bus
│   └── driver
├── trip
│   ├── driver
│   ├── fare
│   ├── seat
│   └── search
├── booking
│   ├── passenger
│   └── allocation
├── payment
│   ├── gateway
│   └── refund
└── ticket
```

Then extend with:

```text
reporting
audit
notification
fleet/maintenance
```

after the core reservation system is stable.
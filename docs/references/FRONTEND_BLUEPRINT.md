Use a **feature-based React structure** matching the Spring Boot backend modules. Keep API access, auth state, routes, shared UI, and domain features separated.

# React Frontend Structure

## 1. Recommended Frontend Stack

```text
React
TypeScript
Vite
React Router
TanStack Query
Axios
React Hook Form
Zod
Tailwind CSS
shadcn/ui or your own component library
Lucide React
Recharts
date-fns
```

Recommended responsibilities:

```text
React Router
→ routing

TanStack Query
→ server state, caching, refetching

Axios
→ HTTP client

React Hook Form
→ forms

Zod
→ form/schema validation

Context or Zustand
→ limited client-side state

Tailwind CSS
→ styling
```

Do not put API data into a large global Redux store unless there is an actual requirement.

---

# 2. Final Project Structure

```text
bus-management-frontend/
│
├── public/
│   ├── favicon.ico
│   └── images/
│       ├── logo.svg
│       ├── bus-placeholder.png
│       └── empty-state.svg
│
├── src/
│   │
│   ├── app/
│   │   ├── App.tsx
│   │   ├── router.tsx
│   │   ├── providers.tsx
│   │   └── queryClient.ts
│   │
│   ├── api/
│   │   ├── axiosClient.ts
│   │   ├── apiConfig.ts
│   │   └── apiError.ts
│   │
│   ├── assets/
│   │   ├── images/
│   │   ├── icons/
│   │   └── fonts/
│   │
│   ├── components/
│   │   ├── ui/
│   │   │   ├── Button.tsx
│   │   │   ├── Input.tsx
│   │   │   ├── Select.tsx
│   │   │   ├── Modal.tsx
│   │   │   ├── Badge.tsx
│   │   │   ├── Card.tsx
│   │   │   ├── Table.tsx
│   │   │   ├── Pagination.tsx
│   │   │   ├── Spinner.tsx
│   │   │   ├── Skeleton.tsx
│   │   │   └── ConfirmDialog.tsx
│   │   │
│   │   ├── layout/
│   │   │   ├── Navbar.tsx
│   │   │   ├── Sidebar.tsx
│   │   │   ├── Footer.tsx
│   │   │   ├── DashboardLayout.tsx
│   │   │   └── PublicLayout.tsx
│   │   │
│   │   ├── feedback/
│   │   │   ├── LoadingState.tsx
│   │   │   ├── ErrorState.tsx
│   │   │   ├── EmptyState.tsx
│   │   │   └── NotFoundState.tsx
│   │   │
│   │   └── navigation/
│   │       ├── ProtectedRoute.tsx
│   │       ├── RoleRoute.tsx
│   │       └── Breadcrumbs.tsx
│   │
│   ├── features/
│   │   │
│   │   ├── auth/
│   │   │   ├── api/
│   │   │   │   └── authApi.ts
│   │   │   ├── components/
│   │   │   │   ├── LoginForm.tsx
│   │   │   │   └── RegisterForm.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useLogin.ts
│   │   │   │   ├── useRegister.ts
│   │   │   │   └── useLogout.ts
│   │   │   ├── pages/
│   │   │   │   ├── LoginPage.tsx
│   │   │   │   └── RegisterPage.tsx
│   │   │   ├── schemas/
│   │   │   │   ├── loginSchema.ts
│   │   │   │   └── registerSchema.ts
│   │   │   ├── store/
│   │   │   │   └── authStore.ts
│   │   │   └── types/
│   │   │       └── auth.types.ts
│   │   │
│   │   ├── user/
│   │   │   ├── api/
│   │   │   │   └── userApi.ts
│   │   │   ├── components/
│   │   │   │   ├── ProfileForm.tsx
│   │   │   │   └── ChangePasswordForm.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useProfile.ts
│   │   │   │   └── useUpdateProfile.ts
│   │   │   ├── pages/
│   │   │   │   ├── ProfilePage.tsx
│   │   │   │   └── UserManagementPage.tsx
│   │   │   └── types/
│   │   │       └── user.types.ts
│   │   │
│   │   ├── station/
│   │   │   ├── api/
│   │   │   │   └── stationApi.ts
│   │   │   ├── components/
│   │   │   │   ├── StationForm.tsx
│   │   │   │   └── StationTable.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useStations.ts
│   │   │   │   └── useStationMutations.ts
│   │   │   ├── pages/
│   │   │   │   └── StationManagementPage.tsx
│   │   │   └── types/
│   │   │       └── station.types.ts
│   │   │
│   │   ├── route/
│   │   │   ├── api/
│   │   │   │   └── routeApi.ts
│   │   │   ├── components/
│   │   │   │   ├── RouteForm.tsx
│   │   │   │   ├── RouteStopEditor.tsx
│   │   │   │   ├── RouteTable.tsx
│   │   │   │   └── RouteDetails.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useRoutes.ts
│   │   │   │   └── useRouteMutations.ts
│   │   │   ├── pages/
│   │   │   │   ├── RouteManagementPage.tsx
│   │   │   │   └── RouteDetailsPage.tsx
│   │   │   └── types/
│   │   │       └── route.types.ts
│   │   │
│   │   ├── fleet/
│   │   │   │
│   │   │   ├── seat-layout/
│   │   │   │   ├── api/
│   │   │   │   │   └── seatLayoutApi.ts
│   │   │   │   ├── components/
│   │   │   │   │   ├── SeatLayoutForm.tsx
│   │   │   │   │   ├── SeatLayoutDesigner.tsx
│   │   │   │   │   └── SeatGrid.tsx
│   │   │   │   ├── pages/
│   │   │   │   │   └── SeatLayoutPage.tsx
│   │   │   │   └── types/
│   │   │   │       └── seatLayout.types.ts
│   │   │   │
│   │   │   ├── bus/
│   │   │   │   ├── api/
│   │   │   │   │   └── busApi.ts
│   │   │   │   ├── components/
│   │   │   │   │   ├── BusForm.tsx
│   │   │   │   │   ├── BusTable.tsx
│   │   │   │   │   └── BusStatusBadge.tsx
│   │   │   │   ├── pages/
│   │   │   │   │   └── BusManagementPage.tsx
│   │   │   │   └── types/
│   │   │   │       └── bus.types.ts
│   │   │   │
│   │   │   └── driver/
│   │   │       ├── api/
│   │   │       │   └── driverApi.ts
│   │   │       ├── components/
│   │   │       │   ├── DriverForm.tsx
│   │   │       │   └── DriverTable.tsx
│   │   │       ├── pages/
│   │   │       │   └── DriverManagementPage.tsx
│   │   │       └── types/
│   │   │           └── driver.types.ts
│   │   │
│   │   ├── trip/
│   │   │   ├── api/
│   │   │   │   └── tripApi.ts
│   │   │   ├── components/
│   │   │   │   ├── TripForm.tsx
│   │   │   │   ├── TripCard.tsx
│   │   │   │   ├── TripTable.tsx
│   │   │   │   ├── TripSearchForm.tsx
│   │   │   │   ├── TripSearchResult.tsx
│   │   │   │   ├── DriverAssignmentForm.tsx
│   │   │   │   └── TripFareEditor.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useTripSearch.ts
│   │   │   │   ├── useTrip.ts
│   │   │   │   └── useTripMutations.ts
│   │   │   ├── pages/
│   │   │   │   ├── TripSearchPage.tsx
│   │   │   │   ├── TripDetailsPage.tsx
│   │   │   │   └── TripManagementPage.tsx
│   │   │   └── types/
│   │   │       └── trip.types.ts
│   │   │
│   │   ├── seat-availability/
│   │   │   ├── api/
│   │   │   │   └── seatAvailabilityApi.ts
│   │   │   ├── components/
│   │   │   │   ├── SeatMap.tsx
│   │   │   │   ├── SeatButton.tsx
│   │   │   │   ├── SeatLegend.tsx
│   │   │   │   └── SelectedSeatsSummary.tsx
│   │   │   ├── hooks/
│   │   │   │   └── useSeatAvailability.ts
│   │   │   └── types/
│   │   │       └── seatAvailability.types.ts
│   │   │
│   │   ├── booking/
│   │   │   ├── api/
│   │   │   │   └── bookingApi.ts
│   │   │   ├── components/
│   │   │   │   ├── PassengerForm.tsx
│   │   │   │   ├── PassengerList.tsx
│   │   │   │   ├── BookingSummary.tsx
│   │   │   │   ├── BookingStatusBadge.tsx
│   │   │   │   ├── BookingCountdown.tsx
│   │   │   │   └── CancellationDialog.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useCreateBooking.ts
│   │   │   │   ├── useBooking.ts
│   │   │   │   ├── useBookings.ts
│   │   │   │   └── useCancelBooking.ts
│   │   │   ├── pages/
│   │   │   │   ├── BookingCheckoutPage.tsx
│   │   │   │   ├── BookingDetailsPage.tsx
│   │   │   │   └── MyBookingsPage.tsx
│   │   │   ├── schemas/
│   │   │   │   └── passengerSchema.ts
│   │   │   └── types/
│   │   │       └── booking.types.ts
│   │   │
│   │   ├── payment/
│   │   │   ├── api/
│   │   │   │   └── paymentApi.ts
│   │   │   ├── components/
│   │   │   │   ├── PaymentMethodSelector.tsx
│   │   │   │   ├── PaymentSummary.tsx
│   │   │   │   └── PaymentStatus.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useCreatePayment.ts
│   │   │   │   └── usePayment.ts
│   │   │   ├── pages/
│   │   │   │   ├── PaymentPage.tsx
│   │   │   │   ├── PaymentSuccessPage.tsx
│   │   │   │   └── PaymentFailurePage.tsx
│   │   │   └── types/
│   │   │       └── payment.types.ts
│   │   │
│   │   ├── ticket/
│   │   │   ├── api/
│   │   │   │   └── ticketApi.ts
│   │   │   ├── components/
│   │   │   │   ├── TicketCard.tsx
│   │   │   │   ├── TicketQrCode.tsx
│   │   │   │   └── TicketStatusBadge.tsx
│   │   │   ├── hooks/
│   │   │   │   ├── useTicket.ts
│   │   │   │   └── useCheckInTicket.ts
│   │   │   ├── pages/
│   │   │   │   ├── TicketPage.tsx
│   │   │   │   └── TicketCheckInPage.tsx
│   │   │   └── types/
│   │   │       └── ticket.types.ts
│   │   │
│   │   ├── dashboard/
│   │   │   ├── api/
│   │   │   │   └── dashboardApi.ts
│   │   │   ├── components/
│   │   │   │   ├── SummaryCard.tsx
│   │   │   │   ├── RevenueChart.tsx
│   │   │   │   ├── OccupancyChart.tsx
│   │   │   │   └── RecentBookingsTable.tsx
│   │   │   ├── pages/
│   │   │   │   └── DashboardPage.tsx
│   │   │   └── types/
│   │   │       └── dashboard.types.ts
│   │   │
│   │   └── reports/
│   │       ├── api/
│   │       │   └── reportApi.ts
│   │       ├── components/
│   │       │   ├── RevenueReport.tsx
│   │       │   ├── OccupancyReport.tsx
│   │       │   └── ReportFilters.tsx
│   │       └── pages/
│   │           └── ReportsPage.tsx
│   │
│   ├── hooks/
│   │   ├── useDebounce.ts
│   │   ├── usePagination.ts
│   │   └── useDocumentTitle.ts
│   │
│   ├── lib/
│   │   ├── queryKeys.ts
│   │   ├── permissions.ts
│   │   └── storage.ts
│   │
│   ├── types/
│   │   ├── api.types.ts
│   │   ├── pagination.types.ts
│   │   └── common.types.ts
│   │
│   ├── utils/
│   │   ├── currency.ts
│   │   ├── dateTime.ts
│   │   ├── formatters.ts
│   │   └── errorUtils.ts
│   │
│   ├── styles/
│   │   └── globals.css
│   │
│   ├── main.tsx
│   └── vite-env.d.ts
│
├── .env.example
├── .gitignore
├── eslint.config.js
├── index.html
├── package.json
├── tsconfig.json
├── tsconfig.app.json
├── tsconfig.node.json
├── vite.config.ts
└── README.md
```

# 3. High-Level Frontend Architecture

```text
React Application
      │
      ├── App Router
      │
      ├── Authentication
      │
      ├── Public Passenger UI
      │
      └── Staff/Admin UI
      │
      ▼
Feature Modules
      │
      ├── Routes
      ├── Trips
      ├── Seats
      ├── Bookings
      ├── Payments
      ├── Tickets
      └── Administration
      │
      ▼
API Layer
      │
      ▼
Spring Boot REST API
```

# 4. Main Application Routes

Recommended route structure:

```text
/
│
├── /
│   └── Home / Trip Search
│
├── /login
├── /register
│
├── /trips
│   └── search results
│
├── /trips/:tripId
│   └── trip details + seat selection
│
├── /booking/:bookingId
│   └── booking details
│
├── /checkout/:tripId
│   └── passenger information + booking
│
├── /payment/:bookingId
│
├── /payment/success
├── /payment/failure
│
├── /tickets/:ticketNumber
│
├── /my-bookings
├── /profile
│
├── /counter/*
│   ├── bookings
│   └── check-in
│
├── /management/*
│   ├── trips
│   ├── drivers
│   └── fares
│
└── /admin/*
    ├── dashboard
    ├── users
    ├── stations
    ├── routes
    ├── seat-layouts
    ├── buses
    ├── drivers
    ├── trips
    └── reports
```

# 5. Router Structure

Conceptually:

```text
Public Routes
├── Home
├── Login
├── Register
├── Trip Search
└── Trip Details

Authenticated Routes
├── Profile
├── My Bookings
├── Booking Details
├── Payment
└── Ticket

Counter Staff Routes
├── Counter Booking
└── Ticket Check-In

Manager Routes
├── Driver Management
├── Trip Management
└── Fare Management

Admin Routes
├── Dashboard
├── User Management
├── Station Management
├── Route Management
├── Seat Layout Management
├── Bus Management
├── Driver Management
├── Trip Management
└── Reports
```

# 6. Layout Structure

Use separate layouts.

```text
PublicLayout
│
├── Navbar
├── Page Content
└── Footer
```

For authenticated back-office users:

```text
DashboardLayout
│
├── Sidebar
├── Topbar
└── Page Content
```

Example:

```text
ADMIN DASHBOARD

┌──────────────────────────────────────────┐
│ Topbar                                   │
├───────────────┬──────────────────────────┤
│               │                          │
│ Sidebar       │ Page Content             │
│               │                          │
│ Dashboard     │                          │
│ Stations      │                          │
│ Routes        │                          │
│ Buses         │                          │
│ Drivers       │                          │
│ Trips         │                          │
│ Reports       │                          │
│               │                          │
└───────────────┴──────────────────────────┘
```

# 7. API Client Structure

Central Axios client:

```text
src/api/
├── axiosClient.ts
├── apiConfig.ts
└── apiError.ts
```

`axiosClient.ts` responsibilities:

```text
base URL

request headers

access token

401 handling

token refresh

standard error conversion
```

Feature API files remain separate.

Example:

```text
route/api/routeApi.ts

trip/api/tripApi.ts

booking/api/bookingApi.ts

payment/api/paymentApi.ts
```

Do not put every API endpoint into one:

```text
apiService.ts
```

file.

# 8. API Flow

Example Trip Search:

```text
TripSearchPage
      ↓
TripSearchForm
      ↓
useTripSearch()
      ↓
tripApi.searchTrips()
      ↓
Axios
      ↓
Spring Boot
```

Booking:

```text
BookingCheckoutPage
      ↓
PassengerForm
      ↓
useCreateBooking()
      ↓
bookingApi.createBooking()
      ↓
Spring Boot Booking API
```

# 9. TanStack Query Structure

Use TanStack Query for backend data.

Examples:

```text
Stations

Routes

Trips

Trip Search

Seat Availability

Bookings

Payments

Tickets

Dashboard Reports
```

Do not manually duplicate backend data into a global store.

Example query keys:

```text
["stations"]

["routes"]

["route", routeId]

["trip", tripId]

["trip-search", searchParameters]

["trip-seats", tripId, fromStopId, toStopId]

["booking", bookingId]

["bookings", userId]

["ticket", ticketNumber]
```

Centralize key helpers:

```text
src/lib/queryKeys.ts
```

# 10. Authentication State

Authentication store should contain only client authentication state.

Example:

```text
accessToken

currentUser

isAuthenticated
```

Avoid storing:

```text
stations
routes
trips
bookings
payments
```

inside auth/global state.

Those belong in TanStack Query.

# 11. JWT Storage Strategy

Preferred model:

```text
Access Token
→ memory

Refresh Token
→ secure HttpOnly cookie
```

if backend architecture supports cookies.

If the backend returns refresh tokens in JSON during the initial learning version, handle them carefully and avoid unnecessary persistence.

The frontend must never decode JWT claims and treat them as authoritative authorization.

Backend authorization remains authoritative.

# 12. ProtectedRoute

Use:

```text
ProtectedRoute
```

for authenticated pages.

Concept:

```text
Authenticated?
    │
    ├── No → /login
    │
    └── Yes → Render Page
```

# 13. RoleRoute

Use:

```text
RoleRoute
```

for administrative UI.

Example:

```text
/admin/*
→ ADMIN
```

```text
/management/*
→ ADMIN or MANAGER
```

```text
/counter/*
→ ADMIN or COUNTER_STAFF
```

Frontend role checks only improve UX.

Backend Spring Security must enforce actual authorization.

# 14. Trip Search UI

Recommended page structure:

```text
TripSearchPage

├── Search Form
│   ├── From Station
│   ├── To Station
│   ├── Journey Date
│   └── Search Button
│
└── Search Results
    ├── Trip Card
    ├── Trip Card
    └── Trip Card
```

Trip card:

```text
Dhaka → Mymensingh

Departure: 08:00
Arrival: 11:30

Bus: AC
Coach: 101

Available Seats: 17

Fare From: ৳450

[View Seats]
```

# 15. Seat Selection UI

Critical component:

```text
SeatMap.tsx
```

Example:

```text
Driver

A1  A2       A3  A4
B1  B2       B3  B4
C1  C2       C3  C4
D1  D2       D3  D4
```

Seat states:

```text
AVAILABLE

SELECTED

UNAVAILABLE

HELD
```

Do not allow selection of unavailable seats.

However, frontend availability is never final.

The backend must recheck and lock seats during booking.

# 16. Seat Selection Flow

```text
Trip selected
      ↓
User selects From Stop
      ↓
User selects To Stop
      ↓
GET seat availability
      ↓
Render SeatMap
      ↓
User selects A1/A2
      ↓
Proceed to Booking
```

# 17. Booking Checkout Page

Structure:

```text
BookingCheckoutPage
│
├── Trip Summary
│
├── Route Segment
│
├── Selected Seats
│
├── Passenger Forms
│
├── Fare Summary
└── Create Booking Button
```

For:

```text
A1
A2
```

render two passenger forms.

Example:

```text
Seat A1
Name
Phone
Age
Gender

Seat A2
Name
Phone
Age
Gender
```

# 18. Booking Countdown

Once booking is successfully created:

```text
Booking.status
=
PENDING_PAYMENT
```

Backend returns:

```text
expiresAt
```

Frontend:

```text
BookingCountdown.tsx
```

renders:

```text
Complete payment within

08:42
```

Do not independently decide expiration.

Backend `expiresAt` is authoritative.

When timer reaches zero:

```text
refetch Booking
```

Backend determines:

```text
EXPIRED
```

# 19. Payment Frontend

```text
PaymentPage

├── Booking Summary
├── Amount
├── Payment Method
└── Pay Button
```

Flow:

```text
Create Payment
      ↓
Backend Payment API
      ↓
Payment Gateway / Mock Gateway
      ↓
Success or Failure
```

Frontend should never mark a Booking confirmed by itself.

Only backend confirmation matters.

# 20. Ticket UI

`TicketCard.tsx`:

```text
BUS TICKET

Ticket: TKT-XXXX

Passenger:
Rahim Ahmed

Route:
Dhaka → Mymensingh

Boarding:
Dhaka

Dropping:
Mymensingh

Seat:
A1

Coach:
101

Departure:
30 Sep 2026 — 08:00

[QR CODE]

Status:
VALID
```

# 21. Admin Dashboard

Recommended:

```text
DashboardPage

├── Total Bookings
├── Today's Revenue
├── Upcoming Trips
├── Active Buses
├── Occupancy Rate
│
├── Revenue Chart
├── Occupancy Chart
└── Recent Bookings
```

# 22. Station Management Page

```text
StationManagementPage
│
├── Add Station
├── Search/Filter
│
└── StationTable
    ├── Code
    ├── Name
    ├── City
    ├── Status
    └── Actions
```

# 23. Route Management Page

```text
RouteManagementPage
│
├── Create Route
│
└── RouteTable
```

Route form:

```text
Route Code

Route Name

Origin

Destination

Route Stops
 ├── Stop 1
 ├── Stop 2
 ├── Stop 3
 └── Stop 4
```

`RouteStopEditor.tsx` should allow ordered stop management.

# 24. Seat Layout Designer

One of the strongest UI features.

```text
SeatLayoutDesigner.tsx
```

Possible editor:

```text
Row 1

[A1] [A2]   aisle   [A3] [A4]

Row 2

[B1] [B2]   aisle   [B3] [B4]
```

Admin can define:

```text
seat number

seat type

row

column

deck
```

# 25. Trip Management

```text
TripManagementPage
│
├── Create Trip
├── Search/Filter Trips
└── TripTable
```

Create Trip form:

```text
Route

Bus

Departure

Arrival

Primary Driver

Secondary Driver

Fare Configuration
```

# 26. Booking Management

Admin/counter page can contain:

```text
Booking Number

Passenger

Trip

Route

Seats

Amount

Booking Status

Payment Status

Created At
```

# 27. Reporting UI

```text
ReportsPage
│
├── Filters
│   ├── Date From
│   ├── Date To
│   ├── Route
│   └── Trip
│
├── Revenue
├── Occupancy
├── Booking Count
└── Cancellation Metrics
```

# 28. Shared UI Components

Keep genuinely reusable UI in:

```text
components/ui/
```

Examples:

```text
Button

Input

Modal

Table

Badge

Pagination

Spinner

Skeleton

ConfirmDialog
```

Do not put feature-specific components here.

Wrong:

```text
components/ui/BookingSummary.tsx
```

Correct:

```text
features/booking/components/BookingSummary.tsx
```

# 29. Shared Utility Functions

```text
utils/
├── currency.ts
├── dateTime.ts
├── formatters.ts
└── errorUtils.ts
```

Examples:

```ts
formatCurrency(450)
→ "৳450.00"
```

```ts
formatTripDate(...)
```

```ts
getApiErrorMessage(...)
```

# 30. TypeScript Types

Keep feature types near their feature.

Example:

```text
features/booking/types/booking.types.ts
```

Could define:

```text
Booking

BookingPassenger

BookingStatus

CreateBookingPayload

BookingResponse
```

Global shared types belong in:

```text
src/types/
```

# 31. Validation

Use Zod + React Hook Form.

Example form chain:

```text
LoginForm
      ↓
loginSchema
      ↓
React Hook Form
      ↓
Submit Valid Data
      ↓
useLogin
```

Frontend validation improves UX.

Backend validation remains mandatory.

# 32. Environment Variables

`.env.example`

```text
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

Potential later variables:

```text
VITE_APP_NAME=Bus Management System
VITE_PAYMENT_MODE=mock
```

Do not store backend secrets in frontend environment variables.

Anything bundled into React should be considered public.

# 33. Vite Configuration

Root:

```text
vite.config.ts
```

Possible local proxy:

```text
/api
↓
http://localhost:8080
```

This can simplify development and CORS handling.

# 34. Error Handling

Three levels:

## Form Error

```text
Email is required
```

## API Business Error

```text
Seat A1 is no longer available.
```

## Global Error

```text
Unable to load this page.
```

Create reusable:

```text
ErrorState.tsx
```

and centralized API error extraction.

# 35. Loading UX

Do not use one giant global spinner.

Use local states.

Examples:

```text
Trip search
→ TripCard skeletons

Seat availability
→ SeatMap skeleton

Booking
→ Button loading state

Dashboard
→ card/chart skeletons
```

# 36. 409 Seat Conflict Handling

This is critical.

User sees:

```text
A1 AVAILABLE
```

selects it.

Another user books A1 before the first user submits.

Backend returns:

```text
409 SEAT_NOT_AVAILABLE
```

Frontend must:

```text
Show conflict message

Refetch seat availability

Remove unavailable selected seats

Allow user to select another seat
```

Do not treat this as an unexpected generic error.

# 37. Query Invalidations

After successful Booking:

```text
invalidate/refetch:

trip seat availability
user bookings
booking details
```

After cancellation:

```text
invalidate:

booking details
my bookings
seat availability
```

After admin updates Trip:

```text
invalidate:

trip
trip lists
trip search where relevant
```

# 38. Recommended Query Key Structure

```ts
stations.all

routes.all
routes.detail(routeId)

trips.all
trips.detail(tripId)
trips.search(filters)

tripSeats.availability(
  tripId,
  fromStopId,
  toStopId
)

bookings.all
bookings.detail(bookingId)

payments.detail(paymentId)

tickets.detail(ticketNumber)
```

Keep this centralized.

# 39. Public Pages

Version 1:

```text
HomePage

LoginPage

RegisterPage

TripSearchPage

TripDetailsPage

BookingCheckoutPage

PaymentPage

PaymentSuccessPage

PaymentFailurePage

MyBookingsPage

BookingDetailsPage

TicketPage

ProfilePage
```

# 40. Administrative Pages

```text
DashboardPage

UserManagementPage

StationManagementPage

RouteManagementPage

SeatLayoutPage

BusManagementPage

DriverManagementPage

TripManagementPage

ReportsPage
```

# 41. Counter Staff Pages

```text
CounterBookingPage

TicketCheckInPage
```

A counter booking can reuse:

```text
TripSearchForm

SeatMap

PassengerForm

BookingSummary
```

rather than duplicating the passenger booking implementation.

# 42. Responsive Design

Passenger-facing pages should work well on:

```text
mobile
tablet
desktop
```

Admin pages can prioritize desktop while remaining usable on tablets.

Particularly important mobile screens:

```text
Trip Search

Seat Selection

Booking Checkout

Payment

Ticket
```

# 43. Accessibility

At minimum:

```text
semantic buttons

keyboard-accessible forms

labels for inputs

visible focus states

ARIA labels where required

sufficient contrast

accessible modal behavior
```

Seat availability must not rely only on color.

Example:

```text
A1
Available
```

rather than only green.

# 44. Frontend Testing Structure

Add later:

```text
src/
└── ...

tests/
or
co-located *.test.tsx
```

Recommended tools:

```text
Vitest

React Testing Library

MSW
```

Test:

```text
LoginForm

TripSearchForm

SeatMap

PassengerForm

BookingSummary

RoleRoute

409 seat conflict handling
```

# 45. End-to-End Testing

Optional but useful:

```text
Playwright
```

Important E2E:

```text
Login
↓
Search Trip
↓
Select Seat
↓
Create Booking
↓
Mock Payment
↓
See Ticket
```

# 46. Incremental Frontend Build Order

Build the React application in the same order as the backend.

```text
01 Project setup
02 Shared UI
03 Router/layout
04 Auth
05 Stations
06 Routes
07 Seat layouts
08 Buses
09 Drivers
10 Trips
11 Trip search
12 Seat availability
13 Booking
14 Booking countdown
15 Payment
16 Ticket
17 Cancellation
18 Counter check-in
19 Admin dashboard
20 Reports
```

# 47. Increment 0 — React Foundation

Create:

```text
Vite + React + TypeScript
```

Configure:

```text
React Router

Axios

TanStack Query

Tailwind

React Hook Form

Zod
```

Create:

```text
app/
api/
components/
features/
hooks/
lib/
types/
utils/
```

# 48. Increment 1 — Layout and Routing

Create:

```text
PublicLayout

DashboardLayout

Navbar

Sidebar

ProtectedRoute

RoleRoute
```

Set up routes before building domain pages.

# 49. Increment 2 — Authentication

Create:

```text
LoginPage

RegisterPage

authApi

authStore

useLogin

useLogout
```

Connect to Spring Boot authentication.

# 50. Increment 3 — Route Network Admin

Create UI for:

```text
Stations
Routes
RouteStops
```

This allows backend data setup directly through frontend.

# 51. Increment 4 — Fleet

Build:

```text
Seat Layout Designer

Bus Management

Driver Management
```

# 52. Increment 5 — Trip Management

Build:

```text
Create Trip

Assign Drivers

Configure Fares

View Trip
```

# 53. Increment 6 — Passenger Trip Search

Create polished public search.

```text
From

To

Date

Search
```

Render results as TripCards.

# 54. Increment 7 — Seat Selection

Build:

```text
SeatMap

SeatButton

SeatLegend

SelectedSeatsSummary
```

Connect to segment-specific availability API.

# 55. Increment 8 — Booking Checkout

Build:

```text
Passenger forms

Selected seats

Fare summary

Booking creation
```

Handle backend conflicts correctly.

# 56. Increment 9 — Booking Hold Timer

Display backend-provided expiration.

When expired:

```text
disable payment

refetch Booking

navigate appropriately
```

# 57. Increment 10 — Payment

Initially integrate only backend MockPaymentGateway flow.

Do not create fake payment state solely in React.

# 58. Increment 11 — Ticket

Build:

```text
TicketPage

TicketCard

QR display
```

# 59. Increment 12 — Cancellation

Implement:

```text
full cancellation

partial passenger cancellation
```

Then invalidate relevant queries.

# 60. Increment 13 — Counter Check-In

Build:

```text
Ticket number input

QR scanner later

Verify

Check-In
```

Start with ticket number entry. Camera QR scanning can be added later.

# 61. Increment 14 — Dashboard

Only after transactional functionality is complete.

Add:

```text
summary cards

revenue graph

occupancy graph

recent bookings
```

# 62. Final Frontend-to-Backend Feature Mapping

| React Feature | Spring Boot Module |
|---|---|
| `features/auth` | `auth` |
| `features/user` | `user` |
| `features/station` | `station` |
| `features/route` | `route` |
| `features/fleet/seat-layout` | `fleet/seatlayout` |
| `features/fleet/bus` | `fleet/bus` |
| `features/fleet/driver` | `fleet/driver` |
| `features/trip` | `trip` |
| `features/seat-availability` | `trip/seat` |
| `features/booking` | `booking` |
| `features/payment` | `payment` |
| `features/ticket` | `ticket` |
| `features/dashboard` | `reporting` |
| `features/reports` | `reporting` |

# 63. Final Full-Stack Repository Option

If backend and frontend are stored together:

```text
bus-management-system/
│
├── backend/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│
├── frontend/
│   ├── package.json
│   ├── vite.config.ts
│   └── src/
│
├── docs/
│
├── docker-compose.yml
│
├── .env.example
│
└── README.md
```

This is the cleaner choice for a single resume project.

Docker Compose can eventually run:

```text
frontend
backend
postgres
```

Final architecture:

```text
React
  │
  │ REST
  ▼
Spring Boot
  │
  ▼
PostgreSQL
```

The React application should mirror the backend's business domains without duplicating backend business rules. React owns presentation, forms, routing, client-side UX state, caching, and interaction. Spring Boot remains authoritative for authentication, authorization, fare calculation, seat availability validation, locking, booking states, payments, refunds, and ticket validity.
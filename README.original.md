# Transport Management System

Full-stack monorepo for managing university bus schedules, seat inventory, and student bookings. The project pairs an Express/MongoDB API with two React front-ends (an admin dashboard and a student-facing booking site) to streamline transport operations end to end.

## Features

- Admin dashboard (React + Tailwind) for managing locations, departure times, trips, and seat allocations.
- Student portal for searching routes, selecting seats visually, and confirming bookings in real time.
- Dynamic seat collections per trip with robust validation to avoid overbooking.
- Email confirmation workflow with PDF ticket generation (NodeMailer + PDFKit) ready for integration.
- Modular service layer and REST API designed for extension (user management, analytics, etc.).

## Repository Structure

```
.
├── backend/                # Express API, Mongoose models, booking utilities
├── frontend/
│   ├── admin-app/          # Admin dashboard (Vite + React + Tailwind)
│   └── user-app/           # Student booking SPA (Vite + React + Tailwind)
└── README.md
```

## Tech Stack

- Backend: Node.js, Express.js, Mongoose, Nodemailer, PDFKit, dotenv, CORS, Body Parser
- Database: MongoDB (Atlas or self-hosted)
- Frontend (Admin): React 18, Vite, Tailwind CSS, ApexCharts, React Router
- Frontend (User): React 18, Vite, Tailwind CSS, Headless UI, Lucide Icons, Framer Motion
- Tooling: TypeScript (admin services/components), ESLint, Prettier, Nodemon

## Prerequisites

- Node.js 18+ and npm 9+
- Running MongoDB instance (local or remote)
- Gmail (or SMTP) account with app password for email delivery
- Recommended: pnpm or yarn if you prefer alternative package managers

## Environment Configuration

Create a `.env` file inside `backend/` with the variables below. Update values to match your infrastructure.

```bash
PORT=8000
MONGO_URL=mongodb://localhost:27017/transport_management
EMAIL_USER=your.email@example.com
EMAIL_APP_PASSWORD=your-app-password
```

> The frontend services assume the API is exposed on `http://localhost:8000/api/admin`. If you change `PORT` or host, adjust the `BASE_URL` constants inside the service files under `frontend/admin-app/src/services` and `frontend/user-app/src/services` accordingly.

## Getting Started

### 1. Clone and install dependencies

```bash
git clone https://github.com/sh-sho1kat/Transport_Management_System.git
cd Transport_Management_System

# Backend
cd backend
npm install

# Admin dashboard
cd ../frontend/admin-app
npm install

# Student portal
cd ../user-app
npm install
```

### 2. Run the backend API

```bash
cd backend
npm run start
```

The server uses Nodemon for auto-restarts. Once MongoDB is reachable you should see `Server is running on port : 8000`.

### 3. Run the admin dashboard

```bash
cd frontend/admin-app
npm run dev
```

Vite defaults to `http://localhost:5173`. Use the dashboard to create locations, departure times, and trips. Creating a trip automatically provisions 40 seats for that trip.

### 4. Run the student booking app

```bash
cd frontend/user-app
npm run dev
```

The booking SPA runs on `http://localhost:5174` (or the next free Vite port). Students can search routes, pick seats, and submit bookings. Bookings trigger seat status updates and are ready to be paired with the email/PDF workflow.

## API Overview

Base URL: `http://localhost:8000/api/admin`

| Area     | Method | Endpoint                            | Description                                       |
| -------- | ------ | ----------------------------------- | ------------------------------------------------- |
| Time     | POST   | `/create-time`                      | Create a departure time slot                      |
|          | GET    | `/get-time`                         | List all time slots                               |
|          | GET    | `/get-time/:id`                     | Fetch a single time slot                          |
|          | PUT    | `/update-time/:id`                  | Update a time slot                                |
|          | DELETE | `/delete-time/:id`                  | Remove a time slot                                |
| Location | POST   | `/create-location`                  | Create a location                                 |
|          | GET    | `/get-location`                     | List locations                                    |
|          | GET    | `/get-location/:id`                 | Fetch location details                            |
|          | PUT    | `/update-location/:id`              | Update a location                                 |
|          | DELETE | `/delete-location/:id`              | Delete a location                                 |
| Trip     | POST   | `/create-trip`                      | Create a trip (generally triggered from admin UI) |
|          | GET    | `/get-trip`                         | List all trips                                    |
|          | GET    | `/get-trip/:id`                     | Fetch a trip by Mongo `_id`                       |
|          | GET    | `/get-trip/:busID`                  | Fetch trips by bus identifier                     |
|          | PUT    | `/update-trip/:id`                  | Update trip details                               |
|          | DELETE | `/delete-trip/:id`                  | Remove a trip                                     |
| Seats    | POST   | `/seats/create/:tripId`             | Initialize 40 seats for a trip                    |
|          | GET    | `/seats/:tripId`                    | List seat map for a trip                          |
|          | GET    | `/seats/:tripId/booked`             | Retrieve booked seats                             |
|          | GET    | `/seats/:tripId/student/:studentId` | Retrieve bookings for a student                   |
|          | GET    | `/seats/:tripId/:seatNo`            | Inspect a specific seat                           |
|          | PUT    | `/seats/:tripId/:seatNo`            | Update a single seat                              |
|          | PUT    | `/seats/:tripId`                    | Bulk-update multiple seats                        |

> User-related endpoints (`/api/user/...`) exist but are not currently wired into the default routing chain.

## Email Confirmation Workflow

- Booking confirmations use Nodemailer with the Gmail SMTP configuration supplied in `.env`.
- PDFKit renders a ticket PDF (`EmailPdfService`) containing student, seat, and trip metadata.
- Temporary PDFs are generated under `backend/temp/` and deleted after dispatch.
- Ensure Gmail two-factor authentication is enabled and generate an App Password for `EMAIL_APP_PASSWORD`.

## Available Scripts

- `npm run start` (backend): start API with Nodemon.
- `npm run dev` (frontends): start Vite development servers with hot reload.
- `npm run build` (frontends): create production bundles.
- `npm run preview` (frontends): preview build output locally.

## Data Seeding Tips

1. Use the admin dashboard to add base locations and departure times before attempting to create trips.
2. After creating a trip, seats are generated automatically and appear instantly in both admin and student apps.
3. If you need to reset seat data, remove the trip via the admin dashboard or expose the existing `deleteAllSeats` controller as a helper endpoint.

## Troubleshooting

- **CORS or network errors:** Confirm backend and frontends are using matching hosts/ports. Update the `BASE_URL` constants if necessary.
- **Mongo connection failure:** Verify `MONGO_URL`, ensure MongoDB is running, and allow network access for Atlas clusters.
- **Email failures:** Check transporter credentials and Gmail security settings. SMTP ports other than 587 may require TLS adjustments.
- **Port conflicts:** Change the `PORT` in `.env` or pass `--port` to `npm run dev` for the Vite apps.

## Roadmap Ideas

- Integrate role-based authentication and secure admin endpoints.
- Expose public endpoints for trip discovery without admin prefix.
- Persist booking history and expose it in analytics views.
- Automate seat reset when trips expire.


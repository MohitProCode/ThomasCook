# Hotel Booking Portal

Search hotels by destination and dates, view detail with live availability, get a price quote, book with an instant confirmation reference, look bookings up by reference + email, and cancel to release the room. Smart alternatives suggest the nearest available dates or the cheapest nearby hotel whenever dates are sold out.

Stack: Spring Boot 3 + Java 17, React 18 + Vite + Node 18+, MySQL 8, Docker Compose for the database.

## Prerequisites

- Java 17 (`java -version` shows 17.x; Java 21 also works since the build targets bytecode 17)
- Maven 3.9+ (`mvn -version`)
- Node.js 18+ (`node --version`) + npm 9+
- MySQL 8.0+ running locally OR Docker Desktop (for `docker compose up -d db`)
- Ports free: 8080 (backend), 3000 (frontend), 3306 (MySQL)

## Quick start (3 terminals)

### 1. Start MySQL

```bash
docker compose up -d db
docker compose ps   # db should show "healthy"
```

Without Docker: create database `hotel_booking` plus a user, then set `DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD` (see Configuration) before starting the backend.

### 2. Run backend

```bash
cd backend && mvn spring-boot:run
```

Expect `Started HotelBookingApplication` on http://localhost:8080. Tables are created automatically and `data.sql` seeds 6 hotels plus demo bookings.

### 3. Run frontend

```bash
cd frontend && npm install && npm run dev
```

Open http://localhost:3000. API calls go through the Vite proxy to `http://localhost:8080`.

### 4. Seed data

Sample hotels load automatically from `backend/src/main/resources/data.sql` on every boot (idempotent `INSERT IGNORE`). Try:

- City `Goa`, check-in `2026-12-20`, check-out `2026-12-23` (2 hotels, 1 room already taken at Palolem Bay Retreat)
- City `Jaipur`, check-in `2026-12-24`, check-out `2026-12-26` (2 hotels)
- City `Manali` for a single-hotel location (no cheapest-alternative suggestion possible)

Demo booking to look up: reference `TC-7K2P9M` + email `demo@example.com`.

## Configuration

| Variable | Used by | Default | Purpose |
|---|---|---|---|
| `DB_HOST` | backend | `localhost` | MySQL host |
| `DB_PORT` | backend, compose | `3306` | MySQL port |
| `DB_NAME` | backend, compose | `hotel_booking` | Database name |
| `DB_USER` | backend, compose | `hotel` | App DB user |
| `DB_PASSWORD` | backend, compose | `hotel` | App DB password (dev default; override in real use) |
| `MYSQL_ROOT_PASSWORD` | compose | `root` | MySQL root password for the container |
| `SERVER_PORT` | backend | `8080` | Backend HTTP port |
| `FRONTEND_ORIGIN` | backend | `http://localhost:3000,http://localhost:5173` | Allowed CORS origins |
| `VITE_API_BASE_URL` | frontend (`.env`) | empty (Vite proxy) | Set to `http://localhost:8080` to bypass the proxy |

Backend settings live in `backend/src/main/resources/application.properties` (all secrets via `${ENV:default}` placeholders); frontend settings live in `frontend/.env` (see `frontend/.env.example`).

## API overview (brief)

| Method + path | Purpose |
|---|---|
| `GET /api/hotels?location=&checkIn=&checkOut=&page=&size=` | Search/browse hotels with per-hotel availability |
| `GET /api/hotels/{id}?checkIn=&checkOut=` | Hotel detail; includes `smartAlternatives` when sold out |
| `POST /api/hotels/{id}/quote` | Backend-computed price quote for dates |
| `POST /api/bookings` | Create booking → `201` with `reference` (`TC-XXXXXX`) |
| `GET /api/bookings/lookup?reference=&email=` | Retrieve booking (alias: `GET /api/bookings/{reference}?email=`) |
| `POST /api/bookings/{reference}/cancel?email=` | Cancel booking (email also accepted as JSON body) |

Errors use the envelope `{timestamp,status,error,message,path,errors}`; a `409` sold-out response additionally carries `smartAlternatives`. See [DESIGN.md](DESIGN.md) section 3 for full contracts.

## Tests

- Backend: `cd backend && mvn test` (JUnit 5 + AssertJ + Mockito; H2 for integration slices, no MySQL needed):
  - `PricingServiceTest` — nights and totals incl. month boundary and 2dp rounding.
  - `AvailabilityServiceTest` — overlap matrix (turnover, partial, enclosing, identical) + availability math.
  - `SmartAlternativesServiceTest` — nearest-date and cheapest-alternative suggestions + null cases.
  - `BookingServiceTest` — server-side totals, sold-out + alternatives, turnover, cancel-release, double-cancel, email mismatch, date rules, reference uniqueness.
  - `BookingRepositoryTest` — overlap JPQL counts, status filter, unique-reference constraint.
  - `BookingValidationTest`, `HotelControllerTest` — MockMvc validation messages, error envelope shape, format hints.
- Frontend: no unit test suite in this release; verify with `cd frontend && npm run build` and the manual smoke checklist in [DESIGN.md](DESIGN.md) section 9.

## Project structure

```
backend/                          Spring Boot 3 / Java 17 / Maven
  src/main/java/com/thomascook/hotelbooking/
    HotelBookingApplication.java
    domain/        Hotel, Booking, BookingStatus
    repository/    HotelRepository, BookingRepository
    dto/           Hotel*, Booking*, Quote*, SmartAlternatives, Page, Error envelope
    service/       Hotel, Booking, Pricing, Availability, SmartAlternatives,
                   BookingReferenceGenerator, DateRangeValidator
    web/           HotelController, BookingController, GlobalExceptionHandler
    config/        ClockConfig, CorsConfig
  src/main/resources/application.properties, data.sql
  src/test/...     7 test classes (see Tests)
frontend/                         React 18 + Vite, plain CSS
  src/api/         client.js, hotels.js, bookings.js
  src/context/     BookingContext.jsx
  src/hooks/       useQueryParams.js, useFetch.js
  src/utils/       dates.js, money.js, validation.js
  src/components/  Header, Footer, HotelCard, AlternativesPanel, PriceBreakdown,
                   BookingForm, BookingSummary, LoadingSkeleton, ErrorBanner
  src/pages/       Search, Results, HotelDetail, Confirmation, MyBooking, NotFound
  src/styles/      tokens, base, layout, components, pages, print
docker-compose.yml                MySQL 8 service
DESIGN.md                         Architecture + design decisions
docs/demo-script.md               Screenshot/demo capture guide
```

## Time spent

| Area | Hours |
|---|---|
| Backend | _Xh — TODO: fill_ |
| Frontend | _Xh — TODO: fill_ |
| DB/DevOps | _Xh — TODO: fill_ |
| Docs/tests | _Xh — TODO: fill_ |
| **Total** | **_Xh — TODO: fill_** |

## Unfinished / known issues

- **Screenshots / demo recording not yet captured.** Follow [docs/demo-script.md](docs/demo-script.md) (about 5 minutes on a machine where ports can bind) and save captures under `docs/screenshots/`.
- **No live MySQL + browser run in the build environment** (sandbox blocks listening sockets and external network). Verified instead via: frontend production build (`npm run build`, green), the full backend suite executed through an offline `javac` + JUnit harness (37/41 green; the 4 failures are a proven H2 2.4.240 `CHECK`-constraint engine quirk — the pom pins H2 2.2.224 via the Boot BOM, so run `cd backend && mvn test` on a normal machine), and a full-stack MockMvc end-to-end pass covering all endpoints plus validation/overlap/cancel cases (18/18 steps green).
- Frontend has no unit test suite (build + manual smoke checklist only).
- Out of scope per spec: authentication, payments, admin panel, cloud deployment.

## AI disclosure

Built with AI assistance (Muse Code, agentic CLI): backend, frontend, tests, seed data, and docs were agent-generated from the task spec, then independently verified — backend compiles cleanly, 37/41 backend tests pass in an offline harness (remaining 4 attributed to an H2 version substitution, with a minimal repro), frontend production build passes, and an 18-step end-to-end API pass was observed. Booking/overlap logic, API contracts, and the surprise feature were reviewed against the spec during verification. Time-spent figures above are the author's own.

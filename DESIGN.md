# Hotel Booking Portal — Design

## 1. Overview & current capabilities

A full-stack hotel booking app: travelers search supplier-backed hotels by
destination and dates, view detail with live availability, get server-computed
quotes, and book with instant `TC-XXXXXX` references. Registered users log in
with email + password (JWT sessions), manage their profile, list their own
bookings, and reset passwords via emailed links. Lookups are scoped to the
account; cancels release inventory immediately. Whenever dates are sold out,
smart alternatives propose the nearest available window or the cheapest nearby
hotel instead of a dead end.

Working today: supplier search + inventory reconcile, quotes, bookings with
concurrency-safe creation, lookup/cancel, JWT auth, profile management,
password reset (SMTP), availability calendar for local hotels, and a `/health`
probe. Out of scope on purpose: payments, room types, admin, refresh tokens.

## 2. Tech stack

- Frontend: React 18 + Vite 5 + react-router, Three.js hero globe, GSAP,
  Phosphor icons, hand-written CSS tokens (no component library).
- Backend: Spring Boot 3.2.5 / Java 17 / Maven — Spring Web (REST), Bean
  Validation, Spring Data JPA + Hibernate, Spring Security (stateless JWT via
  JJWT 0.12.5, BCrypt), Spring Mail.
- Data: MySQL 8 (`ddl-auto=update`, no migrations yet); H2 for test slices.
- Supplier: Nuitee / LiteAPI over HTTPS (search + reconcile into `hotels`).
- Run: Docker Compose for local MySQL; Render (backend Docker) + Vercel
  (frontend) + managed MySQL in production (see `docs/DEPLOYMENT.md`).

## 3. Architecture

One stateless monolith + relational database. A React SPA talks JSON over REST
to a layered Spring Boot service (controller → service → repository) backed by
MySQL 8. No message queue, no cache, no server-side sessions — JWTs carry
identity, and every scaling-sensitive decision (availability, totals,
references) is computed in the API against the database.

```
                +------------------+
                | React SPA (:3000)|
                +--------+---------+
                         |  /api (JSON, CORS, Bearer JWT)
                +--------v---------+      +-----------+
                | Spring Boot (:8082)     | MySQL (:3307)
                | web / service /   --->  | users, hotels,
                | JPA repositories  |     | bookings, tokens
                +--------+---------+      +-----------+
                         |
              +----------v----------+
              | Nuitee API (search) |
              | SMTP (reset mail)   |
              +---------------------+
```

Request flow: `GET /api/hotels` → `HotelController` → `NuiteeSearchService`
(supplier search → synchronized upsert by `source+sourceId` → enrich each hit
with `availableRooms`) → paged JSON. Booking flow: `POST /api/bookings` →
bean validation → `JwtAuthenticationFilter` (validates `Bearer`, pass-through
when absent) → `BookingService.create` (pessimistic-lock hotel row, recheck
availability, server-side totals, insert CONFIRMED) → `201` with reference.
Validation happens at two boundaries: Jakarta bean validation on DTOs (shape)
plus service rules (dates vs. injected `Clock`, availability vs. inventory).
All failures funnel through `GlobalExceptionHandler` into one JSON envelope
`{timestamp,status,error,message,path,errors}`, with `smartAlternatives`
attached to sold-out 409s. CORS allows only configured frontend origins on
`/api/**` with credentials.

## 4. DB design

MySQL 8 was chosen for ACID transactions (the booking critical section needs
`SELECT … FOR UPDATE`), a mature JPA/Hibernate story, and cheap managed
hosting. Four tables; availability is derived, never stored.

```
users    (id PK, email UNIQUE, password_hash, display_name, created_at)
hotels   (id PK, name, location, description, amenities CSV, price_per_night
          DECIMAL(10,2), total_rooms, source, source_id, property/geo/content
          columns, created_at, updated_at, version)
   1 ────< bookings (id PK, reference UNIQUE, hotel_id FK, user_id FK nullable,
          room_offer_id, room_name, guest_count, guest_name, guest_email,
          check_in DATE, check_out DATE, nights, price_per_night snapshot,
          total_price snapshot, status CONFIRMED|CANCELLED, created_at,
          cancelled_at)
users 1 ────< password_reset_tokens (id PK, token_hash UNIQUE, user_id FK,
          expires_at, used)
```

Indexes/constraints: `UNIQUE(name, location)` + location/price/geo indexes for
search and cheapest-alternative scans; `UNIQUE(reference)` for lookups;
`idx_bookings_hotel_dates_status(hotel_id, status, check_in, check_out)`
powering the overlap count; `idx_bookings_email` for guest checks;
`UNIQUE(email)` and `UNIQUE(token_hash)` for auth. Date semantics are
half-open `[check_in, check_out)`: the checkout day is not occupied, so
same-day turnover never overlaps; `DATE` columns plus a UTC clock keep this
unambiguous. Schema is managed by Hibernate `ddl-auto=update` with
`spring.sql.init.mode=never` — there are no migrations and no seed rows;
inventory arrives from the supplier at runtime (a boot-time reconciler deletes
unbooked non-supplier hotels), and H2 (not MySQL) backs the repository slices.

## 5. API design

Base path `/api`, JSON, standard statuses: `200` reads, `201` register/booking
creation, `400` validation and bad date ranges (with `YYYY-MM-DD` hints),
`401` failed login, `404` unknown hotel or unknown booking reference (one
generic message, never revealing which half was wrong), `409` sold-out,
double-cancel, or lock contention ("High demand — please retry").

| Endpoint | Auth | Request | Response |
|---|---|---|---|
| `POST /api/auth/register` | open | `{email,password,displayName}` | `201 {token,tokenType:Bearer,expiresIn,user}` |
| `POST /api/auth/login` | open | `{email,password}` | `{token,tokenType:Bearer,expiresIn,user}` |
| `POST /api/auth/forgot-password` | open | `{email}` | empty; mails link (unknown emails silently accepted) |
| `POST /api/auth/reset-password` | open | `{token,newPassword}` | empty; single-use 30-min token |
| `GET /api/account` | JWT | — | `{id,email,displayName}` |
| `PATCH /api/account` | JWT | `{displayName}` | updated profile |
| `POST /api/account/password` | JWT | `{currentPassword,newPassword}` | empty |
| `GET /api/hotels?location=&checkIn=&checkOut=&guests=2&page=0&size=10` | open | location+dates required | page of hotels + `availableRooms` |
| `GET /api/hotels/{id}?checkIn=&checkOut=` | open | dates optional but paired | detail + `availability`; `smartAlternatives` when dates given and rooms = 0 |
| `GET /api/hotels/{id}/availability?from=&to=` | open | local hotels only; default today→+30d, max 366d | per-day availability + occupancy |
| `POST /api/hotels/{id}/quote` | open | `{checkIn,checkOut}` | `{hotelId,pricePerNight,nights,totalPrice,currency,availableRooms}` — display-only, never trusted at booking time |
| `POST /api/bookings` | JWT | `{hotelId,guestName,email,checkIn,checkOut,roomOfferId?,roomName?,guests?}` | `201 {reference: TC-XXXXXX, …, status: CONFIRMED}` |
| `GET /api/bookings/mine` | JWT | — | caller's bookings, newest first |
| `GET /api/bookings/lookup?reference=&email=` (alias `GET /api/bookings/{reference}?email=`) | JWT | account-scoped | booking detail incl. `createdAt/cancelledAt` |
| `POST /api/bookings/{reference}/cancel` (`?email=` or `{"email"}` body) | JWT | owner booking | `{reference,status: CANCELLED,cancelledAt,message}`; second cancel → 409 |
| `GET /api/health` | open | — | `{"status":"UP"}` liveness probe |

Reference format `TC-` + 6 `SecureRandom` chars from an unambiguous alphabet
(no `0/O/1/I/L`), uniqueness pre-checked (5 attempts) with one save retry on
collision. Money is `BigDecimal(10,2)`, totals recomputed as
`nights × pricePerNight` (HALF_UP, 2dp) at quote and booking time; the response
carries `currency` (`INR`).

## 6. Booking logic

Availability: `free = hotel.totalRooms − COUNT(bookings WHERE hotel AND
status=CONFIRMED AND existing.checkIn < req.checkOut AND existing.checkOut >
req.checkIn)`. The strict inequalities implement the half-open semantics;
`status=CONFIRMED` excludes cancelled rows so cancel immediately releases
inventory. Pricing: `nights = checkOut − checkIn` (ChronoUnit.DAYS),
`total = nights × current pricePerNight`, snapshotted onto the booking so
later price changes never rewrite history. State machine:
`NONE → CONFIRMED → CANCELLED`, terminal — rebooking creates a new reference,
the old one stays `CANCELLED` and lookupable. `create`, `cancel`, and `reset`
are single `@Transactional` units; creation additionally holds a pessimistic
write lock on the hotel row (see section 10).

## 7. Auth & security

Spring Security, stateless (`SessionCreationPolicy.STATELESS`, CSRF off):
`/api/auth/**`, `/api/hotels/**`, `/api/health`, and `/error` are public;
everything else requires a valid `Bearer` JWT. `JwtAuthenticationFilter`
(a `OncePerRequestFilter`) verifies the signature, puts the email subject
into the `SecurityContext`, and passes the request through untouched when no
token is present — rejection happens at the authorization rule, never in the
filter. Passwords are BCrypt-hashed; emails are normalized (trim + lowercase)
and unique-checked with a race-safe DB-constraint fallback. JWTs are HMAC-SHA
signed, 60-minute TTL by default; secrets under 32 chars fail startup, and a
missing secret falls back to a random per-boot key (set `JWT_SECRET` in every
real environment or sessions die on restart). CORS is restricted to
`FRONTEND_ORIGIN` on `/api/**` with credentials allowed. Password-reset
tokens are 32 `SecureRandom` bytes, SHA-256 hashed at rest, 30-minute expiry,
single-use; the reset link points at `{FRONTEND_URL}/reset-password?token=`,
and forgot-password fails loudly when no SMTP sender is configured.

## 8. Supplier integration

`NuiteeSearchService.search` is the only search path: when the supplier is
enabled it calls `NuiteeHotelProvider.search(location, dates, guests)`,
upserts each hit into `hotels` keyed by `source + sourceId` (synchronized, so
concurrent searches cannot insert duplicates), then enriches every hit with
locally computed availability and returns a page. When disabled it
short-circuits with no supplier call. A `CommandLineRunner` reconciler runs
at boot and deletes unbooked non-`NUITEE` hotels, so the bookable catalog is
exactly the supplier catalog; detail, quote, and booking each re-check
`source = NUITEE` and 404 otherwise. Nuitee supplies availability only for a
requested range, so the daily calendar endpoint refuses supplier hotels with
a 400 explaining why. Trade-off: search latency and results depend on a live
third party (no local cache yet), in exchange for real inventory with zero
seed maintenance.

## 9. Decisions / trade-offs

| Decision | Options | Chosen | Why / sacrificed |
|---|---|---|---|
| Monolith + RDBMS | microservices vs. one deployable | monolith | Scope fits ACID transactions; sacrificed independent scaling |
| Stateless JWT auth | sessions vs. JWT | JWT, no refresh | Simple + stateless; sacrificed revocation and silent renewal (60-min TTL) |
| Supplier-backed inventory | seed data vs. live supplier | Nuitee + reconcile | Real inventory, zero seed upkeep; sacrificed offline search and result stability |
| One room category per hotel | rooms table + types vs. `totalRooms` | `totalRooms` on hotel | No room types in scope; sacrificed per-type pricing/capacity (v2) |
| Overlap COUNT vs. per-night rows | query vs. materialized `room_inventory` | overlap COUNT | Always consistent, simple; sacrificed O(1) calendar reads for big hotels |
| In-memory search pagination | DB paging vs. slice-after-reconcile | slice after reconcile | Enrichment needs full rows; sacrificed DB-level paging efficiency |
| Pessimistic hotel-row lock | lock vs. optimistic `@Version` vs. exclusion constraint | pessimistic lock | Correct per-hotel serialization; sacrificed lock-free throughput (waits → retryable 409) |
| `ddl-auto=update`, no seeds | Flyway vs. Hibernate DDL | Hibernate DDL | Zero migration tooling; sacrificed versioned evolution (Flyway is next) |
| Reference `TC-XXXXXX` | UUID vs. short code | short code | Human-readable; sacrificed global scale (fine: unique index + retry) |
| H2 test slices | Testcontainers MySQL vs. H2 | H2 | Fast, no Docker in tests; sacrificed dialect fidelity |
| React + hand CSS | MUI/Tailwind vs. tokens | hand CSS | Tiny bundle; sacrificed pre-built widgets |
| Render + Vercel split | one host vs. split | split + external MySQL | Free tiers; sacrificed single-deploy simplicity (CORS wiring) |

## 10. Simultaneous bookings (concurrency)

Race: two requests both read "1 room left", both insert → overbooking. Fix:
`BookingService.create` runs in one transaction that first executes
`SELECT … FOR UPDATE` on the hotel row
(`HotelRepository.findByIdForUpdate`), serializing the count-then-insert
critical section per hotel. The loser blocks briefly, then re-reads the
committed count and receives `409 sold out` with smart alternatives instead
of a duplicate room. Default READ_COMMITTED isolation is sufficient because
the row lock (not the isolation level) provides the mutual exclusion; lock
waits surface as 409 "High demand — please retry". What was deliberately not
used: `UNIQUE(hotel, dates)` (would allow only one room per range — wrong
cardinality) and optimistic `@Version` on `Hotel` (no hotel column is written
during booking, so no version bump would ever fire). Proven today by the
cancel-release and sold-out cases in `BookingServiceTest`; a true
multi-threaded last-room test is still the next proof to add (manual repro:
fire two `curl POST /api/bookings` with a JWT at a 1-room hotel in parallel
and assert one `201` + one `409`).

## 11. Surprise feature

**Smart alternatives**: whenever a hotel is sold out for requested dates, the
API returns actionable next steps instead of a dead end. `nearestAvailable`
shifts the same stay length forward day-by-day (up to +30 days) and reports
the earliest window with a free room; `cheapestAlternative` scans same-location
hotels cheapest-first for one with a room on the exact requested dates. Both
are computed by `SmartAlternativesService` and surfaced in three places: the
`409` sold-out error envelope, `GET /api/hotels/{id}` when the hotel is sold
out for the requested dates, and the frontend `AlternativesPanel` with
one-click re-search. Why this instead of a plain "sold out": a dead end loses
the traveller, while a concrete next step (shift dates vs. switch hotel, with
prices) keeps the booking funnel alive. It reuses the same overlap COUNT query
as availability, so suggestions can never contradict the availability math.

## 12. Testing

`cd backend && mvn test` — JUnit 5 + AssertJ + Mockito; H2 for repository
slices, no MySQL needed. `PricingServiceTest` (nights/totals/rounding),
`AvailabilityServiceTest` (overlap matrix: turnover, partial, enclosing,
identical), `SmartAlternativesServiceTest` (nearest/cheapest + null cases),
`BookingRepositoryTest` (`@DataJpaTest`: overlap JPQL, status filter, unique
reference), `HealthControllerTest` (`@WebMvcTest` + real `SecurityConfig`:
`UP` without auth). Currently stale after the Nuitee/auth refactor:
`BookingServiceTest`, `BookingValidationTest`, `HotelControllerTest` (missing
`NuiteeSearchService`/`JwtService` wiring in the tests, not app bugs) — see
README section 11. Frontend has no unit tests: `npm run build` plus the smoke
checklist in section 14.

## 13. Limitations & future scope

Limitations today: schema via Hibernate DDL, not versioned Flyway migrations;
no refresh-token rotation or revocation; no rate limiting; no idempotency keys
on booking creation (reference retry covers collisions, not double-clicks); one
room category per hotel; no payments/admin/reviews; search depends on a live
supplier with no cache; three stale test classes; no frontend unit tests; no
multi-threaded last-room test; minimal observability (no Actuator/metrics).

Likely next, in rough value order: fix the three stale test classes; Flyway
migrations; idempotency keys on `POST /api/bookings`; refresh-token rotation;
rate limiting on auth + booking endpoints; a parallel last-room integration
test; room types with per-type inventory; Actuator health/metrics; search
result caching with supplier webhooks; payments + admin panel; frontend
component tests.

## 14. Smoke-test checklist (manual end-to-end)

1. `docker compose up -d db`, then `docker compose ps` shows `db` healthy.
2. `cd backend && mvn spring-boot:run` prints `Started HotelBookingApplication`
   on `:8082` (needs `JWT_SECRET` + `NUITEE_API_KEY` in `backend/.env`).
3. `GET /api/health` returns `{"status":"UP"}`.
4. `POST /api/auth/register` returns `201` + token; save it as `$JWT`.
5. `GET /api/hotels?location=Goa&checkIn=2026-12-20&checkOut=2026-12-23`
   returns supplier hotels with `availableRooms`.
6. `GET /api/hotels/{id}?checkIn=…&checkOut=…` shows availability;
   `POST /api/hotels/{id}/quote` returns nights × price math.
7. `POST /api/bookings` with `Bearer $JWT` returns `201` + `TC-XXXXXX`;
   availability drops by one.
8. `GET /api/bookings/lookup?reference=&email=` (same `$JWT`) returns the
   booking; unknown reference → generic `404`.
9. `GET /api/bookings/mine` lists your bookings.
10. `POST /api/bookings/{ref}/cancel` → `CANCELLED`; repeat → `409`; room
    released.
11. Book the last room of a small hotel, then book once more → `409` with
    `smartAlternatives`; frontend shows the alternatives panel.
12. `POST /api/auth/forgot-password` + `reset-password` rotate the password
    (needs SMTP configured); old password stops working.

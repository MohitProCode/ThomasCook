# Hotel Booking Portal — Design

## 1. Architecture

Single monolith + relational database: a React SPA talks JSON over REST to a layered Spring Boot
service (controller → service → repository) backed by MySQL 8. No auth, no payments, no message
queue — the scope (search, quote, book, lookup, cancel) fits one deployable with ACID guarantees
from the database.

```
                +------------------+
                | React SPA (:3000)|
                +--------+---------+
                         |  /api (JSON, CORS)
                +--------v---------+
                | Spring Boot (:8080)              +-----------+
                | controller / service / JPA  ---> | MySQL (:3306)
                +------------------+              +-----------+
```

Search flow: `GET /api/hotels` → `HotelController` → `HotelService.search` (validates the optional
date pair via `DateRangeValidator` + injected `Clock`, loads hotels, enriches each with
`availableRooms = totalRooms − COUNT(CONFIRMED overlapping)`) → paged JSON. Booking flow:
`POST /api/bookings` → bean validation → `BookingService.create` (pessimistic-lock hotel row,
recompute nights/total server-side, overlap check, insert CONFIRMED) → `201` with reference.
Validation happens at two boundaries: Jakarta bean validation on request DTOs (shape) and service
rules (dates vs. clock, availability vs. inventory). All failures funnel through
`GlobalExceptionHandler` (`@RestControllerAdvice`) into one JSON envelope
`{timestamp,status,error,message,path,errors}`, with `smartAlternatives` attached to 409s. CORS is
restricted to the configured frontend origins.

## 2. DB design

Two tables; availability is derived, never stored.

```
hotels   (id PK, name, location, description, amenities CSV, price_per_night DECIMAL(10,2),
          total_rooms, created_at, updated_at, version)
   1 ────< bookings (id PK, reference UNIQUE, hotel_id FK RESTRICT, guest_name, guest_email,
          check_in DATE, check_out DATE, nights, price_per_night snapshot, total_price snapshot,
          status CONFIRMED|CANCELLED, created_at, cancelled_at)
```

Indexes: `UNIQUE(name, location)` + `idx_hotels_location(_price)` for search and cheapest-alternative
queries; `UNIQUE(reference)` for lookups; `idx_bookings_hotel_dates_status(hotel_id, status,
check_in, check_out)` powering the overlap count; `idx_bookings_email` for owner checks. Date
semantics are half-open `[check_in, check_out)`: the checkout day is not occupied, so a booking
ending on day X and one starting on day X (same-day turnover) do not overlap. `DATE` columns (no
time component) plus UTC clock keep this unambiguous. Schema is managed by Hibernate `ddl-auto`
(`update` in dev, validated against entities) and seed data ships as `data.sql` with idempotent
`INSERT IGNORE` rows: 6 hotels across 4 locations (Goa ×2 and Jaipur ×2 so cheapest-alternative
queries have answers), 2 CONFIRMED + 1 CANCELLED demo bookings.

## 3. API design

Base path `/api`, JSON, standard status codes: `200` reads, `201` booking creation, `400`
validation, `404` unknown hotel / reference+email mismatch (one generic message, never revealing
which half was wrong), `409` sold-out or double-cancel. Every error returns the envelope above;
sold-out 409s add `smartAlternatives {nearestAvailable, cheapestAlternative}`.

| Endpoint | Request | Response |
|---|---|---|
| `GET /api/hotels?location=&checkIn=&checkOut=&page=&size=` | dates optional but paired; `YYYY-MM-DD` | `{content:[{id,name,location,description,amenities[],pricePerNight,totalRooms,availableRooms\|null,nights,totalForStay}],page,size,totalElements}` |
| `GET /api/hotels/{id}?checkIn=&checkOut=` | optional dates | detail + `availability{checkIn,checkOut,nights,availableRooms}`; `smartAlternatives` only when dates given and rooms = 0 |
| `POST /api/hotels/{id}/quote` | `{checkIn,checkOut}` | `{hotelId,pricePerNight,nights,totalPrice,currency,availableRooms}` — display-only, never trusted at booking time |
| `POST /api/bookings` | `{hotelId,guestName,email|guestEmail,checkIn,checkOut}` | `201 {reference: TC-XXXXXX, hotelId, hotelName, ..., nights, pricePerNight, totalPrice, status: CONFIRMED}` |
| `GET /api/bookings/lookup?reference=&email=` (alias `GET /api/bookings/{reference}?email=`) | both required | booking detail incl. `createdAt/cancelledAt` |
| `POST /api/bookings/{reference}/cancel?email=` (email also accepted as `{"email"}` body) | owner email | `{reference,status: CANCELLED,cancelledAt,message}`; second cancel → 409 |

Reference format `TC-` + 6 unambiguous chars (no `0/O/1/I/L`), generated server-side with
`SecureRandom`, uniqueness pre-checked and retried on collision. Money is `BigDecimal(10,2)`,
totals recomputed as `nights × pricePerNight` (HALF_UP, 2dp) at quote and booking time.

## 4. Booking logic

Availability: `free = hotel.totalRooms − COUNT(bookings WHERE hotel AND status=CONFIRMED AND
existing.checkIn < req.checkOut AND existing.checkOut > req.checkIn)`. The strict inequalities
implement the half-open semantics; `status=CONFIRMED` excludes cancelled rows so cancel immediately
releases inventory. Pricing: `nights = checkOut − checkIn` (ChronoUnit.DAYS), `total = nights ×
current pricePerNight`, snapshotted onto the booking so later price changes never rewrite history.
State machine: `NONE → CONFIRMED → CANCELLED`, terminal — rebooking creates a new reference, the
old one stays `CANCELLED` and lookupable. `create` and `cancel` are single `@Transactional` units;
creation additionally holds a pessimistic write lock on the hotel row (see section 6).

## 5. Decisions / trade-offs

| Decision | Options | Chosen | Why / sacrificed |
|---|---|---|---|
| One room category per hotel | rooms table + types vs. `totalRooms` on hotel | `totalRooms` on hotel | Scope has no room types; sacrificed per-type pricing and capacity, documented as v2 |
| Overlap COUNT query vs. per-night inventory rows | query vs. materialized `room_inventory(date, booked)` | overlap COUNT | Simpler, always consistent, fast enough at this scale with the covering index; sacrificed O(1) calendar reads for big hotels |
| Pessimistic hotel-row lock | lock vs. optimistic `@Version` vs. DB exclusion constraint | pessimistic lock | Correct under concurrency with per-hotel granularity; sacrificed lock-free throughput and added possible lock waits (mapped to retryable 409) |
| `data.sql` seed vs. Flyway | `data.sql` vs. Flyway migrations | `data.sql` + `ddl-auto=update` | Zero migration tooling for a demo; sacrificed versioned schema evolution (Flyway is the documented next step) |
| Reference `TC-XXXXXX` | UUID vs. short code | short code | Human-readable over phone/counter; sacrificed global uniqueness scale (fine for one DB with unique index + retry) |
| No auth / no payments | session/JWT + payment provider vs. email-as-key | email-as-owner-key | Keeps the demo end-to-end; sacrificed real identity, idempotency keys, and money movement |
| React + plain CSS, no component lib | MUI/Tailwind vs. hand CSS | hand CSS + tokens | Tiny bundle, no theming fights; sacrificed pre-built widgets (all custom) |

## 6. Simultaneous bookings (concurrency)

Race: two requests both read "1 room left", both insert → overbooking. Fix: `BookingService.create`
runs in one transaction that first executes `SELECT … FOR UPDATE` on the hotel row
(`HotelRepository.findByIdForUpdate`), serializing the count-then-insert critical section per hotel.
The loser blocks briefly, then re-reads the committed count and receives `409 sold out` with smart
alternatives instead of a duplicate room. Default READ_COMMITTED isolation is sufficient because the
row lock (not the isolation level) provides the mutual exclusion; lock waits surface as 409
"High demand — please retry". What was deliberately not used: `UNIQUE(hotel, dates)` (would allow
only one room per date range — wrong cardinality) and optimistic `@Version` on `Hotel` (no hotel
column is written during booking, so no version bump would ever fire). Tested by the cancel-release
and sold-out integration tests in `BookingServiceTest`; a true multi-threaded last-room test is
listed under Limitations as the next proof to add (manual repro: fire two `curl POST /api/bookings`
for a 1-room hotel in parallel and assert one `201` + one `409`).

## 7. Surprise feature

**Smart alternatives** (the surprise): whenever a hotel is sold out for requested dates, the API
returns actionable next steps instead of a dead end. `nearestAvailable` shifts the same stay length
forward day-by-day (up to +30 days) and reports the earliest window with a free room; 
`cheapestAlternative` finds the cheapest same-location hotel that still has a room for the
exact requested dates. Both are computed by `SmartAlternativesService` (+30-day forward scan for
the same stay length; same-location cheapest scan excluding the sold-out hotel) and surfaced in
three places: the `409` sold-out error envelope, `GET /api/hotels/{id}` when the hotel is sold out
for the requested dates, and the frontend `AlternativesPanel` with one-click re-search. Why this
instead of a plain "sold out": a dead end loses the traveller, while a concrete next step (shift
dates vs. switch hotel, with prices) keeps the booking funnel alive. It reuses the same overlap
COUNT query as availability, so suggestions can never contradict the availability math.

## 8. Testing, limitations & next steps

Testing: 41 tests, no MySQL needed (H2 in MySQL mode for slices, Mockito for units, MockMvc for
web validation + envelope shape). `PricingServiceTest` (nights/totals/rounding),
`AvailabilityServiceTest` (overlap matrix: turnover, partial, enclosing, identical),
`SmartAlternativesServiceTest` (nearest/cheapest + null cases), `BookingServiceTest`
(`@SpringBootTest`: server-side totals, sold-out + alternatives, turnover, cancel-release,
double-cancel, email mismatch, date rules, reference uniqueness), `BookingRepositoryTest`
(`@DataJpaTest`: overlap JPQL, status filter, unique reference), `BookingValidationTest` and
`HotelControllerTest` (`@WebMvcTest`: messages, envelope, date-format hints). Run with
`cd backend && mvn test`.

Limitations: no auth (email is the owner key) and no payments; one room category per hotel;
schema via Hibernate `ddl-auto` + `data.sql` instead of versioned Flyway migrations; `data.sql`
uses MySQL-dialect `INSERT IGNORE` (H2 slices disable it); no multi-threaded last-room
concurrency test yet (manual repro in section 6); no frontend unit tests (build + smoke only).

Likely next steps: Flyway migrations; JWT/session auth + idempotency keys on booking creation;
room types with per-type inventory; a parallel last-room integration test; pagination/sort parity
in the UI; frontend component tests.

## 9. Smoke-test checklist (manual end-to-end)

1. `docker compose up -d db`, then `docker compose ps` shows `db` healthy.
2. `cd backend && mvn spring-boot:run` prints `Started HotelBookingApplication` on `:8080`.
3. `GET /api/hotels?size=50` returns the 6 seeded hotels.
4. Search Goa `2026-12-20 → 2026-12-23`: 2 hotels; Palolem Bay Retreat shows 19/20 rooms left.
5. `GET /api/hotels/1?checkIn=2026-12-20&checkOut=2026-12-23` shows availability; `POST
   /api/hotels/1/quote` returns nights × price math.
6. `POST /api/bookings` returns `201` with a `TC-XXXXXX` reference; availability drops by one.
7. `GET /api/bookings/lookup?reference=&email=` returns the booking; wrong email → `404`.
8. `POST /api/bookings/{ref}/cancel?email=` → `CANCELLED`; repeat → `409`; room released.
9. Book the last room of a small hotel, then book once more → `409` with `smartAlternatives`
   (`nearestAvailable` and/or `cheapestAlternative`); frontend shows the alternatives panel.
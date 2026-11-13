# Demo capture script (screenshots / recording)

The repository does not bundle screenshots yet. Capture them on any machine
where ports can bind (the flow needs the backend on :8080 and the frontend
on :3000). Takes about 5 minutes.

## Setup

```bash
docker compose up -d db
cd backend && mvn spring-boot:run     # wait for "Started HotelBookingApplication"
cd frontend && npm install && npm run dev   # open http://localhost:3000
```

## Shots (in order)

1. **Search** — home page. Enter location `Goa`, check-in `2026-12-20`,
   check-out `2026-12-23`, screenshot the filled form.
2. **Results** — submit the search. Screenshot the two Goa hotels with
   per-night prices and stay totals.
3. **Detail + quote** — open `Palolem Bay Retreat`. Screenshot the room,
   price breakdown (3 nights x 4500 = 13500), and booking form.
4. **Confirmation** — book as `Demo Guest / demo-new@example.com`.
   Screenshot the confirmation with the `TC-XXXXXX` reference.
5. **Lookup** — open My Booking, enter the reference + email.
   Screenshot the retrieved booking.
6. **Cancel + rebook** — cancel it (screenshot the cancelled state), then
   book the same hotel/dates again to show the slot was released.
7. **Sold-out alternatives (surprise feature)** — book all rooms of
   `Lake Pichola Heritage Stay` (10 rooms) for one date range via repeated
   bookings, then attempt one more and screenshot the 409 response in the UI:
   nearest available dates + cheapest Udaipur alternative.

Save captures under `docs/screenshots/` as `01-search.png` … `07-alternatives.png`
and link them from the README submission section.

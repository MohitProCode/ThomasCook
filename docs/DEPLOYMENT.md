# Deployment runbook — MySQL 8 + Spring Boot + React (Vercel)

Target: frontend on Vercel, backend on Render Free (Docker), MySQL 8 durable
outside Render. The backend is stateless, so it needs no persistent disk;
only MySQL needs a volume.

## 0. Decide where MySQL lives (pick one)

| Option | Free persistent storage | Notes |
|---|---|---|
| A. Oracle Always Free HeatWave/MySQL | Yes, managed | Recommended: no volume to manage. |
| B. Oracle Always Free VM + `docker-compose.prod.yml` | Yes, Block Volume | Full control; you run `db` + `backend` together. |
| C. Railway Free MySQL template | 0.5 GB volume | Tightest; fine for demo. |

Do not put MySQL on Render Free: its filesystem is ephemeral and Free
services cannot attach a persistent disk.

## 1. Provision MySQL

Option A (HeatWave):

1. Create an Always Free HeatWave/MySQL instance, database `hotel_booking`,
   plus an app user with full rights on that database.
2. Allow ingress from the backend host (Render outbound IPs change; prefer
   `%` host with a strong password for a demo, IP-allowlist for real use).
3. Note `DB_HOST`, `DB_PORT` (usually 3306), `DB_NAME`, `DB_USER`, `DB_PASSWORD`.

Option B (VM + Docker):

1. Launch an Always Free Ampere/AMD VM, attach a Block Volume, install Docker.
2. Copy the repo to the VM, create a `.env` file next to
   `docker-compose.prod.yml` with `MYSQL_ROOT_PASSWORD`, `DB_*`,
   `FRONTEND_ORIGIN`, `FRONTEND_URL`, `JWT_SECRET` (32+ random chars),
   `NUITEE_API_KEY`.
3. Run `docker compose -f docker-compose.prod.yml up -d --build`.
4. Open the firewall for `${HOST_PORT:-8082}` (or put Caddy/Nginx + TLS in
   front and expose 443 only).

Option C (Railway, paid):

1. Create a MySQL service from the template with its volume at
   `/var/lib/mysql`.
2. Enable public TCP networking on the MySQL service. Copy the public
   host, public port (a random port, not 3306), user, and password into
   the backend env vars below. `DB_PORT` is a Blueprint prompt for this
   reason.
3. Create the `hotel_booking` database (Railway's built-in Query tab works).

## 2. Deploy the backend (Render Free + Blueprint)

1. Push this repo to GitHub.
2. Render Dashboard → New → Blueprint → select the repo (`render.yaml`).
3. Fill the `sync: false` prompts: `DB_HOST`, `DB_USER`, `DB_PASSWORD`
   (from step 1), `FRONTEND_ORIGIN` + `FRONTEND_URL` (the Vercel URL from
   step 3 — use a placeholder first, update after), `NUITEE_API_KEY`.
   `JWT_SECRET` is auto-generated; `PORT=10000` is already set.
4. Deploy, then verify: `curl https://<svc>.onrender.com/api/health`
   returns `{"status":"UP"}`.

Free-tier notes: 750 instance hours/month per workspace; the service spins
down on idle, so the first request after idle is slow.

## 3. Deploy the frontend (Vercel)

1. Import the repo in Vercel, set Root Directory to `frontend/`.
2. Build command `npm run build`, output `dist` (auto-detected).
3. Env var: `VITE_API_BASE_URL=https://<render-backend>/` (no trailing
   path; `/api/...` is appended by the client).
4. Deploy. `vercel.json` already handles SPA fallback for react-router.

## 4. Wire CORS and cut over

1. In Render, set `FRONTEND_ORIGIN` and `FRONTEND_URL` to the real Vercel
   URL (e.g. `https://hotel-booking.vercel.app`) and redeploy.
2. Smoke test:
   - `GET /api/hotels?location=Goa&checkIn=2026-12-20&checkOut=2026-12-23`
   - Search → Results → Detail → Quote → Book → Confirmation → MyBooking.
3. Durability test: create a booking, restart the DB service, look the
   booking up again by reference + email. It must still be there.
4. Back up the DB (HeatWave automated backups, or a `mysqldump` cron on
   the VM / Railway backups).

## Env var reference (backend)

`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `PORT`
(Render-injected) or `SERVER_PORT`, `FRONTEND_ORIGIN` ( CSV allowed),
`FRONTEND_URL`, `JWT_SECRET` (32+ random chars), `JWT_TTL_MINUTES`,
`NUITEE_ENABLED`, `NUITEE_API_KEY`, `NUITEE_BASE_URL`,
`NUITEE_GUEST_NATIONALITY`, optional `MAIL_HOST/PORT/USERNAME/PASSWORD/
SMTP_AUTH/SMTP_STARTTLS/MAIL_FROM`, optional `JAVA_OPTS`.
See `backend/.env.example` for local defaults.

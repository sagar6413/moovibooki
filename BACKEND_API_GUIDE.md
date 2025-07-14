# MooviBooki Backend API Guide

Welcome to the MooviBooki backend API! This guide is for frontend developers to quickly understand how to interact with
the backend, including authentication, endpoints, request/response structure, and integration tips.

---

## API Base URL

- **Base URL:** `/api/v1/`
- **Swagger UI:** `/swagger-ui.html` (interactive API docs)
- **OpenAPI JSON:** `/v3/api-docs`

---

## Authentication

- **Type:** JWT Bearer Token
- **Endpoints:**
    - `POST /api/v1/auth/register` — Register a new user
    - `POST /api/v1/auth/login` — Login and receive JWT tokens
    - `POST /api/v1/auth/verify-email` — Email verification
    - `POST /api/v1/auth/password-reset/initiate` — Request password reset
    - `POST /api/v1/auth/password-reset/complete` — Complete password reset
- **How to use:**
    1. Register/login to receive an `accessToken` (JWT).
    2. For all protected endpoints, add header: `Authorization: Bearer <accessToken>`
    3. Use the "Authorize" button in Swagger UI to test authenticated endpoints.

---

## Main API Endpoints

### Movies

- `GET /api/v1/movies` — Search/filter movies
- `GET /api/v1/movies/{movieId}` — Get movie details
- `POST /api/v1/movies` — Create movie (ADMIN)
- `PUT /api/v1/movies/{movieId}` — Update movie (ADMIN)
- `DELETE /api/v1/movies/{movieId}` — Delete movie (ADMIN)
- `POST /api/v1/movies/{movieId}/poster` — Upload poster (ADMIN)
- `POST /api/v1/movies/{movieId}/review` — Add review (USER/ADMIN)

### Theaters & Screens

- `GET /api/v1/theaters/{theaterId}` — Get theater details
- `GET /api/v1/theaters/owner/{ownerId}` — Get theaters by owner (THEATER_OWNER)
- `POST /api/v1/theaters` — Register theater (THEATER_OWNER)
- `PUT /api/v1/theaters/{theaterId}` — Update theater (THEATER_OWNER)
- `DELETE /api/v1/theaters/{theaterId}` — Delete theater (THEATER_OWNER)
- `POST /api/v1/theaters/{theaterId}/screens` — Add screen (THEATER_OWNER)
- `PUT /api/v1/theaters/screens/{screenId}` — Update screen (THEATER_OWNER)
- `DELETE /api/v1/theaters/screens/{screenId}` — Delete screen (THEATER_OWNER)
- `GET /api/v1/theaters/{theaterId}/screens` — Get screens by theater
- `PATCH /api/v1/theaters/{theaterId}/amenities` — Update amenities (THEATER_OWNER)

### Bookings

- `POST /api/v1/bookings` — Book seats (USER/ADMIN/THEATER_OWNER)
- `POST /api/v1/bookings/lock-seats` — Lock seats before booking
- `PUT /api/v1/bookings/{bookingId}` — Modify booking
- `DELETE /api/v1/bookings/{bookingId}` — Cancel booking
- `POST /api/v1/bookings/group` — Group booking
- `GET /api/v1/bookings/{bookingId}` — Get booking details
- `GET /api/v1/bookings/user/{userId}` — Get bookings by user

### Admin

- `GET /api/v1/admin/dashboard` — Admin dashboard (ADMIN)

---

## Request/Response Structure

- **All endpoints use JSON for request and response bodies.**
- **Request DTOs:** See Swagger UI for detailed field requirements.
- **Response DTOs:** All responses are well-typed and documented in Swagger UI.
- **Error Handling:** Standard HTTP status codes. Error messages in response body.

---

## Caching

- Frequently accessed data (movies, theaters, bookings, user profiles) is cached for performance.
- Cache is automatically invalidated on data changes.

---

## Seat Locking

- Real-time seat selection uses distributed locking (Redis/Redisson).
- Seats are locked for 2 minutes during booking to prevent double-booking.
- If booking is not completed, seats are released automatically.

---

## Integration Tips

- Use Swagger UI to explore and test all endpoints interactively.
- Always include the JWT token for protected endpoints.
- Check required roles for each endpoint (see Swagger UI tags and descriptions).
- For file uploads (e.g., movie poster), use `multipart/form-data`.
- Handle error responses gracefully in the frontend.

---

## Need Help?

- All endpoints, models, and parameters are documented in Swagger UI.
- For questions or issues, contact the backend team.

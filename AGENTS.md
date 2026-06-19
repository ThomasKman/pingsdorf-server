# Pingsdorf Backend — Agent Notes

This document captures the high-level product / API specification for the
**Pingsdorf** backend server. Use it as the source of truth when adding
features. Keep it in sync with the implementation.

## Project Overview

Build a backend API for **Pingsdorf** — a web application where couples /
roommates can "ping" items on a shared floor plan map that the other person
forgot to put away. The frontend is a Vite + React + TypeScript SPA that
currently stores all data client-side. The backend persists data and enables
real-time sync between users.

### Tech Stack (this repo)

- **Runtime:** Java 25
- **Framework:** Spring Boot 4 (Web, Security, Validation, Actuator, Data MongoDB)
- **Database:** MongoDB
- **Build:** Maven
- **Auth:** JWT (stateless)
- **Real-time:** WebSocket / STOMP
- **Image storage:** Pluggable; local filesystem by default (S3/MinIO swap-in later)

## Core Concepts

- **Household** — a shared space between partners / roommates. All data
  (floor plan, rooms, pings) belongs to a household.
- **Users** — people who belong to a household. A household typically has 2
  users (partners).
- **Pings** — items marked on the floor plan that need attention. Created by
  one user, can be "cleaned up" by the other.
- **Rooms** — named polygon regions on the floor plan (Kitchen, Bedroom, etc.)
  used to organize pings.

## Data Models

### User

```typescript
interface User {
  id: string
  email: string
  passwordHash: string
  name: string
  color: string         // Hex color for their pings on map
  avatar?: string       // Emoji or image URL
  householdId: string
  createdAt: Date
}
```

### Household

```typescript
interface Household {
  id: string
  name?: string                // e.g. "Our Apartment"
  floorPlanUrl?: string        // URL to uploaded floor plan image
  mapRotation: number          // 0 or 90 degrees
  inviteCode?: string          // For partner to join
  createdAt: Date
}
```

### Ping

```typescript
interface Ping {
  id: string
  name: string
  description: string
  imageUrl?: string            // URL to uploaded image (NOT base64)
  x: number                    // X coordinate as percentage (0-100)
  y: number                    // Y coordinate as percentage (0-100)
  roomId?: string              // Auto-detected, but stored for queries
  householdId: string
  createdByUserId: string
  createdAt: Date
  cleanedUpByUserId?: string   // User who cleaned this up
  cleanedUpAt?: Date
}
```

### Room

```typescript
interface Room {
  id: string
  name: string
  color: string                              // Hex color for boundary / fill
  points: { x: number, y: number }[]         // Polygon vertices (percentages)
  householdId: string
  createdAt: Date
}
```

## API Endpoints

### Authentication

- `POST /auth/signup` — create user account (creates new household)
- `POST /auth/login` — login, return JWT
- `POST /auth/logout` — invalidate session (client discards token)
- `GET  /auth/me` — current user + household info

### Household / Partner Linking

- `POST /household/invite` — generate invite code for partner
- `POST /household/join` — join existing household with invite code
- `PUT  /household` — update household settings (name, mapRotation)
- `POST /household/floor-plan` — upload floor plan image (returns URL)

### Pings

- `GET    /pings` — list all pings in household (active + cleaned up)
- `POST   /pings` — create new ping
- `PUT    /pings/:id` — update ping (name, description, image)
- `DELETE /pings/:id` — delete ping (only creator can delete)
- `POST   /pings/:id/cleanup` — mark ping as cleaned up
- `POST   /pings/:id/image` — upload ping image (returns URL)

### Rooms

- `GET    /rooms` — list all rooms in household
- `POST   /rooms` — create room with polygon points
- `PUT    /rooms/:id` — update room (name, color)
- `DELETE /rooms/:id` — delete room

### Real-time Events (WebSocket)

Topic: `/topic/household.{householdId}` (STOMP).

- `ping:created` — new ping added
- `ping:updated` — ping details changed
- `ping:deleted` — ping removed
- `ping:cleaned` — ping marked as cleaned up
- `room:created` / `room:updated` / `room:deleted`
- `household:updated` — settings changed (e.g. rotation)

## Authorization Rules

- Users may only access their own household's data.
- Only the ping creator may edit or delete their ping.
- Any household member may clean up any ping.
- Any household member may manage rooms.

## Validation Rules

- `x`, `y` coordinates must be `0..100`.
- Room polygons need at least 3 points.
- Names must be non-empty and reasonably bounded.
- Colors should be valid hex codes (e.g. `#RRGGBB`).

## Image Handling

- Frontend currently uses base64 (up to 5 MB per image).
- Backend accepts multipart uploads and stores in object storage.
- Backend returns URLs that the frontend uses in `<img>` tags.
- Consider compression / resizing as a follow-up.

## Example API Responses

### `GET /pings`

```json
{
  "pings": [
    {
      "id": "ping-1",
      "name": "Dirty socks",
      "description": "On the floor again",
      "imageUrl": "https://storage.example.com/pings/abc123.jpg",
      "x": 45.5,
      "y": 62.3,
      "roomId": "room-1",
      "createdBy": {
        "id": "user-1",
        "name": "Alice",
        "color": "#4a9eff"
      },
      "createdAt": "2026-06-19T10:30:00Z",
      "cleanedUpBy": null,
      "cleanedUpAt": null
    }
  ]
}
```

### WebSocket Event

```json
{
  "event": "ping:created",
  "data": {
    "id": "ping-2",
    "name": "Coffee mug"
  }
}
```

## Deployment

- Containerized with Docker.
- `docker-compose.yml` brings up backend + MongoDB (and MinIO when used).
- Frontend reads API base URL from `VITE_API_URL`.

## Repo Layout

```
pingsdorf-server/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── src/main/java/com/pingsdorf/server/
│   ├── PingsdorfServerApplication.java
│   ├── config/        # Security, WebSocket, Jackson, CORS
│   ├── auth/          # JWT, AuthController, AuthService
│   ├── user/          # User document, repository, service
│   ├── household/     # Household document, controller, service
│   ├── ping/          # Ping document, controller, service
│   ├── room/          # Room document, controller, service
│   ├── storage/       # Image storage abstraction
│   └── realtime/      # WebSocket event publisher
└── src/main/resources/application.yml
```

## Implementation Order

1. Domain models + repositories.
2. JWT auth (signup, login, me).
3. Household + invite + join.
4. Ping CRUD + cleanup.
5. Room CRUD.
6. Image upload (local filesystem first).
7. WebSocket real-time events.
8. Docker / docker-compose.
9. (Follow-up) OpenAPI / Swagger docs.

## Conventions

- Java records for DTOs where practical; Lombok for domain entities.
- All controllers under a versionless root for now; add `/api/v1` once
  the frontend is wired up.
- Errors are returned as `ProblemDetail` (RFC 7807) via Spring's default
  exception translation. Custom exceptions extend `ResponseStatusException`
  or use a `@ControllerAdvice`.
- Time fields use `Instant` on the server, serialized as ISO-8601.

# pingsdorf-server

Backend API for **Pingsdorf** — a web app where couples / roommates "ping"
items on a shared floor plan that the other person forgot to put away.

See [`AGENTS.md`](./AGENTS.md) for the full product / API spec.

## Stack

- Java 21, Spring Boot 4 (Web, Security, Validation, WebSocket, Data MongoDB)
- MongoDB
- JWT (HS256) for auth
- STOMP-over-WebSocket for realtime updates
- Maven build, Docker / docker-compose for deployment

## Run with Docker

```bash
docker compose up --build
```

The API is then available at `http://localhost:8080`.

## Run locally (without Docker)

You need a local MongoDB on `mongodb://localhost:27017`.

```bash
mvn spring-boot:run
```

Override the JWT secret via the `PINGSDORF_JWT_SECRET` env var (must be at
least 32 bytes) before going to production.

## API surface

| Group       | Endpoints                                                                                              |
| ----------- | ------------------------------------------------------------------------------------------------------ |
| Auth        | `POST /auth/signup`, `POST /auth/login`, `POST /auth/logout`, `GET /auth/me`                           |
| Household   | `POST /household/invite`, `POST /household/join`, `PUT /household`, `POST /household/floor-plan`        |
| Pings       | `GET/POST /pings`, `PUT/DELETE /pings/{id}`, `POST /pings/{id}/cleanup`, `POST /pings/{id}/image`      |
| Rooms       | `GET/POST /rooms`, `PUT/DELETE /rooms/{id}`                                                            |
| Files       | `GET /files/{folder}/{filename}` (serves uploaded images)                                              |
| WebSocket   | `GET /ws` (SockJS), subscribe to `/topic/household.{householdId}`                                      |

Auth: send `Authorization: Bearer <jwt>` on all protected endpoints. The
JWT is returned by `/auth/signup` and `/auth/login`.

## Realtime events

All published as `{ "event": "...", "data": ... }` on
`/topic/household.{householdId}`:

- `ping:created`, `ping:updated`, `ping:deleted`, `ping:cleaned`
- `room:created`, `room:updated`, `room:deleted`
- `household:updated`

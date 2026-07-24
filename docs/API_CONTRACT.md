# IBMS API Contract

Base URL: `http://localhost:8080` (dev) / `https://api.ibms.puregoldgo.com` (prod)

## Authentication

Accounts are provisioned by a sysadmin and are never self-registered. Google SSO
has been removed.

All endpoints except `POST /auth/login`, `POST /auth/refresh`,
`POST /auth/password/change`, and `GET /health` require:
```
Authorization: Bearer <accessToken>
```

JWT payload: `{ sub: "<user-uuid>", role: "sysadmin|secretary|payables|finance|manager|pending", exp: <unix> }`

Role and status values are **lowercase** on the wire.

## Response Envelope

All API responses follow a unified envelope structure.

### Success Response

```json
{
  "result": "success",
  "message": "Process completed!",
  "status": "200",
  "data": {
    "token": "ada9a8sd6789a"
  }
}
```

### Error Response

```json
{
  "result": "error",
  "status": "500",
  "message": "Internal server error",
  "data": null
}
```

| Field     | Type             | Description                         |
|-----------|------------------|-------------------------------------|
| `result`  | `string`         | `"success"` or `"error"`            |
| `message` | `string`         | Human-readable status message        |
| `status`  | `string`         | HTTP status code as a string         |
| `data`    | `object \| null` | Response payload (`null` on error)   |

HTTP codes: 400 (validation), 401 (unauthenticated), 403 (forbidden), 404, 409 (conflict), 500.

## Pagination

List endpoints support cursor pagination:
```
GET /stores?cursor=<uuid>&limit=50
```
Response includes `nextCursor: string | null`.

## Idempotency

Money-mutating POSTs accept `Idempotency-Key: <uuid>` header. Duplicate keys return the original response.

---

## Endpoints

### Auth

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| POST | `/auth/login` | public | Exchange username + password for a session, or a change-password challenge |
| POST | `/auth/refresh` | public | Rotate a refresh token for a new session |
| POST | `/auth/password/change` | challenge token | Redeem a challenge, set a password, and receive a session |
| GET | `/auth/me` | any | Current user profile |
| POST | `/auth/password` | any | Self-service password rotation |
| POST | `/auth/logout` | any | Revoke the current session |
| POST | `/auth/logout-all` | any | Revoke every session for the caller |

#### POST /auth/login

Request: `{ "username": "...", "password": "..." }`

**Holding a temporary password is not being authenticated.** The response
discriminates on `outcome`:

- `authenticated` — `session` is populated, `passwordChange` is null.
- `password_change_required` — `session` is **null** and `passwordChange` carries
  a single-use challenge token. `user` is populated in *both* cases, so clients
  must branch on `session`, never on the presence of `user`.

```json
{
  "result": "success",
  "message": "Authentication successful!",
  "status": "200",
  "data": {
    "outcome": "authenticated",
    "user": {
      "id": "<uuid>",
      "username": "jdelacruz",
      "name": "Juan Dela Cruz",
      "employeeNumber": "EMP-0001",
      "role": "sysadmin",
      "status": "active",
      "mustChangePassword": false
    },
    "session": {
      "accessToken": "<jwt>",
      "refreshToken": "<opaque>",
      "tokenType": "Bearer",
      "expiresInSeconds": 900
    },
    "passwordChange": null
  }
}
```

Temporary-password variant:

```json
{
  "data": {
    "outcome": "password_change_required",
    "user": { "...": "as above, mustChangePassword: true" },
    "session": null,
    "passwordChange": {
      "challengeToken": "<single-use jwt>",
      "expiresInSeconds": 600,
      "reason": "temporary_password"
    }
  }
}
```

The challenge token authorizes exactly one call to `POST /auth/password/change`
and is rejected as a bearer token everywhere else. It must never be persisted.

#### POST /auth/password/change

Authorized by `Authorization: Bearer <challengeToken>`.
Request: `{ "newPassword": "..." }` → returns the same `LoginResponse` shape with
`outcome: "authenticated"` and a populated `session`.

Password policy (mirrored client-side, but the server is the authority):
12–72 characters, at least one uppercase, one lowercase, one digit, no
whitespace, and must not contain the username.

#### POST /auth/refresh

Request: `{ "refreshToken": "..." }` → `data` is a `session` object.
Rotation is unconditional: the old refresh token is invalidated, so two
concurrent refreshes will kill the session. Clients must single-flight this call.

#### Auth error responses

`DomainError.code` is **not** currently on the wire — clients can only branch on
HTTP status plus the message text.

| Situation | HTTP | Message |
|---|---|---|
| Wrong username or password | 401 | invalid credentials |
| Temporary password past its TTL | 401 | your temporary password has expired — ask a sysadmin to issue a new one |
| Too many failed attempts | 403 | account locked |
| Account deactivated | 403 | this account has been deactivated — contact a sysadmin |
| Weak or reused new password | 400 | password must … (renderable verbatim) |
| Challenge expired or invalid | 401 | — |
| Password already set | 409 | — |

---

### Users

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| GET | `/users/me` | any | Get current user |
| GET | `/users` | sysadmin | List all users |
| PATCH | `/users/{id}/role` | sysadmin | Update user role |

---

### Providers

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| GET | `/providers` | any | List providers |
| POST | `/providers` | sysadmin | Create provider |
| PUT | `/providers/{id}` | sysadmin | Update provider |

---

### Stores

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| GET | `/stores` | any | List stores |
| POST | `/stores` | secretary | Create store |
| PUT | `/stores/{id}` | secretary | Update store |
| POST | `/stores/{id}/deactivate` | secretary | Deactivate store |

---

### Accounts

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| GET | `/accounts` | any | List accounts (filter: storeId, providerId, status) |
| POST | `/accounts` | secretary | Create account |
| PUT | `/accounts/{id}` | secretary, payables | Update account |
| POST | `/accounts/{id}/deactivate` | secretary | Request deactivation (30-day grace) |

#### POST /accounts

Request:
```json
{
  "accountNumber": "5440-123",
  "providerId": "<uuid>",
  "storeId": "<uuid>",
  "rate": "1500.00",
  "installationDate": "2026-07-15",
  "circuitId": "CRT-987654321",
  "billingPeriodLabel": "1st - 30th",
  "planName": "100Mbps Broadband",
  "subscriptionProofIds": ["<attachment-uuid>"]
}
```

| Field | Required | Type | Description |
|-------|----------|------|-------------|
| `accountNumber` | yes | `string` | 3-50 alphanumeric characters, hyphens allowed |
| `providerId` | yes | `uuid` | Existing ISP provider |
| `storeId` | yes | `uuid` | Existing store/branch |
| `rate` | yes | `decimal-string` | Monthly recurring charge, e.g. `1500.00` |
| `installationDate` | yes | `ISO-date` | `YYYY-MM-DD` |
| `circuitId` | yes | `string` | Provider circuit identifier |
| `billingPeriodLabel` | no | `string` | Human-readable billing period, e.g. `1st - 30th` |
| `planName` | no | `string` | Package/plan name |
| `subscriptionProofIds` | yes | `string[]` | At least one attachment id; upload via `/attachments/presign/upload` first |

Response `201 Created`:
```json
{
  "result": "success",
  "message": "Account created",
  "status": "201",
  "data": {
    "id": "<uuid>",
    "accountNumber": "5440-123",
    "circuitId": "CRT-987654321",
    "providerId": "<uuid>",
    "storeId": "<uuid>",
    "planName": "100Mbps Broadband",
    "rate": "1500.00",
    "installationDate": "2026-07-15",
    "billingPeriodLabel": "1st - 30th",
    "isProrated": false,
    "status": "active",
    "subscriptionProofIds": ["<attachment-uuid>"],
    "createdAt": "2026-07-23T00:00:00Z",
    "updatedAt": "2026-07-23T00:00:00Z"
  }
}
```

Note: `isProrated` is not accepted on create. The backend derives it from `installationDate` and the topsheet processing date.

---

### Attachments

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| POST | `/attachments/presign/upload` | any | Get presigned upload URL |
| GET | `/attachments/{id}/presign/download` | any | Get presigned download URL |

Request (upload): `{ "fileName": "...", "contentType": "application/pdf" }`

Response:
```json
{
  "result": "success",
  "message": "Presigned URL generated!",
  "status": "200",
  "data": {
    "url": "https://storage...",
    "attachmentId": "..."
  }
}
```

---

### Topsheets

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| GET | `/topsheets` | any | List topsheets (filter: providerId, status) |
| POST | `/topsheets/preview` | secretary | Preview compilation (no persist) |
| POST | `/topsheets/compile` | secretary | Compile topsheet (atomic) |
| POST | `/topsheets/{id}/approve` | finance | Approve topsheet |
| POST | `/topsheets/{id}/pay` | finance | Mark topsheet as paid |
| GET | `/topsheets/{id}/details` | any | Get topsheet line details |

Request (preview/compile): `{ "providerId": "...", "period": "2026-08" }`
Headers: `Idempotency-Key: <uuid>` (compile only)

---

### Transfers

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| GET | `/transfers` | any | List transfers (filter: accountId) |
| POST | `/transfers` | secretary | Create transfer |

---

### OCR

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| POST | `/ocr/extract` | secretary | Trigger OCR extraction on a batch |
| GET | `/ocr/batches` | secretary | List OCR batches |
| GET | `/ocr/batches/{id}/rows` | secretary | Get extracted rows for a batch |
| GET | `/ocr/templates` | sysadmin, secretary | List OCR templates |
| POST | `/ocr/templates` | sysadmin | Create OCR template |
| PUT | `/ocr/templates/{id}` | sysadmin | Update OCR template |

---

### Activities

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| GET | `/activities` | any | List activities (filter: entityId, limit) |

---

### Exports

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| GET | `/exports/topsheet/{id}.xlsx` | secretary, finance | Download topsheet Excel export |

---

## Role Matrix Summary

| Endpoint Group | Secretary | Finance | Payables | SysAdmin |
|----------------|-----------|---------|----------|----------|
| Auth | x | x | x | x |
| Users (me) | x | x | x | x |
| Users (manage) | - | - | - | x |
| Providers (read) | x | x | x | x |
| Providers (write) | - | - | - | x |
| Stores | x | - | - | - |
| Accounts | x | - | x | - |
| Attachments | x | x | x | x |
| Topsheets (compile) | x | - | - | - |
| Topsheets (approve/pay) | - | x | - | - |
| Topsheets (read) | x | x | x | x |
| Transfers | x | - | - | - |
| OCR (extract/batches) | x | - | - | - |
| OCR (templates) | - | - | - | x |
| Activities | x | x | x | x |
| Exports | x | x | - | - |

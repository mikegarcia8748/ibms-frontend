# IBMS API Contract

Base URL: `http://localhost:8080` (dev) / `https://api.ibms.puregoldgo.com` (prod)

## Authentication

All endpoints (except `POST /auth/google` and `GET /health`) require:
```
Authorization: Bearer <jwt>
```

JWT payload: `{ sub: "<user-uuid>", role: "secretary|finance|payables|sysadmin", exp: <unix> }`

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
| POST | `/auth/google` | public | Exchange Google ID token for backend JWT |

Request: `{ "idToken": "..." }`

Response:
```json
{
  "result": "success",
  "message": "Authentication successful!",
  "status": "200",
  "data": {
    "token": "<jwt>",
    "user": { "id": "<uuid>", "email": "...", "role": "secretary|finance|payables|sysadmin" }
  }
}
```

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
| POST | `/accounts` | secretary, payables | Create account |
| PUT | `/accounts/{id}` | secretary, payables | Update account |
| POST | `/accounts/{id}/deactivate` | secretary | Request deactivation (30-day grace) |

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

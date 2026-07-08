# 04 - API Contract

## 1. Purpose

This document defines the initial REST API contract for the **SRM Credit Engine**.

The API must support:

* exchange-rate management;
* pricing simulation;
* settlement batch creation;
* settlement detail retrieval;
* settlement statement queries;
* supporting reference-data lookup;
* structured validation and error responses.

The backend implementation and Angular frontend integration must follow this contract.

If the API behavior changes during implementation, this file must be updated before or alongside the code change.

## 2. API Design Principles

The API must follow these principles:

1. Use RESTful resource-oriented paths.
2. Use semantic HTTP methods.
3. Use semantic HTTP status codes.
4. Validate input at the backend boundary.
5. Return structured error responses.
6. Keep financial calculation logic in the backend.
7. Represent monetary and rate values as decimal values.
8. Use server-side pagination for list and statement endpoints.
9. Preserve auditability in settlement responses.
10. Document endpoints through OpenAPI/Swagger.

## 3. Base URL

Local backend base URL:

```text
http://localhost:8080/api
```

Docker Compose frontend-to-backend browser URL:

```text
http://localhost:8080/api
```

Internal Docker service URL, when needed:

```text
http://backend:8080/api
```

## 4. Content Type

All JSON endpoints must use:

```http
Content-Type: application/json
Accept: application/json
```

## 5. Date and Time Format

### Date-only fields

Use ISO 8601 date format:

```text
YYYY-MM-DD
```

Example:

```json
"dueDate": "2026-09-07"
```

### Date-time fields

Use ISO 8601 date-time format.

Backend persistence should use UTC.

Example:

```json
"calculatedAt": "2026-07-07T13:45:30Z"
```

## 6. Decimal Representation

Financial values must be represented as JSON numbers and mapped to `BigDecimal` in the backend.

Examples:

```json
{
  "faceValue": 10000.00,
  "baseRate": 0.01000000,
  "spread": 0.01500000,
  "exchangeRate": 5.25000000
}
```

Rules:

* API DTOs must use `BigDecimal` for financial numeric fields.
* Do not use floating-point types in backend DTOs or domain calculation.
* Percentages must be sent as decimal rates.
* `1.5%` must be represented as `0.01500000`.
* `2.5%` must be represented as `0.02500000`.

## 7. Currency Representation

Currencies are represented by uppercase three-letter codes.

Initial supported values:

```text
BRL
USD
```

Examples:

```json
{
  "sourceCurrency": "BRL",
  "paymentCurrency": "USD"
}
```

Unsupported currencies must be rejected.

## 8. Receivable Type Representation

Initial supported receivable types:

```text
MERCANTILE_DUPLICATE
POST_DATED_CHECK
```

Examples:

```json
{
  "receivableType": "MERCANTILE_DUPLICATE"
}
```

Unsupported receivable types must be rejected.

## 9. Common HTTP Status Codes

|                      Status | Usage                                                 |
| --------------------------: | ----------------------------------------------------- |
|                    `200 OK` | Successful read, lookup or simulation                 |
|               `201 Created` | Successful resource creation                          |
|            `204 No Content` | Successful operation with no response body, if needed |
|           `400 Bad Request` | Malformed request or syntactic validation error       |
|             `404 Not Found` | Requested resource does not exist                     |
|              `409 Conflict` | Business conflict, such as duplicate settlement       |
|  `422 Unprocessable Entity` | Valid request shape, but invalid business rule        |
| `500 Internal Server Error` | Unexpected controlled failure                         |

## 10. Common Error Response

All API errors should follow a structured response format.

### Response body

```json
{
  "timestamp": "2026-07-07T13:45:30Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Missing exchange rate for currency pair BRL -> USD.",
  "path": "/api/pricing/simulations",
  "details": [
    {
      "field": "paymentCurrency",
      "message": "Exchange rate BRL -> USD is required for cross-currency operation."
    }
  ]
}
```

### Field definitions

| Field       | Type   | Required | Description                             |
| ----------- | ------ | -------: | --------------------------------------- |
| `timestamp` | string |      yes | Error timestamp in ISO 8601 format      |
| `status`    | number |      yes | HTTP status code                        |
| `error`     | string |      yes | HTTP error reason                       |
| `message`   | string |      yes | Human-readable error message            |
| `path`      | string |      yes | Request path                            |
| `details`   | array  |       no | Field-level or contextual error details |

### Error detail object

```json
{
  "field": "faceValue",
  "message": "Face value must be greater than zero."
}
```

For non-field business errors, `field` may be omitted or set to a contextual value.

## 11. Pagination Contract

Paginated endpoints must accept:

| Query param | Type    | Required |           Default | Description              |
| ----------- | ------- | -------: | ----------------: | ------------------------ |
| `page`      | integer |       no |               `0` | Zero-based page index    |
| `size`      | integer |       no |              `20` | Page size                |
| `sort`      | string  |       no | endpoint-specific | Sort field and direction |

Rules:

* `page` must be greater than or equal to `0`.
* `size` must be greater than `0`.
* maximum `size` is `100`.
* invalid pagination parameters must return `400 Bad Request`.

### Generic page response

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

## 12. Reference Data API

Reference-data endpoints support the frontend in rendering valid options.

These endpoints are not the main business focus, but they reduce frontend hardcoding.

---

# 12.1 List Supported Currencies

```http
GET /api/reference-data/currencies
```

## Description

Returns supported currencies.

## Response - `200 OK`

```json
{
  "currencies": [
    {
      "code": "BRL",
      "name": "Brazilian Real",
      "decimalPlaces": 2
    },
    {
      "code": "USD",
      "name": "US Dollar",
      "decimalPlaces": 2
    }
  ]
}
```

## Response fields

| Field           | Type   | Description                     |
| --------------- | ------ | ------------------------------- |
| `code`          | string | Currency code                   |
| `name`          | string | Currency name                   |
| `decimalPlaces` | number | Standard display decimal places |

---

# 12.2 List Receivable Types

```http
GET /api/reference-data/receivable-types
```

## Description

Returns supported receivable types and reference spreads.

## Response - `200 OK`

```json
{
  "receivableTypes": [
    {
      "code": "MERCANTILE_DUPLICATE",
      "description": "Mercantile Duplicate",
      "monthlySpread": 0.01500000
    },
    {
      "code": "POST_DATED_CHECK",
      "description": "Post-Dated Check",
      "monthlySpread": 0.02500000
    }
  ]
}
```

## Response fields

| Field           | Type    | Description                |
| --------------- | ------- | -------------------------- |
| `code`          | string  | Receivable type code       |
| `description`   | string  | Human-readable description |
| `monthlySpread` | decimal | Monthly risk spread        |

## Business rules

* Values must match seeded reference data.
* Spreads must be represented as decimal rates.
* This endpoint is read-only in the initial version.

## 13. Exchange Rate API

The Exchange Rate API manages currency conversion rates.

Base path:

```http
/api/exchange-rates
```

---

# 13.1 Create Exchange Rate

```http
POST /api/exchange-rates
```

## Description

Creates a manual exchange rate for a currency pair.

## Request body

```json
{
  "sourceCurrency": "USD",
  "targetCurrency": "BRL",
  "rate": 5.25000000,
  "validAt": "2026-07-07T13:00:00Z"
}
```

## Request fields

| Field            | Type    | Required | Validation                                |
| ---------------- | ------- | -------: | ----------------------------------------- |
| `sourceCurrency` | string  |      yes | Supported currency                        |
| `targetCurrency` | string  |      yes | Supported currency, different from source |
| `rate`           | decimal |      yes | Greater than zero                         |
| `validAt`        | string  |      yes | ISO 8601 date-time                        |

## Response - `201 Created`

```json
{
  "id": "9d2c4e9e-15d9-4c77-9d26-48f87dd4fa01",
  "sourceCurrency": "USD",
  "targetCurrency": "BRL",
  "rate": 5.25000000,
  "validAt": "2026-07-07T13:00:00Z",
  "createdAt": "2026-07-07T13:05:00Z"
}
```

## Response headers

```http
Location: /api/exchange-rates/9d2c4e9e-15d9-4c77-9d26-48f87dd4fa01
```

## Validation errors

### Source and target currencies are equal

```json
{
  "timestamp": "2026-07-07T13:05:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Source currency and target currency must be different.",
  "path": "/api/exchange-rates",
  "details": [
    {
      "field": "targetCurrency",
      "message": "Target currency must be different from source currency."
    }
  ]
}
```

### Rate is not positive

Expected status:

```http
400 Bad Request
```

## Business rules

* Exchange-rate direction is explicit.
* `USD -> BRL` is not the same pair as `BRL -> USD`.
* The API must not silently invert exchange rates.
* Exchange rates must not be hardcoded in pricing logic.

---

# 13.2 Get Latest Exchange Rate

```http
GET /api/exchange-rates/latest-sourceCurrency=USD&targetCurrency=BRL
```

## Description

Returns the latest available exchange rate for a currency pair.

## Query parameters

| Parameter        | Type   | Required | Description          |
| ---------------- | ------ | -------: | -------------------- |
| `sourceCurrency` | string |      yes | Source currency code |
| `targetCurrency` | string |      yes | Target currency code |

## Response - `200 OK`

```json
{
  "id": "9d2c4e9e-15d9-4c77-9d26-48f87dd4fa01",
  "sourceCurrency": "USD",
  "targetCurrency": "BRL",
  "rate": 5.25000000,
  "validAt": "2026-07-07T13:00:00Z",
  "createdAt": "2026-07-07T13:05:00Z"
}
```

## Missing rate response - `404 Not Found`

```json
{
  "timestamp": "2026-07-07T13:10:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "No exchange rate found for currency pair USD -> BRL.",
  "path": "/api/exchange-rates/latest",
  "details": []
}
```

## Business rules

* Lookup must match exact direction.
* Same-currency lookup is not required by pricing or settlement.
* Latest rate is selected by `validAt` descending, with deterministic tie-breaking by creation timestamp or identifier.
* Direct lookup through this endpoint returns `404 Not Found` when the exact pair does not exist.
* Pricing and settlement flows that depend on this lookup must translate a missing required pair into `422 Unprocessable Entity`.

## 14. Pricing Simulation API

The Pricing Simulation API calculates receivable pricing without creating settlement records.

Base path:

```http
/api/pricing
```

---

# 14.1 Simulate Receivable Pricing

```http
POST /api/pricing/simulations
```

## Description

Simulates pricing for a single receivable.

This endpoint does not persist a settlement.

It must use the same domain pricing logic used by settlement creation.

## Request body

```json
{
  "faceValue": 10000.00,
  "sourceCurrency": "BRL",
  "paymentCurrency": "USD",
  "baseRate": 0.01000000,
  "receivableType": "MERCANTILE_DUPLICATE",
  "dueDate": "2026-09-07"
}
```

## Request fields

| Field             | Type    | Required | Validation                    |
| ----------------- | ------- | -------: | ----------------------------- |
| `faceValue`       | decimal |      yes | Greater than zero             |
| `sourceCurrency`  | string  |      yes | Supported currency            |
| `paymentCurrency` | string  |      yes | Supported currency            |
| `baseRate`        | decimal |       no | Greater than or equal to zero when provided; otherwise resolved from `DEFAULT_BASE_RATE` |
| `receivableType`  | string  |      yes | Supported receivable type     |
| `dueDate`         | string  |      yes | Future date                   |

## Response - `200 OK`

```json
{
  "faceValue": 10000.00,
  "sourceCurrency": "BRL",
  "paymentCurrency": "USD",
  "presentValueInSourceCurrency": 9509.18,
  "netPaymentValue": 1811.27,
  "discountValue": 490.82,
  "baseRate": 0.01000000,
  "spread": 0.01500000,
  "termInMonths": 2.06666667,
  "exchangeRate": 5.25000000,
  "calculatedAt": "2026-07-07T13:20:00Z"
}
```

## Response fields

| Field                          | Type         | Description                                        |
| ------------------------------ | ------------ | -------------------------------------------------- |
| `faceValue`                    | decimal      | Original receivable value                          |
| `sourceCurrency`               | string       | Receivable currency                                |
| `paymentCurrency`              | string       | Settlement/payment currency                        |
| `presentValueInSourceCurrency` | decimal      | Present value before FX conversion                 |
| `netPaymentValue`              | decimal      | Final value in payment currency                    |
| `discountValue`                | decimal      | Face value minus present value, in source currency |
| `baseRate`                     | decimal      | Monthly base rate used                             |
| `spread`                       | decimal      | Monthly risk spread used                           |
| `termInMonths`                 | decimal      | Term used in formula                               |
| `exchangeRate`                 | decimal/null | Rate used when cross-currency                      |
| `calculatedAt`                 | string       | Calculation timestamp                              |

## Same-currency response example

```json
{
  "faceValue": 10000.00,
  "sourceCurrency": "BRL",
  "paymentCurrency": "BRL",
  "presentValueInSourceCurrency": 9509.18,
  "netPaymentValue": 9509.18,
  "discountValue": 490.82,
  "baseRate": 0.01000000,
  "spread": 0.01500000,
  "termInMonths": 2.06666667,
  "exchangeRate": null,
  "calculatedAt": "2026-07-07T13:20:00Z"
}
```

## Business errors

### Missing exchange rate - `422 Unprocessable Entity`

```json
{
  "timestamp": "2026-07-07T13:20:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Missing exchange rate for currency pair BRL -> USD.",
  "path": "/api/pricing/simulations",
  "details": [
    {
      "field": "paymentCurrency",
      "message": "Exchange rate BRL -> USD is required for cross-currency operation."
    }
  ]
}
```

### Past due date - `422 Unprocessable Entity`

```json
{
  "timestamp": "2026-07-07T13:20:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Receivable due date must be in the future.",
  "path": "/api/pricing/simulations",
  "details": [
    {
      "field": "dueDate",
      "message": "Due date must be after pricing date."
    }
  ]
}
```

## Business rules

* Simulation is non-persistent.
* Backend is the official calculation source.
* Strategy Pattern must resolve the spread by receivable type.
* Effective base rate resolution order is: request `baseRate`, then server-side `DEFAULT_BASE_RATE`, otherwise fail.
* Present value must be calculated in source currency first.
* Cross-currency conversion must happen after present value calculation.
* Same-currency simulation must not require exchange-rate lookup.
* Missing exchange rate must fail the simulation.

## 15. Settlement API

The Settlement API creates and retrieves auditable settlement records.

Base path:

```http
/api/settlements
```

## 15.1 Settlement Creation Design

The initial API supports creating a settlement with inline receivable data.

Rationale:

* the challenge requires receiving a batch of receivables;
* the frontend can submit receivable data directly;
* the backend can persist receivables and settlement items atomically;
* this avoids requiring a separate receivable-management UI for the initial challenge.

The backend must still persist receivables as records because the data model includes them and duplicate settlement prevention depends on receivable identity.

## 15.2 Create Settlement Batch

```http
POST /api/settlements
```

## Description

Creates an auditable settlement for a batch of receivables.

The operation must be atomic.

If any item is invalid, no settlement or settlement item may be persisted.

## Request body

```json
{
  "assignor": {
    "name": "ACME Comercio Ltda.",
    "document": "12345678000199"
  },
  "paymentCurrency": "USD",
  "baseRate": 0.01000000,
  "receivables": [
    {
      "externalReference": "NF-1001",
      "faceValue": 10000.00,
      "sourceCurrency": "BRL",
      "receivableType": "MERCANTILE_DUPLICATE",
      "dueDate": "2026-09-07"
    },
    {
      "externalReference": "CHK-2001",
      "faceValue": 5000.00,
      "sourceCurrency": "BRL",
      "receivableType": "POST_DATED_CHECK",
      "dueDate": "2026-10-15"
    }
  ]
}
```

## Request fields

### Root fields

| Field             | Type    | Required | Validation                    |
| ----------------- | ------- | -------: | ----------------------------- |
| `assignor`        | object  |      yes | Required                      |
| `paymentCurrency` | string  |      yes | Supported currency            |
| `baseRate`        | decimal |       no | Greater than or equal to zero when provided; otherwise resolved from `DEFAULT_BASE_RATE` |
| `receivables`     | array   |      yes | 1 to 100 items                |

### Assignor fields

| Field      | Type   | Required | Validation                |
| ---------- | ------ | -------: | ------------------------- |
| `name`     | string |      yes | Not blank, max length 255 |
| `document` | string |       no | Max length 32             |

### Receivable item fields

| Field               | Type    | Required | Validation                                           |
| ------------------- | ------- | -------: | ---------------------------------------------------- |
| `externalReference` | string  |      yes | Not blank, unique per assignor in the initial design |
| `faceValue`         | decimal |      yes | Greater than zero                                    |
| `sourceCurrency`    | string  |      yes | Supported currency                                   |
| `receivableType`    | string  |      yes | Supported receivable type                            |
| `dueDate`           | string  |      yes | Future date                                          |

## Response - `201 Created`

```json
{
  "id": "7b4b65ab-c30d-47f8-8356-0f0c2ab2e2df",
  "assignor": {
    "id": "5b66f1d1-906e-42cc-bdd2-f7d9d8a08389",
    "name": "ACME Comercio Ltda.",
    "document": "12345678000199"
  },
  "sourceCurrency": "BRL",
  "paymentCurrency": "USD",
  "status": "SETTLED",
  "baseRate": 0.01000000,
  "itemCount": 2,
  "totalFaceValue": 15000.00,
  "totalPresentValue": 13980.15,
  "totalPaymentValue": 2662.89,
  "settledAt": "2026-07-07T13:30:00Z",
  "items": [
    {
      "id": "ff5579e1-b0a9-4df2-974e-82f2d598c003",
      "receivableId": "fa4b2781-c6c1-4041-b564-d75932e421c0",
      "externalReference": "NF-1001",
      "receivableType": "MERCANTILE_DUPLICATE",
      "faceValue": 10000.00,
      "sourceCurrency": "BRL",
      "paymentCurrency": "USD",
      "baseRate": 0.01000000,
      "spread": 0.01500000,
      "termInMonths": 2.06666667,
      "presentValueInSourceCurrency": 9509.18,
      "discountValue": 490.82,
      "paymentValue": 1811.27,
      "exchangeRate": 5.25000000,
      "calculatedAt": "2026-07-07T13:30:00Z"
    },
    {
      "id": "e0b10fd4-7f69-44c5-95fa-1db3488b7f2f",
      "receivableId": "9d373783-f06a-4464-ae85-32be0c161928",
      "externalReference": "CHK-2001",
      "receivableType": "POST_DATED_CHECK",
      "faceValue": 5000.00,
      "sourceCurrency": "BRL",
      "paymentCurrency": "USD",
      "baseRate": 0.01000000,
      "spread": 0.02500000,
      "termInMonths": 3.33333333,
      "presentValueInSourceCurrency": 4470.97,
      "discountValue": 529.03,
      "paymentValue": 851.61,
      "exchangeRate": 5.25000000,
      "calculatedAt": "2026-07-07T13:30:00Z"
    }
  ]
}
```

## Response headers

```http
Location: /api/settlements/7b4b65ab-c30d-47f8-8356-0f0c2ab2e2df
```

## Business errors

### Empty batch - `400 Bad Request`

```json
{
  "timestamp": "2026-07-07T13:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Settlement batch must contain at least one receivable.",
  "path": "/api/settlements",
  "details": [
    {
      "field": "receivables",
      "message": "Receivables list must contain between 1 and 100 items."
    }
  ]
}
```

### Duplicate receivable - `409 Conflict`

```json
{
  "timestamp": "2026-07-07T13:30:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Receivable NF-1001 for assignor 12345678000199 has already been settled.",
  "path": "/api/settlements",
  "details": [
    {
      "field": "receivables[0].externalReference",
      "message": "This receivable has already been settled."
    }
  ]
}
```

### Missing exchange rate - `422 Unprocessable Entity`

```json
{
  "timestamp": "2026-07-07T13:30:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Missing exchange rate for currency pair BRL -> USD.",
  "path": "/api/settlements",
  "details": [
    {
      "field": "paymentCurrency",
      "message": "Exchange rate BRL -> USD is required for cross-currency settlement."
    }
  ]
}
```

### Mixed source currency batch - `422 Unprocessable Entity`

```json
{
  "timestamp": "2026-07-07T13:30:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "All receivables in the settlement batch must share the same source currency.",
  "path": "/api/settlements",
  "details": [
    {
      "field": "receivables",
      "message": "Mixed source currencies are not allowed in the same settlement batch."
    }
  ]
}
```

## Business rules

* Settlement request must contain a batch.
* Batch size must be between 1 and 100.
* All receivables in the batch must share one source currency.
* Settlement creation must be atomic.
* No partial settlement may be persisted.
* Every item must persist calculation snapshot.
* Duplicate settlement must be prevented.
* Effective base rate resolution order is: request `baseRate`, then server-side `DEFAULT_BASE_RATE`, otherwise fail.
* Cross-currency settlement requires exchange-rate snapshot.
* Same-currency settlement does not require exchange rate.
* Historical settlement values must not be recalculated from current rates.

---

# 15.3 Get Settlement by ID

```http
GET /api/settlements/{id}
```

## Description

Returns full settlement details, including calculation snapshots.

## Path parameters

| Parameter | Type | Required | Description           |
| --------- | ---- | -------: | --------------------- |
| `id`      | UUID |      yes | Settlement identifier |

## Response - `200 OK`

```json
{
  "id": "7b4b65ab-c30d-47f8-8356-0f0c2ab2e2df",
  "assignor": {
    "id": "5b66f1d1-906e-42cc-bdd2-f7d9d8a08389",
    "name": "ACME Comercio Ltda.",
    "document": "12345678000199"
  },
  "sourceCurrency": "BRL",
  "paymentCurrency": "USD",
  "status": "SETTLED",
  "baseRate": 0.01000000,
  "itemCount": 2,
  "totalFaceValue": 15000.00,
  "totalPresentValue": 13980.15,
  "totalPaymentValue": 2662.89,
  "settledAt": "2026-07-07T13:30:00Z",
  "items": [
    {
      "id": "ff5579e1-b0a9-4df2-974e-82f2d598c003",
      "receivableId": "fa4b2781-c6c1-4041-b564-d75932e421c0",
      "externalReference": "NF-1001",
      "receivableType": "MERCANTILE_DUPLICATE",
      "faceValue": 10000.00,
      "sourceCurrency": "BRL",
      "paymentCurrency": "USD",
      "baseRate": 0.01000000,
      "spread": 0.01500000,
      "termInMonths": 2.06666667,
      "presentValueInSourceCurrency": 9509.18,
      "discountValue": 490.82,
      "paymentValue": 1811.27,
      "exchangeRate": 5.25000000,
      "calculatedAt": "2026-07-07T13:30:00Z"
    }
  ]
}
```

## Not found response - `404 Not Found`

```json
{
  "timestamp": "2026-07-07T13:35:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Settlement not found.",
  "path": "/api/settlements/7b4b65ab-c30d-47f8-8356-0f0c2ab2e2df",
  "details": []
}
```

## Business rules

* Response must use persisted settlement values.
* Response must not recalculate historical values from current exchange rates.
* Response must include enough information for audit review.

---

# 15.4 Get Settlement Statement

```http
GET /api/settlements/statement
```

## Description

Returns a paginated analytical statement of settlements.

This endpoint is optimized for historical querying and must use database-level filtering.

## Query parameters

| Parameter          | Type    | Required | Description              |
| ------------------ | ------- | -------: | ------------------------ |
| `from`             | date    |       no | Start date, inclusive    |
| `to`               | date    |       no | End date, inclusive      |
| `assignorId`       | UUID    |       no | Assignor filter          |
| `assignorDocument` | string  |       no | Assignor document filter |
| `paymentCurrency`  | string  |       no | Payment currency filter  |
| `sourceCurrency`   | string  |       no | Source currency filter   |
| `receivableType`   | string  |       no | Receivable type filter   |
| `status`           | string  |       no | Settlement status filter |
| `page`             | integer |       no | Default `0`              |
| `size`             | integer |       no | Default `20`, max `100`  |
| `sort`             | string  |       no | Default `settledAt,desc` |

## Example request

```http
GET /api/settlements/statement-from=2026-07-01&to=2026-07-31&paymentCurrency=USD&receivableType=MERCANTILE_DUPLICATE&page=0&size=20
```

## Response - `200 OK`

```json
{
  "content": [
    {
      "settlementId": "7b4b65ab-c30d-47f8-8356-0f0c2ab2e2df",
      "assignorId": "5b66f1d1-906e-42cc-bdd2-f7d9d8a08389",
      "assignorName": "ACME Comercio Ltda.",
      "assignorDocument": "12345678000199",
      "paymentCurrency": "USD",
      "status": "SETTLED",
      "itemCount": 2,
      "totalFaceValue": 15000.00,
      "totalPresentValue": 13980.15,
      "totalPaymentValue": 2662.89,
      "settledAt": "2026-07-07T13:30:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

## Statement row fields

| Field               | Type        | Description                 |
| ------------------- | ----------- | --------------------------- |
| `settlementId`      | UUID        | Settlement identifier       |
| `assignorId`        | UUID        | Assignor identifier         |
| `assignorName`      | string      | Assignor name               |
| `assignorDocument`  | string/null | Assignor document           |
| `paymentCurrency`   | string      | Settlement payment currency |
| `status`            | string      | Settlement status           |
| `itemCount`         | number      | Number of settlement items  |
| `totalFaceValue`    | decimal     | Aggregated face value       |
| `totalPresentValue` | decimal     | Aggregated present value    |
| `totalPaymentValue` | decimal     | Aggregated payment value    |
| `settledAt`         | string      | Settlement timestamp        |

## Invalid date range - `400 Bad Request`

```json
{
  "timestamp": "2026-07-07T13:40:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid statement date range.",
  "path": "/api/settlements/statement",
  "details": [
    {
      "field": "from",
      "message": "From date must be less than or equal to to date."
    }
  ]
}
```

## Business rules

* Filtering must happen at database level.
* Pagination must happen at database level.
* Default sort is `settledAt` descending.
* Frontend must not paginate locally after loading all records.
* Statement must use persisted settlement values.
* Endpoint may use projections, query builders or native SQL for performance.

## 16. Optional Receivable API

The initial settlement endpoint supports inline receivable creation.

A separate Receivable API is optional for the initial challenge.

If implemented, it must follow this contract.

Base path:

```http
/api/receivables
```

---

# 16.1 Create Receivable

```http
POST /api/receivables
```

## Request body

```json
{
  "assignor": {
    "name": "ACME Comercio Ltda.",
    "document": "12345678000199"
  },
  "externalReference": "NF-1001",
  "faceValue": 10000.00,
  "sourceCurrency": "BRL",
  "receivableType": "MERCANTILE_DUPLICATE",
  "dueDate": "2026-09-07"
}
```

## Response - `201 Created`

```json
{
  "id": "fa4b2781-c6c1-4041-b564-d75932e421c0",
  "assignor": {
    "id": "5b66f1d1-906e-42cc-bdd2-f7d9d8a08389",
    "name": "ACME Comercio Ltda.",
    "document": "12345678000199"
  },
  "externalReference": "NF-1001",
  "faceValue": 10000.00,
  "sourceCurrency": "BRL",
  "receivableType": "MERCANTILE_DUPLICATE",
  "dueDate": "2026-09-07",
  "status": "AVAILABLE",
  "createdAt": "2026-07-07T13:15:00Z"
}
```

## Implementation note

This endpoint is optional. If omitted, settlement creation must still persist receivables internally from inline settlement request items.

## 17. Health API

A simple health endpoint may be exposed through Spring Boot Actuator or a custom endpoint.

Preferred approach:

```http
GET /actuator/health
```

## Response - `200 OK`

```json
{
  "status": "UP"
}
```

This endpoint is useful for Docker health checks and local validation.

## 18. OpenAPI / Swagger

The backend must expose OpenAPI documentation.

Expected local URLs:

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
```

or equivalent Springdoc paths.

OpenAPI documentation must include:

* endpoint descriptions;
* request examples;
* response examples where useful;
* validation status codes;
* business error status codes;
* DTO schemas.

## 19. CORS

For local development, backend must allow the Angular frontend origin:

```text
http://localhost:4200
```

Allowed origins must be configurable through environment variables.

Final configuration must avoid unrestricted wildcard CORS unless explicitly justified for local-only development.

## 20. Validation Summary

## 20.1 Common validation rules

| Field             | Rule                                                |
| ----------------- | --------------------------------------------------- |
| `faceValue`       | Required, greater than zero                         |
| `baseRate`        | Optional; if omitted, resolved from `DEFAULT_BASE_RATE`; effective value must be greater than or equal to zero |
| `rate`            | Required, greater than zero                         |
| `sourceCurrency`  | Required, supported currency                        |
| `targetCurrency`  | Required, supported currency, different from source |
| `paymentCurrency` | Required, supported currency                        |
| `receivableType`  | Required, supported type                            |
| `dueDate`         | Required, future date                               |
| `receivables`     | Required, 1 to 100 items                            |
| `page`            | Greater than or equal to zero                       |
| `size`            | Between 1 and 100                                   |
| `from` / `to`     | Valid date range                                    |

## 20.2 Business validation rules

| Scenario                                           | Expected status |
| -------------------------------------------------- | --------------: |
| Missing exchange rate for cross-currency operation |           `422` |
| Unsupported receivable type                        |           `422` |
| Past due date                                      |           `422` |
| Duplicate settlement                               |           `409` |
| Settlement not found                               |           `404` |
| Invalid date range                                 |           `400` |
| Invalid pagination                                 |           `400` |

## 21. API Security Scope

Authentication and authorization are out of scope for the initial technical challenge.

However:

* backend validation is mandatory;
* no stack traces should be exposed;
* no secrets should appear in responses;
* error messages should be useful but not leak internal implementation details;
* CORS must be explicit for local frontend integration.

## 22. Frontend Integration Rules

The Angular frontend must use this API contract.

Rules:

* pricing simulation screen calls `POST /api/pricing/simulations`;
* settlement statement grid calls `GET /api/settlements/statement`;
* exchange-rate form calls `POST /api/exchange-rates`;
* frontend must use server-side pagination;
* frontend must not implement official pricing formula;
* frontend must display structured backend errors.

## 23. Contract Change Policy

When this API contract changes, update:

```text
docs/specs/04-api-contract.md
docs/specs/07-testing-strategy.md
docs/specs/08-acceptance-criteria.md
docs/adr/* when relevant
frontend API models
backend DTOs
OpenAPI annotations
AI prompts that reference the changed endpoint
```

## 24. Minimum API Delivery Checklist

The initial delivery must implement at least:

```text
POST /api/exchange-rates
GET  /api/exchange-rates/latest
GET  /api/reference-data/currencies
GET  /api/reference-data/receivable-types
POST /api/pricing/simulations
POST /api/settlements
GET  /api/settlements/{id}
GET  /api/settlements/statement
```

Optional:

```text
POST /api/receivables
GET  /actuator/health
```

## 25. Related Documents

```text
README.md
AGENTS.md
docs/specs/01-product-brief.md
docs/specs/02-domain-glossary.md
docs/specs/03-business-rules.md
docs/specs/05-data-model.md
docs/specs/06-architecture.md
docs/specs/07-testing-strategy.md
docs/specs/08-acceptance-criteria.md
docs/adr/ADR-001-backend-stack.md
docs/adr/ADR-003-money-precision.md
docs/adr/ADR-004-architecture-style.md
docs/adr/ADR-005-frontend-stack.md
```

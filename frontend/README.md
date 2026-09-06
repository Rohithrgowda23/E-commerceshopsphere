# frontend

React + Vite single-page app for the e-commerce platform. Talks to the
backend exclusively through the **API Gateway** (`http://localhost:8080`)
— never directly to an individual microservice.

## Stack

React 18, Vite, React Router 6, Axios, Context API (Auth/Cart/Wishlist),
Tailwind CSS.

## Run

```bash
npm install
cp .env.example .env   # adjust VITE_API_BASE_URL if needed
npm run dev
```

Runs on **http://localhost:5173**. Requires the API Gateway (and, in turn,
the backend services it routes to) to be running for anything beyond the
static shell to work.

## Auth flow

- `AuthContext` stores `accessToken`/`refreshToken` in `localStorage` and
  the decoded user (`userId`, `email`, `roles`) alongside them.
- `axiosClient` (in `src/api/axiosClient.js`) attaches
  `Authorization: Bearer <accessToken>` to every request, and on a `401`
  automatically calls `POST /api/auth/refresh` once, retries the original
  request, and queues any other requests that 401'd while the refresh was
  in flight — so a burst of concurrent requests doesn't trigger multiple
  refresh calls.
- A `403` is never retried — it's a genuine authorization failure, not an
  expired token.
- `ProtectedRoute` guards any authenticated-only page; `AdminRoute`
  additionally requires the `ADMIN` role. Both redirect to `/login`
  (preserving the original destination) or `/` respectively.

## Pages

Home, Login, Register, Products, Product Details, Search Results, Category
Products, Cart, Checkout, Payment (polls payment status — see below), Order
Success, Orders, Order Details, Profile, Addresses, Wishlist, and an Admin
section (Dashboard, Products, Orders, Inventory).

## Notable design choices / known backend gaps

- **Payment page polls, it doesn't submit.** Checkout only collects a
  shipping address — order-service pulls items from the cart itself, and
  payment is triggered automatically by payment-service consuming
  `ORDER_CREATED` from Kafka. The `/payment/:orderId` page polls
  `GET /api/payments/:orderId` every 2s until it sees `SUCCESS`/`FAILED`
  (or times out after ~1 minute) rather than calling a "pay" endpoint
  itself.
- **Wishlist has no backend.** There's no wishlist microservice in this
  architecture, so `WishlistContext` persists product ids to
  `localStorage`, keyed per user id. It's structured so swapping in a real
  API later only touches that one file.
- **Admin "Orders" shows the admin's own orders, not all orders.**
  order-service's `GET /api/orders` is scoped to the caller regardless of
  role — there's no platform-wide listing endpoint in the current backend.
  This is called out directly in the Admin Orders page. Admin order-status
  changes (beyond cancel) also aren't implemented, since order-service
  only exposes `PUT /api/orders/{id}/cancel`.
- **Admin Dashboard stats are computed client-side** from the same list
  endpoints the rest of the app uses (no dedicated `/admin/stats`
  endpoint exists), so "Revenue" reflects only the orders visible on that
  first page fetch.

## Environment variables

| Variable | Default | Purpose |
|---|---|---|
| `VITE_API_BASE_URL` | `http://localhost:8080` | API Gateway base URL |

## Build

```bash
npm run build   # outputs to dist/
npm run preview # serve the production build locally
```

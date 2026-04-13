# BookStore API Documentation

All requests go through the **API Gateway** at `http://localhost:8080`.

Authentication: Include `Authorization: Bearer <accessToken>` header on protected routes.

---

## Auth Service — `/api/auth`

| Method | Endpoint            | Auth | Description              |
|--------|---------------------|------|--------------------------|
| POST   | `/api/auth/register`| No   | Register as BUYER/SELLER |
| POST   | `/api/auth/login`   | No   | Login and get JWT tokens |
| POST   | `/api/auth/refresh` | No   | Refresh access token     |

### POST /api/auth/register
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "+91 9999999999",
  "role": "BUYER"
}
```
**Response 201:**
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": { "id": "...", "email": "...", "name": "...", "role": "BUYER", "status": "ACTIVE" }
}
```

### POST /api/auth/login
```json
{ "email": "john@example.com", "password": "password123" }
```

### POST /api/auth/refresh
```json
{ "refreshToken": "eyJ..." }
```

---

## Book Service — `/api/books`

| Method | Endpoint                    | Auth        | Description             |
|--------|-----------------------------|-------------|-------------------------|
| GET    | `/api/books`                | No          | List / filter books     |
| GET    | `/api/books/featured`       | No          | Get featured books      |
| GET    | `/api/books/:id`            | No          | Get book details        |
| POST   | `/api/books`                | SELLER/ADMIN| Create book listing     |
| PUT    | `/api/books/:id`            | SELLER/ADMIN| Update book             |
| DELETE | `/api/books/:id`            | SELLER/ADMIN| Delete book             |
| GET    | `/api/books/seller/my-books`| SELLER      | Get seller's own books  |

### GET /api/books — Query Parameters
| Param      | Type    | Description                         |
|------------|---------|-------------------------------------|
| query      | string  | Search in title, author, ISBN       |
| category   | string  | Textbook, Reference, Novel, Guide   |
| grade      | string  | Class 10, Class 12, Undergraduate   |
| subject    | string  | Mathematics, Physics, Chemistry...  |
| stream     | string  | Science, Commerce, Arts, Vocational |
| minPrice   | number  | Minimum price filter                |
| maxPrice   | number  | Maximum price filter                |
| minRating  | number  | Minimum average rating (0-5)        |
| featured   | boolean | Only featured books                 |
| comingSoon | boolean | Only coming soon books              |
| page       | int     | Page number (default: 0)            |
| size       | int     | Page size (default: 20, max: 100)   |
| sortBy     | string  | createdAt, price, averageRating, title |
| sortDir    | string  | asc or desc                         |

### POST /api/books (SELLER / ADMIN)
```json
{
  "title": "NCERT Mathematics Class 10",
  "author": "NCERT",
  "isbn": "978-8174507037",
  "description": "Complete NCERT textbook...",
  "price": 65.00,
  "originalPrice": 80.00,
  "stock": 500,
  "category": "Textbook",
  "grade": "Class 10",
  "subject": "Mathematics",
  "stream": "Science",
  "publisher": "NCERT",
  "publishedYear": "2023",
  "language": "English",
  "pages": 352,
  "coverImageUrl": "https://...",
  "featured": false,
  "comingSoon": false
}
```

---

## Order Service — Cart `/api/cart`

| Method | Endpoint              | Auth  | Description           |
|--------|-----------------------|-------|-----------------------|
| GET    | `/api/cart`           | BUYER | Get user's cart       |
| POST   | `/api/cart/items`     | BUYER | Add item to cart      |
| PATCH  | `/api/cart/items/:id` | BUYER | Update item quantity  |
| DELETE | `/api/cart/items/:id` | BUYER | Remove item from cart |
| DELETE | `/api/cart`           | BUYER | Clear entire cart     |

### POST /api/cart/items
```json
{
  "bookId": "abc123",
  "title": "NCERT Mathematics Class 10",
  "author": "NCERT",
  "coverImageUrl": "https://...",
  "quantity": 2,
  "unitPrice": 65.00,
  "sellerId": "seller-xyz"
}
```

---

## Order Service — Orders `/api/orders`

| Method | Endpoint                    | Auth        | Description             |
|--------|-----------------------------|-------------|-------------------------|
| POST   | `/api/orders/checkout`      | BUYER       | Place order from cart   |
| GET    | `/api/orders`               | BUYER       | Get user's order history|
| GET    | `/api/orders/:id`           | BUYER/ADMIN | Get order details       |
| PATCH  | `/api/orders/:id/status`    | Internal    | Update order status     |

### POST /api/orders/checkout
```json
{
  "shippingAddress": {
    "name": "John Doe",
    "street": "123 Main Street",
    "city": "Mumbai",
    "state": "Maharashtra",
    "pincode": "400001",
    "phone": "+91 9999999999"
  },
  "paymentMethod": "CARD"
}
```

---

## Payment Service — `/api/payments`

| Method | Endpoint               | Auth  | Description          |
|--------|------------------------|-------|----------------------|
| POST   | `/api/payments/process`| BUYER | Process mock payment |

### POST /api/payments/process
```json
{
  "orderId": "order-abc123",
  "amount": 399.00,
  "method": "CARD",
  "cardNumber": "4111111111111111",
  "cardExpiry": "12/27",
  "cardCvv": "123",
  "cardHolder": "JOHN DOE"
}
```
**For UPI:**
```json
{
  "orderId": "order-abc123",
  "amount": 399.00,
  "method": "UPI",
  "upiId": "john@paytm"
}
```

**Response:**
```json
{
  "paymentId": "PAY_ABC123XYZ",
  "orderId": "order-abc123",
  "status": "SUCCESS",
  "amount": 399.00,
  "method": "CARD",
  "message": "Payment successful",
  "processedAt": "2026-04-13T10:30:00Z",
  "transactionRef": "TXN_1234567890"
}
```

**Test Cases:**
- CVV `000` → Always fails
- UPI without `@` → Always fails
- All other inputs → 90% success, 10% random failure

---

## User Service — `/api/users`

| Method | Endpoint                       | Auth       | Description           |
|--------|--------------------------------|------------|-----------------------|
| GET    | `/api/users/me`                | Any        | Get own profile       |
| PUT    | `/api/users/me`                | Any        | Update own profile    |
| GET    | `/api/users/me/wishlist`       | BUYER      | Get wishlist book IDs |
| POST   | `/api/users/me/wishlist/:bookId`| BUYER     | Add to wishlist       |
| DELETE | `/api/users/me/wishlist/:bookId`| BUYER     | Remove from wishlist  |
| GET    | `/api/users`                   | ADMIN      | List all users        |
| PATCH  | `/api/users/:id/status`        | ADMIN      | Update user status    |

---

## Error Response Format
All services return errors in this format:
```json
{
  "status": 401,
  "message": "Invalid email or password",
  "errors": null,
  "timestamp": "2026-04-13T10:30:00Z"
}
```

## Order Status Flow
```
PENDING → PAYMENT_PROCESSING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
                             ↘ PAYMENT_FAILED
                                          ↘ CANCELLED
                                                      ↘ REFUNDED
```

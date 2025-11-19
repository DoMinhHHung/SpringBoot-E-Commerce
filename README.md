# Spring Boot E-Commerce Platform

## 📋 Mục lục
- [Giới thiệu](#giới-thiệu)
- [Công nghệ sử dụng](#công-nghệ-sử-dụng)
- [Tính năng chính](#tính-năng-chính)
- [Cấu trúc dự án](#cấu-trúc-dự án)
- [Luồng hoạt động API](#luồng-hoạt-động-api)
- [Luồng hoạt động từng chức năng](#luồng-hoạt-động-từng-chức-năng)
- [Cài đặt và chạy dự án](#cài-đặt-và-chạy-dự án)
- [Cấu hình](#cấu-hình)
- [Database Schema](#database-schema)
- [Bảo mật](#bảo-mật)
- [Deployment](#deployment)

---

## 🎯 Giới thiệu

**Spring Boot E-Commerce Platform** là một hệ thống thương mại điện tử hoàn chỉnh được xây dựng bằng Spring Boot, cung cấp đầy đủ các chức năng từ quản lý sản phẩm, giỏ hàng, đặt hàng, thanh toán đến quản trị hệ thống. Hệ thống hỗ trợ cả giao diện web và API RESTful, tích hợp nhiều dịch vụ bên thứ ba như PayOS, Cloudinary, Google OAuth2, và Gemini AI.

### Đặc điểm nổi bật:
- ✅ Hệ thống xác thực đa dạng (JWT, OAuth2 Google)
- ✅ Tích hợp AI Chatbot hỗ trợ tìm kiếm sản phẩm (Gemini AI)
- ✅ Thanh toán trực tuyến qua PayOS
- ✅ Quản lý đơn hàng và giao dịch
- ✅ Hỗ trợ khuyến mãi và giảm giá
- ✅ WebSocket cho chat hỗ trợ trực tuyến
- ✅ Upload ảnh lên Cloudinary
- ✅ Gửi email xác thực và OTP

---

## 🛠 Công nghệ sử dụng

### Backend
- **Spring Boot 3.5.7** - Framework chính
- **Spring Security** - Bảo mật và xác thực
- **Spring Data JPA** - ORM và quản lý database
- **Spring WebSocket** - Real-time communication
- **JWT (JSON Web Token)** - Xác thực stateless
- **OAuth2** - Đăng nhập qua Google
- **MapStruct** - Object mapping
- **Lombok** - Giảm boilerplate code

### Database
- **MariaDB** - Database chính
- **PostgreSQL** - Hỗ trợ (optional)

### Frontend
- **Thymeleaf** - Template engine
- **HTML/CSS/JavaScript** - Frontend vanilla
- **WebSocket Client** - Real-time chat

### Dịch vụ bên thứ ba
- **PayOS** - Cổng thanh toán
- **Cloudinary** - Lưu trữ và quản lý hình ảnh
- **Google Gemini AI** - AI Chatbot
- **Gmail SMTP** - Gửi email

### Tools & Libraries
- **Maven** - Build tool
- **SpringDoc OpenAPI** - API Documentation (Swagger)
- **Docker** - Containerization

---

## ✨ Tính năng chính

### 1. Quản lý người dùng (User Management)
- Đăng ký tài khoản với xác thực email
- Đăng nhập bằng email/password hoặc Google OAuth2
- Quản lý profile cá nhân
- Đổi mật khẩu
- Quên mật khẩu với OTP qua email
- Quản lý địa chỉ giao hàng

### 2. Quản lý sản phẩm (Product Management)
- Xem danh sách sản phẩm
- Tìm kiếm và lọc sản phẩm
- Xem chi tiết sản phẩm
- Quản lý sản phẩm (Admin): CRUD
- Upload ảnh sản phẩm lên Cloudinary
- Quản lý tồn kho (stock)

### 3. Giỏ hàng (Shopping Cart)
- Thêm sản phẩm vào giỏ hàng
- Cập nhật số lượng
- Xóa sản phẩm khỏi giỏ hàng
- Xem giỏ hàng theo user
- Xóa toàn bộ giỏ hàng sau khi đặt hàng

### 4. Đặt hàng (Order Management)
- Tạo đơn hàng từ giỏ hàng
- Chọn địa chỉ giao hàng
- Chọn phương thức thanh toán
- Xem lịch sử đơn hàng
- Xem chi tiết đơn hàng
- Cập nhật trạng thái đơn hàng (Admin)

### 5. Thanh toán (Payment)
- Tích hợp PayOS
- Tạo link thanh toán
- Xử lý callback từ PayOS
- Theo dõi trạng thái thanh toán
- Hỗ trợ thanh toán COD và online

### 6. Khuyến mãi (Promotion)
- Tạo và quản lý khuyến mãi
- Áp dụng giảm giá theo phần trăm
- Lọc khuyến mãi theo trạng thái (active, expired, upcoming)
- Xem sản phẩm áp dụng khuyến mãi

### 7. AI Chatbot
- Chat với AI để tìm kiếm sản phẩm
- Hỗ trợ tìm kiếm bằng ngôn ngữ tự nhiên
- Tích hợp Gemini AI
- Gợi ý sản phẩm thông minh

### 8. Hỗ trợ trực tuyến (Support)
- Chat trực tuyến với admin
- WebSocket real-time communication
- Quản lý phiên chat (Admin)
- Chuyển từ AI sang hỗ trợ con người

### 9. Quản trị (Admin Dashboard)
- Dashboard tổng quan
- Quản lý sản phẩm
- Quản lý khuyến mãi
- Quản lý đơn hàng
- Quản lý giao dịch
- Quản lý người dùng
- Hỗ trợ khách hàng

### 10. Giao dịch (Transaction)
- Theo dõi tất cả giao dịch
- Lọc theo loại và trạng thái
- Thống kê giao dịch
- Xem chi tiết giao dịch

---

## 📁 Cấu trúc dự án

```
SpringBoot-E-Commerce/
├── src/
│   ├── main/
│   │   ├── java/iuh/fit/se/ecommerce/
│   │   │   ├── config/          # Cấu hình (Security, JWT, OAuth2, WebSocket, etc.)
│   │   │   ├── controller/      # REST Controllers
│   │   │   ├── service/          # Business logic
│   │   │   │   ├── interfaces/  # Service interfaces
│   │   │   │   └── impl/         # Service implementations
│   │   │   ├── repository/      # Data access layer
│   │   │   ├── entity/          # JPA Entities
│   │   │   │   └── enums/       # Enumerations
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── request/     # Request DTOs
│   │   │   │   ├── response/    # Response DTOs
│   │   │   │   └── mapper/      # MapStruct mappers
│   │   │   └── exception/       # Exception handling
│   │   └── resources/
│   │       ├── templates/       # Thymeleaf templates
│   │       ├── static/          # Static resources (CSS, JS, images)
│   │       └── application.properties
│   └── test/                    # Test files
├── data/                        # Database scripts
├── pom.xml                      # Maven dependencies
├── Dockerfile                   # Docker configuration
└── README.md                    # Documentation
```

---

## 🔄 Luồng hoạt động API

### 1. Luồng xác thực (Authentication Flow)

```
┌─────────┐      ┌──────────┐      ┌──────────┐      ┌──────────┐
│ Client  │─────▶│ Auth API │─────▶│  Email   │─────▶│  Verify  │
└─────────┘      └──────────┘      └──────────┘      └──────────┘
     │                 │
     │                 ▼
     │          ┌──────────────┐
     │          │ Create User  │
     │          │ (disabled)   │
     │          └──────────────┘
     │                 │
     │                 ▼
     │          ┌──────────────┐
     │          │ Send Verify  │
     │          │ Email        │
     │          └──────────────┘
     │
     │          ┌──────────────┐
     └─────────▶│ Login API    │
                │ (JWT Token)  │
                └──────────────┘
```

**Chi tiết:**
1. **Đăng ký**: `POST /api/auth`
   - Client gửi thông tin đăng ký
   - Server tạo user với `enabled = false`
   - Gửi email xác thực với verification token
   - Trả về thông báo kiểm tra email

2. **Xác thực email**: `GET /api/auth/verify?token=xxx`
   - User click link trong email
   - Server verify token và kích hoạt tài khoản
   - Set `enabled = true`

3. **Đăng nhập**: `POST /api/auth/login`
   - Client gửi email/password
   - Server xác thực và tạo JWT tokens
   - Trả về access_token và refresh_token (set trong cookies)

4. **Refresh token**: `POST /api/auth/refresh-token`
   - Client gửi refresh_token
   - Server tạo lại access_token mới

5. **OAuth2 Google**: `GET /oauth2/authorization/google`
   - Redirect đến Google login
   - Callback xử lý và tạo user nếu chưa có
   - Tạo JWT tokens

### 2. Luồng quản lý sản phẩm (Product Flow)

```
┌─────────┐      ┌─────────────┐      ┌──────────┐      ┌──────────┐
│ Client  │─────▶│ Product API │─────▶│ Database │─────▶│Cloudinary│
└─────────┘      └─────────────┘      └──────────┘      └──────────┘
     │                 │
     │                 ▼
     │          ┌──────────────┐
     │          │ CRUD Product │
     │          └──────────────┘
```

**Endpoints:**
- `GET /api/products` - Lấy tất cả sản phẩm
- `GET /api/products/{id}` - Lấy chi tiết sản phẩm
- `GET /api/products/type/{type}` - Lọc theo loại
- `GET /api/products/hot-sale` - Sản phẩm hot sale
- `POST /api/products` - Tạo sản phẩm (Admin)
- `PUT /api/products/{id}` - Cập nhật (Admin)
- `DELETE /api/products/{id}` - Xóa (Admin)

### 3. Luồng giỏ hàng (Cart Flow)

```
┌─────────┐      ┌──────────┐      ┌──────────┐
│ Client  │─────▶│ Cart API │─────▶│ Database │
└─────────┘      └──────────┘      └──────────┘
     │                 │
     │                 ▼
     │          ┌──────────────┐
     │          │ Manage Cart  │
     │          │ Items        │
     │          └──────────────┘
```

**Endpoints:**
- `POST /api/cart/add` - Thêm sản phẩm
- `PUT /api/cart/update` - Cập nhật số lượng
- `DELETE /api/cart/remove` - Xóa sản phẩm
- `GET /api/cart/{userId}` - Lấy giỏ hàng
- `DELETE /api/cart/clear/{userId}` - Xóa toàn bộ

### 4. Luồng đặt hàng và thanh toán (Order & Payment Flow)

```
┌─────────┐      ┌──────────┐      ┌──────────┐      ┌──────────┐
│ Client  │─────▶│ Payment  │─────▶│  Order   │─────▶│  PayOS   │
│         │      │   API    │      │ Service  │      │ Gateway  │
└─────────┘      └──────────┘      └──────────┘      └──────────┘
     │                 │                 │                 │
     │                 │                 ▼                 │
     │                 │          ┌──────────────┐         │
     │                 │          │ Create Order │         │
     │                 │          │ & Payment    │         │
     │                 │          └──────────────┘         │
     │                 │                 │                 │
     │                 │                 ▼                 │
     │                 │          ┌──────────────┐         │
     │                 │          │ Get Payment  │         │
     │                 │          │ Link         │         │
     │                 │          └──────────────┘         │
     │                 │                 │                 │
     │                 ▼                 │                 │
     │          ┌──────────────┐         │                 │
     │          │ Redirect to  │         │                 │
     │          │ PayOS        │─────────┼─────────────────┘
     │          └──────────────┘         │
     │                 │                 │
     │                 ▼                 ▼
     │          ┌──────────────┐  ┌──────────────┐
     │          │ User Payment │  │ Webhook      │
     │          │ Success      │  │ Callback     │
     │          └──────────────┘  └──────────────┘
     │                 │                 │
     │                 ▼                 ▼
     │          ┌──────────────┐  ┌──────────────┐
     │          │ Update Order │  │ Update       │
     │          │ Status        │  │ Payment      │
     │          └──────────────┘  └──────────────┘
```

**Chi tiết:**
1. **Tạo thanh toán**: `POST /api/payments/create`
   - Client gửi thông tin đơn hàng (items, address, payment method)
   - Server tạo Order với status PENDING
   - Tạo Payment record
   - Gọi PayOS API để tạo payment link
   - Trả về checkout URL và QR code

2. **Thanh toán**: Client redirect đến PayOS
   - User thanh toán trên PayOS
   - PayOS gửi webhook callback

3. **Webhook callback**: `POST /api/payments/payos-callback`
   - PayOS gửi thông tin thanh toán
   - Server verify checksum
   - Cập nhật Payment status
   - Cập nhật Order status
   - Tạo Transaction record
   - Cập nhật stock sản phẩm

4. **Kiểm tra trạng thái**: `GET /api/payments/status/{orderCode}`
   - Client kiểm tra trạng thái thanh toán

### 5. Luồng AI Chatbot (Chat Flow)

```
┌─────────┐      ┌──────────┐      ┌──────────┐      ┌──────────┐
│ Client  │─────▶│ WebSocket│─────▶│ Gemini   │─────▶│ Product  │
│         │      │  /chat   │      │   AI     │      │ Service  │
└─────────┘      └──────────┘      └──────────┘      └──────────┘
     │                 │                 │                 │
     │                 ▼                 ▼                 ▼
     │          ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
     │          │ Parse User   │  │ Generate    │  │ Search       │
     │          │ Message      │  │ Query       │  │ Products     │
     │          └──────────────┘  └──────────────┘  └──────────────┘
     │                 │                 │                 │
     │                 └─────────────────┴─────────────────┘
     │                                 │
     │                                 ▼
     │                          ┌──────────────┐
     │                          │ Return       │
     │                          │ Products     │
     │                          └──────────────┘
```

**Chi tiết:**
1. Client kết nối WebSocket: `/ws`
2. Gửi message: `/app/chat`
3. Server xử lý:
   - Nếu message là query format → tìm kiếm trực tiếp
   - Nếu không → gửi đến Gemini AI để parse
   - Gemini trả về format: QUERY, GREETING, CONTEXT_QUESTION, ADVANCED_QUERY, hoặc CALL_HUMAN
4. Server tìm kiếm sản phẩm dựa trên query
5. Trả về kết quả qua `/topic/replies.{sessionId}`

### 6. Luồng hỗ trợ trực tuyến (Support Flow)

```
┌─────────┐      ┌──────────┐      ┌──────────┐
│ Client  │─────▶│ WebSocket│─────▶│  Admin   │
│         │      │ /support │      │ Support  │
└─────────┘      └──────────┘      └──────────┘
     │                 │                 │
     │                 ▼                 ▼
     │          ┌──────────────┐  ┌──────────────┐
     │          │ Request     │  │ Admin Join   │
     │          │ Human        │  │ Session      │
     │          └──────────────┘  └──────────────┘
     │                 │                 │
     │                 └─────────────────┘
     │                         │
     │                         ▼
     │                  ┌──────────────┐
     │                  │ Chat with    │
     │                  │ Admin        │
     │                  └──────────────┘
```

**Endpoints:**
- WebSocket: `/app/support/join` - Admin tham gia
- WebSocket: `/app/support/adminSend` - Admin gửi tin nhắn
- WebSocket: `/app/support/close` - Đóng phiên
- REST: `GET /api/support/pending` - Danh sách yêu cầu chờ (Admin)

---

## 🔧 Luồng hoạt động từng chức năng

### 1. Đăng ký và xác thực

**Bước 1: Đăng ký**
```
User → POST /api/auth
  → Validate input
  → Check email exists
  → Hash password
  → Create User (enabled=false)
  → Generate verification token
  → Save VerificationToken
  → Send verification email
  → Return success message
```

**Bước 2: Xác thực email**
```
User click link → GET /api/auth/verify?token=xxx
  → Find VerificationToken
  → Check expired
  → Enable user (enabled=true)
  → Delete token
  → Return success
```

**Bước 3: Đăng nhập**
```
User → POST /api/auth/login
  → Find user by email
  → Check enabled
  → Verify password
  → Generate JWT tokens (access + refresh)
  → Set cookies
  → Return user info + tokens
```

### 2. Quản lý sản phẩm

**Tạo sản phẩm (Admin)**
```
Admin → POST /api/products (multipart/form-data)
  → Validate input
  → Upload images to Cloudinary
  → Get image URLs
  → Create Product entity
  → Save to database
  → Return ProductResponse
```

**Tìm kiếm sản phẩm**
```
User → GET /api/products?query=...
  → Parse query (brand, type, price, promotion)
  → Build JPA query
  → Filter products
  → Calculate discount if has promotion
  → Return ProductResponse list
```

### 3. Giỏ hàng

**Thêm vào giỏ hàng**
```
User → POST /api/cart/add
  → Find user
  → Find or create Cart
  → Check product exists
  → Check stock
  → Add/Update CartItem
  → Calculate total
  → Return CartResponse
```

**Cập nhật số lượng**
```
User → PUT /api/cart/update
  → Find CartItem
  → Check stock available
  → Update quantity
  → Recalculate total
  → Return CartResponse
```

### 4. Đặt hàng

**Tạo đơn hàng**
```
User → POST /api/payments/create
  → Validate cart items
  → Check stock for each item
  → Create Order (status=PENDING)
  → Create OrderItems
  → Calculate totals (subtotal, discount, shipping, total)
  → Set shipping address
  → Save Order
  → Create Payment record
  → Call PayOS API
  → Get payment link
  → Return PaymentResponse
```

**Xử lý thanh toán thành công**
```
PayOS → POST /api/payments/payos-callback
  → Verify checksum
  → Find Payment by orderCode
  → Update Payment status (SUCCESS)
  → Update Order status (CONFIRMED)
  → Deduct stock for each item
  → Create Transaction (INCOME, SUCCESS)
  → Clear user cart
  → Return success
```

### 5. Quản lý đơn hàng

**Xem danh sách đơn hàng**
```
User → GET /api/orders?status=...&page=...&size=...
  → Get user from JWT
  → Query orders by user
  → Filter by status (optional)
  → Paginate
  → Map to OrderResponse
  → Return Page<OrderResponse>
```

**Cập nhật trạng thái (Admin)**
```
Admin → PUT /api/admin/orders/{orderCode}/status
  → Find Order
  → Validate status transition
  → Update status
  → Add notes (optional)
  → Save
  → Return OrderResponse
```

### 6. Khuyến mãi

**Tạo khuyến mãi (Admin)**
```
Admin → POST /api/promotions
  → Validate dates
  → Check overlap
  → Create Promotion
  → Save
  → Return PromotionResponse
```

**Áp dụng khuyến mãi**
```
When calculating product price:
  → Check if product has active Promotion
  → Check if current date within promotion period
  → Calculate discount = price * (discountPercent / 100)
  → Return priceAfterDiscount = price - discount
```

### 7. AI Chatbot

**Xử lý tin nhắn**
```
User → WebSocket /app/chat
  → Check message format
  → If format query → search directly
  → Else → Send to Gemini AI
  → Parse Gemini response:
    - GREETING → Return greeting message
    - CONTEXT_QUESTION → Ask for more info
    - QUERY: ... → Extract query and search
    - ADVANCED_QUERY → Handle special request
    - CALL_HUMAN → Notify admin
  → Search products by query
  → Format response with products
  → Send to /topic/replies.{sessionId}
```

**Tìm kiếm sản phẩm từ query**
```
Parse query string:
  - brand:XXX → Filter by brand
  - type:XXX → Filter by ProductType
  - price:XXX or price:MIN-MAX → Filter by price
  - promotion:XXX → Filter by promotion
  - Text search → Search in name, description, brand
  → Combine filters
  → Execute query
  → Return results
```

### 8. Hỗ trợ trực tuyến

**Yêu cầu hỗ trợ**
```
User → Send "CALL_HUMAN" message
  → Register session in SupportSessionRegistry
  → Mark as pending
  → Notify admin via /api/support/pending
```

**Admin tham gia**
```
Admin → WebSocket /app/support/join
  → Assign admin to session
  → Notify user
  → Start chat session
```

**Chat**
```
Admin → /app/support/adminSend
  → Send message to user
  → User receives via /topic/replies.{sessionId}
  
User → /app/chat (normal flow)
  → Send message to admin
  → Admin receives via /topic/admin.session.{sessionId}
```

### 9. Quản lý giao dịch

**Tạo giao dịch**
```
When payment success:
  → Create Transaction
  → Type: INCOME
  → Status: SUCCESS
  → Amount: order.totalAmount
  → Link to Payment
  → Save
```

**Xem thống kê (Admin)**
```
Admin → GET /api/admin/transactions/summary
  → Filter by type, status, date range
  → Calculate:
    - Total income
    - Total transactions
    - Success count
    - Failed count
  → Return TransactionSummaryResponse
```

---

## 🚀 Cài đặt và chạy dự án

### Yêu cầu hệ thống
- Java 17+
- Maven 3.6+
- MariaDB 10.5+ hoặc PostgreSQL 12+
- Docker (optional)

### Bước 1: Clone repository
```bash
git clone <repository-url>
cd SpringBoot-E-Commerce
```

### Bước 2: Cấu hình database
Tạo database:
```sql
CREATE DATABASE spring_boot;
```

Import dữ liệu mẫu (optional):
```bash
mysql -u root -p spring_boot < data/database_import.sql
```

### Bước 3: Cấu hình application.properties
Chỉnh sửa file `src/main/resources/application.properties`:
- Database connection
- Email SMTP
- OAuth2 credentials
- Cloudinary credentials
- PayOS credentials
- Gemini API key
- JWT secret

### Bước 4: Build và chạy
```bash
# Build project
./mvnw clean package

# Chạy ứng dụng
./mvnw spring-boot:run

# Hoặc chạy JAR
java -jar target/Springboot_Ecommerce-0.0.1-SNAPSHOT.jar
```

### Bước 5: Truy cập ứng dụng
- Frontend: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/v3/api-docs

---

## ⚙️ Cấu hình

### Database
```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/spring_boot
spring.datasource.username=root
spring.datasource.password=your_password
```

### Email (Gmail SMTP)
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
```

### OAuth2 Google
1. Tạo project trên Google Cloud Console
2. Tạo OAuth 2.0 credentials
3. Cấu hình redirect URI: `http://localhost:8080/oauth2/callback`
4. Cập nhật trong `application.properties`

### Cloudinary
1. Đăng ký tài khoản Cloudinary
2. Lấy Cloud Name, API Key, API Secret
3. Cập nhật trong `application.properties`

### PayOS
1. Đăng ký tài khoản PayOS
2. Lấy Client ID, API Key, Checksum Key
3. Cấu hình return URL và cancel URL
4. Cập nhật trong `application.properties`

### Gemini AI
1. Tạo API key trên Google AI Studio
2. Cập nhật trong `application.properties`

### Admin User
Tự động tạo admin user khi khởi động:
```properties
app.admin.email=admin@example.com
app.admin.fullname=Admin User
app.admin.password=your_password
```

---

## 🗄️ Database Schema

### Các Entity chính:

**User**
- id, fullName, email, phone, password, avatar, gender, dob
- authProvider (LOCAL, GOOGLE)
- role (USER, ADMIN)
- enabled (boolean)
- addresses (OneToMany)

**Product**
- id, name, brand, description, price, stock
- mainImage, images (List)
- productType (LAPTOP, ACCESSORY)
- specifications (OneToMany)
- promotion (ManyToOne)

**Order**
- id, orderCode (unique), user, items, shippingAddress
- status (PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
- subtotal, discountAmount, shippingFee, totalAmount
- notes, createdAt, updatedAt

**Payment**
- id, order, amount, paymentLinkId, qrCode, checkoutUrl
- status (PENDING, SUCCESS, FAILED, CANCELLED)

**Cart & CartItem**
- Cart: id, user, items, createdAt
- CartItem: id, cart, product, quantity

**Promotion**
- id, name, description, discountPercent
- startDate, endDate, active

**Transaction**
- id, type (INCOME, EXPENSE), status (SUCCESS, FAILED, PENDING)
- amount, description, payment, createdAt

**Address**
- id, user, fullName, phone, address, ward, district, city
- isDefault

---

## 🔒 Bảo mật

### 1. Authentication & Authorization
- **JWT Token**: Stateless authentication
  - Access token: 1 giờ
  - Refresh token: 7 ngày
  - Stored in httpOnly cookies
  
- **OAuth2**: Đăng nhập qua Google
- **Role-based**: USER và ADMIN
- **Password**: BCrypt hashing

### 2. Security Configuration
- CSRF disabled (API only)
- Stateless session
- JWT filter cho API requests
- Public endpoints: auth, products (GET), promotions (GET)
- Protected endpoints: orders, payments, cart
- Admin-only: product management, order management, transactions

### 3. API Security
- JWT validation trên mọi protected endpoint
- Role checking với `@PreAuthorize`
- Input validation với `@Valid`
- SQL injection prevention (JPA)

### 4. Payment Security
- PayOS checksum verification
- Webhook signature validation
- Secure payment link generation

---

## 🐳 Deployment

### Docker

**Build image:**
```bash
docker build -t springboot-ecommerce .
```

### Run container:
**
```bash
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mariadb://host.docker.internal:3306/spring_boot \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  springboot-ecommerce
```

### Docker Compose (Recommended)
Tạo file `docker-compose.yml`:
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mariadb://db:3306/spring_boot
      - SPRING_DATASOURCE_USERNAME=root
      - SPRING_DATASOURCE_PASSWORD=root
    depends_on:
      - db
  
  db:
    image: mariadb:10.5
    environment:
      - MYSQL_ROOT_PASSWORD=root
      - MYSQL_DATABASE=spring_boot
    ports:
      - "3306:3306"
    volumes:
      - db_data:/var/lib/mysql

volumes:
  db_data:
```

Chạy:
```bash
docker-compose up -d
```

### Production
1. Set `SPRING_PROFILES_ACTIVE=prod`
2. Cấu hình production database
3. Sử dụng HTTPS
4. Cấu hình CORS
5. Enable logging
6. Setup monitoring

---

## 📝 API Documentation

Truy cập Swagger UI để xem chi tiết API:
- URL: http://localhost:8080/swagger-ui.html

Tất cả endpoints được document với:
- Request/Response schemas
- Authentication requirements
- Example requests
- Error responses

---

## 🧪 Testing

Chạy tests:
```bash
./mvnw test
```

Test files:
- `CartControllerTest.java`
- `SpringbootEcommerceApplicationTests.java`

---

## 📚 Tài liệu tham khảo

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)
- [PayOS Documentation](https://payos.vn/docs)
- [Cloudinary Documentation](https://cloudinary.com/documentation)
- [Gemini AI](https://ai.google.dev/docs)

---

## 👥 Đóng góp

1. Fork repository
2. Tạo feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Tạo Pull Request

---

## 📄 License

This project is licensed under the MIT License.

---

## 📧 Liên hệ

Nếu có câu hỏi hoặc vấn đề, vui lòng tạo issue trên repository.

---

**Lưu ý**: Đây là dự án học tập, một số thông tin nhạy cảm (API keys, passwords) nên được bảo vệ bằng environment variables hoặc secret management trong môi trường production.


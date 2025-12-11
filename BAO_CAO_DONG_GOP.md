# BÁO CÁO CHI TIẾT CÁC PHẦN ĐÓNG GÓP TRONG DỰ ÁN E-COMMERCE

## MỤC LỤC
1. [Tổng quan công nghệ](#1-tổng-quan-công-nghệ)
2. [Hệ thống thanh toán (Payment System)](#2-hệ-thống-thanh-toán-payment-system)
3. [Hệ thống phân quyền (Permission System)](#3-hệ-thống-phân-quyền-permission-system)
4. [Hệ thống tracking user cho statistics](#4-hệ-thống-tracking-user-cho-statistics)
5. [API quản lý (Management APIs)](#5-api-quản-lý-management-apis)
6. [Cơ sở dữ liệu](#6-cơ-sở-dữ-liệu)
7. [Deployment](#7-deployment)

---

## 1. TỔNG QUAN CÔNG NGHỆ

### 1.1. Backend Framework
Dự án sử dụng **Spring Boot 3.5.7** với **Java 17**, đây là phiên bản mới nhất và ổn định của Spring Boot, hỗ trợ đầy đủ các tính năng hiện đại như:
- **Spring Security**: Bảo mật ứng dụng với JWT và OAuth2
- **Spring Data JPA**: Làm việc với database một cách dễ dàng
- **Spring Web**: Xây dựng RESTful APIs
- **Thymeleaf**: Template engine cho frontend

### 1.2. Database
- **MySQL trên Railway**: Database chính được host trên Railway, một platform cloud hiện đại
- Hỗ trợ thêm **MariaDB** và **PostgreSQL** để linh hoạt trong việc deploy

### 1.3. Authentication & Security
- **JWT (JSON Web Token)**: Xác thực người dùng không cần session, phù hợp cho RESTful API
- **OAuth2 với Google**: Cho phép đăng nhập bằng tài khoản Google
- **BCrypt**: Mã hóa mật khẩu an toàn
- **Method-level Security**: Kiểm soát quyền truy cập ở mức method

### 1.4. Payment Integration
- **PayOS Gateway**: Tích hợp thanh toán qua PayOS, hỗ trợ QR code, thẻ ngân hàng, ví điện tử
- **HMAC-SHA256**: Thuật toán bảo mật để xác thực giao dịch

### 1.5. External Services
- **Cloudinary**: Lưu trữ và quản lý hình ảnh sản phẩm
- **SendGrid**: Gửi email xác thực và thông báo
- **Google Gemini AI**: Chatbot hỗ trợ khách hàng
- **Nominatim**: Dịch vụ geocoding để xác định địa chỉ

---

## 2. HỆ THỐNG THANH TOÁN (PAYMENT SYSTEM)

### 2.1. Tổng quan kiến trúc

Hệ thống thanh toán được thiết kế với 3 phương thức chính:
1. **PAYOS**: Thanh toán online qua PayOS (QR code, thẻ, ví điện tử)
2. **COD (Cash on Delivery)**: Thanh toán khi nhận hàng
3. **BANK_TRANSFER**: Chuyển khoản ngân hàng

Mỗi phương thức đều được xử lý thống nhất thông qua một service layer, đảm bảo tính nhất quán và dễ mở rộng.

### 2.2. Luồng thanh toán PayOS chi tiết

#### Bước 1: Khởi tạo thanh toán

Khi người dùng chọn sản phẩm và nhấn "Đặt hàng", hệ thống sẽ thực hiện các bước sau:

**1.1. Xác thực người dùng**
- Hệ thống lấy email từ JWT token (đã được xác thực trước đó)
- Tìm kiếm user trong database dựa trên email
- Nếu không tìm thấy, trả về lỗi "User not found"

**1.2. Tạo đơn hàng (Order)**
- Service `OrderService` sẽ nhận thông tin từ `PaymentRequest` (bao gồm cart items, địa chỉ giao hàng, phương thức thanh toán)
- Kiểm tra tồn kho cho từng sản phẩm
- Tính toán giá trị đơn hàng: subtotal (tổng giá sản phẩm), discount (giảm giá nếu có), shipping fee (phí vận chuyển), total amount (tổng cuối cùng)
- Tạo Order với status = PENDING
- Lưu OrderItems (chi tiết từng sản phẩm trong đơn)

**1.3. Tạo Payment Link từ PayOS**
- Chuyển đổi số tiền từ BigDecimal sang Long (PayOS yêu cầu đơn vị VND dạng số nguyên)
- Gọi `PayOSGateway.createPaymentLink()` với các thông tin:
  - `orderCode`: Mã đơn hàng duy nhất (dùng làm identifier)
  - `amount`: Số tiền cần thanh toán
  - `description`: Mô tả đơn hàng
  - `returnUrl`: URL để redirect sau khi thanh toán thành công
  - `cancelUrl`: URL để redirect nếu người dùng hủy

**1.4. Tạo Payment Record**
- Sau khi nhận được response từ PayOS (chứa paymentLinkId, qrCode, checkoutUrl), hệ thống tạo một Payment record trong database
- Payment record lưu trữ:
  - Link đến Order (One-to-One relationship)
  - Payment method (PAYOS)
  - Status (PENDING)
  - Payment link ID từ PayOS
  - QR code (dạng base64 hoặc URL)
  - Checkout URL (link để người dùng thanh toán)

**1.5. Tạo Transaction Record**
- Mỗi payment sẽ tạo một Transaction record để tracking
- Transaction có type = PAYMENT, amount = số tiền (số âm vì là chi ra), status = PENDING
- Transaction code được generate tự động theo format "TXN" + timestamp

**1.6. Trả về response cho frontend**
- Frontend nhận được QR code và checkout URL
- Hiển thị QR code cho người dùng quét, hoặc nút "Thanh toán" để redirect đến checkout URL

#### Bước 2: Tích hợp với PayOS Gateway

**2.1. Tạo Payment Link Request**

PayOS Gateway là một service class chịu trách nhiệm giao tiếp với PayOS API. Khi tạo payment link:

- **Tạo request body**: Bao gồm orderCode, amount, description, returnUrl, cancelUrl
- **Tạo checksum (chữ ký số)**: Đây là bước quan trọng để bảo mật. Hệ thống sử dụng thuật toán HMAC-SHA256 để tạo chữ ký từ dữ liệu request và checksum key (được lưu trong application.properties)
- **Setup HTTP headers**: 
  - Content-Type: application/json
  - x-client-id: Client ID từ PayOS (được cấp khi đăng ký tài khoản PayOS)
  - x-api-key: API key từ PayOS
- **Gửi POST request** đến PayOS API endpoint: `https://api-merchant.payos.vn/v2/payment-requests`
- **Xử lý response**: PayOS trả về payment link ID, QR code, và checkout URL

**2.2. Checksum Generation (Bảo mật)**

Checksum là một cơ chế bảo mật quan trọng để đảm bảo dữ liệu không bị giả mạo:

- **Tạo data string**: Kết hợp các thông tin theo format PayOS yêu cầu: `amount={amount}&cancelUrl={cancelUrl}&description={description}&orderCode={orderCode}&returnUrl={returnUrl}`
- **Tạo HMAC-SHA256 signature**: Sử dụng checksum key (bí mật, chỉ PayOS và hệ thống biết) để tạo chữ ký
- **Convert sang hex string**: Chuyển đổi byte array sang chuỗi hex để gửi trong request
- PayOS sẽ verify checksum này để đảm bảo request đến từ đúng merchant và dữ liệu không bị thay đổi

#### Bước 3: Xử lý Webhook Callback

Khi người dùng hoàn tất thanh toán trên PayOS, PayOS sẽ gửi một webhook callback đến server:

**3.1. Nhận Webhook**
- Endpoint: `POST /api/payments/payos-callback`
- PayOS gửi POST request với thông tin về trạng thái thanh toán
- Webhook này được cấu hình public (không cần authentication) vì PayOS không thể xác thực JWT

**3.2. Verify Webhook Data**
- Kiểm tra code trong webhook response (code = "00" nghĩa là thành công)
- Parse orderCode từ webhook data
- Tìm Order và Payment tương ứng trong database

**3.3. Update Payment Status**
- Nếu webhook báo "PAID" hoặc code = "00":
  - Update Payment status = PAID
  - Lưu transaction ID từ PayOS (reference)
  - Update Transaction status = SUCCESS
  - Gọi `orderService.confirmOrder()` để xác nhận đơn hàng
- Nếu webhook báo lỗi:
  - Update Payment status = FAILED
  - Log lỗi để xử lý sau

**3.4. Confirm Order**
- Khi order được confirm, hệ thống sẽ:
  - Update Order status = CONFIRMED
  - Trigger event `OrderConfirmedEvent`
  - Event này sẽ kích hoạt StatisticsAuditListener để ghi nhận dữ liệu thống kê

#### Bước 4: Đồng bộ trạng thái thanh toán

Để đảm bảo trạng thái thanh toán luôn chính xác, hệ thống có cơ chế đồng bộ:

**4.1. Polling Payment Status**
- Khi frontend gọi `GET /api/payments/status/{orderCode}`, nếu payment vẫn PENDING, hệ thống sẽ gọi PayOS API để lấy trạng thái mới nhất
- Method `syncPaymentStatusFromPayOS()` sẽ:
  - Gọi PayOS API với paymentLinkId
  - So sánh status từ PayOS với status trong database
  - Nếu PayOS báo đã thanh toán nhưng database chưa update, hệ thống sẽ update và confirm order

**4.2. Xử lý các trường hợp đặc biệt**
- Nếu PayOS báo CANCELLED: Update payment status = CANCELLED
- Nếu PayOS báo FAILED: Update payment status = FAILED
- Nếu vẫn PENDING: Giữ nguyên status, frontend có thể tiếp tục polling

### 2.3. Transaction Tracking

Mỗi payment sẽ tạo một Transaction record để theo dõi:

**Mục đích của Transaction:**
- Lưu trữ lịch sử giao dịch của user
- Hỗ trợ báo cáo tài chính
- Theo dõi các loại giao dịch: PAYMENT (chi ra), REFUND (hoàn tiền), DEPOSIT (nạp tiền), WITHDRAWAL (rút tiền)

**Cấu trúc Transaction:**
- `type`: Loại giao dịch (PAYMENT, REFUND, etc.)
- `amount`: Số tiền (dương = thu vào, âm = chi ra)
- `status`: Trạng thái (PENDING, SUCCESS, FAILED)
- `transactionCode`: Mã giao dịch nội bộ (unique)
- `externalTransactionId`: Mã giao dịch từ PayOS
- `description`: Mô tả giao dịch

**Luồng tạo Transaction:**
1. Khi tạo payment: Tạo Transaction với type = PAYMENT, amount = số tiền (âm), status = PENDING
2. Khi payment thành công: Update Transaction status = SUCCESS, lưu externalTransactionId
3. Khi hoàn tiền: Tạo Transaction mới với type = REFUND, amount = số tiền (dương), status = SUCCESS

---

## 3. HỆ THỐNG PHÂN QUYỀN (PERMISSION SYSTEM)

### 3.1. Tổng quan kiến trúc

Hệ thống phân quyền được thiết kế theo mô hình **RBAC (Role-Based Access Control)** với 3 lớp:

1. **Role (Vai trò)**: ADMIN, EDITOR, USER
2. **Permission (Quyền)**: PRODUCT_CREATE, ORDER_UPDATE, TRANSACTION_VIEW, etc.
3. **RolePermission (Mapping)**: Bảng trung gian để gán permission cho role

**Tại sao dùng RBAC thay vì chỉ dùng Role?**
- Linh hoạt hơn: Có thể tạo nhiều role với các quyền khác nhau (ví dụ: EDITOR có thể tạo sản phẩm nhưng không thể xóa)
- Dễ quản lý: Admin có thể thêm/bớt quyền cho role mà không cần thay đổi code
- Bảo mật tốt hơn: Kiểm soát chi tiết từng hành động (CREATE, UPDATE, DELETE, VIEW)

### 3.2. Cấu trúc Database

**Bảng Permissions:**
- `code`: Mã quyền duy nhất (ví dụ: PRODUCT_CREATE)
- `name`: Tên quyền (ví dụ: "Tạo sản phẩm")
- `description`: Mô tả chi tiết
- `resource`: Tài nguyên (PRODUCT, ORDER, TRANSACTION, etc.)
- `action`: Hành động (CREATE, UPDATE, DELETE, VIEW)

**Bảng Role_Permissions:**
- `role_name`: Tên role (ADMIN, EDITOR, USER)
- `permission_id`: ID của permission
- Primary key là (role_name, permission_id) để đảm bảo mỗi role-permission chỉ xuất hiện 1 lần

**Ví dụ mapping:**
- ADMIN có tất cả permissions: PRODUCT_CREATE, PRODUCT_UPDATE, PRODUCT_DELETE, ORDER_VIEW, ORDER_UPDATE, etc.
- EDITOR có: PRODUCT_CREATE, PRODUCT_UPDATE, ORDER_VIEW (không có DELETE)
- USER không có permission nào (chỉ xem và mua hàng)

### 3.3. Permission Service Implementation

**3.3.1. Kiểm tra quyền của user**

Service `PermissionService` cung cấp các method để kiểm tra quyền:

- `hasPermission(User user, String permissionCode)`: Kiểm tra user có permission cụ thể không
  - Logic: Lấy role của user → Lấy tất cả permissions của role đó → Kiểm tra permissionCode có trong danh sách không
  - Return true nếu có, false nếu không

- `hasAnyPermission(User user, String... permissionCodes)`: Kiểm tra user có ít nhất 1 trong các permissions
  - Logic: Lấy permissions của user → Duyệt qua danh sách permissionCodes → Return true ngay khi tìm thấy 1 permission
  - Dùng khi một hành động có thể được thực hiện bởi nhiều quyền khác nhau

- `hasAllPermissions(User user, String... permissionCodes)`: Kiểm tra user có tất cả permissions
  - Logic: Lấy permissions của user → Duyệt qua danh sách permissionCodes → Return false ngay khi thiếu 1 permission
  - Dùng khi một hành động yêu cầu nhiều quyền cùng lúc

**3.3.2. Lấy danh sách permissions**

- `getPermissionsByRole(Role role)`: Lấy tất cả Permission objects của một role
  - Query: `SELECT p FROM Permission p JOIN RolePermission rp ON p.id = rp.permission.id WHERE rp.role = :role`
  - Return Set<Permission>

- `getPermissionCodesByRole(Role role)`: Lấy tất cả permission codes (String) của một role
  - Map từ Set<Permission> sang Set<String> bằng cách lấy code của mỗi permission
  - Dùng để cache hoặc so sánh nhanh

**3.3.3. Quản lý permissions**

- `assignPermissionToRole(Role role, String permissionCode)`: Gán permission cho role
  - Tìm Permission theo code
  - Kiểm tra đã tồn tại mapping chưa (tránh duplicate)
  - Tạo RolePermission record mới nếu chưa có
  - Lưu vào database

- `removePermissionFromRole(Role role, String permissionCode)`: Xóa permission khỏi role
  - Tìm Permission theo code
  - Tìm tất cả RolePermission records có role và permission này
  - Xóa các records đó

### 3.4. Security Configuration

**3.4.1. Method-level Security**

Spring Security được cấu hình để kiểm tra quyền ở mức method:

```java
@PreAuthorize("hasAuthority('PRODUCT_CREATE')")
public ResponseEntity<ProductResponse> createProduct(...) {
    // Chỉ user có PRODUCT_CREATE permission mới được gọi method này
}
```

**3.4.2. URL-level Security**

Trong `SecurityConfig`, các endpoint được bảo vệ bằng permission:

- `POST /api/products/**` → Yêu cầu `PRODUCT_CREATE`
- `PUT /api/products/**` → Yêu cầu `PRODUCT_UPDATE`
- `DELETE /api/products/**` → Yêu cầu `PRODUCT_DELETE`
- `GET /api/admin/orders/**` → Yêu cầu `ORDER_VIEW`
- `PUT /api/admin/orders/**` → Yêu cầu `ORDER_UPDATE`
- `GET /api/admin/transactions/**` → Yêu cầu `TRANSACTION_VIEW` hoặc `TRANSACTION_SUMMARY`

**3.4.3. Role-based Security**

Một số endpoint quan trọng vẫn dùng role (không thể thay đổi permission):

- `/api/admin/permissions/**` → Chỉ `ADMIN` role mới được truy cập
- `/admin/**` (pages) → Yêu cầu `ADMIN` hoặc `EDITOR` role

**Lý do**: Permission management là chức năng cực kỳ nhạy cảm, chỉ admin mới được quyền thay đổi permissions của các role khác.

### 3.5. Permission Management API

**3.5.1. Lấy danh sách permissions**

- `GET /api/admin/permissions`: Lấy tất cả permissions trong hệ thống
- `GET /api/admin/permissions/{permissionCode}`: Lấy thông tin chi tiết một permission

**3.5.2. Quản lý Role-Permission**

- `GET /api/admin/permissions/roles`: Lấy tất cả roles kèm permissions của mỗi role
- `GET /api/admin/permissions/roles/{roleName}`: Lấy permissions của một role cụ thể
- `GET /api/admin/permissions/roles/{roleName}/permissions`: Lấy danh sách permissions của role
- `POST /api/admin/permissions/roles/{roleName}/permissions`: Gán permission cho role
  - Request body: `{ "permissionCode": "PRODUCT_CREATE" }`
- `DELETE /api/admin/permissions/roles/{roleName}/permissions/{permissionCode}`: Xóa permission khỏi role

**3.5.3. Quản lý User-Role**

- `GET /api/admin/permissions/users`: Lấy tất cả users
- `GET /api/admin/permissions/users/{userId}/roles`: Lấy roles của user
- `GET /api/admin/permissions/users/{userId}/permissions`: Lấy tất cả permissions của user (từ role)
- `PUT /api/admin/permissions/users/{userId}/role`: Gán role cho user
  - Request body: `{ "roleName": "EDITOR" }`

**3.5.4. Quản lý Permission-Role (ngược lại)**

- `GET /api/admin/permissions/permissions/{permissionCode}/roles`: Lấy tất cả roles có permission này
- `GET /api/admin/permissions/permissions/{permissionCode}/users`: Lấy tất cả users có permission này (thông qua role)

### 3.6. Ưu điểm của hệ thống này

1. **Linh hoạt**: Dễ dàng thêm permission mới, gán cho role mới
2. **Bảo mật**: Kiểm soát chi tiết từng hành động, không phải chỉ dựa vào role
3. **Dễ audit**: Có thể biết chính xác user nào có quyền gì
4. **Scalable**: Khi hệ thống mở rộng, chỉ cần thêm permission mới, không cần thay đổi code nhiều

---

## 4. HỆ THỐNG TRACKING USER CHO STATISTICS

### 4.1. Tổng quan kiến trúc

Hệ thống tracking được thiết kế theo mô hình **Event-Driven Architecture** với các Audit Tables riêng biệt để lưu trữ dữ liệu thống kê.

**Tại sao dùng Audit Tables thay vì query trực tiếp từ Orders/Users?**
1. **Performance**: Audit tables chỉ chứa dữ liệu cần thiết, query nhanh hơn
2. **Data Integrity**: Dữ liệu thống kê được ghi ngay khi event xảy ra, không bị ảnh hưởng bởi việc update/delete sau này
3. **Historical Data**: Có thể lưu trữ lịch sử lâu dài mà không ảnh hưởng đến bảng chính
4. **Flexibility**: Có thể thêm metadata cho từng audit record

### 4.2. Các Audit Tables

**4.2.1. Revenue Audit Table**
- Mục đích: Lưu trữ doanh thu theo từng đơn hàng
- Cấu trúc:
  - `order_code`: Mã đơn hàng
  - `user_id`: ID người mua
  - `amount`: Số tiền đơn hàng
  - `status`: Trạng thái đơn hàng (CONFIRMED, DELIVERED, etc.)
  - `recorded_at`: Thời gian ghi nhận
- Khi nào ghi: Khi order được confirm (status chuyển sang CONFIRMED)

**4.2.2. Buyer Audit Table**
- Mục đích: Lưu trữ thông tin người mua (để đếm số lượng buyer unique)
- Cấu trúc:
  - `user_id`: ID người mua
  - `order_code`: Mã đơn hàng
  - `first_purchase_at`: Thời gian mua lần đầu (có thể dùng để phân tích)
  - `recorded_at`: Thời gian ghi nhận
- Unique constraint: (user_id, order_code) - mỗi user-order chỉ ghi 1 lần
- Khi nào ghi: Khi order được confirm, nhưng chỉ ghi nếu chưa tồn tại record cho user-order này

**4.2.3. Product Sale Audit Table**
- Mục đích: Lưu trữ chi tiết từng sản phẩm đã bán
- Cấu trúc:
  - `order_code`: Mã đơn hàng
  - `product_id`: ID sản phẩm
  - `quantity`: Số lượng bán
  - `unit_price`: Giá đơn vị
  - `total_price`: Tổng tiền (quantity × unit_price)
  - `recorded_at`: Thời gian ghi nhận
- Khi nào ghi: Khi order được confirm, ghi 1 record cho mỗi OrderItem

**4.2.4. New Customer Audit Table**
- Mục đích: Lưu trữ thông tin khách hàng mới đăng ký
- Cấu trúc:
  - `user_id`: ID user (unique)
  - `registered_at`: Thời gian đăng ký
- Khi nào ghi: Khi user đăng ký thành công (trigger UserRegisteredEvent)

### 4.3. Event-Driven Tracking

**4.3.1. Order Confirmed Event**

Khi một đơn hàng được confirm (sau khi thanh toán thành công), hệ thống sẽ:

1. **Publish Event**: `OrderService.confirmOrder()` sẽ publish `OrderConfirmedEvent` với Order object
2. **Listener nhận event**: `StatisticsAuditListener.handleOrderConfirmed()` được gọi tự động
3. **Async Processing**: Listener chạy async (`@Async`) để không block order processing
4. **Ghi Audit Records**:
   - Revenue Audit: Ghi doanh thu
   - Buyer Audit: Ghi người mua (nếu chưa có)
   - Product Sale Audit: Ghi từng sản phẩm

**Lý do dùng @Async:**
- Order confirmation là critical path, không được chậm
- Statistics tracking có thể chậm một chút không sao
- Nếu statistics tracking lỗi, không ảnh hưởng đến order processing

**4.3.2. User Registered Event**

Khi user đăng ký thành công:

1. **Publish Event**: `AuthService.register()` publish `UserRegisteredEvent`
2. **Listener nhận event**: `StatisticsAuditListener.handleUserRegistered()` được gọi
3. **Ghi Audit Record**: New Customer Audit (nếu chưa tồn tại)

### 4.4. Statistics Service Implementation

**4.4.1. Get Statistics (Tổng quan)**

Method `getStatistics(String period)` trả về thống kê tổng quan:

- **Input**: Period (DAY, WEEK, MONTH, QUARTER, YEAR)
- **Process**:
  1. Normalize period (chuyển về uppercase, validate)
  2. Tính start date và end date dựa trên period:
     - DAY: Từ 00:00:00 hôm nay đến 00:00:00 ngày mai
     - WEEK: Từ thứ 2 tuần này đến thứ 2 tuần sau
     - MONTH: Từ ngày 1 tháng này đến ngày 1 tháng sau
     - QUARTER: Từ tháng đầu quý đến tháng đầu quý sau
     - YEAR: Từ 1/1 năm này đến 1/1 năm sau
  3. Query từ audit tables:
     - Revenue: SUM từ revenue_audit
     - Buyer count: COUNT DISTINCT user_id từ buyer_audit
     - New customers: COUNT từ new_customer_audit
     - Products sold: SUM quantity từ product_sale_audit
  4. Fallback: Nếu audit table rỗng, query từ bảng gốc (Orders, Users, OrderItems)
- **Output**: StatisticsResponse với revenue, buyerCount, newCustomers, productsSold

**4.4.2. Get Statistics Detail (Chi tiết theo thời gian)**

Method `getStatisticsDetail(String period)` trả về thống kê chi tiết với data points:

- **Granularity (Độ chi tiết)**:
  - DAY period → HOUR granularity (24 data points)
  - WEEK period → DAY granularity (7 data points)
  - MONTH period → DAY granularity (30 data points)
  - YEAR period → MONTH granularity (12 data points)
- **Process**:
  1. Xác định granularity dựa trên period
  2. Generate time slots: Tạo danh sách các khoảng thời gian (ví dụ: 24 giờ trong ngày)
  3. Query từ database theo granularity:
     - Revenue by hour/day/month
     - Buyers by hour/day/month
     - New customers by hour/day/month
     - Products sold by hour/day/month
  4. Merge data: Kết hợp dữ liệu từ các queries vào các time slots
  5. Fill empty slots: Nếu một time slot không có dữ liệu, tạo data point với giá trị 0
- **Output**: StatisticsDetailResponse với mảng dataPoints, mỗi point có:
  - label: Nhãn thời gian (ví dụ: "10:00", "15/01")
  - startTime, endTime: Khoảng thời gian
  - revenue, buyerCount, newCustomers, productsSold: Giá trị thống kê

**4.4.3. Native Queries cho Performance**

Để query nhanh, hệ thống dùng native SQL queries với DATE_FORMAT:

```sql
-- Query revenue theo giờ
SELECT DATE_FORMAT(recorded_at, '%Y-%m-%d %H:00:00') as timeLabel,
       MIN(recorded_at) as startTime,
       MAX(recorded_at) as endTime,
       COALESCE(SUM(amount), 0) as revenue
FROM revenue_audit
WHERE status = 'CONFIRMED'
  AND recorded_at >= :start
  AND recorded_at < :end
GROUP BY DATE_FORMAT(recorded_at, '%Y-%m-%d %H:00:00')
ORDER BY timeLabel
```

**Lý do dùng native query:**
- DATE_FORMAT là function của MySQL, JPA không hỗ trợ trực tiếp
- Native query nhanh hơn JPQL cho các aggregation phức tạp
- Có thể tối ưu query bằng index trên recorded_at

### 4.5. Ưu điểm của hệ thống này

1. **Real-time**: Dữ liệu được ghi ngay khi event xảy ra, không cần batch job
2. **Performance**: Audit tables nhẹ, query nhanh, không ảnh hưởng đến bảng chính
3. **Reliability**: Nếu statistics tracking lỗi, không ảnh hưởng đến business logic
4. **Flexibility**: Dễ dàng thêm audit table mới cho metric mới
5. **Historical Data**: Có thể lưu trữ lịch sử lâu dài để phân tích xu hướng

---

## 5. API QUẢN LÝ (MANAGEMENT APIs)

### 5.1. Tổng quan

API quản lý được thiết kế để admin và editor quản lý hệ thống. Tất cả các API đều yêu cầu authentication và kiểm tra permission.

### 5.2. Order Management API

**5.2.1. Lấy danh sách orders**

- **Endpoint**: `GET /api/admin/orders`
- **Permission**: `ORDER_VIEW`
- **Query Parameters**:
  - `status` (optional): Filter theo OrderStatus (PENDING, CONFIRMED, PROCESSING, etc.)
  - `page` (default: 0): Số trang
  - `size` (default: 20): Số items mỗi trang
- **Response**: Page<OrderResponse> với pagination info
- **Logic**:
  1. Kiểm tra user có ORDER_VIEW permission không
  2. Query orders từ database với filter và pagination
  3. Map Order entities sang OrderResponse DTOs
  4. Return paginated response

**5.2.2. Lấy chi tiết order**

- **Endpoint**: `GET /api/admin/orders/{orderCode}`
- **Permission**: `ORDER_VIEW`
- **Response**: OrderDetailResponse với đầy đủ thông tin:
  - Order info (code, status, amounts, dates)
  - User info (name, email, phone)
  - Shipping address
  - Order items (products, quantities, prices)
  - Payment info (method, status, transaction ID)
- **Logic**:
  1. Tìm Order theo orderCode
  2. Load các relationships (user, address, items, payment)
  3. Map sang OrderDetailResponse

**5.2.3. Update order status**

- **Endpoint**: `PUT /api/admin/orders/{orderCode}/status`
- **Permission**: `ORDER_UPDATE`
- **Request Body**: `{ "status": "PROCESSING", "notes": "Đang chuẩn bị hàng" }`
- **Logic**:
  1. Tìm Order theo orderCode
  2. Validate status transition (ví dụ: không thể chuyển từ DELIVERED về PENDING)
  3. Update status và notes
  4. Lưu vào database
  5. Có thể trigger event nếu cần (ví dụ: gửi email thông báo)
- **Response**: OrderResponse với status mới

### 5.3. Transaction Management API

**5.3.1. Lấy danh sách transactions**

- **Endpoint**: `GET /api/admin/transactions`
- **Permission**: `TRANSACTION_VIEW` hoặc `TRANSACTION_SUMMARY`
- **Query Parameters**:
  - `type` (optional): TransactionType (PAYMENT, REFUND, DEPOSIT, WITHDRAWAL)
  - `status` (optional): TransactionStatus (PENDING, SUCCESS, FAILED)
  - `startDate` (optional): Filter từ ngày
  - `endDate` (optional): Filter đến ngày
  - `page`, `size`: Pagination
- **Response**: Page<TransactionResponse>
- **Logic**:
  1. Build query với các filters
  2. Query từ TransactionRepository với pagination
  3. Map sang TransactionResponse DTOs
  4. Return paginated response

**5.3.2. Lấy transaction summary**

- **Endpoint**: `GET /api/admin/transactions/summary`
- **Permission**: `TRANSACTION_SUMMARY`
- **Query Parameters**: Tương tự như trên (type, status, startDate, endDate)
- **Response**: TransactionSummaryResponse với:
  - `totalTransactions`: Tổng số giao dịch
  - `pendingTransactions`: Số giao dịch đang pending
  - `totalRevenue`: Tổng thu (REFUND + DEPOSIT, số dương)
  - `totalExpense`: Tổng chi (PAYMENT + WITHDRAWAL, số dương)
  - `netAmount`: Doanh thu ròng (revenue - expense)
- **Logic**:
  1. Đếm transactions theo filters
  2. Tính revenue: SUM amount của REFUND và DEPOSIT (status = SUCCESS)
  3. Tính expense: SUM |amount| của PAYMENT và WITHDRAWAL (status = SUCCESS)
  4. Tính net amount = revenue - expense

**5.3.3. Lấy chi tiết transaction**

- **Endpoint**: `GET /api/admin/transactions/{transactionId}`
- **Permission**: `TRANSACTION_VIEW`
- **Response**: TransactionResponse với đầy đủ thông tin:
  - Transaction info (code, type, amount, status, dates)
  - User info (name, email)
  - Order info (nếu có)
  - Payment info (nếu có)
  - External transaction ID

### 5.4. Statistics API

**5.4.1. Lấy statistics tổng quan**

- **Endpoint**: `GET /api/admin/statistics`
- **Query Parameters**:
  - `period` (optional): DAY, WEEK, MONTH, QUARTER, YEAR (default: DAY)
  - `startDate`, `endDate` (optional): Custom date range
- **Response**: StatisticsResponse với:
  - Period info (period, startDate, endDate)
  - Revenue (BigDecimal)
  - Buyer count (Long)
  - New customers (Long)
  - Products sold (Long)
- **Logic**: Đã giải thích ở phần 4.4.1

**5.4.2. Lấy statistics chi tiết**

- **Endpoint**: `GET /api/admin/statistics/detail`
- **Query Parameters**: Tương tự như trên
- **Response**: StatisticsDetailResponse với:
  - Period info
  - Granularity (HOUR, DAY, MONTH)
  - Data points array: Mỗi point có label, startTime, endTime, và các giá trị thống kê
- **Logic**: Đã giải thích ở phần 4.4.2

### 5.5. User Management API

**5.5.1. Ban user**

- **Endpoint**: `POST /api/admin/users/{userId}/ban`
- **Permission**: ADMIN role (không dùng permission vì đây là chức năng cực kỳ nhạy cảm)
- **Query Parameters**:
  - `reason` (optional): Lý do ban
- **Logic**:
  1. Kiểm tra admin không được tự ban chính mình
  2. Tìm user theo userId
  3. Set `banned = true`
  4. Lưu reason (có thể lưu vào bảng ban_history riêng)
  5. Có thể gửi email thông báo cho user
- **Response**: `{ "message": "User banned" }`

**5.5.2. Unban user**

- **Endpoint**: `POST /api/admin/users/{userId}/unban`
- **Permission**: ADMIN role
- **Logic**:
  1. Tìm user theo userId
  2. Set `banned = false`
  3. Lưu vào database
- **Response**: `{ "message": "User unbanned" }`

### 5.6. Permission Management API

Đã giải thích chi tiết ở phần 3.5.

### 5.7. Ưu điểm của API design

1. **RESTful**: Tuân thủ REST principles, dễ hiểu và sử dụng
2. **Pagination**: Tất cả list APIs đều có pagination để tránh load quá nhiều data
3. **Filtering**: Hỗ trợ filter theo nhiều tiêu chí (status, date range, type)
4. **Permission-based**: Mỗi API đều kiểm tra permission cụ thể
5. **Consistent Response**: Tất cả APIs đều trả về DTOs nhất quán, dễ xử lý ở frontend

---

## 6. CƠ SỞ DỮ LIỆU

### 6.1. Core Tables

**6.1.1. Users Table**
- Lưu trữ thông tin người dùng
- Quan trọng: `role` (ADMIN, EDITOR, USER), `enabled` (đã verify email chưa), `banned` (có bị ban không)

**6.1.2. Orders Table**
- Lưu trữ đơn hàng
- Quan trọng: `order_code` (unique, dùng làm PayOS order code), `status` (PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED)

**6.1.3. Payments Table**
- Lưu trữ thông tin thanh toán
- Quan trọng: `payment_link_id` (từ PayOS), `qr_code`, `checkout_url`, `status` (PENDING → PAID → FAILED)

**6.1.4. Transactions Table**
- Lưu trữ lịch sử giao dịch
- Quan trọng: `type` (PAYMENT, REFUND, etc.), `amount` (dương/âm), `transaction_code` (unique)

### 6.2. Audit Tables

Đã giải thích chi tiết ở phần 4.2.

### 6.3. Permission Tables

Đã giải thích chi tiết ở phần 3.2.

### 6.4. Relationships

- User → Orders (One-to-Many)
- Order → Payment (One-to-One)
- Order → OrderItems (One-to-Many)
- OrderItem → Product (Many-to-One)
- User → Transactions (One-to-Many)
- Transaction → Order (Many-to-One)
- Transaction → Payment (Many-to-One)
- Role → Permissions (Many-to-Many qua RolePermission)

---

## 7. DEPLOYMENT

### 7.1. Database Deployment (Railway)

**7.1.1. Tại sao chọn Railway?**
- Free tier cho MySQL
- Dễ setup, không cần cấu hình phức tạp
- Tự động backup
- Connection string dễ lấy

**7.1.2. Cấu hình**
- Tạo MySQL database trên Railway
- Lấy connection string
- Cập nhật vào `application.properties`:
  ```properties
  spring.datasource.url=jdbc:mysql://host:port/database
  spring.datasource.username=root
  spring.datasource.password=password
  ```

**7.1.3. Database Migration**
- Hibernate `ddl-auto=update`: Tự động tạo/update tables khi app start
- Có thể import data mẫu từ file SQL nếu cần

### 7.2. Application Deployment (Render)

**7.2.1. Tại sao chọn Render?**
- Free tier cho web services
- Hỗ trợ Docker
- Tự động deploy từ Git
- SSL certificate tự động

**7.2.2. Dockerfile**
```dockerfile
FROM eclipse-temurin:17-jdk  # JDK 17
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline  # Cache dependencies
COPY src ./src
RUN ./mvnw package -DskipTests  # Build JAR
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -jar target/*.jar"]
```

**7.2.3. Deploy Process**
1. Push code lên Git repository
2. Connect Render với Git repo
3. Render tự động build Docker image
4. Deploy container
5. App chạy trên port 8080

### 7.3. Domain Configuration

**7.3.1. Custom Domain: gigashop.id.vn**
- Đăng ký domain (có thể dùng free domain service)
- Cấu hình DNS:
  - Tạo CNAME record trỏ đến Render service URL
  - Hoặc A record trỏ đến Render IP
- Render tự động cấp SSL certificate (Let's Encrypt)

**7.3.2. Update Application Properties**
- Update `payos.return-url` và `payos.cancel-url` để dùng domain mới
- Update các URLs khác nếu cần

### 7.4. Environment Variables

Các thông tin nhạy cảm nên được lưu trong environment variables trên Render:
- Database credentials
- PayOS credentials
- JWT secret
- Email credentials
- Cloudinary credentials
- OAuth2 credentials

---

## KẾT LUẬN

Dự án E-Commerce này được xây dựng với các tính năng chính:

1. **Hệ thống thanh toán**: Tích hợp PayOS với webhook callback, transaction tracking, và status synchronization
2. **Hệ thống phân quyền**: RBAC với permission-based access control, linh hoạt và bảo mật
3. **Hệ thống tracking**: Event-driven audit system để thống kê real-time, performance cao
4. **API quản lý**: RESTful APIs với pagination, filtering, và permission checking

Tất cả các phần đều được thiết kế với các nguyên tắc:
- **Separation of Concerns**: Mỗi service có trách nhiệm rõ ràng
- **Security First**: Authentication, authorization, và data validation ở mọi layer
- **Performance**: Optimized queries, caching, async processing
- **Scalability**: Dễ dàng mở rộng với audit tables, event-driven architecture
- **Maintainability**: Clean code, consistent patterns, comprehensive documentation


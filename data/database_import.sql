-- =====================================================
-- SQL Script to Import Sample Data for E-Commerce
-- Database: spring_boot (MariaDB/MySQL)
-- =====================================================

-- =====================================================
-- 1. PROMOTION DATA
-- =====================================================
-- Note: Clear existing data first (optional)
-- TRUNCATE TABLE promotion;
-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE product;
-- TRUNCATE TABLE specification;
-- SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO promotion (name, description, discount_percent, start_date, end_date) VALUES
('Khuyến mãi Black Friday', 'Giảm giá lớn nhất năm, áp dụng cho tất cả sản phẩm', 30, '2025-01-01', '2025-12-31'),
('Khuyến mãi đầu năm', 'Chào mừng năm mới với mức giá ưu đãi', 20, '2025-01-01', '2025-03-31'),
('Giảm giá Laptop', 'Khuyến mãi đặc biệt cho dòng Laptop Gaming', 15, '2025-02-01', '2025-02-28'),
('Flash Sale PC Gaming', 'Sale flash trong 24h cho PC Gaming', 25, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY)),
('Khuyến mãi phụ kiện', 'Giảm giá cho tất cả phụ kiện máy tính', 10, '2025-01-01', '2025-06-30');

-- =====================================================
-- 2. PRODUCT DATA
-- =====================================================

-- LAPTOP Products
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Laptop Gaming ASUS ROG Strix G15', 'ASUS', 'Laptop gaming cao cấp với CPU AMD Ryzen 9, RTX 4070, 16GB RAM, SSD 1TB, màn hình 15.6 inch FHD 144Hz. Hiệu năng mạnh mẽ cho gaming và streaming.', 28990000, 15, 'https://via.placeholder.com/600x400?text=Laptop+ASUS+ROG', 'LAPTOP', 3),
('Laptop Dell XPS 15 9530', 'Dell', 'Laptop cao cấp cho công việc chuyên nghiệp, màn hình 4K OLED 15.6 inch, Intel Core i7 gen 13, 32GB RAM, SSD 1TB. Thiết kế mỏng nhẹ, sang trọng.', 45990000, 8, 'https://via.placeholder.com/600x400?text=Laptop+Dell+XPS', 'LAPTOP', NULL),
('Laptop HP Victus 16', 'HP', 'Laptop gaming giá rẻ với Intel Core i5, RTX 3050, 8GB RAM, SSD 512GB, màn hình 16.1 inch FHD 144Hz. Phù hợp cho game thủ có ngân sách hạn chế.', 18990000, 20, 'https://via.placeholder.com/600x400?text=Laptop+HP+Victus', 'LAPTOP', 2),
('Laptop Lenovo Legion 5 Pro', 'Lenovo', 'Laptop gaming màn hình 16 inch 2K 165Hz, AMD Ryzen 7, RTX 4060, 16GB RAM, SSD 512GB. Tản nhiệt tốt, phù hợp chơi game và render video.', 24990000, 12, 'https://via.placeholder.com/600x400?text=Laptop+Lenovo+Legion', 'LAPTOP', 3);

-- PC Products
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('PC Gaming Intel Core i7 RTX 4070', 'Custom Build', 'PC Gaming cấu hình mạnh: Intel Core i7-13700K, RTX 4070 12GB, 32GB RAM DDR5, SSD 1TB NVMe, Nguồn 850W 80 Plus Gold, Vỏ case RGB. Hiệu năng vượt trội cho game AAA.', 35990000, 5, 'https://via.placeholder.com/600x400?text=PC+Gaming+RTX4070', 'PC', 4),
('PC Workstation AMD Ryzen 9', 'Custom Build', 'PC Workstation chuyên nghiệp: AMD Ryzen 9 7950X, RTX 4080 16GB, 64GB RAM DDR5, SSD 2TB NVMe, Nguồn 1000W. Phù hợp cho render 3D, video editing, AI training.', 65990000, 3, 'https://via.placeholder.com/600x400?text=PC+Workstation+AMD', 'PC', 1),
('PC Gaming AMD Ryzen 5 RX 7600', 'Custom Build', 'PC Gaming tầm trung: AMD Ryzen 5 7600X, RX 7600 8GB, 16GB RAM DDR5, SSD 512GB NVMe, Nguồn 650W. Cấu hình cân bằng, giá hợp lý.', 19990000, 10, 'https://via.placeholder.com/600x400?text=PC+Gaming+RX7600', 'PC', 4);

-- KEYBOARD Products
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Bàn phím cơ Corsair K70 RGB', 'Corsair', 'Bàn phím cơ gaming cao cấp, switch Cherry MX Red, RGB per-key, khung nhôm, phím chống tràn nước. Phù hợp cho game thủ chuyên nghiệp.', 3290000, 25, 'https://via.placeholder.com/600x400?text=Keyboard+Corsair+K70', 'KEYBOARD', 5),
('Bàn phím cơ Logitech G Pro X', 'Logitech', 'Bàn phím cơ gaming esports, switch hot-swappable, thiết kế compact TKL, RGB, dây rút được. Được các game thủ chuyên nghiệp sử dụng.', 2990000, 30, 'https://via.placeholder.com/600x400?text=Keyboard+Logitech+GPro', 'KEYBOARD', 5),
('Bàn phím cơ Razer BlackWidow V4', 'Razer', 'Bàn phím cơ gaming với switch Razer Green, RGB Chroma, phím macro, bàn phím số. Thiết kế chắc chắn, độ bền cao.', 3490000, 20, 'https://via.placeholder.com/600x400?text=Keyboard+Razer+BW', 'KEYBOARD', 5),
('Bàn phím cơ Keychron K8 Pro', 'Keychron', 'Bàn phím cơ Bluetooth/wired, switch Gateron, thiết kế compact, hỗ trợ Mac/Windows. Phù hợp cho văn phòng và gaming nhẹ.', 2190000, 35, 'https://via.placeholder.com/600x400?text=Keyboard+Keychron+K8', 'KEYBOARD', NULL);

-- MOUSE Products
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Chuột gaming Logitech G Pro X Superlight', 'Logitech', 'Chuột gaming không dây siêu nhẹ 63g, sensor HERO 25K, micro switch, sạc USB-C. Được các game thủ esports yêu thích.', 2490000, 40, 'https://via.placeholder.com/600x400?text=Mouse+Logitech+GPro', 'MOUSE', 5),
('Chuột gaming Razer DeathAdder V3 Pro', 'Razer', 'Chuột gaming không dây, sensor Focus Pro 30K, thiết kế ergonomic, pin 90h. Phù hợp cho game FPS và MOBA.', 2990000, 35, 'https://via.placeholder.com/600x400?text=Mouse+Razer+DeathAdder', 'MOUSE', 5),
('Chuột gaming SteelSeries Rival 5', 'SteelSeries', 'Chuột gaming có dây, sensor TrueMove Air, 9 nút lập trình được, RGB Prism. Giá tốt, hiệu năng ổn định.', 1490000, 50, 'https://via.placeholder.com/600x400?text=Mouse+SteelSeries+Rival', 'MOUSE', 5),
('Chuột gaming Corsair Sabre RGB Pro', 'Corsair', 'Chuột gaming có dây, sensor 18,000 DPI, 8 nút lập trình, RGB. Thiết kế ergonomic cho tay phải.', 1290000, 45, 'https://via.placeholder.com/600x400?text=Mouse+Corsair+Sabre', 'MOUSE', NULL);

-- MONITOR Products
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Màn hình ASUS ROG Swift PG27UQ', 'ASUS', 'Màn hình gaming 27 inch 4K 144Hz, HDR 1000, G-Sync Ultimate, panel IPS. Màu sắc chân thực, độ trễ thấp.', 19990000, 6, 'https://via.placeholder.com/600x400?text=Monitor+ASUS+ROG+4K', 'MONITOR', NULL),
('Màn hình LG UltraGear 27GP850', 'LG', 'Màn hình gaming 27 inch 2K 180Hz, FreeSync/G-Sync, panel Nano IPS, 1ms response. Hiệu năng tốt, giá hợp lý.', 6990000, 15, 'https://via.placeholder.com/600x400?text=Monitor+LG+UltraGear', 'MONITOR', 1),
('Màn hình Samsung Odyssey G7', 'Samsung', 'Màn hình gaming cong 32 inch 2K 240Hz, HDR600, panel VA, FreeSync Premium Pro. Trải nghiệm gaming sống động.', 8990000, 10, 'https://via.placeholder.com/600x400?text=Monitor+Samsung+G7', 'MONITOR', NULL),
('Màn hình Dell S2721DGF', 'Dell', 'Màn hình gaming 27 inch 2K 165Hz, FreeSync/G-Sync, panel IPS, 1ms. Màu sắc chuẩn, phù hợp game và design.', 7990000, 12, 'https://via.placeholder.com/600x400?text=Monitor+Dell+S2721', 'MONITOR', 1);

-- HEADPHONE Products
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Tai nghe gaming SteelSeries Arctis Pro', 'SteelSeries', 'Tai nghe gaming cao cấp, driver 40mm, surround 7.1, mic retractable, dây rút được. Chất lượng âm thanh studio.', 4990000, 18, 'https://via.placeholder.com/600x400?text=Headphone+SteelSeries', 'HEADPHONE', NULL),
('Tai nghe gaming HyperX Cloud Alpha', 'HyperX', 'Tai nghe gaming có dây, driver 50mm, dây rút được, mic detachable. Giá tốt, chất lượng âm thanh ổn.', 1990000, 40, 'https://via.placeholder.com/600x400?text=Headphone+HyperX+Alpha', 'HEADPHONE', 5),
('Tai nghe gaming Razer BlackShark V2 Pro', 'Razer', 'Tai nghe gaming không dây, driver 50mm, mic detachable, pin 24h, surround THX. Chất lượng build tốt.', 4490000, 25, 'https://via.placeholder.com/600x400?text=Headphone+Razer+BSV2', 'HEADPHONE', NULL),
('Tai nghe gaming Corsair Virtuoso RGB', 'Corsair', 'Tai nghe gaming không dây, driver 50mm, mic detachable, RGB, pin 20h. Thiết kế sang trọng, chất lượng cao.', 3990000, 20, 'https://via.placeholder.com/600x400?text=Headphone+Corsair+Virt', 'HEADPHONE', 5);

-- ACCESSORY Products
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Bàn chuột gaming Logitech G640', 'Logitech', 'Bàn chuột gaming cỡ lớn 460x400mm, bề mặt mịn, viền khâu chống bong, độ dày 2mm. Tốc độ và độ chính xác cao.', 390000, 60, 'https://via.placeholder.com/600x400?text=Mousepad+Logitech', 'ACCESSORY', 5),
('Lót chuột Razer Goliathus Extended', 'Razer', 'Lót chuột gaming cỡ lớn 920x294mm, bề mặt vải mịn, đế cao su chống trượt. Phù hợp bàn phím + chuột.', 590000, 50, 'https://via.placeholder.com/600x400?text=Mousepad+Razer', 'ACCESSORY', 5),
('Webcam Logitech C920 HD Pro', 'Logitech', 'Webcam Full HD 1080p, auto focus, mic stereo, tương thích Windows/Mac. Chất lượng video tốt cho stream/meeting.', 2490000, 30, 'https://via.placeholder.com/600x400?text=Webcam+Logitech+C920', 'ACCESSORY', NULL),
('Tay đỡ màn hình Ergotron LX', 'Ergotron', 'Tay đỡ màn hình đơn, chịu tải 11.3kg, điều chỉnh độ cao, xoay 360 độ. Thiết kế chắc chắn, tiết kiệm không gian.', 3490000, 15, 'https://via.placeholder.com/600x400?text=Monitor+Arm+Ergotron', 'ACCESSORY', NULL);

-- =====================================================
-- 3. PRODUCT IMAGES (ElementCollection - product_images table)
-- =====================================================
-- Note: JPA @ElementCollection creates a separate table
-- Table structure varies by JPA implementation:
-- - Hibernate: table name = entityName_fieldName = "product_images"
-- - Columns: product_id (FK), images (VARCHAR/TEXT)
-- 
-- IMPORTANT: Run "DESCRIBE product_images;" first to check actual column names!
-- It might be: product_id, images OR product_product_id, images
--
-- Alternative: Update products via API or set images as JSON in description temporarily

-- Option 1: If table exists with columns (product_id, images):
INSERT INTO product_images (product_id, images) VALUES
(1, 'https://cdn2.cellphones.com.vn/x/media/catalog/product/a/s/asusus_2.png?_gl=1*1t5ypde*_gcl_aw*R0NMLjE3NjIxODU4MzUuQ2p3S0NBaUF3cUhJQmhBRUVpd0F4OWNUZWExcXZVV2o5Z2txODNSam9rRGZEWHl6dHpTLTZCbjRuSGxFRzZhNG1NVWZBNTlEODEyOER4b0M1RkVRQXZEX0J3RQ..*_gcl_au*NTQxMDUzNzkuMTc2MjE4NTgzNQ..*_ga*NDIzNDkxMjk3LjE3NjIxODU4MzU.*_ga_QLK8WFHNK9*czE3NjIxODU4MzQkbzEkZzAkdDE3NjIxODU4MzYkajU4JGwwJGgyMDU4MDE1OTk0'),
(1, 'https://cdn2.fptshop.com.vn/unsafe/1920x0/filters:format(webp):quality(75)/rog_strix_g16_2025_g615_1_b1f1f2f1bb.png'),
(1, 'https://down-vn.img.susercontent.com/file/60d48c8ffc8504ef2455d7488efb1e35@resize_w900_nl.webp'),
(2, 'https://cdn2.cellphones.com.vn/x/media/catalog/product/t/e/text_ng_n_10__5_210.png?_gl=1*numkec*_gcl_aw*R0NMLjE3NjIxODU5NzYuQ2p3S0NBaUF3cUhJQmhBRUVpd0F4OWNUZWNTdU9GbnJvR083R3JFVGFLQkgydmx5bTZTbXRzWkdwNERsN0xSdGF5OU5aaEY2b1FaWXFob0MzSGtRQXZEX0J3RQ..*_gcl_au*NTQxMDUzNzkuMTc2MjE4NTgzNQ..*_ga*NDIzNDkxMjk3LjE3NjIxODU4MzU.*_ga_QLK8WFHNK9*czE3NjIxODU4MzQkbzEkZzEkdDE3NjIxODU5NzgkajU4JGwwJGgyMDU4MDE1OTk0'),
(2, 'https://newtechshop.vn/wp-content/uploads/2025/10/9350-4.webp'),
(5, 'https://product.hstatic.net/200000722513/product/pc_gvn_rx6600_-_3_762ba90a94904a50809a93355cd819a7_master.png'),
(5, 'https://product.hstatic.net/200000722513/product/pc_gvn_rx6600_-_4_b0f0beb2642a42399c305f3638d2c364_master.png'),
(5, 'https://product.hstatic.net/200000722513/product/pc_gvn_rx6600_-_2_9577b2a53e584c10b3f50521079dd5cf_master.png'),
(5, 'https://via.placeholder.com/600x400?text=PC+Gaming+4https://product.hstatic.net/200000722513/product/pc_gvn_rx6600_-_5_b2d8837656c5443fac89ab0c84f93ba0_master.png');

-- Option 2: Update via API using ProductService after inserting products
-- OR manually update product table if images are stored differently

-- =====================================================
-- 4. SPECIFICATION DATA
-- =====================================================

-- Laptop ASUS ROG (Product ID: 1)
INSERT INTO specification (spec_name, spec_value, product_id) VALUES
('CPU', 'AMD Ryzen 9 7940HS', 1),
('GPU', 'NVIDIA RTX 4070 8GB', 1),
('RAM', '16GB DDR5 4800MHz', 1),
('Ổ cứng', 'SSD 1TB NVMe PCIe 4.0', 1),
('Màn hình', '15.6 inch FHD 144Hz IPS', 1),
('Pin', '90Wh', 1),
('Trọng lượng', '2.1 kg', 1),
('Hệ điều hành', 'Windows 11 Home', 1);

-- Laptop Dell XPS (Product ID: 2)
INSERT INTO specification (spec_name, spec_value, product_id) VALUES
('CPU', 'Intel Core i7-13700H', 2),
('GPU', 'NVIDIA RTX 4060 8GB', 2),
('RAM', '32GB DDR5 5200MHz', 2),
('Ổ cứng', 'SSD 1TB NVMe PCIe 4.0', 2),
('Màn hình', '15.6 inch 4K OLED Touch', 2),
('Pin', '86Wh', 2),
('Trọng lượng', '1.92 kg', 2),
('Hệ điều hành', 'Windows 11 Pro', 2);

-- PC Gaming RTX 4070 (Product ID: 5)
INSERT INTO specification (spec_name, spec_value, product_id) VALUES
('CPU', 'Intel Core i7-13700K', 5),
('GPU', 'NVIDIA RTX 4070 12GB', 5),
('RAM', '32GB DDR5 6000MHz', 5),
('Ổ cứng', 'SSD 1TB NVMe PCIe 4.0', 5),
('Mainboard', 'Z790 ATX', 5),
('Nguồn', '850W 80 Plus Gold', 5),
('Tản nhiệt CPU', 'AIO 240mm RGB', 5),
('Vỏ case', 'Mid Tower RGB Tempered Glass', 5);

-- Bàn phím Corsair K70 (Product ID: 9)
INSERT INTO specification (spec_name, spec_value, product_id) VALUES
('Switch', 'Cherry MX Red', 9),
('Layout', 'Full Size (104 phím)', 9),
('RGB', 'Per-key RGB', 9),
('Kết nối', 'USB-C có dây', 9),
('Khung', 'Nhôm CNC', 9),
('Phím', 'ABS Double-shot', 9);

-- Chuột Logitech G Pro X (Product ID: 13)
INSERT INTO specification (spec_name, spec_value, product_id) VALUES
('Sensor', 'HERO 25K DPI', 13),
('Kết nối', 'Wireless + USB Receiver', 13),
('Pin', 'Lên đến 70 giờ', 13),
('Trọng lượng', '63g', 13),
('Nút', '5 nút lập trình', 13),
('Độ trễ', '1ms (Lightspeed)', 13);

-- Màn hình LG UltraGear (Product ID: 17)
INSERT INTO specification (spec_name, spec_value, product_id) VALUES
('Kích thước', '27 inch', 17),
('Độ phân giải', '2560 x 1440 (2K)', 17),
('Tần số quét', '180Hz (OC)', 17),
('Panel', 'Nano IPS', 17),
('Độ trễ', '1ms (GTG)', 17),
('Màu sắc', '98% DCI-P3', 17),
('HDR', 'HDR10', 17),
('Sync', 'FreeSync/G-Sync Compatible', 17);

-- Tai nghe SteelSeries Arctis (Product ID: 21)
INSERT INTO specification (spec_name, spec_value, product_id) VALUES
('Driver', '40mm neodymium', 21),
('Tần số', '10Hz - 40kHz', 21),
('Trở kháng', '32 Ohm', 21),
('Kết nối', 'Có dây USB + 3.5mm', 21),
('Mic', 'Retractable, ClearCast', 21),
('Surround', 'DTS Headphone:X v2.0', 21);

-- =====================================================
-- NOTES & IMPORTANT INSTRUCTIONS:
-- =====================================================
-- 1. Table names are lowercase (JPA default):
--    - promotion
--    - product  
--    - specification
--    - product_images (for @ElementCollection - check actual name!)
--
-- 2. Column names use snake_case:
--    - discount_percent
--    - start_date
--    - end_date
--    - product_type
--    - main_image
--    - product_id
--    - spec_name
--    - spec_value
--
-- 3. BEFORE RUNNING:
--    a) Let JPA create tables first (run Spring Boot app)
--    b) Check table structure: DESCRIBE product_images;
--    c) Adjust INSERT statements if column names differ
--
-- 4. Product IDs are auto-generated - adjust in specification inserts if needed
--
-- 5. Promotion IDs reference from INSERT promotion above (IDs: 1-5)
--
-- 6. For MariaDB/MySQL:
--    - Use single quotes for strings
--    - Date format: 'YYYY-MM-DD'
--    - CURDATE() returns current date
--
-- 7. PRODUCT IMAGES:
--    @ElementCollection creates separate table - check structure first!
--    If table doesn't exist or has different columns, skip images section
--    You can add images later via API or update manually
--
-- 8. If you get foreign key errors, run in this order:
--    a) Promotions first
--    b) Products (they reference promotions)
--    c) Specifications (they reference products)
--
-- =====================================================
-- VERIFICATION QUERIES
-- =====================================================
-- Check promotions:
-- SELECT * FROM promotion;

-- Check products:
-- SELECT id, name, brand, price, product_type, promotion_id FROM product;

-- Check products with promotion info:
-- SELECT p.id, p.name, p.price, pr.name as promotion_name, pr.discount_percent 
-- FROM product p 
-- LEFT JOIN promotion pr ON p.promotion_id = pr.id;

-- Check specifications:
-- SELECT s.*, p.name as product_name 
-- FROM specification s 
-- JOIN product p ON s.product_id = p.id;

-- Check product images:
-- SELECT * FROM product_images;


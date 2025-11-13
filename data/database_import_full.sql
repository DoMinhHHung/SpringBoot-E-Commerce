-- =====================================================
-- SQL Script to Import FULL Sample Data for E-Commerce
-- Database: spring_boot (MariaDB/MySQL)
-- Purpose: Test all product types with comprehensive data
-- =====================================================

-- =====================================================
-- 1. CLEAR EXISTING DATA (Optional - Uncomment if needed)
-- =====================================================
-- SET FOREIGN_KEY_CHECKS = 0;
-- TRUNCATE TABLE specification;
-- TRUNCATE TABLE product_images;
-- TRUNCATE TABLE product;
-- TRUNCATE TABLE promotion;
-- SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 2. PROMOTION DATA
-- =====================================================
INSERT INTO promotion (name, description, discount_percent, start_date, end_date) VALUES
('Khuyến mãi Black Friday', 'Giảm giá lớn nhất năm, áp dụng cho tất cả sản phẩm', 30, '2025-01-01', '2025-12-31'),
('Khuyến mãi đầu năm', 'Chào mừng năm mới với mức giá ưu đãi', 20, '2025-01-01', '2025-03-31'),
('Giảm giá Laptop', 'Khuyến mãi đặc biệt cho dòng Laptop Gaming', 15, '2025-02-01', '2025-02-28'),
('Flash Sale PC Gaming', 'Sale flash trong 24h cho PC Gaming', 25, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 7 DAY)),
('Khuyến mãi phụ kiện', 'Giảm giá cho tất cả phụ kiện máy tính', 10, '2025-01-01', '2025-06-30'),
('Sale Gaming Gear', 'Giảm giá thiết bị gaming', 15, '2025-01-01', '2025-12-31'),
('Khuyến mãi màn hình', 'Giảm giá màn hình PC Gaming', 12, '2025-01-01', '2025-12-31');

-- =====================================================
-- 3. LAPTOP PRODUCTS (Full data for testing)
-- =====================================================

-- MacBook Series
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('MacBook Pro 14 inch M3', 'MacBook', 'Laptop Apple MacBook Pro 14 inch với chip M3, 18GB RAM, SSD 512GB, màn hình Liquid Retina XDR. Hiệu năng mạnh mẽ cho công việc chuyên nghiệp.', 45990000, 8, 'https://cdn2.cellphones.com.vn/insecure/rs:fill:0:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/m/a/macbook-pro-16-inch-m3-max-2023_1__5.png', 'LAPTOP', NULL),
('MacBook Pro 16 inch M4', 'MacBook', 'Laptop Apple MacBook Pro 16 inch với chip M4, 36GB RAM, SSD 1TB, màn hình Liquid Retina XDR. Phù hợp cho video editing và 3D rendering.', 69990000, 5, 'https://cdn2.cellphones.com.vn/insecure/rs:fill:0:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/t/e/text_ng_n_1__6_142.png', 'LAPTOP', 1),
('MacBook Air 14 inch M4', 'MacBook', 'Laptop Apple MacBook Air 14 inch với chip M4, 16GB RAM, SSD 256GB, màn hình Retina. Thiết kế mỏng nhẹ, pin lâu.', 32990000, 12, 'https://cdn2.cellphones.com.vn/insecure/rs:fill:0:358/q:90/plain/https://cellphones.com.vn/media/catalog/product/t/e/text_ng_n_2__9_15.png', 'LAPTOP', 2);

-- ASUS Laptops
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Laptop ASUS ROG Strix G15', 'ASUS', 'Laptop gaming cao cấp với CPU AMD Ryzen 9, RTX 4070, 16GB RAM, SSD 1TB, màn hình 15.6 inch FHD 144Hz. Hiệu năng mạnh mẽ cho gaming và streaming.', 28990000, 15, 'https://via.placeholder.com/600x400?text=ASUS+ROG+Strix+G15', 'LAPTOP', 3),
('Laptop ASUS TUF Gaming A15', 'ASUS', 'Laptop gaming tầm trung với AMD Ryzen 7, RTX 4060, 16GB RAM, SSD 512GB, màn hình 15.6 inch FHD 144Hz. Bền bỉ, giá tốt.', 21990000, 20, 'https://via.placeholder.com/600x400?text=ASUS+TUF+A15', 'LAPTOP', 3),
('Laptop ASUS ZenBook 14', 'ASUS', 'Laptop văn phòng mỏng nhẹ với Intel Core i7, 16GB RAM, SSD 512GB, màn hình 14 inch OLED. Thiết kế sang trọng, pin lâu.', 24990000, 18, 'https://via.placeholder.com/600x400?text=ASUS+ZenBook+14', 'LAPTOP', 2),
('Laptop ASUS VivoBook 15', 'ASUS', 'Laptop sinh viên với Intel Core i5, 8GB RAM, SSD 256GB, màn hình 15.6 inch FHD. Giá rẻ, phù hợp học tập.', 12990000, 25, 'https://via.placeholder.com/600x400?text=ASUS+VivoBook+15', 'LAPTOP', 2);

-- Lenovo Laptops
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Laptop Lenovo Legion 5 Pro', 'Lenovo', 'Laptop gaming màn hình 16 inch 2K 165Hz, AMD Ryzen 7, RTX 4060, 16GB RAM, SSD 512GB. Tản nhiệt tốt, phù hợp chơi game và render video.', 24990000, 12, 'https://via.placeholder.com/600x400?text=Lenovo+Legion+5+Pro', 'LAPTOP', 3),
('Laptop Lenovo ThinkPad X1 Carbon', 'Lenovo', 'Laptop doanh nhân cao cấp với Intel Core i7, 16GB RAM, SSD 512GB, màn hình 14 inch 2K. Bàn phím tốt, bền bỉ.', 32990000, 10, 'https://via.placeholder.com/600x400?text=Lenovo+ThinkPad+X1', 'LAPTOP', NULL),
('Laptop Lenovo IdeaPad 3', 'Lenovo', 'Laptop sinh viên với AMD Ryzen 5, 8GB RAM, SSD 256GB, màn hình 15.6 inch FHD. Giá tốt, đủ dùng.', 9990000, 30, 'https://via.placeholder.com/600x400?text=Lenovo+IdeaPad+3', 'LAPTOP', 2);

-- DELL Laptops
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Laptop Dell XPS 15 9530', 'DELL', 'Laptop cao cấp cho công việc chuyên nghiệp, màn hình 4K OLED 15.6 inch, Intel Core i7 gen 13, 32GB RAM, SSD 1TB. Thiết kế mỏng nhẹ, sang trọng.', 45990000, 8, 'https://via.placeholder.com/600x400?text=Dell+XPS+15', 'LAPTOP', NULL),
('Laptop Dell Alienware m16', 'DELL', 'Laptop gaming flagship với Intel Core i9, RTX 4080, 32GB RAM, SSD 2TB, màn hình 16 inch QHD 240Hz. Hiệu năng đỉnh cao.', 59990000, 5, 'https://via.placeholder.com/600x400?text=Dell+Alienware+m16', 'LAPTOP', 3),
('Laptop Dell Inspiron 15', 'DELL', 'Laptop văn phòng với Intel Core i5, 8GB RAM, SSD 256GB, màn hình 15.6 inch FHD. Giá hợp lý, đủ dùng.', 11990000, 22, 'https://via.placeholder.com/600x400?text=Dell+Inspiron+15', 'LAPTOP', 2);

-- HP Laptops
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Laptop HP Victus 16', 'hp', 'Laptop gaming giá rẻ với Intel Core i5, RTX 3050, 8GB RAM, SSD 512GB, màn hình 16.1 inch FHD 144Hz. Phù hợp cho game thủ có ngân sách hạn chế.', 18990000, 20, 'https://via.placeholder.com/600x400?text=HP+Victus+16', 'LAPTOP', 3),
('Laptop HP Pavilion 15', 'hp', 'Laptop văn phòng với Intel Core i5, 8GB RAM, SSD 256GB, màn hình 15.6 inch FHD. Thiết kế đẹp, giá tốt.', 13990000, 18, 'https://via.placeholder.com/600x400?text=HP+Pavilion+15', 'LAPTOP', 2),
('Laptop HP EliteBook 840', 'hp', 'Laptop doanh nhân với Intel Core i7, 16GB RAM, SSD 512GB, màn hình 14 inch FHD. Bảo mật tốt, bền bỉ.', 27990000, 12, 'https://via.placeholder.com/600x400?text=HP+EliteBook+840', 'LAPTOP', NULL);

-- Acer Laptops
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Laptop Acer Predator Helios 16', 'acer', 'Laptop gaming với Intel Core i7, RTX 4070, 16GB RAM, SSD 1TB, màn hình 16 inch QHD 165Hz. Tản nhiệt tốt, hiệu năng cao.', 26990000, 10, 'https://via.placeholder.com/600x400?text=Acer+Predator+Helios', 'LAPTOP', 3),
('Laptop Acer Aspire 5', 'acer', 'Laptop sinh viên với AMD Ryzen 5, 8GB RAM, SSD 256GB, màn hình 15.6 inch FHD. Giá rẻ, đủ dùng.', 10990000, 25, 'https://via.placeholder.com/600x400?text=Acer+Aspire+5', 'LAPTOP', 2);

-- MSI Laptops
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Laptop MSI Stealth 16 Studio', 'msi', 'Laptop gaming mỏng nhẹ với Intel Core i9, RTX 4070, 32GB RAM, SSD 2TB, màn hình 16 inch QHD 240Hz. Thiết kế cao cấp.', 49990000, 6, 'https://via.placeholder.com/600x400?text=MSI+Stealth+16', 'LAPTOP', 3),
('Laptop MSI Katana 15', 'msi', 'Laptop gaming giá rẻ với Intel Core i5, RTX 4050, 16GB RAM, SSD 512GB, màn hình 15.6 inch FHD 144Hz. Giá tốt.', 19990000, 15, 'https://via.placeholder.com/600x400?text=MSI+Katana+15', 'LAPTOP', 3);

-- LG Laptops
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Laptop LG Gram 17', 'LG', 'Laptop siêu nhẹ 1.35kg với Intel Core i7, 16GB RAM, SSD 512GB, màn hình 17 inch 2K. Pin lâu, thiết kế đẹp.', 31990000, 8, 'https://via.placeholder.com/600x400?text=LG+Gram+17', 'LAPTOP', 2);

-- GIGABYTE Laptops
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Laptop GIGABYTE AORUS 15', 'GIGABYTE', 'Laptop gaming với Intel Core i7, RTX 4060, 16GB RAM, SSD 1TB, màn hình 15.6 inch QHD 165Hz. Hiệu năng tốt.', 23990000, 10, 'https://via.placeholder.com/600x400?text=GIGABYTE+AORUS+15', 'LAPTOP', 3);

-- Samsung Laptops
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Laptop Samsung Galaxy Book3 Pro', 'Samsung', 'Laptop mỏng nhẹ với Intel Core i7, 16GB RAM, SSD 512GB, màn hình 14 inch AMOLED 2K. Màn hình đẹp, pin lâu.', 27990000, 12, 'https://via.placeholder.com/600x400?text=Samsung+Galaxy+Book3', 'LAPTOP', 2);

-- Microsoft Laptops
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Laptop Microsoft Surface Laptop 5', 'Microsoft', 'Laptop cao cấp với Intel Core i7, 16GB RAM, SSD 512GB, màn hình 13.5 inch PixelSense Touch. Thiết kế đẹp, màn hình cảm ứng.', 33990000, 8, 'https://via.placeholder.com/600x400?text=Microsoft+Surface+Laptop', 'LAPTOP', NULL);

-- =====================================================
-- 4. PC PRODUCTS (Full data for testing)
-- =====================================================

-- Build PC Gaming
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('PC Gaming Intel Core i7 RTX 4070', 'Custom Build', 'PC Gaming cấu hình mạnh: Intel Core i7-13700K, RTX 4070 12GB, 32GB RAM DDR5, SSD 1TB NVMe, Nguồn 850W 80 Plus Gold, Vỏ case RGB. Hiệu năng vượt trội cho game AAA.', 35990000, 5, 'https://via.placeholder.com/600x400?text=PC+Gaming+RTX4070', 'PC', 4),
('PC Gaming AMD Ryzen 5 RX 7600', 'Custom Build', 'PC Gaming tầm trung: AMD Ryzen 5 7600X, RX 7600 8GB, 16GB RAM DDR5, SSD 512GB NVMe, Nguồn 650W. Cấu hình cân bằng, giá hợp lý.', 19990000, 10, 'https://via.placeholder.com/600x400?text=PC+Gaming+RX7600', 'PC', 4),
('PC Gaming Intel Core i5 RTX 4060', 'Custom Build', 'PC Gaming entry-level: Intel Core i5-13400F, RTX 4060 8GB, 16GB RAM DDR4, SSD 512GB NVMe, Nguồn 650W. Phù hợp game thủ mới.', 16990000, 15, 'https://via.placeholder.com/600x400?text=PC+Gaming+RTX4060', 'PC', 4);

-- PC Workstation
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('PC Workstation AMD Ryzen 9', 'Custom Build', 'PC Workstation chuyên nghiệp: AMD Ryzen 9 7950X, RTX 4080 16GB, 64GB RAM DDR5, SSD 2TB NVMe, Nguồn 1000W. Phù hợp cho render 3D, video editing, AI training.', 65990000, 3, 'https://via.placeholder.com/600x400?text=PC+Workstation+AMD', 'PC', 1),
('PC Workstation Intel Xeon', 'Custom Build', 'PC Workstation server-grade: Intel Xeon W5-3435X, RTX 4090 24GB, 128GB RAM DDR5 ECC, SSD 4TB NVMe, Nguồn 1200W. Hiệu năng cực cao.', 129990000, 2, 'https://via.placeholder.com/600x400?text=PC+Workstation+Xeon', 'PC', NULL);

-- All In One PC
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('PC All In One Dell OptiPlex 7410', 'DELL', 'PC All In One văn phòng: Intel Core i7, 16GB RAM, SSD 512GB, màn hình 23.8 inch FHD Touch. Tiết kiệm không gian.', 18990000, 8, 'https://via.placeholder.com/600x400?text=PC+All+In+One+Dell', 'PC', NULL),
('PC All In One HP Pavilion 24', 'hp', 'PC All In One gia đình: Intel Core i5, 8GB RAM, SSD 256GB, màn hình 23.8 inch FHD. Giá tốt, đủ dùng.', 12990000, 12, 'https://via.placeholder.com/600x400?text=PC+All+In+One+HP', 'PC', 2);

-- PC Components - CPU
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('CPU Intel Core i9-13900K', 'Intel', 'CPU Intel Core i9-13900K, 24 cores (8P+16E), 32 threads, 5.8GHz boost, socket LGA1700. Hiệu năng đỉnh cao cho gaming và workstation.', 12990000, 15, 'https://via.placeholder.com/600x400?text=CPU+Intel+i9-13900K', 'PC', NULL),
('CPU AMD Ryzen 9 7950X', 'AMD', 'CPU AMD Ryzen 9 7950X, 16 cores, 32 threads, 5.7GHz boost, socket AM5. Hiệu năng đa nhân mạnh mẽ.', 11990000, 12, 'https://via.placeholder.com/600x400?text=CPU+AMD+Ryzen+9', 'PC', NULL),
('CPU Intel Core i7-13700K', 'Intel', 'CPU Intel Core i7-13700K, 16 cores (8P+8E), 24 threads, 5.4GHz boost, socket LGA1700. Cân bằng giá và hiệu năng.', 8990000, 20, 'https://via.placeholder.com/600x400?text=CPU+Intel+i7-13700K', 'PC', NULL);

-- PC Components - Mainboard
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Mainboard ASUS ROG Strix Z790-E', 'ASUS', 'Mainboard Intel Z790 ATX, socket LGA1700, DDR5, PCIe 5.0, WiFi 6E, RGB. Phù hợp build PC gaming cao cấp.', 8990000, 10, 'https://via.placeholder.com/600x400?text=Mainboard+ASUS+Z790', 'PC', NULL),
('Mainboard MSI MAG B650 Tomahawk', 'msi', 'Mainboard AMD B650 ATX, socket AM5, DDR5, PCIe 4.0, WiFi 6. Giá tốt, đủ tính năng.', 4990000, 15, 'https://via.placeholder.com/600x400?text=Mainboard+MSI+B650', 'PC', NULL);

-- PC Components - RAM
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('RAM DDR5 32GB (2x16GB) Corsair Vengeance', 'Corsair', 'RAM DDR5 32GB kit (2x16GB), 6000MHz, CL30, RGB. Hiệu năng cao, đẹp mắt.', 4990000, 25, 'https://via.placeholder.com/600x400?text=RAM+Corsair+32GB', 'PC', NULL),
('RAM DDR5 16GB (2x8GB) Kingston Fury Beast', 'Kingston', 'RAM DDR5 16GB kit (2x8GB), 5600MHz, CL36. Giá tốt, ổn định.', 2490000, 30, 'https://via.placeholder.com/600x400?text=RAM+Kingston+16GB', 'PC', NULL);

-- PC Components - Storage
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('SSD NVMe 1TB Samsung 990 Pro', 'Samsung', 'SSD NVMe PCIe 4.0 1TB, đọc 7450MB/s, ghi 6900MB/s. Hiệu năng đỉnh cao.', 3490000, 20, 'https://via.placeholder.com/600x400?text=SSD+Samsung+990+Pro', 'PC', NULL),
('SSD NVMe 512GB WD Black SN850X', 'Western Digital', 'SSD NVMe PCIe 4.0 512GB, đọc 7300MB/s, ghi 6300MB/s. Giá tốt.', 1790000, 25, 'https://via.placeholder.com/600x400?text=SSD+WD+SN850X', 'PC', NULL);

-- PC Components - PSU
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Nguồn 850W Corsair RM850x 80 Plus Gold', 'Corsair', 'Nguồn 850W 80 Plus Gold, modular, quạt 135mm, bảo hành 10 năm. Chất lượng cao.', 3490000, 15, 'https://via.placeholder.com/600x400?text=PSU+Corsair+850W', 'PC', NULL),
('Nguồn 650W Seasonic Focus GX-650 80 Plus Gold', 'Seasonic', 'Nguồn 650W 80 Plus Gold, modular, quạt 120mm. Giá tốt, ổn định.', 2490000, 20, 'https://via.placeholder.com/600x400?text=PSU+Seasonic+650W', 'PC', NULL);

-- PC Components - VGA
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('VGA NVIDIA RTX 4090 24GB ASUS ROG Strix', 'ASUS', 'VGA NVIDIA RTX 4090 24GB, hiệu năng đỉnh cao, RGB, 3 quạt. Phù hợp 4K gaming và AI.', 49990000, 3, 'https://via.placeholder.com/600x400?text=VGA+RTX+4090', 'PC', NULL),
('VGA NVIDIA RTX 4070 12GB MSI Gaming X', 'msi', 'VGA NVIDIA RTX 4070 12GB, hiệu năng tốt, RGB, 2 quạt. Cân bằng giá và hiệu năng.', 18990000, 8, 'https://via.placeholder.com/600x400?text=VGA+RTX+4070', 'PC', 4),
('VGA AMD RX 7600 8GB ASRock Challenger', 'ASRock', 'VGA AMD RX 7600 8GB, giá tốt, 2 quạt. Phù hợp 1080p gaming.', 8990000, 12, 'https://via.placeholder.com/600x400?text=VGA+RX+7600', 'PC', 4);

-- PC Components - Cooling
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Tản nhiệt AIO 240mm Corsair H100i RGB', 'Corsair', 'Tản nhiệt AIO 240mm, RGB, quạt 120mm x2, tương thích Intel/AMD. Làm mát tốt.', 3990000, 15, 'https://via.placeholder.com/600x400?text=AIO+Corsair+240mm', 'PC', NULL),
('Tản nhiệt khí Noctua NH-D15', 'Noctua', 'Tản nhiệt khí dual tower, 2 quạt 140mm, tương thích Intel/AMD. Yên tĩnh, hiệu quả.', 2490000, 18, 'https://via.placeholder.com/600x400?text=Cooler+Noctua+NH-D15', 'PC', NULL);

-- PC Components - Case
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Case Lian Li O11 Dynamic EVO RGB', 'Lian Li', 'Case Mid Tower, kính cường lực, RGB, hỗ trợ AIO 360mm, quạt 120mm x3. Thiết kế đẹp.', 4990000, 10, 'https://via.placeholder.com/600x400?text=Case+Lian+Li+O11', 'PC', NULL),
('Case Fractal Design Meshify 2', 'Fractal Design', 'Case Mid Tower, mesh front, quạt 140mm x2, hỗ trợ AIO 360mm. Thông gió tốt.', 3490000, 12, 'https://via.placeholder.com/600x400?text=Case+Fractal+Meshify', 'PC', NULL);

-- =====================================================
-- 5. KEYBOARD PRODUCTS (Full data for testing)
-- =====================================================

-- Logitech Keyboards
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Bàn phím cơ Logitech G Pro X', 'Logitech', 'Bàn phím cơ gaming esports, switch hot-swappable, thiết kế compact TKL, RGB, dây rút được. Được các game thủ chuyên nghiệp sử dụng.', 2990000, 30, 'https://via.placeholder.com/600x400?text=Keyboard+Logitech+GPro', 'KEYBOARD', 5),
('Bàn phím cơ Logitech G915 TKL', 'Logitech', 'Bàn phím cơ không dây, switch GL, thiết kế TKL, RGB, pin 40h. Cao cấp, không dây.', 4990000, 15, 'https://via.placeholder.com/600x400?text=Keyboard+Logitech+G915', 'KEYBOARD', 5),
('Bàn phím Logitech MX Keys', 'Logitech', 'Bàn phím văn phòng không dây, thiết kế ergonomic, backlight, pin 5 tháng. Phù hợp văn phòng.', 2490000, 25, 'https://via.placeholder.com/600x400?text=Keyboard+Logitech+MX', 'KEYBOARD', NULL);

-- Corsair Keyboards
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Bàn phím cơ Corsair K70 RGB', 'Corsair', 'Bàn phím cơ gaming cao cấp, switch Cherry MX Red, RGB per-key, khung nhôm, phím chống tràn nước. Phù hợp cho game thủ chuyên nghiệp.', 3290000, 25, 'https://via.placeholder.com/600x400?text=Keyboard+Corsair+K70', 'KEYBOARD', 5),
('Bàn phím cơ Corsair K65 RGB Mini', 'Corsair', 'Bàn phím cơ compact 60%, switch Cherry MX Red, RGB, USB-C. Nhỏ gọn, di động.', 2490000, 20, 'https://via.placeholder.com/600x400?text=Keyboard+Corsair+K65', 'KEYBOARD', 5);

-- Razer Keyboards
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Bàn phím cơ Razer BlackWidow V4', 'Razer', 'Bàn phím cơ gaming với switch Razer Green, RGB Chroma, phím macro, bàn phím số. Thiết kế chắc chắn, độ bền cao.', 3490000, 20, 'https://via.placeholder.com/600x400?text=Keyboard+Razer+BW', 'KEYBOARD', 5),
('Bàn phím cơ Razer Huntsman V2', 'Razer', 'Bàn phím cơ gaming với switch optical, RGB Chroma, thiết kế full-size. Phản hồi nhanh.', 3990000, 18, 'https://via.placeholder.com/600x400?text=Keyboard+Razer+Huntsman', 'KEYBOARD', 5);

-- SteelSeries Keyboards
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Bàn phím cơ SteelSeries Apex Pro', 'SteelSeries', 'Bàn phím cơ gaming với switch adjustable, RGB, OLED display, thiết kế full-size. Cao cấp, nhiều tính năng.', 4490000, 12, 'https://via.placeholder.com/600x400?text=Keyboard+SteelSeries+Apex', 'KEYBOARD', 5),
('Bàn phím cơ SteelSeries Apex 7 TKL', 'SteelSeries', 'Bàn phím cơ gaming TKL, switch Red, RGB, OLED display. Compact, đẹp.', 2990000, 15, 'https://via.placeholder.com/600x400?text=Keyboard+SteelSeries+Apex7', 'KEYBOARD', 5);

-- HyperX Keyboards
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Bàn phím cơ HyperX Alloy Elite 2', 'HyperX', 'Bàn phím cơ gaming, switch Red, RGB, thiết kế full-size, media controls. Giá tốt.', 2490000, 22, 'https://via.placeholder.com/600x400?text=Keyboard+HyperX+Alloy', 'KEYBOARD', 5);

-- ASUS ROG Keyboards
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Bàn phím cơ ASUS ROG Strix Scope II', 'ASUS ROG', 'Bàn phím cơ gaming, switch Red, RGB, thiết kế full-size, USB passthrough. Chất lượng tốt.', 3290000, 18, 'https://via.placeholder.com/600x400?text=Keyboard+ASUS+ROG+Scope', 'KEYBOARD', 5);

-- Cooler Master Keyboards
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Bàn phím cơ Cooler Master CK720', 'Cooler Master', 'Bàn phím cơ gaming TKL, switch Red, RGB, thiết kế compact. Giá tốt.', 1990000, 20, 'https://via.placeholder.com/600x400?text=Keyboard+Cooler+Master', 'KEYBOARD', 5);

-- Ducky Keyboards
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Bàn phím cơ Ducky One 3', 'Ducky', 'Bàn phím cơ gaming, switch Cherry MX, RGB, thiết kế full-size. Chất lượng build tốt.', 2990000, 15, 'https://via.placeholder.com/600x400?text=Keyboard+Ducky+One3', 'KEYBOARD', NULL);

-- =====================================================
-- 6. MOUSE PRODUCTS (Full data for testing)
-- =====================================================

-- Logitech Mice
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Chuột gaming Logitech G Pro X Superlight', 'Logitech', 'Chuột gaming không dây siêu nhẹ 63g, sensor HERO 25K, micro switch, sạc USB-C. Được các game thủ esports yêu thích.', 2490000, 40, 'https://via.placeholder.com/600x400?text=Mouse+Logitech+GPro', 'MOUSE', 5),
('Chuột gaming Logitech G502 X Plus', 'Logitech', 'Chuột gaming có dây, sensor HERO 25K, 11 nút lập trình, RGB, trọng lượng 89g. Nhiều tính năng.', 2290000, 35, 'https://via.placeholder.com/600x400?text=Mouse+Logitech+G502', 'MOUSE', 5),
('Chuột văn phòng Logitech MX Master 3S', 'Logitech', 'Chuột văn phòng không dây, sensor 8K DPI, scroll wheel MagSpeed, pin 70 ngày. Phù hợp văn phòng.', 2490000, 30, 'https://via.placeholder.com/600x400?text=Mouse+Logitech+MX+Master', 'MOUSE', NULL);

-- Razer Mice
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Chuột gaming Razer DeathAdder V3 Pro', 'Razer', 'Chuột gaming không dây, sensor Focus Pro 30K, thiết kế ergonomic, pin 90h. Phù hợp cho game FPS và MOBA.', 2990000, 35, 'https://via.placeholder.com/600x400?text=Mouse+Razer+DeathAdder', 'MOUSE', 5),
('Chuột gaming Razer Viper V2 Pro', 'Razer', 'Chuột gaming không dây, sensor Focus Pro 30K, trọng lượng 58g, pin 80h. Siêu nhẹ, phản hồi nhanh.', 2790000, 30, 'https://via.placeholder.com/600x400?text=Mouse+Razer+Viper+V2', 'MOUSE', 5);

-- Corsair Mice
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Chuột gaming Corsair Sabre RGB Pro', 'Corsair', 'Chuột gaming có dây, sensor 18,000 DPI, 8 nút lập trình, RGB. Thiết kế ergonomic cho tay phải.', 1290000, 45, 'https://via.placeholder.com/600x400?text=Mouse+Corsair+Sabre', 'MOUSE', NULL),
('Chuột gaming Corsair M65 RGB Ultra', 'Corsair', 'Chuột gaming có dây, sensor 26,000 DPI, 8 nút lập trình, RGB, trọng lượng 97g. Chắc chắn.', 1790000, 28, 'https://via.placeholder.com/600x400?text=Mouse+Corsair+M65', 'MOUSE', 5);

-- SteelSeries Mice
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Chuột gaming SteelSeries Rival 5', 'SteelSeries', 'Chuột gaming có dây, sensor TrueMove Air, 9 nút lập trình được, RGB Prism. Giá tốt, hiệu năng ổn định.', 1490000, 50, 'https://via.placeholder.com/600x400?text=Mouse+SteelSeries+Rival', 'MOUSE', 5),
('Chuột gaming SteelSeries Aerox 5', 'SteelSeries', 'Chuột gaming có dây, sensor TrueMove Air, 9 nút, RGB, trọng lượng 66g. Nhẹ, nhanh.', 1990000, 32, 'https://via.placeholder.com/600x400?text=Mouse+SteelSeries+Aerox', 'MOUSE', 5);

-- ASUS ROG Mice
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Chuột gaming ASUS ROG Gladius III', 'ASUS ROG', 'Chuột gaming có dây, sensor 26,000 DPI, 6 nút lập trình, RGB, switch hot-swappable. Nhiều tính năng.', 2290000, 25, 'https://via.placeholder.com/600x400?text=Mouse+ASUS+ROG+Gladius', 'MOUSE', 5);

-- HyperX Mice
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Chuột gaming HyperX Pulsefire Haste 2', 'HyperX', 'Chuột gaming có dây, sensor 26,000 DPI, 6 nút, RGB, trọng lượng 61g. Nhẹ, giá tốt.', 1490000, 38, 'https://via.placeholder.com/600x400?text=Mouse+HyperX+Pulsefire', 'MOUSE', 5);

-- Zowie Mice
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Chuột gaming Zowie EC2-C', 'Zowie', 'Chuột gaming có dây, sensor 3200 DPI, thiết kế ergonomic, không RGB. Được game thủ FPS yêu thích.', 1790000, 30, 'https://via.placeholder.com/600x400?text=Mouse+Zowie+EC2', 'MOUSE', NULL);

-- Glorious Mice
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Chuột gaming Glorious Model O', 'Glorious', 'Chuột gaming có dây, sensor 12,000 DPI, thiết kế honeycomb, RGB, trọng lượng 58g. Siêu nhẹ.', 1490000, 35, 'https://via.placeholder.com/600x400?text=Mouse+Glorious+Model+O', 'MOUSE', 5);

-- =====================================================
-- 7. MONITOR PRODUCTS (Full data for testing)
-- =====================================================

-- ASUS Monitors
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Màn hình ASUS ROG Swift PG27UQ', 'ASUS', 'Màn hình gaming 27 inch 4K 144Hz, HDR 1000, G-Sync Ultimate, panel IPS. Màu sắc chân thực, độ trễ thấp.', 19990000, 6, 'https://via.placeholder.com/600x400?text=Monitor+ASUS+ROG+4K', 'MONITOR', NULL),
('Màn hình ASUS TUF Gaming VG27AQ', 'ASUS', 'Màn hình gaming 27 inch 2K 165Hz, FreeSync/G-Sync, panel IPS, 1ms. Giá tốt, hiệu năng ổn.', 5990000, 15, 'https://via.placeholder.com/600x400?text=Monitor+ASUS+TUF+VG27', 'MONITOR', 7),
('Màn hình ASUS ProArt PA248QV', 'ASUS', 'Màn hình văn phòng 24 inch FHD, panel IPS, 99% sRGB, pivot/tilt. Phù hợp design và văn phòng.', 3990000, 20, 'https://via.placeholder.com/600x400?text=Monitor+ASUS+ProArt', 'MONITOR', NULL);

-- Samsung Monitors
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Màn hình Samsung Odyssey G7', 'SAMSUNG', 'Màn hình gaming cong 32 inch 2K 240Hz, HDR600, panel VA, FreeSync Premium Pro. Trải nghiệm gaming sống động.', 8990000, 10, 'https://via.placeholder.com/600x400?text=Monitor+Samsung+G7', 'MONITOR', NULL),
('Màn hình Samsung Odyssey G9', 'SAMSUNG', 'Màn hình gaming ultrawide 49 inch 2K 240Hz, HDR1000, panel VA, FreeSync Premium Pro. Siêu rộng, immersive.', 24990000, 4, 'https://via.placeholder.com/600x400?text=Monitor+Samsung+G9', 'MONITOR', 7),
('Màn hình Samsung Smart Monitor M7', 'SAMSUNG', 'Màn hình thông minh 32 inch 4K, Tizen OS, Smart TV, panel VA. Đa năng, tiện lợi.', 7990000, 12, 'https://via.placeholder.com/600x400?text=Monitor+Samsung+M7', 'MONITOR', NULL);

-- DELL Monitors
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Màn hình Dell S2721DGF', 'DELL', 'Màn hình gaming 27 inch 2K 165Hz, FreeSync/G-Sync, panel IPS, 1ms. Màu sắc chuẩn, phù hợp game và design.', 7990000, 12, 'https://via.placeholder.com/600x400?text=Monitor+Dell+S2721', 'MONITOR', 7),
('Màn hình Dell UltraSharp U2720Q', 'DELL', 'Màn hình văn phòng 27 inch 4K, panel IPS, 99% sRGB, USB-C. Chất lượng cao, phù hợp design.', 8990000, 10, 'https://via.placeholder.com/600x400?text=Monitor+Dell+UltraSharp', 'MONITOR', NULL);

-- LG Monitors
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Màn hình LG UltraGear 27GP850', 'LG', 'Màn hình gaming 27 inch 2K 180Hz, FreeSync/G-Sync, panel Nano IPS, 1ms response. Hiệu năng tốt, giá hợp lý.', 6990000, 15, 'https://via.placeholder.com/600x400?text=Monitor+LG+UltraGear', 'MONITOR', 7),
('Màn hình LG UltraWide 34WP65C', 'LG', 'Màn hình ultrawide 34 inch 2K, panel IPS, USB-C, 99% sRGB. Phù hợp multitasking.', 8990000, 8, 'https://via.placeholder.com/600x400?text=Monitor+LG+UltraWide', 'MONITOR', NULL);

-- MSI Monitors
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Màn hình MSI MAG274QRF-QD', 'msi', 'Màn hình gaming 27 inch 2K 165Hz, FreeSync/G-Sync, panel IPS, 1ms, Quantum Dot. Màu sắc đẹp.', 7990000, 12, 'https://via.placeholder.com/600x400?text=Monitor+MSI+MAG274', 'MONITOR', 7);

-- Acer Monitors
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Màn hình Acer Predator X27', 'acer', 'Màn hình gaming 27 inch 4K 144Hz, HDR1000, G-Sync Ultimate, panel IPS. Cao cấp, hiệu năng đỉnh.', 17990000, 5, 'https://via.placeholder.com/600x400?text=Monitor+Acer+Predator', 'MONITOR', NULL),
('Màn hình Acer Nitro XV272U', 'acer', 'Màn hình gaming 27 inch 2K 170Hz, FreeSync, panel IPS, 1ms. Giá tốt, hiệu năng ổn.', 5990000, 18, 'https://via.placeholder.com/600x400?text=Monitor+Acer+Nitro', 'MONITOR', 7);

-- XIAOMI Monitors
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Màn hình XIAOMI Mi Monitor 27', 'XIAOMI', 'Màn hình văn phòng 27 inch 2K, panel IPS, 99% sRGB, giá tốt. Phù hợp văn phòng và học tập.', 3990000, 20, 'https://via.placeholder.com/600x400?text=Monitor+XIAOMI+Mi+27', 'MONITOR', NULL);

-- ViewSonic Monitors
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Màn hình ViewSonic XG270QG', 'ViewSonic', 'Màn hình gaming 27 inch 2K 165Hz, G-Sync, panel IPS, 1ms. Chất lượng tốt.', 7990000, 10, 'https://via.placeholder.com/600x400?text=Monitor+ViewSonic+XG270', 'MONITOR', 7);

-- PHILIPS Monitors
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Màn hình PHILIPS 272E1CA', 'PHILIPS', 'Màn hình văn phòng 27 inch 2K, panel VA, FreeSync, USB-C. Giá tốt, đủ dùng.', 4990000, 15, 'https://via.placeholder.com/600x400?text=Monitor+PHILIPS+272E1', 'MONITOR', NULL);

-- AOC Monitors
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Màn hình AOC AGON AG273QXP', 'AOC', 'Màn hình gaming 27 inch 2K 165Hz, FreeSync/G-Sync, panel IPS, 1ms. Giá tốt, hiệu năng ổn.', 5990000, 14, 'https://via.placeholder.com/600x400?text=Monitor+AOC+AGON', 'MONITOR', 7);

-- =====================================================
-- 8. HEADPHONE PRODUCTS (Full data for testing)
-- =====================================================

-- Sony Headphones
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Tai nghe Sony WH-1000XM5', 'Sony', 'Tai nghe không dây chống ồn, driver 30mm, Bluetooth 5.2, pin 30h, chống ồn chủ động. Chất lượng âm thanh tuyệt vời.', 8990000, 15, 'https://via.placeholder.com/600x400?text=Headphone+Sony+WH1000', 'HEADPHONE', NULL),
('Tai nghe Sony WH-1000XM4', 'Sony', 'Tai nghe không dây chống ồn, driver 30mm, Bluetooth 5.0, pin 30h. Phiên bản trước, giá tốt hơn.', 6990000, 18, 'https://via.placeholder.com/600x400?text=Headphone+Sony+WH1000XM4', 'HEADPHONE', NULL);

-- Bose Headphones
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Tai nghe Bose QuietComfort 45', 'Bose', 'Tai nghe không dây chống ồn, driver 40mm, Bluetooth 5.1, pin 24h. Chống ồn tốt, thoải mái.', 7990000, 12, 'https://via.placeholder.com/600x400?text=Headphone+Bose+QC45', 'HEADPHONE', NULL);

-- Sennheiser Headphones
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Tai nghe Sennheiser HD 660S', 'Sennheiser', 'Tai nghe có dây audiophile, driver 38mm, trở kháng 150 Ohm. Chất lượng âm thanh studio.', 8990000, 8, 'https://via.placeholder.com/600x400?text=Headphone+Sennheiser+HD660', 'HEADPHONE', NULL);

-- Audio-Technica Headphones
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Tai nghe Audio-Technica ATH-M50x', 'Audio-Technica', 'Tai nghe có dây studio, driver 45mm, trở kháng 38 Ohm. Giá tốt, chất lượng ổn.', 3990000, 20, 'https://via.placeholder.com/600x400?text=Headphone+Audio+Technica', 'HEADPHONE', NULL);

-- HyperX Headphones
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Tai nghe gaming HyperX Cloud Alpha', 'HyperX', 'Tai nghe gaming có dây, driver 50mm, dây rút được, mic detachable. Giá tốt, chất lượng âm thanh ổn.', 1990000, 40, 'https://via.placeholder.com/600x400?text=Headphone+HyperX+Alpha', 'HEADPHONE', 5),
('Tai nghe gaming HyperX Cloud II', 'HyperX', 'Tai nghe gaming có dây, driver 53mm, USB sound card, mic detachable. Phổ biến, giá tốt.', 1790000, 35, 'https://via.placeholder.com/600x400?text=Headphone+HyperX+Cloud2', 'HEADPHONE', 5),
('Tai nghe gaming HyperX Cloud Flight', 'HyperX', 'Tai nghe gaming không dây, driver 50mm, pin 30h, mic detachable. Không dây, tiện lợi.', 2990000, 25, 'https://via.placeholder.com/600x400?text=Headphone+HyperX+Flight', 'HEADPHONE', 5);

-- SteelSeries Headphones
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Tai nghe gaming SteelSeries Arctis Pro', 'SteelSeries', 'Tai nghe gaming cao cấp, driver 40mm, surround 7.1, mic retractable, dây rút được. Chất lượng âm thanh studio.', 4990000, 18, 'https://via.placeholder.com/600x400?text=Headphone+SteelSeries', 'HEADPHONE', NULL),
('Tai nghe gaming SteelSeries Arctis 7P', 'SteelSeries', 'Tai nghe gaming không dây, driver 40mm, pin 24h, mic retractable. Phù hợp console và PC.', 3990000, 20, 'https://via.placeholder.com/600x400?text=Headphone+SteelSeries+7P', 'HEADPHONE', 5);

-- Razer Headphones
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Tai nghe gaming Razer BlackShark V2 Pro', 'Razer', 'Tai nghe gaming không dây, driver 50mm, mic detachable, pin 24h, surround THX. Chất lượng build tốt.', 4490000, 25, 'https://via.placeholder.com/600x400?text=Headphone+Razer+BSV2', 'HEADPHONE', NULL),
('Tai nghe gaming Razer Kraken V3', 'Razer', 'Tai nghe gaming có dây, driver 50mm, RGB Chroma, mic retractable. Giá tốt, đẹp mắt.', 2490000, 30, 'https://via.placeholder.com/600x400?text=Headphone+Razer+Kraken', 'HEADPHONE', 5);

-- Logitech Headphones
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Tai nghe gaming Corsair Virtuoso RGB', 'Logitech', 'Tai nghe gaming không dây, driver 50mm, mic detachable, RGB, pin 20h. Thiết kế sang trọng, chất lượng cao.', 3990000, 20, 'https://via.placeholder.com/600x400?text=Headphone+Corsair+Virt', 'HEADPHONE', 5),
('Tai nghe gaming Logitech G Pro X', 'Logitech', 'Tai nghe gaming có dây, driver 50mm, Blue VO!CE mic, dây rút được. Chất lượng mic tốt.', 2990000, 25, 'https://via.placeholder.com/600x400?text=Headphone+Logitech+GPro', 'HEADPHONE', 5);

-- =====================================================
-- 9. ACCESSORY PRODUCTS (Full data for testing)
-- =====================================================

-- USB Drive
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('USB Drive SanDisk Ultra 128GB', 'SanDisk', 'USB 3.0 128GB, tốc độ đọc 150MB/s, thiết kế nhỏ gọn, bảo hành 5 năm. Phù hợp lưu trữ và di chuyển dữ liệu.', 490000, 50, 'https://via.placeholder.com/600x400?text=USB+SanDisk+128GB', 'ACCESSORY', 5),
('USB Drive Kingston DataTraveler 64GB', 'Kingston', 'USB 3.0 64GB, tốc độ đọc 100MB/s, thiết kế bền, bảo hành 5 năm. Giá tốt.', 290000, 60, 'https://via.placeholder.com/600x400?text=USB+Kingston+64GB', 'ACCESSORY', 5);

-- Webcam
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Webcam Logitech C920 HD Pro', 'Logitech', 'Webcam Full HD 1080p, auto focus, mic stereo, tương thích Windows/Mac. Chất lượng video tốt cho stream/meeting.', 2490000, 30, 'https://via.placeholder.com/600x400?text=Webcam+Logitech+C920', 'ACCESSORY', NULL),
('Webcam Logitech C270 HD', 'Logitech', 'Webcam HD 720p, auto focus, mic, tương thích Windows/Mac. Giá tốt, đủ dùng.', 890000, 40, 'https://via.placeholder.com/600x400?text=Webcam+Logitech+C270', 'ACCESSORY', 5);

-- Microphone
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Microphone Blue Yeti', 'Blue', 'Microphone USB condenser, 4 pattern modes, gain control, thiết kế đẹp. Phù hợp stream và recording.', 3990000, 15, 'https://via.placeholder.com/600x400?text=Microphone+Blue+Yeti', 'ACCESSORY', NULL),
('Microphone HyperX SoloCast', 'HyperX', 'Microphone USB condenser, tap-to-mute, thiết kế compact, giá tốt. Phù hợp streamer.', 1490000, 25, 'https://via.placeholder.com/600x400?text=Microphone+HyperX+Solo', 'ACCESSORY', 5);

-- Speaker
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Speaker Logitech Z623 2.1', 'Logitech', 'Loa 2.1 200W, subwoofer, 2 satellite, điều khiển volume. Chất lượng tốt, giá hợp lý.', 2490000, 20, 'https://via.placeholder.com/600x400?text=Speaker+Logitech+Z623', 'ACCESSORY', NULL),
('Speaker Creative Pebble V3', 'Creative', 'Loa 2.0 USB-C, 8W, thiết kế compact, giá tốt. Phù hợp desktop nhỏ.', 490000, 35, 'https://via.placeholder.com/600x400?text=Speaker+Creative+Pebble', 'ACCESSORY', 5);

-- Hub USB
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Hub USB Anker 7-in-1 USB-C', 'Anker', 'Hub USB-C 7 cổng: USB 3.0 x3, USB-C, HDMI, SD card, PD charging. Tiện lợi, chất lượng tốt.', 1290000, 25, 'https://via.placeholder.com/600x400?text=Hub+USB+Anker+7in1', 'ACCESSORY', 5),
('Hub USB Belkin 4-Port USB 3.0', 'Belkin', 'Hub USB 3.0 4 cổng, thiết kế nhỏ gọn, giá tốt. Đủ dùng cho desktop.', 490000, 30, 'https://via.placeholder.com/600x400?text=Hub+USB+Belkin+4Port', 'ACCESSORY', 5);

-- Adapter
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Adapter USB-C to HDMI Anker', 'Anker', 'Adapter USB-C to HDMI 4K 60Hz, thiết kế nhỏ gọn, chất lượng tốt. Phù hợp laptop USB-C.', 490000, 28, 'https://via.placeholder.com/600x400?text=Adapter+Anker+USB-C', 'ACCESSORY', 5);

-- Cable
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Cable HDMI 2.1 Anker 2m', 'Anker', 'Cable HDMI 2.1 2m, hỗ trợ 4K 120Hz, 8K 60Hz, thiết kế bền. Chất lượng cao.', 390000, 40, 'https://via.placeholder.com/600x400?text=Cable+HDMI+Anker', 'ACCESSORY', 5),
('Cable USB-C to USB-C Belkin 1m', 'Belkin', 'Cable USB-C to USB-C 1m, hỗ trợ 100W PD, data 10Gbps. Chất lượng tốt.', 290000, 35, 'https://via.placeholder.com/600x400?text=Cable+USB-C+Belkin', 'ACCESSORY', 5);

-- Stand
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Tay đỡ màn hình Ergotron LX', 'Ergotron', 'Tay đỡ màn hình đơn, chịu tải 11.3kg, điều chỉnh độ cao, xoay 360 độ. Thiết kế chắc chắn, tiết kiệm không gian.', 3490000, 15, 'https://via.placeholder.com/600x400?text=Monitor+Arm+Ergotron', 'ACCESSORY', NULL),
('Tay đỡ màn hình VIVO Single', 'VIVO', 'Tay đỡ màn hình đơn, chịu tải 9kg, điều chỉnh độ cao, giá tốt. Phù hợp màn hình nhỏ.', 1290000, 20, 'https://via.placeholder.com/600x400?text=Monitor+Arm+VIVO', 'ACCESSORY', 5);

-- Mouse Pad
INSERT INTO product (name, brand, description, price, stock, main_image, product_type, promotion_id) VALUES
('Bàn chuột gaming Logitech G640', 'Logitech', 'Bàn chuột gaming cỡ lớn 460x400mm, bề mặt mịn, viền khâu chống bong, độ dày 2mm. Tốc độ và độ chính xác cao.', 390000, 60, 'https://via.placeholder.com/600x400?text=Mousepad+Logitech', 'ACCESSORY', 5),
('Lót chuột Razer Goliathus Extended', 'Razer', 'Lót chuột gaming cỡ lớn 920x294mm, bề mặt vải mịn, đế cao su chống trượt. Phù hợp bàn phím + chuột.', 590000, 50, 'https://via.placeholder.com/600x400?text=Mousepad+Razer', 'ACCESSORY', 5);

-- =====================================================
-- NOTES FOR IMPORTING:
-- =====================================================
-- 1. Run this script in DBeaver or MySQL/MariaDB client
-- 2. Make sure tables are created first (run Spring Boot app)
-- 3. Product IDs are auto-generated - adjust specification inserts if needed
-- 4. Check promotion IDs match (1-7)
-- 5. All prices are in VND (Vietnamese Dong)
-- 6. Main images use placeholder URLs - replace with actual images later
-- 7. This script creates comprehensive test data for all product types
-- 8. Total products: ~150+ items across all categories
-- =====================================================


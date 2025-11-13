-- --------------------------------------------------------
-- Host:                         localhost
-- Server version:               12.1.1-MariaDB - mariadb.org binary distribution
-- Server OS:                    Win64
-- HeidiSQL Version:             12.11.0.7065
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- Dumping data for table spring_boot.addresses: ~0 rows (approximately)

-- Dumping data for table spring_boot.cart: ~1 rows (approximately)
INSERT INTO `cart` (`id`, `user_id`) VALUES
    (1, 2);

-- Dumping data for table spring_boot.cart_item: ~1 rows (approximately)
INSERT INTO `cart_item` (`id`, `quantity`, `cart_id`, `product_id`) VALUES
    (3, 100, 1, 1);

-- Dumping data for table spring_boot.otps: ~0 rows (approximately)

-- Dumping data for table spring_boot.product: ~5 rows (approximately)
INSERT INTO `product` (`id`, `brand`, `description`, `main_image`, `name`, `price`, `product_type`, `stock`, `promotion_id`) VALUES
                                                                                                                                 (1, 'ASUS', 'laptop Asus ROG Zephyrus G14 GA403WR-QS156WS sở hữu màn hình OLED 3K 120Hz sắc nét, chip AMD Ryzen AI 9 HX 370 mạnh mẽ và GPU RTX 5070 Ti hiện đại. Với thiết kế mỏng nhẹ chỉ 1.57kg, mẫu laptop Asus ROG Zephyrus này mang tới sự linh hoạt khi di chuyển. Cùng với đó là viên pin 73Wh kèm sạc nhanh, giúp kéo dài thời gian sử dụng chỉ sau 1 lần sạc đầy.', 'https://res.cloudinary.com/dwbjnxpym/image/upload/v1763027950/hv7uleatlqq0jebix86a.png', 'Laptop Gaming ASUS ROG Strix G15', 65990000.00, 'LAPTOP', 100, NULL),
                                                                                                                                 (3, 'ASUS', 'Laptop ASUS TUF Gaming F16 FX607VU-RL045W sở hữu CPU Intel Core 5 210H, GPU rời RTX 4050 công suất 140W, hỗ trợ MUX Switch cùng với Advanced Optimus. Màn hình với 16 inch tần số quét 144Hz, RAM DDR5 16GB, ổ SSD PCIe 4.0 dung lượng 512GB. Máy có hệ thống âm thanh Dolby Atmos, Wi-Fi 6, bàn phím RGB và pin 56Wh hỗ trợ sạc nhanh.', 'https://res.cloudinary.com/dwbjnxpym/image/upload/v1763030508/bisdjmyblulhd81ekwdt.webp', 'Laptop ASUS TUF Gaming F16 FX607VU-RL045W', 25290000.00, 'LAPTOP', 100, NULL),
                                                                                                                                 (4, 'Intel', 'Máy tính văn phòng Intel i5 Gen 12 được trang bị bo mạch chủ Asrock H610M-HVS/M.2 R2.0 và bộ xử lý Intel Core i5 12400 cho công suất mạnh mẽ. Sản phẩm máy tính văn phòng này mang đến khả năng đa nhiệm ổn định và không gian lưu trữ lớn nhờ combo RAM Kingston 16GB và SSD dung lượng 256GB. Chất lượng hiển thị đỉnh cao với màn hình Dahua LM24-A200Y 24 độ phân giải FullHD và tốc độ làm tươi 100Hz.', 'https://res.cloudinary.com/dwbjnxpym/image/upload/v1763030686/nu0cnryyv8ehqby3proe.webp', 'PC CPS văn phòng Intel i5 Gen 12 - Kèm màn hình', 8590000.00, 'PC', 100, NULL),
                                                                                                                                 (5, 'Intel', 'PC CPS ASUS Gaming Intel i3 Gen 12 được trang bị vi xử lý Intel Core i3-12100F, đi kèm là Mainboard ASUS PRIME H610M-K D4, giúp máy hoạt động ổn định. Sản phẩm PC Gaming còn được gắn thêm dung lượng RAM 8GB và ổ cứng SSD 256GB. Ngoài ra, card đồ hoạ ASUS Dual Radeon RX 6500 XT OC 4GB DUAL-RX6500XT-O4G-V2 sẽ giúp hình ảnh đẹp hơn.', 'https://res.cloudinary.com/dwbjnxpym/image/upload/v1763030818/ze1zvzaiwzhtp5nryitp.webp', 'PC CPS X ASUS Gaming Intel i3 Gen 12 Kèm màn hình', 12590000.00, 'PC', 100, NULL),
                                                                                                                                 (6, 'Logitech', 'Máy tính văn phòng Intel i5 Gen 12 được trang bị bo mạch chủ Asrock H610M-HVS/M.2 R2.0 và bộ xử lý Intel Core i5 12400 cho công suất mạnh mẽ. Sản phẩm máy tính văn phòng này mang đến khả năng đa nhiệm ổn định và không gian lưu trữ lớn nhờ combo RAM Kingston 16GB và SSD dung lượng 256GB. Chất lượng hiển thị đỉnh cao với màn hình Dahua LM24-A200Y 24 độ phân giải FullHD và tốc độ làm tươi 100Hz.', 'https://res.cloudinary.com/dwbjnxpym/image/upload/v1763031013/rixt7qs93hap2v2afltp.webp', 'Chuột không dây Logitech MX Master 2S', 1390000.00, 'MOUSE', 999, NULL);

-- Dumping data for table spring_boot.product_images: ~15 rows (approximately)
INSERT INTO `product_images` (`product_id`, `images`) VALUES
                                                          (1, 'https://res.cloudinary.com/dwbjnxpym/image/upload/v1763027953/uq6odpy4qt1ohg1ewvdh.webp'),
                                                          (1, 'https://res.cloudinary.com/dwbjnxpym/image/upload/v1763027955/mxtr664pyrgx1ahbrbeb.webp'),
                                                          (1, 'https://res.cloudinary.com/dwbjnxpym/image/upload/v1763027957/ogvnwhvwlitktdxdd8gg.webp'),
                                                          (3, 'https://res.cloudinary.com/dwbjnxpym/image/upload/v1763030513/pqfjxkun5llwuaawtobt.webp'),
                                                          (3, 'https://res.cloudinary.com/dwbjnxpym/image/upload/v1763030516/qmtjpzfr22u8fvztjmwt.webp'),
                                                          (3, 'https://res.cloudinary.com/dwbjnxpym/image/upload/v1763030518/gwgtpwdymjziu82eb7lv.webp'),
                                                          (3, 'https://res.cloudinary.com/dwbjnxpym/image/upload/v1763030520/q7jn4azjzapgfrbau3lt.webp'),
                                                          (4, 'https://res.cloudinary.com/dwbjnxpym/image/upload/v1763030687/yfwfuroumpziyrn2gelg.webp'),
                                                          (4, 'https://res.cloudinary.com/dwbjnxpym/image/upload/v1763030689/x40orwnmjvyc9sxopesm.webp'),
                                                          (4, 'https://res.cloudinary.com/dwbjnxpym/image/upload/v1763030691/scxjjchchrb8sazbsoin.webp'),
                                                          (5, 'https://res.cloudinary.com/dwbjnxpym/image/upload/v1763030820/ojad7c5rm7rc6md2yyt3.webp'),
                                                          (5, 'https://res.cloudinary.com/dwbjnxpym/image/upload/v1763030822/t1oavs4kz2p1xxfwcqrg.webp'),
                                                          (6, 'https://res.cloudinary.com/dwbjnxpym/image/upload/v1763031015/nhfouqzjlrh5vekeppwm.webp'),
                                                          (6, 'https://res.cloudinary.com/dwbjnxpym/image/upload/v1763031017/o1gygoqpptihhuzdv8vx.webp'),
                                                          (6, 'https://res.cloudinary.com/dwbjnxpym/image/upload/v1763031018/icicwxqfwflq6ybxt7z0.webp');

-- Dumping data for table spring_boot.product_upload_job: ~0 rows (approximately)

-- Dumping data for table spring_boot.promotion: ~0 rows (approximately)

-- Dumping data for table spring_boot.specification: ~5 rows (approximately)
INSERT INTO `specification` (`id`, `spec_name`, `spec_value`, `product_id`) VALUES
                                                                                (1, 'Chip AI', 'AMD XDNA NPU up to 50TOPS', 1),
                                                                                (2, 'Loại card đồ họa', 'NVIDIA GeForce RTX 5070 Ti 12GB GDDR7 AMD Radeon Graphics', 1),
                                                                                (3, 'Dung lượng RAM', '32GB', 1),
                                                                                (4, 'Loại RAM', 'LPDDR5X 8000 Onboard', 1),
                                                                                (5, 'Ổ cứng', '1TB PCIe 4.0 NVMe M.2 SSD (1 Khe cắm M.2 hỗ trợ SATA hoặc NVMe, tối đa 2TB)', 1);

-- Dumping data for table spring_boot.users: ~2 rows (approximately)
INSERT INTO `users` (`id`, `auth_provider`, `created_at`, `dob`, `email`, `enabled`, `full_name`, `gender`, `password`, `phone`, `role`) VALUES
                                                                                                                                             (1, 'LOCAL', '2025-11-13 16:46:41.616618', NULL, 'admin@gmail.com', b'1', 'Admin', NULL, '$2a$10$mxi4KkG8lYEiMLcLVAomnuYBYe/c5KgDOH4YEIUZJ8ZQIl4IiQLIG', NULL, 'ADMIN'),
                                                                                                                                             (2, 'GOOGLE', '2025-11-13 17:12:50.529524', NULL, 'dominhhung04032003@gmail.com', b'1', 'Hùng Minh', NULL, NULL, NULL, 'USER');

-- Dumping data for table spring_boot.verification_tokens: ~0 rows (approximately)

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;

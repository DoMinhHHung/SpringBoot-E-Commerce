package iuh.fit.se.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    private String label;  // Nhãn địa chỉ (tùy chọn): "Nhà riêng", "Công ty", etc.

    @NotBlank(message = "Tên người nhận không được để trống")
    private String receiverName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String receiverPhone;

    @NotBlank(message = "Tỉnh/TP không được để trống")
    private String province;

    @NotBlank(message = "Phường/Xã không được để trống")
    private String ward;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    private String detail;

    private Boolean isDefault = false;

    // Tọa độ (optional - có thể null nếu user nhập tay không dùng map)
    private Double latitude;   // Vĩ độ
    private Double longitude; // Kinh độ
}


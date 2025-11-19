package iuh.fit.se.ecommerce.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    USER_NOT_FOUND(404, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    INVALID_OTP(400, "OTP không hợp lệ hoặc đã hết hạn", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS(409, "Email đã được sử dụng", HttpStatus.CONFLICT),
    PHONE_ALREADY_EXISTS(409, "Số điện thoại đã được sử dụng", HttpStatus.CONFLICT),
    WRONG_PASSWORD(400, "Mật khẩu cũ không đúng", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(400, "Mật khẩu không đúng", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(401, "Token không hợp lệ hoặc đã hết hạn", HttpStatus.UNAUTHORIZED),
    NOT_AUTHENTICATED(401, "Tài khoản chưa xác thực", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(401, "Bạn không có quyền truy cập", HttpStatus.UNAUTHORIZED),
    BAD_REQUEST(400, "Yêu cầu không hợp lệ", HttpStatus.BAD_REQUEST),
    INTERNAL_ERROR(500, "Lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR),
    NOT_FOUND(404, "Không tìm thấy", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus status;

    ErrorCode(int code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public int getCode() { return code; }
    public String getMessage() { return message; }
    public HttpStatus getStatus() { return status; }
}
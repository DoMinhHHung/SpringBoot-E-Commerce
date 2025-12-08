package iuh.fit.se.ecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping({ "/", "/index", "/index.html" })
    public String index() {
        return "index";
    }

    @GetMapping("/login.html")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register.html")
    public String register() {
        return "auth/register";
    }

    @GetMapping("/forgot-password.html")
    public String forgotPassword() {
        return "auth/forgot-password";
    }

    @GetMapping("/product-detail.html")
    public String productDetail() {
        return "product/product-detail";
    }

    @GetMapping("/profile.html")
    public String profile() {
        return "user/profile";
    }

    @GetMapping("/promotions.html")
    public String promotions() {
        return "promotion/promotions";
    }

    @GetMapping("/products.html")
    public String products() {
        return "products";
    }

    @GetMapping("/search-results.html")
    public String searchResults() {
        return "search-results";
    }

    @GetMapping("/oauth2/callback")
    public String oauth2Callback() {
        return "auth/oauth2-callback";
    }

    // Admin pages
    @GetMapping("/admin/dashboard.html")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/admin/products.html")
    public String adminProducts() {
        return "admin/products";
    }

    @GetMapping("/admin/promotions.html")
    public String adminPromotions() {
        return "admin/promotions";
    }

    @GetMapping("/admin/users.html")
    public String adminUsers() {
        return "admin/users";
    }

    @GetMapping("/admin/orders.html")
    public String adminOrders() {
        return "admin/orders";
    }

    @GetMapping("/admin/transactions.html")
    public String adminTransactions() {
        return "admin/transactions";
    }

    @GetMapping("/admin/support.html")
    public String adminSupport() {
        return "admin/support";
    }

    @GetMapping("/admin/permissions.html")
    public String adminPermissions() {
        return "admin/permissions";
    }

    @GetMapping("/payment-success.html")
    public String paymentSuccess() {
        return "payment/payment-success";
    }

    @GetMapping("/payment-cancel.html")
    public String paymentCancel() {
        return "payment/payment-cancel";
    }

    @GetMapping("/checkout.html")
    public String checkout() {
        return "checkout/checkout";
    }

    // Order pages
    @GetMapping("/orders.html")
    public String orders() {
        return "order/orders";
    }

    @GetMapping("/order-detail.html")
    public String orderDetail() {
        return "order/order-detail";
    }

    @GetMapping("/build-pc.html")
    public String buildPC() {
        return "buildPC/build-pc";
    }
}

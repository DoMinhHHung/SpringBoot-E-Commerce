package iuh.fit.se.ecommerce.controller;


import iuh.fit.se.ecommerce.dto.request.AddItemRequest;
import iuh.fit.se.ecommerce.dto.response.CartResponse;
import iuh.fit.se.ecommerce.service.interfaces.CartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

//    // Lay gio hang cua user id hien tai
//    @GetMapping
//    public CartResponse getCart(@AuthenticationPrincipal User user) {
//        return cartService.getCart(user.getId());
//    }
    //Thêm product vào cart với id user, id product, số lượng
    @PostMapping("/add")
    public CartResponse addItem(@RequestBody AddItemRequest request) {
        return cartService.addItem(request.getUserId(), request.getProductId(), request.getQuantity());
    }

    //Chỉnh sửa số lượng sp trong cart
    @PutMapping("/update")
    public CartResponse updateQuantity(@RequestBody AddItemRequest request) {
        return cartService.updateQuantity(request.getUserId(), request.getProductId(), request.getQuantity());
    }

    //Xoá sp trong cart
    @DeleteMapping("/remove")
    public void removeItem(@RequestParam Long userId, @RequestParam Long productId) {
        cartService.removeItem(userId, productId);
    }

    //hiển thị giỏ hàng theo id user
    @GetMapping("/{userId}")
    public CartResponse getCartByUser(@PathVariable Long userId) {
        return cartService.getCartByUser(userId);
    }

    //Xoá trắng giỏ hàng sau khi đặt hàng
    @DeleteMapping("/clear/{userId}")
    public void clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
    }
}

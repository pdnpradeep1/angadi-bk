package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.model.Cart;
import com.ecom.pradeep.angadi_bk.model.CartSummary;
import com.ecom.pradeep.angadi_bk.service.CartService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/add")
    public Cart addToCart(@RequestParam Long customerId, @RequestParam Long productId, @RequestParam int quantity) {
        return cartService.addToCart(customerId, productId, quantity);
    }

    @GetMapping("/{customerId}")
    public List<Cart> getCartItems(@PathVariable Long customerId) {
        return cartService.getCartItems(customerId);
    }

    @PutMapping("/update")
    public void updateCartItem(@RequestParam Long cartId, @RequestParam int quantity) {
        cartService.updateCartItem(cartId, quantity);
    }

    @DeleteMapping("/remove/{cartId}")
    public void removeCartItem(@PathVariable Long cartId) {
        cartService.removeCartItem(cartId);
    }

    @GetMapping("/summary/{customerId}")
    public CartSummary getCartSummary(
            @PathVariable Long customerId,
            @RequestParam(required = false) String discountCode
    ) {
        return cartService.getCartSummary(customerId, discountCode);
    }
}

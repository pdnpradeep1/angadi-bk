package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.model.*;
import com.ecom.pradeep.angadi_bk.repo.CartRepository;
import com.ecom.pradeep.angadi_bk.repo.DiscountRepository;
import com.ecom.pradeep.angadi_bk.repo.ProductRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final DiscountRepository discountRepository;


    private static final BigDecimal TAX_RATE = new BigDecimal("0.10"); // 10% tax

    public CartService(CartRepository cartRepository, ProductRepository productRepository, DiscountRepository discountRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.discountRepository = discountRepository;
    }

    public Cart addToCart(Long customerId, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Not enough stock available");
        }

        Cart cartItem = cartRepository.findByCustomerIdAndProductId(customerId, productId)
                .orElse(new Cart());

//        cartItem.setCustomer(new User(customerId));  // Assume User has constructor with ID
        cartItem.setProduct(product);
        cartItem.setQuantity(cartItem.getId() == null ? quantity : cartItem.getQuantity() + quantity);

        return cartRepository.save(cartItem);
    }

    public List<Cart> getCartItems(Long customerId) {
        return cartRepository.findByCustomerId(customerId);
    }

    public void updateCartItem(Long cartId, int quantity) {
        Cart cartItem = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (quantity <= 0) {
            cartRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartRepository.save(cartItem);
        }
    }

    public void removeCartItem(Long cartId) {
        cartRepository.deleteById(cartId);
    }

//    public CartSummary getCartSummary(Long customerId) {
//        List<Cart> cartItems = cartRepository.findByCustomerId(customerId);
//
//        BigDecimal subtotal = cartItems.stream()
//                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        BigDecimal tax = subtotal.multiply(TAX_RATE);
//        BigDecimal total = subtotal.add(tax);
//
//        return new CartSummary(subtotal, tax, total);
//    }

    public CartSummary getCartSummary(Long customerId, String discountCode) {
        List<Cart> cartItems = cartRepository.findByCustomerId(customerId);

        BigDecimal subtotal = cartItems.stream()
                .map(item -> item.getProduct().getPrice() // Convert double to BigDecimal
                        .multiply(BigDecimal.valueOf(item.getQuantity()))) // Multiply with quantity
                .reduce(BigDecimal.ZERO, BigDecimal::add); // Sum up the values


        BigDecimal discountAmount = BigDecimal.ZERO;
        if (discountCode != null && !discountCode.isEmpty()) {
            Discount discount = discountRepository.findByCodeAndActiveTrue(discountCode)
                    .orElseThrow(() -> new RuntimeException("Invalid or expired discount code"));

            if (discount.getPercentage() > 0) {
                discountAmount = subtotal.multiply(BigDecimal.valueOf(discount.getPercentage()).divide(BigDecimal.valueOf(100)));
            } else {
                discountAmount = discount.getDiscountAmount();
            }
        }

        BigDecimal discountedSubtotal = subtotal.subtract(discountAmount.max(BigDecimal.ZERO));
        BigDecimal tax = discountedSubtotal.multiply(TAX_RATE);
        BigDecimal total = discountedSubtotal.add(tax);

        return new CartSummary(subtotal, discountAmount, tax, total);
    }
}

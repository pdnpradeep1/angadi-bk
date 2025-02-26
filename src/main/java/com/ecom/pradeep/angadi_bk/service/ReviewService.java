package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.model.Product;
import com.ecom.pradeep.angadi_bk.model.Review;
import com.ecom.pradeep.angadi_bk.model.User;
import com.ecom.pradeep.angadi_bk.repo.OrderRepository;
import com.ecom.pradeep.angadi_bk.repo.ProductRepository;
import com.ecom.pradeep.angadi_bk.repo.ReviewRepository;
import com.ecom.pradeep.angadi_bk.repo.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public Review addReview(Long productId, Long customerId, int rating, String comment) {
        Optional<Product> product = productRepository.findById(productId);
        Optional<User> customer = userRepository.findById(customerId);
        if (product.isEmpty() || customer.isEmpty()) {
            throw new RuntimeException("Invalid product or customer.");
        }
        boolean hasPurchased = orderRepository.existsByCustomerIdAndProductId(customerId, productId);
        if (!hasPurchased) {
            throw new RuntimeException("Only customers who purchased this product can leave a review.");
        }

        Review review = new Review();
//        review.setProduct(new Product(productId)); // Assume Product has a constructor with ID
//        review.setCustomer(new User(customerId)); // Assume User has a constructor with ID
        review.setProduct(product.get());  // ✅ Correct way to associate Product
        review.setCustomer(customer.get()); // ✅ Correct way to associate User
        review.setRating(rating);
        review.setComment(comment);

        return reviewRepository.save(review);
    }

    public List<Review> getReviewsForProduct(Long productId) {
        return reviewRepository.findByProductId(productId);
    }
}

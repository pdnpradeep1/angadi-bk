package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.model.Product;
import com.ecom.pradeep.angadi_bk.model.Review;
import com.ecom.pradeep.angadi_bk.model.User;
import com.ecom.pradeep.angadi_bk.repo.OrderRepository;
import com.ecom.pradeep.angadi_bk.repo.ProductRepository;
import com.ecom.pradeep.angadi_bk.repo.ReviewRepository;
import com.ecom.pradeep.angadi_bk.repo.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public Page<Review> getReviewsForProduct(Long productId, String sortBy, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt"); // Default: latest first

        if ("highest".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "rating"); // Highest rating first
        } else if ("lowest".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "rating"); // Lowest rating first
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        return reviewRepository.findByProductId(productId, pageable);
    }

    public List<Review> getReviewsForProduct(Long productId, String sortBy) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt"); // Default: latest first

        if ("highest".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "rating"); // Highest rating first
        } else if ("lowest".equalsIgnoreCase(sortBy)) {
            sort = Sort.by(Sort.Direction.ASC, "rating"); // Lowest rating first
        }

        return reviewRepository.findByProductId(productId, sort);
    }

    private void updateProductRating(Long productId) {
        double avgRating = reviewRepository.calculateAverageRating(productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setAverageRating(avgRating);
        productRepository.save(product);
    }
}

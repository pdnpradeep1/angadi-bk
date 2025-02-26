package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.model.Review;
import com.ecom.pradeep.angadi_bk.service.ReviewService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/{productId}")
    public Review addReview(
            @PathVariable Long productId,
            @RequestParam Long customerId,
            @RequestParam int rating,
            @RequestParam String comment
    ) {
        return reviewService.addReview(productId, customerId, rating, comment);
    }

    @GetMapping("/{productId}")
    public List<Review> getReviewsForProduct(@PathVariable Long productId) {
        return reviewService.getReviewsForProduct(productId);
    }
}


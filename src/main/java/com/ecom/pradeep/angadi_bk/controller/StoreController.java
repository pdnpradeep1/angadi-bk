package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.model.Store;
import com.ecom.pradeep.angadi_bk.model.StoreRevenue;
import com.ecom.pradeep.angadi_bk.model.StoreStatusUpdateRequest;
import com.ecom.pradeep.angadi_bk.model.User;
import com.ecom.pradeep.angadi_bk.service.StoreService;
import com.ecom.pradeep.angadi_bk.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<Store> createStore(@RequestBody Store store, @AuthenticationPrincipal UserDetails userDetails) {
        store.setOwner(userService.findByEmail(userDetails.getUsername()));
        Store createdStore = storeService.createStore(store);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStore);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Store> updateStore(@PathVariable Long id, @RequestBody Store store, @AuthenticationPrincipal UserDetails userDetails) {
        Store existingStore = storeService.getStoreById(id);
        if (!existingStore.getOwner().getEmail().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Store updatedStore = storeService.updateStore(id, store);
        return ResponseEntity.ok(updatedStore);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        Store existingStore = storeService.getStoreById(id);
        if (!existingStore.getOwner().getEmail().equals(userDetails.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        storeService.deleteStore(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-stores")
    public ResponseEntity<List<Store>> getMyStores(@AuthenticationPrincipal UserDetails userDetails) {
        User owner = userService.findByEmail(userDetails.getUsername());
        List<Store> stores = storeService.getStoresByOwner(owner.getId());
        return ResponseEntity.ok(stores);
    }

    @GetMapping("/stores/{StoreId}")
    public ResponseEntity<Store> getMyStores(@PathVariable Long StoreId, @AuthenticationPrincipal UserDetails userDetails) {
        User owner = userService.findByEmail(userDetails.getUsername());
        Store storeDetails = storeService.getStoreById(StoreId);
        if (storeDetails.getOwner().getEmail().equals(owner.getEmail())) {
            return ResponseEntity.ok(storeDetails);
        } else {
            throw new IllegalAccessError("User does not have access permission for this store ");
        }
    }

    @PatchMapping("/stores/{StoreId}")
    public ResponseEntity<Store> updateVisibility(@PathVariable Long StoreId, @RequestBody StoreStatusUpdateRequest storeRequest, @AuthenticationPrincipal UserDetails userDetails) {
        User owner = userService.findByEmail(userDetails.getUsername());
        Store store = storeService.setStoreVisibility(StoreId, storeRequest.isVisible(), owner.getEmail());
        return ResponseEntity.ok(store);
    }

    @GetMapping("/visible-stores")
    public ResponseEntity<List<Store>> getVisibleStores() {
        List<Store> stores = storeService.getVisibleStores();
        return ResponseEntity.ok(stores);
    }

    @PutMapping("/{storeId}/visibility")
    public ResponseEntity<Store> updateVisibility(@PathVariable Long storeId, @RequestParam boolean visible) {
        String email = getAuthenticatedUserEmail();
        Store updatedStore = storeService.setStoreVisibility(storeId, visible, email);
        return ResponseEntity.ok(updatedStore);
    }

    private String getAuthenticatedUserEmail() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            throw new RuntimeException("User authentication not found");
        }
    }

    @PostMapping("/{storeId}/visit")
    public ResponseEntity<String> trackVisit(@PathVariable Long storeId) {
        storeService.incrementVisitorCount(storeId);
        return ResponseEntity.ok("Visitor counted");
    }

    @PostMapping("/{storeId}/order")
    public ResponseEntity<String> trackOrder(@PathVariable Long storeId) {
        storeService.incrementOrderCount(storeId);
        return ResponseEntity.ok("Order counted");
    }

    @GetMapping("/{storeId}/analytics")
    public ResponseEntity<Store> getStoreAnalytics(@PathVariable Long storeId) {
        return ResponseEntity.ok(storeService.getStoreAnalytics(storeId));
    }

    @PostMapping("/{storeId}/revenue")
    public ResponseEntity<String> addRevenue(@PathVariable Long storeId, @RequestParam BigDecimal amount) {
        storeService.recordRevenue(storeId, amount);
        return ResponseEntity.ok("Revenue recorded");
    }

    @GetMapping("/{storeId}/revenue")
    public ResponseEntity<BigDecimal> getRevenueBetweenDates(
            @PathVariable Long storeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(storeService.getRevenueBetweenDates(storeId, startDate, endDate));
    }

    @GetMapping("/{storeId}/revenue/breakdown")
    public ResponseEntity<List<StoreRevenue>> getRevenueBreakdown(
            @PathVariable Long storeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ResponseEntity.ok(storeService.getRevenueBreakdown(storeId, startDate, endDate));
    }

    @PutMapping("/{storeId}/domain")
    public ResponseEntity<Store> updateCustomDomain(@PathVariable Long storeId, @RequestParam String domain) {
        String email = getAuthenticatedUserEmail();
        Store updatedStore = storeService.addCustomDomain(storeId, domain, email);
        return ResponseEntity.ok(updatedStore);
    }

    @GetMapping("/domain/{domain}")
    public ResponseEntity<Store> getStoreByDomain(@PathVariable String domain) {
        return ResponseEntity.ok(storeService.getStoreByDomain(domain));
    }


}

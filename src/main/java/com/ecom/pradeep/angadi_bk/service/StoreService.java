package com.ecom.pradeep.angadi_bk.service;

import com.ecom.pradeep.angadi_bk.exceptions.ResourceNotFoundException;
import com.ecom.pradeep.angadi_bk.model.Store;
import com.ecom.pradeep.angadi_bk.model.StoreRevenue;
import com.ecom.pradeep.angadi_bk.repo.StoreRepository;
import com.ecom.pradeep.angadi_bk.repo.StoreRevenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class StoreService {
    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreRevenueRepository storeRevenueRepository;

    public Store createStore(Store store) {
        return storeRepository.save(store);
    }

    public Store updateStore(Long id, Store updatedStore) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));
        store.setName(updatedStore.getName());
        store.setDescription(updatedStore.getDescription());
        store.setAddress(updatedStore.getAddress());
        store.setVisible(updatedStore.isVisible());
        return storeRepository.save(store);
    }

    public void deleteStore(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));
        storeRepository.delete(store);
    }

    public List<Store> getStoresByOwner(Long ownerId) {
        return storeRepository.findByOwnerId(ownerId);
    }

    public Store getStoreById(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id: " + id));
    }

    public Store setStoreVisibility(Long storeId, boolean visible, String ownerEmail) {
        Store store = getStoreById(storeId);
        if (!store.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("You are not authorized to update this store");
        }
        store.setVisible(visible);
        return storeRepository.save(store);
    }

    public List<Store> getVisibleStores() {
        return storeRepository.findByVisibleTrue(); // Only return active stores
    }


    @Transactional
    public void incrementVisitorCount(Long storeId) {
        Store store = getStoreById(storeId);
        store.setVisitorCount(store.getVisitorCount() + 1);
        storeRepository.save(store);
    }

    @Transactional
    public void incrementOrderCount(Long storeId) {
        Store store = getStoreById(storeId);
        store.setOrderCount(store.getOrderCount() + 1);
        storeRepository.save(store);
    }

    public Store getStoreAnalytics(Long storeId) {
        return getStoreById(storeId);
    }

    @Transactional
    public void incrementRevenue(Long storeId, BigDecimal amount) {
        Store store = getStoreById(storeId);
        store.setTotalRevenue(store.getTotalRevenue().add(amount));
        storeRepository.save(store);
    }

    @Transactional
    public void recordRevenue(Long storeId, BigDecimal amount) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        store.setTotalRevenue(store.getTotalRevenue().add(amount));
        storeRepository.save(store);

        StoreRevenue revenue = new StoreRevenue();
        revenue.setStoreId(storeId);
        revenue.setAmount(amount);
        revenue.setDate(LocalDate.now()); // ✅ Track revenue per day
        storeRevenueRepository.save(revenue);
    }

    public BigDecimal getRevenueBetweenDates(Long storeId, LocalDate startDate, LocalDate endDate) {
        return storeRevenueRepository.sumRevenueBetweenDates(storeId, startDate, endDate)
                .orElse(BigDecimal.ZERO);
    }

    public List<StoreRevenue> getRevenueBreakdown(Long storeId, LocalDate startDate, LocalDate endDate) {
        return storeRevenueRepository.findRevenueBetweenDates(storeId, startDate, endDate);
    }

    public Store addCustomDomain(Long storeId, String domain, String ownerEmail) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found"));

        if (!store.getOwner().getEmail().equals(ownerEmail)) {
            throw new RuntimeException("You are not authorized to update this store");
        }

        if (!isValidDomain(domain)) {
            throw new RuntimeException("Invalid domain format");
        }

        store.setCustomDomain(domain);
        return storeRepository.save(store);
    }

    public Store getStoreByDomain(String domain) {
        return storeRepository.findByCustomDomain(domain)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found for domain: " + domain));
    }

    private boolean isValidDomain(String domain) {
        String domainRegex = "^(?!-)[A-Za-z0-9-]+(\\.[A-Za-z]{2,})+$";
        return Pattern.matches(domainRegex, domain);
    }

}

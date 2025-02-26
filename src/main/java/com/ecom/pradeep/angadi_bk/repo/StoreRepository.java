package com.ecom.pradeep.angadi_bk.repo;

import com.ecom.pradeep.angadi_bk.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    List<Store> findByOwnerId(Long ownerId);
    List<Store> findByVisibleTrue();
    Optional<Store> findByCustomDomain(String customDomain);

}

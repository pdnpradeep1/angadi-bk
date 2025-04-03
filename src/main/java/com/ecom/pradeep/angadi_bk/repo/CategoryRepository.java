package com.ecom.pradeep.angadi_bk.repo;

import com.ecom.pradeep.angadi_bk.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByStoreId(Long storeId);

    Optional<Category> findByNameAndStore_Id(String name, Long storeId);

    List<Category> findByStoreIdOrderByDisplayOrderAsc(Long storeId);

    List<Category> findByStoreIdAndStatusOrderByDisplayOrderAsc(Long storeId, String status);

    List<Category> findByStoreIdAndParentIsNullOrderByDisplayOrderAsc(Long storeId);

    List<Category> findByStoreIdAndStatusAndParentIsNullOrderByDisplayOrderAsc(Long storeId, String status);

//    List<Category> findByParentIdOrderByDisplayOrderAsc(Long parentId);

    @Query("SELECT c FROM Category c WHERE c.parent.id = :parentId ORDER BY c.displayOrder ASC")
    List<Category> findByParentIdOrderByDisplayOrderAsc(@Param("parentId") Long parentId);

    @Query("SELECT MAX(c.displayOrder) FROM Category c WHERE c.store.id = :storeId")
    Integer findMaxDisplayOrderByStoreId(@Param("storeId") Long storeId);

    @Query("SELECT c FROM Category c WHERE c.store.id = :storeId AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Category> searchByNameOrDescription(@Param("storeId") Long storeId, @Param("searchTerm") String searchTerm);
}
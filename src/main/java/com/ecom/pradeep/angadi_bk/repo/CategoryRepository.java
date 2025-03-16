package com.ecom.pradeep.angadi_bk.repo;

import com.ecom.pradeep.angadi_bk.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByStoreId(Long storeId);
    List<Category> findByStoreIdOrderByDisplayOrderAsc(Long storeId);

    List<Category> findByStoreIdAndStatusOrderByDisplayOrderAsc(Long storeId, String status);

    List<Category> findByStoreIdAndParentIsNullOrderByDisplayOrderAsc(Long storeId);

    List<Category> findByStoreIdAndStatusAndParentIsNullOrderByDisplayOrderAsc(Long storeId, String status);

    List<Category> findByParentIdOrderByDisplayOrderAsc(Long id);
}

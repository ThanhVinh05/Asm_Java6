package com.poly.repository;

import com.poly.common.Status;
import com.poly.model.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {

    @Query(value = "select p from ProductEntity p where p.status = 'ACTIVE'")
    Page<ProductEntity> findAllActive(Pageable pageable);

    @Query(value = "select p from ProductEntity p where  p.status='ACTIVE' " +
            " and lower(p.productName) like :keyword")
    Page<ProductEntity> searchByKeyword(String keyword, Pageable pageable);

    @Query(value = "SELECT p FROM ProductEntity p WHERE p.status = 'ACTIVE' AND p.categoryId = :categoryId")
    Page<ProductEntity> findByCategoryId(Long categoryId, Pageable pageable);

    // Add this method
    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.status = :status")
    long countByStatus(@Param("status") Status status);
}

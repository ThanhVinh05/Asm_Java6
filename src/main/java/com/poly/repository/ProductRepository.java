package com.poly.repository;

import com.poly.model.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    @Query(value = "select p from ProductEntity p where p.status = 'ACTIVE'")
    Page<ProductEntity> findAllActive(Pageable pageable);

    @Query(value = "select p from ProductEntity p where  p.status='ACTIVE' " +
            " and lower(p.productName) like :keyword")
    Page<ProductEntity> searchByKeyword(String keyword, Pageable pageable);
}

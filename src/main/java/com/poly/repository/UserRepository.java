package com.poly.repository;

import com.poly.common.Status;
import com.poly.model.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @Query(value = "select u from UserEntity u where u.status='ACTIVE' " +
            "and(lower(u.username) like :keyword " +
            "or lower(u.phone) like :keyword " +
            "or lower(u.email) like :keyword)")
    Page<UserEntity> searchByKeyword(String keyword, Pageable pageable);

    @Query(value = "select u from UserEntity u where u.status = 'ACTIVE'")
    Page<UserEntity> findAllActive(Pageable pageable);

    UserEntity findByUsername(String username);
    UserEntity findByEmail(String email);

    UserEntity findBySecretCode(String secretCode);

    // Add this method
    @Query("SELECT COUNT(u) FROM UserEntity u WHERE u.status = :status")
    long countByStatus(@Param("status") Status status);
}
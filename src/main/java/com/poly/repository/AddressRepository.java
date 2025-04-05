package com.poly.repository;

import com.poly.model.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<AddressEntity, Long> {

    AddressEntity findByUserIdAndAddressType(Long userId, Integer addressType);

    List<AddressEntity> findByUserId(Long userId);
}

package com.ecommerce.userservice.repository;

import com.ecommerce.userservice.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, String> {
    boolean existsByUserId(String userId);
}

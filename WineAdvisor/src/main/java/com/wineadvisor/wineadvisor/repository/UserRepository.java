package com.wineadvisor.wineadvisor.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wineadvisor.wineadvisor.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    
}

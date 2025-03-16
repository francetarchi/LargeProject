package com.wineadvisor.wineadvisor.repository;

import com.wineadvisor.wineadvisor.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    
}

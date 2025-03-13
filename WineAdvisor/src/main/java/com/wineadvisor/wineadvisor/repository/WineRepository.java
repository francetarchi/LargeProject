package com.wineadvisor.wineadvisor.repository;

import com.wineadvisor.wineadvisor.model.Wine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WineRepository extends JpaRepository<Wine, Long> {
}

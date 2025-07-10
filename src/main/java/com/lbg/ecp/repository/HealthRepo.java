package com.lbg.ecp.repository;

import com.lbg.ecp.entities.tables.Health;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthRepo extends JpaRepository<Health, Long> {}

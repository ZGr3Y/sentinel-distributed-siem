package com.sentinel.core.repository;

import com.sentinel.common.domain.entity.Alert;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface AlertRepository extends JpaRepository<Alert, String> {

    @Query("SELECT COUNT(a) FROM Alert a WHERE a.type = :alertType")
    long countByAlertType(@Param("alertType") String alertType);

    List<Alert> findBySourceIp(String sourceIp);

    List<Alert> findTop10ByOrderByCreatedAtDesc();
}

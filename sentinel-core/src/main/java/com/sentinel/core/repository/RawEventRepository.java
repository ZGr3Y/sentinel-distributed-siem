package com.sentinel.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sentinel.common.domain.entity.RawEvent;

@Repository
public interface RawEventRepository extends JpaRepository<RawEvent, String> {
}

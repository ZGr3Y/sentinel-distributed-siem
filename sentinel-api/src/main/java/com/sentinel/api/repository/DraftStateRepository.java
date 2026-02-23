package com.sentinel.api.repository;

import com.sentinel.common.domain.entity.DraftState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DraftStateRepository extends JpaRepository<DraftState, UUID> {
    Optional<DraftState> findByUserId(UUID userId);
}

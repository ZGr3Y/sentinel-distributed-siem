package com.sentinel.api.repository;

import com.sentinel.common.domain.entity.DraftState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DraftStateRepository extends JpaRepository<DraftState, String> {
    Optional<DraftState> findByUserId(String userId);
}

package com.studyshield.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
    Optional<Session> findBySessionIdAndIsActiveTrue(UUID sessionId);
    Optional<Session> findBySessionIdAndIsActiveTrueAndExpiresAtAfter(UUID sessionId, Long now);
}

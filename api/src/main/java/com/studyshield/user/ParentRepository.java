package com.studyshield.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParentRepository extends JpaRepository<Parent, UUID> {
    List<Parent> findByAccountId(UUID accountId);
    Optional<Parent> findByParentIdAndAccountId(UUID parentId, UUID accountId);
    long countByAccountId(UUID accountId);
}

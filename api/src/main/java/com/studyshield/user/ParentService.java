package com.studyshield.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ParentService {

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private SessionRepository sessionRepository;

    public ParentResponse addParent(UUID sessionId, String name) {
        Session session = sessionRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new RegistrationException("Invalid or expired session", "INVALID_SESSION"));

        String parentName = name != null && !name.trim().isEmpty()
                ? name.trim()
                : generateParentName();
        UUID parentId = UUID.randomUUID();
        Parent parent = new Parent(parentId, session.getAccountId(), parentName);
        parentRepository.save(parent);
        return new ParentResponse(parentId, session.getAccountId(), parentName);
    }

    public List<ParentResponse> listParents(UUID sessionId) {
        Session session = sessionRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new RegistrationException("Invalid or expired session", "INVALID_SESSION"));

        return parentRepository.findByAccountId(session.getAccountId())
                .stream()
                .map(p -> new ParentResponse(p.getParentId(), p.getAccountId(), p.getName()))
                .collect(Collectors.toList());
    }

    public ParentResponse updateMyName(UUID sessionId, String newName) {
        Session session = sessionRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new RegistrationException("Invalid or expired session", "INVALID_SESSION"));

        Parent parent = parentRepository.findById(session.getParentId())
                .orElseThrow(() -> new RegistrationException("Parent not found", "PARENT_NOT_FOUND"));

        parent.setName(newName.trim());
        parentRepository.save(parent);
        return new ParentResponse(parent.getParentId(), parent.getAccountId(), parent.getName());
    }

    private String generateParentName() {
        return "awesome-parent-" + UUID.randomUUID().toString().substring(0, 6);
    }
}

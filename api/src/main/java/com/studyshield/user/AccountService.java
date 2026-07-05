package com.studyshield.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ParentRepository parentRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private PasswordUtil passwordUtil;

    public AuthResponse registerUser(String loginId, String password, String name) {
        Optional<Account> existingAccount = accountRepository.findByLoginId(loginId);
        if (existingAccount.isPresent()) {
            throw new RegistrationException("Email or phone already registered", "EMAIL_PHONE_EXISTS");
        }

        if (password == null || password.length() < 6) {
            throw new RegistrationException("Password must be at least 6 characters", "WEAK_PASSWORD");
        }

        Account account = new Account(loginId, passwordUtil.encodePassword(password));
        Account savedAccount = accountRepository.save(account);

        String parentName = name != null && !name.trim().isEmpty()
                ? name.trim()
                : generateParentName();
        UUID parentId = UUID.randomUUID();
        Parent parent = new Parent(parentId, savedAccount.getAccountId(), parentName);
        parentRepository.save(parent);

        UUID sessionId = UUID.randomUUID();
        Long expirationTime = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000);
        Session session = new Session(savedAccount.getAccountId(), parentId, sessionId, expirationTime);
        sessionRepository.save(session);

        return new AuthResponse(
                savedAccount.getAccountId(),
                savedAccount.getLoginId(),
                sessionId,
                parentId,
                parentName,
                "Sign up successful"
        );
    }

    public AuthResponse authenticateUser(String loginId, String password, UUID parentId) {
        Account account = accountRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RegistrationException("Invalid email/phone or password", "INVALID_CREDENTIALS"));

        if (!passwordUtil.matches(password, account.getPasswordHash())) {
            throw new RegistrationException("Invalid email/phone or password", "INVALID_CREDENTIALS");
        }

        if (parentId != null) {
            Parent parent = parentRepository.findByParentIdAndAccountId(parentId, account.getAccountId())
                    .orElseThrow(() -> new RegistrationException("Parent not found", "INVALID_CREDENTIALS"));

            UUID sessionId = UUID.randomUUID();
            Long expirationTime = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000);
            Session session = new Session(account.getAccountId(), parentId, sessionId, expirationTime);
            sessionRepository.save(session);

            return new AuthResponse(
                    account.getAccountId(),
                    account.getLoginId(),
                    sessionId,
                    parentId,
                    parent.getName(),
                    "Sign in successful"
            );
        }

        long parentCount = parentRepository.countByAccountId(account.getAccountId());

        if (parentCount == 1) {
            Parent parent = parentRepository.findByAccountId(account.getAccountId()).get(0);
            UUID sessionId = UUID.randomUUID();
            Long expirationTime = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000);
            Session session = new Session(account.getAccountId(), parent.getParentId(), sessionId, expirationTime);
            sessionRepository.save(session);

            return new AuthResponse(
                    account.getAccountId(),
                    account.getLoginId(),
                    sessionId,
                    parent.getParentId(),
                    parent.getName(),
                    "Sign in successful"
            );
        }

        List<AuthResponse.ParentSummary> parentSummaries = parentRepository.findByAccountId(account.getAccountId())
                .stream()
                .map(p -> new AuthResponse.ParentSummary(p.getParentId(), p.getName()))
                .collect(Collectors.toList());

        return new AuthResponse(parentSummaries, "Multiple parents found. Please select a parent.");
    }

    public AuthResponse signOut(UUID sessionId) {
        Session session = sessionRepository.findBySessionIdAndIsActiveTrue(sessionId)
                .orElseThrow(() -> new RegistrationException("Invalid or expired session", "INVALID_SESSION"));
        session.setIsActive(false);
        sessionRepository.save(session);
        return new AuthResponse(null, null, null, null, null, "Sign out successful");
    }

    public ValidationResponse validateSession(UUID sessionId) {
        Session session = sessionRepository
                .findBySessionIdAndIsActiveTrueAndExpiresAtAfter(sessionId, System.currentTimeMillis())
                .orElseThrow(() -> new RegistrationException("Session expired or invalid", "INVALID_SESSION"));

        Account account = accountRepository.findById(session.getAccountId())
                .orElseThrow(() -> new RegistrationException("Account not found", "INVALID_SESSION"));

        Parent parent = parentRepository.findById(session.getParentId())
                .orElseThrow(() -> new RegistrationException("Parent not found", "INVALID_SESSION"));

        return new ValidationResponse(
                account.getAccountId(),
                account.getLoginId(),
                parent.getParentId(),
                parent.getName(),
                true,
                "Session valid",
                null
        );
    }

    public Account getAccountById(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + accountId));
    }

    public Optional<Account> getAccountByLoginId(String loginId) {
        return accountRepository.findByLoginId(loginId);
    }

    private String generateParentName() {
        return "awesome-parent-" + UUID.randomUUID().toString().substring(0, 6);
    }
}

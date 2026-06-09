package org.example.counterparty.repository;

import org.example.counterparty.entity.User;
import org.example.counterparty.entity.VerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long> {
    List<VerificationRequest> findByUserOrderByCreatedAtDesc(User user);
    List<VerificationRequest> findByUserAndInnOgrn(User user, String innOgrn);
    int countByUser(User user);
}

package org.example.counterparty.repository;

import org.example.counterparty.entity.User;
import org.example.counterparty.entity.VerificationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class VerificationRequestRepositoryTest {

    @Autowired
    private VerificationRequestRepository verificationRequestRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("integration_user", "password123", "USER");
        userRepository.save(testUser);
    }

    @Test
    void shouldSaveAndFindRequestsByUser() {
        VerificationRequest request1 = new VerificationRequest(testUser, "7707083893");
        VerificationRequest request2 = new VerificationRequest(testUser, "1234567890");

        verificationRequestRepository.save(request1);
        verificationRequestRepository.save(request2);

        List<VerificationRequest> found = verificationRequestRepository
                .findByUserOrderByCreatedAtDesc(testUser);

        assertThat(found).hasSize(2);

        assertThat(found).extracting(VerificationRequest::getInnOgrn)
                .containsExactlyInAnyOrder("7707083893", "1234567890");
    }

    @Test
    void shouldCountRequestsByUser() {
        verificationRequestRepository.save(new VerificationRequest(testUser, "7707083893"));
        verificationRequestRepository.save(new VerificationRequest(testUser, "1234567890"));

        int count = verificationRequestRepository.countByUser(testUser);

        assertThat(count).isEqualTo(2);
    }
}
package org.example.counterparty.service;

import org.example.counterparty.entity.CounterpartyData;
import org.example.counterparty.entity.User;
import org.example.counterparty.entity.VerificationRequest;
import org.example.counterparty.repository.CounterpartyDataRepository;
import org.example.counterparty.repository.VerificationRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CounterpartyService {
    @Autowired
    private VerificationRequestRepository verificationRequestRepository;

    @Autowired
    private CounterpartyDataRepository counterpartyDataRepository;

    public VerificationRequest createRequest(User user, String innOgrn){
        VerificationRequest request = new VerificationRequest(user, innOgrn);
        return verificationRequestRepository.save(request);
    }

    public List<VerificationRequest> getUserRequests(User user) {
        return verificationRequestRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public VerificationRequest getRequestById(Long id) {
        return verificationRequestRepository.findById(id).orElse(null);
    }

    public void updateRequestStatus(VerificationRequest request, String status, String errorMessage) {
        request.setStatus(status);
        if (errorMessage != null) {
            request.setErrorMessage(errorMessage);
        }
        verificationRequestRepository.save(request);
    }

    public void saveCounterpartyData(VerificationRequest request, CounterpartyData data) {
        request.setCounterpartyData(data);
        verificationRequestRepository.save(request);
    }

    public int getUserRequestsCount(User user) {
        return verificationRequestRepository.countByUser(user);
    }

    public int getRemainingRequests(User user) {
        int currentCount = getUserRequestsCount(user);
        int maxRequests = 10;
        return Math.max(0, maxRequests - currentCount);
    }

}

package org.example.counterparty.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_requests")
public class VerificationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String innOgrn;
    private String status;  // PENDING, SUCCESS, ERROR, NOT_FOUND
    private String errorMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "request", cascade = CascadeType.ALL)
    private CounterpartyData counterpartyData;

    public VerificationRequest() {}

    public VerificationRequest(User user, String innOgrn) {
        this.user = user;
        this.innOgrn = innOgrn;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getInnOgrn() {
        return innOgrn;
    }

    public void setInnOgrn(String innOgrn) {
        this.innOgrn = innOgrn;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public CounterpartyData getCounterpartyData() {
        return counterpartyData;
    }

    public void setCounterpartyData(CounterpartyData counterpartyData) {
        this.counterpartyData = counterpartyData;
    }
}

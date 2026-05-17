package org.example.counterparty.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "counterparty_data")
public class CounterpartyData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "request_id", unique = true)
    private VerificationRequest request;

    private String inn;
    private String ogrn;
    private String name;
    private String address;
    private String status;
    private String registrationDate;

    @Column(length = 1000)
    private String fullData;  // JSON с полными данными от DaData

    public CounterpartyData() {}

    public CounterpartyData(VerificationRequest request, String inn, String ogrn,
                            String name, String address, String status,
                            String registrationDate, String fullData) {
        this.request = request;
        this.inn = inn;
        this.ogrn = ogrn;
        this.name = name;
        this.address = address;
        this.status = status;
        this.registrationDate = registrationDate;
        this.fullData = fullData;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VerificationRequest getRequest() {
        return request;
    }

    public void setRequest(VerificationRequest request) {
        this.request = request;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getOgrn() {
        return ogrn;
    }

    public void setOgrn(String ogrn) {
        this.ogrn = ogrn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getFullData() {
        return fullData;
    }

    public void setFullData(String fullData) {
        this.fullData = fullData;
    }
}

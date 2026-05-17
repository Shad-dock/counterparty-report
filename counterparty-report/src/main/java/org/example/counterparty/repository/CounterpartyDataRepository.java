package org.example.counterparty.repository;

import org.example.counterparty.entity.CounterpartyData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CounterpartyDataRepository extends JpaRepository<CounterpartyData, Long> {
}
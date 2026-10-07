package com.apexf1.backend.service;

import com.apexf1.backend.model.Circuit;
import com.apexf1.backend.repository.CircuitRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CircuitService {

    private final CircuitRepository circuitRepository;

    public CircuitService(CircuitRepository circuitRepository) {
        this.circuitRepository = circuitRepository;
    }

    public List<Circuit> findAll() {
        return circuitRepository.findAll();
    }

    public Circuit findById(Long id) {
        return circuitRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Circuit " + id + " not found"));
    }
}

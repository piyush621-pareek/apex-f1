package com.apexf1.backend.controller;

import com.apexf1.backend.model.Circuit;
import com.apexf1.backend.service.CircuitService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/circuits")
public class CircuitController {

    private final CircuitService circuitService;

    public CircuitController(CircuitService circuitService) {
        this.circuitService = circuitService;
    }

    @GetMapping
    public List<Circuit> getAll() {
        return circuitService.findAll();
    }

    @GetMapping("/{id}")
    public Circuit getById(@PathVariable Long id) {
        return circuitService.findById(id);
    }
}

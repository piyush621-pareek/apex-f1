package com.apexf1.backend.controller;

import com.apexf1.backend.model.SimulationResult;
import com.apexf1.backend.simulation.SimulationEngine;
import com.apexf1.backend.simulation.dto.SimulationSnapshot;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST surface for the multithreaded race simulation engine.
 *
 *  POST /api/simulations/race/{raceId}?laps=&timeScale=  → start a race sim
 *  GET  /api/simulations/{simId}                          → live snapshot (poll this)
 *  POST /api/simulations/{simId}/stop                     → stop a sim
 *  GET  /api/simulations/results(/{id})                    → saved results of finished races
 *  GET  /api/simulations                                  → list all sims
 */
@RestController
@RequestMapping("/api/simulations")
public class SimulationController {

    private final SimulationEngine engine;

    public SimulationController(SimulationEngine engine) {
        this.engine = engine;
    }

    @PostMapping("/race/{raceId}")
    public SimulationSnapshot start(@PathVariable Long raceId,
                                    @RequestParam(required = false) Integer laps,
                                    @RequestParam(required = false) Double timeScale) {
        return engine.start(raceId, laps, timeScale);
    }

    @GetMapping("/{simId}")
    public SimulationSnapshot get(@PathVariable long simId) {
        return engine.snapshot(simId);
    }

    @PostMapping("/{simId}/stop")
    public SimulationSnapshot stop(@PathVariable long simId) {
        return engine.stop(simId);
    }

    @GetMapping
    public List<SimulationSnapshot> list() {
        return engine.list();
    }

    @GetMapping("/results")
    public List<SimulationResult> results() {
        return engine.listResults();
    }

    @GetMapping("/results/{id}")
    public SimulationResult result(@PathVariable Long id) {
        return engine.getResult(id);
    }
}

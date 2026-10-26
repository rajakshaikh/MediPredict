package com.diseaseprediction.controller;

import com.diseaseprediction.dto.response.PredictionHistoryResponse;
import com.diseaseprediction.service.PredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/predictions")
public class PredictionHistoryController {

    private final PredictionService predictionService;

    public PredictionHistoryController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    /**
     * Get all prediction records across all patients, ordered by newest first.
     */
    @GetMapping
    public ResponseEntity<List<PredictionHistoryResponse>> getAllPredictions() {
        List<PredictionHistoryResponse> predictions = predictionService.getAllPredictions();
        return ResponseEntity.ok(predictions);
    }
}

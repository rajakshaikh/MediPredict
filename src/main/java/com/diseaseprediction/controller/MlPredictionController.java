package com.diseaseprediction.controller;

import com.diseaseprediction.dto.request.DiseasePredictionRequest;
import com.diseaseprediction.dto.request.HeartDiseasePredictionRequest;
import com.diseaseprediction.dto.response.DiseasePredictionResponse;
import com.diseaseprediction.dto.response.HeartDiseasePredictionResponse;
import com.diseaseprediction.dto.response.SymptomsResponse;
import com.diseaseprediction.service.MlService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ml")
public class MlPredictionController {

    private final MlService mlService;

    public MlPredictionController(MlService mlService) {
        this.mlService = mlService;
    }

    /**
     * Fetch available symptoms from the Python ML service.
     */
    @GetMapping("/symptoms")
    public ResponseEntity<SymptomsResponse> getSymptoms() {
        SymptomsResponse response = mlService.getSymptoms();
        return ResponseEntity.ok(response);
    }

    /**
     * Predict general disease based on symptoms list.
     */
    @PostMapping("/predict")
    public ResponseEntity<DiseasePredictionResponse> predictDisease(
            @Valid @RequestBody DiseasePredictionRequest request) {
        DiseasePredictionResponse response = mlService.predictGeneralDisease(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Predict heart disease based on 13 clinical parameters.
     */
    @PostMapping("/predict-heart-disease")
    public ResponseEntity<HeartDiseasePredictionResponse> predictHeartDisease(
            @Valid @RequestBody HeartDiseasePredictionRequest request) {
        HeartDiseasePredictionResponse response = mlService.predictHeartDisease(request);
        return ResponseEntity.ok(response);
    }
}

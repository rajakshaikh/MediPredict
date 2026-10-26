package com.diseaseprediction.controller;

import com.diseaseprediction.dto.request.DiseasePredictionRequest;
import com.diseaseprediction.dto.request.HeartDiseasePredictionRequest;
import com.diseaseprediction.dto.request.PatientRequest;
import com.diseaseprediction.dto.response.PatientDiseasePredictionResponse;
import com.diseaseprediction.dto.response.PatientHeartDiseasePredictionResponse;
import com.diseaseprediction.dto.response.PatientResponse;
import com.diseaseprediction.dto.response.PredictionHistoryResponse;
import com.diseaseprediction.service.PatientService;
import com.diseaseprediction.service.PredictionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;
    private final PredictionService predictionService;

    public PatientController(PatientService patientService, PredictionService predictionService) {
        this.patientService = patientService;
        this.predictionService = predictionService;
    }

    /**
     * Create a new patient.
     */
    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody PatientRequest request) {
        PatientResponse created = patientService.createPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get patient by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long id) {
        PatientResponse patient = patientService.getPatientById(id);
        return ResponseEntity.ok(patient);
    }

    /**
     * Get all registered patients.
     */
    @GetMapping
    public ResponseEntity<List<PatientResponse>> getAllPatients() {
        List<PatientResponse> patients = patientService.getAllPatients();
        return ResponseEntity.ok(patients);
    }

    /**
     * Predict general disease for a patient and save to database.
     */
    @PostMapping("/{patientId}/predict")
    public ResponseEntity<PatientDiseasePredictionResponse> predictGeneralDisease(
            @PathVariable Long patientId,
            @Valid @RequestBody DiseasePredictionRequest request) {
        PatientDiseasePredictionResponse response =
                predictionService.predictAndSaveGeneralDisease(patientId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Predict heart disease for a patient and save to database.
     */
    @PostMapping("/{patientId}/predict-heart-disease")
    public ResponseEntity<PatientHeartDiseasePredictionResponse> predictHeartDisease(
            @PathVariable Long patientId,
            @Valid @RequestBody HeartDiseasePredictionRequest request) {
        PatientHeartDiseasePredictionResponse response =
                predictionService.predictAndSaveHeartDisease(patientId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get prediction history for a specific patient, ordered by newest first.
     */
    @GetMapping("/{patientId}/predictions")
    public ResponseEntity<List<PredictionHistoryResponse>> getPatientPredictions(@PathVariable Long patientId) {
        List<PredictionHistoryResponse> history = predictionService.getPredictionsByPatientId(patientId);
        return ResponseEntity.ok(history);
    }

    /**
     * Download PDF Medical Report for a patient's prediction record.
     */
    @GetMapping(value = "/{patientId}/predictions/{predictionId}/report", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadPredictionReport(
            @PathVariable Long patientId,
            @PathVariable Long predictionId) {
        byte[] pdfBytes = predictionService.generatePredictionReport(patientId, predictionId);

        String filename = String.format("MedPredict_Report_%d_%d.pdf", patientId, predictionId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(org.springframework.http.ContentDisposition.attachment().filename(filename).build());
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}

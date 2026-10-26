package com.diseaseprediction.service;

import com.diseaseprediction.dto.request.DiseasePredictionRequest;
import com.diseaseprediction.dto.request.HeartDiseasePredictionRequest;
import com.diseaseprediction.dto.response.DiseasePredictionResponse;
import com.diseaseprediction.dto.response.HeartDiseasePredictionResponse;
import com.diseaseprediction.dto.response.PatientDiseasePredictionResponse;
import com.diseaseprediction.dto.response.PatientHeartDiseasePredictionResponse;
import com.diseaseprediction.dto.response.PredictionHistoryResponse;
import com.diseaseprediction.model.Patient;
import com.diseaseprediction.model.PredictionHistory;
import com.diseaseprediction.repository.PredictionHistoryRepository;
import com.diseaseprediction.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PredictionService {

    private static final Logger log = LoggerFactory.getLogger(PredictionService.class);

    private final PatientService patientService;
    private final MlService mlService;
    private final PredictionHistoryRepository predictionHistoryRepository;
    private final ObjectMapper objectMapper;
    private final PdfReportService pdfReportService;

    public PredictionService(PatientService patientService,
                             MlService mlService,
                             PredictionHistoryRepository predictionHistoryRepository,
                             ObjectMapper objectMapper,
                             PdfReportService pdfReportService) {
        this.patientService = patientService;
        this.mlService = mlService;
        this.predictionHistoryRepository = predictionHistoryRepository;
        this.objectMapper = objectMapper;
        this.pdfReportService = pdfReportService;
    }

    /**
     * General disease prediction workflow:
     * 1. Verifies patient exists.
     * 2. Calls MlService.predictGeneralDisease().
     * 3. Saves PredictionHistory entity in MySQL.
     * 4. Returns prediction result.
     */
    public PatientDiseasePredictionResponse predictAndSaveGeneralDisease(Long patientId, DiseasePredictionRequest request) {
        Patient patient = patientService.getPatientEntity(patientId);

        DiseasePredictionResponse mlResponse = mlService.predictGeneralDisease(request);

        String inputJson;
        try {
            inputJson = objectMapper.writeValueAsString(request.getSymptoms());
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize symptoms to JSON, falling back to string representation", e);
            inputJson = String.valueOf(request.getSymptoms());
        }

        PredictionHistory history = new PredictionHistory(
                patient,
                "GENERAL_DISEASE",
                inputJson,
                mlResponse.getPrediction(),
                mlResponse.getProbability()
        );

        PredictionHistory saved = predictionHistoryRepository.save(history);
        log.info("Saved general disease prediction record with ID {} for patient ID {}", saved.getId(), patientId);

        return new PatientDiseasePredictionResponse(
                saved.getId(),
                patient.getId(),
                patient.getName(),
                saved.getPredictionType(),
                mlResponse.getPrediction(),
                mlResponse.getProbability(),
                mlResponse.getSelectedSymptoms() != null ? mlResponse.getSelectedSymptoms() : request.getSymptoms(),
                mlResponse.getMessage(),
                saved.getCreatedAt()
        );
    }

    /**
     * Heart disease prediction workflow:
     * 1. Verifies patient exists.
     * 2. Calls MlService.predictHeartDisease().
     * 3. Saves PredictionHistory entity in MySQL.
     * 4. Returns prediction result.
     */
    public PatientHeartDiseasePredictionResponse predictAndSaveHeartDisease(Long patientId, HeartDiseasePredictionRequest request) {
        Patient patient = patientService.getPatientEntity(patientId);

        HeartDiseasePredictionResponse mlResponse = mlService.predictHeartDisease(request);

        String inputJson;
        try {
            inputJson = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize heart disease request to JSON", e);
            inputJson = request.toString();
        }

        PredictionHistory history = new PredictionHistory(
                patient,
                "HEART_DISEASE",
                inputJson,
                mlResponse.getResult(),
                mlResponse.getProbability()
        );

        PredictionHistory saved = predictionHistoryRepository.save(history);
        log.info("Saved heart disease prediction record with ID {} for patient ID {}", saved.getId(), patientId);

        return new PatientHeartDiseasePredictionResponse(
                saved.getId(),
                patient.getId(),
                patient.getName(),
                saved.getPredictionType(),
                mlResponse.getPrediction(),
                mlResponse.getResult(),
                mlResponse.getProbability(),
                saved.getCreatedAt()
        );
    }

    /**
     * Retrieve all prediction history records for a specific patient, newest first.
     */
    @Transactional(readOnly = true)
    public List<PredictionHistoryResponse> getPredictionsByPatientId(Long patientId) {
        // Verify patient exists
        patientService.getPatientEntity(patientId);

        return predictionHistoryRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieve all prediction history records across all patients, newest first.
     */
    @Transactional(readOnly = true)
    public List<PredictionHistoryResponse> getAllPredictions() {
        return predictionHistoryRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Generate PDF Medical Report for a patient's prediction record.
     * Verifies that the patient exists, the prediction exists, and belongs to that patient.
     */
    @Transactional(readOnly = true)
    public byte[] generatePredictionReport(Long patientId, Long predictionId) {
        Patient patient = patientService.getPatientEntity(patientId);

        PredictionHistory prediction = predictionHistoryRepository.findById(predictionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prediction not found with id: " + predictionId));

        if (!prediction.getPatient().getId().equals(patientId)) {
            throw new ResourceNotFoundException("Prediction with id " + predictionId + " does not belong to patient with id " + patientId);
        }

        return pdfReportService.generateMedicalReport(patient, prediction);
    }

    private PredictionHistoryResponse mapToHistoryResponse(PredictionHistory history) {
        return new PredictionHistoryResponse(
                history.getId(),
                history.getPatient() != null ? history.getPatient().getId() : null,
                history.getPatient() != null ? history.getPatient().getName() : null,
                history.getPredictionType(),
                history.getInputData(),
                history.getPredictedDisease(),
                history.getProbability(),
                history.getCreatedAt()
        );
    }
}

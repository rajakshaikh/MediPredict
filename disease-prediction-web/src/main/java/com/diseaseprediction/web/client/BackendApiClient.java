package com.diseaseprediction.web.client;

import com.diseaseprediction.web.dto.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackendApiClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BackendApiClient() {
        this("http://localhost:8080");
    }

    public BackendApiClient(String baseUrl) {
        if (baseUrl != null && baseUrl.endsWith("/")) {
            this.baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        } else {
            this.baseUrl = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl : "http://localhost:8080";
        }

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Fetch the 132 available symptoms from the Python ML service via Spring Boot.
     * GET /api/ml/symptoms
     */
    public List<String> getSymptoms() throws ApiException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/ml/symptoms"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            checkError(response);

            SymptomsDto dto = objectMapper.readValue(response.body(), SymptomsDto.class);
            return dto.getSymptoms() != null ? dto.getSymptoms() : Collections.emptyList();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Failed to communicate with Spring Boot ML service: " + e.getMessage(), e);
        }
    }

    /**
     * Register a new patient in the hospital MySQL registry.
     * POST /api/patients
     */
    public PatientDto registerPatient(PatientRegistrationDto regDto) throws ApiException {
        try {
            String jsonPayload = objectMapper.writeValueAsString(regDto);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/patients"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            checkError(response);

            return objectMapper.readValue(response.body(), PatientDto.class);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Failed to register patient via Spring Boot API: " + e.getMessage(), e);
        }
    }

    /**
     * Get patient by ID.
     * GET /api/patients/{id}
     */
    public PatientDto getPatientById(Long id) throws ApiException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/patients/" + id))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            checkError(response);

            return objectMapper.readValue(response.body(), PatientDto.class);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Failed to fetch patient #" + id + ": " + e.getMessage(), e);
        }
    }

    /**
     * Fetch all registered patients.
     * GET /api/patients
     */
    public List<PatientDto> getAllPatients() throws ApiException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/patients"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            checkError(response);

            return objectMapper.readValue(response.body(), new TypeReference<List<PatientDto>>() {});
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Failed to fetch patients list: " + e.getMessage(), e);
        }
    }

    /**
     * Run general disease prediction and save to patient's history in MySQL.
     * POST /api/patients/{patientId}/predict
     */
    public DiseasePredictionDto predictGeneralDisease(Long patientId, List<String> symptoms) throws ApiException {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("symptoms", symptoms);

            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/patients/" + patientId + "/predict"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            checkError(response);

            return objectMapper.readValue(response.body(), DiseasePredictionDto.class);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("General disease prediction failed: " + e.getMessage(), e);
        }
    }

    /**
     * Run heart disease risk evaluation and save to patient's history in MySQL.
     * POST /api/patients/{patientId}/predict-heart-disease
     */
    public HeartDiseasePredictionDto predictHeartDisease(Long patientId, Map<String, Object> heartParameters) throws ApiException {
        try {
            String jsonPayload = objectMapper.writeValueAsString(heartParameters);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/patients/" + patientId + "/predict-heart-disease"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            checkError(response);

            return objectMapper.readValue(response.body(), HeartDiseasePredictionDto.class);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Heart disease risk evaluation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Fetch prediction history for a specific patient.
     * GET /api/patients/{patientId}/predictions
     */
    public List<PredictionHistoryDto> getPatientPredictions(Long patientId) throws ApiException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/patients/" + patientId + "/predictions"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            checkError(response);

            return objectMapper.readValue(response.body(), new TypeReference<List<PredictionHistoryDto>>() {});
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Failed to fetch prediction history for patient #" + patientId + ": " + e.getMessage(), e);
        }
    }

    /**
     * Fetch all prediction history records in the system.
     * GET /api/predictions
     */
    public List<PredictionHistoryDto> getAllPredictions() throws ApiException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/predictions"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            checkError(response);

            return objectMapper.readValue(response.body(), new TypeReference<List<PredictionHistoryDto>>() {});
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Failed to fetch all prediction records: " + e.getMessage(), e);
        }
    }

    /**
     * Download the official OpenPDF Medical Report bytes for a prediction.
     * GET /api/patients/{patientId}/predictions/{predictionId}/report
     */
    public byte[] downloadReport(Long patientId, Long predictionId) throws ApiException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/patients/" + patientId + "/predictions/" + predictionId + "/report"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Accept", "application/pdf")
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() >= 400) {
                String errorBody = new String(response.body());
                throw new ApiException(response.statusCode(), "Report Generation Failed", errorBody);
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Failed to download PDF report: " + e.getMessage(), e);
        }
    }

    /**
     * Generates the direct URL to the Spring Boot PDF endpoint.
     */
    public String getDirectReportUrl(Long patientId, Long predictionId) {
        return baseUrl + "/api/patients/" + patientId + "/predictions/" + predictionId + "/report";
    }

    private void checkError(HttpResponse<String> response) throws ApiException {
        int status = response.statusCode();
        if (status >= 400) {
            String errorMsg = "HTTP " + status;
            try {
                ErrorDto errorDto = objectMapper.readValue(response.body(), ErrorDto.class);
                if (errorDto.getMessage() != null && !errorDto.getMessage().isBlank()) {
                    errorMsg = errorDto.getMessage();
                } else if (errorDto.getError() != null && !errorDto.getError().isBlank()) {
                    errorMsg = errorDto.getError();
                }
            } catch (Exception ignored) {
                if (response.body() != null && !response.body().isBlank()) {
                    errorMsg = response.body();
                }
            }
            throw new ApiException(status, "Backend API Error", errorMsg);
        }
    }
}

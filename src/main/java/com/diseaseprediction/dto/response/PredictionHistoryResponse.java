package com.diseaseprediction.dto.response;

import java.time.LocalDateTime;

public class PredictionHistoryResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private String predictionType;
    private String inputData;
    private String predictedDisease;
    private Double probability;
    private LocalDateTime createdAt;

    public PredictionHistoryResponse() {
    }

    public PredictionHistoryResponse(Long id, Long patientId, String patientName, String predictionType,
                                   String inputData, String predictedDisease, Double probability,
                                   LocalDateTime createdAt) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.predictionType = predictionType;
        this.inputData = inputData;
        this.predictedDisease = predictedDisease;
        this.probability = probability;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPredictionType() {
        return predictionType;
    }

    public void setPredictionType(String predictionType) {
        this.predictionType = predictionType;
    }

    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public String getPredictedDisease() {
        return predictedDisease;
    }

    public void setPredictedDisease(String predictedDisease) {
        this.predictedDisease = predictedDisease;
    }

    public Double getProbability() {
        return probability;
    }

    public void setProbability(Double probability) {
        this.probability = probability;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

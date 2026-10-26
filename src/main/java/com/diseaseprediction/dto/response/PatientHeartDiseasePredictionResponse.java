package com.diseaseprediction.dto.response;

import java.time.LocalDateTime;

public class PatientHeartDiseasePredictionResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private String predictionType;
    private Integer prediction;
    private String result;
    private Double probability;
    private LocalDateTime createdAt;

    public PatientHeartDiseasePredictionResponse() {
    }

    public PatientHeartDiseasePredictionResponse(Long id, Long patientId, String patientName, String predictionType,
                                               Integer prediction, String result, Double probability,
                                               LocalDateTime createdAt) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.predictionType = predictionType;
        this.prediction = prediction;
        this.result = result;
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

    public Integer getPrediction() {
        return prediction;
    }

    public void setPrediction(Integer prediction) {
        this.prediction = prediction;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
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

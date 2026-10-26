package com.diseaseprediction.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class PatientDiseasePredictionResponse {

    private Long id;
    private Long patientId;
    private String patientName;
    private String predictionType;
    private String prediction;
    private Double probability;
    private List<String> selectedSymptoms;
    private String message;
    private LocalDateTime createdAt;

    public PatientDiseasePredictionResponse() {
    }

    public PatientDiseasePredictionResponse(Long id, Long patientId, String patientName, String predictionType,
                                          String prediction, Double probability, List<String> selectedSymptoms,
                                          String message, LocalDateTime createdAt) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.predictionType = predictionType;
        this.prediction = prediction;
        this.probability = probability;
        this.selectedSymptoms = selectedSymptoms;
        this.message = message;
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

    public String getPrediction() {
        return prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public Double getProbability() {
        return probability;
    }

    public void setProbability(Double probability) {
        this.probability = probability;
    }

    public List<String> getSelectedSymptoms() {
        return selectedSymptoms;
    }

    public void setSelectedSymptoms(List<String> selectedSymptoms) {
        this.selectedSymptoms = selectedSymptoms;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

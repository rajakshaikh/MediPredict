package com.diseaseprediction.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class DiseasePredictionRequest {

    @NotEmpty(message = "Symptoms list cannot be empty")
    private List<String> symptoms;

    public DiseasePredictionRequest() {
    }

    public DiseasePredictionRequest(List<String> symptoms) {
        this.symptoms = symptoms;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }
}

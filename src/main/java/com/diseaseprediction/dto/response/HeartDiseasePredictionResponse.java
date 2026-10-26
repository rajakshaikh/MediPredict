package com.diseaseprediction.dto.response;

public class HeartDiseasePredictionResponse {

    private Integer prediction;
    private String result;
    private Double probability;
    private String error;

    public HeartDiseasePredictionResponse() {
    }

    public HeartDiseasePredictionResponse(Integer prediction, String result, Double probability) {
        this.prediction = prediction;
        this.result = result;
        this.probability = probability;
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

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

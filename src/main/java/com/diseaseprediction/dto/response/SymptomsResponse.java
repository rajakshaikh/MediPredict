package com.diseaseprediction.dto.response;

import java.util.List;

public class SymptomsResponse {

    private List<String> symptoms;

    public SymptomsResponse() {
    }

    public SymptomsResponse(List<String> symptoms) {
        this.symptoms = symptoms;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }
}

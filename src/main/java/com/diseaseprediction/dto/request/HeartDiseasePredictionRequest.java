package com.diseaseprediction.dto.request;

import jakarta.validation.constraints.NotNull;

public class HeartDiseasePredictionRequest {

    @NotNull(message = "age is required")
    private Double age;

    @NotNull(message = "sex is required")
    private Double sex;

    @NotNull(message = "cp is required")
    private Double cp;

    @NotNull(message = "trestbps is required")
    private Double trestbps;

    @NotNull(message = "chol is required")
    private Double chol;

    @NotNull(message = "fbs is required")
    private Double fbs;

    @NotNull(message = "restecg is required")
    private Double restecg;

    @NotNull(message = "thalach is required")
    private Double thalach;

    @NotNull(message = "exang is required")
    private Double exang;

    @NotNull(message = "oldpeak is required")
    private Double oldpeak;

    @NotNull(message = "slope is required")
    private Double slope;

    @NotNull(message = "ca is required")
    private Double ca;

    @NotNull(message = "thal is required")
    private Double thal;

    public HeartDiseasePredictionRequest() {
    }

    public Double getAge() {
        return age;
    }

    public void setAge(Double age) {
        this.age = age;
    }

    public Double getSex() {
        return sex;
    }

    public void setSex(Double sex) {
        this.sex = sex;
    }

    public Double getCp() {
        return cp;
    }

    public void setCp(Double cp) {
        this.cp = cp;
    }

    public Double getTrestbps() {
        return trestbps;
    }

    public void setTrestbps(Double trestbps) {
        this.trestbps = trestbps;
    }

    public Double getChol() {
        return chol;
    }

    public void setChol(Double chol) {
        this.chol = chol;
    }

    public Double getFbs() {
        return fbs;
    }

    public void setFbs(Double fbs) {
        this.fbs = fbs;
    }

    public Double getRestecg() {
        return restecg;
    }

    public void setRestecg(Double restecg) {
        this.restecg = restecg;
    }

    public Double getThalach() {
        return thalach;
    }

    public void setThalach(Double thalach) {
        this.thalach = thalach;
    }

    public Double getExang() {
        return exang;
    }

    public void setExang(Double exang) {
        this.exang = exang;
    }

    public Double getOldpeak() {
        return oldpeak;
    }

    public void setOldpeak(Double oldpeak) {
        this.oldpeak = oldpeak;
    }

    public Double getSlope() {
        return slope;
    }

    public void setSlope(Double slope) {
        this.slope = slope;
    }

    public Double getCa() {
        return ca;
    }

    public void setCa(Double ca) {
        this.ca = ca;
    }

    public Double getThal() {
        return thal;
    }

    public void setThal(Double thal) {
        this.thal = thal;
    }
}

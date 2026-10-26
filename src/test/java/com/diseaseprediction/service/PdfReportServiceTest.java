package com.diseaseprediction.service;

import com.diseaseprediction.model.Patient;
import com.diseaseprediction.model.PredictionHistory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PdfReportServiceTest {

    private PdfReportService pdfReportService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        pdfReportService = new PdfReportService(objectMapper);
    }

    @Test
    @DisplayName("Should generate valid PDF report for general disease prediction")
    void testGenerateGeneralDiseasePdfReport() {
        Patient patient = new Patient("Arfat", 20, "Male", "example@email.com");
        patient.setId(1L);
        patient.setCreatedAt(LocalDateTime.now());

        String symptomsJson = "[\"fever\",\"headache\",\"joint_pain\",\"skin_rash\"]";
        PredictionHistory prediction = new PredictionHistory(
                patient, "GENERAL_DISEASE", symptomsJson, "Dengue", 87.0);
        prediction.setId(5L);
        prediction.setCreatedAt(LocalDateTime.now());

        byte[] pdfBytes = pdfReportService.generateMedicalReport(patient, prediction);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);

        // Verify PDF Header (%PDF-)
        String magicNumber = new String(pdfBytes, 0, 5);
        assertEquals("%PDF-", magicNumber);
    }

    @Test
    @DisplayName("Should generate valid PDF report for heart disease risk assessment with 13 parameters")
    void testGenerateHeartDiseasePdfReport() {
        Patient patient = new Patient("Jane Doe", 55, "Female", "jane@example.com");
        patient.setId(2L);
        patient.setCreatedAt(LocalDateTime.now());

        String heartJson = "{\"age\":55.0,\"sex\":0.0,\"cp\":1.0,\"trestbps\":130.0,\"chol\":240.0," +
                "\"fbs\":0.0,\"restecg\":0.0,\"thalach\":150.0,\"exang\":0.0,\"oldpeak\":1.2,\"slope\":1.0," +
                "\"ca\":0.0,\"thal\":2.0}";

        PredictionHistory prediction = new PredictionHistory(
                patient, "HEART_DISEASE", heartJson, "Heart Disease Detected", 75.0);
        prediction.setId(12L);
        prediction.setCreatedAt(LocalDateTime.now());

        byte[] pdfBytes = pdfReportService.generateMedicalReport(patient, prediction);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);

        String magicNumber = new String(pdfBytes, 0, 5);
        assertEquals("%PDF-", magicNumber);
    }
}

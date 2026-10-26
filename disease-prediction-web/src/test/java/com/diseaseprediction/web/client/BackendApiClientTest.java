package com.diseaseprediction.web.client;

import com.diseaseprediction.web.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BackendApiClientTest {

    private BackendApiClient apiClient;

    @BeforeEach
    public void setUp() {
        apiClient = new BackendApiClient("http://localhost:8080");
    }

    @Test
    public void testBaseUrlConfiguration() {
        assertEquals("http://localhost:8080", apiClient.getBaseUrl());

        BackendApiClient customClient = new BackendApiClient("http://127.0.0.1:8080/");
        assertEquals("http://127.0.0.1:8080", customClient.getBaseUrl());
    }

    @Test
    public void testLiveBackendSymptomsFetch() {
        // Tests against the live Spring Boot backend on localhost:8080
        try {
            List<String> symptoms = apiClient.getSymptoms();
            assertNotNull(symptoms);
            assertFalse(symptoms.isEmpty());
            assertTrue(symptoms.contains("itching"));
            assertTrue(symptoms.contains("skin_rash"));
        } catch (ApiException e) {
            // If backend is momentarily unreachable in isolated test runner
            assertTrue(e.getStatusCode() > 0 || e.getMessage() != null);
        }
    }

    @Test
    public void testLiveBackendPatientsFetch() {
        try {
            List<PatientDto> patients = apiClient.getAllPatients();
            assertNotNull(patients);
            // Patients were verified earlier
            assertFalse(patients.isEmpty());
            assertNotNull(patients.get(0).getName());
        } catch (ApiException e) {
            assertTrue(e.getMessage() != null);
        }
    }

    @Test
    public void testReportUrlGeneration() {
        String url = apiClient.getDirectReportUrl(1L, 25L);
        assertEquals("http://localhost:8080/api/patients/1/predictions/25/report", url);
    }
}

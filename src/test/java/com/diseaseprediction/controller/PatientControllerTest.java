package com.diseaseprediction.controller;

import com.diseaseprediction.dto.request.PatientRequest;
import com.diseaseprediction.model.Patient;
import com.diseaseprediction.repository.PatientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private com.diseaseprediction.repository.PredictionHistoryRepository predictionHistoryRepository;

    @BeforeEach
    void setUp() {
        predictionHistoryRepository.deleteAll();
        patientRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/patients - create patient")
    void testCreatePatient() throws Exception {
        PatientRequest request = new PatientRequest("John Doe", 35, "Male", "john@example.com");

        mockMvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @DisplayName("GET /api/patients/{id} - get patient by ID")
    void testGetPatientById() throws Exception {
        Patient patient = new Patient("John Doe", 35, "Male", "john_unique@example.com");
        patient = patientRepository.save(patient);

        mockMvc.perform(get("/api/patients/" + patient.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patient.getId()))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/patients/{id} - returns 404 when patient does not exist")
    void testGetPatientNotFound() throws Exception {
        mockMvc.perform(get("/api/patients/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /api/patients - list all patients")
    void testGetAllPatients() throws Exception {
        Patient p1 = new Patient("Alice", 25, "Female", "alice@example.com");
        Patient p2 = new Patient("Bob", 40, "Male", "bob@example.com");
        patientRepository.save(p1);
        patientRepository.save(p2);

        mockMvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/patients/{patientId}/predictions/{predictionId}/report - download PDF report")
    void testDownloadPredictionReportSuccess() throws Exception {
        Patient patient = new Patient("Alice", 25, "Female", "alice_report@example.com");
        patient = patientRepository.save(patient);

        com.diseaseprediction.model.PredictionHistory history = new com.diseaseprediction.model.PredictionHistory(
                patient, "GENERAL_DISEASE", "[\"fever\",\"headache\"]", "Fever", 88.0
        );
        history = predictionHistoryRepository.save(history);

        mockMvc.perform(get("/api/patients/" + patient.getId() + "/predictions/" + history.getId() + "/report"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("MedPredict_Report_" + patient.getId() + "_" + history.getId() + ".pdf")));
    }

    @Test
    @DisplayName("GET /api/patients/{patientId}/predictions/{predictionId}/report - returns 404 when prediction not found")
    void testDownloadPredictionReportNotFound() throws Exception {
        Patient patient = new Patient("Alice", 25, "Female", "alice_notfound@example.com");
        patient = patientRepository.save(patient);

        mockMvc.perform(get("/api/patients/" + patient.getId() + "/predictions/9999/report"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}

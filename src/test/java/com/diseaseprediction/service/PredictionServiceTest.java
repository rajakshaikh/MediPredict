package com.diseaseprediction.service;

import com.diseaseprediction.dto.request.DiseasePredictionRequest;
import com.diseaseprediction.dto.request.HeartDiseasePredictionRequest;
import com.diseaseprediction.dto.response.PatientDiseasePredictionResponse;
import com.diseaseprediction.dto.response.PatientHeartDiseasePredictionResponse;
import com.diseaseprediction.dto.response.PredictionHistoryResponse;
import com.diseaseprediction.exception.ResourceNotFoundException;
import com.diseaseprediction.model.Patient;
import com.diseaseprediction.model.PredictionHistory;
import com.diseaseprediction.repository.PatientRepository;
import com.diseaseprediction.repository.PredictionHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class PredictionServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PredictionHistoryRepository predictionHistoryRepository;

    private PatientService patientService;
    private MlService mlService;
    private MockRestServiceServer mockServer;
    private PredictionService predictionService;
    private ObjectMapper objectMapper;
    private PdfReportService pdfReportService;
    private final String mlBaseUrl = "http://localhost:8000";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        patientService = new PatientService(patientRepository);
        pdfReportService = new PdfReportService(objectMapper);

        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        mlService = new MlService(restTemplate, mlBaseUrl);

        predictionService = new PredictionService(patientService, mlService, predictionHistoryRepository, objectMapper, pdfReportService);
    }

    @Test
    @DisplayName("Should successfully execute general disease prediction and save history")
    void testPredictAndSaveGeneralDisease() {
        Long patientId = 1L;
        Patient mockPatient = new Patient("John Doe", 35, "Male", "john@example.com");
        mockPatient.setId(patientId);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(mockPatient));

        String mlResponseJson = "{\"prediction\":\"Malaria\",\"probability\":88.5," +
                "\"selected_symptoms\":[\"fever\",\"chills\"],\"message\":\"Prediction successful\"}";
        mockServer.expect(requestTo(mlBaseUrl + "/predict"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(mlResponseJson, MediaType.APPLICATION_JSON));

        PredictionHistory savedHistory = new PredictionHistory(
                mockPatient, "GENERAL_DISEASE", "[\"fever\",\"chills\"]", "Malaria", 88.5);
        savedHistory.setId(10L);
        savedHistory.setCreatedAt(LocalDateTime.now());
        when(predictionHistoryRepository.save(any(PredictionHistory.class))).thenReturn(savedHistory);

        DiseasePredictionRequest request = new DiseasePredictionRequest(List.of("fever", "chills"));
        PatientDiseasePredictionResponse result = predictionService.predictAndSaveGeneralDisease(patientId, request);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(patientId, result.getPatientId());
        assertEquals("Malaria", result.getPrediction());
        assertEquals(88.5, result.getProbability());
        assertEquals("GENERAL_DISEASE", result.getPredictionType());
        verify(predictionHistoryRepository, times(1)).save(any(PredictionHistory.class));
        mockServer.verify();
    }

    @Test
    @DisplayName("Should successfully execute heart disease prediction and save history")
    void testPredictAndSaveHeartDisease() {
        Long patientId = 1L;
        Patient mockPatient = new Patient("Jane Doe", 58, "Female", "jane@example.com");
        mockPatient.setId(patientId);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(mockPatient));

        String mlResponseJson = "{\"prediction\":1,\"result\":\"Heart Disease Detected\",\"probability\":78.0}";
        mockServer.expect(requestTo(mlBaseUrl + "/predict-heart-disease"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(mlResponseJson, MediaType.APPLICATION_JSON));

        PredictionHistory savedHistory = new PredictionHistory(
                mockPatient, "HEART_DISEASE", "{}", "Heart Disease Detected", 78.0);
        savedHistory.setId(20L);
        savedHistory.setCreatedAt(LocalDateTime.now());
        when(predictionHistoryRepository.save(any(PredictionHistory.class))).thenReturn(savedHistory);

        HeartDiseasePredictionRequest request = new HeartDiseasePredictionRequest();
        request.setAge(58.0);
        request.setSex(0.0);
        request.setCp(1.0);
        request.setTrestbps(130.0);
        request.setChol(240.0);
        request.setFbs(0.0);
        request.setRestecg(0.0);
        request.setThalach(145.0);
        request.setExang(0.0);
        request.setOldpeak(1.0);
        request.setSlope(1.0);
        request.setCa(0.0);
        request.setThal(2.0);

        PatientHeartDiseasePredictionResponse result = predictionService.predictAndSaveHeartDisease(patientId, request);

        assertNotNull(result);
        assertEquals(20L, result.getId());
        assertEquals(1, result.getPrediction());
        assertEquals("Heart Disease Detected", result.getResult());
        assertEquals(78.0, result.getProbability());
        verify(predictionHistoryRepository, times(1)).save(any(PredictionHistory.class));
        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when patient does not exist")
    void testPatientNotFound() {
        Long nonExistentId = 999L;
        when(patientRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        DiseasePredictionRequest request = new DiseasePredictionRequest(List.of("fever"));

        assertThrows(ResourceNotFoundException.class, () -> {
            predictionService.predictAndSaveGeneralDisease(nonExistentId, request);
        });

        verifyNoInteractions(predictionHistoryRepository);
    }

    @Test
    @DisplayName("Should retrieve patient prediction history ordered newest first")
    void testGetPredictionsByPatientId() {
        Long patientId = 1L;
        Patient mockPatient = new Patient("John Doe", 35, "Male", "john@example.com");
        mockPatient.setId(patientId);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(mockPatient));

        PredictionHistory h1 = new PredictionHistory(mockPatient, "GENERAL_DISEASE", "[\"fever\"]", "Fever", 90.0);
        h1.setId(1L);
        h1.setCreatedAt(LocalDateTime.now());

        when(predictionHistoryRepository.findByPatientIdOrderByCreatedAtDesc(patientId)).thenReturn(List.of(h1));

        List<PredictionHistoryResponse> list = predictionService.getPredictionsByPatientId(patientId);

        assertEquals(1, list.size());
        assertEquals("GENERAL_DISEASE", list.get(0).getPredictionType());
        assertEquals("John Doe", list.get(0).getPatientName());
    }

    @Test
    @DisplayName("Should successfully generate PDF report bytes for a valid patient and prediction")
    void testGeneratePredictionReportSuccess() {
        Long patientId = 1L;
        Long predictionId = 10L;

        Patient mockPatient = new Patient("John Doe", 35, "Male", "john@example.com");
        mockPatient.setId(patientId);

        PredictionHistory mockHistory = new PredictionHistory(
                mockPatient, "GENERAL_DISEASE", "[\"fever\",\"chills\"]", "Malaria", 88.5);
        mockHistory.setId(predictionId);
        mockHistory.setCreatedAt(LocalDateTime.now());

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(mockPatient));
        when(predictionHistoryRepository.findById(predictionId)).thenReturn(Optional.of(mockHistory));

        byte[] pdfBytes = predictionService.generatePredictionReport(patientId, predictionId);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        // Verify PDF Magic Bytes (%PDF-)
        String header = new String(pdfBytes, 0, 5);
        assertEquals("%PDF-", header);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when prediction does not belong to patient")
    void testGeneratePredictionReportMismatchedPatient() {
        Long requestedPatientId = 1L;
        Long actualPatientId = 2L;
        Long predictionId = 10L;

        Patient mockPatient1 = new Patient("John Doe", 35, "Male", "john@example.com");
        mockPatient1.setId(requestedPatientId);

        Patient mockPatient2 = new Patient("Jane Doe", 30, "Female", "jane@example.com");
        mockPatient2.setId(actualPatientId);

        PredictionHistory mockHistory = new PredictionHistory(
                mockPatient2, "GENERAL_DISEASE", "[\"fever\"]", "Fever", 80.0);
        mockHistory.setId(predictionId);

        when(patientRepository.findById(requestedPatientId)).thenReturn(Optional.of(mockPatient1));
        when(predictionHistoryRepository.findById(predictionId)).thenReturn(Optional.of(mockHistory));

        assertThrows(ResourceNotFoundException.class, () -> {
            predictionService.generatePredictionReport(requestedPatientId, predictionId);
        });
    }
}

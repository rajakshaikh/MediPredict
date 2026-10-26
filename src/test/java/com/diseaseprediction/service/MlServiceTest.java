package com.diseaseprediction.service;

import com.diseaseprediction.dto.request.DiseasePredictionRequest;
import com.diseaseprediction.dto.request.HeartDiseasePredictionRequest;
import com.diseaseprediction.dto.response.DiseasePredictionResponse;
import com.diseaseprediction.dto.response.HeartDiseasePredictionResponse;
import com.diseaseprediction.dto.response.SymptomsResponse;
import com.diseaseprediction.exception.MlServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class MlServiceTest {

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private MlService mlService;
    private final String baseUrl = "http://localhost:8000";

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        mlService = new MlService(restTemplate, baseUrl);
    }

    @Test
    @DisplayName("Should successfully fetch symptoms list")
    void testGetSymptomsSuccess() {
        String jsonResponse = "{\"symptoms\":[\"itching\",\"skin_rash\",\"nodal_skin_eruptions\"]}";

        mockServer.expect(requestTo(baseUrl + "/symptoms"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        SymptomsResponse response = mlService.getSymptoms();

        assertNotNull(response);
        assertEquals(3, response.getSymptoms().size());
        assertEquals("itching", response.getSymptoms().get(0));
        mockServer.verify();
    }

    @Test
    @DisplayName("Should successfully predict general disease")
    void testPredictGeneralDiseaseSuccess() {
        String jsonResponse = "{\"prediction\":\"Fungal infection\",\"probability\":92.5," +
                "\"selected_symptoms\":[\"itching\",\"skin_rash\"],\"message\":\"Success\"}";

        mockServer.expect(requestTo(baseUrl + "/predict"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        DiseasePredictionRequest request = new DiseasePredictionRequest(List.of("itching", "skin_rash"));
        DiseasePredictionResponse response = mlService.predictGeneralDisease(request);

        assertNotNull(response);
        assertEquals("Fungal infection", response.getPrediction());
        assertEquals(92.5, response.getProbability());
        assertEquals(2, response.getSelectedSymptoms().size());
        mockServer.verify();
    }

    @Test
    @DisplayName("Should successfully predict heart disease")
    void testPredictHeartDiseaseSuccess() {
        String jsonResponse = "{\"prediction\":1,\"result\":\"Heart Disease Detected\",\"probability\":84.0}";

        mockServer.expect(requestTo(baseUrl + "/predict-heart-disease"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        HeartDiseasePredictionRequest request = new HeartDiseasePredictionRequest();
        request.setAge(55.0);
        request.setSex(1.0);
        request.setCp(0.0);
        request.setTrestbps(140.0);
        request.setChol(240.0);
        request.setFbs(0.0);
        request.setRestecg(0.0);
        request.setThalach(150.0);
        request.setExang(0.0);
        request.setOldpeak(1.5);
        request.setSlope(1.0);
        request.setCa(0.0);
        request.setThal(2.0);

        HeartDiseasePredictionResponse response = mlService.predictHeartDisease(request);

        assertNotNull(response);
        assertEquals(1, response.getPrediction());
        assertEquals("Heart Disease Detected", response.getResult());
        assertEquals(84.0, response.getProbability());
        mockServer.verify();
    }

    @Test
    @DisplayName("Should throw exception when ML service returns error in body")
    void testPredictWithModelError() {
        String jsonResponse = "{\"error\":\"General disease model is not loaded.\"}";

        mockServer.expect(requestTo(baseUrl + "/predict"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        DiseasePredictionRequest request = new DiseasePredictionRequest(List.of("fever"));

        MlServiceException ex = assertThrows(MlServiceException.class, () -> {
            mlService.predictGeneralDisease(request);
        });

        assertTrue(ex.getMessage().contains("General disease model is not loaded"));
        mockServer.verify();
    }
}

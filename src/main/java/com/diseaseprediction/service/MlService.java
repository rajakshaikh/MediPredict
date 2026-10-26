package com.diseaseprediction.service;

import com.diseaseprediction.dto.request.DiseasePredictionRequest;
import com.diseaseprediction.dto.request.HeartDiseasePredictionRequest;
import com.diseaseprediction.dto.response.DiseasePredictionResponse;
import com.diseaseprediction.dto.response.HeartDiseasePredictionResponse;
import com.diseaseprediction.dto.response.SymptomsResponse;
import com.diseaseprediction.exception.MlServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MlService {

    private static final Logger log = LoggerFactory.getLogger(MlService.class);

    private final RestTemplate restTemplate;
    private final String mlBaseUrl;

    public MlService(RestTemplate restTemplate, @Value("${ml.service.base-url:http://localhost:8000}") String mlBaseUrl) {
        this.restTemplate = restTemplate;
        this.mlBaseUrl = mlBaseUrl;
    }

    /**
     * Retrieve the list of 132 available symptoms from the FastAPI ML service.
     */
    public SymptomsResponse getSymptoms() {
        String url = mlBaseUrl + "/symptoms";
        log.info("Calling ML service GET {}", url);

        try {
            ResponseEntity<SymptomsResponse> response = restTemplate.getForEntity(url, SymptomsResponse.class);

            if (response.getBody() == null || response.getBody().getSymptoms() == null) {
                throw new MlServiceException("Received empty symptoms list from ML service.");
            }

            return response.getBody();
        } catch (ResourceAccessException ex) {
            log.error("ML service unreachable at {}: {}", url, ex.getMessage());
            throw ex;
        } catch (HttpStatusCodeException ex) {
            log.error("ML service HTTP error {} at {}: {}", ex.getStatusCode(), url, ex.getResponseBodyAsString());
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error fetching symptoms: {}", ex.getMessage());
            throw new MlServiceException("Failed to fetch symptoms from ML service: " + ex.getMessage(), ex);
        }
    }

    /**
     * Call the ML service to predict a general disease based on symptoms.
     */
    public DiseasePredictionResponse predictGeneralDisease(DiseasePredictionRequest request) {
        String url = mlBaseUrl + "/predict";
        log.info("Calling ML service POST {} with {} symptoms", url,
                request.getSymptoms() != null ? request.getSymptoms().size() : 0);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<DiseasePredictionRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<DiseasePredictionResponse> response =
                    restTemplate.postForEntity(url, entity, DiseasePredictionResponse.class);

            DiseasePredictionResponse body = response.getBody();
            if (body == null) {
                throw new MlServiceException("Received empty response from ML service.");
            }

            if (body.getError() != null && !body.getError().isEmpty()) {
                throw new MlServiceException("ML service error: " + body.getError());
            }

            return body;
        } catch (ResourceAccessException | HttpStatusCodeException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error during general disease prediction: {}", ex.getMessage());
            throw new MlServiceException("General disease prediction failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Call the ML service to predict heart disease risk based on 13 clinical parameters.
     */
    public HeartDiseasePredictionResponse predictHeartDisease(HeartDiseasePredictionRequest request) {
        String url = mlBaseUrl + "/predict-heart-disease";
        log.info("Calling ML service POST {}", url);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<HeartDiseasePredictionRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<HeartDiseasePredictionResponse> response =
                    restTemplate.postForEntity(url, entity, HeartDiseasePredictionResponse.class);

            HeartDiseasePredictionResponse body = response.getBody();
            if (body == null) {
                throw new MlServiceException("Received empty response from heart disease ML service.");
            }

            if (body.getError() != null && !body.getError().isEmpty()) {
                throw new MlServiceException("ML service error: " + body.getError());
            }

            return body;
        } catch (ResourceAccessException | HttpStatusCodeException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error during heart disease prediction: {}", ex.getMessage());
            throw new MlServiceException("Heart disease prediction failed: " + ex.getMessage(), ex);
        }
    }
}

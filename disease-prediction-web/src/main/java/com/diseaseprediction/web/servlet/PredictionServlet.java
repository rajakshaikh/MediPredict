package com.diseaseprediction.web.servlet;

import com.diseaseprediction.web.client.ApiException;
import com.diseaseprediction.web.client.BackendApiClient;
import com.diseaseprediction.web.dto.DiseasePredictionDto;
import com.diseaseprediction.web.dto.HeartDiseasePredictionDto;
import com.diseaseprediction.web.dto.PatientDto;
import com.diseaseprediction.web.util.SymptomCatalog;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.*;

@WebServlet(name = "PredictionServlet", urlPatterns = {"/predict", "/predict/general", "/predict/heart"})
public class PredictionServlet extends HttpServlet {

    private BackendApiClient apiClient;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String baseUrl = config.getServletContext().getInitParameter("backend.base.url");
        this.apiClient = new BackendApiClient(baseUrl);
    }

    public PredictionServlet(BackendApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public PredictionServlet() {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();
        String typeParam = request.getParameter("type");

        boolean isHeart = "/predict/heart".equalsIgnoreCase(servletPath) || "heart".equalsIgnoreCase(typeParam);

        if (isHeart) {
            handleHeartGet(request, response);
        } else {
            handleGeneralGet(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String predictionType = request.getParameter("predictionType");
        String servletPath = request.getServletPath();

        boolean isHeart = "heart".equalsIgnoreCase(predictionType) || "/predict/heart".equalsIgnoreCase(servletPath);

        if (isHeart) {
            handleHeartPost(request, response);
        } else {
            handleGeneralPost(request, response);
        }
    }

    private void handleGeneralGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        loadPatientsList(request);

        try {
            List<String> rawSymptoms = apiClient.getSymptoms();
            request.setAttribute("rawSymptoms", rawSymptoms);
            request.setAttribute("categorizedSymptoms", SymptomCatalog.categorizeSymptoms(rawSymptoms));
        } catch (ApiException e) {
            request.setAttribute("errorMessage", "Unable to load symptoms from ML service: " + e.getMessage());
            request.setAttribute("rawSymptoms", Collections.emptyList());
            request.setAttribute("categorizedSymptoms", Collections.emptyMap());
        }

        String preselectedPatientId = request.getParameter("patientId");
        if (preselectedPatientId != null && !preselectedPatientId.isBlank()) {
            request.setAttribute("selectedPatientId", preselectedPatientId);
        }

        request.setAttribute("activeNav", "general-predict");
        request.getRequestDispatcher("/WEB-INF/views/predict-general.jsp").forward(request, response);
    }

    private void handleHeartGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        loadPatientsList(request);

        String preselectedPatientId = request.getParameter("patientId");
        if (preselectedPatientId != null && !preselectedPatientId.isBlank()) {
            request.setAttribute("selectedPatientId", preselectedPatientId);
        }

        request.setAttribute("activeNav", "heart-predict");
        request.getRequestDispatcher("/WEB-INF/views/predict-heart.jsp").forward(request, response);
    }

    private void handleGeneralPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String patientIdStr = request.getParameter("patientId");
        String[] selectedSymptomsArray = request.getParameterValues("symptoms");

        request.setAttribute("selectedPatientId", patientIdStr);
        request.setAttribute("activeNav", "general-predict");

        Long patientId = parsePatientId(patientIdStr);
        if (patientId == null) {
            request.setAttribute("errorMessage", "A valid Target Patient ID is required.");
            handleGeneralGet(request, response);
            return;
        }

        if (selectedSymptomsArray == null || selectedSymptomsArray.length == 0) {
            request.setAttribute("errorMessage", "Please select at least one medical symptom to evaluate.");
            handleGeneralGet(request, response);
            return;
        }

        List<String> symptoms = Arrays.asList(selectedSymptomsArray);
        request.setAttribute("previouslySelectedSymptoms", symptoms);

        try {
            DiseasePredictionDto result = apiClient.predictGeneralDisease(patientId, symptoms);

            request.setAttribute("predictionType", "GENERAL_DISEASE");
            request.setAttribute("result", result);
            request.setAttribute("directPdfUrl", apiClient.getDirectReportUrl(patientId, result.getId()));
            request.setAttribute("downloadReportUrl", request.getContextPath() + "/history/report?patientId=" + patientId + "&predictionId=" + result.getId());

            request.getRequestDispatcher("/WEB-INF/views/prediction-result.jsp").forward(request, response);

        } catch (ApiException e) {
            request.setAttribute("errorMessage", "Prediction failed: " + e.getMessage());
            handleGeneralGet(request, response);
        }
    }

    private void handleHeartPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String patientIdStr = request.getParameter("patientId");
        request.setAttribute("selectedPatientId", patientIdStr);
        request.setAttribute("activeNav", "heart-predict");

        Long patientId = parsePatientId(patientIdStr);
        if (patientId == null) {
            request.setAttribute("errorMessage", "A valid Target Patient ID is required.");
            handleHeartGet(request, response);
            return;
        }

        Map<String, Object> heartData = new HashMap<>();
        try {
            heartData.put("age", parseRequiredDouble(request, "age", "Age"));
            heartData.put("sex", parseRequiredDouble(request, "sex", "Sex"));
            heartData.put("cp", parseRequiredDouble(request, "cp", "Chest Pain Type (cp)"));
            heartData.put("trestbps", parseRequiredDouble(request, "trestbps", "Resting Blood Pressure (trestbps)"));
            heartData.put("chol", parseRequiredDouble(request, "chol", "Cholesterol (chol)"));
            heartData.put("fbs", parseRequiredDouble(request, "fbs", "Fasting Blood Sugar (fbs)"));
            heartData.put("restecg", parseRequiredDouble(request, "restecg", "Resting ECG (restecg)"));
            heartData.put("thalach", parseRequiredDouble(request, "thalach", "Max Heart Rate (thalach)"));
            heartData.put("exang", parseRequiredDouble(request, "exang", "Exercise Induced Angina (exang)"));
            heartData.put("oldpeak", parseRequiredDouble(request, "oldpeak", "ST Depression (oldpeak)"));
            heartData.put("slope", parseRequiredDouble(request, "slope", "Slope"));
            heartData.put("ca", parseRequiredDouble(request, "ca", "Major Vessels (ca)"));
            heartData.put("thal", parseRequiredDouble(request, "thal", "Thalassemia (thal)"));
        } catch (IllegalArgumentException e) {
            request.setAttribute("errorMessage", e.getMessage());
            handleHeartGet(request, response);
            return;
        }

        try {
            HeartDiseasePredictionDto result = apiClient.predictHeartDisease(patientId, heartData);

            request.setAttribute("predictionType", "HEART_DISEASE");
            request.setAttribute("heartResult", result);
            request.setAttribute("directPdfUrl", apiClient.getDirectReportUrl(patientId, result.getId()));
            request.setAttribute("downloadReportUrl", request.getContextPath() + "/history/report?patientId=" + patientId + "&predictionId=" + result.getId());

            request.getRequestDispatcher("/WEB-INF/views/prediction-result.jsp").forward(request, response);

        } catch (ApiException e) {
            request.setAttribute("errorMessage", "Heart disease risk evaluation failed: " + e.getMessage());
            handleHeartGet(request, response);
        }
    }

    private void loadPatientsList(HttpServletRequest request) {
        try {
            List<PatientDto> patients = apiClient.getAllPatients();
            request.setAttribute("patients", patients != null ? patients : Collections.emptyList());
        } catch (Exception e) {
            request.setAttribute("patientListError", "Could not load patient registry: " + e.getMessage());
            request.setAttribute("patients", Collections.emptyList());
        }
    }

    private Long parsePatientId(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        try {
            long val = Long.parseLong(str.trim());
            return val > 0 ? val : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseRequiredDouble(HttpServletRequest request, String paramName, String label) {
        String val = request.getParameter(paramName);
        if (val == null || val.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required.");
        }
        try {
            return Double.parseDouble(val.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(label + " must be a valid number.");
        }
    }
}

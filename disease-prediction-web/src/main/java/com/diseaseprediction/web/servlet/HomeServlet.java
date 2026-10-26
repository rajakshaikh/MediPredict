package com.diseaseprediction.web.servlet;

import com.diseaseprediction.web.client.BackendApiClient;
import com.diseaseprediction.web.dto.PatientDto;
import com.diseaseprediction.web.dto.PredictionHistoryDto;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "HomeServlet", urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {

    private BackendApiClient apiClient;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String baseUrl = config.getServletContext().getInitParameter("backend.base.url");
        this.apiClient = new BackendApiClient(baseUrl);
    }

    // Constructor for testing
    public HomeServlet(BackendApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public HomeServlet() {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int totalPatients = 0;
        int totalPredictions = 0;
        boolean backendConnected = true;
        String backendStatusMessage = "Connected to Spring Boot REST backend";

        try {
            List<PatientDto> patients = apiClient.getAllPatients();
            totalPatients = patients != null ? patients.size() : 0;

            List<PredictionHistoryDto> predictions = apiClient.getAllPredictions();
            totalPredictions = predictions != null ? predictions.size() : 0;
        } catch (Exception e) {
            backendConnected = false;
            backendStatusMessage = "Backend check warning: " + e.getMessage();
        }

        request.setAttribute("totalPatients", totalPatients);
        request.setAttribute("totalPredictions", totalPredictions);
        request.setAttribute("backendConnected", backendConnected);
        request.setAttribute("backendStatusMessage", backendStatusMessage);
        request.setAttribute("activeNav", "dashboard");

        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }
}

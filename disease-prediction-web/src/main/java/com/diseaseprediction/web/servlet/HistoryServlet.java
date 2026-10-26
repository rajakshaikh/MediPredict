package com.diseaseprediction.web.servlet;

import com.diseaseprediction.web.client.ApiException;
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
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

@WebServlet(name = "HistoryServlet", urlPatterns = {"/history", "/history/report"})
public class HistoryServlet extends HttpServlet {

    private BackendApiClient apiClient;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String baseUrl = config.getServletContext().getInitParameter("backend.base.url");
        this.apiClient = new BackendApiClient(baseUrl);
    }

    public HistoryServlet(BackendApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public HistoryServlet() {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();
        String action = request.getParameter("action");

        if ("/history/report".equalsIgnoreCase(servletPath) || "report".equalsIgnoreCase(action)) {
            handlePdfReportDownload(request, response);
            return;
        }

        handleHistoryView(request, response);
    }

    private void handleHistoryView(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String patientIdStr = request.getParameter("patientId");
        Long patientId = parsePatientId(patientIdStr);

        loadPatientsList(request);

        List<PredictionHistoryDto> historyList = Collections.emptyList();
        try {
            if (patientId != null) {
                historyList = apiClient.getPatientPredictions(patientId);
                request.setAttribute("selectedPatientId", patientId.toString());
                request.setAttribute("filterDescription", "Filtered for Patient #" + patientId);
            } else {
                historyList = apiClient.getAllPredictions();
                request.setAttribute("filterDescription", "All Registered System Records");
            }
        } catch (ApiException e) {
            request.setAttribute("errorMessage", "Unable to load prediction history: " + e.getMessage());
        }

        request.setAttribute("historyList", historyList);
        request.setAttribute("activeNav", "history");
        request.getRequestDispatcher("/WEB-INF/views/history.jsp").forward(request, response);
    }

    private void handlePdfReportDownload(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        Long patientId = parsePatientId(request.getParameter("patientId"));
        Long predictionId = parsePatientId(request.getParameter("predictionId"));

        if (patientId == null || predictionId == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required patientId or predictionId for PDF report.");
            return;
        }

        try {
            byte[] pdfBytes = apiClient.downloadReport(patientId, predictionId);

            String filename = String.format("MedPredict_Report_%d_%d.pdf", patientId, predictionId);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            response.setContentLength(pdfBytes.length);

            try (OutputStream out = response.getOutputStream()) {
                out.write(pdfBytes);
                out.flush();
            }
        } catch (ApiException e) {
            response.sendError(e.getStatusCode(), "Could not retrieve medical report: " + e.getMessage());
        }
    }

    private void loadPatientsList(HttpServletRequest request) {
        try {
            List<PatientDto> patients = apiClient.getAllPatients();
            request.setAttribute("patients", patients != null ? patients : Collections.emptyList());
        } catch (Exception e) {
            request.setAttribute("patientListError", "Could not load patient dropdown: " + e.getMessage());
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
}

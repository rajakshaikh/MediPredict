package com.diseaseprediction.web.servlet;

import com.diseaseprediction.web.client.ApiException;
import com.diseaseprediction.web.client.BackendApiClient;
import com.diseaseprediction.web.dto.PatientDto;
import com.diseaseprediction.web.dto.PatientRegistrationDto;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@WebServlet(name = "PatientServlet", urlPatterns = {"/patients", "/patients/register"})
public class PatientServlet extends HttpServlet {

    private BackendApiClient apiClient;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String baseUrl = config.getServletContext().getInitParameter("backend.base.url");
        this.apiClient = new BackendApiClient(baseUrl);
    }

    public PatientServlet(BackendApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public PatientServlet() {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        loadPatientList(request);
        request.setAttribute("activeNav", "patients");
        request.getRequestDispatcher("/WEB-INF/views/patient-register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");
        String ageStr = request.getParameter("age");
        String gender = request.getParameter("gender");
        String email = request.getParameter("email");

        // Preserve input values in case of error
        request.setAttribute("inputName", name);
        request.setAttribute("inputAge", ageStr);
        request.setAttribute("inputGender", gender);
        request.setAttribute("inputEmail", email);
        request.setAttribute("activeNav", "patients");

        // Validation
        if (name == null || name.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Patient name is required.");
            loadPatientList(request);
            request.getRequestDispatcher("/WEB-INF/views/patient-register.jsp").forward(request, response);
            return;
        }

        Integer age = null;
        try {
            age = Integer.parseInt(ageStr != null ? ageStr.trim() : "");
            if (age < 0 || age > 150) {
                request.setAttribute("errorMessage", "Age must be a realistic number between 0 and 150.");
                loadPatientList(request);
                request.getRequestDispatcher("/WEB-INF/views/patient-register.jsp").forward(request, response);
                return;
            }
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "Valid numeric age is required.");
            loadPatientList(request);
            request.getRequestDispatcher("/WEB-INF/views/patient-register.jsp").forward(request, response);
            return;
        }

        if (gender == null || gender.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Gender is required.");
            loadPatientList(request);
            request.getRequestDispatcher("/WEB-INF/views/patient-register.jsp").forward(request, response);
            return;
        }

        String cleanedEmail = (email != null && !email.trim().isEmpty()) ? email.trim() : null;

        try {
            PatientRegistrationDto regDto = new PatientRegistrationDto(name.trim(), age, gender.trim(), cleanedEmail);
            PatientDto created = apiClient.registerPatient(regDto);

            request.setAttribute("registeredPatient", created);
            request.setAttribute("successMessage", "Patient registered successfully! Patient ID: #" + created.getId());

            // Clear inputs on success
            request.removeAttribute("inputName");
            request.removeAttribute("inputAge");
            request.removeAttribute("inputGender");
            request.removeAttribute("inputEmail");

        } catch (ApiException e) {
            request.setAttribute("errorMessage", "Registration failed: " + e.getMessage());
        } catch (Exception e) {
            request.setAttribute("errorMessage", "Unexpected error during registration: " + e.getMessage());
        }

        loadPatientList(request);
        request.getRequestDispatcher("/WEB-INF/views/patient-register.jsp").forward(request, response);
    }

    private void loadPatientList(HttpServletRequest request) {
        try {
            List<PatientDto> patients = apiClient.getAllPatients();
            request.setAttribute("patients", patients != null ? patients : Collections.emptyList());
        } catch (Exception e) {
            request.setAttribute("patientListError", "Could not load patient registry: " + e.getMessage());
            request.setAttribute("patients", Collections.emptyList());
        }
    }
}

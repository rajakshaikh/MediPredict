package com.diseaseprediction.service;

import com.diseaseprediction.dto.request.PatientRequest;
import com.diseaseprediction.dto.response.PatientResponse;
import com.diseaseprediction.exception.ResourceNotFoundException;
import com.diseaseprediction.model.Patient;
import com.diseaseprediction.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public PatientResponse createPatient(PatientRequest request) {
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (patientRepository.existsByEmail(request.getEmail().trim())) {
                throw new IllegalArgumentException("Patient with email '" + request.getEmail() + "' already exists");
            }
        }

        Patient patient = new Patient(
                request.getName(),
                request.getAge(),
                request.getGender(),
                request.getEmail() != null ? request.getEmail().trim() : null
        );

        Patient saved = patientRepository.save(patient);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long id) {
        Patient patient = getPatientEntity(id);
        return mapToResponse(patient);
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Patient getPatientEntity(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }

    public PatientResponse mapToResponse(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getName(),
                patient.getAge(),
                patient.getGender(),
                patient.getEmail(),
                patient.getCreatedAt()
        );
    }
}

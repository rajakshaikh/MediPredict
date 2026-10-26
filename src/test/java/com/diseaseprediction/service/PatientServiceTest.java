package com.diseaseprediction.service;

import com.diseaseprediction.dto.request.PatientRequest;
import com.diseaseprediction.dto.response.PatientResponse;
import com.diseaseprediction.exception.ResourceNotFoundException;
import com.diseaseprediction.model.Patient;
import com.diseaseprediction.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    private PatientService patientService;

    @BeforeEach
    void setUp() {
        patientService = new PatientService(patientRepository);
    }

    @Test
    @DisplayName("Should successfully create a new patient")
    void testCreatePatient() {
        PatientRequest request = new PatientRequest("Alice Smith", 28, "Female", "alice@example.com");
        Patient saved = new Patient("Alice Smith", 28, "Female", "alice@example.com");
        saved.setId(1L);

        when(patientRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(patientRepository.save(any(Patient.class))).thenReturn(saved);

        PatientResponse response = patientService.createPatient(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Alice Smith", response.getName());
        assertEquals(28, response.getAge());
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    @DisplayName("Should throw exception if email is already registered")
    void testCreatePatientDuplicateEmail() {
        PatientRequest request = new PatientRequest("Alice Smith", 28, "Female", "alice@example.com");

        when(patientRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> patientService.createPatient(request));
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    @DisplayName("Should find patient by ID")
    void testGetPatientById() {
        Patient patient = new Patient("Bob Jones", 45, "Male", "bob@example.com");
        patient.setId(5L);

        when(patientRepository.findById(5L)).thenReturn(Optional.of(patient));

        PatientResponse response = patientService.getPatientById(5L);

        assertNotNull(response);
        assertEquals(5L, response.getId());
        assertEquals("Bob Jones", response.getName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when patient not found")
    void testGetPatientNotFound() {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> patientService.getPatientById(999L));
    }

    @Test
    @DisplayName("Should list all patients")
    void testGetAllPatients() {
        Patient p1 = new Patient("Bob Jones", 45, "Male", "bob@example.com");
        p1.setId(1L);
        Patient p2 = new Patient("Alice Smith", 28, "Female", "alice@example.com");
        p2.setId(2L);

        when(patientRepository.findAll()).thenReturn(List.of(p1, p2));

        List<PatientResponse> list = patientService.getAllPatients();

        assertEquals(2, list.size());
    }
}

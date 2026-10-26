package com.diseaseprediction.repository;

import com.diseaseprediction.model.PredictionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PredictionHistoryRepository extends JpaRepository<PredictionHistory, Long> {

    List<PredictionHistory> findByPatientId(Long patientId);

    List<PredictionHistory> findByPatientIdOrderByCreatedAtDesc(Long patientId);

    List<PredictionHistory> findAllByOrderByCreatedAtDesc();

    java.util.Optional<PredictionHistory> findByIdAndPatientId(Long id, Long patientId);
}

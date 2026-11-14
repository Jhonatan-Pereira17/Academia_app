package com.academiaapp.backend.evaluation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhysicalEvaluationRepository extends JpaRepository<PhysicalEvaluation, Long> {

    List<PhysicalEvaluation> findByStudentIdOrderByEvaluationDateDesc(Long studentId);
}
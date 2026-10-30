package com.academiaapp.backend.exercise;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    List<Exercise> findAllByOrderByNameAsc();

    List<Exercise> findByMuscleGroupOrderByNameAsc(MuscleGroup muscleGroup);
}
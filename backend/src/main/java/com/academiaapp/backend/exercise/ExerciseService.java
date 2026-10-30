package com.academiaapp.backend.exercise;

import com.academiaapp.backend.personal.Personal;
import com.academiaapp.backend.personal.PersonalRepository;
import com.academiaapp.backend.user.Role;
import com.academiaapp.backend.user.User;
import com.academiaapp.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final PersonalRepository personalRepository;
    private final UserRepository userRepository;

    public ExerciseService(ExerciseRepository exerciseRepository,
            PersonalRepository personalRepository,
            UserRepository userRepository) {
        this.exerciseRepository = exerciseRepository;
        this.personalRepository = personalRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ExerciseResponseDTO create(String requesterEmail, CreateExerciseRequest request) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));

        Personal creator = null;
        if (requester.getRole() == Role.PERSONAL) {
            creator = personalRepository.findById(requester.getId())
                    .orElseThrow(() -> new IllegalStateException("Usuário PERSONAL sem perfil de personal"));
        }

        Exercise exercise = new Exercise();
        exercise.setName(request.name());
        exercise.setMuscleGroup(request.muscleGroup());
        exercise.setPrimaryMuscle(request.primaryMuscle());
        exercise.setSecondaryMuscles(request.secondaryMuscles());
        exercise.setEquipment(request.equipment());
        exercise.setDescription(request.description());
        exercise.setInstructions(request.instructions());
        exercise.setImageUrl(request.imageUrl());
        exercise.setCreatedBy(creator);
        exerciseRepository.save(exercise);

        return ExerciseResponseDTO.fromEntity(exercise);
    }

    @Transactional(readOnly = true)
    public List<ExerciseResponseDTO> list(MuscleGroup muscleGroup) {
        List<Exercise> exercises = muscleGroup == null
                ? exerciseRepository.findAllByOrderByNameAsc()
                : exerciseRepository.findByMuscleGroupOrderByNameAsc(muscleGroup);

        return exercises.stream()
                .map(ExerciseResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExerciseResponseDTO findById(Long id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ExerciseNotFoundException(id));
        return ExerciseResponseDTO.fromEntity(exercise);
    }
}
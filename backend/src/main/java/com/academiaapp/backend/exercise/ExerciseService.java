package com.academiaapp.backend.exercise;

import com.academiaapp.backend.personal.Personal;
import com.academiaapp.backend.personal.PersonalRepository;
import com.academiaapp.backend.user.Role;
import com.academiaapp.backend.user.User;
import com.academiaapp.backend.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
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
        User requester = findUser(requesterEmail);

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
        return ExerciseResponseDTO.fromEntity(findExercise(id));
    }

    @Transactional
    public ExerciseResponseDTO update(String requesterEmail, Long id, UpdateExerciseRequest request) {
        User requester = findUser(requesterEmail);
        Exercise exercise = findExercise(id);
        checkCanModify(requester, exercise);

        exercise.setName(request.name());
        exercise.setMuscleGroup(request.muscleGroup());
        exercise.setPrimaryMuscle(request.primaryMuscle());
        exercise.setSecondaryMuscles(request.secondaryMuscles());
        exercise.setEquipment(request.equipment());
        exercise.setDescription(request.description());
        exercise.setInstructions(request.instructions());
        exercise.setImageUrl(request.imageUrl());

        return ExerciseResponseDTO.fromEntity(exercise);
    }

    @Transactional
    public void delete(String requesterEmail, Long id) {
        User requester = findUser(requesterEmail);
        Exercise exercise = findExercise(id);
        checkCanModify(requester, exercise);

        exerciseRepository.delete(exercise);
    }

    private void checkCanModify(User requester, Exercise exercise) {
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        boolean isOwnerPersonal = requester.getRole() == Role.PERSONAL
                && exercise.getCreatedBy() != null
                && exercise.getCreatedBy().getId().equals(requester.getId());

        if (!isAdmin && !isOwnerPersonal) {
            throw new AccessDeniedException("Você não pode alterar este exercício");
        }
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
    }

    private Exercise findExercise(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ExerciseNotFoundException(id));
    }
}
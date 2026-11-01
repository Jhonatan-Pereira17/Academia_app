package com.academiaapp.backend.workout;

import com.academiaapp.backend.config.BadRequestException;
import com.academiaapp.backend.exercise.Exercise;
import com.academiaapp.backend.exercise.ExerciseRepository;
import com.academiaapp.backend.student.Student;
import com.academiaapp.backend.student.StudentNotFoundException;
import com.academiaapp.backend.student.StudentRepository;
import com.academiaapp.backend.user.Role;
import com.academiaapp.backend.user.User;
import com.academiaapp.backend.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final StudentRepository studentRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;

    public WorkoutService(WorkoutRepository workoutRepository,
            StudentRepository studentRepository,
            ExerciseRepository exerciseRepository,
            UserRepository userRepository) {
        this.workoutRepository = workoutRepository;
        this.studentRepository = studentRepository;
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public WorkoutResponseDTO create(String requesterEmail, CreateWorkoutRequest request) {
        User requester = findUser(requesterEmail);
        Student student = resolveTargetStudent(requester, request.studentId());

        Workout workout = new Workout();
        workout.setStudent(student);
        workout.setCreatedBy(requester);
        workout.setName(request.name());
        workout.setDescription(request.description());
        fillExercises(workout, request.exercises());

        workoutRepository.save(workout);
        return WorkoutResponseDTO.fromEntity(workout);
    }

    @Transactional(readOnly = true)
    public List<WorkoutResponseDTO> list(String requesterEmail, boolean includeArchived) {
        User requester = findUser(requesterEmail);

        List<Workout> workouts = switch (requester.getRole()) {
            case ADMIN -> workoutRepository.findAllByOrderByCreatedAtDesc();
            case PERSONAL -> workoutRepository.findByStudentPersonalIdOrderByCreatedAtDesc(requester.getId());
            case STUDENT -> workoutRepository.findByStudentIdOrderByCreatedAtDesc(requester.getId());
        };

        return workouts.stream()
                .filter(workout -> includeArchived || workout.isActive())
                .map(WorkoutResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkoutResponseDTO findById(String requesterEmail, Long id) {
        User requester = findUser(requesterEmail);
        Workout workout = findWorkout(id);
        checkCanView(requester, workout);

        return WorkoutResponseDTO.fromEntity(workout);
    }

    @Transactional
    public WorkoutResponseDTO update(String requesterEmail, Long id, UpdateWorkoutRequest request) {
        User requester = findUser(requesterEmail);
        Workout workout = findWorkout(id);
        checkCanModify(requester, workout);

        workout.setName(request.name());
        workout.setDescription(request.description());
        workout.getExercises().clear();
        fillExercises(workout, request.exercises());

        workoutRepository.saveAndFlush(workout);
        return WorkoutResponseDTO.fromEntity(workout);
    }

    @Transactional
    public WorkoutResponseDTO setActive(String requesterEmail, Long id, boolean active) {
        User requester = findUser(requesterEmail);
        Workout workout = findWorkout(id);
        checkCanModify(requester, workout);

        workout.setActive(active);
        return WorkoutResponseDTO.fromEntity(workout);
    }

    @Transactional
    public void delete(String requesterEmail, Long id) {
        User requester = findUser(requesterEmail);
        Workout workout = findWorkout(id);
        checkCanModify(requester, workout);

        workoutRepository.delete(workout);
    }

    private void fillExercises(Workout workout, List<CreateWorkoutRequest.ExerciseItem> items) {
        int exerciseOrder = 1;
        for (CreateWorkoutRequest.ExerciseItem item : items) {
            Exercise exercise = exerciseRepository.findById(item.exerciseId())
                    .orElseThrow(() -> new BadRequestException("Exercício não encontrado: " + item.exerciseId()));

            WorkoutExercise workoutExercise = new WorkoutExercise();
            workoutExercise.setWorkout(workout);
            workoutExercise.setExercise(exercise);
            workoutExercise.setExerciseOrder(exerciseOrder++);
            workoutExercise.setRestSeconds(item.restSeconds());
            workoutExercise.setNotes(item.notes());

            int setNumber = 1;
            for (CreateWorkoutRequest.SetItem setItem : item.sets()) {
                WorkoutSet workoutSet = new WorkoutSet();
                workoutSet.setWorkoutExercise(workoutExercise);
                workoutSet.setSetNumber(setNumber++);
                workoutSet.setPlannedReps(setItem.plannedReps());
                workoutSet.setPlannedLoad(setItem.plannedLoad());
                workoutSet.setTargetRpe(setItem.targetRpe());
                workoutSet.setToFailure(Boolean.TRUE.equals(setItem.toFailure()));
                workoutExercise.getSets().add(workoutSet);
            }

            workout.getExercises().add(workoutExercise);
        }
    }

    private Student resolveTargetStudent(User requester, Long requestedStudentId) {
        if (requester.getRole() == Role.STUDENT) {
            if (requestedStudentId != null && !requestedStudentId.equals(requester.getId())) {
                throw new AccessDeniedException("O aluno só pode criar treinos para si mesmo");
            }
            return studentRepository.findById(requester.getId())
                    .orElseThrow(() -> new IllegalStateException("Usuário STUDENT sem perfil de aluno"));
        }

        if (requestedStudentId == null) {
            throw new BadRequestException("O aluno do treino (studentId) é obrigatório");
        }

        Student student = studentRepository.findById(requestedStudentId)
                .orElseThrow(() -> new StudentNotFoundException(requestedStudentId));

        if (!student.getPersonal().getId().equals(requester.getId())) {
            throw new AccessDeniedException("Este aluno não pertence a você");
        }
        return student;
    }

    private void checkCanView(User requester, Workout workout) {
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        boolean isOwnerStudent = requester.getRole() == Role.STUDENT
                && workout.getStudent().getId().equals(requester.getId());
        boolean isOwnerPersonal = requester.getRole() == Role.PERSONAL
                && workout.getStudent().getPersonal().getId().equals(requester.getId());

        if (!isAdmin && !isOwnerStudent && !isOwnerPersonal) {
            throw new AccessDeniedException("Acesso negado a este treino");
        }
    }

    private void checkCanModify(User requester, Workout workout) {
        boolean isOwnerPersonal = requester.getRole() == Role.PERSONAL
                && workout.getStudent().getPersonal().getId().equals(requester.getId());
        boolean isCreatorStudent = requester.getRole() == Role.STUDENT
                && workout.getStudent().getId().equals(requester.getId())
                && workout.getCreatedBy().getId().equals(requester.getId());

        if (!isOwnerPersonal && !isCreatorStudent) {
            throw new AccessDeniedException("Você não pode alterar este treino");
        }
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
    }

    private Workout findWorkout(Long id) {
        return workoutRepository.findById(id)
                .orElseThrow(() -> new WorkoutNotFoundException(id));
    }
}
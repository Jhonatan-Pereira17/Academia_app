package com.academiaapp.backend.workoutsession;

import com.academiaapp.backend.config.BadRequestException;
import com.academiaapp.backend.exercise.Exercise;
import com.academiaapp.backend.exercise.ExerciseRepository;
import com.academiaapp.backend.student.Student;
import com.academiaapp.backend.student.StudentRepository;
import com.academiaapp.backend.user.Role;
import com.academiaapp.backend.user.User;
import com.academiaapp.backend.user.UserRepository;
import com.academiaapp.backend.workout.Workout;
import com.academiaapp.backend.workout.WorkoutRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class WorkoutSessionService {

    public record SaveResult(WorkoutSessionResponseDTO session, boolean created) {
    }

    private final WorkoutSessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final WorkoutRepository workoutRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;

    public WorkoutSessionService(WorkoutSessionRepository sessionRepository,
            StudentRepository studentRepository,
            WorkoutRepository workoutRepository,
            ExerciseRepository exerciseRepository,
            UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.studentRepository = studentRepository;
        this.workoutRepository = workoutRepository;
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SaveResult create(String requesterEmail, CreateWorkoutSessionRequest request) {
        User requester = findUser(requesterEmail);
        Student student = studentRepository.findById(requester.getId())
                .orElseThrow(() -> new IllegalStateException("Usuário STUDENT sem perfil de aluno"));

        Optional<WorkoutSession> existing = sessionRepository.findByStudentIdAndClientId(student.getId(),
                request.clientId());
        if (existing.isPresent()) {
            return new SaveResult(WorkoutSessionResponseDTO.fromEntity(existing.get()), false);
        }

        if (request.finishedAt() != null && request.finishedAt().isBefore(request.startedAt())) {
            throw new BadRequestException("O término não pode ser anterior ao início");
        }

        WorkoutSession session = new WorkoutSession();
        session.setClientId(request.clientId());
        session.setStudent(student);
        session.setStartedAt(request.startedAt());
        session.setFinishedAt(request.finishedAt());
        session.setNotes(request.notes());

        if (request.workoutId() != null) {
            Workout workout = workoutRepository.findById(request.workoutId())
                    .orElseThrow(() -> new BadRequestException("Treino não encontrado: " + request.workoutId()));
            if (!workout.getStudent().getId().equals(student.getId())) {
                throw new AccessDeniedException("Este treino não pertence a você");
            }
            session.setWorkout(workout);
            session.setWorkoutName(workout.getName());
        } else if (request.workoutName() == null || request.workoutName().isBlank()) {
            throw new BadRequestException("Informe o treino (workoutId) ou um nome para o treino livre (workoutName)");
        } else {
            session.setWorkoutName(request.workoutName().trim());
        }

        if (request.logs() != null) {
            addLogs(session, request.logs());
        }

        sessionRepository.save(session);
        return new SaveResult(WorkoutSessionResponseDTO.fromEntity(session), true);
    }

    @Transactional
    public WorkoutSessionResponseDTO addLogs(String requesterEmail, Long sessionId,
            List<CreateWorkoutSessionRequest.LogItem> items) {
        User requester = findUser(requesterEmail);
        WorkoutSession session = findSession(sessionId);
        checkIsOwnerStudent(requester, session);

        addLogs(session, items);

        sessionRepository.saveAndFlush(session);
        return WorkoutSessionResponseDTO.fromEntity(session);
    }

    @Transactional
    public WorkoutSessionResponseDTO finish(String requesterEmail, Long sessionId,
            FinishWorkoutSessionRequest request) {
        User requester = findUser(requesterEmail);
        WorkoutSession session = findSession(sessionId);
        checkIsOwnerStudent(requester, session);

        if (request.finishedAt().isBefore(session.getStartedAt())) {
            throw new BadRequestException("O término não pode ser anterior ao início");
        }

        session.setFinishedAt(request.finishedAt());
        if (request.notes() != null) {
            session.setNotes(request.notes());
        }
        return WorkoutSessionResponseDTO.fromEntity(session);
    }

    @Transactional(readOnly = true)
    public WorkoutSessionResponseDTO findById(String requesterEmail, Long sessionId) {
        User requester = findUser(requesterEmail);
        WorkoutSession session = findSession(sessionId);
        checkCanView(requester, session);

        return WorkoutSessionResponseDTO.fromEntity(session);
    }

    private void addLogs(WorkoutSession session, List<CreateWorkoutSessionRequest.LogItem> items) {
        Set<UUID> alreadySaved = new HashSet<>();
        for (ExerciseLog saved : session.getLogs()) {
            alreadySaved.add(saved.getClientId());
        }

        Set<UUID> seenInRequest = new HashSet<>();
        for (CreateWorkoutSessionRequest.LogItem item : items) {
            if (!seenInRequest.add(item.clientId())) {
                throw new BadRequestException("clientId repetido no envio: " + item.clientId());
            }
            if (alreadySaved.contains(item.clientId())) {
                continue;
            }

            Exercise exercise = exerciseRepository.findById(item.exerciseId())
                    .orElseThrow(() -> new BadRequestException("Exercício não encontrado: " + item.exerciseId()));

            ExerciseLog log = new ExerciseLog();
            log.setClientId(item.clientId());
            log.setSession(session);
            log.setExercise(exercise);
            log.setExerciseOrder(item.exerciseOrder());
            log.setSetNumber(item.setNumber());
            log.setReps(item.reps());
            log.setLoadKg(item.loadKg());
            log.setRpe(item.rpe());
            log.setToFailure(Boolean.TRUE.equals(item.toFailure()));
            log.setNotes(item.notes());
            log.setCompletedAt(item.completedAt());
            session.getLogs().add(log);
        }
    }

    private void checkIsOwnerStudent(User requester, WorkoutSession session) {
        boolean isOwnerStudent = requester.getRole() == Role.STUDENT
                && session.getStudent().getId().equals(requester.getId());
        if (!isOwnerStudent) {
            throw new AccessDeniedException("Você não pode alterar esta sessão");
        }
    }

    private void checkCanView(User requester, WorkoutSession session) {
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        boolean isOwnerStudent = requester.getRole() == Role.STUDENT
                && session.getStudent().getId().equals(requester.getId());
        boolean isOwnerPersonal = requester.getRole() == Role.PERSONAL
                && session.getStudent().getPersonal().getId().equals(requester.getId());

        if (!isAdmin && !isOwnerStudent && !isOwnerPersonal) {
            throw new AccessDeniedException("Acesso negado a esta sessão");
        }
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
    }

    private WorkoutSession findSession(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new WorkoutSessionNotFoundException(id));
    }
}
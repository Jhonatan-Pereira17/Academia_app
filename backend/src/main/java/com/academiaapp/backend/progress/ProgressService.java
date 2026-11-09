package com.academiaapp.backend.progress;

import com.academiaapp.backend.config.BadRequestException;
import com.academiaapp.backend.exercise.ExerciseNotFoundException;
import com.academiaapp.backend.exercise.ExerciseRepository;
import com.academiaapp.backend.student.Student;
import com.academiaapp.backend.student.StudentNotFoundException;
import com.academiaapp.backend.student.StudentRepository;
import com.academiaapp.backend.user.Role;
import com.academiaapp.backend.user.User;
import com.academiaapp.backend.user.UserRepository;
import com.academiaapp.backend.workoutsession.ExerciseLog;
import com.academiaapp.backend.workoutsession.WorkoutSession;
import com.academiaapp.backend.workoutsession.WorkoutSessionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ProgressService {

    private static final int MAX_PAGE_SIZE = 100;

    private final WorkoutSessionRepository sessionRepository;
    private final StudentRepository studentRepository;
    private final ExerciseRepository exerciseRepository;
    private final UserRepository userRepository;

    public ProgressService(WorkoutSessionRepository sessionRepository,
            StudentRepository studentRepository,
            ExerciseRepository exerciseRepository,
            UserRepository userRepository) {
        this.sessionRepository = sessionRepository;
        this.studentRepository = studentRepository;
        this.exerciseRepository = exerciseRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<WorkoutSessionSummaryDTO> listSessions(String requesterEmail, Long studentId, int page, int size) {
        User requester = findUser(requesterEmail);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE));

        List<WorkoutSession> sessions;
        if (studentId != null || requester.getRole() == Role.STUDENT) {
            Student student = resolveStudent(requester, studentId);
            sessions = sessionRepository.findByStudentIdOrderByStartedAtDesc(student.getId(), pageable);
        } else if (requester.getRole() == Role.PERSONAL) {
            sessions = sessionRepository.findByStudentPersonalIdOrderByStartedAtDesc(requester.getId(), pageable);
        } else {
            sessions = sessionRepository.findAllByOrderByStartedAtDesc(pageable);
        }

        return sessions.stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<LastPerformanceDTO> lastPerformance(String requesterEmail, Long exerciseId,
            Long studentId, Long excludeSessionId) {
        User requester = findUser(requesterEmail);
        Student student = resolveStudent(requester, studentId);
        ensureExerciseExists(exerciseId);

        List<WorkoutSession> sessions = sessionRepository.findSessionsWithExercise(student.getId(), exerciseId,
                PageRequest.of(0, 2));

        return sessions.stream()
                .filter(session -> excludeSessionId == null || !session.getId().equals(excludeSessionId))
                .findFirst()
                .map(session -> toLastPerformance(session, exerciseId));
    }

    @Transactional(readOnly = true)
    public List<ExerciseHistoryItemDTO> exerciseHistory(String requesterEmail, Long exerciseId,
            Long studentId, int limit) {
        User requester = findUser(requesterEmail);
        Student student = resolveStudent(requester, studentId);
        ensureExerciseExists(exerciseId);

        Pageable pageable = PageRequest.of(0, Math.min(Math.max(limit, 1), MAX_PAGE_SIZE));
        List<WorkoutSession> sessions = sessionRepository.findSessionsWithExercise(student.getId(), exerciseId,
                pageable);

        List<ExerciseHistoryItemDTO> items = new ArrayList<>(sessions.stream()
                .map(session -> toHistoryItem(session, exerciseId))
                .toList());
        Collections.reverse(items);
        return items;
    }

    private Student resolveStudent(User requester, Long requestedStudentId) {
        if (requester.getRole() == Role.STUDENT) {
            if (requestedStudentId != null && !requestedStudentId.equals(requester.getId())) {
                throw new AccessDeniedException("O aluno só pode consultar o próprio histórico");
            }
            return studentRepository.findById(requester.getId())
                    .orElseThrow(() -> new IllegalStateException("Usuário STUDENT sem perfil de aluno"));
        }

        if (requestedStudentId == null) {
            throw new BadRequestException("Informe o aluno (studentId)");
        }

        Student student = studentRepository.findById(requestedStudentId)
                .orElseThrow(() -> new StudentNotFoundException(requestedStudentId));

        if (requester.getRole() == Role.PERSONAL && !student.getPersonal().getId().equals(requester.getId())) {
            throw new AccessDeniedException("Este aluno não pertence a você");
        }
        return student;
    }

    private void ensureExerciseExists(Long exerciseId) {
        if (!exerciseRepository.existsById(exerciseId)) {
            throw new ExerciseNotFoundException(exerciseId);
        }
    }

    private WorkoutSessionSummaryDTO toSummary(WorkoutSession session) {
        BigDecimal volume = session.getLogs().stream()
                .map(this::volumeOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Long duration = session.getFinishedAt() == null
                ? null
                : Duration.between(session.getStartedAt(), session.getFinishedAt()).toMinutes();

        return new WorkoutSessionSummaryDTO(
                session.getId(),
                session.getStudent().getId(),
                session.getStudent().getName(),
                session.getWorkout() == null ? null : session.getWorkout().getId(),
                session.getWorkoutName(),
                session.getStartedAt(),
                session.getFinishedAt(),
                duration,
                session.getLogs().size(),
                volume);
    }

    private LastPerformanceDTO toLastPerformance(WorkoutSession session, Long exerciseId) {
        List<LastPerformanceDTO.SetItem> sets = logsOf(session, exerciseId).stream()
                .map(log -> new LastPerformanceDTO.SetItem(
                        log.getSetNumber(),
                        log.getReps(),
                        log.getLoadKg(),
                        log.getRpe(),
                        log.isToFailure()))
                .toList();

        return new LastPerformanceDTO(session.getId(), session.getWorkoutName(), session.getStartedAt(), sets);
    }

    private ExerciseHistoryItemDTO toHistoryItem(WorkoutSession session, Long exerciseId) {
        List<ExerciseLog> logs = logsOf(session, exerciseId);

        int totalReps = logs.stream().mapToInt(ExerciseLog::getReps).sum();
        BigDecimal maxLoad = logs.stream()
                .map(ExerciseLog::getLoadKg)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        BigDecimal volume = logs.stream()
                .map(this::volumeOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ExerciseHistoryItemDTO(session.getId(), session.getStartedAt(),
                logs.size(), totalReps, maxLoad, volume);
    }

    private List<ExerciseLog> logsOf(WorkoutSession session, Long exerciseId) {
        return session.getLogs().stream()
                .filter(log -> log.getExercise().getId().equals(exerciseId))
                .toList();
    }

    private BigDecimal volumeOf(ExerciseLog log) {
        return log.getLoadKg().multiply(BigDecimal.valueOf(log.getReps()));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
    }
}
package com.academiaapp.backend.dashboard;

import com.academiaapp.backend.config.BadRequestException;
import com.academiaapp.backend.student.Student;
import com.academiaapp.backend.student.StudentAccessService;
import com.academiaapp.backend.user.User;
import com.academiaapp.backend.user.UserRepository;
import com.academiaapp.backend.workout.Workout;
import com.academiaapp.backend.workout.WorkoutRepository;
import com.academiaapp.backend.workoutsession.ExerciseLog;
import com.academiaapp.backend.workoutsession.WorkoutSession;
import com.academiaapp.backend.workoutsession.WorkoutSessionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final int STREAK_LOOKBACK_DAYS = 730;

    private final WorkoutSessionRepository sessionRepository;
    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final StudentAccessService studentAccessService;

    public DashboardService(WorkoutSessionRepository sessionRepository,
            WorkoutRepository workoutRepository,
            UserRepository userRepository,
            StudentAccessService studentAccessService) {
        this.sessionRepository = sessionRepository;
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
        this.studentAccessService = studentAccessService;
    }

    @Transactional(readOnly = true)
    public StudentDashboardDTO studentDashboard(String requesterEmail, Long studentId, String zone) {
        ZoneId zoneId = parseZone(zone);
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
        Student student = studentAccessService.resolveStudent(requester, studentId);

        Instant now = Instant.now();
        Instant sevenDaysAgo = now.minus(7, ChronoUnit.DAYS);

        List<WorkoutSession> last30 = sessionRepository
                .findByStudentIdAndStartedAtGreaterThanEqualOrderByStartedAtDesc(
                        student.getId(), now.minus(30, ChronoUnit.DAYS));
        List<WorkoutSession> last7 = last30.stream()
                .filter(session -> !session.getStartedAt().isBefore(sevenDaysAgo))
                .toList();

        List<Instant> startTimes = sessionRepository.findStartTimesSince(
                student.getId(), now.minus(STREAK_LOOKBACK_DAYS, ChronoUnit.DAYS));

        LocalDate thisWeek = weekStart(now.atZone(zoneId).toLocalDate());
        Set<LocalDate> weeksWithTraining = startTimes.stream()
                .map(instant -> weekStart(instant.atZone(zoneId).toLocalDate()))
                .collect(Collectors.toSet());
        int sessionsThisWeek = (int) startTimes.stream()
                .filter(instant -> weekStart(instant.atZone(zoneId).toLocalDate()).equals(thisWeek))
                .count();

        return new StudentDashboardDTO(
                student.getId(),
                student.getName(),
                findNextWorkout(student.getId()),
                findLastSession(student.getId()),
                summarize(last7),
                summarize(last30),
                sessionsThisWeek,
                weeklyStreak(weeksWithTraining, thisWeek));
    }

    private StudentDashboardDTO.NextWorkout findNextWorkout(Long studentId) {
        List<Workout> activeWorkouts = workoutRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .filter(Workout::isActive)
                .sorted(Comparator.comparing(Workout::getCreatedAt))
                .toList();
        if (activeWorkouts.isEmpty()) {
            return null;
        }

        Optional<WorkoutSession> lastWithWorkout = sessionRepository
                .findByStudentIdAndWorkoutIsNotNullOrderByStartedAtDesc(studentId, PageRequest.of(0, 1))
                .stream()
                .findFirst();

        int nextIndex = 0;
        if (lastWithWorkout.isPresent()) {
            Long lastWorkoutId = lastWithWorkout.get().getWorkout().getId();
            for (int i = 0; i < activeWorkouts.size(); i++) {
                if (activeWorkouts.get(i).getId().equals(lastWorkoutId)) {
                    nextIndex = (i + 1) % activeWorkouts.size();
                    break;
                }
            }
        }

        Workout next = activeWorkouts.get(nextIndex);
        return new StudentDashboardDTO.NextWorkout(next.getId(), next.getName(), next.getExercises().size());
    }

    private StudentDashboardDTO.LastSession findLastSession(Long studentId) {
        return sessionRepository.findByStudentIdOrderByStartedAtDesc(studentId, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(session -> new StudentDashboardDTO.LastSession(
                        session.getId(),
                        session.getWorkoutName(),
                        session.getStartedAt(),
                        durationMinutes(session),
                        session.getLogs().size(),
                        totalVolume(session.getLogs())))
                .orElse(null);
    }

    private StudentDashboardDTO.PeriodSummary summarize(List<WorkoutSession> sessions) {
        int totalSets = sessions.stream().mapToInt(session -> session.getLogs().size()).sum();
        BigDecimal volume = sessions.stream()
                .map(session -> totalVolume(session.getLogs()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long minutes = sessions.stream()
                .map(this::durationMinutes)
                .filter(duration -> duration != null)
                .mapToLong(Long::longValue)
                .sum();

        return new StudentDashboardDTO.PeriodSummary(sessions.size(), totalSets, volume, minutes);
    }

    private int weeklyStreak(Set<LocalDate> weeksWithTraining, LocalDate thisWeek) {
        LocalDate cursor = weeksWithTraining.contains(thisWeek) ? thisWeek : thisWeek.minusWeeks(1);
        int streak = 0;
        while (weeksWithTraining.contains(cursor)) {
            streak++;
            cursor = cursor.minusWeeks(1);
        }
        return streak;
    }

    private LocalDate weekStart(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private Long durationMinutes(WorkoutSession session) {
        return session.getFinishedAt() == null
                ? null
                : Duration.between(session.getStartedAt(), session.getFinishedAt()).toMinutes();
    }

    private BigDecimal totalVolume(List<ExerciseLog> logs) {
        return logs.stream()
                .map(log -> log.getLoadKg().multiply(BigDecimal.valueOf(log.getReps())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ZoneId parseZone(String zone) {
        try {
            return ZoneId.of(zone);
        } catch (DateTimeException ex) {
            throw new BadRequestException("Fuso horário inválido: " + zone);
        }
    }
}
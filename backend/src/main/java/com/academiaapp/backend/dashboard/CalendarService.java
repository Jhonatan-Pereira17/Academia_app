package com.academiaapp.backend.dashboard;

import com.academiaapp.backend.config.BadRequestException;
import com.academiaapp.backend.student.Student;
import com.academiaapp.backend.student.StudentAccessService;
import com.academiaapp.backend.user.User;
import com.academiaapp.backend.user.UserRepository;
import com.academiaapp.backend.workoutsession.WorkoutSession;
import com.academiaapp.backend.workoutsession.WorkoutSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class CalendarService {

    private final WorkoutSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final StudentAccessService studentAccessService;

    public CalendarService(WorkoutSessionRepository sessionRepository,
            UserRepository userRepository,
            StudentAccessService studentAccessService) {
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.studentAccessService = studentAccessService;
    }

    @Transactional(readOnly = true)
    public StudentCalendarDTO studentCalendar(String requesterEmail, Long studentId, String month, String zone) {
        ZoneId zoneId = parseZone(zone);
        YearMonth yearMonth = parseMonth(month, zoneId);

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
        Student student = studentAccessService.resolveStudent(requester, studentId);

        Instant from = yearMonth.atDay(1).atStartOfDay(zoneId).toInstant();
        Instant to = yearMonth.plusMonths(1).atDay(1).atStartOfDay(zoneId).toInstant();

        List<WorkoutSession> sessions = sessionRepository
                .findByStudentIdAndStartedAtGreaterThanEqualAndStartedAtLessThanOrderByStartedAtAsc(
                        student.getId(), from, to);

        Map<LocalDate, List<WorkoutSession>> sessionsByDay = sessions.stream()
                .collect(Collectors.groupingBy(
                        session -> session.getStartedAt().atZone(zoneId).toLocalDate(),
                        TreeMap::new,
                        Collectors.toList()));

        List<StudentCalendarDTO.TrainedDay> trainedDays = sessionsByDay.entrySet().stream()
                .map(entry -> new StudentCalendarDTO.TrainedDay(
                        entry.getKey(),
                        entry.getValue().stream().map(this::toItem).toList()))
                .toList();

        return new StudentCalendarDTO(
                yearMonth.toString(),
                yearMonth.lengthOfMonth(),
                trainedDays.size(),
                sessions.size(),
                trainedDays);
    }

    private StudentCalendarDTO.SessionItem toItem(WorkoutSession session) {
        Long duration = session.getFinishedAt() == null
                ? null
                : Duration.between(session.getStartedAt(), session.getFinishedAt()).toMinutes();

        return new StudentCalendarDTO.SessionItem(
                session.getId(),
                session.getWorkoutName(),
                session.getStartedAt(),
                duration);
    }

    private YearMonth parseMonth(String month, ZoneId zoneId) {
        if (month == null) {
            return YearMonth.now(zoneId);
        }
        try {
            return YearMonth.parse(month);
        } catch (DateTimeException ex) {
            throw new BadRequestException("Mês inválido (use o formato AAAA-MM): " + month);
        }
    }

    private ZoneId parseZone(String zone) {
        try {
            return ZoneId.of(zone);
        } catch (DateTimeException ex) {
            throw new BadRequestException("Fuso horário inválido: " + zone);
        }
    }
}
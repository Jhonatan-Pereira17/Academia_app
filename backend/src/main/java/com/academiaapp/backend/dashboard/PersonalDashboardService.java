package com.academiaapp.backend.dashboard;

import com.academiaapp.backend.personal.Personal;
import com.academiaapp.backend.personal.PersonalRepository;
import com.academiaapp.backend.student.Student;
import com.academiaapp.backend.student.StudentRepository;
import com.academiaapp.backend.user.User;
import com.academiaapp.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PersonalDashboardService {

    private static final int MAX_INACTIVE_DAYS = 90;

    private final DashboardStatsRepository stats;
    private final PersonalRepository personalRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public PersonalDashboardService(DashboardStatsRepository stats,
            PersonalRepository personalRepository,
            StudentRepository studentRepository,
            UserRepository userRepository) {
        this.stats = stats;
        this.personalRepository = personalRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PersonalDashboardDTO personalDashboard(String requesterEmail, int inactiveDays) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
        Personal personal = personalRepository.findById(requester.getId())
                .orElseThrow(() -> new IllegalStateException("Usuário PERSONAL sem perfil de personal"));

        int threshold = Math.min(Math.max(inactiveDays, 1), MAX_INACTIVE_DAYS);
        Instant now = Instant.now();
        Instant inactiveLimit = now.minus(threshold, ChronoUnit.DAYS);

        List<Student> students = studentRepository.findByPersonalId(personal.getId());
        Map<Long, Instant> lastSessionByStudent = stats.findLastSessionPerStudentOfPersonal(personal.getId())
                .stream()
                .collect(Collectors.toMap(StudentLastSession::studentId, StudentLastSession::lastStartedAt));

        List<PersonalDashboardDTO.InactiveStudent> inactiveStudents = students.stream()
                .filter(student -> {
                    Instant last = lastSessionByStudent.get(student.getId());
                    return last == null || last.isBefore(inactiveLimit);
                })
                .map(student -> toInactiveStudent(student, lastSessionByStudent.get(student.getId()), now))
                .sorted(Comparator.comparing(
                        PersonalDashboardDTO.InactiveStudent::lastSessionAt,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .toList();

        return new PersonalDashboardDTO(
                personal.getId(),
                personal.getName(),
                students.size(),
                stats.countActiveWorkoutsOfPersonal(personal.getId()),
                activity(personal.getId(), now.minus(7, ChronoUnit.DAYS)),
                activity(personal.getId(), now.minus(30, ChronoUnit.DAYS)),
                threshold,
                inactiveStudents);
    }

    private PeriodActivityDTO activity(Long personalId, Instant since) {
        return new PeriodActivityDTO(
                stats.countSessionsOfPersonalSince(personalId, since),
                stats.countStudentsTrainedOfPersonalSince(personalId, since));
    }

    private PersonalDashboardDTO.InactiveStudent toInactiveStudent(Student student, Instant lastSession, Instant now) {
        Long daysSince = lastSession == null ? null : Duration.between(lastSession, now).toDays();
        return new PersonalDashboardDTO.InactiveStudent(student.getId(), student.getName(), lastSession, daysSince);
    }
}
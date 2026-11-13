package com.academiaapp.backend.dashboard;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CalendarService calendarService;
    private final PersonalDashboardService personalDashboardService;
    private final AdminDashboardService adminDashboardService;

    public DashboardController(DashboardService dashboardService,
            CalendarService calendarService,
            PersonalDashboardService personalDashboardService,
            AdminDashboardService adminDashboardService) {
        this.dashboardService = dashboardService;
        this.calendarService = calendarService;
        this.personalDashboardService = personalDashboardService;
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/student")
    public StudentDashboardDTO student(Authentication authentication,
            @RequestParam(name = "studentId", required = false) Long studentId,
            @RequestParam(name = "zone", defaultValue = "America/Sao_Paulo") String zone) {
        return dashboardService.studentDashboard(authentication.getName(), studentId, zone);
    }

    @GetMapping("/student/calendar")
    public StudentCalendarDTO calendar(Authentication authentication,
            @RequestParam(name = "studentId", required = false) Long studentId,
            @RequestParam(name = "month", required = false) String month,
            @RequestParam(name = "zone", defaultValue = "America/Sao_Paulo") String zone) {
        return calendarService.studentCalendar(authentication.getName(), studentId, month, zone);
    }

    @GetMapping("/personal")
    @PreAuthorize("hasRole('PERSONAL')")
    public PersonalDashboardDTO personal(Authentication authentication,
            @RequestParam(name = "inactiveDays", defaultValue = "7") int inactiveDays) {
        return personalDashboardService.personalDashboard(authentication.getName(), inactiveDays);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDashboardDTO admin() {
        return adminDashboardService.adminDashboard();
    }
}
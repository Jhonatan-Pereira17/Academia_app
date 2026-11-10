package com.academiaapp.backend.dashboard;

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

    public DashboardController(DashboardService dashboardService, CalendarService calendarService) {
        this.dashboardService = dashboardService;
        this.calendarService = calendarService;
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
}
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

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/student")
    public StudentDashboardDTO student(Authentication authentication,
            @RequestParam(name = "studentId", required = false) Long studentId,
            @RequestParam(name = "zone", defaultValue = "America/Sao_Paulo") String zone) {
        return dashboardService.studentDashboard(authentication.getName(), studentId, zone);
    }
}
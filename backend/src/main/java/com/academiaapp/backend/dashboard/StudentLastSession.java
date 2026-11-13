package com.academiaapp.backend.dashboard;

import java.time.Instant;

public record StudentLastSession(Long studentId, Instant lastStartedAt) {
}
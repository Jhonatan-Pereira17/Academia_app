package com.academiaapp.backend.evaluation;

import com.academiaapp.backend.config.BadRequestException;
import com.academiaapp.backend.personal.Personal;
import com.academiaapp.backend.personal.PersonalRepository;
import com.academiaapp.backend.student.Student;
import com.academiaapp.backend.student.StudentAccessService;
import com.academiaapp.backend.user.Role;
import com.academiaapp.backend.user.User;
import com.academiaapp.backend.user.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class PhysicalEvaluationService {

    private final PhysicalEvaluationRepository evaluationRepository;
    private final PersonalRepository personalRepository;
    private final UserRepository userRepository;
    private final StudentAccessService studentAccessService;

    public PhysicalEvaluationService(PhysicalEvaluationRepository evaluationRepository,
            PersonalRepository personalRepository,
            UserRepository userRepository,
            StudentAccessService studentAccessService) {
        this.evaluationRepository = evaluationRepository;
        this.personalRepository = personalRepository;
        this.userRepository = userRepository;
        this.studentAccessService = studentAccessService;
    }

    @Transactional
    public PhysicalEvaluationResponseDTO create(String requesterEmail, CreatePhysicalEvaluationRequest request) {
        User requester = findUser(requesterEmail);
        Student student = studentAccessService.resolveStudent(requester, request.studentId());
        Personal personal = personalRepository.findById(requester.getId())
                .orElseThrow(() -> new IllegalStateException("Usuário PERSONAL sem perfil de personal"));

        if (!hasAnyMeasurement(request)) {
            throw new BadRequestException("Informe pelo menos uma medida");
        }

        PhysicalEvaluation evaluation = new PhysicalEvaluation();
        evaluation.setStudent(student);
        evaluation.setPersonal(personal);
        apply(evaluation, request);

        evaluationRepository.save(evaluation);
        return PhysicalEvaluationResponseDTO.fromEntity(evaluation);
    }

    @Transactional(readOnly = true)
    public List<PhysicalEvaluationResponseDTO> list(String requesterEmail, Long studentId) {
        User requester = findUser(requesterEmail);
        Student student = studentAccessService.resolveStudent(requester, studentId);

        return evaluationRepository.findByStudentIdOrderByEvaluationDateDesc(student.getId()).stream()
                .map(PhysicalEvaluationResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public PhysicalEvaluationResponseDTO findById(String requesterEmail, Long id) {
        User requester = findUser(requesterEmail);
        PhysicalEvaluation evaluation = findEvaluation(id);
        checkCanView(requester, evaluation);

        return PhysicalEvaluationResponseDTO.fromEntity(evaluation);
    }

    @Transactional
    public PhysicalEvaluationResponseDTO update(String requesterEmail, Long id,
            CreatePhysicalEvaluationRequest request) {
        User requester = findUser(requesterEmail);
        PhysicalEvaluation evaluation = findEvaluation(id);
        checkCanModify(requester, evaluation);

        if (!evaluation.getStudent().getId().equals(request.studentId())) {
            throw new BadRequestException("Não é possível trocar o aluno de uma avaliação");
        }
        if (!hasAnyMeasurement(request)) {
            throw new BadRequestException("Informe pelo menos uma medida");
        }

        apply(evaluation, request);
        evaluationRepository.saveAndFlush(evaluation);
        return PhysicalEvaluationResponseDTO.fromEntity(evaluation);
    }

    @Transactional
    public void delete(String requesterEmail, Long id) {
        User requester = findUser(requesterEmail);
        PhysicalEvaluation evaluation = findEvaluation(id);
        checkCanModify(requester, evaluation);

        evaluationRepository.delete(evaluation);
    }

    private void apply(PhysicalEvaluation evaluation, CreatePhysicalEvaluationRequest request) {
        evaluation.setEvaluationDate(request.evaluationDate());
        evaluation.setWeightKg(request.weightKg());
        evaluation.setHeightCm(request.heightCm());
        evaluation.setBodyFatPercentage(request.bodyFatPercentage());
        evaluation.setMuscleMassKg(request.muscleMassKg());
        evaluation.setArmCm(request.armCm());
        evaluation.setChestCm(request.chestCm());
        evaluation.setWaistCm(request.waistCm());
        evaluation.setHipCm(request.hipCm());
        evaluation.setThighCm(request.thighCm());
        evaluation.setCalfCm(request.calfCm());
        evaluation.setSkinfoldTricepsMm(request.skinfoldTricepsMm());
        evaluation.setSkinfoldSubscapularMm(request.skinfoldSubscapularMm());
        evaluation.setSkinfoldChestMm(request.skinfoldChestMm());
        evaluation.setSkinfoldAxillaryMm(request.skinfoldAxillaryMm());
        evaluation.setSkinfoldSuprailiacMm(request.skinfoldSuprailiacMm());
        evaluation.setSkinfoldAbdominalMm(request.skinfoldAbdominalMm());
        evaluation.setSkinfoldThighMm(request.skinfoldThighMm());
        evaluation.setNotes(request.notes());
    }

    private boolean hasAnyMeasurement(CreatePhysicalEvaluationRequest r) {
        return Stream.<BigDecimal>of(
                r.weightKg(), r.heightCm(), r.bodyFatPercentage(), r.muscleMassKg(),
                r.armCm(), r.chestCm(), r.waistCm(), r.hipCm(), r.thighCm(), r.calfCm(),
                r.skinfoldTricepsMm(), r.skinfoldSubscapularMm(), r.skinfoldChestMm(),
                r.skinfoldAxillaryMm(), r.skinfoldSuprailiacMm(), r.skinfoldAbdominalMm(),
                r.skinfoldThighMm())
                .anyMatch(Objects::nonNull);
    }

    private void checkCanView(User requester, PhysicalEvaluation evaluation) {
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        boolean isOwnerStudent = requester.getRole() == Role.STUDENT
                && evaluation.getStudent().getId().equals(requester.getId());
        boolean isOwnerPersonal = requester.getRole() == Role.PERSONAL
                && evaluation.getStudent().getPersonal().getId().equals(requester.getId());

        if (!isAdmin && !isOwnerStudent && !isOwnerPersonal) {
            throw new AccessDeniedException("Acesso negado a esta avaliação");
        }
    }

    private void checkCanModify(User requester, PhysicalEvaluation evaluation) {
        boolean isOwnerPersonal = requester.getRole() == Role.PERSONAL
                && evaluation.getStudent().getPersonal().getId().equals(requester.getId());

        if (!isOwnerPersonal) {
            throw new AccessDeniedException("Você não pode alterar esta avaliação");
        }
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
    }

    private PhysicalEvaluation findEvaluation(Long id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() -> new PhysicalEvaluationNotFoundException(id));
    }
}
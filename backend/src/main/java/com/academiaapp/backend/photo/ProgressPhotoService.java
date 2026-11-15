package com.academiaapp.backend.photo;

import com.academiaapp.backend.config.BadRequestException;
import com.academiaapp.backend.evaluation.PhysicalEvaluation;
import com.academiaapp.backend.evaluation.PhysicalEvaluationRepository;
import com.academiaapp.backend.student.Student;
import com.academiaapp.backend.student.StudentRepository;
import com.academiaapp.backend.user.User;
import com.academiaapp.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class ProgressPhotoService {

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final int MAX_NOTES_LENGTH = 500;

    private final ProgressPhotoRepository photoRepository;
    private final StudentRepository studentRepository;
    private final PhysicalEvaluationRepository evaluationRepository;
    private final UserRepository userRepository;
    private final PhotoStorage photoStorage;
    private final ImageSanitizer imageSanitizer;

    public ProgressPhotoService(ProgressPhotoRepository photoRepository,
            StudentRepository studentRepository,
            PhysicalEvaluationRepository evaluationRepository,
            UserRepository userRepository,
            PhotoStorage photoStorage,
            ImageSanitizer imageSanitizer) {
        this.photoRepository = photoRepository;
        this.studentRepository = studentRepository;
        this.evaluationRepository = evaluationRepository;
        this.userRepository = userRepository;
        this.photoStorage = photoStorage;
        this.imageSanitizer = imageSanitizer;
    }

    @Transactional
    public ProgressPhotoResponseDTO upload(String requesterEmail, MultipartFile file, LocalDate takenOn,
            PhotoAngle angle, String notes, Long evaluationId) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
        Student student = studentRepository.findById(requester.getId())
                .orElseThrow(() -> new IllegalStateException("Usuário STUDENT sem perfil de aluno"));

        if (file.isEmpty()) {
            throw new BadRequestException("Envie um arquivo de imagem");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new BadRequestException("A foto deve ter no máximo 5 MB");
        }
        if (takenOn.isAfter(LocalDate.now())) {
            throw new BadRequestException("A data da foto não pode estar no futuro");
        }
        if (notes != null && notes.length() > MAX_NOTES_LENGTH) {
            throw new BadRequestException("A observação deve ter no máximo 500 caracteres");
        }

        PhysicalEvaluation evaluation = null;
        if (evaluationId != null) {
            evaluation = evaluationRepository.findById(evaluationId)
                    .filter(found -> found.getStudent().getId().equals(student.getId()))
                    .orElseThrow(() -> new BadRequestException("Avaliação inválida: " + evaluationId));
        }

        byte[] original;
        try {
            original = file.getBytes();
        } catch (IOException ex) {
            throw new BadRequestException("Não foi possível ler o arquivo enviado");
        }
        ImageSanitizer.SanitizedImage image = imageSanitizer.sanitize(original);

        ProgressPhoto photo = new ProgressPhoto();
        photo.setStudent(student);
        photo.setEvaluation(evaluation);
        photo.setTakenOn(takenOn);
        photo.setAngle(angle);
        photo.setNotes(notes);
        photo.setStorageKey(UUID.randomUUID().toString());
        photo.setContentType(image.contentType());
        photo.setSizeBytes((long) image.bytes().length);

        photoRepository.save(photo);
        photoStorage.save(photo.getStorageKey(), image.bytes());

        return ProgressPhotoResponseDTO.fromEntity(photo);
    }
}
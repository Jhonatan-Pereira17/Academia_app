package com.academiaapp.backend.photo;

import com.academiaapp.backend.config.BadRequestException;
import com.academiaapp.backend.evaluation.PhysicalEvaluation;
import com.academiaapp.backend.evaluation.PhysicalEvaluationRepository;
import com.academiaapp.backend.student.Student;
import com.academiaapp.backend.student.StudentNotFoundException;
import com.academiaapp.backend.student.StudentRepository;
import com.academiaapp.backend.user.Role;
import com.academiaapp.backend.user.User;
import com.academiaapp.backend.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ProgressPhotoService {

    private static final Logger log = LoggerFactory.getLogger(ProgressPhotoService.class);

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final int MAX_NOTES_LENGTH = 500;

    public record PhotoFile(byte[] content, String contentType) {
    }

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
        User requester = findUser(requesterEmail);
        denyPersonal(requester);
        if (requester.getRole() != Role.STUDENT) {
            throw new AccessDeniedException("Somente o aluno pode enviar fotos de evolução");
        }
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

    @Transactional(readOnly = true)
    public List<ProgressPhotoResponseDTO> list(String requesterEmail, Long studentId) {
        User requester = findUser(requesterEmail);
        denyPersonal(requester);

        Long targetStudentId;
        if (requester.getRole() == Role.STUDENT) {
            if (studentId != null && !studentId.equals(requester.getId())) {
                throw new AccessDeniedException("O aluno só pode ver as próprias fotos");
            }
            targetStudentId = requester.getId();
        } else {
            if (studentId == null) {
                throw new BadRequestException("Informe o aluno (studentId)");
            }
            targetStudentId = studentRepository.findById(studentId)
                    .orElseThrow(() -> new StudentNotFoundException(studentId))
                    .getId();
        }

        return photoRepository.findByStudentIdOrderByTakenOnDescIdDesc(targetStudentId).stream()
                .map(ProgressPhotoResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProgressPhotoResponseDTO findById(String requesterEmail, Long id) {
        User requester = findUser(requesterEmail);
        return ProgressPhotoResponseDTO.fromEntity(findViewable(requester, id));
    }

    @Transactional(readOnly = true)
    public PhotoFile loadFile(String requesterEmail, Long id) {
        User requester = findUser(requesterEmail);
        ProgressPhoto photo = findViewable(requester, id);

        byte[] content = photoStorage.load(photo.getStorageKey());
        return new PhotoFile(content, photo.getContentType());
    }

    @Transactional
    public void delete(String requesterEmail, Long id) {
        User requester = findUser(requesterEmail);
        denyPersonal(requester);
        if (requester.getRole() != Role.STUDENT) {
            throw new AccessDeniedException("Somente o aluno pode apagar as próprias fotos");
        }

        ProgressPhoto photo = findViewable(requester, id);
        String storageKey = photo.getStorageKey();
        photoRepository.delete(photo);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    photoStorage.delete(storageKey);
                } catch (RuntimeException ex) {
                    log.error("A foto {} foi apagada do banco, mas o arquivo não pôde ser removido", id);
                }
            }
        });
    }

    private ProgressPhoto findViewable(User requester, Long id) {
        denyPersonal(requester);

        ProgressPhoto photo = photoRepository.findById(id)
                .orElseThrow(() -> new PhotoNotFoundException(id));

        boolean isOtherStudent = requester.getRole() == Role.STUDENT
                && !photo.getStudent().getId().equals(requester.getId());
        if (isOtherStudent) {
            throw new PhotoNotFoundException(id);
        }
        return photo;
    }

    private void denyPersonal(User requester) {
        if (requester.getRole() == Role.PERSONAL) {
            throw new AccessDeniedException("Acesso negado às fotos de evolução");
        }
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuário autenticado não encontrado"));
    }
}
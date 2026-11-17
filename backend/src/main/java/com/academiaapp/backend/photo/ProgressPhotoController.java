package com.academiaapp.backend.photo;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/progress-photos")
public class ProgressPhotoController {

    private final ProgressPhotoService photoService;

    public ProgressPhotoController(ProgressPhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ProgressPhotoResponseDTO> upload(Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam("takenOn") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate takenOn,
            @RequestParam(name = "angle", required = false) PhotoAngle angle,
            @RequestParam(name = "notes", required = false) String notes,
            @RequestParam(name = "evaluationId", required = false) Long evaluationId) {
        ProgressPhotoResponseDTO created = photoService.upload(authentication.getName(), file, takenOn, angle, notes,
                evaluationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public List<ProgressPhotoResponseDTO> list(Authentication authentication,
            @RequestParam(name = "studentId", required = false) Long studentId) {
        return photoService.list(authentication.getName(), studentId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ProgressPhotoResponseDTO findById(Authentication authentication, @PathVariable("id") Long id) {
        return photoService.findById(authentication.getName(), id);
    }

    @GetMapping("/{id}/file")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<byte[]> file(Authentication authentication, @PathVariable("id") Long id) {
        ProgressPhotoService.PhotoFile photo = photoService.loadFile(authentication.getName(), id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.contentType()))
                .contentLength(photo.content().length)
                .cacheControl(CacheControl.noStore())
                .header("X-Content-Type-Options", "nosniff")
                .body(photo.content());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable("id") Long id) {
        photoService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
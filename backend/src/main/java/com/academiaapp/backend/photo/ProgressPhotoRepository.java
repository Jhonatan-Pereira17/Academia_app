package com.academiaapp.backend.photo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgressPhotoRepository extends JpaRepository<ProgressPhoto, Long> {

    List<ProgressPhoto> findByStudentIdOrderByTakenOnDescIdDesc(Long studentId);
}
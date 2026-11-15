package com.academiaapp.backend.photo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Component
public class LocalDiskPhotoStorage implements PhotoStorage {

    private final Path baseDir;

    public LocalDiskPhotoStorage(@Value("${app.photos.storage-dir}") String storageDir) {
        this.baseDir = Path.of(storageDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(baseDir);
        } catch (IOException ex) {
            throw new UncheckedIOException("Não foi possível criar a pasta de fotos: " + baseDir, ex);
        }
    }

    @Override
    public void save(String key, byte[] content) {
        try {
            Files.write(resolve(key), content, StandardOpenOption.CREATE_NEW);
        } catch (IOException ex) {
            throw new UncheckedIOException("Falha ao gravar a foto", ex);
        }
    }

    @Override
    public byte[] load(String key) {
        try {
            return Files.readAllBytes(resolve(key));
        } catch (IOException ex) {
            throw new UncheckedIOException("Falha ao ler a foto", ex);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException ex) {
            throw new UncheckedIOException("Falha ao apagar a foto", ex);
        }
    }

    private Path resolve(String key) {
        Path target = baseDir.resolve(key).normalize();
        if (!target.startsWith(baseDir)) {
            throw new IllegalArgumentException("Chave de arquivo inválida");
        }
        return target;
    }
}
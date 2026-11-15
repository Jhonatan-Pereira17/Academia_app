package com.academiaapp.backend.photo;

public interface PhotoStorage {

    void save(String key, byte[] content);

    byte[] load(String key);

    void delete(String key);
}
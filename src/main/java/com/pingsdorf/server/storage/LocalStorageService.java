package com.pingsdorf.server.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/** Stores uploaded files under {@code pingsdorf.storage.local.base-dir}. */
@Service
public class LocalStorageService implements StorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path baseDir;
    private final String publicBaseUrl;

    public LocalStorageService(StorageProperties props) {
        this.baseDir = Paths.get(props.local().baseDir()).toAbsolutePath().normalize();
        this.publicBaseUrl = trimTrailingSlash(props.publicBaseUrl());
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create storage dir " + baseDir, e);
        }
    }

    @Override
    public StoredFile store(String folder, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Empty upload");
        }
        var ext = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new ResponseStatusException(BAD_REQUEST, "Unsupported image type: " + ext);
        }
        var safeFolder = sanitizeFolder(folder);
        var filename = UUID.randomUUID() + "." + ext;
        var key = safeFolder + "/" + filename;
        var target = baseDir.resolve(safeFolder).resolve(filename).normalize();
        if (!target.startsWith(baseDir)) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid path");
        }
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Failed to store file", e);
        }
        return new StoredFile(publicBaseUrl + "/" + key, key);
    }

    /** Resolve a stored key back to its on-disk path; used by the file controller. */
    public Path resolve(String key) {
        var path = baseDir.resolve(key).normalize();
        if (!path.startsWith(baseDir)) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid path");
        }
        return path;
    }

    private static String extensionOf(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        var dot = filename.lastIndexOf('.');
        return dot < 0 ? "" : filename.substring(dot + 1).toLowerCase();
    }

    private static String sanitizeFolder(String folder) {
        if (!StringUtils.hasText(folder)) {
            return "misc";
        }
        return folder.replaceAll("[^a-zA-Z0-9_-]", "");
    }

    private static String trimTrailingSlash(String s) {
        if (s == null) return "";
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }
}

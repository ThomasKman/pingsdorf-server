package com.pingsdorf.server.storage;

import java.io.IOException;
import java.nio.file.Files;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/** Serves files stored by {@link LocalStorageService}. */
@RestController
@RequestMapping("/files")
public class FileController {

    private final LocalStorageService storage;

    public FileController(LocalStorageService storage) {
        this.storage = storage;
    }

    @GetMapping("/{folder}/{filename:.+}")
    public ResponseEntity<Resource> get(@PathVariable String folder, @PathVariable String filename) throws IOException {
        var path = storage.resolve(folder + "/" + filename);
        if (!Files.isRegularFile(path)) {
            throw new ResponseStatusException(NOT_FOUND, "File not found");
        }
        var contentType = Files.probeContentType(path);
        var mediaType = contentType == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(contentType);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(new FileSystemResource(path));
    }
}

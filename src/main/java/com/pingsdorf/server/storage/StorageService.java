package com.pingsdorf.server.storage;

import org.springframework.web.multipart.MultipartFile;

/** Abstraction over image storage so we can swap local-disk for S3/MinIO later. */
public interface StorageService {

    /**
     * Persist a file and return a publicly accessible URL.
     *
     * @param folder logical folder, e.g. {@code "pings"} or {@code "floor-plans"}
     * @param file   uploaded multipart
     */
    StoredFile store(String folder, MultipartFile file);

    record StoredFile(String url, String key) {}
}

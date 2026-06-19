package com.pingsdorf.server.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param local           local-filesystem-specific configuration
 * @param publicBaseUrl   prefix used to build public URLs for stored files
 */
@ConfigurationProperties(prefix = "pingsdorf.storage")
public record StorageProperties(Local local, String publicBaseUrl) {

    public record Local(String baseDir) {}
}

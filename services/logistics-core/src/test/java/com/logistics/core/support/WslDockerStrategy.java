package com.logistics.core.support;

import org.testcontainers.dockerclient.DockerClientProviderStrategy;
import org.testcontainers.dockerclient.InvalidConfigurationException;
import org.testcontainers.dockerclient.TransportConfig;

import java.net.URI;

/**
 * Testcontainers strategy for Docker Desktop 29.x on Windows.
 *
 * Docker Desktop 29.x requires Docker API >= 1.44 but docker-java defaults to 1.32,
 * causing all auto-detected connections to fail. This strategy explicitly targets the
 * dockerDesktopLinuxEngine named pipe with the required API version.
 */
public class WslDockerStrategy extends DockerClientProviderStrategy {

    private static final String NPIPE_HOST = "npipe:////./pipe/dockerDesktopLinuxEngine";
    private static final String REQUIRED_API = "1.44";

    @Override
    public TransportConfig getTransportConfig() throws InvalidConfigurationException {
        return TransportConfig.builder()
                .dockerHost(URI.create(NPIPE_HOST))
                .build();
    }

    @Override
    public String getDescription() {
        return "Docker Desktop named pipe (API " + REQUIRED_API + "+)";
    }

    @Override
    public int getPriority() {
        return 200;
    }
}

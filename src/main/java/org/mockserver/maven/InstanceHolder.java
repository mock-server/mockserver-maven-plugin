package org.mockserver.maven;

import com.google.common.base.Strings;
import org.mockserver.client.MockServerClient;
import org.mockserver.configuration.ConfigurationProperties;
import org.mockserver.initialize.ExpectationInitializer;
import org.mockserver.mockserver.MockServer;
import org.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author jamesdbloom
 */
public class InstanceHolder extends ObjectWithReflectiveEqualsHashCodeToString {

    private MockServer mockServer;

    public static void runInitializationClass(Integer[] mockServerPorts, ExpectationInitializer expectationInitializer) {
        if (mockServerPorts != null && mockServerPorts.length > 0 && expectationInitializer != null) {
            expectationInitializer.initializeExpectations(
                    new MockServerClient("127.0.0.1", mockServerPorts[0])
            );
        }
    }

    public void start(final Integer[] mockServerPorts,
                      final Integer proxyRemotePort,
                      String proxyRemoteHost,
                      final String logLevel,
                      ExpectationInitializer expectationInitializer) {
        if (mockServer == null || !mockServer.isRunning()) {
            if (logLevel != null) {
                ConfigurationProperties.logLevel(logLevel);
            }
            if (mockServerPorts != null && mockServerPorts.length > 0) {
                if (proxyRemotePort != null && proxyRemotePort != -1) {
                    if (Strings.isNullOrEmpty(proxyRemoteHost)) {
                        proxyRemoteHost = "localhost";
                    }
                    mockServer = new MockServer(proxyRemotePort, proxyRemoteHost, mockServerPorts);
                } else {
                    mockServer = new MockServer(mockServerPorts);
                }
                ConfigurationProperties.mockServerPort(mockServer.getLocalPort());
            }
            runInitializationClass(mockServerPorts, expectationInitializer);
        } else {
            throw new IllegalStateException("MockServer is already running!");
        }
    }

    public void stop(final Integer[] mockServerPorts, boolean ignoreFailure) {
        if (mockServerPorts != null && mockServerPorts.length > 0) {
            new MockServerClient("127.0.0.1", mockServerPorts[0]).stop(ignoreFailure);
        }
    }

    public void stop() {
        if (mockServer != null && mockServer.isRunning()) {
            mockServer.stop();

            try {
                // ensure that shutdown has actually completed and won't
                // cause class loader error if JVM starts unloading classes
                SECONDS.sleep(3);
            } catch (InterruptedException ignore) {
                // ignore
            }
        }
    }
}

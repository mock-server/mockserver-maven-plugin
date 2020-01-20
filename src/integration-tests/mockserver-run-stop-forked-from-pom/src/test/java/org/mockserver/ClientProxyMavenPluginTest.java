package org.mockserver;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockserver.client.MockServerClient;
import org.mockserver.echo.http.EchoServer;
import org.mockserver.testing.integration.proxy.AbstractClientProxyIntegrationTest;

/**
 * @author jamesdbloom
 */
public class ClientProxyMavenPluginTest extends AbstractClientProxyIntegrationTest {

    private final static int SERVER_HTTP_PORT = 3086;
    private static EchoServer echoServer;
    private static MockServerClient mockServerClient;

    @BeforeClass
    public static void startServer() {
        echoServer = new EchoServer(false);
        mockServerClient = new MockServerClient("localhost", SERVER_HTTP_PORT, servletContext);
    }

    @AfterClass
    public static void stopServer() {
        if (echoServer != null) {
            echoServer.stop();
        }
    }

    @Before
    public void resetProxy() {
        mockServerClient.reset();
    }

    @Override
    public int getProxyPort() {
        return SERVER_HTTP_PORT;
    }

    @Override
    public int getSecureProxyPort() {
        return getProxyPort();
    }

    @Override
    public MockServerClient getMockServerClient() {
        return mockServerClient;
    }

    @Override
    public int getServerPort() {
        return echoServer.getPort();
    }

}

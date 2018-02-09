package org.mockserver;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockserver.client.MockServerClient;
import org.mockserver.echo.http.EchoServer;
import org.mockserver.integration.server.AbstractBasicMockingIntegrationTest;

/**
 * @author jamesdbloom
 */
public class ClientServerMavenPluginTestPort1086 extends AbstractBasicMockingIntegrationTest {

    private final static int SERVER_HTTP_PORT = 1086;
    private static EchoServer echoServer;

    @BeforeClass
    public static void createClient() throws Exception {
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
    public void clearServer() {
        mockServerClient.reset();
    }

    @Override
    public int getServerPort() {
        return SERVER_HTTP_PORT;
    }

    @Override
    public int getEchoServerPort() {
        return echoServer.getPort();
    }

}

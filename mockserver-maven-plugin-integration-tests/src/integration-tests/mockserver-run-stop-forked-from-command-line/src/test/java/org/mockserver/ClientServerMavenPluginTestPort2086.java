package org.mockserver;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.echo.http.EchoServer;
import org.mockserver.integration.server.AbstractBasicClientServerIntegrationTest;
import org.mockserver.socket.PortFactory;

/**
 * @author jamesdbloom
 */
public class ClientServerMavenPluginTestPort2086 extends AbstractBasicClientServerIntegrationTest {

    private final static int SERVER_HTTP_PORT = 1086;
    private static EchoServer echoServer;

    @BeforeClass
    public static void createClient() throws Exception {
        echoServer = new EchoServer(false);
        mockServerClient = new MockServerClient("localhost", SERVER_HTTP_PORT, servletContext);
    }

    @AfterClass
    public static void stopServer() throws Exception {
        echoServer.stop();
    }

    @Before
    public void clearServer() {
        mockServerClient.reset();
    }

    @Override
    public int getMockServerPort() {
        return SERVER_HTTP_PORT;
    }

    @Override
    public int getMockServerSecurePort() {
        return SERVER_HTTP_PORT;
    }

    @Override
    public int getTestServerPort() {
        return echoServer.getPort();
    }

}

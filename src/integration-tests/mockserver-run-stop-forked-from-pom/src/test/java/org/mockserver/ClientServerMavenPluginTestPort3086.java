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
public class ClientServerMavenPluginTestPort3086 extends AbstractBasicMockingIntegrationTest {

    private final static int SERVER_HTTP_PORT = 3086;

    @BeforeClass
    public static void createClient() throws Exception {
        mockServerClient = new MockServerClient("localhost", SERVER_HTTP_PORT, servletContext);
    }

    @Before
    public void clearServer() {
        mockServerClient.reset();
    }

    @Override
    public int getServerPort() {
        return SERVER_HTTP_PORT;
    }

}

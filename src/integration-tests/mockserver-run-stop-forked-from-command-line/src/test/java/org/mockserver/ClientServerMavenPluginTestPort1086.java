package org.mockserver;

import org.junit.Before;
import org.junit.BeforeClass;
import org.mockserver.client.MockServerClient;
import org.mockserver.testing.integration.mock.AbstractBasicMockingIntegrationTest;

/**
 * @author jamesdbloom
 */
public class ClientServerMavenPluginTestPort1086 extends AbstractBasicMockingIntegrationTest {

    private final static int SERVER_HTTP_PORT = 1086;

    @BeforeClass
    public static void createClient() {
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

package org.mockserver;

import org.junit.Test;
import org.mockserver.client.proxy.ProxyClient;

/**
 * @author jamesdbloom
 */
public class ClientProxyMavenPluginStopTest {

    @Test(expected = RuntimeException.class)
    public void shouldNotBeAbleToReachProxy() {
        new ProxyClient("127.0.0.1", 9094).reset();
    }
}

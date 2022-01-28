package org.mockserver;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockserver.httpclient.NettyHttpClient;
import org.mockserver.model.Header;
import org.mockserver.model.Headers;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.logging.MockServerLogger;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;
import static org.mockserver.model.HttpStatusCode.OK_200;

/**
 * @author jamesdbloom
 */
public class InitializerMavenPluginTest {

    private final static int SERVER_HTTP_PORT = 1082;
    protected List<String> headersToIgnore = Arrays.asList(
            "server",
            "expires",
            "date",
            "host",
            "connection",
            "user-agent",
            "content-type",
            "content-length",
            "accept-encoding",
            "transfer-encoding"
    );
    // http client
    private static EventLoopGroup clientEventLoopGroup;
    private static NettyHttpClient httpClient;

    @BeforeClass
    public static void createClientAndEventLoopGroup() {
        clientEventLoopGroup = new NioEventLoopGroup();
        httpClient = new NettyHttpClient(new MockServerLogger(), clientEventLoopGroup, null, false);
    }

    @AfterClass
    public static void stopEventLoopGroup() {
        clientEventLoopGroup.shutdownGracefully(0, 0, MILLISECONDS).syncUninterruptibly();
    }

    @Test
    public void clientCanCallServer() {
        // then
        // - in http
        assertEquals(
                new HttpResponse()
                        .withStatusCode(OK_200.code())
                        .withReasonPhrase(OK_200.reasonPhrase())
                        .withBody("test_initializer_response_body"),
                makeRequest(
                        new HttpRequest()
                                .withMethod("POST")
                                .withPath("/test_initializer_path")
                                .withBody("test_initializer_request_body"),
                        headersToIgnore
                )
        );
        // - in https
        assertEquals(
                new HttpResponse()
                        .withStatusCode(OK_200.code())
                        .withReasonPhrase(OK_200.reasonPhrase())
                        .withBody("test_initializer_response_body"),
                makeRequest(
                        new HttpRequest()
                                .withMethod("POST")
                                .withPath("/test_initializer_path")
                                .withBody("test_initializer_request_body"),
                        headersToIgnore
                )
        );
    }

    private HttpResponse makeRequest(HttpRequest httpRequest, Collection<String> headersToIgnore) {
        try {
            HttpResponse httpResponse = httpClient.sendRequest(httpRequest, new InetSocketAddress("localhost", SERVER_HTTP_PORT))
                    .get(30, TimeUnit.SECONDS);
            List<Header> headers = new ArrayList<Header>();
            for (Header header : httpResponse.getHeaderList()) {
                if (!headersToIgnore.contains(header.getName().getValue().toLowerCase())) {
                    headers.add(header);
                }
            }
            if (!headers.isEmpty()) {
                httpResponse.withHeaders(headers);
            } else {
                httpResponse.withHeaders(new Headers());
            }
            return httpResponse;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}

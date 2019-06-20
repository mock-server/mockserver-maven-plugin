package org.mockserver.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author jamesdbloom
 */
public class MockServerRunAndWaitMojoTest {

    @Mock
    private SettableFuture<Object> objectSettableFuture;
    @Mock
    private InstanceHolder mockInstanceHolder;
    @InjectMocks
    private MockServerRunAndWaitMojo mockServerRunAndWaitMojo = new MockServerRunAndWaitMojo();

    @Before
    public void setupMocks() {
        initMocks(this);

        MockServerAbstractMojo.instanceHolder = mockInstanceHolder;
    }

    @Test
    public void shouldRunMockServerWithNullTimeout() throws ExecutionException, InterruptedException {
        // given
        mockServerRunAndWaitMojo.serverPort = "1,2";
        mockServerRunAndWaitMojo.logLevel = "WARN";
        mockServerRunAndWaitMojo.pipeLogToConsole = true;
        mockServerRunAndWaitMojo.timeout = null;
        mockServerRunAndWaitMojo.initializationClass = "org.mockserver.maven.ExampleInitializationClass";

        // when
        mockServerRunAndWaitMojo.execute();

        // then
        verify(mockInstanceHolder).start(eq(new Integer[]{1, 2}), eq(-1), eq(""), eq("WARN"), any(ExampleInitializationClass.class), eq(""));
        verify(objectSettableFuture).get();
    }

    @Test
    public void shouldRunMockServerAndWaitIndefinitelyAndHandleInterruptedException() throws ExecutionException, InterruptedException {
        // given
        mockServerRunAndWaitMojo.serverPort = "1";
        mockServerRunAndWaitMojo.timeout = 0;
        doThrow(new InterruptedException("TEST EXCEPTION")).when(objectSettableFuture).get();

        // when
        mockServerRunAndWaitMojo.execute();
    }

    @Test
    public void shouldRunMockServerAndWaitForFixedPeriod() throws ExecutionException, InterruptedException, TimeoutException {
        // given
        mockServerRunAndWaitMojo.serverPort = "1,2";
        mockServerRunAndWaitMojo.timeout = 2;
        mockServerRunAndWaitMojo.initializationClass = "org.mockserver.maven.ExampleInitializationClass";

        // when
        mockServerRunAndWaitMojo.execute();

        // then
        verify(mockInstanceHolder).start(eq(new Integer[]{1, 2}), eq(-1), eq(""), eq("INFO"), any(ExampleInitializationClass.class), eq(""));
        verify(objectSettableFuture).get(2, TimeUnit.SECONDS);
    }

    @Test
    public void shouldSkipStoppingMockServer() throws MojoExecutionException {
        // given
        mockServerRunAndWaitMojo.skip = true;

        // when
        mockServerRunAndWaitMojo.execute();

        // then
        verifyNoMoreInteractions(mockInstanceHolder);
    }
}

package org.mockserver.maven;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.mockserver.configuration.IntegerStringListParser;
import org.mockserver.initialize.ExpectationInitializer;
import org.mockserver.logging.MockServerLogger;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author jamesdbloom
 * @plexus.component role="org.codehaus.plexus.component.configurator.ComponentConfigurator"
 * role-hint="include-project-dependencies"
 * @plexus.requirement role="org.codehaus.plexus.component.configurator.converters.lookup.ConverterLookup"
 * role-hint="default"
 * @requiresDependencyCollection
 * @requiresDependencyResolution
 */
public abstract class MockServerAbstractMojo extends AbstractMojo {

    /**
     * Holds reference to jetty across plugin execution
     */
    @VisibleForTesting
    protected static InstanceHolder instanceHolder;
    /**
     * The HTTP, HTTPS, SOCKS and HTTP CONNECT port for the MockServer
     * for both mocking and proxying requests. Port unification is used
     * to support all protocols for proxying and mocking on the same port.
     */
    @Parameter(property = "mockserver.serverPort", defaultValue = "")
    protected String serverPort = "";
    /**
     * Optionally enables port forwarding mode. When specified all
     * requests received will be forwarded to the specified port,
     * unless they match an expectation.
     */
    @Parameter(property = "mockserver.proxyRemotePort", defaultValue = "-1")
    protected Integer proxyRemotePort = -1;
    /**
     * Specified the host to forward all proxy requests to when port
     * forwarding mode has been enabled using the proxyRemotePort option.
     * This setting is ignored unless proxyRemotePort has been specified.
     * If no value is provided for proxyRemoteHost when proxyRemotePort
     * has been specified, proxyRemoteHost will default to \"localhost\".
     */
    @Parameter(property = "mockserver.proxyRemoteHost", defaultValue = "")
    protected String proxyRemoteHost = "";
    /**
     * Timeout to wait before stopping MockServer, to run MockServer indefinitely do not set a value
     */
    @Parameter(property = "mockserver.timeout")
    protected Integer timeout;
    /**
     * Optionally specify log level as TRACE, DEBUG, INFO, WARN, ERROR or
     * OFF. If not specified default is INFO.
     */
    @Parameter(property = "mockserver.logLevel", defaultValue = "INFO")
    protected String logLevel = "INFO";
    /**
     * Skip the plugin execution completely
     */
    @Parameter(property = "mockserver.skip", defaultValue = "false")
    protected boolean skip;
    /**
     * If true the console of the forked JVM will be piped to the Maven console
     */
    @Parameter(property = "mockserver.pipeLogToConsole", defaultValue = "false")
    protected boolean pipeLogToConsole;
    /**
     * To enable the creation of default expectations that are generic across all tests or mocking scenarios a class can be specified
     * to initialize expectations in the MockServer, this class must implement org.mockserver.initialize.ExpectationInitializer interface,
     * the initializeExpectations(MockServerClient mockServerClient) method will be called once the MockServer has been started (but ONLY
     * if serverPort has been set), however it should be noted that it is generally better practice to create all expectations locally in
     * each test (or test class) for clarity, simplicity and to avoid brittle tests
     */
    @Parameter(property = "mockserver.initializationClass")
    protected String initializationClass;

    /**
     * The main classpath location of the project using this plugin
     */
    @Parameter(property = "project.compileClasspathElements", required = true, readonly = true)
    protected List<String> compileClasspath;

    /**
     * The test classpath location of the project using this plugin
     */
    @Parameter(property = "project.testClasspathElements", required = true, readonly = true)
    protected List<String> testClasspath;

    /**
     * The plugin dependencies
     */
    @Parameter(property = "pluginDescriptor.plugin.dependencies", required = true, readonly = true)
    protected List<Dependency> dependencies;

    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    MavenSession session;

    private Integer[] serverPorts;

    Integer[] getServerPorts() {
        if (serverPorts == null && StringUtils.isNotEmpty(serverPort)) {
            List<Integer> ports = new ArrayList<Integer>();
            for (String port : Splitter.on(',').split(serverPort)) {
                ports.add(Integer.parseInt(port));
            }
            serverPorts = ports.toArray(new Integer[ports.size()]);
        }
        return serverPorts;
    }

    protected InstanceHolder getLocalMockServerInstance() {
        if (instanceHolder == null) {
            // create on demand to avoid log creation for skipped plugins
            instanceHolder = new InstanceHolder();
        }
        return instanceHolder;
    }

    protected ExpectationInitializer createInitializer() {
        try {
            ClassLoader contextClassLoader = setupClasspath();
            if (contextClassLoader != null && StringUtils.isNotEmpty(initializationClass)) {
                Constructor<?> initializerClassConstructor = contextClassLoader.loadClass(initializationClass).getDeclaredConstructor();
                Object expectationInitializer = initializerClassConstructor.newInstance();
                if (expectationInitializer instanceof ExpectationInitializer) {
                    return (ExpectationInitializer) expectationInitializer;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    private ClassLoader setupClasspath() throws MalformedURLException {
        if (compileClasspath != null && testClasspath != null) {
            URL[] urls = new URL[compileClasspath.size() + testClasspath.size()];
            for (int i = 0; i < compileClasspath.size(); i++) {
                urls[i] = new File(compileClasspath.get(i)).toURI().toURL();
            }
            for (int i = compileClasspath.size(); i < compileClasspath.size() + testClasspath.size(); i++) {
                urls[i] = new File(testClasspath.get(i - compileClasspath.size())).toURI().toURL();
            }

            ClassLoader contextClassLoader = URLClassLoader.newInstance(urls, Thread.currentThread().getContextClassLoader());
            Thread.currentThread().setContextClassLoader(contextClassLoader);
            return contextClassLoader;
        }
        return null;
    }

    public static List<Integer> mockServerPort() {
        final String mockServerPort = System.getProperty("mockserver.mockServerPort");
        try {
            return new IntegerStringListParser().toList(mockServerPort);
        } catch (NumberFormatException nfe) {
            MockServerLogger.MOCK_SERVER_LOGGER.error("NumberFormatException converting " + "mockserver.mockServerPort" + " with value [" + mockServerPort + "]", nfe);
            return Collections.emptyList();
        }
    }

    public static void mockServerPort(Integer... port) {
        System.setProperty("mockserver.mockServerPort", new IntegerStringListParser().toString(port));
    }

}

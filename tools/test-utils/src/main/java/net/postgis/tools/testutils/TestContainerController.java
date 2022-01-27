package net.postgis.tools.testutils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.util.Objects;


/**
 * Contains functionality for controlling a test container.
 *
 * @author Phillip Ross
 */
public class TestContainerController {

    private static final Logger logger = LoggerFactory.getLogger(TestContainerController.class);

    public static final String TEST_CONTAINER_ENV_USER_VAR_NAME = "POSTGRES_USER";

    public static final String TEST_CONTAINER_ENV_PW_VAR_NAME = "POSTGRES_PASSWORD";

    public static final String TEST_CONTAINER_ENV_DB_VAR_NAME = "POSTGRES_DB";

    public static final String TEST_CONTAINER_ATTR_NAME = "test.container";

    public static final String TEST_CONTAINER_IPADDR_ATTR_NAME = "test.container.ip-address";

    public static final String TEST_CONTAINER_MAPPED_PORT_ATTR_NAME = "test.container.mapped-port";

    public static final String TEST_CONTAINER_IMAGE_NAME_PARAM_NAME = "test.container.image-name";

    public static final String TEST_CONTAINER_PORT_PARAM_NAME = "test.container.port";

    public static final String TEST_CONTAINER_JDBC_URL_SUFFIX = "test.container.jdbc-url-suffix";

    public static final String TEST_CONTAINER_ENV_USER_PARAM_NAME = "test.container.env.user";

    public static final String TEST_CONTAINER_ENV_PW_PARAM_NAME = "test.container.env.password";

    public static final String TEST_CONTAINER_ENV_DB_PARAM_NAME = "test.container.env.db";


    @BeforeSuite
    public void initializeTestContainer(ITestContext ctx) throws Exception {
        logger.debug("initializing test container");

        Objects.requireNonNull(ctx, "test context was null");
        ISuite suite = ctx.getSuite();
        Objects.requireNonNull(suite, "test suite was null");
        final String imageName = ctx.getSuite().getParameter(TEST_CONTAINER_IMAGE_NAME_PARAM_NAME);
        Objects.requireNonNull(imageName, TEST_CONTAINER_IMAGE_NAME_PARAM_NAME + " param was null");
        final String containerPortString = ctx.getSuite().getParameter(TEST_CONTAINER_PORT_PARAM_NAME);
        Objects.requireNonNull(containerPortString, TEST_CONTAINER_PORT_PARAM_NAME + " param was null");
        final int containerPort;
        try {
            containerPort = Integer.parseInt(containerPortString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Unable to parse specified container port: " + containerPortString, e);
        }
        final String containerEnvUsername = ctx.getSuite().getParameter(TEST_CONTAINER_ENV_USER_PARAM_NAME);
        Objects.requireNonNull(containerEnvUsername, TEST_CONTAINER_ENV_USER_PARAM_NAME + " param was null");
        final String containerEnvPassword = ctx.getSuite().getParameter(TEST_CONTAINER_ENV_PW_PARAM_NAME);
        Objects.requireNonNull(containerEnvPassword, TEST_CONTAINER_ENV_PW_PARAM_NAME + " param was null");
        final String containerEnvDb = ctx.getSuite().getParameter(TEST_CONTAINER_ENV_DB_PARAM_NAME);
        Objects.requireNonNull(containerEnvDb, TEST_CONTAINER_ENV_DB_PARAM_NAME + " param was null");
        logger.debug(
                "Initializing test container\n"
                        + "imageName=[{}]\n   port=[{}]\n   user=[{}]\n   password=[{}]\n   database=[{}]\n",
                imageName, containerPortString, containerEnvUsername, containerEnvPassword, containerEnvDb
        );

        GenericContainer container = new GenericContainer(imageName)
                .withExposedPorts(containerPort)
                .withEnv(TEST_CONTAINER_ENV_USER_VAR_NAME, containerEnvUsername)
                .withEnv(TEST_CONTAINER_ENV_PW_VAR_NAME, containerEnvPassword)
                .withEnv(TEST_CONTAINER_ENV_DB_VAR_NAME, containerEnvDb);
        container.start();
        final int mappedPort = container.getMappedPort(containerPort);
        final String containerIpAddress = container.getContainerIpAddress();
        logger.debug("started container with containerIPAddress=[{}] mappedPort=[{}]", containerIpAddress, mappedPort);
        Thread.sleep(200); // wait a moment more for container to come up beyond the test container wait strategy
        final String jdbcUrlSuffix = "://" + containerIpAddress + ":" + mappedPort + "/" + containerEnvDb;

        ctx.setAttribute(TEST_CONTAINER_ATTR_NAME, container);
        ctx.setAttribute(TEST_CONTAINER_IPADDR_ATTR_NAME, containerIpAddress);
        ctx.setAttribute(TEST_CONTAINER_MAPPED_PORT_ATTR_NAME, mappedPort);
        ctx.setAttribute(TEST_CONTAINER_JDBC_URL_SUFFIX, jdbcUrlSuffix);
        ctx.setAttribute(TEST_CONTAINER_ENV_USER_PARAM_NAME, containerEnvUsername);
        ctx.setAttribute(TEST_CONTAINER_ENV_PW_PARAM_NAME, containerEnvPassword);
        ctx.setAttribute(TEST_CONTAINER_ENV_DB_PARAM_NAME, containerEnvDb);
    }


    @AfterSuite
    public void deInitializeTestContainer(ITestContext ctx) {
        logger.debug("de-initializing test container");
        if (ctx.getAttribute(TEST_CONTAINER_ATTR_NAME) instanceof GenericContainer) {
            ((GenericContainer)ctx.getAttribute(TEST_CONTAINER_ATTR_NAME)).close();
        }
    }


}

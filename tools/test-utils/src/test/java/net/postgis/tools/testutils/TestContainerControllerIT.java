package net.postgis.tools.testutils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;


/**
 * Tests TestContainerController functionality.
 *
 * @author Phillip Ross
 */
public class TestContainerControllerIT {

    private static final Logger logger = LoggerFactory.getLogger(TestContainerControllerIT.class);


    @Test
    public void test(ITestContext ctx) throws Exception {
        Assert.assertNotNull(ctx.getAttribute(TestContainerController.TEST_CONTAINER_ATTR_NAME));
        Assert.assertNotNull(ctx.getAttribute(TestContainerController.TEST_CONTAINER_IPADDR_ATTR_NAME));
        Assert.assertNotNull(ctx.getAttribute(TestContainerController.TEST_CONTAINER_MAPPED_PORT_ATTR_NAME));
        Assert.assertNotNull(ctx.getAttribute(TestContainerController.TEST_CONTAINER_JDBC_URL_SUFFIX));
        Assert.assertNotNull(ctx.getAttribute(TestContainerController.TEST_CONTAINER_ENV_USER_PARAM_NAME));
        Assert.assertNotNull(ctx.getAttribute(TestContainerController.TEST_CONTAINER_ENV_PW_PARAM_NAME));
    }


}

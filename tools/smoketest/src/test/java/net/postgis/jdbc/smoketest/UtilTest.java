package net.postgis.jdbc.smoketest;

import net.postgis.tools.testutils.TestContainerController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.Test;


/**
 * A test class for testing the primary utility.
 *
 * @author Phillip Ross
 */
public class UtilTest {

    private static final Logger logger = LoggerFactory.getLogger(UtilTest.class);


    @Test
    public void test(ITestContext ctx) {
        final String jdbcUrlSuffix = (String)ctx.getAttribute(TestContainerController.TEST_CONTAINER_JDBC_URL_SUFFIX);
        Assert.assertNotNull(jdbcUrlSuffix);
        final String jdbcUrl = "jdbc:postgresql" + jdbcUrlSuffix;
        final String jdbcUsername = (String)ctx.getAttribute(TestContainerController.TEST_CONTAINER_ENV_USER_PARAM_NAME);
        Assert.assertNotNull(jdbcUsername);
        final String jdbcPassword = (String)ctx.getAttribute(TestContainerController.TEST_CONTAINER_ENV_PW_PARAM_NAME);
        OSGeo.main(new String[] {jdbcUrl, jdbcUsername, jdbcPassword});
    }


}

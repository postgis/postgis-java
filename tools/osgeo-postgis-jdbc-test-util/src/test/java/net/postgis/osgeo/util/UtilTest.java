package net.postgis.osgeo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;


/**
 * A test class for testing the primary utility.
 *
 * @author Phillip Ross
 */
public class UtilTest {

    private static final Logger logger = LoggerFactory.getLogger(UtilTest.class);


    @Test
    @Parameters({"jdbcUrlSystemProperty", "jdbcUsernameSystemProperty", "jdbcPasswordSystemProperty"})
    public void test(String jdbcUrlSystemProperty,
                     String jdbcUsernameSystemProperty,
                     String jdbcPasswordSystemProperty) {
        logger.debug("jdbcUrlSystemProperty: {}", jdbcUrlSystemProperty);
        logger.debug("jdbcUsernameSystemProperty: {}", jdbcUsernameSystemProperty);
        logger.debug("jdbcPasswordSystemProperty: {}", jdbcPasswordSystemProperty);

        String jdbcUrl = System.getProperty(jdbcUrlSystemProperty);
        String jdbcUsername = System.getProperty(jdbcUsernameSystemProperty);
        String jdbcPassword = System.getProperty(jdbcPasswordSystemProperty);

        logger.debug("jdbcUrl: {}", jdbcUrl);
        logger.debug("jdbcUsername: {}", jdbcUsername);
        logger.debug("jdbcPassword: {}", jdbcPassword);

        Main.main(new String[] {jdbcUrl, jdbcUsername, jdbcPassword});
    }


}

/*
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * (C) 2005 Markus Schaber, markus.schaber@logix-tt.com
 *
 * (C) 2020 Phillip Ross, phillip.w.g.ross@gmail.com
 */

package org.postgis;


import net.postgis.tools.testutils.TestContainerController;
import org.postgis.util.VersionUtil;
import org.postgresql.Driver;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;


/**
 * This test program tests whether the auto-registration of PostGIS data types within the postgresql jdbc driver was
 * successful.  It also checks for PostGIS version to know whether box2d is available.
 */
public class DatatypesAutoRegistrationTest {

    /** The static logger instance. */
    private static final Logger logger = LoggerFactory.getLogger(DatatypesAutoRegistrationTest.class);

    /** The JDBC Connection to be used for tests. */
    private Connection connection;


    /**
     * Initializes a new JDBC Connection.
     *
     * @param ctx the test context
     * @throws Exception when an exception occurs
     */
    @BeforeMethod
    public void initJdbcConnection(ITestContext ctx) throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        final String jdbcUrlSuffix = (String)ctx.getAttribute(TestContainerController.TEST_CONTAINER_JDBC_URL_SUFFIX);
        Assert.assertNotNull(jdbcUrlSuffix);
        final String jdbcUrl = "jdbc:postgresql" + jdbcUrlSuffix;
        final String jdbcUsername = (String)ctx.getAttribute(TestContainerController.TEST_CONTAINER_ENV_USER_PARAM_NAME);
        Assert.assertNotNull(jdbcUsername);
        final String jdbcPassword = (String)ctx.getAttribute(TestContainerController.TEST_CONTAINER_ENV_PW_PARAM_NAME);
        Assert.assertNotNull(jdbcPassword);
        connection = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
    }


    /**
     * Un-allocates the JDBC connection.
     *
     * @throws Exception when an exception occurs
     */
    @AfterMethod
    public void shutdown() throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        logger.debug("shutting down");
        if (connection != null) {
            connection.close();
        }
    }


    @Test
    public void testAutoRegistration() throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        Driver driver = new Driver();
        int driverMajorVersion = driver.getMajorVersion();
        int driverMinorVersion = driver.getMinorVersion();
        logger.debug("Driver version: {}.{}", driverMajorVersion, driverMinorVersion);
        if (driverMajorVersion < 8) {
            logger.info(
                    "postgresql driver {}.{} is too old, it does not support auto-registration",
                    driverMajorVersion, driverMinorVersion
            );
        } else {
            int postgisServerMajor = Integer.parseInt(VersionUtil.retrievePostGISServerMajorVersion(connection));
            logger.debug("PostGIS Version: " + postgisServerMajor);
            Assert.assertNotEquals(
                    postgisServerMajor,
                    0,
                    "Could not get PostGIS version. Is PostGIS really installed in the database?"
            );
            Statement statement = connection.createStatement();

            // Test geometries
            ResultSet resultSet = statement.executeQuery("SELECT 'POINT(1 2)'::geometry");
            resultSet.next();
            PGobject result = (PGobject) resultSet.getObject(1);
            Assert.assertTrue(result instanceof PGgeometry);

            // Test geography
            resultSet = statement.executeQuery("SELECT 'POINT(1 2)'::geography");
            resultSet.next();
            Object geographyRawObject = resultSet.getObject(1);
            result = (PGobject) resultSet.getObject(1);
            Assert.assertTrue(result instanceof PGgeography);

            // Test box3d
            resultSet = statement.executeQuery("SELECT 'BOX3D(1 2 3, 4 5 6)'::box3d");
            resultSet.next();
            result = (PGobject) resultSet.getObject(1);
            Assert.assertTrue(result instanceof PGbox3d);

            // Test box2d if appropriate
            if (postgisServerMajor < 1) {
                logger.info("PostGIS version is too old, skipping box2ed test");
            } else {
                resultSet = statement.executeQuery("SELECT 'BOX(1 2,3 4)'::box2d");
                resultSet.next();
                result = (PGobject) resultSet.getObject(1);
                Assert.assertTrue(result instanceof PGbox2d);
            }
        }
    }


}

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
 * (C) 2018 Phillip Ross, phillip.w.g.ross@gmail.com
 */

package net.postgis.util;


import net.postgis.tools.testutils.TestContainerController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.StringTokenizer;


/**
 * Integration tests for VersionUtil.
 *
 * @author Phillip Ross
 */
public class VersionUtilIT {

    /** The static logger instance. */
    private static final Logger logger = LoggerFactory.getLogger(VersionUtilIT.class);

    /** The jdbc url prefix containing the jdbc protocol to be used for tests. */
    private static final String JDBC_URL_PROTOCOL_PREFIX = "jdbc:postgresql";

    /** The JDBC Connection to be used for tests. */
    private Connection connection = null;


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
        final String jdbcUrl = JDBC_URL_PROTOCOL_PREFIX + jdbcUrlSuffix;
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
    public void unallocateDatabaseResources() throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        if ((connection != null) && (!connection.isClosed())) {
            connection.close();
        }
    }


    /**
     * Test getting version string with a null connection.
     *
     * @throws Exception when an exception occurs
     */
    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*null connection.*")
    public void test_VersionUtil_GetVersionString_NullConnection() throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        VersionUtil.getVersionString(null, VersionFunctions.POSTGIS_FULL_VERSION.toString());
    }


    /**
     * Test getting version string with an invalid connection.
     *
     * @throws Exception when an exception occurs
     */
    @Test(expectedExceptions = SQLException.class, expectedExceptionsMessageRegExp = ".*connection.*not valid.*")
    public void test_VersionUtil_GetVersionString_InvalidConnection() throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        connection.close();
        VersionUtil.getVersionString(connection, VersionFunctions.POSTGIS_FULL_VERSION.toString());
    }


    /**
     * Test getting version string with an invalid function name.
     *
     * @throws Exception when an exception occurs
     */
    @Test(
            expectedExceptions = SQLException.class,
            expectedExceptionsMessageRegExp = ".*(?!" + VersionUtil.NONEXISTENT_FUNCTION_ERROR_MESSAGE_CONTENT +  ")"
    )
    public void test_VersionUtil_GetVersionString_InvalidFunctionName() throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        VersionUtil.getVersionString(connection, "invalid.function.name");
    }


    /**
     * Test getting version string with a null function.
     *
     * @throws Exception when an exception occurs
     */
    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*null function.*")
    public void test_VersionUtil_GetVersionString_NullFunction() throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        VersionUtil.getVersionString(connection, null);
    }


    /**
     * Test getting version string of an unavailable function.
     *
     * @throws Exception when an exception occurs
     */
    @Test
    public void test_VersionUtil_GetVersionString_Unavailable() throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        Assert.assertTrue(
                VersionUtil.getVersionString(connection, "nonexistent")
                        .contains("unavailable")
        );
    }


    /**
     * Test getting version strings for all enumerated version functions.
     *
     * @throws Exception when an exception occurs
     */
    @Test
    public void test_VersionUtil_GetVersionString_VersionFunctionsEnum() throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        for (int i = 0; i < VersionFunctions.values().length; i++) {
            String function = VersionFunctions.values()[i].toString();
            String version = VersionUtil.getVersionString(connection, function);
            logger.debug("function [{}] => version string [{}]", function, version);
        }
    }


    /**
     * Test getting the server version string with a null connection.
     *
     * @throws Exception when an exception occurs
     */
    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*null connection.*")
    public void test_VersionUtil_RetrievePostGISServerVersionString_NullConnection() throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        VersionUtil.retrievePostGISServerVersionString(null);
    }


    /**
     * Test getting the server version string with an invalid connection.
     *
     * @throws Exception when an exception occurs
     */
    @Test(expectedExceptions = SQLException.class, expectedExceptionsMessageRegExp = ".*connection.*not valid.*")
    public void test_VersionUtil_RetrievePostGISServerVersionString_InvalidConnection() throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        connection.close();
        VersionUtil.retrievePostGISServerVersionString(connection);
    }


    /**
     * Test getting the server version string.
     *
     * @throws Exception when an exception occurs
     */
    @Test
    public void test_VersionUtil_RetrievePostGISServerVersionString() throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        String postGISServerVersionString = VersionUtil.retrievePostGISServerVersionString(connection);
        Assert.assertNotNull(postGISServerVersionString);
        logger.debug("PostGIS server version string [{}]", postGISServerVersionString);
    }


    /**
     * Test getting the server version with a null connection.
     *
     * @throws Exception when an exception occurs
     */
    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*null connection.*")
    public void test_VersionUtil_RetrievePostGISServerVersion_NullConnection() throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        VersionUtil.retrievePostGISServerVersion(null);
    }


    /**
     * Test getting the server version with an invalid connection.
     *
     * @throws Exception when an exception occurs
     */
    @Test(expectedExceptions = SQLException.class, expectedExceptionsMessageRegExp = ".*connection.*not valid.*")
    public void test_VersionUtil_RetrievePostGISServerVersion_InvalidConnection() throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        connection.close();
        VersionUtil.retrievePostGISServerVersion(connection);
    }


    /**
     * Test getting the server version.
     *
     * @throws Exception when an exception occurs
     */
    @Test
    public void test_VersionUtil_RetrievePostGISServerVersion() throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        final String version = VersionUtil.retrievePostGISServerVersion(connection);
        Assert.assertNotNull(version);
        logger.debug("PostGIS server version [{}]", version);
    }


    /**
     * Test getting the server major version with a null connection.
     *
     * @throws Exception when an exception occurs
     */
    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*null connection.*")
    public void test_VersionUtil_RetrievePostGISServerMajorVersion_NullConnection() throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        VersionUtil.retrievePostGISServerMajorVersion(null);
    }


    /**
     * Test getting the server major version with an invalid connection.
     *
     * @throws Exception when an exception occurs
     */
    @Test(expectedExceptions = SQLException.class, expectedExceptionsMessageRegExp = ".*connection.*not valid.*")
    public void test_VersionUtil_RetrievePostGISServerMajorVersion_InvalidConnection() throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        connection.close();
        VersionUtil.retrievePostGISServerMajorVersion(connection);
    }


    /**
     * Test getting the server major version.
     *
     * @throws Exception when an exception occurs
     */
    @Test
    public void test_VersionUtil_RetrievePostGISServerMajorVersion() throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        final String version = VersionUtil.retrievePostGISServerMajorVersion(connection);
        Assert.assertNotNull(version);
        logger.debug("PostGIS server major version [{}]", version);
    }


    /**
     * Test getting the server minor version with a null connection.
     *
     * @throws Exception when an exception occurs
     */
    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = ".*null connection.*")
    public void test_VersionUtil_RetrievePostGISServerMinorVersion_NullConnection() throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        VersionUtil.retrievePostGISServerMinorVersion(null);
    }


    /**
     * Test getting the server minor version with an invalid connection.
     *
     * @throws Exception when an exception occurs
     */
    @Test(expectedExceptions = SQLException.class, expectedExceptionsMessageRegExp = ".*connection.*not valid.*")
    public void test_VersionUtil_RetrievePostGISServerMinorVersion_InvalidConnection() throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        connection.close();
        VersionUtil.retrievePostGISServerMinorVersion(connection);
    }


    /**
     * Test getting the server minor version.
     *
     * @throws Exception when an exception occurs
     */
    @Test
    public void test_VersionUtil_RetrievePostGISServerMinorVersion() throws Exception {
        logger.trace("[{}#{}]", getClass().getName(), new Object(){}.getClass().getEnclosingMethod().getName());
        final String version = VersionUtil.retrievePostGISServerMinorVersion(connection);
        Assert.assertNotNull(version);
        logger.debug("PostGIS server minor version [{}]", version);
    }


    /**
     * Test additional parsing assertions against retrieved versions.
     *
     * @throws Exception when an exception occurs
     */
    @Test
    public void testServerVersionParsing() throws Exception {
        logger.trace("[{}.{}]", getClass(), new Object(){}.getClass().getEnclosingMethod().getName());
        final String versionString = VersionUtil.retrievePostGISServerVersionString(connection);
        Assert.assertNotNull(versionString);
        final String versionFull = VersionUtil.retrievePostGISServerVersion(connection);
        Assert.assertNotNull(versionFull);
        Assert.assertTrue(versionString.startsWith(versionFull));
        final StringTokenizer stringTokenizer =
                new StringTokenizer(versionString, VersionUtil.POSTGIS_SERVER_VERSION_SEPERATOR);
        Assert.assertTrue(stringTokenizer.countTokens() > 0);
        final String versionMajor = VersionUtil.retrievePostGISServerMajorVersion(connection);
        Assert.assertEquals(versionMajor, stringTokenizer.nextToken());
        if (stringTokenizer.countTokens() > 1) {
            final String versionMinor = VersionUtil.retrievePostGISServerMinorVersion(connection);
            Assert.assertEquals(versionMinor, stringTokenizer.nextToken());
        }
    }


}

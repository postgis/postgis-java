/*
 * VersionPrinter.java
 * 
 * PostGIS extension for PostgreSQL JDBC driver - example and test classes
 * 
 * (C) 2005 Markus Schaber, markus.schaber@logix-tt.com
 *
 * (C) 2015 Phillip Ross, phillip.w.g.ross@gmail.com
 * 
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
 */

package org.postgis;


import net.postgis.tools.testutils.TestContainerController;
import org.postgresql.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.*;


/**
 * Prints out as much version information as available.
 */
public class VersionPrinter {

    private static final Logger logger = LoggerFactory.getLogger(VersionPrinter.class);

    private static final String JDBC_DRIVER_CLASS_NAME = "org.postgresql.Driver";

    public static String[] POSTGIS_FUNCTIONS = {
            "postgis_version",
            "postgis_proj_version",
            "postgis_scripts_installed",
            "postgis_lib_version",
            "postgis_scripts_released",
            "postgis_uses_stats",
            "postgis_geos_version",
            "postgis_scripts_build_date",
            "postgis_lib_build_date",
            "postgis_full_version",
            "postgis_gdal_version",
            "postgis_libjson_version",
            "postgis_libxml_version",
            "postgis_raster_lib_version",
            "postgis_svn_version"
    };

    private Connection connection = null;

    private Statement statement = null;


    @Test
    public void test() throws Exception {

        // Print PostGIS version
        logger.info("*** PostGIS jdbc client code ***");
        String fullVersion = Version.getFullVersion();
        Assert.assertNotNull(fullVersion);
        logger.info("\t getFullVersion: {}", fullVersion);

    	// Print PostgreSQL JDBC Versions
        logger.info("*** PostgreSQL JDBC Driver ***");
        String driverVersion = Driver.getVersion();
        Assert.assertNotNull(driverVersion);
        logger.info("\t getVersion: {}", driverVersion);

        try {
            Driver driver = new Driver();
            int majorVersion = driver.getMajorVersion();
            Assert.assertNotEquals(majorVersion, 0);
            logger.info("\t getMajorVersion: {}", majorVersion);
            int minorVersion = driver.getMinorVersion();
            Assert.assertNotEquals(minorVersion, 0);
            logger.info("\t getMinorVersion: {}", majorVersion);
        } catch (Exception e) {
            logger.error("Cannot create Driver instance: {}", e.getMessage());
        }

        // Print PostgreSQL server versions
        Assert.assertNotNull(connection);
        Statement statement = connection.createStatement();
        if (statement == null) {
            logger.info("No online version available.");
        } else {
            logger.info("*** PostgreSQL Server ***");
            String versionString = getVersionString("version");
            logger.debug("\t version: {}", versionString);

            // Print PostGIS versions
            logger.info("*** PostGIS Server ***");
            for (String GISVERSION : POSTGIS_FUNCTIONS) {
                versionString = getVersionString(GISVERSION);
                logger.debug("\t {} version: {}", GISVERSION, versionString);
            }
        }
    }


    public String getVersionString(String function) throws SQLException {
        String result = "-- unavailable -- ";
        try {
            ResultSet resultSet = statement.executeQuery("SELECT " + function + "()");
            if (resultSet.next()) {
                String version = resultSet.getString(1);
                if (version != null) {
                    result = version.trim();
                } else {
                    result = "-- null result --";
                }
            } else {
                result = "-- no result --";
            }
        } catch (SQLException sqle) {
            // If the function does not exist, a SQLException will be thrown, but it should be caught an swallowed if
            // the "does not exist" string is in the error message.  The SQLException might be thrown for some other
            // problem not related to the missing function, so rethrow it if it doesn't contain the string.
            if (!sqle.getMessage().contains("does not exist")) {
                throw sqle;
            }
        }
        return result;
    }


    @BeforeClass
    public void initJdbcConnection(ITestContext ctx) throws Exception {
        final String jdbcUrlSuffix = (String)ctx.getAttribute(TestContainerController.TEST_CONTAINER_JDBC_URL_SUFFIX);
        Assert.assertNotNull(jdbcUrlSuffix);
        final String jdbcUrl = "jdbc:postgresql" + jdbcUrlSuffix;
        final String jdbcUsername = (String)ctx.getAttribute(TestContainerController.TEST_CONTAINER_ENV_USER_PARAM_NAME);
        Assert.assertNotNull(jdbcUsername);
        final String jdbcPassword = (String)ctx.getAttribute(TestContainerController.TEST_CONTAINER_ENV_PW_PARAM_NAME);
        Assert.assertNotNull(jdbcPassword);
        Class.forName(JDBC_DRIVER_CLASS_NAME);
        connection = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
        statement = connection.createStatement();
    }


    @AfterClass
    public void unallocateDatabaseResources() throws Exception {
        if ((statement != null) && (!statement.isClosed())) {
            statement.close();
        }
        if ((connection != null) && (!connection.isClosed())) {
            connection.close();
        }
    }


}
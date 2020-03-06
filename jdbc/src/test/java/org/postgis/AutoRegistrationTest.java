/*
 * AutoRegistrationTest.java
 * 
 * PostGIS extension for PostgreSQL JDBC driver - example and test classes
 * 
 * (C) 2005 Markus Schaber, markus.schaber@logix-tt.com
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


import org.postgresql.Driver;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.sql.*;


/**
 * This test program tests whether the autoregistration of PostGIS data type within the postgresql jdbc driver was
 * successful.  It also checks for PostGIS version to know whether box2d is available.
 */
public class AutoRegistrationTest {

    private static final Logger logger = LoggerFactory.getLogger(AutoRegistrationTest.class);

    private Connection connection = null;

    @Test
    public void testAutoRegistration() throws Exception {

        logger.debug("Driver version: {}", Driver.getVersion());
        int major = new Driver().getMajorVersion();
        Assert.assertTrue(major >= 8, "postgresql driver " + major + ".X is too old, it does not support auto-registration");

        Statement statement = connection.createStatement();
        int postgisServerMajor = getPostgisMajor(statement);
        logger.debug("PostGIS Version: " + postgisServerMajor);
        Assert.assertNotEquals(postgisServerMajor, 0, "Could not get PostGIS version. Is PostGIS really installed in the database?");

        // Test geometries
        ResultSet resultSet = statement.executeQuery("SELECT 'POINT(1 2)'::geometry");
        resultSet.next();
        PGobject result = (PGobject) resultSet.getObject(1);
        Assert.assertTrue(result instanceof PGgeometry);

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


    public static int getPostgisMajor(Statement statement) throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT postgis_version()");
        resultSet.next();
        String version = resultSet.getString(1);
        if (version == null) {
            throw new SQLException("postgis_version returned NULL!");
        }
        version = version.trim();
        int idx = version.indexOf('.');
        return Integer.parseInt(version.substring(0, idx));
    }


    @BeforeClass
    @Parameters({"jdbcUrlSystemProperty", "jdbcUsernameSystemProperty", "jdbcPasswordSystemProperty"})
    public void initJdbcConnection(String jdbcUrlSystemProperty,
                                   String jdbcUsernameSystemProperty,
                                   String jdbcPasswordSystemProperty) throws Exception {
        logger.debug("jdbcUrlSystemProperty: {}", jdbcUrlSystemProperty);
        logger.debug("jdbcUsernameSystemProperty: {}", jdbcUsernameSystemProperty);
        logger.debug("jdbcPasswordSystemProperty: {}", jdbcPasswordSystemProperty);

        String jdbcUrl = System.getProperty(jdbcUrlSystemProperty);
        String jdbcUsername = System.getProperty(jdbcUsernameSystemProperty);
        String jdbcPassword = System.getProperty(jdbcPasswordSystemProperty);

        logger.debug("jdbcUrl: {}", jdbcUrl);
        logger.debug("jdbcUsername: {}", jdbcUsername);
        logger.debug("jdbcPassword: {}", jdbcPassword);

        connection = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
    }


}
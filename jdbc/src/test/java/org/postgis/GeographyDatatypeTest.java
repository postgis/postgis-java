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
 * (C) 2020 Phillip Ross, phillip.w.g.ross@gmail.com
 */

package org.postgis;


import net.postgis.tools.testutils.TestContainerController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;


/**
 * Integration tests for PGgeography.
 *
 * @author Phillip Ross
 */
public class GeographyDatatypeTest {

    /** The static logger instance. */
    private static final Logger logger = LoggerFactory.getLogger(GeographyDatatypeTest.class);

    /** The jdbc url prefix containing the jdbc protocol to be used for tests. */
    private static final String JDBC_URL_PROTOCOL_PREFIX = "jdbc:postgresql";

    /** The jdbc url prefix containing the jdbc lightweight protocol to be used for tests. */
    private static final String JDBC_URL_LW_PROTOCOL_PREFIX = "jdbc:postgresql_lwgis";

    /** The prefix for database tables used in the tests. */
    private static final String DATABASE_TABLE_NAME_PREFIX = "jdbc_test";

    /** Test geometries dataset. */
    public static final String[] testGeometries = new String[] {
            "POINT(10 10)", // 2D
            "POINT(10 10 0)", // 3D with 3rd coordinate set to 0
            "POINT(10 10 20)", // 3D
            "POINT(1e100 1.2345e-100 -2e-5)", // 3D with scientific notation
            "POINTM(10 10 20)", // 2D + Measures
            "POINT(10 10 20 30)", // 3D + Measures
            "MULTIPOINT(11 12, 20 20)", // broken format, see http://lists.jump-project.org/pipermail/jts-devel/2006-April/001572.html
            "MULTIPOINT(11 12 13, 20 20 20)", // broken format
            "MULTIPOINTM(11 12 13, 20 20 20)", // broken format
            "MULTIPOINT(11 12 13 14,20 20 20 20)", // broken format
            "MULTIPOINT((11 12), (20 20))", // OGC conforming format
            "MULTIPOINT((11 12 13), (20 20 20))",
            "MULTIPOINTM((11 12 13), (20 20 20))",
            "MULTIPOINT((11 12 13 14),(20 20 20 20))",
            "LINESTRING(10 10,20 20,50 50,34 34)",
            "LINESTRING(10 10 20,20 20 20,50 50 50,34 34 34)",
            "LINESTRINGM(10 10 20,20 20 20,50 50 50,34 34 34)",
            "LINESTRING(10 10 20 20,20 20 20 20,50 50 50 50,34 34 34 50)",
            "POLYGON((10 10,20 10,20 20,20 10,10 10),(5 5,5 6,6 6,6 5,5 5))",
            "POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))",
            "POLYGONM((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))",
            "POLYGON((10 10 0 7,20 10 0 7,20 20 0 7,20 10 0 7,10 10 0 7),(5 5 0 7,5 6 0 7,6 6 0 7,6 5 0 7,5 5 0 7))",
            "MULTIPOLYGON(((10 10,20 10,20 20,20 10,10 10),(5 5,5 6,6 6,6 5,5 5)),((10 10,20 10,20 20,20 10,10 10),(5 5,5 6,6 6,6 5,5 5)))",
            "MULTIPOLYGON(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))",
            "MULTIPOLYGONM(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))",
            "MULTIPOLYGON(((10 10 0 7,20 10 0 7,20 20 0 7,20 10 0 7,10 10 0 7),(5 5 0 7,5 6 0 7,6 6 0 7,6 5 0 7,5 5 0 7)),((10 10 0 7,20 10 0 7,20 20 0 7,20 10 0 7,10 10 0 7),(5 5 0 7,5 6 0 7,6 6 0 7,6 5 0 7,5 5 0 7)))",
            "MULTILINESTRING((10 10,20 10,20 20,20 10,10 10),(5 5,5 6,6 6,6 5,5 5))",
            "MULTILINESTRING((10 10 5,20 10 5,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))",
            "MULTILINESTRINGM((10 10 7,20 10 7,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))",
            "MULTILINESTRING((10 10 0 7,20 10 0 7,20 20 0 7,20 10 0 7,10 10 0 7),(5 5 0 7,5 6 0 7,6 6 0 7,6 5 0 7,5 5 0 7))",
            "GEOMETRYCOLLECTION(POINT(10 10),POINT(20 20))",
            "GEOMETRYCOLLECTION(POINT(10 10 20),POINT(20 20 20))",
            "GEOMETRYCOLLECTION(POINT(10 10 20 7),POINT(20 20 20 7))",
            "GEOMETRYCOLLECTION(LINESTRING(10 10 20,20 20 20, 50 50 50, 34 34 34),LINESTRING(10 10 20,20 20 20, 50 50 50, 34 34 34))",
            "GEOMETRYCOLLECTION(POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))",
            "GEOMETRYCOLLECTION(MULTIPOINT(10 10 10, 20 20 20),MULTIPOINT(10 10 10, 20 20 20))",  // Cannot be parsed by 0.X servers, broken format
            "GEOMETRYCOLLECTION(MULTIPOINT((10 10 10), (20 20 20)),MULTIPOINT((10 10 10), (20 20 20)))", // Cannot be parsed by 0.X servers, OGC conformant
            "GEOMETRYCOLLECTION(MULTILINESTRING((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))", // PostGIs 0.X "flattens" this geometry, so it is not equal after reparsing.
            "GEOMETRYCOLLECTION(MULTIPOLYGON(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))),MULTIPOLYGON(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))))", // PostGIs 0.X "flattens" this geometry, so it is not equal after reparsing.
            "GEOMETRYCOLLECTION(POINT(10 10 20),LINESTRING(10 10 20,20 20 20, 50 50 50, 34 34 34),POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))",
            "GEOMETRYCOLLECTION(POINT(10 10 20),MULTIPOINT(10 10 10, 20 20 20),LINESTRING(10 10 20,20 20 20, 50 50 50, 34 34 34),POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),MULTIPOLYGON(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))),MULTILINESTRING((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))", // Collections that contain both X and MultiX do not work on PostGIS 0.x, broken format
            "GEOMETRYCOLLECTION(POINT(10 10 20),MULTIPOINT((10 10 10), (20 20 20)),LINESTRING(10 10 20,20 20 20, 50 50 50, 34 34 34),POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),MULTIPOLYGON(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))),MULTILINESTRING((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))", // Collections that contain both X and MultiX do not work on PostGIS 0.x, OGC conformant
            "GEOMETRYCOLLECTION EMPTY", // new (correct) representation
            "GEOMETRYCOLLECTIONM(POINTM(10 10 20),POINTM(20 20 20))"
    };

    /** The JDBC Connection to be used for tests. */
    private Connection connection = null;

    /** The JDBC Connection w/ lightweight protocol to be used for tests. */
    private Connection connectionLW = null;


    /**
     * Initializes a new JDBC Connection.
     *
     * @param ctx the test context
     * @throws Exception when an exception occurs
     */
    @BeforeMethod
    public void initJdbcConnection(ITestContext ctx) throws Exception {
        final String jdbcUrlSuffix = (String)ctx.getAttribute(TestContainerController.TEST_CONTAINER_JDBC_URL_SUFFIX);
        Assert.assertNotNull(jdbcUrlSuffix);
        final String jdbcUrl = JDBC_URL_PROTOCOL_PREFIX + jdbcUrlSuffix;
        final String jdbcUrlLW = JDBC_URL_LW_PROTOCOL_PREFIX + jdbcUrlSuffix;
        final String jdbcUsername = (String)ctx.getAttribute(TestContainerController.TEST_CONTAINER_ENV_USER_PARAM_NAME);
        Assert.assertNotNull(jdbcUsername);
        final String jdbcPassword = (String)ctx.getAttribute(TestContainerController.TEST_CONTAINER_ENV_PW_PARAM_NAME);
        Assert.assertNotNull(jdbcPassword);
        Class.forName("org.postgis.DriverWrapperLW");
        connection = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
        connectionLW = DriverManager.getConnection(jdbcUrlLW, jdbcUsername, jdbcPassword);
    }


    /**
     * Un-allocates the JDBC connection.
     *
     * @throws Exception when an exception occurs
     */
    @AfterMethod
    public void unallocateDatabaseResources() throws Exception {
        if ((connection != null) && (!connection.isClosed())) {
            connection.close();
        }
        if ((connectionLW != null) && (!connectionLW.isClosed())) {
            connection.close();
        }
    }


    /**
     * Test inserting geometries into the database with prepared statements and querying back the results with
     * both geometry/geography and standard/lightweight.
     *
     * @throws Exception when an exception occurs.
     */
    @Test
    public void testDatatypes() throws Exception {

        final String testTableName = DATABASE_TABLE_NAME_PREFIX
                + "_"
                + UUID.randomUUID()
                .toString()
                .replaceAll("-", "");

        final String dropTableSQL = "drop table " + testTableName;

        final String createTableSQL = "create table " + testTableName
                + " ( _id numeric,"
                + " geometry_value geometry, geometrylw_value geometry,"
                + " geography_value geography, geographylw_value geography)";
        final int idColumnIndex = 1;
        final int geometryValueColumnIndex = 2;
        final int geometrylwValueColumnIndex = 3;
        final int geographyValueColumnIndex = 4;
        final int geographylwValueColumnIndex = 5;

        final String insertSQL = "insert into " + testTableName + " ( "
                + "_id, geometry_value, geometrylw_value, "
                + "geography_value, geographylw_value) "
                + "values ( ?, ?, ?, ?, ? )";

        boolean tableExists = false;
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        try (
                ResultSet resultSet = databaseMetaData.getTables(
                        null, null, testTableName.toLowerCase(), new String[] { "TABLE" }
                )
        ) {
            while (resultSet.next()) {
                tableExists = true;
            }
        }

        if (tableExists) {
            logger.debug("Dropping pre-existing test table...");
            try (Statement statement = connection.createStatement()) {
                statement.executeQuery(dropTableSQL);
            }
        }

        logger.debug("Creating test table...");
        try (Statement statement = connection.createStatement()) {
            statement.execute(createTableSQL);
        }


        logger.debug("Inserting test geometries into table...");
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            for (int i = 0; i < testGeometries.length; i++) {
                PGgeometry geometry = new PGgeometry(testGeometries[i]);
                PGgeometryLW geometryLW = new PGgeometryLW(testGeometries[i]);
                PGgeography geography = new PGgeography(testGeometries[i]);
                PGgeographyLW geographyLW = new PGgeographyLW(testGeometries[i]);

                preparedStatement.setInt(idColumnIndex, i);
                preparedStatement.setObject(geometryValueColumnIndex, geometry);
                preparedStatement.setObject(geometrylwValueColumnIndex, geometryLW);
                preparedStatement.setObject(geographyValueColumnIndex, geography);
                preparedStatement.setObject(geographylwValueColumnIndex, geographyLW);
                preparedStatement.executeUpdate();
            }
        }

        logger.debug("Querying table with standard connection...");
        try (
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "select _id, geometry_value, geometrylw_value, geography_value, geographylw_value from "
                                + testTableName
                )
        ) {
            while (resultSet.next()) {
                Assert.assertEquals(resultSet.getObject(geometryValueColumnIndex).getClass(), PGgeometry.class);
                Assert.assertEquals(resultSet.getObject(geometrylwValueColumnIndex).getClass(), PGgeometry.class);
                Assert.assertEquals(resultSet.getObject(geographyValueColumnIndex).getClass(), PGgeography.class);
                Assert.assertEquals(resultSet.getObject(geographylwValueColumnIndex).getClass(), PGgeography.class);
            }
        }

        logger.debug("Querying table with lightweight connection...");
        try (
                Statement statement = connectionLW.createStatement();
                ResultSet resultSet = statement.executeQuery(
                        "select _id, geometry_value, geometrylw_value, geography_value, geographylw_value from "
                                + testTableName
                )
        ) {
            while (resultSet.next()) {
                Assert.assertEquals(resultSet.getObject(geometryValueColumnIndex).getClass(), PGgeometryLW.class);
                Assert.assertEquals(resultSet.getObject(geometrylwValueColumnIndex).getClass(), PGgeometryLW.class);
                Assert.assertEquals(resultSet.getObject(geographyValueColumnIndex).getClass(), PGgeographyLW.class);
                Assert.assertEquals(resultSet.getObject(geographylwValueColumnIndex).getClass(), PGgeographyLW.class);
            }
        }
    }


}
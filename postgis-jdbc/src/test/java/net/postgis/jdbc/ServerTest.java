/*
 * ServerTest.java
 * 
 * PostGIS extension for PostgreSQL JDBC driver - example and test classes
 * 
 * (C) 2004 Paul Ramsey, pramsey@refractions.net
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

package net.postgis.jdbc;


import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import net.postgis.jdbc.geometry.GeometryBuilder;
import net.postgis.jdbc.geometry.LineString;
import net.postgis.jdbc.geometry.LinearRing;
import net.postgis.jdbc.geometry.MultiLineString;
import net.postgis.jdbc.geometry.MultiPoint;
import net.postgis.jdbc.geometry.MultiPolygon;
import net.postgis.jdbc.geometry.Point;
import net.postgis.jdbc.geometry.Polygon;
import net.postgis.tools.testutils.TestContainerController;


public class ServerTest {

    private static final Logger logger = LoggerFactory.getLogger(ServerTest.class);

    private static final String JDBC_DRIVER_CLASS_NAME = "org.postgresql.Driver";

    private static final String DATABASE_TABLE_NAME_PREFIX = "jdbc_test";

    private Connection connection = null;

    private Statement statement = null;


    @Test
	public void testServer() throws Exception {
        String dbtable = DATABASE_TABLE_NAME_PREFIX + "_" + UUID.randomUUID().toString().replaceAll("-", "");

		String dropSQL = "drop table " + dbtable;
		String createSQL = "create table " + dbtable + " (geom geometry, id int4)";
		String insertPointSQL = "insert into " + dbtable + " values ('POINT (10 10 10)',1)";
		String insertPolygonSQL = "insert into " + dbtable + " values ('POLYGON ((0 0 0,0 10 0,10 10 0,10 0 0,0 0 0))',2)";

        logger.debug("Adding geometric type entries...");
        ((org.postgresql.PGConnection)connection).addDataType("geometry", PGgeometry.class);
        ((org.postgresql.PGConnection)connection).addDataType("box3d", PGbox3d.class);

        logger.debug("Creating table with geometric types...");
        boolean tableExists = false;
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        try (ResultSet resultSet = databaseMetaData.getTables(null, null, dbtable.toLowerCase(), new String[] {"TABLE"})) {
            while (resultSet.next()) {
                tableExists = true;
            }
        }
        if (tableExists) {
            statement.execute(dropSQL);
        }
        statement.execute(createSQL);

        logger.debug("Inserting point...");
        statement.execute(insertPointSQL);

        logger.debug("Inserting polygon...");
        statement.execute(insertPolygonSQL);

        logger.debug("Querying table...");
        ResultSet resultSet = statement.executeQuery("select ST_AsText(geom),id from " + dbtable);
        while (resultSet.next()) {
            Object obj = resultSet.getObject(1);
            int id = resultSet.getInt(2);
            logger.debug("Row {}: {}", id, obj.toString());
        }

    }

    @Test
    public void testColumnTypeSafetyNonEmpty1() throws SQLException {
        String tableName = "polygraph_" + UUID.randomUUID().toString().replace('-', '_');

        String createSQL = "create table " + tableName + " (point geometry(point,4326), polygon geometry(polygon,4326), line_string geometry(linestring,4326), multi_line_string geometry(multilinestring,4326), multi_point geometry(multipoint,4326), multi_polygon geometry(multipolygon,4326));";
        String dropSQL = "drop table " + tableName;

        statement.execute(createSQL);

        final PreparedStatement prep = connection
            .prepareStatement("INSERT INTO " + tableName + " VALUES (?, ?, ?, ?, ?, ?)");
        prep.setObject(1, new PGgeometryLW(new Point("SRID=4326;POINT(2.8 1.7)")));
        prep.setObject(2, new PGgeometryLW(new Polygon("SRID=4326;POLYGON((2 2, 2 -2, -2 -2, -2 2, 2 2))")));
        prep.setObject(3, new PGgeometryLW(new LineString("SRID=4326;LINESTRING(0 0, 1 2)")));
        prep.setObject(4, new PGgeometryLW(new MultiLineString("SRID=4326;MULTILINESTRING((0 0, 1 2), (1 2, 3 -1))")));
        prep.setObject(5, new PGgeometryLW(new MultiPoint("SRID=4326;MULTIPOINT((2 3), (7 8))")));
        prep.setObject(6, new PGgeometryLW(new MultiPolygon("SRID=4326;MULTIPOLYGON(((1 1, 1 -1, -1 -1, -1 1, 1 1)),((1 1, 3 1, 3 3, 1 3, 1 1)))")));

        prep.execute();
        statement.execute(dropSQL);
    }

    @Test
    public void testColumnTypeSafetyNonEmpty2() throws SQLException {

        String tableName = "polygraph_" + UUID.randomUUID().toString().replace('-', '_');

        String createSQL = "create table " + tableName + " (point geometry(point,4326), polygon geometry(polygon,4326), line_string geometry(linestring,4326), multi_line_string geometry(multilinestring,4326), multi_point geometry(multipoint,4326), multi_polygon geometry(multipolygon,4326));";
        String dropSQL = "drop table " + tableName;

        statement.execute(createSQL);

        final PreparedStatement prep = connection
            .prepareStatement("INSERT INTO " + tableName + " VALUES (?, ?, ?, ?, ?, ?)");

        prep.setObject(1, new PGgeometryLW(GeometryBuilder.geomFromString("SRID=4326;POINT(2.8 1.7)")));
        prep.setObject(2, new PGgeometryLW(GeometryBuilder.geomFromString("SRID=4326;POLYGON((2 2, 2 -2, -2 -2, -2 2, 2 2))")));
        prep.setObject(3, new PGgeometryLW(GeometryBuilder.geomFromString("SRID=4326;LINESTRING(0 0, 1 2)")));
        prep.setObject(4, new PGgeometryLW(GeometryBuilder.geomFromString("SRID=4326;MULTILINESTRING((0 0, 1 2), (1 2, 3 -1))")));
        prep.setObject(5, new PGgeometryLW(GeometryBuilder.geomFromString("SRID=4326;MULTIPOINT((2 3), (7 8))")));
        prep.setObject(6, new PGgeometryLW(
            GeometryBuilder.geomFromString("SRID=4326;MULTIPOLYGON(((1 1, 1 -1, -1 -1, -1 1, 1 1)),((1 1, 3 1, 3 3, 1 3, 1 1)))")
        ));

        prep.execute();
        statement.execute(dropSQL);
    }


    @Test
    public void testColumnTypeSafetyEmpty1() throws SQLException {
        String tableName = "polygraph_" + UUID.randomUUID().toString().replace('-', '_');

        String createSQL = "create table " + tableName + " (point geometry(point,4326), polygon geometry(polygon,4326), line_string geometry(linestring,4326), multi_line_string geometry(multilinestring,4326), multi_point geometry(multipoint,4326), multi_polygon geometry(multipolygon,4326));";
        String dropSQL = "drop table " + tableName;

        statement.execute(createSQL);

        final PreparedStatement prep = connection
            .prepareStatement("INSERT INTO " + tableName + " VALUES (?, ?, ?, ?, ?, ?)");

        prep.setObject(1, new PGgeometryLW(new Point()));
        prep.setObject(2, new PGgeometryLW(new Polygon()));
        prep.setObject(3, new PGgeometryLW(new LineString()));
        prep.setObject(4, new PGgeometryLW(new MultiLineString()));
        prep.setObject(5, new PGgeometryLW(new MultiPoint()));
        prep.setObject(6, new PGgeometryLW(new MultiPolygon()));

        prep.execute();
        statement.execute(dropSQL);
    }


    @Test
    public void testColumnTypeSafetyEmpty2() throws SQLException {

        String tableName = "polygraph_" + UUID.randomUUID().toString().replace('-', '_');

        String createSQL = "create table " + tableName + " (point geometry(point,4326), polygon geometry(polygon,4326), line_string geometry(linestring,4326), multi_line_string geometry(multilinestring,4326), multi_point geometry(multipoint,4326), multi_polygon geometry(multipolygon,4326));";
        String dropSQL = "drop table " + tableName;

        statement.execute(createSQL);

        final PreparedStatement prep = connection
            .prepareStatement("INSERT INTO " + tableName + " VALUES (?, ?, ?, ?, ?, ?)");

        prep.setObject(1, new PGgeometryLW(GeometryBuilder.geomFromString("SRID=4326;POINT EMPTY")));
        prep.setObject(2, new PGgeometryLW(GeometryBuilder.geomFromString("SRID=4326;POLYGON EMPTY")));
        prep.setObject(3, new PGgeometryLW(GeometryBuilder.geomFromString("SRID=4326;LINESTRING EMPTY")));
        prep.setObject(4, new PGgeometryLW(GeometryBuilder.geomFromString("SRID=4326;MULTILINESTRING EMPTY")));
        prep.setObject(5, new PGgeometryLW(GeometryBuilder.geomFromString("SRID=4326;MULTIPOINT EMPTY")));
        prep.setObject(6, new PGgeometryLW(GeometryBuilder.geomFromString("SRID=4326;MULTIPOLYGON EMPTY")));

        prep.execute();
        statement.execute(dropSQL);
    }


    @Test
    public void testColumnTypeSafetyEmpty3() throws SQLException {
        String tableName = "polygraph_" + UUID.randomUUID().toString().replace('-', '_');

        String createSQL = "create table " + tableName + " (point geometry(point,4326), polygon geometry(polygon,4326), line_string geometry(linestring,4326), multi_line_string geometry(multilinestring,4326), multi_point geometry(multipoint,4326), multi_polygon geometry(multipolygon,4326));";
        String dropSQL = "drop table " + tableName;

        statement.execute(createSQL);

        final PreparedStatement prep = connection
            .prepareStatement("INSERT INTO " + tableName + " VALUES (?, ?, ?, ?, ?, ?)");

        final Point point = new Point();
        point.setSrid(4326);
        prep.setObject(1, new PGgeometryLW(point));

        final Polygon polygon = new Polygon(new LinearRing[0]);
        polygon.setSrid(4326);
        prep.setObject(2, new PGgeometryLW(polygon));

        final LineString lineString = new LineString(new Point[0]);
        lineString.setSrid(4326);
        prep.setObject(3, new PGgeometryLW(lineString));

        final MultiLineString multiLineString = new MultiLineString(new LineString[0]);
        multiLineString.setSrid(4326);
        prep.setObject(4, new PGgeometryLW(multiLineString));

        final MultiPoint multiPoint = new MultiPoint(new Point[0]);
        multiPoint.setSrid(4326);
        prep.setObject(5, new PGgeometryLW(multiPoint));

        final MultiPolygon multiPolygon = new MultiPolygon(new Polygon[0]);
        multiPolygon.setSrid(4326);
        prep.setObject(6, new PGgeometryLW(multiPolygon));

        prep.execute();
        statement.execute(dropSQL);
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
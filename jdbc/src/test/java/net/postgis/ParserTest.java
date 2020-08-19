/*
 * ParserTest.java
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

package net.postgis;


import net.postgis.binary.BinaryParser;
import net.postgis.binary.BinaryWriter;
import net.postgis.binary.ValueSetter;
import net.postgis.tools.testutils.TestContainerController;
import net.postgis.util.VersionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Objects;


public class ParserTest {

    private static final Logger logger = LoggerFactory.getLogger(ParserTest.class);

    private static final String DRIVER_WRAPPER_CLASS_NAME = "net.postgis.DriverWrapper";

    private static final String DRIVER_WRAPPER_AUTOPROBE_CLASS_NAME = "net.postgis.DriverWrapperAutoprobe";

    /** The srid we use for the srid tests */
    public static final int SRID = 4326;

    /** The string prefix we get for the srid tests */
    public static final String SRIDPREFIX = "SRID=" + SRID + ";";

    /**
     * Our set of geometries to test.
     */
    public static final String ALL = "ALL";
    public static final String ONLY10 = "ONLY10";
    public static final String EQUAL10 = "EQUAL10";
    public static final String[][] testset = new String[][]{
        {
            ALL, // 2D
            "POINT(10 10)"},
        {
            ALL, // 3D with 3rd coordinate set to 0
            "POINT(10 10 0)"},
        {
            ALL, // 3D
            "POINT(10 10 20)"},
        {
            ALL, // 3D with scientific notation
            "POINT(1e100 1.2345e-100 -2e-5)"},
        {
            ONLY10, // 2D + Measures
            "POINTM(10 10 20)"},
        {
            ONLY10, // 3D + Measures
            "POINT(10 10 20 30)"},
        {
            ALL, // broken format, see http://lists.jump-project.org/pipermail/jts-devel/2006-April/001572.html
            "MULTIPOINT(11 12, 20 20)"},
        {
            ALL,// broken format
            "MULTIPOINT(11 12 13, 20 20 20)"},
        {
            ONLY10,// broken format
            "MULTIPOINTM(11 12 13, 20 20 20)"},
        {
            ONLY10,// broken format
            "MULTIPOINT(11 12 13 14,20 20 20 20)"},
        {
            ALL, // OGC conforming format
            "MULTIPOINT((11 12), (20 20))"},
        {
            ALL,
            "MULTIPOINT((11 12 13), (20 20 20))"},
        {
            ONLY10,
            "MULTIPOINTM((11 12 13), (20 20 20))"},
        {
            ONLY10,
            "MULTIPOINT((11 12 13 14),(20 20 20 20))"},
        {
            ALL,
            "LINESTRING(10 10,20 20,50 50,34 34)"},
        {
            ALL,
            "LINESTRING(10 10 20,20 20 20,50 50 50,34 34 34)"},
        {
            ONLY10,
            "LINESTRINGM(10 10 20,20 20 20,50 50 50,34 34 34)"},
        {
            ONLY10,
            "LINESTRING(10 10 20 20,20 20 20 20,50 50 50 50,34 34 34 50)"},
        {
            ALL,
            "POLYGON((10 10,20 10,20 20,20 10,10 10),(5 5,5 6,6 6,6 5,5 5))"},
        {
            ALL,
            "POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))"},
        {
            ONLY10,
            "POLYGONM((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))"},
        {
            ONLY10,
            "POLYGON((10 10 0 7,20 10 0 7,20 20 0 7,20 10 0 7,10 10 0 7),(5 5 0 7,5 6 0 7,6 6 0 7,6 5 0 7,5 5 0 7))"},
        {
            ALL,
            "MULTIPOLYGON(((10 10,20 10,20 20,20 10,10 10),(5 5,5 6,6 6,6 5,5 5)),((10 10,20 10,20 20,20 10,10 10),(5 5,5 6,6 6,6 5,5 5)))"},
        {
            ALL,
            "MULTIPOLYGON(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))"},
        {
            ONLY10,
            "MULTIPOLYGONM(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))"},
        {
            ONLY10,
            "MULTIPOLYGON(((10 10 0 7,20 10 0 7,20 20 0 7,20 10 0 7,10 10 0 7),(5 5 0 7,5 6 0 7,6 6 0 7,6 5 0 7,5 5 0 7)),((10 10 0 7,20 10 0 7,20 20 0 7,20 10 0 7,10 10 0 7),(5 5 0 7,5 6 0 7,6 6 0 7,6 5 0 7,5 5 0 7)))"},
        {
            ALL,
            "MULTILINESTRING((10 10,20 10,20 20,20 10,10 10),(5 5,5 6,6 6,6 5,5 5))"},
        {
            ALL,
            "MULTILINESTRING((10 10 5,20 10 5,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))"},
        {
            ONLY10,
            "MULTILINESTRINGM((10 10 7,20 10 7,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))"},
        {
            ONLY10,
            "MULTILINESTRING((10 10 0 7,20 10 0 7,20 20 0 7,20 10 0 7,10 10 0 7),(5 5 0 7,5 6 0 7,6 6 0 7,6 5 0 7,5 5 0 7))"},
        {
            ALL,
            "GEOMETRYCOLLECTION(POINT(10 10),POINT(20 20))"},
        {
            ALL,
            "GEOMETRYCOLLECTION(POINT(10 10 20),POINT(20 20 20))"},
        {
            ONLY10,
            "GEOMETRYCOLLECTION(POINT(10 10 20 7),POINT(20 20 20 7))"},
        {
            ALL,
            "GEOMETRYCOLLECTION(LINESTRING(10 10 20,20 20 20, 50 50 50, 34 34 34),LINESTRING(10 10 20,20 20 20, 50 50 50, 34 34 34))"},
        {
            ALL,
            "GEOMETRYCOLLECTION(POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))"},
        {
            ONLY10, // Cannot be parsed by 0.X servers, broken format
            "GEOMETRYCOLLECTION(MULTIPOINT(10 10 10, 20 20 20),MULTIPOINT(10 10 10, 20 20 20))"},
        {
            ONLY10, // Cannot be parsed by 0.X servers, OGC conformant
            "GEOMETRYCOLLECTION(MULTIPOINT((10 10 10), (20 20 20)),MULTIPOINT((10 10 10), (20 20 20)))"},
        {
            EQUAL10, // PostGIs 0.X "flattens" this geometry, so it is not
            // equal after reparsing.
            "GEOMETRYCOLLECTION(MULTILINESTRING((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))"},
        {
            EQUAL10,// PostGIs 0.X "flattens" this geometry, so it is not equal
            // after reparsing.
            "GEOMETRYCOLLECTION(MULTIPOLYGON(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))),MULTIPOLYGON(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))))"},
        {
            ALL,
            "GEOMETRYCOLLECTION(POINT(10 10 20),LINESTRING(10 10 20,20 20 20, 50 50 50, 34 34 34),POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))"},
        {
            ONLY10, // Collections that contain both X and MultiX do not work on
            // PostGIS 0.x, broken format
            "GEOMETRYCOLLECTION(POINT(10 10 20),MULTIPOINT(10 10 10, 20 20 20),LINESTRING(10 10 20,20 20 20, 50 50 50, 34 34 34),POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),MULTIPOLYGON(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))),MULTILINESTRING((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))"},
        {
            ONLY10, // Collections that contain both X and MultiX do not work on
            // PostGIS 0.x, OGC conformant
            "GEOMETRYCOLLECTION(POINT(10 10 20),MULTIPOINT((10 10 10), (20 20 20)),LINESTRING(10 10 20,20 20 20, 50 50 50, 34 34 34),POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),MULTIPOLYGON(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))),MULTILINESTRING((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))"},
        {
            ALL,// new (correct) representation
            "GEOMETRYCOLLECTION EMPTY"},
        {
            ALL,
            "GEOMETRYCOLLECTIONM(POINTM(10 10 20),POINTM(20 20 20))"},
    // end
    };

    public static final String[][] testSetNonWorking = new String[][]{
        {
            ALL, // Old (bad) PostGIS 0.X Representation
            "GEOMETRYCOLLECTION(EMPTY)"},
        {
            ONLY10,// new (correct) representation - does not work on 0.X
            "POINT EMPTY"},
        {
            ONLY10,// new (correct) representation - does not work on 0.X
            "LINESTRING EMPTY"},
        {
            ONLY10,// new (correct) representation - does not work on 0.X
            "POLYGON EMPTY"},
        {
            ONLY10,// new (correct) representation - does not work on 0.X
            "MULTIPOINT EMPTY"},
        {
            ONLY10,// new (correct) representation - does not work on 0.X
            "MULTILINESTRING EMPTY"},
        {
            ONLY10,// new (correct) representation - does not work on 0.X
            "MULTIPOLYGON EMPTY"}
    };

    private static BinaryParser binaryParser = new BinaryParser();

    private static final BinaryWriter binaryWriter = new BinaryWriter();

    private Connection connection = null;

    private Statement statement = null;


    @Test
    public void testParser() throws Exception {
        for (String[] aTestset : testset) {
            test(aTestset[1], aTestset[0]);
            test(SRIDPREFIX + aTestset[1], aTestset[0]);
        }
    }


    public void test(String WKT, String flags) throws SQLException {
        logger.debug("Original: {} ", WKT);
        Geometry geom = GeometryBuilder.geomFromString(WKT);
        String parsed = geom.toString();
        logger.debug("Parsed: {}", parsed);
        Geometry regeom = GeometryBuilder.geomFromString(parsed);
        String reparsed = regeom.toString();
        logger.debug("Re-Parsed: {}", reparsed);
        Assert.assertEquals(geom, regeom, "Geometries are not equal");
        Assert.assertEquals(reparsed, parsed, "Text Reps are not equal");

        String hexNWKT = binaryWriter.writeHexed(regeom, ValueSetter.NDR.NUMBER);
        logger.debug("NDRHex: {}", hexNWKT);
        regeom = GeometryBuilder.geomFromString(hexNWKT);
        logger.debug("ReNDRHex: {}", regeom);
        Assert.assertEquals(geom, regeom, "Geometries are not equal");

        String hexXWKT = binaryWriter.writeHexed(regeom, ValueSetter.XDR.NUMBER);
        logger.debug("XDRHex: {}", hexXWKT);
        regeom = GeometryBuilder.geomFromString(hexXWKT);
        logger.debug("ReXDRHex: {}", regeom);
        Assert.assertEquals(geom, regeom, "Geometries are not equal");

        byte[] NWKT = binaryWriter.writeBinary(regeom, ValueSetter.NDR.NUMBER);
        regeom = binaryParser.parse(NWKT);
        logger.debug("NDR: {}", regeom);
        Assert.assertEquals(geom, regeom, "Geometries are not equal");

        byte[] XWKT = binaryWriter.writeBinary(regeom, ValueSetter.XDR.NUMBER);
        regeom = binaryParser.parse(XWKT);
        logger.debug("XDR: {}", regeom);
        Assert.assertEquals(geom, regeom, "Geometries are not equal");

        final String postGISVersionString = VersionUtil.retrievePostGISServerVersionString(connection);
        logger.debug("postGISVersionString: {}", postGISVersionString);
        final String postGISMajorVersion = VersionUtil.retrievePostGISServerMajorVersion(connection);
        logger.debug("postGISMajorVersion: {}", postGISMajorVersion);
        final String postGISMinorVersion = VersionUtil.retrievePostGISServerMinorVersion(connection);
        logger.debug("postGISMinorVersion: {}", postGISMinorVersion);

        int serverPostgisMajor = 0;
        try {
            serverPostgisMajor = Integer.parseInt(postGISMajorVersion);
        } catch (NumberFormatException nfe) {
            logger.error("Caught a number format exception attempting to parse PostGIS Server Major Version");
        }

        if ((Objects.equals(flags, ONLY10)) && serverPostgisMajor < 1) {
            logger.info("PostGIS server too old, skipping test on database connection {}", connection.getCatalog());
        } else {
            logger.debug("Testing on connection {}", connection.getCatalog());

            Geometry sqlGeom = viaSQL(WKT);
            logger.debug("SQLin: {}", sqlGeom);
            if (!geom.equals(sqlGeom)) {
                logger.warn("Geometries after SQL are not equal");
                if (Objects.equals(flags, EQUAL10) && serverPostgisMajor < 1) {
                    logger.info("This is expected with PostGIS {}.X", serverPostgisMajor);
                } else {
                    Assert.fail();
                }
            }

            Geometry sqlreGeom = viaSQL(parsed);
            logger.debug("SQLout: {}", sqlreGeom);
            if (!geom.equals(sqlreGeom)) {
                logger.warn("Reparsed Geometries after SQL are not equal!");
                if (Objects.equals(flags, EQUAL10) && serverPostgisMajor < 1) {
                    logger.info("This is expected with PostGIS {}.X", serverPostgisMajor);
                } else {
                    Assert.fail();
                }
            }

            sqlreGeom = viaPrepSQL(geom, connection);
            logger.debug("Prepared: {}", sqlreGeom.toString());
            if (!geom.equals(sqlreGeom)) {
                logger.warn("Reparsed Geometries after prepared StatementSQL are not equal!");
                if (Objects.equals(flags, EQUAL10) && serverPostgisMajor < 1) {
                    logger.info("This is expected with PostGIS {}.X", serverPostgisMajor);
                } else {
                    Assert.fail();
                }
            }

                /*
                // Temporarily removing this check since it breaks between PostGIS v2.4.4 and PostGIS v2.5.0
                // Tests performed via psql between mdillon/postgis:9.3 and mdillon/postgis:9.4 shows the breakage
                // Test is also broken in mdillon/postgis:11-alpine
                // In psql, the sql statement "SELECT ST_AsEWKT(geometry_in('POINT(1e100 1.2345e-100 -2e-5)'));" returns
                // "POINT(1e+100 1.2345e-100 -2e-05)" with 9.3 and "POINT(1e+100 0 -0.00002)" with 9.4 and later

                // asEWKT() function is not present on PostGIS 0.X, and the test
                // is pointless as 0.X uses EWKT as canonical rep so the same
                // functionality was already tested above.
                if (serverPostgisMajor >= 1) {
                    sqlGeom = ewktViaSQL(WKT, statement);
                    logger.debug("asEWKT: {}", sqlGeom);
                    Assert.assertEquals(geom, sqlGeom);
                }
                */

            // asEWKB() function is not present on PostGIS 0.X.
            if (serverPostgisMajor >= 1) {
                sqlGeom = ewkbViaSQL(WKT, statement);
                logger.debug("asEWKB: {}", sqlGeom);
                Assert.assertEquals(geom, sqlGeom);
            }

            // HexEWKB parsing is not present on PostGIS 0.X.
            if (serverPostgisMajor >= 1) {
                sqlGeom = viaSQL(hexNWKT);
                logger.debug("hexNWKT: {}", sqlGeom);
                Assert.assertEquals(geom, sqlGeom);
            }

            if (serverPostgisMajor >= 1) {
                sqlGeom = viaSQL(hexXWKT);
                logger.debug("hexXWKT: {}", sqlGeom);
                Assert.assertEquals(geom, sqlGeom);
            }

            // Canonical binary input is not present before 1.0
            if (serverPostgisMajor >= 1) {
                sqlGeom = binaryViaSQL(NWKT, connection);
                logger.debug("NWKT: {}", sqlGeom);
                Assert.assertEquals(geom, sqlGeom);
            }

            if (serverPostgisMajor >= 1) {
                sqlGeom = binaryViaSQL(XWKT, connection);
                logger.debug("XWKT: {}", sqlGeom);
                Assert.assertEquals(geom, sqlGeom);
            }
        }
    }


    /** Pass a geometry representation through the SQL server */
    private Geometry viaSQL(String rep) throws SQLException {
        logger.trace("Geometry viaSQL(String rep)");
        logger.trace("[P] rep => {}", rep);
        ResultSet resultSet = statement.executeQuery("SELECT geometry_in('" + rep + "')");
        resultSet.next();
        return ((PGgeometry) resultSet.getObject(1)).getGeometry();
    }


    /**
     * Pass a geometry representation through the SQL server via prepared
     * statement
     */
    private static Geometry viaPrepSQL(Geometry geom, Connection conn) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement("SELECT ?::geometry");
        PGgeometry wrapper = new PGgeometry(geom);
        preparedStatement.setObject(1, wrapper, Types.OTHER);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        PGgeometry resultwrapper = (PGgeometry)resultSet.getObject(1);
        return resultwrapper.getGeometry();
    }


    /** Pass a geometry representation through the SQL server via EWKT */
    private static Geometry ewktViaSQL(String rep, Statement stat) throws SQLException {
        ResultSet resultSet = stat.executeQuery("SELECT ST_AsEWKT(geometry_in('" + rep + "'))");
        resultSet.next();
        String resrep = resultSet.getString(1);
        return GeometryBuilder.geomFromString(resrep);
    }


    /** Pass a geometry representation through the SQL server via EWKB */
    private static Geometry ewkbViaSQL(String rep, Statement stat) throws SQLException {
        ResultSet resultSet = stat.executeQuery("SELECT ST_AsEWKB(geometry_in('" + rep + "'))");
        resultSet.next();
        byte[] resrep = resultSet.getBytes(1);
        return binaryParser.parse(resrep);
    }


    /** Pass a EWKB geometry representation through the server */
    private static Geometry binaryViaSQL(byte[] rep, Connection conn) throws SQLException {
        PreparedStatement preparedStatement = conn.prepareStatement("SELECT ?::bytea::geometry");
        preparedStatement.setBytes(1, rep);
        ResultSet resultSet = preparedStatement.executeQuery();
        resultSet.next();
        PGgeometry resultwrapper = ((PGgeometry) resultSet.getObject(1));
        return resultwrapper.getGeometry();
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
        Class.forName(DRIVER_WRAPPER_CLASS_NAME);
        Class.forName(DRIVER_WRAPPER_AUTOPROBE_CLASS_NAME);
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
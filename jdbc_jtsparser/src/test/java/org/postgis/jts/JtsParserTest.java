/*
 * JtsParserTest.java
 *
 * JtsParserTest for JTS - relies on org.postgis V1.0.0+ package.
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

package org.postgis.jts;


import org.postgis.binary.ValueSetter;

import org.locationtech.jts.geom.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.sql.*;


/**
 * JtsParseTest
 *
 * This test class was adapted from the {@code JtsTestParsr} example standalone class.
 * It is meant to be run in standalone mode or run against a PostGIS database, but it will need to be
 * fixed to run against a PostGIS database as it currently fails in some places with an error:
 * {@literal function asewkb(geometry) does not exist}
 */
public class JtsParserTest {

    private static final Logger logger = LoggerFactory.getLogger(JtsParserTest.class);

    private static final String JTS_WRAPPER_CLASS_NAME = "org.postgis.jts.JtsWrapper";

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
    public static final String[][] testset = new String[][] {
            { ALL, // 2D
                    "POINT(10 10)" },
            { ALL, // 3D with 3rd coordinate set to 0
                    "POINT(10 10 0)" },
            { ALL, // 3D
                    "POINT(10 10 20)" },
            { ALL, "MULTIPOINT(11 12, 20 20)" },
            { ALL, "MULTIPOINT(11 12 13, 20 20 20)" },
            { ALL, "LINESTRING(10 10,20 20,50 50,34 34)" },
            { ALL, "LINESTRING(10 10 20,20 20 20,50 50 50,34 34 34)" },
            { ALL, "POLYGON((10 10,20 10,20 20,20 10,10 10),(5 5,5 6,6 6,6 5,5 5))" },
            { ALL, "POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))" },
            {
                    ALL,
                    "MULTIPOLYGON(((10 10,20 10,20 20,20 10,10 10),(5 5,5 6,6 6,6 5,5 5)),((10 10,20 10,20 20,20 10,10 10),(5 5,5 6,6 6,6 5,5 5)))" },
            {
                    ALL,
                    "MULTIPOLYGON(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))" },
            { ALL, "MULTILINESTRING((10 10,20 10,20 20,20 10,10 10),(5 5,5 6,6 6,6 5,5 5))" },
            { ALL, "MULTILINESTRING((10 10 5,20 10 5,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))" },
            { ALL, "GEOMETRYCOLLECTION(POINT(10 10),POINT(20 20))" },
            { ALL, "GEOMETRYCOLLECTION(POINT(10 10 20),POINT(20 20 20))" },
            {
                    ALL,
                    "GEOMETRYCOLLECTION(LINESTRING(10 10 20,20 20 20, 50 50 50, 34 34 34),LINESTRING(10 10 20,20 20 20, 50 50 50, 34 34 34))" },
            {
                    ALL,
                    "GEOMETRYCOLLECTION(POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))" },
            { ONLY10, // Cannot be parsed by 0.X servers
                    "GEOMETRYCOLLECTION(MULTIPOINT(10 10 10, 20 20 20),MULTIPOINT(10 10 10, 20 20 20))" },
            { EQUAL10, // PostGIs 0.X "flattens" this geometry, so it is not
                    // equal after reparsing.
                    "GEOMETRYCOLLECTION(MULTILINESTRING((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))" },
            { EQUAL10,// PostGIs 0.X "flattens" this geometry, so it is not
                    // equal
                    // after reparsing.
                    "GEOMETRYCOLLECTION(MULTIPOLYGON(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))),MULTIPOLYGON(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))))" },
            {
                    ALL,
                    "GEOMETRYCOLLECTION(POINT(10 10 20),LINESTRING(10 10 20,20 20 20, 50 50 50, 34 34 34),POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))" },
            { ONLY10, // Collections that contain both X and MultiX do not
                    // work on
                    // PostGIS 0.x
                    "GEOMETRYCOLLECTION(POINT(10 10 20),MULTIPOINT(10 10 10, 20 20 20),LINESTRING(10 10 20,20 20 20, 50 50 50, 34 34 34),POLYGON((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),MULTIPOLYGON(((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))),MULTILINESTRING((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))" },
            { ALL,// new (correct) representation
                    "GEOMETRYCOLLECTION EMPTY" },
    };

    private static JtsBinaryParser jtsBinaryParser = new JtsBinaryParser();

    private static final JtsBinaryWriter jtsBinaryWriter = new JtsBinaryWriter();

    private boolean testWithDatabase = false;

    private Connection connection = null;

    private Statement statement = null;


    @Test
    public void test() throws Exception {
        for (String[] aTestset : testset) {
            test(aTestset[1], aTestset[0]);
            test(SRIDPREFIX + aTestset[1], aTestset[0]);
        }
    }


    public void test(String WKT, String flags) throws SQLException {
        logger.debug("Original: {}", WKT);
        Geometry geom = JtsGeometry.geomFromString(WKT);
        String parsed = geom.toString();
        if (WKT.startsWith("SRID=")) {
            parsed = "SRID=" + geom.getSRID() + ";" + parsed;
        }
        logger.debug("Parsed: {}", parsed);
        Geometry regeom = JtsGeometry.geomFromString(parsed);
        String reparsed = regeom.toString();
        if (WKT.startsWith("SRID=")) {
            reparsed = "SRID=" + geom.getSRID() + ";" + reparsed;
        }
        logger.debug("Re-Parsed: {}", reparsed);
        Assert.assertEquals(geom, regeom, "Geometries are not equal");
        Assert.assertEquals(geom.getSRID(), regeom.getSRID(), "Geometry SRIDs are not equal");
        Assert.assertEquals(reparsed, parsed, "Text Reps are not equal");

        String hexNWKT = jtsBinaryWriter.writeHexed(geom, ValueSetter.NDR.NUMBER);
        logger.debug("NDRHex: {}", hexNWKT);
        regeom = JtsGeometry.geomFromString(hexNWKT);
        logger.debug("ReNDRHex: {}", regeom);
        Assert.assertEquals(geom, regeom, "Geometries are not equal");

        String hexXWKT = jtsBinaryWriter.writeHexed(geom, ValueSetter.XDR.NUMBER);
        logger.debug("XDRHex: {}", hexXWKT);
        regeom = JtsGeometry.geomFromString(hexXWKT);
        logger.debug("ReXDRHex: {}", regeom);
        Assert.assertEquals(geom, regeom, "Geometries are not equal");

        byte[] NWKT = jtsBinaryWriter.writeBinary(geom, ValueSetter.NDR.NUMBER);
        regeom = jtsBinaryParser.parse(NWKT);
        logger.debug("NDR: {}", regeom);
        Assert.assertEquals(geom, regeom, "Geometries are not equal");

        byte[] XWKT = jtsBinaryWriter.writeBinary(geom, ValueSetter.XDR.NUMBER);
        regeom = jtsBinaryParser.parse(XWKT);
        logger.debug("XDR: {}", regeom);
        Assert.assertEquals(geom, regeom, "Geometries are not equal");

        Geometry coordArrayGeom = rebuildCS(geom);
        logger.debug("CoordArray: {}", regeom);
        Assert.assertEquals(geom, coordArrayGeom, "Geometries are not equal");

        String coordArrayWKT = jtsBinaryWriter.writeHexed(coordArrayGeom, ValueSetter.NDR.NUMBER);
        logger.debug("HexCArray: {}", coordArrayWKT);
        Assert.assertEquals(coordArrayWKT, hexNWKT, "CoordArray HexWKT is not equal");

        if (testWithDatabase) {
            int serverPostgisMajor = getPostgisMajor();

            if ((flags.equals(ONLY10)) && serverPostgisMajor < 1) {
                logger.info("PostGIS server too old, skipping test on connection {}", connection.getCatalog());
            } else {
                logger.debug("Testing on connection {}", connection.getCatalog());

                Geometry sqlGeom = viaSQL(WKT, statement);
                logger.debug("SQLin: {}", sqlGeom.toString());
                if (!geom.equalsExact(sqlGeom)) {
                    logger.warn("Geometries after SQL are not equal");
                    if (flags.equals(EQUAL10) && serverPostgisMajor < 1) {
                        logger.info("This is expected with PostGIS {}.X", serverPostgisMajor);
                    } else {
                        Assert.fail();
                    }
                }

                Geometry sqlreGeom = viaSQL(parsed, statement);
                logger.debug("SQLout: {}", sqlreGeom);
                if (!geom.equalsExact(sqlreGeom)) {
                    logger.warn("Reparsed Geometries after SQL are not equal");
                    if (flags.equals(EQUAL10) && serverPostgisMajor < 1) {
                        logger.info("This is expected with PostGIS {}.X", serverPostgisMajor);
                    } else {
                        Assert.fail();
                    }
                }

                sqlreGeom = viaPrepSQL(geom, connection);
                logger.debug("Prepared: {}", sqlreGeom);
                if (!geom.equalsExact(sqlreGeom)) {
                    logger.debug("Reparsed Geometries after prepared StatementSQL are not equal");
                    if (flags.equals(EQUAL10) && serverPostgisMajor < 1) {
                        logger.info("This is expected with PostGIS {}.X", serverPostgisMajor);
                    } else {
                        Assert.fail();
                    }
                }

                // asEWKT() function is not present on PostGIS 0.X, and the test
                // is pointless as 0.X uses EWKT as canonical rep so the same
                // functionality was already tested above.
                if (serverPostgisMajor >= 1) {
                    sqlGeom = ewktViaSQL(WKT, statement);
                    logger.debug("asEWKT: {}", sqlGeom);
                    Assert.assertEquals(geom, sqlGeom);

                    sqlGeom = ewkbViaSQL(WKT, statement);
                    logger.debug("asEWKB: {}", sqlGeom);
                    Assert.assertEquals(geom, sqlGeom);

                    sqlGeom = viaSQL(hexNWKT, statement);
                    logger.debug("hexNWKT: {}", sqlGeom);
                    Assert.assertEquals(geom, sqlGeom);

                    sqlGeom = viaSQL(hexXWKT, statement);
                    logger.debug("hexXWKT: {}", sqlGeom);
                    Assert.assertEquals(geom, sqlGeom);

                    sqlGeom = binaryViaSQL(NWKT, connection);
                    logger.debug("NWKT: {}", sqlGeom);
                    Assert.assertEquals(geom, sqlGeom);

                    sqlGeom = binaryViaSQL(XWKT, connection);
                    logger.debug("XWKT: {}", sqlGeom);
                    Assert.assertEquals(geom, sqlGeom);
                }
            }
        }
    }


    /** Pass a geometry representation through the SQL server */
    private static Geometry viaSQL(String rep, Statement stat) throws SQLException {
        ResultSet rs = stat.executeQuery("SELECT geometry_in('" + rep + "')");
        rs.next();
        return ((JtsGeometry) rs.getObject(1)).getGeometry();
    }


    /**
     * Pass a geometry representation through the SQL server via prepared
     * statement
     */
    private static Geometry viaPrepSQL(Geometry geom, Connection conn) throws SQLException {
        PreparedStatement prep = conn.prepareStatement("SELECT ?::geometry");
        JtsGeometry wrapper = new JtsGeometry(geom);
        prep.setObject(1, wrapper, Types.OTHER);
        ResultSet rs = prep.executeQuery();
        rs.next();
        JtsGeometry resultwrapper = ((JtsGeometry) rs.getObject(1));
        return resultwrapper.getGeometry();
    }


    /** Pass a geometry representation through the SQL server via EWKT */
    private static Geometry ewktViaSQL(String rep, Statement stat) throws SQLException {
        ResultSet rs = stat.executeQuery("SELECT ST_AsEWKT(geometry_in('" + rep + "'))");
        rs.next();
        String resrep = rs.getString(1);
        return JtsGeometry.geomFromString(resrep);
    }


    /** Pass a geometry representation through the SQL server via EWKB */
    private static Geometry ewkbViaSQL(String rep, Statement stat) throws SQLException {
        ResultSet rs = stat.executeQuery("SELECT ST_AsEWKB(geometry_in('" + rep + "'))");
        rs.next();
        byte[] resrep = rs.getBytes(1);
        return jtsBinaryParser.parse(resrep);
    }


    /** Pass a EWKB geometry representation through the server */
    private static Geometry binaryViaSQL(byte[] rep, Connection conn) throws SQLException {
        PreparedStatement prep = conn.prepareStatement("SELECT ?::bytea::geometry");
        prep.setBytes(1, rep);
        ResultSet rs = prep.executeQuery();
        rs.next();
        JtsGeometry resultwrapper = ((JtsGeometry) rs.getObject(1));
        return resultwrapper.getGeometry();
    }


    // Rebuild given Geometry with a CoordinateArraySequence implementation.
    public static Geometry rebuildCS(Geometry geom) {
        if (geom instanceof Point) {
            return rebuildCSPoint((Point)geom);
        } else if (geom instanceof MultiPoint) {
            return rebuildCSMP((MultiPoint)geom);
        } else if (geom instanceof LineString) {
            return rebuildCSLS((LineString)geom);
        } else if (geom instanceof MultiLineString) {
            return rebuildCSMLS((MultiLineString)geom);
        } else if (geom instanceof Polygon) {
            return rebuildCSP((Polygon)geom);
        } else if (geom instanceof MultiPolygon) {
            return rebuildCSMP((MultiPolygon)geom);
        } else if (geom instanceof GeometryCollection) {
            return rebuildCSGC((GeometryCollection)geom);
        } else {
            throw new AssertionError();
        }
    }


    private static Point rebuildCSPoint(Point point) {
        Point result = point.getFactory().createPoint(point.getCoordinate());
        result.setSRID(point.getSRID());
        return result;
    }


    private static MultiPoint rebuildCSMP(MultiPoint mp) {
        Point[] points = new Point[mp.getNumGeometries()];
        for (int i=0; i < points.length; i++) {
            points[i] = rebuildCSPoint((Point) mp.getGeometryN(i));
        }
        MultiPoint result = mp.getFactory().createMultiPoint(points);
        result.setSRID(mp.getSRID());
        return result;
    }


    private static MultiPolygon rebuildCSMP(MultiPolygon multipoly) {
        Polygon[] polygons = new Polygon[multipoly.getNumGeometries()];
        for (int i=0; i < polygons.length; i++) {
            polygons[i] = rebuildCSP((Polygon)multipoly.getGeometryN(i));
        }
        MultiPolygon result = multipoly.getFactory().createMultiPolygon(polygons);
        result.setSRID(multipoly.getSRID());
        return result;
    }


    private static LineString rebuildCSLS(LineString line) {
        LineString result = line.getFactory().createLineString(line.getCoordinates());
        result.setSRID(line.getSRID());
        return result;
    }


    private static MultiLineString rebuildCSMLS(MultiLineString multiline) {
        LineString[] polygons = new LineString[multiline.getNumGeometries()];
        for (int i=0; i < polygons.length; i++) {
            polygons[i] = rebuildCSLS((LineString)multiline.getGeometryN(i));
        }
        MultiLineString result = multiline.getFactory().createMultiLineString(polygons);
        result.setSRID(multiline.getSRID());
        return result;

    }


    private static Polygon rebuildCSP(Polygon polygon) {
        LinearRing outer = rebuildLR(polygon.getExteriorRing());
        LinearRing[] holes = new LinearRing[polygon.getNumInteriorRing()];
        for (int i=0; i < holes.length; i++) {
            holes[i] = rebuildLR(polygon.getInteriorRingN(i));
        }
        Polygon result = polygon.getFactory().createPolygon(outer, holes);
        result.setSRID(polygon.getSRID());
        return result;
    }


    private static LinearRing rebuildLR(LineString ring) {
        LinearRing result = ring.getFactory().createLinearRing(ring.getCoordinates());
        result.setSRID(ring.getSRID());
        return result;
    }


    private static Geometry rebuildCSGC(GeometryCollection coll) {
        Geometry[] geoms = new Geometry[coll.getNumGeometries()];
        for (int i = 0; i < coll.getNumGeometries(); i++) {
            geoms[i] = rebuildCS(coll.getGeometryN(i));
        }
        Geometry result = coll.getFactory().createGeometryCollection(geoms);
        result.setSRID(coll.getSRID());
        return result;
    }


    public int getPostgisMajor() throws SQLException {
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
    @Parameters({"testWithDatabaseSystemProperty", "jdbcUrlSystemProperty", "jdbcUsernameSystemProperty", "jdbcPasswordSystemProperty"})
    public void initJdbcConnection(String testWithDatabaseSystemProperty,
                                   String jdbcUrlSystemProperty,
                                   String jdbcUsernameSystemProperty,
                                   String jdbcPasswordSystemProperty) throws Exception {
        logger.debug("testWithDatabaseSystemProperty: {}", testWithDatabaseSystemProperty);
        logger.debug("jdbcUrlSystemProperty: {}", jdbcUrlSystemProperty);
        logger.debug("jdbcUsernameSystemProperty: {}", jdbcUsernameSystemProperty);
        logger.debug("jdbcPasswordSystemProperty: {}", jdbcPasswordSystemProperty);

        testWithDatabase = Boolean.parseBoolean(System.getProperty(testWithDatabaseSystemProperty));
        String jdbcUrl = System.getProperty(jdbcUrlSystemProperty);
        String jdbcUsername = System.getProperty(jdbcUsernameSystemProperty);
        String jdbcPassword = System.getProperty(jdbcPasswordSystemProperty);

        logger.debug("testWithDatabase: {}", testWithDatabase);
        logger.debug("jdbcUrl: {}", jdbcUrl);
        logger.debug("jdbcUsername: {}", jdbcUsername);
        logger.debug("jdbcPassword: {}", jdbcPassword);

        if (testWithDatabase) {
            Class.forName(JTS_WRAPPER_CLASS_NAME);
            connection = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
            statement = connection.createStatement();
        } else {
            logger.info("testWithDatabase value was false.  Database tests will be skipped.");
        }
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
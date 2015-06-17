package org.postgis.jts;


import org.postgis.binary.ValueSetter;

import com.vividsolutions.jts.geom.*;
import examples.TestAutoregister;
import org.postgresql.util.PGtokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.sql.*;


/**
 * JtsParseTest
 *
 * This test class was adapted from the {@code JtsTestParsr} example standalone class.
 * It is meant to be run in standalone mode or run against a postgis database, but it will need to be
 * fixed to run against a postgis database as it currently fails in some places with an error:
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

    /** How many tests failed? */
    public static int failcount = 0;


    @Test
    public void test() throws Exception {
        logger.trace("test()");
        loadDrivers();

        // hardcoding this to only do offline testing for now.
        String[] args = new String[] { "offline" };
        //String[] args = new String[] { "jdbc:postgres_jts://<hostname>:<port>/<dbName>", "<dbUsername>", "<dbPassword>" };

        PGtokenizer dburls = null;
        String dbuser = null;
        String dbpass = null;

        if (args.length == 1 && args[0].equalsIgnoreCase("offline")) {
            logger.debug("Performing only offline tests");
            dburls = new PGtokenizer("", ';');
        } else if (args.length == 3) {
            logger.debug("Performing offline and online tests");
            dburls = new PGtokenizer(args[0], ';');
            dbuser = args[1];
            dbpass = args[2];
        }

        if (dburls != null) {
            Connection[] conns;
            conns = new Connection[dburls.getSize()];
            for (int i = 0; i < dburls.getSize(); i++) {
                logger.debug("Creating JDBC connection to {}", dburls.getToken(i));
                conns[i] = connect(dburls.getToken(i), dbuser, dbpass);
            }

            logger.debug("Performing tests...");
            for (String[] aTestset : testset) {
                test(aTestset[1], conns, aTestset[0]);
                test(SRIDPREFIX + aTestset[1], conns, aTestset[0]);
            }

            logger.debug("cleaning up...");
            for (Connection conn : conns) {
                conn.close();
            }

            Assert.assertEquals(failcount, 0);
        } else {
            logger.error("args array should be populated with 1 or 3 elements");
            logger.info("1 element variant: [0] = \"offline\"");
            logger.info("3 element variant: [0] = \"dburls\" \"username\" \"password\"");
            logger.info("dburls has one or more jdbc urls separated by ; in the following format");
            logger.info("jdbc:postgresql://HOST:PORT/DATABASENAME");
        }
    }


    /** The actual test method */
    public static void test(String WKT, Connection[] conns, String flags) throws SQLException {
        logger.debug("Original:  " + WKT);
        Geometry geom = JtsGeometry.geomFromString(WKT);
        String parsed = geom.toString();
        if (WKT.startsWith("SRID=")) {
            parsed = "SRID="+geom.getSRID()+";"+parsed;
        }
        logger.debug("Parsed:    " + parsed);
        Geometry regeom = JtsGeometry.geomFromString(parsed);
        String reparsed = regeom.toString();
        if (WKT.startsWith("SRID=")) {
            reparsed = "SRID="+geom.getSRID()+";"+reparsed;
        }
        logger.debug("Re-Parsed: " + reparsed);
        if (!geom.equalsExact(regeom)) {
            logger.error("--- Geometries are not equal!");
            failcount++;
        } else if (geom.getSRID() != regeom.getSRID()) {
            logger.error("--- Geometriy SRIDs are not equal!");
            failcount++;
        } else if (!reparsed.equals(parsed)) {
            logger.error("--- Text Reps are not equal!");
            failcount++;
        } else {
            logger.debug("Equals:    yes");
        }

        String hexNWKT = jtsBinaryWriter.writeHexed(geom, ValueSetter.NDR.NUMBER);
        logger.debug("NDRHex:    " + hexNWKT);
        regeom = JtsGeometry.geomFromString(hexNWKT);
        logger.debug("ReNDRHex:  " + regeom.toString());
        if (!geom.equalsExact(regeom)) {
            logger.error("--- Geometries are not equal!");
            failcount++;
        } else {
            logger.debug("Equals:    yes");
        }

        String hexXWKT = jtsBinaryWriter.writeHexed(geom, ValueSetter.XDR.NUMBER);
        logger.debug("XDRHex:    " + hexXWKT);
        regeom = JtsGeometry.geomFromString(hexXWKT);
        logger.debug("ReXDRHex:  " + regeom.toString());
        if (!geom.equalsExact(regeom)) {
            logger.error("--- Geometries are not equal!");
            failcount++;
        } else {
            logger.debug("Equals:    yes");
        }

        byte[] NWKT = jtsBinaryWriter.writeBinary(geom, ValueSetter.NDR.NUMBER);
        regeom = jtsBinaryParser.parse(NWKT);
        logger.debug("NDR:       " + regeom.toString());
        if (!geom.equalsExact(regeom)) {
            logger.error("--- Geometries are not equal!");
            failcount++;
        } else {
            logger.debug("Equals:    yes");
        }

        byte[] XWKT = jtsBinaryWriter.writeBinary(geom, ValueSetter.XDR.NUMBER);
        regeom = jtsBinaryParser.parse(XWKT);
        logger.debug("XDR:       " + regeom.toString());
        if (!geom.equalsExact(regeom)) {
            logger.error("--- Geometries are not equal!");
            failcount++;
        } else {
            logger.debug("Equals:    yes");
        }

        Geometry coordArrayGeom = rebuildCS(geom);
        logger.debug("CoordArray:" + regeom.toString());
        if (!geom.equalsExact(coordArrayGeom)) {
            logger.error("--- Geometries are not equal!");
            failcount++;
        } else {
            logger.debug("Equals:    yes");
        }

        String coordArrayWKT = jtsBinaryWriter.writeHexed(coordArrayGeom, ValueSetter.NDR.NUMBER);
        logger.debug("HexCArray: " + coordArrayWKT);
        if (!coordArrayWKT.equals(hexNWKT)) {
            logger.error("--- CoordArray HexWKT is not equal: " + jtsBinaryParser.parse(coordArrayWKT));
            failcount++;
        } else {
            logger.debug("HexEquals: yes");
        }

        for (int i = 0; i < conns.length; i++) {
            Connection connection = conns[i];
            Statement statement = connection.createStatement();
            int serverPostgisMajor = TestAutoregister.getPostgisMajor(statement);

            if ((flags.equals(ONLY10)) && serverPostgisMajor < 1) {
                logger.info("PostGIS server too old, skipping test on connection " + i + ": "
                        + connection.getCatalog());
            } else {
                logger.debug("Testing on connection " + i + ": " + connection.getCatalog());
                try {
                    Geometry sqlGeom = viaSQL(WKT, statement);
                    logger.debug("SQLin    : " + sqlGeom.toString());
                    if (!geom.equalsExact(sqlGeom)) {
                        logger.debug("--- Geometries after SQL are not equal!");
                        if (flags.equals(EQUAL10) && serverPostgisMajor < 1) {
                            logger.debug("--- This is expected with PostGIS " + serverPostgisMajor + ".X");
                        } else {
                            failcount++;
                        }
                    } else {
                        logger.debug("Eq SQL in: yes");
                    }
                } catch (SQLException e) {
                    logger.error("--- Server side error: " + e.toString());
                    failcount++;
                }

                try {
                    Geometry sqlreGeom = viaSQL(parsed, statement);
                    logger.debug("SQLout  :  " + sqlreGeom.toString());
                    if (!geom.equalsExact(sqlreGeom)) {
                        logger.debug("--- reparsed Geometries after SQL are not equal!");
                        if (flags.equals(EQUAL10) && serverPostgisMajor < 1) {
                            logger.debug("--- This is expected with PostGIS " + serverPostgisMajor + ".X");
                        } else {
                            failcount++;
                        }
                    } else {
                        logger.debug("Eq SQLout: yes");
                    }
                } catch (SQLException e) {
                    logger.error("--- Server side error: " + e.toString());
                    failcount++;
                }

                try {
                    Geometry sqlreGeom = viaPrepSQL(geom, connection);
                    logger.debug("Prepared:  " + sqlreGeom.toString());
                    if (!geom.equalsExact(sqlreGeom)) {
                        logger.debug("--- reparsed Geometries after prepared StatementSQL are not equal!");
                        if (flags.equals(EQUAL10) && serverPostgisMajor < 1) {
                            logger.debug("--- This is expected with PostGIS " + serverPostgisMajor + ".X");
                        } else {
                            failcount++;
                        }
                    } else {
                        logger.debug("Eq Prep: yes");
                    }
                } catch (SQLException e) {
                    logger.error("--- Server side error: " + e.toString());
                    failcount++;
                }

                // asEWKT() function is not present on PostGIS 0.X, and the test
                // is pointless as 0.X uses EWKT as canonical rep so the same
                // functionality was already tested above.
                try {
                    if (serverPostgisMajor >= 1) {
                        Geometry sqlGeom = ewktViaSQL(WKT, statement);
                        logger.debug("asEWKT   : " + sqlGeom.toString());
                        if (!geom.equalsExact(sqlGeom)) {
                            logger.error("--- Geometries after EWKT SQL are not equal!");
                            failcount++;
                        } else {
                            logger.debug("equal   : yes");
                        }
                    }
                } catch (SQLException e) {
                    logger.error("--- Server side error: " + e.toString());
                    failcount++;
                }

                // asEWKB() function is not present on PostGIS 0.X.
                try {
                    if (serverPostgisMajor >= 1) {
                        Geometry sqlGeom = ewkbViaSQL(WKT, statement);
                        logger.debug("asEWKB   : " + sqlGeom.toString());
                        if (!geom.equalsExact(sqlGeom)) {
                            logger.error("--- Geometries after EWKB SQL are not equal!");
                            failcount++;
                        } else {
                            logger.debug("equal    : yes");
                        }
                    }
                } catch (SQLException e) {
                    logger.error("--- Server side error: " + e.toString());
                    failcount++;
                }

                // HexEWKB parsing is not present on PostGIS 0.X.
                try {
                    if (serverPostgisMajor >= 1) {
                        Geometry sqlGeom = viaSQL(hexNWKT, statement);
                        logger.debug("hexNWKT:   " + sqlGeom.toString());
                        if (!geom.equalsExact(sqlGeom)) {
                            logger.error("--- Geometries after EWKB SQL are not equal!");
                            failcount++;
                        } else {
                            logger.debug("equal    : yes");
                        }
                    }
                } catch (SQLException e) {
                    logger.error("--- Server side error: " + e.toString());
                    failcount++;
                }
                try {
                    if (serverPostgisMajor >= 1) {
                        Geometry sqlGeom = viaSQL(hexXWKT, statement);
                        logger.debug("hexXWKT:   " + sqlGeom.toString());
                        if (!geom.equalsExact(sqlGeom)) {
                            logger.error("--- Geometries after EWKB SQL are not equal!");
                            failcount++;
                        } else {
                            logger.debug("equal    : yes");
                        }
                    }
                } catch (SQLException e) {
                    logger.error("--- Server side error: " + e.toString());
                    failcount++;
                }

                // Canonical binary input is not present before 1.0
                try {
                    if (serverPostgisMajor >= 1) {
                        Geometry sqlGeom = binaryViaSQL(NWKT, connection);
                        logger.debug("NWKT:      " + sqlGeom.toString());
                        if (!geom.equalsExact(sqlGeom)) {
                            logger.error("--- Geometries after EWKB SQL are not equal!");
                            failcount++;
                        } else {
                            logger.debug("equal    : yes");
                        }
                    }
                } catch (SQLException e) {
                    logger.error("--- Server side error: " + e.toString());
                    failcount++;
                }
                try {
                    if (serverPostgisMajor >= 1) {
                        Geometry sqlGeom = binaryViaSQL(XWKT, connection);
                        logger.debug("XWKT:      " + sqlGeom.toString());
                        if (!geom.equalsExact(sqlGeom)) {
                            logger.error("--- Geometries after EWKB SQL are not equal!");
                            failcount++;
                        } else {
                            logger.debug("equal    : yes");
                        }
                    }
                } catch (SQLException e) {
                    logger.error("--- Server side error: " + e.toString());
                    failcount++;
                }

            }
            statement.close();
        }
        logger.debug("***");
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
        ResultSet rs = stat.executeQuery("SELECT asEWKT(geometry_in('" + rep + "'))");
        rs.next();
        String resrep = rs.getString(1);
        return JtsGeometry.geomFromString(resrep);
    }


    /** Pass a geometry representation through the SQL server via EWKB */
    private static Geometry ewkbViaSQL(String rep, Statement stat) throws SQLException {
        ResultSet rs = stat.executeQuery("SELECT asEWKB(geometry_in('" + rep + "'))");
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


    /**
     * Connect to the databases
     *
     * We use DriverWrapper here. For alternatives, see the DriverWrapper Javadoc
     *
     * @param url JDBC URL to use for the jdbc connection
     * @param dbuser username to use for the jdbc connection
     * @param dbpass password to use for the jdbc conection
     *
     * @see org.postgis.DriverWrapper
     *
     */
    public static Connection connect(String url, String dbuser, String dbpass) throws SQLException {
        logger.trace("Connection connect(String url, String dbuser, String dbpass)");
        logger.trace("   [P] url => [{}]", url);
        logger.trace("   [P] dbuser => [{}]", dbuser);
        logger.trace("   [P] dbpass => [{}]", dbpass);
        Connection conn;
        conn = DriverManager.getConnection(url, dbuser, dbpass);
        return conn;
    }


    public void loadDrivers() throws ClassNotFoundException {
        logger.debug("void loadDrivers()");
        Class.forName(JTS_WRAPPER_CLASS_NAME);
    }


}
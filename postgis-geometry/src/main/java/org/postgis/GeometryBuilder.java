package org.postgis;


import org.postgis.binary.BinaryParser;

import java.sql.SQLException;


/**
 * Builds geometry instances.
 *
 * Note: This class contains the word "builder" but does NOT implement the builder pattern (yet).
 *
 * @author Phillip Ross
 */
public class GeometryBuilder {

    /** The prefix that indicates SRID presence */
    public static final String SRIDPREFIX = "SRID=";


    public static Geometry geomFromString(String value) throws SQLException {
        return geomFromString(value, false);
    }

    public static Geometry geomFromString(String value, boolean haveM) throws SQLException {
        BinaryParser bp = new BinaryParser();

        return geomFromString(value, bp, haveM);
    }

    /**
     * Maybe we could add more error checking here?
     *
     * @param value String representing the geometry
     * @param bp BinaryParser to use whe parsing
     * @return Geometry object parsed from the specified string value
     * @throws SQLException when a SQLException occurs
     */
    public static Geometry geomFromString(String value, BinaryParser bp) throws SQLException {
        return geomFromString(value, bp, false);
    }

    public static Geometry geomFromString(String value, BinaryParser bp, boolean haveM)
            throws SQLException {
        value = value.trim();

        int srid = Geometry.UNKNOWN_SRID;

        if (value.startsWith(SRIDPREFIX)) {
            // break up geometry into srid and wkt
            String[] parts = splitSRID(value);
            value = parts[1].trim();
            srid = Geometry.parseSRID(Integer.parseInt(parts[0].substring(5)));
        }

        Geometry result;
        if (value.startsWith("00") || value.startsWith("01")) {
            result = bp.parse(value);
        } else if (value.endsWith("EMPTY")) {
            // We have a standard conforming representation for an empty
            // geometry which is to be parsed as an empty GeometryCollection.
            result = new GeometryCollection();
        } else if (value.startsWith("MULTIPOLYGON")) {
            result = new MultiPolygon(value, haveM);
        } else if (value.startsWith("MULTILINESTRING")) {
            result = new MultiLineString(value, haveM);
        } else if (value.startsWith("MULTIPOINT")) {
            result = new MultiPoint(value, haveM);
        } else if (value.startsWith("POLYGON")) {
            result = new Polygon(value, haveM);
        } else if (value.startsWith("LINESTRING")) {
            result = new LineString(value, haveM);
        } else if (value.startsWith("POINT")) {
            result = new Point(value, haveM);
        } else if (value.startsWith("GEOMETRYCOLLECTION")) {
            result = new GeometryCollection(value, haveM);
        } else {
            throw new SQLException("Unknown type: " + value);
        }

        if (srid != Geometry.UNKNOWN_SRID) {
            result.srid = srid;
        }

        return result;
    }


    /**
     * Splits a String at the first occurrence of border character.
     *
     * Poor man's String.split() replacement, as String.split() was invented at
     * jdk1.4, and the Debian PostGIS Maintainer had problems building the woody
     * backport of his package using DFSG-free compilers. In all the cases we
     * used split() in the org.postgis package, we only needed to split at the
     * first occurence, and thus this code could even be faster.
     *
     * @param whole the String to be split
     * @return String array containing the split elements
     * @throws SQLException when a SQLException occurrs
     */
    public static String[] splitSRID(String whole) throws SQLException {
        int index = whole.indexOf(';', 5); // sridprefix length is 5
        if (index == -1) {
            throw new SQLException("Error parsing Geometry - SRID not delimited with ';' ");
        } else {
            return new String[]{
                    whole.substring(0, index),
                    whole.substring(index + 1)};
        }
    }


}

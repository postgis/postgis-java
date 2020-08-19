/*
 * JtsBinaryParser.java
 * 
 * Binary Parser for JTS - relies on net.postgis V1.0.0+ package.
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
package net.postgis.jts;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.PackedCoordinateSequence;
import org.locationtech.spatial4j.context.jts.JtsSpatialContextFactory;
import net.postgis.jdbc.geometry.binary.ByteGetter;
import net.postgis.jdbc.geometry.binary.ByteGetter.BinaryByteGetter;
import net.postgis.jdbc.geometry.binary.ByteGetter.StringByteGetter;
import net.postgis.jdbc.geometry.binary.ValueGetter;

/**
 * Parse binary representation of geometries. Currently, only text rep (hexed)
 * implementation is tested.
 * 
 * It should be easy to add char[] and CharSequence ByteGetter instances,
 * although the latter one is not compatible with older jdks.
 * 
 * I did not implement real unsigned 32-bit integers or emulate them with long,
 * as both java Arrays and Strings currently can have only 2^31-1 elements
 * (bytes), so we cannot even get or build Geometries with more than approx.
 * 2^28 coordinates (8 bytes each).
 * 
 * @author Markus Schaber, markus.schaber@logix-tt.com
 * 
 */
public class JtsBinaryParser {
    
    private JtsSpatialContextFactory jtsFactory = new JtsSpatialContextFactory();

    /**
     * Get the appropriate ValueGetter for my endianness
     * 
     * @param bytes
     *            The appropriate Byte Getter
     * 
     * @return the ValueGetter
     */
    public static ValueGetter valueGetterForEndian(ByteGetter bytes) {
        if (bytes.get(0) == ValueGetter.XDR.NUMBER) { // XDR
            return new ValueGetter.XDR(bytes);
        } else if (bytes.get(0) == ValueGetter.NDR.NUMBER) {
            return new ValueGetter.NDR(bytes);
        } else {
            throw new IllegalArgumentException("Unknown Endian type:" + bytes.get(0));
        }
    }


    /**
     * Parse a hex encoded geometry
     * @param value String containing the hex data to be parsed
     * @return the resulting parsed geometry
     */
    public Geometry parse(String value) {
        StringByteGetter bytes = new ByteGetter.StringByteGetter(value);
        return parseGeometry(valueGetterForEndian(bytes));
    }


    /**
     * Parse a binary encoded geometry.
     * @param value byte array containing the binary encoded geometru
     * @return the resulting parsed geometry
     */
    public Geometry parse(byte[] value) {
        BinaryByteGetter bytes = new ByteGetter.BinaryByteGetter(value);
        return parseGeometry(valueGetterForEndian(bytes));
    }


    /**
     * Parse a geometry starting at offset.
     * @param data ValueGetter for the data to be parsed
     * @return The resulting Geometry
     */
    protected Geometry parseGeometry(ValueGetter data) {
        return parseGeometry(data, 0, false);
    }


    /**
     * Parse with a known geometry factory
     * @param data ValueGetter for the data to be parsed
     * @param srid the SRID to be used for parsing
     * @param inheritSrid flag to toggle inheriting SRIDs
     * @return The resulting Geometry
     */
    protected Geometry parseGeometry(ValueGetter data, int srid, boolean inheritSrid) {
        byte endian = data.getByte(); // skip and test endian flag
        if (endian != data.endian) {
            throw new IllegalArgumentException("Endian inconsistency!");
        }
        int typeword = data.getInt();

        int realtype = typeword & 0x1FFFFFFF; // cut off high flag bits

        boolean haveZ = (typeword & 0x80000000) != 0;
        boolean haveM = (typeword & 0x40000000) != 0;
        boolean haveS = (typeword & 0x20000000) != 0;

        if (haveS) {
            int newsrid = net.postgis.jdbc.geometry.Geometry.parseSRID(data.getInt());
            if (inheritSrid && newsrid != srid) {
                throw new IllegalArgumentException("Inconsistent srids in complex geometry: " + srid + ", " + newsrid);
            } else {
                srid = newsrid;
            }
        } else if (!inheritSrid) {
            srid = net.postgis.jdbc.geometry.Geometry.UNKNOWN_SRID;
        }
       
        Geometry result;
        switch (realtype) {
        case net.postgis.jdbc.geometry.Geometry.POINT:
            result = parsePoint(data, haveZ, haveM);
            break;
        case net.postgis.jdbc.geometry.Geometry.LINESTRING:
            result = parseLineString(data, haveZ, haveM);
            break;
        case net.postgis.jdbc.geometry.Geometry.POLYGON:
            result = parsePolygon(data, haveZ, haveM, srid);
            break;
        case net.postgis.jdbc.geometry.Geometry.MULTIPOINT:
            result = parseMultiPoint(data, srid);
            break;
        case net.postgis.jdbc.geometry.Geometry.MULTILINESTRING:
            result = parseMultiLineString(data, srid);
            break;
        case net.postgis.jdbc.geometry.Geometry.MULTIPOLYGON:
            result = parseMultiPolygon(data, srid);
            break;
        case net.postgis.jdbc.geometry.Geometry.GEOMETRYCOLLECTION:
            result = parseCollection(data, srid);
            break;
        default:
            throw new IllegalArgumentException("Unknown Geometry Type!");
        }
        
        result.setSRID(srid);
        
        return result;
    }

    private Point parsePoint(ValueGetter data, boolean haveZ, boolean haveM) {
        double X = data.getDouble();
        double Y = data.getDouble();
        Point result;
        if (haveZ) {
            double Z = data.getDouble();
            result = jtsFactory.getGeometryFactory().createPoint(new Coordinate(X, Y, Z));
        } else {
            result = jtsFactory.getGeometryFactory().createPoint(new Coordinate(X, Y));
        }

        if (haveM) { // skip M value
            data.getDouble();
        }
        
        return result;
    }

    /** Parse an Array of "full" Geometries */
    private void parseGeometryArray(ValueGetter data, Geometry[] container, int srid) {
        for (int i = 0; i < container.length; i++) {
            container[i] = parseGeometry(data, srid, true);
        }
    }

    /**
     * Parse an Array of "slim" Points (without endianness and type, part of
     * LinearRing and Linestring, but not MultiPoint!
     * 
     * @param haveZ
     * @param haveM
     */
    private CoordinateSequence parseCS(ValueGetter data, boolean haveZ, boolean haveM) {
        int count = data.getInt();
        int dims = haveZ ? 3 : 2;
        CoordinateSequence cs = new PackedCoordinateSequence.Double(count, dims, 0);

        for (int i = 0; i < count; i++) {
            for (int d = 0; d < dims; d++) {
                cs.setOrdinate(i, d, data.getDouble());
            }
            if (haveM) { // skip M value
                data.getDouble();
            }
        }
        return cs;
    }

    private MultiPoint parseMultiPoint(ValueGetter data, int srid) {
        Point[] points = new Point[data.getInt()];
        parseGeometryArray(data, points, srid);
        return jtsFactory.getGeometryFactory().createMultiPoint(points);
    }

    private LineString parseLineString(ValueGetter data, boolean haveZ, boolean haveM) {
        return jtsFactory.getGeometryFactory().createLineString(parseCS(data, haveZ, haveM));
    }

    private LinearRing parseLinearRing(ValueGetter data, boolean haveZ, boolean haveM) {
        return jtsFactory.getGeometryFactory().createLinearRing(parseCS(data, haveZ, haveM));
    }

    private Polygon parsePolygon(ValueGetter data, boolean haveZ, boolean haveM, int srid) {
        int holecount = data.getInt() - 1;
        LinearRing[] rings = new LinearRing[holecount];
        LinearRing shell = parseLinearRing(data, haveZ, haveM);
        shell.setSRID(srid);
        for (int i = 0; i < holecount; i++) {
            rings[i] = parseLinearRing(data, haveZ, haveM);
            rings[i].setSRID(srid);
        }
        return jtsFactory.getGeometryFactory().createPolygon(shell, rings);
    }

    private MultiLineString parseMultiLineString(ValueGetter data, int srid) {
        int count = data.getInt();
        LineString[] strings = new LineString[count];
        parseGeometryArray(data, strings, srid);
        return jtsFactory.getGeometryFactory().createMultiLineString(strings);
    }

    private MultiPolygon parseMultiPolygon(ValueGetter data, int srid) {
        int count = data.getInt();
        Polygon[] polys = new Polygon[count];
        parseGeometryArray(data, polys, srid);
        return jtsFactory.getGeometryFactory().createMultiPolygon(polys);
    }

    private GeometryCollection parseCollection(ValueGetter data, int srid) {
        int count = data.getInt();
        Geometry[] geoms = new Geometry[count];
        parseGeometryArray(data, geoms, srid);
        return jtsFactory.getGeometryFactory().createGeometryCollection(geoms);
    }
}

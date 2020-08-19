/*
 * ShapeBinaryParser.java
 * 
 * Shape Binary Parser for Java2D - relies on net.postgis V1.0.0+ package.
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
package net.postgis.jdbc.java2d;

import java.awt.geom.GeneralPath;

import net.postgis.jdbc.geometry.Geometry;
import net.postgis.jdbc.geometry.binary.ByteGetter;
import net.postgis.jdbc.geometry.binary.ValueGetter;
import net.postgis.jdbc.geometry.binary.ByteGetter.BinaryByteGetter;
import net.postgis.jdbc.geometry.binary.ByteGetter.StringByteGetter;

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
public class ShapeBinaryParser {

    /**
     * Get the appropriate ValueGetter for my endianness
     * 
     * @param bytes The appropriate Byte Getter
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
     * 
     * Is synchronized to protect offset counter. (Unfortunately, Java does not
     * have neither call by reference nor multiple return values.)
     *
     * @param value String representation of the value to be parsed
     * @param path GeneralPath to provide the parsed value to
     *
     * @return a potential SRID or Geometry.UNKNOWN_SRID if not present
     */
    public synchronized int parse(String value, GeneralPath path) {
        StringByteGetter bytes = new ByteGetter.StringByteGetter(value);
        return parseGeometry(valueGetterForEndian(bytes), path);
    }

    /**
     * Parse a binary encoded geometry.
     * 
     * Is synchronized to protect offset counter. (Unfortunately, Java does not
     * have neither call by reference nor multiple return values.)
     *
     * @param value byte array representation of the value to be parsed
     * @param path GeneralPath to provide the parsed value to
     * 
     * @return a potential SRID or Geometry.UNKNOWN_SRID if not present
     */
    public synchronized int parse(byte[] value, GeneralPath path) {
        BinaryByteGetter bytes = new ByteGetter.BinaryByteGetter(value);
        return parseGeometry(valueGetterForEndian(bytes), path);
    }

    /**
     * Parse a geometry starting at offset.
     *
     * @param data ValueGetter containing the value to be parsed
     * @param path GeneralPath to provide the parsed value to
     *
     * @return a potential SRID or Geometry.UNKNOWN_SRID if not present
     */
    protected int parseGeometry(ValueGetter data, GeneralPath path) {
        byte endian = data.getByte(); // skip and test endian flag
        if (endian != data.endian) {
            throw new IllegalArgumentException("Endian inconsistency!");
        }
        int typeword = data.getInt();

        int realtype = typeword & 0x1FFFFFFF; // cut off high flag bits

        boolean haveZ = (typeword & 0x80000000) != 0;
        boolean haveM = (typeword & 0x40000000) != 0;
        boolean haveS = (typeword & 0x20000000) != 0;

        int srid = Geometry.UNKNOWN_SRID;

        if (haveS) {
            srid = Geometry.parseSRID(data.getInt());
        }

        switch (realtype) {
            case net.postgis.jdbc.geometry.Geometry.POINT :
                parsePoint(data, haveZ, haveM, path);
                break;
            case net.postgis.jdbc.geometry.Geometry.LINESTRING :
                parseLineString(data, haveZ, haveM, path);
                break;
            case net.postgis.jdbc.geometry.Geometry.POLYGON :
                parsePolygon(data, haveZ, haveM, path);
                break;
            case net.postgis.jdbc.geometry.Geometry.MULTIPOINT :
                parseMultiPoint(data, path);
                break;
            case net.postgis.jdbc.geometry.Geometry.MULTILINESTRING :
                parseMultiLineString(data, path);
                break;
            case net.postgis.jdbc.geometry.Geometry.MULTIPOLYGON :
                parseMultiPolygon(data, path);
                break;
            case net.postgis.jdbc.geometry.Geometry.GEOMETRYCOLLECTION :
                parseCollection(data, path);
                break;
            default :
                throw new IllegalArgumentException("Unknown Geometry Type!");
        }
        return srid;
    }

    private void parsePoint(ValueGetter data, boolean haveZ, boolean haveM, GeneralPath path) {
        double x = data.getDouble();
        double y = data.getDouble();
        path.moveTo(x, y);
        path.lineTo(x, y);
        skipZM(data, haveZ, haveM);
    }

    private void skipZM(ValueGetter data, boolean haveZ, boolean haveM) {
        if (haveZ) { // skip Z value
            data.getDouble();
        }
        if (haveM) { // skip M value
            data.getDouble();
        }
    }

    /** Parse an Array of "full" Geometries */
    private void parseGeometryArray(ValueGetter data, int count, GeneralPath path) {
        for (int i = 0; i < count; i++) {
            parseGeometry(data, path);
        }
    }

    /**
     * Parse an Array of "slim" Points (without endianness and type, part of
     * LinearRing and Linestring, but not MultiPoint!
     *
     * @param data ValueGetter containing the value to be parsed
     * @param haveZ flag indicating if Z values exist
     * @param haveM flag indicating if M values exist
     * @param path GeneralPath to provide the parsed value to
     */
    private void parseCS(ValueGetter data, boolean haveZ, boolean haveM, GeneralPath path) {
        int count = data.getInt();
        if (count > 0) {
            path.moveTo((float) data.getDouble(), (float) data.getDouble());
            skipZM(data, haveZ, haveM);
            for (int i = 1; i < count; i++) {
                path.lineTo((float) data.getDouble(), (float) data.getDouble());
                skipZM(data, haveZ, haveM);
            }
        }
    }


    private void parseMultiPoint(ValueGetter data, GeneralPath path) {
        parseGeometryArray(data, data.getInt(), path);
    }

    private void parseLineString(ValueGetter data, boolean haveZ, boolean haveM, GeneralPath path) {
        parseCS(data, haveZ, haveM, path);
    }

    private void parseLinearRing(ValueGetter data, boolean haveZ, boolean haveM, GeneralPath path) {
        parseCS(data, haveZ, haveM, path);
        path.closePath();
    }

    private void parsePolygon(ValueGetter data, boolean haveZ, boolean haveM, GeneralPath path) {
        int holecount = data.getInt() - 1;
        // parse shell
        parseLinearRing(data, haveZ, haveM, path);
        // parse inner rings
        for (int i = 0; i < holecount; i++) {
            parseLinearRing(data, haveZ, haveM, path);
        }
    }

    private void parseMultiLineString(ValueGetter data, GeneralPath path) {
        int count = data.getInt();
        parseGeometryArray(data, count, path);
    }

    private void parseMultiPolygon(ValueGetter data, GeneralPath path) {
        int count = data.getInt();
        parseGeometryArray(data, count, path);
    }

    private void parseCollection(ValueGetter data, GeneralPath path) {
        int count = data.getInt();
        parseGeometryArray(data, count, path);
    }
}

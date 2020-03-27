/*
 * PGgeometry.java
 *
 * PostGIS extension for PostgreSQL JDBC driver - PGobject Geometry Wrapper
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

package org.postgis;


import org.postgis.binary.BinaryParser;
import org.postgresql.util.PGobject;

import java.sql.SQLException;


public class PGgeometry extends PGobject {
    /* JDK 1.5 Serialization */
    private static final long serialVersionUID = 0x100;

    Geometry geom;

    public PGgeometry() {
        this.setType("geometry");
    }

    public PGgeometry(Geometry geom) {
        this();
        this.geom = geom;
    }

    public PGgeometry(String value) throws SQLException {
        this();
        setValue(value);
    }

    public void setValue(String value) throws SQLException {
        geom = GeometryBuilder.geomFromString(value, new BinaryParser());
    }

    public Geometry getGeometry() {
        return geom;
    }

    public void setGeometry(Geometry newgeom) {
        this.geom = newgeom;
    }

    public int getGeoType() {
        return geom.type;
    }

    public String toString() {
        return geom.toString();
    }

    public String getValue() {
        return geom.toString();
    }

    public Object clone() {
        return new PGgeometry(geom);
    }
}

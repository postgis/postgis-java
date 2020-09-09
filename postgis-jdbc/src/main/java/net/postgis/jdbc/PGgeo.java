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
 * (C) 2004 Paul Ramsey, pramsey@refractions.net
 *
 * (C) 2005 Markus Schaber, markus.schaber@logix-tt.com
 *
 * (C) 2015 Phillip Ross, phillip.w.g.ross@gmail.com
 */

package net.postgis.jdbc;


import net.postgis.jdbc.geometry.Geometry;
import net.postgis.jdbc.geometry.GeometryBuilder;
import net.postgis.jdbc.geometry.binary.BinaryParser;
import org.postgresql.util.PGobject;

import java.sql.SQLException;


/**
 * A PostgreSQL JDBC PGobject extension data type modeling a "geo" type.
 *
 * This class serves as a common superclass for classes such as PGgeometry and PGgeography which model
 * more specific type semantics.
 *
 * @author Phillip Ross
 */
public class PGgeo extends PGobject {

    private static final long serialVersionUID = -3181366908975582090L;

    /** The encapsulated geometry. */
    Geometry geometry;


    /** Instantiate with default state. */
    protected PGgeo() {
    }


    /**
     * Instantiate with the specified state.
     *
     * @param geometry the geometry to instantiate with
     */
    public PGgeo(final Geometry geometry) {
        this();
        this.geometry = geometry;
    }


    /**
     * Instantiate with the specified state.
     *
     * @param value the value to instantiate with
     */
    public PGgeo(final String value) throws SQLException {
        this();
        setValue(value);
    }


    /** {@inheritDoc} */
    @Override
    public String getValue() {
        return geometry.toString();
    }


    /** {@inheritDoc} */
    @Override
    public void setValue(final String value) throws SQLException {
        geometry = GeometryBuilder.geomFromString(value, new BinaryParser());
    }


    /**
     * Get the encapsulated geometry.
     *
     * @return the encapsulated geomtery
     */
    public Geometry getGeometry() {
        return geometry;
    }


    /**
     * Set the encapsulated geometry.
     *
     * @param geometry the encapsulated geometry
     */
    public void setGeometry(final Geometry geometry) {
        this.geometry = geometry;
    }


    /**
     * Get the type of the encapsulated geometry.
     *
     * @return the type of the encapsulated geometry
     */
    public int getGeoType() {
        return geometry.type;
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return geometry.toString();
    }


    /** {@inheritDoc} */
    @Override
    public Object clone() {
        return new PGgeo(geometry);
    }


}

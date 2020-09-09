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

import java.sql.SQLException;


/**
 * A PostgreSQL JDBC PGobject extension data type modeling the geometry type.
 *
 * @author Phillip Ross
 */
public class PGgeometry extends PGgeo {

    private static final long serialVersionUID = 4116907189503026815L;


    /** Instantiate with default state. */
    public PGgeometry() {
        super();
        setType("geometry");
    }


    /**
     * Instantiate with the specified state.
     *
     * @param geometry the geometry to instantiate with
     */
    public PGgeometry(final Geometry geometry) {
        super(geometry);
        setType("geometry");
    }


    /**
     * Instantiate with the specified state.
     *
     * @param value the value to instantiate with
     */
    public PGgeometry(final String value) throws SQLException {
        super(value);
        setType("geometry");
    }


    /** {@inheritDoc} */
    @Override
    public Object clone() {
        return new PGgeometry(geometry);
    }


}

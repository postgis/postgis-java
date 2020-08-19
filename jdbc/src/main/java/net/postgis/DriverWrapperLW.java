/*
 * DriverWrapperLW.java
 * 
 * PostGIS extension for PostgreSQL JDBC driver - Wrapper utility class
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

import org.postgresql.Driver;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * DriverWrapperLW
 * 
 * Wraps the PostGreSQL Driver to transparently add the PostGIS Object Classes.
 * This avoids the need of explicit addDataType() calls from the driver users
 * side.
 * 
 * This DriverWrapper subclass always uses hex encoded EWKB as canonical text
 * representation, and thus only works against PostGIS 1.x servers and newer.
 * 
 * For usage notes, see DriverWrapper class, but use "jdbc:postgresql_lwgis:" as
 * JDBC url prefix and net.postgis.DriverWrapperLW as driver class.
 * 
 * @author {@literal Markus Schaber <markus.schaber@logix-tt.com>}
 * @see DriverWrapper
 */
public class DriverWrapperLW extends DriverWrapper {

    public static final String POSTGIS_LWPROTOCOL = "jdbc:postgresql_lwgis:";
    public static final String REVISIONLW = "$Revision$";

    /**
     * Default constructor.
     *
     * @throws SQLException when a SQLException occurs
     */
    public DriverWrapperLW() throws SQLException {
        super();
    }

    static {
        try {
            // Try to register ourself to the DriverManager
            java.sql.DriverManager.registerDriver(new DriverWrapperLW());
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error registering PostGIS LW Wrapper Driver", e);
        }
    }

    protected String getProtoString() {
        return POSTGIS_LWPROTOCOL;
    }

    protected boolean useLW(Connection result) {
        return true;
    }

    /**
     * Returns our own CVS version plus postgres Version
     *
     * @return String value reprenstation of the version
     */
    public static String getVersion() {
        return "PostGisWrapperLW " + REVISIONLW + ", wrapping " + Driver.getVersion();
    }
}

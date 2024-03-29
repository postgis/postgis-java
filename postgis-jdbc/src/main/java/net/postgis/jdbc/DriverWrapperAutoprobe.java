/*
 * DriverWrapperAutoprobe.java
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

package net.postgis.jdbc;

import org.postgresql.Driver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

/**
 * DriverWrapperAutoprobe
 * 
 * Wraps the PostGreSQL Driver to transparently add the PostGIS Object Classes.
 * This avoids the need of explicit addDataType() calls from the driver users
 * side.
 * 
 * This DriverWrapper tries to autoprobe the installed PostGIS version to decide
 * whether to use EWKT or hex encoded EWKB as canonical text representation. It
 * uses the first PostGIS installation found in your namespace search path (aka
 * schema search path) on the server side, and this works as long as you do not
 * access incompatible PostGIS versions that reside in other schemas.
 * 
 * For usage notes, see DriverWrapper class, but use "jdbc:postgresql_autogis:"
 * as JDBC url prefix and net.postgis.jdbc.DriverWrapperAutoprobe as driver class.
 * 
 * @author {@literal Markus Schaber <markus.schaber@logix-tt.com>}
 * @see DriverWrapper
 */
public class DriverWrapperAutoprobe extends DriverWrapper {

    public static final String POSTGIS_AUTOPROTOCOL = "jdbc:postgresql_autogis:";
    public static final String REVISIONAUTO = "$Revision$";

    /**
     * Default constructor.
     *
     * @throws SQLException when a SQLExceptin occurs
     */
    public DriverWrapperAutoprobe() throws SQLException {
        super();
    }

    static {
        try {
            // Try to register ourself to the DriverManager
            java.sql.DriverManager.registerDriver(new DriverWrapperAutoprobe());
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Error registering PostGIS Autoprobe Wrapper Driver", e);
        }
    }

    protected String getProtoString() {
        return POSTGIS_AUTOPROTOCOL;
    }

    protected boolean useLW(Connection conn) {
        try {
            return supportsEWKB(conn);
        } catch (SQLException e) {
            // fail safe default
            return false;
        }
    }

    /**
     * Returns our own CVS version plus postgres Version
     *
     * @return String value reprenstation of the version
     */
    public static String getVersion() {
        return "PostGisWrapperAutoprobe " + REVISIONAUTO + ", wrapping " + Driver.getVersion();
    }

    public static boolean supportsEWKB(Connection conn) throws SQLException {
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery("SELECT postgis_version()");
        rs.next();
        String version = rs.getString(1);
        rs.close();
        stat.close();
        if (version == null) {
            throw new SQLException("postgis_version returned NULL!");
        }
        version = version.trim();
        int idx = version.indexOf('.');
        int majorVersion = Integer.parseInt(version.substring(0, idx));
        return majorVersion >= 1;
    }
}

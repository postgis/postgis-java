/*
 * Java2DWrapper.java
 * 
 * Allows transparent usage of JTS Geometry classes via PostgreSQL JDBC driver
 * connected to a PostGIS enabled PostgreSQL server.
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

import org.postgresql.Driver;
import org.postgresql.PGConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Java2DWrapper
 * 
 * Wraps the PostGreSQL Driver to add transparent readonly support for PostGIS
 * objects into java2d path objects.
 * 
 * This method currently works with J2EE DataSource implementations, and with
 * DriverManager framework.
 * 
 * Simply replace the "jdbc:postgresql:" with a "jdbc:postgis_j2d" in the
 * jdbc URL.
 * 
 * @author markus.schaber@logix-tt.com
 * 
 */
public class Java2DWrapper extends Driver {

    private static final String POSTGRES_PROTOCOL = "jdbc:postgresql:";
    public static final String POSTGIS_PROTOCOL = "jdbc:postgis_j2d:";
    public static final String REVISION = "$Revision$";

    public Java2DWrapper() {
        super();
    }

    static {
        try {
            // Analogy to org.postgresql.Driver
            java.sql.DriverManager.registerDriver(new Java2DWrapper());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a postgresql connection, and then adds the PostGIS data types to
     * it calling addpgtypes()
     * 
     * @param url the URL of the database to connect to
     * @param info a list of arbitrary tag/value pairs as connection arguments
     * @return a connection to the URL or null if it isnt us
     * @exception SQLException if a database access error occurs
     * 
     * @see java.sql.Driver#connect
     * @see org.postgresql.Driver
     */
    public java.sql.Connection connect(String url, Properties info) throws SQLException {
        url = mangleURL(url);
        Connection result = super.connect(url, info);
        addGISTypes((PGConnection) result);
        return result;
    }


    /**
     * Adds the JTS/PostGIS Data types to a PG Connection.
     *
     * @throws SQLException when a SQLException occurs
     * @param pgconn The PGConnection object to add the types to
     * @throws SQLException when an SQLException occurs
     */
    public static void addGISTypes(PGConnection pgconn) throws SQLException {
        pgconn.addDataType("geometry", PGShapeGeometry.class);
        pgconn.addDataType("box3d", net.postgis.jdbc.PGbox3d.class);
        pgconn.addDataType("box2d", net.postgis.jdbc.PGbox2d.class);
    }


    /**
     * Mangles the PostGIS URL to return the original PostGreSQL URL
     *
     * @param url String containing the url to be "mangled"
     * @return "mangled" string or null if the URL is unsupported
     */
    public static String mangleURL(String url) {
        return url.startsWith(POSTGIS_PROTOCOL)
            ? POSTGRES_PROTOCOL + url.substring(POSTGIS_PROTOCOL.length())
            : null;
    }

    /**
     * Returns true if the driver thinks it can open a connection to the given
     * URL. Typically, drivers will return true if they understand the
     * subprotocol specified in the URL and false if they don't. Our protocols
     * start with jdbc:postgresql_postGIS:
     * 
     * @see java.sql.Driver#acceptsURL
     * @param url the URL of the driver
     * @return true if this driver accepts the given URL
     */
    public boolean acceptsURL(String url) {
        url = mangleURL(url);
        return url != null && super.acceptsURL(url);
    }

    /**
     * Gets the underlying drivers major version number
     * 
     * @return the drivers major version number
     */
    public int getMajorVersion() {
        return super.getMajorVersion();
    }

    /**
     * Get the underlying drivers minor version number
     * 
     * @return the drivers minor version number
     */
    public int getMinorVersion() {
        return super.getMinorVersion();
    }


    /**
     * Returns our own CVS version plus postgres Version
     *
     * @return String identifier for the version
     */
    public static String getVersion() {
        return "Java2DWrapper " + REVISION + ", wrapping " + Driver.getVersion();
    }

    public Logger getParentLogger() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

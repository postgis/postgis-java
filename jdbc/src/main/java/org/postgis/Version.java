/*
 * Version.java
 * 
 * PostGIS extension for PostgreSQL JDBC driver - current version identification
 * 
 * (C) 2005 Markus Schaber, markus.schaber@logix-tt.com
 *
 * (C) 2015 Phillip Ross {@literal <phillip.w.g.ross@gmail.com>}
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA or visit the web at
 * http://www.gnu.org.
 * 
 */

package org.postgis;


import java.io.IOException;
import java.util.Properties;


/** Corresponds to the appropriate PostGIS that carried this source */
public class Version {

    /** We read our version information from this resource... */
    private static final String RESOURCE_FILENAME = "org/postgis/version.properties";

    private static final String VERSION_PROPERTY_NAME = "VERSION";

    public static final String VERSION;

    /** The major version */
    public static final int MAJOR;

    /** The minor version */
    public static final int MINOR;

    /**
     * The micro version, usually a number including possibly textual suffixes
     * like RC3.
     */
    public static final String MICRO;

    static {
        int major = -1;
        int minor = -1;
        String micro = null;
        String version = null;
        try {
            ClassLoader loader = Version.class.getClassLoader();

            Properties prop = new Properties();
            try {
                prop.load(loader.getResourceAsStream(RESOURCE_FILENAME));
            } catch (IOException e) {
                throw new ExceptionInInitializerError("Error initializing PostGIS JDBC version. Cause: Ressource "
                        + RESOURCE_FILENAME + " cannot be read. " + e.getMessage());
            } catch (NullPointerException e) {
                throw new ExceptionInInitializerError("Error initializing PostGIS JDBC version. Cause: Ressource "
                        + RESOURCE_FILENAME + " not found. " + e.getMessage());
            }

            version = prop.getProperty(VERSION_PROPERTY_NAME);
            if (version == null) {
                throw new ExceptionInInitializerError("Error initializing PostGIS JDBC version:  Missing " + VERSION_PROPERTY_NAME + " property.");
            } else if (version.equals("")) {
                    throw new ExceptionInInitializerError("Error initializing PostGIS JDBC version:  Empty " + VERSION_PROPERTY_NAME + " property.");
            } else {
                String[] versions = version.split("\\.");
                if (version.length() < 3) {
                    throw new ExceptionInInitializerError("Error initializing PostGIS JDBC version:  FULL_VERSION (" + version + ") does not contain 3 components ");
                }
                if (versions.length >= 1) {
                    try {
                        major = Integer.parseInt(versions[0]);
                    } catch (NumberFormatException nfe) {
                        throw new ExceptionInInitializerError("Error initializing PostGIS JDBC version! Error parsing major version ");
                    }
                }
                if (versions.length >= 2) {
                    try {
                        minor = Integer.parseInt(versions[1]);
                    } catch (NumberFormatException nfe) {
                        throw new ExceptionInInitializerError("Error initializing PostGIS JDBC version! Error parsing minor version ");
                    }
                }
                if (version.length() >= 3) {
                    micro = versions[2];
                }
            }

        } finally {
            MAJOR = major;
            MINOR = minor;
            MICRO = micro;
            VERSION = version;
        }
    }

    /** Full version for human reading - code should use the constants above */
    public static final String FULL = "PostGIS JDBC V" + MAJOR + "." + MINOR + "." + MICRO;

    public static String getFullVersion() {
        return FULL;
    }


}
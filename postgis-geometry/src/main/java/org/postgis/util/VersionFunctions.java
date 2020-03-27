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
 * (C) 2020 Phillip Ross, phillip.w.g.ross@gmail.com
 */

package org.postgis.util;


/**
 * An enumeration of existing version functions.
 *
 * @author Phillip Ross
 */
public enum VersionFunctions {

    /** The function to return the full version and build configuration info of the PostGIS Server. */
    POSTGIS_FULL_VERSION,

    /** The function to return the version of the GDAL library. */
    POSTGIS_GDAL_VERSION,

    /** The function to return the version of the GEOS library. */
    POSTGIS_GEOS_VERSION,

    /** The function to return the build date of the PostGIS library. */
    POSTGIS_LIB_BUILD_DATE,

    /** The function to return the version of the PostGIS library. */
    POSTGIS_LIB_VERSION,

    /** The function to return the version of the libjson library. */
    POSTGIS_LIBJSON_VERSION,

    /** The function to return the version of the libxml library. */
    POSTGIS_LIBXML_VERSION,

    /** The function to return the version of the Proj library. */
    POSTGIS_PROJ_VERSION,

    /** The function to return the version of the raster library. */
    POSTGIS_RASTER_LIB_VERSION,

    /** The function to return the build date of the scripts. */
    POSTGIS_SCRIPTS_BUILD_DATE,

    /** The function to return the version of the scripts installed in the database. */
    POSTGIS_SCRIPTS_INSTALLED,

    /** The function to return the version of the scripts released with the installed PostGIS library. */
    POSTGIS_SCRIPTS_RELEASED,

    /** The function to return the Subversion version of the PostGIS Server. */
    POSTGIS_SVN_VERSION,

    /** The function to return the version of the PostGIS Server. */
    POSTGIS_VERSION

}

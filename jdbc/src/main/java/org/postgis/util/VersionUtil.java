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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;


/**
 * Utility for working with PostGIS Server version.
 *
 * @author Phillip Ross
 */
public class VersionUtil {

    /** The static logger instance. */
    private static final Logger logger = LoggerFactory.getLogger(VersionUtil.class);

    /** The string to match when determining a function does not exist from the content of an error message. */
    public static final String NONEXISTENT_FUNCTION_ERROR_MESSAGE_CONTENT = "does not exist";

    /** The token which separates version components within the PostGIS Server version. */
    public static final String POSTGIS_SERVER_VERSION_SEPERATOR = ".";

    /** The number of seconds to wait for a connection validation operation. */
    private static final int DEFAULT_CONNECTION_TIMEOUT = 60;


    /**
     * Query a specific version string from the datasource for a specified function.
     *
     * @param connection The connection to issue the version query function against.
     * @param function The version function to use for querying the version.
     * @return a string version for the specified function.
     * @throws SQLException when a jdbc exception occurs.
     */
    public static String getVersionString(final Connection connection, final String function) throws SQLException {
        Objects.requireNonNull(connection, "Unable to retrieve version string from a null connection");
        Objects.requireNonNull(function, "Unable to retrieve version string for a null function");
        validateConnection(connection);

        String result = "-- unavailable -- ";
        try (
                PreparedStatement statement = connection.prepareStatement("SELECT " + function + "()");
                ResultSet resultSet = statement.executeQuery();
        ) {
            if (resultSet.next()) {
                String version = resultSet.getString(1);
                if (version != null) {
                    result = version.trim();
                } else {
                    result = "-- null result --";
                }
            } else {
                result = "-- no result --";
            }
        } catch (SQLException sqle) {
            // If the function does not exist, a SQLException will be thrown, but it should be caught and swallowed if
            // the non-existent function error message content is found in the error message.  The SQLException might
            // be thrown for some other problem not related to the missing function, so rethrow it if it doesn't
            // contain the non-existent function error message content.
            if (!sqle.getMessage().contains(NONEXISTENT_FUNCTION_ERROR_MESSAGE_CONTENT)) {
                throw sqle;
            }
        }
        return result;
    }


    public static String retrievePostGISServerVersionString(final Connection connection) throws SQLException {
        Objects.requireNonNull(
                connection, "Unable to retrieve PostGIS server version string from a null connection"
        );
        validateConnection(connection);
        String postGISVersionString = getVersionString(connection, VersionFunctions.POSTGIS_VERSION.toString());
        logger.debug("retrieved PostGIS server version string: [{}]", postGISVersionString);
        return postGISVersionString;
    }


    public static String retrievePostGISServerVersion(final Connection connection) throws SQLException {
        Objects.requireNonNull(connection, "Unable to retrieve PostGIS version from a null connection");
        validateConnection(connection);
        String versionString = retrievePostGISServerVersionString(connection);

        final String versionTerminatorString = " ";
        final String version;
        final int versionTerminatorIndex = versionString.indexOf(versionTerminatorString);
        if (versionTerminatorIndex == -1) {
            version = versionString;
        } else {
            version = versionString.substring(0, versionTerminatorIndex);
        }
        logger.debug("retrieved PostGIS server version: [{}]", version);
        return version;
    }


    public static String retrievePostGISServerMajorVersion(final Connection connection) throws SQLException {
        Objects.requireNonNull(connection, "Unable to retrieve PostGIS major version from a null connection");
        validateConnection(connection);
        String version = retrievePostGISServerVersion(connection);
        final String majorVersion;
        final int majorVersionSeperatorIndex = version.indexOf(POSTGIS_SERVER_VERSION_SEPERATOR);
        if (majorVersionSeperatorIndex == -1) {
            majorVersion = version;
        } else {
            majorVersion = version.substring(0, majorVersionSeperatorIndex);
        }
        logger.debug("retrieved postGIS major version string: [{}]", majorVersion);
        return majorVersion;
    }


    public static String retrievePostGISServerMinorVersion(final Connection connection) throws SQLException {
        Objects.requireNonNull(connection, "Unable to retrieve PostGIS minor version from a null connection");
        validateConnection(connection);
        String version = retrievePostGISServerVersion(connection);
        final String minorVersion;
        final int majorVersionSeperatorIndex = version.indexOf(POSTGIS_SERVER_VERSION_SEPERATOR);
        if (majorVersionSeperatorIndex == -1) {
            minorVersion = "";
        } else {
            final int minorVersionSeperatorIndex =
                    version.indexOf(POSTGIS_SERVER_VERSION_SEPERATOR, majorVersionSeperatorIndex + 1);
            if (minorVersionSeperatorIndex == -1) {
                minorVersion = version.substring(majorVersionSeperatorIndex + 1);
            } else {
                minorVersion = version.substring(majorVersionSeperatorIndex + 1, minorVersionSeperatorIndex);
            }
        }
        logger.debug("retrieved postGIS minor version string: [{}]", minorVersion);
        return minorVersion;
    }


    /**
     * Validates a connection.
     *
     * @param connection the connection to be validated.
     * @throws SQLException when connection is invalid
     */
    private static void validateConnection(final Connection connection) throws SQLException {
        if (!connection.isValid(DEFAULT_CONNECTION_TIMEOUT)) {
            throw new SQLException("The connection was not valid.");
        }
    }


}

/*
 * EmptyGeometriesTest.java
 *
 * PostGIS extension for PostgreSQL JDBC driver - example and test classes
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


import net.postgis.tools.testutils.TestContainerController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * This class contains tests for handling of empty geometries.
 *
 * @author Phillip Ross {@literal <phillip.r.g.ross@gmail.com>}
 */
public class EmptyGeometriesTest {

    private static final Logger logger = LoggerFactory.getLogger(EmptyGeometriesTest.class);

    private static final String DRIVER_WRAPPER_CLASS_NAME = "net.postgis.jdbc.DriverWrapper";

    private static final String DRIVER_WRAPPER_AUTOPROBE_CLASS_NAME = "net.postgis.jdbc.DriverWrapperAutoprobe";


    public static final String[] geometriesToTest = new String[] {
            "POINT",
            "LINESTRING",
            "POLYGON",
            "MULTIPOINT",
            "MULTILINESTRING",
            "MULTIPOLYGON",
            "GEOMETRYCOLLECTION",
    };

    public static final String[] castTypes = new String[] {
            "bytea",
            "text",
            "geometry",
            "geography"
    };

    private Connection connection = null;

    private Statement statement = null;


    @Test
    public void testSqlStatements() throws SQLException {
        for (String sqlStatement : generateSqlStatements()) {
            logger.debug("**********");
            logger.debug("* Executing sql statemnent => [{}]", sqlStatement);
            logger.debug("**********");
            try (PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
                 ResultSet resultSet = preparedStatement.executeQuery()
            ) {
                resultSet.next();
                for (int i = 1; i <= castTypes.length; i++) {
                    Object resultSetObject = resultSet.getObject(i);
                    logger.debug("returned resultSetObject {} => (class=[{}]) {}", i, resultSetObject.getClass().getName(), resultSetObject);
                }
                resultSet.close();
            }
        }
    }


    private List<String> generateSqlStatements() {
        List<String> sqlStatementList = new ArrayList<>();
        for (String geometry : geometriesToTest) {
            StringBuilder stringBuilder = new StringBuilder("select ");
            for (String castType : castTypes) {
                stringBuilder.append("geometry_in('")
                        .append(geometry)
                        .append(" EMPTY')::")
                        .append(castType)
                        .append(", ");
            }
            String sqlStatement = stringBuilder.substring(0, stringBuilder.lastIndexOf(","));
            logger.debug("generate sql statement: {}", sqlStatement);
            sqlStatementList.add(sqlStatement);
        }
        return sqlStatementList;
    }


    @BeforeClass
    public void initJdbcConnection(ITestContext ctx) throws Exception {
        final String jdbcUrlSuffix = (String)ctx.getAttribute(TestContainerController.TEST_CONTAINER_JDBC_URL_SUFFIX);
        Assert.assertNotNull(jdbcUrlSuffix);
        final String jdbcUrl = "jdbc:postgresql" + jdbcUrlSuffix;
        final String jdbcUsername = (String)ctx.getAttribute(TestContainerController.TEST_CONTAINER_ENV_USER_PARAM_NAME);
        Assert.assertNotNull(jdbcUsername);
        final String jdbcPassword = (String)ctx.getAttribute(TestContainerController.TEST_CONTAINER_ENV_PW_PARAM_NAME);
        Assert.assertNotNull(jdbcPassword);
        Class.forName(DRIVER_WRAPPER_CLASS_NAME);
        Class.forName(DRIVER_WRAPPER_AUTOPROBE_CLASS_NAME);
        connection = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
        statement = connection.createStatement();
    }


    @AfterClass
    public void unallocateDatabaseResources() throws Exception {
        if ((statement != null) && (!statement.isClosed())) {
            statement.close();
        }
        if ((connection != null) && (!connection.isClosed())) {
            connection.close();
        }
    }


}
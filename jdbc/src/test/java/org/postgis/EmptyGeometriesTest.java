package org.postgis;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
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

    private static final String DRIVER_WRAPPER_CLASS_NAME = "org.postgis.DriverWrapper";

    private static final String DRIVER_WRAPPER_AUTOPROBE_CLASS_NAME = "org.postgis.DriverWrapperAutoprobe";


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
            "geometry"
    };

    private boolean testWithDatabase = false;

    private Connection connection = null;

    private Statement statement = null;


    @Test
    public void testSqlStatements() throws SQLException {
        if (testWithDatabase) {
            for (String sqlStatement : generateSqlStatements()) {
                logger.debug("**********");
                logger.debug("* Executing sql statemnent => [{}]", sqlStatement);
                logger.debug("**********");
                try (PreparedStatement preparedStatement = connection.prepareStatement(sqlStatement);
                     ResultSet resultSet = preparedStatement.executeQuery()
                ) {
                    resultSet.next();
                    for (int i = 1; i <= 3; i++) {
                        Object resultSetObject = resultSet.getObject(i);
                        logger.debug("returned resultSetObject {} => (class=[{}]) {}", i, resultSetObject.getClass().getName(), resultSetObject);
                    }
                    resultSet.close();
                }
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
    @Parameters({"testWithDatabaseSystemProperty", "jdbcUrlSystemProperty", "jdbcUsernameSystemProperty", "jdbcPasswordSystemProperty"})
    public void initJdbcConnection(String testWithDatabaseSystemProperty,
                                   String jdbcUrlSystemProperty,
                                   String jdbcUsernameSystemProperty,
                                   String jdbcPasswordSystemProperty) throws Exception {
        logger.debug("testWithDatabaseSystemProperty: {}", testWithDatabaseSystemProperty);
        logger.debug("jdbcUrlSystemProperty: {}", jdbcUrlSystemProperty);
        logger.debug("jdbcUsernameSystemProperty: {}", jdbcUsernameSystemProperty);
        logger.debug("jdbcPasswordSystemProperty: {}", jdbcPasswordSystemProperty);

        testWithDatabase = Boolean.parseBoolean(System.getProperty(testWithDatabaseSystemProperty));
        String jdbcUrl = System.getProperty(jdbcUrlSystemProperty);
        String jdbcUsername = System.getProperty(jdbcUsernameSystemProperty);
        String jdbcPassword = System.getProperty(jdbcPasswordSystemProperty);

        logger.debug("testWithDatabase: {}", testWithDatabase);
        logger.debug("jdbcUrl: {}", jdbcUrl);
        logger.debug("jdbcUsername: {}", jdbcUsername);
        logger.debug("jdbcPassword: {}", jdbcPassword);

        if (testWithDatabase) {
            Class.forName(DRIVER_WRAPPER_CLASS_NAME);
            Class.forName(DRIVER_WRAPPER_AUTOPROBE_CLASS_NAME);
            connection = DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
            statement = connection.createStatement();
        } else {
            logger.info("testWithDatabase value was false.  Database tests will be skipped.");
        }
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
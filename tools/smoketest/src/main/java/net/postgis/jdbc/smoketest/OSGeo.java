package net.postgis.jdbc.smoketest;

import net.postgis.jdbc.PGbox3d;
import net.postgis.jdbc.PGgeometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.UUID;


/**
 * Simple smoke test util for verifying functionality postgis jdbc jar against a postgresql database
 * with posgis extensions on OSGeo-Live.
 *
 * @author Phillip Ross
 */
public class OSGeo {

    private static final Logger logger = LoggerFactory.getLogger(OSGeo.class);

    private static final String DEFAULT_TEST_TABLE_PREFIX = "SMOKE_TEST";

    private static final String JDBC_DRIVER_CLASS_NAME = "org.postgresql.Driver";

    private Connection connection = null;

    private Statement statement = null;

    private String testTableName = null;


    public OSGeo(final String jdbcUrl,
                 final String jdbcUsername,
                 final String jdbcPassword,
                 final String testTablePrefix) {
        try {
            Objects.requireNonNull(jdbcUrl, "A JDBC URL must be specified");
            Objects.requireNonNull(jdbcUsername, "A database username must be specified.");
            Objects.requireNonNull(jdbcPassword, "A database password must be specified.");
            Objects.requireNonNull(testTablePrefix, "Unable to determine test table prefix");
            logger.debug("Running test (url/u/p/ttp): {}/{}/{}/{}", jdbcUrl, jdbcUsername, jdbcPassword, testTablePrefix);
            setupDatabaseResources(jdbcUrl, jdbcUsername, jdbcPassword);
            Objects.requireNonNull(connection, "Unable to continue testing without a connection to the database");
            if (testDatatypeRegistration()) {
                Objects.requireNonNull(statement, "Unable to continue testing without a statement resource");
                if (createTestTable(testTablePrefix)) {
                    if (testSQL()) {
                        logger.debug("All tests passed");
                        dropTestTable();
                    }
                }
            }
            closeDatabaseResources();
        } catch (Exception e) {
            logger.error("Caught exception: {} {}", e.getClass().getName(), e.getMessage());
            e.printStackTrace();
        }
    }


    private boolean testSQL() {
        final String insertPointSQL = "insert into " + testTableName + " values ('POINT (10 10 10)',1)";
        final String insertPolygonSQL = "insert into " + testTableName + " values ('POLYGON ((0 0 0,0 10 0,10 10 0,10 0 0,0 0 0))',2)";

        boolean testPass = false;
        try {
            logger.debug("Inserting point...");
            statement.execute(insertPointSQL);

            logger.debug("Inserting polygon...");
            statement.execute(insertPolygonSQL);

            logger.debug("Querying table...");
            ResultSet resultSet = statement.executeQuery("select ST_AsText(geom),id from " + testTableName);
            while (resultSet.next()) {
                Object obj = resultSet.getObject(1);
                int id = resultSet.getInt(2);
                logger.debug("Row {}: {}", id, obj.toString());
            }
            testPass = true;
        } catch (SQLException se) {
            logger.error(
                    "Caught SQLException attempting to issue SQL to the database: {} {}",
                    se.getClass().getName(),
                    se.getMessage()
            );
        }
        return testPass;
    }


    private boolean createTestTable(final String testTablePrefix) {
        testTableName = testTablePrefix + "_" + UUID.randomUUID().toString().replaceAll("-", "");
        final String dropSQL = "drop table " + testTableName;
        final String createSQL = "create table " + testTableName + " (geom geometry, id int4)";

        boolean testPass = false;
        logger.debug("Creating table with geometric types...");
        boolean tableExists = false;
        try {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            try (ResultSet resultSet = databaseMetaData.getTables(null, null, testTableName.toLowerCase(), new String[] {"TABLE"})) {
                while (resultSet.next()) {
                    tableExists = true;
                }
            }
            if (tableExists) {
                statement.execute(dropSQL);
            }
            statement.execute(createSQL);
            testPass = true;
        } catch (SQLException se) {
            logger.error(
                    "Caught SQLException attempting to create the test table: {} {}",
                    se.getClass().getName(),
                    se.getMessage()
            );
        }
        return testPass;
    }


    private void dropTestTable() {
        final String dropSQL = "drop table " + testTableName;
        logger.debug("Dropping test table");
        boolean tableExists = false;
        try {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            try (ResultSet resultSet = databaseMetaData.getTables(null, null, testTableName.toLowerCase(), new String[] {"TABLE"})) {
                while (resultSet.next()) {
                    tableExists = true;
                }
            }
            if (tableExists) {
                statement.execute(dropSQL);
            }
        } catch (SQLException se) {
            logger.error(
                    "Caught SQLException attempting to drop the test table: {} {}",
                    se.getClass().getName(),
                    se.getMessage()
            );
        }
    }


    private boolean testDatatypeRegistration() {
        boolean testPass = false;
        logger.debug("Adding geometric type entries...");
        try {
            ((org.postgresql.PGConnection)connection).addDataType("geometry", PGgeometry.class);
            ((org.postgresql.PGConnection)connection).addDataType("box3d", PGbox3d.class);
            testPass = true;
        } catch (SQLException se) {
            logger.error(
                    "Caught SQLException attempting to register datatypes with PostgreSQL driver: {} {}",
                    se.getClass().getName(),
                    se.getMessage()
            );
        }
        return testPass;
    }


    private void closeDatabaseResources() {
        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException se) {
            logger.error(
                    "Caught SQLException attempting to close statement resource: {} {}",
                    se.getClass().getName(),
                    se.getMessage()
            );
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException se) {
            logger.error(
                    "Caught SQLException attempting to close connection to the database: {} {}",
                    se.getClass().getName(),
                    se.getMessage()
            );
        }
    }


    private void setupDatabaseResources(final String url, final String username, final String password) {
        try {
            Class.forName(JDBC_DRIVER_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            logger.error("Caught exception attempting to load jdbc driver class {}", JDBC_DRIVER_CLASS_NAME);
            logger.error("Check your classpath to verify that you've included the postgresql jdbc driver.");
        }
        try {
            connection = DriverManager.getConnection(url, username, password);
            statement = connection.createStatement();
        } catch (SQLException se) {
            logger.error(
                    "Caught SQLException attempting to setup database resources: {} {}",
                    se.getClass().getName(),
                    se.getMessage()
            );
        }
    }


    public static void main(final String[] args) {
        if (args.length < 3) {
            System.out.println("parameters: jdbcUrl jdbcUsername jdbcPassword [testTablePrefix]");
        } else {
            String jdbcUrl = args[0];
            String jdbcUsername = args[1];
            String jdbcPassword = args[2];
            String testTablePrefix = DEFAULT_TEST_TABLE_PREFIX;
            if (args.length > 3) {
                testTablePrefix = args[3];
            }
            new OSGeo(jdbcUrl, jdbcUsername, jdbcPassword, testTablePrefix);
        }
    }


}

// java -classpath ~/.m2/repository/net/postgis/postgis-jdbc/2.2.1-SNAPSHOT/postgis-jdbc-2.2.1-SNAPSHOT.jar:target/smoketest-0.0.1-SNAPSHOT.jar net.postgis.osgeo.util.Main jdbc:postgresql://db01:5432/postgis1 postgis1 postgis1 smoke_test

/*
 * ServerTest.java
 * 
 * PostGIS extension for PostgreSQL JDBC driver - example and test classes
 * 
 * (C) 2004 Paul Ramsey, pramsey@refractions.net
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


import net.postgis.tools.testutils.TestContainerController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.*;
import java.util.UUID;


public class ServerTest {

    private static final Logger logger = LoggerFactory.getLogger(ServerTest.class);

    private static final String JDBC_DRIVER_CLASS_NAME = "org.postgresql.Driver";

    private static final String DATABASE_TABLE_NAME_PREFIX = "jdbc_test";

    private Connection connection = null;

    private Statement statement = null;


    @Test
	public void testServer() throws Exception {
        String dbtable = DATABASE_TABLE_NAME_PREFIX + "_" + UUID.randomUUID().toString().replaceAll("-", "");

		String dropSQL = "drop table " + dbtable;
		String createSQL = "create table " + dbtable + " (geom geometry, id int4)";
		String insertPointSQL = "insert into " + dbtable + " values ('POINT (10 10 10)',1)";
		String insertPolygonSQL = "insert into " + dbtable + " values ('POLYGON ((0 0 0,0 10 0,10 10 0,10 0 0,0 0 0))',2)";

        logger.debug("Adding geometric type entries...");
        ((org.postgresql.PGConnection)connection).addDataType("geometry", PGgeometry.class);
        ((org.postgresql.PGConnection)connection).addDataType("box3d", PGbox3d.class);

        logger.debug("Creating table with geometric types...");
        boolean tableExists = false;
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        try (ResultSet resultSet = databaseMetaData.getTables(null, null, dbtable.toLowerCase(), new String[] {"TABLE"})) {
            while (resultSet.next()) {
                tableExists = true;
            }
        }
        if (tableExists) {
            statement.execute(dropSQL);
        }
        statement.execute(createSQL);

        logger.debug("Inserting point...");
        statement.execute(insertPointSQL);

        logger.debug("Inserting polygon...");
        statement.execute(insertPolygonSQL);

        logger.debug("Querying table...");
        ResultSet resultSet = statement.executeQuery("select ST_AsText(geom),id from " + dbtable);
        while (resultSet.next()) {
            Object obj = resultSet.getObject(1);
            int id = resultSet.getInt(2);
            logger.debug("Row {}: {}", id, obj.toString());
        }

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
        Class.forName(JDBC_DRIVER_CLASS_NAME);
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
package net.postgis.jdbc;

import org.junit.Assert;
import org.junit.Test;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Tests to ensure that the drivers that are registered as services in META-INF/services/java.sql.Driver are resolved
 * correctly.
 */
public class ServiceTest {

    @Test
    public void testWrapperService() throws SQLException {
        Driver driver = DriverManager.getDriver("jdbc:postgresql_postGIS:/");
        Assert.assertEquals(DriverWrapper.class, driver.getClass());
    }

    @Test
    public void testWrapperAutoprobeService() throws SQLException {
        Driver driver = DriverManager.getDriver("jdbc:postgresql_autogis:/");
        Assert.assertEquals(DriverWrapperAutoprobe.class, driver.getClass());
    }

    @Test
    public void testWrapperLWService() throws SQLException {
        Driver driver = DriverManager.getDriver("jdbc:postgresql_lwgis:/");
        Assert.assertEquals(DriverWrapperLW.class, driver.getClass());
    }

}

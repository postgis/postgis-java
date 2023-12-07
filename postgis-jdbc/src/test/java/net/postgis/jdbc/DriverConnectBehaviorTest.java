package net.postgis.jdbc;

import org.junit.Assert;
import org.junit.Test;

import java.sql.DriverManager;
import java.sql.SQLException;

public class DriverConnectBehaviorTest {

    @Test
    public void testThatPostGisDoesNotOverwriteSavedExceptionForUnsupportedConnectionString() {
        try {
            DriverManager.getConnection("jdbc:missing");
        } catch (SQLException e) {
            // This should not be "Unknown protocol or subprotocol in url jdbc:missing", which
            // would indicate that PostGIS threw an exception instead of returning `null` from
            // the `connect` method for an unsupported connection string.
            // (This is documented in `java.sql.Driver.connect`.)
            //
            // The former behavior is not desirable as throwing an exception causes a previously
            // saved exception from a "better fitting" driver to be overwritten by PostGis, despite
            // PostGis not actually being able to handle the connection.
            //
            // (Imagine an Oracle connection string with a wrong password, in which the Oracle
            // driver's exception regarding the wrong password would be replaced with a generic
            // nonsensical PostGis exception.)
            Assert.assertEquals("No suitable driver found for jdbc:missing", e.getMessage());
        }
    }

}

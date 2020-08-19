/*
 * DatatypesTest.java
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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.sql.SQLException;


public class DatatypesTest {

    private static final Logger logger = LoggerFactory.getLogger(DatatypesTest.class);

    private static final String mlng_str = "MULTILINESTRING ((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))";


    @Test
    public void testPGgeometry() throws SQLException {
        logger.trace("void testPGgeometry()");
        logger.info(mlng_str);
        PGgeometry pgf = new PGgeometry(mlng_str);
        logger.info(pgf.toString());
    }


    @Test
    public void testPGgeography() throws SQLException {
        logger.trace("void testPGgeography()");
        logger.info(mlng_str);
        PGgeography pgf = new PGgeography(mlng_str);
        logger.info(pgf.toString());
    }


}
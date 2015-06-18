/*
 * Test.java
 * 
 * PostGIS extension for PostgreSQL JDBC driver - example and test classes
 * 
 * (C) 2004 Paul Ramsey, pramsey@refractions.net
 * 
 * (C) 2005 Markus Schaber, markus.schaber@logix-tt.com
 *
 * (C) 2015 Phillip Ross, phillip.w.g.ross@gmail.com
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA or visit the web at
 * http://www.gnu.org.
 * 
 */

package org.postgis;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.sql.SQLException;


public class DatatypesTest {

    private static final Logger logger = LoggerFactory.getLogger(DatatypesTest.class);

    private static final String mlng_str = "MULTILINESTRING ((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))";

    private static final String mplg_str = "MULTIPOLYGON (((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)),((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0)))";

    private static final String plg_str = "POLYGON ((10 10 0,20 10 0,20 20 0,20 10 0,10 10 0),(5 5 0,5 6 0,6 6 0,6 5 0,5 5 0))";

    private static final String lng_str = "LINESTRING  (10 10 20,20 20 20, 50 50 50, 34 34 34)";

    private static final String ptg_str = "POINT(10 10 20)";

    private static final String lr_str = "(10 10 20,34 34 34, 23 19 23 , 10 10 11)";


    @Test
    public void testLinearRing() throws SQLException {
        logger.trace("void testLinearRing()");
        logger.info(lr_str);
        LinearRing lr = new LinearRing(lr_str);
        logger.info(lr.toString());
    }


    @Test
    public void testPoint() throws SQLException {
        logger.trace("void testPoint()");
        logger.info(ptg_str);
        Point ptg = new Point(ptg_str);
        logger.info(ptg.toString());
    }


    @Test
    public void testLineString() throws SQLException {
        logger.trace("void testLineString()");
        logger.info(lng_str);
        LineString lng = new LineString(lng_str);
        logger.info(lng.toString());
    }


    @Test
    public void testPolygon() throws SQLException {
        logger.trace("void testPolygon()");
        logger.info(plg_str);
        Polygon plg = new Polygon(plg_str);
        logger.info(plg.toString());
    }


    @Test
    public void testMultiPolygon() throws SQLException {
        logger.trace("void testMultiPolygon()");
        logger.info(mplg_str);
        MultiPolygon mplg = new MultiPolygon(mplg_str);
        logger.info(mplg.toString());
    }


    @Test
    public void testMultiLineString() throws SQLException {
        logger.trace("void testMultiLineString()");
        logger.info(mlng_str);
        MultiLineString mlng = new MultiLineString(mlng_str);
        logger.info(mlng.toString());
    }


    @Test
    public void testPGgeometry() throws SQLException {
        logger.trace("void testPGgeometry()");
        logger.info(mlng_str);
        PGgeometry pgf = new PGgeometry(mlng_str);
        logger.info(pgf.toString());
    }


}
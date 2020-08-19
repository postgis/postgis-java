/*
 * TestJava2d.java
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

package net.postgis.jdbc.smoketest;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import net.postgis.java2d.Java2DWrapper;

public class Java2d {
    private static final boolean DEBUG = true;

    public static final Shape[] SHAPEARRAY = new Shape[0];

    public static final String[][] testDataset = new String[][] {
            {"point1", "POINT(10 11)"},
            {"multipoint1", "MULTIPOINT(10.25 11,10.5 11,10.75 11,11 11,11.25 11,11.5 11,11.75 11,12 11)"},
            {"linestring1", "LINESTRING(0 0,100 0,100 100,0 100)"},
            {"linestring2", "LINESTRING(-310 110,210 110,210 210,-310 210,-310 110)"},
            {"multilinestring", "MULTILINESTRING((0 0,10 10,20 0,30 10),(40 0,40 10,50 10,50 20,60 20))"},
    };

    static {
        new Java2DWrapper(); // make shure our driver is initialized
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException {

        if (args.length != 5) {
            System.err.println("Usage: java examples/TestJava2D dburl user pass tablename column");
            System.err.println();
            System.err.println("dburl has the following format:");
            System.err.println(Java2DWrapper.POSTGIS_PROTOCOL + "//HOST:PORT/DATABASENAME");
            System.err.println("tablename is 'jdbc_test' by default.");
            System.exit(1);
        }

        Shape[] geometries = read(args[0], args[1], args[2], "SELECT " + args[4] + " FROM " + args[3]);
        if (DEBUG) {
            System.err.println("read " + geometries.length + " geometries.");
        }
        if (geometries.length == 0) {
            if (DEBUG) {
                System.err.println("No geometries were read.");
            }
            return;
        }

        System.err.println("Painting...");
        Frame window = new Frame("PostGIS java2D demo");

        Canvas CV = new GisCanvas(geometries);

        window.add(CV);

        window.setSize(500, 500);

        window.addWindowListener(new EventHandler());

        window.setVisible(true);
    }

    static Rectangle2D calcbbox(Shape[] geometries) {
        Rectangle2D bbox = geometries[0].getBounds2D();
        for (int i = 1; i < geometries.length; i++) {
            bbox = bbox.createUnion(geometries[i].getBounds2D());
        }
        return bbox;
    }

    private static Shape[] read(String dburl, String dbuser, String dbpass, String query)
            throws ClassNotFoundException, SQLException {
        ArrayList geometries = new ArrayList();
        if (DEBUG) {
            System.err.println("Creating JDBC connection...");
        }
        Class.forName("org.postgresql.Driver");
        Connection conn = DriverManager.getConnection(dburl, dbuser, dbpass);

        if (DEBUG) {
            System.err.println("fetching geometries: " + query);
        }
        ResultSet r = conn.createStatement().executeQuery(query);

        while (r.next()) {
            final Shape current = (Shape) r.getObject(1);
            if (current != null) {
                geometries.add(current);
            }
        }
        conn.close();
        return (Shape[]) geometries.toArray(SHAPEARRAY);
    }

    public static class GisCanvas extends Canvas {
        /** Keep java 1.5 compiler happy */
        private static final long serialVersionUID = 1L;

        final Rectangle2D bbox;
        final Shape[] geometries;

        public GisCanvas(Shape[] geometries) {
            this.geometries = geometries;
            this.bbox = calcbbox(geometries);
            setBackground(Color.GREEN);
        }

        public void paint(Graphics og) {
            Graphics2D g = (Graphics2D) og;
            // Add 5% padding on all borders
            final double paddingTop =    bbox.getHeight() * 0.05;
            final double paddingBottom = bbox.getHeight() * 0.05;
            final double paddingLeft =   bbox.getWidth()  * 0.05;
            final double paddingRight =  bbox.getWidth()  * 0.05;
            // If the bounding box has negative coordinates, we need to offset by the negative coordinate
            final double offsetX = (bbox.getX() < 0) ? (0 - bbox.getX()) : 0;
            final double offsetY = (bbox.getY() < 0) ? (0 - bbox.getY()) : 0;
            // Scale by the bounding box and padding
            final double scaleX = (super.getWidth() - (paddingLeft + paddingRight)) / (bbox.getWidth());
            final double scaleY = (super.getHeight() - (paddingTop + paddingBottom)) / (bbox.getHeight());
            // Apply the transform parameters
            AffineTransform at = new AffineTransform();
            at.translate(paddingLeft, paddingTop);
            at.scale(scaleX, scaleY);
            at.translate(offsetX, offsetY);

            if (DEBUG) {
                System.err.println();
                System.err.println("paddingTop: " + paddingTop);
                System.err.println("paddingBottom: " + paddingBottom);
                System.err.println("paddingLeft: " + paddingLeft);
                System.err.println("paddingRight: " + paddingRight);
                System.err.println("offsetX: " + offsetX);
                System.err.println("offsetY: " + offsetY);
                System.err.println("scaleX: " + scaleX);
                System.err.println("scaleY: " + scaleY);
                System.err.println("bbox:  " + bbox);
                System.err.println("trans: " + at);
                System.err.println("new:   " + at.createTransformedShape(bbox).getBounds2D());
                System.err.println("visual:" + super.getBounds());
            }
            for (int i = 0; i < geometries.length; i++) {
                g.setPaint(Color.BLUE);
                final Shape shape = at.createTransformedShape(geometries[i]);
                g.fill(shape);
                g.setPaint(Color.ORANGE);
                g.draw(shape);
            }
        }
    }

    public static class EventHandler implements WindowListener {

        public void windowActivated(WindowEvent e) {//
        }

        public void windowClosed(WindowEvent e) {//
        }

        public void windowClosing(WindowEvent e) {
            e.getWindow().setVisible(false);
            System.exit(0);
        }

        public void windowDeactivated(WindowEvent e) {//
        }

        public void windowDeiconified(WindowEvent e) {//
        }

        public void windowIconified(WindowEvent e) {//
        }

        public void windowOpened(WindowEvent e) {//
        }
    }
}
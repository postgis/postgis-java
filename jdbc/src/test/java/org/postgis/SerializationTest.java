/*
 * ServerTest.java
 *
 * PostGIS extension for PostgreSQL JDBC driver - example and test classes
 *
 * (C) 2004 Paul Ramsey, pramsey@refractions.net
 *
 * (C) 2005 Markus Schaber, markus.schaber@logix-tt.com
 *
 * (C) 2017 Phillip Ross, phillip.w.g.ross@gmail.com
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

package org.postgis;


import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;


public class SerializationTest {


    @Test
    public void serializationCheckPGgeometry() throws Exception {
        try {
            new ObjectOutputStream(new ByteArrayOutputStream())
                    .writeObject(new PGgeometry("MULTIPOLYGON(((1 1,1 2,2 1,1 1)))"));
        }
        catch (NotSerializableException ex) {
            Assert.fail("serialization of PGgeometry failed: " + ex);
        }
    }


}

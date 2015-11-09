/*
 * SimpleJava2DWrapperTest.java
 *
 * SimpleJava2DWrapperTest for Java2D - relies on org.postgis V1.0.0+ package.
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
package org.postgis.java2d;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;


/**
 * This class serves as little more than an initial placeholder for establishing test classes within
 * this module's test suite.
 *
 * @author Phillip Ross {@literal <phillip.w.g.ross@gmail.com>}
 */
public class SimpleJava2DWrapperTest {

    private static final Logger logger = LoggerFactory.getLogger(SimpleJava2DWrapperTest.class);

    private static final String JAVA2D_WRAPPER_CLASS_NAME = "org.postgis.java2d.Java2DWrapper";


    @Test
    public void testWrapperClassLoad() throws Exception {
        logger.debug("Loading java2d wrapper class: {}", JAVA2D_WRAPPER_CLASS_NAME);
        Class.forName(JAVA2D_WRAPPER_CLASS_NAME);
    }


}
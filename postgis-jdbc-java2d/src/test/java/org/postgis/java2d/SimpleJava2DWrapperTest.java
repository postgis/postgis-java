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
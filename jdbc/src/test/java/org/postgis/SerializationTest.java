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

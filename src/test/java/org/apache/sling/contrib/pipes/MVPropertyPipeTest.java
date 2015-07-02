package org.apache.sling.contrib.pipes;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * testing normal case of MV Property Pipe
 */
public class MVPropertyPipeTest extends AbstractPipeTest {

    @Before
    public void setup() {
        super.setup();
        context.load().json("/mv.json", PATH_PIPE);
    }

    @Test
    public void testMV() throws Exception{
        Resource conf = context.resourceResolver().getResource(PATH_PIPE + "/working");
        Pipe pipe = plumber.getPipe(conf);
        Iterator<Resource> outputs = pipe.getOutput();
        Resource resource = outputs.next();
        assertNotNull(resource);
        assertEquals("Resource path should be the input", PATH_FRUITS + "/index", resource.getPath());
        String fruit = (String)pipe.getOutputBinding();
        assertEquals("First resource binding should be apple", "apple", fruit);
        resource = outputs.next();
        assertNotNull(resource);
        assertEquals("Resource path should be the input", PATH_FRUITS + "/index", resource.getPath());
        fruit = (String)pipe.getOutputBinding();
        assertEquals("Second resource binding should be banana", "banana", fruit);
    }

    @Test
    public void testNonWorkingMV() throws Exception{
        Resource conf = context.resourceResolver().getResource(PATH_PIPE + "/typo");
        Pipe pipe = plumber.getPipe(conf);
        assertFalse("There should not be next for a non existing resource", pipe.getOutput().hasNext());
        conf = context.resourceResolver().getResource(PATH_PIPE + "/notProperty");
        pipe = plumber.getPipe(conf);
        assertFalse("There should not be next for a resource that is not a property", pipe.getOutput().hasNext());
        conf = context.resourceResolver().getResource(PATH_PIPE + "/notMultiple");
        pipe = plumber.getPipe(conf);
        assertFalse("There should not be next for a property that is not multiple", pipe.getOutput().hasNext());
    }
}

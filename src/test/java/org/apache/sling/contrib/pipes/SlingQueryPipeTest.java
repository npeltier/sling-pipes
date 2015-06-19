package org.apache.sling.contrib.pipes;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * test the sling query pipe
 */
public class SlingQueryPipeTest extends AbstractPipeTest {

    @Before
    public void setup() {
        super.setup();
        context.load().json("/users.json", "/content/users");
        context.load().json("/slingQuery.json", PATH_PIPE);
    }

    @Test
    public void testChildren() throws Exception {
        Resource resource = context.resourceResolver().getResource(PATH_PIPE + "/" + NN_SIMPLE);
        SlingQueryPipe pipe = (SlingQueryPipe)plumber.getPipe(resource);
        assertNotNull("A Sling query pipe should be built out from the given configuration", pipe);
        Iterator<Resource> it = pipe.getOutput();
        assertTrue("this pipe should have an output", it.hasNext());
    }
}
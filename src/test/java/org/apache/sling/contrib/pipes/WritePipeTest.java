package org.apache.sling.contrib.pipes;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * test write
 */
public class WritePipeTest extends AbstractPipeTest {

    public static final String NN_PIPED = "piped";
    @Before
    public void setup() {
        super.setup();
        context.load().json("/write.json", PATH_PIPE);
    }

    @Test
    public void testSimple() throws Exception {
        Resource confResource = context.resourceResolver().getResource(PATH_PIPE + "/" + NN_SIMPLE);
        Pipe pipe = plumber.getPipe(confResource);
        assertNotNull("pipe should be found", pipe);
        assertTrue("this pipe should be marked as content modifier", pipe.modifiesContent());
        pipe.getOutput();
        context.resourceResolver().commit();
        ValueMap properties =  context.resourceResolver().getResource("/content/fruits/apple").adaptTo(ValueMap.class);
        assertTrue("There should be hasSeed set to true", properties.get("hasSeed", false));
        assertArrayEquals("Colors should be correctly set", new String[]{"green","red"},properties.get("colors", String[].class));
    }

    @Test
    public void testPiped() throws Exception {
        Resource confResource = context.resourceResolver().getResource(PATH_PIPE + "/" + NN_PIPED);
        Pipe pipe = plumber.getPipe(confResource);
        assertNotNull("pipe should be found", pipe);
        assertTrue("this pipe should be marked as content modifier", pipe.modifiesContent());
        Iterator<Resource> it = pipe.getOutput();
        assertTrue("There should be one result", it.hasNext());
        Resource resource = it.next();
        assertNotNull("The result should not be null", resource);
        assertEquals("The result should be the configured one in the piped write pipe", "/content/fruits", resource.getPath());
        context.resourceResolver().commit();
        ValueMap properties = resource.adaptTo(ValueMap.class);
        assertArrayEquals("First fruit should have been correctly instantiated & patched from nothing", new String[]{"apple"}, properties.get("fruits", String[].class));
        assertTrue("There should be a second result awaiting", it.hasNext());
        resource = it.next();
        assertNotNull("The second result should not be null", resource);
        assertEquals("The second result should be the configured one in the piped write pipe", "/content/fruits", resource.getPath());
        context.resourceResolver().commit();
        properties = resource.adaptTo(ValueMap.class);
        assertArrayEquals("Second fruit should have been correctly instantiated & patched, added to the first", new String[]{"apple","banana"}, properties.get("fruits", String[].class));
        assertArrayEquals("Fixed mv should be there", new String[]{"cabbage","carrot"}, properties.get("fixedVegetables", String[].class));
    }
}
package org.apache.sling.contrib.pipes;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @todo add license & javadoc :-)
 */
public class ContainerPipeTest extends AbstractPipeTest {

    private static final String NN_DUMMYTREE = "dummyTree";
    private static final String NN_OTHERTREE = "otherTree";

    @Before
    public void setup() {
        super.setup();
        context.load().json("/container.json", PATH_PIPE);
    }

    @Test
    public void testInstantiateExpression() throws Exception {
        Resource resource = context.resourceResolver().getResource(PATH_PIPE + "/" + NN_DUMMYTREE);
        ContainerPipe pipe = (ContainerPipe)plumber.getPipe(resource);
        Map<String, String> testMap = new HashMap<>();
        testMap.put("a", "apricots");
        testMap.put("b", "bananas");
        pipe.pipeBindings.put("test", testMap);
        String newExpression = pipe.instantiateExpression("test.a + ' and ' + test.b");
        assertEquals("expression should be correctly instantiated", "apricots and bananas", newExpression);
    }

    @Test
    public void testDummyTree() throws Exception {
        Resource resource = context.resourceResolver().getResource(PATH_PIPE + "/" + NN_DUMMYTREE);
        ContainerPipe pipe = (ContainerPipe)plumber.getPipe(resource);
        assertNotNull("A container pipe should be built out from the given configuration", pipe);
        Iterator<Resource> resourceIterator = pipe.getOutput();
        assertTrue("There should be some results", resourceIterator.hasNext());
        Resource firstResource = resourceIterator.next();
        assertNotNull("First resource should not be null", firstResource);
        assertEquals("First resource should be instantiated path with apple & pea",
                PATH_FRUITS + "/apple/isnota/pea/buttheyhavesamecolor",
                firstResource.getPath());
        assertTrue("There should still be another item", resourceIterator.hasNext());
        Resource secondResource = resourceIterator.next();
        assertNotNull("Second resource should not be null", secondResource);
        assertEquals("Second resource should be instantiated path with apple & carrot",
                PATH_FRUITS + "/apple/isnota/carrot/andtheircolorisdifferent",
                secondResource.getPath());
        assertTrue("There should still be another item", resourceIterator.hasNext());
        Resource thirdResource = resourceIterator.next();
        assertNotNull("Third resource should not be null", thirdResource);
        assertEquals("Third resource should be instantiated path with banana & pea",
                PATH_FRUITS + "/banana/isnota/pea/andtheircolorisdifferent",
                thirdResource.getPath());
        assertTrue("There should still be another item", resourceIterator.hasNext());
        Resource fourthResource = resourceIterator.next();
        assertNotNull("Fourth resource should not be null", fourthResource);
        assertEquals("fourthResource resource should be instantiated path with banana & carrot",
                PATH_FRUITS + "/banana/isnota/carrot/andtheircolorisdifferent",
                fourthResource.getPath());
        assertFalse("There should be no more items", resourceIterator.hasNext());
    }

    @Test
    public void testOtherTree() throws Exception {
        Resource resource = context.resourceResolver().getResource(PATH_PIPE + "/" + NN_OTHERTREE);
        ContainerPipe pipe = (ContainerPipe)plumber.getPipe(resource);
        Iterator<Resource> resourceIterator = pipe.getOutput();
        assertTrue("There should be some results", resourceIterator.hasNext());
        Resource firstResource = resourceIterator.next();
        assertNotNull("First resource should not be null", firstResource);
        assertEquals("First resource should be instantiated path with apple & pea",
                PATH_FRUITS + "/apple/isnota/pea/buttheyhavesamecolor",
                firstResource.getPath());
        assertTrue("There should still be another item", resourceIterator.hasNext());
        Resource secondResource = resourceIterator.next();
        assertNotNull("Second resource should not be null", secondResource);
        assertEquals("Second resource should be instantiated path with banana & pea",
                PATH_FRUITS + "/banana/isnota/pea/andtheircolorisdifferent",
                secondResource.getPath());
        assertFalse("There should be no more items", resourceIterator.hasNext());
    }
}
package org.apache.sling.contrib.pipes;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * @todo add license & javadoc :-)
 */
public class JsonPipeTest extends AbstractPipeTest {
    public static final String CONF = "/content/json/conf/weather";

    @Before
    public void setup() {
        super.setup();
        context.load().json("/json.json", "/content/json");
    }

    @Test
    public void testPipedJson() throws Exception{
        Resource resource = context.resourceResolver().getResource(CONF);
        Pipe pipe = plumber.getPipe(resource);
        Iterator<Resource> outputs = pipe.getOutput();
        outputs.next();
        Resource result = outputs.next();
        context.resourceResolver().commit();
        ValueMap properties = result.adaptTo(ValueMap.class);
        assertTrue("There should be a Paris property", properties.containsKey("Paris"));
        assertTrue("There should be a Bucharest property", properties.containsKey("Bucharest"));
    }
}
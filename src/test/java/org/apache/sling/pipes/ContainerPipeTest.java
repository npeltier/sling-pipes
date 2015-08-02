/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.pipes;

import org.apache.sling.api.resource.Resource;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * testing container with dummy child pipes
 */
public class ContainerPipeTest extends AbstractPipeTest {

    public static final String NN_DUMMYTREE = "dummyTree";
    public static final String NN_OTHERTREE = "otherTree";
    public static final String NN_ROTTENTREE = "rottenTree";
    private static final String NN_MOREBINDINGS = "moreBindings";

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
        pipe.getBindings().put("test", testMap);
        String newExpression = pipe.instantiateExpression("test.a + ' and ' + test.b");
        assertEquals("expression should be correctly instantiated", "apricots and bananas", newExpression);
    }

    @Test
    public void testInstantiateObject() throws Exception {
        Resource resource = context.resourceResolver().getResource(PATH_PIPE + "/" + NN_DUMMYTREE);
        ContainerPipe pipe = (ContainerPipe)plumber.getPipe(resource);
        Map<String, String> testMap = new HashMap<>();
        testMap.put("a", "apricots");
        testMap.put("b", "bananas");
        pipe.getBindings().put("test", testMap);
        String newExpression = (String)pipe.instantiateObject("test.a + ' and ' + test.b");
        assertEquals("expression should be correctly instantiated", "apricots and bananas", newExpression);
        Calendar cal = (Calendar)pipe.instantiateObject("new Date(2012,04,12)");
        assertNotNull("calendar should be instantiated", cal);
        assertEquals("year should be correct", 2012, cal.get(Calendar.YEAR));
        assertEquals("month should be correct", 4, cal.get(Calendar.MONTH));
        assertEquals("date should be correct", 11, cal.get(Calendar.DAY_OF_MONTH));
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

    @Test
    public void testRottenTree() throws Exception {
        Resource resource = context.resourceResolver().getResource(PATH_PIPE + "/" + NN_ROTTENTREE);
        ContainerPipe pipe = (ContainerPipe)plumber.getPipe(resource);
        Iterator<Resource> resourceIterator = pipe.getOutput();
        assertFalse("There shouldn't be any resource", resourceIterator.hasNext());
    }

    @Test
    public void testAdditionalBindings() throws Exception {
        Resource resource = context.resourceResolver().getResource(PATH_PIPE + "/" + NN_MOREBINDINGS);
        ContainerPipe pipe = (ContainerPipe)plumber.getPipe(resource);
        String expression = pipe.instantiateExpression("three");
        assertEquals("computed expression should be taking additional bindings 'three' in account", "3", expression);
    }

    @Test
    public void testAdditionalScript() throws Exception {
        context.load().binaryFile("/testSum.js","/content/test/testSum.js");
        Resource resource = context.resourceResolver().getResource(PATH_PIPE + "/" + NN_MOREBINDINGS);
        ContainerPipe pipe = (ContainerPipe)plumber.getPipe(resource);
        Object expression = pipe.instantiateObject("testSumFunction(1,2)");
        assertEquals("computed expression have testSum script's functionavailable", 3, expression);
    }
}
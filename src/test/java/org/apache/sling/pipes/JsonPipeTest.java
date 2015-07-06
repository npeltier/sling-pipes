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
import org.apache.sling.api.resource.ValueMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * testing json pipe with anonymous yahoo meteo API
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
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

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Property;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * pipe that writes to configured resource
 */
public class WritePipe extends BasePipe {
    private static final Logger logger = LoggerFactory.getLogger(WritePipe.class);
    public static final String RESOURCE_TYPE = "slingPipes/write";
    ValueMap writeMap;
    String resourceExpression;
    List<Resource> resources;
    Pattern addPatch = Pattern.compile("\\+\\[(.*)\\]");
    Pattern multi = Pattern.compile("\\[(.*)\\]");
    public static final List<String> IGNORED_PROPERTIES = Arrays.asList(new String[]{"jcr:lastModified", "jcr:primaryType", "jcr:created", "jcr:createdBy"});

    public WritePipe(Plumber plumber, Resource resource) throws Exception {
        super(plumber, resource);
        if (getConfiguration() == null){
            throw new Exception("write pipe is misconfigured: it should have a configuration node");
        }
        writeMap = getConfiguration().adaptTo(ValueMap.class);
        resourceExpression = getPath();
    }

    /**
     * convert the configured value in an actual one
     * @param expression
     * @return
     */
    protected Object computeValue(Resource resource, String key, Object expression) {
        if (expression instanceof String) {
            String value = parent != null ? parent.instantiateExpression((String) expression) : (String) expression;
            if (value != null) {
                Matcher patch = addPatch.matcher(value);
                if (patch.matches()) {
                    String newValue = patch.group(1);
                    String[] actualValues = resource.adaptTo(ValueMap.class).get(key, String[].class);
                    List<String> newValues = actualValues != null ? new LinkedList<>(Arrays.asList(actualValues)) : new ArrayList<>();
                    if (!newValues.contains(newValue)) {
                        newValues.add(newValue);
                    }
                    return newValues.toArray(new String[newValues.size()]);
                }
                Matcher multiMatcher = multi.matcher(value);
                if (multiMatcher.matches()) {
                    return multiMatcher.group(1).split(",");
                }
            }
            return value;
        }
        return expression;
    }

    @Override
    public boolean modifiesContent() {
        return true;
    }

    @Override
    public Iterator<Resource> getOutput() {
        try {
            Resource resource = getInput();
            if (resource != null) {
                ModifiableValueMap properties = resource.adaptTo(ModifiableValueMap.class);
                if (properties != null && writeMap != null) {
                    for (Map.Entry<String, Object> entry : writeMap.entrySet()) {
                        if (!IGNORED_PROPERTIES.contains(entry.getKey())) {
                            String key = parent != null ? parent.instantiateExpression(entry.getKey()) : entry.getKey();
                            Object value = computeValue(resource, key, entry.getValue());
                            if (value == null) {
                                //null value are not handled by modifiable value maps,
                                //removing the property if it exists
                                Resource propertyResource = resource.getChild(key);
                                if (propertyResource != null) {
                                    Property property = propertyResource.adaptTo(Property.class);
                                    if (property != null){
                                        property.remove();
                                    }
                                }
                            } else {
                                properties.put(key, value);
                            }
                        }
                    }
                }
                return super.getOutput();
            }
        } catch (Exception e) {
            logger.error("unable to write values, cutting pipe", e);
        }
        return Collections.emptyIterator();
    }
}

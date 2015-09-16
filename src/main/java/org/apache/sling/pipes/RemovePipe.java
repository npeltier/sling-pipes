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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.Iterator;

/**
 * this pipe tries to remove the input resource, abstracting its type,
 * returning parent of the input
 */
public class RemovePipe extends BasePipe {
    private static Logger logger = LoggerFactory.getLogger(RemovePipe.class);
    public static final String RESOURCE_TYPE = "slingPipes/rm";

    public RemovePipe(Plumber plumber, Resource resource) throws Exception {
        super(plumber, resource);
    }

    @Override
    public boolean modifiesContent() {
        return true;
    }

    @Override
    public Iterator<Resource> getOutput() {
        Resource resource = getInput();
        String parentPath = null;
        try {
            if (resource.adaptTo(Node.class) != null) {
                Node node = resource.adaptTo(Node.class);
                Node parent = node.getParent();
                node.remove();
                parentPath = parent.getPath();
            } else if (resource.adaptTo(Property.class) != null){
                Property property = resource.adaptTo(Property.class);
                parentPath = property.getParent().getPath();
                property.remove();
            }
        } catch (RepositoryException e){
            logger.error("unable to remove current resource {}", resource.getPath(), e);
        }
        if (parentPath != null) {
            return Collections.singleton(resolver.getResource(parentPath)).iterator();
        }
        return Collections.emptyIterator();
    }
}

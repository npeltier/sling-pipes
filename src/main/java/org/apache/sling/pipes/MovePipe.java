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
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Collections;
import java.util.Iterator;

/**
 * Does a JCR Move of a node, returns the resource corresponding to the moved node
 */
public class MovePipe extends BasePipe {
    Logger logger = LoggerFactory.getLogger(MovePipe.class);

    public static final String RESOURCE_TYPE = "slingPipes/mv";

    public MovePipe(Plumber plumber, Resource resource) throws Exception {
        super(plumber, resource);
    }

    @Override
    public boolean modifiesContent() {
        return true;
    }

    @Override
    public Iterator<Resource> getOutput() {
        Iterator<Resource> output = Collections.emptyIterator();
        Resource resource = getInput();
        if (resource != null && resource.adaptTo(Node.class) != null) {
            String targetPath = getExpr();
            try {
                logger.info("moving resource {} to {}", resource.getPath(), targetPath);
                if (!isDryRun()) {
                    resolver.adaptTo(Session.class).move(resource.getPath(), targetPath);
                    Resource target = resolver.getResource(targetPath);
                    output = Collections.singleton(target).iterator();
                }
            } catch (RepositoryException e){
                logger.error("unable to move the resource", e);
            }
        } else {
            logger.warn("bad configuration of the pipe, will do nothing");
        }
        return output;
    }
}

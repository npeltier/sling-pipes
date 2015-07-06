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
package org.apache.sling.contrib.pipes;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * pipe that outputs an authorizable resource based on the id set in expr
 */
public class AuthorizablePipe extends BasePipe {
    private static Logger logger = LoggerFactory.getLogger(AuthorizablePipe.class);
    public static final String RESOURCE_TYPE = "slingPipes/authorizable";

    UserManager userManager;
    ResourceResolver resolver;

    public AuthorizablePipe(Plumber plumber, Resource resource) throws Exception {
        super(plumber, resource);
        resolver = resource.getResourceResolver();
        userManager = resolver.adaptTo(UserManager.class);
    }

    @Override
    public Iterator<Resource> getOutput() {
        try {
            Authorizable auth = userManager.getAuthorizable(getExpr());
            if (auth != null) {
                Resource resource = resolver.getResource(auth.getPath());
                if (resource != null) {
                    List<Resource> resourceList = new ArrayList<>();
                    resourceList.add(resource);
                    return resourceList.iterator();
                }
            }
        } catch (Exception e){
            logger.error("unable to output authorizable based on expression", e);
        }
        return Collections.emptyIterator();
    }
}

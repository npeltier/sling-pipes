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
package org.apache.sling.contrib.pipes.impl;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.contrib.pipes.AuthorizablePipe;
import org.apache.sling.contrib.pipes.BasePipe;
import org.apache.sling.contrib.pipes.ContainerPipe;
import org.apache.sling.contrib.pipes.JsonPipe;
import org.apache.sling.contrib.pipes.MVPropertyPipe;
import org.apache.sling.contrib.pipes.Pipe;
import org.apache.sling.contrib.pipes.Plumber;
import org.apache.sling.contrib.pipes.SlingQueryPipe;
import org.apache.sling.contrib.pipes.WritePipe;
import org.apache.sling.contrib.pipes.XPathPipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * implements plumber interface, and registers default pipes
 */
@Component
@Service
public class PlumberImpl implements Plumber {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    Map<String, Class<? extends BasePipe>> registry;

    @Activate
    public void activate(){
        registry = new HashMap<String, Class<? extends BasePipe>>();
        registerPipe(ContainerPipe.RESOURCE_TYPE, ContainerPipe.class);
        registerPipe(SlingQueryPipe.RESOURCE_TYPE, SlingQueryPipe.class);
        registerPipe(WritePipe.RESOURCE_TYPE, WritePipe.class);
        registerPipe(JsonPipe.RESOURCE_TYPE, JsonPipe.class);
        registerPipe(MVPropertyPipe.RESOURCE_TYPE, MVPropertyPipe.class);
        registerPipe(AuthorizablePipe.RESOURCE_TYPE, AuthorizablePipe.class);
        registerPipe(XPathPipe.RESOURCE_TYPE, XPathPipe.class);
    }

    @Override
    public Pipe getPipe(Resource resource) {
        if ((resource == null) || !registry.containsKey(resource.getResourceType())) {
            log.error("Pipe configuration resource is either null, or its type is not registered");
        } else {
            try {
                Class<? extends Pipe> pipeClass = registry.get(resource.getResourceType());
                return pipeClass.getDeclaredConstructor(Plumber.class, Resource.class).newInstance(this, resource);
            } catch (Exception e) {
                log.error("Unable to properly instantiate the pipe configured in {}", resource.getPath());
            }
        }
        return null;
    }

    @Override
    public Set<Resource> execute(ResourceResolver resolver, String path, boolean save) throws Exception {
        Resource pipeResource = resolver.getResource(path);
        Pipe pipe = getPipe(pipeResource);
        if (pipe == null) {
            throw new Exception("unable to build pipe based on configuration at " + path);
        }
        log.info("[{}] execution starts", pipe.getName());
        Set<Resource> set = new HashSet<>();
        for (Iterator<Resource> it = pipe.getOutput(); it.hasNext();){
            Resource resource = it.next();
            if (resource != null) {
                log.debug("[{}] retrieved {}", pipe.getName(), resource.getPath());
                set.add(resource);
            }
        }
        if  (pipe.modifiesContent() && save && resolver.hasChanges()){
            log.info("[{}] saving changes...", pipe.getName());
            resolver.commit();
        }
        log.info("[{}] done executing.", pipe.getName());
        return set;
    }

    @Override
    public void registerPipe(String type, Class<? extends BasePipe> pipeClass) {
        registry.put(type, pipeClass);
    }
}

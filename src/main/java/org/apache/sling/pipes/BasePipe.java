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

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * provides generic utilities for a pipe
 */
public class BasePipe implements Pipe {
    public static final String RESOURCE_TYPE = "slingPipes/base";

    protected ResourceResolver resolver;
    protected ValueMap properties;
    protected Resource resource;
    protected ContainerPipe parent;

    @Override
    public ContainerPipe getParent() {
        return parent;
    }

    @Override
    public void setParent(ContainerPipe parent) {
        this.parent = parent;
    }

    protected Plumber plumber;
    private String name;

    public BasePipe(Plumber plumber, Resource resource) throws Exception {
        this.resource = resource;
        properties = resource.adaptTo(ValueMap.class);
        resolver = resource.getResourceResolver();
        this.plumber = plumber;
        name = properties.get(PN_NAME, resource.getName());
    }

    @Override
    public boolean modifiesContent() {
        return false;
    }

    public String getName(){
        return name;
    }

    /**
     * Get pipe's expression, instanciated or not
     * @return
     */
    public String getExpr(){
        String rawExpression = properties.get(PN_EXPR, "");
        if(parent != null) {
            return parent.instantiateExpression(rawExpression);
        }
        return rawExpression;
    }

    /**
     * Get pipe's path, instanciated or not
     * @return
     */
    public String getPath() {
        String rawPath = properties.get(PN_PATH, "");
        if(parent != null) {
            return parent.instantiateExpression(rawPath);
        }
        return rawPath;
    }

    @Override
    public Resource getConfiguredResource() {
        Resource resource = null;
        String path = getPath();
        if (StringUtils.isNotBlank(path)){
            resource = resolver.getResource(path);
        }
        return resource;
    }

    @Override
    public Resource getInput() {
        Resource resource = getConfiguredResource();
        if (resource == null && parent != null){
            Pipe previousPipe = parent.getPreviousPipe(this);
            if (previousPipe != null) {
                return parent.getExecutedResource(previousPipe.getName());
            }
        }
        return resource;
    }


    @Override
    public Object getOutputBinding() {
        if (parent != null){
            Resource resource = parent.getExecutedResource(getName());
            if (resource != null) {
                return resource.adaptTo(ValueMap.class);
            }
        }
        return null;
    }

    /**
     * default execution, just returns current resource
     * @return
     */
    public Iterator<Resource> getOutput(){
        List<Resource> resourceList = new ArrayList<>();
        resourceList.add(getInput());
        return resourceList.iterator();
    }

    /**
     * Get configuration node
     * @return
     */
    public Resource getConfiguration() {
        return resource.getChild(NN_CONF);
    }
}

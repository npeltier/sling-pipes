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
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.json.io.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Servlet executing plumber for a pipe path given as 'path' parameter,
 * it can also be launched against a container pipe resource directly (no need for path parameter)
 *
 */
@SlingServlet(resourceTypes = {Plumber.RESOURCE_TYPE, ContainerPipe.RESOURCE_TYPE}, methods={"GET","POST"}, extensions = {"json"})
public class PlumberServlet extends SlingAllMethodsServlet {
    Logger log = LoggerFactory.getLogger(this.getClass());

    protected static final String PARAM_PATH = "path";

    public static final String PATH_KEY = "path";

    protected static final String PARAM_BINDINGS = "bindings";

    protected static final String PARAM_WRITER = "writer";

    @Reference
    Plumber plumber;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        execute(request, response, false);
    }

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        execute(request, response, true);
    }

    protected void execute(SlingHttpServletRequest request, SlingHttpServletResponse response, boolean writeAllowed) throws ServletException {
        String path = request.getResource().getResourceType().equals(Plumber.RESOURCE_TYPE) ? request.getParameter(PARAM_PATH) : request.getResource().getPath();
        try {
            if (StringUtils.isBlank(path)) {
                throw new Exception("path should be provided");
            }
            String paramBindings = request.getParameter(PARAM_BINDINGS);
            Map additionalBindings = null;
            if (StringUtils.isNotBlank(paramBindings)){
                try {
                    JSONObject bindingJSON = new JSONObject(paramBindings);
                    additionalBindings = new HashMap<>();
                    for (Iterator<String> keys = bindingJSON.keys(); keys.hasNext();){
                        String key = keys.next();
                        additionalBindings.put(key, bindingJSON.get(key));
                    }
                } catch (Exception e){
                    log.error("Unable to retrieve bindings information", e);
                }
            }

            String paramWriter = request.getParameter(PARAM_WRITER);
            JSONObject writerObj = null;
            if (StringUtils.isNotBlank(paramWriter)){
                try {
                    writerObj = new JSONObject(paramWriter);
                } catch (Exception e) {
                    log.error("Unable to retrieve the writer object");
                }
            }
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/json");
            JSONWriter writer = new JSONWriter(response.getWriter());
            ResourceResolver resolver = request.getResourceResolver();
            Resource pipeResource = resolver.getResource(path);
            Pipe pipe = plumber.getPipe(pipeResource);
            if (!writeAllowed && pipe.modifiesContent()) {
                throw new Exception("This pipe modifies content, you should use a POST request");
            }
            writer.array();
            if (writerObj != null && pipe instanceof ContainerPipe) {
                ContainerPipe container = (ContainerPipe)pipe;
                container.addBindings(additionalBindings);
                Iterator<Resource> resourceIterator = container.getOutput();
                while (resourceIterator.hasNext()){
                    Resource resource = resourceIterator.next();
                    writer.object();
                    writer.key(PATH_KEY).value(resource.getPath());
                    Iterator<String> keys = writerObj.keys();
                    while (keys.hasNext()){
                        String key = keys.next();
                        writer.key(key).value(container.instantiateObject(writerObj.getString(key)));
                    }
                    writer.endObject();
                }
            } else {
                Set<String> resources = plumber.execute(resolver, pipe, additionalBindings, true);
                for (String resource : resources) {
                    writer.value(resource);
                }
            }
            writer.endArray();
            response.flushBuffer();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
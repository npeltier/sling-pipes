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
package org.apache.sling.pipes.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.io.JSONWriter;
import org.apache.sling.pipes.Plumber;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Set;

/**
 * Servlet executing plumber for a pipe path given as 'path' parameter
 */
@SlingServlet(resourceTypes = {Plumber.RESOURCE_TYPE}, methods={"POST"}, extensions = {"json"})
public class PlumberServlet extends SlingAllMethodsServlet {

    private static final String PARAM_PATH = "path";

    @Reference
    Plumber plumber;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        String path = request.getParameter(PARAM_PATH);
        try {
            if (StringUtils.isBlank(path)) {
                throw new Exception("path parameter should be provided");
            }
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/json");
            JSONWriter writer = new JSONWriter(response.getWriter());
            Set<Resource> resources = plumber.execute(request.getResourceResolver(), path, true);
            writer.array();
            for (Resource resource : resources){
                writer.value(resource.getPath());
            }
            writer.endArray();
            response.flushBuffer();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}

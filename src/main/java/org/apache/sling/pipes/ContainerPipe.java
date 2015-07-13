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

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This pipe executes the pipes it has in its configuration, chaining their result, and
 * modifying each contained pipe's expression with its context
 */
public class ContainerPipe extends BasePipe {
    private static final Logger log = LoggerFactory.getLogger(ContainerPipe.class);

    public static final String RESOURCE_TYPE = "slingPipes/container";

    Map<String, Pipe> pipes = new HashMap<>();

    Map<String, Resource> outputResources = new HashMap<>();

    Map<String, String> pathBindings = new HashMap<>();

    PipeBindings pipeBindings = new PipeBindings();

    List<Pipe> pipeList = new ArrayList<>();

    List<Pipe> reversePipeList = new ArrayList<>();

    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

    public static final String PATH_BINDING = "path";

    public ContainerPipe(Plumber plumber, Resource resource) throws Exception{
        super(plumber, resource);
        for (Iterator<Resource> childPipeResources = getConfiguration().listChildren(); childPipeResources.hasNext();){
            Resource pipeResource = childPipeResources.next();
            Pipe pipe = plumber.getPipe(pipeResource);
            if (pipe == null) {
                log.error("configured pipe {} is either not registered, or not computable by the plumber", pipeResource.getPath());
            } else {
                pipe.setParent(this);
                pipes.put(pipe.getName(), pipe);
                pipeList.add(pipe);
                reversePipeList.add(pipe);
            }
        }
        Collections.reverse(reversePipeList);

        //add path bindings where path.MyPipe will give MyPipe current resource path
        pipeBindings.put(PATH_BINDING, pathBindings);
    }

    /**
     * Expression is a function of variables from execution context, that
     * we implement here as a String
     * @param expr
     * @return
     */
    public String instantiateExpression(String expr){
        try {
            return (String)engine.eval(expr, pipeBindings);
        } catch (ScriptException e) {
            log.error("Unable to evaluate the script", e);
        }
        return expr;
    }

    /**
     * Instantiate object from expression
     * @param expr
     * @return
     */
    public Object instantiateObject(String expr){
        try {
            Object result = engine.eval(expr, pipeBindings);
            if (! (result instanceof String)) {
                JsDate jsDate = ((Invocable) engine).getInterface(result, JsDate.class);
                if (jsDate != null ) {
                    Date date = new Date(jsDate.getTime() + jsDate.getTimezoneOffset() * 60 * 1000);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    return cal;
                }
            }
            return result;
        } catch (ScriptException e) {
            log.error("Unable to evaluate the script", e);
        }
        return expr;
    }

    @Override
    public boolean modifiesContent() {
        for (Pipe pipe : pipes.values()){
            if (pipe.modifiesContent()){
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<Resource> getOutput()  {
        return new ContainerResourceIterator(this);
    }

    /**
     * Update current resource of a given pipe, and appropriate binding
     * @param pipe
     * @param resource
     */
    public void updateBindings(Pipe pipe, Resource resource) {
        outputResources.put(pipe.getName(), resource);
        if (resource != null) {
            pathBindings.put(pipe.getName(), resource.getPath());
        }
        pipeBindings.put(pipe.getName(), pipe.getOutputBinding());
    }

    /**
     *
     * @param name
     * @return
     */
    public Resource getExecutedResource(String name) {
        return outputResources.get(name);
    }

    /**
     * Returns the pipe immediately before the given pipe, null if it's the first
     * @param pipe
     * @return
     */
    public Pipe getPreviousPipe(Pipe pipe){
        Pipe previousPipe = null;
        for (Pipe candidate : pipeList){
            if (candidate.equals(pipe)){
                return previousPipe;
            }
            previousPipe = candidate;
        }
        return null;
    }

    /**
     * Return the first pipe in the container
     * @return
     */
    public Pipe getFirstPipe() {
        return pipeList.iterator().next();
    }

    /**
     * Return the last pipe in the container
     * @return
     */
    public Pipe getLastPipe() {
        return reversePipeList.iterator().next();
    }

    public Resource getOuputResource() {
        return getExecutedResource(getLastPipe().getName());
    }

    /**
     * Container Iterator, that iterates through the whole chain
     * of pipes, returning the result resources of the end of the chain
     */
    static class ContainerResourceIterator implements Iterator<Resource> {
        /**
         * map name -> iterator
         */
        Map<Pipe, Iterator<Resource>> iterators;

        /**
         * container pipe
         */
        ContainerPipe container;

        boolean computedCursor = false;
        boolean hasNext = false;
        int cursor = 0;

        ContainerResourceIterator(ContainerPipe containerPipe) {
            container = containerPipe;
            iterators = new HashMap<>();
            Pipe firstPipe = container.getFirstPipe();
            //we initialize the first iterator the only one not to be updated
            iterators.put(firstPipe, firstPipe.getOutput());
        }

        /**
         * go up and down the container iterators until cursor is at 0 (first pipe) with no
         * more resources, or at length - 1 (last pipe) with a next one
         * @return
         */
        private boolean updateCursor(){
            Pipe currentPipe = container.pipeList.get(cursor);
            Iterator<Resource> it = iterators.get(currentPipe);
            do {
                // go up to at best reach the last pipe, updating iterators & bindings of the
                // all intermediates, if an intermediate pipe is not outputing anything
                // anymore, stop.
                while (it.hasNext() && cursor < container.pipeList.size() - 1) {
                    Resource resource = it.next();
                    container.updateBindings(currentPipe, resource);
                    //now we update the following pipe output with that new context
                    Pipe nextPipe = container.pipeList.get(++cursor);
                    iterators.put(nextPipe, nextPipe.getOutput());
                    currentPipe = nextPipe;
                    it = iterators.get(currentPipe);
                }
                //go down (or stay) to the first pipe having a next item
                while (!it.hasNext() && cursor > 0) {
                    currentPipe = container.pipeList.get(--cursor);
                    it = iterators.get(currentPipe);
                }
            } while (it.hasNext() && cursor < container.pipeList.size() - 1);
            //2 choices here:
            // either cursor is at 0 with no resource left: end,
            // either cursor is on last pipe with a resource left: hasNext
            return cursor > 0;
        }

        /**
         * we need to find the first "path" from first pipe to the last
         * where each pipe returns something, if no "path", this pipe is
         * done, other wise we must have updated iterators (next is allowed
         * up to the pipe before the last), and return true
         * @return
         */
        @Override
        public boolean hasNext() {
            if (! computedCursor) {
                hasNext = updateCursor();
            }
            return hasNext;
        }

        @Override
        public Resource next() {
            hasNext = computedCursor && hasNext || hasNext();
            if (hasNext) {
                computedCursor = false;
                hasNext = false;
                return iterators.get(container.getLastPipe()).next();
            }
            return null;
        }
    }

    /**
     * Container bindings when evaluating a new expression
     */
    static class PipeBindings extends HashMap<String, Object> implements Bindings {

    }

    /**
     * interface mapping a javascript date
     */
    public interface JsDate {
        long getTime();
        int getTimezoneOffset();
    }

}

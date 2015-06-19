package org.apache.sling.contrib.pipes;

import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Collections;
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

    PipeBindings pipeBindings = new PipeBindings();

    List<Pipe> pipeList = new ArrayList<>();

    List<Pipe> reversePipeList = new ArrayList<>();

    ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

    public ContainerPipe(Plumber plumber, Resource resource) throws Exception{
        super(plumber, resource);
        for (Iterator<Resource> childPipeResources = getConfiguration().listChildren(); childPipeResources.hasNext();){
            Resource pipeResource = childPipeResources.next();
            Pipe pipe = plumber.getPipe(pipeResource);
            if (pipe == null) {
                log.error("configured pipe {} is not registered by the plumber", pipeResource.getPath());
            } else {
                pipe.setParent(this);
                pipes.put(pipe.getName(), pipe);
                pipeList.add(pipe);
                reversePipeList.add(pipe);
            }
        }
        Collections.reverse(reversePipeList);
    }

    /**
     * Expression is a function of variables from execution context, that
     * we implement here
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
        ContainerPipe mainPipe;

        boolean firstHit = true;

        ContainerResourceIterator(ContainerPipe containerPipe) {
            mainPipe = containerPipe;
            iterators = new HashMap<>();
            for (Pipe pipe : mainPipe.pipeList){
                Iterator<Resource> iterator = pipe.getOutput();
                iterators.put(pipe, iterator);
                Resource resource = null;
                if (iterator.hasNext()){
                    resource = iterator.next();
                    mainPipe.updateBindings(pipe, resource);
                }
            }
        }

        /**
         * we only return
         * @return
         */
        @Override
        public boolean hasNext() {
            for (Pipe pipe : mainPipe.pipeList){
                if (iterators.get(pipe).hasNext()){
                    return true;
                }
            }
            return false;
        }

        @Override
        public Resource next() {
            if (firstHit) {
                firstHit = false;
                return mainPipe.getOuputResource();
            } else {
                for (Pipe pipe : mainPipe.reversePipeList) {
                    Iterator<Resource> iterator = iterators.get(pipe);
                    if (iterator.hasNext()) {
                        //now we need to refresh all iterators from this one, that were not having any next items
                        Resource resource = iterator.next();
                        mainPipe.updateBindings(pipe, resource);
                        int currentIndex = mainPipe.reversePipeList.size() - mainPipe.reversePipeList.indexOf(pipe) - 1;
                        for (Pipe nextPipe : mainPipe.pipeList) {
                            if (mainPipe.pipeList.indexOf(nextPipe) > currentIndex) {
                                Iterator<Resource> freshIterator = nextPipe.getOutput();
                                iterators.put(nextPipe, freshIterator);
                                Resource freshResource = null;
                                if (freshIterator.hasNext()) {
                                    freshResource = freshIterator.next();
                                }
                                mainPipe.updateBindings(nextPipe, freshResource);
                            }
                        }
                        return mainPipe.getOuputResource();
                    }
                }
            }
            return null;
        }
    }

    /**
     * Container bindings when evaluating a new expression
     */
    static class PipeBindings extends HashMap<String, Object> implements Bindings {

    }
}

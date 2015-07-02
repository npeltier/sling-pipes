package org.apache.sling.contrib.pipes;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.Set;

/**
 * Plumber is an osgi service aiming to make pipes available to the sling system, in order to
 */
public interface Plumber {

    public static final String RESOURCE_TYPE = "slingPipes/plumber";

    /**
     * Instantiate a pipe from the given resource and returns it
     * @param resource
     * @return
     */
    Pipe getPipe(Resource resource);

    /**
     * Executes a pipe at a certain path
     * @param resolver resource resolver with which pipe will be executed
     * @param path path of a valid pipe configuration
     * @param save in case that pipe writes anything, wether the plumber should save changes or not
     * @return
     */
    Set<Resource> execute(ResourceResolver resolver, String path, boolean save) throws Exception;

    /**
     * Registers
     * @param type
     * @param pipeClass
     */
    void registerPipe(String type, Class<? extends BasePipe> pipeClass);
}

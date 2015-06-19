package org.apache.sling.contrib.pipes;

import org.apache.sling.api.resource.Resource;

/**
 * @todo add license & javadoc :-)
 */
public interface Plumber {
    /**
     * Instantiate a pipe from the given resource and returns it
     * @param resource
     * @return
     */
    Pipe getPipe(Resource resource);

    /**
     * Registers
     * @param type
     * @param pipeClass
     */
    void registerPipe(String type, Class<? extends AbstractPipe> pipeClass);
}

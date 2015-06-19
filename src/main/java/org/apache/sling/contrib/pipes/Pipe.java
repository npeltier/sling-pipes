package org.apache.sling.contrib.pipes;

import org.apache.sling.api.resource.Resource;

import javax.jcr.RepositoryException;
import java.util.Iterator;

/**
 * Pipe interface
 */
public interface Pipe {
    /**
     * Name of the pipe
     */
    public static final String PN_NAME = "name";

    /**
     * expression of the pipe, usage depends on the pipe implementation
     */
    public static final String PN_EXPR = "expr";

    /**
     * resource's path associated to the path, usage depends on the pipe implementation
     */
    public static final String PN_PATH = "path";

    /**
     * Node name for the pipe's configuration
     */
    public static final String NN_CONF = "conf";

    /**
     * returns true if that pipe will modify content during its execution
     * @return
     */
    boolean modifiesContent();

    /**
     * Return the name of that pipe
     * @return
     */
    String getName();

    /**
     * Set parent
     */
    void setParent(ContainerPipe parent);

    /**
     * Return parent's pipe (can be null)
     * @return
     */
    ContainerPipe getParent();

    /**
     * Get the pipe's root resource
     * @return
     */
    Resource getRootResource();

    /**
     * returns the binding output associated with the given current resource
     * @return
     */
    Object getBindingObject();

    /**
     * Executes the pipe, can be contained in a parent or not
     * @return
     */
    Iterator<Resource> execute();
}
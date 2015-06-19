package org.apache.sling.contrib.pipes;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

/**
 * provides generic utilities for a pipe
 */
public abstract class AbstractPipe implements Pipe {
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

    public AbstractPipe(Plumber plumber, Resource resource) throws Exception {
        this.resource = resource;
        properties = resource.adaptTo(ValueMap.class);
        resolver = resource.getResourceResolver();
        this.plumber = plumber;
        name = properties.get(PN_NAME, resource.getName());
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
            parent.instantiateExpression(rawExpression);
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

    public Resource getRootResource() {
        Resource resource = null;
        String path = getPath();
        if (StringUtils.isBlank(path) && parent != null){
            //in that case, we try to take the previous pipe (if any)'s resource
            Pipe previousPipe = parent.getPreviousPipe(this);
            if (previousPipe != null) {
                resource = previousPipe.getRootResource();
            }
        } else {
            resource = resolver.getResource(path);
        }
        return resource;
    }

    @Override
    public Object getBindingObject() {
        if (parent != null){
            Resource resource = parent.getCurrentResource(getName());
            if (resource != null) {
                return resource.adaptTo(ValueMap.class);
            }
        }
        return null;
    }

    /**
     * Get configuration node
     * @return
     */
    public Resource getConfiguration() {
        return resource.getChild(NN_CONF);
    }
}

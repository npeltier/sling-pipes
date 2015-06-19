package org.apache.sling.contrib.pipes.dummies;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.contrib.pipes.AbstractPipe;
import org.apache.sling.contrib.pipes.Plumber;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @todo add license & javadoc :-)
 */
public class Dummy extends AbstractPipe {
    public Dummy(Plumber plumber, Resource resource) throws Exception {
        super(plumber, resource);
    }

    @Override
    public boolean modifiesContent() {
        return false;
    }

    @Override
    public Iterator<Resource> execute() {
        List<Resource> resourceList = new ArrayList<>();
        resourceList.add(getRootResource());
        return resourceList.iterator();
    }
}

package org.apache.sling.contrib.pipes.dummies;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.contrib.pipes.BasePipe;
import org.apache.sling.contrib.pipes.Plumber;

/**
 * @todo add license & javadoc :-)
 */
public class Dummy extends BasePipe {
    public Dummy(Plumber plumber, Resource resource) throws Exception {
        super(plumber, resource);
    }
}

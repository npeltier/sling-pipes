package org.apache.sling.contrib.pipes.dummies;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.contrib.pipes.BasePipe;
import org.apache.sling.contrib.pipes.Plumber;

import java.util.Iterator;

/**
 * this pipe has nothing in output
 */
public class DummyNull extends BasePipe {
    public DummyNull(Plumber plumber, Resource resource) throws Exception {
        super(plumber, resource);
    }

    @Override
    public Object getOutputBinding() {
        return null;
    }

    @Override
    public Iterator<Resource> getOutput() {
        return new Iterator<Resource>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public Resource next() {
                return null;
            }
        };
    }
}

package org.apache.sling.contrib.pipes.dummies;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.contrib.pipes.BasePipe;
import org.apache.sling.contrib.pipes.Plumber;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import java.util.Collections;
import java.util.Iterator;

/**
 * dummy search reads its conf node children and returns them.
 */
public class DummySearch extends BasePipe {

    public DummySearch(Plumber plumber, Resource resource) throws Exception {
        super(plumber, resource);
    }

    @Override
    public boolean modifiesContent() {
        return false;
    }

    @Override
    public Iterator<Resource> getOutput() {
        try {
            NodeIterator iterator = getConfiguration().adaptTo(Node.class).getNodes();
            return new Iterator<Resource>() {

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Resource next() {
                    try {
                        return resolver.getResource(iterator.nextNode().getPath());
                    } catch (Exception e) {

                    }
                    return null;
                }
            };
        } catch (Exception e){

        }
        return Collections.<Resource>emptyList().iterator();
    }
}

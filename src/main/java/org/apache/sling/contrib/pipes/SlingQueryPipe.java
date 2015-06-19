package org.apache.sling.contrib.pipes;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.query.SlingQuery;

import static org.apache.sling.query.SlingQuery.$;

import java.util.Collections;
import java.util.Iterator;

/**
 * this pipe uses SlingQuery to filters children (filter defined in expr property) of
 * a resource (defined in the path property)
 */
public class SlingQueryPipe extends AbstractPipe {

    public final static String RESOURCE_TYPE = "slingPipes/slingQuery";

    public SlingQueryPipe(Plumber plumber, Resource resource) throws Exception {
        super(plumber, resource);
    }

    @Override
    public boolean modifiesContent() {
        return false;
    }

    public Iterator<Resource> execute() {
        Resource resource = resolver.getResource(getPath());
        if (resource != null) {
            SlingQuery query = $(resource).children(getExpr());
            return query.iterator();
        }
        return Collections.emptyIterator();
    }
}

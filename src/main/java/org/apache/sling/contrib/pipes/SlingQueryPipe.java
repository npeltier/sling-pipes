package org.apache.sling.contrib.pipes;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.query.SlingQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.sling.query.SlingQuery.$;

import java.util.Collections;
import java.util.Iterator;

/**
 * this pipe uses SlingQuery to filters children (filter defined in expr property) of
 * a resource (defined in the path property)
 */
public class SlingQueryPipe extends BasePipe {
    private static Logger logger = LoggerFactory.getLogger(SlingQueryPipe.class);

    public final static String RESOURCE_TYPE = "slingPipes/slingQuery";

    public SlingQueryPipe(Plumber plumber, Resource resource) throws Exception {
        super(plumber, resource);
    }

    @Override
    public boolean modifiesContent() {
        return false;
    }

    public Iterator<Resource> getOutput() {
        Resource resource = getInput();
        if (resource != null) {
            String queryExpression = getExpr();
            SlingQuery query = $(resource).children(getExpr());
            logger.info("[sling query]: executing $({}).children({})", resource.getPath(), queryExpression);
            return query.iterator();
        }
        return Collections.emptyIterator();
    }
}

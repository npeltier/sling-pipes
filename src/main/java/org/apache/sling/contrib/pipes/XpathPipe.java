package org.apache.sling.contrib.pipes;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;

import javax.jcr.query.Query;
import java.util.Collections;
import java.util.Iterator;

/**
 * generates output based on an xpath query (no input is considered)
 */
public class XPathPipe extends BasePipe {

    public static final String RESOURCE_TYPE = "slingPipes/xpath";

    public XPathPipe(Plumber plumber, Resource resource) throws Exception {
        super(plumber, resource);
    }

    @Override
    public Iterator<Resource> getOutput() {
        String query = getExpr();
        if (StringUtils.isNotBlank(query)){
            return resource.getResourceResolver().findResources(query, Query.XPATH);
        }
        return Collections.emptyIterator();
    }
}

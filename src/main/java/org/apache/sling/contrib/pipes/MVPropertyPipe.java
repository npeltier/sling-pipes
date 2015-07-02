package org.apache.sling.contrib.pipes;

import org.apache.sling.api.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Value;
import java.util.Arrays;
import java.util.Iterator;

/**
 * reads input MV property, outputs N times the input parent node resource, where N is the number of
 * values in the property, outputs each value in the bindings
 */
public class MVPropertyPipe extends BasePipe {
    private static Logger logger = LoggerFactory.getLogger(MVPropertyPipe.class);
    public static final String RESOURCE_TYPE = "slingPipes/mv";

    public MVPropertyPipe(Plumber plumber, Resource resource) throws Exception {
        super(plumber, resource);
    }

    MVResourceIterator iterator;

    @Override
    public Iterator<Resource> getOutput() {
        iterator = new MVResourceIterator(getInput());
        return iterator;
    }

    @Override
    public Object getOutputBinding() {
        if (iterator != null) {
            Value value = iterator.getCurrentValue();
            try {
                switch (value.getType()) {
                    case PropertyType.STRING: {
                        return value.getString();
                    }
                    default: {
                        return value.toString();
                    }
                }
            } catch (Exception e) {
                logger.error("current value format is not supported", e);
            }
            return value.toString();
        }
        return null;
    }

    static class MVResourceIterator implements Iterator<Resource> {
        Resource resource;
        Value currentValue;
        Iterator<Value> itValue;

        public MVResourceIterator(Resource resource){
            try {
                this.resource = resource;
                Property mvProperty = resource.adaptTo(Property.class);
                if (mvProperty == null) {
                    throw new Exception("input resource " + resource.getPath() + " is supposed to be a property");
                }
                if (!mvProperty.isMultiple()) {
                    throw new Exception("given property " + resource.getPath() + " is supposed to be multiple");
                }
                itValue = Arrays.asList(mvProperty.getValues()).iterator();
            } catch (Exception e) {
                logger.error("unable to setup mv iterator", e);
            }
        }

        @Override
        public boolean hasNext() {
            return itValue != null ? itValue.hasNext() : false;
        }

        public Value getCurrentValue() {
            return currentValue;
        }

        @Override
        public Resource next() {
            if (itValue != null) {
                currentValue = itValue.next();
            }
            return resource;
        }
    }
}

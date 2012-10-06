package org.deegree.time.complex;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.GMLObjectType;
import org.deegree.commons.tom.gml.property.Property;

public class TimeTopologyComplex implements TimeComplex {

    @Override
    public String getId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GMLObjectType getType() {
        throw new UnsupportedOperationException ("Implement me");
    }

    @Override
    public List<Property> getProperties() {
        throw new UnsupportedOperationException ("Implement me");
    }

    @Override
    public List<Property> getProperties( QName propName ) {
        throw new UnsupportedOperationException ("Implement me");
    }
}

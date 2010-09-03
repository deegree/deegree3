//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.feature;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElementContent;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.property.Property;
import org.deegree.feature.xpath.FeatureXPath;
import org.deegree.feature.xpath.GMLObjectNode;
import org.deegree.feature.xpath.XPathNode;
import org.deegree.filter.expression.PropertyName;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.StandardGMLFeatureProps;
import org.deegree.gml.props.GMLStdProps;
import org.jaxen.JaxenException;
import org.jaxen.XPath;

/**
 * Abstract base class for common {@link Feature} implementations.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public abstract class AbstractFeature implements Feature {

    /** Stores the default GML properties that every GML feature allows for (gml:name, gml:description, ...). */
    protected StandardGMLFeatureProps standardProps;

    @Override
    public GMLStdProps getGMLProperties() {
        return standardProps;
    }

    public TypedObjectNode[] evalXPath( PropertyName propName, GMLVersion version )
                            throws JaxenException {

        // simple property with just a simple element step?
        QName simplePropName = propName.getAsQName();
        if ( simplePropName != null ) {
            return getProperties( simplePropName, version );
        }

        // no. activate the full xpath machinery
        XPath xpath = new FeatureXPath( propName.getPropertyName(), this, version );
        xpath.setNamespaceContext( propName.getNsContext() );
        List<?> selectedNodes = xpath.selectNodes( new GMLObjectNode<Feature>( null, this, version ) );

        TypedObjectNode[] resultValues = new TypedObjectNode[selectedNodes.size()];
        int i = 0;
        for ( Object node : selectedNodes ) {
            if ( node instanceof XPathNode<?> ) {
                resultValues[i++] = ( (XPathNode<?>) node ).getValue();
            } else if ( node instanceof String || node instanceof Double || node instanceof Boolean ) {
                resultValues[i++] = new PrimitiveValue( node );
            } else {
                throw new RuntimeException( "Internal error. Encountered unexpected value of type '"
                                            + node.getClass().getName() + "' (=" + node + ") during XPath-evaluation." );
            }
        }
        return resultValues;
    }

    @Override
    public Envelope getEnvelope() {
        if ( standardProps == null ) {
            standardProps = new StandardGMLFeatureProps( null, null, null, null, null );
        }
        if ( standardProps.getBoundedBy() == null ) {
            standardProps.setBoundedBy( calcEnvelope() );
        }
        return standardProps.getBoundedBy();
    }

    /**
     * Helper method for calculating the envelope of a feature.
     * 
     * @return envelope of all geometry properties of the feature
     */
    protected Envelope calcEnvelope() {
        Envelope featureBBox = null;
        for ( Property prop : this.getProperties() ) {
            featureBBox = mergeEnvelope( prop, featureBBox );
        }
        return featureBBox;
    }

    private Envelope mergeEnvelope( TypedObjectNode node, Envelope env ) {
        if ( node instanceof Property ) {
            Property prop = (Property) node;
            if ( prop.getValue() != null ) {
                env = mergeEnvelope( prop.getValue(), env );
            }
        } else if ( node instanceof Geometry ) {
            Geometry g = (Geometry) node;
            if ( env != null ) {
                env = env.merge( g.getEnvelope() );
            } else {
                env = g.getEnvelope();
            }
        } else if ( node instanceof GenericXMLElementContent ) {
            GenericXMLElementContent xml = (GenericXMLElementContent) node;
            for (TypedObjectNode child : xml.getChildren()) {
                env = mergeEnvelope( child, env );
            }
        }
        return env;
    }

    @Override
    public Property[] getProperties( GMLVersion version ) {
        if ( standardProps != null ) {
            List<Property> props = new LinkedList<Property>();
            props.addAll( standardProps.getProperties( version ) );
            props.addAll( Arrays.asList( getProperties() ) );
            return props.toArray( new Property[props.size()] );
        }
        return getProperties();
    }

    @Override
    public Property[] getProperties( QName propName, GMLVersion version ) {
        if ( standardProps != null ) {
            Property[] gmlProp = standardProps.getProperties( propName, version );
            if ( gmlProp != null ) {
                return gmlProp;
            }
        }
        return getProperties( propName );
    }

    @Override
    public Property getProperty( QName propName, GMLVersion version ) {
        if ( standardProps != null ) {
            Property gmlProp = standardProps.getProperty( propName, version );
            if ( gmlProp != null ) {
                return gmlProp;
            }
        }
        return getProperty( propName );
    }

    @Override
    public void setProperties( List<Property> props, GMLVersion version )
                            throws IllegalArgumentException {
        Pair<StandardGMLFeatureProps, List<Property>> pair = StandardGMLFeatureProps.create( props, version );
        this.standardProps = pair.first;
        setProperties( pair.second );
    }

    @Override
    public void setPropertyValue( QName propName, int occurence, TypedObjectNode value, GMLVersion version ) {
        if ( standardProps == null || !standardProps.setPropertyValue( propName, occurence, value, version ) ) {
            setPropertyValue( propName, occurence, value );
        }
    }
}

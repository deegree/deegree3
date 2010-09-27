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
import org.deegree.commons.utils.Pair;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.StandardGMLFeatureProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for common {@link Feature} implementations.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public abstract class AbstractFeature implements Feature {

    private static final Logger LOG = LoggerFactory.getLogger( AbstractFeature.class );

    /** Feature id */
    protected String fid;

    /** Feature type */
    protected final FeatureType ft;

    /** Default GML properties that every GML feature allows for (gml:name, gml:description, ...). */
    protected StandardGMLFeatureProps standardProps;

    /**
     * Creates a new {@link AbstractFeature} instance.
     * 
     * @param fid
     *            feature id or <code>null</code> if the feature is anonymous (discouraged for most use cases)
     * @param ft
     *            feature type, must not be <code>null</code>
     */
    protected AbstractFeature( String fid, FeatureType ft ) {
        this.fid = fid;
        this.ft = ft;
    }

    @Override
    public String getId() {
        return fid;
    }

    @Override
    public void setId( String fid ) {
        this.fid = fid;
    }

    @Override
    public QName getName() {
        return ft.getName();
    }

    @Override
    public FeatureType getType() {
        return ft;
    }

    @Override
    public StandardGMLFeatureProps getGMLProperties() {
        return standardProps;
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
            Envelope gEnv = g.getEnvelope();
            // TODO this is to skip one-dimensional bounding boxes...
            if ( gEnv.getCoordinateDimension() > 1 ) {
                if ( env != null ) {
                    env = env.merge( gEnv );
                } else {
                    env = gEnv;
                }
            } else {
                LOG.warn( "Encountered one-dimensional bbox. Ignoring for feature envelope." );
            }
        } else if ( node instanceof GenericXMLElementContent ) {
            GenericXMLElementContent xml = (GenericXMLElementContent) node;
            for ( TypedObjectNode child : xml.getChildren() ) {
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
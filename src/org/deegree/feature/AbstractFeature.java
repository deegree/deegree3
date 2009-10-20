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

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deegree.commons.types.gml.StandardObjectProps;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.feature.gml.FeatureReference;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.feature.xpath.FeatureNode;
import org.deegree.feature.xpath.FeatureXPath;
import org.deegree.feature.xpath.PropertyNode;
import org.deegree.filter.expression.PropertyName;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.jaxen.JaxenException;
import org.jaxen.XPath;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public abstract class AbstractFeature implements Feature {

    private StandardObjectProps standardProps;

    public Object[] getPropertyValues( PropertyName propName )
                            throws JaxenException {

        XPath xpath = new FeatureXPath( propName.getPropertyName(), this );
        xpath.setNamespaceContext( propName.getNsContext() );
        List<?> selectedNodes = xpath.selectNodes( new FeatureNode( null, this ) );

        Object[] resultValues = new Object[selectedNodes.size()];
        int i = 0;
        for ( Object node : selectedNodes ) {
            if ( node instanceof PropertyNode ) {
                Property<?> prop = ( (PropertyNode) node ).getProperty();
                PropertyType pt = prop.getType();

                Object value = ( (PropertyNode) node ).getProperty().getValue();
                if (pt instanceof SimplePropertyType) {
                    switch (( (SimplePropertyType) pt ).getPrimitiveType()) {
                    case BOOLEAN: {
                        value = Boolean.parseBoolean( (String) value );
                        break;
                    }
                    case DATE: {
                        try {
                            value = DateUtils.parseISO8601Date( (String) value );
                        } catch ( ParseException e ) {
                            throw new JaxenException (e.getMessage(), e);
                        }
                        break;
                    }
                    case DATE_TIME: {
                        try {
                            value = DateUtils.parseISO8601Date( (String) value );
                        } catch ( ParseException e ) {
                            throw new JaxenException (e.getMessage(), e);
                        }
                        break;
                    }
                    case NUMBER: {
                        value = new BigDecimal((String) value);
                        break;
                    }
                    case STRING: {
                        // nothing to do
                        break;
                    }
                    }
                }
                resultValues[i++] = value;
            } else {
                resultValues[i++] = node;
            }
        }
        return resultValues;
    }

    public Envelope getEnvelope() {
        return getEnvelope( this, new HashSet<Feature>() );
    }

    /**
     * Helper method for calculating the envelope of a feature (or feature collection), respects multiple geometry
     * properties, subfeatures and cycles in the feature structure.
     * 
     * TODO use caching to prevent permanent recalculation of bbox
     * 
     * @param feature
     *            feature for which the envelope is requested
     * @param visited
     *            features that have already been visited in the top-down traversal
     * @return envelope of the feature
     */
    private static Envelope getEnvelope( Feature feature, Set<Feature> visited ) {
        Envelope featureBBox = null;
        if ( !visited.contains( feature ) ) {
            visited.add( feature );
            if ( !( feature instanceof FeatureReference ) || ( (FeatureReference) feature ).isLocal() ) {
                for ( Property<?> prop : feature.getProperties() ) {
                    Object propValue = prop.getValue();
                    Envelope propBBox = null;
                    if ( propValue instanceof Geometry ) {
                        Geometry geom = (Geometry) propValue;
                        propBBox = geom.getEnvelope();
                    } else if ( propValue instanceof Feature ) {
                        Feature subFeature = (Feature) propValue;
                        propBBox = getEnvelope( subFeature, visited );
                    }
                    if ( propBBox != null ) {
                        if ( featureBBox != null ) {
                            featureBBox = featureBBox.merge( propBBox );
                        } else {
                            featureBBox = propBBox;
                        }
                    }
                }
            }
        }
        return featureBBox;
    }

    @Override
    public StandardObjectProps getStandardGMLProperties() {
        return standardProps;
    }

    @Override
    public void setStandardGMLProperties( StandardObjectProps standardProps ) {
        this.standardProps = standardProps;
    }
}

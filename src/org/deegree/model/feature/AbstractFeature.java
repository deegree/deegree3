//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.model.feature;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deegree.model.feature.xpath.FeatureNode;
import org.deegree.model.feature.xpath.FeatureXPath;
import org.deegree.model.feature.xpath.Node;
import org.deegree.model.feature.xpath.PropertyNode;
import org.deegree.model.filter.expression.PropertyName;
import org.deegree.model.geometry.Envelope;
import org.deegree.model.geometry.Geometry;
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

    /**
     * Returns the value of a certain property of this object.
     * 
     * @param propName
     *            XPath expression that identifies the property
     * @return the property value
     */
    @SuppressWarnings("unchecked")
    public Object getPropertyValue( PropertyName propName )
                            throws JaxenException {
        XPath xpath = new FeatureXPath( propName.getPropertyName() );
        xpath.setNamespaceContext( propName.getNsContext() );
        List<Node> selectedNodes = xpath.selectNodes( new FeatureNode( null, this ) );
        if ( selectedNodes.size() == 0 ) {
            return null;
        }
        if ( selectedNodes.size() > 1 ) {
            String msg = "PropertyName '" + propName + "' matches multiple nodes in feature " + getId()
                         + ". This is currently not supported.";
            throw new JaxenException( msg );
        }
        Node node = selectedNodes.get( 0 );
        if ( !( node instanceof PropertyNode ) ) {
            String msg = "PropertyName '" + propName + "' does not refer to a property.";
            throw new JaxenException( msg );
        }
        return ( (PropertyNode) node ).getProperty().getValue();
    }

    public Envelope getEnvelope() {
        return getEnvelope( this, new HashSet<Feature>() );
    }

    /**
     * Helper method for calculating the envelope of a feature, respects multiple geometry properties, subfeatures and
     * cycles in the feature structure.
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
            for ( Property<?> prop : feature.getProperties() ) {
                Object propValue = prop.getValue();
                Envelope propBBox = null;
                if ( propValue instanceof Geometry ) {
                    Geometry geom = (Geometry) propValue;
                    propBBox = geom.getEnvelope();
                } else if ( propValue instanceof Feature ) {
                    Feature subFeature = (Feature) propValue;
                    propBBox = subFeature.getEnvelope();
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
        return featureBBox;
    }
}

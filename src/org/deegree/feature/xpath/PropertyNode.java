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
package org.deegree.feature.xpath;

import javax.xml.namespace.QName;

import org.deegree.feature.Feature;
import org.deegree.feature.Property;
import org.deegree.feature.types.property.PropertyType;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class PropertyNode extends ElementNode {

    private FeatureNode parent;

    private Property<Object> prop;

    PropertyNode( FeatureNode parent, final Property<?> prop ) {
        super( prop.getName() );
        this.parent = parent;
        // TODO temporary hack to get the xpath expressions to evaluate properly
        this.prop = new Property<Object>() {

            @Override
            public QName getName() {
                return prop.getName();
            }

            @Override
            public PropertyType getType() {
                return prop.getType();
            }

            @Override
            public Object getValue() {
                if ( prop.getValue() instanceof Feature ) {
                    return prop.getValue();
                }
                return prop.getValue().toString();
            }
        };
    }

    @Override
    public Node getParent() {
        return parent;
    }

    /**
     * @return the modified property which converts values to strings
     */
    public Property<Object> getProperty() {
        return prop;
    }
}

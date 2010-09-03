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
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLObject;
import org.deegree.gml.GMLVersion;

/**
 * {@link ElementNode} that wraps a {@link GMLObject}.
 * 
 * @param <V>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GMLObjectNode<V extends GMLObject> extends ElementNode<V> {

    private XPathNode parentNode;

    private V object;

    public GMLObjectNode( XPathNode parentNode, V object, GMLVersion version ) {
        super( getName( object, version ) );
        this.parentNode = parentNode;
        this.object = object;
    }

    private static QName getName( GMLObject object, GMLVersion version ) {
        if ( object instanceof Feature ) {
            return ( (Feature) object ).getName();
        } else if ( object instanceof Geometry ) {
            // TODO
            return new QName( version.getNamespace(), "Geometry" );
        }
        throw new IllegalArgumentException( "Creating GMLObjectNode from " + object.getClass()
                                            + " needs implementation." );
    }

    @Override
    public XPathNode getParent() {
        return parentNode;
    }

    @Override
    public V getValue() {
        return object;
    }
}

//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.gml;

import java.util.HashSet;
import java.util.Set;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElementContent;
import org.deegree.feature.Feature;
import org.deegree.feature.property.Property;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.Geometry.GeometryType;
import org.deegree.geometry.composite.CompositeCurve;
import org.deegree.geometry.composite.CompositeGeometry;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Curve.CurveType;
import org.deegree.geometry.primitive.GeometricPrimitive.PrimitiveType;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public abstract class GMLObjectVisitor {

    private Set<GMLObject> visited = new HashSet<GMLObject>();

    protected GMLObjectVisitor( GMLObject root ) {

    }

    public abstract void visitFeature( Feature node );

    public abstract void visitGeometry( Geometry node );

    private void traverse( GMLObject node ) {
        if ( !visited.contains( node ) ) {
            visited.add( node );
            if ( node instanceof Feature ) {
                Feature feature = (Feature) node;
                visitFeature( feature );
                // TODO GML properties?
                for ( Property prop : feature.getProperties() ) {
                    if ( prop.getValue() != null ) {
                        TypedObjectNode ton = prop.getValue();
                        traverseTypedObjectNode( ton );
                    }
                }
            } else if ( node instanceof Geometry ) {
                visitGeometry( (Geometry) node );
                traverseGeometry( (Geometry) node );
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private void traverseGeometry( Geometry node ) {
        GeometryType type = node.getGeometryType();
        switch ( type ) {
        case COMPOSITE_GEOMETRY:
            for ( Geometry member : (CompositeGeometry<?>) node ) {
                traverse( member );
            }
            break;
        case ENVELOPE:
            // nothing to do
            break;
        case MULTI_GEOMETRY:
            for ( Geometry member : (MultiGeometry<?>) node ) {
                traverse( member );
            }
            break;
        case PRIMITIVE_GEOMETRY:
            GeometricPrimitive pg = (GeometricPrimitive) node;
            PrimitiveType pt = pg.getPrimitiveType();
            switch ( pt ) {
            case Curve:
                CurveType ct = ( (Curve) pg ).getCurveType();
                switch ( ct ) {
                case CompositeCurve:
                    for ( Curve member : ( (CompositeCurve) pg ) ) {
                        traverse( member );
                    }
                    break;
                case Curve:
                    break;
                case LineString:
                    traversePoints( ( (LineString) pg ).getControlPoints() );
                    break;
                case OrientableCurve:
                    break;
                case Ring:
                    break;
                }
                break;
            case Point:
                // nothing to do
                break;
            case Solid:
                break;
            case Surface:
                break;

            }
            break;
        }
    }

    private void traverseTypedObjectNode( TypedObjectNode node ) {
        if ( node instanceof GMLObject ) {
            traverse( (GMLObject) node );
        } else if ( node instanceof GenericXMLElementContent ) {
            GenericXMLElementContent generic = (GenericXMLElementContent) node;
            for ( int i = 0; i < generic.getChildren().size(); i++ ) {
                traverseTypedObjectNode( generic );
            }
        }
    }

    private void traversePoints( Points node ) {

    }
}
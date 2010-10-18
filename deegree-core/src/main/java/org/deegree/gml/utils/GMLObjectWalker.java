//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.gml.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElementContent;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.property.Property;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.Geometry.GeometryType;
import org.deegree.geometry.composite.CompositeCurve;
import org.deegree.geometry.composite.CompositeGeometry;
import org.deegree.geometry.composite.CompositeSolid;
import org.deegree.geometry.composite.CompositeSurface;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.OrientableCurve;
import org.deegree.geometry.primitive.OrientableSurface;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.Tin;
import org.deegree.geometry.primitive.Curve.CurveType;
import org.deegree.geometry.primitive.GeometricPrimitive.PrimitiveType;
import org.deegree.geometry.primitive.Solid.SolidType;
import org.deegree.geometry.primitive.Surface.SurfaceType;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.geometry.primitive.segments.LineStringSegment;
import org.deegree.gml.GMLObject;
import org.deegree.gml.GMLReference;

/**
 * Provides
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GMLObjectWalker {

    private final Set<GMLObject> visited = new HashSet<GMLObject>();

    private final GMLObjectVisitor visitor;

    /**
     * Creates a new {@link GMLObjectWalker} instance that will trigger callbacks to the given {@link GMLObjectVisitor}
     * instance.
     * 
     * @param visitor
     *            visitor instance, must not be <code>null</code>
     */
    public GMLObjectWalker( GMLObjectVisitor visitor ) {
        this.visitor = visitor;
    }

    /**
     * Starts the traversal of the {@link GMLObject} hierarchy.
     * 
     * @param node
     *            start node, must not be <code>null</code>
     */
    @SuppressWarnings("unchecked")
    public void traverse( GMLObject node ) {

        if ( node instanceof GMLReference<?> ) {
            if ( ( (GMLReference<GMLObject>) node ).isResolved() ) {
                node = ( (GMLReference<GMLObject>) node ).getReferencedObject();
            } else {
                return;
            }
        }

        if ( !visited.contains( node ) ) {
            visited.add( node );
            if ( node instanceof FeatureCollection ) {
                FeatureCollection fc = (FeatureCollection) node;
                for ( Feature member : fc ) {
                    traverse( member );
                }
            } else if ( node instanceof Feature ) {
                Feature f = (Feature) node;
                if ( visitor.visitFeature( f ) ) {
                    traverseFeature( f );
                }
            } else if ( node instanceof Geometry ) {
                Geometry g = (Geometry) node;
                if ( visitor.visitGeometry( g ) ) {
                    traverseGeometry( g );
                }
            } else {
                throw new UnsupportedOperationException();
            }
        }

    }

    private void traverseFeature( Feature node ) {
        // TODO GML properties?
        for ( Property prop : node.getProperties() ) {
            if ( prop.getValue() != null ) {
                TypedObjectNode ton = prop.getValue();
                traverseTypedObjectNode( ton );
            }
        }
    }

    private void traverseTypedObjectNode( TypedObjectNode node ) {
        if ( node instanceof GMLObject ) {
            traverse( (GMLObject) node );
        } else if ( node instanceof GenericXMLElementContent ) {
            GenericXMLElementContent generic = (GenericXMLElementContent) node;
            for ( TypedObjectNode child : generic.getChildren() ) {
                traverseTypedObjectNode( child );
            }
        }
    }

    private void traverseGeometry( Geometry node ) {
        GeometryType gt = node.getGeometryType();
        switch ( gt ) {
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
            traversePrimitive( (GeometricPrimitive) node );
            break;
        }
    }

    private void traversePrimitive( GeometricPrimitive g ) {
        PrimitiveType pt = g.getPrimitiveType();
        switch ( pt ) {
        case Curve:
            traverseCurve( (Curve) g );
            break;
        case Point:
            // nothing to do
            break;
        case Solid:
            traverseSolid( (Solid) g );
            break;
        case Surface:
            traverseSurface( (Surface) g );
            break;
        }
    }

    private void traverseCurve( Curve c ) {
        CurveType ct = c.getCurveType();
        switch ( ct ) {
        case CompositeCurve:
            for ( Curve member : ( (CompositeCurve) c ) ) {
                traverse( member );
            }
            break;
        case Curve:
        case Ring:
            for ( CurveSegment segment : c.getCurveSegments() ) {
                traverseSegment( segment );
            }
            break;
        case LineString:
            traversePoints( ( (LineString) c ).getControlPoints() );
            break;
        case OrientableCurve:
            traverse( ( (OrientableCurve) c ).getBaseCurve() );
            break;
        }
    }

    private void traverseSurface( Surface s ) {
        SurfaceType st = s.getSurfaceType();
        switch ( st ) {
        case CompositeSurface:
            for ( Surface member : ( (CompositeSurface) s ) ) {
                traverse( member );
            }
            break;
        case OrientableSurface:
            traverse( ( (OrientableSurface) s ).getBaseSurface() );
            break;
        case Polygon:
            Polygon p = (Polygon) s;
            if ( p.getExteriorRing() != null ) {
                traverse( p.getExteriorRing() );
            }
            for ( Ring inner : p.getInteriorRings() ) {
                traverse( inner );
            }
            break;
        case PolyhedralSurface:
        case Surface:
        case TriangulatedSurface:
            for ( SurfacePatch patch : s.getPatches() ) {
                traversePatch( patch );
            }
            break;
        case Tin:
            Tin tin = (Tin) s;
            for ( List<LineStringSegment> stops : tin.getStopLines() ) {
                for ( LineStringSegment ls : stops ) {
                    traverseSegment( ls );
                }
            }
            for ( List<LineStringSegment> breaks : tin.getBreakLines() ) {
                for ( LineStringSegment ls : breaks ) {
                    traverseSegment( ls );
                }
            }
            break;
        }
    }

    private void traverseSolid( Solid s ) {
        SolidType st = s.getSolidType();
        switch ( st ) {
        case CompositeSolid: {
            for ( Solid member : ( (CompositeSolid) s ) ) {
                traverse( member );
            }
            break;
        }
        case Solid: {
            if ( s.getExteriorSurface() != null ) {
                traverse( s.getExteriorSurface() );
            }
            for ( Surface inner : s.getInteriorSurfaces() ) {
                traverse( inner );
            }
            break;
        }
        }
    }

    private void traversePatch( SurfacePatch p ) {
        // TODO
    }

    private void traverseSegment( CurveSegment c ) {
        // TODO
    }

    private void traversePoints( Points p ) {
        // TODO
    }
}
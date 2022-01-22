//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.rendering.r2d;

import static org.deegree.commons.utils.math.MathUtils.isZero;
import static org.deegree.rendering.r2d.OrientationFixer.fixOrientation;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometries;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.multi.MultiPoint;
import org.deegree.geometry.multi.MultiPolygon;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.deegree.geometry.standard.DefaultEnvelope;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.geometry.standard.primitive.DefaultSurface;
import org.deegree.style.styling.LineStyling;
import org.deegree.style.styling.PolygonStyling;
import org.deegree.style.styling.components.Stroke;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for clipping geometries to the area of the viewport.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
class GeometryClipper {

    private final Envelope viewPort;

    private final Polygon clippingArea;

    GeometryClipper( final Envelope viewPort, final int width ) {
        this.viewPort = viewPort;
        this.clippingArea = calculateClippingArea( viewPort, width );
    }

    private Polygon calculateClippingArea( final Envelope bbox, final int width ) {
        double resolution = bbox.getSpan0() / width;
        double delta = resolution * 100;
        double[] minCords = new double[] { bbox.getMin().get0() - delta, bbox.getMin().get1() - delta };
        double[] maxCords = new double[] { bbox.getMax().get0() + delta, bbox.getMax().get1() + delta };
        Point min = new DefaultPoint( null, bbox.getCoordinateSystem(), null, minCords );
        Point max = new DefaultPoint( null, bbox.getCoordinateSystem(), null, maxCords );
        Envelope enlargedBBox = new DefaultEnvelope( min, max );
        return (Polygon) Geometries.getAsGeometry( enlargedBBox );
    }

    /**
     * Clips the passed geometry with the drawing area if the drawing area does not contain the passed geometry
     * completely.
     * 
     * @param geom
     *            the geometry to clip, must not be <code>null</code> and in the same CRS as the clipping area
     * @return the clipped geometry or the original geometry if the geometry lays completely in the drawing area.
     */
    Geometry clipGeometry( final Geometry geom ) {
        return clipGeometry( geom, clippingArea );
    }

    /**
     * Calculates the points inside the geometry and inside the view port. First the passed geometry is clipped
     * by the view port. A multipolygon may result. For each of the polygon in this multipolygon one interior point
     * is created
     *
     * @param geom to create labels for, must not be <code>null</code> and in the same CRS as the viewPort
     * @return a MultiPoint with all calculated labels
     */
    MultiPoint calculateInteriorPoints( final Geometry geom ) {
        if ( geom == null )
            return null;
        Geometry clippedGeometry = clipGeometry( geom, viewPort );
        List<Point> points = new ArrayList<Point>();
        if ( clippedGeometry != null && clippedGeometry instanceof DefaultSurface ) {
            points.add( ( (DefaultSurface) clippedGeometry ).getInteriorPoint() );
        }
        if ( clippedGeometry != null && clippedGeometry instanceof MultiPolygon ) {
            for ( Polygon p : ( (MultiPolygon) clippedGeometry ) ) {
                if ( p instanceof DefaultSurface ) {
                    points.add( ( (DefaultSurface) p ).getInteriorPoint() );
                }
            }
        }
        return new GeometryFactory().createMultiPoint( null, geom.getCoordinateSystem(), points );
    }


    Geometry clipGeometry( final Geometry geom, Geometry clippingArea ) {
        if ( clippingArea != null && !clippingArea.contains( geom ) ) {
            try {
                Geometry clippedGeometry = clippingArea.getIntersection( geom );
                if ( clippedGeometry == null ) {
                    return null;
                }
                org.locationtech.jts.geom.Geometry jtsOrig = ( (AbstractDefaultGeometry) geom ).getJTSGeometry();
                org.locationtech.jts.geom.Geometry jtsClipped = ( (AbstractDefaultGeometry) clippedGeometry ).getJTSGeometry();
                if ( jtsOrig == jtsClipped ) {
                    return geom;
                }
                if ( isInvertedOrientation( jtsOrig ) ) {
                    return clippedGeometry;
                }

                return fixOrientation( clippedGeometry, clippedGeometry.getCoordinateSystem() );
            } catch ( UnsupportedOperationException e ) {
                // use original geometry if intersection not supported by JTS
                return geom;
            }
        }
        return geom;
    }

    /**
     * Check if the passed Geometry is a Polygon (or the first Geometry of a Collection) and the exterior Ring has CW orientation  
     * 
     * This helper is only a workaround to render polygons in CW/CCW order in the same manner clipped as unclipped
     * 
     * TODO This should be resolved in a more universal way, see https://github.com/deegree/deegree3/issues/645
     * 
     * @param   jtsGeom   JTS Geometry to be evaluated
     * @return  boolean   true if (first) Geometry is Polygon with CW external ring, false otherwise
     */
    private boolean isInvertedOrientation( org.locationtech.jts.geom.Geometry jtsGeom ) {
        org.locationtech.jts.geom.Polygon poly = null;
        try {
            if ( jtsGeom instanceof org.locationtech.jts.geom.GeometryCollection && //
                 ( (org.locationtech.jts.geom.GeometryCollection) jtsGeom ).getNumGeometries() > 0 ) {
                org.locationtech.jts.geom.Geometry firstGeom;
                firstGeom = ( (org.locationtech.jts.geom.GeometryCollection) jtsGeom ).getGeometryN( 0 );
                if ( firstGeom instanceof org.locationtech.jts.geom.Polygon ) {
                    poly = (org.locationtech.jts.geom.Polygon) firstGeom;
                }
            } else if ( jtsGeom instanceof org.locationtech.jts.geom.Polygon ) {
                poly = (org.locationtech.jts.geom.Polygon) jtsGeom;
            }
        
            //TRICKY check if polygon exterior is CW
            if ( poly != null ) {
                org.locationtech.jts.geom.Coordinate[] coords = poly.getExteriorRing().getCoordinates();
                if ( !org.locationtech.jts.algorithm.CGAlgorithms.isCCW( coords ) ) {
                    return true;
                }
            }
        } catch ( Exception ign ) {
            // treat as not affected
        }
        
        return false;
    }

    public static boolean isGenerationExpensive( PolygonStyling styling ) {
        if ( styling == null )
            return false;
        
        return ( !isZero( styling.perpendicularOffset ) || isGenerationExpensive( styling.stroke ));
    }

    public static boolean isGenerationExpensive( LineStyling styling ) {
        if ( styling == null )
            return false;
        
        return ( !isZero( styling.perpendicularOffset ) || isGenerationExpensive( styling.stroke ) );
    }

    private static boolean isGenerationExpensive( Stroke styling ) {
        if ( styling == null )
            return false;

        if ( styling.dasharray != null || styling.stroke != null )
            return true;

        return false;
    }
}

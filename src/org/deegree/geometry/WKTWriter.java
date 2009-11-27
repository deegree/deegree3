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
package org.deegree.geometry;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.deegree.geometry.composite.CompositeCurve;
import org.deegree.geometry.composite.CompositeGeometry;
import org.deegree.geometry.composite.CompositeSurface;
import org.deegree.geometry.linearization.CurveLinearizer;
import org.deegree.geometry.linearization.LinearizationCriterion;
import org.deegree.geometry.linearization.NumPointsCriterion;
import org.deegree.geometry.multi.MultiGeometry;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.GeometricPrimitive;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.LinearRing;
import org.deegree.geometry.primitive.OrientableCurve;
import org.deegree.geometry.primitive.OrientableSurface;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Polygon;
import org.deegree.geometry.primitive.PolyhedralSurface;
import org.deegree.geometry.primitive.Ring;
import org.deegree.geometry.primitive.Solid;
import org.deegree.geometry.primitive.Surface;
import org.deegree.geometry.primitive.Tin;
import org.deegree.geometry.primitive.TriangulatedSurface;
import org.deegree.geometry.standard.AbstractDefaultGeometry;
import org.deegree.geometry.Geometry.GeometryType;

/**
 * Writes {@link Geometry} objects as Well-Known Text (WKT).
 * 
 * TODO re-implement without delegating to JTS TODO add support for non-SFS geometries (e.g. non-linear curves)
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WKTWriter {

    private static final com.vividsolutions.jts.io.WKTWriter jtsWriter = new com.vividsolutions.jts.io.WKTWriter();

    private String geometryString;
    
    private Set<WKTFlag> flags = new HashSet<WKTFlag>();

    public enum WKTFlag {
        /** Export can use ENVELOPE*/
        USE_ENVELOPE, 
        /** Export can use 3D geometries*/
        USE_3D,
        /** Export can use LINEARRING(...) */
        USE_LINEARRING,
        /** Export can use CIRCULARSTRING(...), COMPOUNDSTRING(...), ... **/
        USE_SQL_MM,
        /** If necessary, linearize curves / surface boundaries. */
        USE_LINEARIZATION,
        /** COMPOSITEGEOMETRY(), COMPOSITECURVE(), COMPOSITESOLID() */
        USE_COMPOSITES,
    }
    
    
    
    /**
     * @return the geometryString
     */
    public String getGeometryString() {
        return geometryString;
    }


    public void createSFS11Writer() {
        
        new WKTWriter();
    }
    
    public void createSFS12Writer() {

    }

    public void writeGeometry( Geometry geometry ) {
        geometryString = "";
        switch ( geometry.getGeometryType() ) {
        case COMPOSITE_GEOMETRY:
            writeCompositeGeometry( (CompositeGeometry<GeometricPrimitive>) geometry );
            break;
        case ENVELOPE:
            writeEnvelope( (Envelope) geometry );
            break;
        case MULTI_GEOMETRY:
            writeMultiGeometry( (MultiGeometry<? extends Geometry>) geometry );
            break;
        case PRIMITIVE_GEOMETRY:
            writeGeometricPrimitive( (GeometricPrimitive) geometry );
            break;
        }
    }

    /**
     * @param geometry
     */
    private void writeGeometricPrimitive( GeometricPrimitive geometry ) {
        switch ( geometry.getPrimitiveType() ) {
        case Point:
            writePoint( (Point) geometry );
            break;
        case Curve:
            wirteCurve( (Curve) geometry );
            break;
        case Surface:
            writeSurface( (Surface) geometry );
            break;
        case Solid:
            writeSolid( (Solid) geometry );
            break;
        }

    }

    /**
     * @param geometry
     */
    public void writePoint( Point geometry ) {
        
        geometryString += "POINT(";
        geometryString += geometry.get0();
        geometryString += " ";
        geometryString += geometry.get1();
        if ( geometry.getCoordinateDimension() == 3 ) {
            geometryString += " ";
            geometryString += geometry.get2();
        }
        geometryString += ")";
    }

    /**
     * @param geometry
     */
    private void writeSolid( Solid geometry ) {
        switch ( geometry.getSolidType() ) {

        case Solid:
            break;
        case CompositeSolid:
            break;

        }

    }

    /**
     * @param geometry
     */
    private void writeSurface( Surface geometry ) {
        switch ( geometry.getSurfaceType() ) {

        case Surface:

            writeSurface( (Surface) geometry );
            break;
        case Polygon:
            writePolygon( (Polygon) geometry );
            break;
        case PolyhedralSurface:
            writePolyhedralSurface( (PolyhedralSurface) geometry );
            break;
        case TriangulatedSurface:
            writeTriangulatedSurface( (TriangulatedSurface) geometry );
            break;
        case Tin:
            writeTin( (Tin) geometry );
            break;
        case CompositeSurface:
            writeCompositeSurface( (CompositeSurface) geometry );
            break;
        case OrientableSurface:
            writeOrientableSurface( (OrientableSurface) geometry );
            break;

        }

    }

    /**
     * @param geometry
     */
    private void writeOrientableSurface( OrientableSurface geometry ) {
        // TODO Auto-generated method stub

    }

    /**
     * @param geometry
     */
    private void writeCompositeSurface( CompositeSurface geometry ) {
        // TODO Auto-generated method stub

    }

    /**
     * @param geometry
     */
    private void writeTin( Tin geometry ) {
        // TODO Auto-generated method stub

    }

    /**
     * @param geometry
     */
    private void writeTriangulatedSurface( TriangulatedSurface geometry ) {
        // TODO Auto-generated method stub

    }

    /**
     * @param geometry
     */
    private void writePolyhedralSurface( PolyhedralSurface geometry ) {
        // TODO Auto-generated method stub

    }

    /**
     * @param geometry
     */
    private void writePolygon( Polygon geometry ) {
        // TODO Auto-generated method stub

    }

    /**
     * @param geometry
     */
    private void wirteCurve( Curve geometry ) {
        switch ( geometry.getCurveType() ) {

        case Curve:

            break;

        case LineString:

            break;

        case OrientableCurve:

            break;

        case CompositeCurve:

            break;

        case Ring:
            writeRing( (Ring) geometry );
            break;

        }

    }

    /**
     * @param geometry
     */
    private void writeRing( Ring geometry ) {
        switch ( geometry.getRingType() ) {
        case LinearRing:

            writeLinearRing( (LinearRing) geometry );
            break;

        case Ring:
            writeRing( (Ring) geometry );
            break;

        }

    }

    /**
     * @param geometry
     */
    private void writeLinearRing( LinearRing geometry ) {
        // TODO Auto-generated method stub

    }

    /**
     * @param geometry
     */
    private void writeMultiGeometry( MultiGeometry<? extends Geometry> geometry ) {
        // TODO Auto-generated method stub

    }

    /**
     * @param geometry
     */
    private void writeCompositeGeometry( CompositeGeometry<GeometricPrimitive> geometry ) {
        // TODO Auto-generated method stub

    }

    /**
     * TODO also for 3D
     * @param envelope
     */
    public  void writeEnvelope (Envelope envelope) {
        if (flags.contains(WKTFlag.USE_ENVELOPE )) {
            //ENVELOPE(...)
        }
        else {
            Point pMax = envelope.getMax();
            Point pMin = envelope.getMin();
            if(pMin == pMax){
                this.writePoint( pMin );
            }else{
                geometryString += "POLYGON((";
                double pMinX = pMin.get0();
                double pMinY = pMin.get1();
                double pMaxX = pMax.get0();
                double pMaxY = pMax.get1();
                geometryString += pMinX + " " + pMinY + ", ";
                geometryString += pMaxX + " " + pMinY + ", ";
                geometryString += pMaxX + " " + pMaxY + ", ";
                geometryString += pMinX + " " + pMaxY + ", ";
                geometryString += pMinX + " " + pMinY;
                
                geometryString += "))";
            
                   
                    
            }
        }
    }

    public static String write( Geometry geom ) {
        
        
        return jtsWriter.write( ( (AbstractDefaultGeometry) geom ).getJTSGeometry() );
    }

    public static void write( Geometry geom, Writer writer )
                            throws IOException {
        jtsWriter.write( ( (AbstractDefaultGeometry) geom ).getJTSGeometry(), writer );
    }

}

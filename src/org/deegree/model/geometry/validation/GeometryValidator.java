//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/feature/Feature.java $
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
package org.deegree.model.geometry.validation;

import java.util.LinkedList;
import java.util.List;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.model.geometry.Geometry;
import org.deegree.model.geometry.GeometryFactoryCreator;
import org.deegree.model.geometry.composite.CompositeGeometry;
import org.deegree.model.geometry.linearization.NumPointsCriterion;
import org.deegree.model.geometry.linearization.SurfaceLinearizer;
import org.deegree.model.geometry.multi.MultiGeometry;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.GeometricPrimitive;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.Ring;
import org.deegree.model.geometry.primitive.Solid;
import org.deegree.model.geometry.primitive.Surface;
import org.deegree.model.geometry.primitive.curvesegments.CurveSegment;
import org.deegree.model.geometry.primitive.curvesegments.LineStringSegment;
import org.deegree.model.geometry.primitive.surfacepatches.PolygonPatch;
import org.deegree.model.geometry.primitive.surfacepatches.SurfacePatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.valid.IsValidOp;
import com.vividsolutions.jts.operation.valid.TopologyValidationError;

/**
 * Performs topological validation of {@link Geometry} objects.
 * <p>
 * The following possible errors can be detected by this class:
 * <ul>
 * <li>{@link Point}: point geometries are always valid</li>
 * <li>{@link Curve}: a curve geometry is invalid, if at least one of it's segment boundaries does not coincide.
 * Additionally, a check for self-intersection can be enabled, but this is not generally a topological error.</li>
 * <li>{@link Surface}:</li>
 * <li>{@link Solid}: currently no checks available</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GeometryValidator extends XMLAdapter {

    private static final Logger LOG = LoggerFactory.getLogger( GeometryValidator.class );

    private XMLStreamReaderWrapper xmlStream;

    private SurfaceLinearizer linearizer;

    private GeometryFactory jtsFactory;

    public GeometryValidator() {
        linearizer = new SurfaceLinearizer( GeometryFactoryCreator.getInstance().getGeometryFactory() );
        jtsFactory = new GeometryFactory();
    }

    public boolean validateGeometry( Geometry geom ) {
        boolean isValid = false;
        switch ( geom.getGeometryType() ) {
        case COMPOSITE_GEOMETRY: {
            isValid = validate( (CompositeGeometry<?>) geom );
            break;
        }
        case COMPOSITE_PRIMITIVE: {
            isValid = validate( (CompositeGeometry<?>) geom );
            break;
        }
        case ENVELOPE: {
            String msg = "Internal error: envelope 'geometries' should not occur here.";
            throw new IllegalArgumentException( msg );
        }
        case MULTI_GEOMETRY: {
            isValid = validate( (MultiGeometry<?>) geom );
            break;
        }
        case PRIMITIVE_GEOMETRY: {
            isValid = validate( (GeometricPrimitive) geom );
            break;
        }
        }
        return isValid;
    }

    private boolean validate( GeometricPrimitive geom ) {
        boolean isValid = true;
        switch ( geom.getPrimitiveType() ) {
        case Point: {
            System.out.println( "Point geometry. No validation necessary." );
            break;
        }
        case Curve: {
            System.out.println( "Curve geometry. Validating segment continuity." );
            Curve curve = (Curve) geom;
            Point lastSegmentEndPoint = null;
            for ( CurveSegment segment : curve.getCurveSegments() ) {
                Point startPoint = segment.getStartPoint();
                if ( lastSegmentEndPoint != null ) {
                    if ( startPoint.getX() != lastSegmentEndPoint.getX()
                         || startPoint.getY() != lastSegmentEndPoint.getY() ) {
                        isValid = false;
                        String msg = "Curve discontinuity at segment boundary detected: (" + lastSegmentEndPoint.getX() + ","
                                     + lastSegmentEndPoint.getY() + ") != (" + startPoint.getX() + ","
                                     + startPoint.getY() + ")";
                        System.out.println( msg );
                    }
                }
                lastSegmentEndPoint = segment.getEndPoint();
            }
            break;
        }
        case Surface: {
            System.out.println( "Surface geometry. Validating patches using JTS." );
            Surface surface = (Surface) geom;
            System.out.println( surface.getId() );
            for ( SurfacePatch patch : surface.getPatches() ) {
                if ( !( patch instanceof PolygonPatch ) ) {
                    System.out.println( "Skipping validation of patch -- not a PolygonPatch." );
                } else {
                    if ( !validate( (PolygonPatch) patch ) ) {
                        isValid = false;
                    }
                }
            }
            break;
        }
        case Solid: {
            String msg = "Validation of solids is not available";
            throw new IllegalArgumentException( msg );
        }
        }
        return isValid;
    }

    private boolean validate( PolygonPatch inputPatch ) {

        boolean isValid = true;
        PolygonPatch linearizedPatch = linearizer.linearize( inputPatch, new NumPointsCriterion( 3 ) );
        if ( linearizedPatch.getExteriorRing() == null ) {
            String msg = "Cannot validate patches without exterior boundary.";
            throw new IllegalArgumentException( msg );
        }

        LinearRing shell = getJTSRing( linearizedPatch.getExteriorRing() );

        List<Ring> interiorRings = linearizedPatch.getInteriorRings();
        LinearRing[] holes = new LinearRing[interiorRings.size()];
        for ( int i = 0; i < holes.length; i++ ) {
            holes[i] = getJTSRing( interiorRings.get( i ) );
        }

        Polygon polygon = jtsFactory.createPolygon( shell, holes );
        IsValidOp op = new IsValidOp( polygon );
        if ( !op.isValid() ) {
            TopologyValidationError error = op.getValidationError();
            System.err.println( "Patch has a topology error (" + error.getMessage() + ") at position "
                                + error.getCoordinate() );
            isValid = false;
        }
        return isValid;
    }

    private LinearRing getJTSRing( Ring ring ) {
        List<Coordinate> coordinates = new LinkedList<Coordinate>();
        for ( Curve member : ring.getMembers() ) {
            for ( CurveSegment segment : member.getCurveSegments() ) {
                for ( Point point : ( (LineStringSegment) segment ).getControlPoints() ) {
                    coordinates.add( new Coordinate( point.getX(), point.getY() ) );
                }
            }
        }
        return jtsFactory.createLinearRing( coordinates.toArray( new Coordinate[coordinates.size()] ) );
    }

    private boolean validate( CompositeGeometry<?> geom ) {
        boolean isValid = true;
        for ( GeometricPrimitive geometricPrimitive : geom ) {
            if ( !validate( geometricPrimitive ) ) {
                isValid = false;
            }
        }
        return isValid;
    }

    private boolean validate( MultiGeometry<?> geom ) {
        boolean isValid = true;
        for ( Geometry member : geom ) {
            if ( !validateGeometry( member ) ) {
                isValid = false;
            }
        }
        return isValid;
    }
}

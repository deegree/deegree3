//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/core/trunk/src/main/java/org/deegree/geometry/validation/GeometryFixer.java $
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
package org.deegree.geometry.validation;

import org.deegree.cs.CRS;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryInspectionException;
import org.deegree.geometry.GeometryInspector;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link GeometryInspector} that rejects geometries with coordinates that don't match the CRS of the geometry.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 21083 $, $Date: 2009-11-26 19:06:32 +0100 (Do, 26. Nov 2009) $
 */
public class CoordinateValidityInspector implements GeometryInspector {

    private static final Logger LOG = LoggerFactory.getLogger( CoordinateValidityInspector.class );

    @Override
    public Geometry inspect( Geometry geom )
                            throws GeometryInspectionException {

        CRS crs = geom.getCoordinateSystem();
        if ( crs != null ) {
            try {
                CoordinateSystem cs = crs.getWrappedCRS();
                int csDim = cs.getDimension();
                int coordDim = geom.getCoordinateDimension();
                if ( csDim != coordDim ) {
                    String msg = "Geometry is invalid. Dimensionality of coordinates (=" + coordDim
                                 + ") does not match dimensionality of CRS '" + crs.getName() + "' (=" + csDim + ").";
                    throw new GeometryInspectionException( msg );
                }
            } catch ( UnknownCRSException e ) {
                LOG.warn( "Skipping the check of coordinate validity: crs '" + crs + "' is not known." );
            }
        }
        return geom;
    }

    @Override
    public CurveSegment inspect( CurveSegment segment )
                            throws GeometryInspectionException {
        return segment;
    }

    @Override
    public SurfacePatch inspect( SurfacePatch patch )
                            throws GeometryInspectionException {
        return patch;
    }

    @Override
    public Points inspect( Points points ) {
        return points;
    }

    // private void validate( Geometry geom, CRS CoordinateSystem cs )
    // throws GeometryInspectionException {
    //
    // // check if geometry's bbox is inside the domain of its CRS
    // Envelope bbox = geom.getEnvelope();
    // if ( bbox.getCoordinateSystem() != null ) {
    // // check if geometry's bbox is valid with respect to the CRS domain
    // try {
    // double[] b = bbox.getCoordinateSystem().getAreaOfUse();
    // Envelope domainOfValidity = geomFac.createEnvelope( b[0], b[1], b[2], b[3], CRS.EPSG_4326 );
    // domainOfValidity = transform( domainOfValidity, bbox.getCoordinateSystem() );
    // if ( !bbox.isWithin( domainOfValidity ) ) {
    // String msg =
    // "Invalid geometry constraint in filter. The envelope of the geometry is not within the domain of validity ('"
    // + domainOfValidity + "') of its CRS ('" + bbox.getCoordinateSystem().getName() + "').";
    // throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "filter" );
    // }
    // } catch ( UnknownCRSException e ) {
    // // could not validate constraint, but let's assume it's met
    // } catch ( IllegalArgumentException e ) {
    // // could not validate constraint, but let's assume it's met
    // } catch ( TransformationException e ) {
    // // could not validate constraint, but let's assume it's met
    // }
    // }
    //
    // // check if geometry's bbox is inside the validity domain of the queried CRS
    // if ( queriedCrs != null ) {
    // try {
    // double[] b = queriedCrs.getAreaOfUse();
    // Envelope domainOfValidity = geomFac.createEnvelope( b[0], b[1], b[2], b[3], CRS.EPSG_4326 );
    // domainOfValidity = transform( domainOfValidity, queriedCrs );
    // Envelope bboxTransformed = transform( bbox, queriedCrs );
    // if ( !bboxTransformed.isWithin( domainOfValidity ) ) {
    // String msg =
    // "Invalid geometry constraint in filter. The envelope of the geometry is not within the domain of validity ('"
    // + domainOfValidity + "') of the queried CRS ('" + queriedCrs.getName() + "').";
    // throw new OWSException( msg, OWSException.INVALID_PARAMETER_VALUE, "filter" );
    // }
    // } catch ( UnknownCRSException e ) {
    // // could not validate constraint, but let's assume it's met
    // } catch ( IllegalArgumentException e ) {
    // // could not validate constraint, but let's assume it's met
    // } catch ( TransformationException e ) {
    // // could not validate constraint, but let's assume it's met
    // }
    // }
    // }
    //
    // private Envelope transform( Envelope bbox, CRS targetCrs )
    // throws IllegalArgumentException, TransformationException, UnknownCRSException {
    // if ( bbox.getEnvelope().equals( targetCrs ) ) {
    // return bbox;
    // }
    // GeometryTransformer transformer = new GeometryTransformer( targetCrs.getWrappedCRS() );
    // return (Envelope) transformer.transform( bbox );
    // }
}

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
package org.deegree.crs.transformations;

import static org.deegree.crs.projections.ProjectionUtils.EPS11;

import java.util.Arrays;

import javax.vecmath.GMatrix;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;

import org.deegree.crs.CRSCodeType;
import org.deegree.crs.CRSIdentifiable;
import org.deegree.crs.components.Axis;
import org.deegree.crs.components.Ellipsoid;
import org.deegree.crs.components.GeodeticDatum;
import org.deegree.crs.components.Unit;
import org.deegree.crs.coordinatesystems.CompoundCRS;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.coordinatesystems.GeocentricCRS;
import org.deegree.crs.coordinatesystems.GeographicCRS;
import org.deegree.crs.coordinatesystems.ProjectedCRS;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.i18n.Messages;
import org.deegree.crs.transformations.coordinate.CRSTransformation;
import org.deegree.crs.transformations.coordinate.ConcatenatedTransform;
import org.deegree.crs.transformations.coordinate.DirectTransform;
import org.deegree.crs.transformations.coordinate.GeocentricTransform;
import org.deegree.crs.transformations.coordinate.MatrixTransform;
import org.deegree.crs.transformations.coordinate.ProjectionTransform;
import org.deegree.crs.transformations.helmert.Helmert;
import org.deegree.crs.transformations.polynomial.PolynomialTransformation;
import org.deegree.crs.utilities.Matrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>TransformationFactory</code> class is the central access point for all transformations between different
 * crs's.
 * <p>
 * It creates a transformation chain for two given CoordinateSystems by considering their type. For example the
 * Transformation chain from EPSG:31466 ( a projected crs with underlying geographic crs epsg:4314 using the DHDN datum
 * and the TransverseMercator Projection) to EPSG:28992 (another projected crs with underlying geographic crs epsg:4289
 * using the 'new Amersfoort Datum' and the StereographicAzimuthal Projection) would result in following Transformation
 * Chain:
 * <ol>
 * <li>Inverse projection - thus getting the coordinates in lat/lon for geographic crs epsg:4314</li>
 * <li>Geodetic transformation - thus getting x-y-z coordinates for geographic crs epsg:4314</li>
 * <li>WGS84 transformation -thus getting the x-y-z coordinates for the WGS84 datum</li>
 * <li>Inverse WGS84 transformation -thus getting the x-y-z coordinates for the geodetic from epsg:4289</li>
 * <li>Inverse geodetic - thus getting the lat/lon for epsg:4289</li>
 * <li>projection - getting the coordinates (in meters) for epsg:28992</li>
 * </ol>
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class TransformationFactory {
    private static Logger LOG = LoggerFactory.getLogger( TransformationFactory.class );

    /**
     * The default coordinate transformation factory. Will be constructed only when first needed.
     */
    private static TransformationFactory DEFAULT_INSTANCE = null;

    private TransformationFactory() {
        // nottin
    }

    /**
     * @return the default coordinate transformation factory.
     */
    public static synchronized TransformationFactory getInstance() {
        if ( DEFAULT_INSTANCE == null ) {
            DEFAULT_INSTANCE = new TransformationFactory();
        }
        return DEFAULT_INSTANCE;
    }

    /**
     * Creates a transformation between two coordinate systems. This method will examine the coordinate systems in order
     * to construct a transformation between them.
     * 
     * @param sourceCRS
     *            Input coordinate system.
     * @param targetCRS
     *            Output coordinate system.
     * @return A coordinate transformation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws TransformationException
     * @throws TransformationException
     *             if no transformation path has been found.
     * @throws IllegalArgumentException
     *             if the sourceCRS or targetCRS are <code>null</code>.
     * 
     */
    public Transformation createFromCoordinateSystems( final CoordinateSystem sourceCRS,
                                                       final CoordinateSystem targetCRS )
                            throws TransformationException, IllegalArgumentException {
        if ( sourceCRS == null ) {
            throw new IllegalArgumentException( "The source CRS may not be null" );
        }
        if ( targetCRS == null ) {
            throw new IllegalArgumentException( "The target CRS may not be null" );
        }
        // 1) Call crs-config.getTransformation for source and target (caching????)
        // 2) if( helmert, use this class)
        // 3) if direct, do this=????
        if ( ( sourceCRS.getType() != CoordinateSystem.GEOGRAPHIC_CRS
               && sourceCRS.getType() != CoordinateSystem.COMPOUND_CRS
               && sourceCRS.getType() != CoordinateSystem.PROJECTED_CRS && sourceCRS.getType() != CoordinateSystem.GEOCENTRIC_CRS )
             || ( targetCRS.getType() != CoordinateSystem.GEOGRAPHIC_CRS
                  && targetCRS.getType() != CoordinateSystem.COMPOUND_CRS
                  && targetCRS.getType() != CoordinateSystem.PROJECTED_CRS && targetCRS.getType() != CoordinateSystem.GEOCENTRIC_CRS ) ) {
            throw new TransformationException( sourceCRS, targetCRS,
                                               "Either the target crs type or the source crs type was unknown" );
        }

        if ( sourceCRS.equals( targetCRS ) ) {
            LOG.debug( "Source crs and target crs are equal, no transformation needed (returning identity matrix)." );
            final Matrix matrix = new Matrix( sourceCRS.getDimension() + 1 );
            matrix.setIdentity();
            return createMatrixTransform( sourceCRS, targetCRS, matrix );
        }

        Transformation result = null;
        // check if the source crs has an alternative transformation for the given target, if so use it
        if ( sourceCRS.hasDirectTransformation( targetCRS ) ) {
            PolynomialTransformation direct = sourceCRS.getDirectTransformation( targetCRS );
            LOG.debug( "Using direct (polynomial) transformation instead of a helmert transformation: "
                       + direct.getImplementationName() );
            result = new DirectTransform(
                                          direct,
                                          sourceCRS,
                                          new CRSIdentifiable(
                                                               new CRSCodeType[] { CRSCodeType.valueOf( direct.getCode()
                                                                                                        + "-CRSTransformation" ) } ) );
        } else {
            if ( sourceCRS.getType() == CoordinateSystem.GEOGRAPHIC_CRS ) {
                /**
                 * Geographic --> Geographic, Projected, Geocentric or Compound
                 */
                final GeographicCRS source = (GeographicCRS) sourceCRS;
                if ( targetCRS.getType() == CoordinateSystem.PROJECTED_CRS ) {
                    result = createTransformation( source, (ProjectedCRS) targetCRS );
                } else if ( targetCRS.getType() == CoordinateSystem.GEOGRAPHIC_CRS ) {
                    result = createTransformation( source, (GeographicCRS) targetCRS );
                } else if ( targetCRS.getType() == CoordinateSystem.GEOCENTRIC_CRS ) {
                    result = createTransformation( source, (GeocentricCRS) targetCRS );
                } else if ( targetCRS.getType() == CoordinateSystem.COMPOUND_CRS ) {
                    CompoundCRS target = (CompoundCRS) targetCRS;
                    CompoundCRS sTmp = new CompoundCRS(
                                                        target.getHeightAxis(),
                                                        source,
                                                        target.getDefaultHeight(),
                                                        new CRSIdentifiable(
                                                                             new CRSCodeType[] { CRSCodeType.valueOf( source.getCode()
                                                                                                                      + "_compound" ) } ) );
                    result = createTransformation( sTmp, target );
                }
            } else if ( sourceCRS.getType() == CoordinateSystem.PROJECTED_CRS ) {
                /**
                 * Projected --> Projected, Geographic, Geocentric or Compound
                 */
                final ProjectedCRS source = (ProjectedCRS) sourceCRS;
                if ( targetCRS.getType() == CoordinateSystem.PROJECTED_CRS ) {
                    result = createTransformation( source, (ProjectedCRS) targetCRS );
                } else if ( targetCRS.getType() == CoordinateSystem.GEOGRAPHIC_CRS ) {
                    result = createTransformation( source, (GeographicCRS) targetCRS );
                } else if ( targetCRS.getType() == CoordinateSystem.GEOCENTRIC_CRS ) {
                    result = createTransformation( source, (GeocentricCRS) targetCRS );
                } else if ( targetCRS.getType() == CoordinateSystem.COMPOUND_CRS ) {
                    CompoundCRS target = (CompoundCRS) targetCRS;
                    CompoundCRS sTmp = new CompoundCRS(
                                                        target.getHeightAxis(),
                                                        source,
                                                        target.getDefaultHeight(),
                                                        new CRSIdentifiable(
                                                                             new CRSCodeType[] { CRSCodeType.valueOf( source.getCode()
                                                                                                                      + "_compound" ) } ) );
                    result = createTransformation( sTmp, target );
                }
            } else if ( sourceCRS.getType() == CoordinateSystem.GEOCENTRIC_CRS ) {
                /**
                 * Geocentric --> Projected, Geographic, Geocentric or Compound
                 */
                final GeocentricCRS source = (GeocentricCRS) sourceCRS;
                if ( targetCRS.getType() == CoordinateSystem.PROJECTED_CRS ) {
                    result = createTransformation( source, (ProjectedCRS) targetCRS );
                } else if ( targetCRS.getType() == CoordinateSystem.GEOGRAPHIC_CRS ) {
                    result = createTransformation( source, (GeographicCRS) targetCRS );
                } else if ( targetCRS.getType() == CoordinateSystem.GEOCENTRIC_CRS ) {
                    result = createTransformation( source, (GeocentricCRS) targetCRS );
                } else if ( targetCRS.getType() == CoordinateSystem.COMPOUND_CRS ) {
                    CompoundCRS target = (CompoundCRS) targetCRS;
                    CompoundCRS sTmp = new CompoundCRS(
                                                        target.getHeightAxis(),
                                                        source,
                                                        target.getDefaultHeight(),
                                                        new CRSIdentifiable(
                                                                             new CRSCodeType[] { CRSCodeType.valueOf( source.getCode()
                                                                                                                      + "_compound" ) } ) );
                    result = createTransformation( sTmp, target );
                }
            } else if ( sourceCRS.getType() == CoordinateSystem.COMPOUND_CRS ) {
                /**
                 * Compound --> Projected, Geographic, Geocentric or Compound
                 */
                final CompoundCRS source = (CompoundCRS) sourceCRS;
                CompoundCRS target = null;
                if ( targetCRS.getType() != CoordinateSystem.COMPOUND_CRS ) {
                    target = new CompoundCRS(
                                              source.getHeightAxis(),
                                              targetCRS,
                                              source.getDefaultHeight(),
                                              new CRSIdentifiable(
                                                                   new CRSCodeType[] { CRSCodeType.valueOf( targetCRS.getCode()
                                                                                                            + "_compound" ) } ) );
                } else {
                    target = (CompoundCRS) targetCRS;
                }
                result = createTransformation( source, target );
            }
        }
        if ( result == null ) {
            LOG.debug( "The resulting transformation was null, returning an identity matrix." );
            final Matrix matrix = new Matrix( sourceCRS.getDimension() + 1 );
            matrix.setIdentity();
            result = createMatrixTransform( sourceCRS, targetCRS, matrix );
        } else {
            LOG.debug( "Concatenating the result, with the conversion matrices." );
            result = concatenate(
                                  createMatrixTransform( sourceCRS, sourceCRS, toStandardizedValues( sourceCRS, false ) ),
                                  result, createMatrixTransform( targetCRS, targetCRS, toStandardizedValues( targetCRS,
                                                                                                             true ) ) );
        }
        if ( LOG.isDebugEnabled() ) {
            StringBuilder output = new StringBuilder( "The resulting transformation chain: \n" );
            output = result.getTransformationPath( output );
            LOG.debug( output.toString() );

            if ( result instanceof MatrixTransform ) {
                LOG.debug( "Resulting matrix transform:\n" + ( (MatrixTransform) result ).getMatrix() );
            }

        }
        return result;

    }

    /**
     * Creates a matrix, with which incoming values will be transformed to a standardized form. This means, to radians
     * and meters.
     * 
     * @param sourceCRS
     *            to create the matrix for.
     * @param invert
     *            the values. Using the inverted scale, i.e. going from standard to crs specific.
     * @return the standardized matrix.
     * @throws TransformationException
     *             if the unit of one of the axis could not be transformed to one of the base units.
     */
    private Matrix toStandardizedValues( CoordinateSystem sourceCRS, boolean invert )
                            throws TransformationException {
        final int dim = sourceCRS.getDimension();
        Matrix result = null;
        Axis[] allAxis = sourceCRS.getAxis();
        for ( int i = 0; i < allAxis.length; ++i ) {
            Axis targetAxis = allAxis[i];
            if ( targetAxis != null ) {
                Unit targetUnit = targetAxis.getUnits();
                if ( !( Unit.RADIAN.equals( targetUnit ) || Unit.METRE.equals( targetUnit ) ) ) {
                    if ( !( targetUnit.canConvert( Unit.RADIAN ) || targetUnit.canConvert( Unit.METRE ) ) ) {
                        throw new TransformationException(
                                                           Messages.getMessage(
                                                                                "CRS_TRANSFORMATION_NO_APLLICABLE_UNIT",
                                                                                targetUnit ) );
                    }
                    // lazy instantiation
                    if ( result == null ) {
                        result = new Matrix( dim + 1 );
                        result.setIdentity();
                    }
                    result.setElement( i, i, invert ? 1d / targetUnit.getScale() : targetUnit.getScale() );
                }
            }
        }
        return result;
    }

    /**
     * @param sourceCRS
     * @param targetCRS
     * @return the transformation chain or <code>null</code> if the transformation operation is the identity.
     * @throws TransformationException
     */
    private Transformation createTransformation( GeocentricCRS sourceCRS, GeographicCRS targetCRS )
                            throws TransformationException {
        final Transformation result = createTransformation( targetCRS, sourceCRS );
        if ( result != null ) {
            result.inverse();
        }
        return result;
    }

    /**
     * @param sourceCRS
     * @param targetCRS
     * @return the transformation chain or <code>null</code> if the transformation operation is the identity.
     * @throws TransformationException
     */
    private Transformation createTransformation( GeocentricCRS sourceCRS, ProjectedCRS targetCRS )
                            throws TransformationException {
        final Transformation result = createTransformation( targetCRS, sourceCRS );
        if ( result != null ) {
            result.inverse();
        }
        return result;
    }

    /**
     * This method is valid for all transformations which use a compound crs, because the extra heightvalues need to be
     * considered throughout the transformation.
     * 
     * @param sourceCRS
     * @param targetCRS
     * @return the transformation chain or <code>null</code> if the transformation operation is the identity.
     * @throws TransformationException
     */
    private Transformation createTransformation( CompoundCRS sourceCRS, CompoundCRS targetCRS )
                            throws TransformationException {
        if ( sourceCRS.getUnderlyingCRS().equals( targetCRS.getUnderlyingCRS() ) ) {
            return null;
        }
        LOG.debug( "Creating compound( " + sourceCRS.getUnderlyingCRS().getCode() + ") ->compound transformation( "
                   + targetCRS.getUnderlyingCRS().getCode() + "): from (source): " + sourceCRS.getCode()
                   + " to(target): " + targetCRS.getCode() );
        final int sourceType = sourceCRS.getUnderlyingCRS().getType();
        final int targetType = targetCRS.getUnderlyingCRS().getType();
        Transformation result = null;
        // basic check for simple (invert) projections
        if ( sourceType == CoordinateSystem.PROJECTED_CRS && targetType == CoordinateSystem.GEOGRAPHIC_CRS ) {
            if ( ( ( (ProjectedCRS) sourceCRS.getUnderlyingCRS() ).getGeographicCRS() ).equals( targetCRS.getUnderlyingCRS() ) ) {
                result = new ProjectionTransform( (ProjectedCRS) sourceCRS.getUnderlyingCRS() );
                result.inverse();
            }
        }
        if ( sourceType == CoordinateSystem.GEOGRAPHIC_CRS && targetType == CoordinateSystem.PROJECTED_CRS ) {
            if ( ( ( (ProjectedCRS) targetCRS.getUnderlyingCRS() ).getGeographicCRS() ).equals( sourceCRS.getUnderlyingCRS() ) ) {
                result = new ProjectionTransform( (ProjectedCRS) targetCRS.getUnderlyingCRS() );
            }
        }
        if ( result == null ) {
            GeocentricCRS sourceGeocentric = null;
            if ( sourceType == CoordinateSystem.GEOCENTRIC_CRS ) {
                sourceGeocentric = (GeocentricCRS) sourceCRS.getUnderlyingCRS();
            } else {
                sourceGeocentric = new GeocentricCRS(
                                                      sourceCRS.getGeodeticDatum(),
                                                      CRSCodeType.valueOf( "tmp_" + sourceCRS.getCode() + "_geocentric" ),
                                                      sourceCRS.getName() + "_Geocentric" );
            }
            GeocentricCRS targetGeocentric = null;
            if ( targetType == CoordinateSystem.GEOCENTRIC_CRS ) {
                targetGeocentric = (GeocentricCRS) targetCRS.getUnderlyingCRS();
            } else {
                targetGeocentric = new GeocentricCRS(
                                                      targetCRS.getGeodeticDatum(),
                                                      CRSCodeType.valueOf( "tmp_" + targetCRS.getCode() + "_geocentric" ),
                                                      targetCRS.getName() + "_Geocentric" );
            }
            Transformation helmertTransformation = createTransformation( sourceGeocentric, targetGeocentric );

            Transformation sourceTransformationChain = null;
            Transformation targetTransformationChain = null;
            GeographicCRS sourceGeographic = null;
            GeographicCRS targetGeographic = null;
            switch ( sourceType ) {
            case CoordinateSystem.GEOCENTRIC_CRS:
                break;
            case CoordinateSystem.PROJECTED_CRS:
                sourceTransformationChain = new ProjectionTransform( (ProjectedCRS) sourceCRS.getUnderlyingCRS() );
                sourceTransformationChain.inverse();
                sourceGeographic = ( (ProjectedCRS) sourceCRS.getUnderlyingCRS() ).getGeographicCRS();
            case CoordinateSystem.GEOGRAPHIC_CRS:
                if ( sourceGeographic == null ) {
                    sourceGeographic = (GeographicCRS) sourceCRS.getUnderlyingCRS();
                }
                /*
                 * Only create a geocentric transform if the helmert transformation != null, e.g. the datums and
                 * ellipsoids are not equal.
                 */
                if ( helmertTransformation != null ) {
                    // create a 2d->3d mapping.
                    final CRSTransformation axisAligned = createMatrixTransform( sourceGeographic, sourceGeocentric,
                                                                                 swapAxis( sourceGeographic,
                                                                                           GeographicCRS.WGS84 ) );
                    if ( LOG.isDebugEnabled() ) {
                        StringBuilder sb = new StringBuilder(
                                                              "Resulting axis alignment between source geographic and source geocentric is:" );
                        if ( axisAligned == null ) {
                            sb.append( " not necessary" );
                        } else {
                            sb.append( "\n" ).append( ( (MatrixTransform) axisAligned ).getMatrix() );
                        }
                        LOG.debug( sb.toString() );
                    }
                    final CRSTransformation geoCentricTransform = new GeocentricTransform( sourceCRS, sourceGeocentric );
                    // concatenate the possible projection with the axis alignment and the geocentric transform.
                    sourceTransformationChain = concatenate( sourceTransformationChain, axisAligned,
                                                             geoCentricTransform );
                }
                break;
            }
            switch ( targetType ) {
            case CoordinateSystem.GEOCENTRIC_CRS:
                break;
            case CoordinateSystem.PROJECTED_CRS:
                targetTransformationChain = new ProjectionTransform( (ProjectedCRS) targetCRS.getUnderlyingCRS() );
                targetGeographic = ( (ProjectedCRS) targetCRS.getUnderlyingCRS() ).getGeographicCRS();
            case CoordinateSystem.GEOGRAPHIC_CRS:
                if ( targetGeographic == null ) {
                    targetGeographic = (GeographicCRS) targetCRS.getUnderlyingCRS();
                }
                /*
                 * Only create a geocentric transform if the helmert transformation != null, e.g. the datums and
                 * ellipsoids are not equal.
                 */
                if ( helmertTransformation != null ) {
                    // create a 2d->3d mapping.
                    final CRSTransformation axisAligned = createMatrixTransform( targetGeocentric, targetGeographic,
                                                                                 swapAxis( GeographicCRS.WGS84,
                                                                                           targetGeographic ) );
                    final CRSTransformation geoCentricTransform = new GeocentricTransform( targetCRS, targetGeocentric );
                    geoCentricTransform.inverse();
                    // concatenate the possible projection with the axis alignment and the geocentric transform.
                    targetTransformationChain = concatenate( geoCentricTransform, axisAligned,
                                                             targetTransformationChain );
                }
                break;
            }
            result = concatenate( sourceTransformationChain, helmertTransformation, targetTransformationChain );
        }
        return result;
    }

    /**
     * Creates a transformation between two geographic coordinate systems. This method is automatically invoked by
     * {@link #createFromCoordinateSystems createFromCoordinateSystems(...)}. The default implementation can adjust axis
     * order and orientation (e.g. transforming from <code>(NORTH,WEST)</code> to <code>(EAST,NORTH)</code>), performs
     * units conversion and apply Bursa Wolf transformation if needed.
     * 
     * @param sourceCRS
     *            Input coordinate system.
     * @param targetCRS
     *            Output coordinate system.
     * @return A coordinate transformation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws TransformationException
     *             if no transformation path has been found.
     */
    private Transformation createTransformation( final GeographicCRS sourceCRS, final GeographicCRS targetCRS )
                            throws TransformationException {
        final GeodeticDatum sourceDatum = sourceCRS.getGeodeticDatum();
        final GeodeticDatum targetDatum = targetCRS.getGeodeticDatum();
        // if ( sourceDatum.equals( targetDatum ) ) {
        // LOG.debug( "The datums of geographic (source): " + sourceCRS.getCode() + " equals geographic (target): "
        // + targetCRS.getCode() + " returning null" );
        // return null;
        // }
        LOG.debug( "Creating geographic ->geographic transformation: from (source): " + sourceCRS.getCode()
                   + " to(target): " + targetCRS.getCode() );
        final Ellipsoid sourceEllipsoid = sourceDatum.getEllipsoid();
        final Ellipsoid targetEllipsoid = targetDatum.getEllipsoid();
        // if a conversion needs totake place
        if ( sourceEllipsoid != null
             && !sourceEllipsoid.equals( targetEllipsoid )
             && ( ( sourceDatum.getWGS84Conversion() != null && sourceDatum.getWGS84Conversion().hasValues() ) || ( targetDatum.getWGS84Conversion() != null && targetDatum.getWGS84Conversion().hasValues() ) ) ) {
            /*
             * If the two geographic coordinate systems use different ellipsoid, convert from the source to target
             * ellipsoid through the geocentric coordinate system.
             */
            Transformation step1 = null;
            CRSTransformation step2 = null;
            Transformation step3 = null;
            // use the WGS84 Geocentric transform if no toWGS84 parameters are given and the datums ellipsoid is
            // actually a sphere.
            final GeocentricCRS sourceGCS = ( sourceEllipsoid.isSphere() && ( sourceDatum.getWGS84Conversion() == null || !sourceDatum.getWGS84Conversion().hasValues() ) ) ? GeocentricCRS.WGS84
                                                                                                                                                                           : new GeocentricCRS(
                                                                                                                                                                                                sourceDatum,
                                                                                                                                                                                                sourceCRS.getCode(),
                                                                                                                                                                                                sourceCRS.getName()
                                                                                                                                                                                                                        + "_Geocentric" );
            final GeocentricCRS targetGCS = ( targetEllipsoid.isSphere() && ( targetDatum.getWGS84Conversion() == null || !targetDatum.getWGS84Conversion().hasValues() ) ) ? GeocentricCRS.WGS84
                                                                                                                                                                           : new GeocentricCRS(
                                                                                                                                                                                                targetDatum,
                                                                                                                                                                                                targetCRS.getCode(),
                                                                                                                                                                                                targetCRS.getName()
                                                                                                                                                                                                                        + "_Geocentric" );

            // geographic->geocentric
            step1 = createTransformation( sourceCRS, sourceGCS );
            // helmert->inv_helmert
            step2 = createTransformation( sourceGCS, targetGCS );
            // geocentric->geographic
            step3 = createTransformation( targetCRS, targetGCS );

            if ( step3 != null ) {
                step3.inverse();// call inverseTransform from step 3.
            }
            return concatenate( step1, step2, step3 );
        }

        /*
         * Swap axis order, and rotate the longitude coordinate if prime meridians are different.
         */
        final Matrix matrix = swapAndRotateGeoAxis( sourceCRS, targetCRS );
        CRSTransformation result = createMatrixTransform( sourceCRS, targetCRS, matrix );
        if ( LOG.isDebugEnabled() ) {
            StringBuilder sb = new StringBuilder(
                                                  "Resulting axis alignment between source geographic and target geographic is:" );
            if ( result == null ) {
                sb.append( " not necessary" );
            } else {
                sb.append( "\n" ).append( ( (MatrixTransform) result ).getMatrix() );
            }
            LOG.debug( sb.toString() );
        }
        return result;
    }

    /**
     * Creates a transformation between a geographic and a projected coordinate systems. This method is automatically
     * invoked by {@link #createFromCoordinateSystems createFromCoordinateSystems(...)}.
     * 
     * @param sourceCRS
     *            Input coordinate system.
     * @param targetCRS
     *            Output coordinate system.
     * @return A coordinate transformation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws TransformationException
     *             if no transformation path has been found.
     */
    private Transformation createTransformation( final GeographicCRS sourceCRS, final ProjectedCRS targetCRS )
                            throws TransformationException {

        LOG.debug( "Creating geographic->projected transformation: from (source): " + sourceCRS.getCode()
                   + " to(target): " + targetCRS.getCode() );
        final GeographicCRS stepGeoCS = targetCRS.getGeographicCRS();

        final Transformation geo2geo = createTransformation( sourceCRS, stepGeoCS );
        final CRSTransformation swap = createMatrixTransform( stepGeoCS, targetCRS, swapAxis( stepGeoCS, targetCRS ) );
        if ( LOG.isDebugEnabled() ) {
            StringBuilder sb = new StringBuilder(
                                                  "Resulting axis alignment between target geographic and target projected is:" );
            if ( swap == null ) {
                sb.append( " not necessary" );
            } else {
                sb.append( "\n" ).append( ( (MatrixTransform) swap ).getMatrix() );
            }
            LOG.debug( sb.toString() );
        }
        final CRSTransformation projection = new ProjectionTransform( targetCRS );
        return concatenate( geo2geo, swap, projection );
    }

    /**
     * Creates a transformation between a geographic and a geocentric coordinate systems. Since the source coordinate
     * systems doesn't have a vertical axis, height above the ellipsoid is assumed equals to zero everywhere. This
     * method is automatically invoked by {@link #createFromCoordinateSystems createFromCoordinateSystems(...)}.
     * 
     * @param sourceCRS
     *            Input geographic coordinate system.
     * @param targetCRS
     *            Output coordinate system.
     * @return A coordinate transformation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws TransformationException
     *             if no transformation path has been found.
     */
    private Transformation createTransformation( final GeographicCRS sourceCRS, final GeocentricCRS targetCRS )
                            throws TransformationException {
        LOG.debug( "Creating geographic -> geocentric (helmert) transformation: from (source): " + sourceCRS.getCode()
                   + " to(target): " + targetCRS.getCode() );
        /*
         * if ( !PrimeMeridian.GREENWICH.equals( targetCRS.getGeodeticDatum().getPrimeMeridian() ) ) { throw new
         * TransformationException( "The rotation from " +
         * targetCRS.getGeodeticDatum().getPrimeMeridian().getIdentifier() + " to Greenwich prime meridian is not yet
         * implemented" ); }
         */
        GeocentricCRS sourceGeocentric = new GeocentricCRS( sourceCRS.getGeodeticDatum(),
                                                            CRSCodeType.valueOf( "tmp_" + sourceCRS.getCode()
                                                                                 + "_geocentric" ), sourceCRS.getName()
                                                                                                    + "_geocentric" );
        CRSTransformation helmertTransformation = createTransformation( sourceGeocentric, targetCRS );
        // if no helmert transformation is needed, the targetCRS equals the source-geocentric.
        if ( helmertTransformation == null ) {
            sourceGeocentric = targetCRS;
        }
        final CRSTransformation axisAlign = createMatrixTransform(
                                                                   sourceCRS,
                                                                   sourceGeocentric,
                                                                   swapAndRotateGeoAxis( sourceCRS, GeographicCRS.WGS84 ) );
        if ( LOG.isDebugEnabled() ) {
            StringBuilder sb = new StringBuilder(
                                                  "Resulting axis alignment between source geographic and target geocentric is:" );
            if ( axisAlign == null ) {
                sb.append( " not necessary" );
            } else {
                sb.append( "\n" ).append( ( (MatrixTransform) axisAlign ).getMatrix() );
            }
            LOG.debug( sb.toString() );
        }
        final CRSTransformation geocentric = new GeocentricTransform( sourceCRS, sourceGeocentric );
        return concatenate( axisAlign, geocentric, helmertTransformation );
    }

    /**
     * Creates a transformation between two projected coordinate systems. This method is automatically invoked by
     * {@link #createFromCoordinateSystems createFromCoordinateSystems(...)}. The default implementation can adjust axis
     * order and orientation. It also performs units conversion if it is the only extra change needed. Otherwise, it
     * performs three steps:
     * 
     * <ol>
     * <li>Unproject <code>sourceCRS</code>.</li>
     * <li>Transform from <code>sourceCRS.geographicCS</code> to <code>
     * targetCRS.geographicCS</code>.</li>
     * <li>Project <code>targetCRS</code>.</li>
     * </ol>
     * 
     * @param sourceCRS
     *            Input coordinate system.
     * @param targetCRS
     *            Output coordinate system.
     * @return A coordinate transformation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws TransformationException
     *             if no transformation path has been found.
     */
    private Transformation createTransformation( final ProjectedCRS sourceCRS, final ProjectedCRS targetCRS )
                            throws TransformationException {
        LOG.debug( "Creating projected -> projected transformation: from (source): " + sourceCRS.getCode()
                   + " to(target): " + targetCRS.getCode() );
        if ( sourceCRS.getProjection().equals( targetCRS.getProjection() ) ) {
            return null;
        }
        final GeographicCRS sourceGeo = sourceCRS.getGeographicCRS();
        final GeographicCRS targetGeo = targetCRS.getGeographicCRS();
        final Transformation inverseProjection = createTransformation( sourceCRS, sourceGeo );
        final Transformation geo2geo = createTransformation( sourceGeo, targetGeo );
        final Transformation projection = createTransformation( targetGeo, targetCRS );
        return concatenate( inverseProjection, geo2geo, projection );
    }

    /**
     * Creates a transformation between a projected and a geocentric coordinate systems. This method is automatically
     * invoked by {@link #createFromCoordinateSystems createFromCoordinateSystems(...)}. This method doesn't need to be
     * public since its decomposition in two step should be general enough.
     * 
     * @param sourceCRS
     *            Input projected coordinate system.
     * @param targetCRS
     *            Output coordinate system.
     * @return A coordinate transformation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws TransformationException
     *             if no transformation path has been found.
     */
    private Transformation createTransformation( final ProjectedCRS sourceCRS, final GeocentricCRS targetCRS )
                            throws TransformationException {
        LOG.debug( "Creating projected -> geocentric transformation: from (source): " + sourceCRS.getCode()
                   + " to(target): " + targetCRS.getCode() );
        final GeographicCRS sourceGCS = sourceCRS.getGeographicCRS();

        final Transformation inverseProjection = createTransformation( sourceCRS, sourceGCS );
        final Transformation geocentric = createTransformation( sourceGCS, targetCRS );
        return concatenate( inverseProjection, geocentric );
    }

    /**
     * Creates a transformation between a projected and a geographic coordinate systems. This method is automatically
     * invoked by {@link #createFromCoordinateSystems createFromCoordinateSystems(...)}. The default implementation
     * returns <code>{@link #createTransformation(GeographicCRS, ProjectedCRS)} createTransformation}(targetCRS,
     * sourceCRS) inverse)</code>.
     * 
     * @param sourceCRS
     *            Input coordinate system.
     * @param targetCRS
     *            Output coordinate system.
     * @return A coordinate transformation from <code>sourceCRS</code> to <code>targetCRS</code> or <code>null</code> if
     *         {@link ProjectedCRS#getGeographicCRS()}.equals targetCRS.
     * @throws TransformationException
     *             if no transformation path has been found.
     */
    private Transformation createTransformation( final ProjectedCRS sourceCRS, final GeographicCRS targetCRS )
                            throws TransformationException {
        LOG.debug( "Creating projected->geographic transformation: from (source): " + sourceCRS.getCode()
                   + " to(target): " + targetCRS.getCode() );
        Transformation result = createTransformation( targetCRS, sourceCRS );
        if ( result != null ) {
            result.inverse();
        }
        return result;

    }

    /**
     * Creates a transformation between two geocentric coordinate systems. This method is automatically invoked by
     * {@link #createFromCoordinateSystems createFromCoordinateSystems(...)}. The default implementation can adjust for
     * axis order and orientation, adjust for prime meridian, performs units conversion and apply Bursa Wolf
     * transformation if needed.
     * 
     * @param sourceCRS
     *            Input coordinate system.
     * @param targetCRS
     *            Output coordinate system.
     * @return A coordinate transformation from <code>sourceCRS</code> to <code>targetCRS</code>.
     * @throws TransformationException
     *             if no transformation path has been found.
     */
    private CRSTransformation createTransformation( final GeocentricCRS sourceCRS, final GeocentricCRS targetCRS )
                            throws TransformationException {
        LOG.debug( "Creating geocentric->geocetric transformation: from (source): " + sourceCRS.getCode()
                   + " to(target): " + targetCRS.getCode() );
        final GeodeticDatum sourceDatum = sourceCRS.getGeodeticDatum();
        final GeodeticDatum targetDatum = targetCRS.getGeodeticDatum();
        /*
         * if ( !PrimeMeridian.GREENWICH.equals( sourceDatum.getPrimeMeridian() ) || !PrimeMeridian.GREENWICH.equals(
         * targetDatum.getPrimeMeridian() ) ) { throw new TransformationException( "Rotation of prime meridian not yet
         * implemented" ); }
         */
        CRSTransformation result = null;
        if ( !sourceDatum.equals( targetDatum ) ) {
            final Ellipsoid sourceEllipsoid = sourceDatum.getEllipsoid();
            final Ellipsoid targetEllipsoid = targetDatum.getEllipsoid();
            /*
             * If the two coordinate systems use different ellipsoid, convert from the source to target ellipsoid
             * through the geocentric coordinate system.
             */
            if ( sourceEllipsoid != null
                 && !sourceEllipsoid.equals( targetEllipsoid )
                 && ( ( sourceDatum.getWGS84Conversion() != null && sourceDatum.getWGS84Conversion().hasValues() ) || ( targetDatum.getWGS84Conversion() != null && targetDatum.getWGS84Conversion().hasValues() ) ) ) {
                LOG.debug( "Creating helmert transformation: source(" + sourceCRS.getCode() + ")->target("
                           + targetCRS.getCode() + ")." );

                // Transform between different ellipsoids using Bursa Wolf parameters.
                Matrix tmp = swapAxis( sourceCRS, GeocentricCRS.WGS84 );
                Matrix4d forwardAxisAlign = null;
                if ( tmp != null ) {
                    forwardAxisAlign = new Matrix4d();
                    tmp.get( forwardAxisAlign );
                }
                final Matrix4d forwardToWGS = getWGS84Parameters( sourceDatum );
                final Matrix4d inverseToWGS = getWGS84Parameters( targetDatum );
                tmp = swapAxis( GeocentricCRS.WGS84, targetCRS );
                Matrix4d resultMatrix = null;
                if ( tmp != null ) {
                    resultMatrix = new Matrix4d();
                    tmp.get( resultMatrix );
                }
                if ( forwardAxisAlign == null && forwardToWGS == null && inverseToWGS == null && resultMatrix == null ) {
                    LOG.debug( "The given geocentric crs's do not need a helmert transformation (but they are not equal), returning identity" );
                    resultMatrix = new Matrix4d();
                    resultMatrix.setIdentity();
                } else {
                    LOG.debug( "step1 matrix: \n " + forwardAxisAlign );
                    LOG.debug( "step2 matrix: \n " + forwardToWGS );
                    LOG.debug( "step3 matrix: \n " + inverseToWGS );
                    LOG.debug( "step4 matrix: \n " + resultMatrix );
                    if ( inverseToWGS != null ) {
                        inverseToWGS.invert(); // Invert in place.
                        LOG.debug( "inverseToWGS inverted matrix: \n " + inverseToWGS );
                    }
                    if ( resultMatrix != null ) {
                        if ( inverseToWGS != null ) {
                            resultMatrix.mul( inverseToWGS ); // step4 = step4*step3
                            LOG.debug( "resultMatrix (after mul with inverseToWGS): \n " + resultMatrix );
                        }
                        if ( forwardToWGS != null ) {
                            resultMatrix.mul( forwardToWGS ); // step4 = step4*step3*step2
                            LOG.debug( "resultMatrix (after mul with forwardToWGS2): \n " + resultMatrix );
                        }
                        if ( forwardAxisAlign != null ) {
                            resultMatrix.mul( forwardAxisAlign ); // step4 = step4*step3*step2*step1
                        }
                    } else if ( inverseToWGS != null ) {
                        resultMatrix = inverseToWGS;
                        if ( forwardToWGS != null ) {
                            resultMatrix.mul( forwardToWGS ); // step4 = step3*step2*step1
                            LOG.debug( "resultMatrix (after mul with forwardToWGS2): \n " + resultMatrix );
                        }
                        if ( forwardAxisAlign != null ) {
                            resultMatrix.mul( forwardAxisAlign ); // step4 = step3*step2*step1
                        }
                    } else if ( forwardToWGS != null ) {
                        resultMatrix = forwardToWGS;
                        if ( forwardAxisAlign != null ) {
                            resultMatrix.mul( forwardAxisAlign ); // step4 = step2*step1
                        }
                    } else {
                        resultMatrix = forwardAxisAlign;
                    }
                }

                LOG.debug( "The resulting helmert transformation matrix: from( " + sourceCRS.getCode() + ") to("
                           + targetCRS.getCode() + ")\n " + resultMatrix );
                result = new MatrixTransform( sourceCRS, targetCRS, resultMatrix, "Helmert-Transformation" );

            }
        }
        if ( result == null ) {
            /*
             * Swap axis order, and rotate the longitude coordinate if prime meridians are different.
             */
            final Matrix matrix = swapAxis( sourceCRS, targetCRS );
            result = createMatrixTransform( sourceCRS, targetCRS, matrix );
            if ( LOG.isDebugEnabled() ) {
                StringBuilder sb = new StringBuilder(
                                                      "Resulting axis alignment between source geocentric and target geocentric is:" );
                if ( result == null ) {
                    sb.append( " not necessary" );
                } else {
                    sb.append( "\n" ).append( ( (MatrixTransform) result ).getMatrix() );
                }
                LOG.debug( sb.toString() );
            }
        }
        return result;
    }

    /**
     * Concatenates two existing transforms.
     * 
     * @param first
     *            The first transform to apply to points.
     * @param second
     *            The second transform to apply to points.
     * @return The concatenated transform.
     * 
     */
    private Transformation concatenateTransformations( Transformation first, Transformation second ) {
        if ( first == null ) {
            LOG.debug( "Concatenate: the first transform  is null." );
            return second;
        }
        if ( second == null ) {
            LOG.debug( "Concatenate: the second transform  is null." );
            return first;
        }
        // if one of the two is an identity transformation, just return the other.
        if ( first.isIdentity() ) {
            LOG.debug( "Concatenate:  the first transform  is the identity." );
            return second;
        }
        if ( second.isIdentity() ) {
            LOG.debug( "Concatenate:  the second transform is the identity." );
            return first;
        }

        /*
         * If one transform is the inverse of the other, just return an identitiy transform.
         */
        if ( first.areInverse( second ) ) {
            LOG.debug( "Transformation1 and Transformation2 are inverse operations, returning null" );
            return null;
        }

        /*
         * If both transforms use matrix, then we can create a single transform using the concatened matrix.
         */
        if ( first instanceof MatrixTransform && second instanceof MatrixTransform ) {
            GMatrix m1 = ( (MatrixTransform) first ).getMatrix();
            GMatrix m2 = ( (MatrixTransform) second ).getMatrix();
            if ( m1 == null ) {
                if ( m2 == null ) {
                    // both matrices are null, just return the identiy matrix.
                    return new MatrixTransform( first.getSourceCRS(), first.getTargetCRS(),
                                                new GMatrix( second.getTargetDimension() + 1,
                                                             first.getSourceDimension() + 1 ) );
                }
                return second;
            } else if ( m2 == null ) {
                return first;
            }

            m2.mul( m1 );
            // m1.mul( m2 );
            LOG.debug( "Concatenate: both transforms are matrices, resulting multiply:\n" + m2 );
            MatrixTransform result = new MatrixTransform( first.getSourceCRS(), second.getTargetCRS(), m2 );
            // if ( result.isIdentity() ) {
            // return null;
            // }
            return result;
        }

        /*
         * If one or both math transform are instance of {@link ConcatenatedTransform}, then concatenate
         * <code>tr1</code> or <code>tr2</code> with one of step transforms.
         */
        if ( first instanceof ConcatenatedTransform ) {
            final ConcatenatedTransform ctr = (ConcatenatedTransform) first;
            first = ctr.getFirstTransform();
            second = concatenateTransformations( ctr.getSecondTransform(), second );
        } else if ( second instanceof ConcatenatedTransform ) {
            final ConcatenatedTransform ctr = (ConcatenatedTransform) second;
            first = concatenateTransformations( first, ctr.getFirstTransform() );
            second = ctr.getSecondTransform();
        }
        // because of the concatenation one of the transformations may be null.
        if ( first == null ) {
            return second;
        }
        if ( second == null ) {
            return first;
        }

        return new ConcatenatedTransform( first, second );
    }

    /**
     * Creates an affine transform from a matrix.
     * 
     * @param matrix
     *            The matrix used to define the affine transform.
     * @return The affine transform.
     * @throws TransformationException
     *             if the matrix is not affine.
     * 
     */
    private MatrixTransform createMatrixTransform( CoordinateSystem sourceCRS, CoordinateSystem targetCRS,
                                                   final Matrix matrix )
                            throws TransformationException {
        if ( matrix == null ) {
            return null;
        }
        if ( matrix.isAffine() ) {// Affine transform are square.
            if ( matrix.getNumRow() == 3 && matrix.getNumCol() == 3 && !matrix.isIdentity() ) {
                Matrix3d tmp = matrix.toAffineTransform();
                return new MatrixTransform( sourceCRS, targetCRS, tmp );
            }
            return new MatrixTransform( sourceCRS, targetCRS, matrix );
        }
        throw new TransformationException( "Given matrix is not affine, cannot continue" );
    }

    /**
     * @return the WGS84 parameters as an affine transform, or <code>null</code> if not available.
     */
    private Matrix4d getWGS84Parameters( final GeodeticDatum datum ) {
        final Helmert info = datum.getWGS84Conversion();
        if ( info != null && info.hasValues() ) {
            return info.getAsAffineTransform();
        }
        return null;
    }

    /**
     * Concatenate two transformation steps.
     * 
     * @param step1
     *            The first step, or <code>null</code> for the identity transform.
     * @param step2
     *            The second step, or <code>null</code> for the identity transform.
     * @return A concatenated transform, or <code>null</code> if all arguments was nul.
     */
    private Transformation concatenate( final Transformation step1, final Transformation step2 ) {
        if ( step1 == null )
            return step2;
        if ( step2 == null )
            return step1;
        return concatenateTransformations( step1, step2 );
    }

    /**
     * Concatenate three transformation steps.
     * 
     * @param step1
     *            The first step, or <code>null</code> for the identity transform.
     * @param step2
     *            The second step, or <code>null</code> for the identity transform.
     * @param step3
     *            The third step, or <code>null</code> for the identity transform.
     * @return A concatenated transform, or <code>null</code> if all arguments were <code>null</code>.
     */
    private Transformation concatenate( final Transformation step1, final Transformation step2,
                                        final Transformation step3 ) {
        if ( step1 == null ) {
            return concatenate( step2, step3 );
        }
        if ( step2 == null ) {
            return concatenate( step1, step3 );
        }
        if ( step3 == null ) {
            return concatenate( step1, step2 );
        }
        return concatenateTransformations( step1, concatenateTransformations( step2, step3 ) );
    }

    /**
     * @return an affine transform between two coordinate systems. Only units and axis order (e.g. transforming from
     *         (NORTH,WEST) to (EAST,NORTH)) are taken in account. Other attributes (especially the datum) must be
     *         checked before invoking this method.
     * 
     * @param sourceCRS
     *            The source coordinate system.
     * @param targetCRS
     *            The target coordinate system.
     */
    private Matrix swapAxis( final CoordinateSystem sourceCRS, final CoordinateSystem targetCRS )
                            throws TransformationException {
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Creating swap matrix from: " + sourceCRS.getCode() + " to: " + targetCRS.getCode() );
            LOG.debug( "Source Axis:\n" + Arrays.toString( sourceCRS.getAxis() ) );
            LOG.debug( "Target Axis:\n" + Arrays.toString( targetCRS.getAxis() ) );
        }
        final Matrix matrix;
        try {
            matrix = new Matrix( sourceCRS.getAxis(), targetCRS.getAxis() );
        } catch ( RuntimeException e ) {
            throw new TransformationException( sourceCRS, targetCRS, e.getMessage() );
        }
        return matrix.isIdentity() ? null : matrix;
    }

    /**
     * @return an affine transform between two geographic coordinate systems. Only units, axis order (e.g. transforming
     *         from (NORTH,WEST) to (EAST,NORTH)) and prime meridian are taken in account. Other attributes (especially
     *         the datum) must be checked before invoking this method.
     * 
     * @param sourceCRS
     *            The source coordinate system.
     * @param targetCRS
     *            The target coordinate system.
     */
    private Matrix swapAndRotateGeoAxis( final GeographicCRS sourceCRS, final GeographicCRS targetCRS )
                            throws TransformationException {
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Creating geo swap/rotate matrix from: " + sourceCRS.getCode() + " to: " + targetCRS.getCode() );
        }
        Matrix matrix = swapAxis( sourceCRS, targetCRS );
        if ( !sourceCRS.getGeodeticDatum().getPrimeMeridian().equals( targetCRS.getGeodeticDatum().getPrimeMeridian() ) ) {
            if ( matrix == null ) {
                matrix = new Matrix( sourceCRS.getDimension() + 1 );
            }
            Axis[] targetAxis = targetCRS.getAxis();
            final int lastMatrixColumn = matrix.getNumCol() - 1;
            for ( int i = 0; i < targetAxis.length; ++i ) {
                // Find longitude, and apply a translation if prime meridians are different.
                final int orientation = targetAxis[i].getOrientation();
                if ( Axis.AO_WEST == Math.abs( orientation ) ) {
                    LOG.debug( "Adding prime-meridian translation to axis:" + targetAxis[i] );
                    final double sourceLongitude = sourceCRS.getGeodeticDatum().getPrimeMeridian().getLongitudeAsRadian();
                    final double targetLongitude = targetCRS.getGeodeticDatum().getPrimeMeridian().getLongitudeAsRadian();
                    if ( Math.abs( sourceLongitude - targetLongitude ) > EPS11 ) {
                        double translation = targetLongitude - sourceLongitude;
                        if ( Axis.AO_WEST == orientation ) {
                            translation = -translation;
                        }
                        // add the translation to the matrix translate element of this axis
                        matrix.setElement( i, lastMatrixColumn, matrix.getElement( i, lastMatrixColumn ) - translation );

                    }
                }
            }
        } else {
            LOG.debug( "The primemeridians of the geographic crs's are equal, so no translation needed." );
        }
        return matrix;
    }
}

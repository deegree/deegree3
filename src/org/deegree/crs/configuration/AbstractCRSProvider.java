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

package org.deegree.crs.configuration;

import static org.deegree.crs.utilities.MappingUtils.matchEPSGString;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.deegree.crs.CRSCodeType;
import org.deegree.crs.CRSIdentifiable;
import org.deegree.crs.components.GeodeticDatum;
import org.deegree.crs.configuration.resources.CRSResource;
import org.deegree.crs.coordinatesystems.CompoundCRS;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.coordinatesystems.GeographicCRS;
import org.deegree.crs.coordinatesystems.ProjectedCRS;
import org.deegree.crs.exceptions.CRSConfigurationException;
import org.deegree.crs.i18n.Messages;
import org.deegree.crs.projections.azimuthal.LambertAzimuthalEqualArea;
import org.deegree.crs.projections.azimuthal.StereographicAlternative;
import org.deegree.crs.projections.azimuthal.StereographicAzimuthal;
import org.deegree.crs.projections.conic.LambertConformalConic;
import org.deegree.crs.projections.cylindric.TransverseMercator;
import org.deegree.crs.transformations.Transformation;
import org.deegree.crs.transformations.coordinate.GeocentricTransform;
import org.deegree.crs.transformations.coordinate.NotSupportedTransformation;
import org.deegree.crs.transformations.helmert.Helmert;
import org.deegree.crs.transformations.polynomial.PolynomialTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * @param <T>
 *            the type of object the parse method awaits.
 * 
 */
public abstract class AbstractCRSProvider<T> implements CRSProvider {

    private static Logger LOG = LoggerFactory.getLogger( AbstractCRSProvider.class );

    private static Map<CRSCodeType, CRSIdentifiable> cachedIdentifiables = new HashMap<CRSCodeType, CRSIdentifiable>(
                                                                                                                      42124 );

    private CRSResource<T> resolver;

    /**
     * @param <K>
     * @param properties
     * @param subType
     * @param defaultResolver
     */
    @SuppressWarnings("unchecked")
    public <K extends CRSResource<T>> AbstractCRSProvider( Properties properties, Class<K> subType,
                                                           CRSResource<T> defaultResolver ) {
        if ( properties == null ) {
            throw new IllegalArgumentException( "The properties may not be null." );
        }
        String className = properties.getProperty( "CRS_RESOURCE" );
        if ( className == null || "".equals( className.trim() ) ) {
            if ( defaultResolver != null ) {
                LOG.warn( "Found no configured CRS-Resource to use, trying default: "
                          + defaultResolver.getClass().getCanonicalName() );
                resolver = defaultResolver;
            } else {
                LOG.debug( "Found no configured CRS-Resource and no default crs resource supplied, hoping for the set method." );
            }
        } else {
            try {
                Class<?> tc = null;
                if ( subType == null ) {
                    StringBuilder sb = new StringBuilder( "No subtype suplied trying to use " );
                    if ( defaultResolver != null ) {
                        sb.append( " the default resolver: " );
                        tc = defaultResolver.getClass();
                    } else {
                        tc = CRSResource.class;
                    }
                    sb.append( tc.getCanonicalName() ).append( " to create a subtype from. " );
                    LOG.warn( sb.toString() );
                } else {
                    tc = subType.getClass();
                }
                // use reflection to instantiate the configured provider.
                Class<?> t = Class.forName( className );
                t.asSubclass( tc );
                LOG.debug( "Trying to load configured CRS provider from classname: " + className );
                Constructor<?> constructor = t.getConstructor( this.getClass(), Properties.class );
                if ( constructor == null ) {
                    LOG.error( "No constructor ( " + this.getClass() + ", Properties.class) found in class:"
                               + className );
                } else {
                    resolver = (K) constructor.newInstance( this, properties );
                }
            } catch ( InstantiationException e ) {
                LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", className, e.getMessage() ) );
            } catch ( IllegalAccessException e ) {
                LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", className, e.getMessage() ), e );
            } catch ( ClassNotFoundException e ) {
                LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", className, e.getMessage() ), e );
            } catch ( SecurityException e ) {
                LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", className, e.getMessage() ), e );
            } catch ( NoSuchMethodException e ) {
                LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", className, e.getMessage() ), e );
            } catch ( IllegalArgumentException e ) {
                LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", className, e.getMessage() ), e );
            } catch ( InvocationTargetException e ) {
                LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", className, e.getMessage() ), e );
            } catch ( Throwable t ) {
                LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", className, t.getMessage() ), t );
            } finally {
                if ( resolver == null ) {
                    LOG.info( "The configured class: " + className + " was not instantiated." );
                    if ( defaultResolver != null ) {
                        LOG.info( "Trying to instantiate the default resolver: "
                                  + defaultResolver.getClass().getCanonicalName() );
                        resolver = defaultResolver;
                    } else {
                        LOG.warn( "No default crs resource supplied, hoping for the set method." );
                    }
                }
            }
        }
    }

    /**
     * Retrieves the {@link CoordinateSystem} from the set provider that is identified by the given {@link CRSCodeType}
     * id.
     * 
     * @param id
     *            the {@link CRSCodeType} of the wanted crs
     * @return the {@link CoordinateSystem} that corresponds to the id
     * @throws CRSConfigurationException
     */
    public CoordinateSystem getCRSByCode( CRSCodeType id )
                            throws CRSConfigurationException {

        if ( resolver == null ) {
            throw new CRSConfigurationException( "No resolver initialized, this may not be." );
        }
        CoordinateSystem result = null;
        if ( id != null ) {
            LOG.debug( "Trying to load crs with id: " + id + " from cache." );
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( cachedIdentifiables.keySet().toString() );
            }
            if ( cachedIdentifiables.containsKey( id ) ) {
                CRSIdentifiable r = cachedIdentifiables.get( id );
                LOG.debug( "Found CRSIdentifiable: " + r.getCodeAndName() + " from given id: " + id );
                if ( !( r instanceof CoordinateSystem ) ) {
                    LOG.error( "Found CRSIdentifiable: " + r.getCodeAndName()
                               + " but it is not a coordinate system, your db is inconsistent return null." );
                    r = null;
                }
                result = (CoordinateSystem) r;
            }
            if ( result == null ) {
                LOG.debug( "No crs with id: " + id + " found in cache." );
                try {
                    result = parseCoordinateSystem( resolver.getURIAsType( id.getOriginal() ) );
                } catch ( IOException e ) {
                    LOG.debug( e.getLocalizedMessage(), e );
                    throw new CRSConfigurationException( e );
                }
                if ( result != null ) {
                    GeographicCRS t = null;
                    if ( result.getType() == CoordinateSystem.COMPOUND_CRS ) {
                        if ( ( (CompoundCRS) result ).getUnderlyingCRS().getType() == CoordinateSystem.PROJECTED_CRS ) {
                            t = ( (ProjectedCRS) ( (CompoundCRS) result ).getUnderlyingCRS() ).getGeographicCRS();
                        } else if ( ( (CompoundCRS) result ).getUnderlyingCRS().getType() == CoordinateSystem.GEOGRAPHIC_CRS ) {
                            t = (GeographicCRS) ( (CompoundCRS) result ).getUnderlyingCRS();
                        } else {
                            LOG.warn( "Wgs84 Transformation lookup is currently only supported for GeographicCRS-chains." );
                        }
                    } else if ( result.getType() == CoordinateSystem.PROJECTED_CRS ) {
                        t = ( (ProjectedCRS) result ).getGeographicCRS();
                    } else if ( result.getType() == CoordinateSystem.GEOGRAPHIC_CRS ) {
                        t = (GeographicCRS) result;
                    } else {
                        LOG.warn( "Wgs84 Transformation lookup is currently only supported for GeographicCRS-chains." );
                    }
                    if ( t != null ) {
                        Helmert wgs84 = t.getGeodeticDatum().getWGS84Conversion();
                        if ( wgs84 == null ) {
                            wgs84 = resolver.getWGS84Transformation( t );
                        }
                        if ( wgs84 != null ) {
                            if ( wgs84.getSourceCRS() == null ) {
                                wgs84.setSourceCRS( t );
                                addIdToCache( wgs84, true );
                            }
                            GeodeticDatum datum = result.getGeodeticDatum();
                            if ( datum != null ) {
                                datum.setToWGS84( wgs84 );
                                // update the cache as well
                                addIdToCache( datum, true );
                            }

                        }
                    }
                }
            }
        }
        if ( result == null ) {
            LOG.debug( "The id: "
                       + id
                       + " could not be mapped to a valid deegree-crs, currently projectedCRS, geographicCRS, compoundCRS and geocentricCRS are supported." );
        } else {
            /**
             * Adding the used underlying crs's to the cache.
             */
            addIdToCache( result, false );
            if ( result.getType() == CoordinateSystem.COMPOUND_CRS ) {
                addIdToCache( ( (CompoundCRS) result ).getUnderlyingCRS(), false );
                if ( ( (CompoundCRS) result ).getUnderlyingCRS().getType() == CoordinateSystem.PROJECTED_CRS ) {
                    addIdToCache( ( (ProjectedCRS) ( (CompoundCRS) result ).getUnderlyingCRS() ).getGeographicCRS(),
                                  false );
                }
            } else if ( result.getType() == CoordinateSystem.PROJECTED_CRS ) {
                addIdToCache( ( (ProjectedCRS) result ).getGeographicCRS(), false );
            }
        }
        return result;
    }

    /**
     * Set the resolver to the given resolver.
     * 
     * @param newResolver
     */
    protected void setResolver( CRSResource<T> newResolver ) {
        this.resolver = newResolver;
    }

    /**
     * @return the resolver for a type.
     */
    protected CRSResource<T> getResolver() {
        return resolver;
    }

    /**
     * @param crsDefinition
     *            containing the definition of a crs in the understood type.
     * @return a {@link CoordinateSystem} instance initialized with values from the given type definition fragment or
     *         <code>null</code> if the given crsDefinition is <code>null</code> or not known.
     * @throws CRSConfigurationException
     *             if an error was found in the given crsDefintion
     */
    protected abstract CoordinateSystem parseCoordinateSystem( T crsDefinition )
                            throws CRSConfigurationException;

    /**
     * @param transformationDefinition
     *            containing the parameters needed to build a Transformation.
     * @return a {@link Transformation} instance initialized with values from the given definition or <code>null</code>
     *         if the given transformationDefintion is <code>null</code>. If the parsed transformation is not
     *         supported or a {@link NotSupportedTransformation} will be returned.
     * @throws CRSConfigurationException
     *             if an error was found in the given crsDefintion
     */
    public abstract Transformation parseTransformation( T transformationDefinition )
                            throws CRSConfigurationException;

    /**
     * The id are what they are, not trimming 'upcasing' or other modifications will be done in this method.
     * 
     * @param expectedType
     *            The class of type T which is expected.
     * @param <V>
     *            the type to cast to if the casting fails, null will be returned.
     * @param ids
     *            to search the cache for
     * @return the {@link CRSIdentifiable} of the first matching id or <code>null</code> if it was not found.
     */
    public <V extends CRSIdentifiable> V getCachedIdentifiable( Class<V> expectedType, CRSIdentifiable ids ) {
        if ( ids == null ) {
            return null;
        }
        return getCachedIdentifiable( expectedType, ids.getCodes() );
    }

    /**
     * The id are what they are, not trimming 'upcasing' or other modifications will be done in this method.
     * 
     * @param expectedType
     *            The class of type T which is expected.
     * @param <V>
     *            the type to cast to if the casting fails, null will be returned.
     * @param ids
     *            to search the cache for
     * @return the {@link CRSIdentifiable} of the first matching id or <code>null</code> if it was not found.
     */
    public <V extends CRSIdentifiable> V getCachedIdentifiable( Class<V> expectedType, String[] ids ) {
        if ( ids == null || ids.length == 0 ) {
            return null;
        }
        V result = null;
        for ( int i = 0; i < ids.length && result == null; i++ ) {
            result = getCachedIdentifiable( expectedType, ids[i] );
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "Searched for id: " + ids[i] + " resulted in: "
                           + ( ( result == null ) ? "null" : result.getCode() ) );
            }
        }
        return result;
    }

    /**
     * Get a {@link CRSIdentifiable} (actually a type V that extends it) from the cache that corresponds to the a
     * {@link CRSCodeType}. An array of code types is given; the first identifiable that is found in (for a code, when
     * they are checked in order) is returned
     * 
     * @param <V>
     * @param expectedType
     *            the type of the sought object
     * @param ids
     *            an array of {@link CRSCodeType}s
     * @return the identifiable found in the cache corresponding to the (first) id
     */
    public <V extends CRSIdentifiable> V getCachedIdentifiable( Class<V> expectedType, CRSCodeType[] ids ) {
        if ( ids == null || ids.length == 0 ) {
            return null;
        }
        V result = null;
        for ( int i = 0; i < ids.length && result == null; i++ ) {
            result = getCachedIdentifiable( expectedType, ids[i] );
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "Searched for id: " + ids[i] + " resulted in: "
                           + ( ( result == null ) ? "null" : result.getCode() ) );
            }
        }
        return result;
    }

    /**
     * The id is as it is, not trimming 'upcasing' or other modifications will be done in this method.
     * 
     * @param expectedType
     *            The class of type T which is expected.
     * @param <V>
     *            the type to cast to if the casting fails, null will be returned.
     * 
     * @param id
     *            to search the cache for
     * @return the {@link CRSIdentifiable} or <code>null</code> if it was not found or the wrong type was found.
     */
    @SuppressWarnings("unchecked")
    public <V extends CRSIdentifiable> V getCachedIdentifiable( Class<V> expectedType, String id ) {
        if ( id == null ) {
            return null;
        }
        V result = null;
        try {
            result = (V) cachedIdentifiables.get( CRSCodeType.valueOf( id ) );
        } catch ( ClassCastException cce ) {
            LOG.error( "Given id is not of type: " + expectedType.getCanonicalName() + " found following error: "
                       + cce.getLocalizedMessage() );
        }
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Searched for id: " + id + " resulted in: " + ( ( result == null ) ? "null" : result.getCode() ) );
        }
        return result;
    }

    /**
     * Get a {@link CRSIdentifiable} (actually a type V that extends it) from the cache that corresponds to the a
     * {@link CRSCodeType}.
     * 
     * @param <V>
     * @param expectedType
     *            the type of the sought object
     * @param id
     *            a {@link CRSCodeType}
     * @return the identifiable found in the cache corresponding to the id
     */
    @SuppressWarnings("unchecked")
    public <V extends CRSIdentifiable> V getCachedIdentifiable( Class<V> expectedType, CRSCodeType id ) {
        if ( id == null ) {
            return null;
        }
        V result = null;
        try {
            result = (V) cachedIdentifiables.get( id );
        } catch ( ClassCastException cce ) {
            LOG.error( "Given id is not of type: " + expectedType.getCanonicalName() + " found following error: "
                       + cce.getLocalizedMessage() );
        }
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Searched for id: " + id + " resulted in: " + ( ( result == null ) ? "null" : result.getCode() ) );
        }
        return result;
    }

    /**
     * The id is as it is, not trimming 'upcasing' or other modifications will be done in this method.
     * 
     * @param <V>
     *            the type to cast to if the casting fails, null will be returned.
     * 
     * @param id
     *            to search the cache for
     * @return the {@link CRSIdentifiable} or <code>null</code> if it was not found or the wrong type was found.
     */
    @SuppressWarnings("unchecked")
    public <V extends CRSIdentifiable> V getCachedIdentifiable( String id ) {
        if ( id == null ) {
            return null;
        }
        V result = (V) cachedIdentifiables.get( CRSCodeType.valueOf( id ) );
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Searched for id: " + id + " resulted in: " + ( ( result == null ) ? "null" : result.getCode() ) );
        }
        return result;
    }

    /**
     * Get a {@link CRSIdentifiable} (actually a type V that extends it) from the cache that corresponds to the a
     * {@link CRSCodeType}.
     * 
     * @param <V>
     * @param id
     *            a {@link CRSCodeType}
     * @return a {@link CRSIdentifiable}-extending object that corresponds to the given id
     */
    @SuppressWarnings("unchecked")
    public <V extends CRSIdentifiable> V getCachedIdentifiable( CRSCodeType id ) {
        if ( id == null ) {
            return null;
        }
        V result = (V) cachedIdentifiables.get( id );
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Searched for id: " + id + " resulted in: " + ( ( result == null ) ? "null" : result.getCode() ) );
        }
        return result;
    }

    /**
     * Add the id to the cache, by mapping it to all its identifiers.
     * 
     * @param <V>
     *            type of CRSIdentifiable
     * @param identifiable
     *            to insert into cache
     * @param update
     *            if true an existing identifiable in the cache will be overwritten.
     * @return the identifiable
     */
    public synchronized <V extends CRSIdentifiable> V addIdToCache( V identifiable, boolean update ) {
        if ( identifiable == null ) {
            return null;
        }
        for ( CRSCodeType idString : identifiable.getCodes() ) {
            // if ( idString != null && !"".equals( idString.trim() ) ) {
            if ( idString != null ) {
                if ( cachedIdentifiables.containsKey( idString ) && cachedIdentifiables.get( idString ) != null ) {
                    if ( update ) {
                        LOG.debug( "Updating cache with new identifiable: " + idString );
                        cachedIdentifiables.put( idString, identifiable );
                    }
                } else {
                    LOG.debug( "Adding new identifiable to cache: " + idString );
                    cachedIdentifiables.put( idString, identifiable );
                }
            } else {
                LOG.debug( "Not adding the null string id to the cache of identifiable: " + identifiable.getCode() );
            }

        }
        return identifiable;
    }

    /**
     * The <code>SupportedTransformations</code> enumeration defines currently supported transformations
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * 
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     * 
     */
    public enum SupportedTransformations {
        /**
         * The {@link Helmert}, transformation with 7 values
         */
        HELMERT_7,
        /**
         * The {@link Helmert}, transformation with 3 values
         */
        HELMERT_3,
        /**
         * The {@link GeocentricTransform} going from geographic to geocentric.
         */
        GEOGRAPHIC_GEOCENTRIC,
        /**
         * The primemeridian rotation going from any to greenwich
         */
        LONGITUDE_ROTATION,
        /**
         * The {@link PolynomialTransformation} defining the general 2, 3, ... degree polynomial transformation
         */
        GENERAL_POLYNOMIAL,
        /**
         * The ntv2, currently not supported
         */
        NTV2,
        /**
         * A not supported projection
         */
        NOT_SUPPORTED
    }

    /**
     * The <code>SupportedTransformationParameters</code> enumeration defines currently supported transformation
     * parameters
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * 
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     * 
     */
    public enum SupportedTransformationParameters {
        /**
         * The X TRANSLATION of a (3/7) helmert transformation.
         */
        X_AXIS_TRANSLATION,
        /**
         * The Y TRANSLATION of a (3/7) helmert transformation.
         */
        Y_AXIS_TRANSLATION,
        /**
         * The Z TRANSLATION of a (3/7) helmert transformation.
         */
        Z_AXIS_TRANSLATION,
        /**
         * The X Rotation of a (3/7) helmert transformation.
         */
        X_AXIS_ROTATION,
        /**
         * The Y Rotation of a (3/7) helmert transformation.
         */
        Y_AXIS_ROTATION,
        /**
         * The Z Rotation of a (3/7) helmert transformation.
         */
        Z_AXIS_ROTATION,
        /**
         * The Difference of scale of a (3/7) helmert transformation.
         */
        SCALE_DIFFERENCE,
        /**
         * The longitude offset of a longitude rotation
         */
        LONGITUDE_OFFSET,
        /**
         * GENERIC transformation parameters are not yet supported.
         */
        GENERIC_POLYNOMIAL_PARAM,
        /**
         * A not supported projection parameter.
         */
        NOT_SUPPORTED
    }

    /**
     * The <code>SupportedProjections</code> enumeration defines currently supported projections
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * 
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     * 
     */
    public enum SupportedProjections {
        /**
         * The {@link TransverseMercator} projection
         */
        TRANSVERSE_MERCATOR,
        /**
         * The {@link LambertConformalConic} projection
         */
        LAMBERT_CONFORMAL,
        /**
         * The {@link LambertAzimuthalEqualArea} projection
         */
        LAMBERT_AZIMUTHAL_EQUAL_AREA,
        /**
         * Snyders {@link StereographicAzimuthal} implementation of the stereographic azimuthal projection
         */
        STEREOGRAPHIC_AZIMUTHAL,
        /**
         * EPSG {@link StereographicAlternative} implementation of the Stereographic azimuthal projection
         */
        STEREOGRAPHIC_AZIMUTHAL_ALTERNATIVE,
        /**
         * A not supported projection
         */
        NOT_SUPPORTED
    }

    /**
     * The <code>SupportedProjectionParameters</code> enumeration defines currently supported projection parameters
     * 
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     * 
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     * 
     */
    public enum SupportedProjectionParameters {
        /**
         * The latitude of natural origin of a given projection, aka. projectionLatitude, central-latitude or
         * latitude-of-origin, in Snyder referenced as phi_1 for azimuthal, phi_0 for other projections.
         */
        LATITUDE_OF_NATURAL_ORIGIN,
        /**
         * The longitude of natural origin of a given projection, aka. projectionLongitude, projection-meridian,
         * central-meridian, in Snyder referenced as lambda_0
         */
        LONGITUDE_OF_NATURAL_ORIGIN,
        /**
         * The false easting of the projection.
         */
        FALSE_EASTING,
        /**
         * The false northing of the projection.
         */
        FALSE_NORTHING,
        /**
         * The scale at the natural origin of the projection.
         */
        SCALE_AT_NATURAL_ORIGIN,
        /**
         * The latitude which the scale is 1 of a stereographic azimuthal projection.
         */
        TRUE_SCALE_LATITUDE,
        /**
         * The first parallel latitude of conic projections.
         */
        FIRST_PARALLEL_LATITUDE,
        /**
         * The second parallel latitude of conic projections.
         */
        SECOND_PARALLEL_LATITUDE,

        /**
         * A not supported projection parameter.
         */
        NOT_SUPPORTED
    }

    /**
     * 
     * @param codes
     *            to check for.
     * @return a mapped projection or {@link SupportedProjections#NOT_SUPPORTED}, never <code>null</code>
     */
    protected SupportedProjections mapProjections( CRSCodeType[] codes ) {
        if ( codes == null || codes.length == 0 ) {
            return SupportedProjections.NOT_SUPPORTED;
        }
        for ( CRSCodeType code : codes ) {
            if ( code != null ) {
                String compare = code.getOriginal();
                if ( "TransverseMercator".equalsIgnoreCase( compare )
                     || "Transverse Merctator".equalsIgnoreCase( compare )
                     || matchEPSGString( compare, "method", "9807" ) ) {
                    return SupportedProjections.TRANSVERSE_MERCATOR;
                } else if ( "lambertAzimuthalEqualArea".equalsIgnoreCase( compare )
                            || "Lambert Azimuthal Equal Area".equalsIgnoreCase( compare )
                            || "Lambert Azimuthal Equal Area (Spherical)".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "method", "9820" )
                            || matchEPSGString( compare, "method", "9821" ) ) {
                    return SupportedProjections.LAMBERT_AZIMUTHAL_EQUAL_AREA;
                } else if ( "stereographicAlternative".equalsIgnoreCase( compare )
                            || "Oblique Stereographic".equalsIgnoreCase( compare )
                            || compare.contains( "Polar Stereographic" ) || matchEPSGString( compare, "method", "9809" )
                            || matchEPSGString( compare, "method", "9810" )
                            || matchEPSGString( compare, "method", "9829" )
                            || matchEPSGString( compare, "method", "9830" ) ) {
                    return SupportedProjections.STEREOGRAPHIC_AZIMUTHAL_ALTERNATIVE;
                } else if ( "stereographicAzimuthal".equalsIgnoreCase( compare ) ) {
                    return SupportedProjections.STEREOGRAPHIC_AZIMUTHAL;
                } else if ( "lambertConformalConic".equalsIgnoreCase( compare )
                            || compare.contains( "Lambert Conic Conformal" )
                            || matchEPSGString( compare, "method", "9801" )
                            || matchEPSGString( compare, "method", "9802" )
                            || matchEPSGString( compare, "method", "9803" ) ) {
                    return SupportedProjections.LAMBERT_CONFORMAL;
                }
            }
        }
        return SupportedProjections.NOT_SUPPORTED;
    }

    /**
     * 
     * @param codes
     *            to check for.
     * @return a mapped projections parameters or {@link SupportedProjectionParameters#NOT_SUPPORTED}, never
     *         <code>null</code>
     */
    protected SupportedProjectionParameters mapProjectionParameters( CRSCodeType[] codes ) {
        if ( codes == null || codes.length == 0 ) {
            return SupportedProjectionParameters.NOT_SUPPORTED;
        }
        for ( CRSCodeType name : codes ) {
            if ( name != null ) {
                String compare = name.getOriginal();
                if ( "Latitude of natural origin".equalsIgnoreCase( compare )
                     || "Latitude of false origin".equalsIgnoreCase( compare )
                     || "central latitude".equalsIgnoreCase( compare )
                     || "latitude of origin".equalsIgnoreCase( compare )
                     || "latitudeOfNaturalOrigin".equalsIgnoreCase( compare )
                     || matchEPSGString( compare, "parameter", "8801" )
                     || matchEPSGString( compare, "parameter", "8811" )
                     || matchEPSGString( compare, "parameter", "8821" ) ) {
                    return SupportedProjectionParameters.LATITUDE_OF_NATURAL_ORIGIN;
                } else if ( "Longitude of natural origin".equalsIgnoreCase( compare )
                            || "Central Meridian".equalsIgnoreCase( compare ) || "CM".equalsIgnoreCase( compare )
                            || "Longitude of origin".equalsIgnoreCase( compare )
                            || "Longitude of false origin".equalsIgnoreCase( compare )
                            || "longitudeOfNaturalOrigin".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8802" )
                            || matchEPSGString( compare, "parameter", "8812" )
                            || matchEPSGString( compare, "parameter", "8822" ) ) {
                    return SupportedProjectionParameters.LONGITUDE_OF_NATURAL_ORIGIN;
                } else if ( "Scale factor at natural origin".equalsIgnoreCase( compare )
                            || "scaleFactor".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8805" ) ) {
                    return SupportedProjectionParameters.SCALE_AT_NATURAL_ORIGIN;
                } else if ( "Latitude of pseudo standard parallel ".equalsIgnoreCase( compare )
                            || "Latitude of standard parallel ".equalsIgnoreCase( compare )
                            || "trueScaleLatitude".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8832" )
                            || matchEPSGString( compare, "parameter", "8818" ) ) {
                    return SupportedProjectionParameters.TRUE_SCALE_LATITUDE;
                } else if ( "False easting".equalsIgnoreCase( compare ) || "falseEasting".equalsIgnoreCase( compare )
                            || "false westing".equalsIgnoreCase( compare )
                            || "Easting at false origin".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8806" )
                            || matchEPSGString( compare, "parameter", "8816" )
                            || matchEPSGString( compare, "parameter", "8826" ) ) {
                    return SupportedProjectionParameters.FALSE_EASTING;
                } else if ( "False northing".equalsIgnoreCase( compare ) || "falseNorthing".equalsIgnoreCase( compare )
                            || "false southing".equalsIgnoreCase( compare )
                            || "Northing at false origin".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8807" )
                            || matchEPSGString( compare, "parameter", "8827" )
                            || matchEPSGString( compare, "parameter", "8817" ) ) {
                    return SupportedProjectionParameters.FALSE_NORTHING;
                } else if ( "Latitude of 1st standard parallel".equalsIgnoreCase( compare )
                            || "firstParallelLatitude".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8823" ) ) {
                    return SupportedProjectionParameters.FIRST_PARALLEL_LATITUDE;
                } else if ( "Latitude of 2nd standard parallel".equalsIgnoreCase( compare )
                            || "secondParallelLatitude".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8824" ) ) {
                    return SupportedProjectionParameters.SECOND_PARALLEL_LATITUDE;
                }

            }
        }
        return SupportedProjectionParameters.NOT_SUPPORTED;
    }

    /**
     * 
     * @param codes
     *            to check for.
     * @return a mapped transformation or {@link SupportedTransformations#NOT_SUPPORTED}, never <code>null</code>
     */
    protected SupportedTransformations mapTransformation( CRSCodeType[] codes ) {
        if ( codes == null || codes.length == 0 ) {
            return SupportedTransformations.NOT_SUPPORTED;
        }
        for ( CRSCodeType code : codes ) {
            if ( code != null ) {
                String compare = code.getOriginal();
                if ( "Longitude rotation".equalsIgnoreCase( compare ) || matchEPSGString( compare, "method", "9601" ) ) {
                    return SupportedTransformations.LONGITUDE_ROTATION;
                } else if ( "Geographic/geocentric conversions".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "method", "9602" ) ) {
                    return SupportedTransformations.GEOGRAPHIC_GEOCENTRIC;
                } else if ( "Geocentric translations".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "method", "9603" ) ) {
                    return SupportedTransformations.HELMERT_3;
                } else if ( "Position Vector 7-param. transformation".equalsIgnoreCase( compare )
                            || "Coordinate Frame rotation".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "method", "9606" )
                            || matchEPSGString( compare, "method", "9607" ) ) {
                    return SupportedTransformations.HELMERT_7;
                } else if ( "NTv2".equalsIgnoreCase( compare ) || matchEPSGString( compare, "method", "9615" ) ) {
                    return SupportedTransformations.NOT_SUPPORTED;
                } else if ( matchEPSGString( compare, "method", "9645" ) || matchEPSGString( compare, "method", "9646" )
                            || matchEPSGString( compare, "method", "9647" )
                            || matchEPSGString( compare, "method", "9648" ) ) {
                    return SupportedTransformations.GENERAL_POLYNOMIAL;
                }
            }
        }
        return SupportedTransformations.NOT_SUPPORTED;
    }

    /**
     * 
     * @param codes
     *            to check for.
     * @return a mapped transformation or {@link SupportedTransformations#NOT_SUPPORTED}, never <code>null</code>
     */
    protected SupportedTransformationParameters mapTransformationParameters( CRSCodeType[] codes ) {
        if ( codes == null || codes.length == 0 ) {
            return SupportedTransformationParameters.NOT_SUPPORTED;
        }
        for ( CRSCodeType code : codes ) {
            if ( code != null ) {
                String compare = code.getOriginal();
                if ( "Longitude offset".equalsIgnoreCase( compare ) || matchEPSGString( compare, "parameter", "8602" ) ) {
                    return SupportedTransformationParameters.LONGITUDE_OFFSET;
                } else if ( "X-axis translation".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8605" ) ) {
                    return SupportedTransformationParameters.X_AXIS_TRANSLATION;
                } else if ( "Y-axis translation".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8606" ) ) {
                    return SupportedTransformationParameters.Y_AXIS_TRANSLATION;
                } else if ( "Z-axis translation".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8607" ) ) {
                    return SupportedTransformationParameters.Z_AXIS_TRANSLATION;
                } else if ( "X-axis rotation".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8608" ) ) {
                    return SupportedTransformationParameters.X_AXIS_ROTATION;
                } else if ( "Y-axis rotation".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8609" ) ) {
                    return SupportedTransformationParameters.Y_AXIS_ROTATION;
                } else if ( "Z-axis rotation".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8610" ) ) {
                    return SupportedTransformationParameters.Z_AXIS_ROTATION;
                } else if ( "Scale difference".equalsIgnoreCase( compare )
                            || matchEPSGString( compare, "parameter", "8611" ) ) {
                    return SupportedTransformationParameters.SCALE_DIFFERENCE;
                }
            }
        }
        return SupportedTransformationParameters.NOT_SUPPORTED;
    }
}

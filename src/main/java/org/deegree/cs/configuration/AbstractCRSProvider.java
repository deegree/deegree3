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

package org.deegree.cs.configuration;

import static org.deegree.cs.coordinatesystems.CoordinateSystem.CRSType.COMPOUND;
import static org.deegree.cs.coordinatesystems.CoordinateSystem.CRSType.PROJECTED;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.components.Axis;
import org.deegree.cs.configuration.resources.CRSResource;
import org.deegree.cs.coordinatesystems.CompoundCRS;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.coordinatesystems.ProjectedCRS;
import org.deegree.cs.exceptions.CRSConfigurationException;
import org.deegree.cs.i18n.Messages;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.transformations.coordinate.NotSupportedTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base class for a {@link CRSProvider} which has a caching mechanism for {@link CRSIdentifiable}s and instantiates
 * a given resolver used for inverse lookup.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @version $Revision$, $Date$
 * @param <T>
 *            the type of object the parse method awaits.
 * 
 */
@LoggingNotes(debug = "Get information about the initialization of the provider, as well as on requested objects.")
public abstract class AbstractCRSProvider<T> implements CRSProvider {

    private static Logger LOG = LoggerFactory.getLogger( AbstractCRSProvider.class );

    private static Map<CRSCodeType, CRSIdentifiable> cachedIdentifiables = new HashMap<CRSCodeType, CRSIdentifiable>();

    private static Map<CRSCodeType, CRSIdentifiable> cachedCRSXY = new HashMap<CRSCodeType, CRSIdentifiable>();

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
                    tc = Class.forName( subType.getName() );
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
        return getCRSByCode( id, false );
    }

    @Override
    public CoordinateSystem getCRSByCode( CRSCodeType id, boolean forceXY )
                            throws CRSConfigurationException {
        if ( resolver == null ) {
            throw new CRSConfigurationException( "No resolver initialized, this may not be." );
        }
        CoordinateSystem result = null;
        if ( id != null ) {
            if ( forceXY ) {
                result = getCRSFromCache( cachedCRSXY, id, result );
            }
            if ( result == null ) {
                result = getCRSFromCache( cachedIdentifiables, id, result );
                if ( result == null ) {
                    LOG.debug( "No crs with id: " + id + " found in cache." );
                    try {
                        result = parseCoordinateSystem( resolver.getURIAsType( id.getOriginal() ) );
                    } catch ( IOException e ) {
                        LOG.debug( e.getLocalizedMessage(), e );
                        throw new CRSConfigurationException( e );
                    }
                }
                if ( forceXY && result != null ) {
                    result = createXYCoordinateSystem( result );
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
            if ( forceXY ) {
                addIdToCache( cachedCRSXY, result, false );
                if ( result.getType() == COMPOUND ) {
                    addIdToCache( cachedCRSXY, ( (CompoundCRS) result ).getUnderlyingCRS(), false );
                    if ( ( (CompoundCRS) result ).getUnderlyingCRS().getType() == PROJECTED ) {
                        addIdToCache(
                                      cachedCRSXY,
                                      ( (ProjectedCRS) ( (CompoundCRS) result ).getUnderlyingCRS() ).getGeographicCRS(),
                                      false );
                    }
                } else if ( result.getType() == PROJECTED ) {
                    addIdToCache( ( (ProjectedCRS) result ).getGeographicCRS(), false );
                }
            } else {
                addIdToCache( result, false );
                if ( result.getType() == COMPOUND ) {
                    addIdToCache( ( (CompoundCRS) result ).getUnderlyingCRS(), false );
                    if ( ( (CompoundCRS) result ).getUnderlyingCRS().getType() == PROJECTED ) {
                        addIdToCache(
                                      ( (ProjectedCRS) ( (CompoundCRS) result ).getUnderlyingCRS() ).getGeographicCRS(),
                                      false );
                    }
                } else if ( result.getType() == PROJECTED ) {
                    addIdToCache( ( (ProjectedCRS) result ).getGeographicCRS(), false );
                }
            }
        }
        return result;
    }

    private CoordinateSystem createXYCoordinateSystem( CoordinateSystem result ) {
        switch ( result.getType() ) {
        case GEOGRAPHIC:
            return new GeographicCRS( ( (GeographicCRS) result ).getGeodeticDatum(),
                                      forceXYAxisOrder( result.getAxis() ), new CRSIdentifiable( result ) );
        case COMPOUND:
            CompoundCRS comp = (CompoundCRS) result;
            return new CompoundCRS( comp.getHeightAxis(), createXYCoordinateSystem( comp.getUnderlyingCRS() ),
                                    comp.getDefaultHeight(), new CRSIdentifiable( comp ) );
        }
        return result;
    }

    private Axis[] forceXYAxisOrder( Axis[] axis ) {
        if ( axis != null && axis.length == 2
             && ( axis[0].getOrientation() == Axis.AO_NORTH || axis[0].getOrientation() == Axis.AO_SOUTH ) ) {
            Axis[] xyAxis = new Axis[2];
            xyAxis[0] = axis[1];
            xyAxis[1] = axis[0];
            return xyAxis;
        }
        return axis;
    }

    private CoordinateSystem getCRSFromCache( Map<CRSCodeType, CRSIdentifiable> cache, CRSCodeType id,
                                              CoordinateSystem result ) {
        LOG.debug( "Trying to load crs with id: " + id + " from cache." );
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( cachedIdentifiables.keySet().toString() );
        }
        if ( cache.containsKey( id ) ) {
            CRSIdentifiable r = cache.get( id );
            LOG.debug( "Found CRSIdentifiable: " + r.getCodeAndName() + " from given id: " + id );
            if ( !( r instanceof CoordinateSystem ) ) {
                LOG.error( "Found CRSIdentifiable: " + r.getCodeAndName()
                           + " but it is not a coordinate system, your db is inconsistent return null." );
                r = null;
            }
            result = (CoordinateSystem) r;
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
     *         if the given transformationDefintion is <code>null</code>. If the parsed transformation is not supported
     *         or a {@link NotSupportedTransformation} will be returned.
     * @throws CRSConfigurationException
     *             if an error was found in the given crsDefintion
     */
    public abstract Transformation parseTransformation( T transformationDefinition )
                            throws CRSConfigurationException;

    /**
     * Clears the cache.
     */
    public void clearCache() {
        try {
            synchronized ( cachedIdentifiables ) {
                cachedIdentifiables.clear();
                cachedIdentifiables.notifyAll();
            }
        } catch ( Exception e ) {
            LOG.warn( "The clearing of the cache could not be forefullfilled because: " + e.getLocalizedMessage() );
        }
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
            result = (V) cachedIdentifiables.get( CRSCodeType.valueOf( id.toLowerCase() ) );
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
        return addIdToCache( cachedIdentifiables, identifiable, update );
    }

    private synchronized <V extends CRSIdentifiable> V addIdToCache( Map<CRSCodeType, CRSIdentifiable> cache,
                                                                     V identifiable, boolean update ) {
        if ( identifiable == null ) {
            return null;
        }
        for ( CRSCodeType idString : identifiable.getCodes() ) {
            // if ( idString != null && !"".equals( idString.trim() ) ) {
            if ( idString != null ) {
                if ( cache.containsKey( idString ) && cache.get( idString ) != null ) {
                    if ( update ) {
                        LOG.debug( "Updating cache with new identifiable: " + idString );
                        cache.put( idString, identifiable );
                    }
                } else {
                    LOG.debug( "Adding new identifiable to cache: " + idString );
                    cache.put( idString, identifiable );
                }
            } else {
                LOG.debug( "Not adding the null string id to the cache of identifiable: " + identifiable.getCode() );
            }
        }
        return identifiable;
    }
}

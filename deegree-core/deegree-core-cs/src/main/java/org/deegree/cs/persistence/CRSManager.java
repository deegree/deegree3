//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.cs.persistence;

import static java.lang.System.currentTimeMillis;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.config.WorkspaceInitializationException;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.exceptions.CRSConfigurationException;
import org.deegree.cs.exceptions.CRSStoreException;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.i18n.Messages;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.transformations.TransformationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for creating and retrieving {@link CRSStore} and {@link CRSStoreProvider} instances.
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class CRSManager implements ResourceManager {

    private static boolean loadDefault = true;

    private static Logger LOG = LoggerFactory.getLogger( CRSManager.class );

    private static ServiceLoader<CRSStoreProvider> crssProviderLoader = ServiceLoader.load( CRSStoreProvider.class );

    private static Map<String, CRSStoreProvider> nsToProvider = null;

    private static Map<String, CRSStore> idToCRSStore = Collections.synchronizedMap( new HashMap<String, CRSStore>() );

    private static Map<String, TransformationFactory> idToTransF = new HashMap<String, TransformationFactory>();

    static {
        initDefault();
    }

    private static void initDefault() {
        if ( loadDefault ) {
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "No 'crs' directory -- use default configuration." );
            LOG.info( "--------------------------------------------------------------------------------" );

            URL defaultConfig = CRSManager.class.getResource( "default.xml" );
            try {
                handleConfigFile( defaultConfig );
            } catch ( Throwable t ) {
                LOG.error( "The default configuration could not be loaded: " + t.getMessage() );
            }
        }
    }

    public static Collection<String> getCrsStoreIds() {
        return idToCRSStore.keySet();
    }

    public void startup( DeegreeWorkspace workspace )
                            throws WorkspaceInitializationException {
        init( new File( workspace.getLocation(), "crs" ) );
    }

    public void shutdown() {
        destroy();
    }

    /**
     * cleares the stored configuration
     */
    public static void destroy() {
        LOG.info( "Clear CRS store and transformation map" );
        idToCRSStore.clear();
        idToTransF.clear();
        initDefault();
    }

    // TODO: dependencies
    public Class<? extends ResourceManager>[] getDependencies() {
        return null;
    }

    /**
     * Initializes the {@link CRSManager} by loading all crs store configurations from the given directory. If null, or
     * directory does not exist the default will be used.
     * 
     * @param crsDir
     */
    public static void init( File crsDir ) {
        if ( crsDir != null && crsDir.exists() ) {
            loadDefault = false;
            destroy();
            LOG.info( "--------------------------------------------------------------------------------" );
            LOG.info( "Setting up crs stores." );
            LOG.info( "--------------------------------------------------------------------------------" );

            File[] crsConfigFiles = crsDir.listFiles( new FilenameFilter() {
                @Override
                public boolean accept( File dir, String name ) {
                    return name.toLowerCase().endsWith( ".xml" );
                }
            } );
            for ( File crsConfigFile : crsConfigFiles ) {
                try {
                    handleConfigFile( crsConfigFile.toURI().toURL() );
                } catch ( Throwable t ) {
                    LOG.error( "Unable to read config file '" + crsConfigFile + "'.", t );
                }
            }
            LOG.info( "" );
            loadDefault = true;
        } else {
            LOG.info( "Could not set up CRS stores: CRS workspace directory " + crsDir + "is null or does not exists" );
            destroy();
        }
    }

    private static void handleConfigFile( URL crsConfigFile ) {
        String fileName = crsConfigFile.getFile();
        int fileNameStart = fileName.lastIndexOf( '/' ) + 1;
        // 4 is the length of ".xml"
        String crsId = fileName.substring( fileNameStart, fileName.length() - 4 );
        LOG.info( "Setting up crs store '" + crsId + "' from file '" + fileName + "'..." + "" );
        try {
            CRSStore crss = create( crsConfigFile.toURI().toURL() );
            registerAndInit( crss, crsId );
        } catch ( Exception e ) {
            LOG.error( "Error creating crs store: " + e.getMessage(), e );
        }
    }

    /**
     * Returns an uninitialized {@link CRSStore} instance that's created from the specified CRSStore configuration
     * document.
     * 
     * @param configURL
     *            URL of the configuration document, must not be <code>null</code>
     * @return corresponding {@link CRSStore} instance, not yet initialized, never <code>null</code>
     * @throws CRSStoreException
     *             if the creation fails, e.g. due to a configuration error
     */
    public static synchronized CRSStore create( URL configURL )
                            throws CRSStoreException {
        String namespace = null;
        try {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( configURL.openStream() );
            StAXParsingHelper.nextElement( xmlReader );
            namespace = xmlReader.getNamespaceURI();
        } catch ( Exception e ) {
            String msg = Messages.get( "CRSManager.CREATING_STORE_FAILED", configURL );
            LOG.error( msg );
            throw new CRSStoreException( msg );
        }
        LOG.debug( "Config namespace: '" + namespace + "'" );
        CRSStoreProvider provider = getProviders().get( namespace );
        if ( provider == null ) {
            String msg = Messages.get( "CRSManager.MISSING_PROVIDER", namespace, configURL );
            LOG.error( msg );
            throw new CRSStoreException( msg );
        }
        return provider.getCRSStore( configURL );
    }

    /**
     * Returns all available {@link CRSStore} providers.
     * 
     * @return all available providers, keys: config namespace, value: provider instance
     */
    public static synchronized Map<String, CRSStoreProvider> getProviders() {
        if ( nsToProvider == null ) {
            nsToProvider = new HashMap<String, CRSStoreProvider>();
            try {
                for ( CRSStoreProvider provider : crssProviderLoader ) {
                    LOG.debug( "CRS store provider: " + provider + ", namespace: " + provider.getConfigNamespace() );
                    if ( nsToProvider.containsKey( provider.getConfigNamespace() ) ) {
                        LOG.error( "Multiple crs store providers for config namespace: '"
                                   + provider.getConfigNamespace() + "' on classpath -- omitting provider '"
                                   + provider.getClass().getName() + "'." );
                        continue;
                    }
                    nsToProvider.put( provider.getConfigNamespace(), provider );
                }
            } catch ( Exception e ) {
                LOG.error( e.getMessage(), e );
            }

        }
        return nsToProvider;
    }

    private static void registerAndInit( CRSStore crss, String id )
                            throws CRSStoreException {
        if ( id != null ) {
            if ( idToCRSStore.containsKey( id ) ) {
                throw new CRSStoreException( Messages.getMessage( "CRSManager.DUPLICATE_ID", id ) );
            }
            LOG.info( "Registering global crs store with id '" + id + "', type: '" + crss.getClass().getName() + "'" );
            idToTransF.put( id, new TransformationFactory( crss ) );
            idToCRSStore.put( id, crss );
            crss.init();
        }
    }

    /**
     * Returns all active {@link CRSStore}s.
     * 
     * @return the {@link CRSStore}s instance, may be empty but never <code>null</code>
     */
    public static Collection<CRSStore> getAll() {
        return idToCRSStore.values();
    }

    /**
     * Returns the {@link CRSStore} instance with the specified identifier or <code>null</code> if an assigned
     * {@link CRSStore} is missing
     * 
     * @param id
     *            identifier of the {@link CRSStore} instance
     * @return the corresponding {@link CRSStore} instance or the default {@link CRSStore} instance if no such instance
     *         has been created or <code>null</code> if the default one could also not be created.
     */
    public static CRSStore get( String id ) {
        return idToCRSStore.get( id );
    }

    /*********************************************/

    /**
     * @param storeId
     *            identifier of the {@link CRSStore} instance, may be <code>null</code>, then the first transformation
     *            factory will be returned
     * @return the {@link TransformationFactory} instance assigned to the store with the given id or the first one in
     *         the list of {@link TransformationFactory}s if no such instance has been created or <code>null</code> if
     *         no {@link TransformationFactory} instance could be found.
     */
    public static final TransformationFactory getTransformationFactory( String storeId ) {
        if ( storeId == null ) {
            for ( TransformationFactory tf : idToTransF.values() ) {
                return tf;
            }
        }
        return idToTransF.get( storeId );
    }

    /**
     * Creates a {@link CoordinateSystem} from the given name, if no {@link CoordinateSystem} was found an
     * {@link UnknownCRSException} will be thrown. All configured {@link CRSStore}s will be considered and the first
     * match returned.
     * 
     * @param name
     *            of the crs, e.g. EPSG:4326
     * @return a {@link CoordinateSystem} corresponding to the given name, using all configured {@link CRSStore}s.
     * @throws UnknownCRSException
     *             if name is not known
     */
    public synchronized static CoordinateSystem lookup( String name )
                            throws UnknownCRSException {
        return lookup( name, false );
    }

    /**
     * Creates a {@link CoordinateSystem} from the given name, if no {@link CoordinateSystem} was found an
     * {@link UnknownCRSException} will be thrown. All configured {@link CRSStore}s will be considered and the first
     * match returned.
     * 
     * @param name
     *            of the crs, e.g. EPSG:4326
     * @param forceXY
     *            true if the axis order of the coordinate system should be x/y (EAST/NORTH; WEST/SOUTH); false id the
     *            defined axis order should be used
     * @return a {@link CoordinateSystem} corresponding to the given name, using all configured {@link CRSStore}s.
     * @throws UnknownCRSException
     *             if name is not known
     */
    public synchronized static CoordinateSystem lookup( String name, boolean forceXY )
                            throws UnknownCRSException {
        return lookup( null, name, forceXY );
    }

    /**
     * Get a real coordinate system from the default {@link CRSStore}.
     * 
     * @param codeType
     * @return a real {@link CoordinateSystem} looked up in the default {@link CRSStore}.
     * @throws UnknownCRSException
     */
    public synchronized static CoordinateSystem lookup( CRSCodeType codeType )
                            throws UnknownCRSException {
        return lookup( null, codeType );
    }

    /**
     * Creates a {@link CoordinateSystem} from the given name using the given storeID, if no {@link CoordinateSystem}
     * was found an {@link UnknownCRSException} will be thrown.
     * 
     * @param storeId
     *            identifier of the {@link CRSStore} looking for the {@link CoordinateSystem} with the given name, may
     *            be <code>null</code> if in all {@link CRSStore}s should be searched
     * @param name
     *            of the crs, e.g. EPSG:31466
     * @return a {@link CoordinateSystem} corresponding to the given name from the {@link CRSStore} with the given id
     * @throws UnknownCRSException
     *             if name is not known
     */
    public synchronized static CoordinateSystem lookup( String storeId, String name )
                            throws UnknownCRSException {
        return lookup( storeId, name, false );
    }

    /**
     * Creates a {@link CoordinateSystem} from the given name using the given storeId, if no {@link CoordinateSystem}
     * was found an {@link UnknownCRSException} will be thrown.
     * 
     * @param storeId
     *            identifier of the store, looking for the {@link CoordinateSystem} instance, may be <code>null</code>
     *            if in all {@link CRSStore}s should be searched
     * @param name
     *            of the crs, e.g. EPSG:31466
     * @param forceXY
     *            true if the axis order of the coordinate system should be x/y (EAST/NORTH; WEST/SOUTH); false id the
     *            defined axis order should be used
     * @throws UnknownCRSException
     *             if name is not known
     */
    public static CoordinateSystem lookup( String storeIdName, String name, boolean forceXY )
                            throws UnknownCRSException {
        CRSStore crsStore = get( storeIdName );
        if ( crsStore != null ) {
            return lookupStore( crsStore, name, forceXY );
        } else {
            for ( CRSStore store : idToCRSStore.values() ) {
                try {
                    CoordinateSystem crs = lookupStore( store, name, forceXY );
                    if ( crs != null )
                        return crs;
                } catch ( UnknownCRSException e ) {
                    // nothing to do
                }
            }
        }
        throw new UnknownCRSException( name );
    }

    /**
     * Creates a {@link CoordinateSystem} from the given name using the given storeId, if no {@link CoordinateSystem}
     * was found an {@link UnknownCRSException} will be thrown.
     * 
     * @param storeId
     *            identifier of the store, looking for the {@link CoordinateSystem} instance, may be <code>null</code>
     *            if in all {@link CRSStore}s should be searched
     * @param crsCodeType
     *            of the crs
     * @return a real {@link CoordinateSystem} not just a wrapper.
     * @throws UnknownCRSException
     */
    public synchronized static CoordinateSystem lookup( String storeId, CRSCodeType crsCodeType )
                            throws UnknownCRSException {
        CRSStore crsStore = get( storeId );
        if ( crsStore != null ) {
            return lookupStore( crsStore, crsCodeType, false );
        } else {
            for ( CRSStore store : idToCRSStore.values() ) {
                try {
                    CoordinateSystem crs = lookupStore( store, crsCodeType, false );
                    if ( crs != null )
                        return crs;
                } catch ( UnknownCRSException e ) {
                    // nothing to do
                }
            }
        }
        throw new UnknownCRSException( crsCodeType.getOriginal() );
    }

    /**
     * Creates a {@link CoordinateSystem} from the given name using the given {@link CRSStore}, if no
     * {@link CoordinateSystem} was found an {@link UnknownCRSException} will be thrown.
     * 
     * @param crsStore
     *            {@link CRSStore} instance, looking for the {@link CoordinateSystem} instance, may not be
     *            <code>null</code>, if in all {@link CRSStore}s should be searched
     * @param name
     *            of the crs, e.g. EPSG:31466
     * @param forceXY
     *            true if the axis order of the coordinate system should be x/y (EAST/NORTH; WEST/SOUTH); false id the
     *            defined axis order should be used
     * @throws UnknownCRSException
     *             if name is not known
     * @throws IllegalArgumentException
     *             if crsStore is null
     */
    private static CoordinateSystem lookupStore( CRSStore crsStore, String name, boolean forceXY )
                            throws UnknownCRSException {
        if ( crsStore == null ) {
            throw new IllegalArgumentException( Messages.get( "CRSManager.STORE_NULL" ) );
        }
        long sT = currentTimeMillis();
        long eT = currentTimeMillis() - sT;
        LOG.debug( "Getting provider: " + crsStore + " took: " + eT + " ms." );
        CoordinateSystem realCRS = null;
        try {
            sT = currentTimeMillis();
            realCRS = crsStore.getCRSByCode( CRSCodeType.valueOf( name ), forceXY );
            if ( realCRS == null ) {
                // TODO: try to get CRS with lower case id (bug with id handling in the abstractStore cache)
                realCRS = crsStore.getCRSByCode( CRSCodeType.valueOf( name.toLowerCase() ), forceXY );
            }
            eT = currentTimeMillis() - sT;
            LOG.debug( "Getting crs ( " + name + " )from provider: " + crsStore + " took: " + eT + " ms." );
        } catch ( CRSConfigurationException e ) {
            throw new RuntimeException( Messages.get( "CRSManager.BROKEN_CRS_CONFIG", name, e.getMessage() ), e );
        }
        if ( realCRS == null ) {
            throw new UnknownCRSException( name );
        }
        LOG.debug( "Successfully created the crs with id: " + name );
        return realCRS;
    }

    /**
     * Creates a {@link CoordinateSystem} from the given name using the given {@link CRSStore}, if no
     * {@link CoordinateSystem} was found an {@link UnknownCRSException} will be thrown.
     * 
     * @param crsStore
     *            {@link CRSStore} instance, looking for the {@link CoordinateSystem} instance, may not be
     *            <code>null</code>, if in all {@link CRSStore}s should be searched
     * @param name
     *            of the crs, e.g. EPSG:31466
     * @param forceXY
     *            true if the axis order of the coordinate system should be x/y (EAST/NORTH; WEST/SOUTH); false id the
     *            defined axis order should be used
     * @throws UnknownCRSException
     *             if name is not known
     * @throws IllegalArgumentException
     *             if crsStore is null
     */
    private static CoordinateSystem lookupStore( CRSStore crsStore, CRSCodeType crsCodeType, boolean forceXY )
                            throws UnknownCRSException {
        if ( crsStore == null ) {
            throw new IllegalArgumentException( Messages.get( "CRSManager.STORE_NULL" ) );
        }
        CoordinateSystem realCRS = null;
        try {
            realCRS = crsStore.getCRSByCode( crsCodeType );
        } catch ( CRSConfigurationException e ) {
            LOG.error( e.getMessage(), e );
        }
        if ( realCRS == null ) {
            throw new UnknownCRSException( crsCodeType.getOriginal() );
        }
        LOG.debug( "Successfully created the crs with id: " + crsCodeType );
        return realCRS;
    }

    // TODO: ?
    // /**
    // * Wrapper for the private constructor of the org.deegree.cs class.
    // *
    // * @param realCRS
    // * to wrap
    // *
    // * @return a CRSDeliverable corresponding to the given crs.
    // */
    // public static CoordinateSystem lookup( CoordinateSystem realCRS ) {
    // return realCRS;
    // }

    /**
     * Get a {@link Transformation} with given id, or <code>null</code> if it does not exist.
     * 
     * @param crsStore
     *            {@link CRSStore} instance, looking for the {@link Transformation}, may not be <code>null</code>
     * @param id
     *            of the {@link Transformation}
     * @return the identified {@link Transformation} or <code>null<code> if no transformation is found.
     * @throws IllegalArgumentException
     *             if crsStore is null
     */
    private synchronized static Transformation getTransformation( CRSStore crsStore, String id ) {
        if ( crsStore == null ) {
            throw new IllegalArgumentException( Messages.get( "CRSManager.STORE_NULL" ) );
        }
        CRSIdentifiable t = null;
        try {
            t = crsStore.getIdentifiable( CRSCodeType.valueOf( id ) );
        } catch ( Throwable e ) {
            LOG.debug( "Could not retrieve a transformation for id: " + id );
        }
        if ( t != null && t instanceof Transformation ) {
            return (Transformation) t;
        }
        LOG.debug( "The given id: " + id + " is not of type transformation return null." );
        return null;
    }

    /**
     * Get a {@link Transformation} with given id, or <code>null</code> if it does not exist.
     * 
     * @param storeId
     *            identifier of the store, looking for the {@link Transformation}, may be <code>null</code> if in all
     *            {@link CRSStore}s should be searched
     * @param id
     *            of the {@link Transformation}
     * @return the identified {@link Transformation} or <code>null<code> if no such transformation is found.
     */
    public synchronized static Transformation getTransformation( String storeId, String id ) {
        CRSStore crsStore = idToCRSStore.get( storeId );
        if ( crsStore == null ) {
            for ( CRSStore store : idToCRSStore.values() ) {
                Transformation transformation = getTransformation( store, id );
                if ( transformation != null )
                    return transformation;
            }
        }
        return getTransformation( crsStore, id );
    }

    /**
     * Retrieve a {@link Transformation} (chain) which transforms coordinates from the given source into the given
     * target crs. If no such {@link Transformation} could be found or the implementation does not support inverse
     * lookup of transformations <code>null<code> will be returned.
     * 
     * @param storeId
     *            identifier of the store, looking for the {@link Transformation}, may be <code>null</code> if in all
     *            {@link CRSStore}s should be searched
     * @param sourceCRS
     *            start {@link CoordinateSystem} of the transformation (chain)
     * @param targetCRS
     *            end {@link CoordinateSystem} of the transformation (chain).
     * @return the given {@link Transformation} or <code>null<code> if no such transformation was found.
     * @throws TransformationException
     * @throws IllegalArgumentException
     */
    public synchronized static Transformation getTransformation( String storeId, CoordinateSystem sourceCRS,
                                                                 CoordinateSystem targetCRS )
                            throws IllegalArgumentException, TransformationException {
        return getTransformation( storeId, sourceCRS, targetCRS, null );
    }

    /**
     * Retrieve a {@link Transformation} (chain) which transforms coordinates from the given source into the given
     * target crs. If no such {@link Transformation} could be found or the implementation does not support inverse
     * lookup of transformations <code>null<code> will be returned.
     * 
     * @param storeId
     *            identifier of the store, looking for the {@link Transformation}, may be <code>null</code> if in all
     *            {@link CRSStore}s should be searched
     * @param sourceCRS
     *            start {@link CoordinateSystem} of the transformation (chain)
     * @param targetCRS
     *            end {@link CoordinateSystem} of the transformation (chain).
     * @param transformationsToBeUsed
     *            a list of transformations which must be used on the resulting transformation chain, may be
     *            <code>null</code> or empty
     * @return the given {@link Transformation} or <code>null<code> if no such transformation was found.
     * @throws TransformationException
     * @throws IllegalArgumentException
     */
    public synchronized static Transformation getTransformation( String storeId, CoordinateSystem sourceCRS,
                                                                 CoordinateSystem targetCRS,
                                                                 List<Transformation> transformationsToBeUsed )
                            throws IllegalArgumentException, TransformationException {
        if ( storeId != null ) {
            TransformationFactory fac = getTransformationFactory( storeId );
            return fac.createFromCoordinateSystems( sourceCRS, targetCRS, transformationsToBeUsed );
        } else {
            for ( TransformationFactory tf : idToTransF.values() ) {
                Transformation trans = tf.createFromCoordinateSystems( sourceCRS, targetCRS, transformationsToBeUsed );
                if ( trans != null ) {
                    return trans;
                }
            }
        }
        return null;
    }

    // TODO: required?
    // /**
    // * Wrapper for the private constructor to create a dummy projected crs with no projection parameters set, the
    // * standard wgs84 datum and the given optional name as the identifier. X-Y axis are in metres.
    // *
    // * @param name
    // * optional identifier, if missing, the word 'dummy' will be used.
    // *
    // * @return a dummy CoordinateSystem having filled out all the essential values.
    // */
    // public static CoordinateSystem lookupDummyCRS( String name ) {
    // if ( name == null || "".equals( name.trim() ) ) {
    // name = "dummy";
    // }
    // /**
    // * Standard axis of a geographic crs
    // */
    // final Axis[] axis_degree = new Axis[] { new Axis( Unit.DEGREE, "lon", Axis.AO_EAST ),
    // new Axis( Unit.DEGREE, "lat", Axis.AO_NORTH ) };
    // final Axis[] axis_projection = new Axis[] { new Axis( "x", Axis.AO_EAST ), new Axis( "y", Axis.AO_NORTH ) };
    //
    // final Helmert wgs_info = new Helmert( GeographicCRS.WGS84, GeographicCRS.WGS84, CRSCodeType.valueOf( name
    // + "_wgs" ) );
    // final GeodeticDatum datum = new GeodeticDatum( Ellipsoid.WGS84, wgs_info,
    // new CRSCodeType[] { CRSCodeType.valueOf( name + "_datum" ) } );
    // final GeographicCRS geographicCRS = new GeographicCRS(
    // datum,
    // axis_degree,
    // new CRSCodeType[] { CRSCodeType.valueOf( name
    // + "geographic_crs" ) } );
    // final TransverseMercator projection = new TransverseMercator( true, geographicCRS, 0, 0, new Point2d( 0, 0 ),
    // Unit.METRE, 1 );
    //
    // return new ProjectedCRS( projection, axis_projection,
    // new CRSCodeType[] { CRSCodeType.valueOf( name + "projected_crs" ) } );
    //
    // }

}

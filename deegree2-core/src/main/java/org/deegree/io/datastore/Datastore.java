//$Header: /deegreerepository/deegree/src/org/deegree/io/datastore/Datastore.java,v 1.28 2007/01/16 13:58:34 mschneider Exp $
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
package org.deegree.io.datastore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.trigger.TriggerProvider;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGMLSchema;
import org.deegree.io.datastore.schema.content.MappingGeometryField;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcwebservices.wfs.WFService;
import org.deegree.ogcwebservices.wfs.operation.Lock;
import org.deegree.ogcwebservices.wfs.operation.LockFeature;
import org.deegree.ogcwebservices.wfs.operation.Query;

/**
 * A datastore implementation must extend this class.
 * <p>
 * Describes the access to a datastore that encapsulates the access to a database or file. The
 * accessible objects are {@link Feature} instances. Primarily, datastores are used as persistence
 * layer by the {@link WFService} class.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public abstract class Datastore {

    private static final TriggerProvider TP = TriggerProvider.create( Datastore.class );

    private static final ILogger LOG = LoggerFactory.getLogger( Datastore.class );

    private Collection<MappedGMLSchema> schemas = new ArrayList<MappedGMLSchema>( 10 );

    private DatastoreConfiguration config;
    
    public static final int SRS_UNDEFINED = -1;

    /**
     * Returns the datastore specific annotation parser.
     *
     * @return the datastore specific annotation parser
     */
    public abstract AnnotationDocument getAnnotationParser();

    /**
     * Configures the datastore with the supplied configuration.
     *
     * @param config
     *            configuration
     * @throws DatastoreException
     */
    @SuppressWarnings("unused")
    public void configure( DatastoreConfiguration config )
                            throws DatastoreException {
        this.config = config;
    }

    /**
     * Returns the configuration parameters of the datastore.
     *
     * @return the configuration parameters of the datastore
     */
    public DatastoreConfiguration getConfiguration() {
        return this.config;
    }

    /**
     * Adds the given GML application schema to the set of schemas that are handled by this
     * datastore instance.
     * <p>
     * Note that this method may be called several times for every GML schema that uses this
     * datastore instance.
     *
     * @param schema
     *            GML application schema to bind
     * @throws DatastoreException
     */
    @SuppressWarnings("unused")
    public void bindSchema( MappedGMLSchema schema )
                            throws DatastoreException {
        this.schemas.add( schema );
    }

    /**
     * Returns the GML application schemas that are handled by this datastore.
     *
     * @return the GML application schemas that are handled by this datastore
     */
    public MappedGMLSchema[] getSchemas() {
        return this.schemas.toArray( new MappedGMLSchema[this.schemas.size()] );
    }

    /**
     * Returns the feature type with the given name.
     *
     * @param ftName
     *            name of the feature type
     * @return the feature type with the given name, or null if the <code>Datastore</code> does
     *         not this feature type
     */
    public MappedFeatureType getFeatureType( QualifiedName ftName ) {
        MappedFeatureType ft = null;
        MappedGMLSchema[] schemas = getSchemas();
        for ( int i = 0; i < schemas.length; i++ ) {
            ft = schemas[i].getFeatureType( ftName );
            if ( ft != null ) {
                break;
            }
        }
        return ft;
    }

    /**
     * Closes the datastore so it can free dependent resources.
     *
     * @throws DatastoreException
     */
    public abstract void close()
                            throws DatastoreException;

    /**
     * Performs a query against the datastore.
     *
     * @param query
     *            query to be performed
     * @param rootFts
     *            the root feature types that are queried, more than one type means that the types
     *            are joined
     * @return requested feature instances
     * @throws DatastoreException
     * @throws UnknownCRSException
     */
    public abstract FeatureCollection performQuery( final Query query, final MappedFeatureType[] rootFts )
                            throws DatastoreException, UnknownCRSException;

    /**
     * Performs a query against the datastore (in the given transaction context).
     *
     * @param query
     *            query to be performed
     * @param rootFts
     *            the root feature types that are queried, more than one type means that the types
     *            are joined
     * @param context
     *            context (used to specify the JDBCConnection, for example)
     * @return requested feature instances
     * @throws DatastoreException
     * @throws UnknownCRSException
     */
    public abstract FeatureCollection performQuery( final Query query, final MappedFeatureType[] rootFts,
                                                    final DatastoreTransaction context )
                            throws DatastoreException, UnknownCRSException;

    /**
     * Determines the ids of all features to be locked by the given parts of a {@link LockFeature}
     * request, this includes all descendant and super features of the targeted features as well.
     *
     * @param requestParts
     *            the parts of a <code>LockFeature</code> request that this <code>Datastore</code>
     *            is responsible for
     * @return the ids of all features that have to be locked
     * @throws DatastoreException
     */
    public Set<FeatureId> determineFidsToLock( @SuppressWarnings("unused")
    List<Lock> requestParts )
                            throws DatastoreException {
        throw new DatastoreException( Messages.getMessage( "DATASTORE_METHOD_UNSUPPORTED", this.getClass().getName(),
                                                           "#determineFeaturesToLock( LockFeature )" ) );
    }

    /**
     * Acquires transactional access to the datastore instance. There's only one active transaction
     * per datastore allowed.
     *
     * @return transaction object that allows to perform transactions operations on the datastore
     * @throws DatastoreException
     */
    public DatastoreTransaction acquireTransaction()
                            throws DatastoreException {
        throw new DatastoreException( Messages.getMessage( "DATASTORE_METHOD_UNSUPPORTED", this.getClass().getName(),
                                                           "#acquireTransaction()" ) );
    }

    /**
     * Returns the transaction to the datastore. This makes the transaction available to other
     * clients again (via {@link #acquireTransaction()}). Underlying resources (such as
     * JDBCConnections are freed).
     * <p>
     * The transaction should be terminated, i.e. {@link DatastoreTransaction#commit()} or
     * {@link DatastoreTransaction#rollback()} must have been called before.
     *
     * @param ta
     *            the DatastoreTransaction to be returned
     * @throws DatastoreException
     */
    public void releaseTransaction( @SuppressWarnings("unused")
    DatastoreTransaction ta )
                            throws DatastoreException {
        throw new DatastoreException( Messages.getMessage( "DATASTORE_METHOD_UNSUPPORTED", this.getClass().getName(),
                                                           "#releaseTransaction()" ) );
    }

    /**
     * Transforms the incoming {@link Query} so that the {@link CoordinateSystem} of all spatial
     * arguments (BBOX, etc.) in the {@link Filter} match the SRS of the targeted
     * {@link MappingGeometryField}s.
     * <p>
     * NOTE: If this transformation can be performed by the backend (e.g. by Oracle Spatial), this
     * method should be overwritten to return the original input {@link Query}.
     *
     * @param query
     *            query to be transformed
     * @return query with spatial arguments transformed to target SRS
     */
    protected Query transformQuery( Query query ) {
        LOG.logDebug( "Transforming query." );
        Object[] result = TP.doPreTrigger( this, query );
        return (Query) result[0];
    }

    /**
     * Transforms the {@link FeatureCollection} so that the geometries of all contained geometry
     * properties use the requested SRS.
     *
     * @param fc
     *            feature collection to be transformed
     * @param targetSRS
     *            requested SRS
     * @return transformed FeatureCollection
     */
    protected FeatureCollection transformResult( FeatureCollection fc, String targetSRS ) {
        LOG.logDebug( "Transforming result to SRS '" + targetSRS + "'." );
        Object[] result = TP.doPostTrigger( this, fc, targetSRS );
        return (FeatureCollection) result[0];
    }

    /**
     * Returns whether the datastore is capable of performing a native coordinate transformation
     * (using an SQL function call for example) into the given SRS.
     * <p>
     * <code>Datastore</code> implementations capable of performing native coordinate
     * transformations must override this class.
     *
     * @param targetSRS
     *            target spatial reference system (usually "EPSG:XYZ")
     * @return true, if the datastore can perform the coordinate transformation, false otherwise
     */
    protected boolean canTransformTo( @SuppressWarnings("unused")
    String targetSRS ) {
        return false;
    }
}

//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.feature.persistence.memory;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Features;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.StoredFeatureTypeMetadata;
import org.deegree.feature.persistence.lock.DefaultLockManager;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.CachedFeatureResultSet;
import org.deegree.feature.persistence.query.CombinedResultSet;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sort.SortProperty;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLDocumentIdContext;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLObject;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.ReferenceResolvingException;
import org.slf4j.Logger;

/**
 * {@link FeatureStore} implementation that keeps the feature instances in memory.
 * 
 * @see FeatureStore
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class MemoryFeatureStore implements FeatureStore {

    private static final Logger LOG = getLogger( MemoryFeatureStore.class );

    private final ApplicationSchema schema;

    private final Map<QName, StoredFeatureTypeMetadata> ftNameToMd = new HashMap<QName, StoredFeatureTypeMetadata>();

    private final Map<String, GMLObject> idToObject = new HashMap<String, GMLObject>();

    private final Map<FeatureType, FeatureCollection> ftToFeatures = new HashMap<FeatureType, FeatureCollection>();

    private MemoryFeatureStoreTransaction activeTransaction;

    private Thread transactionHolder;

    final DefaultLockManager lockManager;

    /**
     * Creates a new {@link MemoryFeatureStore} for the given {@link ApplicationSchema}.
     * 
     * @param schema
     *            application schema, must not be <code>null</code>
     * @throws FeatureStoreException
     */
    public MemoryFeatureStore( ApplicationSchema schema ) throws FeatureStoreException {
        this.schema = schema;
        CRS nativeCRS = new CRS( "EPSG:4326" );
        for ( FeatureType ft : schema.getFeatureTypes() ) {
            ftToFeatures.put( ft, new GenericFeatureCollection() );
            String title = ft.getName().toString();
            String desc = ft.getName().toString() + ", served by the GMLMemoryStore";
            StoredFeatureTypeMetadata md = new StoredFeatureTypeMetadata( ft, this, title, desc, nativeCRS );
            ftNameToMd.put( ft.getName(), md );
        }
        lockManager = new DefaultLockManager( this, "LOCK_DB" );
    }

    /**
     * Creates a new {@link MemoryFeatureStore} that is backed by the given GML file.
     * 
     * @param docURL
     * @param schema
     * @throws XMLStreamException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     * @throws FactoryConfigurationError
     * @throws IOException
     * @throws FeatureStoreException
     * @throws ReferenceResolvingException
     */
    public MemoryFeatureStore( URL docURL, ApplicationSchema schema ) throws XMLStreamException, XMLParsingException,
                            UnknownCRSException, FactoryConfigurationError, IOException, FeatureStoreException,
                            ReferenceResolvingException {

        this( schema );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, docURL );
        gmlReader.setApplicationSchema( schema );
        gmlReader.readFeature();
        gmlReader.close();
        GMLDocumentIdContext idContext = gmlReader.getIdContext();
        idContext.resolveLocalRefs();

        // add features
        Map<String, Feature> idToFeature = idContext.getFeatures();
        for ( String id : idToFeature.keySet() ) {
            Feature feature = idToFeature.get( id );
            FeatureType ft = feature.getType();
            FeatureCollection fc2 = ftToFeatures.get( ft );
            fc2.add( feature );
            idToObject.put( id, feature );
        }

        // add geometries
        Map<String, Geometry> idToGeometry = idContext.getGeometries();
        for ( String id : idToGeometry.keySet() ) {
            idToObject.put( id, idToGeometry.get( id ) );
        }
    }

    /**
     * Adds the given {@link Feature} instances.
     * 
     * @param features
     *            features
     */
    void addFeatures( Collection<Feature> features ) {
        for ( Feature feature : features ) {
            FeatureType ft = feature.getType();
            // TODO check if served
            FeatureCollection fc2 = ftToFeatures.get( ft );
            fc2.add( feature );
            if ( feature.getId() != null ) {
                idToObject.put( feature.getId(), feature );
            }
        }
    }

    /**
     * Adds the given identified {@link Geometry} instances.
     * 
     * @param geometries
     *            geometries with ids
     * @throws UnknownCRSException
     */
    void addGeometriesWithId( Collection<Geometry> geometries )
                            throws UnknownCRSException {
        for ( Geometry geometry : geometries ) {
            CRS crs = geometry.getCoordinateSystem();
            // provoke an UnknownCRSException if it is not known
            if ( crs != null ) {
                crs.getWrappedCRS();
            }
            idToObject.put( geometry.getId(), geometry );
        }
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public void init() {
        // TODO Auto-generated method stub

    }

    @Override
    public ApplicationSchema getSchema() {
        return schema;
    }

    @Override
    public FeatureResultSet query( Query query )
                            throws FilterEvaluationException {

        if ( query.getTypeNames() == null || query.getTypeNames().length > 1 ) {
            String msg = "Only queries with exactly one or zero type name(s) are supported.";
            throw new UnsupportedOperationException( msg );
        }

        // TODO what if no type name is specified (use id to determine ft?)
        QName ftName = query.getTypeNames().length > 0 ? query.getTypeNames()[0].getFeatureTypeName()
                                                      : schema.getFeatureTypes()[0].getName();
        FeatureType ft = schema.getFeatureType( ftName );

        // TODO remove this quirk
        if ( ft == null ) {
            for ( FeatureType schemaFt : schema.getFeatureTypes() ) {
                if ( schemaFt.getName().getLocalPart().equals( ftName.getLocalPart() ) ) {
                    ft = schemaFt;
                    break;
                }
            }
        }
        if ( ft == null ) {
            String msg = "Feature type '" + ftName + "' is not served by this datastore.";
            throw new UnsupportedOperationException( msg );
        }

        // determine / filter features
        FeatureCollection fc = ftToFeatures.get( ft );
        if ( query.getFilter() != null ) {
            fc = fc.getMembers( query.getFilter() );
        }

        // sort features
        SortProperty[] sortCrit = query.getSortProperties();
        if ( sortCrit != null ) {
            fc = Features.sortFc( fc, sortCrit );
        }

        return new CachedFeatureResultSet( fc );
    }

    @Override
    public FeatureResultSet query( final Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        Iterator<FeatureResultSet> rsIter = new Iterator<FeatureResultSet>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < queries.length;
            }

            @Override
            public FeatureResultSet next() {
                if ( !hasNext() ) {
                    throw new NoSuchElementException();
                }
                FeatureResultSet rs;
                try {
                    rs = query( queries[i++] );
                } catch ( Exception e ) {
                    throw new RuntimeException( e.getMessage(), e );
                }
                return rs;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
        return new CombinedResultSet( rsIter );
    }

    @Override
    public int queryHits( org.deegree.feature.persistence.query.Query query )
                            throws FilterEvaluationException {
        // TODO maybe implement this more efficiently
        return query( query ).toCollection().size();
    }

    @Override
    public int queryHits( Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO maybe implement this more efficiently
        return query( queries ).toCollection().size();
    }

    private PropertyName findGeoProp( FeatureType ft )
                            throws FilterEvaluationException {

        PropertyName propName = null;

        // TODO what about geometry properties on subfeature levels
        for ( PropertyType<?> pt : ft.getPropertyDeclarations() ) {
            if ( pt instanceof GeometryPropertyType ) {
                propName = new PropertyName( pt.getName() );
                break;
            }
        }

        if ( propName == null ) {
            String msg = "Cannot perform BBox query: requested feature type ('" + ft.getName()
                         + "') does not have a geometry property.";
            throw new FilterEvaluationException( msg );
        }
        return propName;
    }

    @Override
    public GMLObject getObjectById( String id ) {
        return idToObject.get( id );
    }

    @Override
    public synchronized FeatureStoreTransaction acquireTransaction()
                            throws FeatureStoreException {

        while ( this.activeTransaction != null ) {
            Thread holder = this.transactionHolder;
            // check if transaction holder variable has (just) been cleared or if the other thread
            // has been killed (avoid deadlocks)
            if ( holder == null || !holder.isAlive() ) {
                this.activeTransaction = null;
                this.transactionHolder = null;
                break;
            }

            try {
                // wait until the transaction holder wakes us, but not longer than 5000
                // milliseconds (as the transaction holder may very rarely get killed without
                // signalling us)
                wait( 5000 );
            } catch ( InterruptedException e ) {
                // nothing to do
            }
        }

        this.activeTransaction = new MemoryFeatureStoreTransaction( this );
        this.transactionHolder = Thread.currentThread();
        return this.activeTransaction;
    }

    /**
     * Returns the transaction to the datastore. This makes the transaction available to other clients again (via
     * {@link #acquireTransaction()}.
     * <p>
     * The transaction should be terminated, i.e. commit() or rollback() must have been called before.
     * 
     * @param ta
     *            the DatastoreTransaction to be returned
     * @throws FeatureStoreException
     */
    void releaseTransaction( MemoryFeatureStoreTransaction ta )
                            throws FeatureStoreException {
        if ( ta.getStore() != this ) {
            String msg = Messages.getMessage( "TA_NOT_OWNER" );
            throw new FeatureStoreException( msg );
        }
        if ( ta != this.activeTransaction ) {
            String msg = Messages.getMessage( "TA_NOT_ACTIVE" );
            throw new FeatureStoreException( msg );
        }
        this.activeTransaction = null;
        this.transactionHolder = null;
        // notifyAll();
    }

    FeatureCollection getCollection( FeatureType ft ) {
        return ftToFeatures.get( ft );
    }

    void setCollection( FeatureType ft, FeatureCollection fc ) {
        ftToFeatures.put( ft, fc );
    }

    void removeObject( String id )
                            throws FeatureStoreException {
        Object o = idToObject.remove( id );
        if ( o == null ) {
            throw new FeatureStoreException( "Cannot remove feature/geometry with id '" + id + "': no such object." );
        }
        if ( o instanceof Feature ) {
            Feature feature = (Feature) o;
            FeatureCollection fc = ftToFeatures.get( feature.getType() );
            fc.remove( feature );
        }
    }

    @Override
    public LockManager getLockManager()
                            throws FeatureStoreException {
        return lockManager;
    }

    @Override
    public Envelope getEnvelope( QName ftName ) {
        return ftToFeatures.get( schema.getFeatureType( ftName ) ).getEnvelope();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public StoredFeatureTypeMetadata getMetadata( QName ftName ) {
        return ftNameToMd.get( ftName );
    }
}

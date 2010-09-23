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

import static org.deegree.gml.GMLVersion.GML_31;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.index.RTree;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Features;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.DefaultLockManager;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.CombinedResultSet;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.MemoryFeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.xpath.FeatureXPathEvaluator;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.IdFilter;
import org.deegree.filter.sort.SortProperty;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.primitive.Point;
import org.deegree.gml.GMLDocumentIdContext;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLObject;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.ReferenceResolvingException;

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

    private final ApplicationSchema schema;

    private final Map<String, GMLObject> idToObject = new HashMap<String, GMLObject>();

    private final Map<FeatureType, FeatureCollection> ftToFeatures = new HashMap<FeatureType, FeatureCollection>();

    private final Map<FeatureType, RTree<Feature>> ftToIndex = new HashMap<FeatureType, RTree<Feature>>();

    private MemoryFeatureStoreTransaction activeTransaction;

    private Thread transactionHolder;

    // TODO
    FeatureXPathEvaluator evaluator = new FeatureXPathEvaluator( GML_31 );

    DefaultLockManager lockManager;

    CRS storageSRS;

    /**
     * Creates a new {@link MemoryFeatureStore} for the given {@link ApplicationSchema}.
     * 
     * @param schema
     *            application schema, must not be <code>null</code>
     * @param storageSRS
     *            srs used for storing geometries, may be <code>null</code>
     * @throws FeatureStoreException
     */
    MemoryFeatureStore( ApplicationSchema schema, CRS storageSRS ) throws FeatureStoreException {
        this.schema = schema;
        for ( FeatureType ft : schema.getFeatureTypes() ) {
            ftToFeatures.put( ft, new GenericFeatureCollection() );
        }
        // TODO
        lockManager = new DefaultLockManager( this, "LOCK_DB" );
        this.storageSRS = storageSRS;
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

        this( schema, null );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, docURL );
        gmlReader.setApplicationSchema( schema );
        gmlReader.readFeature();
        gmlReader.close();
        GMLDocumentIdContext idContext = gmlReader.getIdContext();
        idContext.resolveLocalRefs();

        // add all features and geometries from the document
        Map<String, GMLObject> idToFeature = idContext.getObjects();
        for ( String id : idToFeature.keySet() ) {
            GMLObject object = idToFeature.get( id );
            if ( object instanceof Feature ) {
                Feature feature = (Feature) object;
                FeatureType ft = feature.getType();
                FeatureCollection fc2 = ftToFeatures.get( ft );
                fc2.add( feature );
                idToObject.put( id, feature );
            } else if ( object instanceof Geometry ) {
                idToObject.put( id, idToFeature.get( id ) );
            }
            idToObject.put( id, object );
        }
    }

    /**
     * Adds the given {@link Feature} instances.
     * 
     * @param features
     *            features
     */
    void addFeatures( Collection<Feature> features ) {

        // add features
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
     * Rebuilds the feature collection maps and indexes after the transaction.
     */
    void rebuildMaps() {

        // create new feature collections (for bounding box recalculation)
        for ( FeatureType ft : ftToFeatures.keySet() ) {
            FeatureCollection oldFc = ftToFeatures.get( ft );
            if ( !oldFc.isEmpty() ) {
                FeatureCollection newFc = new GenericFeatureCollection();
                newFc.addAll( oldFc );
            }
        }

        // (re-) build RTree
        for ( FeatureType ft : ftToFeatures.keySet() ) {
            FeatureCollection fc = ftToFeatures.get( ft );
            Envelope env = fc.getEnvelope();
            if ( env != null ) {
                RTree<Feature> index = new RTree<Feature>( toFloats( env ), 16 );
                List<Pair<float[], Feature>> fBboxes = new ArrayList<Pair<float[], Feature>>( fc.size() );
                for ( Feature f : fc ) {
                    Envelope fEnv = f.getEnvelope();
                    if ( fEnv != null ) {
                        float[] floats = toFloats( fEnv );
                        fBboxes.add( new Pair<float[], Feature>( floats, f ) );
                    }
                }
                index.insertBulk( fBboxes );
                ftToIndex.put( ft, index );
            }
        }
    }

    private float[] toFloats( Envelope env ) {
        return new float[] { (float) env.getMin().get0(), (float) env.getMin().get1(), (float) env.getMax().get0(),
                            (float) env.getMax().get1() };
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
            if ( !( geometry instanceof Point && geometry.getCoordinateDimension() == 1 ) ) {
                CRS crs = geometry.getCoordinateSystem();
                if ( storageSRS != null ) {
                    if ( !storageSRS.equals( crs ) ) {
                        throw new RuntimeException( "Trying to add geometry with CRS " + crs );
                    }
                } else if ( crs != null ) {
                    // provoke an UnknownCRSException if it is not known
                    crs.getWrappedCRS();
                }
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
                            throws FilterEvaluationException, FeatureStoreException {

        if ( query.getTypeNames() == null || query.getTypeNames().length > 1 ) {
            String msg = "Join queries between multiple feature types are currently not supported.";
            throw new UnsupportedOperationException( msg );
        }

        FeatureCollection fc = null;
        if ( query.getTypeNames().length == 1 ) {
            QName ftName = query.getTypeNames()[0].getFeatureTypeName();
            FeatureType ft = schema.getFeatureType( ftName );
            if ( ft == null ) {
                String msg = "Feature type '" + ftName + "' is not served by this feature store.";
                throw new FeatureStoreException( msg );
            }

            // determine / filter features
            fc = ftToFeatures.get( ft );

            // perform index filtering
            Envelope ftEnv = ftToFeatures.get( ft ).getEnvelope();
            if ( query.getPrefilterBBox() != null && ftEnv != null && storageSRS != null ) {
                Envelope prefilterBox = query.getPrefilterBBox();
                if ( prefilterBox.getCoordinateSystem() != null
                     && !prefilterBox.getCoordinateSystem().equals( storageSRS ) ) {
                    try {
                        GeometryTransformer t = new GeometryTransformer( storageSRS );
                        prefilterBox = t.transform( prefilterBox );
                    } catch ( Exception e ) {
                        throw new FeatureStoreException( e.getMessage(), e );
                    }
                }

                float[] floats = toFloats( prefilterBox );
                RTree<Feature> index = ftToIndex.get( ft );
                fc = new GenericFeatureCollection( null, index.query( floats ) );
            }

            if ( query.getFilter() != null ) {
                fc = fc.getMembers( query.getFilter(), evaluator );
            }
        } else {
            // must be an id filter based query
            if ( query.getFilter() == null || !( query.getFilter() instanceof IdFilter ) ) {
                String msg = "Invalid query. If no type names are specified, it must contain an IdFilter.";
                throw new FilterEvaluationException( msg );
            }
            Set<Feature> features = new HashSet<Feature>();
            for ( String id : ( (IdFilter) query.getFilter() ).getMatchingIds() ) {
                GMLObject object = getObjectById( id );
                if ( object != null && object instanceof Feature ) {
                    features.add( (Feature) object );
                }
            }
            fc = new GenericFeatureCollection( null, features );
        }

        // sort features
        SortProperty[] sortCrit = query.getSortProperties();
        if ( sortCrit != null ) {
            fc = Features.sortFc( fc, sortCrit );
        }

        return new MemoryFeatureResultSet( fc );
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
                    e.printStackTrace();
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
                            throws FilterEvaluationException, FeatureStoreException {
        // TODO maybe implement this more efficiently
        return query( query ).toCollection().size();
    }

    @Override
    public int queryHits( Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO maybe implement this more efficiently
        return query( queries ).toCollection().size();
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

    void removeObject( String id ) {
        Object o = idToObject.remove( id );
        if ( o == null ) {
            return;
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
    public CRS getStorageSRS() {
        return storageSRS;
    }
}
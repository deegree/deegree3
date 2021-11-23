//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2013 by:
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

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.db.ConnectionProvider;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.i18n.Messages;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.DefaultLockManager;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.stream.CombinedFeatureInputStream;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Envelope;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;

/**
 * {@link FeatureStore} implementation that keeps the feature instances in memory.
 * 
 * @see FeatureStore
 * @see StoredFeatures
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class MemoryFeatureStore implements FeatureStore {

    private final AppSchema schema;

    private final ICRS storageCRS;

    private MemoryFeatureStoreTransaction activeTransaction;

    private Thread transactionHolder;

    private DefaultLockManager lockManager;

    private StoredFeatures storedFeatures;

    private MemoryFeatureStoreMetadata metadata;

    private boolean strict;

    /**
     * Creates a new {@link MemoryFeatureStore} instance for the given {@link AppSchema}.
     * 
     * @param schema
     *            application schema, must not be <code>null</code>
     * @param storageCRS
     *            crs used for stored geometries, may be <code>null</code> (no transformation on inserts)
     * @param metadata
     * @throws FeatureStoreException
     */
    MemoryFeatureStore( AppSchema schema, ICRS storageCRS, MemoryFeatureStoreMetadata metadata,
                        ConnectionProvider lockConnection ) throws FeatureStoreException {
        this.schema = schema;
        this.storageCRS = storageCRS;
        this.metadata = metadata;
        this.storedFeatures = new StoredFeatures( schema, storageCRS, null );
        // TODO
        lockManager = new DefaultLockManager( this, lockConnection );
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public AppSchema getSchema() {
        return schema;
    }

    @Override
    public boolean isMapped( QName ftName ) {
        return schema.getFeatureType( ftName ) != null;
    }

    @Override
    public FeatureInputStream query( Query query )
                            throws FilterEvaluationException, FeatureStoreException {
        return storedFeatures.query( query );
    }

    @Override
    public FeatureInputStream query( final Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        Iterator<FeatureInputStream> rsIter = new Iterator<FeatureInputStream>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < queries.length;
            }

            @Override
            public FeatureInputStream next() {
                if ( !hasNext() ) {
                    throw new NoSuchElementException();
                }
                FeatureInputStream rs;
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
        return new CombinedFeatureInputStream( rsIter );
    }

    @Override
    public int queryHits( org.deegree.feature.persistence.query.Query query )
                            throws FilterEvaluationException, FeatureStoreException {
        return query( query ).toCollection().size();
    }

    @Override
    public int[] queryHits( Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        int[] hits = new int[queries.length];
        for ( int i = 0; i < queries.length; i++ ) {
            hits[i] = queryHits( queries[i] );
        }
        return hits;
    }

    @Override
    public GMLObject getObjectById( String id ) {
        return storedFeatures.getObjectById( id );
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

        StoredFeatures workingCopy = new StoredFeatures( schema, storageCRS, storedFeatures );
        this.activeTransaction = new MemoryFeatureStoreTransaction( this, workingCopy, lockManager );
        this.transactionHolder = Thread.currentThread();
        return this.activeTransaction;
    }

    /**
     * Returns the transaction to the datastore. This makes the transaction available to other clients again (via
     * {@link #acquireTransaction()}.
     * <p>
     * The transaction should be terminated, i.e. commit() or rollback() must have been called before.
     * </p>
     * 
     * @param ta
     *            the transaction to be released, must not be <code>null</code>
     * @param newFeatures
     * @throws FeatureStoreException
     */
    void releaseTransaction( MemoryFeatureStoreTransaction ta, StoredFeatures newFeatures )
                            throws FeatureStoreException {
        if ( ta.getStore() != this ) {
            String msg = Messages.getMessage( "TA_NOT_OWNER" );
            throw new FeatureStoreException( msg );
        }
        if ( ta != this.activeTransaction ) {
            String msg = Messages.getMessage( "TA_NOT_ACTIVE" );
            throw new FeatureStoreException( msg );
        }
        if ( newFeatures != null ) {
            storedFeatures = newFeatures;
        }
        this.activeTransaction = null;
        this.transactionHolder = null;
        // notifyAll();
    }

    @Override
    public LockManager getLockManager()
                            throws FeatureStoreException {
        return lockManager;
    }

    @Override
    public Envelope getEnvelope( QName ftName )
                            throws FeatureStoreException {
        return calcEnvelope( ftName );
    }

    @Override
    public Envelope calcEnvelope( QName ftName ) {
        Envelope ftEnv = null;
        FeatureType ft = schema.getFeatureType( ftName );
        if ( ft != null ) {
            FeatureCollection fc = storedFeatures.getFeatures( ft );
            if ( fc != null ) {
                ftEnv = fc.getEnvelope();
            }
        }
        return ftEnv;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    /**
     * Returns the CRS used for storing the geometries.
     * 
     * @return the CRS used for storing the geometries, can be <code>null</code> (keeps original CRS)
     */
    public ICRS getStorageCRS() {
        return storageCRS;
    }

    @Override
    public ResourceMetadata<? extends Resource> getMetadata() {
        return metadata;
    }

    @Override
    public void init() {
        // nothing to do
    }
}

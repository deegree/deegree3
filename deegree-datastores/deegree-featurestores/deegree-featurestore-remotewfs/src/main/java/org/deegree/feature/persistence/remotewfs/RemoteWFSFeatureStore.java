//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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

package org.deegree.feature.persistence.remotewfs;

import java.net.URL;

import javax.xml.namespace.QName;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.remotewfs.jaxb.RemoteWFSFeatureStoreConfig;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.types.AppSchema;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.wfs.client.WFSClient;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.metadata.WFSFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureStore} implementation that is backed by a (remote) WFS instance.
 * 
 * @see FeatureStore
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RemoteWFSFeatureStore implements FeatureStore {

    private static final Logger LOG = LoggerFactory.getLogger( RemoteWFSFeatureStore.class );

    private final RemoteWFSFeatureStoreConfig config;

    private WFSClient client;

    private AppSchema appSchema;

    /**
     * Creates a new {@link RemoteWFSFeatureStore} for the given capabilities URL.
     * 
     * @param config
     *            config, must not be <code>null</code>
     */
    RemoteWFSFeatureStore( RemoteWFSFeatureStoreConfig config ) {
        this.config = config;
    }

    @Override
    public void init( DeegreeWorkspace workspace )
                            throws ResourceInitException {
        try {
            LOG.info( "Connecting to " + config.getCapabilitiesURL() + "..." );
            this.client = new WFSClient( new URL( config.getCapabilitiesURL() ) );
            this.appSchema = client.getAppSchema();
            LOG.info( "Ok." );
        } catch ( Exception e ) {
            LOG.info( "Error: " + e.getMessage() );
            throw new ResourceInitException( "Error connecting to WFS: " + e.getMessage(), e );
        }
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public AppSchema getSchema() {
        return appSchema;
    }

    @Override
    public Envelope getEnvelope( QName ftName )
                            throws FeatureStoreException {
        WFSFeatureType ftMetadata = client.getFeatureType( ftName );
        if ( ftMetadata == null ) {
            return null;
        }
        return ftMetadata.getWGS84BoundingBox();
    }

    @Override
    public Envelope calcEnvelope( QName ftName )
                            throws FeatureStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FeatureInputStream query( Query query )
                            throws FeatureStoreException, FilterEvaluationException {

        TypeName[] typeNames = query.getTypeNames();
        Filter filter = query.getFilter();
        int maxFeatures = query.getMaxFeatures();
        return null;
    }

    @Override
    public FeatureInputStream query( Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        return null;
    }

    @Override
    public int queryHits( Query query )
                            throws FeatureStoreException, FilterEvaluationException {
        throw new UnsupportedOperationException( "RemoteWFSFeatureStore doesn't implement #queryHits() (yet)." );
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
    public GMLObject getObjectById( String id )
                            throws FeatureStoreException {
        throw new UnsupportedOperationException( "RemoteWFSFeatureStore doesn't implement #getObjectById() (yet)." );
    }

    @Override
    public FeatureStoreTransaction acquireTransaction()
                            throws FeatureStoreException {
        throw new UnsupportedOperationException( "RemoteWFSFeatureStore doesn't implement #acquireTransaction() (yet)." );
    }

    @Override
    public LockManager getLockManager()
                            throws FeatureStoreException {
        throw new UnsupportedOperationException( "RemoteWFSFeatureStore doesn't implement #getLockManager() (yet)." );
    }
}

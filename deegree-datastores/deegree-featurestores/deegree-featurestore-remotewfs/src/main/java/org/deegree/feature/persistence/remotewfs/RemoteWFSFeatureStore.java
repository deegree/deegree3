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

import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.protocol.wfs.WFSVersion.WFS_110;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.remotewfs.jaxb.RemoteWFSFeatureStoreConfig;
import org.deegree.feature.stream.CombinedFeatureInputStream;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.types.AppSchema;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.projection.PropertyName;
import org.deegree.filter.sort.SortProperty;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.ows.exception.OWSExceptionReport;
import org.deegree.protocol.wfs.client.GetFeatureResponse;
import org.deegree.protocol.wfs.client.WFSClient;
import org.deegree.protocol.wfs.client.WFSFeatureCollection;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.ResultType;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.metadata.WFSFeatureType;
import org.deegree.protocol.wfs.query.FilterQuery;
import org.deegree.protocol.wfs.query.StandardPresentationParams;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;

/**
 * {@link FeatureStore} implementation that is backed by a (remote) WFS instance.
 * 
 * @see FeatureStore
 * @see WFSClient
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RemoteWFSFeatureStore implements FeatureStore {

    // private static final Logger LOG = LoggerFactory.getLogger( RemoteWFSFeatureStore.class );
    //
    // private final RemoteWFSFeatureStoreConfig config;

    private WFSClient client;

    private AppSchema appSchema;

    private boolean strict;

    /**
     * Creates a new {@link RemoteWFSFeatureStore} for the given capabilities URL.
     * 
     * @param config
     *            config, must not be <code>null</code>
     */
    RemoteWFSFeatureStore( RemoteWFSFeatureStoreConfig config ) {
        // this.config = config;
    }

    // @Override
    // public void init( DeegreeWorkspace workspace )
    // throws ResourceInitException {
    // try {
    // LOG.info( "Connecting to " + config.getCapabilitiesURL() + "..." );
    // this.client = new WFSClient( new URL( config.getCapabilitiesURL() ) );
    // this.appSchema = client.getAppSchema();
    // LOG.info( "Ok." );
    // } catch ( Exception e ) {
    // LOG.info( "Error: " + e.getMessage() );
    // throw new ResourceInitException( "Error connecting to WFS: " + e.getMessage(), e );
    // }
    // }

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
    public boolean isMapped( QName ftName ) {
        return appSchema.getFeatureType( ftName ) != null;
    }

    @Override
    public boolean isMaxFeaturesAndStartIndexApplicable( Query[] queries ) {
        return false;
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

        org.deegree.protocol.wfs.query.Query wfsQuery = toWFSQuery( query );

        StandardPresentationParams presentationParams = new StandardPresentationParams( null, null, ResultType.RESULTS,
                                                                                        GML_31.getMimeType() );
        GetFeature request = new GetFeature( WFS_110.getOGCVersion(), null, presentationParams, null,
                                             Collections.singletonList( wfsQuery ) );

        FeatureInputStream is = null;
        try {
            final GetFeatureResponse<Feature> response = client.doGetFeature( request );
            final WFSFeatureCollection<Feature> wfsFc = response.getAsWFSFeatureCollection();
            is = new FeatureInputStream() {

                @Override
                public Iterator<Feature> iterator() {
                    return wfsFc.getMembers();
                }

                @Override
                public FeatureCollection toCollection() {
                    return wfsFc.toCollection();
                }

                @Override
                public int count() {
                    // TODO
                    return 0;
                }

                @Override
                public void close() {
                    try {
                        response.close();
                    } catch ( IOException e ) {
                        throw new RuntimeException( e.getMessage(), e );
                    }
                }
            };
        } catch ( OWSExceptionReport e ) {
            throw new FeatureStoreException( "Remote WFS responded with exception report: " + e.getMessage() );
        } catch ( Throwable t ) {
            throw new FeatureStoreException( "Error performing GetFeature request to remote WFS: " + t.getMessage() );
        }

        return is;
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
    public int queryHits( Query query )
                            throws FeatureStoreException, FilterEvaluationException {

        org.deegree.protocol.wfs.query.Query wfsQuery = toWFSQuery( query );

        StandardPresentationParams presentationParams = new StandardPresentationParams( null, null, ResultType.HITS,
                                                                                        GML_31.getMimeType() );
        GetFeature request = new GetFeature( WFS_110.getOGCVersion(), null, presentationParams, null,
                                             Collections.singletonList( wfsQuery ) );

        int hits = -1;
        GetFeatureResponse<Feature> response = null;
        try {
            response = client.doGetFeature( request );
            hits = response.getAsWFSFeatureCollection().getNumberMatched().intValue();
        } catch ( OWSExceptionReport e ) {
            throw new FeatureStoreException( "Remote WFS responded with exception report: " + e.getMessage() );
        } catch ( Throwable t ) {
            throw new FeatureStoreException( "Error performing GetFeature request to remote WFS: " + t.getMessage() );
        } finally {
            if ( response != null ) {
                try {
                    response.close();
                } catch ( IOException e ) {
                    // nothing to do
                }
            }
        }
        return hits;
    }

    private org.deegree.protocol.wfs.query.Query toWFSQuery( Query query ) {
        TypeName[] typeNames = query.getTypeNames();
        String featureVersion = null;
        ICRS srsName = null;
        PropertyName[] projectionClauses = null;
        SortProperty[] sortBy = query.getSortProperties();
        Filter filter = query.getFilter();
        return new FilterQuery( null, typeNames, featureVersion, srsName, projectionClauses, sortBy, filter );
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

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.workspace.Resource#getMetadata()
     */
    @Override
    public ResourceMetadata<? extends Resource> getMetadata() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.workspace.Resource#init()
     */
    @Override
    public void init() {
        // TODO Auto-generated method stub

    }
}

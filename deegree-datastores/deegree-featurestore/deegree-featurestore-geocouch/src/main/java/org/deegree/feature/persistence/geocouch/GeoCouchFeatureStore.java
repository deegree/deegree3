//$HeadURL: svn+ssh://aschmitz@deegree.wald.intevation.de/deegree/deegree3/trunk/deegree-datastores/deegree-featurestore/deegree-featurestore-shape/src/main/java/org/deegree/feature/persistence/shape/ShapeFeatureStore.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.feature.persistence.geocouch;

import static org.deegree.commons.utils.net.HttpUtils.JSON;
import static org.deegree.feature.persistence.sql.blob.BlobCodec.Compression.NONE;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.utils.CloseableIterator;
import org.deegree.commons.utils.net.HttpUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.Feature;
import org.deegree.feature.Features;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.CombinedResultSet;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.FilteredFeatureResultSet;
import org.deegree.feature.persistence.query.IteratorResultSet;
import org.deegree.feature.persistence.query.MemoryFeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.sql.blob.BlobCodec;
import org.deegree.feature.persistence.sql.blob.BlobCodec.Compression;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.sort.SortProperty;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.primitive.Point;
import org.deegree.gml.GMLObject;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.slf4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;

/**
 * {@link FeatureStore} implementation that uses GeoCouch as backend.
 * 
 * @see FeatureStore
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 30411 $, $Date: 2011-04-11 19:37:01 +0200 (Mon, 11 Apr 2011) $
 */
public class GeoCouchFeatureStore implements FeatureStore {

    private static final Logger LOG = getLogger( GeoCouchFeatureStore.class );

    private ICRS crs;

    private ApplicationSchema schema;

    private String couchUrl;

    public GeoCouchFeatureStore( ICRS crs, ApplicationSchema schema, String couchUrl ) {
        this.crs = crs;
        this.schema = schema;
        this.couchUrl = couchUrl;
    }

    @Override
    public void init( DeegreeWorkspace workspace )
                            throws ResourceInitException {
        try {
            JsonObject obj = HttpUtils.get( HttpUtils.JSON, couchUrl + "_design/main", null ).getAsJsonObject();
            if ( obj.get( "spatial" ) == null ) {
                HttpClient client = new DefaultHttpClient();
                HttpPut put = new HttpPut( couchUrl + "_design/main" );
                // function(doc) {
                // if (doc.bbox) {
                // emit({
                // type: "Point",
                // bbox: doc.bbox,
                // coordinates: [0, 0]
                // }, [doc._id, doc.bbox]);
                // }};"

                JsonObject spatial = new JsonObject();
                JsonObject indexes = new JsonObject();
                spatial.add( "spatial", indexes );
                for ( FeatureType type : schema.getFeatureTypes() ) {
                    String name = type.getName().getLocalPart().toString();
                    String indexFunc = "function(doc){" + "if(doc.bbox){" + "emit({" + "type: \'Point\',"
                                       + "bbox: doc.bbox," + "coordinates[0, 0]" + "}, doc._id);\"" + "}}";
                    indexes.addProperty( name, indexFunc );
                }

                System.out.println( spatial.toString() );
                put.setEntity( new StringEntity( spatial.toString() ) );
                HttpResponse resp = client.execute( put );
                System.out.println( IOUtils.toString( resp.getEntity().getContent() ) );
                client.getConnectionManager().shutdown();
            }
        } catch ( Throwable e ) {
            throw new ResourceInitException( "Error connecting to GeoCouch.", e );
        }

        // http://127.0.0.1:5984/inspire/_design/main/
        // possibly check for 404
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
    public ApplicationSchema getSchema() {
        return schema;
    }

    @Override
    public Envelope getEnvelope( QName ftName )
                            throws FeatureStoreException {
        return null;
    }

    @Override
    public FeatureResultSet query( Query query )
                            throws FeatureStoreException, FilterEvaluationException {
        if ( query.getTypeNames() == null || query.getTypeNames().length > 1 ) {
            String msg = "Join queries between multiple feature types are not supported yet.";
            throw new UnsupportedOperationException( msg );
        }

        FeatureResultSet result = null;
        Filter filter = query.getFilter();

        if ( query.getTypeNames().length == 1 && ( filter == null || filter instanceof OperatorFilter ) ) {
            QName ftName = query.getTypeNames()[0].getFeatureTypeName();
            FeatureType ft = getSchema().getFeatureType( ftName );
            if ( ft == null ) {
                String msg = "Feature type '" + ftName + "' is not served by this feature store.";
                throw new FeatureStoreException( msg );
            }
            result = queryByOperatorFilter( query, ftName, (OperatorFilter) filter );
        } else {
            // must be an id filter based query
            if ( query.getFilter() == null || !( query.getFilter() instanceof IdFilter ) ) {
                String msg = "Invalid query. If no type names are specified, it must contain an IdFilter.";
                throw new FilterEvaluationException( msg );
            }
            result = queryByIdFilter( (IdFilter) filter, query.getSortProperties() );
        }
        return result;
    }

    private FeatureResultSet queryByIdFilter( IdFilter filter, SortProperty[] sortCrit )
                            throws FeatureStoreException {

        FeatureResultSet result = null;
        BlobCodec codec = new BlobCodec( schema.getXSModel().getVersion(), Compression.NONE );
        try {
            result = new IteratorResultSet( new IDIterator( filter.getMatchingIds().iterator(), codec ) );
        } catch ( Throwable e ) {
            String msg = "Error performing id query: " + e.getMessage();
            LOG.debug( msg, e );
            throw new FeatureStoreException( msg, e );
        }

        // sort features
        if ( sortCrit.length > 0 ) {
            result = new MemoryFeatureResultSet( Features.sortFc( result.toCollection(), sortCrit ) );
        }
        return result;
    }

    private FeatureResultSet queryByOperatorFilter( Query query, QName ftName, OperatorFilter filter )
                            throws FeatureStoreException {
        LOG.debug( "Performing blob query by operator filter" );
        FeatureResultSet result = null;

        try {
            if ( query.getPrefilterBBox() != null ) {
                List<String> ids = new ArrayList<String>();
                Envelope box = query.getPrefilterBBox();
                Point min = box.getMin();
                Point max = box.getMax();

                for ( TypeName name : query.getTypeNames() ) {
                    String idxname = name.getFeatureTypeName().getLocalPart().toString();
                    String url = couchUrl + "_design/main/_spatial/" + idxname + "?bbox=";
                    url += min.get0() + "," + min.get1() + "," + max.get0() + "," + max.get1();
                    JsonObject obj = HttpUtils.get( JSON, url, null ).getAsJsonObject();
                    JsonElement elem = obj.get( "rows" );
                    System.out.println(obj);
                    if ( elem == null )
                        throw new FeatureStoreException( "fail bbox query" );
                    JsonArray a = elem.getAsJsonArray();
                    for ( int i = 0; i < a.size(); ++i ) {
                        ids.add( a.get( i ).getAsJsonObject().get( "id" ).getAsString() );
                    }
                }

                BlobCodec codec = new BlobCodec( schema.getXSModel().getVersion(), Compression.NONE );
                result = new IteratorResultSet( new IDIterator( ids.iterator(), codec ) );
            }
        } catch ( Throwable e ) {
            String msg = "Error performing query by operator filter: " + e.getMessage();
            LOG.error( msg, e );
            throw new FeatureStoreException( msg, e );
        }

        if ( filter != null ) {
            LOG.debug( "Applying in-memory post-filtering." );
            result = new FilteredFeatureResultSet( result, filter );
        }

        if ( query.getSortProperties().length > 0 ) {
            LOG.debug( "Applying in-memory post-sorting." );
            result = new MemoryFeatureResultSet( Features.sortFc( result.toCollection(), query.getSortProperties() ) );
        }
        return result;
    }

    @Override
    public FeatureResultSet query( final Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {

        // check for most common case: multiple featuretypes, same bbox (WMS), no filter
        boolean wmsStyleQuery = false;
        Envelope env = queries[0].getPrefilterBBox();
        if ( queries[0].getFilter() == null && queries[0].getSortProperties().length == 0 ) {
            wmsStyleQuery = true;
            for ( int i = 1; i < queries.length; i++ ) {
                Envelope queryBBox = queries[i].getPrefilterBBox();
                if ( queryBBox != env && queries[i].getFilter() != null && queries[i].getSortProperties() != null ) {
                    wmsStyleQuery = false;
                    break;
                }
            }
        }

        if ( wmsStyleQuery ) {
            // return queryMultipleFts( queries, env );
        }

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
                } catch ( Throwable e ) {
                    LOG.debug( e.getMessage(), e );
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
    public int queryHits( Query query )
                            throws FeatureStoreException, FilterEvaluationException {
        return queryHits( new Query[] { query } );
    }

    @Override
    public int queryHits( Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public GMLObject getObjectById( String id )
                            throws FeatureStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    private Feature getFeatureById( String id ) {
        BlobCodec codec = new BlobCodec( schema.getXSModel().getVersion(), NONE );
        HttpClient client = new DefaultHttpClient();
        try {
            HttpGet get = new HttpGet( couchUrl + id + "/feature" );
            HttpResponse resp = client.execute( get );
            return (Feature) codec.decode( resp.getEntity().getContent(), schema.getNamespaceBindings(), schema, crs,
                                           null );
        } catch ( Throwable e ) {
            e.printStackTrace();
        } finally {
            client.getConnectionManager().shutdown();
        }
        return null;
    }

    @Override
    public FeatureStoreTransaction acquireTransaction()
                            throws FeatureStoreException {
        return new GeoCouchFeatureStoreTransaction( this, crs, couchUrl );
    }

    @Override
    public LockManager getLockManager()
                            throws FeatureStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    private class IDIterator implements CloseableIterator<Feature> {

        private final Iterator<String> ids;

        private final BlobCodec codec;

        IDIterator( Iterator<String> ids, BlobCodec codec ) {
            this.ids = ids;
            this.codec = codec;
        }

        @Override
        public void close() {
            // nothing to do
        }

        @Override
        public Collection<Feature> getAsCollectionAndClose( Collection<Feature> collection ) {
            while ( hasNext() ) {
                collection.add( next() );
            }
            return collection;
        }

        @Override
        public List<Feature> getAsListAndClose() {
            return (List<Feature>) getAsCollectionAndClose( new LinkedList<Feature>() );
        }

        @Override
        public boolean hasNext() {
            return ids.hasNext();
        }

        @Override
        public Feature next() {
            HttpClient client = new DefaultHttpClient();
            try {
                HttpGet get = new HttpGet( couchUrl + ids.next() + "/feature" );
                HttpResponse resp = client.execute( get );
                return (Feature) codec.decode( resp.getEntity().getContent(), schema.getNamespaceBindings(), schema,
                                               crs, null );
            } catch ( Throwable e ) {
                e.printStackTrace();
            } finally {
                client.getConnectionManager().shutdown();
            }
            return null;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
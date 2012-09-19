package org.deegree.feature.persistence.geocouch;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.namespace.QName;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.Lock;
import org.deegree.filter.Filter;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.protocol.wfs.transaction.action.IDGenMode;
import org.deegree.protocol.wfs.transaction.action.ParsedPropertyReplacement;
import org.slf4j.Logger;

import com.couchbase.client.CouchbaseClient;

public class GeoCouchFeatureStoreTransaction implements FeatureStoreTransaction {

    private static final Logger LOG = getLogger( GeoCouchFeatureStoreTransaction.class );

    private final GeoCouchFeatureStore store;

    private final ICRS crs;

    private final String couchUrl;

    private CouchbaseClient client;

    public GeoCouchFeatureStoreTransaction( GeoCouchFeatureStore store, ICRS crs, String couchUrl ) {
        this.store = store;
        this.crs = crs;
        this.couchUrl = couchUrl;
        try {
            client = new CouchbaseClient( Collections.singletonList( new URI( couchUrl + "/pools" ) ), "default", "" );
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( URISyntaxException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public FeatureStore getStore() {
        return store;
    }

    @Override
    public void commit()
                            throws FeatureStoreException {
        // TODO by hand
    }

    @Override
    public void rollback()
                            throws FeatureStoreException {
        // TODO by hand
    }

    @Override
    public List<String> performInsert( FeatureCollection fc, IDGenMode mode )
                            throws FeatureStoreException {
        // BlobCodec codec = new BlobCodec( store.getSchema().getGMLSchema().getVersion(), Compression.NONE );
        List<String> ids = new ArrayList<String>();
        FeatureTranscoder trans = new FeatureTranscoder( store, crs );
        try {
            for ( Feature f : fc ) {
                client.add( f.getId(), 0, f, trans );
                client.waitForQueues( 1, TimeUnit.HOURS );
            }
        } catch ( Throwable e ) {
            throw new FeatureStoreException( e );
        }
        return ids;
    }

    @Override
    public int performUpdate( QName ftName, List<ParsedPropertyReplacement> replacementProps, Filter filter, Lock lock )
                            throws FeatureStoreException {
        throw new FeatureStoreException( "Update is not supported yet." );
    }

    @Override
    public int performDelete( QName ftName, OperatorFilter filter, Lock lock )
                            throws FeatureStoreException {
        throw new FeatureStoreException( "Delete is not supported yet." );
    }

    @Override
    public int performDelete( IdFilter filter, Lock lock )
                            throws FeatureStoreException {
        throw new FeatureStoreException( "Delete is not supported yet." );
    }

    @Override
    public String performReplace( Feature replacement, Filter filter, Lock lock, IDGenMode idGenMode )
                            throws FeatureStoreException {
        throw new FeatureStoreException( "Replace is not supported yet." );
    }
}

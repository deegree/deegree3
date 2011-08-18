package org.deegree.feature.persistence.geocouch;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.Lock;
import org.deegree.feature.persistence.sql.blob.BlobCodec;
import org.deegree.feature.persistence.sql.blob.BlobCodec.Compression;
import org.deegree.feature.property.Property;
import org.deegree.filter.Filter;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.primitive.Point;
import org.deegree.protocol.wfs.transaction.IDGenMode;
import org.slf4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GeoCouchFeatureStoreTransaction implements FeatureStoreTransaction {

    private static final Logger LOG = getLogger( GeoCouchFeatureStoreTransaction.class );

    private final GeoCouchFeatureStore store;

    private final ICRS crs;

    private final String couchUrl;

    public GeoCouchFeatureStoreTransaction( GeoCouchFeatureStore store, ICRS crs, String couchUrl ) {
        this.store = store;
        this.crs = crs;
        this.couchUrl = couchUrl;
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
        BlobCodec codec = new BlobCodec( store.getSchema().getGMLSchema().getVersion(), Compression.NONE );
        List<String> ids = new ArrayList<String>();
        try {
            for ( Feature f : fc ) {
                HttpClient client = new DefaultHttpClient();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                codec.encode( f, store.getSchema().getNamespaceBindings(), bos, crs );
                bos.close();
                byte[] bs = bos.toByteArray();
                String req = couchUrl + f.getId();
                ids.add( f.getId() );
                Envelope bbox = f.getEnvelope();
                QName ftName = f.getName();
                String json = "{\"feature_type\": \"" + ftName.toString() + "\"";
                if ( bbox != null ) {
                    json += ", \"bbox\": [";
                    Point min = bbox.getMin();
                    Point max = bbox.getMax();
                    json += min.get0() + "," + min.get1() + "," + max.get0() + "," + max.get1() + "]";
                }
                json += "}";
                HttpPut put = new HttpPut( req );
                put.setEntity( new StringEntity( json, "UTF-8" ) );
                HttpResponse resp = client.execute( put );
                JsonParser parser = new JsonParser();
                String resps = IOUtils.toString( resp.getEntity().getContent(), "UTF-8" );
                JsonObject obj = parser.parse( resps ).getAsJsonObject();
                JsonElement elem = obj.get( "ok" );
                if ( elem == null ) {
                    throw new FeatureStoreException( "GeoCouch insert failed, " + obj.get( "reason" ).getAsString() );
                }
                String revision = obj.get( "rev" ).getAsString();
                String url = couchUrl + f.getId() + "/feature/?rev=" + revision;
                put = new HttpPut( url );
                put.setEntity( new ByteArrayEntity( bs ) );
                resp = client.execute( put );

                resps = IOUtils.toString( resp.getEntity().getContent(), "UTF-8" );
                obj = parser.parse( resps ).getAsJsonObject();
                elem = obj.get( "ok" );
                if ( elem == null ) {
                    throw new FeatureStoreException( "GeoCouch insert failed, " + obj.get( "reason" ).getAsString() );
                }

                client.getConnectionManager().shutdown();
            }
        } catch ( FeatureStoreException e ) {
            throw e;
        } catch ( Throwable e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ids;
    }

    @Override
    public int performUpdate( QName ftName, List<Property> replacementProps, Filter filter, Lock lock )
                            throws FeatureStoreException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int performDelete( QName ftName, OperatorFilter filter, Lock lock )
                            throws FeatureStoreException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int performDelete( IdFilter filter, Lock lock )
                            throws FeatureStoreException {
        // TODO Auto-generated method stub
        return 0;
    }

}

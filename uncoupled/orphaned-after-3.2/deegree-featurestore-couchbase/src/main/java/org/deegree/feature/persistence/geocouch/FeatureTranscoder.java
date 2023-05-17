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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.feature.persistence.geocouch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import net.spy.memcached.CachedData;
import net.spy.memcached.transcoders.Transcoder;

import org.apache.commons.codec.binary.Base64;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.sql.blob.BlobCodec;
import org.deegree.feature.persistence.sql.blob.BlobCodec.Compression;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.primitive.Point;

/**
 * <code>FeatureTranscoder</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * 
 */

public class FeatureTranscoder implements Transcoder<Feature> {

    private final FeatureStore store;

    private final ICRS crs;

    private BlobCodec codec;

    public FeatureTranscoder( FeatureStore store, ICRS crs ) {
        this.store = store;
        this.crs = crs;
        this.codec = new BlobCodec( store.getSchema().getGMLSchema().getVersion(), Compression.NONE );
    }

    @Override
    public boolean asyncDecode( CachedData data ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Feature decode( CachedData data ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CachedData encode( Feature f ) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            codec.encode( f, store.getSchema().getNamespaceBindings(), bos, crs );
            bos.close();
            byte[] bs = bos.toByteArray();
            bs = Base64.encodeBase64( bs );

            Envelope bbox = f.getEnvelope();
            QName ftName = f.getName();
            StringBuilder json = new StringBuilder( "{\"feature_type\": \"" + ftName.toString() + "\"" );
            if ( bbox != null ) {
                json.append( ", \"bbox\": [" );
                Point min = bbox.getMin();
                Point max = bbox.getMax();
                json.append( min.get0() + "," + min.get1() + "," + max.get0() + "," + max.get1() + "]" );
            }
            json.append( ", \"blob\": \"" ).append( new String( bs ) ).append( "\"" );
            json.append( "}" );
            return new CachedData( -1, json.toString().getBytes( "UTF-8" ), 1024 * 1024 * 10 );
        } catch ( UnsupportedEncodingException e ) {
            // UTF-8 is supported
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( FeatureStoreException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( FactoryConfigurationError e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( UnknownCRSException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( TransformationException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getMaxSize() {
        return 1024 * 1024 * 10;
    }

}

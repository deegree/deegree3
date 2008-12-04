//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.commons.utils;

import static javax.imageio.ImageIO.read;
import static org.deegree.commons.utils.ArrayUtils.join;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.deegree.commons.xml.XMLAdapter;

/**
 * <code>HttpUtils</code>
 * 
 * Example use from rhino:
 * 
 * <code>
 * var u = org.deegree.commons.utils.HttpUtils
 * u.retrieve(u.UTF8STRING, "http://demo.deegree.org/deegree-wms/services?request=capabilities&service=WMS")
 * </code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class HttpUtils {

    /**
     * <code>Worker</code> is used to specify how to return the stream from the remote location.
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     * @param <T>
     */
    public interface Worker<T> {
        /**
         * @param in
         * @return some object created from the input stream
         * @throws IOException
         */
        T work( InputStream in )
                                throws IOException;
    }

    /**
     * Directly returns the stream.
     */
    public static final Worker<InputStream> STREAM = new Worker<InputStream>() {
        public InputStream work( InputStream in ) {
            return in;
        }
    };

    /**
     * Returns streaming XMLAdapter.
     */
    public static final Worker<XMLAdapter> XML = new Worker<XMLAdapter>() {
        public XMLAdapter work( InputStream in ) {
            return new XMLAdapter( in );
        }
    };

    /**
     * Returns a decoded String.
     */
    public static final Worker<String> UTF8STRING = getStringWorker( "UTF-8" );

    /**
     * Returns a BufferedImage.
     */
    public static final Worker<BufferedImage> IMAGE = new Worker<BufferedImage>() {
        public BufferedImage work( InputStream in )
                                throws IOException {
            return read( in );
        }
    };

    /**
     * @param encoding
     * @return a string producer for a specific encoding
     */
    public static Worker<String> getStringWorker( final String encoding ) {
        return new Worker<String>() {
            public String work( InputStream in )
                                    throws IOException {
                BufferedReader bin = new BufferedReader( new InputStreamReader( in, encoding ) );
                StringBuilder b = new StringBuilder();
                String str;
                while ( ( str = bin.readLine() ) != null ) {
                    b.append( str ).append( "\n" );
                }
                bin.close();
                return b.toString();
            }
        };
    }

    /**
     * @param <T>
     * @param worker
     * @param url
     * @return some object from the url
     * @throws IOException
     */
    public static <T> T retrieve( Worker<T> worker, URL url )
                            throws IOException {
        return worker.work( url.openStream() );
    }

    /**
     * @param <T>
     * @param worker
     * @param url
     * @return some object from the url
     * @throws MalformedURLException
     * @throws IOException
     */
    public static <T> T retrieve( Worker<T> worker, String url )
                            throws MalformedURLException, IOException {
        return retrieve( worker, new URL( url ) );
    }

    /**
     * @param <T>
     * @param worker
     * @param url
     * @param map
     * @return some object from the url
     * @throws IOException
     * @throws MalformedURLException
     */
    public static <T> T retrieve( Worker<T> worker, String url, Map<String, String> map )
                            throws MalformedURLException, IOException {
        if ( !url.endsWith( "?" ) && !url.endsWith( "&" ) ) {
            url += url.indexOf( "?" ) == -1 ? "?" : "&";
        }
        LinkedList<String> list = new LinkedList<String>();
        for ( String k : map.keySet() ) {
            list.add( k + "=" + map.get( k ) );
        }
        url += join( "&", list );
        return retrieve( worker, url );
    }

    /**
     * @param <T>
     * @param worker
     * @param url
     * @param postdata
     * @param contentType
     * @return some object from the url
     * @throws HttpException
     * @throws IOException
     */
    public static <T> T post( Worker<T> worker, String url, byte[] postdata, String contentType )
                            throws HttpException, IOException {
        // TODO no proxies used, not tested, no content type used - just here for example in case anyone actually needs
        // this
        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod( url );
        post.setRequestEntity( new ByteArrayRequestEntity( postdata ) );
        post.setRequestHeader( "Content-Type", contentType ); // written like this?
        client.executeMethod( post );
        return worker.work( post.getResponseBodyAsStream() );
    }
}

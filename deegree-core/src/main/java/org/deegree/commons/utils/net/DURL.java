//$HeadURL$
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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.commons.utils.net;

import static java.util.Collections.synchronizedMap;
import static org.deegree.commons.utils.net.HttpUtils.enableProxyUsage;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;

/**
 * 
 * [D]eegree[URL]
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DURL {

    private static final Logger LOG = getLogger( DURL.class );

    private static final Map<String, Class<? extends URLStreamHandler>> handlers = synchronizedMap( new HashMap<String, Class<? extends URLStreamHandler>>() );

    private URLStreamHandler handler;

    private URL url;

    static {
        handlers.put( "data", DataHandler.class );
    }

    /**
     * @param url
     */
    public DURL( String url ) {
        String protocol = url.split( ":" )[0];
        Class<? extends URLStreamHandler> handler = handlers.get( protocol );
        if ( handler != null ) {
            try {
                this.handler = handler.newInstance();
                this.url = new URL( null, url, this.handler );
            } catch ( InstantiationException e ) {
                LOG.debug( "URL handler '{}' could not be instantiated: '{}'", handler.getSimpleName(),
                           e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            } catch ( IllegalAccessException e ) {
                LOG.debug( "URL handler constructor of '{}' could not be used: '{}'", handler.getSimpleName(),
                           e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            } catch ( MalformedURLException e ) {
                LOG.debug( "URL '{}' could not be instantiated: '{}'", url, e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            }
        } else {
            try {
                this.url = new URL( url );
            } catch ( MalformedURLException e ) {
                LOG.debug( "URL '{}' could not be instantiated: '{}'", url, e.getLocalizedMessage() );
                LOG.trace( "Stack trace:", e );
            }
        }
    }

    /**
     * @param protocol
     * @param handler
     */
    public static void registerHandler( String protocol, Class<? extends URLStreamHandler> handler ) {
        handlers.put( protocol, handler );
    }

    /**
     * @return true, if the underlying URL object could be instantiated
     */
    public boolean valid() {
        return url != null;
    }

    /**
     * @return the underlying URL
     */
    public URL getURL() {
        return url;
    }

    /**
     * @return an input stream from the URL
     * @throws IOException
     */
    public InputStream openStream()
                            throws IOException {
        // custom handlers should handle proxies themselves
        if ( handler != null ) {
            return url.openStream();
        }

        HttpClient client = enableProxyUsage( new HttpClient(), this );
        GetMethod get = new GetMethod( url.toExternalForm() );
        client.executeMethod( get );
        return get.getResponseBodyAsStream();
    }

}

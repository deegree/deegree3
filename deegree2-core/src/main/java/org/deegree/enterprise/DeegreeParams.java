// $HeadURL$
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
package org.deegree.enterprise;

import java.io.Serializable;
import java.nio.charset.Charset;

import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.ogcwebservices.wms.InvalidFormatException;

/**
 * Base class for the <code>deegreeParams</code> section of configurations for all deegree web
 * service types. The <code>deegreeParams</code> section contains deegree specific parameters that
 * are not part of the OGC CSW capabilities specification. The concrete web service implementations
 * (WMS, WFS CWS, ...) derive this class and add their specific configuration parameters.
 * <p>
 * The common <code>deegreeParams</code> elements are: <table border="1">
 * <tr>
 * <th>Name</th>
 * <th>Mandatory</th>
 * <th>Function</th>
 * </tr>
 * <tr>
 * <td>DefaultOnlineResource</td>
 * <td align="center">X</td>
 * <td>The DefaultOnlineResource will be used whenever a required OnlineResource is not defined.</td>
 * </tr>
 * <tr>
 * <td>CacheSize</td>
 * <td align="center">-</td>
 * <td>Amount of Memory to use for caching, default = 100 (MB).</td>
 * </tr>
 * <tr>
 * <td>RequestTimeLimit</td>
 * <td align="center">-</td>
 * <td>Maximum amount of time that is allowed for the execution of a request, defaults to 2
 * minutes.</td>
 * </tr>
 * <tr>
 * <td>Encoding</td>
 * <td align="center">-</td>
 * <td>String encoding, default is UTF-8.</td>
 * </tr>
 * </table>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public abstract class DeegreeParams implements Serializable {

    private OnlineResource defaultOnlineResource = null;

    private int cacheSize = 100;

    private int requestTimeLimit = 0;

    private Charset characterSet = null;

    /**
     * Creates a new instance of DeegreeParams with characterSet set to UTF-8.
     *
     * @param defaultOnlineResource
     * @param cacheSize
     * @param requestTimeLimit
     *            in milliseconds
     */
    public DeegreeParams( OnlineResource defaultOnlineResource, int cacheSize, int requestTimeLimit ) {
        this.defaultOnlineResource = defaultOnlineResource;
        this.cacheSize = cacheSize;
        this.requestTimeLimit = requestTimeLimit;
        if ( Charset.isSupported( "UTF-8" ) ) {// UTF-8 mus be supported
            this.characterSet = Charset.forName( "UTF-8" );
        }
    }

    /**
     * Creates a new instance of DeegreeParams.
     *
     * @param defaultOnlineResource
     * @param cacheSize
     * @param requestTimeLimit
     * @param characterSet
     */
    public DeegreeParams( OnlineResource defaultOnlineResource, int cacheSize, int requestTimeLimit, String characterSet ) {
        this.defaultOnlineResource = defaultOnlineResource;
        this.cacheSize = cacheSize;
        this.requestTimeLimit = requestTimeLimit;
        if ( Charset.isSupported( characterSet ) ) {
            this.characterSet = Charset.forName( characterSet );
        } else if ( Charset.isSupported( "UTF-8" ) ) {// UTF-8 mus be supported
            this.characterSet = Charset.forName( "UTF-8" );
        }
    }

    /**
     * Returns the CacheSize.
     *
     * @return the size
     */
    public int getCacheSize() {
        return cacheSize;
    }

    /**
     * Sets the CacheSize.
     *
     * @param cacheSize
     */
    public void setCacheSize( int cacheSize ) {
        this.cacheSize = cacheSize;
    }

    /**
     * Returns the defaultOnlineResource.
     *
     * @return the URL
     */
    public OnlineResource getDefaultOnlineResource() {
        return defaultOnlineResource;
    }

    /**
     * Sets the defaultOnlineResource.
     *
     * @param defaultOnlineResource
     */
    public void setDefaultOnlineResource( OnlineResource defaultOnlineResource ) {
        this.defaultOnlineResource = defaultOnlineResource;
    }

    /**
     * @return the requestTimeLimit, in milliseconds.
     */
    public int getRequestTimeLimit() {
        return requestTimeLimit;
    }

    /**
     * Sets the requestTimeLimit.
     *
     * @param requestTimeLimit
     *
     */
    public void setRequestTimeLimit( int requestTimeLimit ) {
        this.requestTimeLimit = requestTimeLimit;
    }

    /**
     * Returns the characterSet.
     *
     * @return the charset
     *
     */
    public String getCharacterSet() {
        return characterSet.displayName();
    }

    /**
     * @return the Charset requested by the deegreeparams.
     */
    public Charset getCharset() {
        return characterSet;
    }

    /**
     * Sets the characterSet.
     *
     * @param characterSet
     * @throws InvalidFormatException
     *
     */
    public void setCharacterSet( String characterSet )
                            throws InvalidFormatException {
        if ( Charset.isSupported( characterSet ) ) {
            this.characterSet = Charset.forName( characterSet );
        } else {
            throw new InvalidFormatException( "DeegreeParams: The given charset is not supported by the jvm" );
        }
    }

}

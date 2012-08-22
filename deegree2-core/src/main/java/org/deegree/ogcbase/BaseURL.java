//$HeadURL$
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
package org.deegree.ogcbase;

import java.net.URL;

/**
 * The address is represented by the &lt;onlineResource&gt; element.
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @version 2002-03-01, $Revision$, $Date$
 * @since 1.0
 */
public class BaseURL {

    private String format = null;

    private URL onlineResource = null;

    /**
     * constructor initializing the class with the &lt;BaseURL&gt;
     *
     * @param format
     * @param onlineResource
     */
    public BaseURL( String format, URL onlineResource ) {
        setFormat( format );
        setOnlineResource( onlineResource );
    }

    /**
     * returns the MIME type of the resource
     *
     * @return the MIME type of the resource
     */
    public String getFormat() {
        return format;
    }

    /**
     * sets the MIME type of the resource
     *
     * @param format
     *
     */
    public void setFormat( String format ) {
        this.format = format;
    }

    /**
     * returns the address (URL) of the resource
     *
     * @return the address (URL) of the resource
     */
    public URL getOnlineResource() {
        return onlineResource;
    }

    /**
     * returns the address (URL) of the resource
     *
     * @param onlineResource
     */
    public void setOnlineResource( URL onlineResource ) {
        this.onlineResource = onlineResource;
    }

    @Override
    public String toString() {
        String ret = null;
        ret = "format = " + format + "\n";
        ret += ( "onlineResource = " + onlineResource + "\n" );
        return ret;
    }
}

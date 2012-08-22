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

package org.deegree.ogcwebservices.wass.wss.operation;

import java.io.InputStream;

import org.apache.commons.httpclient.Header;

/**
 * A <code>DoServiceResponse</code> class encapsulates all the relevant data a hiddenservice
 * responded to the clients DoService request, which was forwarded by this wss - proxy.<br/>
 *
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class DoServiceResponse {
    private Header[] headers = null;

    private Header[] footers = null;

    private InputStream responseBody = null;

    /**
     * @param headers
     * @param footers
     * @param responseBody
     */
    public DoServiceResponse( Header[] headers, InputStream responseBody, Header[] footers ) {
        this.headers = headers;
        this.footers = footers;
        this.responseBody = responseBody;
    }

    /**
     * @return Returns the footers.
     */
    public Header[] getFooters() {
        return footers;
    }

    /**
     * @return Returns the headers.
     */
    public Header[] getHeaders() {
        return headers;
    }

    /**
     * @return Returns the responseBody.
     */
    public InputStream getResponseBody() {
        return responseBody;
    }

}

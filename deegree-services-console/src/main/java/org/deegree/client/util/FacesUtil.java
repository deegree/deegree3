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
package org.deegree.client.util;

import static org.slf4j.LoggerFactory.getLogger;

import java.net.MalformedURLException;
import java.net.URL;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.slf4j.Logger;

/**
 * <code>FacesUtil</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FacesUtil {

    private static final Logger LOG = getLogger( FacesUtil.class );

    /**
     * @return the current URL, or null if an error occurred. Example: http://localhost:8080/context/
     */
    public static String getServerURL() {
        ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();
        URL url;
        try {
            url = new URL( ctx.getRequestScheme(), ctx.getRequestServerName(), ctx.getRequestServerPort(),
                           ctx.getRequestContextPath() );
            return url.toExternalForm() + "/";
        } catch ( MalformedURLException e ) {
            LOG.debug( "Constructing the url was a problem..." );
            LOG.trace( "Stack trace:", e );
        }
        return null;
    }

}

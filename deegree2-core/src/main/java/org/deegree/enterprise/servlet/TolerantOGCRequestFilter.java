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

package org.deegree.enterprise.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * <code>TolerantOGCRequestFilter</code> currently implements the following relaxations:
 *
 * <ul>
 * <li>format is not required for WMS GetFeatureInfo requests</li>
 * <li>version can be 1.0 for WFS requests (and will be interpreted as 1.0.0)</li>
 * </ul>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class TolerantOGCRequestFilter implements Filter {

    public void destroy() {
        // nothing to destroy
    }

    public void doFilter( ServletRequest request, ServletResponse response, FilterChain chain )
                            throws IOException, ServletException {
        Map<?, ?> map = request.getParameterMap();
        final Map<String, String[]> newMap = new HashMap<String, String[]>();

        for ( Object key : map.keySet() ) {
            newMap.put( key.toString().toUpperCase(), (String[]) map.get( key ) );
        }

        if ( newMap.get( "REQUEST" ) != null && newMap.get( "REQUEST" )[0].equals( "GetFeatureInfo" ) ) {
            if ( !newMap.containsKey( "FORMAT" ) ) {
                newMap.put( "format", new String[] { "image/png" } );
            }
        }

        if ( newMap.get( "SERVICE" ) != null && newMap.get( "SERVICE" )[0].equals( "WFS" ) ) {
            if ( newMap.get( "VERSION" )[0].equals( "1.0" ) ) {
                newMap.put( "VERSION", new String[] { "1.0.0" } );
            }
        }

        chain.doFilter( new HttpServletRequestWrapper( (HttpServletRequest) request ) {
            @Override
            public Map<?, ?> getParameterMap() {
                return newMap;
            }
        }, response );
    }

    public void init( FilterConfig conf )
                            throws ServletException {
        // no configuration is required
    }

}

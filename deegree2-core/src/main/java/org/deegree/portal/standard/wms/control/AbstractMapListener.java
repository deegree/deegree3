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
package org.deegree.portal.standard.wms.control;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.portal.context.ViewContext;

/**
 * Basic class for all listerens that shall be notified if a map cliented raises an action/event.
 * <p>
 * </p>
 * There are several predefined listeres for actions that most map clients support:
 * <ul>
 * <li><tt>ZoomInListener</tt> for handling zoomin actions. supported are zooming via point or vie rectangle.
 * <li><tt>ZoomOutListener</tt> for handling zoomout action.
 * <li><tt>PanListener</tt> for handling of pan action. supported is panning to eight directions.
 * <li><tt>RecenterListener</tt> recenters the map to a specified point. This can be interpreted as a special versio
 * of zooming
 * <li><tt>RefreshListener</tt> reloads the map without any change
 * <li><tt>ResetListener</tt> recovers the initial status of the map
 * <li><tt>InfoListener</tt> will be notified if a feature info request should be send.
 * </ul>
 * The user can additional listeners/action by extending the <tt>AbstractActionListener</tt> class or one of the
 * predefined listener.
 * <p>
 * </p>
 * Each Listerner have to be registered to the <tt>MapListener</tt> which is the class that will be informed about
 * each event/action within a map client. To register a class as listener it has to stored within the
 * MapListener.ConfigurationFile.
 *
 * <p>
 * ---------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: mays$
 *
 * @version $Revision$ $Date$
 */
abstract class AbstractMapListener extends AbstractListener {

    /**
     * @param event
     */
    @Override
    public void actionPerformed( FormEvent event ) {

        HttpSession session = ( (HttpServletRequest) getRequest() ).getSession();
        // get configuration from the users session
        ViewContext vc = (ViewContext) session.getAttribute( "DefaultMapContext" );

        this.getRequest().setAttribute( "MapContext", vc );

    }

    /**
     * maps a string representation of a request to a <tt>HashMap</tt>
     */
    protected HashMap<String, String> toMap( String request ) {
        int p = request.indexOf( '?' );
        if ( p >= 0 ) {
            request = request.substring( p + 1, request.length() );
        }
        StringTokenizer st = new StringTokenizer( request, "&" );
        HashMap<String, String> map = new HashMap<String, String>();

        while ( st.hasMoreTokens() ) {
            String s = st.nextToken();
            int pos = s.indexOf( '=' );
            String s1 = s.substring( 0, pos );
            String s2 = s.substring( pos + 1, s.length() );
            try {
                map.put( s1.toUpperCase(), URLDecoder.decode( s2, CharsetUtils.getSystemCharset() ) );
            } catch ( UnsupportedEncodingException e ) {
                e.printStackTrace();
            }
        }

        return map;
    }

    /**
     * the method returns the scale of the map defined as diagonal size of a pixel at the center of the map.
     */
    protected double getScale( GetMap mrm ) {
        double minx = mrm.getBoundingBox().getMin().getX();
        double maxx = mrm.getBoundingBox().getMax().getX();
        double miny = mrm.getBoundingBox().getMin().getY();
        double maxy = mrm.getBoundingBox().getMax().getY();
        double width = mrm.getWidth();
        double height = mrm.getHeight();

        double sx = Math.sqrt( Math.pow( maxx - minx, 2 ) + Math.pow( maxy - miny, 2 ) );
        double px = Math.sqrt( width * width + height * height );

        return sx / px;
    }
}

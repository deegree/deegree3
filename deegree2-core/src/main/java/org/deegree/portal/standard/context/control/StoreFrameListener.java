//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.portal.standard.context.control;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.deegree.enterprise.control.ajax.AbstractListener;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.portal.Constants;
import org.deegree.portal.context.Frontend;
import org.deegree.portal.context.GUIArea;
import org.deegree.portal.context.ViewContext;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class StoreFrameListener extends AbstractListener {
    
    private static final ILogger LOG = LoggerFactory.getLogger( StoreFrameListener.class );

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.enterprise.control.ajax.AbstractListener#actionPerformed(org.deegree.enterprise.control.ajax.WebEvent
     * , org.deegree.enterprise.control.ajax.ResponseHandler)
     */
    @SuppressWarnings("unchecked")
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {
        HttpSession session = event.getSession();
        ViewContext vc = (ViewContext) session.getAttribute( Constants.CURRENTMAPCONTEXT );
        Map<String, Object> parameter = event.getParameter();
        LOG.logDebug( "parameter: ", parameter );
        int width = ( (Number) parameter.get( "width" ) ).intValue();
        int height = ( (Number) parameter.get( "height" ) ).intValue();
        int top = ( (Number) parameter.get( "top" ) ).intValue();
        int left = ( (Number) parameter.get( "left" ) ).intValue();
        boolean visible = "true".equalsIgnoreCase( parameter.get( "visible" ).toString() );
        String aera = (String) parameter.get( "area" );

        Frontend frontend = vc.getGeneral().getExtension().getFrontend();
        GUIArea gui = null;
        if ( "west".equalsIgnoreCase( aera ) ) {
            gui = frontend.getEast();
        } else if ( "east".equalsIgnoreCase( aera ) ) {
            gui = frontend.getEast();
        } else if ( "south".equalsIgnoreCase( aera ) ) {
            gui = frontend.getEast();
        } else if ( "north".equalsIgnoreCase( aera ) ) {
            gui = frontend.getEast();
        } else if ( "center".equalsIgnoreCase( aera ) ) {
            gui = frontend.getEast();
        }
        gui.setHeight( height );
        gui.setWidth( width );
        gui.setLeft( left );
        gui.setTop( top );
        gui.setHidden( !visible );
    }

}

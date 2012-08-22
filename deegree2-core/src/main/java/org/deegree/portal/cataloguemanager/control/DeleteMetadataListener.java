//$HeadURL: 
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
package org.deegree.portal.cataloguemanager.control;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpMethod;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.FileUtils;
import org.deegree.framework.util.HttpUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.portal.cataloguemanager.model.ExceptionBean;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DeleteMetadataListener extends AbstractMetadataListener {

    private static final ILogger LOG = LoggerFactory.getLogger( DeleteMetadataListener.class );

    @SuppressWarnings("unchecked")
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {

        try {
            CatalogueManagerConfiguration conf = getCatalogueManagerConfiguration( event );
            String id = (String) event.getParameter().get( "ID" );

            URL url = getClass().getResource( "/org/deegree/portal/cataloguemanager/control/resources/delete.xml" );
            String s = FileUtils.readTextFile( url ).toString();
            s = StringTools.replace( s, "$id$", id, false );
            XMLFragment xml = new XMLFragment();
            xml.load( new StringReader( s ), XMLFragment.DEFAULT_URL );

            Enumeration<String> en = ( (HttpServletRequest) getRequest() ).getHeaderNames();
            Map<String, String> map = new HashMap<String, String>();
            while ( en.hasMoreElements() ) {
                String name = (String) en.nextElement();
                if ( !name.equalsIgnoreCase( "accept-encoding" ) && !name.equalsIgnoreCase( "content-length" )
                     && !name.equalsIgnoreCase( "user-agent" ) ) {
                    map.put( name, ( (HttpServletRequest) getRequest() ).getHeader( name ) );
                }
            }
            HttpMethod post = HttpUtils.performHttpPost( conf.getCatalogueURL(), xml, 60000, null, null, map );
            s = post.getResponseBodyAsString();
            if ( s.toLowerCase().indexOf( "exception" ) > -1 ) {
                ExceptionBean eb = new ExceptionBean( getClass().getName(), "delete failed" );
                responseHandler.writeAndClose( true, eb );
            } else {
                responseHandler.writeAndClose( "insert performed" );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            ExceptionBean eb = new ExceptionBean( getClass().getName(), e.getMessage() );
            responseHandler.writeAndClose( true, eb );
            return;
        }

    }

}

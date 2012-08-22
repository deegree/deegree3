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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.HttpUtils;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Element;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GetParentsListener extends AbstractMetadataListener {

    private static final ILogger LOG = LoggerFactory.getLogger( GetParentsListener.class );

    private static NamespaceContext nsc = CommonNamespaces.getNamespaceContext();

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.portal.cataloguemanager.control.AbstractMetadataListener#actionPerformed(
     * org.deegree.enterprise.control.ajax.WebEvent, org.deegree.portal.cataloguemanager.control.ResponseHandler)
     */
    @SuppressWarnings("unchecked")
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {
        List<String> parents = new ArrayList<String>();
        CatalogueManagerConfiguration config = getCatalogueManagerConfiguration( event );
        String addr = config.getCatalogueURL();
        URL url = getClass().getResource( "/org/deegree/portal/cataloguemanager/control/resources/GetRecordsSeries.xml" );
        // read all metadata series from a CSW
        LOG.logInfo( "CSW Address: ", addr );
        Enumeration<String> en = ( (HttpServletRequest) getRequest() ).getHeaderNames();
        Map<String, String> map = new HashMap<String, String>();
        while ( en.hasMoreElements() ) {
            String name = (String) en.nextElement();
            if ( !name.equalsIgnoreCase( "accept-encoding" ) && !name.equalsIgnoreCase( "content-length" )
                 && !name.equalsIgnoreCase( "user-agent" ) ) {
                map.put( name, ( (HttpServletRequest) getRequest() ).getHeader( name ) );
            }
        }
        InputStream is = HttpUtils.performHttpPost( addr, url.openStream(), 60000, null, null, "text/xml", null, map ).getResponseBodyAsStream();
        try {
            XMLFragment xml = new XMLFragment();
            xml.load( is, addr );
            String xpath = "./csw202:SearchResults/gmd:MD_Metadata";
            List<Element> mdSets = XMLTools.getElements( xml.getRootElement(), xpath, nsc );
            for ( Element element : mdSets ) {
                xpath = "./gmd:fileIdentifier/gco:CharacterString";
                String s1 = XMLTools.getNodeAsString( element, xpath, nsc, "" );
                xpath = "./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString";
                String s2 = XMLTools.getNodeAsString( element, xpath, nsc, "" );
                parents.add( s1 + ';' + s2 );
            }
        } catch ( Exception e ) {
            LOG.logError( e );
            throw new IOException( e.getMessage() );
        }
        responseHandler.writeAndClose( false, parents );
    }

}

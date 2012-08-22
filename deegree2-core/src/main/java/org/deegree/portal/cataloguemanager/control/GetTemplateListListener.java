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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GetTemplateListListener extends AbstractMetadataListener {

    private static final ILogger LOG = LoggerFactory.getLogger( GetMetadataListListener.class );

    private static NamespaceContext nsc = CommonNamespaces.getNamespaceContext();

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.portal.cataloguemanager.control.AbstractMetadataListener#actionPerformed(
     * org.deegree.enterprise.control.ajax.WebEvent, org.deegree.portal.cataloguemanager.control.ResponseHandler)
     */
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {
        List<String[]> rows = new ArrayList<String[]>();
        CatalogueManagerConfiguration config = getCatalogueManagerConfiguration( event );
        String templateDirectory = config.getTemplateDirectory();
        templateDirectory = event.getAbsolutePath( templateDirectory );
        File[] templates = new File( templateDirectory ).listFiles( new FilenameFilter() {

            public boolean accept( File dir, String name ) {
                return name != null && name.toLowerCase().endsWith( ".xml" );
            }
        } );

        try {
            for ( File file : templates ) {
                XMLFragment xml = new XMLFragment( file.toURL() );
                String[] s = new String[3];
                s[0] = file.getAbsolutePath();
                String xpath = "./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString";
                s[1] = XMLTools.getNodeAsString( xml.getRootElement(), xpath, nsc, "" );
                xpath = "./gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString";
                s[2] = XMLTools.getNodeAsString( xml.getRootElement(), xpath, nsc, "" );
                rows.add( s );

            }
        } catch ( Exception e ) {
            LOG.logError( e );
            throw new IOException( e.getMessage() );
        }

        HttpServletRequest request = ( (HttpServletRequest) event.getSource() );
        request.setAttribute( "TEMPLATES", rows );

        try {
            HttpServletResponse resp = responseHandler.getHttpServletResponse();
            // result page uses UTF-8 encoding
            resp.setCharacterEncoding( "UTF-8" );
            request.getRequestDispatcher( '/' + getNextPage() ).forward( request, resp );
        } catch ( ServletException e ) {
            throw new IOException( e.getMessage() );
        }
    }

}

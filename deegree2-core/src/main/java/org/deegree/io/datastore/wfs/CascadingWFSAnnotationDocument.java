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

package org.deegree.io.datastore.wfs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.io.datastore.AnnotationDocument;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Handles the annotation parsing for datastores that cascade remote WFS.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class CascadingWFSAnnotationDocument extends AnnotationDocument {

    private static final long serialVersionUID = -2801053207484913910L;

    @Override
    public CascadingWFSDatastoreConfiguration parseDatastoreConfiguration()
                            throws XMLParsingException {
        Element appinfoElement = (Element) XMLTools.getRequiredNode( getRootElement(), "xs:annotation/xs:appinfo",
                                                                     nsContext );
        List<Node> list = XMLTools.getRequiredNodes( appinfoElement, "deegreewfs:Url", nsContext );

        WFSDescription[] wfs = new WFSDescription[list.size()];

        String tmp = null;
        try {
            for ( int i = 0; i < wfs.length; i++ ) {
                Node node = list.get( i );
                tmp = XMLTools.getStringValue( node );
                URL url = new URL( tmp );

                String xPath = "./@inFilter";
                XSLTDocument inFilter = readFilter( node, xPath );

                xPath = "./@outFilter";
                XSLTDocument outFilter = readFilter( node, xPath );

                xPath = "./@timeout";
                int timeout = XMLTools.getNodeAsInt( node , xPath, nsContext, 5000 );

                wfs[i] = new WFSDescription( url, inFilter, outFilter, timeout );
            }
        } catch ( MalformedURLException e ) {
            throw new XMLParsingException( tmp + " is not a valid URL.", e );
        }
        return new CascadingWFSDatastoreConfiguration( wfs );
    }

    /**
     * reads and parses optional filter attributes for a WFS
     *
     * @param node
     * @param xPath
     * @return XSLT script if a filter as been defined otherwise <code>null</code>
     *          will be returned
     * @throws XMLParsingException
     */
    private XSLTDocument readFilter( Node node, String xPath )
                            throws XMLParsingException {

        XSLTDocument filter = null;
        String tmp = XMLTools.getNodeAsString( node , xPath, nsContext, null );
        try {
            if ( tmp != null ) {
                File file = new File( tmp );
                URL u = null;
                if ( !file.isAbsolute() ) {
                    u = this.resolve( tmp );
                }
                filter = new XSLTDocument(u);
            }
        } catch ( Exception e ) {
            throw new XMLParsingException( e.getMessage(), e );
        }
        return filter;
    }
}

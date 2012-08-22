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

package org.deegree.ogcwebservices.wmps.operation;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.deegree.framework.xml.XMLFragment;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.DefaultOGCWebServiceResponse;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * 
 * Represents an Initial Response document for a WMPS GetAvailableTemplatesResponse
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */

public class GetAvailableTemplatesResponseDocument extends DefaultOGCWebServiceResponse {

    //private static final ILogger LOG = LoggerFactory.getLogger( PrintMapResponseDocument.class );

    protected static final URI WMPSNS = CommonNamespaces.WMPSNS;

    private static final String XML_TEMPLATE = "WMPSGetAvailableTemplatesResponseTemplate.xml";

    private Element root;

    /**
     * @param request
     */
    public GetAvailableTemplatesResponseDocument( OGCWebServiceRequest request ) {
        super( request );
    }

    /**
     * Creates a skeleton response document that contains the mandatory elements only.
     *
     * @throws IOException
     * @throws SAXException
     */
    public void createEmptyDocument()
                            throws IOException, SAXException {

        URL url = GetAvailableTemplatesResponseDocument.class.getResource( XML_TEMPLATE );
        if ( url == null ) {
            throw new IOException( "The resource '" + XML_TEMPLATE + " could not be found." );
        }
        XMLFragment fragment = new XMLFragment();
        fragment.load( url );
        this.root = fragment.getRootElement();

    }

    /**
     * Get Root Element of the document.
     *
     * @return Element
     */
    public Element getRootElement() {
        return this.root;
    }

}

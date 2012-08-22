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
package org.deegree.ogcwebservices.wps.describeprocess;

import java.io.IOException;
import java.net.URL;

import org.deegree.ogcbase.OGCDocument;
import org.xml.sax.SAXException;

/**
 * ProcessDescriptionsDocument.java
 *
 * Created on 10.03.2006. 13:00:42h
 *
 * Creates an empty <code>WPSProcessDescriptions</code> Document for output as an describe process response.
 *
 *
 * @author <a href="mailto:christian@kiehle.org">Christian Kiehle</a>
 * @author <a href="mailto:christian.heier@gmx.de">Christian Heier</a>
 *
 * @version 1.0.
 *
 * @since 2.0
 */

public class ProcessDescriptionsDocument extends OGCDocument {

    /**
     *
     */
    private static final long serialVersionUID = 3087902818648682642L;

    private static final String XML_TEMPLATE = "ProcessDescriptionsTemplate.xml";

    /**
     * creates an empty document by loading the file in XML_TEMPLATE
     *
     * @throws IOException
     * @throws SAXException
     */
    public void createEmptyDocument()
                            throws IOException, SAXException {
        URL url = ProcessDescriptionsDocument.class.getResource( XML_TEMPLATE );
        if ( null == url ) {
            throw new IOException( "The resource '" + XML_TEMPLATE + " ' could not be found" );
        }
        load( url );
    }

}

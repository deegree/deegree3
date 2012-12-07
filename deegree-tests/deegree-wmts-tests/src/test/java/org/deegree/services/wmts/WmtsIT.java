//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wmts;

import java.io.IOException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests requests which yield XML responses.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class WmtsIT {
    private static final String base = "http://localhost:" + System.getProperty( "portnumber" )
                                       + "/deegree-wmts-tests/services";

    @Test
    public void testGetCapabilities()
                            throws XMLStreamException, IOException {
        String req = base + "?request=GetCapabilities&service=WMTS&version=1.0.0";
        XMLStreamReader in = XMLInputFactory.newInstance().createXMLStreamReader( new URL( req ).openStream() );
        boolean gfiFound = false;
        QName oper = new QName( "http://www.opengis.net/ows/1.1", "Operation" );
        while ( !gfiFound && in.hasNext() ) {
            in.next();
            if ( in.isStartElement() && in.getName().equals( oper ) ) {
                String val = in.getAttributeValue( null, "name" );
                gfiFound = val != null && val.equals( "GetFeatureInfo" );
            }
        }
        Assert.assertTrue( "No Operation element with name GetFeatureInfo found.", gfiFound );
    }

}

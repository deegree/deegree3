//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/feature/Feature.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.commons.xml;

import static javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for a bug in the StaX implementation shipped with the Oracle/Sun JDK.
 * <p>
 * The problem is that multiple calls to XMLStreamWriter#setPrefix(String,String) can cause a prefix to be bound more
 * than once (which results in invalid XML). Interestingly, one and two calls lead to a single binding, but three calls
 * cause a double binding. Current workaround is to use Woodstox.
 * </p>
 * 
 * @author <a href="mailto:name@deegree.org">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class StaXBugTest {

    @Test
    public void testForSetPrefixBug()
                            throws XMLStreamException, UnsupportedEncodingException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLOutputFactory of = XMLOutputFactory.newInstance();
        of.setProperty( IS_REPAIRING_NAMESPACES, true );
        XMLStreamWriter xmlStream = of.createXMLStreamWriter( bos );

        xmlStream.setPrefix( "ogc", "http://www.opengis.net/ogc" );
        xmlStream.setPrefix( "ogc", "http://www.opengis.net/ogc" );
        xmlStream.setPrefix( "ogc", "http://www.opengis.net/ogc" );

        xmlStream.writeStartElement( "A" );
        xmlStream.writeEndElement();
        xmlStream.close();
        String s = bos.toString( "UTF-8" );
        int n = 0;
        int pos = 0;
        while ( ( pos = s.indexOf( "xmlns:ogc", pos ) ) != -1 ) {
            n++;
            pos++;
        }
        Assert.assertTrue( n <= 1 );
    }
}
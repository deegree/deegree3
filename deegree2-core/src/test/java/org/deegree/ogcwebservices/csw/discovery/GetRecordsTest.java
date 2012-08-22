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


package org.deegree.ogcwebservices.csw.discovery;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.xml.sax.SAXException;

import alltests.Configuration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author: poth $
 *
 * @version. $Revision: 6251 $, $Date: 2007-03-19 16:59:28 +0100 (Mo, 19 Mrz 2007) $
 */
public class GetRecordsTest  extends TestCase {


    public static Test suite() {
        return new TestSuite( GetRecordsTest.class );
    }

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /*
     * @see TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetRecords1() throws IOException, SAXException, MissingParameterValueException, InvalidParameterValueException, OGCWebServiceException {
            URL url = Configuration.getCSWBaseDir();
            url = new URL( url.toExternalForm() + "example/deegree/dublincore/requests/GetRecords/xml/PropertyIsEqualToFull.xml" );
            XMLFragment xml = new XMLFragment( url );
            GetRecords gr = GetRecords.create(  "ww", xml.getRootElement() );
            QualifiedName qn = gr.getQuery().getTypeNamesAsList().get( 0 );
            assertEquals(  qn.getLocalName(), "dataset" );
            assertEquals(  qn.getPrefix(), "csw" );
            assertNull( qn.getNamespace() );
            assertNotNull(  gr.getQuery().getContraint() );
    }

}

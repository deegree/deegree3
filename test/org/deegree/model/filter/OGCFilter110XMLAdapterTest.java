//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2008 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/
package org.deegree.model.filter;


import java.io.IOException;
import java.net.URL;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * TODO add documentation here
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class OGCFilter110XMLAdapterTest {

    private URL filterURL;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
                            throws Exception {
        filterURL = OGCFilter110XMLAdapterTest.class.getResource( "testfilter_110.xml" );
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown()
                            throws Exception {
    }

    @Test    
    public void parseFilterDocument () throws IOException, XMLStreamException, FactoryConfigurationError {
        OGCFilter110XMLAdapter adapter = new OGCFilter110XMLAdapter ();
        adapter.load( filterURL );
        Filter filter = adapter.parse();
        System.out.println (filter);
    }

    @Test    
    public void parseAndExportFilterDocument () throws IOException, XMLStreamException, FactoryConfigurationError {

        OGCFilter110XMLAdapter adapter = new OGCFilter110XMLAdapter ();
        adapter.load( filterURL );
        Filter filter = adapter.parse();

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        factory.setProperty("javax.xml.stream.isRepairingNamespaces", Boolean.TRUE);        
        XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(System.out);

        OGCFilter110XMLAdapter.export( filter, xmlWriter );
        xmlWriter.flush();
    }    
}

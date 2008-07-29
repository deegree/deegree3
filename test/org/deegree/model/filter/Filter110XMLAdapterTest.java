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


import java.net.URL;

import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.junit.XMLAssert;
import org.deegree.junit.XMLMemoryStreamWriter;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;

/**
 * TODO add documentation here
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class Filter110XMLAdapterTest {

    @Test    
    public void parseFilterDocument() {
        Filter110XMLAdapter adapter = new Filter110XMLAdapter ();
        URL filterURL = Filter110XMLAdapterTest.class.getResource( "testfilter_110.xml" );
        adapter.load( filterURL );
        Filter filter = adapter.parse();
        Assert.assertNotNull (filter);
    }

    @Test(expected=XMLParsingException.class)    
    public void parseBrokenIdFilterDocument() {
        Filter110XMLAdapter adapter = new Filter110XMLAdapter ();
        URL filterURL = Filter110XMLAdapterTest.class.getResource( "testfilter_110_id_broken.xml" );
        adapter.load( filterURL );
        adapter.parse();
    }    

    @Test    
    public void parseAndExportFilterDocument()
                            throws XMLStreamException {

        Filter110XMLAdapter adapter = new Filter110XMLAdapter ();
        URL filterURL = Filter110XMLAdapterTest.class.getResource( "testfilter_110.xml" );
        adapter.load( filterURL );
        Filter filter = adapter.parse();

        XMLMemoryStreamWriter writer = new XMLMemoryStreamWriter();
        Filter110XMLAdapter.export( filter, writer.getXMLStreamWriter() );

        String schemaLocation = "file:///home/tonnhofer/workspace/opengis/filter/1.1.0/filter.xsd";
        XMLAssert.assertValidDocument( schemaLocation, new InputSource( writer.getReader() ) );
        
    }    
}

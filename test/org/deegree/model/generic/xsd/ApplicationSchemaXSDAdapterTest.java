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
package org.deegree.model.generic.xsd;

import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.deegree.model.generic.ApplicationSchema;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class ApplicationSchemaXSDAdapterTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp()
                            throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown()
                            throws Exception {
    }

    @Test
    public void parseSchema() throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        System.setProperty( DOMImplementationRegistry.PROPERTY, "org.apache.xerces.dom.DOMXSImplementationSourceImpl" );
        DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
        XSImplementation impl = (XSImplementation) registry.getDOMImplementation( "XS-Loader" );
        XSLoader schemaLoader = impl.createXSLoader( null );

//        String schemaURL = "file:///home/schneider/workspace/d3_commons/resources/model/examples/ipo/ipo.xsd";        
//      String schemaURL = "file:///home/schneider/workspace/d3_commons/resources/model/examples/imro2006/IMRO2006.xsd";
//      String schemaURL = "file:///home/schneider/workspace/d3_commons/resources/model/examples/imro2008/IMRO2008.xsd";
//        String schemaURL = "file:///home/schneider/workspace/d3_commons/resources/model/examples/philosopher/Philosopher.xsd";
//        String schemaURL = "file:///home/schneider/workspace/d3_commons/resources/model/examples/xplanung/XPlanGml.xsd";        
        String schemaURL = "file:///home/schneider/workspace/d3_commons/resources/model/examples/generic_xml/test.xsd";
        XSModel model = schemaLoader.loadURI( schemaURL );
        ApplicationSchemaXSDAdapter adapter = new ApplicationSchemaXSDAdapter (model);
        ApplicationSchema appSchema = adapter.parse();
        System.out.println (appSchema);
    }
}

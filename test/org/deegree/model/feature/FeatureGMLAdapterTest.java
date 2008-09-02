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
package org.deegree.model.feature;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.Assert;

import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.GenericFeatureType;
import org.deegree.model.feature.schema.GeometryPropertyDeclaration;
import org.deegree.model.feature.schema.PropertyDeclaration;
import org.deegree.model.feature.schema.SimplePropertyDeclaration;
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
public class FeatureGMLAdapterTest {
    
    private FeatureGMLAdapter adapter;
    
    @Before
    public void setUp() {

        // manually set up a simple Country feature type                
        List<PropertyDeclaration> propDecls = new ArrayList<PropertyDeclaration>();
        propDecls.add(new SimplePropertyDeclaration (new QName ("http://www.deegree.org/app", "name"), 1, 1, new QName ("http://www.w3.org/2001/XMLSchema", "string")));
        propDecls.add(new GeometryPropertyDeclaration (new QName ("http://www.deegree.org/app", "boundary"), 1, 1, new QName ("http://www.opengis.net", "MultiSurfacePropertyType")));        
        
        FeatureType ft = new GenericFeatureType (new QName ("http://www.deegree.org/app", "Country"), propDecls );
        List<FeatureType> fts = new ArrayList<FeatureType>();
        fts.add(ft);

        adapter = new FeatureGMLAdapter(fts);
    }    
    
    @Test
    public void testGenericFeatureParsing () throws XMLStreamException, FactoryConfigurationError, IOException { 

        URL docURL = FeatureGMLAdapterTest.class.getResource( "SimpleFeatureExample1.gml" );
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( docURL.toString(), docURL.openStream() );
        xmlReader.next();
        Feature feature = adapter.parseFeature(xmlReader, null);
        xmlReader.close();
        
        Assert.assertEquals (new QName ("http://www.deegree.org/app", "Country"), feature.getName());
        Assert.assertEquals ("COUNTRY_1", feature.getId());
        Assert.assertEquals (2, feature.getProperties().length);
        Assert.assertEquals ("France", feature.getProperties()[0].getValue());        
    }

//    public void testFeatureExport () throws XMLStreamException, FactoryConfigurationError, IOException {
//
//        XMLStreamWriter xmlWriter = new IndentingXMLStreamWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(System.out));
//        xmlWriter.setPrefix("app", "http://www.deegree.org/app");
//        adapter.export(xmlWriter, feature);
//        xmlWriter.flush();
//    }
}

//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.feature.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.Assert;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.memory.MemoryFeatureStore;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.IdFilter;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.primitive.Ring;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.ReferenceResolvingException;
import org.deegree.gml.feature.GMLFeatureReaderTest;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDDecoder;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.junit.Before;
import org.junit.Test;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class MemoryFeatureStoreTest {

    private static final String BASE_DIR = "../../gml/feature/testdata/features/";

    private MemoryFeatureStore store;

    @Before
    public void setUp()
                            throws XMLParsingException, XMLStreamException, UnknownCRSException,
                            FactoryConfigurationError, IOException, JAXBException, FeatureStoreException,
                            ReferenceResolvingException, ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException {

        String schemaURL = this.getClass().getResource( "/org/deegree/gml/feature/testdata/schema/Philosopher.xsd" ).toString();
        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, null, schemaURL );
        ApplicationSchema schema = adapter.extractFeatureTypeSchema();

        URL docURL = GMLFeatureReaderTest.class.getResource( BASE_DIR + "Philosopher_FeatureCollection.xml" );
        store = new MemoryFeatureStore( docURL, schema );
    }

    @Test
    public void testQueryAllPhilosophers()
                            throws FilterEvaluationException, FeatureStoreException {
        TypeName[] typeNames = new TypeName[] { new TypeName(
                                                              QName.valueOf( "{http://www.deegree.org/app}Philosopher" ),
                                                              null ) };
        Query query = new Query( typeNames, null, null, null, null );
        FeatureCollection fc = store.query( query ).toCollection();
        Assert.assertEquals( typeNames[0].getFeatureTypeName(), fc.iterator().next().getName() );
        Assert.assertEquals( 7, fc.size() );
    }

    @Test
    public void testQueryAllPlaces()
                            throws FilterEvaluationException, FeatureStoreException {
        TypeName[] typeNames = new TypeName[] { new TypeName( QName.valueOf( "{http://www.deegree.org/app}Place" ),
                                                              null ) };
        Query query = new Query( typeNames, null, null, null, null );
        FeatureCollection fc = store.query( query ).toCollection();
        Assert.assertEquals( typeNames[0].getFeatureTypeName(), fc.iterator().next().getName() );
        Assert.assertEquals( 7, fc.size() );
    }

    @Test
    public void testQueryAllCountries()
                            throws FilterEvaluationException, FeatureStoreException {
        TypeName[] typeNames = new TypeName[] { new TypeName( QName.valueOf( "{http://www.deegree.org/app}Country" ),
                                                              null ) };
        Query query = new Query( typeNames, null, null, null, null );
        FeatureCollection fc = store.query( query ).toCollection();
        Assert.assertEquals( typeNames[0].getFeatureTypeName(), fc.iterator().next().getName() );
        Assert.assertEquals( 4, fc.size() );
    }

    // @Test
    public void testQueryAllBooks()
                            throws FilterEvaluationException, FeatureStoreException {
        TypeName[] typeNames = new TypeName[] { new TypeName( QName.valueOf( "{http://www.deegree.org/app}Book" ), null ) };
        Query query = new Query( typeNames, null, null, null, null );
        FeatureCollection fc = store.query( query ).toCollection();
        Assert.assertEquals( typeNames[0].getFeatureTypeName(), fc.iterator().next().getName() );
        Assert.assertEquals( 1, fc.size() );
    }

    @Test
    public void testQueryPhilosopherById()
                            throws FilterEvaluationException, FeatureStoreException {
        TypeName[] typeNames = new TypeName[] { new TypeName(
                                                              QName.valueOf( "{http://www.deegree.org/app}Philosopher" ),
                                                              null ) };
        Filter filter = new IdFilter( "PHILOSOPHER_1", "PHILOSOPHER_2" );
        Query query = new Query( typeNames, filter, null, null, null );
        FeatureCollection fc = store.query( query ).toCollection();
        Assert.assertEquals( typeNames[0].getFeatureTypeName(), fc.iterator().next().getName() );
        Assert.assertEquals( 2, fc.size() );
    }

    @Test
    public void testGetObjectByIdFeature() {
        Object o = store.getObjectById( "PHILOSOPHER_7" );
        Assert.assertTrue( o instanceof Feature );
    }

    @Test
    public void testGetObjectByIdGeometry1() {
        Object o = store.getObjectById( "MULTIPOLYGON_1" );
        Assert.assertTrue( o instanceof Geometry );
    }

    @Test
    public void testGetObjectByIdGeometry2()
                            throws FileNotFoundException, XMLStreamException, UnknownCRSException,
                            TransformationException {
        Object o = store.getObjectById( "RING_1" );
        Assert.assertTrue( o instanceof Ring );

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", new Boolean( true ) );
        OutputStream out = new FileOutputStream( System.getProperty( "java.io.tmpdir" ) + File.separatorChar
                                                 + "exported_ring.gml" );
        XMLStreamWriter writer = outputFactory.createXMLStreamWriter( out );
        writer.setDefaultNamespace( "http://www.opengis.net/gml" );

        GMLStreamWriter gmlStream = GMLOutputFactory.createGMLStreamWriter( GMLVersion.GML_31, writer );
        gmlStream.write( (Geometry) o );
    }
}

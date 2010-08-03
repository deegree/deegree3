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
package org.deegree.feature.persistence.postgis;

import java.io.IOException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.xml.stax.FormattingXMLStreamWriter;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreManager;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.xml.Filter110XMLDecoder;
import org.deegree.geometry.Envelope;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.junit.Assert;
import org.junit.Test;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PostGISFeatureStoreTest {

    private static final boolean enable = false;

    @Test
    public void testInstantiation()
                            throws FeatureStoreException {

        if ( enable ) {
            ConnectionManager.addConnection( "philosopher-db", "jdbc:postgresql://hurricane:5432/deegreetest",
                                             "deegreetest", "deegreetest", 1, 10 );

            URL configURL = this.getClass().getResource( "philosopher.xml" );
            PostGISFeatureStore fs = (PostGISFeatureStore) FeatureStoreManager.create( configURL );
            fs.init();

            ApplicationSchema schema = fs.getSchema();
            Assert.assertEquals( 4, schema.getFeatureTypes().length );

            FeatureType ft = schema.getFeatureTypes()[0];
            System.out.println( ft );

            QName countryName = QName.valueOf( "{http://www.deegree.org/app}Country" );
            Envelope env = fs.getEnvelope( countryName );
            System.out.println( env );
        }
    }

    @Test
    public void testQueryCountry()
                            throws FeatureStoreException, FilterEvaluationException, XMLStreamException,
                            FactoryConfigurationError, UnknownCRSException, TransformationException {

        if ( enable ) {
            ConnectionManager.addConnection( "philosopher-db", "jdbc:postgresql://hurricane:5432/deegreetest",
                                             "postgres", "postgres", 1, 10 );

            URL configURL = this.getClass().getResource( "philosopher.xml" );
            PostGISFeatureStore fs = (PostGISFeatureStore) FeatureStoreManager.create( configURL );
            fs.init();

            TypeName[] typeNames = new TypeName[] { new TypeName(
                                                                  QName.valueOf( "{http://www.deegree.org/app}Country" ),
                                                                  null ) };
            Query query = new Query( typeNames, null, null, null, null );
            FeatureResultSet rs = fs.query( query );
            FeatureCollection fc = rs.toCollection();
            print( fc );
        }
    }

    @Test
    public void testQueryCountryWithFilter()
                            throws FeatureStoreException, FilterEvaluationException, XMLStreamException,
                            FactoryConfigurationError, UnknownCRSException, TransformationException {

        if ( enable ) {
            ConnectionManager.addConnection( "philosopher-db", "jdbc:postgresql://192.168.1.2:5432/deegreetest",
                                             "postgres", "postgres", 1, 10 );

            URL configURL = this.getClass().getResource( "philosopher.xml" );
            PostGISFeatureStore fs = (PostGISFeatureStore) FeatureStoreManager.create( configURL );
            fs.init();

            TypeName[] typeNames = new TypeName[] { new TypeName(
                                                                  QName.valueOf( "{http://www.deegree.org/app}Country" ),
                                                                  null ) };
            PropertyName propName = new PropertyName( QName.valueOf( "{http://www.deegree.org/app}name" ) );
            Literal literal = new Literal( "United Kingdom" );

            PropertyIsEqualTo propIsEqualTo = new PropertyIsEqualTo( propName, literal, false );
            Filter filter = new OperatorFilter( propIsEqualTo );
            Query query = new Query( typeNames, filter, null, null, null );
            FeatureResultSet rs = fs.query( query );
            try {
                FeatureCollection fc = rs.toCollection();
                XMLStreamWriter xmlStream = new FormattingXMLStreamWriter(
                                                                           XMLOutputFactory.newInstance().createXMLStreamWriter(
                                                                                                                                 System.out ) );
                GMLStreamWriter gmlStream = GMLOutputFactory.createGMLStreamWriter( GMLVersion.GML_31, xmlStream );
                gmlStream.write( fc );
                gmlStream.close();
            } finally {
                rs.close();
            }
        }
    }

    @Test
    public void testQueryPlace()
                            throws FeatureStoreException, FilterEvaluationException, XMLStreamException,
                            FactoryConfigurationError, UnknownCRSException, TransformationException {

        if ( enable ) {
            ConnectionManager.addConnection( "philosopher-db", "jdbc:postgresql://hurricane:5432/deegreetest",
                                             "deegreetest", "deegreetest", 1, 10 );

            URL configURL = this.getClass().getResource( "philosopher.xml" );
            PostGISFeatureStore fs = (PostGISFeatureStore) FeatureStoreManager.create( configURL );
            fs.init();

            TypeName[] typeNames = new TypeName[] { new TypeName( QName.valueOf( "{http://www.deegree.org/app}Place" ),
                                                                  null ) };
            Query query = new Query( typeNames, null, null, null, null );
            FeatureResultSet rs = fs.query( query );
            try {
                FeatureCollection fc = rs.toCollection();
                XMLStreamWriter xmlStream = new FormattingXMLStreamWriter(
                                                                           XMLOutputFactory.newInstance().createXMLStreamWriter(
                                                                                                                                 System.out ) );
                GMLStreamWriter gmlStream = GMLOutputFactory.createGMLStreamWriter( GMLVersion.GML_31, xmlStream );
                gmlStream.setLocalXLinkTemplate( "http://bla?fid={}" );
                gmlStream.setXLinkDepth( -1 );
                gmlStream.write( fc );
                gmlStream.close();
            } finally {
                rs.close();
            }
        }
    }

    @Test
    public void testQueryPhilosopher()
                            throws FeatureStoreException, FilterEvaluationException, XMLStreamException,
                            FactoryConfigurationError, UnknownCRSException, TransformationException, IOException {

        if ( enable ) {
            ConnectionManager.addConnection( "philosopher-db", "jdbc:postgresql://hurricane:5432/d3_philosopher",
                                             "postgres", "postgres", 1, 10 );

            URL configURL = this.getClass().getResource( "philosopher.xml" );
            PostGISFeatureStore fs = (PostGISFeatureStore) FeatureStoreManager.create( configURL );
            fs.init();

            Filter filter = parse( "filter1.xml" );
            System.out.println( filter );

            TypeName[] typeNames = new TypeName[] { new TypeName(
                                                                  QName.valueOf( "{http://www.deegree.org/app}Philosopher" ),
                                                                  null ) };
            Query query = new Query( typeNames, filter, null, null, null );
            FeatureResultSet rs = fs.query( query );
            FeatureCollection fc = rs.toCollection();

            print( fc );
        }
    }

    private void print( FeatureCollection fc )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        XMLStreamWriter xmlStream = new FormattingXMLStreamWriter(
                                                                   XMLOutputFactory.newInstance().createXMLStreamWriter(
                                                                                                                         System.out ) );
        GMLStreamWriter gmlStream = GMLOutputFactory.createGMLStreamWriter( GMLVersion.GML_31, xmlStream );
        gmlStream.setLocalXLinkTemplate( "http://bla?fid={}" );
        gmlStream.setXLinkDepth( -1 );
        gmlStream.write( fc );
        gmlStream.close();
    }

    private Filter parse( String resourceName )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        URL url = PostGISFeatureStoreTest.class.getResource( resourceName );
        XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( url.toString(),
                                                                                         url.openStream() );
        xmlStream.nextTag();
        return Filter110XMLDecoder.parse( xmlStream );
    }
}
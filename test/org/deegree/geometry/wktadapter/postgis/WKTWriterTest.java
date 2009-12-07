//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.geometry.wktadapter.postgis;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.jdbc.ConnectionManagerTest;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.WKTWriter;
import org.deegree.geometry.WKTWriterNG;
import org.deegree.geometry.Geometry.GeometryType;
import org.deegree.geometry.WKTWriterNG.WKTFlag;
import org.deegree.geometry.linearization.CurveLinearizer;
import org.deegree.geometry.linearization.LinearizationCriterion;
import org.deegree.geometry.linearization.NumPointsCriterion;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Curve;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.primitive.DefaultCurve;
import org.deegree.geometry.standard.primitive.DefaultLineString;
import org.deegree.gml.GMLDocumentIdContext;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.geometry.GML3GeometryDecoder;
import org.deegree.gml.geometry.GML3GeometryDecoderTest;
import org.junit.Test;

/**
 * Tests the correct syntax of the sql statement that should be dispatched against the postgis database
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class WKTWriterTest extends TestCase {

    private final String BASE_DIR = "../../geometry/gml/testdata/geometries/";
    
    
    //############################## USING DKT-FLAGS
    @Test
    public void test_PointDKT()
                            throws XMLParsingException, XMLStreamException, FactoryConfigurationError, IOException,
                            UnknownCRSException, SQLException, JAXBException {
        
        Set<WKTFlag> flag = new HashSet<WKTFlag>();
        flag.add( WKTFlag.USE_DKT );
        Writer writer = new StringWriter();
        WKTWriterNG WKTwriter = new WKTWriterNG( flag, writer );
        Geometry geom = parseGeometry( "Point_coord.gml" );
        WKTwriter.writeGeometry( geom, writer );
        System.out.print(writer.toString() + "\n");
        
            assertEquals("POINT [id='P1',metadataproperty=(),description='',name=()](7.12 50.72)", writer.toString());
        
    }
    
    @Test
    public void test_LineStringDKT()
                            throws XMLParsingException, XMLStreamException, FactoryConfigurationError, IOException,
                            UnknownCRSException, SQLException, JAXBException {
        
        Set<WKTFlag> flag = new HashSet<WKTFlag>();
        flag.add( WKTFlag.USE_DKT );
        Writer writer = new StringWriter();
        WKTWriterNG WKTwriter = new WKTWriterNG( flag, writer );
        Geometry geom = parseGeometry( "LineString_coord.gml" );
        WKTwriter.writeGeometry( geom, writer );
        System.out.print(writer.toString() + "\n");
        
            assertEquals("LINESTRING [id='L1',metadataproperty=(),description='',name=()](7.12 50.72,9.98 53.55,13.42 52.52)", writer.toString());
            
    }
    
    @Test
    public void test_LinearRingDKT()
                            throws XMLParsingException, XMLStreamException, FactoryConfigurationError, IOException,
                            UnknownCRSException, SQLException, JAXBException {
        
        Set<WKTFlag> flag = new HashSet<WKTFlag>();
        flag.add( WKTFlag.USE_DKT );
        Writer writer = new StringWriter();
        WKTWriterNG WKTwriter = new WKTWriterNG( flag, writer );
        Geometry geom = parseGeometry( "LinearRing.gml" );
        WKTwriter.writeGeometry( geom, writer );
        System.out.print("LinearRing in DKT "+writer.toString() + "\n");
        
            assertEquals("LINEARRING [id='',metadataproperty=(),description='',name=()](2.0 0.0,0.0 2.0,-2.0 0.0,-4.0 2.0,-6.0 0.0,0.0 10.0,2.0 0.0)", writer.toString());
            
    }
    
    @Test
    public void test_PolygonDKT()
                            throws XMLParsingException, XMLStreamException, FactoryConfigurationError, IOException,
                            UnknownCRSException, SQLException, JAXBException {
        
        Set<WKTFlag> flag = new HashSet<WKTFlag>();
        flag.add( WKTFlag.USE_DKT );
        Writer writer = new StringWriter();
        WKTWriterNG WKTwriter = new WKTWriterNG( flag, writer );
        Geometry geom = parseGeometry( "Polygon.gml" );
        WKTwriter.writeGeometry( geom, writer );
        System.out.print(writer.toString() + "\n");
        
            assertEquals("POLYGON [id='',metadataproperty=(),description='',name=()]((0.0 0.0,10.0 0.0,10.0 10.0,0.0 10.0,0.0 0.0),(1.0 9.0,1.0 9.5,2.0 9.5,2.0 9.0,1.0 9.0),(9.0 1.0,9.0 2.0,9.5 2.0,9.5 1.0,9.0 1.0))", writer.toString());
            
    }
    
    /*@Test
    public void test_CurveDKT()
                            throws XMLParsingException, XMLStreamException, FactoryConfigurationError, IOException,
                            UnknownCRSException, SQLException, JAXBException {
        
        Set<WKTFlag> flag = new HashSet<WKTFlag>();
        flag.add( WKTFlag.USE_DKT );
        Writer writer = new StringWriter();
        WKTWriterNG WKTwriter = new WKTWriterNG( flag, writer );
        Geometry geom = parseGeometry( "Curve.gml" );
        
        Curve c = (Curve)geom;
        
        Curve curve = new DefaultCurve(c.getId(), c.getCoordinateSystem(), c.getPrecision(), c.getCurveSegments());
        
        //TODO
        
        Points points = c.getControlPoints();
        double[] pointsArray = points.getAsArray();
        int len = pointsArray.length;
        String s = "";
        int counter = 0;
        int counterLen = 1;
        for(double p : pointsArray){
            counter++;
            counterLen++;
            s += p;
            if(points.getDimension()==2 && counter != 2){
                s += " ";
            }
            if(points.getDimension()==2 && counter == 2){
                if(counterLen < len){
                s += ",";
                counter = 0;
                }
            }
            if(points.getDimension()==3 && counter == 3){
                s += ",";
                counter = 0;
            }
            
            
        }
        
        WKTwriter.writeGeometry( geom, writer );
        System.out.print(writer.toString() + "\n");        
        
            assertEquals("CURVE [id='C1',metadataproperty=(),description='',name=()](" + s + ")", writer.toString());
        
    }*/
    
    /*@Test
    public void test_EnvelopeDKT()
                            throws XMLParsingException, XMLStreamException, FactoryConfigurationError, IOException,
                            UnknownCRSException, SQLException, JAXBException {
        
        Set<WKTFlag> flag = new HashSet<WKTFlag>();
        flag.add( WKTFlag.USE_DKT );
        Writer writer = new StringWriter();
        WKTWriterNG WKTwriter = new WKTWriterNG( flag, writer );
        Geometry geom = parseGeometry( "Envelope.gml" );
        WKTwriter.writeGeometry( geom, writer );
        System.out.print(writer.toString() + "\n");
        
            assertEquals("ENVELOPE [id='',metadataproperty=(),description='',name=()](11 22,44 88)", writer.toString());
        
    }*/
    
  //############################## USING OTHER FLAGS
    @Test
    public void test_LinearRing_FLAGLinearRing()
                            throws XMLParsingException, XMLStreamException, FactoryConfigurationError, IOException,
                            UnknownCRSException, SQLException, JAXBException {
        
        Set<WKTFlag> flag = new HashSet<WKTFlag>();
        flag.add( WKTFlag.USE_LINEARRING );
        Writer writer = new StringWriter();
        WKTWriterNG WKTwriter = new WKTWriterNG( flag, writer );
        Geometry geom = parseGeometry( "LinearRing.gml" );
        WKTwriter.writeGeometry( geom, writer );
        System.out.print("LinearRing as Flagged-LinearRing "+writer.toString() + "\n");
        
            assertEquals("LINEARRING (2.0 0.0,0.0 2.0,-2.0 0.0,-4.0 2.0,-6.0 0.0,0.0 10.0,2.0 0.0)", writer.toString());
            
    }
    
    /*@Test
    public void test_Envelope_FLAGEnvelope()
                            throws XMLParsingException, XMLStreamException, FactoryConfigurationError, IOException,
                            UnknownCRSException, SQLException, JAXBException {
        
        Set<WKTFlag> flag = new HashSet<WKTFlag>();
        flag.add( WKTFlag.USE_ENVELOPE );
        Writer writer = new StringWriter();
        WKTWriterNG WKTwriter = new WKTWriterNG( flag, writer );
        Geometry geom = parseGeometry( "Envelope.gml" );
        WKTwriter.writeGeometry( geom, writer );
        System.out.print(writer.toString() + "\n");
        
            assertEquals("ENVELOPE (11 22,44 88)", writer.toString());
        
    }*/
    
    //############################## USING NO FLAGS
    @Test
    public void test_Point()
                            throws XMLParsingException, XMLStreamException, FactoryConfigurationError, IOException,
                            UnknownCRSException, SQLException, JAXBException {
        
        Set<WKTFlag> flag = new HashSet<WKTFlag>();
        Writer writer = new StringWriter();
        WKTWriterNG WKTwriter = new WKTWriterNG( flag, writer );
        Geometry geom = parseGeometry( "Point_coord.gml" );
        WKTwriter.writeGeometry( geom, writer );
        System.out.print(writer.toString() + "\n");
        
            assertEquals("POINT (7.12 50.72)", writer.toString());
            
    }
    
    @Test
    public void test_LineString()
                            throws XMLParsingException, XMLStreamException, FactoryConfigurationError, IOException,
                            UnknownCRSException, SQLException, JAXBException {
        
        Set<WKTFlag> flag = new HashSet<WKTFlag>();
        Writer writer = new StringWriter();
        WKTWriterNG WKTwriter = new WKTWriterNG( flag, writer );
        Geometry geom = parseGeometry( "LineString_coord.gml" );
        WKTwriter.writeGeometry( geom, writer );
        System.out.print(writer.toString() + "\n");
        
            assertEquals("LINESTRING (7.12 50.72,9.98 53.55,13.42 52.52)", writer.toString());
            
    }
    
    @Test
    public void test_LinearRing()
                            throws XMLParsingException, XMLStreamException, FactoryConfigurationError, IOException,
                            UnknownCRSException, SQLException, JAXBException {
        
        Set<WKTFlag> flag = new HashSet<WKTFlag>();
        Writer writer = new StringWriter();
        WKTWriterNG WKTwriter = new WKTWriterNG( flag, writer );
        Geometry geom = parseGeometry( "LinearRing.gml" );
        WKTwriter.writeGeometry( geom, writer );
        System.out.print(writer.toString() + "\n");
        
            assertEquals("LINESTRING (2.0 0.0,0.0 2.0,-2.0 0.0,-4.0 2.0,-6.0 0.0,0.0 10.0,2.0 0.0)", writer.toString());
            
    }
    
    @Test
    public void test_Polygon()
                            throws XMLParsingException, XMLStreamException, FactoryConfigurationError, IOException,
                            UnknownCRSException, SQLException, JAXBException {
        
        Set<WKTFlag> flag = new HashSet<WKTFlag>();
        Writer writer = new StringWriter();
        WKTWriterNG WKTwriter = new WKTWriterNG( flag, writer );
        Geometry geom = parseGeometry( "Polygon.gml" );
        WKTwriter.writeGeometry( geom, writer );
        System.out.print(writer.toString() + "\n");
        
            assertEquals("POLYGON ((0.0 0.0,10.0 0.0,10.0 10.0,0.0 10.0,0.0 0.0),(1.0 9.0,1.0 9.5,2.0 9.5,2.0 9.0,1.0 9.0),(9.0 1.0,9.0 2.0,9.5 2.0,9.5 1.0,9.0 1.0))", writer.toString());
            
    }
    
    @Test
    public void test_Curve()
                            throws XMLParsingException, XMLStreamException, FactoryConfigurationError, IOException,
                            UnknownCRSException, SQLException, JAXBException {
        
        Set<WKTFlag> flag = new HashSet<WKTFlag>();
        Writer writer = new StringWriter();
        WKTWriterNG WKTwriter = new WKTWriterNG( flag, writer );
        Geometry geom = parseGeometry( "Curve.gml" );
        
        
        CurveLinearizer cl = new CurveLinearizer( new GeometryFactory() );
        //maybe a global setting??
        LinearizationCriterion crit = new NumPointsCriterion( 10 );
        Curve c = cl.linearize( (Curve) geom, crit );
        
        Points points = c.getControlPoints();
        double[] pointsArray = points.getAsArray();
        int len = pointsArray.length;
        String s = "";
        int counter = 0;
        int counterLen = 1;
        for(double p : pointsArray){
            counter++;
            counterLen++;
            s += p;
            if(points.getDimension()==2 && counter != 2){
                s += " ";
            }
            if(points.getDimension()==2 && counter == 2){
                if(counterLen < len){
                s += ",";
                counter = 0;
                }
            }
            if(points.getDimension()==3 && counter == 3){
                s += ",";
                counter = 0;
            }
            
            
        }
        
        WKTwriter.writeGeometry( geom, writer );
        System.out.print(writer.toString() + "\n");        
        
            assertEquals("LINESTRING (" + s + ")", writer.toString());
        
    }
    
    /*@Test
    public void test_Envelope()
                            throws XMLParsingException, XMLStreamException, FactoryConfigurationError, IOException,
                            UnknownCRSException, SQLException, JAXBException {
        
        Set<WKTFlag> flag = new HashSet<WKTFlag>();
        Writer writer = new StringWriter();
        WKTWriterNG WKTwriter = new WKTWriterNG( flag, writer );
        Geometry geom = parseGeometry( "Envelope.gml" );
        WKTwriter.writeGeometry( geom, writer );
        System.out.print(writer.toString() + "\n");
        
            assertEquals("POLYGON((11 22, 11 44, 44 88, 44 22, 11 22))", writer.toString());
        
    }*/
    
    
    //############################## PARSING THE GEOMETRY-FILE
    private Geometry parseGeometry( String fileName ) throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException, UnknownCRSException {

        URL gmlDocURL = GML3GeometryDecoderTest.class.getResource( BASE_DIR + fileName );
        GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31, gmlDocURL );
        return gmlReader.readGeometry();
    }
}

//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/test/org/deegree/geometry/GeometryTest.java $
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
package org.deegree.geometry;

import static junit.framework.Assert.assertTrue;

import java.io.FileWriter;
import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import junit.framework.Assert;

import org.deegree.commons.xml.stax.FormattingXMLStreamWriter;
import org.deegree.crs.CRS;
import org.deegree.crs.exceptions.TransformationException;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.geometry.primitive.LineString;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.points.PackedPoints;
import org.deegree.geometry.standard.primitive.DefaultPoint;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.geometry.GML3GeometryWriter;
import org.junit.Before;
import org.junit.Test;

/**
 * Basic test for some spatial analyis operations.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GeometryAnalysisTest {

    private static GeometryFactory geomFactory = new GeometryFactory();

    private static double DELTA = 0.00000001;

    private Point p1, p2, p3, p4;

    private LineString l1, l2, l3, l4;

    private Envelope env1, env2;

    /**
     * common envelopes as test geometry
     */
    @Before
    public void setUp() {

        CRS crs = new CRS( "EPSG:4326" );
        p1 = geomFactory.createPoint( "p1", 0.0, 0.0, crs );
        p2 = geomFactory.createPoint( "p2", 10.0, 10.0, crs );
        p3 = geomFactory.createPoint( "p3", 10.0, 10.0, crs );
        p4 = geomFactory.createPoint( "p4", 20.0, 20.0, crs );

        l1 = geomFactory.createLineString( "l1", crs,
                                           new PackedPoints( new double[] { 10.0, 5.0, 15.0, 9.0, 20.0, 20.0 }, 2 ) );
        l2 = geomFactory.createLineString( "l2", crs, new PackedPoints( new double[] { 15.0, 20.0, 15.0, 6.0 }, 2 ) );
        l3 = geomFactory.createLineString( "l3", crs, new PackedPoints( new double[] { 9.0, 9.0, 12.0, 5.0 }, 2 ) );
        l4 = geomFactory.createLineString( "l4", crs, new PackedPoints( new double[] { 0, 0, 1, 0 }, 2 ) );

        env1 = geomFactory.createEnvelope( 13.0, 7.0, 21.0, 21.0, crs );
    }

    @Test
    public void testIntersectionPointPoint() {
        Geometry result = p1.getIntersection( p1 );
        assertTrue( p1.equals( result ) );
    }

    @Test
    public void testIntersectionPointLineString() {
        Geometry result = p1.getIntersection( l1 );
        Assert.assertNull( result );

        result = p4.getIntersection( l1 );
        assertTrue( result instanceof Point );
        assertTrue( p4.equals( result ) );
    }

    @Test
    public void testIntersectionLineStringLineString() {
        Geometry result = l1.getIntersection( l1 );
        Assert.assertTrue( l1.equals( result ) );

        result = l2.getIntersection( l1 );
        Assert.assertTrue( result.equals( new DefaultPoint( null, new CRS( "EPSG:4326" ), null, new double[] { 15.0,
                                                                                                              9.0 } ) ) );

        result = l3.getIntersection( l4 );
        Assert.assertNull( result );
    }

    private void writeResult( Geometry result )
                            throws UnknownCRSException, TransformationException {
        try {
            XMLStreamWriter writer = new FormattingXMLStreamWriter(
                                                                    XMLOutputFactory.newInstance().createXMLStreamWriter(
                                                                                                                          new FileWriter(
                                                                                                                                          "/tmp/out.gml" ) ) );
            writer.setPrefix( "gml", "http://www.opengis.net/gml" );
            GMLStreamWriter gmlStream = GMLOutputFactory.createGMLStreamWriter( GMLVersion.GML_31, writer );
            gmlStream.write( result );
            writer.close();
        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( FactoryConfigurationError e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

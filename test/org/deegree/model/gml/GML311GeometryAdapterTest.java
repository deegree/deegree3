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
package org.deegree.model.gml;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.model.geometry.GeometryFactory;
import org.deegree.model.geometry.GeometryFactoryCreator;
import org.deegree.model.geometry.primitive.Curve;
import org.deegree.model.geometry.primitive.Point;
import org.deegree.model.geometry.primitive.CurveSegment.Interpolation;
import org.junit.Assert;
import org.junit.Test;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GML311GeometryAdapterTest {

    private static GeometryFactory geomFac = GeometryFactoryCreator.getInstance().getGeometryFactory();

    @Test
    public void parsePointPos()
                            throws XMLStreamException, FactoryConfigurationError, IOException {        
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryAdapterTest.class.getResource( "Point1_pos.gml" ) );
        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Point point = new GML311GeometryParser( geomFac, xmlReader ).parsePoint( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Assert.assertEquals( 7.12, point.getX() );
        Assert.assertEquals( 50.72, point.getY() );
        Assert.assertEquals( 2, point.getCoordinateDimension() );
        Assert.assertEquals( "EPSG:4326", point.getCoordinateSystem().getIdentifier() );
    }

    @Test
    public void parsePointCoordinates()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryAdapterTest.class.getResource( "Point1_coordinates.gml" ) );
        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Point point = new GML311GeometryParser( geomFac, xmlReader ).parsePoint( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Assert.assertEquals( 7.12, point.getX() );
        Assert.assertEquals( 50.72, point.getY() );
        Assert.assertEquals( 2, point.getCoordinateDimension() );
        Assert.assertEquals( "EPSG:4326", point.getCoordinateSystem().getIdentifier() );
    }

    @Test
    public void parsePointCoord()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryAdapterTest.class.getResource( "Point1_coord.gml" ) );
        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Point point = new GML311GeometryParser( geomFac, xmlReader ).parsePoint( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Point" ), xmlReader.getName() );
        Assert.assertEquals( 7.12, point.getX() );
        Assert.assertEquals( 50.72, point.getY() );
        Assert.assertEquals( 2, point.getCoordinateDimension() );
        Assert.assertEquals( "EPSG:4326", point.getCoordinateSystem().getIdentifier() );
    }

    @Test
    public void parseLineStringPos()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryAdapterTest.class.getResource( "LineString1_pos.gml" ) );
        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Curve curve = new GML311GeometryParser( geomFac, xmlReader ).parseLineString( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
    }

    @Test
    public void parseLineStringPosList()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryAdapterTest.class.getResource( "LineString1_posList.gml" ) );
        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Curve curve = new GML311GeometryParser( geomFac, xmlReader ).parseLineString( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Assert.assertEquals( 1, curve.getCurveSegments().size() );
        Assert.assertEquals( Interpolation.linear, curve.getCurveSegments().get( 0 ).getInterpolation() );
        Assert.assertEquals( 3, curve.getPoints().size() );
        Assert.assertEquals( 7.12, curve.getPoints().get( 0 ).getX() );
        Assert.assertEquals( 50.72, curve.getPoints().get( 0 ).getY() );
        Assert.assertEquals( 9.98, curve.getPoints().get( 1 ).getX() );
        Assert.assertEquals( 53.55, curve.getPoints().get( 1 ).getY() );
        Assert.assertEquals( 13.42, curve.getPoints().get( 2 ).getX() );
        Assert.assertEquals( 52.52, curve.getPoints().get( 2 ).getY() );
    }

    @Test
    public void parseLineStringCoordinates()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryAdapterTest.class.getResource( "LineString1_coordinates.gml" ) );
        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Curve curve = new GML311GeometryParser( geomFac, xmlReader ).parseLineString( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Assert.assertEquals( 1, curve.getCurveSegments().size() );
        Assert.assertEquals( Interpolation.linear, curve.getCurveSegments().get( 0 ).getInterpolation() );
        Assert.assertEquals( 3, curve.getPoints().size() );
        Assert.assertEquals( 7.12, curve.getPoints().get( 0 ).getX() );
        Assert.assertEquals( 50.72, curve.getPoints().get( 0 ).getY() );
        Assert.assertEquals( 9.98, curve.getPoints().get( 1 ).getX() );
        Assert.assertEquals( 53.55, curve.getPoints().get( 1 ).getY() );
        Assert.assertEquals( 13.42, curve.getPoints().get( 2 ).getX() );
        Assert.assertEquals( 52.52, curve.getPoints().get( 2 ).getY() );
    }

    @Test
    public void parseLineStringPointProperty()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryAdapterTest.class.getResource( "LineString1_pointProperty.gml" ) );
        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Curve curve = new GML311GeometryParser( geomFac, xmlReader ).parseLineString( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Assert.assertEquals( 1, curve.getCurveSegments().size() );
        Assert.assertEquals( Interpolation.linear, curve.getCurveSegments().get( 0 ).getInterpolation() );
        Assert.assertEquals( 3, curve.getPoints().size() );
        Assert.assertEquals( 7.12, curve.getPoints().get( 0 ).getX() );
        Assert.assertEquals( 50.72, curve.getPoints().get( 0 ).getY() );
        Assert.assertEquals( 9.98, curve.getPoints().get( 1 ).getX() );
        Assert.assertEquals( 53.55, curve.getPoints().get( 1 ).getY() );
        Assert.assertEquals( 13.42, curve.getPoints().get( 2 ).getX() );
        Assert.assertEquals( 52.52, curve.getPoints().get( 2 ).getY() );
    }

    @Test
    public void parseLineStringPointRep()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryAdapterTest.class.getResource( "LineString1_pointRep.gml" ) );

        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Curve curve = new GML311GeometryParser( geomFac, xmlReader ).parseLineString( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "LineString" ), xmlReader.getName() );
        Assert.assertEquals( 1, curve.getCurveSegments().size() );
        Assert.assertEquals( Interpolation.linear, curve.getCurveSegments().get( 0 ).getInterpolation() );
        Assert.assertEquals( 3, curve.getPoints().size() );
        Assert.assertEquals( 7.12, curve.getPoints().get( 0 ).getX() );
        Assert.assertEquals( 50.72, curve.getPoints().get( 0 ).getY() );
        Assert.assertEquals( 9.98, curve.getPoints().get( 1 ).getX() );
        Assert.assertEquals( 53.55, curve.getPoints().get( 1 ).getY() );
        Assert.assertEquals( 13.42, curve.getPoints().get( 2 ).getX() );
        Assert.assertEquals( 52.52, curve.getPoints().get( 2 ).getY() );
    }

    @Test
    public void parseCurve()
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper(
                                                                       GML311GeometryAdapterTest.class.getResource( "Curve.gml" ) );
        xmlReader.nextTag();
        Assert.assertEquals( XMLStreamConstants.START_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Curve" ), xmlReader.getName() );
        Curve curve = new GML311GeometryParser( geomFac, xmlReader ).parseCurve( null );
        Assert.assertEquals( XMLStreamConstants.END_ELEMENT, xmlReader.getEventType() );
        Assert.assertEquals( new QName( "http://www.opengis.net/gml", "Curve" ), xmlReader.getName() );
        Assert.assertEquals( 2, curve.getCurveSegments().size() );
        Assert.assertEquals( Interpolation.linear, curve.getCurveSegments().get( 0 ).getInterpolation() );
        Assert.assertEquals( Interpolation.linear, curve.getCurveSegments().get( 1 ).getInterpolation() );
    }
}

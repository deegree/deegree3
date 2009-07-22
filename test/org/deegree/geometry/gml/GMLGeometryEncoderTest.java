//$HeadURL: svn+ssh://aionita@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.geometry.gml;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.commons.xml.stax.XMLStreamWriterWrapper;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.gml.GMLIdContext;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.gml.GML311CurveSegmentDecoder;
import org.deegree.geometry.gml.GML311GeometryEncoder;
import org.deegree.geometry.gml.GML311GeometryDecoder;
import org.deegree.geometry.gml.GML311SurfacePatchDecoder;
import org.deegree.geometry.primitive.curvesegments.CurveSegment;
import org.deegree.geometry.primitive.surfacepatches.SurfacePatch;
import org.deegree.junit.XMLAssert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * Exporting all types of geometries and validating them.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author: ionita $
 * 
 * @version $Revision: $, $Date: $
 */
public class GMLGeometryEncoderTest {

    private static final Logger LOG = LoggerFactory.getLogger( GMLGeometryEncoderTest.class );

    final static String DIR = "testdata/geometries/";

    final static String PATCH_DIR = "testdata/patches/";

    final static String SEGMENT_DIR = "testdata/segments/";

    private static List<String> sources = new ArrayList<String>();

    private static List<String> patchSources = new ArrayList<String>();

    private static List<String> segmentSources = new ArrayList<String>();

    private static List<String> envelopeSources = new ArrayList<String>();

    final String SCHEMA_LOCATION_ATTRIBUTE = "http://www.opengis.net/gml http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";

    final String SCHEMA_LOCATION = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";

    static {
        sources.add( "CompositeCurve.gml" );
        sources.add( "CompositeSolid.gml" );
        sources.add( "CompositeSurface.gml" );
        sources.add( "Curve.gml" );
        sources.add( "GeometricComplex.gml" );
        sources.add( "LinearRing.gml" );
        sources.add( "LineString_coord.gml" );
        sources.add( "LineString_coordinates.gml" );
        sources.add( "LineString_pointProperty.gml" );
        sources.add( "LineString_pointRep.gml" );
        sources.add( "LineString_pos.gml" );
        sources.add( "LineString_posList.gml" );
        sources.add( "MultiCurve.gml" );
        sources.add( "MultiGeometry.gml" );
        sources.add( "MultiLineString.gml" );
        sources.add( "MultiPoint_members.gml" );
        sources.add( "MultiPolygon.gml" );
        sources.add( "MultiSolid.gml" );
        sources.add( "MultiSurface.gml" );
        sources.add( "OrientableCurve.gml" );
        sources.add( "OrientableSurface.gml" );
        sources.add( "Point_coord.gml" );
        sources.add( "Point_coordinates.gml" );
        sources.add( "Point_pos.gml" );
        sources.add( "Polygon.gml" );
        sources.add( "PolyhedralSurface.gml" );
        sources.add( "Ring.gml" );
        sources.add( "Solid.gml" );
        sources.add( "Surface.gml" );
        sources.add( "Tin.gml" );
        sources.add( "TriangulatedSurface.gml" );

        patchSources.add( "Cone.gml" );
        patchSources.add( "Cylinder.gml" );
        patchSources.add( "PolygonPatch.gml" );
        patchSources.add( "Rectangle.gml" );
        patchSources.add( "Sphere.gml" );
        patchSources.add( "Triangle.gml" );

        segmentSources.add( "Arc.gml" );
        segmentSources.add( "ArcByBulge.gml" );
        segmentSources.add( "ArcByCenterPoint.gml" );
        segmentSources.add( "ArcString.gml" );
        segmentSources.add( "ArcStringByBulge.gml" );
        segmentSources.add( "Bezier.gml" );
        segmentSources.add( "BSpline.gml" );
        segmentSources.add( "Circle.gml" );
        segmentSources.add( "CircleByCenterPoint.gml" );
        segmentSources.add( "Clothoid.gml" );
        segmentSources.add( "CubicSpline.gml" );
        segmentSources.add( "Geodesic.gml" );
        segmentSources.add( "GeodesicString.gml" );
        segmentSources.add( "LineStringSegment.gml" );

        envelopeSources.add( "Envelope_coord.gml" );
        envelopeSources.add( "Envelope_coordinates.gml" );
        envelopeSources.add( "Envelope_pos.gml" );
        envelopeSources.add( "Envelope.gml" );
    }

    @Test
    public void testValidatingExportedAbstractGeometryTypes()
                            throws XMLStreamException, XMLParsingException, UnknownCRSException,
                            FactoryConfigurationError, IOException {
        for ( String source : sources ) {
            LOG.info( "Exporting " + DIR + source );
            GMLIdContext idContext = new GMLIdContext();
            GML311GeometryDecoder parser = new GML311GeometryDecoder( new GeometryFactory(), idContext );
            URL docURL = GMLGeometryEncoderTest.class.getResource( DIR + source );
            XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( docURL );
            xmlReader.nextTag();
            Geometry geom = parser.parseAbstractGeometry( xmlReader, null );

            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", new Boolean( true ) );
            OutputStream out = new FileOutputStream( "/tmp/exported_" + source );
            XMLStreamWriterWrapper writer = new XMLStreamWriterWrapper( outputFactory.createXMLStreamWriter( out ),
                                                                        SCHEMA_LOCATION_ATTRIBUTE );
            writer.setPrefix( "app", "http://www.deegree.org/app" );
            writer.setPrefix( "gml", "http://www.opengis.net/gml" );
            writer.setPrefix( "ogc", "http://www.opengis.net/ogc" );
            writer.setPrefix( "wfs", "http://www.opengis.net/wfs" );
            writer.setPrefix( "xlink", "http://www.w3.org/1999/xlink" );
            writer.setPrefix( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
            GML311GeometryEncoder exporter = new GML311GeometryEncoder( writer );
            exporter.export( geom );
            writer.flush();
            writer.close();
            out.close();

            XMLAssert.assertValidDocument( SCHEMA_LOCATION,
                                           new InputSource( new FileReader( "/tmp/exported_" + source ) ) );
        }
    }

    @Test
    public void testValidatingExportedSurfacePatches()
                            throws XMLStreamException, XMLParsingException, UnknownCRSException,
                            FactoryConfigurationError, IOException {
        for ( String patchSource : patchSources ) {
            LOG.info( "Exporting " + PATCH_DIR + patchSource );
            GMLIdContext idContext = new GMLIdContext();
            GeometryFactory geomFactory = new GeometryFactory();
            GML311GeometryDecoder geometryParser = new GML311GeometryDecoder( geomFactory, idContext );
            GML311SurfacePatchDecoder parser = new GML311SurfacePatchDecoder( geometryParser, geomFactory );
            URL docURL = GMLGeometryEncoderTest.class.getResource( PATCH_DIR + patchSource );
            if ( docURL == null )
                System.out.println( GMLGeometryEncoderTest.class.getResource( PATCH_DIR + patchSource ) );
            XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( docURL );
            xmlReader.nextTag();
            SurfacePatch surfPatch = parser.parseSurfacePatch( xmlReader, null );

            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", new Boolean( true ) );
            OutputStream out = new FileOutputStream( "/tmp/exported_" + patchSource );
            XMLStreamWriterWrapper writer = new XMLStreamWriterWrapper( outputFactory.createXMLStreamWriter( out ),
                                                                        SCHEMA_LOCATION_ATTRIBUTE );
            writer.setPrefix( "app", "http://www.deegree.org/app" );
            writer.setPrefix( "gml", "http://www.opengis.net/gml" );
            writer.setPrefix( "ogc", "http://www.opengis.net/ogc" );
            writer.setPrefix( "wfs", "http://www.opengis.net/wfs" );
            writer.setPrefix( "xlink", "http://www.w3.org/1999/xlink" );
            writer.setPrefix( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
            GML311GeometryEncoder exporter = new GML311GeometryEncoder( writer );
            exporter.export( surfPatch );
            writer.flush();
            writer.close();
            out.close();

            XMLAssert.assertValidDocument( SCHEMA_LOCATION, new InputSource( new FileReader( "/tmp/exported_"
                                                                                             + patchSource ) ) );
        }
    }

    @Test
    public void testValidatingExportedCurveSegments()
                            throws XMLStreamException, XMLParsingException, UnknownCRSException,
                            FactoryConfigurationError, IOException {
        for ( String segmentSource : segmentSources ) {
            LOG.info( "Exporting " + SEGMENT_DIR + segmentSource );
            GMLIdContext idContext = new GMLIdContext();
            GeometryFactory geomFactory = new GeometryFactory();
            GML311GeometryDecoder geometryParser = new GML311GeometryDecoder( geomFactory, idContext );
            GML311CurveSegmentDecoder parser = new GML311CurveSegmentDecoder( geometryParser, geomFactory );
            URL docURL = GMLGeometryEncoderTest.class.getResource( SEGMENT_DIR + segmentSource );
            XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( docURL );
            xmlReader.nextTag();
            CurveSegment curveSegment = parser.parseCurveSegment( xmlReader, null );

            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", new Boolean( true ) );
            OutputStream out = new FileOutputStream( "/tmp/exported_" + segmentSource );
            XMLStreamWriterWrapper writer = new XMLStreamWriterWrapper( outputFactory.createXMLStreamWriter( out ),
                                                                        SCHEMA_LOCATION_ATTRIBUTE );
            writer.setPrefix( "app", "http://www.deegree.org/app" );
            writer.setPrefix( "gml", "http://www.opengis.net/gml" );
            writer.setPrefix( "ogc", "http://www.opengis.net/ogc" );
            writer.setPrefix( "wfs", "http://www.opengis.net/wfs" );
            writer.setPrefix( "xlink", "http://www.w3.org/1999/xlink" );
            writer.setPrefix( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
            GML311GeometryEncoder exporter = new GML311GeometryEncoder( writer );
            exporter.export( curveSegment );
            writer.flush();
            writer.close();
            out.close();

            XMLAssert.assertValidDocument( SCHEMA_LOCATION, new InputSource( new FileReader( "/tmp/exported_"
                                                                                             + segmentSource ) ) );
        }
    }

    @Test
    public void testValidatingExportedEnvelope()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException {
        for ( String envelopeSource : envelopeSources ) {
            LOG.info( "Exporting " + DIR + envelopeSource );
            GMLIdContext idContext = new GMLIdContext();
            GML311GeometryDecoder parser = new GML311GeometryDecoder( new GeometryFactory(), idContext );
            URL docURL = GMLGeometryEncoderTest.class.getResource( DIR + envelopeSource );
            XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( docURL );
            xmlReader.nextTag();
            Geometry geom = parser.parseEnvelope( xmlReader, null );

            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", new Boolean( true ) );
            OutputStream out = new FileOutputStream( "/tmp/exported_" + envelopeSource );
            XMLStreamWriterWrapper writer = new XMLStreamWriterWrapper( outputFactory.createXMLStreamWriter( out ),
                                                                        SCHEMA_LOCATION_ATTRIBUTE );
            writer.setPrefix( "app", "http://www.deegree.org/app" );
            writer.setPrefix( "gml", "http://www.opengis.net/gml" );
            writer.setPrefix( "ogc", "http://www.opengis.net/ogc" );
            writer.setPrefix( "wfs", "http://www.opengis.net/wfs" );
            writer.setPrefix( "xlink", "http://www.w3.org/1999/xlink" );
            writer.setPrefix( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
            GML311GeometryEncoder exporter = new GML311GeometryEncoder( writer );
            exporter.export( geom );
            writer.flush();
            writer.close();
            out.close();

            XMLAssert.assertValidDocument( SCHEMA_LOCATION, new InputSource( new FileReader( "/tmp/exported_"
                                                                                             + envelopeSource ) ) );
        }
    }

    @Test
    public void testValidatingExportedXLinkMultiGeometry1()
                            throws XMLParsingException, XMLStreamException, UnknownCRSException,
                            FactoryConfigurationError, IOException {
        String source = "XLinkMultiGeometry1.gml";
        LOG.info( "Exporting " + DIR + source );
        GMLIdContext idContext = new GMLIdContext();
        GML311GeometryDecoder parser = new GML311GeometryDecoder( new GeometryFactory(), idContext );
        URL docURL = GMLGeometryEncoderTest.class.getResource( DIR + source );
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( docURL );
        xmlReader.nextTag();
        Geometry geom = parser.parseMultiGeometry( xmlReader, null );

        idContext.resolveXLinks( null );

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", new Boolean( true ) );
        OutputStream out = new FileOutputStream( "/tmp/exported_" + source );
        XMLStreamWriterWrapper writer = new XMLStreamWriterWrapper( outputFactory.createXMLStreamWriter( out ),
                                                                    SCHEMA_LOCATION_ATTRIBUTE );
        writer.setPrefix( "app", "http://www.deegree.org/app" );
        writer.setPrefix( "gml", "http://www.opengis.net/gml" );
        writer.setPrefix( "ogc", "http://www.opengis.net/ogc" );
        writer.setPrefix( "wfs", "http://www.opengis.net/wfs" );
        writer.setPrefix( "xlink", "http://www.w3.org/1999/xlink" );
        writer.setPrefix( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
        GML311GeometryEncoder exporter = new GML311GeometryEncoder( writer );
        exporter.export( geom );
        writer.flush();
        writer.close();
        out.close();

        XMLAssert.assertValidDocument( SCHEMA_LOCATION, new InputSource( new FileReader( "/tmp/exported_" + source ) ) );
    }
}

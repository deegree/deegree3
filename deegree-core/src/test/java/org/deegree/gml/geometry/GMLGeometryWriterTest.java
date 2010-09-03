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

package org.deegree.gml.geometry;

import static org.deegree.gml.GMLVersion.GML_31;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.commons.xml.stax.XMLStreamWriterWrapper;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.patches.SurfacePatch;
import org.deegree.geometry.primitive.segments.CurveSegment;
import org.deegree.gml.GMLDocumentIdContext;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.ReferenceResolvingException;
import org.deegree.junit.XMLAssert;
import org.deegree.junit.XMLMemoryStreamWriter;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exporting all types of geometries and validating them.
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author: ionita $
 * 
 * @version $Revision: $, $Date: $
 */
public class GMLGeometryWriterTest {

    private static final Logger LOG = LoggerFactory.getLogger( GMLGeometryWriterTest.class );

    final static String DIR = "../../geometry/gml/testdata/geometries/";

    final static String PATCH_DIR = "../../geometry/gml/testdata/patches/";

    final static String SEGMENT_DIR = "../../geometry/gml/testdata/segments/";

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

    /**
     * @throws XMLStreamException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     * @throws FactoryConfigurationError
     * @throws IOException
     * @throws TransformationException
     */
    @Test
    public void testValidatingExportedAbstractGeometryTypes()
                            throws XMLStreamException, XMLParsingException, UnknownCRSException,
                            FactoryConfigurationError, IOException, TransformationException {
        for ( String source : sources ) {
            GMLDocumentIdContext idContext = new GMLDocumentIdContext( GMLVersion.GML_31 );
            GML3GeometryReader parser = new GML3GeometryReader( GMLVersion.GML_31, new GeometryFactory(), idContext, 2 );
            URL docURL = GMLGeometryWriterTest.class.getResource( DIR + source );
            XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( docURL );
            xmlReader.nextTag();
            Geometry geom = parser.parse( xmlReader, null );

            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", new Boolean( true ) );
            XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();

            XMLStreamWriterWrapper writer = new XMLStreamWriterWrapper( memoryWriter.getXMLStreamWriter(),
                                                                        SCHEMA_LOCATION_ATTRIBUTE );
            writer.setPrefix( "app", "http://www.deegree.org/app" );
            writer.setPrefix( "gml", "http://www.opengis.net/gml" );
            writer.setPrefix( "ogc", "http://www.opengis.net/ogc" );
            writer.setPrefix( "wfs", "http://www.opengis.net/wfs" );
            writer.setPrefix( "xlink", "http://www.w3.org/1999/xlink" );
            writer.setPrefix( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
            GML3GeometryWriter exporter = new GML3GeometryWriter( GML_31, writer, null, null, false,
                                                                  new HashSet<String>() );
            exporter.export( geom );
            writer.flush();

            XMLAssert.assertValidity( memoryWriter.getReader(), SCHEMA_LOCATION );
        }
    }

    /**
     * @throws XMLStreamException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     * @throws FactoryConfigurationError
     * @throws IOException
     * @throws TransformationException
     */
    @Test
    public void testValidatingExportedSurfacePatches()
                            throws XMLStreamException, XMLParsingException, UnknownCRSException,
                            FactoryConfigurationError, IOException, TransformationException {
        for ( String patchSource : patchSources ) {
            GMLDocumentIdContext idContext = new GMLDocumentIdContext( GMLVersion.GML_31 );
            GeometryFactory geomFactory = new GeometryFactory();
            GML3GeometryReader geometryParser = new GML3GeometryReader( GMLVersion.GML_31, geomFactory, idContext, 2 );
            GML3SurfacePatchReader parser = new GML3SurfacePatchReader( geometryParser, geomFactory, 2 );
            URL docURL = GMLGeometryWriterTest.class.getResource( PATCH_DIR + patchSource );
            if ( docURL == null )
                LOG.debug( "patch dir: " + GMLGeometryWriterTest.class.getResource( PATCH_DIR + patchSource ) );
            XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( docURL );
            xmlReader.nextTag();
            SurfacePatch surfPatch = parser.parseSurfacePatch( xmlReader, null );

            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", new Boolean( true ) );
            XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
            XMLStreamWriterWrapper writer = new XMLStreamWriterWrapper( memoryWriter.getXMLStreamWriter(),
                                                                        SCHEMA_LOCATION_ATTRIBUTE );
            writer.setPrefix( "app", "http://www.deegree.org/app" );
            writer.setPrefix( "gml", "http://www.opengis.net/gml" );
            writer.setPrefix( "ogc", "http://www.opengis.net/ogc" );
            writer.setPrefix( "wfs", "http://www.opengis.net/wfs" );
            writer.setPrefix( "xlink", "http://www.w3.org/1999/xlink" );
            writer.setPrefix( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
            GML3GeometryWriter exporter = new GML3GeometryWriter( GML_31, writer, null, null, false,
                                                                  new HashSet<String>() );
            ;
            exporter.exportSurfacePatch( surfPatch );
            writer.flush();

            XMLAssert.assertValidity( memoryWriter.getReader(), SCHEMA_LOCATION );
        }
    }

    /**
     * @throws XMLStreamException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     * @throws FactoryConfigurationError
     * @throws IOException
     * @throws TransformationException
     */
    @Test
    public void testValidatingExportedCurveSegments()
                            throws XMLStreamException, XMLParsingException, UnknownCRSException,
                            FactoryConfigurationError, IOException, TransformationException {
        for ( String segmentSource : segmentSources ) {
            GMLDocumentIdContext idContext = new GMLDocumentIdContext( GMLVersion.GML_31 );
            GeometryFactory geomFactory = new GeometryFactory();
            GML3GeometryReader geometryParser = new GML3GeometryReader( GMLVersion.GML_31, geomFactory, idContext, 2 );
            GML3CurveSegmentReader parser = new GML3CurveSegmentReader( geometryParser, geomFactory, 2 );
            URL docURL = GMLGeometryWriterTest.class.getResource( SEGMENT_DIR + segmentSource );
            XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( docURL );
            xmlReader.nextTag();
            CurveSegment curveSegment = parser.parseCurveSegment( xmlReader, null );

            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", new Boolean( true ) );
            XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
            XMLStreamWriterWrapper writer = new XMLStreamWriterWrapper( memoryWriter.getXMLStreamWriter(),
                                                                        SCHEMA_LOCATION_ATTRIBUTE );
            writer.setPrefix( "app", "http://www.deegree.org/app" );
            writer.setPrefix( "gml", "http://www.opengis.net/gml" );
            writer.setPrefix( "ogc", "http://www.opengis.net/ogc" );
            writer.setPrefix( "wfs", "http://www.opengis.net/wfs" );
            writer.setPrefix( "xlink", "http://www.w3.org/1999/xlink" );
            writer.setPrefix( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
            GML3GeometryWriter exporter = new GML3GeometryWriter( GML_31, writer, null, null, false,
                                                                  new HashSet<String>() );
            ;
            exporter.exportCurveSegment( curveSegment );
            writer.flush();

            XMLAssert.assertValidity( memoryWriter.getReader(), SCHEMA_LOCATION );
        }
    }

    /**
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     * @throws IOException
     * @throws XMLParsingException
     * @throws UnknownCRSException
     * @throws TransformationException
     */
    @Test
    public void testValidatingExportedEnvelope()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException, TransformationException {
        for ( String envelopeSource : envelopeSources ) {
            GMLDocumentIdContext idContext = new GMLDocumentIdContext( GMLVersion.GML_31 );
            GML3GeometryReader parser = new GML3GeometryReader( GMLVersion.GML_31, new GeometryFactory(), idContext, 2 );
            URL docURL = GMLGeometryWriterTest.class.getResource( DIR + envelopeSource );
            XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( docURL );
            xmlReader.nextTag();
            Geometry geom = parser.parseEnvelope( xmlReader, null );

            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", new Boolean( true ) );
            XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
            XMLStreamWriterWrapper writer = new XMLStreamWriterWrapper( memoryWriter.getXMLStreamWriter(),
                                                                        SCHEMA_LOCATION_ATTRIBUTE );
            writer.setPrefix( "app", "http://www.deegree.org/app" );
            writer.setPrefix( "gml", "http://www.opengis.net/gml" );
            writer.setPrefix( "ogc", "http://www.opengis.net/ogc" );
            writer.setPrefix( "wfs", "http://www.opengis.net/wfs" );
            writer.setPrefix( "xlink", "http://www.w3.org/1999/xlink" );
            writer.setPrefix( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
            GML3GeometryWriter exporter = new GML3GeometryWriter( GML_31, writer, null, null, false,
                                                                  new HashSet<String>() );
            exporter.export( geom );
            writer.flush();

            XMLAssert.assertValidity( memoryWriter.getReader(), SCHEMA_LOCATION );
        }
    }

    /**
     * @throws XMLParsingException
     * @throws XMLStreamException
     * @throws UnknownCRSException
     * @throws FactoryConfigurationError
     * @throws IOException
     * @throws TransformationException
     * @throws ReferenceResolvingException
     */
    @Test
    public void testValidatingExportedXLinkMultiGeometry1()
                            throws XMLParsingException, XMLStreamException, UnknownCRSException,
                            FactoryConfigurationError, IOException, TransformationException,
                            ReferenceResolvingException {

        String source = "XLinkMultiGeometry1.gml";
        GMLDocumentIdContext idContext = new GMLDocumentIdContext( GMLVersion.GML_31 );
        GML3GeometryReader parser = new GML3GeometryReader( GMLVersion.GML_31, new GeometryFactory(), idContext, 2 );
        URL docURL = GMLGeometryWriterTest.class.getResource( DIR + source );
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( docURL );
        xmlReader.nextTag();
        Geometry geom = parser.parseMultiGeometry( xmlReader, null );

        idContext.resolveLocalRefs();

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", new Boolean( true ) );
        XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
        XMLStreamWriterWrapper writer = new XMLStreamWriterWrapper( memoryWriter.getXMLStreamWriter(),
                                                                    SCHEMA_LOCATION_ATTRIBUTE );
        writer.setPrefix( "app", "http://www.deegree.org/app" );
        writer.setPrefix( "gml", "http://www.opengis.net/gml" );
        writer.setPrefix( "ogc", "http://www.opengis.net/ogc" );
        writer.setPrefix( "wfs", "http://www.opengis.net/wfs" );
        writer.setPrefix( "xlink", "http://www.w3.org/1999/xlink" );
        writer.setPrefix( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
        GML3GeometryWriter exporter = new GML3GeometryWriter( GML_31, writer, null, null, false, new HashSet<String>() );
        ;
        exporter.export( geom );
        writer.flush();

        XMLAssert.assertValidity( memoryWriter.getReader(), SCHEMA_LOCATION );
    }

    @Test
    public void testValidatingExportedXLinkMultiGeometry2()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException, TransformationException, ReferenceResolvingException {
        String source = "XLinkMultiGeometry2.gml";
        GMLDocumentIdContext idContext = new GMLDocumentIdContext( GMLVersion.GML_31 );
        GML3GeometryReader parser = new GML3GeometryReader( GMLVersion.GML_31, new GeometryFactory(), idContext, 2 );
        URL docURL = GMLGeometryWriterTest.class.getResource( DIR + source );
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( docURL );
        xmlReader.nextTag();
        Geometry geom = parser.parseMultiCurve( xmlReader, null );

        idContext.resolveLocalRefs();

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", new Boolean( true ) );
        XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
        XMLStreamWriterWrapper writer = new XMLStreamWriterWrapper( memoryWriter.getXMLStreamWriter(),
                                                                    SCHEMA_LOCATION_ATTRIBUTE );
        writer.setPrefix( "app", "http://www.deegree.org/app" );
        writer.setPrefix( "gml", "http://www.opengis.net/gml" );
        writer.setPrefix( "ogc", "http://www.opengis.net/ogc" );
        writer.setPrefix( "wfs", "http://www.opengis.net/wfs" );
        writer.setPrefix( "xlink", "http://www.w3.org/1999/xlink" );
        writer.setPrefix( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
        GML3GeometryWriter exporter = new GML3GeometryWriter( GML_31, writer, null, null, false, new HashSet<String>() );
        exporter.export( geom );
        writer.flush();

        XMLAssert.assertValidity( memoryWriter.getReader(), SCHEMA_LOCATION );

    }

    @Test
    public void testValidatingExportedXLinkMultiLineString()
                            throws XMLStreamException, FactoryConfigurationError, IOException, XMLParsingException,
                            UnknownCRSException, TransformationException, ReferenceResolvingException {
        String source = "XLinkMultiLineString.gml";
        GMLDocumentIdContext idContext = new GMLDocumentIdContext( GMLVersion.GML_31 );
        GML3GeometryReader parser = new GML3GeometryReader( GMLVersion.GML_31, new GeometryFactory(), idContext, 2 );
        URL docURL = GMLGeometryWriterTest.class.getResource( DIR + source );
        XMLStreamReaderWrapper xmlReader = new XMLStreamReaderWrapper( docURL );
        xmlReader.nextTag();
        Geometry geom = parser.parseMultiLineString( xmlReader, null );

        idContext.resolveLocalRefs();

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty( "javax.xml.stream.isRepairingNamespaces", new Boolean( true ) );
        XMLMemoryStreamWriter memoryWriter = new XMLMemoryStreamWriter();
        XMLStreamWriterWrapper writer = new XMLStreamWriterWrapper( memoryWriter.getXMLStreamWriter(),
                                                                    SCHEMA_LOCATION_ATTRIBUTE );
        writer.setPrefix( "app", "http://www.deegree.org/app" );
        writer.setPrefix( "gml", "http://www.opengis.net/gml" );
        writer.setPrefix( "ogc", "http://www.opengis.net/ogc" );
        writer.setPrefix( "wfs", "http://www.opengis.net/wfs" );
        writer.setPrefix( "xlink", "http://www.w3.org/1999/xlink" );
        writer.setPrefix( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
        GML3GeometryWriter exporter = new GML3GeometryWriter( GML_31, writer, null, null, false, new HashSet<String>() );
        ;
        exporter.export( geom );
        writer.flush();

        XMLAssert.assertValidity( memoryWriter.getReader(), SCHEMA_LOCATION );

    }

}

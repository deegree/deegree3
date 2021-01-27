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
package org.deegree.gml.schema;

import junit.framework.Assert;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSTypeDefinition;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.feature.types.AppSchema;
import org.deegree.gml.GMLVersion;
import org.junit.Test;
import org.slf4j.Logger;
import org.xmlmatchers.namespace.SimpleNamespaceContext;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;
import static org.xmlmatchers.XmlMatchers.hasXPath;
import static org.xmlmatchers.transform.XmlConverters.the;
import static org.xmlmatchers.xpath.XpathReturnType.returningANumber;

/**
 * TODO add documentation here
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 *
 * @version $Revision:$, $Date:$
 */
public class GMLAppSchemaWriterTest {

    private static final Logger LOG = getLogger( GMLAppSchemaWriterTest.class );

    @Test
    public void testPhilosopher()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        GMLSchemaInfoSet analyzer = new GMLSchemaInfoSet( null,
                                                          this.getClass().getResource( "../misc/schema/Philosopher.xsd" ).toString() );
        List<XSElementDeclaration> featureElementDecls = analyzer.getFeatureElementDeclarations( "http://www.deegree.org/app",
                                                                                                 false );
        for ( XSElementDeclaration featureElementDecl : featureElementDecls ) {
            LOG.debug( "- Feature type: " + featureElementDecl.getName() );
        }
        List<XSElementDeclaration> featureCollectionElementDecls = analyzer.getFeatureCollectionElementDeclarations( null,
                                                                                                                     false );
        for ( XSElementDeclaration featureCollectionElementDecl : featureCollectionElementDecls ) {
            LOG.debug( "- Feature collection type: " + featureCollectionElementDecl.getName() );
        }
        List<XSElementDeclaration> geometryElementDecls = analyzer.getGeometryElementDeclarations( null, true );
        for ( XSElementDeclaration geometryElementDecl : geometryElementDecls ) {
            LOG.debug( "- Geometry type: " + geometryElementDecl.getName() );
        }
        List<XSTypeDefinition> featureTypeDefinitions = analyzer.getFeatureTypeDefinitions( null, true );
        for ( XSTypeDefinition xsTypeDefinition : featureTypeDefinitions ) {
            LOG.debug( "- Feature type definition: " + xsTypeDefinition.getName() );
        }
    }

    @Test
    public void testPhilosopherAndWFS()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "../misc/schema/Philosopher.xsd" ).toString();
        String schemaURL2 = "http://schemas.opengis.net/wfs/1.1.0/wfs.xsd";
        GMLSchemaInfoSet analyzer = new GMLSchemaInfoSet( null, schemaURL2, schemaURL );
        Assert.assertEquals( 4, analyzer.getFeatureElementDeclarations( "http://www.deegree.org/app", true ).size() );
        Assert.assertEquals( 1, analyzer.getFeatureElementDeclarations( "http://www.opengis.net/wfs", true ).size() );
    }

    @Test
    public void testGML311AggregateElements()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";
        GMLSchemaInfoSet analyzer = new GMLSchemaInfoSet( null, schemaURL );
        Set<String> substitutionts = getConcreteSubstitutions( "_GeometricAggregate", analyzer );
        for ( String string : substitutionts ) {
            LOG.debug( string );
        }
        LOG.debug( "" + substitutionts.size() );
    }

    @Test
    public void testGML311PrimitiveElements()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";
        GMLSchemaInfoSet analyzer = new GMLSchemaInfoSet( null, schemaURL );
        Set<String> substitutionts = getConcreteSubstitutions( "_GeometricPrimitive", analyzer );
        for ( String string : substitutionts ) {
            LOG.debug( string );
        }
        LOG.debug( "" + substitutionts.size() );
    }

    @Test
    public void testGML311ImplicitGeometryElements()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";
        GMLSchemaInfoSet analyzer = new GMLSchemaInfoSet( null, schemaURL );
        Set<String> substitutionts = getConcreteSubstitutions( "_ImplicitGeometry", analyzer );
        for ( String string : substitutionts ) {
            LOG.debug( string );
        }
        LOG.debug( "" + substitutionts.size() );
    }

    @Test
    public void testGML311GeometryElements()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";
        GMLSchemaInfoSet analyzer = new GMLSchemaInfoSet( null, schemaURL );
        Set<String> substitutionts = getConcreteSubstitutions( "_Curve", analyzer );
        for ( String string : substitutionts ) {
            LOG.debug( string );
        }
        LOG.debug( "" + substitutionts.size() );
    }

    @Test
    public void testGML311CurveElements()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";
        GMLSchemaInfoSet analyzer = new GMLSchemaInfoSet( null, schemaURL );
        Set<String> substitutionts = getConcreteSubstitutions( "_Curve", analyzer );
        Assert.assertEquals( 4, substitutionts.size() );
        Assert.assertTrue( substitutionts.contains( "CompositeCurve" ) );
        Assert.assertTrue( substitutionts.contains( "Curve" ) );
        Assert.assertTrue( substitutionts.contains( "LineString" ) );
        Assert.assertTrue( substitutionts.contains( "OrientableCurve" ) );
    }

    @Test
    public void testGML311RingElements()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";
        GMLSchemaInfoSet analyzer = new GMLSchemaInfoSet( null, schemaURL );
        Set<String> substitutionts = getConcreteSubstitutions( "_Ring", analyzer );
        Assert.assertEquals( 2, substitutionts.size() );
        Assert.assertTrue( substitutionts.contains( "LinearRing" ) );
        Assert.assertTrue( substitutionts.contains( "Ring" ) );
    }

    @Test
    public void testGML311SurfaceElements()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";
        GMLSchemaInfoSet analyzer = new GMLSchemaInfoSet( null, schemaURL );
        Set<String> substitutions = getConcreteSubstitutions( "_Surface", analyzer );
        Assert.assertEquals( 7, substitutions.size() );
        Assert.assertTrue( substitutions.contains( "CompositeSurface" ) );
        Assert.assertTrue( substitutions.contains( "OrientableSurface" ) );
        Assert.assertTrue( substitutions.contains( "Polygon" ) );
        Assert.assertTrue( substitutions.contains( "PolyhedralSurface" ) );
        Assert.assertTrue( substitutions.contains( "Surface" ) );
        Assert.assertTrue( substitutions.contains( "Tin" ) );
        Assert.assertTrue( substitutions.contains( "TriangulatedSurface" ) );
    }

    @Test
    public void testGML311SolidElements()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";
        GMLSchemaInfoSet analyzer = new GMLSchemaInfoSet( null, schemaURL );
        Set<String> substitutions = getConcreteSubstitutions( "_Solid", analyzer );
        Assert.assertEquals( 2, substitutions.size() );
        Assert.assertTrue( substitutions.contains( "CompositeSolid" ) );
        Assert.assertTrue( substitutions.contains( "Solid" ) );
    }

    @Test
    public void testGML311CurveSegments()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";
        GMLSchemaInfoSet analyzer = new GMLSchemaInfoSet( null, schemaURL );
        List<XSElementDeclaration> elementDecls = analyzer.getSubstitutions( new QName( "http://www.opengis.net/gml",
                                                                                        "_CurveSegment" ),
                                                                             "http://www.opengis.net/gml", true, true );
        for ( XSElementDeclaration elementDecl : elementDecls ) {
            LOG.debug( elementDecl.getName() );
        }
    }

    @Test
    public void testGML311SurfacePatches()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";
        GMLSchemaInfoSet analyzer = new GMLSchemaInfoSet( null, schemaURL );
        List<XSElementDeclaration> elementDecls = analyzer.getSubstitutions( new QName( "http://www.opengis.net/gml",
                                                                                        "_SurfacePatch" ),
                                                                             "http://www.opengis.net/gml", true, true );
        for ( XSElementDeclaration elementDecl : elementDecls ) {
            LOG.debug( elementDecl.getName() );
        }
    }

    @Test
    public void testGML321CurveSegments()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.2.1/gml.xsd";
        GMLSchemaInfoSet analyzer = new GMLSchemaInfoSet( null, schemaURL );
        List<XSElementDeclaration> elementDecls = analyzer.getSubstitutions( new QName(
                                                                                        "http://www.opengis.net/gml/3.2",
                                                                                        "AbstractCurveSegment" ),
                                                                             "http://www.opengis.net/gml/3.2", true,
                                                                             true );
        for ( XSElementDeclaration elementDecl : elementDecls ) {
            LOG.debug( elementDecl.getName() );
        }
    }

    @Test
    public void testGML321SurfacePatches()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.2.1/gml.xsd";
        GMLSchemaInfoSet analyzer = new GMLSchemaInfoSet( null, schemaURL );
        List<XSElementDeclaration> elementDecls = analyzer.getSubstitutions( new QName(
                                                                                        "http://www.opengis.net/gml/3.2",
                                                                                        "AbstractSurfacePatch" ),
                                                                             "http://www.opengis.net/gml/3.2", true,
                                                                             true );
        for ( XSElementDeclaration elementDecl : elementDecls ) {
            LOG.debug( elementDecl.getName() );
        }
    }

    @Test
    public void testGML321GeometryElements()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.2.1/gml.xsd";
        GMLSchemaInfoSet analyzer = new GMLSchemaInfoSet( null, schemaURL );
        List<XSElementDeclaration> elementDecls = analyzer.getSubstitutions( new QName(
                                                                                        "http://www.opengis.net/gml/3.2",
                                                                                        "AbstractGeometry" ),
                                                                             "http://www.opengis.net/gml/3.2", true,
                                                                             true );
        for ( XSElementDeclaration elementDecl : elementDecls ) {
            LOG.debug( elementDecl.getName() );
        }
        LOG.debug( "" + elementDecls.size() );
    }

    private Set<String> getConcreteSubstitutions( String localName, GMLSchemaInfoSet analyzer ) {
        List<XSElementDeclaration> elementDecls = analyzer.getSubstitutions( new QName( "http://www.opengis.net/gml",
                                                                                        localName ),
                                                                             "http://www.opengis.net/gml", true, true );
        HashSet<String> localNames = new HashSet<String>();
        for ( XSElementDeclaration elementDecl : elementDecls ) {
            localNames.add( elementDecl.getName() );
        }
        return localNames;
    }

    @Test
    public void testReexportCiteSF1()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException, XMLStreamException, FactoryConfigurationError, IOException {

        String schemaURL = this.getClass().getResource( "../cite/schema/cite-gmlsf1.xsd" ).toString();
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( GMLVersion.GML_31, null, schemaURL );
        AppSchema schema = adapter.extractAppSchema();

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty( XMLOutputFactory.IS_REPAIRING_NAMESPACES, true );
        OutputStream os = new FileOutputStream( System.getProperty( "java.io.tmpdir" ) + File.separatorChar + "out.xml" );
        XMLStreamWriter writer = new IndentingXMLStreamWriter( outputFactory.createXMLStreamWriter( os ) );
        GMLAppSchemaWriter encoder = new GMLAppSchemaWriter( GMLVersion.GML_31, "http://cite.opengeospatial.org/gmlsf",
                                                             null, schema.getNamespaceBindings() );
        encoder.export( writer, schema );
        writer.close();
    }

    @Test
    public void testExportSchemaWrappped()
                    throws Exception {
        String schemaURL = this.getClass().getResource( "../cite/schema/cite-gmlsf1.xsd" ).toURI().toString();
        GMLSchemaInfoSet gmlSchemaInfoSet = new GMLSchemaInfoSet( null, schemaURL );

        GMLAppSchemaReader adapter = new GMLAppSchemaReader( GMLVersion.GML_31, null, schemaURL );
        AppSchema schema = adapter.extractAppSchema();

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty( XMLOutputFactory.IS_REPAIRING_NAMESPACES, true );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        XMLStreamWriter writer = new IndentingXMLStreamWriter( outputFactory.createXMLStreamWriter( os ) );
        GMLAppSchemaWriter encoder = new GMLAppSchemaWriter( GMLVersion.GML_31, "http://cite.opengeospatial.org/gmlsf",
                                                             null, schema.getNamespaceBindings() );
        encoder.export( writer, gmlSchemaInfoSet, "http://cite.opengeospatial.org/gmlsf", uri -> uri );
        writer.close();

        assertThat( the( os.toString() ),
                    hasXPath( "count(/xs:schema/xs:include)", nsContext(), returningANumber(), is( 2.0 ) ) );
    }

    @Test
    public void testExportSchema()
                    throws Exception {
        String schemaURL = this.getClass().getResource( "../cite/schema/cite-gmlsf0.xsd" ).toURI().toString();

        GMLSchemaInfoSet gmlSchemaInfoSet = new GMLSchemaInfoSet( null, schemaURL );

        GMLAppSchemaReader adapter = new GMLAppSchemaReader( GMLVersion.GML_31, null, schemaURL );
        AppSchema schema = adapter.extractAppSchema();

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        outputFactory.setProperty( XMLOutputFactory.IS_REPAIRING_NAMESPACES, true );
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        XMLStreamWriter writer = new IndentingXMLStreamWriter( outputFactory.createXMLStreamWriter( os ) );
        GMLAppSchemaWriter encoder = new GMLAppSchemaWriter( GMLVersion.GML_31, "http://cite.opengeospatial.org/gmlsf",
                                                             null, schema.getNamespaceBindings() );
        encoder.export( writer, gmlSchemaInfoSet, "http://cite.opengeospatial.org/gmlsf", uri -> uri );
        writer.close();

        System.out.println( os.toString() );
    }

    private NamespaceContext nsContext() {
        return new SimpleNamespaceContext().withBinding( "xs", "http://www.w3.org/2001/XMLSchema" );
    }

}

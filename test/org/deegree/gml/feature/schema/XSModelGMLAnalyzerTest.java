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
package org.deegree.gml.feature.schema;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.apache.xerces.xs.XSElementDeclaration;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.schema.GMLSchemaAnalyzer;
import org.junit.Test;

/**
 * TODO add documentation here
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class XSModelGMLAnalyzerTest {

    @Test
    public void testPhilosopher()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        GMLSchemaAnalyzer analyzer = new GMLSchemaAnalyzer(
                                                              GMLVersion.GML_31,
                                                              this.getClass().getResource(
                                                                                           "../testdata/schema/Philosopher.xsd" ).toString() );
        List<XSElementDeclaration> featureElementDecls = analyzer.getFeatureElementDeclarations(
                                                                                                 "http://www.deegree.org/app",
                                                                                                 false );
        for ( XSElementDeclaration featureElementDecl : featureElementDecls ) {
            System.out.println( "- Feature type: " + featureElementDecl.getName() );
        }
        List<XSElementDeclaration> featureCollectionElementDecls = analyzer.getFeatureCollectionElementDeclarations(
                                                                                                                     null,
                                                                                                                     false );
        for ( XSElementDeclaration featureCollectionElementDecl : featureCollectionElementDecls ) {
            System.out.println( "- Feature collection type: " + featureCollectionElementDecl.getName() );
        }
        List<XSElementDeclaration> geometryElementDecls = analyzer.getGeometryElementDeclarations( null, true );
        for ( XSElementDeclaration geometryElementDecl : geometryElementDecls ) {
            System.out.println( "- Geometry type: " + geometryElementDecl.getName() );
        }
    }

    @Test
    public void testGML311AggregateElements()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";
        GMLSchemaAnalyzer analyzer = new GMLSchemaAnalyzer( GMLVersion.GML_31, schemaURL );
        Set<String> substitutionts = getConcreteSubstitutions( "_GeometricAggregate", analyzer );
        for ( String string : substitutionts ) {
            System.out.println( string );
        }
        System.out.println( substitutionts.size() );
    }

    @Test
    public void testGML311PrimitiveElements()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";
        GMLSchemaAnalyzer analyzer = new GMLSchemaAnalyzer( GMLVersion.GML_31, schemaURL );
        Set<String> substitutionts = getConcreteSubstitutions( "_GeometricPrimitive", analyzer );
        for ( String string : substitutionts ) {
            System.out.println( string );
        }
        System.out.println( substitutionts.size() );
    }

    @Test
    public void testGML311ImplicitGeometryElements()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";
        GMLSchemaAnalyzer analyzer = new GMLSchemaAnalyzer( GMLVersion.GML_31, schemaURL );
        Set<String> substitutionts = getConcreteSubstitutions( "_ImplicitGeometry", analyzer );
        for ( String string : substitutionts ) {
            System.out.println( string );
        }
        System.out.println( substitutionts.size() );
    }

    @Test
    public void testGML311GeometryElements()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";
        GMLSchemaAnalyzer analyzer = new GMLSchemaAnalyzer( GMLVersion.GML_31, schemaURL );
        Set<String> substitutionts = getConcreteSubstitutions( "_Curve", analyzer );
        for ( String string : substitutionts ) {
            System.out.println( string );
        }
        System.out.println( substitutionts.size() );
    }

    @Test
    public void testGML311CurveElements()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";
        GMLSchemaAnalyzer analyzer = new GMLSchemaAnalyzer( GMLVersion.GML_31, schemaURL );
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
        GMLSchemaAnalyzer analyzer = new GMLSchemaAnalyzer( GMLVersion.GML_31, schemaURL );
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
        GMLSchemaAnalyzer analyzer = new GMLSchemaAnalyzer( GMLVersion.GML_31, schemaURL );
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
        GMLSchemaAnalyzer analyzer = new GMLSchemaAnalyzer( GMLVersion.GML_31, schemaURL );
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
        GMLSchemaAnalyzer analyzer = new GMLSchemaAnalyzer( GMLVersion.GML_31, schemaURL );
        List<XSElementDeclaration> elementDecls = analyzer.getSubstitutions( new QName( "http://www.opengis.net/gml",
                                                                                        "_CurveSegment" ),
                                                                             "http://www.opengis.net/gml", true, true );
        for ( XSElementDeclaration elementDecl : elementDecls ) {
            System.out.println( elementDecl.getName() );
        }
    }

    @Test
    public void testGML311SurfacePatches()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd";
        GMLSchemaAnalyzer analyzer = new GMLSchemaAnalyzer( GMLVersion.GML_31, schemaURL );
        List<XSElementDeclaration> elementDecls = analyzer.getSubstitutions( new QName( "http://www.opengis.net/gml",
                                                                                        "_SurfacePatch" ),
                                                                             "http://www.opengis.net/gml", true, true );
        for ( XSElementDeclaration elementDecl : elementDecls ) {
            System.out.println( elementDecl.getName() );
        }
    }

    @Test
    public void testGML321CurveSegments()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.2.1/gml.xsd";
        GMLSchemaAnalyzer analyzer = new GMLSchemaAnalyzer( GMLVersion.GML_32, schemaURL );
        List<XSElementDeclaration> elementDecls = analyzer.getSubstitutions(
                                                                             new QName(
                                                                                        "http://www.opengis.net/gml/3.2",
                                                                                        "AbstractCurveSegment" ),
                                                                             "http://www.opengis.net/gml/3.2", true,
                                                                             true );
        for ( XSElementDeclaration elementDecl : elementDecls ) {
            System.out.println( elementDecl.getName() );
        }
    }

    @Test
    public void testGML321SurfacePatches()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.2.1/gml.xsd";
        GMLSchemaAnalyzer analyzer = new GMLSchemaAnalyzer( GMLVersion.GML_32, schemaURL );
        List<XSElementDeclaration> elementDecls = analyzer.getSubstitutions(
                                                                             new QName(
                                                                                        "http://www.opengis.net/gml/3.2",
                                                                                        "AbstractSurfacePatch" ),
                                                                             "http://www.opengis.net/gml/3.2", true,
                                                                             true );
        for ( XSElementDeclaration elementDecl : elementDecls ) {
            System.out.println( elementDecl.getName() );
        }
    }

    @Test
    public void testGML321GeometryElements()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/gml/3.2.1/gml.xsd";
        GMLSchemaAnalyzer analyzer = new GMLSchemaAnalyzer( GMLVersion.GML_32, schemaURL );
        List<XSElementDeclaration> elementDecls = analyzer.getSubstitutions(
                                                                             new QName(
                                                                                        "http://www.opengis.net/gml/3.2",
                                                                                        "AbstractGeometry" ),
                                                                             "http://www.opengis.net/gml/3.2", true,
                                                                             true );
        for ( XSElementDeclaration elementDecl : elementDecls ) {
            System.out.println( elementDecl.getName() );
        }
        System.out.println( elementDecls.size() );
    }

    private Set<String> getConcreteSubstitutions( String localName, GMLSchemaAnalyzer analyzer ) {
        List<XSElementDeclaration> elementDecls = analyzer.getSubstitutions( new QName( "http://www.opengis.net/gml",
                                                                                        localName ),
                                                                             "http://www.opengis.net/gml", true, true );
        HashSet<String> localNames = new HashSet<String>();
        for ( XSElementDeclaration elementDecl : elementDecls ) {
            localNames.add( elementDecl.getName() );
        }
        return localNames;
    }
}

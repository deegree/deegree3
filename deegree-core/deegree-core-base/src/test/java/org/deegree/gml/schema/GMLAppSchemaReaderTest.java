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
package org.deegree.gml.schema;

import static javax.xml.namespace.QName.valueOf;
import static org.deegree.commons.tom.gml.GMLObjectCategory.TIME_OBJECT;
import static org.deegree.commons.tom.gml.GMLObjectCategory.TIME_SLICE;
import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.xerces.impl.xs.XSAttributeDecl;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSTypeDefinition;
import org.deegree.commons.tom.gml.GMLObjectType;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.utils.test.TestProperties;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureCollectionType;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.gml.GMLVersion;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests that check the correct extraction of {@link GMLObjectType}s from various GML application schemas.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 *
 * @version $Revision: $, $Date: $
 */
public class GMLAppSchemaReaderTest {

    @Test
    public void testParsingPhilosopher()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "../misc/schema/Philosopher.xsd" ).toString();
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaURL );
        AppSchema schema = adapter.extractAppSchema();
        List<FeatureType> fts = schema.getFeatureTypes( "http://www.deegree.org/app", true, true );
        Assert.assertEquals( 4, fts.size() );
    }

    @Test
    public void testParsingPhilosopherAndWFS()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "../misc/schema/Philosopher.xsd" ).toString();
        String schemaURL2 = "http://schemas.opengis.net/wfs/1.1.0/wfs.xsd";
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaURL, schemaURL2 );
        List<FeatureType> fts = adapter.extractAppSchema().getFeatureTypes( "http://www.deegree.org/app", false, false );
        Assert.assertEquals( 4, fts.size() );
    }

    @Test
    public void testParsingCityGML()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/citygml/profiles/base/1.0/CityGML.xsd";
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaURL );
        AppSchema schema = adapter.extractAppSchema();
        FeatureType[] fts = schema.getFeatureTypes();
        Assert.assertEquals( 69, fts.length );

        FeatureType buildingFt = schema.getFeatureType( QName.valueOf( "{http://www.opengis.net/citygml/building/1.0}Building" ) );
        PropertyType pt = buildingFt.getPropertyDeclaration( QName.valueOf( "{http://www.opengis.net/citygml/1.0}_GenericApplicationPropertyOfCityObject" ) );
        Assert.assertEquals( 8, pt.getSubstitutions().length );
    }

    @Test
    public void testParsingGeoSciML()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = TestProperties.getProperty( "schema_geosciml" );
        if ( schemaURL == null ) {
            return;
        }

        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaURL );
        AppSchema schema = adapter.extractAppSchema();
        schema.getFeatureTypes();
    }

    @Test
    public void testParsingCite110SF0()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "../cite/schema/cite-gmlsf0.xsd" ).toString();
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaURL );
        List<FeatureType> fts = adapter.extractAppSchema().getFeatureTypes( "http://cite.opengeospatial.org/gmlsf",
                                                                            false, false );
        Assert.assertEquals( 3, fts.size() );
    }

    @Test
    public void testParsingCite110SF1()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "../cite/schema/cite-gmlsf1.xsd" ).toString();
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaURL );
        FeatureType[] fts = adapter.extractAppSchema().getFeatureTypes();
        Assert.assertEquals( 5, fts.length );
    }

    @Test
    public void testParsingCite110SF2()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "../cite/schema/cite-gmlsf2.xsd" ).toString();
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaURL );
        FeatureType[] fts = adapter.extractAppSchema().getFeatureTypes();
        Assert.assertEquals( 6, fts.length );
    }

    @Test
    public void testParsingCite100DataFeatures()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "../cite/schema/all.xsd" ).toString();
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaURL );
        FeatureType[] fts = adapter.extractAppSchema().getFeatureTypes();
        Assert.assertEquals( 21, fts.length );
    }

    @Test
    public void testCite100GeometryFeatureCollection()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "../cite/schema/all.xsd" ).toString();
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaURL );
        AppSchema schema = adapter.extractAppSchema();
        FeatureCollectionType ft = (FeatureCollectionType) schema.getFeatureType( QName.valueOf( "{http://www.opengis.net/gml}_FeatureCollection" ) );
        ft = (FeatureCollectionType) schema.getFeatureType( QName.valueOf( "{http://www.opengis.net/cite/geometry}GeometryFeatureCollection" ) );
        List<PropertyType> newPropertyDecls = schema.getNewPropertyDecls( ft );
    }

    @Test
    public void testParsingXPlanGML20()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = TestProperties.getProperty( "schema_xplan2" );
        if ( schemaURL == null ) {
            return;
        }

        GMLAppSchemaReader adapter = new GMLAppSchemaReader( GMLVersion.GML_31, null, schemaURL );
        AppSchema schema = adapter.extractAppSchema();
        Assert.assertEquals( 132, schema.getFeatureTypes().length );
    }

    @Test
    public void testParsingNAS511()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = TestProperties.getProperty( "schema_nas511" );
        if ( schemaURL == null ) {
            return;
        }

        GMLAppSchemaReader adapter = new GMLAppSchemaReader( GMLVersion.GML_30, null, schemaURL );
        AppSchema schema = adapter.extractAppSchema();
        Assert.assertEquals( 237, schema.getFeatureTypes().length );

        QName ftName = new QName( "http://www.adv-online.de/namespaces/adv/gid/5.1.1", "AX_BesondereFlurstuecksgrenze" );
        FeatureType ft = adapter.extractAppSchema().getFeatureType( ftName );
        QName propName = new QName( "http://www.adv-online.de/namespaces/adv/gid/5.1.1", "position" );
        GeometryPropertyType pt = (GeometryPropertyType) ft.getPropertyDeclaration( propName );
        assertEquals( 2, pt.getAllowedGeometryTypes().size() );
        assertTrue( pt.getAllowedGeometryTypes().contains( GeometryPropertyType.GeometryType.CURVE ) );
        assertTrue( pt.getAllowedGeometryTypes().contains( GeometryPropertyType.GeometryType.COMPOSITE_CURVE ) );
    }

    @Test
    public void testParsingINSPIREAddresses()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = TestProperties.getProperty( "schema_inspire_addresses" );
        if ( schemaURL == null ) {
            return;
        }

        GMLAppSchemaReader adapter = new GMLAppSchemaReader( GMLVersion.GML_32, null, schemaURL );
        AppSchema schema = adapter.extractAppSchema();
        FeatureType[] fts = schema.getFeatureTypes();
        Assert.assertEquals( 75, fts.length );
        for ( String ns : schema.getGMLSchema().getAppNamespaces() ) {
            System.out.println( ns );
        }
    }

    @Test
    public void testParsingCustomProperties()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "../misc/schema/CustomProperties.xsd" ).toString();
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaURL );
        List<FeatureType> fts = adapter.extractAppSchema().getFeatureTypes( "http://www.deegree.org/app", false, false );
        Assert.assertEquals( 1, fts.size() );
        FeatureType ft = fts.get( 0 );
        Assert.assertEquals( 9, ft.getPropertyDeclarations().size() );
    }

    @Test
    public void testAIXMNumberOfFeatureTypes()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaUrl = this.getClass().getResource( "../aixm/schema/message/AIXM_BasicMessage.xsd" ).toString();
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaUrl );
        AppSchema schema = adapter.extractAppSchema();
        FeatureType[] fts = schema.getFeatureTypes();
        Assert.assertEquals( 157, fts.length );
    }

    @Test
    public void testAIXMNumberOfGeometryTypes()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaUrl = this.getClass().getResource( "../aixm/schema/message/AIXM_BasicMessage.xsd" ).toString();
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaUrl );
        AppSchema schema = adapter.extractAppSchema();
        Assert.assertEquals( 35, schema.getGeometryTypes().size() );
    }

    @Test
    public void testAIXMNumberOfTimeObjectTypes()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {
        final String schemaUrl = this.getClass().getResource( "../aixm/schema/message/AIXM_BasicMessage.xsd" ).toString();
        final GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaUrl );
        final AppSchema schema = adapter.extractAppSchema();
        final List<GMLObjectType> objectTypes = schema.getGmlObjectTypes( TIME_OBJECT );
        assertEquals( 10, objectTypes.size() );
    }

    @Test
    public void testAIXMNumberOfTimeSliceTypes()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {
        final String schemaUrl = this.getClass().getResource( "../aixm/schema/message/AIXM_BasicMessage.xsd" ).toString();
        final GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaUrl );
        final AppSchema schema = adapter.extractAppSchema();
        final List<GMLObjectType> objectTypes = schema.getGmlObjectTypes( TIME_SLICE );
        assertEquals( 127, objectTypes.size() );
    }

    @Test
    public void testAIXMCustomGeometryHierarchy()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaUrl = this.getClass().getResource( "../aixm/schema/message/AIXM_BasicMessage.xsd" ).toString();
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaUrl );
        AppSchema schema = adapter.extractAppSchema();

        // gml:Point
        GMLObjectType pointType = schema.getGeometryType( valueOf( "{http://www.opengis.net/gml/3.2}Point" ) );
        assertNotNull( pointType );
        List<GMLObjectType> pointSubstitutions = schema.getSubstitutions( pointType.getName() );
        assertEquals( 2, pointSubstitutions.size() );

        // gml:Curve
        GMLObjectType curveType = schema.getGeometryType( valueOf( "{http://www.opengis.net/gml/3.2}Curve" ) );
        assertNotNull( curveType );
        List<GMLObjectType> curveSubstitutions = schema.getSubstitutions( curveType.getName() );
        assertEquals( 2, curveSubstitutions.size() );

        // gml:Surface
        GMLObjectType surfaceType = schema.getGeometryType( valueOf( "{http://www.opengis.net/gml/3.2}Surface" ) );
        assertNotNull( surfaceType );
        List<GMLObjectType> surfaceSubstitutions = schema.getSubstitutions( surfaceType.getName() );
        assertEquals( 5, surfaceSubstitutions.size() );
    }

    @Test
    public void testAIXMCustomGeometryDeclarations()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaUrl = this.getClass().getResource( "../aixm/schema/message/AIXM_BasicMessage.xsd" ).toString();
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaUrl );
        AppSchema schema = adapter.extractAppSchema();
        String aixmNs = "http://www.aixm.aero/schema/5.1";

        // {http://www.aixm.aero/schema/5.1}Point
        GMLObjectType gt = schema.getGeometryType( new QName( aixmNs, "Point" ) );
        Assert.assertEquals( 9, gt.getPropertyDeclarations().size() );
        assertPropertyType( gt, 0, new QName( GML3_2_NS, "metaDataProperty" ), 0, -1 );
        assertPropertyType( gt, 1, new QName( GML3_2_NS, "description" ), 0, 1 );
        assertPropertyType( gt, 2, new QName( GML3_2_NS, "descriptionReference" ), 0, 1 );
        assertPropertyType( gt, 3, new QName( GML3_2_NS, "identifier" ), 0, 1 );
        assertPropertyType( gt, 4, new QName( GML3_2_NS, "name" ), 0, -1 );
        // gml:pos/gml:coordinates are actually part of a choice (that's why minOccurs is 1)
        assertPropertyType( gt, 5, new QName( GML3_2_NS, "pos" ), 1, 1 );
        assertPropertyType( gt, 6, new QName( GML3_2_NS, "coordinates" ), 1, 1 );
        assertPropertyType( gt, 7, new QName( aixmNs, "horizontalAccuracy" ), 0, 1 );
        assertPropertyType( gt, 8, new QName( aixmNs, "annotation" ), 0, -1 );

        // {http://www.aixm.aero/schema/5.1}ElevatedPoint
        gt = schema.getGeometryType( new QName( aixmNs, "ElevatedPoint" ) );
        Assert.assertEquals( 14, gt.getPropertyDeclarations().size() );
        assertPropertyType( gt, 0, new QName( GML3_2_NS, "metaDataProperty" ), 0, -1 );
        assertPropertyType( gt, 1, new QName( GML3_2_NS, "description" ), 0, 1 );
        assertPropertyType( gt, 2, new QName( GML3_2_NS, "descriptionReference" ), 0, 1 );
        assertPropertyType( gt, 3, new QName( GML3_2_NS, "identifier" ), 0, 1 );
        assertPropertyType( gt, 4, new QName( GML3_2_NS, "name" ), 0, -1 );
        // gml:pos/gml:coordinates are actually part of a choice (that's why minOccurs is 1)
        assertPropertyType( gt, 5, new QName( GML3_2_NS, "pos" ), 1, 1 );
        assertPropertyType( gt, 6, new QName( GML3_2_NS, "coordinates" ), 1, 1 );
        assertPropertyType( gt, 7, new QName( aixmNs, "horizontalAccuracy" ), 0, 1 );
        assertPropertyType( gt, 8, new QName( aixmNs, "annotation" ), 0, -1 );
        assertPropertyType( gt, 9, new QName( aixmNs, "elevation" ), 0, 1 );
        assertPropertyType( gt, 10, new QName( aixmNs, "geoidUndulation" ), 0, 1 );
        assertPropertyType( gt, 11, new QName( aixmNs, "verticalDatum" ), 0, 1 );
        assertPropertyType( gt, 12, new QName( aixmNs, "verticalAccuracy" ), 0, 1 );
        assertPropertyType( gt, 13, new QName( aixmNs, "extension" ), 0, -1 );

        // {http://www.aixm.aero/schema/5.1}Curve
        gt = schema.getGeometryType( new QName( aixmNs, "Curve" ) );
        Assert.assertEquals( 8, gt.getPropertyDeclarations().size() );
        assertPropertyType( gt, 0, new QName( GML3_2_NS, "metaDataProperty" ), 0, -1 );
        assertPropertyType( gt, 1, new QName( GML3_2_NS, "description" ), 0, 1 );
        assertPropertyType( gt, 2, new QName( GML3_2_NS, "descriptionReference" ), 0, 1 );
        assertPropertyType( gt, 3, new QName( GML3_2_NS, "identifier" ), 0, 1 );
        assertPropertyType( gt, 4, new QName( GML3_2_NS, "name" ), 0, -1 );
        assertPropertyType( gt, 5, new QName( GML3_2_NS, "segments" ), 1, 1 );
        assertPropertyType( gt, 6, new QName( aixmNs, "horizontalAccuracy" ), 0, 1 );
        assertPropertyType( gt, 7, new QName( aixmNs, "annotation" ), 0, -1 );

        // {http://www.aixm.aero/schema/5.1}ElevatedCurve
        gt = schema.getGeometryType( new QName( aixmNs, "ElevatedCurve" ) );
        Assert.assertEquals( 13, gt.getPropertyDeclarations().size() );
        assertPropertyType( gt, 0, new QName( GML3_2_NS, "metaDataProperty" ), 0, -1 );
        assertPropertyType( gt, 1, new QName( GML3_2_NS, "description" ), 0, 1 );
        assertPropertyType( gt, 2, new QName( GML3_2_NS, "descriptionReference" ), 0, 1 );
        assertPropertyType( gt, 3, new QName( GML3_2_NS, "identifier" ), 0, 1 );
        assertPropertyType( gt, 4, new QName( GML3_2_NS, "name" ), 0, -1 );
        assertPropertyType( gt, 5, new QName( GML3_2_NS, "segments" ), 1, 1 );
        assertPropertyType( gt, 6, new QName( aixmNs, "horizontalAccuracy" ), 0, 1 );
        assertPropertyType( gt, 7, new QName( aixmNs, "annotation" ), 0, -1 );
        assertPropertyType( gt, 8, new QName( aixmNs, "elevation" ), 0, 1 );
        assertPropertyType( gt, 9, new QName( aixmNs, "geoidUndulation" ), 0, 1 );
        assertPropertyType( gt, 10, new QName( aixmNs, "verticalDatum" ), 0, 1 );
        assertPropertyType( gt, 11, new QName( aixmNs, "verticalAccuracy" ), 0, 1 );
        assertPropertyType( gt, 12, new QName( aixmNs, "extension" ), 0, -1 );

        // {http://www.aixm.aero/schema/5.1}Surface
        gt = schema.getGeometryType( new QName( aixmNs, "Surface" ) );
        Assert.assertEquals( 8, gt.getPropertyDeclarations().size() );
        assertPropertyType( gt, 0, new QName( GML3_2_NS, "metaDataProperty" ), 0, -1 );
        assertPropertyType( gt, 1, new QName( GML3_2_NS, "description" ), 0, 1 );
        assertPropertyType( gt, 2, new QName( GML3_2_NS, "descriptionReference" ), 0, 1 );
        assertPropertyType( gt, 3, new QName( GML3_2_NS, "identifier" ), 0, 1 );
        assertPropertyType( gt, 4, new QName( GML3_2_NS, "name" ), 0, -1 );
        assertPropertyType( gt, 5, new QName( GML3_2_NS, "patches" ), 1, 1 );
        assertPropertyType( gt, 6, new QName( aixmNs, "horizontalAccuracy" ), 0, 1 );
        assertPropertyType( gt, 7, new QName( aixmNs, "annotation" ), 0, -1 );

        // {http://www.aixm.aero/schema/5.1}ElevatedSurface
        gt = schema.getGeometryType( new QName( aixmNs, "ElevatedSurface" ) );
        Assert.assertEquals( 13, gt.getPropertyDeclarations().size() );
        assertPropertyType( gt, 0, new QName( GML3_2_NS, "metaDataProperty" ), 0, -1 );
        assertPropertyType( gt, 1, new QName( GML3_2_NS, "description" ), 0, 1 );
        assertPropertyType( gt, 2, new QName( GML3_2_NS, "descriptionReference" ), 0, 1 );
        assertPropertyType( gt, 3, new QName( GML3_2_NS, "identifier" ), 0, 1 );
        assertPropertyType( gt, 4, new QName( GML3_2_NS, "name" ), 0, -1 );
        assertPropertyType( gt, 5, new QName( GML3_2_NS, "patches" ), 1, 1 );
        assertPropertyType( gt, 6, new QName( aixmNs, "horizontalAccuracy" ), 0, 1 );
        assertPropertyType( gt, 7, new QName( aixmNs, "annotation" ), 0, -1 );
        assertPropertyType( gt, 8, new QName( aixmNs, "elevation" ), 0, 1 );
        assertPropertyType( gt, 9, new QName( aixmNs, "geoidUndulation" ), 0, 1 );
        assertPropertyType( gt, 10, new QName( aixmNs, "verticalDatum" ), 0, 1 );
        assertPropertyType( gt, 11, new QName( aixmNs, "verticalAccuracy" ), 0, 1 );
        assertPropertyType( gt, 12, new QName( aixmNs, "extension" ), 0, -1 );
    }

    @Test
    public void testAIXMTimeSliceDeclarations()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaUrl = this.getClass().getResource( "../aixm/schema/message/AIXM_BasicMessage.xsd" ).toString();
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaUrl );
        AppSchema schema = adapter.extractAppSchema();
        GMLSchemaInfoSet gmlSchema = schema.getGMLSchema();
        List<XSElementDeclaration> timeSliceElementDecls = gmlSchema.getTimeSliceElementDeclarations( null, true );
        Assert.assertEquals( 126, timeSliceElementDecls.size() );
    }

    @Test
    public void testAIXMTimeSlicePropertyDeclarations()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaUrl = this.getClass().getResource( "../aixm/schema/message/AIXM_BasicMessage.xsd" ).toString();
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaUrl );
        AppSchema schema = adapter.extractAppSchema();
        GMLSchemaInfoSet gmlSchema = schema.getGMLSchema();
        String aixmNs = "http://www.aixm.aero/schema/5.1";
        QName ftName = new QName( aixmNs, "Unit" );
        FeatureType ft = schema.getFeatureType( ftName );
        QName propName = new QName( aixmNs, "timeSlice" );
        PropertyType pt = ft.getPropertyDeclaration( propName );
        XSElementDeclaration propDecl = pt.getElementDecl();
        GMLPropertySemantics propertySemantics = gmlSchema.getTimeSlicePropertySemantics( propDecl );
        assertNotNull( propertySemantics );

        propName = new QName( GML3_2_NS, "identifier" );
        pt = ft.getPropertyDeclaration( propName );
        propDecl = pt.getElementDecl();
        propertySemantics = gmlSchema.getTimeSlicePropertySemantics( propDecl );
        assertNull( propertySemantics );
    }

    @Test
    public void testIncludedGml321BaseSchemaIncludesGmlIdCorrigendum()
                            throws ClassCastException,
                            ClassNotFoundException,
                            InstantiationException,
                            IllegalAccessException {
        // schemaUrl is internally redirected to copy of the schema in the deegree-ogcschemas.jar
        String schemaUrl = "http://schemas.opengis.net/gml/3.2.1/gmlBase.xsd";
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaUrl );
        AppSchema schema = adapter.extractAppSchema();
        XSElementDeclaration abstractGmlElementDecl = schema.getGMLSchema().getAbstractGMLElementDeclaration();
        XSComplexTypeDefinition typeDefinition = (XSComplexTypeDefinition) abstractGmlElementDecl.getTypeDefinition();
        XSAttributeUse attrUse = null;
        for ( Object object : typeDefinition.getAttributeUses() ) {
            XSAttributeDecl attrDecl = (XSAttributeDecl) ( (XSAttributeUse) object ).getAttrDeclaration();
            if ( attrDecl.getNamespace().equals( "http://www.opengis.net/gml/3.2" )
                 && attrDecl.getName().equals( "id" ) ) {
                attrUse = (XSAttributeUse) object;
            }
        }
        assertFalse( attrUse.getRequired() );
    }

    private void assertPropertyType( GMLObjectType geometryDecl, int propDeclIdx, QName propName, int minOccurs,
                                     int maxOccurs ) {
        PropertyType pt = geometryDecl.getPropertyDeclarations().get( propDeclIdx );
        assertEquals( propName, pt.getName() );
        assertEquals( minOccurs, pt.getMinOccurs() );
        assertEquals( maxOccurs, pt.getMaxOccurs() );
        assertEquals( propName.getLocalPart(), pt.getElementDecl().getName() );
        assertEquals( propName.getNamespaceURI(), pt.getElementDecl().getNamespace() );
    }
}

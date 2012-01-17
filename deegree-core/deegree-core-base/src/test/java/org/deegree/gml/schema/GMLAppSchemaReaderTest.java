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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.utils.test.TestProperties;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.gml.GMLVersion;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * Tests that check the extraction of {@link FeatureType}s from various GML application schemas.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GMLAppSchemaReaderTest {

    private static final Logger LOG = getLogger( GMLAppSchemaReaderTest.class );

    @Test
    public void testParsingPhilosopher()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "Philosopher.xsd" ).toString();
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaURL );
        AppSchema schema = adapter.extractAppSchema();
        List<FeatureType> fts = schema.getFeatureTypes( "http://www.deegree.org/app", true, true );
        Assert.assertEquals( 4, fts.size() );
    }

    @Test
    public void testParsingPhilosopherAndWFS()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "Philosopher.xsd" ).toString();
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

        String schemaURL = this.getClass().getResource( "cite/cite-gmlsf0.xsd" ).toString();
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaURL );
        List<FeatureType> fts = adapter.extractAppSchema().getFeatureTypes( "http://cite.opengeospatial.org/gmlsf",
                                                                            false, false );
        Assert.assertEquals( 3, fts.size() );
    }

    @Test
    public void testParsingCite110SF1()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "cite/cite-gmlsf1.xsd" ).toString();
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaURL );
        FeatureType[] fts = adapter.extractAppSchema().getFeatureTypes();
        Assert.assertEquals( 5, fts.length );
        for ( FeatureType ft : fts ) {
            LOG.debug( "\nFt: " + ft.getName() );
            for ( PropertyType pt : ft.getPropertyDeclarations() ) {
                LOG.debug( "" + pt );
            }
        }
    }

    @Test
    public void testParsingCite110SF2()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "cite/cite-gmlsf2.xsd" ).toString();
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaURL );
        FeatureType[] fts = adapter.extractAppSchema().getFeatureTypes();
        for ( int i = 0; i < fts.length; i++ ) {
            LOG.debug( "" + fts[i] );
        }

        // TODO do more thorough testing
    }

    @Test
    public void testParsingCite100DataFeatures()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "cite/all.xsd" ).toString();
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaURL );
        FeatureType[] fts = adapter.extractAppSchema().getFeatureTypes();
        Assert.assertEquals( 21, fts.length );
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

        String schemaURL = this.getClass().getResource( "CustomProperties.xsd" ).toString();
        GMLAppSchemaReader adapter = new GMLAppSchemaReader( null, null, schemaURL );
        List<FeatureType> fts = adapter.extractAppSchema().getFeatureTypes( "http://www.deegree.org/app", false, false );
        Assert.assertEquals( 1, fts.size() );
        FeatureType ft = fts.get( 0 );
        Assert.assertEquals( 9, ft.getPropertyDeclarations().size() );
    }
}

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
package org.deegree.gml.feature.schema;

import static org.slf4j.LoggerFactory.getLogger;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.gml.GMLVersion;
import org.junit.Test;
import org.slf4j.Logger;

/**
 * Tests that check the extraction of {@link FeatureType}s from GML application schemas.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class ApplicationSchemaXSDDecoderTest {
    private static final Logger LOG = getLogger( ApplicationSchemaXSDDecoderTest.class );

    @Test
    public void testParsingPhilosopher()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "../testdata/schema/Philosopher.xsd" ).toString();
        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, null, schemaURL );
        FeatureType[] fts = adapter.extractFeatureTypeSchema().getFeatureTypes();
        Assert.assertEquals( 4, fts.length );
        // TODO do more thorough testing
    }

    @Test
    public void testParsingPhilosopherAndWFS()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "../testdata/schema/Philosopher.xsd" ).toString();
        String schemaURL2 = "http://schemas.opengis.net/wfs/1.1.0/wfs.xsd";
        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, null, schemaURL,
                                                                               schemaURL2 );
        FeatureType[] fts = adapter.extractFeatureTypeSchema().getFeatureTypes();
        LOG.debug( "" + fts[0].getName() );
        Assert.assertEquals( 5, fts.length );
        // TODO do more thorough testing
    }

    @Test
    public void testParsingCityGML()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/citygml/profiles/base/1.0/CityGML.xsd";
        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, null, schemaURL );
        ApplicationSchema schema = adapter.extractFeatureTypeSchema();
        FeatureType[] fts = schema.getFeatureTypes();
        Assert.assertEquals( 54, fts.length );

        FeatureType buildingFt = schema.getFeatureType( QName.valueOf( "{http://www.opengis.net/citygml/building/1.0}Building" ) );
        PropertyType pt = buildingFt.getPropertyDeclaration( QName.valueOf( "{http://www.opengis.net/citygml/1.0}_GenericApplicationPropertyOfCityObject" ) );
        Assert.assertEquals( 8, pt.getSubstitutions().length );
    }
    
//    @Test
//    public void testParsingGeoSciML()
//                            throws ClassCastException, ClassNotFoundException, InstantiationException,
//                            IllegalAccessException {
//
//        String schemaURL = "file:/home/markus/workspace/geosciml/xsd/geosciml.xsd";
//        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, null, schemaURL );
//        ApplicationSchema schema = adapter.extractFeatureTypeSchema();
//        FeatureType[] fts = schema.getFeatureTypes();
//        for ( FeatureType ft : fts ) {
//            System.out.println (ft.getName());
//        }
//        System.out.println (fts.length);
//    }

    @Test
    public void testParsingCite110SF0()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "../testdata/schema/cite/cite-gmlsf0.xsd" ).toString();
        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, null, schemaURL );
        FeatureType[] fts = adapter.extractFeatureTypeSchema().getFeatureTypes();
        Assert.assertEquals( 3, fts.length );
    }

    @Test
    public void testParsingCite110SF1()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "../testdata/schema/cite/cite-gmlsf1.xsd" ).toString();
        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, null, schemaURL );
        FeatureType[] fts = adapter.extractFeatureTypeSchema().getFeatureTypes();
        Assert.assertEquals( 4, fts.length );
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

        String schemaURL = this.getClass().getResource( "../testdata/schema/cite/cite-gmlsf2.xsd" ).toString();
        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, null, schemaURL );
        FeatureType[] fts = adapter.extractFeatureTypeSchema().getFeatureTypes();
        for ( int i = 0; i < fts.length; i++ ) {
            LOG.debug( "" + fts[i] );
        }

        // TODO do more thorough testing
    }

    @Test
    public void testParsingCite100DataFeatures()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "../testdata/schema/cite/all.xsd" ).toString();
        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_2, null, schemaURL );
        FeatureType[] fts = adapter.extractFeatureTypeSchema().getFeatureTypes();
        Assert.assertEquals( 19, fts.length );
    }

    // @Test
    public void testParsingXPlanGML20()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "file:/home/schneider/workspace/lkee_xplanung2/resources/schema/XPlanGML_2_0/XPlanGml.xsd";
        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, null, schemaURL );
        FeatureType ft = adapter.extractFeatureTypeSchema().getFeatureType(
                                                                            new QName(
                                                                                       "http://www.xplanung.de/xplangml",
                                                                                       "BP_Plan" ) );
        LOG.debug( "" + ft );
        // TODO do more thorough testing
    }
    
    @Test
    public void testParsingCustomProperties()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "../testdata/schema/CustomProperties.xsd" ).toString();
        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, null, schemaURL );
        FeatureType[] fts = adapter.extractFeatureTypeSchema().getFeatureTypes();
        Assert.assertEquals( 1, fts.length );
        FeatureType ft = fts [0];
        Assert.assertEquals( 4, ft.getPropertyDeclarations().size() );
    }    
}

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
package org.deegree.feature.gml.schema;

import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.deegree.commons.gml.GMLVersion;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.PropertyType;
import org.junit.Test;

/**
 * Tests that check the extraction of {@link FeatureType}s from GML application schemas.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GMLApplicationSchemaXSDDecoderTest {

    @Test
    public void testParsingPhilosopher()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "../testdata/schema/Philosopher.xsd" ).toString();
        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, schemaURL );
        FeatureType[] fts = adapter.extractFeatureTypeSchema().getFeatureTypes();
        Assert.assertEquals( 4, fts.length );

        for ( int i = 0; i < fts.length; i++ ) {
            System.out.println (fts[i]);
        }        
        // TODO do more thorough testing
    }
   
    @Test
    public void testParsingCityGML()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "http://schemas.opengis.net/citygml/profiles/base/1.0/CityGML.xsd";
        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, schemaURL );
        ApplicationSchema schema = adapter.extractFeatureTypeSchema();
        FeatureType[] fts = schema.getFeatureTypes();        
        Assert.assertEquals( 54, fts.length );
        FeatureType cityModelFt = schema.getFeatureType( new QName ("http://www.opengis.net/citygml/1.0", "CityModel" ));
        System.out.println (cityModelFt.getClass());        
        System.out.println (cityModelFt);

        // TODO do more thorough testing
    }
    
    @Test
    public void testParsingCiteSF0()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "../testdata/schema/cite/cite-gmlsf0.xsd" ).toString();
        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, schemaURL );
        FeatureType[] fts = adapter.extractFeatureTypeSchema().getFeatureTypes();
        Assert.assertEquals( 3, fts.length );
    }
    
    @Test
    public void testParsingCiteSF1()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "../testdata/schema/cite/cite-gmlsf1.xsd" ).toString();
        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, schemaURL );
        FeatureType[] fts = adapter.extractFeatureTypeSchema().getFeatureTypes();
        Assert.assertEquals( 4, fts.length );
        for ( FeatureType ft : fts ) {
            System.out.println ("\nFt: " + ft.getName());
            for ( PropertyType pt : ft.getPropertyDeclarations() ) {
                System.out.println (pt);
            }
        }
    }

    @Test
    public void testParsingCiteSF2()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = this.getClass().getResource( "../testdata/schema/cite/cite-gmlsf2.xsd" ).toString();
        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, schemaURL );
        FeatureType[] fts = adapter.extractFeatureTypeSchema().getFeatureTypes();
        for ( int i = 0; i < fts.length; i++ ) {
            System.out.println (fts[i]);
        }

        // TODO do more thorough testing
    }    
    
//    @Test
    public void testParsingXPlanGML20()
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException {

        String schemaURL = "file:/home/schneider/workspace/lkee_xplanung2/resources/schema/XPlanGML_2_0/XPlanGml.xsd";
        ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, schemaURL );
        FeatureType ft = adapter.extractFeatureTypeSchema().getFeatureType(new QName("http://www.xplanung.de/xplangml", "BP_Plan"));
        System.out.println (ft);
        // TODO do more thorough testing
    }
}

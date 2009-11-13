//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.feature.persistence.postgis;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.deegree.feature.persistence.postgis.jaxbconfig.ApplicationSchemaDecl;
import org.deegree.feature.persistence.postgis.jaxbconfig.FeaturePropertyMappingType;
import org.deegree.feature.persistence.postgis.jaxbconfig.GlobalMappingHints;
import org.deegree.feature.persistence.postgis.jaxbconfig.SimplePropertyMappingType;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.junit.Test;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class JAXBAdapterTest {

    @Test
    public void testToInternal()
                            throws JAXBException {

        JAXBContext jc = JAXBContext.newInstance( "org.deegree.feature.persistence.postgis.jaxbconfig" );
        Unmarshaller u = jc.createUnmarshaller();
        ApplicationSchemaDecl jaxbAppSchema = (ApplicationSchemaDecl) u.unmarshal( this.getClass().getResource(
                                                                                                                "postgis_philosopher.xml" ) );

        PostGISApplicationSchema mappedSchema = JAXBApplicationSchemaAdapter.toInternal( jaxbAppSchema );

        GlobalMappingHints globalHints = mappedSchema.getGlobalHints();
        assertNotNull( globalHints );
        assertEquals( "conn1", globalHints.getJDBCConnId() );
        assertTrue( globalHints.isUseObjectLookupTable() );

        ApplicationSchema schema = mappedSchema.getSchema();
        assertEquals( 4, schema.getFeatureTypes().length );

        FeatureType philosopherFt = schema.getFeatureType( QName.valueOf( "{http://www.deegree.org/app}Philosopher" ) );
        assertNotNull( philosopherFt );
        assertFalse( philosopherFt.isAbstract() );
        assertEquals( 9, philosopherFt.getPropertyDeclarations().size() );
        FeatureTypeMapping philosopherMapping = mappedSchema.getFtMapping( philosopherFt.getName() );
        assertNotNull( philosopherMapping );
        assertEquals( "PHILOSOPHER", philosopherMapping.getFeatureTypeHints().getDBTable() );
        assertTrue( philosopherMapping.getFeatureTypeHints().isGMLDefaultProps() );

        FeaturePropertyType placeOfBirthProp = (FeaturePropertyType) philosopherFt.getPropertyDeclaration( QName.valueOf( "{http://www.deegree.org/app}placeOfBirth" ) );
        assertNotNull( placeOfBirthProp );
        assertEquals (QName.valueOf( "{http://www.deegree.org/app}Place" ), placeOfBirthProp.getValueFt().getName());
        FeaturePropertyMappingType placeOfBirthMapping = (FeaturePropertyMappingType) philosopherMapping.getPropertyHints( placeOfBirthProp.getName() );
        assertNotNull( placeOfBirthMapping.getDBColumn() );
        assertNull( placeOfBirthMapping.getFeatureJoinTable() );
        assertEquals( "PLACE_OF_BIRTH", placeOfBirthMapping.getDBColumn().getName() );

        FeatureType placeFt = schema.getFeatureType( QName.valueOf( "{http://www.deegree.org/app}Place" ) );
        assertNotNull( placeFt );
        assertFalse( placeFt.isAbstract() );
        assertEquals( 2, placeFt.getPropertyDeclarations().size() );

        FeatureType countryFt = schema.getFeatureType( QName.valueOf( "{http://www.deegree.org/app}Country" ) );
        assertNotNull( countryFt );
        assertFalse( countryFt.isAbstract() );

        FeatureType bookFt = schema.getFeatureType( QName.valueOf( "{http://www.deegree.org/app}Book" ) );
        assertNotNull( bookFt );
        assertFalse( bookFt.isAbstract() );        
    }
    
    @Test
    public void testToJAXB()
                            throws JAXBException {

        JAXBContext jc = JAXBContext.newInstance( "org.deegree.feature.persistence.postgis.jaxbconfig" );
        Unmarshaller u = jc.createUnmarshaller();
        ApplicationSchemaDecl jaxbAppSchema = (ApplicationSchemaDecl) u.unmarshal( this.getClass().getResource(
                                                                                                                "postgis_philosopher.xml" ) );

        PostGISApplicationSchema postgisSchema = JAXBApplicationSchemaAdapter.toInternal( jaxbAppSchema );
        jaxbAppSchema = JAXBApplicationSchemaAdapter.toJAXB( postgisSchema );
        
        jc = JAXBContext.newInstance( "org.deegree.feature.persistence.postgis.jaxbconfig" );
        Marshaller m = jc.createMarshaller();
        m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
        m.setProperty( Marshaller.JAXB_SCHEMA_LOCATION, "http://www.deegree.org/feature/featuretype http://schemas.deegree.org/feature/0.3.0/postgis_appschema.xsd" );
        m.marshal( jaxbAppSchema, new File ("/tmp/out.txt") );
    }    
}

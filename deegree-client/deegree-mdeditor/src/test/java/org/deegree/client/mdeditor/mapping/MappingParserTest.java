//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.client.mdeditor.mapping;

import java.net.MalformedURLException;

import java.net.URL;
import java.util.List;

import org.deegree.client.mdeditor.configuration.ConfigurationException;
import org.deegree.client.mdeditor.configuration.mapping.MappingParser;
import org.deegree.client.mdeditor.model.mapping.MappingElement;
import org.deegree.client.mdeditor.model.mapping.MappingGroup;
import org.deegree.client.mdeditor.model.mapping.MappingInformation;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class MappingParserTest extends TestCase {

    @Test
    public void testParseMapping()
                            throws MalformedURLException, ConfigurationException {
        URL url = new URL(
                           "file:///home/lyn/workspace/deegree-mdeditor/src/test/resources/org/deegree/client/mdeditor/mapping/mappingTest.xml" );
        MappingInformation mapping = MappingParser.parseMapping( url );
        assertNotNull( mapping );
        assertEquals( "ISO_10_MAPPING", mapping.getId() );
        assertEquals( "ISO Application Profile", mapping.getName() );
        assertEquals( "1.0", mapping.getVersion() );
        assertNull( mapping.getDescribtion() );
        assertEquals( "http://schemas.opengis.net/iso/19139/20060504/gmd/metadataEntity.xsd", mapping.getSchema() );

        List<MappingElement> me = mapping.getMappingElements();
        assertNotNull( me );
        assertEquals( 6, me.size() );

        assertEquals( "FormGroup3/text5", me.get( 0 ).getFormFieldPath() );
        assertEquals( "gmd:MD_Metadata/gmd:fileIdentifier/gco:CharacterString", me.get( 0 ).getSchemaPath() );

        assertTrue( me.get( 5 ) instanceof MappingGroup );
        MappingGroup mg = (MappingGroup) me.get( 5 );
        assertEquals( "SimpleUnboundedFormGroup", mg.getFormFieldPath() );
        assertEquals(
                      "gmd:MD_Metadata/gmd:distributionInfo/gmd:MD_Distribution/gmd:distributor/gmd:MD_Distributor/gmd:distributorFormat/gmd:MD_Format",
                      mg.getSchemaPath() );
        List<MappingElement> elements = mg.getMappingElements();
        assertNotNull( elements );
        assertEquals( 2, elements.size() );

        assertEquals( "SimpleUnboundedFormGroup/in1", elements.get( 0 ).getFormFieldPath() );
        assertEquals( "gmd:name/gco:CharacterString", elements.get( 0 ).getSchemaPath() );
    }
}

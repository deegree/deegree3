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
package org.deegree.services.wps.provider.jrxml.contentprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.process.jaxb.java.LiteralInputDefinition;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.deegree.services.wps.provider.jrxml.ParameterDescription;
import org.junit.Test;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public class TestOtherContentProviderTest {

    @Test
    public void testInspectInputParametersFromJrxml() {
        OtherContentProvider wmsContentProvider = new OtherContentProvider( null );
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put( "DATE", "java.util.Date" );
        parameters.put( "DESCRIPTION", "java.lang.String" );
        parameters.put( "MAPSCALE", "java.lang.Integer" );
        parameters.put( "printOptTxt", "java.lang.Boolean" );
        List<JAXBElement<? extends ProcessletInputDefinition>> inputs = new ArrayList<JAXBElement<? extends ProcessletInputDefinition>>();

        XMLAdapter adapter = new XMLAdapter(
                                             TestOtherContentProviderTest.class.getResourceAsStream( "../testWPSreportTemplate.jrxml" ) );
        List<String> handledParams = new ArrayList<String>();
        wmsContentProvider.inspectInputParametersFromJrxml( new HashMap<String, ParameterDescription>(), inputs,
                                                            adapter, parameters, handledParams );

        assertEquals( 4, parameters.size() );

        // handled
        assertEquals( 4, handledParams.size() );
        assertEquals( 4, inputs.size() );

        JAXBElement<? extends ProcessletInputDefinition> pDate = null;
        JAXBElement<? extends ProcessletInputDefinition> pString = null;
        JAXBElement<? extends ProcessletInputDefinition> pInt = null;
        JAXBElement<? extends ProcessletInputDefinition> pBool = null;

        for ( JAXBElement<? extends ProcessletInputDefinition> in : inputs ) {
            if ( "DATE".equals( in.getValue().getIdentifier().getValue() ) ) {
                pDate = in;
            } else if ( "DESCRIPTION".equals( in.getValue().getIdentifier().getValue() ) ) {
                pString = in;
            } else if ( "MAPSCALE".equals( in.getValue().getIdentifier().getValue() ) ) {
                pInt = in;
            } else if ( "printOptTxt".equals( in.getValue().getIdentifier().getValue() ) ) {
                pBool = in;
            }
        }

        assertNotNull( pDate );
        assertNotNull( pString );
        assertNotNull( pInt );
        assertNotNull( pBool );

        assertTrue( pDate.getDeclaredType() == LiteralInputDefinition.class );
        assertTrue( pString.getDeclaredType() == LiteralInputDefinition.class );
        assertTrue( pInt.getDeclaredType() == LiteralInputDefinition.class );
        assertTrue( pBool.getDeclaredType() == LiteralInputDefinition.class );

        assertEquals( "date", ( ( (LiteralInputDefinition) pDate.getValue() ).getDataType().getValue() ) );
        assertEquals( "string", ( ( (LiteralInputDefinition) pString.getValue() ).getDataType().getValue() ) );
        assertEquals( "integer", ( ( (LiteralInputDefinition) pInt.getValue() ).getDataType().getValue() ) );
        assertEquals( "boolean", ( ( (LiteralInputDefinition) pBool.getValue() ).getDataType().getValue() ) );
    }

}

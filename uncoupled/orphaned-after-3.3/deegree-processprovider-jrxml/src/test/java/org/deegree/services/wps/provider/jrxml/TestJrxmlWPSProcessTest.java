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
package org.deegree.services.wps.provider.jrxml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBElement;

import org.deegree.process.jaxb.java.ProcessDefinition;
import org.deegree.process.jaxb.java.ProcessDefinition.InputParameters;
import org.deegree.process.jaxb.java.ProcessletInputDefinition;
import org.junit.Test;

/**
 * 
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
 * 
 */
public class TestJrxmlWPSProcessTest {

    @Test
    public void testCreationOfAProcessOutOfAJRXML() {
        JrxmlProcessDescription desc = new JrxmlProcessDescription(
                                                                    "id",
                                                                    TestJrxmlParserTest.class.getResource( "testWPSreportTemplate.jrxml" ),
                                                                    null, new HashMap<String, ParameterDescription>(), null,
                                                                    new HashMap<String, URL>(), null );
        JrxmlWPSProcess wpsProcess = new JrxmlWPSProcess( desc );
        wpsProcess.init( null );
        ProcessDefinition pd = wpsProcess.getDescription();
        assertNotNull( pd );
        assertNotNull( pd.getIdentifier() );
        assertEquals( "id", pd.getIdentifier().getValue() );
        assertEquals( "createReportByAWPSProcess", pd.getTitle().getValue() );

        InputParameters inputParameters = pd.getInputParameters();
        assertNotNull( inputParameters );

        List<JAXBElement<? extends ProcessletInputDefinition>> processInput = inputParameters.getProcessInput();
        assertNotNull( processInput );
        assertEquals( 6, processInput.size() );
    }
}

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
package org.deegree.services.wms;

import java.util.Collection;

import org.deegree.services.AbstractCiteIntegrationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Wraps the execution of the CITE WMS 1.3.0 TestSuite as a JUnit-test.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@RunWith(Parameterized.class)
public class WMSCite130IntegrationTest extends AbstractCiteIntegrationTest {

    private String testLabel = "WMS130";

    @Parameters
    public static Collection getResultSnippets() throws Exception {
        return getResultSnippets( "/citewms130/ctl/" , "capabilities-url" , "wms?request=GetCapabilities&service=WMS&version=1.3.0");
    }

    public WMSCite130IntegrationTest( String testLabel, String resultSnippet ) {
        this.testLabel = testLabel;
        this.resultSnippet = resultSnippet;
    }

    @Test
    public void singleTest() {
        if ( resultSnippet.contains( "Failed" ) ) {
            throw new RuntimeException( "Test '" + testLabel + "' failed." );
        }
    }
}

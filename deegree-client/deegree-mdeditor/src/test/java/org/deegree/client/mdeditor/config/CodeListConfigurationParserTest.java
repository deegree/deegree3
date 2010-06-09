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
package org.deegree.client.mdeditor.config;

import java.util.List;

import junit.framework.TestCase;

import org.deegree.client.mdeditor.configuration.Configuration;
import org.deegree.client.mdeditor.configuration.ConfigurationException;
import org.deegree.client.mdeditor.configuration.codelist.CodeListConfigurationFactory;
import org.deegree.client.mdeditor.model.CodeList;
import org.junit.Test;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class CodeListConfigurationParserTest extends TestCase {

    @Test
    public void testParseCodeLists()
                            throws ConfigurationException {
        Configuration.setCodeListURL( "/home/lyn/workspace/deegree-mdeditor/src/test/resources/org/deegree/client/mdeditor/mapping/guiSchemaMapping.xsd" );
        List<CodeList> codeLists = CodeListConfigurationFactory.getCodeLists();

        assertNotNull( codeLists );
        assertTrue( codeLists.size() == 3 );

        CodeList codeList1 = codeLists.get( 0 );
        CodeList codeList2 = codeLists.get( 1 );
        CodeList codeList3 = codeLists.get( 2 );
        assertEquals( "keyword", codeList1.getId() );
        assertEquals( "hierarchylevel", codeList2.getId() );
        assertEquals( "roleCode", codeList3.getId() );

        assertEquals( 4, codeList1.getCodes().size() );
        assertEquals( 2, codeList2.getCodes().size() );
        assertEquals( 11, codeList3.getCodes().size() );
    }

    @Test
    public void testParseCode()
                            throws ConfigurationException {
        Configuration.setCodeListURL( "/home/lyn/workspace/deegree-mdeditor/src/test/resources/org/deegree/client/mdeditor/config/codelistTestConfiguration.xsd" );
        CodeList codeList = CodeListConfigurationFactory.getCodeList( "hierarchylevel" );

        assertNotNull( codeList );

        assertEquals( "hierarchylevel", codeList.getId() );
        assertEquals( 2, codeList.getCodes().size() );

        String value = "service";
        assertNotNull( codeList.getCodes().get( value ) );
        assertEquals( "Service", codeList.getCodes().get( value ) );

    }

}

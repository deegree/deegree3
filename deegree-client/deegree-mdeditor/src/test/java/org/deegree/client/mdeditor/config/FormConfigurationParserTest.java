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

import org.deegree.client.mdeditor.model.CodeList;
import org.deegree.client.mdeditor.model.FormConfiguration;
import org.deegree.client.mdeditor.model.FormElement;
import org.deegree.client.mdeditor.model.FormFieldPath;
import org.deegree.client.mdeditor.model.FormGroup;
import org.deegree.client.mdeditor.model.InputFormField;
import org.deegree.client.mdeditor.model.ReferencedElement;
import org.deegree.client.mdeditor.model.SelectFormField;
import org.junit.Test;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class FormConfigurationParserTest extends TestCase {

    @Test
    public void testParseFormGroups()
                            throws ConfigurationException {
        Configuration.setFormConfURL( "/home/lyn/workspace/deegree-mdeditor/src/test/resources/org/deegree/client/mdeditor/config/simpleTestConfig.xml" );
        FormConfiguration configuration = FormConfigurationFactory.getOrCreateFormConfiguration( "test" );
        List<FormGroup> formGroups = configuration.getFormGroups();

        assertNotNull( formGroups );
        assertTrue( formGroups.size() == 2 );

        assertEquals( "FormGroup3", formGroups.get( 0 ).getId() );
        assertEquals( "FormGroup", formGroups.get( 1 ).getId() );

        assertEquals( 2, formGroups.get( 0 ).getFormElements().size() );
        assertEquals( 3, formGroups.get( 1 ).getFormElements().size() );

        FormElement formElement = formGroups.get( 1 ).getFormElements().get( 2 );
        assertTrue( formElement instanceof FormGroup );
        assertEquals( 4, ( (FormGroup) formElement ).getFormElements().size() );
    }

    @Test
    public void testParseReferencedFormElementAndIdentifier() throws ConfigurationException {
        Configuration.setFormConfURL( "/home/lyn/workspace/deegree-mdeditor/src/test/resources/org/deegree/client/mdeditor/config/simpleTestConfig.xml" );
        FormConfiguration configuration = FormConfigurationFactory.getOrCreateFormConfiguration( "test" );
        List<FormGroup> formGroups = configuration.getFormGroups();

        assertNotNull( formGroups );
        assertTrue( formGroups.size() == 2 );

        assertEquals( "FormGroup3", formGroups.get( 0 ).getId() );
        assertEquals( "FormGroup", formGroups.get( 1 ).getId() );

        assertEquals( 2, formGroups.get( 0 ).getFormElements().size() );
        assertEquals( 3, formGroups.get( 1 ).getFormElements().size() );

        FormElement refFormElement = formGroups.get( 0 ).getFormElements().get( 1 );
        assertTrue( refFormElement instanceof ReferencedElement );
        assertEquals( "generateIdBean", ( (ReferencedElement) refFormElement ).getBeanName() );
        assertEquals( true, ( (ReferencedElement) refFormElement ).isIdentifier() );

    }

    @Test
    public void testParseFormElements() throws ConfigurationException {
        Configuration.setFormConfURL( "/home/lyn/workspace/deegree-mdeditor/src/test/resources/org/deegree/client/mdeditor/config/simpleTestConfig.xml" );
        FormConfiguration configuration = FormConfigurationFactory.getOrCreateFormConfiguration( "test" );
        List<FormGroup> formGroups = configuration.getFormGroups();

        assertNotNull( formGroups );
        assertTrue( formGroups.size() == 2 );

        assertEquals( "FormGroup", formGroups.get( 1 ).getId() );
        assertEquals( 3, formGroups.get( 1 ).getFormElements().size() );

        FormElement input = formGroups.get( 1 ).getFormElements().get( 1 );
        assertTrue( input instanceof InputFormField );
        FormFieldPath inputPath = new FormFieldPath( "FormGroup", "text1" );
        assertEquals( inputPath, ( (InputFormField) input ).getPath() );

        FormElement formElement = formGroups.get( 1 ).getFormElements().get( 2 );
        assertTrue( formElement instanceof FormGroup );
        assertEquals( 4, ( (FormGroup) formElement ).getFormElements().size() );

        FormElement select = ( (FormGroup) formElement ).getFormElements().get( 3 );
        assertTrue( select instanceof SelectFormField );
        FormFieldPath selectPath = new FormFieldPath( "FormGroup", "FormGroup11", "selectOne2" );
        assertEquals( selectPath, ( (SelectFormField) select ).getPath() );
    }

    @Test
    public void testParseCodeLists() throws ConfigurationException {
        Configuration.setFormConfURL( "/home/lyn/workspace/deegree-mdeditor/src/test/resources/org/deegree/client/mdeditor/config/simpleTestConfig.xml" );
        FormConfiguration configuration = FormConfigurationFactory.getOrCreateFormConfiguration( "test" );
        List<CodeList> codeLists = configuration.getCodeLists();

        assertNotNull( codeLists );
        assertTrue( codeLists.size() == 2 );

        assertEquals( "testCodeList1", codeLists.get( 0 ).getId() );
        assertEquals( "testCodeList2", codeLists.get( 1 ).getId() );

        assertEquals( 2, codeLists.get( 0 ).getCodes().size() );
        assertEquals( 3, codeLists.get( 1 ).getCodes().size() );
    }

    @Test
    public void testParseCode()
                            throws ConfigurationException {
        Configuration.setFormConfURL( "/home/lyn/workspace/deegree-mdeditor/src/test/resources/org/deegree/client/mdeditor/config/simpleTestConfig.xml" );
        FormConfiguration configuration = FormConfigurationFactory.getOrCreateFormConfiguration( "test" );
        CodeList codeList = configuration.getCodeList( "testCodeList2" );

        assertNotNull( codeList );

        assertEquals( "testCodeList2", codeList.getId() );
        assertEquals( 3, codeList.getCodes().size() );

        String value = "nummer1";
        assertNotNull( codeList.getCodes().get( value ) );
        assertEquals( "Nummer 1", codeList.getCodes().get( value ) );

    }

}

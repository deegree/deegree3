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
package org.deegree.client.mdeditor.configuration;

import java.net.URL;

import java.util.List;

import junit.framework.TestCase;

import org.deegree.client.mdeditor.configuration.ConfigurationException;
import org.deegree.client.mdeditor.model.FormConfiguration;
import org.deegree.client.mdeditor.model.FormElement;
import org.deegree.client.mdeditor.model.FormFieldPath;
import org.deegree.client.mdeditor.model.FormGroup;
import org.deegree.client.mdeditor.model.InputFormField;
import org.deegree.client.mdeditor.model.LAYOUT_TYPE;
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
    public void testParseFormConfiguration()
                            throws ConfigurationException {
        FormConfiguration configuration = ConfigurationManager.getConfiguration().getConfiguration( "simple" );

        assertEquals( LAYOUT_TYPE.TAB, configuration.getLayoutType() );

        assertNotNull( configuration.getPathToIdentifier() );
        assertEquals( "FormGroup3/ref", configuration.getPathToIdentifier().toString() );

        assertNotNull( configuration.getPathToTitle() );
        assertEquals( "FormGroup/text1", configuration.getPathToTitle().toString() );

        assertNotNull( configuration.getPathToDescription() );
        assertEquals( "FormGroup/FormGroup11/text2", configuration.getPathToDescription().toString() );
    }

    @Test
    public void testParseFormGroups()
                            throws ConfigurationException {
        FormConfiguration configuration = ConfigurationManager.getConfiguration().getConfiguration( "simple" );
        List<FormGroup> formGroups = configuration.getFormGroups();

        assertNotNull( formGroups );
        assertTrue( formGroups.size() == 4 );

        assertEquals( "FormGroup3", formGroups.get( 0 ).getId() );
        assertEquals( "FormGroup", formGroups.get( 1 ).getId() );

        assertEquals( 2, formGroups.get( 0 ).getFormElements().size() );
        assertEquals( 3, formGroups.get( 1 ).getFormElements().size() );

        FormElement formElement = formGroups.get( 1 ).getFormElements().get( 2 );
        assertTrue( formElement instanceof FormGroup );
        assertEquals( 5, ( (FormGroup) formElement ).getFormElements().size() );
    }

    @Test
    public void testParseReferencedFormElementAndIdentifier()
                            throws ConfigurationException {
        FormConfiguration configuration = ConfigurationManager.getConfiguration().getConfiguration( "simple" );
        List<FormGroup> formGroups = configuration.getFormGroups();

        assertNotNull( formGroups );
        assertTrue( formGroups.size() == 4 );

        assertEquals( "FormGroup3", formGroups.get( 0 ).getId() );
        assertEquals( "FormGroup", formGroups.get( 1 ).getId() );

        assertEquals( 2, formGroups.get( 0 ).getFormElements().size() );
        assertEquals( 3, formGroups.get( 1 ).getFormElements().size() );

        FormElement refFormElement = formGroups.get( 0 ).getFormElements().get( 1 );
        assertTrue( refFormElement instanceof ReferencedElement );
        assertEquals( "generateIdBean", ( (ReferencedElement) refFormElement ).getBeanName() );

    }

    @Test
    public void testParseFormElements()
                            throws ConfigurationException {
        FormConfiguration configuration = ConfigurationManager.getConfiguration().getConfiguration( "simple" );
        List<FormGroup> formGroups = configuration.getFormGroups();

        assertNotNull( formGroups );
        assertTrue( formGroups.size() == 4 );

        assertEquals( "FormGroup", formGroups.get( 1 ).getId() );
        assertEquals( 3, formGroups.get( 1 ).getFormElements().size() );

        FormElement select1 = formGroups.get( 1 ).getFormElements().get( 0 );
        assertTrue( select1 instanceof SelectFormField );
        assertEquals( "selectOne1", select1.getId() );
        assertFalse( ( (SelectFormField) select1 ).isRequired() );

        FormElement input = formGroups.get( 1 ).getFormElements().get( 1 );
        assertTrue( input instanceof InputFormField );
        FormFieldPath inputPath = new FormFieldPath( "FormGroup", "text1" );
        assertEquals( inputPath, ( (InputFormField) input ).getPath() );
        InputFormField iff = (InputFormField) input;
        assertNotNull( iff.getValidation() );
        assertEquals( 5, iff.getValidation().getLength() );
        assertTrue( iff.isRequired() );

        FormElement formElement = formGroups.get( 1 ).getFormElements().get( 2 );
        assertTrue( formElement instanceof FormGroup );
        assertEquals( 5, ( (FormGroup) formElement ).getFormElements().size() );

        FormElement select = ( (FormGroup) formElement ).getFormElements().get( 3 );
        assertTrue( select instanceof SelectFormField );
        FormFieldPath selectPath = new FormFieldPath( "FormGroup", "FormGroup11", "selectOne2" );
        assertEquals( selectPath, ( (SelectFormField) select ).getPath() );
        assertFalse( ( (SelectFormField) select ).isRequired() );

    }

    @Test(expected = org.deegree.client.mdeditor.configuration.ConfigurationException.class)
    public void testdoubleIDException() {
        try {
            ConfigurationManager.getConfiguration().getConfiguration( "simple" );
        } catch ( ConfigurationException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testParseMapping()
                            throws ConfigurationException {
        FormConfiguration configuration = ConfigurationManager.getConfiguration().getConfiguration( "simple" );
        List<URL> mappings = configuration.getMappingURLs();

        assertNotNull( mappings );
        assertEquals( 1, mappings.size() );
        assertTrue( mappings.get( 0 ).getPath().endsWith( "mappingTest.xml" ) );
    }

    @Test
    public void testParseOccurence()
                            throws ConfigurationException {
        FormConfiguration configuration = ConfigurationManager.getConfiguration().getConfiguration( "simple" );
        List<FormGroup> formGroups = configuration.getFormGroups();

        assertNotNull( formGroups );
        assertTrue( formGroups.size() == 4 );

        assertEquals( "FormGroup", formGroups.get( 1 ).getId() );
        assertTrue( formGroups.get( 1 ).getOccurence() < 1 );
    }

}

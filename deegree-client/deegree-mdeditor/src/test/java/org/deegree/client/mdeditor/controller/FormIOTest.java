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
package org.deegree.client.mdeditor.controller;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.deegree.client.mdeditor.configuration.Configuration;
import org.deegree.client.mdeditor.configuration.ConfigurationException;
import org.deegree.client.mdeditor.configuration.form.FormConfigurationFactory;
import org.deegree.client.mdeditor.io.DataHandler;
import org.deegree.client.mdeditor.io.DataIOException;
import org.deegree.client.mdeditor.model.DataGroup;
import org.deegree.client.mdeditor.model.Dataset;
import org.deegree.client.mdeditor.model.FormConfiguration;
import org.deegree.client.mdeditor.model.FormGroup;
import org.deegree.client.mdeditor.model.InputFormField;
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
public class FormIOTest extends TestCase {

    @Test
    public void testDatasetWriter()
                            throws FileNotFoundException, XMLStreamException, ConfigurationException {
        Configuration.setFilesDirURL( "/home/lyn/workspace/deegree-mdeditor/tmp/test/" );
        Configuration.setFormConfURL( "/home/lyn/workspace/deegree-mdeditor/src/test/resources/org/deegree/client/mdeditor/config/simpleTestConfig.xml" );
        FormConfiguration configuration = FormConfigurationFactory.getOrCreateFormConfiguration( "test" );
        List<FormGroup> formGroups = configuration.getFormGroups();

        String v1 = "testWert";
        ( (InputFormField) formGroups.get( 0 ).getFormElements().get( 0 ) ).setValue( v1 );
        String p1 = ( (InputFormField) formGroups.get( 0 ).getFormElements().get( 0 ) ).getPath().toString();

        String v2 = "dataset";
        String[] s = new String[] { v2 };
        ( (SelectFormField) formGroups.get( 1 ).getFormElements().get( 0 ) ).setValue( s );
        String p2 = ( (SelectFormField) formGroups.get( 1 ).getFormElements().get( 0 ) ).getPath().toString();

        String v31 = "eins";
        String v32 = "zwei";
        String[] s1 = new String[] { v31, v32 };
        FormGroup fg = (FormGroup) formGroups.get( 1 ).getFormElements().get( 2 );
        ( (SelectFormField) fg.getFormElements().get( 3 ) ).setValue( s1 );
        String p3 = ( (SelectFormField) fg.getFormElements().get( 3 ) ).getPath().toString();
        List<String> v3 = new ArrayList<String>();
        v3.add( v31 );
        v3.add( v32 );

        HashMap<String, List<DataGroup>> dataGroups = new HashMap<String, List<DataGroup>>();
        FormGroup formGroup = formGroups.get( 2 );
        String grpID = formGroup.getId();
        List<DataGroup> dgs = new ArrayList<DataGroup>();
        DataGroup dg1 = new DataGroup( grpID );
        Map<String, Object> values1 = new HashMap<String, Object>();
        values1.put( "SimpleUnboundedFormGroup/in1", "dg1 1" );
        values1.put( "SimpleUnboundedFormGroup/in2", "dg1 2" );
        dg1.setValues( values1 );
        dgs.add( dg1 );

        DataGroup dg2 = new DataGroup( grpID );
        Map<String, Object> values2 = new HashMap<String, Object>();
        values2.put( "SimpleUnboundedFormGroup/in1", "dg2 1" );
        values2.put( "SimpleUnboundedFormGroup/in2", "dg2 2" );
        dg2.setValues( values2 );
        dgs.add( dg2 );

        DataGroup dg3 = new DataGroup( grpID );
        Map<String, Object> values3 = new HashMap<String, Object>();
        values3.put( "SimpleUnboundedFormGroup/in1", "dg3 1" );
        dg3.setValues( values3 );
        dgs.add( dg3 );

        dataGroups.put( formGroup.getId(), dgs );

        try {
            // write
            DataHandler.getInstance().writeDataset( "testWriting", formGroups, dataGroups );

            // and read
            Dataset dataset = DataHandler.getInstance().getDataset( "testWriting" );

            Map<String, Object> values = dataset.getValues();
            assertEquals( 4, values.size() );

            String v4 = String.valueOf( ( (InputFormField) fg.getFormElements().get( 2 ) ).getValue() );
            String p4 = ( (InputFormField) fg.getFormElements().get( 2 ) ).getPath().toString();

            assertTrue( values.containsKey( p1 ) );
            assertTrue( values.containsKey( p2 ) );
            assertTrue( values.containsKey( p3 ) );
            assertTrue( values.containsKey( p4 ) );

            assertNotNull( values.get( p1 ) );
            assertNotNull( values.get( p2 ) );
            assertNotNull( values.get( p3 ) );
            assertNotNull( values.get( p4 ) );

            assertTrue( values.get( p1 ) instanceof String );
            assertTrue( values.get( p2 ) instanceof String );
            assertTrue( values.get( p3 ) instanceof List<?> );
            assertTrue( values.get( p4 ) instanceof String );

            assertEquals( v1, values.get( p1 ) );
            assertEquals( v2, values.get( p2 ) );
            assertEquals( v3, values.get( p3 ) );
            assertEquals( v4, values.get( p4 ) );

            Map<String, List<DataGroup>> resultDG = dataset.getDataGroups();

            assertNotNull( resultDG );
            assertEquals( 1, resultDG.size() );
            assertTrue( resultDG.containsKey( grpID ) );

            List<DataGroup> list = resultDG.get( grpID );
            assertEquals( 3, list.size() );

            DataGroup dataGroup1 = list.get( 0 );
            assertNotNull( dataGroup1.getValues() );
            assertEquals( 2, dataGroup1.getValues().size() );

            DataGroup dataGroup3 = list.get( 2 );
            assertNotNull( dataGroup3.getValues() );
            assertEquals( 1, dataGroup3.getValues().size() );
            assertEquals( "dg3 1", dataGroup3.getValues().get( "SimpleUnboundedFormGroup/in1" ) );

        } catch ( DataIOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}

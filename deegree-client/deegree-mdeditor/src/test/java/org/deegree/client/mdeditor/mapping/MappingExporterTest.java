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

import java.io.File;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.apache.commons.lang.math.RandomUtils;
import org.deegree.client.mdeditor.configuration.ConfigurationException;
import org.deegree.client.mdeditor.configuration.ConfigurationManager;
import org.deegree.client.mdeditor.configuration.mapping.MappingParser;
import org.deegree.client.mdeditor.io.DataHandler;
import org.deegree.client.mdeditor.io.DataIOException;
import org.deegree.client.mdeditor.model.Dataset;
import org.deegree.client.mdeditor.model.FormConfiguration;
import org.deegree.client.mdeditor.model.FormField;
import org.deegree.client.mdeditor.model.InputFormField;
import org.deegree.client.mdeditor.model.SelectFormField;
import org.deegree.client.mdeditor.model.mapping.MappingInformation;
import org.junit.Test;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class MappingExporterTest extends TestCase {

    @Test
    public void test()
                            throws ConfigurationException, IOException, DataIOException, XMLStreamException,
                            FactoryConfigurationError {
        Map<String, FormField> formFields = prepareFormFields();

        Dataset readTestDataset = prepareDataset();
        URL url = new URL(
                           "file:///home/lyn/workspace/deegree-mdeditor/src/test/resources/org/deegree/client/mdeditor/mapping/mappingTest.xml" );
        MappingInformation mapping = MappingParser.parseMapping( url );

        File f = new File( "/home/lyn/workspace/deegree-mdeditor/tmp/test/output.xml" );
        if ( f.exists() ) {
            f.delete();
        }
        f.createNewFile();
        MappingExporter.export( f, mapping, formFields, readTestDataset.getDataGroups() );
    }

    private Dataset prepareDataset()
                            throws DataIOException, ConfigurationException {
        return DataHandler.getInstance().getDataset( "exampleDataset.xml" );
    }

    private Map<String, FormField> prepareFormFields()
                            throws ConfigurationException {
        FormConfiguration configuration = ConfigurationManager.getConfiguration().getConfiguration( "simple" );
        Map<String, FormField> formFields = configuration.getFormFields();
        for ( String path : formFields.keySet() ) {
            FormField ff = formFields.get( path );
            if ( ff instanceof InputFormField ) {
                switch ( ( (InputFormField) ff ).getInputType() ) {
                case INT:
                    ff.setValue( RandomUtils.nextInt() );
                    break;
                case TIMESTAMP:
                    ff.setValue( new Date() );
                default:
                    ff.setValue( "text" + RandomUtils.nextInt( 50 ) );
                    break;
                }
            } else if ( ff instanceof SelectFormField ) {
                SelectFormField ffSelect = (SelectFormField) ff;
                switch ( ffSelect.getSelectType() ) {
                case MANY:
                    List<String> list = new ArrayList<String>();
                    list.add( "selectMany" + RandomUtils.nextInt( 5 ) );
                    list.add( "selectMany" + RandomUtils.nextInt( 5 ) );
                    ff.setValue( list );
                    break;
                default:
                    ff.setValue( "select" + RandomUtils.nextInt( 5 ) );
                    break;
                }
            }
        }
        return formFields;
    }
}

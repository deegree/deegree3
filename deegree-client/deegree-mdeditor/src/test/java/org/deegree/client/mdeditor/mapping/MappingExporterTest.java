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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.deegree.client.mdeditor.configuration.Configuration;
import org.deegree.client.mdeditor.configuration.ConfigurationException;
import org.deegree.client.mdeditor.configuration.ConfigurationManager;
import org.deegree.client.mdeditor.configuration.mapping.MappingParser;
import org.deegree.client.mdeditor.io.DataHandler;
import org.deegree.client.mdeditor.io.DataIOException;
import org.deegree.client.mdeditor.model.DataGroup;
import org.deegree.client.mdeditor.model.Dataset;
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
                            FactoryConfigurationError, URISyntaxException {

        Configuration conf = ConfigurationManager.getConfiguration();
        assertNotNull( conf );

        Map<String, List<DataGroup>> dataGroups = prepareDataGroups();

        URL url = MappingExporterTest.class.getResource( "mappingTest.xml" );
        MappingInformation mapping = MappingParser.parseMapping( url );

        URL output = MappingExporterTest.class.getResource( "output.xml" );
        File f = new File( output.toURI() );
        if ( f.exists() ) {
            f.delete();
        }
        f.createNewFile();
        MappingExporter.export( f, mapping, conf, "simple", dataGroups );
    }

    private Map<String, List<DataGroup>> prepareDataGroups()
                            throws DataIOException, ConfigurationException {
        Dataset dataset = DataHandler.getInstance().getDataset( "exampleDataset.xml" );
        Map<String, List<DataGroup>> dataGroups = dataset.getDataGroups();
        Map<String, Object> values1 = new HashMap<String, Object>();
        values1.put( "SpatialFormGroup/country", "brd" );
        dataGroups.put( "SpatialFormGroup", Collections.singletonList( new DataGroup( "dg1", values1 ) ) );

        Map<String, Object> values2 = new HashMap<String, Object>();
        values2.put( "FormGroup3/text5", "2b2c0938-4000-437f-8466-4a312dfac170" );
        dataGroups.put( "FormGroup3", Collections.singletonList( new DataGroup( "dg2", values2 ) ) );

        Map<String, Object> values3 = new HashMap<String, Object>();
        values3.put( "FormGroup/FormGroup11/text2", "testnfff" );
        List<String> multipleSelect = new ArrayList<String>();
        multipleSelect.add( "abitur" );
        multipleSelect.add( "abfall" );
        values3.put( "FormGroup/FormGroup11/selectOne2", multipleSelect );

        values3.put( "FormGroup/text1", "defaultValue" );
        values3.put( "FormGroup/selectOne1", "dataset" );
        dataGroups.put( "FormGroup", Collections.singletonList( new DataGroup( "dg2", values3 ) ) );

        return dataGroups;
    }
}

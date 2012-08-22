// $HeadURL:
// /deegreerepository/deegree/test/org/deegree/io/datastore2/SelectBuilderTest.java,v
// 1.2 2005/05/24 15:37:33 mschneider Exp $
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
package org.deegree.io.datastore.shape;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

import junit.framework.TestCase;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGMLSchema;
import org.deegree.io.datastore.schema.MappedGMLSchemaDocument;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.GMLFeatureAdapter;
import org.deegree.model.filterencoding.AbstractFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathFactory;
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.deegree.ogcwebservices.wfs.operation.GetFeature.RESULT_TYPE;
import org.w3c.dom.Element;

import alltests.Configuration;

/**
 * Tests the correct generation of SQL select statements for the example datastore configuration.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 *
 * @author last edited by: $Author: mschneider $
 *
 * @version 2.0, $Revision: 18195 $, $Date: 2009-06-18 17:55:39 +0200 (Do, 18 Jun 2009) $
 *
 * @since 2.0
 */
public class ShapeDatastoreTest extends TestCase {
    private static ILogger LOG = LoggerFactory.getLogger( ShapeDatastoreTest.class );

    private static final String CONFIG = "wms/featuretypes/Buildings.xsd";

    private static final String FILTER = "wms/shapefile/buildings_filter.xml";

    private MappedGMLSchemaDocument doc;

    protected void setUp()
                            throws Exception {
        super.setUp();
//        fail( "Not Setting up mapped schema document -- fixme" );
        doc = new MappedGMLSchemaDocument();
        URL inputURL = new URL( Configuration.getResourceDir(), CONFIG );
        doc.load( inputURL );
    }

    protected void tearDown()
                            throws Exception {
        super.tearDown();
    }

    public void testPerformQuery()
                            throws Exception {
//        fail( "Not testing PerformQuery -- fixme" );
        QualifiedName featureType = new QualifiedName( "app", "Buildings", new URI( "http://www.deegree.org/app" ) );
        QualifiedName typTest = new QualifiedName( "app", "address", new URI( "http://www.deegree.org/app" ) );

        MappedGMLSchema schema = doc.parseMappedGMLSchema();
        assertNotNull( schema );
        assertNotNull( schema.getFeatureTypes() );

        URL filterURL = new URL( Configuration.getResourceDir(), FILTER );
        XMLFragment xml = new XMLFragment();
        xml.load( filterURL );
        Element filterRoot = xml.getRootElement();
        assertNotNull( filterRoot );

        Filter filter = AbstractFilter.buildFromDOM( filterRoot );
        assertNotNull( filter );

        PropertyPath[] paths = new PropertyPath[1];
        paths[0] = PropertyPathFactory.createPropertyPath( typTest );
        assertNotNull( paths[0] );
        assertEquals( 1, paths[0].getSteps() );

        QualifiedName[] features = new QualifiedName[1];
        features[0] = featureType;

        Query query = Query.create( paths, null, null, "handle", "1.1.0", features, null, "EPSG:4326", filter, -1, 0,
                                    RESULT_TYPE.RESULTS );
        assertNotNull( query );

        FeatureCollection result = schema.getDatastore().performQuery( query, new MappedFeatureType[]{schema.getFeatureType( featureType )} );
        assertNotNull( result );
        assertEquals( 1, result.size() );

        String resultFileName = new URL( Configuration.getWFSBaseDir(), Configuration.GENERATED_DIR
                                                                        + "/ShapeDatastoreQueryResult.xml" ).getFile();
        OutputStream outputStream = new FileOutputStream( resultFileName );
        new GMLFeatureAdapter().export( result, outputStream );
        LOG.logInfo( "Wrote '" + resultFileName + "'." );

    }
}

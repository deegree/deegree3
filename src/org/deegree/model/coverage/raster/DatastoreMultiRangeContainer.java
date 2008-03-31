//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/
package org.deegree.model.coverage.raster;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.schema.XMLSchemaException;
import org.deegree.io.datastore.Datastore;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGMLSchema;
import org.deegree.io.datastore.schema.MappedGMLSchemaDocument;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.xml.sax.SAXException;

public class DatastoreMultiRangeContainer {
    Datastore datastore;

    String baseDir = "/home/tonnhofer/daten/multidim"; // TODO

    static private final Log log = LogFactory.getLog( DatastoreMultiRangeContainer.class );

    private static final URI DEEGREEAPP = CommonNamespaces.buildNSURI( "http://www.deegree.org/app" );

    public DatastoreMultiRangeContainer( Datastore datastore ) {
        this.datastore = datastore;
    }

    public DatastoreMultiRangeContainer( String schemaFileName ) throws IOException, SAXException, XMLSchemaException,
                            XMLParsingException, UnknownCRSException {
        MappedGMLSchemaDocument schemaDoc = new MappedGMLSchemaDocument();
        schemaDoc.load( new File( schemaFileName ).toURL() );
        MappedGMLSchema gmlSchema = schemaDoc.parseMappedGMLSchema();
        this.datastore = gmlSchema.getDatastore();
    }

    public List<AbstractRaster> getRanges( Filter filter ) {
        List<AbstractRaster> result = new ArrayList<AbstractRaster>();

        QualifiedName tileTypeName = null;

        tileTypeName = new QualifiedName( "app:Tile", DEEGREEAPP );

        MappedFeatureType featureType = datastore.getFeatureType( tileTypeName );
        MappedFeatureType[] rootFts = new MappedFeatureType[] { featureType };
        Query query = Query.create( tileTypeName, filter );

        try {
            FeatureCollection fc = datastore.performQuery( query, rootFts );
            Iterator<Feature> iter = fc.iterator();
            while ( iter.hasNext() ) {
                String filename = getLocationProperty( iter.next() );
                log.debug( "filter match for " + filename );
                AbstractRaster raster = RasterFactory.createRasterFromFile( filename );
                result.add( raster );
            }
        } catch ( DatastoreException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( UnknownCRSException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Returns absolut pathname for feature property LOCATION.
     */
    private String getLocationProperty( Feature feature ) {
        QualifiedName locationQN = new QualifiedName( "app:location", DEEGREEAPP );
        FeatureProperty locationProp = feature.getProperties( locationQN )[0];

        String location = (String) locationProp.getValue();

        return baseDir + File.separator + location;
    }
}

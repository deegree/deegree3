//$HeadURL: http://svn.wald.intevation.org/svn/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.protocol.wps;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.deegree.services.wps.input.ComplexInputImpl;
import org.deegree.services.wps.provider.GMLSchema;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
public class ExampleData {

    // example data
    public static final ExampleData GML_31_MULTILPOLYGON = new ExampleData(
                                                                            ExampleData.class.getResource( "GML31_MultiPolygon.xml" ),
                                                                            GMLSchema.GML_31_GEOMETRY_SCHEMA );

    public static final ExampleData GML_31_MULTILINESTRING = new ExampleData(
                                                                              ExampleData.class.getResource( "GML31_MultiLineString.xml" ),
                                                                              GMLSchema.GML_31_GEOMETRY_SCHEMA );

    public static final ExampleData GML_31_MULTIPOINT = new ExampleData(
                                                                         ExampleData.class.getResource( "GML31_MultiPoint.xml" ),
                                                                         GMLSchema.GML_31_GEOMETRY_SCHEMA );

    public static final ExampleData GML_31_POLYGON = new ExampleData(
                                                                      ExampleData.class.getResource( "GML31_Polygon.xml" ),
                                                                      GMLSchema.GML_31_GEOMETRY_SCHEMA );

    public static final ExampleData GML_31_LINESTRING = new ExampleData(
                                                                         ExampleData.class.getResource( "GML31_LineString.xml" ),
                                                                         GMLSchema.GML_31_GEOMETRY_SCHEMA );

    public static final ExampleData GML_31_POINT = new ExampleData( ExampleData.class.getResource( "GML31_Point.xml" ),
                                                                    GMLSchema.GML_31_GEOMETRY_SCHEMA );

    public static final ExampleData GML_31_FEATURE_COLLECTION_MULTIPOLYGONS_1 = new ExampleData(
                                                                                                 ExampleData.class.getResource( "GML31_FeatureCollection_Deegree_MultiPolygons.xml" ),
                                                                                                 GMLSchema.GML_31_FEATURE_COLLECTION_SCHEMA );

    public static final ExampleData GML_31_FEATURE_COLLECTION_MULTIPOLYGONS_2 = new ExampleData(
                                                                                                 ExampleData.class.getResource( "GML31_FeatureCollection_GeoServer_MultiPolygons.xml" ),
                                                                                                 GMLSchema.GML_31_FEATURE_COLLECTION_SCHEMA );

    public static final ExampleData GML_31_FEATURE_COLLECTION_MULTILINESTRINGS_1 = new ExampleData(
                                                                                                    ExampleData.class.getResource( "GML31_FeatureCollection_Deegree_MultiLineStrings.xml" ),
                                                                                                    GMLSchema.GML_31_FEATURE_COLLECTION_SCHEMA );

    public static final ExampleData GML_31_FEATURE_COLLECTION_LINESTRINGS_1 = new ExampleData(
                                                                                               ExampleData.class.getResource( "GML31_FeatureCollection_Deegree_LineStrings.xml" ),
                                                                                               GMLSchema.GML_31_FEATURE_COLLECTION_SCHEMA );

    public static final ExampleData GML_31_FEATURE_COLLECTION_MULTIPOINTS_1 = new ExampleData(
                                                                                               ExampleData.class.getResource( "GML31_FeatureCollection_Deegree_MultiPoints.xml" ),
                                                                                               GMLSchema.GML_31_FEATURE_COLLECTION_SCHEMA );

    public static final ExampleData GML_31_FEATURE_COLLECTION_POINTS_1 = new ExampleData(
                                                                                          ExampleData.class.getResource( "GML31_FeatureCollection_Deegree_Points.xml" ),
                                                                                          GMLSchema.GML_31_FEATURE_COLLECTION_SCHEMA );

    private final URL url;

    private final GMLSchema schema;

    private final String mimeType;

    private final String encoding;

    private ExampleData( URL url, GMLSchema schema ) {
        this( url, schema, null, null );
    }

    private ExampleData( URL url, GMLSchema schema, String mimeType, String encoding ) {
        this.url = url;
        this.schema = schema;

        if ( mimeType != null )
            this.mimeType = mimeType;
        else
            this.mimeType = "text/xml";

        if ( encoding != null )
            this.encoding = encoding;
        else
            this.encoding = "UTF-8";
    }

    public String getSchema() {
        return schema.getSchema();
    }

    public String getEncoding() {
        return encoding;
    }

    public String getMimeType() {
        return mimeType;
    }

    public URL getURL() {
        return url;
    }

    public String getFilename() {
        try {
            return new File( url.toURI() ).getName();
        } catch ( Exception e ) {
            return url.getFile();
        }
    }
}

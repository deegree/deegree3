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
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import org.deegree.feature.FeatureCollection;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLVersion;
import org.deegree.services.wps.provider.sextante.GMLSchema;
import org.deegree.services.wps.provider.sextante.GMLSchema.GMLType;
import es.unex.sextante.core.GeoAlgorithm;

/**
 * This class wraps all test data as static attributes. <br>
 * A instance of this class presents one data set of test data.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
public class ExampleData {

    /**
     * 
     * This enumeration class contains all important geometry types like point, line, polygon, etc. for testing a
     * SEXTANTE {@link GeoAlgorithm}. Some algorithms need only one type, other all types of data.
     * 
     * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
     * @author last edited by: $Author: pabel $
     * 
     * @version $Revision: $, $Date: $
     */
    public enum GeometryType {
        POINT, LINE, POLYGON, MIX
    }

    // all example data
    private static final HashMap<String, ExampleData> ALL_EXAMPLE_DATA = new HashMap<String, ExampleData>();

    // example data
    public static final ExampleData GML_31_MULTILPOLYGON = new ExampleData(
                                                                            ExampleData.class.getResource( "GML31_MultiPolygon.xml" ),
                                                                            GeometryType.POLYGON,
                                                                            GMLSchema.GML_31_GEOMETRY_SCHEMA );

    public static final ExampleData GML_31_MULTILINESTRING = new ExampleData(
                                                                              ExampleData.class.getResource( "GML31_MultiLineString.xml" ),
                                                                              GeometryType.LINE,
                                                                              GMLSchema.GML_31_GEOMETRY_SCHEMA );

    public static final ExampleData GML_31_MULTIPOINT = new ExampleData(
                                                                         ExampleData.class.getResource( "GML31_MultiPoint.xml" ),
                                                                         GeometryType.POINT,
                                                                         GMLSchema.GML_31_GEOMETRY_SCHEMA );

    public static final ExampleData GML_31_POLYGON = new ExampleData(
                                                                      ExampleData.class.getResource( "GML31_Polygon.xml" ),
                                                                      GeometryType.POLYGON,
                                                                      GMLSchema.GML_31_GEOMETRY_SCHEMA );

    public static final ExampleData GML_31_LINESTRING = new ExampleData(
                                                                         ExampleData.class.getResource( "GML31_LineString.xml" ),
                                                                         GeometryType.LINE,
                                                                         GMLSchema.GML_31_GEOMETRY_SCHEMA );

    public static final ExampleData GML_31_POINT = new ExampleData( ExampleData.class.getResource( "GML31_Point.xml" ),
                                                                    GeometryType.POINT,
                                                                    GMLSchema.GML_31_GEOMETRY_SCHEMA );

    public static final ExampleData GML_31_FEATURE_COLLECTION_MULTIPOLYGONS = new ExampleData(
                                                                                               ExampleData.class.getResource( "GML31_FeatureCollection_Deegree_MultiPolygons.xml" ),
                                                                                               GeometryType.POLYGON,
                                                                                               GMLSchema.GML_31_FEATURE_COLLECTION_SCHEMA );

    public static final ExampleData GML_31_FEATURE_COLLECTION_POLYGONS = new ExampleData(
                                                                                          ExampleData.class.getResource( "GML31_FeatureCollection_Deegree_Polygons.xml" ),
                                                                                          GeometryType.POLYGON,
                                                                                          GMLSchema.GML_31_FEATURE_COLLECTION_SCHEMA );

    public static final ExampleData GML_31_FEATURE_COLLECTION_MULTILINESTRINGS = new ExampleData(
                                                                                                  ExampleData.class.getResource( "GML31_FeatureCollection_Deegree_MultiLineStrings.xml" ),
                                                                                                  GeometryType.LINE,
                                                                                                  GMLSchema.GML_31_FEATURE_COLLECTION_SCHEMA );

    public static final ExampleData GML_31_FEATURE_COLLECTION_LINESTRINGS = new ExampleData(
                                                                                             ExampleData.class.getResource( "GML31_FeatureCollection_Deegree_LineStrings.xml" ),
                                                                                             GeometryType.LINE,
                                                                                             GMLSchema.GML_31_FEATURE_COLLECTION_SCHEMA );

    public static final ExampleData GML_31_FEATURE_COLLECTION_MULTIPOINTS = new ExampleData(
                                                                                             ExampleData.class.getResource( "GML31_FeatureCollection_Deegree_MultiPoints.xml" ),
                                                                                             GeometryType.POINT,
                                                                                             GMLSchema.GML_31_FEATURE_COLLECTION_SCHEMA );

    public static final ExampleData GML_31_FEATURE_COLLECTION_POINTS = new ExampleData(
                                                                                        ExampleData.class.getResource( "GML31_FeatureCollection_Deegree_Points.xml" ),
                                                                                        GeometryType.POINT,
                                                                                        GMLSchema.GML_31_FEATURE_COLLECTION_SCHEMA );

    // URL of the data source
    private final URL url;

    // GML schema of the test data
    private final GMLSchema schema;

    // mime type of the test data
    private final String mimeType;

    // encoding of the test data
    private final String encoding;

    // geometry type of the test data
    private final GeometryType type;

    /**
     * Returns all {@link ExampleData}.
     * 
     * @return List of all {@link ExampleData}.
     */
    public static LinkedList<ExampleData> getAllData() {
        return getAllData( null );
    }

    /**
     * Returns all {@link ExampleData} without some {@link ExampleData}.
     * 
     * @param without
     *            - Array of unwanted {@link ExampleData}.
     * @return
     */
    public static LinkedList<ExampleData> getAllData( ExampleData[] without ) {
        return getData( null, without );
    }

    /**
     * Returns all {@link ExampleData} with {@link FeatureCollection}s.
     * 
     * @return List of {@link ExampleData} with {@link FeatureCollection}s.
     */
    public static LinkedList<ExampleData> getAllFeatureCollections() {

        // all test data
        LinkedList<ExampleData> collections = getAllData();

        // remove all not feature collection data
        Iterator<ExampleData> it = collections.iterator();
        while ( it.hasNext() ) {
            ExampleData data = it.next();
            if ( !data.schema.getGMLType().equals( GMLType.FEATURE_COLLECTION ) ) {
                it.remove();
            }
        }

        return collections;
    }

    /**
     * Returns all {@link ExampleData} with {@link Geometry}s.
     * 
     * @return List of {@link ExampleData} with {@link Geometry}s.
     */
    public static LinkedList<ExampleData> getAllGeometryies() {

        // all test data
        LinkedList<ExampleData> geometries = getAllData();

        // remove all not geometry data
        Iterator<ExampleData> it = geometries.iterator();
        while ( it.hasNext() ) {
            ExampleData data = it.next();
            if ( !data.schema.getGMLType().equals( GMLType.GEOMETRY ) ) {
                it.remove();
            }
        }

        return geometries;
    }

    /**
     * Returns all {@link ExampleData} with the same {@link GeometryType}.
     * 
     * @param type
     *            - {@link GeometryType} of the {@link ExampleData}.
     * 
     * @return {@link ExampleData} with the same {@link GeometryType}.
     */
    public static LinkedList<ExampleData> getData( GeometryType type ) {
        return ( getData( type, null ) );
    }

    /**
     * Returns all {@link ExampleData} with the same {@link GeometryType} and without some {@link ExampleData}.
     * 
     * @param type
     *            - {@link GeometryType} of the {@link ExampleData}.
     * @param without
     *            - Array of unwanted {@link ExampleData}.
     * @return
     */
    public static LinkedList<ExampleData> getData( GeometryType type, ExampleData[] without ) {

        LinkedList<ExampleData> allAsList = new LinkedList<ExampleData>();
        Set<String> allKeySet = ALL_EXAMPLE_DATA.keySet();

        // remove unwanted data
        Set<String> modifiedKeySet = new HashSet<String>();
        modifiedKeySet.addAll( allKeySet );
        if ( without != null )
            for ( int i = 0; i < without.length; i++ ) {
                modifiedKeySet.remove( without[i].getFilename() );
            }

        if ( type != null )
            // notice wanted data by type
            for ( String key : modifiedKeySet ) {

                ExampleData data = ALL_EXAMPLE_DATA.get( key );

                if ( data.type.equals( type ) ) {
                    allAsList.add( data );
                }

            }
        else
            // notice all wanted data
            for ( String key : modifiedKeySet ) {
                allAsList.add( ALL_EXAMPLE_DATA.get( key ) );
            }

        return allAsList;
    }

    /**
     * Creates an {@link ExampleData} object. <br>
     * Encoding will set to "UTF-8" and the mime type to "text/xml".
     * 
     * @param url
     *            - URL of the data source.
     * @param type
     *            - {@link GeometryType} of the test data.
     * @param schema
     *            - GML schema URL of the data
     */
    private ExampleData( URL url, GeometryType type, GMLSchema schema ) {
        this( url, type, schema, null, null );
    }

    /**
     * Creates an {@link ExampleData} object.
     * 
     * @param url
     *            - URL of the data source.
     * @param type
     *            - {@link GeometryType} of the test data.
     * @param schema
     *            - GML schema URL of the test data.
     * @param mimeType
     *            - Mime type of the test data.
     * @param encoding
     *            - Encoding of the test data.
     */
    private ExampleData( URL url, GeometryType type, GMLSchema schema, String mimeType, String encoding ) {
        this.url = url;
        this.schema = schema;
        this.type = type;

        if ( mimeType != null )
            this.mimeType = mimeType;
        else
            this.mimeType = "text/xml";

        if ( encoding != null )
            this.encoding = encoding;
        else
            this.encoding = "UTF-8";

        ALL_EXAMPLE_DATA.put( this.getFilename(), this );
    }

    /**
     * Returns the schema URL of the data.
     * 
     * @return Schema URL of the data.
     */
    public String getSchemaURL() {
        return schema.getSchemaURL();
    }

    /**
     * Returns the encoding of the data.
     * 
     * @return Encoding of the data.
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Returns the mime type of the data.
     * 
     * @return Mime type of the data.
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Returns the URL of the data file.
     * 
     * @return URL of the data file.
     */
    public URL getURL() {
        return url;
    }

    /**
     * Returns the {@link GMLVersion} of the data.
     * 
     * @return {@link GMLVersion} of the data.
     */
    public GMLVersion getGMLVersion() {
        return schema.getGMLVersion();
    }

    /**
     * Returns the filename of the data.
     * 
     * @return filename of the data.
     */
    public String getFilename() {
        try {
            return new File( url.toURI() ).getName();
        } catch ( Exception e ) {
            return url.getFile();
        }
    }

    public String toString() {
        String s = ExampleData.class.getSimpleName();
        s += "(" + getFilename() + ", ";
        s += type.name() + ", ";
        s += schema.getGMLType().name() + ", ";
        s += schema.getGMLVersion().name() + ", ";
        s += schema.getSchemaURL();
        s += ")";
        return s;
    }
}

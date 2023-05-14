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
import java.util.Iterator;
import java.util.LinkedList;
import org.deegree.feature.FeatureCollection;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLVersion;
import org.deegree.services.wps.provider.sextante.ExampleData;
import org.deegree.services.wps.provider.sextante.GMLSchema;
import org.deegree.services.wps.provider.sextante.GMLSchema.GMLType;
import es.unex.sextante.core.GeoAlgorithm;

/**
 * This class wraps all geometric test data as static attributes. <br>
 * A instance of this class presents one data set of test data.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * 
 */
public class VectorExampleData implements ExampleData {

    /**
     * 
     * This enumeration class contains all important geometry types like point, line, polygon, etc. for testing a
     * SEXTANTE {@link GeoAlgorithm}. Some algorithms need only one type, other all types of data.
     * 
     * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
     * 
     */
    public enum GeometryType {
        POINT, LINE, POLYGON, MIX
    }

    // all geometry example data
    private static final LinkedList<VectorExampleData> ALL_EXAMPLE_DATA = new LinkedList<VectorExampleData>();

    // example data
    public static final VectorExampleData GML_31_MULTILPOLYGON = new VectorExampleData(
                                                                                        VectorExampleData.class.getResource( "GML31_MultiPolygon.xml" ),
                                                                                        GeometryType.POLYGON,
                                                                                        GMLSchema.GML_31_GEOMETRY_SCHEMA );

    public static final VectorExampleData GML_31_MULTILINESTRING = new VectorExampleData(
                                                                                          VectorExampleData.class.getResource( "GML31_MultiLineString.xml" ),
                                                                                          GeometryType.LINE,
                                                                                          GMLSchema.GML_31_GEOMETRY_SCHEMA );

    public static final VectorExampleData GML_31_MULTIPOINT = new VectorExampleData(
                                                                                     VectorExampleData.class.getResource( "GML31_MultiPoint.xml" ),
                                                                                     GeometryType.POINT,
                                                                                     GMLSchema.GML_31_GEOMETRY_SCHEMA );

    public static final VectorExampleData GML_31_POLYGON = new VectorExampleData(
                                                                                  VectorExampleData.class.getResource( "GML31_Polygon.xml" ),
                                                                                  GeometryType.POLYGON,
                                                                                  GMLSchema.GML_31_GEOMETRY_SCHEMA );

    public static final VectorExampleData GML_31_POLYGON_2 = new VectorExampleData(
                                                                                    VectorExampleData.class.getResource( "GML31_Polygon_2.xml" ),
                                                                                    GeometryType.POLYGON,
                                                                                    GMLSchema.GML_31_GEOMETRY_SCHEMA );

    public static final VectorExampleData GML_31_LINESTRING = new VectorExampleData(
                                                                                     VectorExampleData.class.getResource( "GML31_LineString.xml" ),
                                                                                     GeometryType.LINE,
                                                                                     GMLSchema.GML_31_GEOMETRY_SCHEMA );

    public static final VectorExampleData GML_31_POINT = new VectorExampleData(
                                                                                VectorExampleData.class.getResource( "GML31_Point.xml" ),
                                                                                GeometryType.POINT,
                                                                                GMLSchema.GML_31_GEOMETRY_SCHEMA );

    public static final VectorExampleData GML_31_FEATURE_COLLECTION_MULTIPOLYGONS = new VectorExampleData(
                                                                                                           VectorExampleData.class.getResource( "GML31_FeatureCollection_deegree_MultiPolygons.xml" ),
                                                                                                           GeometryType.POLYGON,
                                                                                                           GMLSchema.GML_31_FEATURE_COLLECTION_SCHEMA );

    public static final VectorExampleData GML_31_FEATURE_COLLECTION_POLYGONS = new VectorExampleData(
                                                                                                      VectorExampleData.class.getResource( "GML31_FeatureCollection_deegree_Polygons.xml" ),
                                                                                                      GeometryType.POLYGON,
                                                                                                      GMLSchema.GML_31_FEATURE_COLLECTION_SCHEMA );

    public static final VectorExampleData GML_31_FEATURE_COLLECTION_MULTILINESTRINGS = new VectorExampleData(
                                                                                                              VectorExampleData.class.getResource( "GML31_FeatureCollection_deegree_MultiLineStrings.xml" ),
                                                                                                              GeometryType.LINE,
                                                                                                              GMLSchema.GML_31_FEATURE_COLLECTION_SCHEMA );

    public static final VectorExampleData GML_31_FEATURE_COLLECTION_LINESTRINGS = new VectorExampleData(
                                                                                                         VectorExampleData.class.getResource( "GML31_FeatureCollection_deegree_LineStrings.xml" ),
                                                                                                         GeometryType.LINE,
                                                                                                         GMLSchema.GML_31_FEATURE_COLLECTION_SCHEMA );

    public static final VectorExampleData GML_31_FEATURE_COLLECTION_MULTIPOINTS = new VectorExampleData(
                                                                                                         VectorExampleData.class.getResource( "GML31_FeatureCollection_deegree_MultiPoints.xml" ),
                                                                                                         GeometryType.POINT,
                                                                                                         GMLSchema.GML_31_FEATURE_COLLECTION_SCHEMA );

    public static final VectorExampleData GML_31_FEATURE_COLLECTION_POINTS = new VectorExampleData(
                                                                                                    VectorExampleData.class.getResource( "GML31_FeatureCollection_deegree_Points.xml" ),
                                                                                                    GeometryType.POINT,
                                                                                                    GMLSchema.GML_31_FEATURE_COLLECTION_SCHEMA );

    public static final VectorExampleData GML_31_FEATURE_COLLECTION_WFS = new VectorExampleData(
                                                                                                 VectorExampleData.class.getResource( "GML2_FeatureCollection_wfs.xml" ),
                                                                                                 GeometryType.POLYGON,
                                                                                                 GMLSchema.GML_2_FEATURE_COLLECTION_SCHEMA );

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
     * Returns all {@link VectorExampleData}.
     * 
     * @return List of all {@link VectorExampleData}.
     */
    public static LinkedList<VectorExampleData> getAllData() {
        return getAllData( null );
    }

    /**
     * Returns list of all {@link VectorExampleData} without the {@link VectorExampleData} of the array.
     * 
     * @param without
     *            Array of unwanted {@link VectorExampleData}, may be <code>null</code>.
     * 
     * @return List of all {@link VectorExampleData} without the {@link VectorExampleData} of the array. If the array of
     *         unwanted {@link VectorExampleData is <code>null</code>, returns all {@link VectorExampleData}.
     */
    public static LinkedList<VectorExampleData> getAllData( VectorExampleData[] without ) {
        return getData( null, without );
    }

    /**
     * Returns list of all {@link VectorExampleData} with {@link FeatureCollection}s.
     * 
     * @return List of {@link VectorExampleData} with {@link FeatureCollection}s.
     */
    public static LinkedList<VectorExampleData> getAllFeatureCollections() {

        // all test data
        LinkedList<VectorExampleData> collections = getAllData();

        // remove all not feature collection data
        Iterator<VectorExampleData> it = collections.iterator();
        while ( it.hasNext() ) {
            VectorExampleData data = it.next();
            if ( !data.schema.getGMLType().equals( GMLType.FEATURE_COLLECTION ) ) {
                it.remove();
            }
        }

        return collections;
    }

    /**
     * Returnslist of all {@link VectorExampleData} with {@link Geometry}s.
     * 
     * @return List of {@link VectorExampleData} with {@link Geometry}s.
     */
    public static LinkedList<VectorExampleData> getAllGeometries() {

        // all test data
        LinkedList<VectorExampleData> geometries = getAllData();

        // remove all not geometry data
        Iterator<VectorExampleData> it = geometries.iterator();
        while ( it.hasNext() ) {
            VectorExampleData data = it.next();
            if ( !data.schema.getGMLType().equals( GMLType.GEOMETRY ) ) {
                it.remove();
            }
        }

        return geometries;
    }

    /**
     * Returns list of all {@link VectorExampleData} with the same {@link GeometryType}.
     * 
     * @param type
     *            {@link GeometryType} of the {@link VectorExampleData}, may be <code>null</code>.
     * 
     * @return List of {@link VectorExampleData} with the same {@link GeometryType}. If the {@link GeometryType} is
     *         <code>null</code>, returns all {@link VectorExampleData}.
     */
    public static LinkedList<VectorExampleData> getData( GeometryType type ) {
        return ( getData( type, null ) );
    }

    /**
     * Returns list of all {@link VectorExampleData} with the same {@link GeometryType} and without some
     * {@link VectorExampleData}.
     * 
     * @param type
     *            - {@link GeometryType} of the {@link VectorExampleData}, may be <code>null</code>.
     * @param without
     *            - Array of unwanted {@link VectorExampleData}, may be <code>null</code>.
     * 
     * @return List of all {@link VectorExampleData} with the same {@link GeometryType} and without some
     *         {@link VectorExampleData}. If the Array of unwanted {@link VectorExampleData is <code>null</code>,
     *         returns all {@link VectorExampleData} with the same {@link GeometryType}. If the {@link GeometryType} is
     *         additional <code>null</code>, returns all {@link VectorExampleData}. If the {@link GeometryType} is only
     *         <code>null</code>, returns all {@link VectorExampleData} without the {@link VectorExampleData} of the
     *         array.
     */
    public static LinkedList<VectorExampleData> getData( GeometryType type, VectorExampleData[] without ) {

        LinkedList<VectorExampleData> resultData = new LinkedList<VectorExampleData>();

        // notice wanted data by type
        if ( type != null )
            for ( VectorExampleData data : ALL_EXAMPLE_DATA ) {
                if ( data.type.equals( type ) ) {
                    resultData.add( data );
                }
            }
        else
            resultData.addAll( ALL_EXAMPLE_DATA );

        // remove unwanted data
        if ( without != null )
            for ( int i = 0; i < without.length; i++ ) {
                resultData.remove( without[i] );
            }

        return resultData;
    }

    /**
     * Creates an {@link VectorExampleData} object. <br>
     * Encoding will set to "UTF-8" and the mime type to "text/xml".
     * 
     * @param url
     *            URL of the data source.
     * @param type
     *            {@link GeometryType} of the test data.
     * @param schema
     *            GML schema URL of the data
     */
    private VectorExampleData( URL url, GeometryType type, GMLSchema schema ) {
        this( url, type, schema, null, null );
    }

    /**
     * Creates an {@link VectorExampleData} object.
     * 
     * @param url
     *            URL of the data source.
     * @param type
     *            {@link GeometryType} of the test data.
     * @param schema
     *            GML schema URL of the test data.
     * @param mimeType
     *            Mime type of the test data.
     * @param encoding
     *            Encoding of the test data.
     */
    private VectorExampleData( URL url, GeometryType type, GMLSchema schema, String mimeType, String encoding ) {
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

        ALL_EXAMPLE_DATA.add( this );
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
        String s = getFilename();
        // s += "(" + type.name() + ", ";
        // s += schema.getGMLType().name() + ", ";
        // s += schema.getGMLVersion().name() + ", ";
        // s += schema.getSchemaURL();
        // s += ")";
        return s;
    }
}

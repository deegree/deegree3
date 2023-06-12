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
package org.deegree.services.wps.provider.sextante;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.deegree.gml.GMLVersion;

/**
 * Describes a GML schema with {@link URL}, {@link GMLVersion} and {@link GMLType}.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * 
 */
public class GMLSchema implements OutputFormat {

    // private static final Logger LOG = LoggerFactory.getLogger( GMLSchema.class );

    /**
     * Describes the type (like GEOMETRY or FEATURE_COLLECTION) of the GML data.
     * 
     * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
     * 
     */
    public enum GMLType {
        GEOMETRY, FEATURE_COLLECTION;
    }

    // GML schemas as map
    private static final Map<String, GMLSchema> ALL_SCHEMAS = new HashMap<String, GMLSchema>();

    // GML schemas individual
    public static final GMLSchema GML_2_GEOMETRY_SCHEMA = new GMLSchema(
                                                                         "http://schemas.opengis.net/gml/2.1.2/geometry.xsd",
                                                                         GMLVersion.GML_2, GMLType.GEOMETRY );

    public static final GMLSchema GML_30_GEOMETRY_SCHEMA = new GMLSchema(
                                                                          "http://schemas.opengis.net/gml/3.0.1/base/geometryComplexes.xsd",
                                                                          GMLVersion.GML_30, GMLType.GEOMETRY );

    public static final GMLSchema GML_31_GEOMETRY_SCHEMA = new GMLSchema(
                                                                          "http://schemas.opengis.net/gml/3.1.1/base/geometryComplexes.xsd",
                                                                          GMLVersion.GML_31, GMLType.GEOMETRY );

    public static final GMLSchema GML_32_GEOMETRY_SCHEMA = new GMLSchema(
                                                                          "http://schemas.opengis.net/gml/3.2.1/gml.xsd",
                                                                          GMLVersion.GML_32, GMLType.GEOMETRY );

    public static final GMLSchema GML_2_FEATURE_COLLECTION_SCHEMA = new GMLSchema(
                                                                                   "http://schemas.opengis.net/gml/2.1.2/feature.xsd",
                                                                                   GMLVersion.GML_2,
                                                                                   GMLType.FEATURE_COLLECTION );

    public static final GMLSchema GML_30_FEATURE_COLLECTION_SCHEMA = new GMLSchema(
                                                                                    "http://schemas.opengis.net/gml/3.0.1/base/feature.xsd",
                                                                                    GMLVersion.GML_30,
                                                                                    GMLType.FEATURE_COLLECTION );

    public static final GMLSchema GML_31_FEATURE_COLLECTION_SCHEMA = new GMLSchema(
                                                                                    "http://schemas.opengis.net/gml/3.1.1/base/feature.xsd",
                                                                                    GMLVersion.GML_31,
                                                                                    GMLType.FEATURE_COLLECTION );

    public static final GMLSchema GML_32_FEATURE_COLLECTION_SCHEMA = new GMLSchema(
                                                                                    "http://schemas.opengis.net/gml/3.2.1/feature.xsd",
                                                                                    GMLVersion.GML_32,
                                                                                    GMLType.FEATURE_COLLECTION );

    // type
    private final GMLType type;

    // schema URL
    private final String schema;

    // GML version
    private final GMLVersion version;

    /**
     * Returns a {@link GMLSchema} based on the schema URL.
     * 
     * @param schema
     *            - Schema URL.
     * @return The {@link GMLSchema} based on the schema URL
     */
    public static GMLSchema getGMLSchema( String schema ) {
        GMLSchema foundSchema = ALL_SCHEMAS.get( schema );

        if ( foundSchema != null ) {
            return foundSchema;
        } else {
            throw new IllegalArgumentException( "'" + schema + "' is a not supported GML schema." );
        }
    }

    /**
     * Returns a list of all {@link GMLSchema}s.
     * 
     * @return List of all {@link GMLSchema}s.
     */
    public static LinkedList<GMLSchema> getAllSchemas() {

        LinkedList<GMLSchema> schemas = new LinkedList<GMLSchema>();

        Set<String> keys = ALL_SCHEMAS.keySet();
        for ( String key : keys ) {
            schemas.add( ALL_SCHEMAS.get( key ) );
        }

        return schemas;
    }

    /**
     * Returns a list of all geometry {@link GMLSchema}s.
     * 
     * @return List of all geometry {@link GMLSchema}s.
     */
    public static LinkedList<GMLSchema> getGeometrySchemas() {

        LinkedList<GMLSchema> schemas = new LinkedList<GMLSchema>();

        Set<String> keys = ALL_SCHEMAS.keySet();
        for ( String key : keys ) {

            GMLSchema schema = ALL_SCHEMAS.get( key );
            if ( schema.getGMLType().equals( GMLType.GEOMETRY ) )
                schemas.add( schema );
        }

        return schemas;
    }

    /**
     * Returns a list of all feature collection {@link GMLSchema}s.
     * 
     * @return List of all feature collection {@link GMLSchema}s.
     */
    public static LinkedList<GMLSchema> getFeatureCollectionSchemas() {

        LinkedList<GMLSchema> schemas = new LinkedList<GMLSchema>();

        Set<String> keys = ALL_SCHEMAS.keySet();
        for ( String key : keys ) {

            GMLSchema schema = ALL_SCHEMAS.get( key );
            if ( schema.getGMLType().equals( GMLType.FEATURE_COLLECTION ) )
                schemas.add( schema );
        }

        return schemas;
    }

    /**
     * Creates a new {@link GMLSchema} and add the schema to map from {@link FormatHelper}.
     * 
     * @param schema
     *            Schema URL.
     * @param version
     *            {@link GMLVersion} of the schema URL.
     * @param type
     *            {@link GMLType} of the schema URL.
     * 
     */
    private GMLSchema( String schema, GMLVersion version, GMLType type ) {
        this.schema = schema;
        this.version = version;
        this.type = type;
        ALL_SCHEMAS.put( this.schema, this );
    }

    /**
     * Returns {@link GMLVersion} of the schema.
     * 
     * @return {@link GMLVersion} of the schema.
     */
    public GMLVersion getGMLVersion() {
        return version;
    }

    /**
     * Returns the schema URL.
     * 
     * @return Schema URL.
     */
    public String getSchemaURL() {
        return schema;
    }

    /**
     * Returns the {@link GMLType} of the schema.
     * 
     * @return {@link GMLType} of the schema.
     */
    public GMLType getGMLType() {
        return type;
    }

    public String toString() {
        String s = GMLSchema.class.getSimpleName() + "(";
        s += schema + ", " + type.name() + ", " + version.name() + ")";
        return s;
    }
}

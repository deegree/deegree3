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
package org.deegree.services.wps.provider;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.services.jaxb.wps.ComplexFormatType;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.output.ComplexOutput;
import org.deegree.services.wps.provider.GMLSchema.GMLType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages all supported GML schemas for SEXTANTE algorithms and provide intelligent access method.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
public class FormatHelper {

    private static final Logger LOG = LoggerFactory.getLogger( FormatHelper.class );

    // GML schemas as map
    static final Map<String, GMLSchema> ALL_SCHEMAS = new HashMap<String, GMLSchema>();

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
                                                                          "http://schemas.opengis.net/gml/3.2.1/geometryComplexes.xsd",
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
                                                                                    GMLType.FEATURE_COLLECTION );;

    /**
     * Returns the {@link GMLVersion} of the input data. <br>
     * 
     * @param input
     *            - {@link ComplexInput}, can be created with the {@link ProcessletInputs}.
     * @return It's the {@link GMLVersion} of the input data.
     */
    public static GMLVersion determineGMLVersion( ComplexInput input ) {
        GMLSchema schema = ALL_SCHEMAS.get( input.getSchema() );

        if ( schema != null ) {
            return schema.getGMLVersion();
        } else {
            LOG.error( "\"" + input.getSchema() + " \" is a not supported GML schema." );
            // TODO throw Exception
            return null;
        }
    }

    /**
     * Returns the {@link GMLVersion} of the output data. <br>
     * 
     * @param output
     *            - {@link ComplexOutput}, can be created with the {@link ProcessletOutputs}.
     * @return It's the {@link GMLVersion} of the output data.
     */
    public static GMLVersion determineGMLVersion( ComplexOutput output ) {
        GMLSchema schema = ALL_SCHEMAS.get( output.getRequestedSchema() );

        if ( schema != null ) {
            return schema.getGMLVersion();
        } else {
            LOG.error( "\"" + output.getRequestedSchema() + " \" is a not supported GML schema." );
            // TODO throw Exception
            return null;
        }
    }

    /**
     * Returns the {@link GMLType} of the input data.
     * 
     * @param input
     *            - {@link ComplexInput}, can be created with the {@link ProcessletInputs}.
     * @return It's the {@link GMLType} of the input data.
     */
    public static GMLType determineGMLType( ComplexInput input ) {
        GMLSchema schema = ALL_SCHEMAS.get( input.getSchema() );

        if ( schema != null ) {
            return schema.getGMLType();
        } else {
            LOG.error( "\"" + input.getSchema() + " \" is a not supported GML schema." );
            // TODO throw Exception
            return null;
        }
    }

    /**
     * Returns the {@link GMLType} of the output data.
     * 
     * @param output
     *            - {@link ComplexOutput}, can be created with the {@link ProcessletOutputs}
     * @return It's the GML {@link GMLType} of the output data.
     */
    public static GMLType determineGMLType( ComplexOutput output ) {
        GMLSchema schema = ALL_SCHEMAS.get( output.getRequestedSchema() );

        if ( schema != null ) {
            return schema.getGMLType();
        } else {
            LOG.error( "\"" + output.getRequestedSchema() + " \" is a not supported GML schema." );
            // TODO throw Exception
            return null;
        }
    }

    public static ComplexFormatType getDefaultInputFormat() {

        ComplexFormatType cft = new ComplexFormatType();
        cft.setEncoding( "UTF-8" );
        cft.setMimeType( "text/xml" );
        cft.setSchema( GML_2_GEOMETRY_SCHEMA.getSchema() );

        return cft;
    }

    public static ComplexFormatType getDefaultOutputFormat() {
        return getDefaultInputFormat();
    }

    public static LinkedList<ComplexFormatType> getInputFormatsWithoutDefault() {

        // default schema
        String defaultKey = getDefaultInputFormat().getSchema();

        // notice other schemas
        LinkedList<ComplexFormatType> inputCft = new LinkedList<ComplexFormatType>();
        Set<String> keys = ALL_SCHEMAS.keySet();
        for ( String key : keys ) {

            if ( !defaultKey.equals( key ) ) {
                GMLSchema gmlSchema = ALL_SCHEMAS.get( key );
                ComplexFormatType cft = new ComplexFormatType();
                cft.setEncoding( "UTF-8" );
                cft.setMimeType( "text/xml" );
                cft.setSchema( gmlSchema.getSchema() );
                inputCft.add( cft );
            }

        }

        return inputCft;
    }

    public static LinkedList<ComplexFormatType> getOutputFormatsWithoutDefault() {
        return getInputFormatsWithoutDefault();
    }

    public static String getApplicationSchema( ComplexInput input ) {
        // TODO
        return input.getSchema();
    }

    public static String getApplicationSchema( ComplexOutput output ) {
        // TODO
        return output.getRequestedSchema();
    }

    public static GMLSchema getGMLSchema( String schema ) {
        GMLSchema foundSchema = ALL_SCHEMAS.get( schema );

        if ( foundSchema != null ) {
            return foundSchema;
        } else {
            LOG.error( "\"" + schema + " \" is a not supported GML schema." );
            // TODO throw Exception
            return null;
        }

    }

}

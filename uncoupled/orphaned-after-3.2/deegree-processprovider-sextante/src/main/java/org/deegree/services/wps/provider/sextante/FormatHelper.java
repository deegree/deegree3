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

import java.util.LinkedList;

import org.deegree.gml.GMLVersion;
import org.deegree.process.jaxb.java.ComplexFormatType;
import org.deegree.services.wps.ProcessletInputs;
import org.deegree.services.wps.ProcessletOutputs;
import org.deegree.services.wps.input.ComplexInput;
import org.deegree.services.wps.output.ComplexOutput;
import org.deegree.services.wps.provider.sextante.GMLSchema.GMLType;

import es.unex.sextante.core.GeoAlgorithm;

/**
 * This class presents static methods to determine the {@link GMLVersion} and the {@link GMLType} of the input and
 * output data. Additional the class knows all supported formats of the deegree WPS for SEXTANTE {@link GeoAlgorithm}s
 * and provides corresponding get methods.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * 
 */
public class FormatHelper {

    // private static final Logger LOG = LoggerFactory.getLogger( FormatHelper.class );

    /**
     * Returns the {@link GMLVersion} of the input data.
     * 
     * @param input
     *            {@link ComplexInput}, can be created with the {@link ProcessletInputs}.
     * @return The {@link GMLVersion} of the input data.
     */
    public static GMLVersion determineGMLVersion( ComplexInput input ) {
        GMLSchema schema = GMLSchema.getGMLSchema( input.getSchema() );

        if ( schema != null ) {
            return schema.getGMLVersion();
        } else {
            throw new IllegalArgumentException( "INPUT: '" + input.getSchema() + "' is a not supported GML schema." );
        }
    }

    /**
     * Returns the {@link GMLVersion} of the output data.
     * 
     * @param output
     *            {@link ComplexOutput}, can be created with the {@link ProcessletOutputs}.
     * @return {@link GMLVersion} of the output data. If this method can't determine the {@link GMLVersion}, returns
     *         default {@link GMLVersion}.
     */
    public static GMLVersion determineGMLVersion( ComplexOutput output ) {
        GMLSchema schema = GMLSchema.getGMLSchema( output.getRequestedSchema() );

        if ( schema != null ) {
            return schema.getGMLVersion();
        } else {
            throw new IllegalArgumentException( "OUTPUT: '" + output.getRequestedSchema()
                                                + "' is a not supported GML schema." );
        }
    }

    /**
     * Returns the {@link GMLType} of the input data.
     * 
     * @param input
     *            {@link ComplexInput}, can be created with the {@link ProcessletInputs}.
     * @return The {@link GMLType} of the input data.
     */
    public static GMLType determineGMLType( ComplexInput input ) {
        GMLSchema schema = GMLSchema.getGMLSchema( input.getSchema() );

        if ( schema != null ) {
            return schema.getGMLType();
        } else {
            throw new IllegalArgumentException( "INPUT: '" + input.getSchema() + "' is a not supported GML schema." );
        }
    }

    /**
     * Returns the {@link GMLType} of the output data.
     * 
     * @param output
     *            - {@link ComplexOutput}, can be created with the {@link ProcessletOutputs}
     * @return The GML {@link GMLType} of the output data. If this method can't determine the {@link GMLVersion},
     *         returns default {@link GMLVersion}.
     */
    public static GMLType determineGMLType( ComplexOutput output ) {
        GMLSchema schema = GMLSchema.getGMLSchema( output.getRequestedSchema() );

        if ( schema != null ) {
            return schema.getGMLType();
        } else {
            throw new IllegalArgumentException( "OUTPUT: '" + output.getRequestedSchema()
                                                + " ' is a not supported GML schema." );
        }
    }

    /**
     * Returns the {@link ComplexFormatType} of the default input schema.
     * 
     * @return The {@link ComplexFormatType} of the default input schema
     */
    public static ComplexFormatType getDefaultInputFormat() {

        ComplexFormatType cft = new ComplexFormatType();
        cft.setEncoding( "UTF-8" );
        cft.setMimeType( "text/xml" );
        cft.setSchema( GMLSchema.GML_2_GEOMETRY_SCHEMA.getSchemaURL() );

        return cft;
    }

    /**
     * Returns the {@link ComplexFormatType} of the default output schema.
     * 
     * @return The {@link ComplexFormatType} of the default output schema
     */
    public static ComplexFormatType getDefaultOutputFormat() {
        return getDefaultInputFormat();
    }

    /**
     * Returns the {@link ComplexFormatType}s of all input schemas without the default schema.
     * 
     * @return The {@link ComplexFormatType} of all input schemas without the default schema.
     */
    public static LinkedList<ComplexFormatType> getInputFormatsWithoutDefault() {

        // default schema
        String defaultKey = getDefaultInputFormat().getSchema();

        // notice other schemas
        LinkedList<ComplexFormatType> inputCft = new LinkedList<ComplexFormatType>();

        LinkedList<GMLSchema> schemas = GMLSchema.getAllSchemas();
        for ( GMLSchema gmlSchema : schemas ) {
            String schema = gmlSchema.getSchemaURL();
            if ( !defaultKey.equals( schema ) ) {
                ComplexFormatType cft = new ComplexFormatType();
                cft.setEncoding( "UTF-8" );
                cft.setMimeType( "text/xml" );
                cft.setSchema( schema );
                inputCft.add( cft );
            }
        }

        return inputCft;
    }

    /**
     * Returns the {@link ComplexFormatType}s of all output schemas without the default schema.
     * 
     * @return The {@link ComplexFormatType} of all output schemas without the default schema.
     */
    public static LinkedList<ComplexFormatType> getOutputFormatsWithoutDefault() {
        return getInputFormatsWithoutDefault();
    }

}

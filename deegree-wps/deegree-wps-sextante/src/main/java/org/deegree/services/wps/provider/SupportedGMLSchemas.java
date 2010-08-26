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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.lang.reflect.Field;
import org.deegree.gml.GMLVersion;
import org.deegree.services.wps.provider.GMLSchema.GMLSchemaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * @author last edited by: $Author: pabel $
 * 
 * @version $Revision: $, $Date: $
 */
public class SupportedGMLSchemas {

    private static final Logger LOG = LoggerFactory.getLogger( SupportedGMLSchemas.class );

    static Map<String, GMLSchema> ALL_SCHEMAS = new HashMap<String, GMLSchema>();

    public static final GMLSchema GML_2_GEOMETRY_SCHEMA = new GMLSchema(
                                                                         "http://schemas.opengis.net/gml/2.1.2/geometry.xsd",
                                                                         GMLVersion.GML_2, GMLSchemaType.GEOMETRY );

    public static final GMLSchema GML_30_GEOMETRY_SCHEMA = new GMLSchema(
                                                                          "http://schemas.opengis.net/gml/3.0.1/base/geometryComplexes.xsd",
                                                                          GMLVersion.GML_30, GMLSchemaType.GEOMETRY );

    public static final GMLSchema GML_31_GEOMETRY_SCHEMA = new GMLSchema(
                                                                          "http://schemas.opengis.net/gml/3.1.1/base/geometryComplexes.xsd",
                                                                          GMLVersion.GML_31, GMLSchemaType.GEOMETRY );

    public static final GMLSchema GML_32_GEOMETRY_SCHEMA = new GMLSchema(
                                                                          "http://schemas.opengis.net/gml/3.2.1/geometryComplexes.xsd",
                                                                          GMLVersion.GML_32, GMLSchemaType.GEOMETRY );

    public static final GMLSchema GML_2_FEATURE_COLLECTION_SCHEMA = new GMLSchema(
                                                                                   "http://schemas.opengis.net/gml/2.1.2/feature.xsd",
                                                                                   GMLVersion.GML_2,
                                                                                   GMLSchemaType.FEATURE_COLLECTION );

    public static final GMLSchema GML_30_FEATURE_COLLECTION_SCHEMA = new GMLSchema(
                                                                                    "http://schemas.opengis.net/gml/3.0.1/base/feature.xsd",
                                                                                    GMLVersion.GML_30,
                                                                                    GMLSchemaType.FEATURE_COLLECTION );

    public static final GMLSchema GML_31_FEATURE_COLLECTION_SCHEMA = new GMLSchema(
                                                                                    "http://schemas.opengis.net/gml/3.1.1/base/feature.xsd",
                                                                                    GMLVersion.GML_31,
                                                                                    GMLSchemaType.FEATURE_COLLECTION );

    public static final GMLSchema GML_32_FEATURE_COLLECTION_SCHEMA = new GMLSchema(
                                                                                    "http://schemas.opengis.net/gml/3.2.1/feature.xsd",
                                                                                    GMLVersion.GML_32,
                                                                                    GMLSchemaType.FEATURE_COLLECTION );;

    public static GMLSchema getDefaultInputSchema() {
        return GML_2_GEOMETRY_SCHEMA;
    }

    public static GMLSchema getDefaultOutputSchema() {
        return GML_2_GEOMETRY_SCHEMA;
    }

    public static GMLVersion getGMLVersion( String schema ) {
        GMLVersion version = ALL_SCHEMAS.get( schema ).getGMLVersion();

        if ( version == null ) {
            LOG.error( "\"" + schema + " \" is a not supported GML schema." );
            // TODO throw Exception
        }

        return version;

    }

    public static GMLSchema getGMLSchema( String schema ) {
        GMLSchema foundSchema = ALL_SCHEMAS.get( schema );

        if ( foundSchema == null ) {
            LOG.error( "\"" + schema + " \" is a not supported GML schema." );
            // TODO throw Exception
        }

        return foundSchema;

    }
}

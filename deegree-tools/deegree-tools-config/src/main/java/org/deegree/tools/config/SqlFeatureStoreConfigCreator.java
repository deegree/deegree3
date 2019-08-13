/*-
 * #%L
 * deegree-cli-utility
 * %%
 * Copyright (C) 2016 - 2017 weichand.de, lat/lon GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.deegree.tools.config;

import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.annotations.Tool;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.feature.persistence.sql.GeometryStorageParams;
import org.deegree.feature.persistence.sql.MappedAppSchema;
import org.deegree.feature.persistence.sql.config.SQLFeatureStoreConfigWriter;
import org.deegree.feature.persistence.sql.ddl.DDLCreator;
import org.deegree.feature.persistence.sql.mapper.AppSchemaMapper;
import org.deegree.feature.types.AppSchema;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.deegree.sqldialect.SQLDialect;
import org.deegree.sqldialect.oracle.OracleDialect;
import org.deegree.sqldialect.postgis.PostGISDialect;

/**
 * CLI utility
 * 
 * @author Juergen Weichand
 */
@Tool(value = "Creates SQLFeatureStore configuration and DDL from a GML application schema")
public class SqlFeatureStoreConfigCreator {

    private static final PropertyNameParser propertyNameParser = new PropertyNameParser();

    // default values
    private static String format = "deegree"; // generates deegree SQLFeatureStore config file only

    private static int srid = 4258; // uses EPSG:4258

    private static boolean useIntegerFids = true; // uses FIDMapping with integer type

    private static boolean relationalMapping = true; // generates relational mapping derived from GML application schema

    private static String dialect = "postgis"; // generates mapping for PostGIS dialect

    private static SQLDialect sqlDialect = instantiateDialect( null ); // generates mapping for PostGIS dialect (per
                                                                       // default)

    private static int depth = 0;

    private static List<QName> propertiesWithPrimitiveHref; // primitive href mapping instead of feature mapping is used
                                                            // in deegree configuration for listed properties

    public static void main( String[] args )
                    throws Exception {

        if ( args.length == 0 ) {
            System.out.println( "Usage: java -jar deegree-cli-utility.jar [options] schema_url" );
            System.out.println( "" );
            System.out.println( "options:" );
            System.out.println( " --format={deegree|ddl|all}" );
            System.out.println( " --srid=<epsg_code>" );
            System.out.println( " --idtype={int|uuid}" );
            System.out.println( " --mapping={relational|blob}" );
            System.out.println( " --dialect={postgis|oracle}" );
            System.out.println( " --cycledepth=INT (positive integer value to specify the depth of cycles; default: 0)" );
            System.out.println( " --listOfPropertiesWithPrimitiveHref=<path/to/file>" );
            System.out.println( "" );
            System.out.println( "The option listOfPropertiesWithPrimitiveHref references a file listing properties which are written with primitive instead of feature mappings (see deegree-webservices documentation and README of this tool for further information):" );
            System.out.println( "---------- begin file ----------" );
            System.out.println( "# lines beginning with an # are ignored" );
            System.out.println( "# property with namespace binding" );
            System.out.println( "{http://inspire.ec.europa.eu/schemas/ps/4.0}designation" );
            System.out.println( "# property without namespace binding" );
            System.out.println( "designation" );
            System.out.println( "# empty lines are ignored" );
            System.out.println( "" );
            System.out.println( "# leading and trailing white spaces are ignored" );
            System.out.println( "---------- end file ----------" );
            return;
        }

        String schemaUrl = "";
        for ( String arg : args ) {
            if ( arg.startsWith( "--format" ) ) {
                format = arg.split( "=" )[1];
                System.out.println( "Using format=" + format );
            } else if ( arg.startsWith( "--srid" ) ) {
                srid = Integer.valueOf( arg.split( "=" )[1] );
                System.out.println( "Using srid=" + srid );
            } else if ( arg.startsWith( "--idtype" ) ) {
                String idMappingArg = arg.split( "=" )[1];
                useIntegerFids = idMappingArg.equals( "uuid" ) ? false : true;
                System.out.println( "Using idtype=" + idMappingArg );
            } else if ( arg.startsWith( "--mapping" ) ) {
                String mapping = arg.split( "=" )[1];
                relationalMapping = mapping.equalsIgnoreCase( "blob" ) ? false : true;
                System.out.println( "Using mapping=" + mapping );
            } else if ( arg.startsWith( "--dialect" ) ) {
                String dialect = arg.split( "=" )[1];
                sqlDialect = instantiateDialect( dialect );
                System.out.println( "Using dialect=" + dialect );
            } else if ( arg.startsWith( "--cycledepth" ) ) {
                String depthAsString = arg.split( "=" )[1];
                depth = Integer.parseInt( depthAsString );
                System.out.println( "Using cycledepth=" + depth );
            } else if ( arg.startsWith( "--listOfPropertiesWithPrimitiveHref" ) ) {
                String pathToFile = arg.split( "=" )[1];
                propertiesWithPrimitiveHref = propertyNameParser.parsePropertiesWithPrimitiveHref( pathToFile );
                System.out.println( "Using listOfPropertiesWithPrimitiveHref=" + propertiesWithPrimitiveHref );
            } else {
                schemaUrl = arg;
            }
        }

        String[] schemaUrls = { schemaUrl };
        GMLAppSchemaReader xsdDecoder = new GMLAppSchemaReader( null, null, schemaUrls );
        AppSchema appSchema = xsdDecoder.extractAppSchema();

        CRSRef storageCrs = CRSManager.getCRSRef( "EPSG:" + String.valueOf( srid ) );
        GeometryStorageParams geometryParams = new GeometryStorageParams( storageCrs, String.valueOf( srid ), DIM_2 );
        AppSchemaMapper mapper = new AppSchemaMapper( appSchema, !relationalMapping, relationalMapping, geometryParams,
                                                      sqlDialect.getMaxColumnNameLength(), true, useIntegerFids,
                                                      depth );
        MappedAppSchema mappedSchema = mapper.getMappedSchema();
        SQLFeatureStoreConfigWriter configWriter = new SQLFeatureStoreConfigWriter( mappedSchema,
                        propertiesWithPrimitiveHref );
        String uriPathToSchema = new URI( schemaUrl ).getPath();
        String schemaFileName = uriPathToSchema.substring( uriPathToSchema.lastIndexOf( '/' ) + 1 );
        String fileName = schemaFileName.replaceFirst( "[.][^.]+$", "" );

        if ( format.equals( "all" ) ) {
            writeSqlDdlFile( mappedSchema, fileName );
            writeXmlConfigFile( schemaUrls, configWriter, fileName );
        } else if ( format.equals( "deegree" ) ) {
            writeXmlConfigFile( schemaUrls, configWriter, fileName );
        } else if ( format.equals( "ddl" ) ) {
            writeSqlDdlFile( mappedSchema, fileName );
        }

    }

    private static void writeSqlDdlFile( MappedAppSchema mappedSchema, String fileName )
                    throws IOException {
        String[] createStmts = DDLCreator.newInstance( mappedSchema, sqlDialect ).getDDL();
        String sqlOutputFilename = "./" + fileName + ".sql";
        Path pathToSqlOutputFile = Paths.get( sqlOutputFilename );
        System.out.println( "Writing SQL DDL into file: " + pathToSqlOutputFile.toUri() );
        try ( BufferedWriter writer = Files.newBufferedWriter( pathToSqlOutputFile ) ) {
            for ( String sqlStatement : createStmts ) {
                writer.write( sqlStatement + ";" + System.getProperty( "line.separator" ) );
            }
        }
    }

    private static void writeXmlConfigFile( String[] schemaUrls, SQLFeatureStoreConfigWriter configWriter,
                                            String fileName )
                    throws XMLStreamException, IOException {
        List<String> configUrls = Arrays.asList( schemaUrls );
        String xmlOutputFilename = "./" + fileName + ".xml";
        Path pathToXmlOutputFile = Paths.get( xmlOutputFilename );
        System.out.println( "Writing deegree SQLFeatureStore configuration into file: " + pathToXmlOutputFile.toUri() );
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( bos );
        xmlWriter = new IndentingXMLStreamWriter( xmlWriter );
        configWriter.writeConfig( xmlWriter, fileName+"DS", configUrls );
        xmlWriter.close();
        Files.write( pathToXmlOutputFile, bos.toString().getBytes( StandardCharsets.UTF_8 ) );
    }

    private static SQLDialect instantiateDialect( String dialect ) {
        if ( dialect != null && "oracle".equalsIgnoreCase( dialect ) )
            return new OracleDialect( "", 11, 2 );
        return new PostGISDialect( "2.0.0" );
    }

}
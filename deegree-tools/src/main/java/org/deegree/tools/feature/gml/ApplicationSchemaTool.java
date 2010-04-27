//$HeadURL$
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
package org.deegree.tools.feature.gml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.feature.persistence.postgis.FeatureTypeMapping;
import org.deegree.feature.persistence.postgis.JAXBApplicationSchemaAdapter;
import org.deegree.feature.persistence.postgis.PostGISApplicationSchema;
import org.deegree.feature.persistence.postgis.jaxbconfig.ApplicationSchemaDecl;
import org.deegree.feature.persistence.postgis.jaxbconfig.CustomPropertyMappingType;
import org.deegree.feature.persistence.postgis.jaxbconfig.DBColumn;
import org.deegree.feature.persistence.postgis.jaxbconfig.FeatureJoinTable;
import org.deegree.feature.persistence.postgis.jaxbconfig.FeaturePropertyMappingType;
import org.deegree.feature.persistence.postgis.jaxbconfig.FeatureTypeMappingHints;
import org.deegree.feature.persistence.postgis.jaxbconfig.GeometryDBColumn;
import org.deegree.feature.persistence.postgis.jaxbconfig.GeometryPropertyMappingType;
import org.deegree.feature.persistence.postgis.jaxbconfig.GeometryPropertyTable;
import org.deegree.feature.persistence.postgis.jaxbconfig.GlobalMappingHints;
import org.deegree.feature.persistence.postgis.jaxbconfig.MeasurePropertyMappingType;
import org.deegree.feature.persistence.postgis.jaxbconfig.PropertyMappingType;
import org.deegree.feature.persistence.postgis.jaxbconfig.PropertyTable;
import org.deegree.feature.persistence.postgis.jaxbconfig.SimplePropertyMappingType;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.MeasurePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.schema.ApplicationSchemaXSDDecoder;
import org.deegree.tools.CommandUtils;
import org.deegree.tools.annotations.Tool;
import org.deegree.tools.i18n.Messages;

/**
 * Swiss Army knife for GML/deegree application schemas.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
@Tool("Swiss Army knife for GML/deegree application schemas.")
public class ApplicationSchemaTool {

    // command line parameters
    private static final String OPT_ACTION = "action";

    private static final String OPT_INPUT_FILE = "inputfile";

    private static final String OPT_INPUT_FORMAT = "inputformat";

    private static final String OPT_RULES_FILE = "rulesfile";

    private static final String OPT_DB_SCHEMA = "dbschema";

    private enum Action {
        /** Print defined feature types */
        analyze,
        /** Extract SQL script for creating relational schema */
        create_ddl,
        /** Create mapped application schema (PostGIS) */
        map_to_postgis,
        /** Create mapped application schema (Oracle) */
        map_to_oracle;
    }

    private enum InputFormat {
        /** GML 2 application schema */
        gml2,
        /** GML 3.0/3.1 application schema */
        gml31,
        /** GML 3.2 application schema */
        gml32,
        /** deegree application schema */
        deegree,
        /** deegree application schema mapped to PostGIS */
        deegree_postgis,
        /** deegree application schema mapped to Oracle Spatial */
        deegree_oracle,
        /** deegree application schema mapped to an ESRI Shapefile */
        deegree_shape;
    }

    private static void analyze( InputFormat inputFormat, String inputFileName )
                            throws ClassCastException, MalformedURLException, ClassNotFoundException,
                            InstantiationException, IllegalAccessException, JAXBException {
        ApplicationSchema schema = loadAppSchema( inputFormat, inputFileName );

        Set<String> ftNames = new TreeSet<String>();
        Set<String> propNames = new TreeSet<String>();
        printFtHierarchy( schema, ftNames, propNames );

        System.out.println( ftNames.size() + " feature type names" );
        for ( String ftName : ftNames ) {
            System.out.println( ftName );
        }
        System.out.println( propNames.size() + " property names" );
        for ( String propName : propNames ) {
            System.out.println( propName );
        }
    }

    private static void mapToPostGIS( InputFormat inputFormat, String inputFileName, String rulesFileName )
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException, JAXBException, IOException {

        LinkedHashMap<String, String> rules = new LinkedHashMap<String, String>();
        if ( rulesFileName != null ) {
            BufferedReader reader = new BufferedReader( new FileReader( rulesFileName ) );
            String line = null;
            while ( ( line = reader.readLine() ) != null ) {
                line = line.trim();
                int indexOfDelim = line.indexOf( '=' );
                if ( indexOfDelim != -1 ) {
                    String from = line.substring( 0, indexOfDelim );
                    String to = line.substring( indexOfDelim + 1, line.length() );
                    rules.put( from, to );
                }
            }
            System.err.println( "Loaded " + rules.size() + " replacement rule(s) from " + rulesFileName );
        } else {
            System.err.println( "Continuing without rules file." );
        }

        ApplicationSchema schema = loadAppSchema( inputFormat, inputFileName );

        Map<QName, FeatureTypeMapping> ftNameToHints = new HashMap<QName, FeatureTypeMapping>();
        FeatureType[] fts = schema.getFeatureTypes();
        Arrays.sort( fts, new Comparator<FeatureType>() {
            @Override
            public int compare( FeatureType o1, FeatureType o2 ) {
                return o1.getName().toString().compareTo( o2.getName().toString() );
            }
        } );
        for ( FeatureType ft : fts ) {
            ftNameToHints.put( ft.getName(), getFtHints( schema, ft, rules ) );
        }

        PostGISApplicationSchema postgisSchema = new PostGISApplicationSchema( schema, getGlobalHints( schema ),
                                                                               ftNameToHints );
        ApplicationSchemaDecl jaxbSchema = JAXBApplicationSchemaAdapter.toJAXB( postgisSchema );
        JAXBContext jc = JAXBContext.newInstance( "org.deegree.feature.persistence.postgis.jaxbconfig" );
        Marshaller m = jc.createMarshaller();
        m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
        m.setProperty( Marshaller.JAXB_SCHEMA_LOCATION,
                       "http://www.deegree.org/feature/featuretype http://schemas.deegree.org/feature/0.5.0/postgis_appschema.xsd" );
        m.marshal( jaxbSchema, System.out );
    }

    private static String shortenName( String s, LinkedHashMap<String, String> rules ) {
        String shortened = s;
        for ( Entry<?, ?> rule : rules.entrySet() ) {
            String from = (String) rule.getKey();
            String to = (String) rule.getValue();
            shortened = shortened.replaceAll( from, to );
        }
        return shortened;
    }

    private static FeatureTypeMapping getFtHints( ApplicationSchema schema, FeatureType ft,
                                                  LinkedHashMap<String, String> rules ) {

        String featureTable = shortenName( ft.getName().getLocalPart().toLowerCase(), rules );
        FeatureTypeMappingHints ftHints = new FeatureTypeMappingHints();
        ftHints.setDBTable( featureTable );
        ftHints.setGMLDefaultProps( true );
        Map<QName, PropertyMappingType> propNameToHint = new HashMap<QName, PropertyMappingType>();
        int multiPropId = 0;
        for ( PropertyType pt : ft.getPropertyDeclarations() ) {
            String propName = shortenName( pt.getName().getLocalPart().toLowerCase(), rules );
            String qPropName = featureTable + "_multi" + multiPropId;
            try {
                propNameToHint.put( pt.getName(), getPropertyHints( featureTable, pt, rules, propName, qPropName ) );
            } catch ( RuntimeException e ) {
                System.err.println( "Omitting property " + pt.getName() + " (class)" + pt.getClass() + "..." );
            }
            if ( pt.getMaxOccurs() != 1 ) {
                multiPropId++;
            }
        }
        return new FeatureTypeMapping( ftHints, propNameToHint );
    }

    private static PropertyMappingType getPropertyHints( String featureTable, PropertyType pt,
                                                         LinkedHashMap<String, String> rules, String propName,
                                                         String qPropName ) {

        PropertyMappingType hints = null;
        if ( pt instanceof SimplePropertyType ) {
            hints = new SimplePropertyMappingType();
            String postgisType = getPostGISType( ( (SimplePropertyType) pt ).getPrimitiveType() );
            if ( pt.getMaxOccurs() != 1 ) {
                PropertyTable propTable = new PropertyTable();
                propTable.setTable( qPropName );
                propTable.setColumn( "value" );
                propTable.setSqlType( postgisType );
                ( (SimplePropertyMappingType) hints ).setPropertyTable( propTable );
            } else {
                DBColumn dbColumn = new DBColumn();
                dbColumn.setName( propName );
                dbColumn.setSqlType( postgisType );
                ( (SimplePropertyMappingType) hints ).setDBColumn( dbColumn );
            }
        } else if ( pt instanceof GeometryPropertyType ) {
            hints = new GeometryPropertyMappingType();
            // TODO
            String postgisType = "GEOMETRY";
            if ( pt.getMaxOccurs() != 1 ) {
                GeometryPropertyTable propTable = new GeometryPropertyTable();
                propTable.setTable( qPropName );
                propTable.setColumn( "value" );
                // TODO
                propTable.setDimension( new BigInteger( "2" ) );
                // TODO
                propTable.setSrid( new BigInteger( "31466" ) );
                propTable.setSqlType( postgisType );
                ( (GeometryPropertyMappingType) hints ).setGeometryPropertyTable( propTable );
            } else {
                GeometryDBColumn dbColumn = new GeometryDBColumn();
                dbColumn.setName( propName );
                dbColumn.setSqlType( postgisType );
                // TODO
                dbColumn.setDimension( new BigInteger( "2" ) );
                // TODO
                dbColumn.setSrid( new BigInteger( "31466" ) );
                ( (GeometryPropertyMappingType) hints ).setGeometryDBColumn( dbColumn );
            }
        } else if ( pt instanceof FeaturePropertyType ) {
            hints = new FeaturePropertyMappingType();
            if ( pt.getMaxOccurs() != 1 ) {
                FeatureJoinTable propTable = new FeatureJoinTable();
                propTable.setTable( qPropName );
                ( (FeaturePropertyMappingType) hints ).setFeatureJoinTable( propTable );
            } else {
                DBColumn dbColumn = new DBColumn();
                // TODO
                dbColumn.setName( propName );
                dbColumn.setSqlType( "integer" );
                ( (FeaturePropertyMappingType) hints ).setDBColumn( dbColumn );
            }
        } else if ( pt instanceof MeasurePropertyType ) {
            hints = new MeasurePropertyMappingType();
            if ( pt.getMaxOccurs() != 1 ) {
                PropertyTable propTable = new PropertyTable();
                propTable.setTable( qPropName );
                propTable.setColumn( "value" );
                ( (MeasurePropertyMappingType) hints ).setPropertyTable( propTable );
            } else {
                DBColumn dbColumn = new DBColumn();
                dbColumn.setName( propName );
                dbColumn.setSqlType( "double precision" );
                ( (MeasurePropertyMappingType) hints ).setDBColumn( dbColumn );
            }
        } else if ( pt instanceof CustomPropertyType ) {
            hints = new CustomPropertyMappingType();
            ( (CustomPropertyMappingType) hints ).setXsdType( ( (CustomPropertyType) pt ).getXSDValueType() );
        }
        return hints;
    }

    private static String getPostGISType( PrimitiveType primitiveType ) {
        String postgisType = null;
        switch ( primitiveType ) {
        case BOOLEAN: {
            postgisType = "boolean";
            break;
        }
        case DATE: {
            postgisType = "date";
            break;
        }
        case DATE_TIME: {
            postgisType = "timestamp with time zone";
            break;
        }
        case DECIMAL: {
            postgisType = "decimal";
            break;
        }
        case INTEGER: {
            postgisType = "bigint";
            break;
        }
        case DOUBLE: {
            postgisType = "double precision";
            break;
        }
        case STRING: {
            postgisType = "text";
            break;
        }
        case TIME: {
            postgisType = "time with time zone";
            break;
        }
        }
        return postgisType;
    }

    private static GlobalMappingHints getGlobalHints( ApplicationSchema schema ) {
        GlobalMappingHints hints = new GlobalMappingHints();
        hints.setUseObjectLookupTable( true );
        hints.setJDBCConnId( "conn1" );
        return hints;
    }

    private static void mapToOracle( InputFormat inputFormat, String inputFileName ) {
        System.out.println( "Not implemented yet." );
    }

    private static void createDDL( InputFormat inputFormat, String inputFileName, String dbSchema )
                            throws JAXBException {
        switch ( inputFormat ) {
        case deegree_oracle:
            System.out.println( "Not implemented yet." );
            break;
        case deegree_postgis:
            PostGISApplicationSchema postgisSchema = loadPostGISSchema( inputFileName );
            postgisSchema.writeCreateScript( dbSchema, new PrintWriter( System.out ) );
            System.out.flush();
            break;
        default: {
            System.out.println( "Action " + Action.create_ddl + " is only supported for "
                                + InputFormat.deegree_postgis.name() + " and " + InputFormat.deegree_oracle.name()
                                + " input formats." );
            return;
        }
        }
    }

    private static ApplicationSchema loadAppSchema( InputFormat inputFormat, String inputFileName )
                            throws ClassCastException, MalformedURLException, ClassNotFoundException,
                            InstantiationException, IllegalAccessException, JAXBException {
        ApplicationSchema schema = null;
        switch ( inputFormat ) {
        case deegree:
            break;
        case deegree_oracle:
            break;
        case deegree_postgis: {
            schema = loadPostGISSchema( inputFileName ).getSchema();
            break;
        }
        case deegree_shape:
            break;
        case gml2: {
            String inputURL = new File( inputFileName ).toURI().toURL().toString();
            ApplicationSchemaXSDDecoder decoder = new ApplicationSchemaXSDDecoder( GMLVersion.GML_2, null, inputURL );
            schema = decoder.extractFeatureTypeSchema();
            break;
        }
        case gml31: {
            String inputURL = new File( inputFileName ).toURI().toURL().toString();
            ApplicationSchemaXSDDecoder decoder = new ApplicationSchemaXSDDecoder( GMLVersion.GML_31, null, inputURL );
            schema = decoder.extractFeatureTypeSchema();
            System.err.println( "Mangled types:" );
            for ( QName typeName : decoder.getAllEncounteredTypes() ) {
                System.err.println( "- " + typeName );
            }
            break;
        }
        case gml32: {
            String inputURL = new File( inputFileName ).toURI().toURL().toString();
            ApplicationSchemaXSDDecoder decoder = new ApplicationSchemaXSDDecoder( GMLVersion.GML_32, null, inputURL );
            schema = decoder.extractFeatureTypeSchema();
            break;
        }
        }
        return schema;
    }

    private static PostGISApplicationSchema loadPostGISSchema( String inputFileName )
                            throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance( "org.deegree.feature.persistence.postgis.jaxbconfig" );
        Unmarshaller u = jc.createUnmarshaller();
        ApplicationSchemaDecl jaxbAppSchema = (ApplicationSchemaDecl) u.unmarshal( new File( inputFileName ) );
        PostGISApplicationSchema mappedSchema = JAXBApplicationSchemaAdapter.toInternal( jaxbAppSchema );
        return mappedSchema;
    }

    private static void printFtHierarchy( ApplicationSchema schema, Set<String> ftNames, Set<String> propNames ) {
        int concrete = 0;
        for ( FeatureType ft : schema.getFeatureTypes() ) {
            if ( !ft.isAbstract() ) {
                concrete++;
            }
            printFt( ft, ftNames, propNames );
        }
        System.out.println( schema.getFeatureTypes().length + " feature types (" + concrete + " concrete)" );
    }

    private static void printFt( FeatureType ft, Set<String> ftNames, Set<String> propNames ) {
        System.out.println( "\n - feature type '" + ft.getName() + "'" + ( ft.isAbstract() ? " (abstract)" : "" ) );
        ftNames.add( ft.getName().getLocalPart() );
        for ( PropertyType pt : ft.getPropertyDeclarations() ) {
            propNames.add( pt.getName().getLocalPart() );
            String ptName = pt.getName().getNamespaceURI().equals( ft.getName().getNamespaceURI() ) ? pt.getName().getLocalPart()
                                                                                                   : pt.getName().toString();
            System.out.println( "  - '" + ptName + "', minOccurs=" + pt.getMinOccurs() + ", maxOccurs="
                                + pt.getMaxOccurs() + ", type: " + pt.getClass().getSimpleName() );
        }
    }

    /**
     * @param args
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     * @throws ClassCastException
     * @throws JAXBException
     * @throws IOException
     */
    public static void main( String[] args )
                            throws ClassCastException, ClassNotFoundException, InstantiationException,
                            IllegalAccessException, JAXBException, IOException {

        Options options = initOptions();

        // for the moment, using the CLI API there is no way to respond to a help argument; see
        // https://issues.apache.org/jira/browse/CLI-179
        if ( args.length == 0 || ( args.length > 0 && ( args[0].contains( "help" ) || args[0].contains( "?" ) ) ) ) {
            printHelp( options );
        }

        try {
            new PosixParser().parse( options, args );

            Action action = null;
            try {
                action = Action.valueOf( options.getOption( OPT_ACTION ).getValue() );
            } catch ( IllegalArgumentException e ) {
                System.out.println( "Unknown action '" + options.getOption( OPT_ACTION ).getValue()
                                    + "'. Call with '-help' for displaying available actions." );
                System.exit( 0 );
            }

            InputFormat inputFormat = null;
            try {
                inputFormat = InputFormat.valueOf( options.getOption( OPT_INPUT_FORMAT ).getValue() );
            } catch ( IllegalArgumentException e ) {
                System.out.println( "Unknown input format '" + options.getOption( OPT_INPUT_FORMAT ).getValue()
                                    + "'. Call with '-help' for displaying valid formats." );
                System.exit( 0 );
            }

            String inputFileName = options.getOption( OPT_INPUT_FILE ).getValue();
            String rulesFileName = options.getOption( OPT_RULES_FILE ).getValue();
            String dbSchema = options.getOption( OPT_DB_SCHEMA ).getValue();

            switch ( action ) {
            case analyze:
                analyze( inputFormat, inputFileName );
                break;
            case create_ddl:
                createDDL( inputFormat, inputFileName, dbSchema );
                break;
            case map_to_oracle:
                mapToOracle( inputFormat, inputFileName );
                break;
            case map_to_postgis:
                mapToPostGIS( inputFormat, inputFileName, rulesFileName );
                break;
            }
        } catch ( ParseException exp ) {
            System.err.println( Messages.getMessage( "TOOL_COMMANDLINE_ERROR", exp.getMessage() ) );
            // printHelp( options );
        }
    }

    private static Options initOptions() {

        Options opts = new Options();

        Option opt = new Option( "?", "help", false, "print (this) usage information" );
        opt.setArgs( 0 );
        opts.addOption( opt );

        String actionsList = "";
        Action[] actions = Action.values();
        actionsList += actions[0].toString();
        for ( int i = 1; i < actions.length; i++ ) {
            actionsList += ", " + actions[i];
        }

        opt = new Option( OPT_ACTION, true, "action, one of: " + actionsList + "" );
        opt.setRequired( true );
        opts.addOption( opt );

        String formatsList = "";
        InputFormat[] formats = InputFormat.values();
        formatsList += formats[0].toString();
        for ( int i = 1; i < formats.length; i++ ) {
            formatsList += ", " + formats[i];
        }

        opt = new Option( OPT_INPUT_FORMAT, true, "input format, one of: " + formatsList + "" );
        opt.setRequired( true );
        opts.addOption( opt );

        opt = new Option( OPT_INPUT_FILE, true, "input filename" );
        opt.setRequired( true );
        opts.addOption( opt );

        opt = new Option( OPT_RULES_FILE, true,
                          "rules filename (Java properties file with string replacement rules for feature type / property names)" );
        opt.setRequired( false );
        opts.addOption( opt );

        opt = new Option( OPT_DB_SCHEMA, true, "optional database schema name" );
        opt.setRequired( false );
        opts.addOption( opt );
        return opts;
    }

    private static void printHelp( Options options ) {
        CommandUtils.printHelp( options, ApplicationSchemaTool.class.getSimpleName(), null, null );
    }
}

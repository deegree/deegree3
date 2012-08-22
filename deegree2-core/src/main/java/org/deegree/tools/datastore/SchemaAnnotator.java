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
package org.deegree.tools.datastore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;

import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XSLTDocument;
import org.xml.sax.SAXException;

/**
 * Annotates a plain GML application schema with (default) mapping information.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class SchemaAnnotator {

    private static final String XSL_FILE = "annotator.xsl";

    private static XSLTDocument xslSheet = new XSLTDocument();

    private static Properties featureTypeMappings = new Properties();

    private static Properties propertyMappings = new Properties();

    static {
        URL sheetURL = SchemaAnnotator.class.getResource( XSL_FILE );
        try {
            xslSheet.load( sheetURL );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * @param columnName
     * @return the converted column name
     */
    public static String getColumnName( String columnName ) {
        columnName = columnName.toLowerCase();
        String colName = (String) propertyMappings.get( columnName );
        if ( colName == null ) {
            System.out.println( "No field name -> column mapping for feature type '" + columnName
                                + "'. Using field name as column name." );
            colName = columnName;
        }
        return colName;
    }

    /**
     * @param featureTypeName
     * @return the table name
     */
    public static String getTableName( String featureTypeName ) {
        featureTypeName = featureTypeName.toLowerCase();
        String tableName = (String) featureTypeMappings.get( featureTypeName );
        if ( tableName == null ) {
            System.out.println( "No feature type -> table mapping for feature type '" + featureTypeName
                                + "'. Using feature type name as table name." );
            tableName = featureTypeName;
        }
        return tableName;
    }

    /**
     * @param args
     * @throws SAXException
     * @throws IOException
     * @throws TransformerException
     */
    public static void main( String[] args )
                            throws IOException, SAXException, TransformerException {

        if ( args.length < 2 ) {
            System.out.println( "Usage: SchemaAnnotator <input.xsd> <output.xsd> [tableName.properties] [columnName.properties]" );
            System.exit( 0 );
        }

        String tableNames = args.length < 4 ? null : args[3];
        String columnNames = args.length < 3 ? null : args[2];
        URL outputFile = new File( args[1] ).toURL();
        URL inputFile = new File( args[0] ).toURL();

        if ( tableNames != null ) {
            System.out.println( "Loading feature type -> table name translations from file '" + tableNames + "'..." );
            featureTypeMappings.load( new FileInputStream( new URL( tableNames ).getFile() ) );
        }

        if ( columnNames != null ) {
            System.out.println( "Loading property -> column name translations from file '" + columnNames + "'..." );
            propertyMappings.load( new FileInputStream( new URL( columnNames ).getFile() ) );
        }

        System.out.println( "Loading input schema file '" + inputFile + "'..." );
        XMLFragment inputSchema = new XMLFragment();
        inputSchema.load( inputFile );

        System.out.println( "Adding annotation information..." );
        XMLFragment outputSchema = xslSheet.transform( inputSchema );

        System.out.println( "Writing annotated schema file '" + outputFile + "'..." );
        FileWriter writer = new FileWriter( outputFile.getFile() );
        Properties properties = new Properties();

        properties.setProperty( OutputKeys.ENCODING, "UTF-8" );
        outputSchema.write( writer, properties );
        writer.close();
    }
}

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

package org.deegree.tools.crs;

import static org.deegree.tools.CommandUtils.OPT_VERBOSE;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.xml.stax.FormattingXMLStreamWriter;
import org.deegree.cs.CRS;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.XMLTransformer;
import org.deegree.tools.CommandUtils;
import org.deegree.tools.annotations.Tool;
import org.slf4j.Logger;

/**
 * Tool for converting the GML geometries inside an XML document from one SRS to another.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@Tool("Converts the GML geometries inside an XML document from one SRS to another.")
public class XMLCoordinateTransform {

    private static final Logger LOG = getLogger( XMLCoordinateTransform.class );

    private static final String OPT_S_SRS = "source_srs";

    private static final String OPT_T_SRS = "target_srs";

    private static final String OPT_TRANSFORMATION = "transformation";

    private static final String OPT_INPUT = "input";

    private static final String OPT_GML_VERSION = "gml_version";

    private static final String OPT_OUTPUT = "output";

    /**
     * a starter method to transform a given point or a serie of points read from a file.
     * 
     * @param args
     */
    public static void main( String[] args ) {
        CommandLineParser parser = new PosixParser();

        Options options = initOptions();
        boolean verbose = false;

        // for the moment, using the CLI API there is no way to respond to a help argument; see
        // https://issues.apache.org/jira/browse/CLI-179
        if ( args != null && args.length > 0 ) {
            for ( String a : args ) {
                if ( a != null && a.toLowerCase().contains( "help" ) || "-?".equals( a ) ) {
                    printHelp( options );
                }
            }
        }

        CommandLine line = null;
        try {
            line = parser.parse( options, args );
            verbose = line.hasOption( OPT_VERBOSE );
            doTransform( line );
        } catch ( ParseException exp ) {
            System.err.println( "ERROR: Invalid command line: " + exp.getMessage() );
            printHelp( options );
        } catch ( Throwable e ) {
            System.err.println( "An Exception occurred while transforming your document, error message: "
                                + e.getMessage() );
            if ( verbose ) {
                e.printStackTrace();
            }
            System.exit( 1 );
        }
    }

    private static void doTransform( CommandLine line )
                            throws IllegalArgumentException, TransformationException, UnknownCRSException, IOException,
                            XMLStreamException, FactoryConfigurationError {

        CoordinateSystem source = null;
        String sourceCRS = line.getOptionValue( OPT_S_SRS );
        if ( sourceCRS != null ) {
            source = new CRS( sourceCRS ).getWrappedCRS();
        }

        String targetCRS = line.getOptionValue( OPT_T_SRS );
        CoordinateSystem target = new CRS( targetCRS ).getWrappedCRS();

        GMLVersion gmlVersion = null;
        String gmlVersionString = line.getOptionValue( OPT_GML_VERSION );
        gmlVersion = GMLVersion.valueOf( gmlVersionString );

        String i = line.getOptionValue( OPT_INPUT );
        File inputFile = new File( i );
        if ( !inputFile.exists() ) {
            throw new IllegalArgumentException( "Input file '" + inputFile + "' does not exist." );
        }
        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(
                                                                                         new FileInputStream( inputFile ) );

        String o = line.getOptionValue( OPT_OUTPUT );
        File outputFile = new File( o );
        XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(
                                                                                          new FileOutputStream(
                                                                                                                outputFile ),
                                                                                          "UTF-8" );
        xmlWriter = new FormattingXMLStreamWriter( xmlWriter, "    ", true );
        xmlWriter.writeStartDocument( "UTF-8", "1.0" );
        XMLTransformer transformer = new XMLTransformer( targetCRS );
        transformer.transform( xmlReader, xmlWriter, source, gmlVersion, false, null );
        xmlWriter.close();
    }

    private static Options initOptions() {

        Options options = new Options();

        Option option = new Option( OPT_S_SRS, true, "Identifier of the source srs, e.g. 'EPSG:4326'." );
        option.setArgs( 1 );
        options.addOption( option );

        option = new Option( OPT_T_SRS, true, "Identifier of the target srs, e.g. 'EPSG:4326'." );
        option.setArgs( 1 );
        option.setRequired( true );
        options.addOption( option );

        option = new Option( OPT_INPUT, true, "Path to the XML file to be transformed" );
        option.setArgs( 1 );
        option.setRequired( true );
        options.addOption( option );

        option = new Option( OPT_GML_VERSION, true,
                             "GML version used for encoding geometries (GML_2, GML_30, GML_31 or GML_32)" );
        option.setArgs( 1 );
        option.setRequired( true );
        options.addOption( option );

        option = new Option( OPT_OUTPUT, true, "Path to the output file" );
        option.setArgs( 1 );
        options.addOption( option );

        CommandUtils.addDefaultOptions( options );

        return options;
    }

    private static void printHelp( Options options ) {
        CommandUtils.printHelp( options, XMLCoordinateTransform.class.getCanonicalName(), null, null );
    }
}

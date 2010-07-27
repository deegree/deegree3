//$HeadURL$
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
package org.deegree.tools.services.wms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.util.HashSet;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.FormattingXMLStreamWriter;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreManager;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.tools.CommandUtils;
import org.deegree.tools.annotations.Tool;
import org.deegree.tools.i18n.Messages;
import org.deegree.tools.rendering.r2d.se.StyleChecker;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@Tool(value = "generates a WMS layer tree/configuration file from a feature type hierarchy")
public class FeatureTypesToLayerTree {

    private static Options initOptions() {
        Options opts = new Options();

        Option opt = new Option( "f", "file", true, "path to a feature store configuration" );
        opt.setRequired( true );
        opts.addOption( opt );

        opt = new Option( "o", "output", true, "path to the WMS configuration output file" );
        opt.setRequired( true );
        opts.addOption( opt );
        //
        // opt = new Option( "p", "dbpassword", true, "database password, if left off, will be set as empty" );
        // opt.setRequired( false );
        // opts.addOption( opt );
        //
        // opt = new Option( "c", "clean", false,
        // "if set, faulty styles will be deleted (currently only in the styles table)" );
        // opt.setRequired( false );
        // opts.addOption( opt );

        CommandUtils.addDefaultOptions( opts );

        return opts;

    }

    private static String ns = "http://www.deegree.org/services/wms";

    private static void writeLayer( HashSet<FeatureType> visited, XMLStreamWriter out, FeatureType ft, String storeId )
                            throws XMLStreamException {
        if ( visited.contains( ft ) || ft == null ) {
            return;
        }
        visited.add( ft );

        out.writeCharacters( "\n" );
        out.writeStartElement( ns, "RequestableLayer" );

        XMLAdapter.writeElement( out, ns, "Name", ft.getName().getLocalPart() );
        XMLAdapter.writeElement( out, ns, "Title", ft.getName().getLocalPart() );
        XMLAdapter.writeElement( out, ns, "FeatureStoreId", storeId );

        for ( FeatureType sub : ft.getSchema().getDirectSubtypes( ft ) ) {
            writeLayer( visited, out, sub, storeId );
        }

        out.writeEndElement();
        out.writeCharacters( "\n" );
    }

    /**
     * @param args
     */
    public static void main( String[] args ) {
        Options options = initOptions();

        // for the moment, using the CLI API there is no way to respond to a help argument; see
        // https://issues.apache.org/jira/browse/CLI-179
        if ( args.length == 0 || ( args.length > 0 && ( args[0].contains( "help" ) || args[0].contains( "?" ) ) ) ) {
            CommandUtils.printHelp( options, FeatureTypesToLayerTree.class.getSimpleName(), null, null );
        }

        try {
            CommandLine line = new PosixParser().parse( options, args );

            String storeFile = line.getOptionValue( "f" );
            String nm = new File( storeFile ).getName();
            String storeId = nm.substring( 0, nm.length() - 4 );

            FileOutputStream os = new FileOutputStream( line.getOptionValue( "o" ) );
            XMLOutputFactory fac = XMLOutputFactory.newInstance();
            XMLStreamWriter out = new FormattingXMLStreamWriter( fac.createXMLStreamWriter( os ) );
            out.setDefaultNamespace( ns );

            FeatureStore store = FeatureStoreManager.create( new File( storeFile ).toURI().toURL() );
            ApplicationSchema schema = store.getSchema();

            // prepare document
            out.writeStartDocument();
            out.writeStartElement( ns, "deegreeWMS" );
            out.writeDefaultNamespace( ns );
            out.writeAttribute( "version", "0.5.0" );
            out.writeStartElement( ns, "ServiceConfiguration" );

            HashSet<FeatureType> visited = new HashSet<FeatureType>();

            if ( schema.getRootFeatureTypes().length == 1 ) {
                writeLayer( visited, out, schema.getRootFeatureTypes()[0], storeId );
            } else {
                out.writeCharacters( "\n" );
                out.writeStartElement( ns, "UnrequestableLayer" );
                XMLAdapter.writeElement( out, ns, "Title", "Root Layer" );
                for ( FeatureType ft : schema.getRootFeatureTypes() ) {
                    writeLayer( visited, out, ft, storeId );
                }
                out.writeEndElement();
                out.writeCharacters( "\n" );
            }

            out.writeEndElement();
            out.writeEndElement();
            out.writeEndDocument();
            out.close();
        } catch ( ParseException exp ) {
            System.err.println( Messages.getMessage( "TOOL_COMMANDLINE_ERROR", exp.getMessage() ) );
            CommandUtils.printHelp( options, StyleChecker.class.getSimpleName(), null, null );
        } catch ( MalformedURLException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( FeatureStoreException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( FactoryConfigurationError e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}

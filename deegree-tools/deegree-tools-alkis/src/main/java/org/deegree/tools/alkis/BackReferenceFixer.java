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
package org.deegree.tools.alkis;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.IOUtils;
import org.deegree.commons.annotations.Tool;
import org.deegree.commons.tools.CommandUtils;
import org.deegree.commons.xml.stax.IndentingXMLStreamWriter;
import org.deegree.feature.Feature;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.types.AppSchema;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.FeatureReference;
import org.deegree.gml.schema.GMLAppSchemaReader;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@Tool(value = "adds inversDientZurDarstellungVon properties for GeoInfoDok 6.0.1 files")
public class BackReferenceFixer {

    private static final Logger LOG = getLogger( BackReferenceFixer.class );

    private static final String ns601 = "http://www.adv-online.de/namespaces/adv/gid/6.0";

    private static Options initOptions() {
        Options opts = new Options();

        Option opt = new Option( "i", "input", true, "input file" );
        opt.setRequired( true );
        opts.addOption( opt );

        opt = new Option( "o", "output", true, "output file" );
        opt.setRequired( true );
        opts.addOption( opt );

        opt = new Option( "s", "schema", true, "schema file" );
        opt.setRequired( true );
        opts.addOption( opt );

        CommandUtils.addDefaultOptions( opts );

        return opts;
    }

    public static void main( String[] args ) {
        Options opts = initOptions();
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            CommandLine line = new PosixParser().parse( opts, args );
            String input = line.getOptionValue( 'i' );
            String output = line.getOptionValue( 'o' );
            String schema = line.getOptionValue( 's' );
            fis = new FileInputStream( input );
            fos = new FileOutputStream( output );
            XMLInputFactory xifac = XMLInputFactory.newInstance();
            XMLOutputFactory xofac = XMLOutputFactory.newInstance();
            XMLStreamReader xreader = xifac.createXMLStreamReader( input, fis );
            IndentingXMLStreamWriter xwriter = new IndentingXMLStreamWriter( xofac.createXMLStreamWriter( fos ) );
            GMLStreamReader reader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_32, xreader );

            AppSchema appSchema = new GMLAppSchemaReader( null, null, schema ).extractAppSchema();
            reader.setApplicationSchema( appSchema );

            GMLStreamWriter writer = GMLOutputFactory.createGMLStreamWriter( GMLVersion.GML_32, xwriter );
            XlinkedObjectsHandler handler = new XlinkedObjectsHandler( xwriter, true, null );
            writer.setAdditionalObjectHandler( handler );

            QName prop = new QName( ns601, "dientZurDarstellungVon" );

            Map<String, String> refs = new HashMap<String, String>();
            Map<String, String> bindings = null;

            for ( Feature f : reader.readFeatureCollectionStream() ) {
                if ( bindings == null ) {
                    bindings = f.getType().getSchema().getNamespaceBindings();
                }
                if ( f.getProperty( prop ) != null ) {
                    GenericProperty p = (GenericProperty) f.getProperty( prop );
                    FeatureReference ref = (FeatureReference) p.getValue();
                    refs.put( ref.getId(), f.getId() );
                }
            }

            reader.close();
            fis.close();
            writer.setNamespaceBindings( bindings );

            fis = new FileInputStream( input );
            xreader = xifac.createXMLStreamReader( input, fis );
            reader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_32, xreader );
            reader.setApplicationSchema( appSchema );

            if ( bindings != null ) {
                for ( Map.Entry<String, String> e : bindings.entrySet() ) {
                    if ( !e.getKey().isEmpty() ) {
                        xwriter.setPrefix( e.getValue(), e.getKey() );
                    }
                }
            }
            xwriter.writeStartDocument();
            xwriter.setPrefix( "gml", "http://www.opengis.net/gml/3.2" );
            xwriter.writeStartElement( "http://www.opengis.net/gml/3.2", "FeatureCollection" );
            xwriter.writeNamespace( "gml", "http://www.opengis.net/gml/3.2" );

            for ( Feature f : reader.readFeatureCollectionStream() ) {
                if ( refs.containsKey( f.getId() ) ) {

                }
                xwriter.writeStartElement( "http://www.opengis.net/gml/3.2", "featureMember" );
                writer.write( f );
                xwriter.writeEndElement();
            }

            xwriter.writeEndElement();
            xwriter.close();
        } catch ( Throwable e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly( fis );
            IOUtils.closeQuietly( fos );
        }
    }
}

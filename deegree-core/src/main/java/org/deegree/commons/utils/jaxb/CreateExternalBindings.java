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

package org.deegree.commons.utils.jaxb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.utils.StringPair;
import org.deegree.commons.xml.stax.FormattingXMLStreamWriter;

/**
 * The <code>CreateExternalBindings</code> is a little utitly class, which writes a jaxb binding file by reading java
 * files from a 'configuration' package and create a map=false schema binding file for those classes.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class CreateExternalBindings {

    public void createExternalBindingsFile( String sourceDir, String targetFile, String nameSpace, String referencedXSD )
                            throws XMLStreamException, IOException {
        if ( sourceDir == null ) {
            System.out.println( "No source directory given." );
            return;
        }
        if ( targetFile == null ) {
            System.out.println( "No target file given." );
            return;
        }

        File source = new File( sourceDir );
        if ( !source.exists() ) {
            System.out.println( "Given source directory does not exist: " + source.getAbsolutePath()
                                + "Please supply an existing source directory!" );
            return;
        }
        File target = new File( targetFile );

        String name = source.getAbsolutePath();
        int index = name.indexOf( "src" );
        if ( index == -1 ) {
            System.out.println( "Found no src directory in your path, this is strange!" );
            return;
        }
        String prefix = name.substring( 0, index + 4 );
        // src + 1
        String packageName = pathToPackage( name.substring( index + 4 ) );

        List<StringPair> classes = findClasses( source, prefix );

        System.out.println( "Found following classes: " );
        for ( StringPair s : classes ) {
            System.out.println( s );
        }

        // Get the reference file if any
        String targetSchemaFile = referencedXSD;
        if ( referencedXSD == null ) {
            targetSchemaFile = "../" + target.getName();
        }

        String ns = nameSpace;
        if ( ns == null || "".equals( ns.trim() ) ) {
            index = packageName.lastIndexOf( '.' );
            String nsName = null;
            if ( index == -1 ) {
                nsName = packageName;
            } else {
                nsName = packageName.substring( index + 1 );
            }
            ns = "http://www.deegree.org/" + nsName;
        }
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        factory.setProperty( "javax.xml.stream.isRepairingNamespaces", Boolean.TRUE );
        FileOutputStream fos = new FileOutputStream( targetFile );
        XMLStreamWriter writer = new FormattingXMLStreamWriter(
                                                                factory.createXMLStreamWriter( new OutputStreamWriter(
                                                                                                                       fos,
                                                                                                                       "UTF-8" ) ) );
        writeBindings( classes, writer, targetSchemaFile, ns, packageName );
        writer.flush();
        System.out.println( "Successfully wrote external-bindings file: " + target.getAbsoluteFile().toString() );
    }

    private void writeBindings( List<StringPair> classes, XMLStreamWriter writer, String referencedXSD,
                                String namespace, String packageName )
                            throws XMLStreamException {
        final String JAXB_NS = "http://java.sun.com/xml/ns/jaxb";
        final String XJC_NS = "http://java.sun.com/xml/ns/jaxb/xjc";
        writer.setPrefix( "jaxb", JAXB_NS );
        writer.setPrefix( "xjc", XJC_NS );
        writer.setPrefix( "xs", "http://www.w3.org/2001/XMLSchema" );
        writer.writeStartDocument();
        writer.writeStartElement( JAXB_NS, "bindings" );
        writer.writeAttribute( "version", "2.1" );
        writer.writeStartElement( JAXB_NS, "bindings" );
        // writer.writeAttribute( "schemaLocation", referencedXSD );
        writer.writeAttribute( "scd", "x-schema::tns" );
        writer.setPrefix( "tns", namespace );
        writer.writeAttribute( "map", "false" );
        // writer.writeEmptyElement( XJC_NS, "noUnmarshaller" );
        // writer.writeEmptyElement( XJC_NS, "noMarshaller" );
        // writer.writeEmptyElement( XJC_NS, "noValidator" );
        // writer.writeEmptyElement( XJC_NS, "noValidatingUnmarshaller" );
        writer.writeStartElement( JAXB_NS, "schemaBindings" );
        // <schemaBindings map="false">
        // <package name="org.deegree.dataaccess.configuration"></package>
        writer.writeStartElement( JAXB_NS, "package" );
        writer.writeAttribute( "name", packageName );
        // JAXB_NS, "package"
        writer.writeEndElement();

        // JAXB_NS, "schemaBindings"
        writer.writeEndElement();
        for ( StringPair cl : classes ) {
            if ( cl != null ) {
                int index = cl.first.lastIndexOf( '.' );
                if ( index != -1 ) {
                    String clName = cl.first.substring( index + 1 );
                    // If the class name doesn't have a Type in its name, it is probably a schema element... *g*
                    // http://www.w3.org/TR/xmlschema-ref/
                    writer.writeStartElement( JAXB_NS, "bindings" );
                    // writer.writeAttribute( "scd", "/" + cl.second + "::tns:" + clName );
                    // if ( cl.first.toLowerCase().contains( "type" ) ) {
                    if ( "type".equals( cl.second ) || "simpleType".equals( cl.second ) ) {
                        writer.writeAttribute( "scd", "~tns:" + clName );
                    } else {
                        writer.writeAttribute( "scd", "tns:" + clName );
                    }
                    writer.writeStartElement( JAXB_NS, "class" );
                    writer.writeAttribute( "ref", cl.first );
                    writer.writeEndElement();// JAXB_NS, "class"
                    writer.writeEndElement();// JAXB_NS, "bindings"
                }
            }
        }

        writer.writeEndElement(); // JAXB_NS, "bindings"

        writer.writeEndElement(); // JAXB_NS, "bindings"
        writer.writeEndDocument();
    }

    /**
     * @param source2
     * @return
     * @throws IOException
     */
    private List<StringPair> findClasses( File sourceDir, String prefix )
                            throws IOException {

        List<StringPair> classes = new ArrayList<StringPair>();
        findAndAddClasses( prefix, classes, sourceDir, new CustomFileFilter() );

        return classes;
    }

    /**
     * @param classes
     * @param parent
     * @throws IOException
     */
    private void findAndAddClasses( final String prefix, List<StringPair> classes, File parent, CustomFileFilter filter )
                            throws IOException {
        if ( parent != null ) {
            File[] sons = parent.listFiles( filter );
            for ( File tmp : sons ) {
                if ( tmp.isDirectory() ) {
                    findAndAddClasses( prefix, classes, tmp, filter );
                } else {
                    String t = tmp.getAbsoluteFile().toString();

                    // find out if it is a type, an simpleType( enum ) or an element.
                    String className = t.substring( prefix.length() );
                    className = className.substring( 0, className.length() - 5 );

                    classes.add( new StringPair( pathToPackage( className ), matchSCDType( className, t ) ) );

                }
            }
        }

    }

    private String matchSCDType( String className, String file )
                            throws IOException {
        String type = "type";
        // if ( !className.toLowerCase().contains( "type" ) ) {
        BufferedReader br = new BufferedReader( new FileReader( file ) );
        String line = br.readLine();
        boolean foundType = false;
        while ( line != null && !foundType ) {
            if ( line.contains( "public enum" ) ) {
                type = "simpleType";
                foundType = true;
            } else if ( line.contains( "public class" ) ) {
                if ( !className.toLowerCase().contains( "type" ) ) {
                    type = "element";
                }
                foundType = true;
            } else {
                line = br.readLine();
            }
            // }
        }
        br.close();
        return type;

    }

    private String pathToPackage( String path ) {
        path = path.replace( File.separatorChar, '.' );
        // sometimes on windows this is the default behavior
        path = path.replace( '/', '.' );
        return path;
    }

    /**
     * @param args
     *            the arguments to the bindings creator, args[0] sourceDir, args[1] destination file, [Optional
     *            args[2]=namespace of configuration file, args[3]=relative link to target schema file].
     * @throws XMLStreamException
     * @throws IOException
     */
    public static void main( String[] args )
                            throws XMLStreamException, IOException {

        if ( args != null ) {
            CreateExternalBindings ceb = new CreateExternalBindings();
            switch ( args.length ) {
            case 0:
            case 1:
                System.out.println( "Not enough parameters given, please supply a source directory (param 1) and a target file( param 2)." );
                break;
            case 2:
                ceb.createExternalBindingsFile( args[0], args[1], null, null );
                break;
            case 3:
                ceb.createExternalBindingsFile( args[0], args[1], args[2], null );
                break;
            default:
                ceb.createExternalBindingsFile( args[0], args[1], args[2], args[3] );
                break;
            }
        }
    }

    /**
     *
     * The <code>CustomFileFilter</code> class adds functionality to the filefilter mechanism of the JFileChooser.
     *
     * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
     *
     * @author last edited by: $Author$
     *
     * @version $Revision$, $Date$
     *
     */
    class CustomFileFilter implements java.io.FileFilter {

        public boolean accept( File pathname ) {
            if ( pathname.isDirectory() ) {
                return true;
            }
            String extension = getExtension( pathname );
            if ( extension != null ) {
                if ( "java".equals( extension.trim() ) ) {
                    return true;
                }
            }
            return false;
        }

        private String getExtension( File f ) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf( '.' );

            if ( i > 0 && i < s.length() - 1 ) {
                ext = s.substring( i + 1 ).toLowerCase();
            }
            if ( s.contains( "ObjectFactory" ) || s.contains( "package-info" ) ) {
                ext = null;
            }

            return ext;
        }
    }

}

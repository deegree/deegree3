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
package org.deegree.tools.metadata.importhandling;

import static org.deegree.commons.xml.CommonNamespaces.XSINS;
import static org.deegree.commons.xml.CommonNamespaces.XSI_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_PREFIX;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.XMLStreamWriterWrapper;
import org.deegree.metadata.persistence.neededdatastructures.BoundingBox;
import org.deegree.services.controller.ows.capabilities.OWSCapabilitiesXMLAdapter;
import org.deegree.tools.annotations.Tool;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@Tool("At the moment the input is a folder where the transformable datas are stored, the output is in ~/wpsimport/output. ")
public class WPSImporter extends AbstractDCImporter {

    static Map<QName, String> mapping = new HashMap<QName, String>();

    private List<String> subjectList;

    private String title;

    private String identifier;

    private String abstract_;

    private String type;

    private String creator;

    private String publisher;

    private String contributor;

    private String format;

    private String source;

    private String language;

    private String relation;

    private BoundingBox bbox;

    private String rights;

    static {
        mapping.put( new QName( nsContext.getURI( OWSCapabilitiesXMLAdapter.OWS_PREFIX ), "ServiceType",
                                OWSCapabilitiesXMLAdapter.OWS_PREFIX ), "subject" );

    }

    public WPSImporter( File file ) {
        super( file );

        OMElement element = this.importFile();
        if ( element.getLocalName().equals( "Capabilities" ) ) {
            parseCapabilities( element );
        } else {
            parseProcess( element );
        }

    }

    private void parseCapabilities( OMElement element ) {
        subjectList = new ArrayList<String>();
        identifier = getNodeAsString( element, new XPath( "./ows:ServiceIdentification/ows:Title", nsContext ), "" );
        String serviceType = getNodeAsString( element, new XPath( "./ows:ServiceIdentification/ows:ServiceType",
                                                                  nsContext ), "" );
        subjectList.add( serviceType );
        title = getNodeAsString( element, new XPath( "./ows:ServiceIdentification/ows:Title", nsContext ), "" );
        abstract_ = getNodeAsString( element, new XPath( "./ows:ServiceIdentification/ows:Abstract", nsContext ), "" );
        type = "service";

    }

    private void parseProcess( OMElement element ) {
        subjectList = new ArrayList<String>();
        identifier = getNodeAsString( element, new XPath( "./ProcessDescription/ows:Identifier", nsContext ), "" );
        title = getNodeAsString( element, new XPath( "./ProcessDescription/ows:Title", nsContext ), "" );
        abstract_ = getNodeAsString( element, new XPath( "./ProcessDescription/ows:Abstract", nsContext ), "" );
        type = "datasets";
        subjectList.add( identifier );
    }

    public OMElement generateDCRepresentation()
                            throws ParseException {

        OMElement element = factory.createOMElement( "Record", namespaceCSW );

        concatElements( element, identifierElem, identifier );
        concatElements( element, titleElem, title );
        for ( String subject : subjectList ) {
            concatElements( element, subjectElem, subject );
        }
        java.util.Date currentDate = new java.util.Date();
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" );
        concatElements( element, modifiedElem, new Date( sdf.format( currentDate ) ).toString() );
        concatElements( element, abstract_Elem, abstract_ );
        concatElements( element, typeElem, type );
        concatElements( element, creatorElem, creator );
        concatElements( element, publisherElem, publisher );
        concatElements( element, contributorElem, contributor );
        concatElements( element, formatElem, format );
        concatElements( element, sourceElem, source );
        concatElements( element, languageElem, language );
        concatElements( element, relationElem, relation );
        // concatElements( element, bboxElem, bbox );
        concatElements( element, rightsElem, rights );

        Iterator iter = element.getChildElements();
        if ( iter.hasNext() == false ) {
            return null;
        }

        return element;

    }

    private void concatElements( OMElement element, QName elementNode, String elementValue ) {

        if ( elementValue != null && !elementValue.equals( "" ) ) {
            OMElement subElem = factory.createOMElement( elementNode );
            subElem.setText( elementValue );
            element.addChild( subElem );
        }

    }

    public void genOutput( XMLStreamWriter writer ) {
        try {
            readXMLFragment( generateDCRepresentation(), writer );
        } catch ( ParseException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // ByteArrayInputStream byteArray = new ByteArrayInputStream();
        // byteArray.
        // BufferedInputStream bis;
        // bis = new BufferedInputStream( new ByteArrayInputStream( arg0 )) );
        // InputStreamReader in = new InputStreamReader( bis, "UTF-8" );
    }

    // @Override
    // public void elementMapping() {
    //        
    // }

    /**
     * @param args
     */
    public static void main( String[] args ) {
        WPSImporter wps;

        File tmpFile = new File( "tmpFile" );
        tmpFile.mkdir();

        FileOutputStream fout;
        try {
            fout = new FileOutputStream( tmpFile.getName() + File.separator + "output.xml" );
            XMLStreamWriter writer = new XMLStreamWriterWrapper(
                                                                 XMLOutputFactory.newInstance().createXMLStreamWriter(
                                                                                                                       fout ),
                                                                 schemaLocation );
            writer.setDefaultNamespace( CSW_202_NS );
            writer.setPrefix( CSW_PREFIX, CSW_202_NS );
            writer.writeStartDocument();
            writer.writeStartElement( CSW_202_NS, "Transaction" );
            writer.writeNamespace( CSW_PREFIX, CSW_202_NS );
            writer.writeNamespace( XSI_PREFIX, XSINS );
            writer.writeAttribute( "service", CSW_PREFIX.toUpperCase() );
            writer.writeAttribute( "version", new Version( 2, 0, 2 ).toString() );
            writer.writeStartElement( CSW_202_NS, "Insert" );

            String folder = args[0];

            File output = new File( folder + "output" );
            if ( output.exists() ) {
                deleteDir( output );
            }
            File fileFolder = new File( folder );
            File[] files = fileFolder.listFiles();

            if ( files != null ) {
                for ( File file : files ) {
                    System.out.print( "File " + file + " will be exported..." );
                    wps = new WPSImporter( file );
                    wps.genOutput( writer );
                    System.out.println( "DONE" );
                }
            }

            writer.writeEndElement();
            writer.writeEndElement();
            writer.writeEndDocument();
            System.out.println( "New file generated." );

            output.mkdir();

            FileOutputStream newOutput = new FileOutputStream( output.getAbsolutePath() + File.separator + "output.xml" );
            FileInputStream in = new FileInputStream( tmpFile.getName() + File.separator + "output.xml" );
            System.out.println( "Outputfolder: " + output.getAbsolutePath() + File.separator + "output.xml" );
            copy( in, newOutput );
            in.close();
            newOutput.close();
            fout.close();
            deleteDir( tmpFile );

        } catch ( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( FactoryConfigurationError e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Copies an InputStream into an OutputStream
     * 
     * @param in
     * @param out
     * @throws IOException
     */
    static void copy( InputStream in, OutputStream out )
                            throws IOException {
        byte[] buffer = new byte[0xFFFF];
        for ( int len; ( len = in.read( buffer ) ) != -1; )
            out.write( buffer, 0, len );
    }

    /**
     * Deletes all files and subdirectories under dir. Returns true if all deletions were successful. If a deletion
     * fails, the method stops attempting to delete and returns false.
     * 
     * @param dir
     *            directory that should be deleted
     * @return
     */
    public static boolean deleteDir( File dir ) {
        if ( dir.isDirectory() ) {
            String[] children = dir.list();
            for ( int i = 0; i < children.length; i++ ) {
                boolean success = deleteDir( new File( dir, children[i] ) );
                if ( !success ) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }

}

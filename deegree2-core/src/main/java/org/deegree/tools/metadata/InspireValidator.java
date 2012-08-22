//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.tools.metadata;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.ElementIterator;
import javax.swing.text.StyleConstants;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.BootLogger;
import org.deegree.framework.util.FileUtils;
import org.deegree.framework.util.HttpUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class InspireValidator {

    private static final ILogger LOG = LoggerFactory.getLogger( InspireValidator.class );

    private static NamespaceContext nsc = CommonNamespaces.getNamespaceContext();

    private String cswAddress;

    private String outputFile;

    private PrintWriter outputWriter;

    private XMLFragment query;

    /**
     * 
     * @param cswAddress
     * @param outputFile
     * @throws SAXException
     * @throws IOException
     * @throws MalformedURLException
     */
    public InspireValidator( String cswAddress, String outputFile ) throws MalformedURLException, IOException,
                            SAXException {
        this.outputFile = outputFile;
        this.cswAddress = cswAddress;
    }

    /**
     * 
     * @param cswAddress
     * @param query
     * @param outputFile
     */
    public InspireValidator( String cswAddress, XMLFragment query, String outputFile ) {
        this.query = query;
    }

    /**
     * reads a number of random records from a CSW and validates them against INSPIRE metadata validator
     * 
     * @param mr
     *            number of records to be tested
     */
    public void runRandomQuery( String mr ) {
        int maxQueries = 50;
        if ( mr != null ) {
            maxQueries = Integer.parseInt( mr );
            LOG.logInfo( Messages.get( "m1", mr ) );
        } else {
            LOG.logInfo( Messages.get( "m2", mr ) );
        }
        try {
            int max = getNumberOfAvailableRecords();
            if ( maxQueries > max ) {
                maxQueries = max;
                LOG.logInfo( Messages.get( "m3", max ) );
            }
            // create base GetRecords request without constraint but with startPosition and maxRecords = 1
            String s = Messages.get( "getRecordsWithoutFilter" );
            XMLFragment request = new XMLFragment( new StringReader( s ), XMLFragment.DEFAULT_URL );
            Random random = new Random();
            outputWriter = new PrintWriter( new File( outputFile ) );
            for ( int i = 0; i < maxQueries; i++ ) {
                // set index of record to read
                Node node = XMLTools.getNode( request.getRootElement(), Messages.get( "xPathStartPosition" ), nsc );
                node.setNodeValue( Integer.toString( random.nextInt( max ) + 1 ) );

                HttpMethod m = HttpUtils.performHttpPost( cswAddress, request, 60000, null, null, null );
                XMLFragment xml = new XMLFragment();
                xml.load( m.getResponseBodyAsStream(), cswAddress );
                List<org.w3c.dom.Element> list = XMLTools.getElements( xml.getRootElement(),
                                                                       Messages.get( "xPathMetadata" ), nsc );
                System.out.println( Messages.get( "m4", i + 1, maxQueries ) );
                outputWriter.println( "---------------------------------------------------------------------" );
                String id = XMLTools.getNodeAsString( list.get( 0 ), Messages.get( "xPathIdentifier" ), nsc, "none" );
                outputWriter.println( Messages.get( "m5", id ) );
                validateINSPIRE( list.get( 0 ) );
            }
            System.out.println();
        } catch ( Exception e ) {
            LOG.logError( e );
        } finally {
            if ( outputWriter != null ) {
                outputWriter.flush();
                outputWriter.close();
            }
        }
        System.out.println( Messages.get( "m6", new File( outputFile ).getAbsolutePath() ) );
    }

    /**
     * @return
     * @throws IOException
     * @throws SAXException
     */
    private int getNumberOfAvailableRecords()
                            throws Exception {
        String s = Messages.get( "getRecordsHits" );
        XMLFragment xml = new XMLFragment( new StringReader( s ), XMLFragment.DEFAULT_URL );
        HttpMethod m = HttpUtils.performHttpPost( cswAddress, xml, 60000, null, null, null );
        XMLFragment result = new XMLFragment();
        result.load( m.getResponseBodyAsStream(), cswAddress );
        return XMLTools.getNodeAsInt( result.getRootElement(), Messages.get( "xPathNumberOfRecordsMatched" ), nsc, 0 );
    }

    /**
     * reads a records from a CSW defined by a GetRecord request and validates them against INSPIRE metadata validator
     * 
     * @param queryFile
     *            reference to GetRecords request
     */
    public void runDefinedQuery( String queryFile ) {
        try {
            query = new XMLFragment( new File( queryFile ) );
            HttpMethod m = HttpUtils.performHttpPost( cswAddress, query, 60000, null, null, null );
            XMLFragment xml = new XMLFragment();
            xml.load( m.getResponseBodyAsStream(), cswAddress );
            List<org.w3c.dom.Element> list = XMLTools.getElements( xml.getRootElement(),
                                                                   Messages.get( "xPathMetadata" ), nsc );
            outputWriter = new PrintWriter( new File( outputFile ) );
            int cnt = 1;
            for ( org.w3c.dom.Element element : list ) {
                System.out.println( Messages.get( "m7", cnt, list.size() ) );
                outputWriter.println( "---------------------------------------------------------------------" );
                String id = XMLTools.getNodeAsString( element, Messages.get( "xPathIdentifier" ), nsc, "none" );
                outputWriter.println( Messages.get( "m8", id ) );
                validateINSPIRE( element );
                cnt++;
            }
            System.out.println();
        } catch ( Exception e ) {
            LOG.logError( e );
        } finally {
            if ( outputWriter != null ) {
                outputWriter.flush();
                outputWriter.close();
            }
        }
        System.out.println( "report stored at: " + new File( outputFile ).getAbsolutePath() );
    }

    /**
     * 
     * @param fileName
     *            name of a file containing a metadata record to be validated
     */
    public void runISOFile( String fileName ) {
        try {
            XMLFragment xml = new XMLFragment( new File( fileName ) );
            outputWriter = new PrintWriter( new File( outputFile ) );
            outputWriter.println( "---------------------------------------------------------------------" );
            outputWriter.println( Messages.get( "m9", fileName ) );
            validateINSPIRE( xml.getRootElement() );
        } catch ( Exception e ) {
            LOG.logError( e );
        } finally {
            if ( outputWriter != null ) {
                outputWriter.flush();
                outputWriter.close();
            }
        }
    }

    /**
     * 
     * @param dirName
     *            name of the directory containing metadata records to be validated
     */
    public void runISOFileDirectory( String dirName ) {
        try {
            File dir = new File( dirName );            
            File[] files = dir.listFiles();
            outputWriter = new PrintWriter( new File( outputFile ) );
            outputWriter.println( Messages.get( "m10", dir.getAbsolutePath() ) );
            for ( File file : files ) {
                XMLFragment xml = new XMLFragment( file );
                outputWriter.println( "---------------------------------------------------------------------" );
                outputWriter.println( Messages.get( "m11", file.getAbsolutePath() ) );
                System.out.println( Messages.get( "m11", file.getAbsolutePath() ) );
                validateINSPIRE( xml.getRootElement() );
            }
        } catch ( Exception e ) {
            LOG.logError( e );
        } finally {
            if ( outputWriter != null ) {
                outputWriter.flush();
                outputWriter.close();
            }
        }
    }

    /**
     * main method to validate a metadata record using INSPIRE metadata validator
     * 
     * @param element
     * @throws Exception
     */
    private void validateINSPIRE( org.w3c.dom.Element element )
                            throws Exception {
        HttpClient httpclient = new DefaultHttpClient();

        HttpPost httpPost = new HttpPost( Messages.get( "validator" ) );
        // xml response: httpPost.addHeader("Accept", "application/xml");
        // html response
        httpPost.addHeader( "Accept", "text/html" );
        XMLFragment xml = new XMLFragment( element );
        File tmp = new File( "tmp.xml" );
        tmp.deleteOnExit();
        xml.write( new FileWriter( tmp ) );
        FileBody dataFile = new FileBody( new File( "tmp.xml" ) );
        MultipartEntity reqEntity = new MultipartEntity();
        reqEntity.addPart( "dataFile", dataFile );
        httpPost.setEntity( reqEntity );

        HttpResponse response = httpclient.execute( httpPost );
        parseServiceResponse( response );

    }

    /**
     * parse INSPIRE metadata validator response and print out result onto the console
     * 
     * @param response
     * @throws IOException
     * @throws IllegalStateException
     */
    private void parseServiceResponse( HttpResponse response )
                            throws Exception {
        String s = FileUtils.readTextFile( ( (BasicHttpResponse) response ).getEntity().getContent() ).toString();
        if ( response.getStatusLine().getStatusCode() != 200 ) {
            outputWriter.println( s );
            outputWriter.println();
            return;
        }
        s = "<html><head></head><body>" + s + "</body></html>";
        BufferedReader br = new BufferedReader( new StringReader( s ) );

        HTMLEditorKit htmlKit = new HTMLEditorKit();
        HTMLDocument htmlDoc = (HTMLDocument) htmlKit.createDefaultDocument();
        HTMLEditorKit.Parser parser = new ParserDelegator();
        HTMLEditorKit.ParserCallback callback = htmlDoc.getReader( 0 );
        parser.parse( br, callback, true );

        // Parse
        ElementIterator iterator = new ElementIterator( htmlDoc );
        Element element;
        while ( ( element = iterator.next() ) != null ) {
            AttributeSet attributes = element.getAttributes();
            Object name = attributes.getAttribute( StyleConstants.NameAttribute );
            if ( ( name instanceof HTML.Tag ) && ( ( name == HTML.Tag.IMPLIED ) ) ) {
                // Build up content text as it may be within multiple elements
                StringBuffer text = new StringBuffer();
                int count = element.getElementCount();
                for ( int i = 0; i < count; i++ ) {
                    Element child = element.getElement( i );
                    AttributeSet childAttributes = child.getAttributes();
                    if ( childAttributes.getAttribute( StyleConstants.NameAttribute ) == HTML.Tag.CONTENT ) {
                        int startOffset = child.getStartOffset();
                        int endOffset = child.getEndOffset();
                        int length = endOffset - startOffset;
                        text.append( htmlDoc.getText( startOffset, length ) );
                    }
                }
                outputWriter.println( text.toString() );
            }
        }
        outputWriter.println( "---------------------------------------------------------------------" );
        outputWriter.println();
    }

    /**
     * validate input parameter
     * 
     * @param map
     * @throws Exception
     */
    private static void validate( Properties map )
                            throws Exception {
//        if ( map.get( "-cswAddress" ) == null ) {
//            throw new Exception( "-cswAddress parameter must be set" );
//        }
        if ( map.get( "-outFile" ) == null ) {
            map.put( "-outFile", "validation_result.txt" );
        }

    }

    public static void main( String[] args )
                            throws Exception {
        Properties map = new Properties();
        for ( int i = 0; i < args.length; i += 2 ) {
            map.put( args[i], args[i + 1] );
        }
        try {
            validate( map );
        } catch ( Exception e ) {
            LOG.logError( "!!! E R R O R !!!", e );
            return;
        }
        LOG.logInfo( Messages.get( "m16" ) );
        StringTools.printMap( map, System.out );
        InspireValidator iv = new InspireValidator( map.getProperty( "-cswAddress" ), map.getProperty( "-outFile" ) );
        if ( map.get( "-queryFile" ) != null ) {
            LOG.logInfo( Messages.get( "m12" ), map.get( "-queryFile" ) );
            iv.runDefinedQuery( map.getProperty( "-queryFile" ) );
        } else if ( map.get( "-isoFile" ) != null ) {
            LOG.logInfo( Messages.get( "m13" ), map.get( "-isoFile" ) );
            iv.runISOFile( map.getProperty( "-isoFile" ) );
        } else if ( map.get( "-directory" ) != null ) {
            LOG.logInfo( Messages.get( "m14" ), map.get( "-directory" ) );
            iv.runISOFileDirectory( map.getProperty( "-directory" ) );
        } else {
            LOG.logInfo( Messages.get( "m15" ) );
            iv.runRandomQuery( map.getProperty( "-maxRecords" ) );
        }
    }

    /**
     * 
     * TODO add class documentation here
     * 
     * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    private static class Messages {

        private static Properties props = new Properties();

        /**
         * Initialization done at class loading time.
         */
        static {
            try {
                InputStream is = InspireValidator.class.getResourceAsStream( "InspireValidator.properties" );
                props.load( is );
                is.close();
            } catch ( IOException e ) {
                BootLogger.logError( "Error while initializing " + Messages.class.getName() + " : " + e.getMessage(), e );
            }
        }

        static String get( String key, Object... args ) {
            String s = (String) props.get( key );
            if ( s != null ) {
                return MessageFormat.format( s, args );
            }

            return "$Message with key: " + key + " not found$";
        }
    }

}

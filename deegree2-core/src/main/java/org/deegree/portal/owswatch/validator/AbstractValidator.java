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

package org.deegree.portal.owswatch.validator;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.HttpMethodBase;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.w3c.dom.Document;

import org.deegree.portal.owswatch.Status;
import org.deegree.portal.owswatch.ValidatorResponse;

/**
 * Abstract class implementing the method validate.
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public abstract class AbstractValidator implements Validator {

    private static final ILogger LOG = LoggerFactory.getLogger( AbstractValidator.class );

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.portal.owswatch.validator.Validator#validateAnswer(org.apache.commons.httpclient.HttpMethodBase, int,
     *      int)
     */
    public ValidatorResponse validateAnswer( HttpMethodBase method, int statusCode ) {
        if ( isValidHttpResponse( statusCode ) ) {
            return validateXml( method );
        } else {
            return validateErrorHttpResponse( statusCode );
        }
    }

    /**
     * Validates the HttpMethodBase and checks if the execution was successful or not
     *
     * @param method
     *            the httpmethod after executing it
     * @return an instance of ValidatorResponse with the necessary information after validation
     */
    protected ValidatorResponse validateXml( HttpMethodBase method ) {

        String lastMessage = null;
        Status status = null;

        String contentType = method.getResponseHeader( "Content-Type" ).getValue();
        if ( !contentType.contains( "xml" ) ) {
            status = Status.RESULT_STATE_UNEXPECTED_CONTENT;
            lastMessage = StringTools.concat( 100, "Error: Response Content is ", contentType, " not xml" );
            return new ValidatorResponse( lastMessage, status );
        }

        String xml = null;
        try {
            InputStream stream = copyStream( method.getResponseBodyAsStream() );
            stream.reset();
            xml = parseStream( stream );
        } catch ( IOException e ) {
            status = Status.RESULT_STATE_BAD_RESPONSE;
            lastMessage = status.getStatusMessage();
            return new ValidatorResponse( lastMessage, status );
        }

        if ( xml.length() == 0 ) {
            status = Status.RESULT_STATE_BAD_RESPONSE;
            lastMessage = "Error: XML Response is empty";
            return new ValidatorResponse( lastMessage, status );
        }
        if ( xml.contains( "ServiceException" ) ) {
            return validateXmlServiceException( method );
        }
        // If its an xml, and there's no service exception, then don't really parse the xml,
        // we assume that its well formed, since there might be huge xmls, which would take time to be parsed
        status = Status.RESULT_STATE_AVAILABLE;
        lastMessage = status.getStatusMessage();
        return new ValidatorResponse( lastMessage, status );
    }

    /**
     * This method is called To read the ServiceExceptionReport from the xml file
     *
     * @param method
     *            the httpmethod after executing it
     * @return an instance of ValidatorResponse with the necessary information after validation
     */
    protected ValidatorResponse validateXmlServiceException( HttpMethodBase method ) {

        Document doc = null;
        String lastMessage = null;
        Status status = null;
        try {
            InputStream stream = method.getResponseBodyAsStream();
            stream.reset();
            doc = instantiateParser().parse( stream );
        } catch ( Exception e ) {
            status = Status.RESULT_STATE_INVALID_XML;
            lastMessage = "Error: MalFormed XML Response";
            return new ValidatorResponse( lastMessage, status );
        }
        try {
            status = Status.RESULT_STATE_SERVICE_UNAVAILABLE;
            lastMessage = XMLTools.getNodeAsString( doc.getDocumentElement(), "./ServiceException", null,
                                                    "Service Unavailable. Unknown error" );
            return new ValidatorResponse( lastMessage, status );
        } catch ( XMLParsingException e ) {
            status = Status.RESULT_STATE_SERVICE_UNAVAILABLE;
            lastMessage = status.getStatusMessage();
            return new ValidatorResponse( lastMessage, status );
        }
    }

    /**
     * Makes sure that the HttpResponse is not a critical error
     *
     * @param statusCode
     */
    protected boolean isValidHttpResponse( int statusCode ) {

        if ( statusCode >= 100 && statusCode < 400 ) {
            return true;
        }
        return false;
    }

    /**
     * @param statusCode
     * @return instance of the validatorresponse with the error
     */
    protected ValidatorResponse validateErrorHttpResponse( int statusCode ) {

        String lastMessage = null;
        Status status = null;

        if ( statusCode == HttpServletResponse.SC_REQUEST_TIMEOUT ) {
            status = Status.RESULT_STATE_TIMEOUT;
            lastMessage = status.getStatusMessage();
        } else {
            status = Status.RESULT_STATE_PAGE_UNAVAILABLE;
            lastMessage = status.getStatusMessage();
        }

        return new ValidatorResponse( lastMessage, status );
    }

    /**
     * Parses a given InputStream to a String
     *
     * @param stream
     * @return String
     * @throws IOException
     */
    protected String parseStream( InputStream stream )
                            throws IOException {
        stream.reset();
        InputStreamReader reader = new InputStreamReader( stream );
        BufferedReader bufReader = new BufferedReader( reader );
        StringBuilder builder = new StringBuilder();
        String line = null;

        line = bufReader.readLine();
        while ( line != null ) {
            builder.append( line );
            line = bufReader.readLine();
        }

        String answer = builder.toString();
        return answer;
    }

    /**
     * Creates a new instance of DocumentBuilder
     *
     * @return DocumentBuilder
     * @throws IOException
     */
    protected DocumentBuilder instantiateParser()
                            throws IOException {

        DocumentBuilder parser = null;

        try {
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
            fac.setNamespaceAware( true );
            fac.setValidating( false );
            fac.setIgnoringElementContentWhitespace( false );
            parser = fac.newDocumentBuilder();
            return parser;
        } catch ( ParserConfigurationException e ) {
            throw new IOException( "Unable to initialize DocumentBuilder: " + e.getMessage() );
        }
    }

    protected boolean closeStream( InputStream stream ) {
        try {
            stream.close();
        } catch ( IOException e ) {
            LOG.logError( e.getLocalizedMessage() );
            return false;
        }
        return true;
    }

    /**
     * Creates a new copy of the given InputStream
     *
     * @param stream
     * @return InputStream
     * @throws IOException
     */
    protected InputStream copyStream( InputStream stream )
                            throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read;
        byte[] bs = new byte[16384];
        while ( ( read = stream.read( bs ) ) != -1 ) {
            out.write( bs, 0, read );
        }
        return new ByteArrayInputStream( out.toByteArray() );
    }
}

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
package org.deegree.enterprise.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.deegree.enterprise.ServiceException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.EchoRequest;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.csw.CSWExceptionCode;
import org.deegree.ogcwebservices.csw.CSWFactory;
import org.deegree.ogcwebservices.csw.CSWPropertiesAccess;
import org.deegree.ogcwebservices.csw.CatalogueService;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueCapabilities;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueGetCapabilities;
import org.deegree.ogcwebservices.csw.discovery.DescribeRecordResult;
import org.deegree.ogcwebservices.csw.discovery.GetRecordByIdResult;
import org.deegree.ogcwebservices.csw.discovery.GetRecordsResult;
import org.deegree.ogcwebservices.csw.discovery.GetRepositoryItem;
import org.deegree.ogcwebservices.csw.discovery.GetRepositoryItemResponse;
import org.deegree.ogcwebservices.csw.manager.HarvestResult;
import org.deegree.ogcwebservices.csw.manager.TransactionResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Web servlet client for CSW.
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </A>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 * @see <a href="http://www.dofactory.com/patterns/PatternChain.aspx">Chain of Responsibility Design Pattern </a>
 */

public class CSWHandler extends AbstractOWServiceHandler {

    private static final ILogger LOG = LoggerFactory.getLogger( CSWHandler.class );

    /**
     * @param request
     * @param httpResponse
     * @throws ServiceException
     * @throws OGCWebServiceException
     * @see "org.deegree.enterprise.servlet.ServiceDispatcher#perform(org.deegree.services.AbstractOGCWebServiceRequest,javax.servlet.http.HttpServletResponse)"
     */
    public void perform( OGCWebServiceRequest request, HttpServletResponse httpResponse )
                            throws ServiceException, OGCWebServiceException {

        LOG.logDebug( "Performing request: " + request.toString() );

        try {
            CatalogueService service = CSWFactory.getService();
            Object response = service.doService( request );
            if ( response instanceof OGCWebServiceException ) {
                if ( request instanceof GetRepositoryItem ) {
                    sendOWSException( httpResponse, (OGCWebServiceException) response );
                } else {
                    sendExceptionReport( httpResponse, (OGCWebServiceException) response );
                }
            } else if ( response instanceof Exception ) {
                sendExceptionReport( httpResponse, (Exception) response );
            } else if ( response instanceof CatalogueCapabilities ) {
                sendCapabilities( httpResponse, (CatalogueGetCapabilities) request, (CatalogueCapabilities) response );
            } else if ( response instanceof GetRecordsResult ) {
                sendGetRecord( httpResponse, (GetRecordsResult) response );
            } else if ( response instanceof GetRecordByIdResult ) {
                sendGetRecordById( httpResponse, (GetRecordByIdResult) response );
            } else if ( response instanceof DescribeRecordResult ) {
                sendDescribeRecord( httpResponse, (DescribeRecordResult) response );
            } else if ( response instanceof TransactionResult ) {
                sendTransactionResult( httpResponse, (TransactionResult) response );
            } else if ( response instanceof HarvestResult ) {
                sendHarvestResult( httpResponse, (HarvestResult) response );
            } else if ( response instanceof EchoRequest ) {
                sendHarvestResult( httpResponse );
            } else if ( response instanceof GetRepositoryItemResponse ) {
                sendGetRepositoryItem( httpResponse, (GetRepositoryItemResponse) response );
            } else {
                OGCWebServiceException e = new OGCWebServiceException(
                                                                       this.getClass().getName(),
                                                                       "Unknown response class: "
                                                                                               + ( response == null ? "null response object"
                                                                                                                   : response.getClass().getName() )
                                                                                               + "." );
                sendExceptionReport( httpResponse, e );
            }
        } catch ( IOException ex ) {
            throw new ServiceException( "Error while sending response: " + ex.getMessage(), ex );
        } catch ( OGCWebServiceException ogc ) {
            if ( request instanceof GetRepositoryItem ) {
                sendOWSException( httpResponse, ogc );
            } else {
                sendExceptionReport( httpResponse, ogc );
            }
        }

    }

    /**
     * @param httpResponse
     * @param response
     * @throws IOException
     * @throws ServiceException
     */
    private void sendGetRepositoryItem( HttpServletResponse httpResponse, GetRepositoryItemResponse response )
                            throws IOException, ServiceException {
        httpResponse.setContentType( "text/xml; charset=" + CharsetUtils.getSystemCharset() );
        PrintWriter pw = httpResponse.getWriter();
        try {
            response.getRepositoryItem().prettyPrint( pw );
            pw.flush();
        } catch ( TransformerException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new ServiceException( e.getLocalizedMessage(), e );
        }
    }

    /**
     * Sends the passed <tt>OGCWebServiceException</tt> to the calling client and flushes/closes the writer.
     *
     * @param responseWriter
     *            to write the message of the exception to
     * @param e
     *            the exception to 'send' e.g. write to the stream.
     * @throws ServiceException
     *             if a writer could not be created from the response.
     */
    private void sendOWSException( HttpServletResponse httpResponse, OGCWebServiceException e )
                            throws ServiceException {

        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            Thread.dumpStack();
        }
        httpResponse.setContentType( "application/xml" );
        LOG.logDebug( "Sending OGCWebServiceException to client with message: " + e.getMessage() );
        Document doc = XMLTools.create();

        XMLFragment frag = new XMLFragment( doc.createElementNS( CommonNamespaces.OWSNS.toString(),
                                                                 "ows:ExceptionReport" ) );
        Element message = null;
        ExceptionCode code = e.getCode();
        if ( code == null ) {
            code = CSWExceptionCode.WRS_INVALIDREQUEST;
        }
        if ( code == CSWExceptionCode.WRS_INVALIDREQUEST ) {
            message = XMLTools.appendElement( frag.getRootElement(), CommonNamespaces.WRS_EBRIMNS, code.value );
        } else {
            message = XMLTools.appendElement( frag.getRootElement(), CommonNamespaces.WRS_EBRIMNS,
                                              CSWExceptionCode.WRS_NOTFOUND.value );
        }
        XMLTools.setNodeValue( message, e.getMessage() );

        try {
            PrintWriter writer = httpResponse.getWriter();
            writer.write( frag.getAsPrettyString() );
            writer.flush();
        } catch ( IOException e1 ) {
            throw new ServiceException( e1 );
        }

        // writer.close();

    }

    /**
     * Sends the passed <tt>HarvestResult</tt> to the http client.
     *
     * @param httpResponse
     *            http connection to the client
     * @param result
     *            object to send
     * @throws IOException
     */
    private void sendHarvestResult( HttpServletResponse httpResponse, HarvestResult result )
                            throws IOException {
        XMLFragment doc = null;
        try {
            doc = org.deegree.ogcwebservices.csw.manager.XMLFactory.export( result );
        } catch ( XMLParsingException e ) {
            throw new IOException( "could not export TransactionResult as XML: " + e.getMessage() );
        }
        httpResponse.setContentType( "text/xml; charset=" + CharsetUtils.getSystemCharset() );
        OutputStream os = httpResponse.getOutputStream();
        doc.write( os );
        os.close();
    }

    /**
     *
     * @param httpResponse
     * @throws IOException
     */
    private void sendHarvestResult( HttpServletResponse httpResponse )
                            throws IOException {

        httpResponse.setContentType( "text/xml; charset=" + CharsetUtils.getSystemCharset() );
        PrintWriter pw = httpResponse.getWriter();
        pw.write( "<HarvestResponse>Harvest request has been received " );
        pw.write( "and will be performed</HarvestResponse>" );
        pw.close();
    }

    /**
     * Sends the passed <tt>TransactionResult</tt> to the http client.
     *
     * @param httpResponse
     *            http connection to the client
     * @param result
     *            object to send
     */
    private void sendTransactionResult( HttpServletResponse httpResponse, TransactionResult result )
                            throws IOException {
        XMLFragment doc = null;
        try {
            doc = org.deegree.ogcwebservices.csw.manager.XMLFactory.export( result );
        } catch ( XMLParsingException e ) {
            throw new IOException( "could not export TransactionResult as XML: " + e.getMessage() );
        }
        httpResponse.setContentType( "text/xml; charset=" + CharsetUtils.getSystemCharset() );
        OutputStream os = httpResponse.getOutputStream();
        doc.write( os );
        os.close();
    }

    /**
     * Sends the passed <tt>CatalogCapabilities</tt> to the http client.
     *
     * @param response
     *            http connection to the client
     * @param capabilities
     *            object to send
     */
    private void sendCapabilities( HttpServletResponse response, CatalogueGetCapabilities getCapabilities,
                                   CatalogueCapabilities capabilities )
                            throws IOException {

        boolean xmlOk = false;
        String[] formats = getCapabilities.getAcceptFormats();
        if ( formats == null || formats.length == 0 ) {
            xmlOk = true;
        } else {
            for ( int i = 0; i < formats.length; i++ ) {
                if ( formats[i].equals( "text/xml" ) ) {
                    xmlOk = true;
                    break;
                }
            }
        }
        if ( !xmlOk ) {
            ExceptionCode code = ExceptionCode.INVALIDPARAMETERVALUE;
            InvalidParameterValueException e = new InvalidParameterValueException( this.getClass().getName(),
                                                                                   "OutputFormat must be 'text/xml'.",
                                                                                   code );
            sendExceptionReport( response, e );
        } else {
            String version = getCapabilities.getVersion();
            String className = CSWPropertiesAccess.getString( "XMLFactory" + version );
            XMLFragment doc = null;
            try {
                Class<?> clzz = Class.forName( className );
                Class<?>[] parameterTypes = new Class<?>[] { CatalogueCapabilities.class, String[].class };
                Object[] parameters = new Object[] { capabilities, getCapabilities.getSections() };
                Method method = clzz.getMethod( "export", parameterTypes );
                doc = (XMLFragment) method.invoke( null, parameters );
            } catch ( Exception e ) {
                e.printStackTrace();
            }

            response.setContentType( "text/xml; charset=" + CharsetUtils.getSystemCharset() );
            OutputStream os = response.getOutputStream();
            doc.write( os );
            os.close();
        }
    }

    /**
     *
     * @param response
     * @param getRecordResponse
     * @throws IOException
     */
    private void sendGetRecord( HttpServletResponse response, GetRecordsResult getRecordResponse )
                            throws IOException {
        XMLFragment doc = org.deegree.ogcwebservices.csw.discovery.XMLFactory.export( getRecordResponse );
        if ( LOG.isDebug() ) {
            LOG.logDebug( "GetRecord response", doc.getAsPrettyString() );
        }
        response.setContentType( "text/xml; charset=" + Charset.defaultCharset().displayName() );
        OutputStream os = response.getOutputStream();
        doc.write( os );
        os.close();
    }

    /**
     *
     * @param response
     * @param getRecordByIdResponse
     * @throws IOException
     */
    private void sendGetRecordById( HttpServletResponse response, GetRecordByIdResult getRecordByIdResponse )
                            throws IOException {
        XMLFragment doc = org.deegree.ogcwebservices.csw.discovery.XMLFactory.export( getRecordByIdResponse );
        if ( LOG.isDebug() ) {
            LOG.logDebug( "GetRecordById response", doc.getAsPrettyString() );
        }
        response.setContentType( "text/xml" );
        OutputStream os = response.getOutputStream();
        doc.write( os );
        os.close();
    }

    /**
     *
     * @param response
     * @param describeRecordResponse
     * @throws IOException
     */
    private void sendDescribeRecord( HttpServletResponse response, DescribeRecordResult describeRecordResponse )
                            throws IOException {
        XMLFragment doc = org.deegree.ogcwebservices.csw.discovery.XMLFactory.export( describeRecordResponse );
        if ( LOG.isDebug() ) {
            LOG.logDebug( "DescribeRecord response", doc.getAsPrettyString() );
        }
        response.setContentType( "text/xml; charset=" + CharsetUtils.getSystemCharset() );
        OutputStream os = response.getOutputStream();
        doc.write( os );
        os.close();
    }
}

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.mail.MessagingException;
import javax.mail.internet.ContentDisposition;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.http.HttpServletRequest;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLException;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * <code>RequestMultiPartHandler</code> handles the multiparts of a request with the content-type
 * set to <code>multipart/form-data</code>. It appends the multiparts to an element which will be
 * retrieved by calling the {@link #getElementForId(XMLFragment, String)} method which should be
 * implemented by a sub-class.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public abstract class RequestMultiPartHandler {

    private static ILogger LOG = LoggerFactory.getLogger( RequestMultiPartHandler.class );

    /**
     * This method handles the multiparts of a ServletRequest. This method is called when the
     * <code>content-type:form/multipart</code> header is set. For this method to take affect, a
     * subclass must supply the xmlNode to which the multiparts will be appended.
     *
     * @param request
     *            the actual HttpServletRequest.
     * @return all the XML-Representations of the incoming request including the multiparts. The
     *         xmlFragment at index 0 is the first mime-multipart, e.g. the request, and the
     *         following are the multi-part elements. These elements could be hooked into the
     *         request by calling the the {@link #getElementForId(XMLFragment, String )} method and
     *         thus receiving the element to which the multipart should be appended. If the stream
     *         didn't contain parsable data an array containing the data up-to that multipart will
     *         be returned never <code>null</code>.
     * @throws OGCWebServiceException
     *             if an exception occurred while processing the mime parts.
     */
    public XMLFragment[] handleMultiparts( HttpServletRequest request )
                            throws OGCWebServiceException {
        XMLFragment[] parsedData = new XMLFragment[0];
        try {
            ByteArrayDataSource bads = new ByteArrayDataSource( request.getInputStream(), "application/xml" );
            LOG.logInfo( "Setting the 'mail.mime.multipart.ignoremissingendboundary' System property to false." );
            System.setProperty( "mail.mime.multipart.ignoremissingendboundary", "false" );
            MimeMultipart multi = new MimeMultipart( bads );
            parsedData = new XMLFragment[multi.getCount()];
            for ( int i = 0; i < multi.getCount(); i++ ) {
                MimeBodyPart content = (MimeBodyPart) multi.getBodyPart( i );

                if ( !( content.isMimeType( "application/xml" ) || content.isMimeType( "text/xml" ) ) ) {
                    throw new OGCWebServiceException(
                                                      "Other than xml-encoded data can not be handled in the multiparts",
                                                      ExceptionCode.INVALID_FORMAT );
                }
                String[] names = content.getHeader( "Content-Disposition" );
                String nameID = null;
                if ( names != null ) {
                    for ( String name : names ) {
                        ContentDisposition cd = new ContentDisposition( name );
                        String nm = cd.getParameter( "name" );
                        if ( nm != null ) {
                            nameID = nm;
                            break;
                        }
                    }
                }

                if ( nameID == null ) {
                    nameID = content.getContentID();
                    if ( nameID == null ) {
                        throw new OGCWebServiceException(
                                                          "Exactly one 'name' parameter must be set in the multipart-header.",
                                                          ExceptionCode.INVALID_FORMAT );
                    }
                }

                if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
                    StringBuilder sb = new StringBuilder( "Handling Multipart (" );
                    sb.append( ( i + 1 ) );
                    sb.append( " of " );
                    sb.append( multi.getCount() );
                    sb.append( ")\ncontent id: " ).append( content.getContentID() );
                    sb.append( "\ncontentType: " ).append( content.getContentType() );
                    sb.append( "\ncontent name: " ).append( nameID );
                    sb.append( "\nlineCount: " ).append( content.getLineCount() );
                    sb.append( "\nencoding: " ).append( content.getEncoding() );
                    LOG.logDebug( sb.toString() );
                }

                InputStream contentIS = null;
                if ( !"UTF-8".equalsIgnoreCase( content.getEncoding() ) ) {
                    contentIS = MimeUtility.decode( content.getInputStream(), content.getEncoding() );
                } else {
                    contentIS = content.getInputStream();
                }
                BufferedReader reader = new BufferedReader( new InputStreamReader( contentIS ) );
                String firstLine = reader.readLine();
                if ( !reader.ready() || firstLine == null ) {
                    throw new OGCWebServiceException( "No characters found in multipart with id: " + nameID,
                                                      ExceptionCode.INVALID_FORMAT );
                }
                LOG.logDebug( "first line of multipart: " + firstLine );
                // The root node is the first node to
                parsedData[i] = new XMLFragment( reader, XMLFragment.DEFAULT_URL );
                // if not the root node, append the attribute originalNameID to the root element of
                // the multipart. This
                // way the multiparts may be found again.
                if ( i != 0 ) {
                    Element root = parsedData[i].getRootElement();
                    if ( root != null ) {
                        root.setAttribute( "originalNameID", nameID );
                    } else {
                        LOG.logError( "Allthough the xml was parsed no root element was found, this is strange!!" );
                    }
                }
            }
        } catch ( IOException ioe ) {
            throw new OGCWebServiceException( "Following error occurred while handling the mime multiparts:"
                                              + ioe.getMessage(), ExceptionCode.INVALID_FORMAT );
        } catch ( MessagingException me ) {
            throw new OGCWebServiceException( "Following error occurred while handling the mime multiparts:"
                                              + me.getMessage(), ExceptionCode.INVALID_FORMAT );
        } catch ( XMLException xmle ) {
            throw new OGCWebServiceException( "Following error occurred while handling the mime multiparts:"
                                              + xmle.getMessage(), ExceptionCode.INVALID_FORMAT );
        } catch ( SAXException saxe ) {
            throw new OGCWebServiceException( "Following error occurred while handling the mime multiparts:"
                                              + saxe.getMessage(), ExceptionCode.INVALID_FORMAT );
        }
        // finished so lets give back the xml-tree as a result.
        return parsedData;
    }

    /**
     * Sub-classes should implement this method to supply the xml-nodes to which the multiparts will
     * be appended.
     *
     * @param xmlBody
     *            of the request.
     *
     * @param id
     *            of the multipart
     * @return the Element to which the multipart with given id will be appended.
     */
    public abstract Element getElementForId( XMLFragment xmlBody, String id );

}

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

package org.deegree.portal.owswatch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLException;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLTools;
import org.w3c.dom.Element;

/**
 * A class to log the testings results into a protocol file, i.e. a logger class
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ServiceLog implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2010721267404089317L;

    private static final ILogger LOG = LoggerFactory.getLogger( ServiceLog.class );

    private int serviceId = -1;

    private String serviceName = null;

    private String serviceType = null;

    private String protocolURI;

    private String protDirPath = null;

    private String protHttpAddr = null;

    private EmailSender emailSender = null;

    private ValidatorResponse response = null;

    /**
     * @param serviceId
     * @param serviceName
     * @param serviceType
     * @param protDirPath
     * @param servletAddr
     * @param emailSender
     *            The class responsible for sending emails
     * @throws IOException
     *             if the given paths are invalid
     * @throws XMLException
     *             if the xmlDocument(s) are mal formed
     */
    public ServiceLog( String protDirPath, int serviceId, String serviceName, String serviceType, String servletAddr,
                       EmailSender emailSender ) throws IOException {
        this.protDirPath = protDirPath;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
        this.serviceType = serviceType;
        this.emailSender = emailSender;

        StringBuilder builder = new StringBuilder( servletAddr );
        if ( !( builder.lastIndexOf( "?" ) == builder.length() - 1 ) ) {
            builder.append( "?" );
        }
        protHttpAddr = builder.append( "action=serviceProtocol&serviceId=" ).append( serviceId ).toString();

        this.response = new ValidatorResponse( "Not yet Tested", Status.RESULT_STATE_WAITING );
        response.setLastLapse( -1 );
    }

    /**
     * creates an xml protocol file for this Protocol Object if it does not already exist
     */
    protected Element init()
                            throws IOException {

        protocolURI = buildProtocolURI();

        Element root = null;
        try {
            File file = new File( protocolURI );

            if ( !file.exists() || file.length() == 0 ) {
                root = XMLTools.create().createElement( "owsWatchProtocol" );

                root.setAttribute( "serviceName", serviceName );
                root.setAttribute( "serviceType", serviceType );
                XMLFragment frag = new XMLFragment( root );
                frag.write( new FileWriter( protocolURI ) );
            } else {
                XMLFragment xmlFrag = new XMLFragment( new File( protocolURI ) );
                root = xmlFrag.getRootElement();
            }
        } catch ( Exception e ) {
            throw new IOException( Messages.getMessage( "ERROR_CONFS_EXCEPTION", protocolURI )
                                   + e.getLocalizedMessage() );
        }
        return root;
    }

    /**
     * adds a message to protocol file
     *
     * @param response
     * @param serviceconfig
     *
     */
    public void addMessage( ValidatorResponse response, ServiceConfiguration serviceconfig ) {

        try {

            Element root = init();
            if ( root != null ) {

                Element entry = XMLTools.appendElement( root, null, "Entry" );
                XMLTools.appendElement( entry, null, "TimeStamp", response.getLastTest().toString() );
                XMLTools.appendElement( entry, null, "Message", response.getMessage() );
                XMLTools.appendElement( entry, null, "Lapse", String.valueOf( response.getLastLapse() ) );

                XMLFragment frag = new XMLFragment( root );
                FileWriter writer = new FileWriter( new File( protocolURI ) );
                writer.write( frag.getAsPrettyString() );
                writer.close();
            } else {
                LOG.logError( "Exception while creating the Protocol" );
            }
        } catch ( Exception e ) {
            LOG.logError( "Error building the xml document in addMessage()", e );
        }

        if ( ( this.response.getStatus().isAvailable() || this.response.getStatus().isWaiting() )
             && !response.getStatus().isAvailable() && !response.getStatus().isWaiting() ) {
            emailSender.createAndSendMail( serviceconfig, response, getProtHttpAddr() );
        }
        this.response = response;
    }

    /**
     * @return String the uri of this Protocol Object
     */
    public String getProtocolURI() {
        return buildProtocolURI();
    }

    /**
     * @return protocol path
     */
    public String getProtPath() {
        return protocolURI;
    }

    /**
     * @return String
     */
    public String getProtDirPath() {
        return protDirPath;
    }

    /**
     * @param protDirPath
     */
    public void setProtDirPath( String protDirPath ) {
        this.protDirPath = protDirPath;
        protocolURI = buildProtocolURI();
    }

    private String buildProtocolURI() {

        if ( protocolURI != null ) {
            return protocolURI;
        }
        // Writing the file name
        StringBuffer buffer = new StringBuffer( 100 );
        buffer.append( protDirPath );
        if ( !protDirPath.endsWith( "/" ) ) {
            buffer.append( "/" );
        }
        buffer.append( "protocol_ID" ).append( "_" ).append( serviceId ).append( "_" );
        Calendar cal = Calendar.getInstance();
        // I added 1 to the month since the returned month index is always -1 that the true,
        // ex. january returns 0, feb 1, etc...
        buffer.append( cal.get( Calendar.YEAR ) ).append( "_" );
        int month = cal.get( Calendar.MONTH ) + 1;
        buffer.append( month > 9 ? month : "0" + month );
        buffer.append( ".xml" );

        return buffer.toString();
    }

    /**
     * @return http address that forwards you to the protocol page
     */
    public String getProtHttpAddr() {
        return protHttpAddr;
    }

    /**
     * @return {@link EmailSender}
     */
    public EmailSender getEmailSender() {
        return emailSender;
    }

    /**
     * @return {@link EmailSender}
     */
    public ValidatorResponse getResponse() {
        return response;
    }
}

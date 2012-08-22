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
package org.deegree.portal.cataloguemanager.control;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;

import javax.activation.DataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.TimeTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.portal.cataloguemanager.model.ExceptionBean;
import org.deegree.portal.cataloguemanager.model.MetadataBean;
import org.stringtree.json.JSONWriter;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class UploadMetadatasetListener extends AbstractMetadataListener {

    private static final ILogger LOG = LoggerFactory.getLogger( UploadMetadatasetListener.class );

    private static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    private XMLFragment xml;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.enterprise.control.ajax.AbstractListener#actionPerformed(org.deegree.enterprise.control.ajax.WebEvent
     * , org.deegree.enterprise.control.ajax.ResponseHandler)
     */
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {
        HttpServletRequest request = (HttpServletRequest) getRequest();

        try {
            StreamDataSource sds = new StreamDataSource( request );
            MimeMultipart multi = new MimeMultipart( sds );

            MimeBodyPart content = (MimeBodyPart) multi.getBodyPart( 0 );
            InputStream is = content.getInputStream();
            xml = new XMLFragment();
            xml.load( is, XMLFragment.DEFAULT_URL );

        } catch ( Exception e ) {
            LOG.logError( e );
            ExceptionBean bean = new ExceptionBean( this.getClass().getName(), e.getMessage() );
            responseHandler.writeAndClose( true, bean );
            return;
        }

        String rootName = xml.getRootElement().getLocalName();
        String uri = xml.getRootElement().getNamespaceURI();
        if ( uri != null && uri.trim().length() > 0 ) {
            rootName = '{' + uri + "}:" + rootName;
        }

        String s = getInitParameter( rootName );
        if ( s != null ) {
            File f = new File( s );
            if ( !f.isAbsolute() ) {
                f = new File( event.getAbsolutePath( getInitParameter( s ) ) );
            }
            try {
                XSLTDocument xslt = new XSLTDocument( f.toURL() );
                xml = xslt.transform( xml );
            } catch ( Exception e ) {
                LOG.logError( e );
                ExceptionBean bean = new ExceptionBean( this.getClass().getName(), e.getMessage() );
                responseHandler.writeAndClose( true, bean );
                return;
            }
        }

        event.getSession().setAttribute( "MD_TEMPLATE", xml );

        CatalogueManagerConfiguration config = getCatalogueManagerConfiguration( event );

        MetadataBean metadata = new MetadataBean();

        String tmp = getValue( config.getXPath( "datasetTitle" ) );
        if ( "".equals( tmp ) ) {
            tmp = getValue( config.getXPath( "seviceTitle" ) );
        }
        metadata.setDatasetTitle( tmp );
        metadata.setIdentifier( getValue( config.getXPath( "identifier" ) ) );
        metadata.setAbstract_( getValue( config.getXPath( "abstract_" ) ) );
        tmp = getValue( config.getXPath( "begin" ) );
        if ( tmp != null && tmp.trim().length() > 0 ) {
            Calendar cal = TimeTools.createCalendar( tmp );
            metadata.setBegin( toISO( cal ) );
        }
        metadata.setContactCity( getValue( config.getXPath( "contactCity" ) ) );
        metadata.setContactCountry( getValue( config.getXPath( "contactCountry" ) ) );
        metadata.setContactDeliveryPoint( getValue( config.getXPath( "contactDeliveryPoint" ) ) );
        metadata.setContactEmailAddress( getValue( config.getXPath( "contactEmailAddress" ) ) );
        metadata.setContactFacsimile( getValue( config.getXPath( "contactFacsimile" ) ) );
        metadata.setContactIndividualName( getValue( config.getXPath( "contactIndividualName" ) ) );
        metadata.setContactOrganisationName( getValue( config.getXPath( "contactOrganisationName" ) ) );
        metadata.setContactPostalCode( getValue( config.getXPath( "contactPostalCode" ) ) );
        metadata.setContactRole( getValue( config.getXPath( "contactRole" ) ) );
        metadata.setContactVoice( getValue( config.getXPath( "contactVoice" ) ) );
        tmp = getValue( config.getXPath( "creation" ) );
        if ( tmp != null && tmp.trim().length() > 0 ) {
            Calendar cal = TimeTools.createCalendar( tmp );
            metadata.setCreation( toISO( cal ) );
        }
        metadata.setCrs( getValue( config.getXPath( "crs" ) ) );
        tmp = getValue( config.getXPath( "end" ) );
        if ( tmp != null && tmp.trim().length() > 0 ) {
            Calendar cal = TimeTools.createCalendar( tmp );
            metadata.setEnd( toISO( cal ) );
        }
        metadata.setGeogrDescription( getValue( config.getXPath( "geogrDescription" ) ) );
        metadata.setHlevel( getValue( config.getXPath( "hlevel" ) ) );

        metadata.setKeywords( getValues( config.getXPath( "keywords" ) ) );
        metadata.setParentId( getValue( config.getXPath( "parentId" ) ) );
        metadata.setPocCity( getValue( config.getXPath( "pocCity" ) ) );
        metadata.setPocCountry( getValue( config.getXPath( "pocCountry" ) ) );
        metadata.setPocDeliveryPoint( getValue( config.getXPath( "pocDeliveryPoint" ) ) );
        metadata.setPocEmailAddress( getValue( config.getXPath( "pocEmailAddress" ) ) );
        metadata.setPocFacsimile( getValue( config.getXPath( "pocFacsimile" ) ) );
        metadata.setPocIndividualName( getValue( config.getXPath( "pocIndividualName" ) ) );
        metadata.setPocOrganisationName( getValue( config.getXPath( "pocOrganisationName" ) ) );
        metadata.setPocPostalCode( getValue( config.getXPath( "pocPostalCode" ) ) );
        metadata.setPocRole( getValue( config.getXPath( "pocRole" ) ) );
        metadata.setPocVoice( getValue( config.getXPath( "pocVoice" ) ) );
        tmp = getValue( config.getXPath( "publication" ) );
        if ( tmp != null && tmp.trim().length() > 0 ) {
            Calendar cal = TimeTools.createCalendar( tmp );
            metadata.setPublication( toISO( cal ) );
        }
        tmp = getValue( config.getXPath( "revision" ) );
        if ( tmp != null && tmp.trim().length() > 0 ) {
            Calendar cal = TimeTools.createCalendar( tmp );
            metadata.setRevision( toISO( cal ) );
        }
        metadata.setScale( getValue( config.getXPath( "scale" ) ) );
        metadata.setTopCat( getValue( config.getXPath( "topCat" ) ) );

        tmp = getValue( config.getXPath( "begin" ) );
        if ( tmp.length() == 0 ) {
            tmp = getValue( config.getXPath( "begin2" ) );
        }
        Calendar cal = TimeTools.createCalendar( tmp );
        metadata.setBegin( toISO( cal ) );

        tmp = getValue( config.getXPath( "end" ) );
        if ( tmp.length() == 0 ) {
            tmp = getValue( config.getXPath( "end2" ) );
        }
        cal = TimeTools.createCalendar( tmp );
        metadata.setEnd( toISO( cal ) );
        metadata.setLineage( getValue( config.getXPath( "lineage" ) ) );

        metadata.setTransferOnline( getValue( config.getXPath( "transferOptOnline" ) ) );
        metadata.setTransferFormatName( getValue( config.getXPath( "transferOptFormatName" ) ) );
        metadata.setTransferFormatVersion( getValue( config.getXPath( "transferOptFormatVersion" ) ) );

        s = getValue( config.getXPath( "accessConstraints" ) );
        if ( s == null ) {
            s = getValue( config.getXPath( "srvAccessConstraints" ) );
        }
        metadata.setAccessConstraints( s );

        JSONWriter writer = new JSONWriter( false );
        metadata.getClass().getModifiers();
        request.setAttribute( "TEMPLATES", writer.write( metadata ) );

        try {
            HttpServletResponse resp = responseHandler.getHttpServletResponse();
            // result page uses UTF-8 encoding
            resp.setCharacterEncoding( "UTF-8" );
            request.getRequestDispatcher( '/' + getNextPage() ).forward( request, resp );
        } catch ( ServletException e ) {
            LOG.logError( e );
            ExceptionBean bean = new ExceptionBean( this.getClass().getName(), e.getMessage() );
            responseHandler.writeAndClose( true, bean );
            return;
        }
    }

    private String toISO( Calendar cal ) {
        StringBuffer sb = new StringBuffer();
        sb.append( Integer.toString( cal.get( Calendar.YEAR ) ) ).append( '-' );
        sb.append( Integer.toString( cal.get( Calendar.MONTH ) + 1 ) ).append( '-' );
        sb.append( Integer.toString( cal.get( Calendar.DAY_OF_MONTH ) ) );
        return sb.toString();
    }

    private String getValue( String xPath )
                            throws IOException {
        try {
            return XMLTools.getNodeAsString( xml.getRootElement(), xPath, nsContext, "" );
        } catch ( XMLParsingException e ) {
            throw new IOException( e.getMessage() );
        }
    }

    private List<String> getValues( String xPath )
                            throws IOException {
        try {
            return XMLTools.getNodesAsStringList( xml.getRootElement(), xPath, nsContext );
        } catch ( XMLParsingException e ) {
            throw new IOException( e.getMessage() );
        }
    }

    /**
     * This class maps the request stream to the content parser that is able to pick files from it.
     */
    private class StreamDataSource implements DataSource {
        private HttpServletRequest m_req;

        public StreamDataSource( HttpServletRequest req ) {
            m_req = req;
        }

        /**
         * Returns the content type for the request stream.
         */
        public String getContentType() {
            return m_req.getContentType();
        }

        /**
         * Returns a stream from the request.
         */
        public InputStream getInputStream()
                                throws IOException {
            return m_req.getInputStream();
        }

        /**
         * This method is useless and it always returns a null.
         */
        public String getName() {
            return null;
        }

        /**
         * Maps output to System.out. Do something more sensible here...
         */
        public OutputStream getOutputStream() {
            return System.out;
        }

    }

}

//$HeadURL: 
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
package org.deegree.portal.cataloguemanager.control;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpMethod;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.HttpUtils;
import org.deegree.framework.util.TimeTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.portal.cataloguemanager.model.ExceptionBean;
import org.deegree.portal.cataloguemanager.model.MetadataBean;

/**
 * 
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class LoadMetadataSetListener extends AbstractMetadataListener {

    private static ILogger LOG = LoggerFactory.getLogger( LoadMetadataSetListener.class );

    private static String GRBID = "request=GetRecordById&OUTPUTSCHEMA=http://www.isotc211.org/2005/gmd&service=CSW&version=2.0.2&ID=";

    private static String PARAM_ID = "ID";

    private XMLFragment xml;

    private static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    @SuppressWarnings("unchecked")
    @Override
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {

        CatalogueManagerConfiguration config = getCatalogueManagerConfiguration( event );
        Map<String, String> param = event.getParameter();
        LOG.logDebug( "load metadataset request param: ", param );
        String charset = event.getCharacterEncoding();
        if ( charset == null ) {
            charset = Charset.defaultCharset().displayName();
        }

        String s = URLEncoder.encode( param.get( PARAM_ID ), charset );
        URL url = new URL( config.getCatalogueURL() + '?' + GRBID + s );
        LOG.logDebug( "load metadataset: ", url );
        try {
            Enumeration<String> en = ( (HttpServletRequest) getRequest() ).getHeaderNames();
            Map<String, String> map = new HashMap<String, String>();
            while ( en.hasMoreElements() ) {
                String name = (String) en.nextElement();
                if ( !name.equalsIgnoreCase( "accept-encoding" ) && !name.equalsIgnoreCase( "content-length" )
                     && !name.equalsIgnoreCase( "user-agent" ) ) {
                    map.put( name, ( (HttpServletRequest) getRequest() ).getHeader( name ) );
                }
            }
            HttpMethod get = HttpUtils.performHttpGet( url.toExternalForm(), null, 60000, null, null, map );
            xml = new XMLFragment();
            xml.load( get.getResponseBodyAsStream(), url.toExternalForm() );
            xml = new XMLFragment( XMLTools.getFirstChildElement( xml.getRootElement() ) );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            ExceptionBean eb = new ExceptionBean( getClass().getName(), e.getMessage() );
            responseHandler.writeAndClose( true, eb );
            return;
        }
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( xml.getAsPrettyString() );
        }

        event.getSession().setAttribute( "MD_TEMPLATE", xml );

        MetadataBean metadata = new MetadataBean();

        if ( param.get( "ISTEMPLATE" ) == null || "false".equalsIgnoreCase( param.get( "ISTEMPLATE" ) ) ) {
            String tmp = getValue( config.getXPath( "datasetTitle" ) );
            if ( "".equals( tmp ) ) {
                tmp = getValue( config.getXPath( "seviceTitle" ) );
            }
            metadata.setDatasetTitle( tmp );
            metadata.setIdentifier( getValue( config.getXPath( "identifier" ) ) );
            metadata.setAbstract_( getValue( config.getXPath( "abstract_" ) ) );
        }
        String tmp = getValue( config.getXPath( "begin" ) );
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

        // result page uses UTF-8 encoding
        String charEnc = Charset.defaultCharset().displayName();
        responseHandler.setContentType( "application/json; charset=" + charEnc );
        responseHandler.writeAndClose( true, metadata );
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
            LOG.logError( e );
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
}

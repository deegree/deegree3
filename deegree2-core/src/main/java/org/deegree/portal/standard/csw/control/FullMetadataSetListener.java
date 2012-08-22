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
package org.deegree.portal.standard.csw.control;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.HttpMethod;
import org.deegree.enterprise.control.ajax.AbstractListener;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.HttpUtils;
import org.deegree.framework.util.KVP2Map;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.i18n.Messages;
import org.deegree.portal.cataloguemanager.model.ExceptionBean;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FullMetadataSetListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( FullMetadataSetListener.class );

    private URL xslURL;

    private Locale loc;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.deegree.enterprise.control.ajax.AbstractListener#actionPerformed(org.deegree.enterprise.control.ajax.WebEvent
     * , org.deegree.enterprise.control.ajax.ResponseHandler)
     */
    @SuppressWarnings("unchecked")
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {

        loc = getRequest().getLocale();

        String p = event.getAbsolutePath( getInitParameter( "XSLT" ) );
        xslURL = new File( p ).toURL();

        Map<String, String> param = event.getParameter();
        System.out.println( param );
        String request = param.get( "request" );
        XMLFragment resultXML = null;
        try {
            resultXML = performQuery( request );
        } catch ( Exception e ) {
            LOG.logError( e );
            ExceptionBean bean = new ExceptionBean( this.getClass().getName(), e.getMessage() );
            responseHandler.writeAndClose( true, bean );
            return;
        }
        writeHTML( responseHandler, resultXML );

    }

    private void writeHTML( ResponseHandler responseHandler, XMLFragment resultXML )
                            throws IOException {
        String html;
        try {
            html = formatResult( resultXML );
        } catch ( Exception e ) {
            LOG.logError( e );
            ExceptionBean bean = new ExceptionBean( this.getClass().getName(), e.getMessage() );
            responseHandler.writeAndClose( true, bean );
            return;
        }
        String charEnc = Charset.defaultCharset().displayName();
        responseHandler.setContentType( "application/json; charset=" + charEnc );
        responseHandler.writeAndClose( html );
    }

    /**
     * 
     * @param resultXML
     * @return
     * @throws Exception
     */
    private String formatResult( XMLFragment resultXML )
                            throws Exception {
        XSLTDocument xsl = new XSLTDocument( xslURL );
        ByteArrayOutputStream bos = new ByteArrayOutputStream( 10000 );
        DOMSource xmlSource = new DOMSource( resultXML.getRootElement() );
        DOMSource xslSource = new DOMSource( xsl.getRootElement().getOwnerDocument(),
                                             xsl.getSystemId() == null ? null : xsl.getSystemId().toString() );
        StreamResult sr = new StreamResult( bos );
        Map<String, String> param = new HashMap<String, String>();
        param.put( "TITLE", Messages.get( loc, "CATMANAGE_RESULT_TITLE" ) );
        param.put( "ABSTRACT", Messages.get( loc, "CATMANAGE_RESULT_ABSTRACT" ) );
        param.put( "TOPICCATEGORY", Messages.get( loc, "CATMANAGE_RESULT_TOPICCATEGORY" ) );
        param.put( "HIERARCHYLEVEL", Messages.get( loc, "CATMANAGE_RESULT_HIERARCHYLEVEL" ) );
        param.put( "GEOGRDESC", Messages.get( loc, "CATMANAGE_RESULT_GEOGRDESC" ) );
        param.put( "CREATIONDATE", Messages.get( loc, "CATMANAGE_RESULT_CREATIONDATE" ) );
        param.put( "PUBLICATIONDATE", Messages.get( loc, "CATMANAGE_RESULT_PUBLICATIONDATE" ) );
        param.put( "REVISIONDATE", Messages.get( loc, "CATMANAGE_RESULT_REVISIONDATE" ) );
        param.put( "CONTACT", Messages.get( loc, "CATMANAGE_RESULT_CONTACT" ) );
        param.put( "NAME", Messages.get( loc, "CATMANAGE_RESULT_NAME" ) );
        param.put( "ORGANISATION", Messages.get( loc, "CATMANAGE_RESULT_ORGANISATION" ) );
        param.put( "ADDRESS", Messages.get( loc, "CATMANAGE_RESULT_ADDRESS" ) );
        param.put( "VOICE", Messages.get( loc, "CATMANAGE_RESULT_VOICE" ) );
        param.put( "FAX", Messages.get( loc, "CATMANAGE_RESULT_FAX" ) );
        param.put( "EMAIL", Messages.get( loc, "CATMANAGE_RESULT_EMAIL" ) );
        param.put( "DISTONLINE", Messages.get( loc, "CATMANAGE_RESULT_DISTONLINE" ) );

        XSLTDocument.transform( xmlSource, xslSource, sr, null, param );
        return new String( bos.toByteArray() );
    }

    /**
     * @param cswAddress
     * @return
     */
    @SuppressWarnings("unchecked")
    private XMLFragment performQuery( String request )
                            throws Exception {
        LOG.logDebug( "GetRecordById: ", request );
        Enumeration<String> en = ( (HttpServletRequest) getRequest() ).getHeaderNames();
        Map<String, String> map = new HashMap<String, String>();
        while ( en.hasMoreElements() ) {
            String name = (String) en.nextElement();
            if ( !name.equalsIgnoreCase( "accept-encoding" ) && !name.equalsIgnoreCase( "content-length" )
                 && !name.equalsIgnoreCase( "user-agent" ) ) {
                map.put( name, ( (HttpServletRequest) getRequest() ).getHeader( name ) );
            }
        }
        URL url = new URL( request );
        map = KVP2Map.toMap( url.getQuery() );
        StringBuilder sb = new StringBuilder( url.toExternalForm().split( "\\?" )[0] );
        sb.append( '?' );
        Iterator<String> iter = map.keySet().iterator();
        while ( iter.hasNext() ) {
            String key = (String) iter.next();
            sb.append( key ).append( '=' ).append(
                                                   URLEncoder.encode( map.get( key ),
                                                                      Charset.defaultCharset().displayName() ) );
            if ( iter.hasNext() ) {
                sb.append( '&' );
            }
        }
        HttpMethod method = HttpUtils.performHttpGet( sb.toString(), null, 60000, null, null, map );
        XMLFragment xml = new XMLFragment();
        xml.load( method.getResponseBodyAsStream(), request );
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( "GetRecordById result: ", xml.getAsPrettyString() );
        }
        return xml;
    }

}

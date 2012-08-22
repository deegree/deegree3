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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperRunManager;

import org.apache.commons.httpclient.HttpMethod;
import org.deegree.datatypes.QualifiedName;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.FileUtils;
import org.deegree.framework.util.HttpUtils;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.i18n.Messages;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.Literal;
import org.deegree.model.filterencoding.LogicalOperation;
import org.deegree.model.filterencoding.Operation;
import org.deegree.model.filterencoding.OperationDefines;
import org.deegree.model.filterencoding.PropertyIsLikeOperation;
import org.deegree.model.filterencoding.PropertyName;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcwebservices.csw.discovery.GetRecords;
import org.deegree.ogcwebservices.csw.discovery.Query;
import org.deegree.ogcwebservices.csw.discovery.XMLFactory;
import org.deegree.ogcwebservices.csw.discovery.GetRecords.RESULT_TYPE;
import org.deegree.portal.cataloguemanager.model.ExceptionBean;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FullMetadataSetListener extends AbstractMetadataListener {

    private static final ILogger LOG = LoggerFactory.getLogger( FullMetadataSetListener.class );

    private CatalogueManagerConfiguration config;

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

        config = getCatalogueManagerConfiguration( event );
        Map<String, String> param = event.getParameter();
        String id = param.get( "ID" );
        String format = param.get( "FORMAT" );
        String cswAddress = param.get( "CSW" );
        GetRecords getRecords = createGetRecords( id );
        XMLFragment resultXML = null;
        try {
            resultXML = performQuery( getRecords, cswAddress );
        } catch ( Exception e ) {
            LOG.logError( e );
            ExceptionBean bean = new ExceptionBean( this.getClass().getName(), e.getMessage() );
            responseHandler.writeAndClose( true, bean );
            return;
        }

        if ( "XML".equalsIgnoreCase( format ) ) {
            writeXML( responseHandler, resultXML );
        } else if ( "PDF".equalsIgnoreCase( format ) ) {
            writePDF( event, responseHandler, resultXML );
        } else {
            writeHTML( responseHandler, cswAddress, resultXML );
        }

    }

    private void writePDF( WebEvent event, ResponseHandler responseHandler, XMLFragment resultXML )
                            throws IOException {
        Properties p = Messages.getProperties( loc );
        XSLTDocument xsl = null;
        try {
            xsl = new XSLTDocument( config.getPdfXSL() );
            XMLFragment xml = xsl.transform( resultXML );
            List<Node> nodes = XMLTools.getNodes( xml.getRootElement(), "./*", CommonNamespaces.getNamespaceContext() );
            for ( Node node : nodes ) {
                if ( node instanceof Element ) {
                    String key = node.getLocalName();
                    String value = XMLTools.getStringValue( node );
                    p.put( key, value );
                }
            }

            JRDataSource ds = new JREmptyDataSource();
            String path = event.getAbsolutePath( "./WEB-INF/conf/cataloguemanager/reports/report1.jasper" );
            byte[] result = JasperRunManager.runReportToPdf( path, p, ds );
            HttpServletResponse resp = responseHandler.getHttpServletResponse();
            resp.setContentType( "application/pdf" );
            resp.setContentLength( result.length );
            OutputStream os = resp.getOutputStream();
            os.write( result );
            os.flush();
            os.close();
        } catch ( Exception e ) {
            LOG.logError( e );
            ExceptionBean bean = new ExceptionBean( this.getClass().getName(), e.getMessage() );
            responseHandler.writeAndClose( true, bean );
            return;
        }

    }

    private void writeHTML( ResponseHandler responseHandler, String cswAddress, XMLFragment resultXML )
                            throws IOException {
        String html;
        try {
            html = formatResult( resultXML, cswAddress );
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

    private void writeXML( ResponseHandler responseHandler, XMLFragment resultXML )
                            throws IOException {
        String charEnc = Charset.defaultCharset().displayName();
        responseHandler.setContentType( "text/xml; charset=" + charEnc );
        Element md;
        try {
            md = XMLTools.getElement( resultXML.getRootElement(), "csw202:SearchResults/gmd:MD_Metadata",
                                      CommonNamespaces.getNamespaceContext() );
        } catch ( XMLParsingException e ) {
            LOG.logError( e );
            throw new IOException( e.getMessage() );
        }
        String s = "<table width='900' cellpadding='2' border='0'><tr><td width='80'></td>";
        s += ( "<td><textarea cols='97' rows='35'>" + new XMLFragment( md ).getAsPrettyString() + "</textarea></td>" );
        s += "</tr><tr><td></td><td><a class='button' href='javascript:closeFullMD()'>";
        s += ( Messages.get( loc, "CATMANAGE_RESULT_CLOSE" ) + "</a></td>" );
        s += "</tr></table>";
        responseHandler.writeAndClose( s );
    }

    /**
     * @param resultXML
     * @return
     */
    private String formatResult( XMLFragment resultXML, String cswAddress )
                            throws Exception {
        URL xslURL = config.getFullHTMLXSL();
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
        param.put( "CLOSE", Messages.get( loc, "CATMANAGE_RESULT_CLOSE" ) );
        param.put( "XML", Messages.get( loc, "CATMANAGE_RESULT_XML" ) );
        param.put( "PDF", Messages.get( loc, "CATMANAGE_RESULT_PDF" ) );
        param.put( "SERVICES", Messages.get( loc, "CATMANAGE_RESULT_SERVICES" ) );
        param.put( "CSW", cswAddress );

        XSLTDocument.transform( xmlSource, xslSource, sr, null, param );
        return new String( bos.toByteArray() );
    }

    /**
     * @param cswAddress
     * @return
     */
    @SuppressWarnings("unchecked")
    private XMLFragment performQuery( GetRecords getRecords, String cswAddress )
                            throws Exception {
        XMLFragment gr = XMLFactory.exportWithVersion( getRecords );
        LOG.logDebug( "GetRecords: ", gr.getAsPrettyString() );
        Enumeration<String> en = ( (HttpServletRequest) getRequest() ).getHeaderNames();
        Map<String, String> map = new HashMap<String, String>();
        while ( en.hasMoreElements() ) {
            String name = (String) en.nextElement();
            if ( !name.equalsIgnoreCase( "accept-encoding" ) && !name.equalsIgnoreCase( "content-length" )
                 && !name.equalsIgnoreCase( "user-agent" ) ) {
                map.put( name, ( (HttpServletRequest) getRequest() ).getHeader( name ) );
            }
        }
        HttpMethod method = HttpUtils.performHttpPost( cswAddress, gr, 60000, null, null, map );
        XMLFragment xml = new XMLFragment();
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            String s = FileUtils.readTextFile( method.getResponseBodyAsStream() ).toString();
            LOG.logDebug( "CSW Address: ", cswAddress );
            LOG.logDebug( "Header: ", map );
            LOG.logDebug( "GetRecords result: ", s );
            xml.load( new StringReader( s ), cswAddress );
        } else {
            xml.load( method.getResponseBodyAsStream(), cswAddress );
        }
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( "GetRecords result: ", xml.getAsPrettyString() );
        }
        return xml;
    }

    /**
     * @return
     */
    private GetRecords createGetRecords( String id ) {
        QualifiedName qn = new QualifiedName( "ResourceIdentifier",
                                              URI.create( "http://www.opengis.net/cat/csw/apiso/1.0" ) );
        PropertyName propertyName = new PropertyName( qn );
        PropertyIsLikeOperation operation1 = new PropertyIsLikeOperation( propertyName, new Literal( '*' + id + '*' ),
                                                                          '*', '?', '/' );
        qn = new QualifiedName( "Identifier", URI.create( "http://www.opengis.net/cat/csw/apiso/1.0" ) );
        propertyName = new PropertyName( qn );
        PropertyIsLikeOperation operation2 = new PropertyIsLikeOperation( propertyName, new Literal( '*' + id + '*' ),
                                                                          '*', '?', '/' );
        List<Operation> list = new ArrayList<Operation>();
        list.add( operation1 );
        list.add( operation2 );
        Filter filter = new ComplexFilter( new LogicalOperation( OperationDefines.OR, list ) );
        List<QualifiedName> typeNames = new ArrayList<QualifiedName>();
        typeNames.add( new QualifiedName( "{http://www.isotc211.org/2005/gmd}:MD_Metadata" ) );
        Query query = new Query( "full", new ArrayList<QualifiedName>(), new HashMap<String, QualifiedName>(),
                                 new ArrayList<PropertyPath>(), filter, null, typeNames,
                                 new HashMap<String, QualifiedName>() );
        return new GetRecords( UUID.randomUUID().toString(), "2.0.2", null, null, RESULT_TYPE.RESULTS,
                               "application/xml", "http://www.isotc211.org/2005/gmd", 1, config.getStepSize() * 2, -1,
                               null, query );
    }

}

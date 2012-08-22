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
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.httpclient.HttpMethod;
import org.deegree.datatypes.QualifiedName;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.HttpUtils;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
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

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class FindServicesListener extends AbstractMetadataListener {

    private static final ILogger LOG = LoggerFactory.getLogger( FindServicesListener.class );

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
        String id = param.get( "resourceIdentifier" );
        String cswAddress = param.get( "CSW" );
        GetRecords getRecords = createGetRecords( id );
        GetRecords getRecordsForData = createGetRecordsForData( id );
        XMLFragment resultXML = null;
        XMLFragment resultXMLForData = null;
        try {
            resultXML = performQuery( getRecords, cswAddress );
            resultXMLForData = performQuery( getRecordsForData, cswAddress );
        } catch ( Exception e ) {
            LOG.logError( e );
            ExceptionBean bean = new ExceptionBean( this.getClass().getName(), e.getMessage() );
            responseHandler.writeAndClose( true, bean );
            return;
        }

        writeHTML( responseHandler, cswAddress, resultXML, resultXMLForData, id );
    }

    /**
     * @param id
     * @return
     */
    private GetRecords createGetRecords( String id ) {
        QualifiedName qn = new QualifiedName( "OperatesOnIdentifier",
                                              URI.create( "http://www.opengis.net/cat/csw/apiso/1.0" ) );
        PropertyName propertyName = new PropertyName( qn );
        PropertyIsLikeOperation operation = new PropertyIsLikeOperation( propertyName, new Literal( '*' + id + '*' ),
                                                                         '*', '?', '/' );

        Filter filter = new ComplexFilter( operation );
        List<QualifiedName> typeNames = new ArrayList<QualifiedName>();
        typeNames.add( new QualifiedName( "{http://www.isotc211.org/2005/gmd}:MD_Metadata" ) );
        Query query = new Query( "full", new ArrayList<QualifiedName>(), new HashMap<String, QualifiedName>(),
                                 new ArrayList<PropertyPath>(), filter, null, typeNames,
                                 new HashMap<String, QualifiedName>() );
        return new GetRecords( UUID.randomUUID().toString(), "2.0.2", null, null, RESULT_TYPE.RESULTS,
                               "application/xml", "http://www.isotc211.org/2005/gmd", 1, config.getStepSize() * 2, -1,
                               null, query );
    }

    /**
     * @return
     */
    private GetRecords createGetRecordsForData( String id ) {
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
        xml.load( method.getResponseBodyAsStream(), cswAddress );
        if ( LOG.getLevel() == ILogger.LOG_DEBUG ) {
            LOG.logDebug( "GetRecords result: ", xml.getAsPrettyString() );
        }
        return xml;
    }

    /**
     * 
     * @param responseHandler
     * @param cswAddress
     * @param resultXML
     * @param resultXMLForData
     * @param datasetId
     * @throws IOException
     */
    private void writeHTML( ResponseHandler responseHandler, String cswAddress, XMLFragment resultXML,
                            XMLFragment resultXMLForData, String datasetId )
                            throws IOException {
        String html;
        try {
            html = formatResult( resultXML, resultXMLForData, cswAddress, datasetId );
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
     * @param resultXMLForData
     * @param cswAddress
     * @param datasetId
     * @return
     * @throws Exception
     */
    private String formatResult( XMLFragment resultXML, XMLFragment resultXMLForData, String cswAddress,
                                 String datasetId )
                            throws Exception {
        URL xslURL = config.getLinkageXSL();
        XSLTDocument xsl = new XSLTDocument( xslURL );
        ByteArrayOutputStream bos = new ByteArrayOutputStream( 10000 );
        DOMSource xmlSource = new DOMSource( resultXML.getRootElement() );
        DOMSource xslSource = new DOMSource( xsl.getRootElement().getOwnerDocument(),
                                             xsl.getSystemId() == null ? null : xsl.getSystemId().toString() );
        StreamResult sr = new StreamResult( bos );
        Map<String, String> param = new HashMap<String, String>();
        param.put( "DATASETID", datasetId );
        param.put( "TITLE", Messages.get( loc, "CATMANAGE_RESULT_TITLE" ) );
        param.put( "SERVICETYPE", Messages.get( loc, "CATMANAGE_RESULT_SERVICETYPE" ) );
        param.put( "ADDTOPORTAL", Messages.get( loc, "CATMANAGE_RESULT_ADDTOPORTAL" ) );
        param.put( "OPENCAPS", Messages.get( loc, "CATMANAGE_RESULT_OPENCAPS" ) );
        param.put( "OPENBUTTON", Messages.get( loc, "CATMANAGE_RESULT_OPENBUTTON" ) );
        param.put( "ADDBUTTON", Messages.get( loc, "CATMANAGE_RESULT_ADDBUTTON" ) );
        param.put( "CLOSE", Messages.get( loc, "CATMANAGE_RESULT_CLOSE" ) );
        param.put( "CSW", cswAddress );
        NamespaceContext nsc = CommonNamespaces.getNamespaceContext();
        String xpath = "csw202:SearchResults/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:citation/gmd:CI_Citation/gmd:title/gco:CharacterString";
        String datasetTitle = XMLTools.getRequiredNodeAsString( resultXMLForData.getRootElement(), xpath, nsc );
        xpath = "csw202:SearchResults/gmd:MD_Metadata/gmd:identificationInfo/gmd:MD_DataIdentification/gmd:abstract/gco:CharacterString";
        String abstract_ = XMLTools.getNodeAsString( resultXMLForData.getRootElement(), xpath, nsc, "" );
        param.put( "DATASETTITLE", datasetTitle );
        param.put( "DATASETABSTRACT", abstract_ );

        XSLTDocument.transform( xmlSource, xslSource, sr, null, param );
        return new String( bos.toByteArray() );
    }

}

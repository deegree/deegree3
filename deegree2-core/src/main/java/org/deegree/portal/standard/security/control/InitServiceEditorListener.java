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
package org.deegree.portal.standard.security.control;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.deegree.enterprise.control.AbstractListener;
import org.deegree.enterprise.control.FormEvent;
import org.deegree.enterprise.control.RPCException;
import org.deegree.enterprise.control.RPCMethodCall;
import org.deegree.enterprise.control.RPCParameter;
import org.deegree.enterprise.control.RPCWebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.NetWorker;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.security.UnauthorizedException;
import org.deegree.security.drm.SecurityAccess;
import org.deegree.security.drm.SecurityAccessManager;
import org.deegree.security.drm.model.User;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * This <code>Listener</code> reacts on 'initServiceAdministration'-events, queries the WCAS and
 * passes the service data on to be displayed by the JSP.
 * <p>
 * NOTE: The submitted catalog name in the event is currently ignored, the catalog to be queried is
 * taken from the harvester configuration.
 * </p>
 *
 * @author <a href="mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class InitServiceEditorListener extends AbstractListener {

    private static final NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    private static final ILogger LOG = LoggerFactory.getLogger( InitServiceEditorListener.class );

    // address of the catalog-server to query
    protected static URL catalogURL;

    /**
     * Called by init-method of <code>SecurityRequestDispatcher</code> once.
     *
     * @param configURL
     * @throws IOException
     * @throws SAXException
     * @throws XMLParsingException
     */
    static void setHarvesterConfig( String configURL )
                            throws IOException, SAXException, XMLParsingException {
        // config file -> DOM
        Reader reader = new InputStreamReader( new URL( configURL ).openStream() );
        Document doc = XMLTools.parse( reader );
        Element element = doc.getDocumentElement();
        reader.close();

        // extract configuration information from DOM
        catalogURL = new URL( XMLTools.getRequiredStringValue( "catalogURL", null, element ) );
    }

    @Override
    public void actionPerformed( FormEvent event ) {

        try {
            SecurityAccessManager manager = SecurityAccessManager.getInstance();
            User user = manager.getUserByName( toModel().get( ClientHelper.KEY_USERNAME ) );
            SecurityAccess token = manager.acquireAccess( user );
            boolean isAdmin = true;

            // perform access check
            try {
                ClientHelper.checkForAdminRole( token );
            } catch ( UnauthorizedException e ) {
                isAdmin = false;
            }

            // decode RPC-event
            if ( event instanceof RPCWebEvent ) {
                RPCWebEvent ev = (RPCWebEvent) event;
                RPCMethodCall rpcCall = ev.getRPCMethodCall();
                RPCParameter[] params = rpcCall.getParameters();

                if ( params == null || params.length != 1 ) {
                    throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_WRONG_PARAM_NUM" ) );

                }
                if ( !( params[0].getValue() instanceof String ) ) {
                    throw new RPCException( Messages.getMessage( "IGEO_STD_SEC_MISSING_STRING" ) );
                }

            } else {
                throw new Exception( Messages.getMessage( "IGEO_STD_SEC_ERROR_RPC_NOT_VALID" ) );
            }

            Set allServices = getBriefDescriptions( catalogURL );
            String[] serviceDetails = new String[14];
            for ( int i = 0; i < serviceDetails.length; i++ ) {
                serviceDetails[i] = "";
            }

            // display the first service
            String serviceId = null;
            Iterator it = allServices.iterator();
            if ( it.hasNext() ) {
                serviceId = ( (String[]) it.next() )[0];
            }
            if ( serviceId != null ) {
                serviceDetails = getFullDescription( catalogURL, serviceId );
            }

            getRequest().setAttribute( "ALL_SERVICES", allServices );
            getRequest().setAttribute( "SERVICE_DETAILS", serviceDetails );
            getRequest().setAttribute( "IS_ADMIN", new Boolean( isAdmin ) );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            getRequest().setAttribute( "SOURCE", this.getClass().getName() );
            getRequest().setAttribute( "MESSAGE",
                                       Messages.getMessage( "IGEO_STD_SEC_FAIL_INIT_SERVICE_EDITOR", e.getMessage() ) );

            setNextPage( "admin/admin_error.jsp" );
        }

    }

    /**
     * Retrieves "brief descriptions" of all available services from the WCAS.
     *
     * @param catalogURL
     * @return elements are arrays of Strings (title, id, type)
     * @throws IOException
     * @throws SAXException
     * @throws XMLParsingException
     */
    protected Set<String[]> getBriefDescriptions( URL catalogURL )
                            throws IOException, SAXException, XMLParsingException {

        TreeSet<String[]> services = new TreeSet<String[]>( new Comparator() {
            public int compare( Object o1, Object o2 ) {
                if ( o1 instanceof String[] && o2 instanceof String[] ) {
                    String id1 = ( (String[]) o1 )[0];
                    String id2 = ( (String[]) o2 )[1];
                    return id1.compareTo( id2 );
                }
                throw new ClassCastException( "Incompatible object types!" );
            }
        } );

        // build WCAS-request
        String briefRequest = "<?xml version=\"1.0\" encoding=\"" + CharsetUtils.getSystemCharset()
                              + "\"?><GetRecord xmlns:ogc=\"http://www.opengis.net/ogc\""
                              + " xmlns:gml=\"http://www.opengis.net/gml\" maxRecords=\"10\""
                              + " outputFormat=\"XML\" outputRecType=\"ISO19119\" queryScope=\"0\""
                              + " startPosition=\"-1\"><Query typeName=\"Service\">"
                              + "<PropertySet setName=\"Full\"/></Query></GetRecord>";

        // open connection and send request
        NetWorker netWorker = new NetWorker( catalogURL, briefRequest );

        // server response -> DOM
        InputStreamReader reader = new InputStreamReader( netWorker.getInputStream(), CharsetUtils.getSystemCharset() );

        Document doc = XMLTools.parse( reader );
        reader.close();

        // extract service information from DOM
        Element searchResultElement = XMLTools.getRequiredChildElement( "searchResult", null, doc.getDocumentElement() );
        List serviceElements = XMLTools.getRequiredNodes( searchResultElement, "ISO19119", nsContext );

        for ( int i = 0; i < serviceElements.size(); i++ ) {
            Element serviceElement = (Element) serviceElements.get( i );
            String serviceId = XMLTools.getRequiredStringValue( "fileIdentifier", null, serviceElement );
            String serviceType = XMLTools.getRequiredStringValue( "serviceType", null, serviceElement );
            Element citationElement = XMLTools.getRequiredChildElement( "citation", null, serviceElement );
            String serviceName = XMLTools.getRequiredStringValue( "title", null, citationElement );
            String[] service = new String[] { serviceId, serviceType, serviceName };
            services.add( service );
        }

        return services;
    }

    /**
     * Retrieves the full description for the specified service from the WCAS.
     *
     * @param catalogURL
     * @param serviceId
     * @return String array (14 values) describing the service
     * @throws Exception
     */
    protected String[] getFullDescription( URL catalogURL, String serviceId )
                            throws Exception {

        String[] details = new String[14];

        // build WCAS-request
        String fullRequest = "<?xml version=\"1.0\" encoding=\"" + CharsetUtils.getSystemCharset() + "\"?>"
                             + "<GetRecord xmlns:ogc=\"http://www.opengis.net/ogc\""
                             + " xmlns:gml=\"http://www.opengis.net/gml\" maxRecords=\"10\""
                             + " outputFormat=\"XML\" outputRecType=\"ISO19119\" queryScope=\"0\""
                             + " startPosition=\"-1\">" + "<Query typeName=\"Service\">"
                             + "<PropertySet setName=\"Full\"/>" + "<ogc:Filter>" + "<ogc:PropertyIsEqualTo>"
                             + "<ogc:PropertyName>ISO19119/fileIdentifier</ogc:PropertyName>"
                             + "<ogc:Literal><![CDATA[" + serviceId + "]]></ogc:Literal>" + "</ogc:PropertyIsEqualTo>"
                             + "</ogc:Filter>" + "</Query>" + "</GetRecord>";

        // open connection and send request
        NetWorker netWorker = new NetWorker( catalogURL, fullRequest );

        // server response -> DOM
        InputStreamReader reader = new InputStreamReader( netWorker.getInputStream(), CharsetUtils.getSystemCharset() );
        Document doc = XMLTools.parse( reader );
        reader.close();

        // extract service information from DOM
        Element searchResultElement = XMLTools.getRequiredChildElement( "searchResult", null, doc.getDocumentElement() );
        List serviceElements = XMLTools.getRequiredNodes( searchResultElement, "ISO19119", nsContext );
        if ( serviceElements.size() != 1 ) {
            throw new XMLParsingException( "Error in WCAS-response. Unexpected number (" + serviceElements.size()
                                           + ") of ISO19119 elements." );
        }
        Element serviceElement = (Element) serviceElements.get( 0 );

        details[0] = serviceId;
        details[1] = XMLTools.getRequiredStringValue( "serviceType", null, serviceElement );
        details[2] = XMLTools.getRequiredStringValue( "serviceTypeVersion", null, serviceElement );

        Element citationElement = XMLTools.getChildElement( "citation", null, serviceElement );
        if ( citationElement != null ) {
            details[3] = XMLTools.getStringValue( "title", null, citationElement, "" );
            details[4] = XMLTools.getStringValue( "abstract", null, serviceElement, "" );

            Element pointOfContactElement = XMLTools.getChildElement( "pointOfContact", null, serviceElement );
            if ( pointOfContactElement != null ) {
                details[5] = XMLTools.getStringValue( "individualName", null, pointOfContactElement, "" );
                details[6] = XMLTools.getStringValue( "positionName", null, pointOfContactElement, "" );
                details[7] = XMLTools.getStringValue( "organizationName", null, pointOfContactElement, "" );
                details[8] = XMLTools.getStringValue( "onlineResource", null, pointOfContactElement, "" );

                Element contactInfoElement = XMLTools.getChildElement( "contactInfo", null, pointOfContactElement );
                if ( contactInfoElement != null ) {
                    Element addressElement = XMLTools.getChildElement( "address", null, contactInfoElement );
                    if ( addressElement != null ) {
                        details[9] = XMLTools.getStringValue( "deliveryPoint", null, addressElement, "" );
                        details[10] = XMLTools.getStringValue( "city", null, addressElement, "" );
                        details[11] = XMLTools.getStringValue( "postalCode", null, addressElement, "" );
                        details[12] = XMLTools.getStringValue( "country", null, addressElement, "" );
                        details[13] = XMLTools.getStringValue( "electronicMailAddress", null, addressElement, "" );
                    }
                }
            }
        }

        return details;
    }
}

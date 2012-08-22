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

package org.deegree.ogcwebservices.wass.common;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.ogcwebservices.wass.was.capabilities.WASCapabilities;
import org.deegree.ogcwebservices.wass.was.capabilities.WASCapabilitiesDocument;
import org.deegree.ogcwebservices.wass.wss.capabilities.WSSCapabilities;
import org.deegree.ogcwebservices.wass.wss.capabilities.WSSCapabilitiesDocument;
import org.deegree.owscommon.OWSDomainType;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * This is the XMLFactory for both the WAS and the WSS. Please note that it only works with the 1.0
 * version of the OWS base classes, mostly recognizable by the appendix _1_0.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public class XMLFactory extends org.deegree.owscommon.XMLFactory {

    private static final URI WAS = CommonNamespaces.GDINRW_WAS;

    private static final String PWAS = CommonNamespaces.GDINRWWAS_PREFIX + ":";

    private static final URI WSS = CommonNamespaces.GDINRW_WSS;

    private static final String PWSS = CommonNamespaces.GDINRWWSS_PREFIX + ":";

    private static final URI OWS = CommonNamespaces.OWSNS;

    private static final String POWS = CommonNamespaces.OWS_PREFIX + ":";

    private static final URI AUTHN = CommonNamespaces.GDINRW_AUTH;

    private static final String PAUTHN = CommonNamespaces.GDINRW_AUTH_PREFIX + ":";

    private static final ILogger LOG = LoggerFactory.getLogger( XMLFactory.class );

    /**
     *
     * Exports the given WAS capabilities as XML document.
     *
     * @param capabilities
     *            the data to export
     * @return the XMLFragment
     */
    public static WASCapabilitiesDocument export( WASCapabilities capabilities ) {

        WASCapabilitiesDocument doc = new WASCapabilitiesDocument();

        try {
            doc.createEmptyDocument();
            Element root = doc.getRootElement();

            appendBaseCapabilities( capabilities, root );
            appendExtendedCapabilities( capabilities, root, WAS, PWAS );

        } catch ( SAXException e ) {
            LOG.logError( Messages.getMessage( "WASS_ERROR_XML_TEMPLATE_NOT_PARSED",
                                           new Object[] { "WAS",
                                                         WASCapabilitiesDocument.XML_TEMPLATE } ),
                          e );
        } catch ( IOException e ) {
            LOG.logError( Messages.getMessage( "WASS_ERROR_XML_TEMPLATE_NOT_READ",
                                           new Object[] { "WAS",
                                                         WASCapabilitiesDocument.XML_TEMPLATE } ),
                          e );
        }

        return doc;
    }

    /**
     *
     * Exports the given WSS capabilities as XML document. Also appends the WSS specific element
     * SecuredServiceType.
     *
     * @param capabilities
     *            the data to export
     * @return the XMLFragment
     */
    public static WSSCapabilitiesDocument export( WSSCapabilities capabilities ) {

        WSSCapabilitiesDocument doc = new WSSCapabilitiesDocument();

        try {
            doc.createEmptyDocument();
            Element root = doc.getRootElement();

            appendBaseCapabilities( capabilities, root );
            Element cap = appendExtendedCapabilities( capabilities, root, WSS, PWSS );

            XMLTools.appendElement( cap, WSS, PWSS + "SecuredServiceType",
                                    capabilities.getSecuredServiceType() );
        } catch ( SAXException e ) {
            LOG.logError( Messages.getMessage( "WASS_ERROR_XML_TEMPLATE_NOT_PARSED",
                                           new Object[] { "WSS",
                                                         WSSCapabilitiesDocument.XML_TEMPLATE } ),
                          e );
        } catch ( IOException e ) {
            LOG.logError( Messages.getMessage( "WASS_ERROR_XML_TEMPLATE_NOT_READ",
                                           new Object[] { "WSS",
                                                         WSSCapabilitiesDocument.XML_TEMPLATE } ),
                          e );
        }

        return doc;
    }

    /**
     *
     * Appends the WAS/WSS specific capabilities elements, but only those WAS and WSS have in
     * common.
     *
     * @param capabilities
     *            the data to append
     * @param root
     *            the WAS/WSS_Capabilities element
     * @param namespace
     *            the namespace URI, WAS or WSS
     * @param prefix
     * @return the appended Capability element
     */
    private static Element appendExtendedCapabilities( OWSCapabilitiesBaseType_1_0 capabilities,
                                                      Element root, URI namespace, String prefix ) {

        Element cap = XMLTools.appendElement( root, namespace, prefix + "Capability" );
        Element sams = XMLTools.appendElement( cap, namespace,
                                               prefix + "SupportedAuthenticationMethodList" );

        ArrayList<SupportedAuthenticationMethod> methods = capabilities.getAuthenticationMethods();
        for ( SupportedAuthenticationMethod method : methods )
            appendSupportedAuthenticationMethod( sams, method );


        return cap;
    }

    /**
     *
     * Appends a SupportedAuthenticationMethod element to the given element.
     *
     * @param sams
     *            the SupportedAuthenticationMethodList element
     * @param method
     *            the data to append
     */
    private static void appendSupportedAuthenticationMethod( Element sams,
                                                            SupportedAuthenticationMethod method ) {

        Element sam = XMLTools.appendElement( sams, AUTHN, PAUTHN + "SupportedAuthenticationMethod" );
        Element authMethod = XMLTools.appendElement( sam, AUTHN, PAUTHN + "AuthenticationMethod" );
        authMethod.setAttribute( "id", method.getMethod().toString() );

        String unknownMD = method.getMetadata();
        if ( unknownMD != null )
            XMLTools.appendElement( sam, AUTHN, PAUTHN + "UnknownMethodMetadata", unknownMD );

        WASAuthenticationMethodMD metadata = method.getWasMetadata();
        if ( metadata != null )
            appendMetadata( sam, metadata );

    }

    /**
     *
     * Appends the OWS base capabilities data to the given element.
     *
     * @param capabilities
     *            the data to append
     * @param root
     *            the element to append to, WAS_- or WSS_Capabilities
     */
    private static void appendBaseCapabilities( OWSCapabilitiesBaseType_1_0 capabilities,
                                               Element root ) {

        root.setAttribute( "version", capabilities.getVersion() );
        root.setAttribute( "updateSequence", capabilities.getUpdateSequence() );

        // may have to be changed/overwritten (?)
        ServiceIdentification serviceIdentification = capabilities.getServiceIdentification();
        if ( serviceIdentification != null )
            appendServiceIdentification( root, serviceIdentification );

        ServiceProvider serviceProvider = capabilities.getServiceProvider();
        if ( serviceProvider != null )
            appendServiceProvider( root, serviceProvider );

        OperationsMetadata_1_0 metadata = capabilities.getOperationsMetadata();
        if ( metadata != null )
            appendOperationsMetadata_1_0( root, metadata );

    }

    /**
     *
     * Appends an OperationsMetadata element to the given element.
     *
     * @param elem
     *            the element to append to
     * @param operationsMetadata
     *            the data to append
     */
    private static void appendOperationsMetadata_1_0( Element elem,
                                                     OperationsMetadata_1_0 operationsMetadata ) {

        Element root = XMLTools.appendElement( elem, OWS, POWS + "OperationsMetadata" );

        Operation[] operations = operationsMetadata.getAllOperations();
        for ( Operation operation : operations )
            appendOperation_1_0( root, (Operation_1_0) operation );

        OWSDomainType[] parameters = operationsMetadata.getParameter();
        if ( parameters != null )
            for ( OWSDomainType parameter : parameters )
                appendParameter( root, parameter, POWS + "Parameter" );

        OWSDomainType[] constraints = operationsMetadata.getConstraints();
        if ( constraints != null )
            for ( OWSDomainType constraint : constraints )
                appendParameter( root, constraint, POWS + "Constraint" );

        String extCap = operationsMetadata.getExtendedCapabilities();
        if ( extCap != null )
            XMLTools.appendElement( root, OWS, POWS + "ExtendedCapabilities", extCap );

    }

    /**
     *
     * Appends an Operation element to the argument element.
     *
     * @param root
     *            the element to append to
     * @param operation
     *            the data to append
     */
    private static void appendOperation_1_0( Element root, Operation_1_0 operation ) {

        Element op = XMLTools.appendElement( root, OWS, POWS + "Operation" );

        op.setAttribute( "Name", operation.getName() );

        DCPType[] dcps = operation.getDCPs();
        for ( DCPType dcp : dcps )
            appendDCP( op, dcp );

        OWSDomainType[] parameters = operation.getParameters();
        for ( OWSDomainType parameter : parameters )
            appendParameter( op, parameter, "Parameter" );

        OWSDomainType[] constraints = operation.getConstraints();
        for ( OWSDomainType constraint : constraints )
            appendParameter( op, constraint, "Constraint" );

        Object[] metadatas = operation.getMetadata();
        for ( Object metadata : metadatas )
            appendMetadata( op, metadata );

    }

}

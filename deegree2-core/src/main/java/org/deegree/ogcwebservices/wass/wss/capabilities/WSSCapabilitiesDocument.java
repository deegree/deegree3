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

package org.deegree.ogcwebservices.wass.wss.capabilities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.ogcwebservices.wass.common.OWSCapabilitiesBaseDocument_1_0;
import org.deegree.ogcwebservices.wass.common.OperationsMetadata_1_0;
import org.deegree.ogcwebservices.wass.common.SupportedAuthenticationMethod;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * A <code>WSSCapabilitiesDocument</code> class can parse xml-based requests. The gdi-nrw access
 * control specification 1.0 defines xml-elements of type owscommon 1.0 hence this class base class
 * is OWSCapabilitiesBaseDocument_1_0. For creating an empty response document a
 * XML-Response-Template is located under WSSCapablitiesTemplate.xml .
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class WSSCapabilitiesDocument extends OWSCapabilitiesBaseDocument_1_0 {

    private static final long serialVersionUID = 4456377564478064784L;

    /**
     * The logger enhances the quality and simplicity of Debugging within the deegree2 framework
     */
    private static final ILogger LOG = LoggerFactory.getLogger( WSSCapabilitiesDocument.class );

    /**
     * This is the XML template used for the GetCapabilities response document.
     */
    public static final String XML_TEMPLATE = "WSSCapabilitiesTemplate.xml";

    private static String PRE = CommonNamespaces.GDINRWWSS_PREFIX + ":";

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.ogcwebservices.getcapabilities.OGCCapabilitiesDocument#parseCapabilities()
     */
    @Override
    public OGCCapabilities parseCapabilities()
                            throws InvalidCapabilitiesException {

        WSSCapabilities wssCapabilities = null;
        try {

            ServiceIdentification sf = parseServiceIdentification();
            ServiceProvider sp = parseServiceProvider();
            OperationsMetadata_1_0 om = parseOperationsMetadata();
            String version = parseVersion();
            String updateSequence = parseUpdateSequence();

            ArrayList<SupportedAuthenticationMethod> am = parseSupportedAuthenticationMethods( CommonNamespaces.GDINRWWSS_PREFIX );

            String securedServiceType = parseSecuredServiceType();

            wssCapabilities = new WSSCapabilities( version, updateSequence, sf, sp, om, securedServiceType, am );

        } catch ( XMLParsingException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InvalidCapabilitiesException( Messages.getMessage( "WASS_ERROR_CAPABILITIES_NOT_PARSED", "WSS" ) );
        } catch ( URISyntaxException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InvalidCapabilitiesException( Messages.getMessage( "WASS_ERROR_URI_NOT_READ",
                                                                         new Object[] { "WSS", "(unknown)" } ) );
        } catch ( MalformedURLException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InvalidCapabilitiesException( Messages.getMessage( "WASS_ERROR_URL_NOT_READ",
                                                                         new Object[] { "WSS", "(unknown)" } ) );
        }

        return wssCapabilities;
    }

    /**
     * @return the secured service as String
     * @throws XMLParsingException
     */
    public String parseSecuredServiceType()
                            throws XMLParsingException {
        Element capability = (Element) XMLTools.getRequiredNode( getRootElement(), PRE + "Capability", nsContext );
        return XMLTools.getRequiredNodeAsString( capability, PRE + "SecuredServiceType", nsContext );
    }

    /**
     * @throws SAXException
     * @throws IOException
     *
     */
    public void createEmptyDocument()
                            throws IOException, SAXException {
        super.createEmptyDocument( XML_TEMPLATE );
    }

}

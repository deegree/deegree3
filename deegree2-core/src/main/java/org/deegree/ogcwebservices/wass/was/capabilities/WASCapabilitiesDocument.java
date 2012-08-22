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

package org.deegree.ogcwebservices.wass.was.capabilities;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.ogcwebservices.wass.common.OWSCapabilitiesBaseDocument_1_0;
import org.deegree.ogcwebservices.wass.common.OperationsMetadata_1_0;
import org.deegree.ogcwebservices.wass.common.SupportedAuthenticationMethod;
import org.xml.sax.SAXException;

/**
 * Parser for the WAS capabilities according to GDI NRW spec V1.0.
 *
 * Namespace: http://www.gdi-nrw.org/was
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class WASCapabilitiesDocument extends OWSCapabilitiesBaseDocument_1_0 {

    private static final long serialVersionUID = -6646616562364235109L;

    private static final ILogger LOG = LoggerFactory.getLogger( WASCapabilitiesDocument.class );

    /**
     * This is the XML template used for the GetCapabilities response document.
     */
    public static final String XML_TEMPLATE = "WASCapabilitiesTemplate.xml";

    /**
     * Creates an empty document from default template.
     *
     * @throws IOException
     * @throws SAXException
     */
    public void createEmptyDocument()
                            throws IOException, SAXException {
        super.createEmptyDocument( XML_TEMPLATE );
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.ogcwebservices.getcapabilities.OGCCapabilitiesDocument#parseCapabilities()
     */
    @Override
    public OGCCapabilities parseCapabilities()
                            throws InvalidCapabilitiesException {


        WASCapabilities wasCapabilites = null;
        try {

            ServiceIdentification sf = parseServiceIdentification();
            ServiceProvider sp = parseServiceProvider();
            OperationsMetadata_1_0 om = parseOperationsMetadata();
            String version = parseVersion();
            String updateSequence = parseUpdateSequence();

            ArrayList<SupportedAuthenticationMethod> am = parseSupportedAuthenticationMethods( CommonNamespaces.GDINRWWAS_PREFIX );
            wasCapabilites = new WASCapabilities( version, updateSequence, sf, sp, om, am );

        } catch ( XMLParsingException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InvalidCapabilitiesException(
                                                    Messages.getMessage(
                                                                     "WASS_ERROR_CAPABILITIES_NOT_PARSED",
                                                                     "WAS" ) );
        } catch ( URISyntaxException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InvalidCapabilitiesException(
                                                    Messages.getMessage(
                                                                     "WASS_ERROR_URI_NOT_READ",
                                                                     new Object[] { "WAS",
                                                                                   "(unknown)" } ) );
        } catch ( MalformedURLException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InvalidCapabilitiesException(
                                                    Messages.getMessage(
                                                                     "WASS_ERROR_URL_NOT_READ",
                                                                     new Object[] { "WAS",
                                                                                   "(unknown)" } ) );
        }


        return wasCapabilites;
    }

}

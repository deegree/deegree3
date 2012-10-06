//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.metadata.provider;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;
import static org.deegree.services.metadata.MetadataUtils.convertFromJAXB;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.dom.DOMSource;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.services.jaxb.metadata.DeegreeServicesMetadataType;
import org.deegree.services.jaxb.metadata.ExtendedCapabilitiesType;
import org.slf4j.Logger;

/**
 * {@link OWSMetadataProviderProvider} implementation that retrieves the provided metadata from XML files.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */
public class DefaultOWSMetadataProviderProvider implements OWSMetadataProviderProvider {

    private static final Logger LOG = getLogger( DefaultOWSMetadataProviderProvider.class );

    private static final URL CONFIG_SCHEMA = DefaultOWSMetadataProviderProvider.class.getResource( "/META-INF/schemas/services/metadata/3.2.0/metadata.xsd" );

    private DeegreeWorkspace workspace;

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/services/metadata";
    }

    @Override
    public URL getConfigSchema() {
        return CONFIG_SCHEMA;
    }

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @Override
    public DefaultOWSMetadataProvider create( URL configUrl )
                            throws ResourceInitException {
        try {
            JAXBElement<DeegreeServicesMetadataType> md;
            md = (JAXBElement<DeegreeServicesMetadataType>) unmarshall( "org.deegree.services.jaxb.metadata",
                                                                        CONFIG_SCHEMA, configUrl, workspace );
            Pair<ServiceIdentification, ServiceProvider> smd = convertFromJAXB( md.getValue() );
            Map<String, List<OMElement>> extendedCapabilities = new HashMap<String, List<OMElement>>();
            if ( md.getValue().getExtendedCapabilities() != null ) {
                for ( ExtendedCapabilitiesType ex : md.getValue().getExtendedCapabilities() ) {
                    String version = ex.getProtocolVersions();
                    if ( version == null ) {
                        version = "default";
                    }
                    List<OMElement> list = extendedCapabilities.get( version );
                    if ( list == null ) {
                        list = new ArrayList<OMElement>();
                        extendedCapabilities.put( version, list );
                    }
                    DOMSource domSource = new DOMSource( ex.getAny() );
                    XMLStreamReader xmlStream;
                    try {
                        xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( domSource );
                    } catch ( Throwable t ) {
                        throw new ResourceInitException( "Error extracting extended capabilities: " + t.getMessage(), t );
                    }
                    list.add( new XMLAdapter( xmlStream ).getRootElement() );
                }
            }
            return new DefaultOWSMetadataProvider( smd.first, smd.second, extendedCapabilities, null );
        } catch ( Throwable e ) {
            LOG.trace( "Stack trace:", e );
            throw new ResourceInitException( "Unable to read service metadata config.", e );
        }
    }

    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] {};
    }
}

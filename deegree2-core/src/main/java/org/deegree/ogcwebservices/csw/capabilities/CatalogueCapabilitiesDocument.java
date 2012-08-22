// $HeadURL:
// /cvsroot/deegree/src/org/deegree/ogcwebservices/csw/capabilities/CatalogCapabilitiesDocument.java,v
// 1.22 2004/08/05 15:40:08 ap Exp $
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
package org.deegree.ogcwebservices.csw.capabilities;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.xlink.SimpleLink;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.model.filterencoding.capabilities.FilterCapabilities;
import org.deegree.model.filterencoding.capabilities.FilterCapabilities100Fragment;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.owscommon.OWSCommonCapabilitiesDocument;
import org.deegree.owscommon.OWSDomainType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Represents an XML capabilities document for an OGC CSW 2.0 compliant service.
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 *
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 *
 */
public class CatalogueCapabilitiesDocument extends OWSCommonCapabilitiesDocument {

    /**
     *
     */
    private static final long serialVersionUID = 7913971828771510110L;

    private static final ILogger LOG = LoggerFactory.getLogger( CatalogueCapabilitiesDocument.class );

    /**
     *
     */
    public final static String FILTER_CAPABILITIES_NAME = "FilterCapabilities";

    /**
     *
     */
    public final static String EBRIM_CAPABILITIES_NAME = "EBRIMCapabilities";

    protected static final URI OGCNS = CommonNamespaces.OGCNS;

    private static final String XML_TEMPLATE = "CatalogueCapabilitiesTemplate.xml";

    /**
     * Creates a skeleton capabilities document that contains the mandatory elements only.
     *
     * @throws IOException
     * @throws SAXException
     */
    public void createEmptyDocument()
                            throws IOException, SAXException {
        URL url = CatalogueCapabilitiesDocument.class.getResource( XML_TEMPLATE );
        if ( url == null ) {
            throw new IOException( "The resource '" + XML_TEMPLATE + " could not be found." );
        }
        load( url );
    }

    /**
     * Creates a class representation of the document.
     *
     * @return class representation of the configuration document
     */
    @Override
    public OGCCapabilities parseCapabilities()
                            throws InvalidCapabilitiesException {
        try {
            FilterCapabilities filterCapabilities = null;
            Element filterCapabilitiesElement = (Element) XMLTools.getNode( getRootElement(),
                                                                            "ogc:Filter_Capabilities", nsContext );
            if ( filterCapabilitiesElement != null ) {
                filterCapabilities = new FilterCapabilities100Fragment( filterCapabilitiesElement, getSystemId() ).parseFilterCapabilities();
            }
            return new CatalogueCapabilities( parseVersion(), parseUpdateSequence(), getServiceIdentification(),
                                              getServiceProvider(), getOperationsMetadata(), null, filterCapabilities );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidCapabilitiesException( e.getMessage() );
        }
    }

    /**
     * Creates a class representation of the <code>OperationsMetadata</code>- section.
     *
     * @return the new instance
     * @throws XMLParsingException
     */
    public OperationsMetadata getOperationsMetadata()
                            throws XMLParsingException {

        Node root = this.getRootElement();

        Node omNode = XMLTools.getRequiredChildElement( "OperationsMetadata", OWSNS, root );
        ElementList elementList = XMLTools.getChildElements( "Operation", OWSNS, omNode );

        ElementList parameterElements = XMLTools.getChildElements( "Parameter", OWSNS, omNode );
        OWSDomainType[] parameters = new OWSDomainType[parameterElements.getLength()];

        for ( int i = 0; i < parameters.length; i++ ) {
            parameters[i] = getOWSDomainType( null, parameterElements.item( i ) );
        }

        OWSDomainType[] constraints = getContraints( (Element) omNode );

        // build HashMap of 'Operation'-elements for easier access
        HashMap<String, Node> operations = new HashMap<String, Node>();
        for ( int i = 0; i < elementList.getLength(); i++ ) {
            operations.put( XMLTools.getRequiredAttrValue( "name", null, elementList.item( i ) ), elementList.item( i ) );
        }

        // 'GetCapabilities'-operation
        Operation getCapabilites = getOperation( OperationsMetadata.GET_CAPABILITIES_NAME, true, operations );
        // 'DescribeRecord'-operation
        Operation describeRecord = getOperation( CatalogueOperationsMetadata.DESCRIBE_RECORD_NAME, true, operations );
        // 'GetDomain'-operation
        Operation getDomain = getOperation( CatalogueOperationsMetadata.GET_DOMAIN_NAME, false, operations );
        // 'GetRecords'-operation
        Operation getRecords = getOperation( CatalogueOperationsMetadata.GET_RECORDS_NAME, true, operations );
        // 'GetRecordById'-operation
        Operation getRecordById = getOperation( CatalogueOperationsMetadata.GET_RECORD_BY_ID_NAME, true, operations );
        // 'Transaction'-operation
        Operation transaction = getOperation( CatalogueOperationsMetadata.TRANSACTION_NAME, false, operations );
        // 'Harvest'-operation
        Operation harvest = getOperation( CatalogueOperationsMetadata.HARVEST_NAME, false, operations );

        return new CatalogueOperationsMetadata( getCapabilites, describeRecord, getDomain, getRecords, getRecordById,
                                                transaction, harvest, parameters, constraints );
    }

    /**
     * @return a {@link EBRIMCapabilities} element (specified in the ogc-ebrim extension)
     * @throws XMLParsingException
     *             if a required node isn't found
     */
    protected EBRIMCapabilities parseEBRIMCapabilities()
                            throws XMLParsingException {
        Element rootElement = getRootElement();

        String prefix = rootElement.getOwnerDocument().lookupPrefix( CommonNamespaces.WRS_EBRIMNS.toString() );

        if ( prefix == null || "".equals( prefix.trim() ) ) {
            return null;
        }

        // SeviceFeatures
        Element serviceFeature = (Element) XMLTools.getRequiredNode( rootElement, "wrs:ServiceFeatures", nsContext );
        List<Node> nl = XMLTools.getNodes( serviceFeature, "wrs:feature", nsContext );
        Map<URI, CSWFeature> features = new HashMap<URI, CSWFeature>();
        for ( Object n : nl ) {
            Node featureElement = (Node) n;
            URI featureName = XMLTools.getRequiredNodeAsURI( featureElement, "@name", nsContext );
            LOG.logDebug( "found featurename: " + featureName );
            if ( features.containsKey( featureName ) ) {
                throw new XMLParsingException(
                                               Messages.getMessage( "WRS_UNAMBIGUOUS_FEAT_PROP", featureName.toString() ) );
            }
            CSWFeature feature = new CSWFeature( parsePropties( featureElement ) );
            features.put( featureName, feature );
        }

        Element serviceProps = (Element) XMLTools.getRequiredNode( rootElement, "wrs:ServiceProperties", nsContext );
        Map<URI, List<String>> serviceProperties = parsePropties( serviceProps );
        SimpleLink wsdl_SimpleLink = parseSimpleLink( (Element) XMLTools.getRequiredNode( rootElement,
                                                                                          "wrs:WSDL-services",
                                                                                          nsContext ) );

        return new EBRIMCapabilities( features, serviceProperties, wsdl_SimpleLink );
    }

    private Map<URI, List<String>> parsePropties( Node xmlNode )
                            throws XMLParsingException {
        List<Node> pnl = XMLTools.getNodes( xmlNode, "wrs:property", nsContext );
        Map<URI, List<String>> properties = new HashMap<URI, List<String>>();

        for ( Object pn : pnl ) {
            Node property = (Node) pn;
            URI propName = XMLTools.getRequiredNodeAsURI( property, "@name", nsContext );
            List<String> propValues = Arrays.asList( XMLTools.getRequiredNodesAsStrings( property, "wrs:value",
                                                                                         nsContext ) );
            properties.put( propName, propValues );
        }
        return properties;
    }
}

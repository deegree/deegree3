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

package org.deegree.ogcwebservices.csw.configuration;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.deegree.datatypes.xlink.SimpleLink;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.InvalidConfigurationException;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.io.JDBCConnection;
import org.deegree.model.filterencoding.capabilities.FilterCapabilities;
import org.deegree.model.filterencoding.capabilities.FilterCapabilities100Fragment;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.csw.capabilities.CatalogueCapabilitiesDocument;
import org.deegree.owscommon.OWSDomainType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Represents an XML configuration document for a deegree CSW 2.0 instance, i.e. it consists of all
 * sections common to an OGC CSW 2.0 capabilities document plus a deegree specific section named
 * <code>deegreeParams</code>.
 *
 * @author <a href="mailto:tfr@users.sourceforge.net">Torsten Friebe </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class CatalogueConfigurationDocument extends CatalogueCapabilitiesDocument {

    private static ILogger LOG = LoggerFactory.getLogger( CatalogueConfigurationDocument.class );

    private static final long serialVersionUID = -2923926335089417513L;

    private static final NamespaceContext nsc = CommonNamespaces.getNamespaceContext();

    protected static final URI DEEGREECSW = CommonNamespaces.DEEGREECSW;

    private static final String XML_TEMPLATE = "CatalogueConfigurationTemplate.xml";

    /**
     * Creates a skeleton configuration document that contains the mandatory elements only.
     *
     * @throws IOException
     * @throws SAXException
     */
    @Override
    public void createEmptyDocument()
                            throws IOException, SAXException {
        URL url = CatalogueConfigurationDocument.class.getResource( XML_TEMPLATE );
        if ( url == null ) {
            throw new IOException( "The resource '" + XML_TEMPLATE + " could not be found." );
        }
        load( url );
    }

    /**
     * Creates a class representation of the whole document.
     *
     * @return class representation of the configuration document
     * @throws InvalidConfigurationException
     */
    public CatalogueConfiguration getConfiguration()
                            throws InvalidConfigurationException {
        CatalogueConfiguration configuration = null;
        try {
            FilterCapabilities filterCapabilities = null;
            Element filterCapabilitiesElement = (Element) XMLTools.getNode( getRootElement(),
                                                                            "ogc:Filter_Capabilities", nsContext );
            if ( filterCapabilitiesElement != null ) {
                filterCapabilities = new FilterCapabilities100Fragment( filterCapabilitiesElement, getSystemId() ).parseFilterCapabilities();
            }
            configuration = new CatalogueConfiguration( parseVersion(), parseUpdateSequence(),
                                                        getServiceIdentification(), getServiceProvider(),
                                                        getOperationsMetadata(), null, filterCapabilities,
                                                        getDeegreeParams(), getSystemId(), parseEBRIMCapabilities() );
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidConfigurationException( "Class representation of the catalog configuration "
                                                     + "document could not be generated: " + e.getMessage(), e );
        }
        return configuration;
    }

    /**
     * Creates a class representation of the <code>deegreeParams</code>- section.
     *
     * @return the DeegreeParams of the catalogue
     * @throws InvalidConfigurationException
     */
    public CatalogueDeegreeParams getDeegreeParams()
                            throws InvalidConfigurationException {

        CatalogueDeegreeParams deegreeParams = null;

        try {
            Node root = this.getRootElement();
            Element element = XMLTools.getRequiredChildElement( "deegreeParams", DEEGREECSW, root );

            // 'deegreecsw:DefaultOnlineResource'-element (mandatory)
            OnlineResource defaultOnlineResource = parseOnLineResource( XMLTools.getRequiredChildElement(
                                                                                                          "DefaultOnlineResource",
                                                                                                          DEEGREECSW,
                                                                                                          element ) );

            // 'deegreecsw:CacheSize'-element (optional, default: 100)
            int cacheSize = XMLTools.getNodeAsInt( element, "./deegreecsw:CacheSize", nsContext, 100 );

            // 'deegreecsw:RequestTimeLimit'-element (optional, default: 2)
            int requestTimeLimit = XMLTools.getNodeAsInt( element, "./deegreecsw:RequestTimeLimit", nsContext, 2 );

            // 'deegreecsw:Encoding'-element (optional, default: UTF-8)
            String characterSet = XMLTools.getStringValue( "Encoding", DEEGREECSW, element, "UTF-8" );

            // default output schema used by a catalogue
            String defaultOutputSchema = XMLTools.getStringValue( "DefaultOutputSchema", DEEGREECSW, element, "OGCCORE" );

            // 'deegreecsw:WFSResource'-element (mandatory)
            SimpleLink wfsResource = parseSimpleLink( XMLTools.getRequiredChildElement( "WFSResource", DEEGREECSW,
                                                                                        element ) );

            // 'deegreecsw:CatalogAddresses'-element (optional)
            Element catalogAddressesElement = XMLTools.getChildElement( "CatalogAddresses", DEEGREECSW, element );
            OnlineResource[] catalogAddresses = new OnlineResource[0];
            if ( catalogAddressesElement != null ) {
                // 'deegreecsw:CatalogAddresses'-element (optional)
                ElementList el = XMLTools.getChildElements( "CatalogAddress", DEEGREECSW, catalogAddressesElement );
                catalogAddresses = new OnlineResource[el.getLength()];
                for ( int i = 0; i < catalogAddresses.length; i++ ) {
                    catalogAddresses[i] = parseOnLineResource( el.item( i ) );
                }
            }

            OnlineResource transInXslt = null;
            Element elem = (Element) XMLTools.getNode( element, "deegreecsw:TransactionInputXSLT", nsc );
            if ( elem != null ) {
                transInXslt = parseOnLineResource( elem );
            }
            OnlineResource transOutXslt = null;
            elem = (Element) XMLTools.getNode( element, "deegreecsw:TransactionOutputXSLT", nsc );
            if ( elem != null ) {
                transOutXslt = parseOnLineResource( elem );
            }
            if ( ( transInXslt != null && transOutXslt == null ) || ( transInXslt == null && transOutXslt != null ) ) {
                throw new InvalidConfigurationException(
                                                         "if CSW-deegreeParam "
                                                                                 + "'TransactionInputXSLT' is defined 'TransactionOutputXSLT' must "
                                                                                 + "be defined too and vice versa!" );
            }

            // 'deegreecsw:HarvestRepository'-element (optional)
            Element harvestRepositoryElement = XMLTools.getChildElement( "HarvestRepository", DEEGREECSW, element );
            JDBCConnection harvestRepository = null;
            if ( harvestRepositoryElement != null ) {
                // 'deegreecsw:Connection'-element (optional)
                Element connectionElement = XMLTools.getChildElement( "Connection", DEEGREECSW,
                                                                      harvestRepositoryElement );
                if ( connectionElement != null ) {
                    harvestRepository = new JDBCConnection( XMLTools.getRequiredStringValue( "Driver", DEEGREECSW,
                                                                                             connectionElement ),
                                                            XMLTools.getRequiredStringValue( "Logon", DEEGREECSW,
                                                                                             connectionElement ),
                                                            XMLTools.getRequiredStringValue( "User", DEEGREECSW,
                                                                                             connectionElement ),
                                                            XMLTools.getRequiredStringValue( "Password", DEEGREECSW,
                                                                                             connectionElement ), null,
                                                            null, null );
                }
            }
            deegreeParams = new CatalogueDeegreeParams( defaultOnlineResource, cacheSize, requestTimeLimit,
                                                        characterSet, wfsResource, catalogAddresses, harvestRepository,
                                                        defaultOutputSchema, transInXslt, transOutXslt );
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
            throw new InvalidConfigurationException( "Error parsing the deegreeParams "
                                                     + "section of the CSW configuration: \n" + e.getMessage()
                                                     + StringTools.stackTraceToString( e ) );
        }
        return deegreeParams;
    }

    /**
     * Overwritten to cope with additional deegree CSW specific attributes (used in the
     * "outputSchema" parameter element).
     *
     * @param operation
     * @param parameterElement
     * @return OWSDomainType
     * @throws XMLParsingException
     *
     * @see "org.deegree.owscommon.OWSCommonCapabilitiesDocument#getParameter(org.w3c.dom.Element)"
     */
    @Override
    protected OWSDomainType getOWSDomainType( String operation, Element parameterElement )
                            throws XMLParsingException {
        // "name"-attribute
        String parameterName = XMLTools.getRequiredAttrValue( "name", null, parameterElement );

        if ( "GetRecords".equals( operation ) && "outputSchema".equals( parameterName ) ) {
            // "ows:Value"-elements
            NodeList valueNodes = parameterElement.getElementsByTagNameNS( OWSNS.toString(), "Value" );
            CatalogueOutputSchemaValue[] values = new CatalogueOutputSchemaValue[valueNodes.getLength()];
            for ( int i = 0; i < valueNodes.getLength(); i++ ) {
                String value = XMLTools.getStringValue( valueNodes.item( i ) );
                String input = XMLTools.getRequiredAttrValue( "input", DEEGREECSW, valueNodes.item( i ) );
                String output = XMLTools.getRequiredAttrValue( "output", DEEGREECSW, valueNodes.item( i ) );
                if ( value == null || value.equals( "" ) ) {
                    throw new XMLParsingException( "Missing or empty node '" + value + "'." );
                }
                values[i] = new CatalogueOutputSchemaValue( value, input, output );
            }
            return new CatalogueOutputSchemaParameter( parameterName, values, null );
        } else if ( "GetRecords".equals( operation ) && "typeName".equals( parameterName ) ) {
            NodeList valueNodes = parameterElement.getElementsByTagNameNS( OWSNS.toString(), "Value" );
            CatalogueTypeNameSchemaValue[] values = new CatalogueTypeNameSchemaValue[valueNodes.getLength()];
            for ( int i = 0; i < valueNodes.getLength(); i++ ) {
                String value = XMLTools.getStringValue( valueNodes.item( i ) );
                String schema = XMLTools.getRequiredAttrValue( "schema", DEEGREECSW, valueNodes.item( i ) );
                values[i] = new CatalogueTypeNameSchemaValue( value, schema );
            }
            return new CatalogueTypeNameSchemaParameter( parameterName, values, null );
        } else {
            return super.getOWSDomainType( operation, parameterElement );
        }
    }
}

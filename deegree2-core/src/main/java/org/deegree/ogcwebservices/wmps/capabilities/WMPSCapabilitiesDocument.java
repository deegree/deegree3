// $HeadURL$
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
package org.deegree.ogcwebservices.wmps.capabilities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.deegree.datatypes.Code;
import org.deegree.datatypes.xlink.SimpleLink;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.metadata.iso19115.Address;
import org.deegree.model.metadata.iso19115.ContactInfo;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.model.metadata.iso19115.Phone;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Position;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.HTTP;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.MetadataURL;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.Protocol;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.ogcwebservices.wms.capabilities.Attribution;
import org.deegree.ogcwebservices.wms.capabilities.AuthorityURL;
import org.deegree.ogcwebservices.wms.capabilities.DataURL;
import org.deegree.ogcwebservices.wms.capabilities.Dimension;
import org.deegree.ogcwebservices.wms.capabilities.Extent;
import org.deegree.ogcwebservices.wms.capabilities.FeatureListURL;
import org.deegree.ogcwebservices.wms.capabilities.Identifier;
import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.LayerBoundingBox;
import org.deegree.ogcwebservices.wms.capabilities.LegendURL;
import org.deegree.ogcwebservices.wms.capabilities.LogoURL;
import org.deegree.ogcwebservices.wms.capabilities.ScaleHint;
import org.deegree.ogcwebservices.wms.capabilities.Style;
import org.deegree.ogcwebservices.wms.capabilities.StyleSheetURL;
import org.deegree.ogcwebservices.wms.capabilities.StyleURL;
import org.deegree.ogcwebservices.wms.capabilities.UserDefinedSymbolization;
import org.deegree.owscommon.OWSCommonCapabilitiesDocument;
import org.deegree.owscommon.OWSDomainType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Represents an XML capabilities document for an OGC WFS 1.1.0 compliant web service.
 *
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh </a>
 *
 * @version 2.0
 */
public class WMPSCapabilitiesDocument extends OWSCommonCapabilitiesDocument {

    private static final long serialVersionUID = -9098679671644329509L;

    private static final ILogger LOG = LoggerFactory.getLogger( WMPSCapabilitiesDocument.class );

    protected static final URI WMPSNS = CommonNamespaces.WMPSNS;

    private static final String XML_TEMPLATE = "WMPSCapabilitiesTemplate.xml";

    /**
     * Creates a skeleton capabilities document that contains the mandatory elements only.
     *
     * @throws IOException
     * @throws SAXException
     */
    public void createEmptyDocument()
                            throws IOException, SAXException {
        URL url = WMPSCapabilitiesDocument.class.getResource( XML_TEMPLATE );
        if ( url == null ) {
            throw new IOException( "The resource '" + XML_TEMPLATE + " could not be found." );
        }
        load( url );
    }

    /**
     * Creates a class representation of the document.
     *
     * @return OGCCapabilities class representation of the configuration document
     * @throws InvalidCapabilitiesException
     */
    @Override
    public OGCCapabilities parseCapabilities()
                            throws InvalidCapabilitiesException {


        LOG.logDebug( "Parsing Capabilties Request." );
        ServiceIdentification serviceIdentification = null;
        ServiceProvider serviceProvider = null;
        UserDefinedSymbolization uds = null;
        OperationsMetadata metadata = null;
        Layer layer = null;
        String version = parseVersion();
        try {
            serviceIdentification = parseServiceIdentification();
            serviceProvider = parseServiceProvider();
            LOG.logDebug( "Retrieved serviceIdentification and serviceProvider information "
                          + "from the request." );
            metadata = parseOperationsMetadata();
            LOG.logDebug( "Retrieved metadData information from the request." );
            uds = parseUserDefinedSymbolization();
            Element layerElem = (Element) XMLTools.getRequiredNode( getRootElement(),
                                                                    "./Capability/Layer", nsContext );
            LOG.logDebug( "Layer Element retrieved." );
            layer = parseLayers( layerElem, null );
        } catch ( XMLParsingException e ) {
            String msg = "Error parsing the capabilities request to retrieve 'serviceIdentification',"
                         + " 'serviceProvider', 'metaData' and 'layer' " + e.getMessage();
            throw new InvalidCapabilitiesException( msg );
        } catch (UnknownCRSException e) {
            throw new InvalidCapabilitiesException( getClass().getName(),  e.getMessage() );
        }
        WMPSCapabilities wmpsCapabilities = new WMPSCapabilities( version, serviceIdentification,
                                                                  serviceProvider, uds, metadata,
                                                                  layer );

        return wmpsCapabilities;
    }

    /**
     * Parse the UserDefinedSymbolization
     *
     * @return UserDefinedSymbolization
     * @throws XMLParsingException
     */
    protected UserDefinedSymbolization parseUserDefinedSymbolization()
                            throws XMLParsingException {


        String xPath = "./Capability/UserDefinedSymbolization/@SupportSLD";
        boolean supportSLD = XMLTools.getNodeAsBoolean( getRootElement(), xPath, nsContext, false );

        xPath = "./Capability/UserDefinedSymbolization/@UserLayer";
        boolean userLayer = XMLTools.getNodeAsBoolean( getRootElement(), xPath, nsContext, false );

        xPath = "./Capability/UserDefinedSymbolization/@UserStyle";
        boolean userStyle = XMLTools.getNodeAsBoolean( getRootElement(), xPath, nsContext, false );

        xPath = "./Capability/UserDefinedSymbolization/@RemoteWFS";
        boolean remoteWFS = XMLTools.getNodeAsBoolean( getRootElement(), xPath, nsContext, false );

        UserDefinedSymbolization uds = new UserDefinedSymbolization( supportSLD, userLayer,
                                                                     remoteWFS, userStyle );


        return uds;
    }

    /**
     * returns the services indentification read from the WMPS capabilities service section
     *
     * @return ServiceIdentification
     * @throws XMLParsingException
     */
    protected ServiceIdentification parseServiceIdentification()
                            throws XMLParsingException {


        LOG.logDebug( "Parsing service identification parameter." );
        String name = XMLTools.getNodeAsString( getRootElement(), "./Service/Name", nsContext, null );
        String title = XMLTools.getNodeAsString( getRootElement(), "./Service/Title", nsContext,
                                                 name );
        String serviceAbstract = XMLTools.getNodeAsString( getRootElement(), "./Service/Abstract",
                                                           nsContext, null );

        String[] kw = XMLTools.getNodesAsStrings( getRootElement(),
                                                  "./Service/KeywordList/Keyword", nsContext );

        Keywords[] keywords = new Keywords[] { new Keywords( kw ) };

        String fees = XMLTools.getNodeAsString( getRootElement(), "./Service/Fees", nsContext, null );

        String[] accessConstraints = XMLTools.getNodesAsStrings( getRootElement(),
                                                                 "./Service/AccessConstraints",
                                                                 nsContext );

        String[] acceptedVersion = new String[] { "1.0.0" };
        Code code = new Code( "WMPS" );
        ServiceIdentification serviceIdentification = new ServiceIdentification( code,
                                                                                 acceptedVersion,
                                                                                 title,
                                                                                 serviceAbstract,
                                                                                 keywords, fees,
                                                                                 accessConstraints );

        return serviceIdentification;
    }

    /**
     * returns WMPS contact informaion encapsulated within a <code>ServiceProvider</code> object
     *
     * @return ServiceProvider
     * @throws XMLParsingException
     */
    protected ServiceProvider parseServiceProvider()
                            throws XMLParsingException {


        SimpleLink sLink = retrieveOnlineResourceSimpleLink();

        LOG.logDebug( "Parsing service provider parameter." );
        /**
         * according to WMPS (draft) specification this element is mandatory but there are several
         * services online which does not contain this element in its capabilities
         */
        Node contactInfo = XMLTools.getRequiredNode( getRootElement(),
                                                     "./Service/ContactInformation", nsContext );

        String person = XMLTools.getRequiredNodeAsString( contactInfo,
                                                          "./ContactPersonPrimary/ContactPerson",
                                                          nsContext );
        String orga = XMLTools.getRequiredNodeAsString(
                                                        contactInfo,
                                                        "./ContactPersonPrimary/ContactOrganization",
                                                        nsContext );
        String position = XMLTools.getRequiredNodeAsString( contactInfo, "./ContactPosition",
                                                            nsContext );
        ContactInfo contact = parseContactInfo();

        ServiceProvider sp = new ServiceProvider( orga, sLink, person, position, contact, null );



        return sp;
    }

    /**
     * Returns the SimpleLink from the Online Resource node in the Service element.
     *
     * @return SimpleLink
     * @throws XMLParsingException
     */
    private SimpleLink retrieveOnlineResourceSimpleLink()
                            throws XMLParsingException {



        String simpleLink = XMLTools.getNodeAsString( getRootElement(),
                                                      "./Service/OnlineResource/@xlink:href",
                                                      nsContext, null );
        SimpleLink sLink = null;
        if ( simpleLink != null ) {
            try {
                sLink = new SimpleLink( new URI( simpleLink ) );
            } catch ( URISyntaxException e ) {
                throw new XMLParsingException( "Error parsing service online resource", e );
            }
        } else {
            try {
                /**
                 * use default if no online resource is contained in the capabilities (see comment
                 * above)
                 */
                sLink = new SimpleLink( new URI( "http://www.opengeospatial.org/" ) );
            } catch ( URISyntaxException neverHappens ) {
                neverHappens.printStackTrace();
            }
        }

        return sLink;
    }

    /**
     * Parse Contact Information
     *
     * @return ContactInfo
     * @throws XMLParsingException
     */
    protected ContactInfo parseContactInfo()
                            throws XMLParsingException {


        LOG.logDebug( "Parsing contact information parameter." );
        Node contactInfo = XMLTools.getNode( getRootElement(), "./Service/ContactInformation",
                                             nsContext );
        String[] addr = XMLTools.getNodesAsStrings( contactInfo, "./ContactAddress/Address",
                                                    nsContext );
        // String addrType = XMLTools.getNodeAsString( contactInfo, "./ContactAddress/AddressType",
        // nsContext, null );
        String city = XMLTools.getNodeAsString( contactInfo, "./ContactAddress/City", nsContext,
                                                null );
        String state = XMLTools.getNodeAsString( contactInfo, "./ContactAddress/StateOrProvince",
                                                 nsContext, null );
        String pc = XMLTools.getNodeAsString( contactInfo, "./ContactAddress/PostCode", nsContext,
                                              null );
        String country = XMLTools.getNodeAsString( contactInfo, "./ContactAddress/Country",
                                                   nsContext, null );
        String[] mail = XMLTools.getNodesAsStrings( contactInfo, "./ContactElectronicMailAddress",
                                                    nsContext );
        Address address = new Address( state, city, country, addr, mail, pc );

        String[] phone = XMLTools.getNodesAsStrings( contactInfo, "./ContactVoiceTelephone",
                                                     nsContext );
        String[] fax = XMLTools.getNodesAsStrings( contactInfo, "./ContactFacsimileTelephone",
                                                   nsContext );

        Phone ph = new Phone( fax, phone );

        ContactInfo cont = new ContactInfo( address, null, null, null, ph );


        return cont;
    }

    /**
     * returns the services capabilitiy read from the WMPS capabilities file
     *
     * @return OperationsMetadata
     * @throws XMLParsingException
     */
    protected OperationsMetadata parseOperationsMetadata()
                            throws XMLParsingException {


        LOG.logDebug( "Parsing operations metdata parameter." );
        Node opNode = XMLTools.getNode( getRootElement(), "./Capability/Request/GetCapabilities",
                                        nsContext );

        Operation getCapabilities = parseOperation( opNode );
        LOG.logDebug( "Operation getCapabilities created for the GetCapabilities node." );

        opNode = XMLTools.getRequiredNode( getRootElement(), "./Capability/Request/PrintMap",
                                           nsContext );

        Operation printMap = parseOperation( opNode );

        LOG.logDebug( "Operation printMap created for the PrintMap node." );

        WMPSOperationsMetadata metadata = new WMPSOperationsMetadata( getCapabilities, printMap );


        return metadata;
    }

    /**
     * Creates an <tt>Operation</tt>-instance according to the contents of the DOM-subtree
     * starting at the given <tt>Node</tt>.
     * <p>
     * Notice: operation to be parsed must be operations in sense of WMPS (draft). The method will
     * return an OWSCommon Operation which encapsulates parsed WMPS operation
     * <p>
     *
     * @param node
     *            the <tt>Element</tt> that describes an <tt>Operation</tt>
     * @throws XMLParsingException
     *             if a syntactic or semantic error in the DOM-subtree is encountered
     * @return the constructed <tt>Operation</tt>-instance
     */
    protected Operation parseOperation( Node node )
                            throws XMLParsingException {


        LOG.logDebug( "Parsing Operation." );
        // use node name as name of the Operation to be defined
        String name = node.getNodeName();
        String[] tmp = XMLTools.getRequiredNodesAsStrings( node, "./Format", nsContext );
        OWSDomainType owsDomainType = new OWSDomainType( "Format", tmp, null );
        OWSDomainType[] odt = new OWSDomainType[] { owsDomainType };

        List<Node> nl = XMLTools.getRequiredNodes( node, "./DCPType", nsContext );
        DCPType[] dcpTypes = new DCPType[nl.size()];
        for ( int i = 0; i < dcpTypes.length; i++ ) {
            dcpTypes[i] = getDCP( (Element) nl.get( i ) );
        }
        LOG.logDebug( "Creating operation with name, dcpTypes and OWSDomainType." );

        return new Operation( name, dcpTypes, odt );
    }

    /**
     * Creates a <code>DCPType</code> object from the passed <code>DCP</code> element.
     * <p>
     * NOTE: Currently the <code>OnlineResources</code> included in the <code>DCPType</code> are
     * just stored as simple <code>URLs</code> (not as <code>OnLineResource</code> instances)!
     * <p>
     * NOTE: In an <code>OGCStandardCapabilitiesDocument</code> the <code>XLinks</code> (the
     * <code>URLs</code>) are stored in separate elements (<code>OnlineResource</code>), in
     * an <code>OGCCommonCapabilitiesDocument</code> they are the
     * <code>Get<code>/<code>Post</code> elements themselves.
     *
     * @param element
     *
     * @return created <code>DCPType</code>
     * @throws XMLParsingException
     *
     * @see org.deegree.ogcwebservices.getcapabilities.OGCStandardCapabilities
     */
    @Override
    protected DCPType getDCP( Element element )
                            throws XMLParsingException {

        DCPType dcpType = null;
        try {
            Element elem = (Element) XMLTools.getRequiredNode( element, "HTTP", nsContext );
            List<Node> nl = XMLTools.getNodes( elem, "Get", nsContext );

            URL[] get = new URL[nl.size()];
            for ( int i = 0; i < get.length; i++ ) {
                String s = XMLTools.getNodeAsString( (Node) nl.get( i ), "./@xlink:href",
                                                     nsContext, null );
                if ( s == null ) {
                    s = XMLTools.getRequiredNodeAsString( (Node) nl.get( i ),
                                                          "./OnlineResource/@xlink:href", nsContext );
                }
                get[i] = new URL( s );
            }
            nl = XMLTools.getNodes( elem, "Post", nsContext );

            URL[] post = new URL[nl.size()];
            for ( int i = 0; i < post.length; i++ ) {
                String s = XMLTools.getNodeAsString( (Node) nl.get( i ), "./@xlink:href",
                                                     nsContext, null );
                if ( s == null ) {
                    s = XMLTools.getRequiredNodeAsString( (Node) nl.get( i ),
                                                          "./OnlineResource/@xlink:href", nsContext );
                }
                post[i] = new URL( s );
            }
            Protocol protocol = new HTTP( get, post );
            dcpType = new DCPType( protocol );
        } catch ( MalformedURLException e ) {
            throw new XMLParsingException( "Couldn't parse DCPType onlineresource URL about: "
                                           + StringTools.stackTraceToString( e ) );
        }

        return dcpType;
    }

    /**
     * returns the layers offered by the WMPS
     *
     * @param layerElem
     * @param parent
     * @return Layer
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    protected Layer parseLayers( Element layerElem, Layer parent )
                            throws XMLParsingException, UnknownCRSException {


        boolean queryable = XMLTools.getNodeAsBoolean( layerElem, "./@queryable", nsContext, false );

        int cascaded = XMLTools.getNodeAsInt( layerElem, "./@cascaded", nsContext, 0 );
        boolean opaque = XMLTools.getNodeAsBoolean( layerElem, "./@opaque", nsContext, false );
        boolean noSubsets = XMLTools.getNodeAsBoolean( layerElem, "./@noSubsets", nsContext, false );
        int fixedWidth = XMLTools.getNodeAsInt( layerElem, "./@fixedWidth", nsContext, 0 );
        int fixedHeight = XMLTools.getNodeAsInt( layerElem, "./@fixedHeight", nsContext, 0 );
        String name = XMLTools.getNodeAsString( layerElem, "./Name", nsContext, null );
        String title = XMLTools.getRequiredNodeAsString( layerElem, "./Title", nsContext );
        String layerAbstract = XMLTools.getNodeAsString( layerElem, "./Abstract", nsContext, null );
        String[] keywords = XMLTools.getNodesAsStrings( layerElem, "./KeywordList/Keyword",
                                                        nsContext );
        String[] srs = XMLTools.getNodesAsStrings( layerElem, "./SRS", nsContext );

        List<Node> nl = XMLTools.getNodes( layerElem, "./BoundingBox", nsContext );
        // TODO replace with Envelope
        LayerBoundingBox[] bboxes = null;
        if ( nl.size() == 0 && parent != null ) {
            // inherit BoundingBoxes from parent layer
            bboxes = parent.getBoundingBoxes();
        } else {
            bboxes = parseLayerBoundingBoxes( nl );
        }

        Element llBox = (Element) XMLTools.getNode( layerElem, "./LatLonBoundingBox", nsContext );
        Envelope llBoundingBox = null;

        if ( llBox == null && parent != null ) {
            // inherit LatLonBoundingBox parent layer
            llBoundingBox = parent.getLatLonBoundingBox();
        } else if ( llBox != null ) {
            llBoundingBox = parseLatLonBoundingBox( llBox );
        } else {
            /** Default crs = EPSG:4326 */
            CoordinateSystem crs = CRSFactory.create( "EPSG:4326" );
            llBoundingBox = GeometryFactory.createEnvelope( -180, -90, 180, 90, crs );
        }

        Dimension[] dimensions = parseDimensions( layerElem );

        Extent[] extents = parseExtents( layerElem );

        Attribution attribution = parseAttribution( layerElem );

        AuthorityURL[] authorityURLs = parseAuthorityURLs( layerElem );

        MetadataURL[] metadataURLs = parseMetadataURLs( layerElem );

        DataURL[] dataURLs = parseDataURL( layerElem );

        Identifier[] identifiers = parseIdentifiers( layerElem );

        FeatureListURL[] featureListURLs = parseFeatureListURL( layerElem );

        Style[] styles = parseStyles( layerElem );

        ScaleHint scaleHint = parseScaleHint( layerElem );

        Layer layer = new Layer( queryable, cascaded, opaque, noSubsets, fixedWidth, fixedHeight,
                                 name, title, layerAbstract, llBoundingBox, attribution, scaleHint,
                                 keywords, srs, bboxes, dimensions, extents, authorityURLs,
                                 identifiers, metadataURLs, dataURLs, featureListURLs, styles,
                                 null, null, parent );

        // get Child layers
        nl = XMLTools.getNodes( layerElem, "./Layer", nsContext );
        Layer[] layers = new Layer[nl.size()];
        for ( int i = 0; i < layers.length; i++ ) {
            layers[i] = parseLayers( (Element) nl.get( i ), layer );
        }

        // set child layers
        layer.setLayer( layers );


        return layer;
    }

    /**
     * Parse Dimensions
     *
     * @param layerElem
     * @return Dimension[]
     * @throws XMLParsingException
     */
    protected Dimension[] parseDimensions( Element layerElem )
                            throws XMLParsingException {


        List<Node> nl = XMLTools.getNodes( layerElem, "./Dimension", nsContext );
        Dimension[] dimensions = new Dimension[nl.size()];
        for ( int i = 0; i < dimensions.length; i++ ) {
            String name = XMLTools.getNodeAsString( (Node) nl.get( i ), "./@name", nsContext, null );
            String units = XMLTools.getNodeAsString( (Node) nl.get( i ), "./@units", nsContext,
                                                     null );
            String unitSymbol = XMLTools.getNodeAsString( (Node) nl.get( i ), "./@unitSymbol",
                                                          nsContext, null );
            dimensions[i] = new Dimension( name, units, unitSymbol );
        }


        return dimensions;
    }

    /**
     * Parse Extents
     *
     * @param layerElem
     * @return Extent[]
     * @throws XMLParsingException
     */
    protected Extent[] parseExtents( Element layerElem )
                            throws XMLParsingException {


        List<Node> nl = XMLTools.getNodes( layerElem, "./Extent", nsContext );
        Extent[] extents = new Extent[nl.size()];
        for ( int i = 0; i < extents.length; i++ ) {
            String name = XMLTools.getNodeAsString( (Node) nl.get( i ), "./@name", nsContext, null );
            String deflt = XMLTools.getNodeAsString( (Node) nl.get( i ), "./@default", nsContext,
                                                     null );
            boolean nearestValue = XMLTools.getNodeAsBoolean( (Node) nl.get( i ),
                                                              "./@nearestValue", nsContext, false );
            String value = XMLTools.getNodeAsString( (Node) nl.get( i ), ".", nsContext, "" );
            extents[i] = new Extent( name, deflt, nearestValue, value );
        }


        return extents;
    }

    /**
     * Parse Attribution
     *
     * @param layerElem
     * @return Attribution
     * @throws XMLParsingException
     */
    protected Attribution parseAttribution( Element layerElem )
                            throws XMLParsingException {


        Attribution attribution = null;
        Node node = XMLTools.getNode( layerElem, "./Attribution", nsContext );
        if ( node != null ) {
            String title = XMLTools.getRequiredNodeAsString( layerElem, "./Attribution/Title",
                                                             nsContext );
            Node onlineR = XMLTools.getRequiredNode( node, "./OnlineResource", nsContext );
            OnlineResource onLineResource = parseOnLineResource( (Element) onlineR );
            node = XMLTools.getNode( node, "./LogoURL", nsContext );
            LogoURL logoURL = null;
            if ( node != null ) {
                int width = XMLTools.getRequiredNodeAsInt( node, "./@width", nsContext );
                int height = XMLTools.getRequiredNodeAsInt( node, "./@height", nsContext );
                String format = XMLTools.getRequiredNodeAsString( node, "./Format", nsContext );
                onlineR = XMLTools.getRequiredNode( node, "./OnlineResource", nsContext );
                OnlineResource logoOR = parseOnLineResource( (Element) onlineR );
                logoURL = new LogoURL( width, height, format, logoOR.getLinkage().getHref() );
            }
            attribution = new Attribution( title, onLineResource.getLinkage().getHref(), logoURL );
        }


        return attribution;
    }

    /**
     * Parse AuthorityURL
     *
     * @param layerElem
     * @return AuthorityURL[]
     * @throws XMLParsingException
     */
    protected AuthorityURL[] parseAuthorityURLs( Element layerElem )
                            throws XMLParsingException {


        List<Node> nl = XMLTools.getNodes( layerElem, "./AuthorityURL", nsContext );
        AuthorityURL[] authorityURLs = new AuthorityURL[nl.size()];
        for ( int i = 0; i < authorityURLs.length; i++ ) {
            String name = XMLTools.getRequiredNodeAsString( (Node) nl.get( i ), "./@name",
                                                            nsContext );
            Element tmp = (Element) XMLTools.getRequiredNode( (Node) nl.get( i ),
                                                              "./OnlineResource", nsContext );
            OnlineResource olr = parseOnLineResource( tmp );
            authorityURLs[i] = new AuthorityURL( name, olr.getLinkage().getHref() );
        }


        return authorityURLs;
    }

    /**
     * Parse MetadataURL
     *
     * @param layerElem
     * @return MetadataURL[]
     * @throws XMLParsingException
     */
    protected MetadataURL[] parseMetadataURLs( Element layerElem )
                            throws XMLParsingException {


        List<Node> nl = XMLTools.getNodes( layerElem, "./MetadataURL", nsContext );
        MetadataURL[] metadataURL = new MetadataURL[nl.size()];
        for ( int i = 0; i < metadataURL.length; i++ ) {
            String type = XMLTools.getRequiredNodeAsString( (Node) nl.get( i ), "./@type",
                                                            nsContext );
            String format = XMLTools.getRequiredNodeAsString( (Node) nl.get( i ), "./Format",
                                                              nsContext );
            Element tmp = (Element) XMLTools.getRequiredNode( (Node) nl.get( i ),
                                                              "./OnlineResource", nsContext );
            OnlineResource olr = parseOnLineResource( tmp );
            metadataURL[i] = new MetadataURL( type, format, olr.getLinkage().getHref() );

        }


        return metadataURL;
    }

    /**
     * Parse Data URL
     *
     * @param layerElem
     * @return DataURL[]
     * @throws XMLParsingException
     */
    protected DataURL[] parseDataURL( Element layerElem )
                            throws XMLParsingException {


        List<Node> nl = XMLTools.getNodes( layerElem, "./DataURL", nsContext );
        DataURL[] dataURL = new DataURL[nl.size()];
        for ( int i = 0; i < dataURL.length; i++ ) {

            String format = XMLTools.getRequiredNodeAsString( (Node) nl.get( i ), "./Format",
                                                              nsContext );
            Element tmp = (Element) XMLTools.getRequiredNode( (Node) nl.get( i ),
                                                              "./OnlineResource", nsContext );
            OnlineResource olr = parseOnLineResource( tmp );
            dataURL[i] = new DataURL( format, olr.getLinkage().getHref() );

        }


        return dataURL;
    }

    /**
     * Parse FeatureListURL
     *
     * @param layerElem
     * @return FeatureListURL[]
     * @throws XMLParsingException
     */
    protected FeatureListURL[] parseFeatureListURL( Element layerElem )
                            throws XMLParsingException {


        List<Node> nl = XMLTools.getNodes( layerElem, "./FeatureListURL", nsContext );
        FeatureListURL[] flURL = new FeatureListURL[nl.size()];
        for ( int i = 0; i < flURL.length; i++ ) {

            String format = XMLTools.getRequiredNodeAsString( (Node) nl.get( i ), "./Format",
                                                              nsContext );
            Element tmp = (Element) XMLTools.getRequiredNode( (Node) nl.get( i ),
                                                              "./OnlineResource", nsContext );
            OnlineResource olr = parseOnLineResource( tmp );
            flURL[i] = new FeatureListURL( format, olr.getLinkage().getHref() );

        }


        return flURL;
    }

    /**
     * Parse Styles
     *
     * @param layerElem
     * @return Style[]
     * @throws XMLParsingException
     */
    protected Style[] parseStyles( Element layerElem )
                            throws XMLParsingException {


        List<Node> nl = XMLTools.getNodes( layerElem, "./Style", nsContext );
        Style[] styles = new Style[nl.size()];
        for ( int i = 0; i < styles.length; i++ ) {
            String name = XMLTools.getRequiredNodeAsString( (Node) nl.get( i ), "./Name", nsContext );
            String title = XMLTools.getNodeAsString( (Node) nl.get( i ), "./Title", nsContext, null );
            String styleAbstract = XMLTools.getNodeAsString( (Node) nl.get( i ), "./Abstract",
                                                             nsContext, null );
            LegendURL[] legendURLs = parseLegendURL( (Node) nl.get( i ) );
            StyleURL styleURL = parseStyleURL( (Node) nl.get( i ) );
            StyleSheetURL styleSheetURL = parseStyleSheetURL( (Node) nl.get( i ) );

            styles[i] = new Style( name, title, styleAbstract, legendURLs, styleSheetURL, styleURL,
                                   null );
        }


        return styles;
    }

    /**
     * Parse Legend URL
     *
     * @param node
     * @return LegendURL[]
     * @throws XMLParsingException
     */
    protected LegendURL[] parseLegendURL( Node node )
                            throws XMLParsingException {


        List<Node> nl = XMLTools.getNodes( node, "./LegendURL", nsContext );
        LegendURL[] lURL = new LegendURL[nl.size()];
        for ( int i = 0; i < lURL.length; i++ ) {
            int width = XMLTools.getRequiredNodeAsInt( (Node) nl.get( i ), "./@width", nsContext );
            int height = XMLTools.getRequiredNodeAsInt( (Node) nl.get( i ), "./@height", nsContext );
            String format = XMLTools.getRequiredNodeAsString( (Node) nl.get( i ), "./Format",
                                                              nsContext );
            Element tmp = (Element) XMLTools.getRequiredNode( (Node) nl.get( i ),
                                                              "./OnlineResource", nsContext );
            OnlineResource olr = parseOnLineResource( tmp );
            lURL[i] = new LegendURL( width, height, format, olr.getLinkage().getHref() );

        }


        return lURL;
    }

    /**
     * Parse Style URL
     *
     * @param node
     * @return StyleURL
     * @throws XMLParsingException
     */
    protected StyleURL parseStyleURL( Node node )
                            throws XMLParsingException {


        StyleURL styleURL = null;
        Node styleNode = XMLTools.getNode( node, "./StyleURL", nsContext );

        if ( styleNode != null ) {
            String format = XMLTools.getRequiredNodeAsString( styleNode, "./Format", nsContext );
            Element tmp = (Element) XMLTools.getRequiredNode( styleNode, "./OnlineResource",
                                                              nsContext );
            OnlineResource olr = parseOnLineResource( tmp );
            styleURL = new StyleURL( format, olr.getLinkage().getHref() );

        }


        return styleURL;
    }

    /**
     * Parse Style Sheet URL
     *
     * @param node
     * @return StyleSheetURL
     * @throws XMLParsingException
     */
    protected StyleSheetURL parseStyleSheetURL( Node node )
                            throws XMLParsingException {


        StyleSheetURL styleSheetURL = null;
        Node styleNode = XMLTools.getNode( node, "./StyleSheetURL", nsContext );

        if ( styleNode != null ) {
            String format = XMLTools.getRequiredNodeAsString( styleNode, "./Format", nsContext );
            Element tmp = (Element) XMLTools.getRequiredNode( styleNode, "./OnlineResource",
                                                              nsContext );
            OnlineResource olr = parseOnLineResource( tmp );
            styleSheetURL = new StyleSheetURL( format, olr.getLinkage().getHref() );

        }


        return styleSheetURL;
    }

    /**
     * Parse Scale Hint
     *
     * @param layerElem
     * @return ScaleHint
     * @throws XMLParsingException
     */
    protected ScaleHint parseScaleHint( Element layerElem )
                            throws XMLParsingException {


        ScaleHint scaleHint = null;

        Node scNode = XMLTools.getNode( layerElem, "./ScaleHint", nsContext );
        if ( scNode != null ) {
            double mn = XMLTools.getNodeAsDouble( scNode, "./@min", nsContext, 0 );
            double mx = XMLTools.getNodeAsDouble( scNode, "./@max", nsContext, Double.MAX_VALUE );
            scaleHint = new ScaleHint( mn, mx );
        } else {
            // set default value to avoid NullPointerException
            // when accessing a layers scalehint
            scaleHint = new ScaleHint( 0, Double.MAX_VALUE );
        }


        return scaleHint;
    }

    /**
     * Parse Identifiers
     *
     * @param layerElem
     * @return Identifier[]
     * @throws XMLParsingException
     */
    protected Identifier[] parseIdentifiers( Element layerElem )
                            throws XMLParsingException {


        List<Node> nl = XMLTools.getNodes( layerElem, "./Identifier", nsContext );
        Identifier[] identifiers = new Identifier[nl.size()];
        for ( int i = 0; i < identifiers.length; i++ ) {
            String value = XMLTools.getStringValue( (Node) nl.get( i ) );
            String authority = XMLTools.getNodeAsString( layerElem, "./@authority", nsContext, null );
            identifiers[i] = new Identifier( value, authority );
        }


        return identifiers;
    }

    /**
     * Parse Layer Bounding Boxes
     *
     * @param nl
     * @return LayerBoundingBox[]
     * @throws XMLParsingException
     */
    protected LayerBoundingBox[] parseLayerBoundingBoxes( List<Node> nl )
                            throws XMLParsingException {

        LayerBoundingBox[] llBoxes = new LayerBoundingBox[nl.size()];
        for ( int i = 0; i < llBoxes.length; i++ ) {
            double minx = XMLTools.getRequiredNodeAsDouble( (Node) nl.get( i ), "./@minx",
                                                            nsContext );
            double maxx = XMLTools.getRequiredNodeAsDouble( (Node) nl.get( i ), "./@maxx",
                                                            nsContext );
            double miny = XMLTools.getRequiredNodeAsDouble( (Node) nl.get( i ), "./@miny",
                                                            nsContext );
            double maxy = XMLTools.getRequiredNodeAsDouble( (Node) nl.get( i ), "./@maxy",
                                                            nsContext );
            double resx = XMLTools.getNodeAsDouble( (Node) nl.get( i ), "./@resx", nsContext, -1 );
            double resy = XMLTools.getNodeAsDouble( (Node) nl.get( i ), "./@resx", nsContext, -1 );
            String srs = XMLTools.getRequiredNodeAsString( (Node) nl.get( i ), "./@SRS", nsContext );
            Position min = GeometryFactory.createPosition( minx, miny );
            Position max = GeometryFactory.createPosition( maxx, maxy );
            llBoxes[i] = new LayerBoundingBox( min, max, srs, resx, resy );
        }

        return llBoxes;
    }

    /**
     * Parse Lat Lon Bounding Box
     *
     * @param llBox
     * @return Envelope
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    protected Envelope parseLatLonBoundingBox( Element llBox )
                            throws XMLParsingException, UnknownCRSException {


        double minx = XMLTools.getRequiredNodeAsDouble( llBox, "./@minx", nsContext );
        double maxx = XMLTools.getRequiredNodeAsDouble( llBox, "./@maxx", nsContext );
        double miny = XMLTools.getRequiredNodeAsDouble( llBox, "./@miny", nsContext );
        double maxy = XMLTools.getRequiredNodeAsDouble( llBox, "./@maxy", nsContext );
        /** default crs = EPSG:4326 */
        CoordinateSystem crs = CRSFactory.create( "EPSG:4326" );
        Envelope env = GeometryFactory.createEnvelope( minx, miny, maxx, maxy, crs );


        return env;
    }

}

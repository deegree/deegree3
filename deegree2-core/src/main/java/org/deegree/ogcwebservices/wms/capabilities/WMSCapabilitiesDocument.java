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
package org.deegree.ogcwebservices.wms.capabilities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.deegree.datatypes.Code;
import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.values.TypedLiteral;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.metadata.iso19115.Address;
import org.deegree.model.metadata.iso19115.CitedResponsibleParty;
import org.deegree.model.metadata.iso19115.Constraints;
import org.deegree.model.metadata.iso19115.ContactInfo;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.metadata.iso19115.Linkage;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.model.metadata.iso19115.Phone;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Position;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.MetadataURL;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilitiesDocument;
import org.deegree.owscommon_new.DCP;
import org.deegree.owscommon_new.DomainType;
import org.deegree.owscommon_new.HTTP;
import org.deegree.owscommon_new.Operation;
import org.deegree.owscommon_new.OperationsMetadata;
import org.deegree.owscommon_new.Parameter;
import org.deegree.owscommon_new.ServiceIdentification;
import org.deegree.owscommon_new.ServiceProvider;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <code>WMSCapabilitiesDocument</code> is the parser class for WMS capabilities documents that uses the new OWS common
 * classes to encapsulate the data.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version 2.0, $Revision$, $Date$
 * 
 * @since 2.0
 */

public class WMSCapabilitiesDocument extends OGCCapabilitiesDocument {

    private static final long serialVersionUID = -4165017479515126226L;

    private static final String XML_TEMPLATE = "WMSCapabilitiesTemplate.xml";

    private static final ILogger LOG = LoggerFactory.getLogger( WMSCapabilitiesDocument.class );

    /**
     * Creates a skeleton capabilities document that contains the mandatory elements only.
     * 
     * @throws IOException
     * @throws SAXException
     */
    public void createEmptyDocument()
                            throws IOException, SAXException {

        URL url = WMSCapabilitiesDocument.class.getResource( XML_TEMPLATE );
        if ( url == null ) {
            throw new IOException( "The resource '" + XML_TEMPLATE + " could not be found." );
        }
        load( url );
    }

    protected List<String> parseExceptionFormats( Element elem )
                            throws XMLParsingException {
        String[] formats = XMLTools.getRequiredNodesAsStrings( elem, "Format", nsContext );
        return Arrays.asList( formats );
    }

    /**
     * Creates a class representation of the document.
     * 
     * @return class representation of the configuration document
     * @throws InvalidCapabilitiesException
     */
    @Override
    public OGCCapabilities parseCapabilities()
                            throws InvalidCapabilitiesException {
        ServiceIdentification serviceIdentification = null;
        ServiceProvider serviceProvider = null;
        OperationsMetadata metadata = null;
        Layer layer = null;
        UserDefinedSymbolization uds = null;
        String version = parseVersion();
        List<String> exceptions;

        String updateSeq = parseUpdateSequence();
        try {
            serviceIdentification = parseServiceIdentification();
            serviceProvider = parseServiceProvider();
            metadata = parseOperationsMetadata();

            Element exceptionElement = XMLTools.getRequiredElement( getRootElement(), "Capability/Exception", nsContext );
            exceptions = parseExceptionFormats( exceptionElement );

            uds = parseUserDefinedSymbolization();
            Element layerElem = XMLTools.getRequiredElement( getRootElement(), "./Capability/Layer", nsContext );
            layer = parseLayers( layerElem, null, null );
        } catch ( XMLParsingException e ) {
            e.printStackTrace();
            throw new InvalidCapabilitiesException( e.getMessage() + StringTools.stackTraceToString( e ) );
        } catch ( UnknownCRSException e ) {
            e.printStackTrace();
            throw new InvalidCapabilitiesException( e.getMessage() + StringTools.stackTraceToString( e ) );
        }

        WMSCapabilities wmsCapabilities = new WMSCapabilities( version, updateSeq, serviceIdentification,
                                                               serviceProvider, uds, metadata, layer, exceptions );

        return wmsCapabilities;
    }

    /**
     * @return the services indentification read from the WMS capabilities service section
     * 
     * @throws XMLParsingException
     */
    protected ServiceIdentification parseServiceIdentification()
                            throws XMLParsingException {
        String name = XMLTools.getNodeAsString( getRootElement(), "./Service/Name", nsContext, null );
        String title = XMLTools.getNodeAsString( getRootElement(), "./Service/Title", nsContext, name );
        String serviceAbstract = XMLTools.getNodeAsString( getRootElement(), "./Service/Abstract", nsContext, null );

        String[] kw = XMLTools.getNodesAsStrings( getRootElement(), "./Service/KeywordList/Keyword", nsContext );

        Keywords[] keywordArray = new Keywords[] { new Keywords( kw ) };
        List<Keywords> keywords = Arrays.asList( keywordArray );

        String fees = XMLTools.getNodeAsString( getRootElement(), "./Service/Fees", nsContext, null );

        List<Constraints> accessConstraints = new ArrayList<Constraints>();

        String[] constraints = XMLTools.getNodesAsStrings( getRootElement(), "./Service/AccessConstraints", nsContext );

        for ( String constraint : constraints ) {
            List<String> limits = new ArrayList<String>();
            limits.add( constraint );
            accessConstraints.add( new Constraints( fees, null, null, null, limits, null, null, null ) );
        }

        List<String> versions = new ArrayList<String>();
        versions.add( "1.0.0" );
        versions.add( "1.1.0" );
        versions.add( "1.1.1" );
        versions.add( "1.2.0" );
        versions.add( "1.3.0" );

        ServiceIdentification serviceIdentification = new ServiceIdentification(
                                                                                 new Code( "OGC:WMS" ),
                                                                                 versions,
                                                                                 title,
                                                                                 null,
                                                                                 new Date( System.currentTimeMillis() ),
                                                                                 title, serviceAbstract, keywords,
                                                                                 accessConstraints );

        return serviceIdentification;
    }

    /**
     * returns WMS contact informaion encapsulated within a <code>ServiceProvider</code> object
     * 
     * @return the service provider data
     * @throws XMLParsingException
     */
    protected ServiceProvider parseServiceProvider()
                            throws XMLParsingException {
        Node ci = XMLTools.getNode( getRootElement(), "./Service/ContactInformation", nsContext );
        // according to WMS 1.1.1 specification this element is mandatory
        // but there are several services online which does not contain
        // this element in its capabilities :-(
        String s = XMLTools.getNodeAsString( getRootElement(), "./Service/OnlineResource/@xlink:href", nsContext, null );

        OnlineResource providerSite = null;

        if ( s != null ) {
            try {
                providerSite = new OnlineResource( new Linkage( new URL( s ) ) );
            } catch ( MalformedURLException e ) {
                LOG.logError(
                              "could not parse service online resource; use default URL: http://www.opengeospatial.org instead",
                              e );
                try {
                    providerSite = new OnlineResource( new Linkage( new URL( "http://www.opengeospatial.org" ) ) );
                } catch ( MalformedURLException neverHappens ) {
                    // useless exception
                }
            }
        } else {
            // use default if no online resource is contained in the
            // capabilities (see comment above)
            try {
                providerSite = new OnlineResource( new Linkage( new URL( "http://www.opengeospatial.org/" ) ) );
            } catch ( MalformedURLException neverHappens ) {
                // useless exception
            }
        }

        String person = null;
        String orga = null;
        String position = null;
        if ( ci != null ) {
            person = XMLTools.getNodeAsString( ci, "./ContactPersonPrimary/ContactPerson", nsContext, null );
            orga = XMLTools.getNodeAsString( ci, "./ContactPersonPrimary/ContactOrganization", nsContext, null );
            position = XMLTools.getNodeAsString( ci, "./ContactPosition", nsContext, null );
        }
        ContactInfo contact = parseContactInfo();
        // ServiceProvider sp = new ServiceProvider( orga, sLink, person, position, contact, null );
        CitedResponsibleParty party = new CitedResponsibleParty(
                                                                 contact == null ? new ContactInfo[] {}
                                                                                : new ContactInfo[] { contact },
                                                                 person == null ? new String[] {}
                                                                               : new String[] { person },
                                                                 orga == null ? new String[] {} : new String[] { orga },
                                                                 position == null ? new String[] {}
                                                                                 : new String[] { position }, null );
        ServiceProvider sp = new ServiceProvider( person, providerSite, party );

        return sp;
    }

    /**
     * 
     * @return the contact information
     * @throws XMLParsingException
     */
    protected ContactInfo parseContactInfo()
                            throws XMLParsingException {
        Node ci = XMLTools.getNode( getRootElement(), "./Service/ContactInformation", nsContext );
        ContactInfo cont = null;
        if ( ci != null ) {
            String[] addr = XMLTools.getNodesAsStrings( ci, "./ContactAddress/Address", nsContext );
            // String addrType =
            // XMLTools.getNodeAsString( ci, "./ContactAddress/AddressType", nsContext, null );
            String city = XMLTools.getNodeAsString( ci, "./ContactAddress/City", nsContext, null );
            String state = XMLTools.getNodeAsString( ci, "./ContactAddress/StateOrProvince", nsContext, null );
            String pc = XMLTools.getNodeAsString( ci, "./ContactAddress/PostCode", nsContext, null );
            String country = XMLTools.getNodeAsString( ci, "./ContactAddress/Country", nsContext, null );
            String[] mail = XMLTools.getNodesAsStrings( ci, "./ContactElectronicMailAddress", nsContext );
            Address address = new Address( state, city, country, addr, mail, pc );

            String[] phone = XMLTools.getNodesAsStrings( ci, "./ContactVoiceTelephone", nsContext );
            String[] fax = XMLTools.getNodesAsStrings( ci, "./ContactFacsimileTelephone", nsContext );

            Phone ph = new Phone( fax, phone );

            cont = new ContactInfo( address, null, null, null, ph );
        }

        return cont;
    }

    /**
     * returns the services capabilitiy read from the WMS capabilities file
     * 
     * @return the operations metadata
     * @throws XMLParsingException
     */
    protected OperationsMetadata parseOperationsMetadata()
                            throws XMLParsingException {

        Node opNode = XMLTools.getNode( getRootElement(), "./Capability/Request/GetCapabilities", nsContext );

        if ( opNode == null ) {
            // may it is a WMS 1.0.0 capabilities document
            opNode = XMLTools.getRequiredNode( getRootElement(), "./Capability/Request/Capabilities", nsContext );
        }
        Operation getCapa = parseOperation( opNode );

        opNode = XMLTools.getNode( getRootElement(), "./Capability/Request/GetMap", nsContext );
        if ( opNode == null ) {
            // may it is a WMS 1.0.0 capabilities document
            opNode = XMLTools.getRequiredNode( getRootElement(), "./Capability/Request/Map", nsContext );
        }
        Operation getMap = parseOperation( opNode );

        opNode = XMLTools.getNode( getRootElement(), "./Capability/Request/GetFeatureInfo", nsContext );
        Operation getFI = null;
        if ( opNode != null ) {
            getFI = parseOperation( opNode );
        } else {
            // maybe its WMS 1.0.0
            opNode = XMLTools.getNode( getRootElement(), "./Capability/Request/FeatureInfo", nsContext );
            if ( opNode != null ) {
                getFI = parseOperation( opNode );
            }
        }

        opNode = XMLTools.getNode( getRootElement(), "./Capability/Request/GetLegendGraphic", nsContext );
        Operation getLG = null;
        if ( opNode != null ) {
            getLG = parseOperation( opNode );
        }

        opNode = XMLTools.getNode( getRootElement(), "./Capability/Request/DescribeLayer", nsContext );
        Operation descL = null;
        if ( opNode != null ) {
            descL = parseOperation( opNode );
        }

        opNode = XMLTools.getNode( getRootElement(), "./Capability/Request/GetStyles", nsContext );
        Operation getStyles = null;
        if ( opNode != null ) {
            getStyles = parseOperation( opNode );
        }

        opNode = XMLTools.getNode( getRootElement(), "./Capability/Request/PutStyles", nsContext );
        Operation putStyles = null;
        if ( opNode != null ) {
            putStyles = parseOperation( opNode );
        }

        List<Operation> operations = new ArrayList<Operation>();
        if ( getCapa != null )
            operations.add( getCapa );
        if ( getMap != null )
            operations.add( getMap );
        if ( getFI != null )
            operations.add( getFI );
        if ( getLG != null )
            operations.add( getLG );
        if ( descL != null )
            operations.add( descL );
        if ( getStyles != null )
            operations.add( getStyles );
        if ( putStyles != null )
            operations.add( putStyles );

        OperationsMetadata metadata = new OperationsMetadata( null, null, operations, null );

        return metadata;
    }

    /**
     * Creates an <tt>Operation</tt>-instance according to the contents of the DOM-subtree starting at the given
     * <tt>Node</tt>.
     * <p>
     * Notice: operation to be parsed must be operations in sense of WMS 1.0.0 - 1.3.0 and not as defined in OWSCommons.
     * But the method will return an OWSCommon Operation which encapsulates parsed WMS operation
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
        // use node name as name of the Operation to be defined
        String name = node.getNodeName();
        if ( name.equals( "Capabilities" ) ) {
            name = "GetCapabilities";
        } else if ( name.equals( "Map" ) ) {
            name = "GetMap";
        } else if ( name.equals( "FeatureInfo" ) ) {
            name = "GetFeatureInfo";
        }

        String[] tmp = XMLTools.getRequiredNodesAsStrings( node, "./Format", nsContext );
        List<TypedLiteral> values = new ArrayList<TypedLiteral>();

        URI stringURI = null;
        try {
            stringURI = new URI( null, "String", null );
        } catch ( URISyntaxException e ) {
            // cannot happen, why do I have to catch this?
        }

        for ( String str : tmp )
            values.add( new TypedLiteral( str, stringURI ) );

        DomainType owsDomainType = new DomainType( false, true, null, 0, new QualifiedName( "Format" ), values, null,
                                                   null, false, null, false, null, null, null, null );
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add( owsDomainType );

        List<Element> nl = XMLTools.getRequiredElements( node, "./DCPType", nsContext );
        List<DCP> dcps = new ArrayList<DCP>();

        for ( Element element : nl ) {
            dcps.add( parseDCP( element ) );
        }

        return new Operation( new QualifiedName( name ), dcps, parameters, null, null, null );
    }

    /**
     * Parses a DCPType element. Does not override the method defined in the base class any more.
     * 
     * @param element
     * @return created <code>DCPType</code>
     * @throws XMLParsingException
     * @see org.deegree.ogcwebservices.getcapabilities.OGCStandardCapabilities
     */
    protected DCP parseDCP( Element element )
                            throws XMLParsingException {

        List<HTTP.Type> types = new ArrayList<HTTP.Type>();
        List<OnlineResource> links = new ArrayList<OnlineResource>();

        Element elem = XMLTools.getRequiredElement( element, "HTTP", nsContext );
        String s = null;
        try {
            List<Node> nl = XMLTools.getNodes( elem, "Get", nsContext );

            for ( int i = 0; i < nl.size(); i++ ) {
                s = XMLTools.getNodeAsString( nl.get( i ), "./@xlink:href", nsContext, null );
                if ( s == null ) {
                    s = XMLTools.getRequiredNodeAsString( nl.get( i ), "./OnlineResource/@xlink:href", nsContext );
                }
                types.add( HTTP.Type.Get );
                links.add( new OnlineResource( new Linkage( new URL( s ) ) ) );
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new XMLParsingException( Messages.getMessage( "WMS_DCPGET", s ) );
        }
        try {
            List<Node> nl = XMLTools.getNodes( elem, "Post", nsContext );

            for ( int i = 0; i < nl.size(); i++ ) {
                s = XMLTools.getNodeAsString( nl.get( i ), "./@xlink:href", nsContext, null );
                if ( s == null ) {
                    s = XMLTools.getRequiredNodeAsString( nl.get( i ), "./OnlineResource/@xlink:href", nsContext );
                }
                types.add( HTTP.Type.Post );
                links.add( new OnlineResource( new Linkage( new URL( s ) ) ) );
            }

        } catch ( MalformedURLException e ) {
            throw new XMLParsingException( Messages.getMessage( "WMS_DCPPOST", s ) );
        }
        HTTP http = new HTTP( links, null, types );

        return http;
    }

    /**
     * 
     * @return the parsed data
     * @throws XMLParsingException
     */
    protected UserDefinedSymbolization parseUserDefinedSymbolization()
                            throws XMLParsingException {

        boolean supportSLD = XMLTools.getNodeAsBoolean( getRootElement(),
                                                        "./Capability/UserDefinedSymbolization/@SupportSLD", nsContext,
                                                        false );

        boolean userLayer = XMLTools.getNodeAsBoolean( getRootElement(),
                                                       "./Capability/UserDefinedSymbolization/@UserLayer", nsContext,
                                                       false );

        boolean userStyle = XMLTools.getNodeAsBoolean( getRootElement(),
                                                       "./Capability/UserDefinedSymbolization/@UserStyle", nsContext,
                                                       false );

        boolean remoteWFS = XMLTools.getNodeAsBoolean( getRootElement(),
                                                       "./Capability/UserDefinedSymbolization/@RemoteWFS", nsContext,
                                                       false );

        UserDefinedSymbolization uds = new UserDefinedSymbolization( supportSLD, userLayer, remoteWFS, userStyle );

        return uds;
    }

    /**
     * @param layerElem
     * @param parent
     * @param scaleHint
     * 
     * @return the layer created from the given element
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    protected Layer parseLayers( Element layerElem, Layer parent, ScaleHint scaleHint )
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
        String[] keywords = XMLTools.getNodesAsStrings( layerElem, "./KeywordList/Keyword", nsContext );
        String[] srs = XMLTools.getNodesAsStrings( layerElem, "./SRS", nsContext );

        List<Element> nl = XMLTools.getElements( layerElem, "./BoundingBox", nsContext );
        // TODO
        // substitue with Envelope
        LayerBoundingBox[] bboxes = null;
        if ( nl.size() == 0 && parent != null ) {
            // inherit BoundingBoxes from parent layer
            bboxes = parent.getBoundingBoxes();
        } else {
            bboxes = parseLayerBoundingBoxes( nl );
        }

        Element llBox = XMLTools.getElement( layerElem, "./LatLonBoundingBox", nsContext );
        Envelope llBoundingBox = null;

        if ( llBox == null && parent != null ) {
            // inherit LatLonBoundingBox parent layer
            llBoundingBox = parent.getLatLonBoundingBox();
        } else if ( llBox != null ) {
            llBoundingBox = parseLatLonBoundingBox( llBox );
        } else {
            llBoundingBox = GeometryFactory.createEnvelope( -180, -90, 180, 90, CRSFactory.create( "EPSG:4326" ) );
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

        scaleHint = parseScaleHint( layerElem, scaleHint );

        Layer layer = new Layer( queryable, cascaded, opaque, noSubsets, fixedWidth, fixedHeight, name, title,
                                 layerAbstract, llBoundingBox, attribution, scaleHint, keywords, srs, bboxes,
                                 dimensions, extents, authorityURLs, identifiers, metadataURLs, dataURLs,
                                 featureListURLs, styles, null, null, parent );

        // get Child layers
        nl = XMLTools.getElements( layerElem, "./Layer", nsContext );
        Layer[] layers = new Layer[nl.size()];
        for ( int i = 0; i < layers.length; i++ ) {
            layers[i] = parseLayers( nl.get( i ), layer, scaleHint );
        }

        // set child layers
        layer.setLayer( layers );

        return layer;
    }

    /**
     * 
     * @param layerElem
     * @return the dimensions
     * @throws XMLParsingException
     */
    protected Dimension[] parseDimensions( Element layerElem )
                            throws XMLParsingException {

        List<Node> nl = XMLTools.getNodes( layerElem, "./Dimension", nsContext );
        Dimension[] dimensions = new Dimension[nl.size()];
        for ( int i = 0; i < dimensions.length; i++ ) {
            String name = XMLTools.getNodeAsString( nl.get( i ), "./@name", nsContext, null );
            String units = XMLTools.getNodeAsString( nl.get( i ), "./@units", nsContext, null );
            String unitSymbol = XMLTools.getNodeAsString( nl.get( i ), "./@unitSymbol", nsContext, null );
            dimensions[i] = new Dimension( name, units, unitSymbol );
        }

        return dimensions;
    }

    /**
     * 
     * @param layerElem
     * @return the extent
     * @throws XMLParsingException
     */
    protected Extent[] parseExtents( Element layerElem )
                            throws XMLParsingException {

        List<Node> nl = XMLTools.getNodes( layerElem, "./Extent", nsContext );
        Extent[] extents = new Extent[nl.size()];
        for ( int i = 0; i < extents.length; i++ ) {
            String name = XMLTools.getNodeAsString( nl.get( i ), "./@name", nsContext, null );
            String deflt = XMLTools.getNodeAsString( nl.get( i ), "./@default", nsContext, null );
            boolean nearestValue = XMLTools.getNodeAsBoolean( nl.get( i ), "./@nearestValue", nsContext, false );
            String value = XMLTools.getNodeAsString( nl.get( i ), ".", nsContext, "" );
            extents[i] = new Extent( name, deflt, nearestValue, value );
        }

        return extents;
    }

    /**
     * 
     * @param layerElem
     * @return the attribution
     * @throws XMLParsingException
     */
    protected Attribution parseAttribution( Element layerElem )
                            throws XMLParsingException {

        Attribution attribution = null;
        Node node = XMLTools.getNode( layerElem, "./Attribution", nsContext );
        if ( node != null ) {
            String title = XMLTools.getRequiredNodeAsString( layerElem, "./Attribution/Title", nsContext );
            OnlineResource onLineResource = parseOnLineResource( XMLTools.getRequiredElement( node, "./OnlineResource",
                                                                                              nsContext ) );
            node = XMLTools.getNode( node, "./LogoURL", nsContext );
            LogoURL logoURL = null;
            if ( node != null ) {
                int width = XMLTools.getRequiredNodeAsInt( node, "./@width", nsContext );
                int height = XMLTools.getRequiredNodeAsInt( node, "./@height", nsContext );
                String format = XMLTools.getRequiredNodeAsString( node, "./Format", nsContext );
                OnlineResource logoOR = parseOnLineResource( XMLTools.getRequiredElement( node, "./OnlineResource",
                                                                                          nsContext ) );
                logoURL = new LogoURL( width, height, format, logoOR.getLinkage().getHref() );
            }
            attribution = new Attribution( title, onLineResource.getLinkage().getHref(), logoURL );
        }

        return attribution;
    }

    /**
     * 
     * @param layerElem
     * @return the URLs
     * @throws XMLParsingException
     */
    protected AuthorityURL[] parseAuthorityURLs( Element layerElem )
                            throws XMLParsingException {

        List<Node> nl = XMLTools.getNodes( layerElem, "./AuthorityURL", nsContext );
        AuthorityURL[] authorityURLs = new AuthorityURL[nl.size()];
        for ( int i = 0; i < authorityURLs.length; i++ ) {
            String name = XMLTools.getRequiredNodeAsString( nl.get( i ), "./@name", nsContext );
            Element tmp = XMLTools.getRequiredElement( nl.get( i ), "./OnlineResource", nsContext );
            OnlineResource olr = parseOnLineResource( tmp );
            authorityURLs[i] = new AuthorityURL( name, olr.getLinkage().getHref() );
        }

        return authorityURLs;
    }

    /**
     * 
     * @param layerElem
     * @return the URLs
     * @throws XMLParsingException
     */
    protected MetadataURL[] parseMetadataURLs( Element layerElem )
                            throws XMLParsingException {

        List<Node> nl = XMLTools.getNodes( layerElem, "./MetadataURL", nsContext );
        MetadataURL[] metadataURL = new MetadataURL[nl.size()];
        for ( int i = 0; i < metadataURL.length; i++ ) {
            String type = XMLTools.getRequiredNodeAsString( nl.get( i ), "./@type", nsContext );
            String format = XMLTools.getRequiredNodeAsString( nl.get( i ), "./Format", nsContext );
            Element tmp = XMLTools.getRequiredElement( nl.get( i ), "./OnlineResource", nsContext );
            OnlineResource olr = parseOnLineResource( tmp );
            metadataURL[i] = new MetadataURL( type, format, olr.getLinkage().getHref() );

        }

        return metadataURL;
    }

    /**
     * 
     * @param layerElem
     * @return the URLs
     * @throws XMLParsingException
     */
    protected DataURL[] parseDataURL( Element layerElem )
                            throws XMLParsingException {

        List<Node> nl = XMLTools.getNodes( layerElem, "./DataURL", nsContext );
        DataURL[] dataURL = new DataURL[nl.size()];
        for ( int i = 0; i < dataURL.length; i++ ) {

            String format = XMLTools.getRequiredNodeAsString( nl.get( i ), "./Format", nsContext );
            Element tmp = XMLTools.getRequiredElement( nl.get( i ), "./OnlineResource", nsContext );
            OnlineResource olr = parseOnLineResource( tmp );
            dataURL[i] = new DataURL( format, olr.getLinkage().getHref() );

        }

        return dataURL;
    }

    /**
     * 
     * @param layerElem
     * @return the URLs
     * @throws XMLParsingException
     */
    protected FeatureListURL[] parseFeatureListURL( Element layerElem )
                            throws XMLParsingException {

        List<Node> nl = XMLTools.getNodes( layerElem, "./FeatureListURL", nsContext );
        FeatureListURL[] flURL = new FeatureListURL[nl.size()];
        for ( int i = 0; i < flURL.length; i++ ) {

            String format = XMLTools.getRequiredNodeAsString( nl.get( i ), "./Format", nsContext );
            Element tmp = XMLTools.getRequiredElement( nl.get( i ), "./OnlineResource", nsContext );
            OnlineResource olr = parseOnLineResource( tmp );
            flURL[i] = new FeatureListURL( format, olr.getLinkage().getHref() );

        }

        return flURL;
    }

    /**
     * 
     * @param layerElem
     * @return the styles
     * @throws XMLParsingException
     */
    protected Style[] parseStyles( Element layerElem )
                            throws XMLParsingException {

        List<Node> nl = XMLTools.getNodes( layerElem, "./Style", nsContext );
        Style[] styles = new Style[nl.size()];
        for ( int i = 0; i < styles.length; i++ ) {
            String name = XMLTools.getRequiredNodeAsString( nl.get( i ), "./Name", nsContext );

            if ( name == null ) {
                throw new XMLParsingException( Messages.getMessage( "WMS_STYLENAME" ) );
            }
            String title = XMLTools.getNodeAsString( nl.get( i ), "./Title", nsContext, null );
            if ( title == null ) {
                throw new XMLParsingException( Messages.getMessage( "WMS_STYLETITLE" ) );
            }
            String styleAbstract = XMLTools.getNodeAsString( nl.get( i ), "./Abstract", nsContext, null );
            LegendURL[] legendURLs = parseLegendURL( nl.get( i ) );
            StyleURL styleURL = parseStyleURL( nl.get( i ) );
            StyleSheetURL styleSheetURL = parseStyleSheetURL( nl.get( i ) );

            styles[i] = new Style( name, title, styleAbstract, legendURLs, styleSheetURL, styleURL, null );
        }

        return styles;
    }

    /**
     * 
     * @param node
     * @return the URLs
     * @throws XMLParsingException
     */
    protected LegendURL[] parseLegendURL( Node node )
                            throws XMLParsingException {

        List<Node> nl = XMLTools.getNodes( node, "./LegendURL", nsContext );
        LegendURL[] lURL = new LegendURL[nl.size()];
        for ( int i = 0; i < lURL.length; i++ ) {
            int width = XMLTools.getRequiredNodeAsInt( nl.get( i ), "./@width", nsContext );
            int height = XMLTools.getRequiredNodeAsInt( nl.get( i ), "./@height", nsContext );
            String format = XMLTools.getRequiredNodeAsString( nl.get( i ), "./Format", nsContext );
            Element tmp = XMLTools.getRequiredElement( nl.get( i ), "./OnlineResource", nsContext );
            OnlineResource olr = parseOnLineResource( tmp );
            lURL[i] = new LegendURL( width, height, format, olr.getLinkage().getHref() );

        }

        return lURL;
    }

    /**
     * 
     * @param node
     * @return the URL
     * @throws XMLParsingException
     */
    protected StyleURL parseStyleURL( Node node )
                            throws XMLParsingException {

        StyleURL styleURL = null;
        Node styleNode = XMLTools.getNode( node, "./StyleURL", nsContext );

        if ( styleNode != null ) {
            String format = XMLTools.getRequiredNodeAsString( styleNode, "./Format", nsContext );
            Element tmp = XMLTools.getRequiredElement( styleNode, "./OnlineResource", nsContext );
            OnlineResource olr = parseOnLineResource( tmp );
            styleURL = new StyleURL( format, olr.getLinkage().getHref() );

        }

        return styleURL;
    }

    /**
     * 
     * @param node
     * @return the URL
     * @throws XMLParsingException
     */
    protected StyleSheetURL parseStyleSheetURL( Node node )
                            throws XMLParsingException {

        StyleSheetURL styleSheetURL = null;
        Node styleNode = XMLTools.getNode( node, "./StyleSheetURL", nsContext );

        if ( styleNode != null ) {
            String format = XMLTools.getRequiredNodeAsString( styleNode, "./Format", nsContext );
            Element tmp = XMLTools.getRequiredElement( styleNode, "./OnlineResource", nsContext );
            OnlineResource olr = parseOnLineResource( tmp );
            styleSheetURL = new StyleSheetURL( format, olr.getLinkage().getHref() );

        }

        return styleSheetURL;
    }

    /**
     * 
     * @param layerElem
     * @param scaleHint
     *            the default scale hint
     * @return the scale hint
     * @throws XMLParsingException
     */
    protected ScaleHint parseScaleHint( Element layerElem, ScaleHint scaleHint )
                            throws XMLParsingException {

        Node scNode = XMLTools.getNode( layerElem, "./ScaleHint", nsContext );
        if ( scNode != null ) {
            double mn = XMLTools.getNodeAsDouble( scNode, "./@min", nsContext, 0 );
            double mx = XMLTools.getNodeAsDouble( scNode, "./@max", nsContext, Double.MAX_VALUE );
            scaleHint = new ScaleHint( mn, mx );
        }

        if ( scaleHint == null ) {
            // set default value to avoid NullPointerException
            // when accessing a layers scalehint
            scaleHint = new ScaleHint( 0, Double.MAX_VALUE );
        }

        return scaleHint;
    }

    /**
     * 
     * @param layerElem
     * @return the identifiers
     * @throws XMLParsingException
     */
    protected Identifier[] parseIdentifiers( Element layerElem )
                            throws XMLParsingException {

        List<Node> nl = XMLTools.getNodes( layerElem, "./Identifier", nsContext );
        Identifier[] identifiers = new Identifier[nl.size()];
        for ( int i = 0; i < identifiers.length; i++ ) {
            String value = XMLTools.getStringValue( nl.get( i ) );
            String authority = XMLTools.getNodeAsString( layerElem, "./@authority", nsContext, null );
            identifiers[i] = new Identifier( value, authority );
        }

        return identifiers;
    }

    /**
     * 
     * @param nl
     * @return the bboxes
     * @throws XMLParsingException
     */
    protected LayerBoundingBox[] parseLayerBoundingBoxes( List<Element> nl )
                            throws XMLParsingException {

        LayerBoundingBox[] llBoxes = new LayerBoundingBox[nl.size()];
        for ( int i = 0; i < llBoxes.length; i++ ) {
            double minx = XMLTools.getRequiredNodeAsDouble( nl.get( i ), "./@minx", nsContext );
            double maxx = XMLTools.getRequiredNodeAsDouble( nl.get( i ), "./@maxx", nsContext );
            double miny = XMLTools.getRequiredNodeAsDouble( nl.get( i ), "./@miny", nsContext );
            double maxy = XMLTools.getRequiredNodeAsDouble( nl.get( i ), "./@maxy", nsContext );
            double resx = XMLTools.getNodeAsDouble( nl.get( i ), "./@resx", nsContext, -1 );
            double resy = XMLTools.getNodeAsDouble( nl.get( i ), "./@resx", nsContext, -1 );
            String srs = XMLTools.getRequiredNodeAsString( nl.get( i ), "./@SRS", nsContext );
            Position min = GeometryFactory.createPosition( minx, miny );
            Position max = GeometryFactory.createPosition( maxx, maxy );
            llBoxes[i] = new LayerBoundingBox( min, max, srs, resx, resy );
        }

        return llBoxes;
    }

    /**
     * 
     * @param llBox
     * @return the envelope
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    protected Envelope parseLatLonBoundingBox( Element llBox )
                            throws XMLParsingException, UnknownCRSException {

        double minx = XMLTools.getRequiredNodeAsDouble( llBox, "./@minx", nsContext );
        double maxx = XMLTools.getRequiredNodeAsDouble( llBox, "./@maxx", nsContext );
        double miny = XMLTools.getRequiredNodeAsDouble( llBox, "./@miny", nsContext );
        double maxy = XMLTools.getRequiredNodeAsDouble( llBox, "./@maxy", nsContext );
        CoordinateSystem crs = CRSFactory.create( "EPSG:4326" );

        Envelope env = GeometryFactory.createEnvelope( minx, miny, maxx, maxy, crs );

        return env;
    }

}

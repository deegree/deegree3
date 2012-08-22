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

import static java.lang.Double.MAX_VALUE;
import static org.deegree.framework.xml.XMLTools.getElements;

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
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.MetadataURL;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
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
 * <code>WMSCapabilitiesDocument_1_3_0</code> is a parser class for capabilities documents according to the OGC WMS
 * 1.3.0 specification.
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version 2.0, $Revision$, $Date$
 * 
 * @since 2.0
 */

public class WMSCapabilitiesDocument_1_3_0 extends WMSCapabilitiesDocument {

    private static final long serialVersionUID = -5085466301546020723L;

    private static final String XML_TEMPLATE = "WMSCapabilitiesTemplate_1_3_0.xml";

    private static final ILogger LOG = LoggerFactory.getLogger( WMSCapabilitiesDocument_1_3_0.class );

    private static final String PWMS = CommonNamespaces.WMS_PREFIX + ":";

    private static final String PSLD = CommonNamespaces.SLD_PREFIX + ":";

    /*
     * Just using a different XML_TEMPLATE.
     */
    @Override
    public void createEmptyDocument()
                            throws IOException, SAXException {

        URL url = WMSCapabilitiesDocument.class.getResource( XML_TEMPLATE );
        if ( url == null ) {
            throw new IOException( "The resource '" + XML_TEMPLATE + " could not be found." );
        }
        load( url );
    }

    @Override
    protected List<String> parseExceptionFormats( Element elem )
                            throws XMLParsingException {
        String[] formats = XMLTools.getRequiredNodesAsStrings( elem, PWMS + "Format", nsContext );
        return Arrays.asList( formats );
    }

    /*
     * Prefix added, not parsing UserDefinedSymbolization. Parsing new LayerLimit, MaxWidth, MaxHeight elements.
     */
    @Override
    public OGCCapabilities parseCapabilities()
                            throws InvalidCapabilitiesException {
        ServiceIdentification serviceIdentification = null;
        ServiceProvider serviceProvider = null;
        OperationsMetadata metadata = null;
        Layer layer = null;
        String version = parseVersion();
        String updateSeq = parseUpdateSequence();
        List<String> exceptions;

        Element root = getRootElement();

        int layerLimit = 0;
        int maxWidth = 0;
        int maxHeight = 0;

        try {
            Element serviceElement = (Element) XMLTools.getRequiredNode( root, PWMS + "Service", nsContext );

            layerLimit = XMLTools.getNodeAsInt( serviceElement, PWMS + "LayerLimit", nsContext, 0 );
            maxWidth = XMLTools.getNodeAsInt( serviceElement, PWMS + "MaxWidth", nsContext, 0 );
            maxHeight = XMLTools.getNodeAsInt( serviceElement, PWMS + "MaxHeight", nsContext, 0 );

            serviceIdentification = parseServiceIdentification();
            serviceProvider = parseServiceProvider();
            metadata = parseOperationsMetadata();

            Element exceptionElement = XMLTools.getRequiredElement( getRootElement(), PWMS + "Capability/" + PWMS
                                                                                      + "Exception", nsContext );
            exceptions = parseExceptionFormats( exceptionElement );

            Element layerElem = (Element) XMLTools.getRequiredNode( root, PWMS + "Capability/" + PWMS + "Layer",
                                                                    nsContext );
            layer = parseLayers( layerElem, null, null );
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InvalidCapabilitiesException( e.getMessage() + StringTools.stackTraceToString( e ) );
        } catch ( Exception e ) {
            LOG.logError( e.getLocalizedMessage(), e );
            throw new InvalidCapabilitiesException( e.getMessage() + StringTools.stackTraceToString( e ) );
        }

        WMSCapabilities_1_3_0 wmsCapabilities = new WMSCapabilities_1_3_0( version, updateSeq, serviceIdentification,
                                                                           serviceProvider, metadata, layer,
                                                                           layerLimit, maxWidth, maxHeight, exceptions );
        return wmsCapabilities;
    }

    /*
     * Prefix added. Name is now "WMS" instead of "OGC:WMS"
     */
    @Override
    protected ServiceIdentification parseServiceIdentification()
                            throws XMLParsingException {

        String name = XMLTools.getNodeAsString( getRootElement(), PWMS + "Service/" + PWMS + "Name", nsContext, null );
        String title = XMLTools.getNodeAsString( getRootElement(), PWMS + "Service/" + PWMS + "Title", nsContext, name );
        String serviceAbstract = XMLTools.getNodeAsString( getRootElement(), PWMS + "Service/" + PWMS + "Abstract",
                                                           nsContext, null );

        String[] kw = XMLTools.getNodesAsStrings( getRootElement(), PWMS + "Service/" + PWMS + "KeywordList/" + PWMS
                                                                    + "Keyword", nsContext );

        Keywords[] keywordArray = new Keywords[] { new Keywords( kw ) };
        List<Keywords> keywords = Arrays.asList( keywordArray );

        String fees = XMLTools.getNodeAsString( getRootElement(), PWMS + "Service/" + PWMS + "Fees", nsContext, null );

        List<Constraints> accessConstraints = new ArrayList<Constraints>();

        String[] constraints = XMLTools.getNodesAsStrings( getRootElement(), PWMS + "Service/" + PWMS
                                                                             + "AccessConstraints", nsContext );

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
                                                                                 new Code( "WMS" ),
                                                                                 versions,
                                                                                 title,
                                                                                 null,
                                                                                 new Date( System.currentTimeMillis() ),
                                                                                 title, serviceAbstract, keywords,
                                                                                 accessConstraints );

        return serviceIdentification;
    }

    /*
     * Prefix added.
     */
    @Override
    protected ServiceProvider parseServiceProvider()
                            throws XMLParsingException {
        Node ci = XMLTools.getNode( getRootElement(), PWMS + "Service/" + PWMS + "ContactInformation", nsContext );

        // according to WMS 1.1.1 specification this element is mandatory
        // but there are several services online which does not contain
        // this element in its capabilities :-(
        String s = XMLTools.getNodeAsString( getRootElement(), PWMS + "Service/" + PWMS + "OnlineResource/@xlink:href",
                                             nsContext, null );

        OnlineResource providerSite = null;

        if ( s != null ) {
            try {
                providerSite = new OnlineResource( new Linkage( new URL( s ) ) );
            } catch ( MalformedURLException e ) {
                throw new XMLParsingException( "could not parse service online resource", e );
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
            person = XMLTools.getNodeAsString( ci, PWMS + "ContactPersonPrimary/" + PWMS + "ContactPerson", nsContext,
                                               null );
            orga = XMLTools.getNodeAsString( ci, PWMS + "ContactPersonPrimary/" + PWMS + "ContactOrganization",
                                             nsContext, null );
            position = XMLTools.getNodeAsString( ci, PWMS + "ContactPosition", nsContext, null );
        }
        ContactInfo contact = parseContactInfo();

        CitedResponsibleParty party = new CitedResponsibleParty( new ContactInfo[] { contact },
                                                                 new String[] { person }, new String[] { orga },
                                                                 new String[] { position }, null );
        ServiceProvider sp = new ServiceProvider( person, providerSite, party );

        return sp;
    }

    /*
     * Prefix added.
     */
    @Override
    protected ContactInfo parseContactInfo()
                            throws XMLParsingException {
        Node ci = XMLTools.getNode( getRootElement(), PWMS + "Service/" + PWMS + "ContactInformation", nsContext );
        ContactInfo cont = null;
        if ( ci != null ) {
            String[] addr = XMLTools.getNodesAsStrings( ci, PWMS + "ContactAddress/" + PWMS + "Address", nsContext );
            // String addrType =
            // XMLTools.getNodeAsString( ci, "./ContactAddress/AddressType", nsContext, null );
            String city = XMLTools.getNodeAsString( ci, PWMS + "ContactAddress/" + PWMS + "City", nsContext, null );
            String state = XMLTools.getNodeAsString( ci, PWMS + "ContactAddress/" + PWMS + "StateOrProvince",
                                                     nsContext, null );
            String pc = XMLTools.getNodeAsString( ci, PWMS + "ContactAddress/" + PWMS + "PostCode", nsContext, null );
            String country = XMLTools.getNodeAsString( ci, PWMS + "ContactAddress/" + PWMS + "Country", nsContext, null );
            String[] mail = XMLTools.getNodesAsStrings( ci, PWMS + "ContactElectronicMailAddress", nsContext );
            Address address = new Address( state, city, country, addr, mail, pc );

            String[] phone = XMLTools.getNodesAsStrings( ci, PWMS + "ContactVoiceTelephone", nsContext );
            String[] fax = XMLTools.getNodesAsStrings( ci, PWMS + "ContactFacsimileTelephone", nsContext );

            Phone ph = new Phone( fax, phone );

            cont = new ContactInfo( address, null, null, null, ph );
        }

        return cont;
    }

    /*
     * Prefix added.
     */
    @Override
    protected OperationsMetadata parseOperationsMetadata()
                            throws XMLParsingException {

        Node opNode = XMLTools.getNode( getRootElement(), PWMS + "Capability/" + PWMS + "Request/" + PWMS
                                                          + "GetCapabilities", nsContext );

        if ( opNode == null ) {
            // may it is a WMS 1.0.0 capabilities document
            opNode = XMLTools.getRequiredNode( getRootElement(), PWMS + "Capability/" + PWMS + "Request/" + PWMS
                                                                 + "Capabilities", nsContext );
        }
        Operation getCapa = parseOperation( opNode );

        opNode = XMLTools.getNode( getRootElement(), PWMS + "Capability/" + PWMS + "Request/" + PWMS + "GetMap",
                                   nsContext );
        if ( opNode == null ) {
            // may it is a WMS 1.0.0 capabilities document
            opNode = XMLTools.getRequiredNode( getRootElement(), PWMS + "Capability/" + PWMS + "Request/" + PWMS
                                                                 + "Map", nsContext );
        }
        Operation getMap = parseOperation( opNode );

        opNode = XMLTools.getNode( getRootElement(),
                                   PWMS + "Capability/" + PWMS + "Request/" + PWMS + "GetFeatureInfo", nsContext );
        Operation getFI = null;
        if ( opNode != null ) {
            getFI = parseOperation( opNode );
        } else {
            // maybe its WMS 1.0.0
            opNode = XMLTools.getNode( getRootElement(), PWMS + "Capability/" + PWMS + "Request/" + PWMS
                                                         + "FeatureInfo", nsContext );
            if ( opNode != null ) {
                getFI = parseOperation( opNode );
            }
        }

        opNode = XMLTools.getNode( getRootElement(), PWMS + "Capability/" + PWMS + "Request/" + PSLD
                                                     + "GetLegendGraphic", nsContext );
        Operation getLG = null;
        if ( opNode != null ) {
            getLG = parseOperation( opNode );
        }

        opNode = XMLTools.getNode( getRootElement(), PWMS + "Capability/" + PWMS + "Request/" + PSLD + "DescribeLayer",
                                   nsContext );
        Operation descL = null;
        if ( opNode != null ) {
            descL = parseOperation( opNode );
        }

        opNode = XMLTools.getNode( getRootElement(), PWMS + "Capability/" + PWMS + "Request/" + PSLD + "GetStyles",
                                   nsContext );
        Operation getStyles = null;
        if ( opNode != null ) {
            getStyles = parseOperation( opNode );
        }

        opNode = XMLTools.getNode( getRootElement(), PWMS + "Capability/" + PWMS + "Request/" + PSLD + "PutStyles",
                                   nsContext );
        Operation putStyles = null;
        if ( opNode != null ) {
            putStyles = parseOperation( opNode );
        }

        List<Operation> operations = new ArrayList<Operation>();

        StringBuffer debug = new StringBuffer();

        if ( getCapa != null ) {
            operations.add( getCapa );
            debug.append( " GetCapabilities" );
        }
        if ( getMap != null ) {
            operations.add( getMap );
            debug.append( " GetMap" );
        }
        if ( getFI != null ) {
            operations.add( getFI );
            debug.append( " GetFeatureInfo" );
        }
        if ( getLG != null ) {
            operations.add( getLG );
            debug.append( " GetLegendGraphic" );
        }
        if ( descL != null ) {
            operations.add( descL );
            debug.append( " DescribeLayer" );
        }
        if ( getStyles != null ) {
            operations.add( getStyles );
            debug.append( " GetStyles" );
        }
        if ( putStyles != null ) {
            operations.add( putStyles );
            debug.append( " PutStyles" );
        }

        LOG.logDebug( "Configured request types:" + debug );

        OperationsMetadata metadata = new OperationsMetadata( null, null, operations, null );

        return metadata;
    }

    /*
     * Prefix added.
     */
    @Override
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

        String[] tmp = XMLTools.getRequiredNodesAsStrings( node, PWMS + "Format", nsContext );
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

        List<?> nl = XMLTools.getRequiredNodes( node, PWMS + "DCPType", nsContext );
        List<DCP> dcps = new ArrayList<DCP>();

        for ( Object element : nl ) {
            dcps.add( parseDCP( (Element) element ) );
        }

        return new Operation( new QualifiedName( name ), dcps, parameters, null, null, null );
    }

    /*
     * Prefix added.
     */
    @Override
    protected DCP parseDCP( Element element )
                            throws XMLParsingException {
        List<HTTP.Type> types = new ArrayList<HTTP.Type>();
        List<OnlineResource> links = new ArrayList<OnlineResource>();

        Element elem = (Element) XMLTools.getRequiredNode( element, PWMS + "HTTP", nsContext );
        String s = null;
        try {
            List<?> nl = XMLTools.getNodes( elem, PWMS + "Get", nsContext );

            for ( int i = 0; i < nl.size(); i++ ) {
                s = XMLTools.getNodeAsString( (Node) nl.get( i ), "./@xlink:href", nsContext, null );
                if ( s == null ) {
                    s = XMLTools.getRequiredNodeAsString( (Node) nl.get( i ), PWMS + "OnlineResource/@xlink:href",
                                                          nsContext );
                }
                types.add( HTTP.Type.Get );
                links.add( new OnlineResource( new Linkage( new URL( s ) ) ) );
            }
        } catch ( Exception e ) {
            throw new XMLParsingException( Messages.getMessage( "WMS_DCPGET", s ) );
        }
        try {
            List<?> nl = XMLTools.getNodes( elem, PWMS + "Post", nsContext );

            for ( int i = 0; i < nl.size(); i++ ) {
                s = XMLTools.getNodeAsString( (Node) nl.get( i ), "./@xlink:href", nsContext, null );
                if ( s == null ) {
                    s = XMLTools.getRequiredNodeAsString( (Node) nl.get( i ), PWMS + "OnlineResource/@xlink:href",
                                                          nsContext );
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

    /*
     * Prefix added. Removed parsing of Extent. Removed parsing of ScaleHint. Changed SRS to CRS.
     */
    @Override
    protected Layer parseLayers( Element layerElem, Layer parent, ScaleHint scaleHint )
                            throws XMLParsingException, UnknownCRSException {

        boolean queryable = XMLTools.getNodeAsBoolean( layerElem, "./@queryable", nsContext, false );

        int cascaded = XMLTools.getNodeAsInt( layerElem, "./@cascaded", nsContext, 0 );
        boolean opaque = XMLTools.getNodeAsBoolean( layerElem, "./@opaque", nsContext, false );
        boolean noSubsets = XMLTools.getNodeAsBoolean( layerElem, "./@noSubsets", nsContext, false );
        int fixedWidth = XMLTools.getNodeAsInt( layerElem, "./@fixedWidth", nsContext, 0 );
        int fixedHeight = XMLTools.getNodeAsInt( layerElem, "./@fixedHeight", nsContext, 0 );
        String name = XMLTools.getNodeAsString( layerElem, PWMS + "Name", nsContext, null );
        String title = XMLTools.getRequiredNodeAsString( layerElem, PWMS + "Title", nsContext );
        String layerAbstract = XMLTools.getNodeAsString( layerElem, PWMS + "Abstract", nsContext, null );
        String[] keywords = XMLTools.getNodesAsStrings( layerElem, PWMS + "KeywordList/" + PWMS + "Keyword", nsContext );
        String[] srs = XMLTools.getNodesAsStrings( layerElem, PWMS + "CRS", nsContext );

        List<Element> nl = XMLTools.getElements( layerElem, PWMS + "BoundingBox", nsContext );
        // TODO
        // substitue with Envelope
        LayerBoundingBox[] bboxes = null;
        if ( nl.size() == 0 && parent != null ) {
            // inherit BoundingBoxes from parent layer
            bboxes = parent.getBoundingBoxes();
        } else {
            bboxes = parseLayerBoundingBoxes( nl );
        }

        Element llBox = (Element) XMLTools.getNode( layerElem, PWMS + "EX_GeographicBoundingBox", nsContext );
        Envelope llBoundingBox = null;

        if ( llBox == null && parent != null ) {
            // inherit LatLonBoundingBox parent layer
            llBoundingBox = parent.getLatLonBoundingBox();
        } else if ( llBox != null ) {
            llBoundingBox = parseEX_GeographicBoundingBox( llBox );
        } else {
            llBoundingBox = GeometryFactory.createEnvelope( -180, -90, 180, 90, CRSFactory.create( "EPSG:4326" ) );
        }

        Dimension[] dimensions = parseDimensions( layerElem );

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
                                 dimensions, null, authorityURLs, identifiers, metadataURLs, dataURLs, featureListURLs,
                                 styles, null, null, parent );

        // get Child layers
        nl = XMLTools.getElements( layerElem, PWMS + "Layer", nsContext );
        Layer[] layers = new Layer[nl.size()];
        for ( int i = 0; i < layers.length; i++ ) {
            layers[i] = parseLayers( nl.get( i ), layer, scaleHint );
        }

        // set child layers
        layer.setLayer( layers );

        return layer;
    }

    /*
     * Prefix added. Changed to Min/MaxScaleDenominator.
     */
    @Override
    protected ScaleHint parseScaleHint( Element layerElem, ScaleHint scaleHint )
                            throws XMLParsingException {

        Node min = XMLTools.getNode( layerElem, PWMS + "MinScaleDenominator", nsContext );
        Node max = XMLTools.getNode( layerElem, PWMS + "MaxScaleDenominator", nsContext );
        double mn = 0;
        double mx = MAX_VALUE;
        if ( min != null ) {
            mn = XMLTools.getRequiredNodeAsDouble( min, ".", nsContext );
        }
        if ( max != null ) {
            mx = XMLTools.getRequiredNodeAsDouble( max, ".", nsContext );
        }

        scaleHint = new ScaleHint( mn, mx );

        return scaleHint;
    }

    @Override
    protected Dimension[] parseDimensions( Element layerElem )
                            throws XMLParsingException {
        List<Element> nl = getElements( layerElem, PWMS + "Dimension", nsContext );
        Dimension[] dimensions = new Dimension[nl.size()];

        for ( int i = 0; i < dimensions.length; i++ ) {
            dimensions[i] = new Dimension( nl.get( i ) );
        }

        return dimensions;
    }

    /*
     * Prefix added.
     */
    @Override
    protected Attribution parseAttribution( Element layerElem )
                            throws XMLParsingException {

        Attribution attribution = null;
        Node node = XMLTools.getNode( layerElem, PWMS + "Attribution", nsContext );
        if ( node != null ) {
            String title = XMLTools.getRequiredNodeAsString( layerElem, PWMS + "Attribution/" + PWMS + "Title",
                                                             nsContext );
            OnlineResource onLineResource = parseOnLineResource( (Element) XMLTools.getRequiredNode(
                                                                                                     node,
                                                                                                     PWMS
                                                                                                                             + "OnlineResource",
                                                                                                     nsContext ) );
            node = XMLTools.getNode( node, PWMS + "LogoURL", nsContext );
            LogoURL logoURL = null;
            if ( node != null ) {
                int width = XMLTools.getRequiredNodeAsInt( node, "./@width", nsContext );
                int height = XMLTools.getRequiredNodeAsInt( node, "./@height", nsContext );
                String format = XMLTools.getRequiredNodeAsString( node, "./Format", nsContext );
                OnlineResource logoOR = parseOnLineResource( (Element) XMLTools.getRequiredNode(
                                                                                                 node,
                                                                                                 PWMS
                                                                                                                         + "OnlineResource",
                                                                                                 nsContext ) );
                logoURL = new LogoURL( width, height, format, logoOR.getLinkage().getHref() );
            }
            attribution = new Attribution( title, onLineResource.getLinkage().getHref(), logoURL );
        }

        return attribution;
    }

    /*
     * Prefix added.
     */
    @Override
    protected AuthorityURL[] parseAuthorityURLs( Element layerElem )
                            throws XMLParsingException {

        List<?> nl = XMLTools.getNodes( layerElem, PWMS + "AuthorityURL", nsContext );
        AuthorityURL[] authorityURLs = new AuthorityURL[nl.size()];
        for ( int i = 0; i < authorityURLs.length; i++ ) {
            String name = XMLTools.getRequiredNodeAsString( (Node) nl.get( i ), "./@name", nsContext );
            Element tmp = (Element) XMLTools.getRequiredNode( (Node) nl.get( i ), PWMS + "OnlineResource", nsContext );
            OnlineResource olr = parseOnLineResource( tmp );
            authorityURLs[i] = new AuthorityURL( name, olr.getLinkage().getHref() );
        }

        return authorityURLs;
    }

    /*
     * Prefix added.
     */
    @Override
    protected MetadataURL[] parseMetadataURLs( Element layerElem )
                            throws XMLParsingException {

        List<?> nl = XMLTools.getNodes( layerElem, PWMS + "MetadataURL", nsContext );
        MetadataURL[] metadataURL = new MetadataURL[nl.size()];
        for ( int i = 0; i < metadataURL.length; i++ ) {
            String type = XMLTools.getRequiredNodeAsString( (Node) nl.get( i ), "./@type", nsContext );
            String format = XMLTools.getRequiredNodeAsString( (Node) nl.get( i ), PWMS + "Format", nsContext );
            Element tmp = (Element) XMLTools.getRequiredNode( (Node) nl.get( i ), PWMS + "OnlineResource", nsContext );
            OnlineResource olr = parseOnLineResource( tmp );
            metadataURL[i] = new MetadataURL( type, format, olr.getLinkage().getHref() );

        }

        return metadataURL;
    }

    /*
     * Prefix added.
     */
    @Override
    protected DataURL[] parseDataURL( Element layerElem )
                            throws XMLParsingException {

        List<?> nl = XMLTools.getNodes( layerElem, PWMS + "DataURL", nsContext );
        DataURL[] dataURL = new DataURL[nl.size()];
        for ( int i = 0; i < dataURL.length; i++ ) {

            String format = XMLTools.getRequiredNodeAsString( (Node) nl.get( i ), PWMS + "Format", nsContext );
            Element tmp = (Element) XMLTools.getRequiredNode( (Node) nl.get( i ), PWMS + "OnlineResource", nsContext );
            OnlineResource olr = parseOnLineResource( tmp );
            dataURL[i] = new DataURL( format, olr.getLinkage().getHref() );

        }

        return dataURL;
    }

    /*
     * Prefix added.
     */
    @Override
    protected FeatureListURL[] parseFeatureListURL( Element layerElem )
                            throws XMLParsingException {

        List<?> nl = XMLTools.getNodes( layerElem, PWMS + "FeatureListURL", nsContext );
        FeatureListURL[] flURL = new FeatureListURL[nl.size()];
        for ( int i = 0; i < flURL.length; i++ ) {

            String format = XMLTools.getRequiredNodeAsString( (Node) nl.get( i ), "./Format", nsContext );
            Element tmp = (Element) XMLTools.getRequiredNode( (Node) nl.get( i ), PWMS + "OnlineResource", nsContext );
            OnlineResource olr = parseOnLineResource( tmp );
            flURL[i] = new FeatureListURL( format, olr.getLinkage().getHref() );

        }

        return flURL;
    }

    /*
     * Prefix added.
     */
    @Override
    protected Style[] parseStyles( Element layerElem )
                            throws XMLParsingException {

        List<?> nl = XMLTools.getNodes( layerElem, PWMS + "Style", nsContext );
        Style[] styles = new Style[nl.size()];
        for ( int i = 0; i < styles.length; i++ ) {
            String name = XMLTools.getRequiredNodeAsString( (Node) nl.get( i ), PWMS + "Name", nsContext );

            if ( name == null ) {
                throw new XMLParsingException( Messages.getMessage( "WMS_STYLENAME" ) );
            }
            String title = XMLTools.getNodeAsString( (Node) nl.get( i ), PWMS + "Title", nsContext, null );
            if ( title == null ) {
                throw new XMLParsingException( Messages.getMessage( "WMS_STYLETITLE" ) );
            }
            String styleAbstract = XMLTools.getNodeAsString( (Node) nl.get( i ), PWMS + "Abstract", nsContext, null );
            LegendURL[] legendURLs = parseLegendURL( (Node) nl.get( i ) );
            StyleURL styleURL = parseStyleURL( (Node) nl.get( i ) );
            StyleSheetURL styleSheetURL = parseStyleSheetURL( (Node) nl.get( i ) );

            styles[i] = new Style( name, title, styleAbstract, legendURLs, styleSheetURL, styleURL, null );
        }

        return styles;
    }

    /*
     * Prefix added.
     */
    @Override
    protected LegendURL[] parseLegendURL( Node node )
                            throws XMLParsingException {

        List<?> nl = XMLTools.getNodes( node, PWMS + "LegendURL", nsContext );
        LegendURL[] lURL = new LegendURL[nl.size()];
        for ( int i = 0; i < lURL.length; i++ ) {
            int width = XMLTools.getRequiredNodeAsInt( (Node) nl.get( i ), "./@width", nsContext );
            int height = XMLTools.getRequiredNodeAsInt( (Node) nl.get( i ), "./@height", nsContext );
            String format = XMLTools.getRequiredNodeAsString( (Node) nl.get( i ), PWMS + "Format", nsContext );
            Element tmp = (Element) XMLTools.getRequiredNode( (Node) nl.get( i ), PWMS + "OnlineResource", nsContext );
            OnlineResource olr = parseOnLineResource( tmp );
            lURL[i] = new LegendURL( width, height, format, olr.getLinkage().getHref() );

        }

        return lURL;
    }

    /*
     * Prefix added.
     */
    @Override
    protected StyleURL parseStyleURL( Node node )
                            throws XMLParsingException {

        StyleURL styleURL = null;
        Node styleNode = XMLTools.getNode( node, PWMS + "StyleURL", nsContext );

        if ( styleNode != null ) {
            String format = XMLTools.getRequiredNodeAsString( styleNode, PWMS + "Format", nsContext );
            Element tmp = (Element) XMLTools.getRequiredNode( styleNode, PWMS + "OnlineResource", nsContext );
            OnlineResource olr = parseOnLineResource( tmp );
            styleURL = new StyleURL( format, olr.getLinkage().getHref() );

        }

        return styleURL;
    }

    /*
     * Prefix added.
     */
    @Override
    protected StyleSheetURL parseStyleSheetURL( Node node )
                            throws XMLParsingException {

        StyleSheetURL styleSheetURL = null;
        Node styleNode = XMLTools.getNode( node, PWMS + "StyleSheetURL", nsContext );

        if ( styleNode != null ) {
            String format = XMLTools.getRequiredNodeAsString( styleNode, PWMS + "Format", nsContext );
            Element tmp = (Element) XMLTools.getRequiredNode( styleNode, PWMS + "OnlineResource", nsContext );
            OnlineResource olr = parseOnLineResource( tmp );
            styleSheetURL = new StyleSheetURL( format, olr.getLinkage().getHref() );

        }

        return styleSheetURL;
    }

    /*
     * Prefix added.
     */
    @Override
    protected Identifier[] parseIdentifiers( Element layerElem )
                            throws XMLParsingException {

        List<?> nl = XMLTools.getNodes( layerElem, PWMS + "Identifier", nsContext );
        Identifier[] identifiers = new Identifier[nl.size()];
        for ( int i = 0; i < identifiers.length; i++ ) {
            String value = XMLTools.getStringValue( (Node) nl.get( i ) );
            String authority = XMLTools.getNodeAsString( layerElem, "./@authority", nsContext, null );
            identifiers[i] = new Identifier( value, authority );
        }

        return identifiers;
    }

    /*
     * Changed SRS to CRS.
     */
    @Override
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
            String srs = XMLTools.getRequiredNodeAsString( nl.get( i ), "./@CRS", nsContext );
            if ( srs.startsWith( "EPSG" ) ) {
                Position min = GeometryFactory.createPosition( miny, minx );
                Position max = GeometryFactory.createPosition( maxy, maxx );
                llBoxes[i] = new LayerBoundingBox( min, max, srs, resx, resy );
            } else {
                Position min = GeometryFactory.createPosition( minx, miny );
                Position max = GeometryFactory.createPosition( maxx, maxy );
                llBoxes[i] = new LayerBoundingBox( min, max, srs, resx, resy );
            }
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
    protected Envelope parseEX_GeographicBoundingBox( Element llBox )
                            throws XMLParsingException, UnknownCRSException {

        double minx = XMLTools.getRequiredNodeAsDouble( llBox, PWMS + "westBoundLongitude", nsContext );
        double maxx = XMLTools.getRequiredNodeAsDouble( llBox, PWMS + "eastBoundLongitude", nsContext );
        double miny = XMLTools.getRequiredNodeAsDouble( llBox, PWMS + "southBoundLatitude", nsContext );
        double maxy = XMLTools.getRequiredNodeAsDouble( llBox, PWMS + "northBoundLatitude", nsContext );
        CoordinateSystem crs = CRSFactory.create( "EPSG:4326" );

        Envelope env = GeometryFactory.createEnvelope( minx, miny, maxx, maxy, crs );

        return env;
    }

}

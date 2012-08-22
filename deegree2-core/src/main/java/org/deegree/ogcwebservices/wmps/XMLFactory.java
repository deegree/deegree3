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
package org.deegree.ogcwebservices.wmps;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.deegree.datatypes.xlink.SimpleLink;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.Pair;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.metadata.iso19115.Address;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.metadata.iso19115.Phone;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.HTTP;
import org.deegree.ogcwebservices.getcapabilities.MetadataURL;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.ogcwebservices.wmps.capabilities.WMPSCapabilities;
import org.deegree.ogcwebservices.wmps.capabilities.WMPSCapabilitiesDocument;
import org.deegree.ogcwebservices.wmps.capabilities.WMPSOperationsMetadata;
import org.deegree.ogcwebservices.wmps.operation.DescribeTemplateResponse;
import org.deegree.ogcwebservices.wmps.operation.DescribeTemplateResponseDocument;
import org.deegree.ogcwebservices.wmps.operation.GetAvailableTemplatesResponse;
import org.deegree.ogcwebservices.wmps.operation.GetAvailableTemplatesResponseDocument;
import org.deegree.ogcwebservices.wmps.operation.PrintMapResponse;
import org.deegree.ogcwebservices.wmps.operation.PrintMapResponseDocument;
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
import org.deegree.owscommon.OWSDomainType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Helper class to create WMPS responses.
 *
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh</a>
 * @version 2.0
 *
 */
public class XMLFactory extends org.deegree.owscommon.XMLFactory {

    private static final ILogger LOG = LoggerFactory.getLogger( XMLFactory.class );

    private static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    /**
     * Exports a <code>WMPSCapabilities</code> instance to a <code>WMPSCapabilitiesDocument</code>.
     *
     * @param capabilities
     * @return DOM representation of the <code>WMPSCapabilities</code>
     * @throws IOException
     *             if XML template could not be loaded
     */
    public static WMPSCapabilitiesDocument export( WMPSCapabilities capabilities )
                            throws IOException {

        WMPSCapabilitiesDocument capabilitiesDocument = new WMPSCapabilitiesDocument();
        try {
            capabilitiesDocument.createEmptyDocument();
            Element root = capabilitiesDocument.getRootElement();
            root.setAttribute( "version", capabilities.getVersion() );

            appendService( root, capabilities.getServiceIdentification(), capabilities.getServiceProvider() );

            String xPath = "./Capability";
            appendUserDefinedSymbolization( (Element) XMLTools.getNode( root, xPath, nsContext ),
                                            capabilities.getUserDefinedSymbolization() );

            appendCapabilityRequests( root, (WMPSOperationsMetadata) capabilities.getOperationMetadata() );

            appendCapabilityLayer( (Element) XMLTools.getNode( root, "./Capability", nsContext ),
                                   capabilities.getLayer() );

        } catch ( SAXException e ) {
            LOG.logError( e.getMessage(), e );
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
        }

        return capabilitiesDocument;
    }

    /**
     * Append User defined symbolization.
     *
     * @param root
     * @param uds
     */
    protected static void appendUserDefinedSymbolization( Element root, UserDefinedSymbolization uds ) {

        Element elem = XMLTools.appendElement( root, null, "UserDefinedSymbolization" );
        elem.setAttribute( "SupportSLD", Boolean.toString( uds.isSldSupported() ) );
        elem.setAttribute( "UserLayer", Boolean.toString( uds.isUserLayerSupported() ) );
        elem.setAttribute( "UserStyle", Boolean.toString( uds.isUserStyleSupported() ) );
        elem.setAttribute( "RemoteWFS", Boolean.toString( uds.isRemoteWFSSupported() ) );

    }

    /**
     * Append service element
     *
     * @param root
     * @param identification
     * @param provider
     * @throws XMLParsingException
     */
    protected static void appendService( Element root, ServiceIdentification identification, ServiceProvider provider )
                            throws XMLParsingException {

        root = (Element) XMLTools.getRequiredNode( root, "./Service", nsContext );

        Node node = XMLTools.getRequiredNode( root, "./Name", nsContext );
        XMLTools.setNodeValue( (Element) node, identification.getTitle() );

        node = XMLTools.getRequiredNode( root, "./Title", nsContext );
        XMLTools.setNodeValue( (Element) node, identification.getTitle() );

        String serviceAbstract = identification.getAbstract();
        if ( serviceAbstract != null ) {
            XMLTools.appendElement( root, null, "Abstract", serviceAbstract );
        }

        Keywords[] keywords = identification.getKeywords();
        if ( keywords.length > 0 ) {
            String[] kw = keywords[0].getKeywords();
            Element kwl = XMLTools.appendElement( root, null, "KeywordList" );
            for ( int i = 0; i < kw.length; i++ ) {
                XMLTools.appendElement( kwl, null, "Keyword", kw[i] );
            }
        }

        node = XMLTools.getRequiredNode( root, "./OnlineResource", nsContext );
        SimpleLink sLink = provider.getProviderSite();
        ( (Element) node ).setAttribute( "xlink:href", sLink.getHref().toASCIIString() );

        appendContactInformation( root, provider );

        if ( identification.getFees() != null ) {
            XMLTools.appendElement( root, null, "Fees", identification.getFees() );
        } else {
            XMLTools.appendElement( root, null, "Fees", "none" );
        }

        if ( identification.getAccessConstraints().length > 0 ) {
            XMLTools.appendElement( root, null, "AccessConstraints", identification.getAccessConstraints()[0] );
        } else {
            XMLTools.appendElement( root, null, "AccessConstraints", "none" );
        }

    }

    /**
     * Append contact information
     *
     * @param root
     * @param provider
     */
    protected static void appendContactInformation( Element root, ServiceProvider provider ) {

        Element ciNode = XMLTools.appendElement( root, null, "ContactInformation" );
        Element cppNode = XMLTools.appendElement( ciNode, null, "ContactPersonPrimary" );
        if ( provider.getIndividualName() != null ) {
            XMLTools.appendElement( cppNode, null, "ContactPerson", provider.getIndividualName() );
        }
        if ( provider.getProviderName() != null ) {
            XMLTools.appendElement( cppNode, null, "ContactOrganization", provider.getProviderName() );
        }
        if ( provider.getPositionName() != null ) {
            XMLTools.appendElement( ciNode, null, "ContactPosition", provider.getPositionName() );
        }
        Element caNode = XMLTools.appendElement( ciNode, null, "ContactAddress" );

        XMLTools.appendElement( caNode, null, "AddressType", "postal" );

        Address addr = provider.getContactInfo().getAddress();
        String[] dp = addr.getDeliveryPoint();
        if ( dp.length > 0 ) {
            XMLTools.appendElement( caNode, null, "Address", dp[0] );
        }
        if ( addr.getCity() != null ) {
            XMLTools.appendElement( caNode, null, "City", addr.getCity() );
        }
        if ( addr.getAdministrativeArea() != null ) {
            XMLTools.appendElement( caNode, null, "StateOrProvince", addr.getAdministrativeArea() );
        }
        if ( addr.getPostalCode() != null ) {
            XMLTools.appendElement( caNode, null, "PostCode", addr.getPostalCode() );
        }
        if ( addr.getCountry() != null ) {
            XMLTools.appendElement( caNode, null, "Country", addr.getCountry() );
        }
        Phone phone = provider.getContactInfo().getPhone();
        if ( phone.getVoice().length > 0 ) {
            XMLTools.appendElement( ciNode, null, "ContactVoiceTelephone", phone.getVoice()[0] );
        }
        if ( phone.getFacsimile().length > 0 ) {
            XMLTools.appendElement( ciNode, null, "ContactFacsimileTelephone", phone.getFacsimile()[0] );
        }
        if ( addr.getElectronicMailAddress().length > 0 ) {
            XMLTools.appendElement( ciNode, null, "ContactElectronicMailAddress", addr.getElectronicMailAddress()[0] );
        }

    }

    /**
     * Append capability element.
     *
     * @param root
     * @param operationsMetadata
     * @throws XMLParsingException
     */
    protected static void appendCapabilityRequests( Element root, WMPSOperationsMetadata operationsMetadata )
                            throws XMLParsingException {

        root = (Element) XMLTools.getRequiredNode( root, "./Capability/Request", nsContext );

        Operation[] ops = operationsMetadata.getOperations();
        for ( int i = 0; i < ops.length; i++ ) {
            if ( ops[i] != null ) {
                appendOperation( root, ops[i] );
            }
        }

    }

    /**
     * Append Operations.
     *
     * @param root
     * @param operation
     */
    protected static void appendOperation( Element root, Operation operation ) {

        String name = operation.getName();
        root = XMLTools.appendElement( root, null, name );

        OWSDomainType odt = operation.getParameter( "Format" );
        String[] values = odt.getValues();
        for ( int i = 0; i < values.length; i++ ) {
            XMLTools.appendElement( root, null, "Format", values[i] );
        }

        DCPType[] dcps = operation.getDCPs();
        for ( int i = 0; i < dcps.length; i++ ) {
            Element http = XMLTools.appendElement( root, null, "DCPType" );
            http = XMLTools.appendElement( http, null, "HTTP" );
            HTTP ht = (HTTP) dcps[i].getProtocol();
            URL[] urls = ht.getGetOnlineResources();
            appendURLs( http, urls, "Get" );
            urls = ht.getPostOnlineResources();
            appendURLs( http, urls, "Post" );
        }

    }

    /**
     * Append URLs
     *
     * @param http
     * @param urls
     * @param type
     */
    protected static void appendURLs( Element http, URL[] urls, String type ) {
        for ( int j = 0; j < urls.length; j++ ) {
            Element olr = XMLTools.appendElement( http, null, type );
            appendOnlineResource( olr, urls[j] );
        }
    }

    /**
     * Append capability layer
     *
     * @param root
     * @param layer
     * @throws XMLParsingException
     */
    protected static void appendCapabilityLayer( Element root, Layer layer )
                            throws XMLParsingException {

        root = XMLTools.appendElement( root, null, "Layer" );
        root.setAttribute( "queryable", Boolean.toString( layer.isQueryable() ) );
        root.setAttribute( "cascaded", Integer.toString( layer.getCascaded() ) );
        root.setAttribute( "opaque", Boolean.toString( layer.isOpaque() ) );
        root.setAttribute( "noSubsets", Boolean.toString( layer.hasNoSubsets() ) );
        if ( layer.getFixedWidth() > 0 ) {
            root.setAttribute( "fixedWidth", Integer.toString( layer.getFixedWidth() ) );
        }
        if ( layer.getFixedHeight() > 0 ) {
            root.setAttribute( "fixedHeight", Integer.toString( layer.getFixedHeight() ) );
        }

        if ( layer.getName() != null ) {
            XMLTools.appendElement( root, null, "Name", layer.getName() );
        }
        XMLTools.appendElement( root, null, "Title", layer.getTitle() );

        if ( layer.getAbstract() != null ) {
            XMLTools.appendElement( root, null, "Abstract", layer.getAbstract() );
        }

        String[] keywords = layer.getKeywordList();
        if ( keywords.length > 0 ) {
            Element elem = XMLTools.appendElement( root, null, "KeywordList" );
            for ( int i = 0; i < keywords.length; i++ ) {
                XMLTools.appendElement( elem, null, "Keyword", keywords[i] );
            }
        }

        String[] srs = layer.getSrs();
        for ( int i = 0; i < srs.length; i++ ) {
            XMLTools.appendElement( root, null, "SRS", srs[i] );
        }

        Envelope llBox = layer.getLatLonBoundingBox();
        appendLatLonBoundingBox( root, llBox );

        LayerBoundingBox[] lBoxes = layer.getBoundingBoxes();
        for ( int i = 0; i < lBoxes.length; i++ ) {
            appendLayerBoundingBox( root, lBoxes[i] );
        }

        Dimension[] dims = layer.getDimension();
        for ( int i = 0; i < dims.length; i++ ) {
            appendDimension( root, dims[i] );
        }

        Extent[] extents = layer.getExtent();
        for ( int i = 0; i < extents.length; i++ ) {
            appendExtent( root, extents[i] );
        }

        Attribution attr = layer.getAttribution();
        if ( attr != null ) {
            appendAttribution( root, attr );
        }

        AuthorityURL[] authorityURLs = layer.getAuthorityURL();
        for ( int i = 0; i < authorityURLs.length; i++ ) {
            appendAuthorityURL( root, authorityURLs[i] );
        }

        Identifier[] identifiers = layer.getIdentifier();
        for ( int i = 0; i < identifiers.length; i++ ) {
            appendIdentifier( root, identifiers[i] );
        }

        MetadataURL[] metadataURLs = layer.getMetadataURL();
        for ( int i = 0; i < metadataURLs.length; i++ ) {
            appendMetadataURL( root, metadataURLs[i] );
        }

        DataURL[] dataURLs = layer.getDataURL();
        for ( int i = 0; i < dataURLs.length; i++ ) {
            appendDataURL( root, dataURLs[i] );
        }

        FeatureListURL[] featureListURLs = layer.getFeatureListURL();
        for ( int i = 0; i < featureListURLs.length; i++ ) {
            appendFeatureListURL( root, featureListURLs[i] );
        }

        Style[] styles = layer.getStyles();
        for ( int i = 0; i < styles.length; i++ ) {
            appendStyle( root, styles[i] );
        }

        ScaleHint scaleHint = layer.getScaleHint();
        Element elem = XMLTools.appendElement( root, null, "ScaleHint" );
        elem.setAttribute( "min", "" + scaleHint.getMin() );
        elem.setAttribute( "max", "" + scaleHint.getMax() );

        Layer[] layers = layer.getLayer();
        for ( int i = 0; i < layers.length; i++ ) {
            appendCapabilityLayer( root, layers[i] );
        }

    }

    /**
     * Append style
     *
     * @param root
     * @param style
     */
    protected static void appendStyle( Element root, Style style ) {

        root = XMLTools.appendElement( root, null, "Style" );
        XMLTools.appendElement( root, null, "Name", style.getName() );
        if ( style.getTitle() != null ) {
            XMLTools.appendElement( root, null, "Title", style.getTitle() );
        }
        if ( style.getAbstract() != null ) {
            XMLTools.appendElement( root, null, "Abstract", style.getAbstract() );
        }
        LegendURL[] legendURLs = style.getLegendURL();
        for ( int i = 0; i < legendURLs.length; i++ ) {
            appendLegendURL( root, legendURLs[i] );
        }

        StyleSheetURL styleSheetURL = style.getStyleSheetURL();
        if ( styleSheetURL != null ) {
            appendStyleSheetURL( root, styleSheetURL );
        }

        StyleURL styleURL = style.getStyleURL();
        if ( styleURL != null ) {
            appendStyleURL( root, styleURL );
        }

    }

    /**
     * Append Style URL
     *
     * @param root
     * @param styleURL
     *
     */
    protected static void appendStyleURL( Element root, StyleURL styleURL ) {
        Element elem = XMLTools.appendElement( root, null, "StyleURL" );
        XMLTools.appendElement( elem, null, "Format", styleURL.getFormat() );
        appendOnlineResource( elem, styleURL.getOnlineResource() );
    }

    /**
     * Append Style sheet.
     *
     * @param root
     * @param styleSheetURL
     */
    protected static void appendStyleSheetURL( Element root, StyleSheetURL styleSheetURL ) {
        Element elem = XMLTools.appendElement( root, null, "StyleSheetURL" );
        XMLTools.appendElement( elem, null, "Format", styleSheetURL.getFormat() );
        appendOnlineResource( elem, styleSheetURL.getOnlineResource() );
    }

    /**
     * Append legend url.
     *
     * @param root
     * @param legendURL
     */
    protected static void appendLegendURL( Element root, LegendURL legendURL ) {
        Element elem = XMLTools.appendElement( root, null, "LegendURL" );
        elem.setAttribute( "width", "" + legendURL.getWidth() );
        elem.setAttribute( "height", "" + legendURL.getWidth() );
        XMLTools.appendElement( elem, null, "Format", legendURL.getFormat() );

        appendOnlineResource( elem, legendURL.getOnlineResource() );
    }

    /**
     * Append feature list url.
     *
     * @param root
     * @param featureListURL
     */
    protected static void appendFeatureListURL( Element root, FeatureListURL featureListURL ) {
        Element elem = XMLTools.appendElement( root, null, "FeatureListURL" );
        XMLTools.appendElement( elem, null, "Format", featureListURL.getFormat() );
        appendOnlineResource( elem, featureListURL.getOnlineResource() );
    }

    /**
     * Append data url.
     *
     * @param root
     * @param dataURL
     */
    protected static void appendDataURL( Element root, DataURL dataURL ) {
        Element elem = XMLTools.appendElement( root, null, "DataURL" );
        XMLTools.appendElement( elem, null, "Format", dataURL.getFormat() );
        appendOnlineResource( elem, dataURL.getOnlineResource() );
    }

    /**
     * Append metadata url.
     *
     * @param root
     * @param metadataURL
     */
    protected static void appendMetadataURL( Element root, MetadataURL metadataURL ) {
        Element elem = XMLTools.appendElement( root, null, "MetadataURL" );
        elem.setAttribute( "type", metadataURL.getType() );
        XMLTools.appendElement( elem, null, "Format", metadataURL.getFormat() );
        appendOnlineResource( elem, metadataURL.getOnlineResource() );
    }

    /**
     * Append identifiers.
     *
     * @param root
     * @param identifier
     */
    protected static void appendIdentifier( Element root, Identifier identifier ) {
        Element elem = XMLTools.appendElement( root, null, "Identifier" );
        elem.setAttribute( "authority", identifier.getAuthority() );
        XMLTools.setNodeValue( elem, identifier.getValue() );
    }

    /**
     * Append authority url.
     *
     * @param root
     * @param authorityURL
     */
    protected static void appendAuthorityURL( Element root, AuthorityURL authorityURL ) {
        Element elem = XMLTools.appendElement( root, null, "AuthorityURL" );
        elem.setAttribute( "name", authorityURL.getName() );
        appendOnlineResource( elem, authorityURL.getOnlineResource() );
    }

    /**
     * Append attribution url.
     *
     * @param root
     * @param attr
     */
    protected static void appendAttribution( Element root, Attribution attr ) {
        Element elem = XMLTools.appendElement( root, null, "Attribution" );
        XMLTools.appendElement( elem, null, "Title", attr.getTitle() );
        appendOnlineResource( elem, attr.getOnlineResource() );
        LogoURL logoURL = attr.getLogoURL();
        if ( logoURL != null ) {
            elem = XMLTools.appendElement( elem, null, "LogoURL" );
            elem.setAttribute( "width", "" + logoURL.getWidth() );
            elem.setAttribute( "height", "" + logoURL.getHeight() );
            XMLTools.appendElement( elem, null, "Format", logoURL.getFormat() );
            appendOnlineResource( elem, logoURL.getOnlineResource() );
        }
    }

    /**
     * Append online resource.
     *
     * @param root
     * @param url
     */
    protected static void appendOnlineResource( Element root, URL url ) {
        Element olr = XMLTools.appendElement( root, null, "OnlineResource" );
        olr.setAttribute( "xlink:type", "simple" );
        olr.setAttribute( "xlink:href", url.toExternalForm() );
    }

    /**
     * Apppend extent.
     *
     * @param root
     * @param extent
     */
    protected static void appendExtent( Element root, Extent extent ) {
        Element exNode = XMLTools.appendElement( root, null, "Extent" );
        exNode.setAttribute( "name", extent.getName() );
        exNode.setAttribute( "default", extent.getDefault() );
        exNode.setAttribute( "nearestValue", Boolean.toString( extent.useNearestValue() ) );
        XMLTools.setNodeValue( exNode, extent.getValue() );
    }

    /**
     * Append dimension.
     *
     * @param root
     * @param dim
     */
    protected static void appendDimension( Element root, Dimension dim ) {
        Element dimNode = XMLTools.appendElement( root, null, "Dimension" );
        dimNode.setAttribute( "name", dim.getName() );
        dimNode.setAttribute( "units", dim.getUnits() );
        dimNode.setAttribute( "unitSymbol", dim.getUnitSymbol() );
    }

    /**
     * Append layer bounding box.
     *
     * @param root
     * @param lBox
     */
    protected static void appendLayerBoundingBox( Element root, LayerBoundingBox lBox ) {
        Element bbNode = XMLTools.appendElement( root, null, "BoundingBox" );
        bbNode.setAttribute( "minx", "" + lBox.getMin().getX() );
        bbNode.setAttribute( "miny", "" + lBox.getMin().getY() );
        bbNode.setAttribute( "maxx", "" + lBox.getMax().getX() );
        bbNode.setAttribute( "maxy", "" + lBox.getMax().getY() );
        bbNode.setAttribute( "resx", "" + lBox.getResx() );
        bbNode.setAttribute( "resy", "" + lBox.getResy() );
        bbNode.setAttribute( "SRS", "" + lBox.getSRS() );
    }

    /**
     * Append lat-lon bounding box.
     *
     * @param root
     * @param llBox
     */
    protected static void appendLatLonBoundingBox( Element root, Envelope llBox ) {
        Element bbNode = XMLTools.appendElement( root, null, "LatLonBoundingBox" );
        bbNode.setAttribute( "minx", "" + llBox.getMin().getX() );
        bbNode.setAttribute( "miny", "" + llBox.getMin().getY() );
        bbNode.setAttribute( "maxx", "" + llBox.getMax().getX() );
        bbNode.setAttribute( "maxy", "" + llBox.getMax().getY() );
    }

    /**
     * Export the print map initial response document.
     *
     * @param response
     * @return PrintMapResponseDocument
     * @throws XMLParsingException
     *
     */
    public static PrintMapResponseDocument export( PrintMapResponse response )
                            throws XMLParsingException {

        PrintMapResponseDocument document = new PrintMapResponseDocument( null );
        try {
            document.createEmptyDocument();
            Element root = document.getRootElement();
            root.setAttribute( "id", response.getId() );
            appendEmailAddress( root, response.getEmailAddress() );
            appendTimeStamp( root, response.getTimeStamp() );
            String exception = response.getException();
            String message = response.getMessage();
            if ( exception != null ) {
                message = message + " " + exception;
            }
            appendMessage( root, message );
            appendExpectedTime( root, response.getExpectedTime() );
        } catch ( SAXException e ) {
            LOG.logError( e.getMessage(), e );
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
        }

        return document;

    }
    
    /**
     * Export the print map initial response document.
     *
     * @param response
     * @return GetAvailableTemplatesResponseDocument
     * @throws XMLParsingException
     *
     */
    public static GetAvailableTemplatesResponseDocument export( GetAvailableTemplatesResponse response )
                            throws XMLParsingException {

        GetAvailableTemplatesResponseDocument document = new GetAvailableTemplatesResponseDocument( null );
        try {
            document.createEmptyDocument();
            Element root = document.getRootElement();
            
            List<String> list = response.getTemplates();
            for ( String template : list ) {
                Element tmplNode = XMLTools.appendElement( root, CommonNamespaces.DEEGREEWMPS, "Template" );
                XMLTools.setNodeValue( tmplNode, template );
            }
            
        } catch ( SAXException e ) {
            LOG.logError( e.getMessage(), e );
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
        }

        return document;

    }
    
    /**
     * @param response
     * @return DescribeTemplateResponse as XML document
     */
    public static Object export( DescribeTemplateResponse response ) {
        DescribeTemplateResponseDocument document = new DescribeTemplateResponseDocument( null );
        try {
            document.createEmptyDocument();
            Element root = document.getRootElement();
            
            List<Pair<String, String>> list = response.getParamter();
            for ( Pair<String, String> pair : list ) {                
                Element tmplNode = XMLTools.appendElement( root, CommonNamespaces.DEEGREEWMPS, "Parameter" );
                tmplNode.setAttribute( "name", pair.first );
                tmplNode.setAttribute( "type", pair.second );
            }
            
        } catch ( SAXException e ) {
            LOG.logError( e.getMessage(), e );
        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
        }

        return document;

    }

    /**
     * Append email address.
     *
     * @param root
     * @param emailAddress
     * @throws XMLParsingException
     */
    private static void appendEmailAddress( Element root, String emailAddress )
                            throws XMLParsingException {

        Node node;
        try {
            node = XMLTools.getRequiredNode( root, "deegreewmps:EmailAddress", nsContext );
        } catch ( XMLParsingException e ) {
            throw new XMLParsingException( "Error getting node 'deegreewmps:EmailAddress'. "
                                           + "Please check the WMPSInitialResponseTemplate "
                                           + "to confirm its presence." );
        }
        XMLTools.setNodeValue( (Element) node, emailAddress );

    }

    /**
     * Append expected processing time.
     *
     * @param root
     * @param expectedTime
     * @throws XMLParsingException
     */
    private static void appendExpectedTime( Element root, Date expectedTime )
                            throws XMLParsingException {

        Node node;
        try {
            node = XMLTools.getRequiredNode( root, "deegreewmps:ExpectedProcessingTime", nsContext );
        } catch ( XMLParsingException e ) {
            throw new XMLParsingException( "Error getting node " + "'deegreewmps:expectedProcessingTime'. "
                                           + "Please check the WMPSInitialResponseTemplate "
                                           + "to confirm its presence." );

        }
        XMLTools.setNodeValue( (Element) node, expectedTime.toString() );

    }

    /**
     * Append message to be displayed to the user.
     *
     * @param root
     * @param message
     * @throws XMLParsingException
     */
    private static void appendMessage( Element root, String message )
                            throws XMLParsingException {

        Node node;
        try {
            node = XMLTools.getRequiredNode( root, "deegreewmps:Message", nsContext );
        } catch ( XMLParsingException e ) {
            throw new XMLParsingException( "Error getting node 'deegreewmps:message'. "
                                           + "Please check the WMPSInitialResponseTemplate "
                                           + "to confirm its presence." );

        }
        XMLTools.setNodeValue( (Element) node, message );

    }

    /**
     * Append time stamp.
     *
     * @param root
     * @param timeStamp
     * @throws XMLParsingException
     */
    private static void appendTimeStamp( Element root, Date timeStamp )
                            throws XMLParsingException {

        Node node;
        try {
            node = XMLTools.getRequiredNode( root, "deegreewmps:Timestamp", nsContext );
        } catch ( XMLParsingException e ) {
            throw new XMLParsingException( "Error getting node 'deegreewmps:timestamp'. "
                                           + "Please check the WMPSInitialResponseTemplate "
                                           + "to confirm its presence." );
        }
        XMLTools.setNodeValue( (Element) node, timeStamp.toString() );

    }
   

}

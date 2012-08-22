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
package org.deegree.ogcwebservices.wms.configuration;

import static org.deegree.framework.xml.XMLTools.getElements;
import static org.deegree.framework.xml.XMLTools.getNodeAsBoolean;

import java.awt.Color;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.TransformerException;

import org.deegree.datatypes.QualifiedName;
import org.deegree.enterprise.Proxy;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.BootLogger;
import org.deegree.framework.util.IDGenerator;
import org.deegree.framework.util.KVP2Map;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.InvalidConfigurationException;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.framework.xml.XSLTDocument;
import org.deegree.i18n.Messages;
import org.deegree.io.IODocument;
import org.deegree.io.JDBCConnection;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.MetadataURL;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.wcs.RemoteWCService;
import org.deegree.ogcwebservices.wcs.WCService;
import org.deegree.ogcwebservices.wcs.configuration.WCSConfiguration;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSCapabilities;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSCapabilitiesDocument;
import org.deegree.ogcwebservices.wcs.getcoverage.GetCoverage;
import org.deegree.ogcwebservices.wfs.RemoteWFService;
import org.deegree.ogcwebservices.wfs.WFServiceFactory;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.configuration.WFSConfiguration;
import org.deegree.ogcwebservices.wfs.configuration.WFSConfigurationDocument;
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.deegree.ogcwebservices.wms.RemoteWMService;
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
import org.deegree.ogcwebservices.wms.capabilities.ScaleHint;
import org.deegree.ogcwebservices.wms.capabilities.Style;
import org.deegree.ogcwebservices.wms.capabilities.StyleSheetURL;
import org.deegree.ogcwebservices.wms.capabilities.StyleURL;
import org.deegree.ogcwebservices.wms.capabilities.UserDefinedSymbolization;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocument;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilitiesDocumentFactory;
import org.deegree.ogcwebservices.wms.dataaccess.ExternalDataAccess;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.owscommon_new.OperationsMetadata;
import org.deegree.owscommon_new.ServiceIdentification;
import org.deegree.owscommon_new.ServiceProvider;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * <code>WMSConfigurationDocument</code> is the parser class for a standard 1.1.1 WMS configuration document, ie, a
 * capabilities document enriched by deegree parameters.
 * 
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version 2.0, $Revision$, $Date$
 * 
 * @since 2.0
 */
public class WMSConfigurationDocument extends WMSCapabilitiesDocument {

    private static final long serialVersionUID = 2320990982989322325L;

    protected static final URI DEEGREEWMSNS = CommonNamespaces.DEEGREEWMS;

    private static final String XML_TEMPLATE = "WMSConfigurationTemplate.xml";

    private static final String XSLT_TEMPLATE_NAME = "WMSConfigurationTransform.xsl";

    private static XSLTDocument XSLT_TEMPLATE;

    private static Map<URL, OGCCapabilities> capaCache = new HashMap<URL, OGCCapabilities>();

    private static final ILogger LOG = LoggerFactory.getLogger( WMSConfigurationDocument.class );

    private static final QualifiedName DEFAULT_GEO_PROP = new QualifiedName(
                                                                             "app",
                                                                             "GEOM",
                                                                             CommonNamespaces.buildNSURI( "http://www.deegree.org/app" ) );

    static {
        XSLT_TEMPLATE = new XSLTDocument();
        try {
            XSLT_TEMPLATE.load( WMSConfigurationDocument.class.getResource( XSLT_TEMPLATE_NAME ) );
        } catch ( Exception e ) {
            BootLogger.logError( "Error loading XSLT sheet in WMSConfigurationDocument.", e );
        }
    }

    /**
     *
     */
    public static void resetCapabilitiesCache() {
        capaCache.clear();
    }

    /**
     * Creates a skeleton capabilities document that contains the mandatory elements only.
     * 
     * @throws IOException
     * @throws SAXException
     */
    @Override
    public void createEmptyDocument()
                            throws IOException, SAXException {
        URL url = WMSConfigurationDocument.class.getResource( XML_TEMPLATE );
        if ( url == null ) {
            throw new IOException( "The resource '" + XML_TEMPLATE + " could not be found." );
        }
        load( url );
    }

    /**
     * Creates a class representation of the document.
     * 
     * @return class representation of the configuration document
     * @throws InvalidConfigurationException
     */
    public WMSConfiguration parseConfiguration()
                            throws InvalidConfigurationException {

        try {
            // transform document to fill missing elements and attributes with
            // default values
            XMLFragment frag = XSLT_TEMPLATE.transform( this );
            this.setRootElement( frag.getRootElement() );
            if ( LOG.isDebug() ) {
                LOG.logDebug( "Transformed WMS configuration document", getAsPrettyString() );
            }
        } catch ( TransformerException e ) {
            String msg = Messages.getMessage( "WMS_CONFIGURATION_TRANSFORM" );
            LOG.logError( msg, e );
            throw new InvalidConfigurationException( msg, e );
        }

        ServiceIdentification serviceIdentification = null;
        ServiceProvider serviceProvider = null;
        OperationsMetadata metadata = null;
        Layer layer = null;
        UserDefinedSymbolization uds = null;
        WMSDeegreeParams params = null;
        Element root = getRootElement();
        String version = root.getAttribute( "version" );
        String updateSeq = root.getAttribute( "updateSequence" );
        List<String> exceptions;

        try {
            Node node = XMLTools.getRequiredNode( getRootElement(), "./deegreewms:DeegreeParam", nsContext );
            params = parseDeegreeParams( node );

            serviceIdentification = parseServiceIdentification();
            serviceProvider = parseServiceProvider();
            metadata = parseOperationsMetadata();

            Element exceptionElement = XMLTools.getRequiredElement( getRootElement(), "Capability/Exception", nsContext );
            exceptions = parseExceptionFormats( exceptionElement );

            uds = parseUserDefinedSymbolization();
            Element layerElem = (Element) XMLTools.getRequiredNode( getRootElement(), "./Capability/Layer", nsContext );
            layer = parseLayers( layerElem, null, null );

        } catch ( XMLParsingException e ) {
            e.printStackTrace();
            throw new InvalidConfigurationException( e.getMessage() + StringTools.stackTraceToString( e ) );
        } catch ( MalformedURLException e ) {
            throw new InvalidConfigurationException( e.getMessage() + " - " + StringTools.stackTraceToString( e ) );
        } catch ( UnknownCRSException e ) {
            throw new InvalidConfigurationException( e.getMessage() + " - " + StringTools.stackTraceToString( e ) );
        }

        WMSConfiguration wmsConfiguration = new WMSConfiguration( version, updateSeq, serviceIdentification,
                                                                  serviceProvider, uds, metadata, layer, params,
                                                                  getSystemId(), exceptions );

        return wmsConfiguration;
    }

    /**
     * Creates a class representation of the <code>deegreeParams</code>- section.
     * 
     * @param root
     * 
     * @return the deegree params
     * @throws XMLParsingException
     * @throws MalformedURLException
     */
    public WMSDeegreeParams parseDeegreeParams( Node root )
                            throws XMLParsingException, MalformedURLException {

        Element elem = (Element) XMLTools.getRequiredNode( root, "./deegreewms:DefaultOnlineResource", nsContext );
        OnlineResource ol = parseOnLineResource( elem );
        int cache = XMLTools.getNodeAsInt( root, "./deegreewms:CacheSize", nsContext, 100 );
        int maxLifeTime = XMLTools.getNodeAsInt( root, "./deegreewms:MaxLifeTime", nsContext, 3600 );
        int reqTimeLimit = XMLTools.getNodeAsInt( root, "./deegreewms:RequestTimeLimit", nsContext, 15 );
        reqTimeLimit *= 1000;
        double mapQuality = XMLTools.getNodeAsDouble( root, "./deegreewms:MapQuality", nsContext, 0.95 );
        int maxMapWidth = XMLTools.getNodeAsInt( root, "./deegreewms:MaxMapWidth", nsContext, 1000 );
        int maxMapHeight = XMLTools.getNodeAsInt( root, "./deegreewms:MaxMapHeight", nsContext, 1000 );
        int featureInfoRadius = XMLTools.getNodeAsInt( root, "./deegreewms:FeatureInfoRadius", nsContext, 5 );
        String copyright = XMLTools.getNodeAsString( root, "./deegreewms:Copyright", nsContext, "" );
        String defaultPNGFormat = XMLTools.getNodeAsString( root, "deegreewms:DefaultPNGFormat", nsContext,
                                                            "image/png; mode=24bit" );

        URL dtdLocation = null;
        if ( XMLTools.getNode( root, "deegreewms:DTDLocation", nsContext ) != null ) {
            elem = (Element) XMLTools.getRequiredNode( root, "./deegreewms:DTDLocation/deegreewms:OnlineResource",
                                                       nsContext );
            OnlineResource olr = parseOnLineResource( elem );
            dtdLocation = olr.getLinkage().getHref();
        } else {
            dtdLocation = new URL( "http://schemas.opengis.net/wms/1.1.1/WMS_MS_Capabilities.dtd" );
        }

        URL featureSchemaLocation = null;
        String featureSchemaNamespace = null;
        if ( XMLTools.getNode( root, "deegreewms:FeatureInfoSchema", nsContext ) != null ) {
            featureSchemaNamespace = XMLTools.getRequiredNodeAsString(
                                                                       root,
                                                                       "deegreewms:FeatureInfoSchema/deegreewms:Namespace",
                                                                       nsContext );
            elem = (Element) XMLTools.getRequiredNode( root, "deegreewms:FeatureInfoSchema/deegreewms:OnlineResource",
                                                       nsContext );
            OnlineResource link = parseOnLineResource( elem );
            featureSchemaLocation = link.getLinkage().getHref();
        }

        boolean antiAliased = XMLTools.getNodeAsBoolean( root, "./deegreewms:AntiAliased", nsContext, true );

        boolean filtersAllowed = getNodeAsBoolean( root, "./deegreewms:FiltersAllowed", nsContext, false );

        Proxy proxy = parseProxy( root );

        List<String> supportedVersions = parseSupportedVersions( root );

        WMSDeegreeParams deegreeParams = new WMSDeegreeParams( cache, maxLifeTime, reqTimeLimit, (float) mapQuality,
                                                               ol, maxMapWidth, maxMapHeight, antiAliased,
                                                               featureInfoRadius, copyright, null, dtdLocation, proxy,
                                                               supportedVersions, featureSchemaLocation,
                                                               featureSchemaNamespace, filtersAllowed, defaultPNGFormat );

        return deegreeParams;
    }

    // returns the list of supported versions
    private List<String> parseSupportedVersions( Node root )
                            throws XMLParsingException {

        String[] versions = XMLTools.getNodesAsStrings( root, "./deegreewms:SupportedVersion", nsContext );

        if ( versions != null )
            return Arrays.asList( versions );

        return new ArrayList<String>();

    }

    /**
     * @param root
     * @return the proxy
     * @throws XMLParsingException
     */
    private Proxy parseProxy( Node root )
                            throws XMLParsingException {

        Proxy proxy = null;
        Node pro = XMLTools.getNode( root, "./deegreewms:Proxy", nsContext );
        if ( pro != null ) {
            String proxyHost = XMLTools.getRequiredNodeAsString( pro, "./@proxyHost", nsContext );
            String proxyPort = XMLTools.getRequiredNodeAsString( pro, "./@proxyPort", nsContext );
            proxy = new Proxy( proxyHost, proxyPort );
        }

        return proxy;
    }

    /**
     * returns the layers offered by the WMS
     * 
     * @throws XMLParsingException
     * @throws UnknownCRSException
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

        Element llBox = (Element) XMLTools.getNode( layerElem, "./LatLonBoundingBox", nsContext );
        Envelope llBoundingBox = null;
        if ( llBox == null && parent != null ) {
            // inherit LatLonBoundingBox parent layer
            llBoundingBox = parent.getLatLonBoundingBox();
        } else {
            llBoundingBox = parseLatLonBoundingBox( llBox );
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

        AbstractDataSource[] ds = parseDataSources( layerElem, name, scaleHint );

        Layer layer = new Layer( queryable, cascaded, opaque, noSubsets, fixedWidth, fixedHeight, name, title,
                                 layerAbstract, llBoundingBox, attribution, scaleHint, keywords, srs, bboxes,
                                 dimensions, extents, authorityURLs, identifiers, metadataURLs, dataURLs,
                                 featureListURLs, styles, null, ds, parent );

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

    @Override
    protected Dimension[] parseDimensions( Element layerElem )
                            throws XMLParsingException {
        // it's a config document and uses extended 1.3.0 style dimension configuration
        List<Element> nl = getElements( layerElem, "Dimension", nsContext );
        Dimension[] dimensions = new Dimension[nl.size()];

        for ( int i = 0; i < dimensions.length; i++ ) {
            dimensions[i] = new Dimension( nl.get( i ) );
        }

        return dimensions;
    }

    /**
     * 
     * @param layerElem
     * @return the data sources
     * @throws XMLParsingException
     */
    protected AbstractDataSource[] parseDataSources( Element layerElem, String layerName, ScaleHint scaleHint )
                            throws XMLParsingException {

        List<Node> nl = XMLTools.getNodes( layerElem, "./deegreewms:DataSource", nsContext );

        AbstractDataSource[] ds = new AbstractDataSource[nl.size()];
        for ( int i = 0; i < ds.length; i++ ) {
            boolean failOnEx = XMLTools.getNodeAsBoolean( nl.get( i ), "./@failOnException", nsContext, true );
            boolean queryable = XMLTools.getNodeAsBoolean( nl.get( i ), "./@queryable", nsContext, false );
            QualifiedName name = XMLTools.getNodeAsQualifiedName( nl.get( i ), "./deegreewms:Name/text()", nsContext,
                                                                  new QualifiedName( layerName ) );
            String stype = XMLTools.getRequiredNodeAsString( nl.get( i ), "./deegreewms:Type", nsContext );

            int reqTimeLimit = XMLTools.getNodeAsInt( nl.get( i ), "./deegreewms:RequestTimeLimit/text()", nsContext,
                                                      30 );

            scaleHint = parseDSScaleHint( (Element) nl.get( i ), scaleHint );

            String s = "./deegreewms:OWSCapabilities/deegreewms:OnlineResource";
            Node node = XMLTools.getRequiredNode( nl.get( i ), s, nsContext );

            URL url = parseOnLineResource( (Element) node ).getLinkage().getHref();

            Geometry validArea = parseValidArea( nl.get( i ) );
            try {
                if ( "LOCALWFS".equals( stype ) ) {
                    ds[i] = createLocalWFSDataSource( nl.get( i ), failOnEx, queryable, name, url, scaleHint,
                                                      validArea, reqTimeLimit );
                } else if ( "LOCALWCS".equals( stype ) ) {
                    ds[i] = createLocalWCSDataSource( nl.get( i ), failOnEx, queryable, name, url, scaleHint,
                                                      validArea, reqTimeLimit );
                } else if ( "REMOTEWFS".equals( stype ) ) {
                    ds[i] = createRemoteWFSDataSource( nl.get( i ), failOnEx, queryable, name, url, scaleHint,
                                                       validArea, reqTimeLimit );
                } else if ( "REMOTEWCS".equals( stype ) ) {
                    ds[i] = createRemoteWCSDataSource( nl.get( i ), failOnEx, queryable, name, url, scaleHint,
                                                       validArea, reqTimeLimit );
                } else if ( "REMOTEWMS".equals( stype ) ) {
                    ds[i] = createRemoteWMSDataSource( nl.get( i ), failOnEx, queryable, name, url, scaleHint,
                                                       validArea, reqTimeLimit );
                } else if ( "DATABASE".equals( stype ) ) {
                    ds[i] = createDatabaseSource( nl.get( i ), failOnEx, queryable, name, scaleHint, validArea,
                                                  reqTimeLimit );
                } else if ( "EXTERNALDATAACCESS".equals( stype ) ) {
                    ds[i] = createExternalDataAccess( nl.get( i ), failOnEx, queryable, name, scaleHint, validArea,
                                                      reqTimeLimit );
                } else {
                    throw new XMLParsingException( "invalid DataSource type: " + stype + " defined "
                                                   + "in deegree WMS configuration for DataSource: " + name );
                }
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
                throw new XMLParsingException( "could not create service instance for WMS " + "datasource: " + name, e );
            }
        }

        return ds;
    }

    /**
     * @param node
     * @param failOnEx
     * @param queryable
     * @param name
     * @param scaleHint
     * @param validArea
     * @param reqTimeLimit
     */
    private AbstractDataSource createExternalDataAccess( Node node, boolean failOnEx, boolean queryable,
                                                         QualifiedName name, ScaleHint scaleHint, Geometry validArea,
                                                         int reqTimeLimit )
                            throws Exception {
        String className = XMLTools.getRequiredNodeAsString( node, "./deegreewms:ClassName/text()", nsContext );
        String confPath = XMLTools.getNodeAsString( node, "./deegreewms:ConfigurationFile/text()", nsContext, null );
        ExternalDataAccess externalDataAccess = (ExternalDataAccess) Class.forName( className ).newInstance();
        URL confURL = null;
        if ( confPath != null ) {
            confURL = resolve( confPath );
        }
        externalDataAccess.setConfigurationFile( confURL );

        return new ExternalDataAccessDataSource( queryable, failOnEx, name, scaleHint, validArea, reqTimeLimit,
                                                 externalDataAccess );
    }

    /**
     * parses the ScaleHint for a data source
     * 
     * @param layerElem
     * @param scaleHint
     * @return the scale hint for the data source
     * @throws XMLParsingException
     */
    protected ScaleHint parseDSScaleHint( Element layerElem, ScaleHint scaleHint )
                            throws XMLParsingException {

        Node scNode = XMLTools.getNode( layerElem, "./deegreewms:ScaleHint", nsContext );
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
     * returns the area a data source is valid. If the optional element <ValidArea>is not defined in the configuration
     * <code>null</code>.
     * 
     * @param node
     * @return the geometry
     */
    private Geometry parseValidArea( Node node )
                            throws XMLParsingException {

        Geometry geom = null;

        List<Node> nl = XMLTools.getNodes( node, "./deegreewms:ValidArea/*", nsContext );
        if ( node != null ) {

            try {
                for ( int i = 0; i < nl.size(); i++ ) {

                    if ( nl.get( 0 ).getNamespaceURI().equals( GMLNS.toString() ) ) {

                        geom = GMLGeometryAdapter.wrap( (Element) nl.get( 0 ), null );
                        break;
                    }
                }
            } catch ( GeometryException e ) {
                e.printStackTrace();
                throw new XMLParsingException( "couldn't parse/create valid aera of a datasource", e );
            }
        }

        return geom;
    }

    /**
     * @param vendor
     * @return the parsed list
     * @throws XMLParsingException
     */
    static List<String> parsePassedParameters( Node vendor )
                            throws XMLParsingException {
        List<String> passedParameters = new ArrayList<String>( 10 );

        if ( vendor == null ) {
            return passedParameters;
        }

        List<Node> nl = XMLTools.getNodes( vendor, "deegreewms:PassedVendorspecificParameter/deegreewms:Name",
                                           nsContext );

        for ( Object obj : nl ) {
            passedParameters.add( ( (Node) obj ).getTextContent().toUpperCase() );
        }

        return passedParameters;
    }

    /**
     * @param vendor
     * @return the parsed map
     * @throws XMLParsingException
     */
    static Map<String, String> parseAddedParameters( Node vendor )
                            throws XMLParsingException {
        Map<String, String> addedParameters = new HashMap<String, String>( 10 );

        if ( vendor == null ) {
            return addedParameters;
        }

        List<Node> nl = XMLTools.getNodes(
                                           vendor,
                                           "deegreewms:AddedVendorspecificParameter/deegreewms:VendorspecificParameter",
                                           nsContext );

        for ( Object obj : nl ) {
            String pName = XMLTools.getRequiredNodeAsString( (Node) obj, "deegreewms:Name", nsContext );
            String pValue = XMLTools.getRequiredNodeAsString( (Node) obj, "deegreewms:Value", nsContext );

            addedParameters.put( pName, pValue );
        }
        return addedParameters;
    }

    /**
     * 
     * @param node
     * @param failOnEx
     * @param queryable
     * @param name
     * @param scaleHint
     * @param validArea
     * @param reqTimeLimit
     * @return {@link DatabaseDataSource}
     * @throws Exception
     */
    public static DatabaseDataSource createDatabaseSource( Node node, boolean failOnEx, boolean queryable,
                                                           QualifiedName name, ScaleHint scaleHint, Geometry validArea,
                                                           int reqTimeLimit )
                            throws Exception {

        String s = "./dgjdbc:JDBCConnection";
        Node jdbcNode = XMLTools.getRequiredNode( node, s, nsContext );
        IODocument io = new IODocument( (Element) jdbcNode );
        JDBCConnection jdbc = io.parseJDBCConnection();
        String sqlTemplate = XMLTools.getRequiredNodeAsString( node, "./deegreewms:SQLTemplate/text()", nsContext );
        String geometryField = XMLTools.getRequiredNodeAsString( node, "./deegreewms:GeometryField/text()", nsContext );
        String tmp = XMLTools.getRequiredNodeAsString( node, "./deegreewms:NativeCRS/text()", nsContext );
        boolean customSQL = getNodeAsBoolean( node, "deegreewms:CustomSQLAllowed", nsContext, false );
        CoordinateSystem nativeCRS = CRSFactory.create( tmp );

        Map<String, String> dimProps = new HashMap<String, String>();

        for ( Element e : getElements( node, "deegreewms:DimensionProperty", nsContext ) ) {
            dimProps.put( e.getAttribute( "name" ), e.getTextContent() );
        }

        return new DatabaseDataSource( queryable, failOnEx, name, scaleHint, validArea, reqTimeLimit, jdbc,
                                       sqlTemplate, geometryField, nativeCRS, customSQL, dimProps );
    }

    /**
     * @param node
     * @param failOnEx
     * @param queryable
     * @param name
     * @param url
     * @param scaleHint
     * @throws Exception
     */
    private RemoteWMSDataSource createRemoteWMSDataSource( Node node, boolean failOnEx, boolean queryable,
                                                           QualifiedName name, URL url, ScaleHint scaleHint,
                                                           Geometry validArea, int reqTimeLimit )
                            throws Exception {
        int type = AbstractDataSource.REMOTEWMS;

        String s = "./deegreewms:FeatureInfoTransformation/deegreewms:OnlineResource";
        Node fitNode = XMLTools.getNode( node, s, nsContext );
        URL fitURL = null;
        if ( fitNode != null ) {
            fitURL = parseOnLineResource( (Element) fitNode ).getLinkage().getHref();
        }

        GetMap getMap = parseWMSFilterCondition( node );
        Color[] colors = parseTransparentColors( node );
        WMSCapabilities wCapa = null;
        if ( capaCache.get( url ) != null ) {
            wCapa = (WMSCapabilities) capaCache.get( url );
        } else {
            LOG.logDebug( "Fetching remote WMS capabilities from URL " + url );
            WMSCapabilitiesDocument doc = WMSCapabilitiesDocumentFactory.getWMSCapabilitiesDocument( url );
            wCapa = (WMSCapabilities) doc.parseCapabilities();
            capaCache.put( url, wCapa );
        }
        OGCWebService ows = new RemoteWMService( wCapa );

        // parse added/passed parameter map/list
        Node vendor = XMLTools.getNode( node,
                                        "deegreewms:FilterCondition/deegreewms:VendorspecificParameterDefinition",
                                        nsContext );

        List<String> passedParameters = parsePassedParameters( vendor );
        Map<String, String> addedParameters = parseAddedParameters( vendor );

        return new RemoteWMSDataSource( queryable, failOnEx, name, type, ows, url, scaleHint, validArea, getMap,
                                        colors, fitURL, reqTimeLimit, passedParameters, addedParameters );
    }

    /**
     * @param node
     * @param failOnEx
     * @param queryable
     * @param name
     * @param url
     * @param scaleHint
     * @throws Exception
     */
    private RemoteWFSDataSource createRemoteWFSDataSource( Node node, boolean failOnEx, boolean queryable,
                                                           QualifiedName name, URL url, ScaleHint scaleHint,
                                                           Geometry validArea, int reqTimeLimit )
                            throws Exception {
        int type = AbstractDataSource.REMOTEWFS;
        String s = "./deegreewms:FeatureInfoTransformation/deegreewms:OnlineResource";
        Node fitNode = XMLTools.getNode( node, s, nsContext );
        URL fitURL = null;
        if ( fitNode != null ) {
            fitURL = parseOnLineResource( (Element) fitNode ).getLinkage().getHref();
        }
        Query query = parseWFSFilterCondition( node );

        WFSCapabilities wfsCapa = null;
        if ( capaCache.get( url ) != null ) {
            wfsCapa = (WFSCapabilities) capaCache.get( url );
        } else {
            WFSCapabilitiesDocument wfsDoc = new WFSCapabilitiesDocument();
            LOG.logDebug( "Fetching remote WFS capabilities from URL " + url );
            wfsDoc.load( url );
            wfsCapa = (WFSCapabilities) wfsDoc.parseCapabilities();
            capaCache.put( url, wfsCapa );
        }
        OGCWebService ows = new RemoteWFService( wfsCapa );
        // OGCWebService ows = null;

        Node geoPropNode = XMLTools.getNode( node, "deegreewms:GeometryProperty/text()", nsContext );
        QualifiedName geoProp = DEFAULT_GEO_PROP;
        if ( geoPropNode != null ) {
            geoProp = parseQualifiedName( geoPropNode );
        }

        return new RemoteWFSDataSource( queryable, failOnEx, name, type, geoProp, ows, url, scaleHint, validArea,
                                        query, fitURL, reqTimeLimit );

    }

    /**
     * @param node
     * @param failOnEx
     * @param queryable
     * @param name
     * @param url
     * @param scaleHint
     * @throws Exception
     */
    private LocalWCSDataSource createLocalWCSDataSource( Node node, boolean failOnEx, boolean queryable,
                                                         QualifiedName name, URL url, ScaleHint scaleHint,
                                                         Geometry validArea, int reqTimeLimit )
                            throws Exception {
        int type = AbstractDataSource.LOCALWCS;
        GetCoverage getCoverage = parseWCSFilterCondition( node );
        Color[] colors = parseTransparentColors( node );
        WCSConfiguration configuration = null;
        if ( capaCache.get( url ) != null ) {
            configuration = (WCSConfiguration) capaCache.get( url );
        } else {
            configuration = WCSConfiguration.create( url );
            capaCache.put( url, configuration );
        }

        OGCWebService ows = new WCService( configuration );

        return new LocalWCSDataSource( queryable, failOnEx, name, type, ows, url, scaleHint, validArea, getCoverage,
                                       colors, reqTimeLimit );
    }

    /**
     * 
     * @param node
     * @param failOnEx
     * @param queryable
     * @param name
     * @param url
     * @param scaleHint
     * @param validArea
     * @param reqTimeLimit
     * @return {@link RemoteWCSDataSource}
     * @throws XMLParsingException
     * @throws SAXException
     * @throws IOException
     * @throws InvalidCapabilitiesException
     */
    private AbstractDataSource createRemoteWCSDataSource( Node node, boolean failOnEx, boolean queryable,
                                                          QualifiedName name, URL url, ScaleHint scaleHint,
                                                          Geometry validArea, int reqTimeLimit )
                            throws XMLParsingException, InvalidCapabilitiesException, IOException, SAXException {
        int type = AbstractDataSource.REMOTEWCS;

        GetCoverage getCoverage = parseWCSFilterCondition( node );
        Color[] colors = parseTransparentColors( node );
        WCSCapabilities capabilities = null;
        if ( capaCache.get( url ) != null ) {
            capabilities = (WCSCapabilities) capaCache.get( url );
        } else {
            WCSCapabilitiesDocument doc = new WCSCapabilitiesDocument();
            doc.load( url );
            capabilities = (WCSCapabilities) doc.parseCapabilities();
            capaCache.put( url, capabilities );
        }

        OGCWebService ows = new RemoteWCService( capabilities );

        return new RemoteWCSDataSource( queryable, failOnEx, name, type, ows, url, scaleHint, validArea, getCoverage,
                                        colors, reqTimeLimit );

    }

    /**
     * @param node
     * @param failOnEx
     * @param queryable
     * @param name
     * @param url
     * @param scaleHint
     * @throws Exception
     */
    private LocalWFSDataSource createLocalWFSDataSource( Node node, boolean failOnEx, boolean queryable,
                                                         QualifiedName name, URL url, ScaleHint scaleHint,
                                                         Geometry validArea, int reqTimeLimit )
                            throws Exception {
        int type = AbstractDataSource.LOCALWFS;
        String s = "./deegreewms:FeatureInfoTransformation/deegreewms:OnlineResource";
        Node fitNode = XMLTools.getNode( node, s, nsContext );
        URL fitURL = null;
        if ( fitNode != null ) {
            fitURL = parseOnLineResource( (Element) fitNode ).getLinkage().getHref();
        }
        Query query = parseWFSFilterCondition( node );
        WFSConfiguration wfsCapa = null;
        if ( capaCache.get( url ) != null ) {
            wfsCapa = (WFSConfiguration) capaCache.get( url );
        } else {
            WFSConfigurationDocument wfsDoc = new WFSConfigurationDocument();
            wfsDoc.load( url );
            wfsCapa = wfsDoc.getConfiguration();
            // wfsCapa = new WFSCapabilitiesDocument( url ).createCapabilities();
            capaCache.put( url, wfsCapa );
        }
        // OGCWebService ows = WFServiceFactory.getUncachedService( wfsCapa );
        OGCWebService ows = WFServiceFactory.createInstance( wfsCapa );

        Node geoPropNode = XMLTools.getNode( node, "deegreewms:GeometryProperty/text()", nsContext );
        QualifiedName geoProp = DEFAULT_GEO_PROP;
        if ( geoPropNode != null ) {
            geoProp = parseQualifiedName( geoPropNode );
        }

        Map<String, String> dimProps = new HashMap<String, String>();

        for ( Element e : getElements( node, "deegreewms:DimensionProperty", nsContext ) ) {
            dimProps.put( e.getAttribute( "name" ), e.getTextContent() );
        }

        LOG.logDebug( "geometry property", geoProp );

        return new LocalWFSDataSource( queryable, failOnEx, name, type, geoProp, ows, url, scaleHint, validArea, query,
                                       fitURL, reqTimeLimit, dimProps );
    }

    private Color[] parseTransparentColors( Node node )
                            throws XMLParsingException {

        String s = "./deegreewms:TransparentColors/deegreewms:Color";
        List<Node> clnl = XMLTools.getNodes( node, s, nsContext );
        Color[] colors = new Color[clnl.size()];
        for ( int j = 0; j < colors.length; j++ ) {
            colors[j] = Color.decode( XMLTools.getStringValue( clnl.get( j ) ) );
        }

        return colors;
    }

    /**
     * 
     * @param node
     * @return the query
     * @throws XMLParsingException
     */
    private Query parseWFSFilterCondition( Node node )
                            throws XMLParsingException {

        Query o = null;

        Node queryNode = XMLTools.getNode( node, "./deegreewms:FilterCondition/wfs:Query", nsContext );
        if ( queryNode != null ) {
            try {
                o = Query.create( (Element) queryNode );
            } catch ( Exception e ) {
                throw new XMLParsingException( StringTools.stackTraceToString( e ) );
            }
        }

        return o;
    }

    /**
     * 
     * @param node
     * @return the request
     * @throws XMLParsingException
     */
    private GetCoverage parseWCSFilterCondition( Node node )
                            throws XMLParsingException {

        GetCoverage o = null;

        String id = "" + IDGenerator.getInstance().generateUniqueID();

        StringBuffer sd = new StringBuffer( 1000 );
        sd.append( "version=1.0.0&Coverage=%default%&" );
        sd.append( "CRS=EPSG:4326&BBOX=0,0,1,1&Width=1" );
        sd.append( "&Height=1&Format=%default%&" );
        String s = XMLTools.getNodeAsString( node, "./deegreewms:FilterCondition/deegreewms:WCSRequest", nsContext, "" );
        sd.append( s );
        try {
            o = GetCoverage.create( id, sd.toString() );
        } catch ( Exception e ) {
            throw new XMLParsingException( "could not create GetCoverage from layer FilterCondition", e );
        }

        return o;
    }

    /**
     * 
     * @param node
     * @return the request
     * @throws XMLParsingException
     */
    private GetMap parseWMSFilterCondition( Node node )
                            throws XMLParsingException {

        GetMap o = null;

        String id = "" + IDGenerator.getInstance().generateUniqueID();

        StringBuffer sd = new StringBuffer( 1000 );
        sd.append( "REQUEST=GetMap&LAYERS=%default%&" );
        sd.append( "STYLES=&SRS=EPSG:4326&BBOX=0,0,1,1&WIDTH=1&" );
        sd.append( "HEIGHT=1&FORMAT=%default%" );
        Map<String, String> map1 = KVP2Map.toMap( sd.toString() );
        String s = XMLTools.getRequiredNodeAsString( node, "./deegreewms:FilterCondition/deegreewms:WMSRequest",
                                                     nsContext );
        Map<String, String> map2 = KVP2Map.toMap( s );
        if ( map2.get( "VERSION" ) == null && map2.get( "WMTVER" ) == null ) {
            map2.put( "VERSION", "1.1.1" );
        }
        // if no service is set use WMS as default
        if ( map2.get( "SERVICE" ) == null ) {
            map2.put( "SERVICE", "WMS" );
        }
        map1.putAll( map2 );
        try {
            map1.put( "ID", id );
            o = GetMap.create( map1 );
        } catch ( Exception e ) {
            throw new XMLParsingException( "could not create GetMap from layer FilterCondition", e );
        }

        return o;
    }

    /**
     * 
     * @param layerElem
     * @throws XMLParsingException
     */
    @Override
    protected Style[] parseStyles( Element layerElem )
                            throws XMLParsingException {

        List<Node> nl = XMLTools.getNodes( layerElem, "./Style", nsContext );
        Style[] styles = new Style[nl.size()];
        for ( int i = 0; i < styles.length; i++ ) {
            String name = XMLTools.getRequiredNodeAsString( nl.get( i ), "./Name", nsContext );
            String title = XMLTools.getNodeAsString( nl.get( i ), "./Title", nsContext, null );
            String styleAbstract = XMLTools.getNodeAsString( nl.get( i ), "./Abstract", nsContext, null );
            LegendURL[] legendURLs = parseLegendURL( nl.get( i ) );
            StyleURL styleURL = parseStyleURL( nl.get( i ) );
            StyleSheetURL styleSheetURL = parseStyleSheetURL( nl.get( i ) );
            String styleResource = XMLTools.getNodeAsString( nl.get( i ), "deegreewms:StyleResource", nsContext,
                                                             "styles.xml" );
            URL sr = null;
            try {
                sr = resolve( styleResource );
            } catch ( MalformedURLException e ) {
                throw new XMLParsingException( "could not parse style resource of style: " + name, e );
            }

            String layerName = XMLTools.getRequiredNodeAsString( layerElem, "Name", nsContext );
            if ( name == null || name.length() == 0 ) {
                name = "default:" + layerName;
            }
            if ( name.equals( "default" ) ) {
                name += ":" + layerName;
            }
            if ( name.equals( "default:" ) ) {
                name += layerName;
            }

            styles[i] = new Style( name, title, styleAbstract, legendURLs, styleSheetURL, styleURL, sr );
        }

        return styles;
    }

}

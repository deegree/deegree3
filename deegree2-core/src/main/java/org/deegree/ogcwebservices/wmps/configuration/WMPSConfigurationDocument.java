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
package org.deegree.ogcwebservices.wmps.configuration;

import java.awt.Color;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
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
import org.deegree.io.IODocument;
import org.deegree.io.JDBCConnection;
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
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.ogcwebservices.wcs.RemoteWCService;
import org.deegree.ogcwebservices.wcs.WCService;
import org.deegree.ogcwebservices.wcs.configuration.WCSConfiguration;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSCapabilities;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSCapabilitiesDocument;
import org.deegree.ogcwebservices.wcs.getcoverage.GetCoverage;
import org.deegree.ogcwebservices.wfs.WFServiceFactory;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilitiesDocument;
import org.deegree.ogcwebservices.wfs.configuration.WFSConfiguration;
import org.deegree.ogcwebservices.wfs.configuration.WFSConfigurationDocument;
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.deegree.ogcwebservices.wmps.capabilities.WMPSCapabilitiesDocument;
import org.deegree.ogcwebservices.wms.RemoteWMService;
import org.deegree.ogcwebservices.wms.capabilities.Attribution;
import org.deegree.ogcwebservices.wms.capabilities.AuthorityURL;
import org.deegree.ogcwebservices.wms.capabilities.DataURL;
import org.deegree.ogcwebservices.wms.capabilities.Dimension;
import org.deegree.ogcwebservices.wms.capabilities.Extent;
import org.deegree.ogcwebservices.wms.capabilities.FeatureListURL;
import org.deegree.ogcwebservices.wms.capabilities.GazetteerParam;
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
import org.deegree.ogcwebservices.wms.configuration.AbstractDataSource;
import org.deegree.ogcwebservices.wms.configuration.LocalWCSDataSource;
import org.deegree.ogcwebservices.wms.configuration.LocalWFSDataSource;
import org.deegree.ogcwebservices.wms.configuration.RemoteWCSDataSource;
import org.deegree.ogcwebservices.wms.configuration.RemoteWFSDataSource;
import org.deegree.ogcwebservices.wms.configuration.RemoteWMSDataSource;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Represents an XML configuration document for a deegree WMPS 1.0 instance, i.e. it consists of all sections common to
 * an OGC WMS 1.1.1 capabilities document plus a deegree specific section named <code>deegreeParams</code> and... TODO
 * 
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh</a>
 * @author last edited by: $Author$
 * 
 * @version 2.0, $Revision$, $Date$
 */
public class WMPSConfigurationDocument extends WMPSCapabilitiesDocument {

    private static final long serialVersionUID = -7940857863171829848L;

    private static final ILogger LOG = LoggerFactory.getLogger( WMPSConfigurationDocument.class );

    protected static final URI DEEGREEWPSNS = CommonNamespaces.DEEGREEWMPS;

    private static final String XML_TEMPLATE = "WMPSConfigurationTemplate.xml";

    private static final String XSLT_TEMPLATE_NAME = "WMPSConfigurationTransform.xsl";

    private static XSLTDocument XSLT_TEMPLATE;

    private static Map<URL, Object> capaCache = new HashMap<URL, Object>();

    static {
        XSLT_TEMPLATE = new XSLTDocument();
        try {
            XSLT_TEMPLATE.load( WMPSConfigurationDocument.class.getResource( XSLT_TEMPLATE_NAME ) );
        } catch ( Exception e ) {
            BootLogger.logError( "Error loading XSLT sheet in WMPSConfigurationDocument.", e );
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
        URL url = WMPSConfigurationDocument.class.getResource( XML_TEMPLATE );
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
    public WMPSConfiguration parseConfiguration()
                            throws InvalidConfigurationException {

        try {
            // transform document to fill missing elements and attributes with default values
            XMLFragment frag = XSLT_TEMPLATE.transform( this );
            this.setRootElement( frag.getRootElement() );
        } catch ( TransformerException e ) {
            String msg = "Error transforming WMPS configuration document (in order to fill " + "in default value). "
                         + e.getMessage();
            LOG.logError( msg, e );
            throw new InvalidConfigurationException( msg, e );
        }

        ServiceIdentification serviceIdentification = null;
        ServiceProvider serviceProvider = null;
        UserDefinedSymbolization uds = null;
        OperationsMetadata metadata = null;
        Layer layer = null;
        WMPSDeegreeParams params = null;
        String version = parseVersion();
        try {
            Node node = XMLTools.getRequiredNode( getRootElement(), "deegreewmps:DeegreeParam", nsContext );
            params = parseDeegreeParams( node );
            serviceIdentification = parseServiceIdentification();
            serviceProvider = parseServiceProvider();
            uds = parseUserDefinedSymbolization();
            metadata = parseOperationsMetadata();
            Element layerElem = (Element) XMLTools.getRequiredNode( getRootElement(), "./Capability/Layer", nsContext );
            layer = parseLayers( layerElem, null );
        } catch ( XMLParsingException e ) {
            e.printStackTrace();
            throw new InvalidConfigurationException( e.getMessage() + StringTools.stackTraceToString( e ) );
        } catch ( MalformedURLException e ) {
            throw new InvalidConfigurationException( e.getMessage() + " - " + StringTools.stackTraceToString( e ) );
        } catch ( UnknownCRSException e ) {
            throw new InvalidConfigurationException( getClass().getName(), e.getMessage() );
        }
        WMPSConfiguration wmpsConfiguration = new WMPSConfiguration( version, serviceIdentification, serviceProvider,
                                                                     uds, metadata, layer, params, getSystemId() );

        return wmpsConfiguration;
    }

    /**
     * Creates a class representation of the <code>deegreeParams</code>- section.
     * 
     * @param root
     * @return WMPSDeegreeParams
     * @throws XMLParsingException
     * @throws MalformedURLException
     */
    public WMPSDeegreeParams parseDeegreeParams( Node root )
                            throws XMLParsingException, MalformedURLException {

        String xPath = "./deegreewmps:DefaultOnlineResource";
        Element elem = (Element) XMLTools.getRequiredNode( root, xPath, nsContext );

        OnlineResource ol = parseOnLineResource( elem );
        xPath = "./deegreewmps:CacheSize";
        int cache = XMLTools.getNodeAsInt( root, xPath, nsContext, 100 );
        xPath = "./deegreewmps:MaxLifeTime";
        int maxLifeTime = XMLTools.getNodeAsInt( root, xPath, nsContext, 3600 );
        xPath = "./deegreewmps:RequestTimeLimit";
        int reqTimeLimit = XMLTools.getNodeAsInt( root, xPath, nsContext, 15 );
        reqTimeLimit *= 1000;
        xPath = "./deegreewmps:MapQuality";
        double mapQuality = XMLTools.getNodeAsDouble( root, xPath, nsContext, 0.95 );
        xPath = "./deegreewmps:MaxMapWidth";
        int maxMapWidth = XMLTools.getNodeAsInt( root, xPath, nsContext, 1000 );
        xPath = "./deegreewmps:MaxMapHeight";
        int maxMapHeight = XMLTools.getNodeAsInt( root, xPath, nsContext, 1000 );
        xPath = "./deegreewmps:Copyright";
        String copyright = XMLTools.getNodeAsString( root, xPath, nsContext, "" );
        URL dtdLocation = null;
        xPath = "deegreewmps:DTDLocation";
        if ( XMLTools.getNode( root, xPath, nsContext ) != null ) {
            xPath = "./deegreewmps:DTDLocation/deegreewmps:OnlineResource";
            elem = (Element) XMLTools.getRequiredNode( root, xPath, nsContext );
            OnlineResource olr = parseOnLineResource( elem );
            dtdLocation = olr.getLinkage().getHref();
        } else {
            dtdLocation = new URL( "http://schemas.opengis.net/wms/1.1.1/WMS_MS_Capabilities.dtd" );
        }
        xPath = "./deegreewmps:AntiAliased";
        boolean antiAliased = XMLTools.getNodeAsBoolean( root, xPath, nsContext, true );
        xPath = "./deegreewmps:GazetteerParameter";
        elem = (Element) XMLTools.getNode( root, xPath, nsContext );
        GazetteerParam gazetteer = null;
        if ( elem != null ) {
            gazetteer = parseGazetteerParameter( elem );
        }

        Proxy proxy = parseProxy( root );

        List<String> synchList = parseSynchTemplates( root );

        CacheDatabase cacheDatabase = parseCacheDatabase( root );

        PrintMapParam printMapParam = parsePrintMapParam( root );

        WMPSDeegreeParams deegreeParams = new WMPSDeegreeParams( cache, maxLifeTime, reqTimeLimit, (float) mapQuality,
                                                                 ol, maxMapWidth, maxMapHeight, antiAliased, copyright,
                                                                 gazetteer, null, dtdLocation, proxy, synchList,
                                                                 cacheDatabase, printMapParam );

        return deegreeParams;
    }

    /**
     * Parse the cache database parameters used by the wmps to store the asynchronous requests.
     * 
     * @param root
     * @return CacheDatabase
     * @throws XMLParsingException
     */
    private CacheDatabase parseCacheDatabase( Node root )
                            throws XMLParsingException {

        CacheDatabase cacheDatabase = null;
        String xPath = "./deegreewmps:CacheDatabase";
        String driver = null;
        String url = null;
        String user = null;
        String password = null;
        Node cacheDb = XMLTools.getNode( root, xPath, nsContext );
        if ( cacheDb != null ) {
            xPath = "./deegreewmps:JDBCConnection";
            Node jdbcConnection = XMLTools.getNode( cacheDb, xPath, nsContext );
            if ( jdbcConnection != null ) {               
                driver = XMLTools.getRequiredNodeAsString( jdbcConnection, "./deegreewmps:Driver", nsContext );
                url = XMLTools.getRequiredNodeAsString( jdbcConnection, "./deegreewmps:Url", nsContext );
                user = XMLTools.getRequiredNodeAsString( jdbcConnection, "./deegreewmps:User", nsContext );
                password = XMLTools.getRequiredNodeAsString( jdbcConnection, "./deegreewmps:Password", nsContext );
            } else {
                xPath = "./dgjdbc:JDBCConnection";
                jdbcConnection = XMLTools.getRequiredNode( cacheDb, xPath, nsContext );
                IODocument doc = new IODocument( (Element) jdbcConnection );
                doc.setSystemId( getSystemId() );
                JDBCConnection jdbc = doc.parseJDBCConnection();
                driver = jdbc.getDriver();
                url = jdbc.getURL();
                user = jdbc.getUser();
                password = jdbc.getPassword();
            }
            cacheDatabase = new CacheDatabase( driver, url, user, password );
            LOG.logDebug( "Successfully parsed the deegree wmps cache database parameters." );
        }

        return cacheDatabase;
    }

    /**
     * Parse the PrintMapParam node and retrieve the (jasper reports)template directory, the default template name if
     * none is specified in the request. Also parse the output directory location and the location of the printed image
     * output directory.
     * 
     * @param root
     * @return PrintMapParam
     * @throws XMLParsingException
     * @throws MalformedURLException
     */
    private PrintMapParam parsePrintMapParam( Node root )
                            throws XMLParsingException, MalformedURLException {

        Node printParam = XMLTools.getRequiredNode( root, "./deegreewmps:PrintMapParam", nsContext );
        // set default as pdf
        String format = XMLTools.getNodeAsString( printParam, "./deegreewmps:Format", nsContext, "pdf" );
        Node templ = XMLTools.getRequiredNode( printParam, "./deegreewmps:Template", nsContext );
        String templateDirectory = XMLTools.getRequiredNodeAsString( templ, "./deegreewmps:Directory", nsContext );

        templateDirectory = this.resolve( templateDirectory ).toExternalForm();
        if ( !templateDirectory.endsWith( "/" ) ) {
            templateDirectory = templateDirectory + '/';
        }
        String plotDirectory = XMLTools.getRequiredNodeAsString( printParam, "./deegreewmps:PlotDirectory", nsContext );
        plotDirectory = this.resolve( plotDirectory ).toExternalForm();
        String onlineResource = XMLTools.getRequiredNodeAsString( printParam, "./deegreewmps:OnlineResource", nsContext );
        String plotImgDir = XMLTools.getRequiredNodeAsString( printParam, "./deegreewmps:PlotImageDirectory", nsContext );
        plotImgDir = this.resolve( plotImgDir ).toExternalForm();
        String adminMail = XMLTools.getRequiredNodeAsString( printParam, "./deegreewmps:AdministratorEMailAddress",
                                                             nsContext );
        String mailHost = XMLTools.getRequiredNodeAsString( printParam, "./deegreewmps:MailHost", nsContext );
        String mailHostUser = XMLTools.getNodeAsString( printParam, "./deegreewmps:MailHostUser", nsContext, null );
        String mailHostPassword = XMLTools.getNodeAsString( printParam, "./deegreewmps:MailHostPassword", nsContext,
                                                            null );

        String mailTextTemplate = XMLTools.getNodeAsString( printParam, "./deegreewmps:MailTextTemplate", nsContext,
                                                            "You can access the printMap result at {1}" );

        int targetRes = XMLTools.getNodeAsInt( printParam, "./deegreewmps:TargetResolution", nsContext, 300 );

        return new PrintMapParam( format, templateDirectory, onlineResource, plotDirectory, plotImgDir, adminMail,
                                  mailHost, mailHostUser, mailHostPassword, mailTextTemplate, targetRes );
    }

    /**
     * parses the list of templates that shall be handled synchronously
     * 
     * @param root
     * @return List
     * @throws XMLParsingException
     */
    private List<String> parseSynchTemplates( Node root )
                            throws XMLParsingException {

        String xPath = "./deegreewmps:SynchronousTemplates/deegreewmps:Template";
        String[] nodes = XMLTools.getNodesAsStrings( root, xPath, nsContext );
        List<String> list = new ArrayList<String>( nodes.length );
        for ( int i = 0; i < nodes.length; i++ ) {
            list.add( nodes[i] );
        }

        return list;
    }

    /**
     * @param root
     * @return Proxy
     * @throws XMLParsingException
     */
    private Proxy parseProxy( Node root )
                            throws XMLParsingException {

        Proxy proxy = null;
        Node pro = XMLTools.getNode( root, "./deegreewmps:Proxy", nsContext );
        if ( pro != null ) {
            String proxyHost = XMLTools.getRequiredNodeAsString( pro, "./@proxyHost", nsContext );
            String proxyPort = XMLTools.getRequiredNodeAsString( pro, "./@proxyPort", nsContext );
            proxy = new Proxy( proxyHost, proxyPort );
        }

        return proxy;
    }

    /**
     * creates an object that describes the access to a gazetteer if one have been defined at the <DeegreeParam>section
     * of the capabilities/configuration
     * 
     * @param element
     * @return GazetteerParam
     * @throws XMLParsingException
     */
    private GazetteerParam parseGazetteerParameter( Element element )
                            throws XMLParsingException {

        GazetteerParam gazetteer = null;

        if ( element != null ) {
            String xPath = "./deegreewmps:OnlineResource";
            Element elem = (Element) XMLTools.getRequiredNode( element, xPath, nsContext );
            OnlineResource olr = parseOnLineResource( elem );
            URL onlineResource = olr.getLinkage().getHref();
            // optional: <LocationRadius>, default: 10
            double radius = XMLTools.getNodeAsDouble( element, "LocationRadius", nsContext, 10 );
            gazetteer = new GazetteerParam( onlineResource, radius );
        }

        return gazetteer;
    }

    /**
     * returns the layers offered by the WMS
     * 
     * @param layerElem
     * @param parent
     * @return Layer
     * @throws XMLParsingException
     * @throws UnknownCRSException
     */
    @Override
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
        String[] keywords = XMLTools.getNodesAsStrings( layerElem, "./KeywordList/Keyword", nsContext );
        String[] srs = XMLTools.getNodesAsStrings( layerElem, "./SRS", nsContext );
        List<Node> nl = XMLTools.getNodes( layerElem, "./BoundingBox", nsContext );
        // substitute with Envelope
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
        ScaleHint scaleHint = parseScaleHint( layerElem );
        AbstractDataSource[] ds = parseDataSources( layerElem, name );

        Layer layer = new Layer( queryable, cascaded, opaque, noSubsets, fixedWidth, fixedHeight, name, title,
                                 layerAbstract, llBoundingBox, attribution, scaleHint, keywords, srs, bboxes,
                                 dimensions, extents, authorityURLs, identifiers, metadataURLs, dataURLs,
                                 featureListURLs, styles, null, ds, parent );

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
     * Parse the Datasources element
     * 
     * @param layerElem
     * @param layerName
     * @return AbstractDataSource[]
     * @throws XMLParsingException
     */
    private AbstractDataSource[] parseDataSources( Element layerElem, String layerName )
                            throws XMLParsingException {

        List<Node> nl = XMLTools.getNodes( layerElem, "./deegreewmps:DataSource", nsContext );

        AbstractDataSource[] ds = new AbstractDataSource[nl.size()];
        for ( int i = 0; i < ds.length; i++ ) {

            boolean failOnEx = XMLTools.getNodeAsBoolean( (Node) nl.get( i ), "./@failOnException", nsContext, true );

            boolean queryable = XMLTools.getNodeAsBoolean( (Node) nl.get( i ), "./@queryable", nsContext, false );

            QualifiedName name = XMLTools.getNodeAsQualifiedName( (Node) nl.get( i ), "./deegreewmps:Name/text()",
                                                                  nsContext, new QualifiedName( layerName ) );

            String stype = XMLTools.getRequiredNodeAsString( (Node) nl.get( i ), "./deegreewmps:Type", nsContext );

            String xPath = "./deegreewmps:OWSCapabilities/deegreewmps:OnlineResource";
            Node node = XMLTools.getRequiredNode( (Node) nl.get( i ), xPath, nsContext );

            URL url = parseOnLineResource( (Element) node ).getLinkage().getHref();
            ScaleHint scaleHint = parseScaleHint( (Node) nl.get( i ) );
            Geometry validArea = parseValidArea( (Node) nl.get( i ) );

            try {
                if ( "LOCALWFS".equals( stype ) ) {
                    ds[i] = createLocalWFSDataSource( (Node) nl.get( i ), failOnEx, queryable, name, url, scaleHint,
                                                      validArea, 60 );
                } else if ( "LOCALWCS".equals( stype ) ) {
                    ds[i] = createLocalWCSDataSource( (Node) nl.get( i ), failOnEx, queryable, name, url, scaleHint,
                                                      validArea, 60 );
                } else if ( "REMOTEWFS".equals( stype ) ) {
                    ds[i] = createRemoteWFSDataSource( (Node) nl.get( i ), failOnEx, queryable, name, url, scaleHint,
                                                       validArea, 60 );
                } else if ( "REMOTEWCS".equals( stype ) ) {
                    ds[i] = createRemoteWCSDataSource( (Node) nl.get( i ), failOnEx, queryable, name, url, scaleHint,
                                                       validArea, 60 );
                } else if ( "REMOTEWMS".equals( stype ) ) {
                    ds[i] = createRemoteWMSDataSource( (Node) nl.get( i ), failOnEx, queryable, name, url, scaleHint,
                                                       validArea, 60 );
                } else {
                    throw new XMLParsingException( "invalid DataSource type: " + stype + " defined in deegree WMS "
                                                   + "configuration for DataSource: " + name );
                }
            } catch ( Exception e ) {
                throw new XMLParsingException( "could not create service instance for " + "WMPS datasource: " + name, e );
            }
        }

        return ds;
    }

    /**
     * returns the area a data source is valid. If the optional element <ValidArea>is not defined in the configuration
     * returns <code>null</code>.
     * 
     * @param node
     * @return Geometry
     * @throws XMLParsingException
     */
    private Geometry parseValidArea( Node node )
                            throws XMLParsingException {

        Geometry geom = null;
        List<Node> nl = XMLTools.getNodes( node, "./deegreewmps:ValidArea/*", nsContext );
        if ( node != null ) {
            try {
                for ( int i = 0; i < nl.size(); i++ ) {
                    if ( URI.create( nl.get( 0 ).getNamespaceURI() ).equals( GMLNS ) ) {
                        geom = GMLGeometryAdapter.wrap( (Element) nl.get( 0 ), null );
                        break;
                    }
                }
            } catch ( GeometryException e ) {
                throw new XMLParsingException( "couldn't parse/create valid area of a " + "datasource", e );
            }
        }

        return geom;
    }

    /**
     * Create a RemoteWMS datasource.
     * 
     * @param node
     * @param failOnEx
     * @param queryable
     * @param name
     * @param url
     * @param scaleHint
     * @param validArea
     * @param reqTimeLimit
     * @return XMLParsingException
     * @throws Exception
     */
    private RemoteWMSDataSource createRemoteWMSDataSource( Node node, boolean failOnEx, boolean queryable,
                                                           QualifiedName name, URL url, ScaleHint scaleHint,
                                                           Geometry validArea, int reqTimeLimit )
                            throws Exception {

        int type = AbstractDataSource.REMOTEWMS;
        String xPath = "./deegreewmps:FeatureInfoTransformation/deegreewmps:OnlineResource";
        Node fitNode = XMLTools.getNode( node, xPath, nsContext );
        URL fitURL = null;
        if ( fitNode != null ) {
            fitURL = parseOnLineResource( (Element) fitNode ).getLinkage().getHref();
        }

        GetMap getMap = parseWMSFilterCondition( node );
        Color[] colors = parseTransparentColors( node );
        WMSCapabilities wCapa = null;
        try {
            if ( capaCache.get( url ) != null ) {
                wCapa = (WMSCapabilities) capaCache.get( url );
            } else {
                WMSCapabilitiesDocument doc = WMSCapabilitiesDocumentFactory.getWMSCapabilitiesDocument( url );
                wCapa = (WMSCapabilities) doc.parseCapabilities();
                capaCache.put( url, wCapa );
            }
        } catch ( Exception e ) {
            LOG.logError( "could not connet: " + url, e );
        }
        OGCWebService ows = new RemoteWMService( wCapa );

        // parse added/passed parameter map/list
        Node vendor = XMLTools.getNode( node,
                                        "deegreewmps:FilterCondition/deegreewmps:VendorspecificParameterDefinition",
                                        nsContext );

        List<String> passedParameters = parsePassedParameters( vendor );
        Map<String, String> addedParameters = parseAddedParameters( vendor );

        return new RemoteWMSDataSource( queryable, failOnEx, name, type, ows, url, scaleHint, validArea, getMap,
                                        colors, fitURL, reqTimeLimit, passedParameters, addedParameters );
    }

    /**
     * @param vendor
     * @return the parsed list
     * @throws XMLParsingException
     */
    private static List<String> parsePassedParameters( Node vendor )
                            throws XMLParsingException {
        List<String> passedParameters = new ArrayList<String>( 10 );

        if ( vendor == null ) {
            return passedParameters;
        }

        List<Node> nl = XMLTools.getNodes( vendor, "deegreewmps:PassedVendorspecificParameter/deegreewmps:Name",
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
    private static Map<String, String> parseAddedParameters( Node vendor )
                            throws XMLParsingException {
        Map<String, String> addedParameters = new HashMap<String, String>( 10 );

        if ( vendor == null ) {
            return addedParameters;
        }

        List<Node> nl = XMLTools.getNodes(
                                           vendor,
                                           "deegreewmps:AddedVendorspecificParameter/deegreewmps:VendorspecificParameter",
                                           nsContext );

        for ( Object obj : nl ) {
            String pName = XMLTools.getRequiredNodeAsString( (Node) obj, "deegreewmps:Name", nsContext );
            String pValue = XMLTools.getRequiredNodeAsString( (Node) obj, "deegreewmps:Value", nsContext );

            addedParameters.put( pName, pValue );
        }
        return addedParameters;
    }

    /**
     * Create a Remote WFS datasource.
     * 
     * @param node
     * @param failOnEx
     * @param queryable
     * @param name
     * @param url
     * @param scaleHint
     * @param validArea
     * @param reqTimeLimit
     * @return RemoteWFSDataSource
     * @throws Exception
     */
    private RemoteWFSDataSource createRemoteWFSDataSource( Node node, boolean failOnEx, boolean queryable,
                                                           QualifiedName name, URL url, ScaleHint scaleHint,
                                                           Geometry validArea, int reqTimeLimit )
                            throws Exception {

        int type = AbstractDataSource.REMOTEWFS;
        String xPath = "./deegreewmps:FeatureInfoTransformation/deegreewmps:OnlineResource";
        Node fitNode = XMLTools.getNode( node, xPath, nsContext );
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
            wfsDoc.load( url );
            wfsCapa = (WFSCapabilities) wfsDoc.parseCapabilities();
            capaCache.put( url, wfsCapa );
        }
        // OGCWebService ows = new RemoteWFService( wfsCapa );
        OGCWebService ows = null;
        xPath = "./deegreewmps:GeometryProperty/text()";
        QualifiedName geom = new QualifiedName( "app", "GEOM", new URI( "http://www.deegree.org/app" ) );
        QualifiedName geoProp = XMLTools.getNodeAsQualifiedName( node, xPath, nsContext, geom );

        return new RemoteWFSDataSource( queryable, failOnEx, name, type, geoProp, ows, url, scaleHint, validArea,
                                        query, fitURL, reqTimeLimit );

    }

    /**
     * Create a Local WCS Datasource
     * 
     * @param node
     * @param failOnEx
     * @param queryable
     * @param name
     * @param url
     * @param scaleHint
     * @param validArea
     * @param reqTimeLimit
     * @return RemoteWFSDataSource
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
     * @param i
     * @return
     * @throws XMLParsingException
     * @throws SAXException
     * @throws IOException
     * @throws org.deegree.ogcwebservices.wcs.configuration.InvalidConfigurationException
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
     * Create a Local WFS Datasource
     * 
     * @param node
     * @param failOnEx
     * @param queryable
     * @param name
     * @param url
     * @param scaleHint
     * @param validArea
     * @param reqTimeLimit
     * @return LocalWFSDataSource
     * @throws Exception
     */
    private LocalWFSDataSource createLocalWFSDataSource( Node node, boolean failOnEx, boolean queryable,
                                                         QualifiedName name, URL url, ScaleHint scaleHint,
                                                         Geometry validArea, int reqTimeLimit )
                            throws Exception {

        int type = AbstractDataSource.LOCALWFS;
        String xPath = null;
        Query query = parseWFSFilterCondition( node );
        WFSConfiguration wfsCapa = null;
        if ( capaCache.get( url ) != null ) {
            wfsCapa = (WFSConfiguration) capaCache.get( url );
        } else {
            WFSConfigurationDocument wfsDoc = new WFSConfigurationDocument();
            wfsDoc.load( url );
            wfsCapa = wfsDoc.getConfiguration();
            capaCache.put( url, wfsCapa );
        }
        // OGCWebService ows = WFServiceFactory.getUncachedService( wfsCapa );
        OGCWebService ows = WFServiceFactory.createInstance( wfsCapa );

        QualifiedName geom = new QualifiedName( "app", "GEOM", new URI( "http://www.deegree.org/app" ) );
        xPath = "./deegreewmps:GeometryProperty/text()";
        QualifiedName geoProp = XMLTools.getNodeAsQualifiedName( node, xPath, nsContext, geom );

        return new LocalWFSDataSource( queryable, failOnEx, name, type, geoProp, ows, url, scaleHint, validArea, query,
                                       null, reqTimeLimit );
    }

    /**
     * Parse trasparent colors.
     * 
     * @param node
     * @return Color[]
     * @throws XMLParsingException
     */
    private Color[] parseTransparentColors( Node node )
                            throws XMLParsingException {

        String xPath = "./deegreewmps:TransparentColors/deegreewmps:Color";
        List<Node> clnl = XMLTools.getNodes( node, xPath, nsContext );
        Color[] colors = new Color[clnl.size()];
        for ( int i = 0; i < colors.length; i++ ) {
            colors[i] = Color.decode( XMLTools.getStringValue( (Node) clnl.get( i ) ) );
        }

        return colors;
    }

    /**
     * Parse Scale hint
     * 
     * @param node
     * @return ScaleHint
     * @throws XMLParsingException
     */
    private ScaleHint parseScaleHint( Node node )
                            throws XMLParsingException {

        String xPath = "./deegreewmps:ScaleHint/@min";
        double minScale = XMLTools.getNodeAsDouble( node, xPath, nsContext, 0 );
        xPath = "./deegreewmps:ScaleHint/@max";
        double maxScale = XMLTools.getNodeAsDouble( node, xPath, nsContext, Double.MAX_VALUE );

        return new ScaleHint( minScale, maxScale );
    }

    /**
     * Parse WFS Filter condition
     * 
     * @param node
     * @return Query
     * @throws XMLParsingException
     */
    private Query parseWFSFilterCondition( Node node )
                            throws XMLParsingException {

        Query query = null;
        String xPath = "./deegreewmps:FilterCondition/wfs:Query";
        Node queryNode = XMLTools.getNode( node, xPath, nsContext );
        if ( queryNode != null ) {
            try {
                query = Query.create( (Element) queryNode );
            } catch ( Exception e ) {
                throw new XMLParsingException( StringTools.stackTraceToString( e ) );
            }
        }

        return query;
    }

    /**
     * Parse WCS Filter Condition
     * 
     * @param node
     * @return GetCoverage
     * @throws XMLParsingException
     */
    private GetCoverage parseWCSFilterCondition( Node node )
                            throws XMLParsingException {

        GetCoverage getCoverage = null;

        String id = "" + IDGenerator.getInstance().generateUniqueID();

        StringBuffer sd = new StringBuffer( 1000 );
        sd.append( "version=1.0.0&Coverage=%default%&" );
        sd.append( "CRS=EPSG:4326&BBOX=0,0,1,1&Width=1" );
        sd.append( "&Height=1&Format=%default%&" );
        String xPath = "./deegreewmps:FilterCondition/deegreewmps:WCSRequest";
        String s = XMLTools.getNodeAsString( node, xPath, nsContext, "" );
        sd.append( s );
        try {
            getCoverage = GetCoverage.create( id, sd.toString() );
        } catch ( Exception e ) {
            throw new XMLParsingException( "could not create GetCoverage from WMS " + "FilterCondition", e );
        }

        return getCoverage;
    }

    /**
     * Returns a GetMap instance , parsing the WMS Filter condition
     * 
     * @param node
     * @return GetMap
     * @throws XMLParsingException
     */
    private GetMap parseWMSFilterCondition( Node node )
                            throws XMLParsingException {

        GetMap getMap = null;

        String id = "" + IDGenerator.getInstance().generateUniqueID();

        StringBuffer sd = new StringBuffer( 1000 );
        sd.append( "REQUEST=GetMap&LAYERS=%default%&" );
        sd.append( "STYLES=&SRS=EPSG:4326&BBOX=0,0,1,1&WIDTH=1&" );
        sd.append( "HEIGHT=1&FORMAT=%default%" );
        Map<String, String> map1 = KVP2Map.toMap( sd.toString() );
        String xPath = "./deegreewmps:FilterCondition/deegreewmps:WMSRequest";
        String s = XMLTools.getRequiredNodeAsString( node, xPath, nsContext );
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
            getMap = GetMap.create( map1 );
        } catch ( Exception e ) {
            throw new XMLParsingException( "could not create GetCoverage from WMS " + "FilterCondition", e );
        }

        return getMap;
    }

    /**
     * Returns a list of Style elements, parsing the style element
     * 
     * @param layerElem
     * @return Style[]
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
            String xPath = "deegreewmps:StyleResource";
            String styleResource = XMLTools.getNodeAsString( nl.get( i ), xPath, nsContext, "styles.xml" );
            URL sr = null;
            try {
                sr = resolve( styleResource );
            } catch ( MalformedURLException e ) {
                throw new XMLParsingException( "could not parse style resource of " + "style: " + name, e );
            }
            styles[i] = new Style( name, title, styleAbstract, legendURLs, styleSheetURL, styleURL, sr );
        }

        return styles;
    }

}

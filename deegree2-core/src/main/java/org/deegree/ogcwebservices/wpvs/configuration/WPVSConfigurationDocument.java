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

package org.deegree.ogcwebservices.wpvs.configuration;

import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.IDGenerator;
import org.deegree.framework.util.KVP2Map;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.InvalidConfigurationException;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.filterencoding.AbstractFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Surface;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.wcs.getcoverage.GetCoverage;
import org.deegree.ogcwebservices.wms.operation.GetMap;
import org.deegree.ogcwebservices.wpvs.capabilities.DataProvider;
import org.deegree.ogcwebservices.wpvs.capabilities.Dataset;
import org.deegree.ogcwebservices.wpvs.capabilities.DatasetReference;
import org.deegree.ogcwebservices.wpvs.capabilities.Dimension;
import org.deegree.ogcwebservices.wpvs.capabilities.ElevationModel;
import org.deegree.ogcwebservices.wpvs.capabilities.FeatureListReference;
import org.deegree.ogcwebservices.wpvs.capabilities.Identifier;
import org.deegree.ogcwebservices.wpvs.capabilities.MetaData;
import org.deegree.ogcwebservices.wpvs.capabilities.OWSCapabilities;
import org.deegree.ogcwebservices.wpvs.capabilities.Style;
import org.deegree.ogcwebservices.wpvs.capabilities.WPVSCapabilitiesDocument;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Parser for WPVS configuration documents.
 * 
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WPVSConfigurationDocument extends WPVSCapabilitiesDocument {

    private static final long serialVersionUID = 1511898601495679163L;

    private static final ILogger LOG = LoggerFactory.getLogger( WPVSConfigurationDocument.class );

    private static String PRE_DWPVS = CommonNamespaces.DEEGREEWPVS_PREFIX + ":";

    private static String PRE_OWS = CommonNamespaces.OWS_PREFIX + ":";

    // The smallestMinimalScaleDenomiator is needed to calculate the smallest resolutionstripe
    // possible
    private double smallestMinimalScaleDenominator = Double.MAX_VALUE;

    /**
     * Creates a class representation of the <code>WPVSConfiguration</code> document.
     * 
     * @return Returns a WPVSConfiguration object.
     * @throws InvalidConfigurationException
     */
    public WPVSConfiguration parseConfiguration()
                            throws InvalidConfigurationException {
        WPVSConfiguration wpvsConfiguration = null;
        try {

            // TODO 'contents' field not verified, therefore null! Check spec.
            Element requestedNode = (Element) XMLTools.getRequiredNode( getRootElement(), PRE_DWPVS + "deegreeParams",
                                                                        nsContext );
            WPVSDeegreeParams wpvsDeegreeParams = parseDeegreeParams( requestedNode );

            requestedNode = (Element) XMLTools.getRequiredNode( getRootElement(), PRE_DWPVS + "Dataset", nsContext );
            Dataset rootDataset = parseDataset( requestedNode, null, null, 0, 9E9,
                                                wpvsDeegreeParams.getMinimalWCS_DGMResolution() );

            wpvsConfiguration = new WPVSConfiguration(
                                                       parseVersion(),
                                                       parseUpdateSequence(),
                                                       getServiceIdentification(),
                                                       getServiceProvider(),
                                                       parseOperationsMetadata( wpvsDeegreeParams.getDefaultOnlineResource() ),
                                                       null,
                                                       rootDataset,
                                                       wpvsDeegreeParams,
                                                       ( Double.isInfinite( smallestMinimalScaleDenominator ) ? 1.0
                                                                                                             : smallestMinimalScaleDenominator ) );

        } catch ( XMLParsingException e ) {
            throw new InvalidConfigurationException( e.getMessage() + "\n" + StringTools.stackTraceToString( e ) );

        } catch ( MissingParameterValueException e ) {
            throw new InvalidConfigurationException( e.getMessage() + "\n" + StringTools.stackTraceToString( e ) );

        } catch ( InvalidParameterValueException e ) {
            throw new InvalidConfigurationException( e.getMessage() + "\n" + StringTools.stackTraceToString( e ) );

        } catch ( OGCWebServiceException e ) {
            throw new InvalidConfigurationException( e.getMessage() + "\n" + StringTools.stackTraceToString( e ) );

        } catch ( InvalidConfigurationException e ) {
            throw new InvalidConfigurationException( e.getMessage() + "\n" + StringTools.stackTraceToString( e ) );

        }
        return wpvsConfiguration;
    }

    /**
     * Creates and returns a new <code>WPVSDeegreeParams</code> object from the given <code>Node</code>.
     * 
     * @param deegreeNode
     * @return Returns a new WPVSDeegreeParams object.
     * @throws XMLParsingException
     * @throws InvalidConfigurationException
     */
    private WPVSDeegreeParams parseDeegreeParams( Node deegreeNode )
                            throws XMLParsingException, InvalidConfigurationException {

        Element deegreeElement = (Element) XMLTools.getRequiredNode( deegreeNode, PRE_DWPVS + "DefaultOnlineResource",
                                                                     nsContext );
        OnlineResource defaultOnlineResource = parseOnLineResource( deegreeElement );

        int cacheSize = XMLTools.getNodeAsInt( deegreeNode, PRE_DWPVS + "CacheSize", nsContext, 100 );

        int maxLifeTime = XMLTools.getNodeAsInt( deegreeNode, PRE_DWPVS + "MaxLifeTime", nsContext, 3600 );

        int reqTimeLimit = XMLTools.getNodeAsInt( deegreeNode, PRE_DWPVS + "RequestTimeLimit", nsContext, 60 );
        reqTimeLimit *= 1000;

        int maxTextureDimension = XMLTools.getNodeAsInt( deegreeNode, PRE_DWPVS + "MaxTextureDimension", nsContext,
                                                         Integer.MAX_VALUE );

        int quadMergeCount = XMLTools.getNodeAsInt( deegreeNode, PRE_DWPVS + "QuadMergeCount", nsContext, 10 );

        float viewQuality = (float) XMLTools.getNodeAsDouble( deegreeNode, PRE_DWPVS + "ViewQuality", nsContext, 0.95f );

        int maxMapWidth = XMLTools.getNodeAsInt( deegreeNode, PRE_DWPVS + "MaxViewWidth", nsContext, 1000 );

        int maxMapHeight = XMLTools.getNodeAsInt( deegreeNode, PRE_DWPVS + "MaxViewHeight", nsContext, 1000 );

        String charSet = XMLTools.getNodeAsString( deegreeNode, PRE_DWPVS + "CharacterSet", nsContext, "UTF-8" );

        Node copyrightNode = XMLTools.getNode( deegreeNode, PRE_DWPVS + "Copyright", nsContext );
        String copyright = null;
        boolean isWatermarked = false;
        if ( copyrightNode != null ) {

            Node copyTextNode = XMLTools.getNode( copyrightNode, PRE_DWPVS + "Text", nsContext );
            Node copyURLNode = XMLTools.getNode( copyrightNode, PRE_DWPVS + "ImageURL/@xlink:href", nsContext );

            if ( copyTextNode != null ) {
                copyright = XMLTools.getRequiredNodeAsString( copyrightNode, PRE_DWPVS + "Text/text()", nsContext );
            } else if ( copyURLNode != null ) {
                copyright = XMLTools.getRequiredNodeAsString( copyrightNode, PRE_DWPVS + "ImageURL/@xlink:href",
                                                              nsContext );

                isWatermarked = XMLTools.getNodeAsBoolean( copyrightNode, PRE_DWPVS + "ImageURL/@watermark", nsContext,
                                                           isWatermarked );

                try {
                    copyright = resolve( copyright ).toString();
                } catch ( MalformedURLException e ) {
                    throw new InvalidConfigurationException( "Copyright/ImageURL '" + copyright
                                                             + "' doesn't seem to be a valid URL!" );
                }

            } else {
                throw new InvalidConfigurationException( "Copyright must contain either "
                                                         + "a Text-Element or an ImageURL-Element!" );
            }
        }

        Map<String, URL> backgroundMap = new HashMap<String, URL>( 10 );
        Element backgrounds = (Element) XMLTools.getNode( deegreeNode, PRE_DWPVS + "BackgroundList", nsContext );
        if ( backgrounds != null ) {
            List<Element> backgroundList = XMLTools.getElements( backgrounds, PRE_DWPVS + "Background", nsContext );
            for ( Element background : backgroundList ) {

                String bgName = background.getAttribute( "name" );
                String bgHref = background.getAttribute( "href" );

                if ( bgName == null || bgName.length() == 0 || bgHref == null || bgHref.length() == 0 )
                    throw new InvalidConfigurationException(
                                                             "Background must contain a 'name' and a "
                                                                                     + " 'href' attribute, both if which must contain non-empty strings." );

                try {

                    backgroundMap.put( bgName, resolve( bgHref ) );
                } catch ( MalformedURLException e ) {
                    throw new InvalidConfigurationException( "Background", e.getMessage() );
                }
            }

        }

        boolean quality = XMLTools.getNodeAsBoolean( deegreeNode, PRE_DWPVS + "RequestQualityPreferred", nsContext,
                                                     true );
        double maximumFarClippingPlane = XMLTools.getNodeAsDouble( deegreeNode, PRE_DWPVS
                                                                                + "RequestsMaximumFarClippingPlane",
                                                                   nsContext, 15000 );

        double nearClippingPlane = XMLTools.getNodeAsDouble( deegreeNode, PRE_DWPVS + "NearClippingPlane", nsContext, 2 );

        String defaultSplitter = XMLTools.getNodeAsString( deegreeNode, PRE_DWPVS + "DefaultSplitter", nsContext,
                                                           "QUAD" ).toUpperCase();

        double minimalTerrainHeight = XMLTools.getNodeAsDouble( deegreeNode, PRE_DWPVS + "MinimalTerrainHeight",
                                                                nsContext, 0 );

        double minimalWCS_DGMResolution = XMLTools.getNodeAsDouble( deegreeNode,
                                                                    PRE_DWPVS + "MinimalWCSElevationModelResolution",
                                                                    nsContext, 0 );

        double extendRequestPercentage = XMLTools.getNodeAsDouble( deegreeNode, PRE_DWPVS + "ExtendRequestPercentage",
                                                                   nsContext, 0 );
        if ( extendRequestPercentage > 100 ) {
            LOG.logWarning( Messages.getMessage( "WPVS_WRONG_EXTEND_REQUEST_PERCENTAGE",
                                                 Double.valueOf( extendRequestPercentage ), Double.valueOf( 100 ) ) );
            extendRequestPercentage = 100d;
        } else if ( extendRequestPercentage < -0.00000001 ) {
            LOG.logWarning( Messages.getMessage( "WPVS_WRONG_EXTEND_REQUEST_PERCENTAGE",
                                                 Double.valueOf( extendRequestPercentage ), Double.valueOf( 0 ) ) );
            extendRequestPercentage = 0d;
        }

        boolean antialiased = XMLTools.getNodeAsBoolean( deegreeNode, PRE_DWPVS + "RenderAntialiased", nsContext, true );
        WPVSDeegreeParams wpvsDeegreeParams = new WPVSDeegreeParams( defaultOnlineResource, cacheSize, reqTimeLimit,
                                                                     charSet, copyright, isWatermarked, maxLifeTime,
                                                                     viewQuality, backgroundMap, maxMapWidth,
                                                                     maxMapHeight, quality, maximumFarClippingPlane,
                                                                     nearClippingPlane, defaultSplitter,
                                                                     minimalTerrainHeight, minimalWCS_DGMResolution,
                                                                     extendRequestPercentage * 0.01,
                                                                     maxTextureDimension, quadMergeCount, antialiased );

        return wpvsDeegreeParams;
    }

    /**
     * Creates and returns a new <code>Dataset</code> object from the given <code>Element</code> and the parent
     * <code>Dataset</code> object.
     * 
     * @param datasetElement
     * @param parent
     *            may be null if root Dataset
     * @return Returns a new Dataset object.
     * @throws XMLParsingException
     * @throws MissingParameterValueException
     * @throws InvalidParameterValueException
     * @throws OGCWebServiceException
     * @throws InvalidConfigurationException
     */
    private Dataset parseDataset( Element datasetElement, Dataset parent, CoordinateSystem defaultCoordinateSystem,
                                  double defaultMinScaleDonominator, double defaultMaxScaleDenominator,
                                  double minimalWCS_DGMResolution )
                            throws XMLParsingException, MissingParameterValueException, InvalidParameterValueException,
                            OGCWebServiceException, InvalidConfigurationException {
        // attributes
        boolean queryable = XMLTools.getNodeAsBoolean( datasetElement, "@queryable", nsContext, false );
        boolean opaque = XMLTools.getNodeAsBoolean( datasetElement, "@opaque", nsContext, false );
        boolean noSubsets = XMLTools.getNodeAsBoolean( datasetElement, "@noSubsets", nsContext, false );
        int fixedWidth = XMLTools.getNodeAsInt( datasetElement, "@fixedWidth", nsContext, 0 );
        int fixedHeight = XMLTools.getNodeAsInt( datasetElement, "@fixedHeight", nsContext, 0 );

        // elements
        String name = XMLTools.getNodeAsString( datasetElement, PRE_DWPVS + "Name/text()", nsContext, null );
        String title = XMLTools.getRequiredNodeAsString( datasetElement, PRE_DWPVS + "Title/text()", nsContext );
        String abstract_ = XMLTools.getNodeAsString( datasetElement, PRE_DWPVS + "Abstract/text()", nsContext, null );
        Keywords[] keywords = getKeywords( XMLTools.getNodes( datasetElement, PRE_OWS + "Keywords", nsContext ) );
        String[] crsStrings = XMLTools.getNodesAsStrings( datasetElement, PRE_DWPVS + "CRS/text()", nsContext );
        List<CoordinateSystem> crsList = parseCoordinateSystems( crsStrings );

        if ( parent == null ) { // root dataset
            if ( crsList.size() == 0 || crsList.get( 0 ) == null ) {
                throw new InvalidCapabilitiesException( Messages.getMessage( "WPVS_NO_TOPLEVEL_DATASET_CRS", title ) );
            }
            defaultCoordinateSystem = crsList.get( 0 );
        }

        String[] format = XMLTools.getRequiredNodesAsStrings( datasetElement, PRE_DWPVS + "Format/text()", nsContext );
        // wgs84 == mandatory
        Element boundingBoxElement = (Element) XMLTools.getRequiredNode( datasetElement, PRE_OWS + "WGS84BoundingBox",
                                                                         nsContext );
        Envelope wgs84BoundingBox = getWGS84BoundingBoxType( boundingBoxElement );

        Envelope[] boundingBoxes = getBoundingBoxes( datasetElement, defaultCoordinateSystem );
        Dimension[] dimensions = parseDimensions( datasetElement );
        DataProvider dataProvider = parseDataProvider( datasetElement );
        Identifier identifier = parseDatasetIdentifier( datasetElement, PRE_DWPVS + "Identifier" );
        MetaData[] metaData = parseMetaData( datasetElement );
        DatasetReference[] datasetRefs = parseDatasetReferences( datasetElement );
        FeatureListReference[] featureListRefs = parseFeatureListReferences( datasetElement );
        Style[] style = parseStyles( datasetElement );
        double minScaleDenom = XMLTools.getNodeAsDouble( datasetElement, PRE_DWPVS + "MinimumScaleDenominator/text()",
                                                         nsContext, defaultMinScaleDonominator );

        // update the smallestMinimalScaleDenomiator
        if ( minScaleDenom < smallestMinimalScaleDenominator ) {
            smallestMinimalScaleDenominator = minScaleDenom;
        }

        double maxScaleDenom = XMLTools.getNodeAsDouble( datasetElement, PRE_DWPVS + "MaximumScaleDenominator/text()",
                                                         nsContext, defaultMaxScaleDenominator );

        if ( parent == null ) {// toplevel dataset sets the default minScaleDenominator
            defaultMinScaleDonominator = minScaleDenom;
            defaultMaxScaleDenominator = maxScaleDenom;
        }

        if ( minScaleDenom >= maxScaleDenom ) {
            throw new InvalidCapabilitiesException( "MinimumScaleDenominator must be "
                                                    + "less than MaximumScaleDenominator!" );
        }
        CoordinateSystem currentCRS = defaultCoordinateSystem;
        if ( crsList.size() > 0 && crsList.get( 0 ) != null ) {
            currentCRS = crsList.get( 0 );
        }
        ElevationModel elevationModel = parseElevationModel( datasetElement, name, minimalWCS_DGMResolution,
                                                             currentCRS, defaultMinScaleDonominator,
                                                             defaultMaxScaleDenominator );
        AbstractDataSource[] dataSources = parseAbstractDatasources( datasetElement, name, defaultMinScaleDonominator,
                                                                     defaultMaxScaleDenominator, currentCRS );

        // create new root dataset
        Dataset dataset = new Dataset( queryable, opaque, noSubsets, fixedWidth, fixedHeight, name, title, abstract_,
                                       keywords, crsList, format, wgs84BoundingBox, boundingBoxes, dimensions,
                                       dataProvider, identifier, metaData, datasetRefs, featureListRefs, style,
                                       minScaleDenom, maxScaleDenom, null, elevationModel, dataSources, parent );

        // get child datasets
        List<Element> nl = XMLTools.getElements( datasetElement, PRE_DWPVS + "Dataset", nsContext );
        Dataset[] childDatasets = new Dataset[nl.size()];
        for ( int i = 0; i < childDatasets.length; i++ ) {
            childDatasets[i] = parseDataset( nl.get( i ), dataset, defaultCoordinateSystem, defaultMinScaleDonominator,
                                             defaultMaxScaleDenominator, minimalWCS_DGMResolution );

        }

        // set child datasets
        dataset.setDatasets( childDatasets );

        return dataset;
    }

    /**
     * Creates and returns a new <code>ElevationModel</code> object from the given <code>Element</code> and the parent
     * <code>Dataset</code>.
     * 
     * The OGC ElevationModel contains only a String. The Deegree ElevationModel additionally contains a complex
     * dataSource.
     * 
     * @param datasetElement
     * @param parentName
     * @return Returns the ElevationModel object.
     * @throws XMLParsingException
     * @throws OGCWebServiceException
     * @throws InvalidParameterValueException
     * @throws MissingParameterValueException
     * @throws InvalidConfigurationException
     */
    private ElevationModel parseElevationModel( Element datasetElement, String parentName,
                                                double minimalWCS_DGMResolution, CoordinateSystem defaultCRS,
                                                double defaultMinScaleDenominator, double defaultMaxScaleDenominator )
                            throws XMLParsingException, MissingParameterValueException, InvalidParameterValueException,
                            OGCWebServiceException, InvalidConfigurationException {

        Element elevationElement = null;
        String name = null;
        ElevationModel elevationModel = null;

        elevationElement = (Element) XMLTools.getNode( datasetElement, PRE_DWPVS + "ElevationModel", nsContext );

        AbstractDataSource[] dataSources = null;
        if ( elevationElement != null ) {

            name = XMLTools.getRequiredNodeAsString( elevationElement, PRE_DWPVS + "Name/text()", nsContext );

            dataSources = parseAbstractDatasources( elevationElement, parentName, defaultMinScaleDenominator,
                                                    defaultMaxScaleDenominator, defaultCRS );
            if ( dataSources.length < 1 ) {
                throw new InvalidCapabilitiesException( "Each '" + elevationElement.getNodeName()
                                                        + "' must contain at least one data source!" );
            }
            if ( !Double.isNaN( minimalWCS_DGMResolution ) ) {
                // little trick to know which dgm datasources have a configured minimal resolution,
                // if the minimalWCS_DGMResolution is not set (e.g Double.nan) nothing has to be
                // done (a value of 0d is presumed)
                for ( AbstractDataSource source : dataSources ) {
                    if ( source.getServiceType() == AbstractDataSource.LOCAL_WCS
                         || source.getServiceType() == AbstractDataSource.REMOTE_WCS ) {
                        ( (LocalWCSDataSource) source ).setConfiguredMinimalDGMResolution( minimalWCS_DGMResolution );
                    }
                }
            }
        }

        elevationModel = new ElevationModel( name, dataSources );

        return elevationModel;
    }

    /**
     * Creates and returns a new array of <code>AbstractDataSource</code> objects from the given <code>Element</code>.
     * 
     * If the objects are used within an ElevationModel object, they may be of the following types: LocalWCSDataSource,
     * RemoteWCSDataSource, LocalWFSDataSource, RemoteWFSDataSource. If the objects are used within a Dataset object,
     * they may additionaly be of the types: LocalWMSDataSource, RemoteWMSDataSource.
     * 
     * @param element
     * @return Returns a new array of AbstractDataSource objects.
     * @throws XMLParsingException
     * @throws OGCWebServiceException
     * @throws InvalidConfigurationException
     */
    private AbstractDataSource[] parseAbstractDatasources( Element element, String parentName,
                                                           double defaultMinScaleDenominator,
                                                           double defaultMaxScaleDenominator,
                                                           CoordinateSystem defaultCRS )
                            throws XMLParsingException, OGCWebServiceException, InvalidConfigurationException {

        List<Element> abstractDataSources = XMLTools.getElements( element, "*", nsContext );
        List<AbstractDataSource> tempDataSources = new ArrayList<AbstractDataSource>( abstractDataSources.size() );

        for ( Element dataSourceElement : abstractDataSources ) {

            // String nodeName = dataSourceElement.getNodeName();
            String nodeName = dataSourceElement.getLocalName();

            if ( nodeName != null && nodeName.endsWith( "DataSource" ) ) {
                QualifiedName pn = null;
                if ( parentName != null ) {
                    pn = new QualifiedName( PRE_DWPVS, parentName, nsContext.getURI( PRE_DWPVS ) );
                }
                QualifiedName name = XMLTools.getNodeAsQualifiedName( dataSourceElement, PRE_DWPVS + "Name/text()",
                                                                      nsContext, pn );

                OWSCapabilities owsCapabilities = parseOWSCapabilities( dataSourceElement );

                double minScaleDenom = XMLTools.getNodeAsDouble( dataSourceElement, PRE_DWPVS
                                                                                    + "MinimumScaleDenominator/text()",
                                                                 nsContext, defaultMinScaleDenominator );

                // update the smallestMinimalScaleDenomiator
                if ( minScaleDenom < smallestMinimalScaleDenominator )
                    smallestMinimalScaleDenominator = minScaleDenom;

                double maxScaleDenom = XMLTools.getNodeAsDouble( dataSourceElement, PRE_DWPVS
                                                                                    + "MaximumScaleDenominator/text()",
                                                                 nsContext, defaultMaxScaleDenominator );

                Surface validArea = parseValidArea( dataSourceElement, defaultCRS );
                AbstractDataSource dataSource = null;

                if ( nodeName.equals( "LocalWCSDataSource" ) || "RemoteWCSDataSource".equals( nodeName ) ) {
                    Element filterElement = (Element) XMLTools.getRequiredNode( dataSourceElement, PRE_DWPVS
                                                                                                   + "FilterCondition",
                                                                                nsContext );
                    GetCoverage getCoverage = parseWCSFilterCondition( filterElement );
                    Color[] transparentColors = parseTransparentColors( dataSourceElement );

                    if ( "RemoteWCSDataSource".equals( nodeName ) ) {
                        dataSource = new RemoteWCSDataSource( name, owsCapabilities, validArea, minScaleDenom,
                                                              maxScaleDenom, getCoverage, transparentColors );
                        LOG.logDebug( "created remote wcs with name: " + name );
                    } else {
                        dataSource = new LocalWCSDataSource( name, owsCapabilities, validArea, minScaleDenom,
                                                             maxScaleDenom, getCoverage, transparentColors );
                        LOG.logDebug( "created local wcs with name: " + name );
                    }
                } else if ( "RemoteWFSDataSource".equals( nodeName ) || "LocalWFSDataSource".equals( nodeName ) ) {
                    Text geoPropNode = (Text) XMLTools.getRequiredNode( dataSourceElement, PRE_DWPVS
                                                                                           + "GeometryProperty/text()",
                                                                        nsContext );
                    PropertyPath geometryProperty = parsePropertyPath( geoPropNode );

                    Element filterElement = (Element) XMLTools.getNode( dataSourceElement,
                                                                        PRE_DWPVS + "FilterCondition/ogc:Filter",
                                                                        nsContext );

                    int maxFeatures = XMLTools.getNodeAsInt( dataSourceElement, PRE_DWPVS + "MaxFeatures", nsContext,
                                                             -1 );

                    Filter filterCondition = null;
                    if ( filterElement != null ) {
                        filterCondition = AbstractFilter.buildFromDOM( filterElement, false );
                    }

                    // FeatureCollectionAdapter adapter = createFCAdapterFromAdapterClassName(
                    // dataSourceElement );

                    if ( "LocalWFSDataSource".equals( nodeName ) ) {
                        dataSource = new LocalWFSDataSource( name, owsCapabilities, validArea, minScaleDenom,
                                                             maxScaleDenom, geometryProperty, filterCondition,
                                                             maxFeatures );
                        LOG.logDebug( "created local wfs with name: " + name );
                    } else {
                        dataSource = new RemoteWFSDataSource( name, owsCapabilities, validArea, minScaleDenom,
                                                              maxScaleDenom, geometryProperty, filterCondition,
                                                              maxFeatures );
                        LOG.logDebug( "created remote wfs with name: " + name );
                    }
                } else if ( nodeName.equals( "LocalWMSDataSource" ) ) {
                    if ( element.getNodeName().endsWith( "ElevationModel" ) ) {
                        throw new InvalidConfigurationException( "An ElevationModel cannot "
                                                                 + "contain a LocalWMSDataSource!" );
                    }
                    Element filterElement = (Element) XMLTools.getRequiredNode( dataSourceElement, PRE_DWPVS
                                                                                                   + "FilterCondition",
                                                                                nsContext );
                    GetMap getMap = parseWMSFilterCondition( filterElement );

                    Color[] transparentColors = parseTransparentColors( dataSourceElement );

                    dataSource = new LocalWMSDataSource( name, owsCapabilities, validArea, minScaleDenom,
                                                         maxScaleDenom, getMap, transparentColors );
                    LOG.logDebug( "created local wms with name: " + name );

                } else if ( nodeName.equals( "RemoteWMSDataSource" ) ) {
                    if ( element.getNodeName().endsWith( "ElevationModel" ) ) {
                        throw new InvalidConfigurationException( "An ElevationModel cannot "
                                                                 + "contain a LocalWMSDataSource!" );
                    }
                    Element filterElement = (Element) XMLTools.getRequiredNode( dataSourceElement, PRE_DWPVS
                                                                                                   + "FilterCondition",
                                                                                nsContext );
                    GetMap getMap = parseWMSFilterCondition( filterElement );

                    Color[] transparentColors = parseTransparentColors( dataSourceElement );

                    dataSource = new RemoteWMSDataSource( name, owsCapabilities, validArea, minScaleDenom,
                                                          maxScaleDenom, getMap, transparentColors );
                    LOG.logDebug( "created remote wms with name: " + name );
                } else {
                    throw new InvalidCapabilitiesException( "Unknown data source: '" + nodeName + "'" );
                }

                tempDataSources.add( dataSource );
            }
        }

        AbstractDataSource[] dataSources = tempDataSources.toArray( new AbstractDataSource[tempDataSources.size()] );

        return dataSources;
    }

    /**
     * FIXME check content of StringBuffer and Map! This is an adapted copy from:
     * org.deegree.ogcwebservices.wms.configuration#parseWMSFilterCondition(Node)
     * 
     * Creates and returns a new <code>GetMap</code> object from the given <code>Element</code>.
     * 
     * @param filterElement
     * @return a partial wms GetMap request instance
     * @throws XMLParsingException
     */
    private GetMap parseWMSFilterCondition( Element filterElement )
                            throws XMLParsingException {

        GetMap getMap = null;

        String wmsRequest = XMLTools.getRequiredNodeAsString( filterElement, PRE_DWPVS + "WMSRequest/text()", nsContext );

        StringBuffer sd = new StringBuffer( 1000 );
        sd.append( "REQUEST=GetMap&LAYERS=%default%&STYLES=&SRS=EPSG:4326&" );
        sd.append( "BBOX=0,0,1,1&WIDTH=1&HEIGHT=1&FORMAT=%default%" );

        Map<String, String> map1 = KVP2Map.toMap( sd.toString() );

        Map<String, String> map2 = KVP2Map.toMap( wmsRequest );
        if ( map2.get( "VERSION" ) == null && map2.get( "WMTVER" ) == null ) {
            map2.put( "VERSION", "1.1.1" );
        }
        // if no service is set use WMS as default
        if ( map2.get( "SERVICE" ) == null ) {
            map2.put( "SERVICE", "WMS" );
        }

        map1.putAll( map2 );

        String id = Long.toString( IDGenerator.getInstance().generateUniqueID() );
        map1.put( "ID", id );
        try {
            getMap = GetMap.create( map1 );
        } catch ( Exception e ) {
            throw new XMLParsingException( "could not create GetMap from WMS FilterCondition", e );
        }

        return getMap;
    }

    /**
     * FIXME check content of StringBuffer ! This is an adapted copy from:
     * org.deegree.ogcwebservices.wms.configuration#parseWCSFilterCondition(Node)
     * 
     * Creates and returns a new <code>GetCoverage</code> object from the given <code>Element</code>.
     * 
     * @param filterElement
     * @return a partial GetCoverage request
     * @throws XMLParsingException
     */
    private GetCoverage parseWCSFilterCondition( Element filterElement )
                            throws XMLParsingException {

        GetCoverage coverage = null;

        String wcsRequest = XMLTools.getRequiredNodeAsString( filterElement, PRE_DWPVS + "WCSRequest/text()", nsContext );

        StringBuffer sd = new StringBuffer( 1000 );
        sd.append( "version=1.0.0&Coverage=%default%&CRS=EPSG:4326&BBOX=0,0,1,1" );
        sd.append( "&Width=1&Height=1&Format=%default%&" );
        sd.append( wcsRequest );

        String id = "" + IDGenerator.getInstance().generateUniqueID();

        try {
            coverage = GetCoverage.create( id, sd.toString() );
        } catch ( Exception e ) {
            throw new XMLParsingException( "Could not create GetCoverage " + "from WPVS FilterCondition", e );
        }

        return coverage;
    }

    /**
     * Creates and returns a new <code>OWSCapabilities</code> object from the given <code>Element</code>.
     * 
     * @param element
     * @return Returns a new OWSCapabilities object.
     * @throws XMLParsingException
     * @throws InvalidCapabilitiesException
     */
    private OWSCapabilities parseOWSCapabilities( Element element )
                            throws XMLParsingException, InvalidCapabilitiesException {

        Element owsCapabilitiesElement = (Element) XMLTools.getRequiredNode( element, PRE_DWPVS + "OWSCapabilities",
                                                                             nsContext );

        String format = null;

        // FIXME
        // schema has onlineResourceType as not optional, so it should be mandatory.
        // but in other examples onlineResource is never created with this onlineResourceType.
        // therefore it gets omitted here, too.

        // String onlineResourceType = XMLTools.getRequiredNodeAsString(
        // owsCapabilitiesElement, PRE_DWPVS+"OnlineResource/@xlink:type", nsContext );

        String onlineResourceURI = XMLTools.getRequiredNodeAsString( owsCapabilitiesElement,
                                                                     PRE_DWPVS + "OnlineResource/@xlink:href",
                                                                     nsContext );

        URL onlineResource;
        try {
            onlineResource = resolve( onlineResourceURI );
        } catch ( MalformedURLException e ) {
            throw new InvalidCapabilitiesException( onlineResourceURI + " does not represent a valid URL: "
                                                    + e.getMessage() );
        }
        LOG.logDebug( "found following onlineResource: " + onlineResource );
        return new OWSCapabilities( format, onlineResource );

        // FIXME
        // if onlineResourceType is going to be used, the returned new OnlineResource should be
        // created with different constructor:
        // return new OWSCapabilities( format, onlineResourceType, onlineResource );
    }

    /**
     * Creates and returns a new <code>Geometry</code> object from the given Element.
     * 
     * @param dataSource
     * @return Returns a new Geometry object.
     * @throws XMLParsingException
     * @throws InvalidConfigurationException
     */
    private Surface parseValidArea( Element dataSource, CoordinateSystem defaultCRS )
                            throws XMLParsingException, InvalidConfigurationException {
        List<Element> nl = XMLTools.getElements( dataSource, PRE_DWPVS + "ValidArea/*", nsContext );
        if ( nl.size() == 0 ) {
            return null;
        }

        if ( nl.size() > 1 ) {
            LOG.logWarning( Messages.getMessage( "WPVS_WRONG_NUMBER_OF_VALID_AREAS", GMLNS.toASCIIString(),
                                                 dataSource.getLocalName() ) );
            return null;
        }
        Surface validArea = null;
        Element firsElement = nl.get( 0 );
        if ( firsElement.getNamespaceURI().equals( GMLNS.toASCIIString() ) ) {
            try {
                String srsName = XMLTools.getNodeAsString( firsElement, "@srsName", nsContext, null );
                if ( srsName == null ) {
                    srsName = defaultCRS.getFormattedString();
                }

                Geometry geom = GMLGeometryAdapter.wrap( firsElement, srsName );
                if ( !( geom instanceof Surface ) ) {
                    throw new InvalidConfigurationException( Messages.getMessage( "WPVS_WRONG_GEOMTERY_VALID_AREA",
                                                                                  dataSource.getLocalName() ) );
                }
                validArea = (Surface) geom;
            } catch ( GeometryException e ) {
                throw new InvalidConfigurationException( Messages.getMessage( "WPVS_WRONG_GEOMTERY_VALID_AREA",
                                                                              dataSource.getLocalName() )
                                                         + "\n" + e.getMessage(),

                e );
            }

        } else if ( URI.create( firsElement.getNamespaceURI() ).equals( CommonNamespaces.OWSNS ) ) {
            try {
                if ( !"BoundingBox".equals( firsElement.getLocalName() ) ) {
                    throw new InvalidConfigurationException( Messages.getMessage( "WPVS_WRONG_GEOMTERY_VALID_AREA",
                                                                                  dataSource.getLocalName() ) );
                }
                Envelope env = parseBoundingBox( firsElement, null );
                validArea = GeometryFactory.createSurface( env, env.getCoordinateSystem() );
            } catch ( GeometryException e ) {
                throw new InvalidConfigurationException( Messages.getMessage( "WPVS_WRONG_GEOMTERY_VALID_AREA",
                                                                              dataSource.getLocalName() )
                                                         + "\n" + e.getMessage(), e );
            } catch ( InvalidParameterValueException e ) {
                throw new InvalidConfigurationException( Messages.getMessage( "WPVS_WRONG_GEOMTERY_VALID_AREA",
                                                                              dataSource.getLocalName() )
                                                         + "\n" + e.getMessage(), e );
            }
        } else {
            LOG.logWarning( Messages.getMessage( "WPVS_WRONG_GEOMTERY_VALID_AREA", dataSource.getLocalName() ) );
            return null;
        }
        return validArea;
    }

    /**
     * Creates and returns a new array of <code>Color</code> objects from the given Element.
     * 
     * @param dataSourceElement
     * @return Returns a new array of Color objects.
     * @throws XMLParsingException
     */
    private Color[] parseTransparentColors( Element dataSourceElement )
                            throws XMLParsingException {

        List<Element> colorList = XMLTools.getElements( dataSourceElement, PRE_DWPVS + "TransparentColors/" + PRE_DWPVS
                                                                           + "Color", nsContext );

        Color[] transparentColors = null;
        if ( colorList != null ) {
            transparentColors = new Color[colorList.size()];

            for ( int i = 0; i < transparentColors.length; i++ ) {

                Element colorElement = colorList.get( i );
                String color = XMLTools.getRequiredNodeAsString( colorElement, "text()", nsContext );

                transparentColors[i] = Color.decode( color );
            }
        }

        return transparentColors;
    }

}

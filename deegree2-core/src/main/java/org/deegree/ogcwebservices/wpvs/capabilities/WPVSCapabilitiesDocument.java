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

package org.deegree.ogcwebservices.wpvs.capabilities;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import org.deegree.datatypes.values.TypedLiteral;
import org.deegree.datatypes.values.ValueRange;
import org.deegree.datatypes.xlink.SimpleLink;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.metadata.iso19115.Keywords;
import org.deegree.model.metadata.iso19115.OnlineResource;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.HTTP;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.Protocol;
import org.deegree.owscommon.OWSCommonCapabilitiesDocument;
import org.deegree.owscommon.OWSMetadata;
import org.deegree.owscommon.com110.HTTP110;
import org.deegree.owscommon.com110.OWSAllowedValues;
import org.deegree.owscommon.com110.OWSDomainType110;
import org.deegree.owscommon.com110.OWSRequestMethod;
import org.deegree.owscommon.com110.Operation110;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * This class represents a <code>WPVSCapabilitiesDocument</code> object.
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * $Revision$, $Date$
 *
 */
public class WPVSCapabilitiesDocument extends OWSCommonCapabilitiesDocument {

    private ArrayList<String> datasetIdentifiers = new ArrayList<String>();

    private ArrayList<String> styleIdentifiers = new ArrayList<String>();

    /**
     *
     */
    private static final long serialVersionUID = 2633513531080190745L;

    private static final ILogger LOG = LoggerFactory.getLogger( WPVSCapabilitiesDocument.class );

    private static final String XML_TEMPLATE = "WPVSCapabilitiesTemplate.xml";

    private static String PRE_WPVS = CommonNamespaces.WPVS_PREFIX + ":";

    private static String PRE_OWS = CommonNamespaces.OWS_PREFIX + ":";

    /**
     * Creates a skeleton capabilities document that contains the mandatory elements only.
     *
     * @throws IOException
     * @throws SAXException
     */
    public void createEmptyDocument()
                                     throws IOException, SAXException {
        URL url = WPVSCapabilitiesDocument.class.getResource( XML_TEMPLATE );
        if ( url == null ) {
            throw new IOException( "The resource '" + XML_TEMPLATE + " could not be found." );
        }
        load( url );
    }

    /**
     * @see org.deegree.ogcwebservices.getcapabilities.OGCCapabilitiesDocument#parseCapabilities()
     */
    @Override
    public OGCCapabilities parseCapabilities()
                                              throws InvalidCapabilitiesException {
        WPVSCapabilities wpvsCapabilities = null;

        try {
            wpvsCapabilities = new WPVSCapabilities( parseVersion(),
                                                     parseUpdateSequence(),
                                                     getServiceIdentification(),
                                                     getServiceProvider(),
                                                     parseOperationsMetadata( null ),
                                                     null,
                                                     getDataset() );

        } catch ( XMLParsingException e ) {
            throw new InvalidCapabilitiesException( e.getMessage() + "\n" + StringTools.stackTraceToString( e ) );

        } catch ( MissingParameterValueException e ) {
            throw new InvalidCapabilitiesException( e.getMessage() + "\n" + StringTools.stackTraceToString( e ) );

        } catch ( InvalidParameterValueException e ) {
            throw new InvalidCapabilitiesException( e.getMessage() + "\n" + StringTools.stackTraceToString( e ) );

        } catch ( OGCWebServiceException e ) {
            throw new InvalidCapabilitiesException( e.getMessage() + "\n" + StringTools.stackTraceToString( e ) );
        }

        return wpvsCapabilities;
    }

    /**
     * Gets the <code>Dataset</code> object from the root element of the WPVSCapabilities element.
     *
     *
     * @return Returns the Dataset object form root element.
     * @throws XMLParsingException
     * @throws OGCWebServiceException
     * @throws InvalidParameterValueException
     * @throws MissingParameterValueException
     */
    private Dataset getDataset()
                                throws XMLParsingException, MissingParameterValueException,
                                InvalidParameterValueException, OGCWebServiceException {

        Element datasetElement = (Element) XMLTools.getRequiredNode( getRootElement(), PRE_WPVS + "Dataset", nsContext );
        Dataset dataset = parseDataset( datasetElement, null, null, null );

        return dataset;
    }

    /**
     * Creates and returns a new <code>Dataset</code> object from the given <code>Element</code> and the parent
     * <code>Dataset</code> object.
     *
     * @param datasetElement
     * @param parent
     *            may be null if root node
     * @param defaultCoordinateSystem
     * @param defaultElevationModel
     * @return Returns a new Dataset object.
     * @throws XMLParsingException
     * @throws OGCWebServiceException
     * @throws InvalidParameterValueException
     */
    private Dataset parseDataset( Element datasetElement, Dataset parent, CoordinateSystem defaultCoordinateSystem,
                                  ElevationModel defaultElevationModel )
                                                                        throws XMLParsingException,
                                                                        InvalidParameterValueException,
                                                                        OGCWebServiceException {

        // attributes are all optional
        boolean queryable = XMLTools.getNodeAsBoolean( datasetElement, "@queryable", nsContext, false );
        boolean opaque = XMLTools.getNodeAsBoolean( datasetElement, "@opaque", nsContext, false );
        boolean noSubsets = XMLTools.getNodeAsBoolean( datasetElement, "@noSubsets", nsContext, false );
        int fixedWidth = XMLTools.getNodeAsInt( datasetElement, "@fixedWidth", nsContext, 0 );
        int fixedHeight = XMLTools.getNodeAsInt( datasetElement, "@fixedHeight", nsContext, 0 );

        // elements
        String name = XMLTools.getNodeAsString( datasetElement, PRE_WPVS + "Name/text()", nsContext, null );
        String title = XMLTools.getRequiredNodeAsString( datasetElement, PRE_WPVS + "Title/text()", nsContext );
        String abstract_ = XMLTools.getNodeAsString( datasetElement, PRE_WPVS + "Abstract/text()", nsContext, null );
        // keywords == optional
        Keywords[] keywords = getKeywords( XMLTools.getNodes( datasetElement, PRE_OWS + "Keywords", nsContext ) );
        // crsstrings == optional
        String[] crsStrings = XMLTools.getNodesAsStrings( datasetElement, PRE_WPVS + "CRS/text()", nsContext );
        List<CoordinateSystem> crsList = parseCoordinateSystems( crsStrings );
        ElevationModel elevationModel = parseElevationModel( datasetElement, defaultElevationModel );
        // create a default ElevationModel if not exists allready, a little HACK to circumvent the
        // optional deegree:dataset:ElevationModel mapping onto the mandatory
        // ogc:dataset:ElevationModel
        if ( defaultElevationModel == null && elevationModel != null ) {
            defaultElevationModel = elevationModel;
            // found an ElevationModel setting it to default
            // and update the parents of this dataset, until we have the root,

            if ( parent != null ) {
                // first find root parent
                Dataset tmpParent = parent;
                while ( tmpParent.getParent() != null ) {
                    tmpParent = tmpParent.getParent();
                }
                // now iterate over all so far created children to set an default elevationmodel
                tmpParent.setElevationModel( defaultElevationModel );
                Queue<Dataset> children = new LinkedBlockingQueue<Dataset>( Arrays.asList( tmpParent.getDatasets() ) );
                while ( !children.isEmpty() ) {
                    Dataset child = children.poll();
                    if ( child != null ) {
                        child.setElevationModel( defaultElevationModel );
                        for ( Dataset dataset : child.getDatasets() ) {
                            children.offer( dataset );
                        }
                    }
                }
            }
        }
        // now find a defaultcoordinatesystem to use if no crs is given in a child dataset
        if ( parent == null ) { // root dataset
            if ( crsList.size() == 0 || crsList.get( 0 ) == null ) {
                throw new InvalidCapabilitiesException( Messages.getMessage( "WPVS_NO_TOPLEVEL_DATASET_CRS", title ) );
            }
            defaultCoordinateSystem = crsList.get( 0 );
        }

        String[] format = XMLTools.getRequiredNodesAsStrings( datasetElement, PRE_WPVS + "Format/text()", nsContext );
        // wgs84 == mandatory
        Element boundingBoxElement = (Element) XMLTools.getRequiredNode( datasetElement,
                                                                         PRE_OWS + "WGS84BoundingBox",
                                                                         nsContext );
        Envelope wgs84BoundingBox = getWGS84BoundingBoxType( boundingBoxElement );

        // boundingboxes can be used to make a more precise specification of the useable area of
        // this dataset in it's native crs's, the wgs84 bbox can be inaccurate
        Envelope[] boundingBoxes = getBoundingBoxes( datasetElement, defaultCoordinateSystem );

        // optional
        Dimension[] dimensions = parseDimensions( datasetElement );

        // optional
        DataProvider dataProvider = parseDataProvider( datasetElement );

        // mandatory
        Identifier identifier = parseDatasetIdentifier( datasetElement, PRE_WPVS + "Identifier" );

        // optional
        MetaData[] metaData = parseMetaData( datasetElement );

        // optional
        DatasetReference[] datasetRefs = parseDatasetReferences( datasetElement );

        // optional
        FeatureListReference[] featureListRefs = parseFeatureListReferences( datasetElement );

        // optional
        Style[] style = parseStyles( datasetElement );

        // mandatory
        double minScaleDenom = XMLTools.getRequiredNodeAsDouble( datasetElement,
                                                                 PRE_WPVS + "MinimumScaleDenominator/text()",
                                                                 nsContext );
        // mandatory
        double maxScaleDenom = XMLTools.getRequiredNodeAsDouble( datasetElement,
                                                                 PRE_WPVS + "MaximumScaleDenominator/text()",
                                                                 nsContext );

        if ( minScaleDenom > maxScaleDenom ) {
            throw new InvalidCapabilitiesException( Messages.getMessage( "WPVS_WRONG_SCALE_DENOMINATORS" ) );
        }

        // create new root dataset
        Dataset dataset = new Dataset( queryable,
                                       opaque,
                                       noSubsets,
                                       fixedWidth,
                                       fixedHeight,
                                       name,
                                       title,
                                       abstract_,
                                       keywords,
                                       crsList,
                                       format,
                                       wgs84BoundingBox,
                                       boundingBoxes,
                                       dimensions,
                                       dataProvider,
                                       identifier,
                                       metaData,
                                       datasetRefs,
                                       featureListRefs,
                                       style,
                                       minScaleDenom,
                                       maxScaleDenom,
                                       null,
                                       elevationModel,
                                       null,
                                       parent );

        // get child datasets
        List<Node> nl = XMLTools.getNodes( datasetElement, PRE_WPVS + "Dataset", nsContext );
        Dataset[] childDatasets = new Dataset[nl.size()];
        for ( int i = 0; i < childDatasets.length; i++ ) {
            childDatasets[i] = parseDataset( (Element) nl.get( i ),
                                             dataset,
                                             defaultCoordinateSystem,
                                             defaultElevationModel );
        }

        // set child datasets
        dataset.setDatasets( childDatasets );

        return dataset;
    }

    /**
     * @param coordinateStrings
     *            the Strings to create the coordinates from
     * @return a List of coordinatesystems, if no coordinateString were given (null || length==0 ) an emtpy list is
     *         returned.
     */
    protected List<CoordinateSystem> parseCoordinateSystems( String[] coordinateStrings ) {
        if ( coordinateStrings == null )
            return new ArrayList<CoordinateSystem>();
        ArrayList<CoordinateSystem> crsList = new ArrayList<CoordinateSystem>( coordinateStrings.length );
        for ( String tmpCRS : coordinateStrings ) {
            try {
                CoordinateSystem crs = CRSFactory.create( tmpCRS );
                crsList.add( crs );
            } catch ( UnknownCRSException e ) {
                // fail configuration notify the user
                LOG.logError( e.getLocalizedMessage(), e );
            }
        }
        return crsList;
    }

    /**
     * Creates and returns a new <code>ElevationModel</code> object from the given <code>Element</code>.
     *
     * This OGC ElevationModel contains only a String. ATTENTION ogc elevation model is mandatory, we IGNORE this and
     * say it's mandatory.
     *
     * @param datasetElement
     * @param defaultElevationModel
     *            the defaultElevationModel will be used if no elevationmodel was defined in this dataset(for example
     *            the topdatasets dgm-name)
     * @return Returns the ElevationModel object or <code>null</code> if none defined (optional)
     * @throws XMLParsingException
     */
    private ElevationModel parseElevationModel( Element datasetElement, ElevationModel defaultElevationModel )
                                                                                                              throws XMLParsingException {
        // ATTENTION ogc elevation model is mandatory, we IGNORE it for the capabilities.
        String name = XMLTools.getNodeAsString( datasetElement, PRE_WPVS + "ElevationModel/text()", nsContext, null );
        if ( name == null ) {
            if ( defaultElevationModel == null ) {
                return null;
            }
            return defaultElevationModel;
        }
        ElevationModel elevationModel = new ElevationModel( name );

        return elevationModel;
    }

    /**
     * Creates and returns a new array of <code>Style</code> objects from the given <code>Element</code>.
     *
     * @param datasetElement
     * @return Returns a new array of Style objects or <code>null</code> if none were defined
     * @throws XMLParsingException
     * @throws InvalidCapabilitiesException
     */
    protected Style[] parseStyles( Element datasetElement )
                                                           throws XMLParsingException, InvalidCapabilitiesException {

        List<Node> styleList = XMLTools.getNodes( datasetElement, PRE_WPVS + "Style", nsContext );
        // optional therefore return null if not present
        if ( styleList.size() == 0 ) {
            return null;
        }

        Style[] styles = new Style[styleList.size()];

        for ( int i = 0; i < styles.length; i++ ) {

            Element styleElement = (Element) styleList.get( i );

            String name = XMLTools.getRequiredNodeAsString( styleElement, PRE_WPVS + "Name/text()", nsContext );
            String title = XMLTools.getRequiredNodeAsString( styleElement, PRE_WPVS + "Title/text()", nsContext );
            String abstract_ = XMLTools.getRequiredNodeAsString( styleElement, PRE_WPVS + "Abstract/text()", nsContext );
            // optional
            Keywords[] keywords = getKeywords( XMLTools.getNodes( styleElement, PRE_OWS + "Keywords", nsContext ) );
            // mandatory
            Identifier identifier = parseStyleIdentifier( styleElement, PRE_WPVS + "Identifier" );

            // optional
            LegendURL[] legendURLs = parseLegendURLs( styleElement );

            // optional
            StyleSheetURL styleSheetURL = parseStyleSheetURL( styleElement );
            StyleURL styleURL = parseStyleURL( styleElement );

            styles[i] = new Style( name, title, abstract_, keywords, identifier, legendURLs, styleSheetURL, styleURL );
        }

        return styles;
    }

    /**
     * Creates and returns a new <code>StyleURL</code> object from the given <code>Element</code>.
     *
     * @param styleElement
     * @return Returns a new StyleURL object or <code>null</code> if not defined (optional)
     * @throws XMLParsingException
     * @throws InvalidCapabilitiesException
     */
    private StyleURL parseStyleURL( Element styleElement )
                                                          throws XMLParsingException, InvalidCapabilitiesException {

        Element styleURLElement = (Element) XMLTools.getNode( styleElement, PRE_WPVS + "StyleURL", nsContext );
        if ( styleURLElement == null ) {
            return null;
        }
        String format = XMLTools.getRequiredNodeAsString( styleURLElement, PRE_WPVS + "Format/text()", nsContext );

        // optional
        URI onlineResourceURI = XMLTools.getNodeAsURI( styleURLElement,
                                                       PRE_WPVS + "OnlineResource/@xlink:href",
                                                       nsContext,
                                                       null );
        URL onlineResource = null;
        if ( onlineResourceURI != null ) {
            try {
                onlineResource = onlineResourceURI.toURL();
            } catch ( MalformedURLException e ) {
                throw new InvalidCapabilitiesException( onlineResourceURI + " does not represent a valid URL: "
                                                        + e.getMessage() );
            }
        }
        return new StyleURL( format, onlineResource );
    }

    /**
     * Creates and returns a new <code>StyleSheetURL</code> object from the given <code>Element</code>.
     *
     * @param styleElement
     * @return Returns a new StyleSheetURL object or <code>null</code> if no stylSheetURL was given (optional)
     * @throws XMLParsingException
     * @throws InvalidCapabilitiesException
     */
    private StyleSheetURL parseStyleSheetURL( Element styleElement )
                                                                    throws XMLParsingException,
                                                                    InvalidCapabilitiesException {

        Element styleSheetURLElement = (Element) XMLTools.getNode( styleElement, PRE_WPVS + "StyleSheetURL", nsContext );
        if ( styleSheetURLElement == null ) {
            return null;
        }
        String format = XMLTools.getRequiredNodeAsString( styleSheetURLElement, PRE_WPVS + "Format/text()", nsContext );

        // optional onlineResource
        URI onlineResourceURI = XMLTools.getNodeAsURI( styleSheetURLElement,
                                                       PRE_WPVS + "OnlineResource/@xlink:href",
                                                       nsContext,
                                                       null );
        URL onlineResource = null;
        if ( onlineResourceURI != null ) {
            try {
                onlineResource = onlineResourceURI.toURL();
            } catch ( MalformedURLException e ) {
                throw new InvalidCapabilitiesException( onlineResourceURI + " does not represent a valid URL: "
                                                        + e.getMessage() );
            }
        }

        return new StyleSheetURL( format, onlineResource );
    }

    /**
     * Creates and returns a new array of <code>LegendURL</code> objects from the given <code>Element</code>.
     *
     * @param styleElement
     * @return Returns a new array of LegendURL objects or <code>null</code> if none were defined (optional).
     * @throws XMLParsingException
     * @throws InvalidCapabilitiesException
     */
    private LegendURL[] parseLegendURLs( Element styleElement )
                                                               throws XMLParsingException, InvalidCapabilitiesException {

        List<Node> legendList = XMLTools.getNodes( styleElement, PRE_WPVS + "LegendURL", nsContext );
        if ( legendList.size() == 0 ) {
            return null;
        }

        LegendURL[] legendURLs = new LegendURL[legendList.size()];

        for ( int i = 0; i < legendURLs.length; i++ ) {

            Element legendURLElement = (Element) legendList.get( i );

            int width = XMLTools.getRequiredNodeAsInt( legendURLElement, "@width", nsContext );
            int height = XMLTools.getRequiredNodeAsInt( legendURLElement, "@height", nsContext );
            if ( width < 0 || height < 0 ) {
                throw new InvalidCapabilitiesException( "The attributes width and height of '" + legendURLElement.getNodeName()
                                                        + "' must be positive!" );
            }

            String format = XMLTools.getRequiredNodeAsString( legendURLElement, PRE_WPVS + "Format/text()", nsContext );
            // optional
            URI onlineResourceURI = XMLTools.getNodeAsURI( legendURLElement,
                                                           PRE_WPVS + "OnlineResource/@xlink:href",
                                                           nsContext,
                                                           null );

            URL onlineResource = null;
            if ( onlineResourceURI != null ) {
                try {
                    onlineResource = onlineResourceURI.toURL();
                } catch ( MalformedURLException e ) {
                    throw new InvalidCapabilitiesException( onlineResourceURI + " does not represent a valid URL: "
                                                            + e.getMessage() );
                }
            }

            legendURLs[i] = new LegendURL( width, height, format, onlineResource );
        }
        return legendURLs;
    }

    /**
     * Creates and returns a new array of <code>FeatureListReference</code> objects from the given
     * <code>Element</code>.
     *
     * @param datasetElement
     * @return Returns an array of FeatureListReference instances or <code>null</code> if none were defined
     *         (optional).
     * @throws XMLParsingException
     * @throws InvalidCapabilitiesException
     */
    protected FeatureListReference[] parseFeatureListReferences( Element datasetElement )
                                                                                         throws XMLParsingException,
                                                                                         InvalidCapabilitiesException {

        List<Node> featureList = XMLTools.getNodes( datasetElement, PRE_WPVS + "FeatureListReference", nsContext );
        if ( featureList.size() == 0 ) {
            return null;
        }
        FeatureListReference[] featureRefs = new FeatureListReference[featureList.size()];
        for ( int i = 0; i < featureRefs.length; i++ ) {

            Element featureRefElement = (Element) featureList.get( i );

            String format = XMLTools.getRequiredNodeAsString( featureRefElement, PRE_WPVS + "Format/text()", nsContext );

            URI onlineResourceURI = XMLTools.getNodeAsURI( featureRefElement,
                                                           PRE_WPVS + "OnlineResource/@xlink:href",
                                                           nsContext,
                                                           null );
            URL onlineResource = null;
            if ( onlineResourceURI != null ) {
                try {
                    onlineResource = onlineResourceURI.toURL();
                } catch ( MalformedURLException e ) {
                    throw new InvalidCapabilitiesException( onlineResourceURI + " does not represent a valid URL: "
                                                            + e.getMessage() );
                }
            }
            featureRefs[i] = new FeatureListReference( format, onlineResource );
        }
        return featureRefs;
    }

    /**
     * Creates and returns a new array of <code>DatasetReference</code> objects from the given <code>Element</code>.
     *
     * @param datasetElement
     * @return Returns a new array of DatasetReference objects or <code>null</code> if no DatasetReferences are
     *         specified in this dataset (optional)
     * @throws XMLParsingException
     * @throws InvalidCapabilitiesException
     */
    protected DatasetReference[] parseDatasetReferences( Element datasetElement )
                                                                                 throws XMLParsingException,
                                                                                 InvalidCapabilitiesException {

        List<Node> datasetRefList = XMLTools.getNodes( datasetElement, PRE_WPVS + "DatasetReference", nsContext );
        if ( datasetRefList == null ) {
            return null;
        }
        DatasetReference[] datasetRefs = new DatasetReference[datasetRefList.size()];

        for ( int i = 0; i < datasetRefs.length; i++ ) {

            Element datasetRefElement = (Element) datasetRefList.get( i );

            String format = XMLTools.getRequiredNodeAsString( datasetRefElement, PRE_WPVS + "Format/text()", nsContext );

            URI onlineResourceURI = XMLTools.getNodeAsURI( datasetRefElement,
                                                           PRE_WPVS + "OnlineResource/@xlink:href",
                                                           nsContext,
                                                           null );
            URL onlineResource = null;
            if ( onlineResourceURI != null ) {
                try {
                    onlineResource = onlineResourceURI.toURL();
                } catch ( MalformedURLException e ) {
                    throw new InvalidCapabilitiesException( onlineResourceURI + " does not represent a valid URL: "
                                                            + e.getMessage() );
                }
            }
            datasetRefs[i] = new DatasetReference( format, onlineResource );
        }

        return datasetRefs;
    }

    /**
     * Creates and returns a new <code>MetaData</code> object from the given <code>Element</code>.
     *
     * @param datasetElement
     * @return Returns an array of MetaData objects, or <code>null</code> if no metadata was specified in the dataset
     *         (optional).
     * @throws XMLParsingException
     * @throws InvalidCapabilitiesException
     */
    protected MetaData[] parseMetaData( Element datasetElement )
                                                                throws XMLParsingException,
                                                                InvalidCapabilitiesException {

        List<Node> metaDataList = XMLTools.getNodes( datasetElement, PRE_WPVS + "MetaData", nsContext );
        if ( metaDataList.size() == 0 ) {
            return null;
        }
        MetaData[] metaData = new MetaData[metaDataList.size()];

        for ( int i = 0; i < metaData.length; i++ ) {

            Element metaDataElement = (Element) metaDataList.get( i );

            String type = XMLTools.getRequiredNodeAsString( metaDataElement, "@type", nsContext );

            String format = XMLTools.getRequiredNodeAsString( metaDataElement, PRE_WPVS + "Format/text()", nsContext );
            URI onlineResourceURI = XMLTools.getNodeAsURI( metaDataElement,
                                                           PRE_WPVS + "OnlineResource/@xlink:href",
                                                           nsContext,
                                                           null );
            URL onlineResource = null;
            if ( onlineResourceURI != null ) {
                try {
                    onlineResource = onlineResourceURI.toURL();
                } catch ( MalformedURLException e ) {
                    throw new InvalidCapabilitiesException( onlineResourceURI + " does not represent a valid URL: "
                                                            + e.getMessage() );
                }
            }

            metaData[i] = new MetaData( type, format, onlineResource );
        }

        return metaData;
    }

    /**
     * Creates and returns a new <code>Identifier</code> object from the given <code>Element</code> and the given
     * <cod>xPathQuery</code>.
     *
     * @param element
     * @param xPathQuery
     * @return Returns a new Identifier object.
     * @throws XMLParsingException
     * @throws InvalidCapabilitiesException
     *             if no (valid) identifier is found
     */
    protected Identifier parseStyleIdentifier( Element element, String xPathQuery )
                                                                                   throws XMLParsingException,
                                                                                   InvalidCapabilitiesException {

        Element identifierElement = (Element) XMLTools.getRequiredNode( element, xPathQuery, nsContext );

        String value = XMLTools.getStringValue( identifierElement ).trim();
        if ( "".equals( value ) ) {
            throw new InvalidCapabilitiesException( Messages.getMessage( "WPVS_NO_VALID_IDENTIFIER", "style" ) );
        }
        URI codeSpace = XMLTools.getNodeAsURI( identifierElement, "@codeSpace", nsContext, null );

        Identifier id = new Identifier( value, codeSpace );
        if ( styleIdentifiers.contains( id.toString() ) ) {
            throw new InvalidCapabilitiesException( Messages.getMessage( "WPVS_NO_UNIQUE_IDENTIFIER",
                                                                         "styles",
                                                                         id.toString() ) );

        }
        styleIdentifiers.add( id.toString() );
        return id;
    }

    /**
     * Creates and returns a new <code>Identifier</code> object from the given <code>Element</code> and the given
     * <cod>xPathQuery</code>.
     *
     * @param element
     * @param xPathQuery
     * @return Returns a new Identifier object.
     * @throws XMLParsingException
     * @throws InvalidCapabilitiesException
     *             if no (valid) identifier is found
     */
    protected Identifier parseDatasetIdentifier( Element element, String xPathQuery )
                                                                                     throws XMLParsingException,
                                                                                     InvalidCapabilitiesException {

        Element identifierElement = (Element) XMLTools.getRequiredNode( element, xPathQuery, nsContext );

        String value = XMLTools.getStringValue( identifierElement ).trim();
        if ( "".equals( value ) ) {
            throw new InvalidCapabilitiesException( Messages.getMessage( "WPVS_NO_VALID_IDENTIFIER", "dataset" ) );
        }
        URI codeSpace = XMLTools.getNodeAsURI( identifierElement, "@codeSpace", nsContext, null );
        Identifier id = new Identifier( value, codeSpace );
        if ( datasetIdentifiers.contains( id.toString() ) ) {
            throw new InvalidCapabilitiesException( Messages.getMessage( "WPVS_NO_UNIQUE_IDENTIFIER",
                                                                         "datasets",
                                                                         id.toString() ) );

        }
        datasetIdentifiers.add( id.toString() );

        return id;
    }

    /**
     * Creates and returns a new <code>DataProvider</code> object from the given <code>Element</code>.
     *
     * @param datasetElement
     * @return Returns a new DataProvider object or <code>null</code>if no provider was defined.
     * @throws XMLParsingException
     * @throws InvalidCapabilitiesException
     */
    protected DataProvider parseDataProvider( Element datasetElement )
                                                                      throws XMLParsingException,
                                                                      InvalidCapabilitiesException {

        String providerName = null;
        URL providerSite = null;
        LogoURL logoURL = null;

        Element dataProviderElement = (Element) XMLTools.getNode( datasetElement, PRE_WPVS + "DataProvider", nsContext );
        if ( dataProviderElement == null )
            return null;

        providerName = XMLTools.getNodeAsString( dataProviderElement, PRE_WPVS + "ProviderName/text()", nsContext, null );
        URI providerSiteURI = XMLTools.getNodeAsURI( dataProviderElement,
                                                     PRE_WPVS + "ProviderSite/@xlink:href",
                                                     nsContext,
                                                     null );
        if ( providerSiteURI != null ) {
            try {
                providerSite = providerSiteURI.toURL();
            } catch ( MalformedURLException e ) {
                throw new InvalidCapabilitiesException( providerSiteURI + " does not represent a valid URL: "
                                                        + e.getMessage() );
            }
        }

        Node logoURLElement = XMLTools.getNode( dataProviderElement, PRE_WPVS + "LogoURL", nsContext );
        if ( logoURLElement != null ) {

            int width = XMLTools.getRequiredNodeAsInt( logoURLElement, "@width", nsContext );
            int height = XMLTools.getRequiredNodeAsInt( logoURLElement, "@height", nsContext );
            if ( width < 0 || height < 0 ) {
                throw new InvalidCapabilitiesException( "width and height of '" + logoURLElement
                                                        + "' must be positive!" );
            }

            String format = XMLTools.getRequiredNodeAsString( logoURLElement, PRE_WPVS + "Format/text()", nsContext );

            URI onlineResourceURI = XMLTools.getNodeAsURI( logoURLElement,
                                                           PRE_WPVS + "OnlineResource/@xlink:href",
                                                           nsContext,
                                                           null );
            URL onlineResource = null;
            if ( onlineResourceURI != null ) {
                try {
                    onlineResource = onlineResourceURI.toURL();
                } catch ( MalformedURLException e ) {
                    throw new InvalidCapabilitiesException( onlineResourceURI + " does not represent a valid URL: "
                                                            + e.getMessage() );
                }
            }

            logoURL = new LogoURL( width, height, format, onlineResource );
        }
        return new DataProvider( providerName, providerSite, logoURL );
    }

    /**
     *
     * @param element
     * @return the Dimensions of a given element or <code>null</code> if no dimension is found (optional).
     * @throws XMLParsingException
     */
    protected Dimension[] parseDimensions( Element element )
                                                            throws XMLParsingException {

        List<Node> nl = XMLTools.getNodes( element, PRE_WPVS + "Dimension", nsContext );
        if ( nl.size() == 0 ) {
            return null;
        }
        Dimension[] dimensions = new Dimension[nl.size()];

        for ( int i = 0; i < dimensions.length; i++ ) {
            Node dimNode = nl.get( i );
            String name = XMLTools.getRequiredNodeAsString( dimNode, "@name", nsContext );
            String units = XMLTools.getRequiredNodeAsString( dimNode, "@units", nsContext );
            String unitSymbol = XMLTools.getNodeAsString( dimNode, "@unitSymbol", nsContext, null );
            String default_ = XMLTools.getNodeAsString( dimNode, "@default", nsContext, null );
            Boolean multipleValues = Boolean.valueOf( XMLTools.getNodeAsBoolean( dimNode,
                                                                                 "@multipleValues",
                                                                                 nsContext,
                                                                                 true ) );
            Boolean nearestValues = Boolean.valueOf( XMLTools.getNodeAsBoolean( dimNode,
                                                                                "@nearestValues",
                                                                                nsContext,
                                                                                true ) );
            Boolean current = Boolean.valueOf( XMLTools.getNodeAsBoolean( dimNode,
                                                                          "@current",
                                                                          nsContext,
                                                                          true ) );
            String value = XMLTools.getNodeAsString( dimNode, ".", nsContext, null );

            dimensions[i] = new Dimension( name,
                                           units,
                                           unitSymbol,
                                           default_,
                                           multipleValues,
                                           nearestValues,
                                           current,
                                           value );
        }

        return dimensions;
    }

    /**
     * Gets an array of <code>boundingBoxes</code> from the given <code>Element</code>. This method returns all
     * boundingBoxes together in one array.
     *
     * @param element
     * @param defaultCoordinateSystem
     *            to be used for not defined coordinate system attribute in the bbox element
     * @return Returns an array of boundingBoxes.
     * @throws XMLParsingException
     * @throws InvalidParameterValueException
     */
    protected Envelope[] getBoundingBoxes( Element element, CoordinateSystem defaultCoordinateSystem )
                                                                                                      throws XMLParsingException,
                                                                                                      InvalidParameterValueException {

        List<Node> boundingBoxList = XMLTools.getNodes( element, PRE_OWS + "BoundingBox", nsContext );

        List<Envelope> bboxesList = new ArrayList<Envelope>( boundingBoxList.size() );

        for ( int i = 0; i < boundingBoxList.size(); i++ ) {
            bboxesList.add( parseBoundingBox( (Element) boundingBoxList.get( i ), defaultCoordinateSystem ) );
        }

        // The ogc_wpvs schema says: wgs84 is mandatory therefore-> not checking parents to use it's
        // bboxes.

        // if ( parent != null ) {
        // Envelope[] boundingBoxes = parent.getBoundingBoxes();
        // for ( int i = 0; i < boundingBoxes.length; i++ ) {
        // bboxesList.add( boundingBoxes[i] );
        // }
        // }

        Envelope[] boxes = bboxesList.toArray( new Envelope[bboxesList.size()] );
        return boxes;
    }

    /**
     * Usable with any BoundingBox. Changed crs from null to given attribute value of crs. Added check for min values to
     * be smaler than max values.
     *
     * Creates an <code>Envelope</code> object from the given element of type <code>ows:WGS84BoundingBox</code> or
     * <code>ows:BoundingBox</code>.
     *
     * @param element
     * @param defaultCoordinateSystem
     *            if the crs-attribute of the bbox element is not defined
     * @return a boundingbox of a dataset
     * @throws XMLParsingException
     * @throws InvalidParameterValueException
     */
    protected Envelope parseBoundingBox( Element element, CoordinateSystem defaultCoordinateSystem )
                                                                                                    throws XMLParsingException,
                                                                                                    InvalidParameterValueException {

        // Envelope env = getWGS84BoundingBoxType( element );

        String crsAtt = element.getAttribute( "crs" );
        CoordinateSystem crs = null;
        if ( crsAtt == null ) {
            crs = defaultCoordinateSystem;
        } else {
            try {
                crs = CRSFactory.create( crsAtt );
            } catch ( UnknownCRSException e ) {
                throw new InvalidParameterValueException( e.getMessage() );
            }
        }

        double[] lowerCorner = XMLTools.getRequiredNodeAsDoubles( element,
                                                                  PRE_OWS + "LowerCorner/text()",
                                                                  nsContext,
                                                                  " " );
        if ( lowerCorner.length < 2 ) {
            throw new XMLParsingException( Messages.getMessage( "WPVS_NO_VALID_BBOX_POINT", PRE_OWS + "LowerCorner" ) );
        }
        double[] upperCorner = XMLTools.getRequiredNodeAsDoubles( element,
                                                                  PRE_OWS + "UpperCorner/text()",
                                                                  nsContext,
                                                                  " " );
        if ( upperCorner.length < 2 ) {
            throw new XMLParsingException( Messages.getMessage( "WPVS_NO_VALID_BBOX_POINT", PRE_OWS + "UpperCorner" ) );
        }
        if ( upperCorner.length != lowerCorner.length ) {
            throw new XMLParsingException( Messages.getMessage( "WPVS_DIFFERENT_BBOX_DIMENSIONS",
                                                                PRE_OWS + "LowerCorner",
                                                                PRE_OWS + "UpperCorner" ) );
        }

        for ( int i = 0; i < upperCorner.length; ++i ) {
            if ( lowerCorner[i] >= upperCorner[i] ) {
                throw new InvalidParameterValueException( Messages.getMessage( "WPVS_WRONG_BBOX_POINT_POSITIONS",
                                                                               new Integer( i ),
                                                                               new Double( lowerCorner[i] ),
                                                                               PRE_OWS + "LowerCorner",
                                                                               new Double( upperCorner[i] ),
                                                                               PRE_OWS + "UpperCorner" ) );

            }
        }

        return GeometryFactory.createEnvelope( lowerCorner[0], lowerCorner[1], upperCorner[0], upperCorner[1], crs );

    }

    /**
     * Creates and returns a new <code>OperationsMetadata</code> object.
     *
     * @param defaultOnlineResource
     *            to fill in the left out dcp's or <code>null</code> if no default URL was given.
     *
     * @return Returns a new OperationsMetadata object.
     * @throws XMLParsingException
     * @throws InvalidCapabilitiesException
     */
    protected OperationsMetadata parseOperationsMetadata( OnlineResource defaultOnlineResource )
                                                                                                throws XMLParsingException,
                                                                                                InvalidCapabilitiesException {

        Node operationMetadata = XMLTools.getRequiredNode( getRootElement(), PRE_OWS + "OperationsMetadata", nsContext );
        List<Node> operationElementList = XMLTools.getNodes( operationMetadata, PRE_OWS + "Operation", nsContext );

        Map<String, Element> operations = new HashMap<String, Element>();
        for ( int i = 0; i < operationElementList.size(); i++ ) {
            operations.put( XMLTools.getRequiredNodeAsString( operationElementList.get( i ), "@name", nsContext ),
                            (Element) operationElementList.get( i ) );
        }

        Operation110 getCapabilities = getOperation110( OperationsMetadata.GET_CAPABILITIES_NAME,
                                                        true,
                                                        operations,
                                                        defaultOnlineResource );
        Operation110 getView = getOperation110( WPVSOperationsMetadata.GET_VIEW_NAME,
                                                true,
                                                operations,
                                                defaultOnlineResource );
        Operation110 getDescription = getOperation110( WPVSOperationsMetadata.GET_DESCRIPTION_NAME,
                                                       false,
                                                       operations,
                                                       defaultOnlineResource );

        Operation110 get3DFeatureInfo = getOperation110( WPVSOperationsMetadata.GET_3D_FEATURE_INFO,
                                                         false,
                                                         operations,
                                                         defaultOnlineResource );

        Operation110 getLegendGraphics = getOperation110( WPVSOperationsMetadata.GET_LEGEND_GRAPHIC_NAME,
                                                          false,
                                                          operations,
                                                          defaultOnlineResource );

        List<Node> parameterElementList = XMLTools.getNodes( operationMetadata, PRE_OWS + "Parameter", nsContext );
        OWSDomainType110[] parameters = new OWSDomainType110[parameterElementList.size()];
        for ( int i = 0; i < parameters.length; i++ ) {
            parameters[i] = getOWSDomainType110( (Element) parameterElementList.get( i ) );
        }

        List<Node> constraintElementList = XMLTools.getNodes( operationMetadata, PRE_OWS + "Constraint", nsContext );
        OWSDomainType110[] constraints = new OWSDomainType110[constraintElementList.size()];
        for ( int i = 0; i < constraints.length; i++ ) {
            constraints[i] = getOWSDomainType110( (Element) constraintElementList.get( i ) );
        }

        List<Node> extendedCapsList = XMLTools.getNodes( operationMetadata, PRE_OWS + "ExtendedCapabilities", nsContext );
        Object[] extendedCapabilities = new Object[extendedCapsList.size()];
        for ( int i = 0; i < extendedCapabilities.length; i++ ) {
            extendedCapabilities[i] = extendedCapsList.get( i );
        }

        WPVSOperationsMetadata metadata = new WPVSOperationsMetadata( getCapabilities,
                                                                      getView,
                                                                      getDescription,
                                                                      getLegendGraphics,
                                                                      parameters,
                                                                      constraints,
                                                                      extendedCapabilities,
                                                                      get3DFeatureInfo );

        return metadata;
    }

    /**
     * FIXME needs to be handled, when OWSDomainType110 ceases to exist.
     *
     * @see org.deegree.owscommon.OWSCommonCapabilitiesDocument#getOperation(String, boolean, Map)
     *
     * @param name
     * @param isMandatory
     * @param operations
     * @return the Operation110 with the given name from the map
     * @throws XMLParsingException
     * @throws InvalidCapabilitiesException
     */
    private Operation110 getOperation110( String name, boolean isMandatory, Map<String, Element> operations,
                                          OnlineResource defaultOnlineResource )
                                                                                throws XMLParsingException,
                                                                                InvalidCapabilitiesException {

        Operation110 operation = null;
        Element operationElement = operations.get( name );
        if ( operationElement == null ) {
            if ( isMandatory ) {
                throw new XMLParsingException( "Mandatory operation '" + name
                                               + "' not defined in "
                                               + "'OperationsMetadata'-section." );
            }
        } else {
            // 'ows:DCP' - elements
            DCPType[] dcps = getDCPs( XMLTools.getRequiredElements( operationElement, PRE_OWS + "DCP", nsContext ) );
            //fill in the default online resource if one is missing.
            if ( defaultOnlineResource != null ) {
                for ( DCPType dcp : dcps ) {
                    Protocol pr = dcp.getProtocol();
                    if ( pr != null ) {
                        if ( pr instanceof HTTP ) {
                            if ( ( (HTTP) pr ).getGetOnlineResources() == null || ( (HTTP) pr ).getGetOnlineResources().length == 0 ) {
                                ( (HTTP) pr ).setGetOnlineResources( new URL[] { defaultOnlineResource.getLinkage()
                                                                                                      .getHref() } );
                            }
                            if ( ( (HTTP) pr ).getPostOnlineResources() == null || ( (HTTP) pr ).getPostOnlineResources().length == 0 ) {
                                ( (HTTP) pr ).setPostOnlineResources( new URL[] { defaultOnlineResource.getLinkage()
                                                                                                       .getHref() } );
                            }
                        }
                    }
                }
            }

            // 'Parameter' - elements
            List<Node> parameterList = XMLTools.getNodes( operationElement, PRE_OWS + "Parameter", nsContext );
            OWSDomainType110[] parameters = new OWSDomainType110[parameterList.size()];
            for ( int i = 0; i < parameters.length; i++ ) {
                parameters[i] = getOWSDomainType110( (Element) parameterList.get( i ) );
            }
            // 'Constraint' - elements
            List<Node> constraintList = XMLTools.getNodes( operationElement, PRE_OWS + "Constraint", nsContext );
            OWSDomainType110[] constraints = new OWSDomainType110[constraintList.size()];
            for ( int i = 0; i < constraintList.size(); i++ ) {
                constraints[i] = getOWSDomainType110( (Element) constraintList.get( i ) );
            }
            // 'ows:Metadata' - element
            List<Node> metadataList = XMLTools.getNodes( operationElement, PRE_OWS + "Metadata", nsContext );
            OWSMetadata[] metadata = new OWSMetadata[metadataList.size()];
            for ( int i = 0; i < metadata.length; i++ ) {
                metadata[i] = getOWSMetadata( operationElement, PRE_OWS + "Metadata", nsContext );
            }

            // return new Operation110 object
            operation = new Operation110( name, dcps, parameters, constraints, metadata );
        }

        return operation;
    }

    /**
     * FIXME there is a similar method in org.deegree.owscommon.OWSCommonCapabilitiesDocument#getDCP. overrides that
     * method!
     *
     * Creates a <code>DCPType</code> object from the passed <code>DCP</code> element.
     *
     * @param element
     * @return created <code>DCPType</code>
     * @throws XMLParsingException
     * @see org.deegree.ogcwebservices.getcapabilities.OGCStandardCapabilities
     */
    @Override
    protected DCPType getDCP( Element element )
                                               throws XMLParsingException {
        DCPType dcpType = null;
        Element httpElement = (Element) XMLTools.getRequiredNode( element, PRE_OWS + "HTTP", nsContext );

        try {
            List<Node> requestList = XMLTools.getNodes( httpElement, PRE_OWS + "Get", nsContext );
            OWSRequestMethod[] getRequests = new OWSRequestMethod[requestList.size()];
            for ( int i = 0; i < getRequests.length; i++ ) {

                List<Node> constraintList = XMLTools.getNodes( requestList.get( i ), PRE_OWS + "Constraint", nsContext );
                OWSDomainType110[] constraint = new OWSDomainType110[constraintList.size()];
                for ( int j = 0; j < constraint.length; j++ ) {
                    constraint[j] = getOWSDomainType110( (Element) constraintList.get( i ) );
                }

                SimpleLink link = parseSimpleLink( (Element) requestList.get( i ) );

                getRequests[i] = new OWSRequestMethod( link, constraint );
            }

            requestList = XMLTools.getNodes( httpElement, PRE_OWS + "Post", nsContext );
            OWSRequestMethod[] postRequests = new OWSRequestMethod[requestList.size()];
            for ( int i = 0; i < postRequests.length; i++ ) {

                List<Node> constraintList = XMLTools.getNodes( requestList.get( i ), PRE_OWS + "Constraint", nsContext );
                OWSDomainType110[] constraint = new OWSDomainType110[constraintList.size()];
                for ( int j = 0; j < constraint.length; j++ ) {
                    constraint[j] = getOWSDomainType110( (Element) constraintList.get( i ) );
                }

                SimpleLink link = parseSimpleLink( (Element) requestList.get( i ) );

                postRequests[i] = new OWSRequestMethod( link, constraint );
            }

            Protocol protocol = new HTTP110( getRequests, postRequests );
            dcpType = new DCPType( protocol );

        } catch ( InvalidCapabilitiesException e ) {
            throw new XMLParsingException( "Couldn't parse the OWSDomainType110 within DCPType: " + StringTools.stackTraceToString( e ) );
        }

        return dcpType;
    }

    /**
     * FIXME needs to be handled, when OWSDomainType110 ceases to exist.
     *
     * @see org.deegree.owscommon.OWSCommonCapabilitiesDocument#getOWSDomainType(String, Element)
     *
     * @param element
     * @return Returns owsDomainType110 object.
     * @throws InvalidCapabilitiesException
     */
    private OWSDomainType110 getOWSDomainType110( Element element )
                                                                   throws XMLParsingException,
                                                                   InvalidCapabilitiesException {

        // 'name' - attribute
        String name = XMLTools.getRequiredNodeAsString( element, "@name", nsContext );

        // 'ows:AllowedValues' - element
        Element allowedElement = (Element) XMLTools.getNode( element, PRE_OWS + "AllowedValues", nsContext );
        OWSAllowedValues allowedValues = null;
        if ( allowedElement != null ) {

            // 'ows:Value' - elements
            String[] values = XMLTools.getNodesAsStrings( allowedElement, PRE_OWS + "Value/text()", nsContext );
            TypedLiteral[] literals = null;
            if ( values != null ) {
                literals = new TypedLiteral[values.length];
                for ( int i = 0; i < literals.length; i++ ) {
                    literals[i] = new TypedLiteral( values[i], null );
                }
            }

            // 'ows:Range' - elements
            List<Node> rangeList = XMLTools.getNodes( allowedElement, PRE_OWS + "Range", nsContext );
            ValueRange[] ranges = new ValueRange[rangeList.size()];
            for ( int i = 0; i < ranges.length; i++ ) {
                String minimum = XMLTools.getNodeAsString( rangeList.get( i ),
                                                           PRE_OWS + "MinimumValue",
                                                           nsContext,
                                                           null );
                String maximum = XMLTools.getNodeAsString( rangeList.get( i ),
                                                           PRE_OWS + "MaximumValue",
                                                           nsContext,
                                                           null );
                String spacing = XMLTools.getNodeAsString( rangeList.get( i ),
                                                           PRE_OWS + "Spacing",
                                                           nsContext,
                                                           null );
                TypedLiteral min = new TypedLiteral( minimum, null );
                TypedLiteral max = new TypedLiteral( maximum, null );
                TypedLiteral space = new TypedLiteral( spacing, null );

                ranges[i] = new ValueRange( min, max, space );
            }

            if ( values.length < 1 && ranges.length < 1 ) {
                throw new XMLParsingException( "At least one 'ows:Value'-element or one 'ows:Range'-element must be defined " + "in each element of type 'ows:AllowedValues'." );
            }

            allowedValues = new OWSAllowedValues( literals, ranges );
        }

        // FIXME manage elements: ows:AnyValue, ows:NoValues.
        boolean anyValue = false;
        boolean noValues = false;

        // 'ows:ValuesListReference' - element
        OWSMetadata valuesListReference = getOWSMetadata( element, PRE_OWS + "ValuesListReference", nsContext );

        // 'ows:DefaulValue' - element
        String defaultValue = XMLTools.getNodeAsString( element, PRE_OWS + "DefaultValue/text()", nsContext, null );

        // 'ows:Meaning' - element
        OWSMetadata meaning = getOWSMetadata( element, PRE_OWS + "Meaning", nsContext );

        // 'ows:DataType - element
        OWSMetadata dataType = getOWSMetadata( element, PRE_OWS + "DataType", nsContext );

        // choose up to one measurement element
        String measurementType = null;
        // 'ows:ReferenceSystem' - element
        Element referenceElement = (Element) XMLTools.getNode( element, PRE_OWS + "ReferenceSystem", nsContext );
        // 'ows:UOM' - element
        Element uomElement = (Element) XMLTools.getNode( element, PRE_OWS + "UOM", nsContext );
        OWSMetadata measurement = null;

        if ( referenceElement != null && uomElement != null ) {
            throw new InvalidCapabilitiesException( "Within an 'ows:DomainType'-Element only one " + "of the following elements is allowed: "
                                                    + "'ows:ReferenceSystem' OR 'ows:UOM'." );
        } else if ( referenceElement != null ) {
            measurementType = OWSDomainType110.REFERENCE_SYSTEM;
            measurement = getOWSMetadata( element, PRE_OWS + "ReferenceSystem", nsContext );
        } else if ( uomElement != null ) {
            measurementType = OWSDomainType110.UOM;
            measurement = getOWSMetadata( element, PRE_OWS + "UOM", nsContext );
        }

        // 'ows:Metadata' - elements
        List<Node> metaList = XMLTools.getNodes( element, PRE_OWS + "Metadata", nsContext );
        OWSMetadata[] metadata = new OWSMetadata[metaList.size()];
        for ( int i = 0; i < metadata.length; i++ ) {
            metadata[i] = getOWSMetadata( (Element) metaList.get( i ), PRE_OWS + "Metadata", nsContext );
        }

        // return new OWSDomainType110
        OWSDomainType110 domainType110 = null;
        if ( allowedValues != null && !anyValue && !noValues && valuesListReference == null ) {
            domainType110 = new OWSDomainType110( allowedValues,
                                                  defaultValue,
                                                  meaning,
                                                  dataType,
                                                  measurementType,
                                                  measurement,
                                                  metadata,
                                                  name );
        } else if ( ( anyValue || noValues ) && allowedValues == null && valuesListReference == null ) {
            domainType110 = new OWSDomainType110( anyValue,
                                                  noValues,
                                                  defaultValue,
                                                  meaning,
                                                  dataType,
                                                  measurementType,
                                                  measurement,
                                                  metadata,
                                                  name );
        } else if ( valuesListReference != null && allowedValues == null && !anyValue && !noValues ) {
            domainType110 = new OWSDomainType110( valuesListReference,
                                                  defaultValue,
                                                  meaning,
                                                  dataType,
                                                  measurementType,
                                                  measurement,
                                                  metadata,
                                                  name );
        } else {
            throw new InvalidCapabilitiesException( "Only one of the following elements may be " + "contained within an 'ows:DomainType': 'ows:AllowedValues', 'ows:AnyValue', "
                                                    + "'ows:NoValues' or 'ows:ValuesListReference'." );
        }

        return domainType110;
    }

    /**
     * FIXME check, wether the URIs go to the correct address within OWSMetadata. So far, no example was given to check
     * this with.
     *
     * Creates and returns a new <code>OWSMetadata</code> object (or null) from the given <code>Element</code> at
     * the given <code>XPath</code>.
     *
     * @param element
     * @param xPath
     * @param nsContext
     * @return Returns a new OWSMetadata object (may be null).
     * @throws XMLParsingException
     */
    private OWSMetadata getOWSMetadata( Element element, String xPath, NamespaceContext nsContext )
                                                                                                   throws XMLParsingException {

        Element child = (Element) XMLTools.getNode( element, xPath, nsContext );

        if ( child == null ) {
            return null;
        }

        // attrib about
        URI about = XMLTools.getNodeAsURI( child, "@about", nsContext, null );

        // attribs for SimpleLink
        URI href = XMLTools.getNodeAsURI( child, "@xlink:href", nsContext, null );
        URI role = XMLTools.getNodeAsURI( child, "@xlink:role", nsContext, null );
        URI arcrole = XMLTools.getNodeAsURI( child, "@xlink:arcrole", nsContext, null );
        String title = XMLTools.getNodeAsString( child, "@xlink:title", nsContext, null );
        String show = XMLTools.getNodeAsString( child, "@xlink:show", nsContext, null );
        String actuate = XMLTools.getNodeAsString( child, "@xlink:actuate", nsContext, null );

        // ows:name (ows:AbstractMetaData)
        String name = XMLTools.getNodeAsString( child, "text()", nsContext, null );

        SimpleLink link = new SimpleLink( href, role, arcrole, title, show, actuate );

        return new OWSMetadata( about, link, name );
    }

}

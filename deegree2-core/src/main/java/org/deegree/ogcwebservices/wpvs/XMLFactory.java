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

package org.deegree.ogcwebservices.wpvs;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.deegree.datatypes.values.TypedLiteral;
import org.deegree.datatypes.values.ValueRange;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcbase.BaseURL;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.ImageURL;
import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.ogcwebservices.getcapabilities.ServiceIdentification;
import org.deegree.ogcwebservices.getcapabilities.ServiceProvider;
import org.deegree.ogcwebservices.wpvs.capabilities.DataProvider;
import org.deegree.ogcwebservices.wpvs.capabilities.Dataset;
import org.deegree.ogcwebservices.wpvs.capabilities.Dimension;
import org.deegree.ogcwebservices.wpvs.capabilities.Identifier;
import org.deegree.ogcwebservices.wpvs.capabilities.MetaData;
import org.deegree.ogcwebservices.wpvs.capabilities.Style;
import org.deegree.ogcwebservices.wpvs.capabilities.WPVSCapabilities;
import org.deegree.ogcwebservices.wpvs.capabilities.WPVSCapabilitiesDocument;
import org.deegree.ogcwebservices.wpvs.capabilities.WPVSOperationsMetadata;
import org.deegree.owscommon.OWSMetadata;
import org.deegree.owscommon.com110.HTTP110;
import org.deegree.owscommon.com110.OWSAllowedValues;
import org.deegree.owscommon.com110.OWSDomainType110;
import org.deegree.owscommon.com110.OWSRequestMethod;
import org.deegree.owscommon.com110.Operation110;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * TODO class description
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * $Revision$, $Date$
 */
public class XMLFactory extends org.deegree.owscommon.XMLFactory {

    private static final URI WPVSNS = CommonNamespaces.WPVSNS;

    private static final String PRE_OWS = CommonNamespaces.OWS_PREFIX + ':';

    private static final String PRE_WPVS = CommonNamespaces.WPVS_PREFIX + ':';

    private XMLFactory() {
        // not instantiable.
    }

    /**
     * This method exporst a wpvs capabilitiesDocument with following information taken from the
     * given WPVSCapabilities
     * <ul>
     * <li>ServiceIdentification</li>
     * <li>ServiceProvide</li>
     * <li>operationMetadata</li>
     * <li>the root dataset</li>
     * </ul>
     *
     * @param wpvsCapabilities
     * @return the WPVSCapabilitiesDocument of this wpvs
     * @throws IOException
     *             if wpvsCapabilitiesDocument cannot be instantiated
     */
    public static WPVSCapabilitiesDocument export( WPVSCapabilities wpvsCapabilities )
                            throws IOException {
        XMLFactory factory = new XMLFactory();
        return factory.createCapabilitiesDocument( wpvsCapabilities );
    }

    private WPVSCapabilitiesDocument createCapabilitiesDocument( WPVSCapabilities wpvsCapabilities )
                            throws IOException {
        WPVSCapabilitiesDocument wpvsCapabilitiesDocument = new WPVSCapabilitiesDocument();
        try {
            wpvsCapabilitiesDocument.createEmptyDocument();
            Element root = wpvsCapabilitiesDocument.getRootElement();

            ServiceIdentification serviceIdentification = wpvsCapabilities.getServiceIdentification();
            if ( serviceIdentification != null ) {
                appendServiceIdentification( root, serviceIdentification );
            }

            ServiceProvider serviceProvider = wpvsCapabilities.getServiceProvider();
            if ( serviceProvider != null ) {
                appendServiceProvider( root, serviceProvider );
            }

            OperationsMetadata operationMetadata = wpvsCapabilities.getOperationsMetadata();
            if ( operationMetadata != null && operationMetadata instanceof WPVSOperationsMetadata ) {
                appendWPVSOperationsMetadata( root, (WPVSOperationsMetadata) operationMetadata );
            }

            Dataset dataset = wpvsCapabilities.getDataset();
            if ( dataset != null ) {
                appendDataset( root, dataset );
            }

        } catch ( SAXException e ) {
            e.printStackTrace();
            LOG.logError( e.getMessage(), e );
        }

        return wpvsCapabilitiesDocument;
    }

    /**
     * Appends the DOM representation of an <code>WPVSOperationsMetadata</code> to the passed
     * <code>Element</code>.
     *
     * @param root
     * @param operationsMetadata
     */
    private void appendWPVSOperationsMetadata( Element root, WPVSOperationsMetadata operationsMetadata ) {
        // 'ows:OperationsMetadata'-element
        Element operationsMetadataNode = XMLTools.appendElement( root, OWSNS, PRE_OWS + "OperationsMetadata" );

        // append all Operations
        Operation110[] operations = (Operation110[]) operationsMetadata.getAllOperations();
        for ( int i = 0; i < operations.length; i++ ) {
            Operation110 operation = operations[i];

            // 'ows:Operation' - element
            Element operationElement = XMLTools.appendElement( operationsMetadataNode, OWSNS, PRE_OWS + "Operation" );
            operationElement.setAttribute( "name", operation.getName() );

            // 'ows:DCP' - elements
            DCPType[] dcps = operation.getDCPs();
            for ( int j = 0; j < dcps.length; j++ ) {
                appendDCPValue( operationElement, dcps[j] );
            }

            // 'ows:Parameter' - elements
            OWSDomainType110[] parameters = operation.getParameters110();
            for ( int j = 0; j < parameters.length; j++ ) {
                appendDomainType( operationElement, parameters[j], PRE_OWS + "Parameter" );
            }

            // 'ows:Constraint' - elements
            OWSDomainType110[] constraints = operation.getConstraints110();
            for ( int j = 0; j < constraints.length; j++ ) {
                appendDomainType( operationElement, constraints[j], PRE_OWS + "Constraint" );
            }

            // 'ows:Metadata' - elements
            OWSMetadata[] metadata = operation.getMetadata110();
            for ( int j = 0; j < metadata.length; j++ ) {
                appendOWSMetadata( operationElement, metadata[j], PRE_OWS + "Metadata" );
            }
        }

        // append general parameters
        OWSDomainType110[] parameters = operationsMetadata.getParameters110();
        for ( int i = 0; i < parameters.length; i++ ) {
            appendDomainType( operationsMetadataNode, parameters[i], PRE_OWS + "Parameter" );
        }

        // append general constraints
        OWSDomainType110[] constraints = operationsMetadata.getConstraints110();
        for ( int i = 0; i < constraints.length; i++ ) {
            appendDomainType( operationsMetadataNode, constraints[i], PRE_OWS + "Constraint" );
        }

        // append 'ows:ExtendedCapabilities'
        // TODO when needed.

    }

    /**
     * Appends the DOM representation of an <code>OWSMetadata</code> to the passed
     * <code>Element</code>. The given <code>String</code> is used to distinguish between the
     * different Metadata types.
     *
     * @param element
     * @param metadata
     * @param tagName
     */
    private void appendOWSMetadata( Element element, OWSMetadata metadata, String tagName ) {

        if ( metadata != null ) {

            Element metadataElement = XMLTools.appendElement( element, OWSNS, tagName );

            appendSimpleLinkAttributes( metadataElement, metadata.getLink() );

            Element nameElement = XMLTools.appendElement( metadataElement, OWSNS, CommonNamespaces.OWS_PREFIX + ":Name" );
            metadataElement.appendChild( nameElement );
            nameElement.setNodeValue( metadata.getName() );
        }

    }

    /**
     * Appends the DOM representation of an <code>OWSDomainType</code> to the passed
     * <code>Element</code>. The given <code>String</code> is used to distinguish between
     * <code>Parameter</code> and <code>Constraint</code>.
     *
     * @param element
     * @param domainType
     * @param tagName
     */
    private void appendDomainType( Element element, OWSDomainType110 domainType, String tagName ) {

        Element domainElement = XMLTools.appendElement( element, OWSNS, tagName );

        // attribute
        domainElement.setAttribute( "name", domainType.getName() );

        // elements
        OWSAllowedValues allowedValues = domainType.getAllowedValues();
        OWSMetadata valuesListReference = domainType.getValuesListReference();
        if ( allowedValues != null ) {
            appendAllowedValues( domainElement, allowedValues );
        }
        // else if ( domainType.isAnyValue() ) {
        // Element anyElement = XMLTools.appendElement( domainElement, OWSNS,
        // CommonNamespaces.OWS_PREFIX+":AnyValue" );
        // // TODO content of this tag!
        // } else if ( domainType.hasNoValues() ) {
        // Element noValuesElement = XMLTools.appendElement( domainElement, OWSNS,
        // CommonNamespaces.OWS_PREFIX+":NoValues" );
        // // TODO content of this tag!
        // }
        else if ( valuesListReference != null ) {
            appendOWSMetadata( domainElement, valuesListReference, CommonNamespaces.OWS_PREFIX + ":ValuesListReference" );
        } else {
            // TODO "domainType object is invalid!"
        }

        appendTypedLiteral( domainElement, domainType.getDefaultValue(), PRE_OWS + "DefaultValue", OWSNS );

        appendOWSMetadata( domainElement, domainType.getMeaning(), PRE_OWS + "Meaning" );

        appendOWSMetadata( domainElement, domainType.getOwsDataType(), PRE_OWS + "DataType" );

        String measurement = domainType.getMeasurementType();
        if ( OWSDomainType110.REFERENCE_SYSTEM.equals( measurement ) ) {
            appendOWSMetadata( domainElement, domainType.getMeasurement(), PRE_OWS + "ReferenceSystem" );
        } else if ( OWSDomainType110.UOM.equals( measurement ) ) {
            appendOWSMetadata( domainElement, domainType.getMeasurement(), PRE_OWS + "UOM" );
        }

        OWSMetadata[] metadata = domainType.getMetadata();
        for ( int i = 0; i < metadata.length; i++ ) {
            appendOWSMetadata( domainElement, metadata[i], PRE_OWS + "Metadata" );
        }

    }

    /**
     * Appends the DOM representation of an <code>OWSAllowedValues</code> object to the passed
     * <code>Element</code>.
     *
     * @param element
     * @param allowedValues
     */
    private void appendAllowedValues( Element element, OWSAllowedValues allowedValues ) {

        Element allowedElement = XMLTools.appendElement( element, OWSNS, PRE_OWS + "AllowedValues" );

        TypedLiteral[] literals = allowedValues.getOwsValues();
        for ( int i = 0; i < literals.length; i++ ) {
            appendTypedLiteral( allowedElement, literals[i], PRE_OWS + "Value", OWSNS );
        }

        ValueRange[] range = allowedValues.getValueRanges();
        for ( int i = 0; i < range.length; i++ ) {
            Element rangeElement = XMLTools.appendElement( allowedElement, OWSNS, PRE_OWS + "Range" );

            appendTypedLiteral( rangeElement, range[i].getMin(), PRE_OWS + "MinimumValue", OWSNS );
            appendTypedLiteral( rangeElement, range[i].getMax(), PRE_OWS + "MaximumValue", OWSNS );
            appendTypedLiteral( rangeElement, range[i].getSpacing(), PRE_OWS + "Spacing", OWSNS );
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.owscommon.XMLFactory#appendDCP(org.w3c.dom.Element,
     *      org.deegree.ogcwebservices.getcapabilities.DCPType)
     */
    private void appendDCPValue( Element operationElement, DCPType dcp ) {

        // 'ows:DCP'-element
        Element dcpNode = XMLTools.appendElement( operationElement, OWSNS, PRE_OWS + "DCP" );

        // currently, the only supported DCP are HTTP and HTTP110!
        if ( dcp.getProtocol() instanceof HTTP110 ) {
            HTTP110 http = (HTTP110) dcp.getProtocol();

            // 'ows:HTTP'-element
            Element httpNode = XMLTools.appendElement( dcpNode, OWSNS, PRE_OWS + "HTTP" );

            // 'ows:Get'-elements
            OWSRequestMethod[] getRequest = http.getGetRequests();
            for ( int i = 0; i < getRequest.length; i++ ) {
                appendRequest( httpNode, PRE_OWS + "Get", getRequest[i] );
            }

            // 'ows:Post'-elements
            OWSRequestMethod[] postRequest = http.getPostRequests();
            for ( int i = 0; i < postRequest.length; i++ ) {
                appendRequest( httpNode, PRE_OWS + "Post", postRequest[i] );
            }
        }

    }

    /**
     * Appends the DOM representation of an <code>OWSRequestMethod</code> to the passed
     * <code>Element</code>. The given <code>String</code> is used to distinguish between
     * <code>ows:Get</code> and <code>ows:Post</code> requests.
     *
     * @param httpNode
     * @param type
     * @param request
     */
    private void appendRequest( Element httpNode, String type, OWSRequestMethod request ) {

        Element owsElement = XMLTools.appendElement( httpNode, OWSNS, type );

        appendSimpleLinkAttributes( owsElement, request.getLink() );

        OWSDomainType110[] constraint = request.getConstraints();
        for ( int i = 0; i < constraint.length; i++ ) {
            appendDomainType( owsElement, constraint[i], PRE_OWS + "Constraint" );
        }

    }

    /**
     * Appends the DOM representation of a <code>Dataset</code> to the passed <code>Element</code>.
     *
     * @param root
     * @param dataset
     */
    private void appendDataset( Element root, Dataset dataset ) {

        // 'wpvs:Dataset'-element (parent)
        Element datasetNode = XMLTools.appendElement( root, WPVSNS, PRE_WPVS + "Dataset" );

        // attributes
        datasetNode.setAttribute( "queryable", ( ( dataset.getQueryable() ) ? "1" : "0" ) );
        datasetNode.setAttribute( "opaque", ( ( dataset.getOpaque() ) ? "1" : "0" ) );
        datasetNode.setAttribute( "noSubsets", ( ( dataset.getNoSubset() ) ? "1" : "0" ) );
        datasetNode.setAttribute( "fixedWidth", String.valueOf( dataset.getFixedWidth() ) );
        datasetNode.setAttribute( "fixedHeight", String.valueOf( dataset.getFixedHeight() ) );

        // optional 'wpvs:Name'-element
        appendName( datasetNode, dataset );

        // mandatory 'wpvs:Title'-element
        appendTitle( datasetNode, dataset );

        // optional 'wpvs:Abstract'-element
        appendAbstract( datasetNode, dataset );

        // optional 'ows:Keywords'-elements
        appendOWSKeywords( datasetNode, dataset.getKeywords() );

        // optional 'wpvs:CRS'-elements
        appendCRSNodes( datasetNode, dataset.getCrs() );

        // optional 'wpvs:Format'-elements
        appendFormats( datasetNode, dataset.getMimeTypeFormat() );

        // mandatory 'ows:WGS84BoundingBox
        appendBoundingBox( datasetNode, dataset.getWgs84BoundingBox(), PRE_OWS + "WGS84BoundingBox",
                           "urn:ogc:def:crs:OGC:2:84", "2" );

        // optional 'ows:BoundingBox'-elements
        Envelope[] boundingBoxes = dataset.getBoundingBoxes();

        for ( int i = 0; i < boundingBoxes.length; i++ ) {

            if ( boundingBoxes[i] != null ) {
                String crsName = boundingBoxes[i].getCoordinateSystem().getIdentifier();

                appendBoundingBox( datasetNode, boundingBoxes[i], PRE_OWS + "BoundingBox", crsName, "2" );
            }
        }

        // optional 'wpvs:Dimension'-elements
        appendDimensions( datasetNode, dataset.getDimensions() );

        // optional 'wpvs:DataProvider'-element
        appendDataProvider( datasetNode, dataset.getDataProvider() );

        // mandatory 'wpvs:Identifier'-element
        appendIdentifier( datasetNode, dataset.getIdentifier() );

        // 'wpvs:MetaData'-elements
        appendURLs( datasetNode, dataset.getMetadata(), WPVSNS, PRE_WPVS + "MetaData" );

        // 'wpvs:DatasetReference'-elements
        appendURLs( datasetNode, dataset.getDatasetReferences(), WPVSNS, PRE_WPVS + "DatasetReference" );

        // 'wpvs:FeatureListReference'-elements
        appendURLs( datasetNode, dataset.getFeatureListReferences(), WPVSNS, PRE_WPVS + "FeatureListReference" );

        // 'wpvs:Style'-elements
        appendStyles( datasetNode, dataset.getStyles() );

        // 'wpvs:MinimumScaleDenominator'-element
        appendScaleDenominator( datasetNode, dataset.getMinimumScaleDenominator(), "MIN" );

        // 'wpvs:MaximumScaleDenominator'-element
        appendScaleDenominator( datasetNode, dataset.getMaximumScaleDenominator(), "MAX" );

        // 'wpvs:Dataset'-elements (children)
        Dataset[] datasets = dataset.getDatasets();
        for ( int i = 0; i < datasets.length; i++ ) {
            appendDataset( datasetNode, datasets[i] );
        }

        // 'ElevationModel'-element (the simple ogc-ElevationModel)
        String emName = dataset.getElevationModel().getName();
        if ( emName != null ) {
            appendElevationModel( datasetNode, emName );
        }
    }

    /**
     * Appends the DOM representation of an OGC <code>ElevationModel</code> to the passed
     * <code>Element</code>.
     *
     * @param datasetNode
     * @param elevationModelName
     */
    private void appendElevationModel( Element datasetNode, String elevationModelName ) {

        Element elevation = XMLTools.appendElement( datasetNode, WPVSNS, PRE_WPVS + "ElevationModel" );
        Text elevationText = elevation.getOwnerDocument().createTextNode( elevationModelName );
        elevation.appendChild( elevationText );

        elevation.appendChild( elevationText );

    }

    /**
     * Appends the DOM representations of the given <code>ScaleDenominator</code> to the passed
     * <code>Element</code>. The given <code>String</code> is used to distinguish between
     * MinimumsScaleDenominator and MaximumScaleDenominator.
     *
     * @param datasetNode
     * @param scaleDenominator
     * @param extremum
     *            must be either 'MIN' or 'MAX'.
     */
    private void appendScaleDenominator( Element datasetNode, double scaleDenominator, String extremum ) {
        Element scaleElement = null;

        if ( "MIN".equalsIgnoreCase( extremum ) ) {
            scaleElement = XMLTools.appendElement( datasetNode, WPVSNS, PRE_WPVS + "MinimumScaleDenominator" );
        } else if ( "MAX".equalsIgnoreCase( extremum ) ) {
            scaleElement = XMLTools.appendElement( datasetNode, WPVSNS, PRE_WPVS + "MaximumScaleDenominator" );
        } else {
            throw new IllegalArgumentException( "The extremum must be either 'MIN' or 'MAX'." );
        }

        String value = String.valueOf( scaleDenominator );
        Text scaleText = scaleElement.getOwnerDocument().createTextNode( value );
        scaleElement.appendChild( scaleText );

    }

    /**
     * Appends the DOM representations of the <code>Abstract</code> Element from the given
     * <code>Object</code> to the passed <code>Element</code>.
     *
     * @param root
     * @param obj
     *            may be of the following types: Style, Dataset.
     */
    private void appendAbstract( Element root, Object obj ) {

        String abstractString = null;
        if ( obj instanceof Style ) {
            abstractString = ( (Style) obj ).getAbstract();
        } else if ( obj instanceof Dataset ) {
            abstractString = ( (Dataset) obj ).getAbstract();
        }
        if ( abstractString != null ) {
            Element abstractElement = XMLTools.appendElement( root, WPVSNS, PRE_WPVS + "Abstract" );
            Text abstractText = abstractElement.getOwnerDocument().createTextNode( abstractString );
            abstractElement.appendChild( abstractText );
        }

    }

    /**
     * Appends the DOM representations of the <code>Title</code> Element from the given
     * <code>Object</code> to the passed <code>Element</code>.
     *
     * @param root
     * @param obj
     *            may be of the following types: Style, Dataset.
     */
    private void appendTitle( Element root, Object obj ) {

        String title = null;
        if ( obj instanceof Style ) {
            title = ( (Style) obj ).getTitle();
        } else if ( obj instanceof Dataset ) {
            title = ( (Dataset) obj ).getTitle();
        }
        Element titleElement = XMLTools.appendElement( root, WPVSNS, PRE_WPVS + "Title" );
        Text titleText = titleElement.getOwnerDocument().createTextNode( title );
        titleElement.appendChild( titleText );
    }

    /**
     * Appends the DOM representations of the <code>Name</code> Element from the given
     * <code>Object</code> to the passed <code>Element</code>.
     *
     * @param root
     * @param obj
     *            may be of the following types: Style, Dataset.
     */
    private void appendName( Element root, Object obj ) {

        String name = null;
        if ( obj instanceof Style ) {
            name = ( (Style) obj ).getName();
        } else if ( obj instanceof Dataset ) {
            name = ( (Dataset) obj ).getName();
        }

        if ( name != null ) {
            Element nameElement = XMLTools.appendElement( root, WPVSNS, PRE_WPVS + "Name" );
            Text nameText = nameElement.getOwnerDocument().createTextNode( name );
            nameElement.appendChild( nameText );
        }

    }

    /**
     * Appends the DOM representations of the given array of <code>Style</code> to the passed
     * <code>Element</code>.
     *
     * @param datasetNode
     * @param styles
     */
    private void appendStyles( Element datasetNode, Style[] styles ) {

        if ( styles != null ) {
            for ( int i = 0; i < styles.length; i++ ) {

                Element styleElement = XMLTools.appendElement( datasetNode, WPVSNS, PRE_WPVS + "Style" );

                appendName( styleElement, styles[i] );
                appendTitle( styleElement, styles[i] );
                appendAbstract( styleElement, styles[i] );

                appendOWSKeywords( styleElement, styles[i].getKeywords() );

                if ( styles[i].getIdentifier() != null ) {
                    appendIdentifier( styleElement, styles[i].getIdentifier() );
                }

                appendURLs( styleElement, styles[i].getLegendURLs(), WPVSNS, PRE_WPVS + "LegendURL" );

                Element styleSheetURLElement = XMLTools.appendElement( styleElement, WPVSNS, PRE_WPVS + "StyleSheetURL" );
                appendURL( styleSheetURLElement, styles[i].getStyleSheetURL(), WPVSNS );

                Element styleURLElement = XMLTools.appendElement( styleElement, WPVSNS, PRE_WPVS + "StyleURL" );
                appendURL( styleURLElement, styles[i].getStyleURL(), WPVSNS );

            }
        }
    }

    /**
     * Appends the DOM representations of the given array of <code>BaseURL</code> under the given
     * name to the passed <code>Element</code>.
     *
     * @param root
     * @param baseURL
     * @param uri
     * @param newNode
     */
    private void appendURLs( Element root, BaseURL[] baseURL, URI uri, String newNode ) {
        if ( baseURL != null ) {
            for ( int i = 0; i < baseURL.length; i++ ) {
                Element urlElement = XMLTools.appendElement( root, uri, newNode );
                appendURL( urlElement, baseURL[i], uri );
            }
        }
    }

    /**
     * Appends the contents of the given <code>BaseURL</code> within the given <code>URI</code>
     * as DOM representation to the passed URL <code>Element</code>.
     *
     * @param urlElement
     *            example: logoURLElement
     * @param baseURL
     *            example: dataProvider.getLogoURL()
     * @param uri
     *            example: "WPVSNS"
     */
    private void appendURL( Element urlElement, BaseURL baseURL, URI uri ) {

        // child elements of urlElement
        Element formatElement = XMLTools.appendElement( urlElement, uri, PRE_WPVS + "Format" );
        String format = baseURL != null ? baseURL.getFormat() : "";
        Text formatText = formatElement.getOwnerDocument().createTextNode( format );
        formatElement.appendChild( formatText );

        Element onlineElement = XMLTools.appendElement( urlElement, uri, PRE_WPVS + "OnlineResource" );
        String url = ( baseURL != null && baseURL.getOnlineResource() != null ) ? baseURL.getOnlineResource().toString()
                                                                               : "";
        onlineElement.setAttribute( "xlink:href", url );

        // attributes of urlElement
        if ( baseURL instanceof ImageURL ) {
            String width = String.valueOf( ( (ImageURL) baseURL ).getWidth() );
            String height = String.valueOf( ( (ImageURL) baseURL ).getHeight() );
            urlElement.setAttribute( "width", width );
            urlElement.setAttribute( "height", height );

        } else if ( baseURL instanceof MetaData ) {

            urlElement.setAttribute( "type", ( (MetaData) baseURL ).getType() );
        }

    }

    /**
     * Appends the DOM representation of the given <code>Identifier</code> to the passed
     * <code>Element</code>.
     *
     * @param root
     * @param identifier
     */
    private void appendIdentifier( Element root, Identifier identifier ) {

        Element idElement = XMLTools.appendElement( root, WPVSNS, PRE_WPVS + "Identifier" );

        if ( identifier.getCodeSpace() != null ) {
            idElement.setAttribute( "codeSpace", identifier.getCodeSpace().toASCIIString() );
        }

        Text idText = idElement.getOwnerDocument().createTextNode( identifier.getValue() );
        idElement.appendChild( idText );

    }

    /**
     * Appends the DOM representation of the given <code>DataProvider</code> to the passed
     * <code>Element</code>.
     *
     * @param datasetNode
     * @param dataProvider
     */
    private void appendDataProvider( Element datasetNode, DataProvider dataProvider ) {
        if ( dataProvider != null ) {
            Element provider = XMLTools.appendElement( datasetNode, WPVSNS, PRE_WPVS + "DataProvider" );

            String provName = dataProvider.getProviderName();
            if ( provName != null ) {
                Element providerName = XMLTools.appendElement( provider, WPVSNS, PRE_WPVS + "ProviderName" );
                Text providerNameText = providerName.getOwnerDocument().createTextNode( provName );
                providerName.appendChild( providerNameText );

            }

            Element providerSite = XMLTools.appendElement( provider, WPVSNS, PRE_WPVS + "ProviderSite" );
            URL siteURL = dataProvider.getProviderSite();
            String site = "";
            if ( siteURL != null ) {
                site = siteURL.toString();
            }
            providerSite.setAttribute( "xlink:href", site );

            Element logoURLElement = XMLTools.appendElement( provider, WPVSNS, PRE_WPVS + "LogoURL" );
            appendURL( logoURLElement, dataProvider.getLogoURL(), WPVSNS );
        }
    }

    /**
     * Appends the DOM representations of the given array of <code>Dimension</code> to the passed
     * <code>Element</code>.
     *
     * @param datasetNode
     * @param dimensions
     */
    private void appendDimensions( Element datasetNode, Dimension[] dimensions ) {
        if ( dimensions != null ) {
            for ( Dimension dimension : dimensions ) {
                if ( dimension != null ) {
                    Element dimensionElement = XMLTools.appendElement( datasetNode, WPVSNS, PRE_WPVS + "Dimension" );
                    dimensionElement.setAttribute( "name", dimension.getName() );
                    dimensionElement.setAttribute( "units", dimension.getUnits() );
                    dimensionElement.setAttribute( "unitSymbol", dimension.getUnitSymbol() );
                    dimensionElement.setAttribute( "default", dimension.getDefault() );
                    dimensionElement.setAttribute( "multipleValues",
                                                   ( ( dimension.getMultipleValues().booleanValue() ) ? "1" : "0" ) );
                    dimensionElement.setAttribute( "nearestValue",
                                                   ( ( dimension.getNearestValue().booleanValue() ) ? "1" : "0" ) );
                    dimensionElement.setAttribute( "current", ( ( dimension.getCurrent().booleanValue() ) ? "1" : "0" ) );

                    Text dimensionText = dimensionElement.getOwnerDocument().createTextNode( dimension.getValue() );
                    dimensionElement.appendChild( dimensionText );
                }
            }
        }
    }

    /**
     * Appends the DOM representations of the given array of <code>Format</code> to the passed
     * <code>Element</code>.
     *
     * @param datasetNode
     * @param mimeTypeFormat
     */
    private void appendFormats( Element datasetNode, String[] mimeTypeFormat ) {

        if ( mimeTypeFormat != null ) {

            for ( int i = 0; i < mimeTypeFormat.length; i++ ) {
                Element format = XMLTools.appendElement( datasetNode, WPVSNS, PRE_WPVS + "Format" );
                Text formatText = format.getOwnerDocument().createTextNode( mimeTypeFormat[i] );
                format.appendChild( formatText );
            }
        }
    }

    /**
     * Appends the DOM representations of the given array of <code>CRS</code> to the passed
     * <code>Element</code>.
     *
     * @param datasetNode
     * @param coordinateSystems
     */
    private void appendCRSNodes( Element datasetNode, CoordinateSystem[] coordinateSystems ) {

        if ( coordinateSystems != null ) {
            for ( CoordinateSystem crs : coordinateSystems ) {
                Element crsElement = XMLTools.appendElement( datasetNode, WPVSNS, PRE_WPVS + "CRS" );
                Text crsText = crsElement.getOwnerDocument().createTextNode( crs.getFormattedString() );
                crsElement.appendChild( crsText );
            }
        }
    }

    /**
     * Appends the DOM representation of the given parameters <code>Envelope, elementName, crsName,
     * dimension</code>
     * to the passed <code>Element</code>.
     *
     * elementName should be of the kind ows:WGS84BoundingBox" or "ows:BoundingBox". crsName should
     * be of the kind "urn:ogc:def:crs:OGC:2:84" or "...TODO...". dimension should be "2".
     *
     * @param root
     * @param envelope
     * @param elementName
     * @param crsName
     * @param dimension
     */
    private void appendBoundingBox( Element root, Envelope envelope, String elementName, String crsName,
                                    String dimension ) {

        Element boundingBoxElement = XMLTools.appendElement( root, OWSNS, elementName );
        boundingBoxElement.setAttribute( "crs", crsName );
        boundingBoxElement.setAttribute( "dimensions", dimension );

        Element lowerCornerElement = XMLTools.appendElement( boundingBoxElement, OWSNS, PRE_OWS + "LowerCorner" );
        Text lowerCornerText = lowerCornerElement.getOwnerDocument().createTextNode(
                                                                                     envelope.getMin().getX()
                                                                                                             + " "
                                                                                                             + envelope.getMin().getY() );
        lowerCornerElement.appendChild( lowerCornerText );

        Element upperCornerElement = XMLTools.appendElement( boundingBoxElement, OWSNS, PRE_OWS + "UpperCorner" );
        Text upperCornerText = upperCornerElement.getOwnerDocument().createTextNode(
                                                                                     envelope.getMax().getX()
                                                                                                             + " "
                                                                                                             + envelope.getMax().getY() );
        upperCornerElement.appendChild( upperCornerText );

    }

}

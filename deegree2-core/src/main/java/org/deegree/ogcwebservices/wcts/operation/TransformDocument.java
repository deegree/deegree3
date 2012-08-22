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

package org.deegree.ogcwebservices.wcts.operation;

import static org.deegree.framework.xml.XMLTools.getElement;
import static org.deegree.framework.xml.XMLTools.getElements;
import static org.deegree.framework.xml.XMLTools.getNodeAsBoolean;
import static org.deegree.framework.xml.XMLTools.getNodeAsString;
import static org.deegree.framework.xml.XMLTools.getStringValue;
import static org.deegree.ogcbase.CommonNamespaces.DEEGREEWCTS;
import static org.deegree.ogcbase.CommonNamespaces.DEEGREEWCTS_PREFIX;
import static org.deegree.ogcbase.CommonNamespaces.GML_PREFIX;
import static org.deegree.ogcbase.CommonNamespaces.OWS_1_1_0PREFIX;
import static org.deegree.ogcbase.CommonNamespaces.WCS_1_2_0_PREFIX;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3d;

import org.deegree.crs.transformations.Transformation;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wcts.WCTSExceptionCode;
import org.deegree.ogcwebservices.wcts.WCTService;
import org.deegree.ogcwebservices.wcts.WCTServiceFactory;
import org.deegree.ogcwebservices.wcts.capabilities.Content;
import org.deegree.ogcwebservices.wcts.capabilities.FeatureAbilities;
import org.deegree.ogcwebservices.wcts.capabilities.InputOutputFormat;
import org.deegree.ogcwebservices.wcts.configuration.WCTSConfiguration;
import org.deegree.ogcwebservices.wcts.data.FeatureCollectionData;
import org.deegree.ogcwebservices.wcts.data.GeometryData;
import org.deegree.ogcwebservices.wcts.data.SimpleData;
import org.deegree.ogcwebservices.wcts.data.TransformableData;
import org.deegree.owscommon_1_1_0.Manifest;
import org.deegree.owscommon_1_1_0.ManifestDocument;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <code>WCTSTransformDocument</code> is a helper class which supplies a constructor to parse wcts Transform requests
 * version 0.4.0.
 * <p>
 * Following elements are currently not supported:
 * <ul>
 * <li>wcts:transformation</li>
 * <li>wcs:GridCRS</li>
 * <li>wcts:InterpolationType, which is of type wcs:InterpolationMethodBaseType</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class TransformDocument extends WCTSRequestBaseDocument {
    private static ILogger LOG = LoggerFactory.getLogger( TransformDocument.class );

    private static final long serialVersionUID = 1343985893563449983L;

    private final Transform transformRequest;

    /**
     * @param requestId
     * @param rootElement
     *            should not be <code>null</code>
     * @throws OGCWebServiceException
     *             if an {@link XMLParsingException} occurred or a mandatory element/attribute is missing.
     */
    public TransformDocument( String requestId, Element rootElement ) throws OGCWebServiceException {
        super( rootElement );
        String version = parseVersion();

        // check for valid request.
        parseService();

        try {
            String sCRS = getNodeAsString( getRootElement(), PRE + "SourceCRS", nsContext, null );
            String tCRS = getNodeAsString( getRootElement(), PRE + "TargetCRS", nsContext, null );
            CoordinateSystem sourceCRS = null;
            CoordinateSystem targetCRS = null;
            TransformationReference transformationReference = null;

            /**
             * Try to parse the xml choice.
             */
            if ( ( sCRS != null && tCRS == null ) || ( sCRS == null && tCRS != null ) ) {
                throw new OGCWebServiceException(
                                                  Messages.getMessage(
                                                                       "WCTS_ISTRANSFORMABLE_MISSING_CRS",
                                                                       ( ( sCRS == null ) ? "TargetCRS" : "SourceCRS" ),
                                                                       ( ( sCRS == null ) ? "SourceCRS" : "TargetCRS" ) ),
                                                  ExceptionCode.INVALIDPARAMETERVALUE );
            }
            if ( sCRS != null && tCRS != null ) {
                sourceCRS = CRSFactory.create( WCTService.CRS_PROVIDER, sCRS );
                targetCRS = CRSFactory.create( WCTService.CRS_PROVIDER, tCRS );
            } else {
                // Check for supported transformation element.
                Element transformation = getElement( getRootElement(), PRE + "Transformation", nsContext );
                if ( transformation != null ) {
                    transformationReference = handleTransformation( transformation );
                }
                if ( transformationReference == null ) {
                    throw new OGCWebServiceException(
                                                      Messages.getMessage( "WCTS_NOT_VALID_XML_CHOICE",
                                                                           PRE + "SourceCRS/TargetCRS and " + PRE
                                                                                                   + "Transformation" ),
                                                      ExceptionCode.MISSINGPARAMETERVALUE );
                }
            }
            // Check for not supported gridcrs.
            Element gridCRS = getElement( getRootElement(), WCS_1_2_0_PREFIX + ":GridCRS", nsContext );
            if ( gridCRS != null ) {
                throw new OGCWebServiceException( Messages.getMessage( "WCTS_OPERATION_NOT_SUPPORTED",
                                                                       "Definition of output GridCRS ("
                                                                                               + WCS_1_2_0_PREFIX
                                                                                               + ":GridCRS element)" ),
                                                  ExceptionCode.OPERATIONNOTSUPPORTED );
            }

            Element inputDataElement = getElement( getRootElement(), OWS_1_1_0PREFIX + ":InputData", nsContext );
            Manifest inputData = null;
            TransformableData<?> transformableData = null;
            int dataPresentation = Transform.MULTIPART;
            if ( inputDataElement != null ) {
                ManifestDocument doc = new ManifestDocument();
                inputData = doc.parseManifestType( inputDataElement );
                /**
                 * get deegree specific elements. The featurecollections provided by the mime/multiparts were put
                 * beneath the d_wcts:InsertedMultiparts.
                 */
                Element multiParts = getElement( inputDataElement, DEEGREEWCTS_PREFIX + ":MultiParts", nsContext );
                if ( multiParts != null ) {
                    List<FeatureCollection> allData = parseFeatureCollectionData( multiParts );
                    transformableData = new FeatureCollectionData( allData );
                }
            } else {
                LOG.logDebug( "Found no " + OWS_1_1_0PREFIX
                              + ":InputData element, now checking for the deegree element" );
                inputDataElement = getElement( getRootElement(), DEEGREEWCTS_PREFIX + ":InputData", nsContext );
                if ( inputDataElement != null ) {
                    ManifestDocument doc = new ManifestDocument();
                    inputData = doc.parseManifestType( inputDataElement );
                    Element inlineData = getElement( inputDataElement, DEEGREEWCTS_PREFIX + ":InlineData", nsContext );
                    if ( inlineData != null ) {
                        if ( sourceCRS == null ) {
                            WCTSConfiguration config = WCTServiceFactory.getConfiguration();
                            Content cont = config.getContents();
                            if ( transformationReference == null ) {
                                throw new OGCWebServiceException(
                                                                  Messages.getMessage( "WCTS_OPERATION_NOT_SUPPORTED",
                                                                                       " transforming of simple data without a transformation or source CRS " ),
                                                                  ExceptionCode.OPERATIONNOTSUPPORTED );
                            }
                            Transformation trans = cont.getTransformations().get(
                                                                                  transformationReference.gettransformationId() );
                            if ( trans == null || trans.getSourceCRS() == null ) {
                                throw new OGCWebServiceException(
                                                                  Messages.getMessage( "WCTS_OPERATION_NOT_SUPPORTED",
                                                                                       " transforming of simple data without a transformation or source CRS " ),
                                                                  ExceptionCode.OPERATIONNOTSUPPORTED );
                            }
                            transformableData = parseInlineData( CRSFactory.create( trans.getSourceCRS() ), inlineData );
                        } else {
                            transformableData = parseInlineData( sourceCRS, inlineData );
                        }
                        dataPresentation = Transform.INLINE;
                    } else {
                        // Handle the xlink:href attributes of the inputdata/referencegroup/reference@xlink:href.
                    }
                }
            }
            if ( inputData == null || transformableData == null ) {
                throw new OGCWebServiceException( Messages.getMessage( "WCTS_TRANSFORM_MISSING_DATA" ),
                                                  WCTSExceptionCode.NO_INPUT_DATA );
            }

            // Check for not supported interpolationType.
            Element interpolationType = getElement( getRootElement(), PRE + "InterpolationType", nsContext );
            if ( interpolationType != null ) {
                throw new OGCWebServiceException(
                                                  Messages.getMessage(
                                                                       "WCTS_OPERATION_NOT_SUPPORTED",
                                                                       "Defining an InterpolationType ("
                                                                                               + PRE
                                                                                               + "InterpolationType element)" ),
                                                  ExceptionCode.OPERATIONNOTSUPPORTED );
            }

            String outputFormat = getNodeAsString( getRootElement(), PRE + "OutputFormat", nsContext, null );
            if ( outputFormat != null && !"".equals( outputFormat.trim() )
                 && !"text/xml; gmlVersion=3.1.1".equalsIgnoreCase( outputFormat.trim() ) ) {
                WCTSConfiguration config = WCTServiceFactory.getConfiguration();
                boolean outputFormatDefined = false;
                if ( config != null ) {
                    Content content = config.getContents();
                    if ( content != null ) {
                        FeatureAbilities fa = content.getFeatureAbilities();
                        if ( fa != null ) {
                            List<InputOutputFormat> formats = fa.getFeatureFormats();
                            if ( formats != null ) {
                                for ( InputOutputFormat format : formats ) {
                                    if ( outputFormatDefined && format != null && format.canOutput() ) {
                                        outputFormat.equals( format.getValue() );
                                        outputFormatDefined = true;
                                    }
                                }
                            }
                        }
                    }
                }
                if ( !outputFormatDefined ) {
                    throw new OGCWebServiceException( Messages.getMessage( "WCTS_REQUESTED_OUTPUTFORMAT_NOT_KNOWN",
                                                                           outputFormat, "Transform" ),
                                                      ExceptionCode.INVALIDPARAMETERVALUE );
                }

            } else {
                outputFormat = "text/xml";
            }
            boolean store = getNodeAsBoolean( getRootElement(), "@store", nsContext, true );
            this.transformRequest = new Transform( version, requestId, store, sourceCRS, targetCRS,
                                                   transformationReference, inputData, transformableData, outputFormat,
                                                   dataPresentation );

        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( e.getMessage(), ExceptionCode.NOAPPLICABLECODE );
        } catch ( UnknownCRSException e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( e.getMessage(), ExceptionCode.NOAPPLICABLECODE );
        }
    }

    /**
     * @param rootElement
     * @return the transform reference.
     *
     * @throws XMLParsingException
     *             if the xlink:href was not given.
     */
    private TransformationReference handleTransformation( Element rootElement )
                            throws XMLParsingException {
        if ( rootElement == null ) {
            LOG.logDebug( "No transformation element given, using standard transformation type." );
            return null;
        }
        String id = rootElement.getAttributeNS( CommonNamespaces.XLNNS.toASCIIString(), "href" );
        String sourceID = null;
        String targetID = null;
        if ( id == null || "".equals( id.trim() ) ) {
            Element sCRSNode = XMLTools.getElement( rootElement, PRE + "sourceCRS", nsContext );
            Element tCRSNode = XMLTools.getElement( rootElement, PRE + "targetCRS", nsContext );
            if ( ( sCRSNode == null && tCRSNode != null ) || ( sCRSNode != null && tCRSNode == null ) ) {
                throw new XMLParsingException(
                                               Messages.getMessage(
                                                                    "WCTS_ISTRANSFORMABLE_MISSING_CRS",
                                                                    ( ( sCRSNode == null ) ? "TargetCRS" : "SourceCRS" ),
                                                                    ( ( tCRSNode == null ) ? "SourceCRS" : "TargetCRS" ) ) );
            }
            if ( sCRSNode != null ) {
                sourceID = sCRSNode.getAttributeNS( CommonNamespaces.XLNNS.toASCIIString(), "href" );
            }
            if ( tCRSNode != null ) {
                targetID = tCRSNode.getAttributeNS( CommonNamespaces.XLNNS.toASCIIString(), "href" );
            }
            LOG.logDebug( "The evaluation os supplied sourceID: ", sourceID, " and/or targetID: ", targetID,
                          " are currently not supported." );
            throw new XMLParsingException( "Currently only referencing of transformations is supported." );
        }

        return new TransformationReference( id );

    }

    /**
     * Parses the deegree inlinedata element.
     *
     * @param sourceCRS
     *            of the data.
     * @param targetCRS
     *            in which the data is to be transformed.
     * @param inlineData
     *            element to extract the data from.
     * @return a {@link TransformableData} element instantiated with the right type.
     * @throws OGCWebServiceException
     *             if for any reason the data could not be parsed or processed.
     */
    private TransformableData<?> parseInlineData( CoordinateSystem sourceCRS, Node inlineData )
                            throws OGCWebServiceException {
        Node firstChild = null;
        try {
            firstChild = getElement( inlineData, "*[1]", nsContext );
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
        }
        if ( firstChild != null ) {
            LOG.logDebug( "Incoming inlineData has localname: " + inlineData.getLocalName()
                          + " has a firstchild with localname: " + firstChild.getLocalName() );
            String prefix = firstChild.getPrefix();
            String nameSpace = firstChild.getNamespaceURI();
            if ( prefix != null ) {
                String tmp = firstChild.lookupNamespaceURI( prefix );
                if ( tmp != null && !"".equals( tmp ) ) {
                    nameSpace = tmp;
                }
            }
            if ( nameSpace == null ) {
                nameSpace = "";
            }
            LOG.logDebug( "Firstchild is bound to namespace: " + nameSpace );
            if ( !DEEGREEWCTS.toASCIIString().equalsIgnoreCase( nameSpace.trim() ) ) {
                LOG.logError( "The node beneath an " + DEEGREEWCTS_PREFIX
                              + ":inlineData element must be bound to the deegree-wcts (" + DEEGREEWCTS.toASCIIString()
                              + ") name space, found following namespace: " + nameSpace );
            } else {
                String localName = firstChild.getLocalName();
                if ( localName != null ) {
                    localName = localName.trim();
                    if ( "SimpleData".equals( localName ) ) {
                        SimpleData result = parseSimpleData( sourceCRS, (Element) firstChild );
                        if ( result == null ) {
                            result = new SimpleData();
                        }
                        return result;
                    } else if ( "GeometryData".equals( localName ) ) {
                        return new GeometryData( parseGeometryData( sourceCRS.getIdentifier(), (Element) firstChild ) );
                    } else if ( "FeatureCollectionData".equals( localName ) ) {
                        return new FeatureCollectionData( parseFeatureCollectionData( (Element) firstChild ) );
                    } else {
                        throw new OGCWebServiceException(
                                                          Messages.getMessage( "WCTS_TRANSFORM_UNKNOWN_INLINE_DATA",
                                                                               localName,
                                                                               "SimpleData, GeometryData or FeatureCollectionData" ),
                                                          ExceptionCode.INVALIDPARAMETERVALUE );
                    }

                }
            }
        }
        throw new OGCWebServiceException( Messages.getMessage( "WCTS_TRANSFORM_MISSING_DATA" ),
                                          WCTSExceptionCode.NO_INPUT_DATA );
    }

    /**
     * @param simpleData
     *            the dom-xml element to be parsed.
     * @return a list of point3d's or <code>null</code> if the given parameter <code>null</code>.
     * @throws OGCWebServiceException
     *             if the number of points is not congruent with the dimension.
     */
    private SimpleData parseSimpleData( CoordinateSystem sourceCRS, Element simpleData )
                            throws OGCWebServiceException {
        if ( simpleData == null ) {
            return null;
        }

        String cs = simpleData.getAttribute( "cs" );
        if ( cs == null || "".equals( cs ) ) {
            cs = ",";
        }
        String ts = simpleData.getAttribute( "ts" );
        if ( ts == null || "".equals( ts ) ) {
            ts = " ";
        }
        String values = getStringValue( simpleData );
        List<Point3d> points = SimpleData.parseData( values, sourceCRS.getDimension(), cs, ts, "." );
        if ( points.size() == 0 ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_TRANSFORM_MISSING_DATA" ),
                                              WCTSExceptionCode.NO_INPUT_DATA );
        }
        return new SimpleData( points, cs, ts );
    }

    /**
     * Parse the featurecollections from the given featureCollectionsData element.
     *
     * @param featureCollectionData
     *            (a deegreewcts:inlineElement/deegreewcts:FeatureCollectionData or a deegreewcts:mulipart element).
     * @return the list of feature collections.
     * @throws OGCWebServiceException
     *             if no feature collections were found or an xml parsing exception occurred.
     */
    private List<FeatureCollection> parseFeatureCollectionData( Element featureCollectionData )
                            throws OGCWebServiceException {
        if ( featureCollectionData == null ) {
            return null;
        }
        List<FeatureCollection> transformableData = new ArrayList<FeatureCollection>();
        try {
            List<Element> fcElements = getElements( featureCollectionData, GML_PREFIX + ":FeatureCollection", nsContext );
            if ( fcElements == null || fcElements.size() == 0 ) {
                LOG.logError( "Could not find any feature collections, this is strange!" );
                throw new OGCWebServiceException( Messages.getMessage( "WCTS_TRANSFORM_NO_DATA_FOUND",
                                                                       "gml:FeatureCollection" ),
                                                  WCTSExceptionCode.NO_INPUT_DATA );
            }
            GMLFeatureCollectionDocument fd = new GMLFeatureCollectionDocument( true, true );
            for ( Element fc : fcElements ) {
                fd.setRootElement( fc );
                FeatureCollection data = fd.parse();
                if ( data != null ) {
                    transformableData.add( data );
                }
            }
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( e.getMessage(), ExceptionCode.NOAPPLICABLECODE );
        }
        return transformableData;
    }

    /**
     * Parse the geometries from the given geometries data element.
     *
     * @param sourceCRSID
     *            needed for the wrap function of the GMLGeometrieAdapter.
     * @param geometryData
     *            (an deegreewcts:inlineElement/deegreewcts:GeometryData or a deegreewcts:mulipart element).
     * @return the list of geometries.
     * @throws OGCWebServiceException
     *             if no feature collections were found or an xml parsing exception occurred.
     */
    private List<Geometry> parseGeometryData( String sourceCRSID, Element geometryData )
                            throws OGCWebServiceException {
        if ( geometryData == null ) {
            return null;
        }
        List<Geometry> transformableData = new ArrayList<Geometry>();
        try {
            List<Element> geomElements = getElements( geometryData, "*", nsContext );
            if ( geomElements == null || geomElements.size() == 0 ) {
                LOG.logError( "Could not find any geometries, this is strange!" );
                throw new OGCWebServiceException(
                                                  Messages.getMessage( "WCTS_TRANSFORM_NO_DATA_FOUND", "gml:Geometries" ),
                                                  WCTSExceptionCode.NO_INPUT_DATA );
            }
            for ( Element fc : geomElements ) {
                try {
                    Geometry data = GMLGeometryAdapter.wrap( fc, sourceCRSID );
                    if ( data != null ) {
                        transformableData.add( data );
                    }
                } catch ( GeometryException e ) {
                    LOG.logError( e.getMessage(), e );
                }
            }
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( e.getMessage(), ExceptionCode.NOAPPLICABLECODE );
        }
        return transformableData;
    }

    /**
     * @return the transformRequest may be <code>null</code>.
     */
    public final Transform getTransformRequest() {
        return transformRequest;
    }

}

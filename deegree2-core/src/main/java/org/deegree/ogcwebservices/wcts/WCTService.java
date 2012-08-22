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

package org.deegree.ogcwebservices.wcts;

import static org.deegree.framework.xml.XMLTools.appendElement;
import static org.deegree.framework.xml.XMLTools.create;
import static org.deegree.framework.xml.XMLTools.getElement;
import static org.deegree.ogcbase.CommonNamespaces.GMLNS;
import static org.deegree.ogcbase.CommonNamespaces.GML_PREFIX;
import static org.deegree.ogcbase.CommonNamespaces.OWS_1_1_0PREFIX;
import static org.deegree.ogcbase.CommonNamespaces.WCTSNS;
import static org.deegree.ogcbase.CommonNamespaces.WCTS_PREFIX;
import static org.deegree.ogcbase.CommonNamespaces.getNamespaceContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.deegree.crs.configuration.CRSConfiguration;
import org.deegree.crs.configuration.CRSProvider;
import org.deegree.crs.transformations.Transformation;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.Pair;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.GMLGeometryAdapter;
import org.deegree.ogcbase.ExceptionCode;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.wcts.capabilities.Content;
import org.deegree.ogcwebservices.wcts.capabilities.FeatureAbilities;
import org.deegree.ogcwebservices.wcts.capabilities.WCTSCapabilities;
import org.deegree.ogcwebservices.wcts.capabilities.mdprofiles.MetadataProfile;
import org.deegree.ogcwebservices.wcts.capabilities.mdprofiles.TransformationMetadata;
import org.deegree.ogcwebservices.wcts.configuration.WCTSConfiguration;
import org.deegree.ogcwebservices.wcts.configuration.WCTSDeegreeParams;
import org.deegree.ogcwebservices.wcts.data.TransformableData;
import org.deegree.ogcwebservices.wcts.operation.GetResourceByID;
import org.deegree.ogcwebservices.wcts.operation.GetTransformation;
import org.deegree.ogcwebservices.wcts.operation.IsTransformable;
import org.deegree.ogcwebservices.wcts.operation.Transform;
import org.deegree.ogcwebservices.wcts.operation.TransformResponse;
import org.deegree.ogcwebservices.wcts.operation.TransformationReference;
import org.deegree.ogcwebservices.wcts.operation.WCTSGetCapabilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The <code>WCTService</code> class is the interface between the actual handling of an incoming request and the
 * configuration, key method is the {@link #doService(OGCWebServiceRequest)} implementation.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version February 7th 2008
 * 
 */
public class WCTService implements OGCWebService {
    private static ILogger LOG = LoggerFactory.getLogger( WCTService.class );

    private final WCTSConfiguration config;

    /**
     * The version of this wcts, will be read from the configuration.
     */
    public static String version = "0.4.0";

    /**
     * The configured crs provider to use.
     */
    public static String CRS_PROVIDER = null;

    /**
     * @param config
     */
    public WCTService( final WCTSConfiguration config ) {
        this.config = config;
        synchronized ( LOG ) {
            version = config.getVersion();
            LOG.notifyAll();
        }
        CRS_PROVIDER = config.getDeegreeParams().getConfiguredCRSProvider();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.ogcwebservices.OGCWebService#doService(org.deegree.ogcwebservices.OGCWebServiceRequest)
     */
    public Object doService( OGCWebServiceRequest request )
                            throws OGCWebServiceException {
        Object result = null;
        if ( request != null ) {
            long time = System.currentTimeMillis();
            LOG.logDebug( "Incoming request with id: " + request.getId() );
            if ( request instanceof GetResourceByID ) {
                result = handleGetResourceByID( (GetResourceByID) request );
            } else if ( request instanceof IsTransformable ) {
                result = handleIsTransformable( (IsTransformable) request );
            } else if ( request instanceof GetTransformation ) {
                throw new OGCWebServiceException( Messages.getMessage( "WCTS_OPERATION_NOT_SUPPORTED",
                                                                       "GetTransformation" ),
                                                  ExceptionCode.OPERATIONNOTSUPPORTED );
            } else if ( request instanceof WCTSGetCapabilities ) {
                result = handleCapabilities( (WCTSGetCapabilities) request );
            } else if ( request instanceof Transform ) {
                result = handleTransform( (Transform) request );
            } else {
                throw new OGCWebServiceException( request.toString(), Messages.getMessage( "WCTS_UNKNOWN_REQUEST" ),
                                                  ExceptionCode.OPERATIONNOTSUPPORTED );
            }
            LOG.logDebug( "The handling of request with id: " + request.getId() + " took: "
                          + ( System.currentTimeMillis() - time ) / 1000. + " seconds" );
        }
        if ( result == null ) {
            LOG.logError( Messages.getMessage( "WCTS_ILLEGAL_STATE" ) + " incoming request is: " + request );
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_ILLEGAL_STATE" ),
                                              ExceptionCode.NOAPPLICABLECODE );
        }

        return result;
    }

    /**
     * @param request
     *            to be handled.
     * @return the response to a GetResourceByID request (i.e. a gml:Dictionary with the description of the resource in
     *         gml3 ).
     */
    private XMLFragment handleGetResourceByID( GetResourceByID request ) {
        Document doc = create();
        Element root = doc.createElementNS( GMLNS.toASCIIString(), GML_PREFIX + ":Dictionary" );
        XMLFragment response = new XMLFragment( root );
        List<String> ids = request.getResourceIDs();
        // for a crs this might be the way, maybe enhance the CRSProvider api to get an
        // Identifiable?
        CRSProvider provider = CRSConfiguration.getCRSConfiguration().getProvider();
        List<org.deegree.crs.coordinatesystems.CoordinateSystem> requestedCRSs = new ArrayList<org.deegree.crs.coordinatesystems.CoordinateSystem>(
                                                                                                                                                    ids.size() );
        for ( String id : ids ) {
            org.deegree.crs.coordinatesystems.CoordinateSystem tmp = provider.getCRSByID( id );
            if ( tmp != null ) {
                requestedCRSs.add( tmp );
            } else {
                // what to do here? maybe an exception or try something else?.
            }
        }

        return response;
    }

    /**
     * @param request
     *            to be handled.
     * @return a IsTransformable response, never <code>null</code>.
     */
    private XMLFragment handleIsTransformable( IsTransformable request ) {
        Document doc = create();
        Element root = doc.createElementNS( WCTSNS.toASCIIString(), WCTS_PREFIX + ":IsTransformableResponse" );
        XMLFragment response = new XMLFragment( root );

        Content content = config.getContents();
        List<String> problems = new ArrayList<String>();
        if ( request.getSourceCRS() == null ) {
            problems.add( "SourceCRS" );
        } else {
            if ( content != null ) {
                List<CoordinateSystem> sourceCRSs = content.getSourceCRSs();
                if ( sourceCRSs == null || !sourceCRSs.contains( request.getSourceCRS() ) ) {
                    problems.add( "SourceCRS" );
                }
            }
        }
        if ( request.getTargetCRS() == null ) {
            problems.add( "TargetCRS" );
        } else {
            if ( content != null ) {
                List<CoordinateSystem> targetCRSs = content.getTargetCRSs();
                if ( targetCRSs == null || !targetCRSs.contains( request.getTargetCRS() ) ) {
                    problems.add( "TargetCRS" );
                }
            }
        }

        // currently not supported operations.
        if ( request.getCoverageTypes() != null && request.getCoverageTypes().size() > 0 ) {
            problems.add( "CoverageType" );
        }
        if ( request.getInterpolationTypes() != null && request.getInterpolationTypes().size() > 0 ) {
            problems.add( "InterpolationMethod" );
        }

        // check for geometry types.
        List<Pair<String, String>> requestedGeoms = request.getGeometryTypes();
        boolean geometriesFitConfigured = true;
        if ( requestedGeoms != null && requestedGeoms.size() > 0 ) {
            if ( content != null ) {
                FeatureAbilities featureAbilities = content.getFeatureAbilities();
                if ( featureAbilities != null ) {
                    List<Pair<String, String>> configuredGeomTypes = featureAbilities.getGeometryTypes();
                    if ( configuredGeomTypes != null && configuredGeomTypes.size() > 0 ) {
                        for ( Pair<String, String> requestedGeometry : requestedGeoms ) {
                            if ( geometriesFitConfigured && !configuredGeomTypes.contains( requestedGeometry ) ) {
                                problems.add( "GeometryType" );
                                geometriesFitConfigured = false;
                            }
                        }
                    } else {
                        problems.add( "GeometryType" );
                        geometriesFitConfigured = false;
                    }
                } else {
                    problems.add( "GeometryType" );
                    geometriesFitConfigured = false;
                }
            } else {
                problems.add( "GeometryType" );
                geometriesFitConfigured = false;
            }
        }
        if ( geometriesFitConfigured ) {
            // the requested geometries did fit the configured geometries, but does deegree support them too?
            if ( requestedGeoms != null ) {
                boolean deegreeSupported = true;
                for ( Pair<String, String> requestedGeometry : requestedGeoms ) {
                    if ( deegreeSupported && requestedGeometry != null ) {
                        String value = requestedGeometry.first;
                        if ( !GMLGeometryAdapter.isGeometrieSupported( value ) ) {
                            problems.add( "GeometryType" );
                            deegreeSupported = false;
                        }
                    }
                }
            }
        }
        root.setAttribute( "transformable", problems.size() == 0 ? "true" : "false" );
        if ( problems.size() != 0 ) {
            for ( String problem : problems ) {
                if ( problem != null && !"".equals( problem.trim() ) ) {
                    Element problemo = appendElement( root, WCTSNS, WCTS_PREFIX + ":problem", problem );
                    problemo.setAttribute( "codeSpace", "http://schemas.opengis.net/wcts/0.0.0/problemType.xml" );
                }
            }
        }
        return response;
    }

    /**
     * @return the capabilities according to the request.
     */
    private XMLFragment handleCapabilities( WCTSGetCapabilities request ) {
        XMLFragment result = XMLFactory.create( getCapabilities() );
        Element oldRoot = result.getRootElement();
        Document doc = XMLTools.create();
        Element root = (Element) doc.importNode( oldRoot, true );
        doc.appendChild( root );
        List<String> sections = request.getSections();
        if ( sections.size() > 0 ) {
            try {
                // sections
                if ( !sections.contains( "all" ) ) {
                    if ( !sections.contains( "serviceidentification" ) ) {
                        Element remove = getElement( root, OWS_1_1_0PREFIX + ":ServiceIdentification",
                                                     getNamespaceContext() );
                        if ( remove != null ) {
                            root.removeChild( remove );
                        }
                    }
                    if ( !sections.contains( "serviceprovider" ) ) {
                        Element remove = getElement( root, OWS_1_1_0PREFIX + ":ServiceProvider", getNamespaceContext() );
                        if ( remove != null ) {
                            root.removeChild( remove );
                        }
                    }
                    if ( !sections.contains( "operationsmetadata" ) ) {
                        Element remove = getElement( root, OWS_1_1_0PREFIX + ":OperationsMetadata",
                                                     getNamespaceContext() );
                        if ( remove != null ) {
                            root.removeChild( remove );
                        }
                    }
                    if ( !sections.contains( "contents" ) ) {
                        Element remove = getElement( root, WCTS_PREFIX + ":Contents", getNamespaceContext() );
                        if ( remove != null ) {
                            root.removeChild( remove );
                        }
                    }
                }
            } catch ( XMLParsingException e ) {
                LOG.logError( "Could not handle requested 'sections' parameter because: " + e.getMessage(), e );
            }
        }
        return new XMLFragment( root );
    }

    /**
     * @param request
     *            to be handled
     * @return the response or <code>null</code> if the request was <code>null</code>.
     * @throws OGCWebServiceException
     *             if the inputdata could not be found, or the given crs are not configured to be supported.
     */
    public TransformResponse handleTransform( Transform request )
                            throws OGCWebServiceException {
        if ( request == null ) {
            return null;
        }
        // get the data and call transform.
        TransformableData<?> transformableData = request.getTransformableData();
        if ( transformableData == null ) {
            throw new OGCWebServiceException( Messages.getMessage( "WCTS_TRANSFORM_MISSING_DATA" ),
                                              WCTSExceptionCode.NO_INPUT_DATA );
        }
        Content content = config.getContents();
        CoordinateSystem sourceCRS = request.getSourceCRS();
        CoordinateSystem targetCRS = request.getTargetCRS();
        Transformation transform = null;
        if ( content != null ) {
            if ( sourceCRS != null && targetCRS != null ) {
                List<CoordinateSystem> sourceCRSs = content.getSourceCRSs();
                if ( sourceCRSs == null || !sourceCRSs.contains( request.getSourceCRS() ) ) {
                    throw new OGCWebServiceException( Messages.getMessage( "CRS_UNKNOWNCRS",
                                                                           request.getSourceCRS().getIdentifier() ),
                                                      ExceptionCode.INVALID_SRS );
                }
                List<CoordinateSystem> targetCRSs = content.getTargetCRSs();
                if ( targetCRSs == null || !targetCRSs.contains( request.getTargetCRS() ) ) {
                    throw new OGCWebServiceException( Messages.getMessage( "CRS_UNKNOWNCRS",
                                                                           request.getTargetCRS().getIdentifier() ),
                                                      ExceptionCode.INVALID_SRS );
                }
            } else {
                // if no source and target crs were given, lets find the transformation.
                TransformationReference transRef = request.getTransformationReference();
                if ( transRef != null ) {
                    Map<String, Transformation> transformations = content.getTransformations();
                    String transformID = transRef.gettransformationId();
                    if ( transformID == null ) {
                        throw new OGCWebServiceException( Messages.getMessage( "WCTS_NO_TRANSFORM_ID" ),
                                                          ExceptionCode.MISSINGPARAMETERVALUE );
                    }
                    transform = transformations.get( transformID );
                    if ( transform == null ) {
                        List<MetadataProfile<?>> transformMetadata = content.getTransformMetadata();
                        if ( transformMetadata.size() > 0 ) {
                            for ( int i = 0; i < transformMetadata.size() && sourceCRS == null; ++i ) {
                                MetadataProfile<?> mp = transformMetadata.get( i );
                                if ( mp != null && ( mp instanceof TransformationMetadata ) ) {
                                    if ( transformID.equals( ( (TransformationMetadata) mp ).getTransformID() ) ) {
                                        sourceCRS = ( (TransformationMetadata) mp ).getSourceCRS();
                                        targetCRS = ( (TransformationMetadata) mp ).getTargetCRS();
                                    }
                                }
                            }
                        } else {
                            throw new OGCWebServiceException( Messages.getMessage( "WCTS_INVALID_TRANSFORM",
                                                                                   transformID ),
                                                              ExceptionCode.MISSINGPARAMETERVALUE );
                        }
                    }
                } else {
                    throw new OGCWebServiceException( Messages.getMessage( "WCTS_NOT_VALID_XML_CHOICE",
                                                                           "SourceCRS/TargetCRS and Transformation" ),
                                                      ExceptionCode.MISSINGPARAMETERVALUE );
                }

            }
        }
        if ( transform != null ) {
            transformableData.doTransform( transform, request.extensiveLogging() );
            sourceCRS = CRSFactory.create( transform.getSourceCRS() );
            targetCRS = CRSFactory.create( transform.getTargetCRS() );
        } else {
            if ( sourceCRS == null || targetCRS == null ) {
                throw new OGCWebServiceException( Messages.getMessage( "WCTS_NO_TRANSFORM_ID",
                                                                       ExceptionCode.MISSINGPARAMETERVALUE ) );
            }
            transformableData.doTransform( sourceCRS, targetCRS, request.extensiveLogging() );
        }

        return new TransformResponse( sourceCRS, targetCRS, request.getDataPresentation(), request.mustStore(),
                                      request.getInputData(), transformableData );
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.ogcwebservices.OGCWebService#getCapabilities()
     */
    public WCTSCapabilities getCapabilities() {
        return config;
    }

    /**
     * The configuration of this wcts.
     * 
     * @return the configuration of this wcts.
     */
    public final WCTSConfiguration getConfiguration() {
        return config;
    }

    /**
     * @return the deegree specific parameters for the wcts.
     */
    public final WCTSDeegreeParams getDeegreeParams() {
        return config.getDeegreeParams();
    }

}

//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.services.wfs.format.gml.request;

import static java.math.BigInteger.ZERO;
import static org.deegree.commons.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.deegree.commons.ows.exception.OWSException.OPERATION_PROCESSING_FAILED;
import static org.deegree.commons.ows.exception.OWSException.OPTION_NOT_SUPPORTED;
import static org.deegree.commons.tom.datetime.ISO8601Converter.formatDateTime;
import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.stax.XMLStreamUtils.writeNamespaceIfNotBound;
import static org.deegree.gml.GMLOutputFactory.createGMLStreamWriter;
import static org.deegree.gml.GMLVersion.GML_2;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.WFSConstants.WFS_100_BASIC_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_110_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;
import static org.deegree.protocol.wfs.getfeature.ResultType.RESULTS;
import static org.deegree.services.wfs.query.StoredQueryHandler.GET_FEATURE_BY_ID;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ResolveParams;
import org.deegree.commons.tom.datetime.DateTime;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.KVPUtils;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.lock.Lock;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.projection.ProjectionClause;
import org.deegree.filter.projection.PropertyName;
import org.deegree.geometry.Envelope;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.reference.GmlXlinkOptions;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.kvp.GetFeature200KVPEncoder;
import org.deegree.protocol.wfs.getfeaturewithlock.GetFeatureWithLock;
import org.deegree.protocol.wfs.query.StandardPresentationParams;
import org.deegree.protocol.wfs.query.StoredQuery;
import org.deegree.services.controller.OGCFrontController;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.i18n.Messages;
import org.deegree.services.wfs.WebFeatureService;
import org.deegree.services.wfs.WfsFeatureStoreManager;
import org.deegree.services.wfs.format.gml.BufferableXMLStreamWriter;
import org.deegree.services.wfs.format.gml.GmlFormat;
import org.deegree.services.wfs.query.QueryAnalyzer;
import org.deegree.services.wfs.util.ResponsePagingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles {@link GetFeature} and {@link GetFeatureWithLock} requests for the {@link GmlFormat}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GmlGetFeatureHandler extends AbstractGmlRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger( GmlGetFeatureHandler.class );

    /**
     * Creates a new {@link GmlGetFeatureHandler} instance.
     * 
     * @param format
     *            never <code>null</code>
     */
    public GmlGetFeatureHandler( GmlFormat format ) {
        super( format );
    }

    /**
     * Performs the given {@link GetFeature} request.
     * 
     * @param request
     *            request to be handled, never <code>null</code>
     * @param response
     *            response that is used to write the result, never <code>null</code>
     */
    public void doGetFeatureResults( GetFeature request, HttpResponseBuffer response )
                            throws Exception {

        LOG.debug( "Performing GetFeature (results) request." );

        GMLVersion gmlVersion = options.getGmlVersion();

        QueryAnalyzer analyzer = new QueryAnalyzer( request.getQueries(), format.getMaster(),
                                                    format.getMaster().getStoreManager(), options.isCheckAreaOfUse() );
        Lock lock = acquireLock( request, analyzer );

        String schemaLocation = getSchemaLocation( request.getVersion(), analyzer.getFeatureTypes() );

        int traverseXLinkDepth = 0;
        BigInteger resolveTimeout = null;
        String xLinkTemplate = getObjectXlinkTemplate( request.getVersion(), gmlVersion );

        if ( VERSION_110.equals( request.getVersion() ) || VERSION_200.equals( request.getVersion() ) ) {
            if ( request.getResolveParams().getDepth() != null ) {
                if ( "*".equals( request.getResolveParams().getDepth() ) ) {
                    traverseXLinkDepth = -1;
                } else {
                    try {
                        traverseXLinkDepth = Integer.parseInt( request.getResolveParams().getDepth() );
                    } catch ( NumberFormatException e ) {
                        String msg = Messages.get( "WFS_TRAVERSEXLINKDEPTH_INVALID",
                                                   request.getResolveParams().getDepth() );
                        throw new OWSException( new InvalidParameterValueException( msg ) );
                    }
                }
            }
            if ( request.getResolveParams().getTimeout() != null ) {
                resolveTimeout = request.getResolveParams().getTimeout();
                // needed for CITE 1.1.0 compliance (wfs:GetFeature-traverseXlinkExpiry)
                if ( resolveTimeout == null || resolveTimeout.equals( ZERO ) ) {
                    String msg = Messages.get( "WFS_TRAVERSEXLINKEXPIRY_ZERO", resolveTimeout );
                    throw new OWSException( new InvalidParameterValueException( msg ) );
                }
            }
        }

        // quick check if local references in the output can be ruled out
        boolean localReferencesPossible = localReferencesPossible( analyzer, traverseXLinkDepth );

        String contentType = options.getMimeType();
        XMLStreamWriter xmlStream = WebFeatureService.getXMLResponseWriter( response, contentType, schemaLocation );
        xmlStream = new BufferableXMLStreamWriter( xmlStream, xLinkTemplate );

        QName memberElementName = determineFeatureMemberElement( request.getVersion() );

        QName responseContainerEl = options.getResponseContainerEl();

        boolean isGetFeatureById = isGetFeatureByIdRequest( request );

        // open "wfs:FeatureCollection" element
        if ( request.getVersion().equals( VERSION_100 ) ) {
            if ( responseContainerEl != null ) {
                xmlStream.setPrefix( responseContainerEl.getPrefix(), responseContainerEl.getNamespaceURI() );
                xmlStream.writeStartElement( responseContainerEl.getNamespaceURI(), responseContainerEl.getLocalPart() );
                xmlStream.writeNamespace( responseContainerEl.getPrefix(), responseContainerEl.getNamespaceURI() );
            } else {
                xmlStream.setPrefix( "wfs", WFS_NS );
                xmlStream.writeStartElement( WFS_NS, "FeatureCollection" );
                xmlStream.writeNamespace( "wfs", WFS_NS );
                if ( lock != null ) {
                    xmlStream.writeAttribute( "lockId", lock.getId() );
                }
            }
            prebindNamespaces( xmlStream, format.getGmlFormatOptions().getPrebindNamespaces() );
        } else if ( request.getVersion().equals( VERSION_110 ) ) {
            if ( responseContainerEl != null ) {
                xmlStream.setPrefix( responseContainerEl.getPrefix(), responseContainerEl.getNamespaceURI() );
                xmlStream.writeStartElement( responseContainerEl.getNamespaceURI(), responseContainerEl.getLocalPart() );
                xmlStream.writeNamespace( responseContainerEl.getPrefix(), responseContainerEl.getNamespaceURI() );
            } else {
                xmlStream.setPrefix( "wfs", WFS_NS );
                xmlStream.writeStartElement( WFS_NS, "FeatureCollection" );
                xmlStream.writeNamespace( "wfs", WFS_NS );
                if ( lock != null ) {
                    xmlStream.writeAttribute( "lockId", lock.getId() );
                }
                xmlStream.writeAttribute( "timeStamp", getTimestamp() );
            }
            prebindNamespaces( xmlStream, format.getGmlFormatOptions().getPrebindNamespaces() );
        } else if ( request.getVersion().equals( VERSION_200 ) && ( !isGetFeatureById ) ) {
            xmlStream.setPrefix( "wfs", WFS_200_NS );
            xmlStream.writeStartElement( WFS_200_NS, "FeatureCollection" );
            xmlStream.writeNamespace( "wfs", WFS_200_NS );
            xmlStream.writeAttribute( "timeStamp", getTimestamp() );
            if ( lock != null ) {
                xmlStream.writeAttribute( "lockId", lock.getId() );
            }
            prebindNamespaces( xmlStream, format.getGmlFormatOptions().getPrebindNamespaces() );
        }

        if ( !isGetFeatureById ) {
            // ensure that namespace for feature member elements is bound
            writeNamespaceIfNotBound( xmlStream, memberElementName.getPrefix(), memberElementName.getNamespaceURI() );

            // ensure that namespace for gml (e.g. geometry elements) is bound
            writeNamespaceIfNotBound( xmlStream, "gml", gmlVersion.getNamespace() );

            if ( GML_32 == gmlVersion && !request.getVersion().equals( VERSION_200 ) ) {
                xmlStream.writeAttribute( "gml", GML3_2_NS, "id", "WFS_RESPONSE" );
            }
        }

        int returnMaxFeatures = options.getQueryMaxFeatures();
        BigInteger count = request.getPresentationParams().getCount();
        if ( count != null && ( options.getQueryMaxFeatures() < 1 || count.intValue() < options.getQueryMaxFeatures() ) ) {
            returnMaxFeatures = count.intValue();
        }

        int startIndex = 0;
        if ( request.getPresentationParams().getStartIndex() != null ) {
            startIndex = request.getPresentationParams().getStartIndex().intValue();
        }

        GMLStreamWriter gmlStream = createGMLStreamWriter( gmlVersion, xmlStream );
        gmlStream.setProjections( analyzer.getProjections() );
        gmlStream.setOutputCrs( analyzer.getRequestedCRS() );
        gmlStream.setCoordinateFormatter( options.getFormatter() );
        gmlStream.setGenerateBoundedByForFeatures( options.isGenerateBoundedByForFeatures() );
        gmlStream.setGeometrySimplifier( options.getGeometrySimplifier() );
        Map<String, String> prefixToNs = new HashMap<String, String>(
                                                                      format.getMaster().getStoreManager().getPrefixToNs() );
        prefixToNs.putAll( getFeatureTypeNsPrefixes( analyzer.getFeatureTypes() ) );
        gmlStream.setNamespaceBindings( prefixToNs );
        GmlXlinkOptions resolveOptions = new GmlXlinkOptions( request.getResolveParams() );
        WfsXlinkStrategy additionalObjects = new WfsXlinkStrategy( (BufferableXMLStreamWriter) xmlStream,
                                                                   localReferencesPossible, xLinkTemplate,
                                                                   resolveOptions );
        gmlStream.setReferenceResolveStrategy( additionalObjects );

        if ( isGetFeatureById ) {
            writeSingleFeatureMember( gmlStream, analyzer, resolveOptions );
        } else if ( options.isDisableStreaming() ) {
            ResponsePagingUris responsePagingUris = null;
            if ( options.isEnableResponsePaging() )
                responsePagingUris = createResponsePagingUris( request, count, startIndex );
            writeFeatureMembersCached( request.getVersion(), gmlStream, analyzer, gmlVersion, returnMaxFeatures,
                                       startIndex, memberElementName, lock, responsePagingUris );
        } else {
            writeFeatureMembersStream( request.getVersion(), gmlStream, analyzer, gmlVersion, returnMaxFeatures,
                                       startIndex, memberElementName, lock );
        }

        if ( !isGetFeatureById ) {
            writeAdditionalObjects( gmlStream, additionalObjects, memberElementName, request.getVersion() );

            // close container element
            xmlStream.writeEndElement();
        }
        xmlStream.flush();

        // append buffered parts of the stream
        if ( ( (BufferableXMLStreamWriter) xmlStream ).hasBuffered() ) {
            ( (BufferableXMLStreamWriter) xmlStream ).appendBufferedXML( gmlStream );
        }
    }
    private ResponsePagingUris createResponsePagingUrisHits( GetFeature request )
                            throws UnknownCRSException, XMLStreamException, TransformationException,
                            UnsupportedEncodingException, FilterEvaluationException, FeatureStoreException,
                            OWSException {
        StandardPresentationParams requestPresentationParams = request.getPresentationParams();
        BigInteger count = requestPresentationParams.getCount();
        int startIndex = 0;
        if ( requestPresentationParams.getStartIndex() != null ) {
            startIndex = requestPresentationParams.getStartIndex().intValue();
        }

        StandardPresentationParams presentationParams = new StandardPresentationParams( requestPresentationParams.getStartIndex(),
                                                                                        count,
                                                                                        RESULTS,
                                                                                        requestPresentationParams.getOutputFormat() );
        GetFeature nextGetFeature = new GetFeature( request.getVersion(), request.getHandle(),
                                                    presentationParams,
                                                    request.getResolveParams(), request.getQueries() );
        if ( count != null ) {
            Map<String, String> kvpGetFeature = GetFeature200KVPEncoder.export( nextGetFeature );
            String nextUri = createUrlWithStartindex( kvpGetFeature, startIndex );
            return new ResponsePagingUris( nextUri, null );
        }
        return createResponsePagingUris( nextGetFeature, count, startIndex );
    }

    private ResponsePagingUris createResponsePagingUris( GetFeature request, BigInteger count, int startIndex )
                            throws UnknownCRSException, XMLStreamException, TransformationException,
                            UnsupportedEncodingException, FilterEvaluationException, FeatureStoreException,
                            OWSException {
        if ( count != null ) {
            Map<String, String> kvpGetFeature = GetFeature200KVPEncoder.export( request );
            String nextUri = createNextUri( count, startIndex, kvpGetFeature, request );
            String previousUri = createPreviousUri( count, startIndex, kvpGetFeature );
            return new ResponsePagingUris( nextUri, previousUri );
        }
        return null;
    }

    private String createNextUri( BigInteger count, int startIndex, Map<String, String> kvpGetFeature,
                                  GetFeature request )
                            throws OWSException, FeatureStoreException, FilterEvaluationException {
        QueryAnalyzer analyzer = new QueryAnalyzer( request.getQueries(), format.getMaster(),
                                                    format.getMaster().getStoreManager(), options.isCheckAreaOfUse() );
        Hits hits = retrieveHits( request, analyzer );
        int nextStartIndex = ResponsePagingUtils.calculateNextStartIndex( startIndex, count.intValue(), hits.hitsTotal );
        if ( nextStartIndex >= 0 )
            return createUrlWithStartindex( kvpGetFeature, nextStartIndex );
        return null;
    }

    private String createPreviousUri( BigInteger count, int startIndex, Map<String, String> kvpGetFeature ) {
        int previousStartIndex = ResponsePagingUtils.calculatePreviousStartIndex( startIndex, count.intValue() );
        if ( previousStartIndex >= 0 )
            return createUrlWithStartindex( kvpGetFeature, previousStartIndex );
        return null;
    }

    public void doGetFeatureHits( GetFeature request, HttpResponseBuffer response )
                            throws OWSException, XMLStreamException, IOException, FeatureStoreException,
                            FilterEvaluationException, TransformationException, UnknownCRSException {

        LOG.debug( "Performing GetFeature (hits) request." );

        QueryAnalyzer analyzer = new QueryAnalyzer( request.getQueries(), format.getMaster(),
                                                    format.getMaster().getStoreManager(), options.isCheckAreaOfUse() );
        Lock lock = acquireLock( request, analyzer );
        String schemaLocation = null;
        if ( VERSION_100.equals( request.getVersion() ) ) {
            schemaLocation = WFS_NS + " " + WFS_100_BASIC_SCHEMA_URL;
        } else if ( VERSION_110.equals( request.getVersion() ) ) {
            schemaLocation = WFS_NS + " " + WFS_110_SCHEMA_URL;
        } else if ( VERSION_200.equals( request.getVersion() ) ) {
            schemaLocation = WFS_200_NS + " " + WFS_200_SCHEMA_URL;
        }

        String contentType = options.getMimeType();
        XMLStreamWriter xmlStream = WebFeatureService.getXMLResponseWriter( response, contentType, schemaLocation );

        Hits hits = retrieveHits( request, analyzer );

        // open "wfs:FeatureCollection" element
        if ( request.getVersion().equals( VERSION_100 ) ) {
            xmlStream.setPrefix( "wfs", WFS_NS );
            xmlStream.writeStartElement( WFS_NS, "FeatureCollection" );
            xmlStream.writeNamespace( "wfs", WFS_NS );
            if ( lock != null ) {
                xmlStream.writeAttribute( "lockId", lock.getId() );
            }
            xmlStream.writeAttribute( "numberOfFeatures", "" + hits.hitsTotal );
        } else if ( request.getVersion().equals( VERSION_110 ) ) {
            xmlStream.setPrefix( "wfs", WFS_NS );
            xmlStream.writeStartElement( WFS_NS, "FeatureCollection" );
            xmlStream.writeNamespace( "wfs", WFS_NS );
            if ( lock != null ) {
                xmlStream.writeAttribute( "lockId", lock.getId() );
            }
            xmlStream.writeAttribute( "timeStamp", getTimestamp() );
            xmlStream.writeAttribute( "numberOfFeatures", "" + hits.hitsTotal );
        } else if ( request.getVersion().equals( VERSION_200 ) ) {
            xmlStream.setPrefix( "wfs", WFS_200_NS );
            xmlStream.writeStartElement( WFS_200_NS, "FeatureCollection" );
            xmlStream.writeNamespace( "wfs", WFS_200_NS );
            xmlStream.writeAttribute( "timeStamp", getTimestamp() );
            xmlStream.writeAttribute( "numberMatched", "" + hits.hitsTotal );
            xmlStream.writeAttribute( "numberReturned", "0" );
            if ( options.isEnableResponsePaging() ) {
                ResponsePagingUris responsePagingUris = createResponsePagingUrisHits( request );
                writeResponsePagingUris( xmlStream, responsePagingUris );
            }
            if ( hits.queryHits.length > 1 ) {
                for ( int j = 0; j < hits.queryHits.length; j++ ) {
                    xmlStream.writeStartElement( "wfs", "member", WFS_200_NS );
                    xmlStream.writeEmptyElement( "wfs", "FeatureCollection", WFS_200_NS );
                    xmlStream.writeAttribute( "timeStamp", formatDateTime( hits.queryTimeStamps[j] ) );
                    xmlStream.writeAttribute( "numberMatched", "" + hits.queryHits[j] );
                    xmlStream.writeAttribute( "numberReturned", "0" );
                    xmlStream.writeEndElement();
                }
            }
        }

        // "gml:boundedBy" is necessary for GML 2 schema compliance
        if ( options.getGmlVersion().equals( GMLVersion.GML_2 ) ) {
            xmlStream.writeStartElement( "gml", "boundedBy", GMLNS );
            xmlStream.writeStartElement( GMLNS, "null" );
            xmlStream.writeCharacters( "unknown" );
            xmlStream.writeEndElement();
            xmlStream.writeEndElement();
        }

        // close "wfs:FeatureCollection"
        xmlStream.writeEndElement();
        xmlStream.flush();
    }

    private Hits retrieveHits( GetFeature request, QueryAnalyzer analyzer )
                            throws FeatureStoreException, FilterEvaluationException {
        Map<org.deegree.protocol.wfs.query.Query, Integer> wfsQueryToIndex = new HashMap<org.deegree.protocol.wfs.query.Query, Integer>();
        int i = 0;
        for ( org.deegree.protocol.wfs.query.Query query : request.getQueries() ) {
            wfsQueryToIndex.put( query, i++ );
        }

        int hitsTotal = 0;
        int[] queryHits = new int[wfsQueryToIndex.size()];
        DateTime[] queryTimeStamps = new DateTime[queryHits.length];

        for ( Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet() ) {
            FeatureStore fs = fsToQueries.getKey();
            Query[] queries = fsToQueries.getValue().toArray( new Query[fsToQueries.getValue().size()] );
            int[] hits = fs.queryHits( queries );

            // map the hits from the feature store back to the original query sequence
            for ( int j = 0; j < hits.length; j++ ) {
                Query query = queries[j];
                int singleHits = hits[j];
                org.deegree.protocol.wfs.query.Query wfsQuery = analyzer.getQuery( query );
                int index = wfsQueryToIndex.get( wfsQuery );
                hitsTotal += singleHits;
                queryHits[index] = queryHits[index] + singleHits;
                queryTimeStamps[index] = getCurrentDateTimeWithoutMilliseconds();
            }
        }
        return new Hits( hitsTotal, queryHits, queryTimeStamps );
    }

    private void prebindNamespaces( final XMLStreamWriter xmlStream, final NamespaceBindings prebindNamespaces )
                            throws XMLStreamException {
        if ( prebindNamespaces == null ) {
            return;
        }
        final Iterator<String> prefixes = prebindNamespaces.getPrefixes();
        while ( prefixes.hasNext() ) {
            final String prefix = prefixes.next();
            final String uri = prebindNamespaces.getNamespaceURI( prefix );
            if ( !uri.equals( CommonNamespaces.XMLNS ) ) {
                writeNamespaceIfNotBound( xmlStream, prefix, uri );
            }
        }
    }

    private void writeFeatureMembersStream( Version wfsVersion, GMLStreamWriter gmlStream, QueryAnalyzer analyzer,
                                            GMLVersion outputFormat, int maxFeatures, int startIndex,
                                            QName featureMemberEl, Lock lock )
                            throws XMLStreamException, UnknownCRSException, TransformationException,
                            FeatureStoreException, FilterEvaluationException, FactoryConfigurationError {

        XMLStreamWriter xmlStream = gmlStream.getXMLStream();

        if ( wfsVersion.equals( VERSION_200 ) ) {
            xmlStream.writeAttribute( "numberMatched", "unknown" );
            xmlStream.writeAttribute( "numberReturned", "0" );
            xmlStream.writeComment( "NOTE: numberReturned attribute should be 'unknown' as well, but this would not validate against the current version of the WFS 2.0 schema (change upcoming). See change request (CR 144): https://portal.opengeospatial.org/files?artifact_id=43925." );
        }

        if ( outputFormat == GML_2 ) {
            // "gml:boundedBy" is necessary for GML 2 schema compliance
            xmlStream.writeStartElement( "gml", "boundedBy", GMLNS );
            xmlStream.writeStartElement( GMLNS, "null" );
            xmlStream.writeCharacters( "unknown" );
            xmlStream.writeEndElement();
            xmlStream.writeEndElement();
        }

        // retrieve and write result features
        int featuresAdded = 0;
        int featuresSkipped = 0;
        GmlXlinkOptions resolveState = gmlStream.getReferenceResolveStrategy().getResolveOptions();
        for ( Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet() ) {
            FeatureStore fs = fsToQueries.getKey();
            Query[] queries = fsToQueries.getValue().toArray( new Query[fsToQueries.getValue().size()] );
            FeatureInputStream rs = fs.query( queries );
            try {
                for ( Feature member : rs ) {
                    if ( lock != null && !lock.isLocked( member.getId() ) ) {
                        continue;
                    }
                    if ( featuresAdded == maxFeatures ) {
                        // limit the number of features written to maxfeatures
                        break;
                    }
                    if ( featuresSkipped < startIndex ) {
                        featuresSkipped++;
                    } else {
                        writeMemberFeature( member, gmlStream, xmlStream, resolveState, featureMemberEl );
                        featuresAdded++;
                    }
                }
            } finally {
                LOG.debug( "Closing FeatureResultSet (stream)" );
                rs.close();
            }
        }
    }

    private void writeFeatureMembersCached( Version wfsVersion, GMLStreamWriter gmlStream, QueryAnalyzer analyzer,
                                            GMLVersion outputFormat, int maxFeatures, int startIndex,
                                            QName featureMemberEl, Lock lock, ResponsePagingUris responsePagingUris )
                            throws XMLStreamException, UnknownCRSException, TransformationException,
                            FeatureStoreException, FilterEvaluationException, FactoryConfigurationError {

        FeatureCollection allFeatures = new GenericFeatureCollection();
        Set<String> fids = new HashSet<String>();

        // retrieve maxfeatures features
        int featuresAdded = 0;
        int featuresSkipped = 0;
        for ( Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet() ) {
            FeatureStore fs = fsToQueries.getKey();
            Query[] queries = fsToQueries.getValue().toArray( new Query[fsToQueries.getValue().size()] );
            FeatureInputStream rs = fs.query( queries );
            try {
                for ( Feature feature : rs ) {
                    if ( lock != null && !lock.isLocked( feature.getId() ) ) {
                        continue;
                    }
                    if ( featuresAdded == maxFeatures ) {
                        break;
                    }
                    if ( featuresSkipped < startIndex ) {
                        featuresSkipped++;
                    } else if ( !fids.contains( feature.getId() ) ) {
                        allFeatures.add( feature );
                        fids.add( feature.getId() );
                        featuresAdded++;
                    }
                }
            } finally {
                LOG.debug( "Closing FeatureResultSet (cached)" );
                rs.close();
            }
        }

        XMLStreamWriter xmlStream = gmlStream.getXMLStream();
        if ( wfsVersion.equals( VERSION_200 ) ) {
            xmlStream.writeAttribute( "numberMatched", "" + allFeatures.size() );
            xmlStream.writeAttribute( "numberReturned", "" + allFeatures.size() );
            writeResponsePagingUris( xmlStream, responsePagingUris );
        } else if ( !wfsVersion.equals( VERSION_100 ) && options.getResponseContainerEl() == null ) {
            xmlStream.writeAttribute( "numberOfFeatures", "" + allFeatures.size() );
        }

        if ( outputFormat == GML_2 || allFeatures.getEnvelope() != null ) {
            writeBoundedBy( wfsVersion, gmlStream, outputFormat, allFeatures.getEnvelope() );
        }

        // retrieve and write result features
        GmlXlinkOptions resolveState = gmlStream.getReferenceResolveStrategy().getResolveOptions();
        for ( Feature member : allFeatures ) {
            writeMemberFeature( member, gmlStream, xmlStream, resolveState, featureMemberEl );
        }
    }

    private void writeResponsePagingUris( XMLStreamWriter xmlStream, ResponsePagingUris responsePagingUris )
                            throws XMLStreamException {
        if ( responsePagingUris == null )
            return;
        if ( responsePagingUris.nextUri != null )
            xmlStream.writeAttribute( "next", "" + responsePagingUris.nextUri );
        if ( responsePagingUris.previousUri != null )
            xmlStream.writeAttribute( "previous", "" + responsePagingUris.previousUri );
    }

    private void writeBoundedBy( Version wfsVersion, GMLStreamWriter gmlStream, GMLVersion outputFormat, Envelope env )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        XMLStreamWriter xmlStream = gmlStream.getXMLStream();
        switch ( outputFormat ) {
        case GML_2: {
            xmlStream.writeStartElement( "gml", "boundedBy", GMLNS );
            if ( env == null ) {
                xmlStream.writeStartElement( "gml", "null", GMLNS );
                xmlStream.writeCharacters( "inapplicable" );
                xmlStream.writeEndElement();
            } else {
                gmlStream.write( env );
            }
            xmlStream.writeEndElement();
            break;
        }
        case GML_30:
        case GML_31: {
            xmlStream.writeStartElement( "gml", "boundedBy", GMLNS );
            if ( env == null ) {
                xmlStream.writeStartElement( "gml", "Null", GMLNS );
                xmlStream.writeCharacters( "inapplicable" );
                xmlStream.writeEndElement();
            } else {
                gmlStream.write( env );
            }
            xmlStream.writeEndElement();
            break;
        }
        case GML_32: {
            if ( wfsVersion.equals( VERSION_200 ) ) {
                xmlStream.writeStartElement( "wfs", "boundedBy", GML3_2_NS );
                if ( env == null ) {
                    xmlStream.writeStartElement( "gml", "Null", GML3_2_NS );
                    xmlStream.writeCharacters( "inapplicable" );
                    xmlStream.writeEndElement();
                } else {
                    gmlStream.write( env );
                }
                xmlStream.writeEndElement();
            } else {
                xmlStream.writeStartElement( "gml", "boundedBy", GML3_2_NS );
                if ( env == null ) {
                    xmlStream.writeStartElement( "gml", "Null", GML3_2_NS );
                    xmlStream.writeCharacters( "inapplicable" );
                    xmlStream.writeEndElement();
                } else {
                    gmlStream.write( env );
                }
                xmlStream.writeEndElement();
            }
            break;
        }
        }
    }

    private Lock acquireLock( GetFeature request, QueryAnalyzer analyzer )
                            throws OWSException {

        Lock lock = null;

        if ( request instanceof GetFeatureWithLock ) {
            GetFeatureWithLock gfLock = (GetFeatureWithLock) request;

            // CITE 1.1.0 compliance (wfs:GetFeatureWithLock-Xlink)
            if ( analyzer.getProjections() != null ) {
                for ( List<ProjectionClause> projections : analyzer.getProjections().values() ) {
                    for ( ProjectionClause clause : projections ) {
                        if ( clause instanceof PropertyName ) {
                            PropertyName propName = (PropertyName) clause;
                            ResolveParams resolveParams = propName.getResolveParams();
                            if ( resolveParams.getDepth() != null || resolveParams.getMode() != null
                                 || resolveParams.getTimeout() != null ) {
                                throw new OWSException( "GetFeatureWithLock does not support XlinkPropertyName",
                                                        OPTION_NOT_SUPPORTED );
                            }
                        }
                    }
                }
            }

            // default: lock all
            boolean lockAll = true;
            if ( gfLock.getLockAll() != null ) {
                lockAll = gfLock.getLockAll();
            }

            // default: 5 minutes
            long expiryInMilliseconds = 5 * 60 * 1000;
            if ( gfLock.getExpiryInSeconds() != null ) {
                expiryInMilliseconds = gfLock.getExpiryInSeconds().longValue() * 1000;
            }

            LockManager manager = null;
            try {
                // TODO strategy for multiple LockManagers / feature stores
                WfsFeatureStoreManager storeManager = format.getMaster().getStoreManager();
                manager = storeManager.getStores()[0].getLockManager();
                List<Query> queries = analyzer.getQueries().get( storeManager.getStores()[0] );
                lock = manager.acquireLock( queries, lockAll, expiryInMilliseconds );
            } catch ( OWSException e ) {
                throw new OWSException( e.getMessage(), "CannotLockAllFeatures" );
            } catch ( FeatureStoreException e ) {
                throw new OWSException( "Cannot acquire lock: " + e.getMessage(), NO_APPLICABLE_CODE );
            }
        }
        return lock;
    }

    private boolean isGetFeatureByIdRequest( GetFeature request ) {
        if ( request.getQueries().size() == 1 ) {
            if ( request.getQueries().get( 0 ) instanceof StoredQuery ) {
                StoredQuery getFeatureByIdQuery = (StoredQuery) request.getQueries().get( 0 );
                if ( getFeatureByIdQuery.getId().equals( GET_FEATURE_BY_ID ) ) {
                    LOG.debug( "processing " + GET_FEATURE_BY_ID + " request" );
                    return true;
                }
            }
        }
        return false;
    }

    private void writeSingleFeatureMember( GMLStreamWriter gmlStream, QueryAnalyzer analyzer,
                                           GmlXlinkOptions resolveState )
                            throws XMLStreamException, UnknownCRSException, TransformationException,
                            FeatureStoreException, FilterEvaluationException, OWSException {

        FeatureCollection allFeatures = new GenericFeatureCollection();
        Set<String> fids = new HashSet<String>();

        for ( Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet() ) {
            FeatureStore fs = fsToQueries.getKey();
            Query[] queries = fsToQueries.getValue().toArray( new Query[fsToQueries.getValue().size()] );
            FeatureInputStream rs = fs.query( queries );
            try {
                for ( Feature feature : rs ) {
                    if ( !fids.contains( feature.getId() ) ) {
                        allFeatures.add( feature );
                        fids.add( feature.getId() );
                        break;
                    }
                }
            } catch ( RuntimeException e ) {
                throw new OWSException( e.getLocalizedMessage(), OPERATION_PROCESSING_FAILED );
            } finally {
                LOG.debug( "Closing FeatureResultSet (cached)" );
                rs.close();
            }
        }
        if ( fids.isEmpty() ) {
            throw new OWSException( "feature not found", OPERATION_PROCESSING_FAILED );
        }
        for ( Feature member : allFeatures ) {
            gmlStream.getFeatureWriter().export( member, resolveState );
            break;
        }
    }

    private String createUrlWithStartindex( Map<String, String> kvpGetFeature, int startIndex ) {
        kvpGetFeature.put( "STARTINDEX", "" + startIndex );
        return OGCFrontController.getHttpGetURL() + KVPUtils.toQueryString( kvpGetFeature );
    }

    private class ResponsePagingUris {

        private final String nextUri;

        private final String previousUri;

        private ResponsePagingUris( String nextUri, String previousUri ) {
            this.nextUri = nextUri;
            this.previousUri = previousUri;
        }

    }

    private class Hits {

        private final int hitsTotal;

        private final int[] queryHits;

        private final DateTime[] queryTimeStamps;

        private Hits( int hitsTotal, int[] queryHits, DateTime[] queryTimeStamps ) {
            this.hitsTotal = hitsTotal;
            this.queryHits = queryHits;
            this.queryTimeStamps = queryTimeStamps;
        }

    }

}

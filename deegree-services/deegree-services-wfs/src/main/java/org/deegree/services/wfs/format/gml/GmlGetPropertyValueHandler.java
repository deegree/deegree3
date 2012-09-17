//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.services.wfs.format.gml;

import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_SCHEMA_URL;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.xpath.GMLObjectXPathEvaluator;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.GMLFeatureWriter;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.wfs.getpropertyvalue.GetPropertyValue;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.i18n.Messages;
import org.deegree.services.wfs.QueryAnalyzer;
import org.deegree.services.wfs.WebFeatureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class GmlGetPropertyValueHandler extends AbstractGmlRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger( GmlGetPropertyValueHandler.class );

    /**
     * @param storeManager
     */
    GmlGetPropertyValueHandler( GMLFormat format ) {
        super( format );
    }

    void doGetPropertyValueResult( GetPropertyValue request, HttpResponseBuffer response )
                            throws Exception {

        LOG.debug( "doGetPropertyValue: " + request );

        QueryAnalyzer analyzer = new QueryAnalyzer( Collections.singletonList( request.getQuery() ),
                                                    format.getMaster(), format.getMaster().getStoreManager(),
                                                    options.isCheckAreaOfUse() );
        String schemaLocation = getSchemaLocation( request.getVersion(), analyzer.getFeatureTypes() );

        GMLVersion gmlVersion = options.getGmlVersion();

        int traverseXLinkDepth = 0;
        String xLinkTemplate = getObjectXlinkTemplate( request.getVersion(), gmlVersion );

        if ( request.getResolveParams().getDepth() != null ) {
            if ( "*".equals( request.getResolveParams().getDepth() ) ) {
                traverseXLinkDepth = -1;
            } else {
                try {
                    traverseXLinkDepth = Integer.parseInt( request.getResolveParams().getDepth() );
                } catch ( NumberFormatException e ) {
                    String msg = Messages.get( "WFS_TRAVERSEXLINKDEPTH_INVALID", request.getResolveParams().getDepth() );
                    throw new OWSException( new InvalidParameterValueException( msg ) );
                }
            }
        }
        BigInteger resolveTimeout = request.getResolveParams().getTimeout();

        // quick check if local references in the output can be ruled out
        boolean localReferencesPossible = localReferencesPossible( analyzer, traverseXLinkDepth );

        String contentType = options.getMimeType();
        XMLStreamWriter xmlStream = WebFeatureService.getXMLResponseWriter( response, contentType, schemaLocation );
        xmlStream = new BufferableXMLStreamWriter( xmlStream, xLinkTemplate );

        // open "wfs:ValueCollection" element
        xmlStream.setPrefix( "wfs", WFS_200_NS );
        xmlStream.writeStartElement( WFS_200_NS, "ValueCollection" );
        xmlStream.writeNamespace( "wfs", WFS_200_NS );
        xmlStream.writeAttribute( "timeStamp", getTimestamp() );
        xmlStream.writeAttribute( "numberMatched", "UNKNOWN" );
        xmlStream.writeAttribute( "numberReturned", "UNKNOWN" );

        GMLStreamWriter gmlStream = GMLOutputFactory.createGMLStreamWriter( gmlVersion, xmlStream );
        gmlStream.setRemoteXLinkTemplate( xLinkTemplate );
        gmlStream.setXLinkDepth( traverseXLinkDepth );
        gmlStream.setXLinkExpiry( resolveTimeout == null ? -1 : resolveTimeout.intValue() );
        gmlStream.setProjection( analyzer.getProjection() );
        gmlStream.setOutputCrs( analyzer.getRequestedCRS() );
        gmlStream.setCoordinateFormatter( options.getFormatter() );
        gmlStream.setGenerateBoundedByForFeatures( options.isGenerateBoundedByForFeatures() );
        Map<String, String> prefixToNs = new HashMap<String, String>(
                                                                      format.getMaster().getStoreManager().getPrefixToNs() );
        prefixToNs.putAll( getFeatureTypeNsPrefixes( analyzer.getFeatureTypes() ) );
        gmlStream.setNamespaceBindings( prefixToNs );
        XlinkedObjectsHandler additionalObjects = new XlinkedObjectsHandler( (BufferableXMLStreamWriter) xmlStream,
                                                                             localReferencesPossible, xLinkTemplate );
        gmlStream.setAdditionalObjectHandler( additionalObjects );

        // retrieve and write result features
        int startIndex = 0;
        int maxResults = -1;
        // TODO evaluate if maxResults should have a default / configurable value
        if ( request.getPresentationParams().getCount() != null ) {
            maxResults = request.getPresentationParams().getCount().intValue();
        }
        if ( request.getPresentationParams().getStartIndex() != null ) {
            startIndex = request.getPresentationParams().getStartIndex().intValue();
        }

        GMLObjectXPathEvaluator evaluator = new GMLObjectXPathEvaluator();
        GMLFeatureWriter featureWriter = gmlStream.getFeatureWriter();

        int numberReturned = 0;
        int valuesSkipped = 0;
        for ( Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet() ) {
            FeatureStore fs = fsToQueries.getKey();
            Query[] queries = fsToQueries.getValue().toArray( new Query[fsToQueries.getValue().size()] );
            FeatureInputStream rs = fs.query( queries );
            try {
                for ( Feature member : rs ) {
                    if ( numberReturned == maxResults ) {
                        break;
                    }
                    TypedObjectNode[] values = evaluator.eval( member, request.getValueReference() );
                    for ( TypedObjectNode value : values ) {
                        if ( valuesSkipped < startIndex ) {
                            valuesSkipped++;
                        } else {
                            xmlStream.writeStartElement( WFS_200_NS, "member" );
                            featureWriter.export( value, 0, traverseXLinkDepth );
                            xmlStream.writeEndElement();
                            numberReturned++;
                            if ( numberReturned == maxResults ) {
                                break;
                            }
                        }
                    }
                }
            } finally {
                LOG.debug( "Closing FeatureResultSet (stream)" );
                rs.close();
            }
        }

        if ( !additionalObjects.getAdditionalRefs().isEmpty() ) {
            xmlStream.writeStartElement( WFS_200_NS, "additionalValues" );
            xmlStream.writeStartElement( WFS_200_NS, "SimpleFeatureCollection" );
            writeAdditionalObjects( gmlStream, additionalObjects, traverseXLinkDepth, new QName( WFS_200_NS, "member" ) );
            xmlStream.writeEndElement();
            xmlStream.writeEndElement();
        }

        // close container element
        xmlStream.writeEndElement();
        xmlStream.flush();

        // append buffered parts of the stream
        if ( ( (BufferableXMLStreamWriter) xmlStream ).hasBuffered() ) {
            ( (BufferableXMLStreamWriter) xmlStream ).appendBufferedXML( gmlStream );
        }
    }

    void doGetPropertyValueHits( GetPropertyValue request, HttpResponseBuffer response )
                            throws FeatureStoreException, FilterEvaluationException, IOException, OWSException,
                            XMLStreamException {

        LOG.debug( "Performing doGetPropertyValue (HITS) request: " + request );

        QueryAnalyzer analyzer = new QueryAnalyzer( Collections.singletonList( request.getQuery() ),
                                                    format.getMaster(), format.getMaster().getStoreManager(),
                                                    options.isCheckAreaOfUse() );
        String contentType = options.getMimeType();
        String schemaLocation = WFS_200_NS + " " + WFS_200_SCHEMA_URL;
        XMLStreamWriter xmlStream = WebFeatureService.getXMLResponseWriter( response, contentType, schemaLocation );

        GMLObjectXPathEvaluator evaluator = new GMLObjectXPathEvaluator();

        int numFeatures = 0;

        for ( Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet() ) {
            FeatureStore fs = fsToQueries.getKey();
            Query[] queries = fsToQueries.getValue().toArray( new Query[fsToQueries.getValue().size()] );
            FeatureInputStream rs = fs.query( queries );
            try {
                for ( Feature member : rs ) {
                    TypedObjectNode[] values = evaluator.eval( member, request.getValueReference() );
                    numFeatures += values.length;
                }
            } finally {
                LOG.debug( "Closing FeatureResultSet (stream)" );
                rs.close();
            }
        }

        xmlStream.setPrefix( "wfs", WFS_200_NS );
        xmlStream.writeStartElement( WFS_200_NS, "ValueCollection" );
        xmlStream.writeNamespace( "wfs", WFS_200_NS );
        xmlStream.writeAttribute( "timeStamp", getTimestamp() );
        xmlStream.writeAttribute( "numberMatched", Integer.toString( numFeatures ) );
        xmlStream.writeAttribute( "numberReturned", "0" );
        xmlStream.writeEndElement();
        xmlStream.flush();
    }

}

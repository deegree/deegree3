//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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

import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.gml.GMLVersion.GML_2;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_PREFIX;
import static org.deegree.protocol.wfs.getfeature.ResultType.RESULTS;
import static org.deegree.services.controller.exception.ControllerException.NO_APPLICABLE_CODE;
import static org.deegree.services.controller.ows.OWSException.OPERATION_NOT_SUPPORTED;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.lock.Lock;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.CoordinateFormatter;
import org.deegree.gml.GMLObject;
import org.deegree.gml.GMLOutputFactory;
import org.deegree.gml.GMLReference;
import org.deegree.gml.GMLStreamWriter;
import org.deegree.gml.GMLVersion;
import org.deegree.protocol.wfs.describefeaturetype.DescribeFeatureType;
import org.deegree.protocol.wfs.getfeature.BBoxQuery;
import org.deegree.protocol.wfs.getfeature.FeatureIdQuery;
import org.deegree.protocol.wfs.getfeature.FilterQuery;
import org.deegree.protocol.wfs.getfeature.GetFeature;
import org.deegree.protocol.wfs.getfeature.ResultType;
import org.deegree.protocol.wfs.getfeaturewithlock.GetFeatureWithLock;
import org.deegree.protocol.wfs.getgmlobject.GetGmlObject;
import org.deegree.protocol.wfs.lockfeature.BBoxLock;
import org.deegree.protocol.wfs.lockfeature.FeatureIdLock;
import org.deegree.protocol.wfs.lockfeature.FilterLock;
import org.deegree.protocol.wfs.lockfeature.LockOperation;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.i18n.Messages;
import org.deegree.services.wfs.GetFeatureAnalyzer;
import org.deegree.services.wfs.WFSController;
import org.deegree.services.wfs.WFService;
import org.deegree.services.wfs.format.OutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link OutputFormat} implementation that can handle GML 2/3.0/3.1/3.2.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class GMLOutputFormat implements OutputFormat {

    private static final Logger LOG = LoggerFactory.getLogger( GMLOutputFormat.class );

    final GMLVersion gmlVersion;

    private final String mimeType;

    private final QName responseContainerEl;

    private final String schemaLocation;

    private final boolean disableStreaming;

    private final WFSController master;

    private final WFService service;

    private final int featureLimit;

    private final boolean checkAreaOfUse;

    private final CoordinateFormatter formatter;

    private final DescribeFeatureTypeHandler dftHandler;

    /**
     * 
     * @param master
     *            corresponding WFS controller
     * @param disableStreaming
     *            if <code>true</code>, features are not streamed (implies that the FeatureCollection's
     *            boundedBy-element is populated and that the numberOfFeatures attribute is be written)
     * @param formatter
     *            coordinate formatter to use, must not be <code>null</code>
     * @param mimeType
     * @param responseContainerEl
     * @param schemaLocation
     */
    GMLOutputFormat( WFSController master, boolean disableStreaming, CoordinateFormatter formatter, String mimeType,
                     QName responseContainerEl, String schemaLocation ) {
        this.master = master;
        this.service = master.getService();
        this.dftHandler = new DescribeFeatureTypeHandler( service );
        this.disableStreaming = disableStreaming;
        this.featureLimit = master.getMaxFeatures();
        this.checkAreaOfUse = master.getCheckAreaOfUse();
        this.formatter = formatter;
        this.mimeType = mimeType;
        this.gmlVersion = GMLVersion.fromMimeType( mimeType, GML_31 );
        this.responseContainerEl = responseContainerEl;
        this.schemaLocation = schemaLocation;
    }

    @Override
    public void doDescribeFeatureType( DescribeFeatureType request, HttpResponseBuffer response )
                            throws OWSException, XMLStreamException, IOException {
        dftHandler.doDescribeFeatureType( request, response, this );
    }

    @Override
    public void doGetFeature( GetFeature request, HttpResponseBuffer response )
                            throws Exception {
        ResultType type = request.getResultType();
        if ( type == RESULTS || type == null ) {
            doResults( request, response );
        } else {
            doHits( request, response );
        }
    }

    @Override
    public void doGetGmlObject( GetGmlObject request, HttpResponseBuffer response )
                            throws Exception {

        LOG.debug( "doGetGmlObject: " + request );

        GMLObject o = retrieveObject( request.getRequestedId() );

        response.setContentType( gmlVersion.getMimeType() );

        int traverseXLinkDepth = 0;
        if ( request.getTraverseXlinkDepth() != null ) {
            if ( "*".equals( request.getTraverseXlinkDepth() ) ) {
                traverseXLinkDepth = -1;
            } else {
                try {
                    traverseXLinkDepth = Integer.parseInt( request.getTraverseXlinkDepth() );
                } catch ( NumberFormatException e ) {
                    String msg = Messages.get( "WFS_TRAVERSEXLINKDEPTH_INVALID", request.getTraverseXlinkDepth() );
                    throw new OWSException( new InvalidParameterValueException( msg ) );
                }

            }
        }

        String schemaLocation = null;
        if ( o instanceof Feature ) {
            schemaLocation = WFSController.getSchemaLocation( request.getVersion(), gmlVersion,
                                                              ( (Feature) o ).getName() );
        } else if ( o instanceof Geometry ) {
            switch ( gmlVersion ) {
            case GML_2:
                schemaLocation = GMLNS + " http://schemas.opengis.net/gml/2.1.2.1/geometry.xsd";
                break;
            case GML_30:
                schemaLocation = GMLNS + " http://schemas.opengis.net/gml/3.0.1/base/geometryComplexes.xsd";
                break;
            case GML_31:
                schemaLocation = GMLNS + " http://schemas.opengis.net/gml/3.1.1/base/geometryComplexes.xsd";
                break;
            case GML_32:
                schemaLocation = GML3_2_NS + " http://schemas.opengis.net/gml/3.2.1/geometryComplexes.xsd";
                break;
            }
        } else {
            String msg = "Error exporting GML object: only exporting of features and geometries is implemented.";
            throw new OWSException( msg, OPERATION_NOT_SUPPORTED );
        }
        XMLStreamWriter xmlStream = WFSController.getXMLResponseWriter( response, schemaLocation );
        GMLStreamWriter gmlStream = GMLOutputFactory.createGMLStreamWriter( gmlVersion, xmlStream );
        gmlStream.setOutputCRS( master.getDefaultQueryCrs() );
        gmlStream.setLocalXLinkTemplate( master.getObjectXlinkTemplate( request.getVersion(), gmlVersion ) );
        gmlStream.setXLinkDepth( traverseXLinkDepth );
        gmlStream.setCoordinateFormatter( formatter );
        gmlStream.setNamespaceBindings( service.getPrefixToNs() );
        try {
            gmlStream.write( o );
        } catch ( UnknownCRSException e ) {
            String msg = "Error exporting GML object: " + e.getMessage();
            throw new OWSException( msg, NO_APPLICABLE_CODE );
        } catch ( TransformationException e ) {
            String msg = "Error exporting GML object: " + e.getMessage();
            throw new OWSException( msg, NO_APPLICABLE_CODE );
        }
    }

    private void doResults( GetFeature request, HttpResponseBuffer response )
                            throws Exception {

        LOG.debug( "Performing GetFeature (results) request." );

        GetFeatureAnalyzer analyzer = new GetFeatureAnalyzer( request, master, service, gmlVersion, checkAreaOfUse );
        String lockId = acquireLock( request, analyzer );
        String schemaLocation = getSchemaLocation( request.getVersion(), analyzer.getFeatureTypes() );

        int traverseXLinkDepth = 0;
        int traverseXLinkExpiry = -1;
        String xLinkTemplate = master.getObjectXlinkTemplate( request.getVersion(), gmlVersion );

        if ( VERSION_110.equals( request.getVersion() ) || VERSION_200.equals( request.getVersion() ) ) {
            if ( request.getTraverseXlinkDepth() != null ) {
                if ( "*".equals( request.getTraverseXlinkDepth() ) ) {
                    traverseXLinkDepth = -1;
                } else {
                    try {
                        traverseXLinkDepth = Integer.parseInt( request.getTraverseXlinkDepth() );
                    } catch ( NumberFormatException e ) {
                        String msg = Messages.get( "WFS_TRAVERSEXLINKDEPTH_INVALID", request.getTraverseXlinkDepth() );
                        throw new OWSException( new InvalidParameterValueException( msg ) );
                    }
                }
            }
            if ( request.getTraverseXlinkExpiry() != null ) {
                traverseXLinkExpiry = request.getTraverseXlinkExpiry();
                // needed for CITE 1.1.0 compliance (wfs:GetFeature-traverseXlinkExpiry)
                if ( traverseXLinkExpiry <= 0 ) {
                    String msg = Messages.get( "WFS_TRAVERSEXLINKEXPIRY_ZERO", request.getTraverseXlinkDepth() );
                    throw new OWSException( new InvalidParameterValueException( msg ) );
                }
            }
        }

        XMLStreamWriter xmlStream = WFSController.getXMLResponseWriter( response, schemaLocation );
        setContentType( gmlVersion, response );

        // open "wfs:FeatureCollection" element
        if ( request.getVersion().equals( VERSION_100 ) ) {
            if ( responseContainerEl != null ) {
                xmlStream.writeStartElement( responseContainerEl.getPrefix(), responseContainerEl.getLocalPart(),
                                             responseContainerEl.getNamespaceURI() );
            } else {
                xmlStream.writeStartElement( "wfs", "FeatureCollection", WFS_NS );
                if ( lockId != null ) {
                    xmlStream.writeAttribute( "lockId", lockId );
                }
            }
        } else if ( request.getVersion().equals( VERSION_110 ) ) {
            if ( responseContainerEl != null ) {
                xmlStream.writeStartElement( responseContainerEl.getPrefix(), responseContainerEl.getLocalPart(),
                                             responseContainerEl.getNamespaceURI() );
            } else {
                xmlStream.writeStartElement( "wfs", "FeatureCollection", WFS_NS );
                if ( lockId != null ) {
                    xmlStream.writeAttribute( "lockId", lockId );
                }
                xmlStream.writeAttribute( "timeStamp", DateUtils.formatISO8601Date( new Date() ) );
            }
        } else if ( request.getVersion().equals( VERSION_200 ) ) {
            if ( responseContainerEl != null ) {
                xmlStream.writeStartElement( responseContainerEl.getPrefix(), responseContainerEl.getLocalPart(),
                                             responseContainerEl.getNamespaceURI() );
            } else {
                xmlStream.writeStartElement( "wfs", "FeatureCollection", WFS_200_NS );
                xmlStream.writeAttribute( "timeStamp", DateUtils.formatISO8601Date( new Date() ) );
            }
        }

        if ( GML_32 == gmlVersion ) {
            xmlStream.writeAttribute( "gml", GML3_2_NS, "id", "WFS_RESPONSE" );
        }

        int maxFeatures = featureLimit;
        if ( request.getMaxFeatures() != null && request.getMaxFeatures() < maxFeatures ) {
            maxFeatures = request.getMaxFeatures();
        }

        if ( disableStreaming ) {
            writeFeatureMembersCached( request.getVersion(), xmlStream, analyzer, gmlVersion, xLinkTemplate,
                                       traverseXLinkDepth, traverseXLinkExpiry, maxFeatures );
        } else {
            writeFeatureMembersStream( request.getVersion(), xmlStream, analyzer, gmlVersion, xLinkTemplate,
                                       traverseXLinkDepth, traverseXLinkExpiry, maxFeatures );
        }

        // close container element
        xmlStream.writeEndElement();
        xmlStream.flush();
    }

    private void writeFeatureMembersStream( Version wfsVersion, XMLStreamWriter xmlStream, GetFeatureAnalyzer analyzer,
                                            GMLVersion outputFormat, String xLinkTemplate, int traverseXLinkDepth,
                                            int traverseXLinkExpiry, int maxFeatures )
                            throws XMLStreamException, UnknownCRSException, TransformationException,
                            FeatureStoreException, FilterEvaluationException {

        GMLStreamWriter gmlStream = GMLOutputFactory.createGMLStreamWriter( outputFormat, xmlStream );
        gmlStream.setOutputCRS( analyzer.getRequestedCRS() );
        gmlStream.setCoordinateFormatter( formatter );
        gmlStream.setFeatureProperties( analyzer.getRequestedProps() );
        gmlStream.setLocalXLinkTemplate( xLinkTemplate );
        gmlStream.setXLinkDepth( traverseXLinkDepth );
        gmlStream.setXLinkExpiry( traverseXLinkExpiry );
        gmlStream.setXLinkFeatureProperties( analyzer.getXLinkProps() );
        gmlStream.setNamespaceBindings( service.getPrefixToNs() );
        XlinkedObjectsHandler additionalObjects = new XlinkedObjectsHandler();
        gmlStream.setAdditionalObjectHandler( additionalObjects );
        bindFeatureTypePrefixes( xmlStream, analyzer.getFeatureTypes() );

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
        for ( Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet() ) {
            FeatureStore fs = fsToQueries.getKey();
            Query[] queries = fsToQueries.getValue().toArray( new Query[fsToQueries.getValue().size()] );
            FeatureResultSet rs = fs.query( queries );
            try {
                for ( Feature member : rs ) {
                    writeMemberFeature( member, gmlStream, xmlStream, wfsVersion );
                    featuresAdded++;
                    if ( featuresAdded == maxFeatures ) {
                        // limit the number of features written to maxfeatures
                        break;
                    }
                }
            } finally {
                LOG.debug( "Closing FeatureResultSet (stream)" );
                rs.close();
            }
        }

        if ( !additionalObjects.getAdditionalRefs().isEmpty() ) {
            xmlStream.writeComment( "Additional features (subfeatures of requested features)" );
            writeAdditionalObjects( wfsVersion, gmlStream, additionalObjects, traverseXLinkDepth );
        }
    }

    private void writeFeatureMembersCached( Version wfsVersion, XMLStreamWriter xmlStream, GetFeatureAnalyzer analyzer,
                                            GMLVersion outputFormat, String xLinkTemplate, int traverseXLinkDepth,
                                            int traverseXLinkExpiry, int maxFeatures )
                            throws XMLStreamException, UnknownCRSException, TransformationException,
                            FeatureStoreException, FilterEvaluationException {

        FeatureCollection allFeatures = new GenericFeatureCollection();
        Set<String> fids = new HashSet<String>();

        // retrieve maxfeatures features
        int featuresAdded = 0;
        for ( Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet() ) {
            FeatureStore fs = fsToQueries.getKey();
            Query[] queries = fsToQueries.getValue().toArray( new Query[fsToQueries.getValue().size()] );
            FeatureResultSet rs = fs.query( queries );
            try {
                for ( Feature feature : rs ) {
                    if ( !fids.contains( feature.getId() ) ) {
                        allFeatures.add( feature );
                        fids.add( feature.getId() );
                        featuresAdded++;
                        if ( featuresAdded == maxFeatures ) {
                            break;
                        }
                    }
                }
            } finally {
                LOG.debug( "Closing FeatureResultSet (cached)" );
                rs.close();
            }
        }

        if ( !wfsVersion.equals( VERSION_100 ) && responseContainerEl == null ) {
            xmlStream.writeAttribute( "numberOfFeatures", "" + allFeatures.size() );
        }

        XlinkedObjectsHandler additionalObjects = new XlinkedObjectsHandler();

        GMLStreamWriter gmlStream = GMLOutputFactory.createGMLStreamWriter( outputFormat, xmlStream );
        gmlStream.setLocalXLinkTemplate( xLinkTemplate );
        gmlStream.setXLinkDepth( traverseXLinkDepth );
        gmlStream.setXLinkExpiry( traverseXLinkExpiry );
        gmlStream.setXLinkFeatureProperties( analyzer.getXLinkProps() );
        gmlStream.setFeatureProperties( analyzer.getRequestedProps() );
        gmlStream.setOutputCRS( analyzer.getRequestedCRS() );
        gmlStream.setCoordinateFormatter( formatter );
        gmlStream.setNamespaceBindings( service.getPrefixToNs() );
        gmlStream.setAdditionalObjectHandler( additionalObjects );
        bindFeatureTypePrefixes( xmlStream, analyzer.getFeatureTypes() );

        if ( outputFormat == GML_2 || allFeatures.getEnvelope() != null ) {
            writeBoundedBy( gmlStream, outputFormat, allFeatures.getEnvelope() );
        }

        // retrieve and write result features
        for ( Feature member : allFeatures ) {
            writeMemberFeature( member, gmlStream, xmlStream, wfsVersion );
        }

        if ( !additionalObjects.getAdditionalRefs().isEmpty() ) {
            xmlStream.writeComment( "Additional features (subfeatures of requested features)" );
            writeAdditionalObjects( wfsVersion, gmlStream, additionalObjects, traverseXLinkDepth );
        }
    }

    private void writeAdditionalObjects( Version wfsVersion, GMLStreamWriter gmlStream,
                                         XlinkedObjectsHandler additionalObjects, int traverseXLinkDepth )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        int currentLevel = 1;
        Collection<GMLReference<?>> includeObjects = additionalObjects.getAdditionalRefs();

        while ( ( traverseXLinkDepth == -1 || currentLevel++ <= traverseXLinkDepth ) && !includeObjects.isEmpty() ) {
            additionalObjects.clear();
            for ( GMLReference<?> gmlReference : includeObjects ) {
                Feature feature = (Feature) gmlReference;
                writeMemberFeature( feature, gmlStream, gmlStream.getXMLStream(), wfsVersion );
            }
            includeObjects = additionalObjects.getAdditionalRefs();
        }
    }

    private void writeMemberFeature( Feature member, GMLStreamWriter gmlStream, XMLStreamWriter xmlStream,
                                     Version wfsVersion )
                            throws XMLStreamException, UnknownCRSException, TransformationException {
        if ( gmlStream.isObjectExported( member.getId() ) ) {
            if ( GML_32 == gmlVersion ) {
                if ( VERSION_200.equals( wfsVersion ) ) {
                    xmlStream.writeEmptyElement( "wfs", "member", WFS_200_NS );
                } else {
                    xmlStream.writeEmptyElement( "gml", "featureMember", GML3_2_NS );
                    // xmlStream.writeEmptyElement( "wfs", "member", WFS_NS );
                }
            } else {
                xmlStream.writeEmptyElement( "gml", "featureMember", GMLNS );
            }
            xmlStream.writeAttribute( "xlink", XLNNS, "href", "#" + member.getId() );
        } else {
            if ( GML_32 == gmlVersion ) {
                if ( VERSION_200.equals( wfsVersion ) ) {
                    xmlStream.writeStartElement( "wfs", "member", WFS_200_NS );
                } else {
                    xmlStream.writeStartElement( "gml", "featureMember", GML3_2_NS );
                    // xmlStream.writeStartElement( "wfs", "member", WFS_NS );
                }
            } else {
                xmlStream.writeStartElement( "gml", "featureMember", GMLNS );
            }
            gmlStream.write( member );
            xmlStream.writeEndElement();
        }
    }

    private void writeBoundedBy( GMLStreamWriter gmlStream, GMLVersion outputFormat, Envelope env )
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
            xmlStream.writeStartElement( "gml", "boundedBy", GML3_2_NS );
            if ( env == null ) {
                xmlStream.writeStartElement( "gml", "Null", GML3_2_NS );
                xmlStream.writeCharacters( "inapplicable" );
                xmlStream.writeEndElement();
            } else {
                gmlStream.write( env );
            }
            xmlStream.writeEndElement();
            break;
        }
        }
    }

    private void bindFeatureTypePrefixes( XMLStreamWriter xmlStream, Collection<FeatureType> fts )
                            throws XMLStreamException {

        if ( fts == null ) {
            fts = service.getFeatureTypes();
        }

        Map<String, String> nsToPrefix = new HashMap<String, String>();
        for ( FeatureType ft : fts ) {
            QName ftName = ft.getName();
            if ( ftName.getPrefix() != null ) {
                nsToPrefix.put( ftName.getNamespaceURI(), ftName.getPrefix() );
            }
        }

        for ( Map.Entry<String, String> nsBinding : nsToPrefix.entrySet() ) {
            xmlStream.setPrefix( nsBinding.getValue(), nsBinding.getKey() );
        }
    }

    private void doHits( GetFeature request, HttpResponseBuffer response )
                            throws OWSException, XMLStreamException, IOException, FeatureStoreException,
                            FilterEvaluationException {

        LOG.debug( "Performing GetFeature (hits) request." );

        GetFeatureAnalyzer analyzer = new GetFeatureAnalyzer( request, master, service, gmlVersion, checkAreaOfUse );
        String lockId = acquireLock( request, analyzer );
        String schemaLocation = getSchemaLocation( request.getVersion(), analyzer.getFeatureTypes() );

        XMLStreamWriter xmlStream = WFSController.getXMLResponseWriter( response, schemaLocation );

        setContentType( gmlVersion, response );

        // open "wfs:FeatureCollection" element
        if ( request.getVersion().equals( VERSION_100 ) ) {
            xmlStream.writeStartElement( "wfs", "FeatureCollection", WFS_NS );
            if ( lockId != null ) {
                xmlStream.writeAttribute( "lockId", lockId );
            }
        } else if ( request.getVersion().equals( VERSION_110 ) ) {
            xmlStream.writeStartElement( "wfs", "FeatureCollection", WFS_NS );
            if ( lockId != null ) {
                xmlStream.writeAttribute( "lockId", lockId );
            }
            xmlStream.writeAttribute( "timeStamp", DateUtils.formatISO8601Date( new Date() ) );
        } else if ( request.getVersion().equals( VERSION_200 ) ) {
            xmlStream.writeStartElement( "wfs", "FeatureCollection", WFS_200_NS );
            xmlStream.writeAttribute( "timeStamp", DateUtils.formatISO8601Date( new Date() ) );
        }

        int numHits = 0;

        for ( Map.Entry<FeatureStore, List<Query>> fsToQueries : analyzer.getQueries().entrySet() ) {
            FeatureStore fs = fsToQueries.getKey();
            Query[] queries = fsToQueries.getValue().toArray( new Query[fsToQueries.getValue().size()] );
            // TODO what about features that occur multiple times as result of different queries?
            numHits += fs.queryHits( queries );
        }

        xmlStream.writeAttribute( "numberOfFeatures", "" + numHits );

        // "gml:boundedBy" is necessary for GML 2 schema compliance
        if ( gmlVersion.equals( GMLVersion.GML_2 ) ) {
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

    /**
     * Returns the value for the <code>xsi:schemaLocation</code> attribute in the response document.
     * 
     * @param requestVersion
     *            requested WFS version, must not be <code>null</code>
     * @param requestedFts
     *            requested feature types, can be <code>null</code> (any feature type may occur in the output)
     * @return value for the <code>xsi:schemaLocation</code> attribute, never <code>null</code>
     */
    private String getSchemaLocation( Version requestVersion, Collection<FeatureType> requestedFts ) {

        String schemaLocation = this.schemaLocation;
        if ( schemaLocation == null ) {
            // use "wfs:FeatureCollection" then
            QName wfsFeatureCollection = new QName( WFS_NS, "FeatureCollection", WFS_PREFIX );
            if ( responseContainerEl == null || wfsFeatureCollection.equals( responseContainerEl ) ) {
                if ( VERSION_100.equals( requestVersion ) ) {
                    if ( GML_2 == gmlVersion ) {
                        schemaLocation = WFS_NS + " http://schemas.opengis.net/wfs/1.0.0/WFS-basic.xsd";
                    } else {
                        schemaLocation = WFSController.getSchemaLocation( requestVersion, gmlVersion,
                                                                          wfsFeatureCollection );
                    }
                } else if ( VERSION_110.equals( requestVersion ) ) {
                    if ( GML_31 == gmlVersion ) {
                        schemaLocation = WFS_NS + " http://schemas.opengis.net/wfs/1.1.0/wfs.xsd";
                    } else {
                        schemaLocation = WFSController.getSchemaLocation( requestVersion, gmlVersion,
                                                                          wfsFeatureCollection );
                    }
                } else if ( VERSION_200.equals( requestVersion ) ) {
                    if ( GML_32 == gmlVersion ) {
                        schemaLocation = WFS_200_NS + " http://schemas.opengis.net/wfs/2.0.0/wfs.xsd";
                    } else {
                        schemaLocation = WFSController.getSchemaLocation( requestVersion, gmlVersion,
                                                                          wfsFeatureCollection );
                    }
                } else {
                    throw new RuntimeException( "Internal error: Unhandled WFS version: " + requestVersion );
                }
            }
        }

        if ( requestedFts == null ) {
            requestedFts = service.getFeatureTypes();
        }

        QName[] requestedFtNames = new QName[requestedFts.size()];
        int i = 0;
        for ( FeatureType requestedFt : requestedFts ) {
            requestedFtNames[i++] = requestedFt.getName();
        }

        return schemaLocation + " " + WFSController.getSchemaLocation( requestVersion, gmlVersion, requestedFtNames );
    }

    /**
     * Sets the content type header for the HTTP response.
     * 
     * TODO integrate handling for custom formats
     * 
     * @param outputFormat
     *            output format to be used, must not be <code>null</code>
     * @param response
     *            http response, must not be <code>null</code>
     */
    private void setContentType( GMLVersion outputFormat, HttpServletResponse response ) {

        switch ( outputFormat ) {
        case GML_2:
            response.setContentType( "text/xml; subtype=gml/2.1.2" );
            break;
        case GML_30:
            response.setContentType( "text/xml; subtype=gml/3.0.1" );
            break;
        case GML_31:
            response.setContentType( "text/xml; subtype=gml/3.1.1" );
            break;
        case GML_32:
            response.setContentType( "text/xml; subtype=gml/3.2.1" );
            break;
        }
    }

    private String acquireLock( GetFeature request, GetFeatureAnalyzer analyzer )
                            throws OWSException {

        String lockId = null;

        if ( request instanceof GetFeatureWithLock ) {
            GetFeatureWithLock gfLock = (GetFeatureWithLock) request;

            // CITE 1.1.0 compliance (wfs:GetFeatureWithLock-Xlink)
            if ( analyzer.getXLinkProps() != null ) {
                throw new OWSException( "GetFeatureWithLock does not support XlinkPropertyName",
                                        OWSException.OPTION_NOT_SUPPORTED );
            }

            boolean mustLockAll = true;

            // default: 5 minutes
            int expiry = 5 * 60 * 1000;
            if ( gfLock.getExpiry() != null ) {
                expiry = gfLock.getExpiry() * 60 * 1000;
            }

            LockManager manager = null;
            try {
                // TODO strategy for multiple LockManagers / feature stores
                manager = service.getStores()[0].getLockManager();

                LockOperation[] lockOperations = new LockOperation[request.getQueries().length];
                int i = 0;
                for ( org.deegree.protocol.wfs.getfeature.Query wfsQuery : request.getQueries() ) {
                    lockOperations[i++] = buildLockOperation( wfsQuery );
                }
                Lock lock = manager.acquireLock( lockOperations, mustLockAll, expiry );
                lockId = lock.getId();
            } catch ( FeatureStoreException e ) {
                throw new OWSException( "Cannot acquire lock: " + e.getMessage(), NO_APPLICABLE_CODE );
            }
        }
        return lockId;
    }

    private LockOperation buildLockOperation( org.deegree.protocol.wfs.getfeature.Query wfsQuery ) {
        LockOperation lockOperation = null;
        if ( wfsQuery instanceof BBoxQuery ) {
            BBoxQuery bboxQuery = (BBoxQuery) wfsQuery;
            lockOperation = new BBoxLock( bboxQuery.getBBox(), bboxQuery.getTypeNames() );
        } else if ( wfsQuery instanceof FeatureIdQuery ) {
            FeatureIdQuery fidQuery = (FeatureIdQuery) wfsQuery;
            lockOperation = new FeatureIdLock( fidQuery.getFeatureIds(), fidQuery.getTypeNames() );
        } else if ( wfsQuery instanceof FilterQuery ) {
            FilterQuery filterQuery = (FilterQuery) wfsQuery;
            // TODO multiple type names
            lockOperation = new FilterLock( null, filterQuery.getTypeNames()[0], filterQuery.getFilter() );
        } else {
            throw new RuntimeException();
        }
        return lockOperation;
    }

    private GMLObject retrieveObject( String id )
                            throws OWSException {
        GMLObject o = null;
        for ( FeatureStore fs : service.getStores() ) {
            try {
                o = fs.getObjectById( id );
            } catch ( FeatureStoreException e ) {
                throw new OWSException( e.getMessage(), NO_APPLICABLE_CODE );
            }
            if ( o != null ) {
                break;
            }
        }
        if ( o == null ) {
            String msg = Messages.getMessage( "WFS_NO_SUCH_OBJECT", id );
            throw new OWSException( new InvalidParameterValueException( msg ) );
        }
        return o;
    }
}
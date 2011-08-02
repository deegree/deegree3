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

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.config.ResourceInitException;
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
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.CoordinateFormatter;
import org.deegree.geometry.io.DecimalCoordinateFormatter;
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
import org.deegree.protocol.wfs.getfeature.TypeName;
import org.deegree.protocol.wfs.getfeaturewithlock.GetFeatureWithLock;
import org.deegree.protocol.wfs.getgmlobject.GetGmlObject;
import org.deegree.protocol.wfs.lockfeature.BBoxLock;
import org.deegree.protocol.wfs.lockfeature.FeatureIdLock;
import org.deegree.protocol.wfs.lockfeature.FilterLock;
import org.deegree.protocol.wfs.lockfeature.LockOperation;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.i18n.Messages;
import org.deegree.services.jaxb.wfs.GMLFormat.GetFeatureResponse;
import org.deegree.services.wfs.GetFeatureAnalyzer;
import org.deegree.services.wfs.WFSController;
import org.deegree.services.wfs.WFService;
import org.deegree.services.wfs.format.Format;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link Format} implementation that can handle GML 2/3.0/3.1/3.2.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GMLFormat implements Format {

    private static final Logger LOG = LoggerFactory.getLogger( GMLFormat.class );

    final GMLVersion gmlVersion;

    private QName responseContainerEl;

    private QName responseFeatureMemberEl;

    private String schemaLocation;

    private boolean disableStreaming;

    private final WFSController master;

    private final WFService service;

    private final int featureLimit;

    private final boolean checkAreaOfUse;

    private CoordinateFormatter formatter;

    private final DescribeFeatureTypeHandler dftHandler;

    private boolean exportOriginalSchema;

    private String appSchemaBaseURL;

    public GMLFormat( WFSController master, GMLVersion gmlVersion ) {

        this.master = master;
        this.service = master.getService();
        this.dftHandler = new DescribeFeatureTypeHandler( service, exportOriginalSchema, null );

        this.featureLimit = master.getMaxFeatures();
        this.checkAreaOfUse = master.getCheckAreaOfUse();
        this.gmlVersion = gmlVersion;
    }

    public GMLFormat( WFSController master, org.deegree.services.jaxb.wfs.GMLFormat formatDef )
                            throws ResourceInitException {

        this.master = master;
        this.service = master.getService();

        GetFeatureResponse responseConfig = formatDef.getGetFeatureResponse();
        if ( responseConfig != null ) {
            if ( responseConfig.isDisableStreaming() != null ) {
                disableStreaming = responseConfig.isDisableStreaming();
            }
            if ( responseConfig.getContainerElement() != null ) {
                responseContainerEl = responseConfig.getContainerElement();
            }
            if ( responseConfig.getFeatureMemberElement() != null ) {
                responseFeatureMemberEl = responseConfig.getFeatureMemberElement();
            }
            if ( responseConfig.getAdditionalSchemaLocation() != null ) {
                schemaLocation = responseConfig.getAdditionalSchemaLocation();
            }
            if ( responseConfig.getDisableDynamicSchema() != null ) {
                exportOriginalSchema = responseConfig.getDisableDynamicSchema().isValue();
                appSchemaBaseURL = responseConfig.getDisableDynamicSchema().getBaseURL();
                if ( appSchemaBaseURL != null && appSchemaBaseURL.endsWith( "/" ) ) {
                    appSchemaBaseURL = appSchemaBaseURL.substring( 0, appSchemaBaseURL.length() - 1 );
                }
                if ( appSchemaBaseURL != null && appSchemaBaseURL.isEmpty() ) {
                    appSchemaBaseURL = null;
                }
            }
        }

        this.dftHandler = new DescribeFeatureTypeHandler( service, exportOriginalSchema, appSchemaBaseURL );
        this.featureLimit = master.getMaxFeatures();
        this.checkAreaOfUse = master.getCheckAreaOfUse();

        this.formatter = null;
        try {
            JAXBElement<?> formatterEl = formatDef.getAbstractCoordinateFormatter();
            if ( formatterEl != null ) {
                Object formatterConf = formatterEl.getValue();
                if ( formatterConf instanceof org.deegree.services.jaxb.wfs.DecimalCoordinateFormatter ) {
                    LOG.info( "Setting up configured DecimalCoordinateFormatter." );
                    org.deegree.services.jaxb.wfs.DecimalCoordinateFormatter decimalFormatterConf = (org.deegree.services.jaxb.wfs.DecimalCoordinateFormatter) formatterConf;
                    this.formatter = new DecimalCoordinateFormatter( decimalFormatterConf.getPlaces().intValue() );
                } else if ( formatterConf instanceof org.deegree.services.jaxb.wfs.CustomCoordinateFormatter ) {
                    LOG.info( "Setting up CustomCoordinateFormatter." );
                    org.deegree.services.jaxb.wfs.CustomCoordinateFormatter customFormatterConf = (org.deegree.services.jaxb.wfs.CustomCoordinateFormatter) formatterConf;
                    this.formatter = (CoordinateFormatter) Class.forName( customFormatterConf.getJavaClass() ).newInstance();
                } else {
                    LOG.warn( "Unexpected JAXB type '" + formatterConf.getClass() + "'." );
                }
            }
        } catch ( Exception e ) {
            throw new ResourceInitException( "Error initializing coordinate formatter: " + e.getMessage(), e );
        }

        this.gmlVersion = GMLVersion.valueOf( formatDef.getGmlVersion().value() );
    }

    @Override
    public void destroy() {
        // nothing to do
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

        String contentType = getContentType( request.getOutputFormat(), request.getVersion() );
        XMLStreamWriter xmlStream = WFSController.getXMLResponseWriter( response, contentType, schemaLocation );
        GMLStreamWriter gmlStream = GMLOutputFactory.createGMLStreamWriter( gmlVersion, xmlStream );
        gmlStream.setOutputCRS( master.getDefaultQueryCrs() );
        gmlStream.setRemoteXLinkTemplate( master.getObjectXlinkTemplate( request.getVersion(), gmlVersion ) );
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

        // quick check if local references in the output can be ruled out
        boolean localReferencesPossible = localReferencesPossible( analyzer, traverseXLinkDepth );

        String contentType = getContentType( request.getOutputFormat(), request.getVersion() );
        XMLStreamWriter xmlStream = WFSController.getXMLResponseWriter( response, contentType, schemaLocation );
        xmlStream = new BufferableXMLStreamWriter( xmlStream, xLinkTemplate );

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
        if ( request.getMaxFeatures() != null && ( maxFeatures == -1 || request.getMaxFeatures() < maxFeatures ) ) {
            maxFeatures = request.getMaxFeatures();
        }

        GMLStreamWriter gmlStream = GMLOutputFactory.createGMLStreamWriter( gmlVersion, xmlStream );
        gmlStream.setRemoteXLinkTemplate( xLinkTemplate );
        gmlStream.setXLinkDepth( traverseXLinkDepth );
        gmlStream.setXLinkExpiry( traverseXLinkExpiry );
        gmlStream.setXLinkFeatureProperties( analyzer.getXLinkProps() );
        gmlStream.setFeatureProperties( analyzer.getRequestedProps() );
        gmlStream.setOutputCRS( analyzer.getRequestedCRS() );
        gmlStream.setCoordinateFormatter( formatter );
        gmlStream.setNamespaceBindings( service.getPrefixToNs() );
        XlinkedObjectsHandler additionalObjects = new XlinkedObjectsHandler( (BufferableXMLStreamWriter) xmlStream,
                                                                             localReferencesPossible, xLinkTemplate );
        gmlStream.setAdditionalObjectHandler( additionalObjects );
        bindFeatureTypePrefixes( xmlStream, analyzer.getFeatureTypes() );

        if ( disableStreaming ) {
            writeFeatureMembersCached( request.getVersion(), gmlStream, analyzer, gmlVersion, xLinkTemplate,
                                       traverseXLinkDepth, traverseXLinkExpiry, maxFeatures );
        } else {
            writeFeatureMembersStream( request.getVersion(), gmlStream, analyzer, gmlVersion, xLinkTemplate,
                                       traverseXLinkDepth, traverseXLinkExpiry, maxFeatures );
        }

        if ( !additionalObjects.getAdditionalRefs().isEmpty() ) {
            xmlStream.writeComment( "Additional features (subfeatures of requested features)" );
            writeAdditionalObjects( request.getVersion(), gmlStream, additionalObjects, traverseXLinkDepth,
                                    xLinkTemplate );
        }

        // close container element
        xmlStream.writeEndElement();
        xmlStream.flush();

        // append buffered parts of the stream
        if ( ( (BufferableXMLStreamWriter) xmlStream ).hasBuffered() ) {
            ( (BufferableXMLStreamWriter) xmlStream ).appendBufferedXML( gmlStream );
        }
    }

    private boolean localReferencesPossible( GetFeatureAnalyzer analyzer, int traverseXLinkDepth ) {
        if ( traverseXLinkDepth == 0 && analyzer.getQueries().size() == 1 ) {
            List<Query> queries = analyzer.getQueries().values().iterator().next();
            if ( queries.size() == 1 ) {
                Query query = queries.get( 0 );
                if ( query.getTypeNames().length == 1 ) {
                    TypeName typeName = query.getTypeNames()[0];
                    FeatureStore fs = analyzer.getQueries().keySet().iterator().next();
                    FeatureType ft = fs.getSchema().getFeatureType( typeName.getFeatureTypeName() );
                    for ( PropertyType pt : ft.getPropertyDeclarations() ) {
                        if ( pt instanceof FeaturePropertyType ) {
                            FeaturePropertyType fpt = (FeaturePropertyType) pt;
                            FeatureType targetFt = fpt.getValueFt();
                            if ( targetFt == null || fs.getSchema().isSubType( targetFt, ft ) ) {
                                return true;
                            }
                        }
                    }
                    LOG.debug( "Forward references can be ruled out." );
                    return false;
                }
            }
        }
        return true;
    }

    private void writeFeatureMembersStream( Version wfsVersion, GMLStreamWriter gmlStream, GetFeatureAnalyzer analyzer,
                                            GMLVersion outputFormat, String xLinkTemplate, int traverseXLinkDepth,
                                            int traverseXLinkExpiry, int maxFeatures )
                            throws XMLStreamException, UnknownCRSException, TransformationException,
                            FeatureStoreException, FilterEvaluationException, FactoryConfigurationError, IOException {

        XMLStreamWriter xmlStream = gmlStream.getXMLStream();
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
                    writeMemberFeature( member, gmlStream, xmlStream, wfsVersion, xLinkTemplate, 0 );
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
    }

    private void writeFeatureMembersCached( Version wfsVersion, GMLStreamWriter gmlStream, GetFeatureAnalyzer analyzer,
                                            GMLVersion outputFormat, String xLinkTemplate, int traverseXLinkDepth,
                                            int traverseXLinkExpiry, int maxFeatures )
                            throws XMLStreamException, UnknownCRSException, TransformationException,
                            FeatureStoreException, FilterEvaluationException, FactoryConfigurationError, IOException {

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

        XMLStreamWriter xmlStream = gmlStream.getXMLStream();
        if ( !wfsVersion.equals( VERSION_100 ) && responseContainerEl == null ) {
            xmlStream.writeAttribute( "numberOfFeatures", "" + allFeatures.size() );
        }

        if ( outputFormat == GML_2 || allFeatures.getEnvelope() != null ) {
            writeBoundedBy( gmlStream, outputFormat, allFeatures.getEnvelope() );
        }

        // retrieve and write result features
        for ( Feature member : allFeatures ) {
            writeMemberFeature( member, gmlStream, xmlStream, wfsVersion, xLinkTemplate, 0 );
        }
    }

    private void writeAdditionalObjects( Version wfsVersion, GMLStreamWriter gmlStream,
                                         XlinkedObjectsHandler additionalObjects, int traverseXLinkDepth,
                                         String xLinkTemplate )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        int currentLevel = 1;
        Collection<GMLReference<?>> includeObjects = additionalObjects.getAdditionalRefs();

        while ( ( traverseXLinkDepth == -1 || currentLevel <= traverseXLinkDepth ) && !includeObjects.isEmpty() ) {
            additionalObjects.clear();
            for ( GMLReference<?> gmlReference : includeObjects ) {
                Feature feature = (Feature) gmlReference;
                writeMemberFeature( feature, gmlStream, gmlStream.getXMLStream(), wfsVersion, xLinkTemplate,
                                    currentLevel );
            }
            includeObjects = additionalObjects.getAdditionalRefs();
            currentLevel++;
        }
    }

    private void writeMemberFeature( Feature member, GMLStreamWriter gmlStream, XMLStreamWriter xmlStream,
                                     Version wfsVersion, String xLinkTemplate, int level )
                            throws XMLStreamException, UnknownCRSException, TransformationException {

        if ( gmlStream.isObjectExported( member.getId() ) ) {
            if ( responseFeatureMemberEl != null ) {
                xmlStream.writeEmptyElement( responseFeatureMemberEl.getPrefix(),
                                             responseFeatureMemberEl.getLocalPart(),
                                             responseFeatureMemberEl.getNamespaceURI() );
            } else if ( GML_32 == gmlVersion ) {
                if ( VERSION_200.equals( wfsVersion ) ) {
                    xmlStream.writeEmptyElement( "wfs", "member", WFS_200_NS );
                } else {
                    xmlStream.writeEmptyElement( "gml", "featureMember", GML3_2_NS );
                }
            } else {
                xmlStream.writeEmptyElement( "gml", "featureMember", GMLNS );
            }
            xmlStream.writeAttribute( "xlink", XLNNS, "href", "#" + member.getId() );
        } else {
            if ( responseFeatureMemberEl != null ) {
                xmlStream.writeStartElement( responseFeatureMemberEl.getPrefix(),
                                             responseFeatureMemberEl.getLocalPart(),
                                             responseFeatureMemberEl.getNamespaceURI() );
            } else if ( GML_32 == gmlVersion ) {
                if ( VERSION_200.equals( wfsVersion ) ) {
                    xmlStream.writeStartElement( "wfs", "member", WFS_200_NS );
                } else {
                    xmlStream.writeStartElement( "gml", "featureMember", GML3_2_NS );
                }
            } else {
                xmlStream.writeStartElement( "gml", "featureMember", GMLNS );
            }
            gmlStream.getFeatureWriter().export( member, level );
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

        String contentType = getContentType( request.getOutputFormat(), request.getVersion() );
        XMLStreamWriter xmlStream = WFSController.getXMLResponseWriter( response, contentType, schemaLocation );

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
        if ( responseContainerEl == null ) {
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

        if ( schemaLocation == null || schemaLocation.isEmpty() ) {
            schemaLocation = WFSController.getSchemaLocation( requestVersion, gmlVersion, requestedFtNames );
        } else {
            schemaLocation += " " + WFSController.getSchemaLocation( requestVersion, gmlVersion, requestedFtNames );
        }

        return schemaLocation;
    }

    /**
     * Returns the content type header for the HTTP response.
     * 
     * @param outputFormat
     *            requested output format, may be <code>null</code>
     * @param version
     *            request version, must not be <code>null</code>
     * @return content type for the http header, never <code>null</code>
     */
    static String getContentType( String outputFormat, Version version ) {

        String contentType = outputFormat;
        if ( outputFormat == null ) {
            if ( VERSION_100.equals( version ) ) {
                contentType = "text/xml; subtype=gml/2.1.2";
            } else if ( VERSION_110.equals( version ) ) {
                contentType = "text/xml; subtype=gml/3.1.1";
            } else if ( VERSION_200.equals( version ) ) {
                contentType = "text/xml; subtype=gml/3.2.1";
            }
        }
        return contentType;
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
//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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

package org.deegree.services.wfs;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.ows.exception.OWSException.INVALID_PARAMETER_VALUE;
import static org.deegree.commons.ows.exception.OWSException.INVALID_VALUE;
import static org.deegree.commons.ows.exception.OWSException.MISSING_PARAMETER_VALUE;
import static org.deegree.commons.ows.exception.OWSException.NO_APPLICABLE_CODE;
import static org.deegree.commons.ows.exception.OWSException.OPERATION_NOT_SUPPORTED;
import static org.deegree.commons.xml.CommonNamespaces.FES_20_NS;
import static org.deegree.commons.xml.CommonNamespaces.OGCNS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;
import static org.deegree.commons.xml.XMLAdapter.writeElement;
import static org.deegree.commons.xml.stax.XMLStreamUtils.skipElement;
import static org.deegree.feature.Features.findFeaturesAndGeometries;
import static org.deegree.gml.GMLInputFactory.createGMLStreamReader;
import static org.deegree.gml.GMLVersion.GML_32;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_100;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_110;
import static org.deegree.protocol.wfs.WFSConstants.VERSION_200;
import static org.deegree.protocol.wfs.WFSConstants.WFS_100_TRANSACTION_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_110_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_NS;
import static org.deegree.protocol.wfs.WFSConstants.WFS_200_SCHEMA_URL;
import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;
import static org.deegree.protocol.wfs.transaction.ReleaseAction.ALL;
import static org.deegree.protocol.wfs.transaction.action.IDGenMode.GENERATE_NEW;
import static org.deegree.services.wfs.ReferenceResolvingMode.CHECK_ALL;
import static org.deegree.services.wfs.ReferenceResolvingMode.CHECK_INTERNALLY;
import static org.deegree.services.wfs.WebFeatureService.getXMLResponseWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElement;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.XMLStreamReaderWrapper;
import org.deegree.commons.xml.stax.XMLStreamUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreGMLIdResolver;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.Lock;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.filter.Filter;
import org.deegree.filter.Filters;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.expression.ValueReference;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.SimpleGeometryFactory;
import org.deegree.geometry.validation.CoordinateValidityInspector;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.GMLFeatureReader;
import org.deegree.gml.reference.FeatureReference;
import org.deegree.gml.reference.matcher.ReferencePatternMatcher;
import org.deegree.protocol.wfs.transaction.ReleaseAction;
import org.deegree.protocol.wfs.transaction.Transaction;
import org.deegree.protocol.wfs.transaction.TransactionAction;
import org.deegree.protocol.wfs.transaction.action.Delete;
import org.deegree.protocol.wfs.transaction.action.IDGenMode;
import org.deegree.protocol.wfs.transaction.action.Insert;
import org.deegree.protocol.wfs.transaction.action.Native;
import org.deegree.protocol.wfs.transaction.action.ParsedPropertyReplacement;
import org.deegree.protocol.wfs.transaction.action.PropertyReplacement;
import org.deegree.protocol.wfs.transaction.action.Replace;
import org.deegree.protocol.wfs.transaction.action.Update;
import org.deegree.protocol.wfs.transaction.action.UpdateAction;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.i18n.Messages;
import org.jaxen.expr.Expr;
import org.jaxen.expr.LocationPath;
import org.jaxen.expr.NameStep;
import org.jaxen.expr.NumberExpr;
import org.jaxen.expr.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles a single {@link Transaction} request for the {@link WebFeatureService}.
 * 
 * @see WebFeatureService
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class TransactionHandler {

    private static final Logger LOG = LoggerFactory.getLogger( TransactionHandler.class );

    private static final SimpleGeometryFactory GEOM_FACTORY = new SimpleGeometryFactory();

    private final WebFeatureService master;

    private final WfsFeatureStoreManager service;

    private final Transaction request;

    private final Map<FeatureStore, FeatureStoreTransaction> acquiredTransactions = new HashMap<FeatureStore, FeatureStoreTransaction>();

    private final ActionResults inserted = new ActionResults();

    private final ActionResults updated = new ActionResults();

    private final ActionResults replaced = new ActionResults();

    private int deleted;

    private final IDGenMode idGenMode;

    private final ReferenceResolvingMode referenceResolvingMode;

    /**
     * Creates a new {@link TransactionHandler} instance that uses the given service to lookup requested
     * {@link FeatureType}s.
     *  @param master
     *
     * @param service
     *            WFS instance used to lookup the feature types
     * @param request
 *            request to be handled
     * @param idGenMode
     * @param referenceResolvingMode
     */
    TransactionHandler( WebFeatureService master, WfsFeatureStoreManager service, Transaction request,
                        IDGenMode idGenMode, ReferenceResolvingMode referenceResolvingMode ) {
        this.master = master;
        this.service = service;
        this.request = request;
        this.idGenMode = idGenMode;
        this.referenceResolvingMode = referenceResolvingMode;
    }

    /**
     * Performs the given {@link Transaction} request.
     * 
     * @param response
     *            response that is used to write the result
     * @throws OWSException
     *             if a WFS specific exception occurs, e.g. a feature type is not served
     * @throws IOException
     * @throws XMLStreamException
     */
    void doTransaction( HttpResponseBuffer response )
                            throws OWSException, XMLStreamException, IOException {

        LOG.debug( "doTransaction: " + request );

        try {
            Lock lock = null;
            String lockId = request.getLockId();
            LockManager manager = null;
            try {
                // TODO: determine correct feature store
                manager = service.getStores()[0].getLockManager();
            } catch ( FeatureStoreException e ) {
                throw new OWSException( "Cannot acquire lock manager: " + e.getMessage(),
                                        OWSException.NO_APPLICABLE_CODE );
            }
            if ( lockId != null && manager != null ) {
                lock = manager.getLock( lockId );
            }

            for ( TransactionAction operation : request.getActions() ) {
                switch ( operation.getType() ) {
                case DELETE: {
                    doDelete( (Delete) operation, lock );
                    break;
                }
                case INSERT: {
                    doInsert( (Insert) operation );
                    break;
                }
                case NATIVE: {
                    doNative( (Native) operation );
                    break;
                }
                case UPDATE: {
                    doUpdate( (Update) operation, lock );
                    break;
                }
                case REPLACE: {
                    doReplace( (Replace) operation, lock );
                    break;
                }
                }
            }

            // if a lockId has been specified and releaseAction="ALL", release lock
            ReleaseAction releaseAction = request.getReleaseAction();
            if ( lock != null && ( releaseAction == null || releaseAction == ALL || lock.getNumLocked() == 0 ) ) {
                lock.release();
            } else {
                // TODO renew expiry timeout according to WFS spec
            }

            for ( FeatureStoreTransaction ta : acquiredTransactions.values() ) {
                LOG.debug( "Committing feature store transaction:" + ta );
                ta.commit();
            }
        } catch ( MissingParameterException e ) {
            // needed for CITE compliance (wfs:wfs-1.1.0-Transaction-tc12.1)
            LOG.debug( "Error occured during transaction, performing rollback." );
            for ( FeatureStoreTransaction ta : acquiredTransactions.values() ) {
                try {
                    LOG.debug( "Rolling back feature store transaction:" + ta );
                    ta.rollback();
                } catch ( FeatureStoreException e1 ) {
                    LOG.debug( "Error occured during rollback: " + e.getMessage(), e );
                }
            }
            if ( request.getVersion().equals( VERSION_100 ) ) {
                sendResponse100( request, response, true );
                return;
            }
            throw new OWSException( "Error occured during transaction: " + e.getMessage(), MISSING_PARAMETER_VALUE,
                                    e.getName() );
        } catch ( InvalidParameterValueException e ) {
            // needed for CITE compliance (wfs:wfs-1.1.0-LockFeature-tc2.1)
            LOG.debug( "Error occured during transaction, performing rollback." );
            for ( FeatureStoreTransaction ta : acquiredTransactions.values() ) {
                try {
                    LOG.debug( "Rolling back feature store transaction:" + ta );
                    ta.rollback();
                } catch ( FeatureStoreException e1 ) {
                    LOG.debug( "Error occured during rollback: " + e.getMessage(), e );
                }
            }
            if ( request.getVersion().equals( VERSION_100 ) ) {
                sendResponse100( request, response, true );
                return;
            }
            throw new OWSException( "Error occured during transaction: " + e.getMessage(), INVALID_PARAMETER_VALUE,
                                    e.getName() );
        } catch ( OWSException e ) {
            LOG.debug( "Error occured during transaction, performing rollback." );
            for ( FeatureStoreTransaction ta : acquiredTransactions.values() ) {
                try {
                    LOG.debug( "Rolling back feature store transaction:" + ta );
                    ta.rollback();
                } catch ( FeatureStoreException e1 ) {
                    LOG.debug( "Error occured during rollback: " + e.getMessage(), e );
                }
            }
            throw e;
        } catch ( Exception e ) {
            LOG.debug( "Error occured during transaction, performing rollback." );
            LOG.trace( "Stack trace:", e );
            for ( FeatureStoreTransaction ta : acquiredTransactions.values() ) {
                try {
                    LOG.debug( "Rolling back feature store transaction:" + ta );
                    ta.rollback();
                } catch ( FeatureStoreException e1 ) {
                    LOG.debug( "Error occured during rollback: " + e.getMessage(), e );
                }
            }
            throw new OWSException( "Error occured during transaction: " + e.getMessage(), NO_APPLICABLE_CODE );
        }

        if ( VERSION_100.equals( request.getVersion() ) ) {
            sendResponse100( request, response, false );
        } else if ( VERSION_110.equals( request.getVersion() ) ) {
            sendResponse110( response );
        } else if ( VERSION_200.equals( request.getVersion() ) ) {
            sendResponse200( response );
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private void doDelete( Delete delete, Lock lock )
                            throws OWSException {

        LOG.debug( "doDelete: " + delete );
        QName ftName = delete.getTypeName();
        FeatureStore fs = service.getStore( ftName );
        if ( fs == null ) {
            throw new OWSException( Messages.get( "WFS_FEATURE_TYPE_NOT_SERVED", ftName ), INVALID_PARAMETER_VALUE );
        }

        FeatureStoreTransaction ta = acquireTransaction( fs );

        Filter filter = delete.getFilter();
        // superimpose default query CRS
        Filters.setDefaultCRS( filter, master.getDefaultQueryCrs() );

        try {
            switch ( filter.getType() ) {
            case ID_FILTER: {
                deleted += ta.performDelete( (IdFilter) filter, lock );
                break;
            }
            case OPERATOR_FILTER: {
                deleted += ta.performDelete( ftName, (OperatorFilter) filter, lock );
                break;
            }
            }
        } catch ( FeatureStoreException e ) {
            throw new OWSException( Messages.get( "WFS_ERROR_PERFORMING_DELETE", e.getMessage() ), NO_APPLICABLE_CODE );
        }
    }

    private void doInsert( Insert insert )
                            throws OWSException {

        LOG.debug( "doInsert: " + insert );

        if ( service.getStores().length == 0 ) {
            throw new OWSException( "Cannot perform insert. No feature store defined.", NO_APPLICABLE_CODE );
        }

        // TODO deal with this problem
        if ( service.getStores().length > 1 ) {
            String msg = "Cannot perform insert. More than one feature store is active -- "
                         + "this is currently not supported. Please deactivate all feature stores, "
                         + "but one in order to make Insert transactions work.";
            throw new OWSException( msg, NO_APPLICABLE_CODE );
        }

        ICRS defaultCRS = determineDefaultCrs( insert );
        GMLVersion inputFormat = determineFormat( request.getVersion(), insert.getInputFormat() );

        // TODO streaming
        try {
            XMLStreamReader xmlStream = insert.getFeatures();
            FeatureCollection fc = parseFeaturesOrCollection( xmlStream, inputFormat, defaultCRS );
            FeatureStore fs = service.getStores()[0];
            FeatureStoreTransaction ta = acquireTransaction( fs );
            IDGenMode mode = insert.getIdGen();
            if ( mode == null ) {
                if ( VERSION_110.equals( request.getVersion() ) ) {
                    mode = GENERATE_NEW;
                } else {
                    mode = idGenMode;
                }
            }
            List<String> newFids = ta.performInsert( fc, mode );
            for ( String newFid : newFids ) {
                inserted.add( newFid, insert.getHandle() );
            }
        } catch ( OWSException e ) {
            throw e;
        } catch ( XMLParsingException e ) {
            String exceptionCode = INVALID_PARAMETER_VALUE;
            if ( VERSION_200.equals( request.getVersion() ) ) {
                exceptionCode = OWSException.INVALID_VALUE;
            }
            LOG.debug( e.getMessage(), e );
            String msg = "Cannot perform insert operation: " + e.getMessage();
            throw new OWSException( msg, exceptionCode );
        } catch ( Exception e ) {
            LOG.debug( e.getMessage(), e );
            String msg = "Cannot perform insert operation: " + e.getMessage();
            throw new OWSException( msg, INVALID_PARAMETER_VALUE );
        }
    }

    private FeatureCollection parseFeaturesOrCollection( XMLStreamReader xmlStream, GMLVersion inputFormat,
                                                         ICRS defaultCRS )
                                                                                 throws XMLStreamException,
                                                                                 XMLParsingException,
                                                                                 UnknownCRSException,
                                                                                 ReferenceResolvingException {

        FeatureCollection fc = null;

        // TODO determine correct schema
        FeatureStore featureStore = service.getStores()[0];
        AppSchema schema = featureStore.getSchema();
        GMLStreamReader gmlStream = GMLInputFactory.createGMLStreamReader( inputFormat, xmlStream );

        if ( CHECK_INTERNALLY.equals( referenceResolvingMode ) ) {
            gmlStream.setInternalResolver( new FeatureStoreGMLIdResolver( featureStore ) );
        }
        gmlStream.setApplicationSchema( schema );
        gmlStream.setDefaultCRS( defaultCRS );
        gmlStream.setReferencePatternMatcher( master.getReferencePatternMatcher() );

        if ( new QName( WFS_NS, "FeatureCollection" ).equals( xmlStream.getName() ) ) {
            LOG.debug( "Features embedded in wfs:FeatureCollection" );
            fc = parseWFSFeatureCollection( xmlStream, gmlStream );
            // skip to wfs:Insert END_ELEMENT
            xmlStream.nextTag();
        } else {
            // must contain one or more features or a feature collection from the application schema
            Feature feature = gmlStream.readFeature();
            if ( feature instanceof FeatureCollection ) {
                LOG.debug( "Features embedded in application FeatureCollection" );
                fc = (FeatureCollection) feature;
                // skip to wfs:Insert END_ELEMENT
                xmlStream.nextTag();
            } else {
                LOG.debug( "Unenclosed features to be inserted" );
                List<Feature> features = new LinkedList<Feature>();
                features.add( feature );
                while ( xmlStream.nextTag() == START_ELEMENT ) {
                    // more features
                    feature = gmlStream.readFeature();
                    features.add( feature );
                }
                fc = new GenericFeatureCollection( null, features );
            }
        }

        if ( CHECK_ALL.equals( referenceResolvingMode ) || CHECK_INTERNALLY.equals( referenceResolvingMode ) ) {
            // resolve local xlink references
            gmlStream.getIdContext().resolveLocalRefs();
        }

        return fc;
    }

    private FeatureCollection parseWFSFeatureCollection( XMLStreamReader xmlStream, GMLStreamReader gmlStream )
                            throws XMLStreamException, XMLParsingException, UnknownCRSException {

        // TODO handle crs + move this method somewhere else
        xmlStream.require( START_ELEMENT, WFS_NS, "FeatureCollection" );
        List<Feature> memberFeatures = new ArrayList<Feature>();

        while ( xmlStream.nextTag() == START_ELEMENT ) {
            QName elName = xmlStream.getName();
            if ( CommonNamespaces.GMLNS.equals( elName.getNamespaceURI() ) ) {
                if ( "featureMember".equals( elName.getLocalPart() ) ) {
                    // xlink?
                    String href = xmlStream.getAttributeValue( XLNNS, "href" );
                    if ( href != null ) {
                        FeatureReference refFeature = new FeatureReference( gmlStream.getIdContext(), href, null );
                        memberFeatures.add( refFeature );
                        gmlStream.getIdContext().addReference( refFeature );
                    } else {
                        xmlStream.nextTag();
                        memberFeatures.add( gmlStream.readFeature() );
                    }
                    xmlStream.nextTag();
                } else if ( "featureMembers".equals( elName.getLocalPart() ) ) {
                    while ( xmlStream.nextTag() == START_ELEMENT ) {
                        memberFeatures.add( gmlStream.readFeature() );
                    }
                } else {
                    LOG.debug( "Ignoring element '" + elName + "'" );
                    XMLStreamUtils.skipElement( xmlStream );
                }
            } else {
                LOG.debug( "Ignoring element '" + elName + "'" );
                XMLStreamUtils.skipElement( xmlStream );
            }
        }

        // idContext.resolveXLinks( decoder.getApplicationSchema() );
        xmlStream.require( END_ELEMENT, WFS_NS, "FeatureCollection" );
        return new GenericFeatureCollection( null, memberFeatures );
    }

    private void doNative( Native nativeOp )
                            throws OWSException {
        LOG.debug( "doNative: " + nativeOp );
        if ( nativeOp.isSafeToIgnore() == false ) {
            throw new OWSException( "Native operations are not supported by this WFS.", INVALID_PARAMETER_VALUE,
                                    "Native" );
        }

        XMLStreamReader xmlStream = nativeOp.getVendorSpecificData();
        try {
            skipElement( xmlStream );
        } catch ( XMLStreamException e ) {
            String msg = "Error in native operation: " + e.getMessage();
            throw new OWSException( msg, INVALID_PARAMETER_VALUE );
        }
    }

    private void doUpdate( Update update, Lock lock )
                            throws OWSException {
        LOG.debug( "doUpdate: " + update );
        QName ftName = update.getTypeName();
        FeatureType ft = service.lookupFeatureType( ftName );
        FeatureStore fs = service.getStore( ftName );
        if ( fs == null ) {
            throw new OWSException( Messages.get( "WFS_FEATURE_TYPE_NOT_SERVED", ftName ), INVALID_PARAMETER_VALUE );
        }

        GMLVersion inputFormat = determineFormat( request.getVersion(), update.getInputFormat() );

        FeatureStoreTransaction ta = acquireTransaction( fs );
        List<ParsedPropertyReplacement> replacementProps = getReplacementProps( update, ft, inputFormat );
        Filter filter = null;
        try {
            filter = update.getFilter();
            // superimpose default query CRS
            Filters.setDefaultCRS( filter, master.getDefaultQueryCrs() );
        } catch ( Exception e ) {
            throw new OWSException( e.getMessage(), INVALID_PARAMETER_VALUE );
        }

        try {
            List<String> updatedFids = ta.performUpdate( ftName, replacementProps, filter, lock );
            for ( String updatedFid : updatedFids ) {
                this.updated.add( updatedFid, update.getHandle() );
            }
        } catch ( FeatureStoreException e ) {
            throw new OWSException( "Error performing update: " + e.getMessage(), e, NO_APPLICABLE_CODE );
        }
    }

    private Pair<QName, Integer> trySimpleMultiProp( ValueReference valueReference, FeatureType ft )
                            throws OWSException {
        Expr expr = valueReference.getAsXPath();
        if ( !( expr instanceof LocationPath ) ) {
            throw new OWSException( "Cannot update property on feature type '" + ft.getName()
                                    + "'. Complex property paths are not supported.", OPERATION_NOT_SUPPORTED );
        }
        Object obj = ( (LocationPath) expr ).getSteps().get( 0 );
        if ( !( obj instanceof NameStep ) ) {
            throw new OWSException( "Cannot update property on feature type '" + ft.getName()
                                    + "'. Complex property paths are not supported.", OPERATION_NOT_SUPPORTED );
        }
        NameStep namestep = (NameStep) obj;
        obj = namestep.getPredicates().get( 0 );
        if ( !( obj instanceof Predicate ) ) {
            throw new OWSException( "Cannot update property on feature type '" + ft.getName()
                                    + "'. Complex property paths are not supported.", OPERATION_NOT_SUPPORTED );
        }
        Predicate pred = (Predicate) obj;
        expr = pred.getExpr();
        if ( !( expr instanceof NumberExpr ) ) {
            throw new OWSException( "Cannot update property on feature type '" + ft.getName()
                                    + "'. Complex property paths are not supported.", OPERATION_NOT_SUPPORTED );
        }
        NumberExpr ne = (NumberExpr) expr;
        int index = Math.round( Float.parseFloat( ne.getText() ) );
        String namespaceUri = determineNamespaceUri( valueReference, ft, namestep );
        return new Pair<QName, Integer>( new QName( namespaceUri, namestep.getLocalName() ), index - 1 );
    }

    private List<ParsedPropertyReplacement> getReplacementProps( Update update, FeatureType ft, GMLVersion inputFormat )
                            throws OWSException {

        List<ParsedPropertyReplacement> newProperties = new ArrayList<ParsedPropertyReplacement>();
        Iterator<PropertyReplacement> replacementIter = update.getReplacementProps();
        while ( replacementIter.hasNext() ) {
            PropertyReplacement replacement = replacementIter.next();
            QName propName = replacement.getPropertyName().getAsQName();
            Pair<QName, Integer> simpleMultiProp = null;
            if ( propName == null ) {
                simpleMultiProp = trySimpleMultiProp( replacement.getPropertyName(), ft );
                propName = simpleMultiProp.first;
            }

            PropertyType pt = ft.getPropertyDeclaration( propName );
            if ( pt == null ) {
                throw new OWSException( "Cannot update property '" + propName + "' of feature type '" + ft.getName()
                                        + "'. The feature type does not define this property.",
                                        OPERATION_NOT_SUPPORTED );
            }
            XMLStreamReader xmlStream = replacement.getReplacementValue();
            int index = simpleMultiProp == null ? 0 : simpleMultiProp.second;
            UpdateAction updateAction = replacement.getUpdateAction();

            if ( xmlStream != null ) {
                try {
                    xmlStream.require( START_ELEMENT, null, "Value" );
                    GMLStreamReader gmlReader = createGMLStreamReader( inputFormat, xmlStream );
                    gmlReader.setApplicationSchema( ft.getSchema() );
                    GeometryFactory geomFac = new GeometryFactory();
                    geomFac.addInspector( new CoordinateValidityInspector() );
                    gmlReader.setGeometryFactory( geomFac );
                    GMLFeatureReader featureReader = gmlReader.getFeatureReader();

                    ICRS crs = master.getDefaultQueryCrs();
                    Property prop = featureReader.parseProperty( new XMLStreamReaderWrapper( xmlStream, null ), pt,
                                                                 crs );

                    // TODO make this hack unnecessary
                    TypedObjectNode propValue = prop.getValue();
                    if ( pt instanceof CustomPropertyType && propValue instanceof GenericXMLElement ) {
                        prop = new GenericProperty( pt, propValue );
                        prop.setChildren( ( (GenericXMLElement) propValue ).getChildren() );
                    }

                    ParsedPropertyReplacement repl = new ParsedPropertyReplacement( prop, updateAction,
                                                                                    replacement.getPropertyName(),
                                                                                    index );
                    newProperties.add( repl );

                    // contract: skip to "wfs:Property" END_ELEMENT
                    xmlStream.nextTag();
                    xmlStream.require( END_ELEMENT, null, "Property" );
                    // contract: skip to next ELEMENT_EVENT
                    xmlStream.nextTag();
                } catch ( XMLParsingException e ) {
                    LOG.debug( e.getMessage(), e );
                    throw new OWSException( e.getMessage(), INVALID_VALUE );
                } catch ( Exception e ) {
                    LOG.debug( e.getMessage(), e );
                    throw new OWSException( e.getMessage(), NO_APPLICABLE_CODE );
                }

            } else {
                // if the wfs:Value element is omitted, the property shall be removed (CITE 1.1.0 test,
                // wfs:wfs-1.1.0-Transaction-tc11.1)
                GenericProperty newProp = new GenericProperty( pt, null );
                ParsedPropertyReplacement repl = new ParsedPropertyReplacement( newProp, updateAction,
                                                                                replacement.getPropertyName(), index );
                newProperties.add( repl );
            }
        }
        return newProperties;
    }

    private void doReplace( Replace replace, Lock lock )
                            throws OWSException {

        LOG.debug( "doReplace: " + replace );
        XMLStreamReader xmlStream = replace.getReplacementFeatureStream();
        QName ftName = xmlStream.getName();
        FeatureStore fs = service.getStore( ftName );
        if ( fs == null ) {
            throw new OWSException( Messages.get( "WFS_FEATURE_TYPE_NOT_SERVED", ftName ), INVALID_PARAMETER_VALUE );
        }

        Feature replacementFeature = null;
        Filter filter = null;
        try {
            GMLStreamReader gmlReader = createGMLStreamReader( GML_32, xmlStream );
            gmlReader.setApplicationSchema( fs.getSchema() );
            replacementFeature = gmlReader.readFeature();
            filter = replace.getFilter();
            // superimpose default CRS
            Filters.setDefaultCRS( filter, master.getDefaultQueryCrs() );
        } catch ( Exception e ) {
            throw new OWSException( e.getMessage(), INVALID_PARAMETER_VALUE );
        }

        FeatureStoreTransaction ta = acquireTransaction( fs );
        try {
            String newFid = ta.performReplace( replacementFeature, filter, lock, idGenMode );
            replaced.add( newFid, replace.getHandle() );
        } catch ( FeatureStoreException e ) {
            throw new OWSException( "Error performing replace: " + e.getMessage(), e, NO_APPLICABLE_CODE );
        }
    }

    private FeatureStoreTransaction acquireTransaction( FeatureStore fs )
                            throws OWSException {

        FeatureStoreTransaction ta = acquiredTransactions.get( fs );
        if ( ta == null ) {
            try {
                LOG.debug( "Acquiring transaction for feature store " + fs );
                ta = fs.acquireTransaction();
                acquiredTransactions.put( fs, ta );
            } catch ( FeatureStoreException e ) {
                throw new OWSException( Messages.get( "WFS_CANNOT_ACQUIRE_TA", e.getMessage() ), NO_APPLICABLE_CODE );
            }
        }
        return ta;
    }

    private void sendResponse100( Transaction request, HttpResponseBuffer response, boolean failed )
                            throws XMLStreamException, IOException {

        String schemaLocation = WFS_NS + " " + WFS_100_TRANSACTION_URL;
        XMLStreamWriter xmlWriter = getXMLResponseWriter( response, "text/xml", schemaLocation );
        xmlWriter.setPrefix( "wfs", WFS_NS );
        xmlWriter.writeStartElement( WFS_NS, "WFS_TransactionResponse" );
        xmlWriter.writeNamespace( "wfs", WFS_NS );
        xmlWriter.writeNamespace( "ogc", OGCNS );
        xmlWriter.writeAttribute( "version", VERSION_100.toString() );

        if ( inserted.getTotal() > 0 ) {
            for ( String handle : inserted.getHandles() ) {
                xmlWriter.writeStartElement( "wfs", "InsertResult", WFS_NS );
                writeHandle( xmlWriter, handle );
                Collection<String> fids = inserted.getFids( handle );
                for ( String fid : fids ) {
                    LOG.debug( "Inserted fid: " + fid );
                    xmlWriter.writeStartElement( "ogc", "FeatureId", OGCNS );
                    xmlWriter.writeAttribute( "fid", fid );
                    xmlWriter.writeEndElement();
                }
                xmlWriter.writeEndElement();
            }
            if ( !inserted.getFidsWithoutHandle().isEmpty() ) {
                xmlWriter.writeStartElement( "wfs", "InsertResult", WFS_NS );
                for ( String fid : inserted.getFidsWithoutHandle() ) {
                    LOG.debug( "Inserted fid: " + fid );
                    xmlWriter.writeStartElement( "ogc", "FeatureId", OGCNS );
                    xmlWriter.writeAttribute( "fid", fid );
                    xmlWriter.writeEndElement();
                }
                xmlWriter.writeEndElement();
            }
        }

        xmlWriter.writeStartElement( "wfs", "TransactionResult", WFS_NS );
        writeHandle( xmlWriter, request.getHandle() );
        xmlWriter.writeStartElement( "wfs", "Status", WFS_NS );
        if ( failed ) {
            xmlWriter.writeEmptyElement( "wfs", "FAILED", WFS_NS );
        } else {
            xmlWriter.writeEmptyElement( "wfs", "SUCCESS", WFS_NS );
        }

        xmlWriter.writeEndElement(); // wfs:Status
        xmlWriter.writeEndElement(); // wfs:TransactionResult
        xmlWriter.writeEndElement(); // wfs:WFS_TransactionResult
        xmlWriter.flush();
    }

    private void writeHandle( XMLStreamWriter xmlWriter, String handle )
                            throws XMLStreamException {
        if ( handle != null ) {
            xmlWriter.writeAttribute( "handle", handle );
        }
    }

    private void sendResponse110( HttpResponseBuffer response )
                            throws XMLStreamException, IOException {

        String ns = WFS_NS;
        String schemaLocation = ns + " " + WFS_110_SCHEMA_URL;

        XMLStreamWriter xmlWriter = getXMLResponseWriter( response, "text/xml", schemaLocation );

        xmlWriter.setPrefix( "wfs", WFS_NS );
        xmlWriter.writeStartElement( WFS_NS, "TransactionResponse" );
        xmlWriter.writeNamespace( "wfs", WFS_NS );
        xmlWriter.writeNamespace( "ogc", OGCNS );

        xmlWriter.writeAttribute( "version", VERSION_110.toString() );

        xmlWriter.writeStartElement( WFS_NS, "TransactionSummary" );
        writeElement( xmlWriter, WFS_NS, "totalInserted", "" + inserted.getTotal() );
        writeElement( xmlWriter, WFS_NS, "totalUpdated", "" + updated.getTotal() );
        writeElement( xmlWriter, WFS_NS, "totalDeleted", "" + deleted );
        xmlWriter.writeEndElement();
        if ( inserted.getTotal() > 0 ) {
            xmlWriter.writeStartElement( WFS_NS, "InsertResults" );
            for ( String handle : inserted.getHandles() ) {
                Collection<String> fids = inserted.getFids( handle );
                for ( String fid : fids ) {
                    LOG.debug( "Inserted fid: " + fid );
                    xmlWriter.writeStartElement( WFS_NS, "Feature" );
                    xmlWriter.writeAttribute( "handle", handle );
                    xmlWriter.writeStartElement( OGCNS, "FeatureId" );
                    xmlWriter.writeAttribute( "fid", fid );
                    xmlWriter.writeEndElement();
                    xmlWriter.writeEndElement();
                }
            }
            for ( String fid : inserted.getFidsWithoutHandle() ) {
                LOG.debug( "Inserted fid: " + fid );
                xmlWriter.writeStartElement( WFS_NS, "Feature" );
                xmlWriter.writeStartElement( OGCNS, "FeatureId" );
                xmlWriter.writeAttribute( "fid", fid );
                xmlWriter.writeEndElement();
                xmlWriter.writeEndElement();
            }
            xmlWriter.writeEndElement();
        }

        xmlWriter.writeEndElement();
        xmlWriter.flush();
    }

    private void sendResponse200( HttpResponseBuffer response )
                            throws XMLStreamException, IOException {

        String ns = WFS_200_NS;
        String schemaLocation = ns + " " + WFS_200_SCHEMA_URL;
        XMLStreamWriter xmlWriter = getXMLResponseWriter( response, "text/xml", schemaLocation );

        xmlWriter.setPrefix( "wfs", WFS_200_NS );
        xmlWriter.writeStartElement( WFS_200_NS, "TransactionResponse" );
        xmlWriter.writeAttribute( "version", VERSION_200.toString() );
        xmlWriter.writeNamespace( "wfs", WFS_200_NS );
        xmlWriter.writeNamespace( "fes", FES_20_NS );

        xmlWriter.writeStartElement( WFS_200_NS, "TransactionSummary" );
        writeElement( xmlWriter, WFS_200_NS, "totalInserted", "" + inserted.getTotal() );
        writeElement( xmlWriter, WFS_200_NS, "totalUpdated", "" + updated.getTotal() );
        writeElement( xmlWriter, WFS_200_NS, "totalReplaced", "" + replaced.getTotal() );
        writeElement( xmlWriter, WFS_200_NS, "totalDeleted", "" + deleted );
        xmlWriter.writeEndElement();

        writeActionResults200( xmlWriter, "InsertResults", inserted );
        writeActionResults200( xmlWriter, "UpdateResults", updated );
        writeActionResults200( xmlWriter, "ReplaceResults", replaced );

        xmlWriter.writeEndElement();
        xmlWriter.flush();
    }

    private void writeActionResults200( XMLStreamWriter xmlWriter, String elName, ActionResults results )
                            throws XMLStreamException {

        if ( results.getTotal() > 0 ) {
            xmlWriter.writeStartElement( WFS_200_NS, elName );
            for ( String handle : results.getHandles() ) {
                Collection<String> fids = results.getFids( handle );
                for ( String fid : fids ) {
                    xmlWriter.writeStartElement( WFS_200_NS, "Feature" );
                    xmlWriter.writeAttribute( "handle", handle );
                    xmlWriter.writeStartElement( FES_20_NS, "ResourceId" );
                    xmlWriter.writeAttribute( "rid", fid );
                    xmlWriter.writeEndElement();
                    xmlWriter.writeEndElement();
                }
            }

            for ( String fid : results.getFidsWithoutHandle() ) {
                xmlWriter.writeStartElement( WFS_200_NS, "Feature" );
                xmlWriter.writeStartElement( FES_20_NS, "ResourceId" );
                xmlWriter.writeAttribute( "rid", fid );
                xmlWriter.writeEndElement();
                xmlWriter.writeEndElement();
            }
            xmlWriter.writeEndElement();
        }
    }

    private GMLVersion determineFormat( Version requestVersion, String format ) {

        GMLVersion gmlVersion = null;

        if ( format == null ) {
            // default values for the different WFS version
            if ( VERSION_100.equals( requestVersion ) ) {
                gmlVersion = GMLVersion.GML_2;
            } else if ( VERSION_110.equals( requestVersion ) ) {
                gmlVersion = GMLVersion.GML_31;
            } else if ( VERSION_200.equals( requestVersion ) ) {
                gmlVersion = GMLVersion.GML_32;
            } else {
                throw new RuntimeException( "Internal error: Unhandled WFS version: " + requestVersion );
            }
        } else {
            if ( "text/xml; subtype=gml/2.1.2".equals( format ) || "GML2".equals( format ) ) {
                gmlVersion = GMLVersion.GML_2;
            } else if ( "text/xml; subtype=gml/3.0.1".equals( format ) ) {
                gmlVersion = GMLVersion.GML_30;
            } else if ( "text/xml; subtype=gml/3.1.1".equals( format ) || "GML3".equals( format ) ) {
                gmlVersion = GMLVersion.GML_31;
            } else if ( "text/xml; subtype=gml/3.2.1".equals( format ) ) {
                gmlVersion = GMLVersion.GML_32;
            } else if ( "text/xml; subtype=gml/3.2.2".equals( format ) ) {
                gmlVersion = GMLVersion.GML_32;
            }
        }
        return gmlVersion;
    }

    private String determineNamespaceUri( ValueReference valueReference, FeatureType ft, NameStep namestep ) {
        String prefix = namestep.getPrefix();
        if ( prefix != null && !"".equals( prefix ) ) {
            String namespaceUriByPrefix = valueReference.getNsContext().getNamespaceURI( prefix );
            if ( namespaceUriByPrefix != null && !"".equals( namespaceUriByPrefix ) )
                return namespaceUriByPrefix;
        }
        return ft.getName().getNamespaceURI();
    }

    private ICRS determineDefaultCrs( Insert insert )
                    throws OWSException {
        String srsName = insert.getSrsName();
        if ( srsName != null ) {
            try {
                return CRSManager.lookup( insert.getSrsName() );
            } catch ( UnknownCRSException e ) {
                String msg = "Cannot perform insert. Specified srsName '" + srsName + "' is not supported by this WFS.";
                throw new OWSException( msg, INVALID_PARAMETER_VALUE, "srsName" );
            }
        }
        return null;
    }

}
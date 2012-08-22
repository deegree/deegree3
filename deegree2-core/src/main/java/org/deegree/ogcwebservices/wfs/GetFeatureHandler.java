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
package org.deegree.ogcwebservices.wfs;

import static org.deegree.framework.util.TimeTools.getISOFormattedTime;
import static org.deegree.ogcbase.ExceptionCode.INVALIDPARAMETERVALUE;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.concurrent.ExecutionFinishedEvent;
import org.deegree.framework.concurrent.Executor;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.Datastore;
import org.deegree.io.datastore.PropertyPathResolvingException;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.GMLFeatureAdapter;
import org.deegree.model.feature.GMLFeatureCollectionDocument;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.capabilities.WFSFeatureType;
import org.deegree.ogcwebservices.wfs.capabilities.WFSOperationsMetadata;
import org.deegree.ogcwebservices.wfs.configuration.WFSConfiguration;
import org.deegree.ogcwebservices.wfs.operation.FeatureResult;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wfs.operation.Query;
import org.deegree.ogcwebservices.wfs.operation.GetFeature.RESULT_TYPE;
import org.deegree.owscommon.OWSDomainType;

/**
 * Handles {@link GetFeature} requests to the {@link WFService}. Since a {@link GetFeature} request may contain more
 * than one {@link Query}, each {@link Query} is delegated to an own thread.
 * <p>
 * The results of all threads are collected and merged before they are returned to the calling {@link WFService} as a
 * single {@link FeatureCollection}.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
class GetFeatureHandler {

    private static final ILogger LOG = LoggerFactory.getLogger( GetFeatureHandler.class );

    // upper limit for timeout (overrides WFS configuration)
    private static long MAX_TIMEOUT_MILLIS = 60 * 60 * 1000;

    private WFService wfs;

    private int maxFeatures = -1;

    /**
     * Creates a new instance of <code>GetFeatureHandler</code>. Only called by the associated {@link WFService} (once).
     *
     * @param wfs
     *            associated WFService
     */
    GetFeatureHandler( WFService wfs ) {
        this.wfs = wfs;
        WFSCapabilities capa = wfs.getCapabilities();
        WFSOperationsMetadata md = (WFSOperationsMetadata) capa.getOperationsMetadata();
        OWSDomainType[] dt = md.getConstraints();
        for ( int i = 0; i < dt.length; i++ ) {
            if ( dt[i].getName().equals( "DefaultMaxFeatures" ) ) {
                try {
                    String tmp = dt[i].getValues()[0];
                    this.maxFeatures = Integer.parseInt( tmp );
                } catch ( Exception e ) {
                    // e.printStackTrace();
                }
                break;
            }
        }
        LOG.logDebug( "default maxFeatures " + this.maxFeatures );
    }

    /**
     * Handles a {@link GetFeature} request by delegating the contained {@link Query} objects to different threads.
     * <p>
     * If at least one query fails an exception will be thrown and all running threads will be stopped.
     *
     * @param getFeature
     * @return result of the request
     * @throws OGCWebServiceException
     */
    FeatureResult handleRequest( GetFeature getFeature )
                            throws OGCWebServiceException {

        WFSConfiguration conf = (WFSConfiguration) wfs.getCapabilities();

        if ( getFeature.getMaxFeatures() > this.maxFeatures || getFeature.getMaxFeatures() <= 0 ) {
            getFeature.setMaxFeatures( this.maxFeatures );
        }

        LOG.logDebug( "maxFeatures " + getFeature.getMaxFeatures() );

        Query[] queries = getFeature.getQuery();
        List<Callable<FeatureCollection>> queryTasks = new ArrayList<Callable<FeatureCollection>>( queries.length );

        for ( Query query : queries ) {

            if ( conf.getDeegreeParams().checkUTMZones() ) {
                query.performBBoxTest();
            }

            query.deleteBBoxTest();

            QualifiedName[] ftNames = query.getTypeNames();
            MappedFeatureType[] requestedFts = new MappedFeatureType[ftNames.length];
            Datastore ds = null;

            for ( int i = 0; i < ftNames.length; i++ ) {
                QualifiedName ftName = ftNames[i];
                MappedFeatureType ft = this.wfs.getMappedFeatureType( ftName );

                if ( ft == null ) {
                    String msg = Messages.getMessage( "WFS_FEATURE_TYPE_UNKNOWN", ftName );
                    throw new OGCWebServiceException( this.getClass().getName(), msg );
                }
                if ( ft.isAbstract() ) {
                    String msg = Messages.getMessage( "WFS_FEATURE_TYPE_ABSTRACT", ftName );
                    throw new OGCWebServiceException( this.getClass().getName(), msg );
                }
                if ( !ft.isVisible() ) {
                    String msg = Messages.getMessage( "WFS_FEATURE_TYPE_INVISIBLE", ftName );
                    throw new OGCWebServiceException( this.getClass().getName(), msg );
                }
                Datastore dsForFt = ft.getGMLSchema().getDatastore();
                if ( ds != null ) {
                    if ( ds != dsForFt ) {
                        String msg = Messages.getMessage( "WFS_QUERY_JOIN_OVER_DIFFERENT_DS" );
                        throw new OGCWebServiceException( this.getClass().getName(), msg );
                    }
                } else {
                    ds = dsForFt;
                }
                requestedFts[i] = ft;
            }

            // TODO what about joins here?
            String srsName = query.getSrsName();
            if ( srsName != null ) {
                WFSFeatureType wfsFT = this.wfs.getCapabilities().getFeatureTypeList().getFeatureType( ftNames[0] );

                if ( !( wfsFT.supportsSrs( srsName ) ) ) {
                    String msg = Messages.getMessage( "WFS_FEATURE_TYPE_SRS_UNSUPPORTED", ftNames[0], srsName );
                    throw new OGCWebServiceException( this.getClass().getName(), msg );
                }
            }

            QueryTask task = new QueryTask( ds, query, requestedFts );
            queryTasks.add( task );
        }

        long timeout = conf.getDeegreeParams().getRequestTimeLimit() * 1000;
        if ( timeout > MAX_TIMEOUT_MILLIS ) {
            // limit max timeout
            timeout = MAX_TIMEOUT_MILLIS;
        }

        List<ExecutionFinishedEvent<FeatureCollection>> finishedEvents = null;
        try {
            finishedEvents = Executor.getInstance().performSynchronously( queryTasks, timeout );
        } catch ( InterruptedException e ) {
            String msg = "Exception occured while waiting for the GetFeature results: " + e.getMessage();
            throw new OGCWebServiceException( this.getClass().getName(), msg );
        }

        // use id of the request as id of the result feature collection
        // to allow identification of the original request that produced
        // the feature collection
        FeatureCollection fc = null;
        if ( getFeature.getResultType() == RESULT_TYPE.RESULTS ) {
            fc = mergeResults( getFeature.getId(), finishedEvents );
        } else {
            fc = mergeHits( getFeature.getId(), finishedEvents );
        }

        // TODO this is not a good solution
        // I think it can happen if more than one feature type is requested
        while ( getFeature.getMaxFeatures() > 0 && fc.size() > getFeature.getMaxFeatures() ) {
            fc.remove( fc.size() - 1 );
        }

        if ( LOG.isDebug() ) {
            try {
                GMLFeatureAdapter ada = new GMLFeatureAdapter( false );
                GMLFeatureCollectionDocument doc = ada.export( fc );
                LOG.logDebugXMLFile( "GetFeatureHandler_result", doc );
            } catch ( Exception e ) {
                LOG.logError( e.getMessage(), e );
            }
        }

        FeatureResult fr = new FeatureResult( getFeature, fc );
        return fr;
    }

    /**
     * Merges the results of the request subparts into one feature collection.
     *
     * @param fcid
     *            id of the new (result) feature collection
     * @param finishedEvents
     * @return feature collection containing all features from all responses
     * @throws OGCWebServiceException
     */
    private FeatureCollection mergeResults( String fcid, List<ExecutionFinishedEvent<FeatureCollection>> finishedEvents )
                            throws OGCWebServiceException {

        FeatureCollection result = null;

        try {
            for ( ExecutionFinishedEvent<FeatureCollection> event : finishedEvents ) {
                if ( result == null ) {
                    result = event.getResult();
                } else {
                    result.addAllUncontained( event.getResult() );
                }
            }

            if ( result == null ) {
                return result;
            }
        } catch ( CancellationException e ) {
            LOG.logError( e.getMessage(), e );
            String msg = Messages.getMessage( "WFS_GET_FEATURE_TIMEOUT", e.getMessage() );
            throw new OGCWebServiceException( this.getClass().getName(), msg );
        } catch ( PropertyPathResolvingException e ) {
            LOG.logDebug( "Stack trace", e );
            throw new OGCWebServiceException( e.getLocalizedMessage(), INVALIDPARAMETERVALUE );
        } catch ( Throwable t ) {
            LOG.logError( t.getMessage(), t );
            String msg = Messages.getMessage( "WFS_GET_FEATURE_BACKEND", t.getMessage() );
            throw new OGCWebServiceException( this.getClass().getName(), msg );
        }

        result.setId( fcid );
        result.setAttribute( "numberOfFeatures", "" + result.size() );
        return result;
    }

    /**
     * Merges the results of the request subparts into one feature collection.
     * <p>
     * This method is used if only the HITS have been requested, i.e. the number of features.
     *
     * TODO: Do this a better way (maybe change feature model).
     *
     * @param fcid
     *            id of the new (result) feature collection
     * @param finishedEvents
     * @return empty feature collection with "numberOfFeatures" attribute
     * @throws OGCWebServiceException
     */
    private FeatureCollection mergeHits( String fcid, List<ExecutionFinishedEvent<FeatureCollection>> finishedEvents )
                            throws OGCWebServiceException {

        FeatureCollection result = null;
        int numberOfFeatures = 0;

        try {
            for ( ExecutionFinishedEvent<FeatureCollection> event : finishedEvents ) {
                FeatureCollection fc = event.getResult();
                try {
                    numberOfFeatures += Integer.parseInt( ( fc.getAttribute( "numberOfFeatures" ) ) );
                } catch ( NumberFormatException e ) {
                    String msg = "Internal error. Could not parse 'numberOfFeatures' attribute "
                                 + "of sub-result as an integer value.";
                    throw new OGCWebServiceException( this.getClass().getName(), msg );
                }
                if ( result == null ) {
                    result = fc;
                } else {
                    result.addAllUncontained( fc );
                }
            }

            if ( result == null ) {
                return result;
            }
        } catch ( CancellationException e ) {
            String msg = Messages.getMessage( "WFS_GET_FEATURE_TIMEOUT" );
            LOG.logError( msg, e );
            throw new OGCWebServiceException( this.getClass().getName(), msg );
        } catch ( Throwable t ) {
            String msg = Messages.getMessage( "WFS_GET_FEATURE_BACKEND", t.getMessage() );
            LOG.logError( msg, t );
            throw new OGCWebServiceException( this.getClass().getName(), msg );
        }

        result.setId( fcid );
        result.setAttribute( "numberOfFeatures", "" + numberOfFeatures );
        result.setAttribute( "timeStamp", getISOFormattedTime() );
        return result;
    }

    // ///////////////////////////////////////////////////////////////////////////
    // inner classes
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Inner class for performing queries on a datastore.
     */
    private class QueryTask implements Callable<FeatureCollection> {

        private Datastore ds;

        private Query query;

        private MappedFeatureType[] fts;

        QueryTask( Datastore ds, Query query, MappedFeatureType[] fts ) {
            this.ds = ds;
            this.query = query;
            this.fts = fts;
        }

        /**
         * Performs the associated {@link Query} and returns the result.
         *
         * @return resulting feature collection
         * @throws Exception
         */
        public FeatureCollection call()
                                throws Exception {
            FeatureCollection result = this.ds.performQuery( query, fts );
            return result;
        }
    }
}

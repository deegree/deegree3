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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.Datastore;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.FeatureId;
import org.deegree.io.datastore.LockManager;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wfs.operation.FeatureResult;
import org.deegree.ogcwebservices.wfs.operation.GetFeature;
import org.deegree.ogcwebservices.wfs.operation.GetFeatureWithLock;
import org.deegree.ogcwebservices.wfs.operation.Lock;
import org.deegree.ogcwebservices.wfs.operation.LockFeature;
import org.deegree.ogcwebservices.wfs.operation.LockFeatureResponse;
import org.deegree.ogcwebservices.wfs.operation.Query;

/**
 * Handles {@link LockFeature} requests to the {@link WFService}.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
class LockFeatureHandler {

    private static final ILogger LOG = LoggerFactory.getLogger( LockFeatureHandler.class );

    // shared instance to handle GetFeature requests (for GetFeatureWithLock)
    private GetFeatureHandler getFeatureHandler;

    private WFService wfs;

    /**
     * Creates a new instance of <code>LockFeatureHandler</code>. Only called by the associated {@link WFService}
     * (once).
     *
     * @param wfs
     *            associated WFService
     */
    LockFeatureHandler( WFService wfs ) {
        this.wfs = wfs;
        this.getFeatureHandler = new GetFeatureHandler( wfs );
    }

    /**
     * Handles a {@link LockFeature} request.
     *
     * @param request
     *            <code>LockFeature</code> request to perform
     * @return response to the request
     * @throws OGCWebServiceException
     */
    LockFeatureResponse handleRequest( LockFeature request )
                            throws OGCWebServiceException {

        LockFeatureResponse response = null;

        try {
            List<FeatureId> fidsToLock = new ArrayList<FeatureId>();
            Map<Datastore, List<Lock>> dsToLocksMap = buildDsToLocksMap( request );
            for ( Datastore ds : dsToLocksMap.keySet() ) {
                fidsToLock.addAll( ds.determineFidsToLock( dsToLocksMap.get( ds ) ) );
            }
            org.deegree.io.datastore.Lock lock = LockManager.getInstance().acquireLock( request, fidsToLock );
            Set<String> lockedFeatures = lock.getLockedFids();
            String[] lockedFids = new String[lockedFeatures.size()];
            String[] notLockedFids = new String[fidsToLock.size() - lockedFeatures.size()];
            int lockedIdx = 0, notLockedIdx = 0;
            for ( FeatureId fid : fidsToLock ) {
                String fidAsString = fid.getAsString();
                if ( lockedFeatures.contains( fidAsString ) ) {
                    lockedFids[lockedIdx++] = fidAsString;
                } else {
                    notLockedFids[notLockedIdx++] = fidAsString;
                }
            }
            response = new LockFeatureResponse( request, lock.getId(), lockedFids, notLockedFids );
        } catch ( DatastoreException e ) {
            LOG.logDebug( e.getMessage(), e );
            throw new OGCWebServiceException( this.getClass().getName(), e.getMessage() );
        }

        return response;
    }

    /**
     * Handles a {@link GetFeatureWithLock} request.
     * <p>
     * This is performed using the following strategy:
     * <ul>
     * <li>Perform the request as a standard {@link GetFeature} request.</li>
     * <li>Create a corresponding {@link LockFeature} request from the {@link GetFeatureWithLock} request.</li>
     * <li>Set the "lockId" attribute in the result feature collection.</li>
     * <li>Remove all features from the feature collection that could not be locked.</li>
     * </ul>
     *
     * @param request
     *            <code>GetFeatureWithLock</code> request to perform
     * @return response to the request
     * @throws OGCWebServiceException
     */
    FeatureResult handleRequest( GetFeatureWithLock request )
                            throws OGCWebServiceException {

        FeatureResult response = this.getFeatureHandler.handleRequest( request );

        List<Lock> locks = new ArrayList<Lock>( request.getQuery().length );
        for ( Query query : request.getQuery() ) {
            Lock lock = new Lock( null, query.getTypeNames()[0], query.getFilter() );
            locks.add( lock );
        }
        LockFeature lockRequest = LockFeature.create( WFService.VERSION, null, null, request.getExpiry(),
                                                      request.getLockAction(), locks );
        LockFeatureResponse lockResponse = handleRequest( lockRequest );

        // set "lockId" parameter in result feature collection
        FeatureCollection fc = (FeatureCollection) response.getResponse();
        fc.setAttribute( "lockId", lockResponse.getLockId() );

        // remove all features from the result feature collection that could not be locked (and
        // count removed features)
        int removed = 0;
        for ( String notLockedFid : lockResponse.getFeaturesNotLocked() ) {
            Feature feature = fc.getFeature( notLockedFid );
            if ( feature != null ) {
                fc.remove( feature );
                removed++;
            }
        }

        // correct "numberOfFeatures" attribute (and make it work even for resultType=HITS)
        int before = Integer.parseInt( fc.getAttribute( "numberOfFeatures" ) );
        fc.setAttribute( "numberOfFeatures", "" + ( before - removed ) );

        return response;
    }

    /**
     * Groups all {@link Lock}s contained in the given {@link LockFeature} request by the responsible {@link Datastore},
     * i.e. that serves the {@link FeatureType} to be locked.
     *
     * @param request
     * @return keys: responsible <code>Datastore</code>, values: List of <code>Lock</code>s
     * @throws OGCWebServiceException
     */
    private Map<Datastore, List<Lock>> buildDsToLocksMap( LockFeature request )
                            throws OGCWebServiceException {

        Map<Datastore, List<Lock>> dsToLocksMap = new HashMap<Datastore, List<Lock>>();
        List<Lock> locks = request.getLocks();

        for ( Lock lock : locks ) {
            QualifiedName ftName = lock.getTypeName();
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

            Datastore ds = ft.getGMLSchema().getDatastore();
            List<Lock> dsLocks = dsToLocksMap.get( ds );
            if ( dsLocks == null ) {
                dsLocks = new ArrayList<Lock>();
                dsToLocksMap.put( ds, dsLocks );
            }
            dsLocks.add( lock );
        }
        return dsToLocksMap;
    }
}

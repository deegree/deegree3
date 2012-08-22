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
package org.deegree.ogcwebservices.wfs.operation;

import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.KVP2Map;
import org.deegree.i18n.Messages;
import org.deegree.model.filterencoding.Filter;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.SortProperty;
import org.deegree.ogcwebservices.InconsistentRequestException;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.MissingParameterValueException;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.wfs.operation.LockFeature.ALL_SOME_TYPE;
import org.w3c.dom.Element;

/**
 * Represents a <code>GetFeatureWithLock</code> request to a web feature service.
 * <p>
 * This is identical to a {@link GetFeature} request, except that the features matching the request will also be locked.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$
 */
public class GetFeatureWithLock extends GetFeature {

    private static final ILogger LOG = LoggerFactory.getLogger( GetFeatureWithLock.class );

    private static final long serialVersionUID = 8885456550385437651L;

    /** Duration until timeout (in milliseconds). */
    private long expiry;

    private ALL_SOME_TYPE lockAction;

    /**
     * Creates a new <code>GetFeatureWithLock</code> instance.
     *
     * @param version
     *            request version
     * @param id
     *            id of the request
     * @param handle
     * @param resultType
     *            desired result type (results | hits)
     * @param outputFormat
     *            requested result format
     * @param maxFeatures
     * @param startPosition
     *            deegree specific parameter defining where to start considering features
     * @param traverseXLinkDepth
     * @param traverseXLinkExpiry
     * @param queries
     * @param vendorSpecificParam
     * @param expiry
     *            the limit on how long the web feature service keeps the lock (in milliseconds)
     * @param lockAction
     *            method for lock acquisition
     */
    GetFeatureWithLock( String version, String id, String handle, RESULT_TYPE resultType, String outputFormat,
                        int maxFeatures, int startPosition, int traverseXLinkDepth, int traverseXLinkExpiry,
                        Query[] queries, Map<String, String> vendorSpecificParam, long expiry, ALL_SOME_TYPE lockAction ) {
        super( version, id, handle, resultType, outputFormat, maxFeatures, startPosition, traverseXLinkDepth,
               traverseXLinkExpiry, queries, vendorSpecificParam );
        this.expiry = expiry;
        this.lockAction = lockAction;
    }

    /**
     * Creates a new <code>GetFeatureWithLock</code> instance from the given parameters.
     *
     * @param version
     *            request version
     * @param id
     *            id of the request
     * @param handle
     * @param resultType
     *            desired result type (results | hits)
     * @param outputFormat
     *            requested result format
     * @param maxFeatures
     * @param startPosition
     *            deegree specific parameter defining where to start considering features
     * @param traverseXLinkDepth
     * @param traverseXLinkExpiry
     * @param queries
     * @param vendorSpecificParam
     * @param expiry
     *            the limit on how long the web feature service keeps the lock (in milliseconds)
     * @param lockAction
     *            method for lock acquisition
     * @return new <code>GetFeatureWithLock</code> request
     */
    public static GetFeatureWithLock create( String version, String id, String handle, RESULT_TYPE resultType,
                                             String outputFormat, int maxFeatures, int startPosition,
                                             int traverseXLinkDepth, int traverseXLinkExpiry, Query[] queries,
                                             Map<String, String> vendorSpecificParam, long expiry,
                                             ALL_SOME_TYPE lockAction ) {
        return new GetFeatureWithLock( version, id, handle, resultType, outputFormat, maxFeatures, startPosition,
                                       traverseXLinkDepth, traverseXLinkExpiry, queries, vendorSpecificParam, expiry,
                                       lockAction );
    }

    /**
     * Creates a new <code>GetFeatureWithLock</code> instance from a document that contains the DOM representation of
     * the request.
     *
     * @param id
     *            of the request
     * @param root
     *            element that contains the DOM representation of the request
     * @return new <code>GetFeatureWithLock</code> request
     * @throws OGCWebServiceException
     */
    public static GetFeatureWithLock create( String id, Element root )
                            throws OGCWebServiceException {
        GetFeatureWithLockDocument doc = new GetFeatureWithLockDocument();
        doc.setRootElement( root );
        GetFeatureWithLock request;
        try {
            request = doc.parse( id );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( "GetFeatureWithLock", e.getMessage() );
        }
        return request;
    }

    /**
     * Creates a new <code>GetFeatureWithLock</code> instance from the given key-value pair encoded request.
     *
     * @param id
     *            request identifier
     * @param request
     * @return new <code>GetFeatureWithLock</code> request
     * @throws InvalidParameterValueException
     * @throws InconsistentRequestException
     * @throws MissingParameterValueException
     */
    public static GetFeatureWithLock create( String id, String request )
                            throws InconsistentRequestException, InvalidParameterValueException,
                            MissingParameterValueException {
        Map<String, String> map = KVP2Map.toMap( request );
        map.put( "ID", id );
        return create( map );
    }

    /**
     * Creates a new <code>GetFeatureWithLock</code> request from the given map.
     *
     * @param kvp
     *            key-value pairs, keys have to be uppercase
     * @return new <code>GetFeatureWithLock</code> request
     * @throws InconsistentRequestException
     * @throws InvalidParameterValueException
     * @throws MissingParameterValueException
     */
    public static GetFeatureWithLock create( Map<String, String> kvp )
                            throws InconsistentRequestException, InvalidParameterValueException,
                            MissingParameterValueException {

        // SERVICE
        checkServiceParameter( kvp );

        // ID (deegree specific)
        String id = kvp.get( "ID" );

        // VERSION
        String version = checkVersionParameter( kvp );

        // OUTPUTFORMAT
        String outputFormat = getParam( "OUTPUTFORMAT", kvp, version.equals( "1.0.0" ) ? FORMAT_GML2_WFS100
                                                                                      : FORMAT_GML3 );

        // RESULTTYPE
        RESULT_TYPE resultType = RESULT_TYPE.RESULTS;
        String resultTypeString = kvp.get( "RESULTTYPE" );
        if ( "hits".equals( resultTypeString ) ) {
            resultType = RESULT_TYPE.HITS;
        }

        // FEATUREVERSION
        String featureVersion = kvp.get( "FEATUREVERSION" );

        // MAXFEATURES
        String maxFeaturesString = kvp.get( "MAXFEATURES" );
        // -1: fetch all features
        int maxFeatures = -1;
        if ( maxFeaturesString != null ) {
            try {
                maxFeatures = Integer.parseInt( maxFeaturesString );
                if ( maxFeatures < 1 ) {
                    throw new NumberFormatException();
                }
            } catch ( NumberFormatException e ) {
                LOG.logError( e.getMessage(), e );
                String msg = Messages.getMessage( "WFS_PARAMETER_INVALID_INT", maxFeaturesString, "MAXFEATURES" );
                throw new InvalidParameterValueException( msg );
            }
        }

        // STARTPOSITION (deegree specific)
        String startPosString = getParam( "STARTPOSITION", kvp, "1" );
        int startPosition = 1;
        try {
            startPosition = Integer.parseInt( startPosString );
            if ( startPosition < 1 ) {
                throw new NumberFormatException();
            }
        } catch ( NumberFormatException e ) {
            LOG.logError( e.getMessage(), e );
            String msg = Messages.getMessage( "WFS_PARAMETER_INVALID_INT", startPosString, "STARTPOSITION" );
            throw new InvalidParameterValueException( msg );
        }

        // SRSNAME
        String srsName = kvp.get( "SRSNAME" );

        // TYPENAME
        QualifiedName[] typeNames = extractTypeNames( kvp );
        if ( typeNames == null ) {
            // no TYPENAME parameter -> FEATUREID must be present
            String featureId = kvp.get( "FEATUREID" );
            if ( featureId != null ) {
                String msg = Messages.getMessage( "WFS_FEATUREID_PARAM_UNSUPPORTED" );
                throw new InvalidParameterValueException( msg );
            }
            String msg = Messages.getMessage( "WFS_TYPENAME+FID_PARAMS_MISSING" );
            throw new InvalidParameterValueException( msg );
        }

        // BBOX
        Filter bboxFilter = extractBBOXFilter( kvp );

        // FILTER (prequisite: TYPENAME)
        Map<QualifiedName, Filter> filterMap = extractFilters( kvp, typeNames );
        if ( bboxFilter != null && filterMap.size() > 0 ) {
            String msg = Messages.getMessage( "WFS_BBOX_FILTER_INVALID" );
            throw new InvalidParameterValueException( msg );
        }

        // PROPERTYNAME
        Map<QualifiedName, PropertyPath[]> propertyNameMap = extractPropNames( kvp, typeNames );

        // SORTBY
        SortProperty[] sortProperties = null;

        // TRAVERSEXLINKDEPTH
        int traverseXLinkDepth = -1;

        // TRAVERSEXLINKEXPIRY
        int traverseXLinkExpiry = -1;

        // build a Query instance for each requested feature type (later also for each featureid...)
        Query[] queries = new Query[typeNames.length];
        for ( int i = 0; i < queries.length; i++ ) {
            QualifiedName ftName = typeNames[i];
            PropertyPath[] properties = propertyNameMap.get( ftName );
            Filter filter;
            if ( bboxFilter != null ) {
                filter = bboxFilter;
            } else {
                filter = filterMap.get( ftName );
            }
            QualifiedName[] ftNames = new QualifiedName[] { ftName };
            queries[i] = new Query( properties, null, sortProperties, null, featureVersion, ftNames, null, srsName,
                                    filter, resultType, maxFeatures, startPosition );
        }

        // EXPIRY
        String expiryString = getParam( "EXPIRY", kvp, LockFeature.DEFAULT_EXPIRY );
        int expiry = 0;
        try {
            expiry = Integer.parseInt( expiryString );
            if ( expiry < 1 ) {
                throw new NumberFormatException();
            }
        } catch ( NumberFormatException e ) {
            String msg = Messages.getMessage( "WFS_PARAMETER_INVALID_INT", expiryString, "EXPIRY" );
            throw new InvalidParameterValueException( msg );
        }

        // LOCKACTION
        String lockActionString = getParam( "LOCKACTION", kvp, "ALL" );
        ALL_SOME_TYPE lockAction = LockFeature.validateLockAction( lockActionString );

        // build a GetFeatureLock request that contains all queries
        GetFeatureWithLock request = new GetFeatureWithLock( version, id, null, resultType, outputFormat, maxFeatures,
                                                             startPosition, traverseXLinkDepth, traverseXLinkExpiry,
                                                             queries, kvp, expiry, lockAction );
        return request;
    }

    /**
     * Returns the limit on how long the web feature service holds the lock in the event that a transaction is never
     * issued that would release the lock. The expiry limit is specified in milliseconds.
     *
     * @return the limit on how long the web feature service holds the lock (in milliseconds)
     */
    public long getExpiry() {
        return this.expiry;
    }

    /**
     * Returns the mode for lock acquisition.
     *
     * @see ALL_SOME_TYPE
     *
     * @return the mode for lock acquisition
     */
    public ALL_SOME_TYPE getLockAction() {
        return this.lockAction;
    }
}

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
package org.deegree.io.datastore.cached;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.Datastore;
import org.deegree.io.datastore.DatastoreException;
import org.deegree.io.datastore.DatastoreTransaction;
import org.deegree.io.datastore.schema.MappedFeatureType;
import org.deegree.io.datastore.schema.MappedGMLSchema;
import org.deegree.io.datastore.schema.MappedGMLSchemaDocument;
import org.deegree.io.rtree.HyperBoundingBox;
import org.deegree.io.rtree.HyperPoint;
import org.deegree.io.rtree.RTree;
import org.deegree.io.rtree.RTreeException;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.filterencoding.ComplexFilter;
import org.deegree.model.filterencoding.Filter;
import org.deegree.model.filterencoding.FilterEvaluationException;
import org.deegree.model.filterencoding.FilterTools;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryException;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Position;
import org.deegree.ogcwebservices.wfs.operation.Query;

/**
 * {@link Datastore} implementation that
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class CachedWFSDatastore extends Datastore {

    private ILogger LOG = LoggerFactory.getLogger( CachedWFSDatastore.class );

    private static RTree rTree;

    private static List<Feature> featureList;

    private String srsName;

    private int cacheSize = 100000;

    /**
     * default constructor; reads cache size from /cache.properties or if not available from
     * cache.properties
     *
     */
    public CachedWFSDatastore() {
        try {
            Properties prop = new Properties();
            InputStream is = CachedWFSDatastore.class.getResourceAsStream( "/cache.properties" );
            if ( is == null ) {
                is = CachedWFSDatastore.class.getResourceAsStream( "cache.properties" );
            }
            prop.load( is );
            is.close();
            cacheSize = Integer.parseInt( prop.getProperty( "size" ) );
        } catch ( Exception e ) {
            cacheSize = 100000;
        }
    }

    @Override
    public void bindSchema( MappedGMLSchema schema )
                            throws DatastoreException {
        super.bindSchema( schema );

        srsName = schema.getDefaultSRS().toString();
        try {
            init();
        } catch ( DatastoreException e ) {
            e.printStackTrace();
        }
    }

    private void init()
                            throws DatastoreException {
        CachedWFSDatastoreConfiguration mconf = (CachedWFSDatastoreConfiguration) this.getConfiguration();
        LOG.logInfo( "using cache size: " + cacheSize );
        if ( mconf != null ) {
            synchronized ( mconf ) {
                QualifiedName ft = mconf.getFeatureType();

                FeatureType fType = this.getSchemas()[0].getFeatureTypes()[0];
                if ( rTree == null ) {
                    LOG.logInfo( "initializing MemoryWFSDatastore for faeturetype ", fType );
                    try {
                        rTree = new RTree( 2, cacheSize );
                    } catch ( RTreeException e ) {
                        LOG.logError( e.getMessage(), e );
                        throw new DatastoreException( e.getMessage(), e );
                    }
                    featureList = new ArrayList<Feature>( cacheSize );
                    int accessSize = 1000;
                    try {
                        Query query = Query.create( ft );
                        query.setMaxFeatures( accessSize );
                        MappedGMLSchemaDocument doc = new MappedGMLSchemaDocument();
                        doc.load( mconf.getSchemaLocation() );
                        MappedGMLSchema mgs = doc.parseMappedGMLSchema();
                        Datastore ds = mgs.getDatastore();

                        FeatureCollection fc = null;
                        int k = 1;
                        int c = 0;
                        do {
                            String s = StringTools.concat( 100, "reading feature: " , k , " - " , (k+accessSize) );
                            LOG.logInfo( s );
                            MappedFeatureType[] rootFts = new MappedFeatureType[] { mgs.getFeatureType( ft ) };
                            query.setStartPosition( k-1 );
                            fc = ds.performQuery( query, rootFts );
                            for ( int i = 0; i < fc.size(); i++ ) {
                                Feature feature = fc.getFeature( i );
                                // insert feature into RTree
                                featureList.add( feature );
                                insertIntoRTree( rTree, feature, c++ );
                            }
                            k += accessSize;
                        } while ( fc.size() == accessSize && k <= cacheSize );
                        LOG.logInfo( Integer.toString( featureList.size() ), " features loaded" );
                    } catch ( Exception e ) {
                        LOG.logError( e.getMessage(), e );
                        throw new DatastoreException( e.getMessage(), e );
                    }
                }
            }
        }
    }

    private void insertIntoRTree( RTree rTree, Feature feature, int pos )
                            throws RTreeException {
        Envelope envelope = null;
        try {
            envelope = feature.getBoundedBy();
        } catch ( GeometryException e ) {
            LOG.logError( e.getMessage(), e );
            // maybe thrown because feature has no envelope; than use default BBOX
            envelope = GeometryFactory.createEnvelope( -999999, -999999, -999998, -999998, null );
        }

        Position p = envelope.getMin();
        HyperPoint min = new HyperPoint( new double[] { p.getX(), p.getY() } );
        p = envelope.getMax();
        HyperPoint max = new HyperPoint( new double[] { p.getX(), p.getY() } );
        HyperBoundingBox hbb = new HyperBoundingBox( min, max );
        rTree.insert( pos, hbb );

    }

    @Override
    public CachedWFSAnnotationDocument getAnnotationParser() {
        return new CachedWFSAnnotationDocument();
    }

    @Override
    public void close()
                            throws DatastoreException {
        // nothing to do
    }

    @Override
    @SuppressWarnings("unused")
    public FeatureCollection performQuery( Query query, MappedFeatureType[] rootFts, DatastoreTransaction context )
                            throws DatastoreException, UnknownCRSException {
        return performQuery( query, rootFts );
    }

    @Override
    @SuppressWarnings("unused")
    public FeatureCollection performQuery( Query query, MappedFeatureType[] rootFts )
                            throws DatastoreException, UnknownCRSException {

        if ( rootFts.length > 1 ) {
            String msg = Messages.getMessage( "DATASTORE_SHAPE_DOES_NOT_SUPPORT_JOINS" );
            throw new DatastoreException( msg );
        }

        MappedFeatureType ft = rootFts[0];

        // perform CRS transformation (if necessary)
        Query transformedQuery = transformQuery( query );

        FeatureCollection result = null;
        int startPosition = -1;
        int maxFeatures = -1;

        int record = -1;
        try {
            startPosition = transformedQuery.getStartPosition();
            maxFeatures = transformedQuery.getMaxFeatures();
            Filter filter = transformedQuery.getFilter();
            Envelope bbox = null;
            if ( filter instanceof ComplexFilter ) {
                Object[] objects = null;
                try {
                    objects = FilterTools.extractFirstBBOX( (ComplexFilter) filter );
                } catch ( Exception e ) {
                    LOG.logError( e.getMessage(), e );
                    String msg = Messages.getMessage( "DATASTORE_EXTRACTBBOX", record );
                    throw new DatastoreException( msg, e );
                }
                bbox = (Envelope) objects[0];
                filter = (Filter) objects[1];
            }
            if ( bbox == null ) {
                bbox = GeometryFactory.createEnvelope( Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE,
                                                       Integer.MAX_VALUE, null );
            }

            Position p = bbox.getMin();
            HyperPoint min = new HyperPoint( new double[] { p.getX(), p.getY() } );
            p = bbox.getMax();
            HyperPoint max = new HyperPoint( new double[] { p.getX(), p.getY() } );
            HyperBoundingBox hbb = new HyperBoundingBox( min, max );
            Object[] obj = rTree.intersects( hbb );
            // id=identity required
            if ( obj != null ) {
                // check parameters for sanity
                if ( startPosition < 1 ) {
                    startPosition = 1;
                }
                if ( ( maxFeatures < 0 ) || ( maxFeatures >= obj.length ) ) {
                    maxFeatures = obj.length;
                }
                result = FeatureFactory.createFeatureCollection( UUID.randomUUID().toString(), obj.length );

                // TODO: respect startposition

                CoordinateSystem crs = CRSFactory.create( srsName );
                for ( int i = 0; i < maxFeatures; i++ ) {
                    Feature feat = featureList.get( ( (Integer) obj[i] ).intValue() );
                    if ( filter == null || filter.evaluate( feat ) ) {
                        String msg = StringTools.concat( 200, "Adding feature '", feat.getId(),
                                                         "' to FeatureCollection (with CRS ", srsName, ")." );
                        LOG.logDebug( msg );

                        result.add( feat );
                    }
                }

                // update the envelopes
                result.setEnvelopesUpdated();
                result.getBoundedBy();
            } else {
                result = FeatureFactory.createFeatureCollection( UUID.randomUUID().toString(), 1 );
            }
        } catch ( FilterEvaluationException e ) {
            throw new DatastoreException( e.getMessage(), e );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            String msg = Messages.getMessage( "DATASTORE_READINGFROMDBF", record );
            throw new DatastoreException( msg, e );
        }

        // transform result to queried srs if necessary
        String targetSrsName = transformedQuery.getSrsName();
        if ( targetSrsName != null && !targetSrsName.equals( this.srsName ) ) {
            result = transformResult( result, transformedQuery.getSrsName() );
        }

        return result;
    }
}

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
package org.deegree.feature.persistence.shape;

import static org.deegree.commons.utils.CollectionUtils.unzipPair;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2_OR_3;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.GEOMETRY;
import static org.deegree.feature.types.property.ValueRepresentation.BOTH;
import static org.deegree.geometry.utils.GeometryUtils.createEnvelope;
import static org.deegree.gml.GMLVersion.GML_31;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import org.deegree.commons.index.RTree;
import org.deegree.commons.utils.CloseableIterator;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.exceptions.WKTParsingException;
import org.deegree.feature.Feature;
import org.deegree.feature.Features;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.cache.FeatureStoreCache;
import org.deegree.feature.persistence.cache.SimpleFeatureStoreCache;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.CombinedResultSet;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.FilteredFeatureResultSet;
import org.deegree.feature.persistence.query.IteratorResultSet;
import org.deegree.feature.persistence.query.MemoryFeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.sort.SortProperty;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.gml.GMLObject;
import org.slf4j.Logger;

/**
 * {@link FeatureStore} implementation that uses shape files as backend.
 * 
 * @see FeatureStore
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(trace = "logs stack traces", debug = "logs information about reading the files, which srs etc.", warn = "logs reasons for not setting up the store", error = "logs grave errors that were not forseen to happen")
public class ShapeFeatureStore implements FeatureStore {

    private static final Logger LOG = getLogger( ShapeFeatureStore.class );

    private SHPReader shp;

    private DBFReader dbf;

    private long shpLastModified, dbfLastModified;

    private File shpFile, dbfFile;

    private String shpName;

    private CRS crs;

    private Charset encoding;

    private boolean available = true;

    private GeometryTransformer transformer;

    private FeatureType ft;

    private ApplicationSchema schema;

    private final String namespace;

    private final FeatureStoreCache cache;

    private DBFIndex dbfIndex;

    private QName featureTypeName;

    private boolean generateAlphanumericIndexes;

    /**
     * Creates a new {@link ShapeFeatureStore} instance from the given parameters.
     * 
     * @param shpName
     *            name of the shape file to be loaded, may omit the ".shp" extension, must not be <code>null</code
     * @param crs
     *            crs used by the shape file, must not be <code>null</code>
     * @param encoding
     *            encoding used in the dbf file, can be <code>null</code> (encoding guess mode)
     * @param namespace
     *            namespace to be used for the feature type, must not be <code>null</code>
     * @param featureTypeName
     *            if null, the shape file base name will be used
     * @param generateAlphanumericIndexes
     *            whether to copy the dbf into a h2 database for indexing
     * @param cache
     *            used for caching retrieved feature instances, can be <code>null</code> (will create a default cache)
     */
    public ShapeFeatureStore( String shpName, CRS crs, Charset encoding, String namespace, String featureTypeName,
                              boolean generateAlphanumericIndexes, FeatureStoreCache cache ) {
        this.shpName = shpName;
        this.crs = crs;
        this.encoding = encoding;
        this.namespace = namespace;
        featureTypeName = featureTypeName == null ? new File( shpName ).getName() : featureTypeName;
        this.featureTypeName = new QName( namespace, featureTypeName );
        this.generateAlphanumericIndexes = generateAlphanumericIndexes;
        if ( cache != null ) {
            this.cache = cache;
        } else {
            this.cache = new SimpleFeatureStoreCache();
        }
    }

    @Override
    public void init() {

        if ( shpName.toLowerCase().endsWith( ".shp" ) ) {
            shpName = shpName.substring( 0, shpName.length() - 4 );
        }

        LOG.debug( "Loading shape file '{}'", shpName );

        if ( crs == null ) {
            File prj = new File( shpName + ".PRJ" );
            if ( !prj.exists() ) {
                prj = new File( shpName + ".prj" );
            }
            if ( prj.exists() ) {
                try {
                    try {
                        crs = new CRS( prj );
                    } catch ( IOException e ) {
                        LOG.trace( "Stack trace:", e );
                        LOG.warn( "The shape datastore for '{}' could not be initialized, because no CRS was defined.",
                                  shpName );
                        available = false;
                        return;
                    } catch ( Exception e1 ) {
                        getCRSFromFile( prj );
                        if ( crs == null ) {
                            return;
                        }
                    }
                } catch ( WKTParsingException e ) {
                    getCRSFromFile( prj );
                    if ( crs == null ) {
                        return;
                    }
                }
            } else {
                LOG.debug( "No crs configured, and no .prj found, assuming CRS:84 (WGS84 in x/y axis order)." );
                crs = new CRS( "CRS:84" );
            }
        }

        try {
            transformer = new GeometryTransformer( crs.getWrappedCRS() );
        } catch ( IllegalArgumentException e ) {
            LOG.error( "Unknown error", e );
        } catch ( UnknownCRSException e ) {
            LOG.error( "Unknown error", e );
        }

        shpFile = new File( shpName + ".SHP" );
        if ( !shpFile.exists() ) {
            shpFile = new File( shpName + ".shp" );
        }
        shpLastModified = shpFile.lastModified();
        dbfFile = new File( shpName + ".DBF" );
        if ( !dbfFile.exists() ) {
            dbfFile = new File( shpName + ".dbf" );
        }
        dbfLastModified = dbfFile.lastModified();

        try {
            shp = getSHP( false );
        } catch ( IOException e ) {
            LOG.debug( "Stack trace:", e );
            LOG.warn( "The shape datastore for '{}' could not be initialized, because the .shp could not be loaded.",
                      shpName );
        }

        try {
            dbf = new DBFReader( new RandomAccessFile( dbfFile, "r" ), encoding, featureTypeName, namespace );

            if ( generateAlphanumericIndexes ) {
                // set up index
                dbfIndex = new DBFIndex( dbf, dbfFile, shp.readEnvelopes() );
            }

            ft = dbf.getFeatureType();
        } catch ( IOException e ) {
            LOG.warn( "A dbf file was not loaded (no attributes will be available): {}.dbf", shpName );
            GeometryPropertyType geomProp = new GeometryPropertyType( new QName( namespace, "geometry" ), 0, 1, false,
                                                                      false, null, GEOMETRY, DIM_2_OR_3, BOTH );
            ft = new GenericFeatureType( featureTypeName, Collections.<PropertyType> singletonList( geomProp ), false );
        }
        schema = new ApplicationSchema( new FeatureType[] { ft }, null, null, null );
    }

    private void getCRSFromFile( File prj ) {
        try {
            BufferedReader in = new BufferedReader( new FileReader( prj ) );
            String c = in.readLine().trim();
            try {
                crs = new CRS( c );
                crs.getWrappedCRS(); // resolve NOW
                LOG.debug( ".prj contained EPSG code '{}'", crs.getName() );
            } catch ( UnknownCRSException e2 ) {
                LOG.warn( "Could not parse the .prj projection file for {}, reason: {}.", shpName,
                          e2.getLocalizedMessage() );
                LOG.warn( "The file also does not contain a valid EPSG code, assuming CRS:84 (WGS84 with x/y axis order)." );
                LOG.trace( "Stack trace of failed WKT parsing:", e2 );
                crs = new CRS( "CRS:84" );
            }
        } catch ( IOException e1 ) {
            LOG.debug( "Stack trace:", e1 );
            LOG.warn( "The shape datastore for '{}' could not be initialized, because no CRS was defined.", shpName );
            available = false;
        } catch ( Exception e1 ) {
            LOG.warn( "The shape datastore for '{}' could not be initialized, because no CRS was defined.", shpName );
            LOG.trace( "Stack trace:", e1 );
            available = false;
        }
    }

    private SHPReader getSHP( boolean forceIndexRebuild )
                            throws IOException {

        shp = null;

        File rtfile = new File( shpName + ".rti" );
        RandomAccessFile raf = new RandomAccessFile( shpFile, "r" );

        if ( rtfile.exists() && !( rtfile.lastModified() < shpFile.lastModified() ) && !forceIndexRebuild ) {
            try {
                LOG.debug( "Loading RTree from disk." );
                RTree<Long> rtree = RTree.loadFromDisk( shpName + ".rti" );
                shp = new SHPReader( raf, crs, rtree, rtree.getExtraFlag() );
            } catch ( IOException e ) {
                LOG.debug( "Stack trace:", e );
                LOG.warn( "Existing rtree index could not be read. Generating a new one..." );
            }
            if ( shp != null ) {
                return shp;
            }
        }

        shp = new SHPReader( raf, crs, null, false );

        LOG.debug( "Building rtree index in memory for '{}'", new File( shpName ).getName() );

        Pair<RTree<Long>, Boolean> p = createIndex( shp );
        LOG.debug( "done building index." );
        shp = new SHPReader( raf, crs, p.first, p.second );
        p.first.writeTreeToDisk( shpName + ".rti" );
        return shp;
    }

    /**
     * @param shapeReader
     * @throws IOException
     */
    private static Pair<RTree<Long>, Boolean> createIndex( SHPReader shapeReader )
                            throws IOException {
        Envelope env = shapeReader.getEnvelope();
        // use 128 values per rect.
        RTree<Long> result = new RTree<Long>( createEnvelope( env ), -1 );
        // to work around Java's non-existent variant type
        LOG.debug( "Read envelopes from shape file..." );
        Pair<ArrayList<Pair<float[], Long>>, Boolean> p = shapeReader.readEnvelopes();
        LOG.debug( "done reading envelopes." );
        result.insertBulk( p.first );
        return new Pair<RTree<Long>, Boolean>( result, p.second );
    }

    private void checkForUpdate() {
        try {
            synchronized ( shpFile ) {
                if ( shpLastModified != shpFile.lastModified() ) {
                    shp.close();
                    LOG.debug( "Re-opening the shape file {}", shpName );
                    shp = getSHP( true );
                    shpLastModified = shpFile.lastModified();
                    cache.clear();
                }
            }
            synchronized ( dbfFile ) {
                if ( dbf != null && dbfLastModified != dbfFile.lastModified() ) {
                    dbf.close();
                    LOG.debug( "Re-opening the dbf file {}", shpName );
                    dbf = new DBFReader( new RandomAccessFile( dbfFile, "r" ), encoding,
                                         new QName( namespace, shpName ), namespace );
                    if ( generateAlphanumericIndexes ) {
                        // set up index
                        dbfIndex = new DBFIndex( dbf, dbfFile, shp.readEnvelopes() );
                    }
                    ft = dbf.getFeatureType();
                    schema = new ApplicationSchema( new FeatureType[] { ft }, null, null, null );
                    dbfLastModified = dbfFile.lastModified();
                    cache.clear();
                }
            }
        } catch ( IOException e ) {
            available = false;
            LOG.debug( "Shape file {} is unavailable at the moment: {}", shpName, e.getLocalizedMessage() );
            LOG.trace( "Stack trace was {}", e );
        }
    }

    /**
     * @param bbox
     * @return the bbox in the native srs
     */
    private Envelope getTransformedEnvelope( Envelope bbox ) {
        if ( bbox != null && transformer != null ) {
            try {
                bbox = transformer.transform( bbox );
            } catch ( IllegalArgumentException e ) {
                LOG.error( "Unknown error", e );
            } catch ( TransformationException e ) {
                LOG.error( "Unknown error", e );
            } catch ( UnknownCRSException e ) {
                LOG.error( "Unknown error", e );
            }
        }
        return bbox;
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public FeatureResultSet query( Query query )
                            throws FilterEvaluationException, FeatureStoreException {

        if ( query.getTypeNames() == null || query.getTypeNames().length > 1 ) {
            String msg = "Only queries with exactly one or zero type name(s) are supported.";
            throw new UnsupportedOperationException( msg );
        }

        QName featureType = query.getTypeNames()[0].getFeatureTypeName();
        if ( featureType != null && !featureType.equals( ft.getName() ) ) {
            // or null?
            return new MemoryFeatureResultSet( new GenericFeatureCollection() );
        }

        checkForUpdate();

        if ( !available ) {
            return null;
        }

        List<Pair<Integer, Long>> recNumsAndPos = new LinkedList<Pair<Integer, Long>>();
        Envelope bbox = getTransformedEnvelope( query.getPrefilterBBox() );

        Filter filter = query.getFilter();

        boolean queryIndex = filter == null || !generateAlphanumericIndexes;
        Pair<Filter, SortProperty[]> p = queryIndex ? null : dbfIndex.query( recNumsAndPos, filter,
                                                                             query.getSortProperties() );

        try {
            HashSet<Integer> recNums = new HashSet<Integer>( unzipPair( recNumsAndPos ).first );
            recNumsAndPos = shp.query( bbox, filter == null || p == null ? null : recNums );
            LOG.debug( "{} records matching after BBOX filtering", recNumsAndPos.size() );
        } catch ( IOException e ) {
            LOG.debug( "Stack trace", e );
            throw new FeatureStoreException( e );
        }

        FeatureResultSet rs = new IteratorResultSet( new FeatureIterator( recNumsAndPos.iterator() ) );

        if ( p != null && p.first != null ) {
            LOG.debug( "Applying in-memory filtering." );
            rs = new FilteredFeatureResultSet( rs, p.first );
        }

        if ( p != null && p.second != null && p.second.length > 0 ) {
            LOG.debug( "Applying in-memory sorting." );
            rs = new MemoryFeatureResultSet( Features.sortFc( rs.toCollection(), p.second ) );
        }

        return rs;
    }

    @Override
    public FeatureResultSet query( final Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        Iterator<FeatureResultSet> rsIter = new Iterator<FeatureResultSet>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < queries.length;
            }

            @Override
            public FeatureResultSet next() {
                if ( !hasNext() ) {
                    throw new NoSuchElementException();
                }
                FeatureResultSet rs;
                try {
                    rs = query( queries[i++] );
                } catch ( Exception e ) {
                    throw new RuntimeException( e.getMessage(), e );
                }
                return rs;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
        return new CombinedResultSet( rsIter );
    }

    private Feature retrieveFeature( Pair<Integer, Long> recNumAndPos )
                            throws FeatureStoreException {

        String fid = buildFID( recNumAndPos.first );
        Feature feature = (Feature) cache.get( fid );

        if ( feature == null ) {
            LOG.trace( "Cache miss for feature {}", fid );

            // add simple properties
            HashMap<SimplePropertyType, Property> entry;
            if ( dbf != null ) {
                try {
                    entry = dbf.getEntry( recNumAndPos.first );
                } catch ( IOException e ) {
                    LOG.trace( "Stack trace", e );
                    throw new FeatureStoreException( e );
                }
            } else {
                entry = new HashMap<SimplePropertyType, Property>();
            }
            LinkedList<Property> props = new LinkedList<Property>();
            for ( PropertyType t : ft.getPropertyDeclarations() ) {
                if ( entry.containsKey( t ) ) {
                    props.add( entry.get( t ) );
                }
            }

            // add geometry property
            Geometry g = shp.readGeometry( recNumAndPos.second );
            props.add( new GenericProperty( ft.getDefaultGeometryPropertyDeclaration(), g ) );
            feature = ft.newFeature( fid, props, GML_31 );

            cache.add( feature );
        } else {
            LOG.trace( "Cache hit for feature {}", fid );
        }
        return feature;
    }

    private String buildFID( int num ) {
        // TODO make this configurable
        return "shp_" + num;
    }

    @Override
    public int queryHits( Query query )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO
        return query( query ).toCollection().size();
    }

    @Override
    public int queryHits( final Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO
        return query( queries ).toCollection().size();
    }

    /**
     * @return the envelope of the shape file
     */
    @Override
    public Envelope getEnvelope( QName ftName ) {
        checkForUpdate();
        if ( shp == null ) {
            return null;
        }
        return shp.getEnvelope();
    }

    @Override
    public void destroy() {
        cache.clear();
        try {
            if ( shp != null ) {
                shp.close();
            }
        } catch ( IOException e ) {
            LOG.debug( "SHP could not be closed:", e );
        }
        if ( dbf != null ) {
            try {
                dbf.close();
            } catch ( IOException e ) {
                LOG.debug( "DBF could not be closed:", e );
            }
        }
    }

    /**
     * @return whether the shape file is currently available
     */
    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public FeatureStoreTransaction acquireTransaction()
                            throws FeatureStoreException {
        throw new FeatureStoreException( "The shape datastore is currently not transactional." );
    }

    @Override
    public LockManager getLockManager()
                            throws FeatureStoreException {
        throw new FeatureStoreException( "The shape datastore is currently not transactional." );
    }

    @Override
    public GMLObject getObjectById( String id )
                            throws FeatureStoreException {
        // TODO
        throw new FeatureStoreException( "This feature is currently not implemented for the shape datastore." );
    }

    @Override
    public ApplicationSchema getSchema() {
        return schema;
    }

    @Override
    public CRS getStorageSRS() {
        return crs;
    }

    private class FeatureIterator implements CloseableIterator<Feature> {

        private final Iterator<Pair<Integer, Long>> recIter;

        private FeatureIterator( Iterator<Pair<Integer, Long>> recIter ) {
            this.recIter = recIter;
        }

        @Override
        public void close() {
            // nothing to do
        }

        @Override
        public Collection<Feature> getAsCollectionAndClose( Collection<Feature> collection ) {
            while ( hasNext() ) {
                collection.add( next() );
            }
            return collection;
        }

        @Override
        public List<Feature> getAsListAndClose() {
            return (List<Feature>) getAsCollectionAndClose( new LinkedList<Feature>() );
        }

        @Override
        public boolean hasNext() {
            return recIter.hasNext();
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public Feature next() {
            Feature f = null;
            try {
                f = retrieveFeature( recIter.next() );
            } catch ( FeatureStoreException e ) {
                throw new RuntimeException( e.getMessage(), e );
            }
            return f;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}

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
import static org.deegree.feature.types.property.ValueRepresentation.BOTH;
import static org.deegree.filter.Filters.splitOffBBoxConstraint;
import static org.deegree.geometry.utils.GeometryUtils.createEnvelope;
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

import org.apache.commons.io.IOUtils;
import org.deegree.commons.annotations.LoggingNotes;
import org.deegree.commons.index.RTree;
import org.deegree.commons.tom.gml.GMLObject;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.gml.property.PropertyType;
import org.deegree.commons.utils.CloseableIterator;
import org.deegree.commons.utils.Pair;
import org.deegree.cs.configuration.wkt.WKTParser;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.cs.exceptions.WKTParsingException;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.feature.Feature;
import org.deegree.feature.Features;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.cache.FeatureStoreCache;
import org.deegree.feature.persistence.cache.SimpleFeatureStoreCache;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.persistence.shape.ShapeFeatureStoreProvider.Mapping;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.stream.CombinedFeatureInputStream;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.stream.FilteredFeatureInputStream;
import org.deegree.feature.stream.IteratorFeatureInputStream;
import org.deegree.feature.stream.MemoryFeatureInputStream;
import org.deegree.feature.types.AppSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericAppSchema;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.IdFilter;
import org.deegree.filter.ResourceId;
import org.deegree.filter.sort.SortProperty;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
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

    private ICRS crs;

    private Charset encoding;

    private boolean available = true;

    private GeometryTransformer transformer;

    private FeatureType ft;

    private AppSchema schema;

    private final FeatureStoreCache cache;

    private DBFIndex dbfIndex;

    private QName ftName;

    private boolean generateAlphanumericIndexes;

    private String fidPrefix;

    private final List<Mapping> mappings;

    private ResourceMetadata<FeatureStore> metadata;

    private boolean strict;

    /**
     * Creates a new {@link ShapeFeatureStore} instance from the given parameters.
     * 
     * @param shpName
     *            name of the shape file to be loaded, may omit the ".shp" extension, must not be <code>null</code
     * @param crs
     *            crs used by the shape file, must not be <code>null</code>
     * @param encoding
     *            encoding used in the dbf file, can be <code>null</code> (encoding guess mode)
     * @param ftNamespace
     *            namespace to be used for the feature type, must not be <code>null</code>
     * @param localFtName
     *            if null, the shape file base name will be used
     * @param ftPrefix
     * @param generateAlphanumericIndexes
     *            whether to copy the dbf into a h2 database for indexing
     * @param cache
     *            used for caching retrieved feature instances, can be <code>null</code> (will create a default cache)
     * @param mappings
     *            may be null, in which case the original DBF names and 'geometry' will be used
     */
    public ShapeFeatureStore( String shpName, ICRS crs, Charset encoding, String ftNamespace, String localFtName,
                              String ftPrefix, boolean generateAlphanumericIndexes, FeatureStoreCache cache,
                              List<Mapping> mappings, ResourceMetadata<FeatureStore> metadata ) {
        this.shpName = shpName;
        this.crs = crs;
        this.encoding = encoding;
        this.mappings = mappings;
        this.metadata = metadata;

        localFtName = localFtName == null ? new File( shpName ).getName() : localFtName;
        if ( localFtName.endsWith( ".shp" ) ) {
            localFtName = localFtName.substring( 0, localFtName.length() - 4 );
        }

        // TODO make this configurable
        fidPrefix = localFtName.toUpperCase() + "_";

        // TODO allow null namespaces / empty prefix
        // NOTE: verify that the WFS code for dealing with that (e.g. repairing
        // unqualified names) works with that first
        ftNamespace = ( ftNamespace != null && !ftNamespace.isEmpty() ) ? ftNamespace : "http://www.deegree.org/app";
        ftPrefix = ( ftPrefix != null && !ftPrefix.isEmpty() ) ? ftPrefix : "app";

        this.ftName = new QName( ftNamespace, localFtName, ftPrefix );
        this.generateAlphanumericIndexes = generateAlphanumericIndexes;
        if ( cache != null ) {
            this.cache = cache;
        } else {
            this.cache = new SimpleFeatureStoreCache();
        }
    }

    private void getCRSFromFile( File prj ) {
        BufferedReader in = null;
        try {
            in = new BufferedReader( new FileReader( prj ) );
            String c = in.readLine().trim();
            try {
                crs = CRSManager.lookup( c );
                LOG.debug( ".prj contained EPSG code '{}'", crs.getAlias() );
            } catch ( UnknownCRSException e2 ) {
                LOG.warn( "Could not parse the .prj projection file for {}, reason: {}.", shpName,
                          e2.getLocalizedMessage() );
                LOG.warn( "The file also does not contain a valid EPSG code, assuming CRS:84 (WGS84 with x/y axis order)." );
                LOG.trace( "Stack trace of failed WKT parsing:", e2 );
                crs = CRSManager.lookup( "CRS:84" );
            }
        } catch ( IOException e1 ) {
            LOG.debug( "Stack trace:", e1 );
            LOG.warn( "The shape datastore for '{}' could not be initialized, because no CRS was defined.", shpName );
            available = false;
        } catch ( Exception e1 ) {
            LOG.warn( "The shape datastore for '{}' could not be initialized, because no CRS was defined.", shpName );
            LOG.trace( "Stack trace:", e1 );
            available = false;
        } finally {
            IOUtils.closeQuietly( in );
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
     */
    private static Pair<RTree<Long>, Boolean> createIndex( SHPReader shapeReader ) {
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
                    dbf = new DBFReader( new RandomAccessFile( dbfFile, "r" ), encoding, ftName, shp.getGeometryType(),
                                         mappings );
                    if ( generateAlphanumericIndexes ) {
                        // set up index
                        dbfIndex = new DBFIndex( dbf, dbfFile, shp.readEnvelopes(), mappings );
                    }
                    ft = dbf.getFeatureType();
                    schema = new GenericAppSchema( new FeatureType[] { ft }, null, null, null, null, null );
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
        if ( bbox != null && bbox.getCoordinateSystem() != null && transformer != null ) {
            try {
                bbox = transformer.transform( bbox );
            } catch ( Exception e ) {
                LOG.error( "Transformation of bbox failed: " + e.getMessage(), e );
            }
        }
        return bbox;
    }

    @Override
    public FeatureInputStream query( Query query )
                            throws FilterEvaluationException, FeatureStoreException {

        if ( query.getTypeNames() == null || query.getTypeNames().length > 1 ) {
            String msg = "Only queries with exactly one or zero type name(s) are supported.";
            throw new UnsupportedOperationException( msg );
        }

        HashSet<Integer> idFilterNums = null;
        if ( query.getFilter() instanceof IdFilter ) {
            idFilterNums = new HashSet<Integer>();
            IdFilter f = (IdFilter) query.getFilter();
            List<ResourceId> ids = f.getSelectedIds();
            for ( ResourceId id : ids ) {
                if ( id.getRid().startsWith( fidPrefix ) ) {
                    String[] ss = id.getRid().split( "_" );
                    idFilterNums.add( Integer.valueOf( ss[1] ) );
                }
            }
        }

        if ( query.getTypeNames().length == 0 && !( query.getFilter() instanceof IdFilter ) || idFilterNums != null
             && idFilterNums.isEmpty() ) {
            return new MemoryFeatureInputStream( new GenericFeatureCollection() );
        }

        if ( query.getTypeNames().length > 0 ) {
            QName featureType = query.getTypeNames()[0].getFeatureTypeName();
            if ( featureType != null && !featureType.equals( ft.getName() ) ) {
                // or null?
                return new MemoryFeatureInputStream( new GenericFeatureCollection() );
            }
        }

        checkForUpdate();

        if ( !available ) {
            return null;
        }

        Filter filter = query.getFilter();
        Pair<Filter, Envelope> filterPair = splitOffBBoxConstraint( filter );

        List<Pair<Integer, Long>> recNumsAndPos = new LinkedList<Pair<Integer, Long>>();
        Envelope bbox = getTransformedEnvelope( query.getPrefilterBBoxEnvelope() );

        if ( bbox == null ) {
            getEnvelope( null );
        }

        boolean queryIndex = filterPair.first == null || !generateAlphanumericIndexes;
        Pair<Filter, SortProperty[]> p = queryIndex ? null : dbfIndex.query( recNumsAndPos, filterPair.first,
                                                                             query.getSortProperties() );
        HashSet<Integer> recNums = new HashSet<Integer>( unzipPair( recNumsAndPos ).first );
        if ( idFilterNums != null ) {
            recNums.addAll( idFilterNums );
        }
        recNumsAndPos = shp.query( bbox, filter == null || p == null ? null : recNums );
        LOG.debug( "{} records matching after BBOX filtering", recNumsAndPos.size() );

        // don't forget about filters if dbf index could not be queried
        if ( p == null ) {
            p = new Pair<Filter, SortProperty[]>( filterPair.first, query.getSortProperties() );
        }

        FeatureInputStream rs = new IteratorFeatureInputStream( new FeatureIterator( recNumsAndPos.iterator() ) );

        if ( p.first != null ) {
            LOG.debug( "Applying in-memory filtering." );
            rs = new FilteredFeatureInputStream( rs, p.first );
        }

        if ( p.second != null && p.second.length > 0 ) {
            LOG.debug( "Applying in-memory sorting." );
            rs = new MemoryFeatureInputStream( Features.sortFc( rs.toCollection(), p.second ) );
        }

        return rs;
    }

    @Override
    public FeatureInputStream query( final Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        Iterator<FeatureInputStream> rsIter = new Iterator<FeatureInputStream>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < queries.length;
            }

            @Override
            public FeatureInputStream next() {
                if ( !hasNext() ) {
                    throw new NoSuchElementException();
                }
                FeatureInputStream rs;
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
        return new CombinedFeatureInputStream( rsIter );
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
            feature = ft.newFeature( fid, props, null );

            cache.add( feature );
        } else {
            LOG.trace( "Cache hit for feature {}", fid );
        }
        return feature;
    }

    private String buildFID( int num ) {
        return fidPrefix + num;
    }

    @Override
    public int queryHits( Query query )
                            throws FeatureStoreException, FilterEvaluationException {
        // TODO
        return query( query ).toCollection().size();
    }

    @Override
    public int[] queryHits( Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        int[] hits = new int[queries.length];
        for ( int i = 0; i < queries.length; i++ ) {
            hits[i] = queryHits( queries[i] );
        }
        return hits;
    }

    @Override
    public Envelope getEnvelope( QName ftName )
                            throws FeatureStoreException {
        return calcEnvelope( ftName );
    }

    @Override
    public Envelope calcEnvelope( QName ftName ) {
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
        if ( dbfIndex != null ) {
            dbfIndex.destroy();
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
    public AppSchema getSchema() {
        return schema;
    }

    @Override
    public boolean isMapped( QName ftName ) {
        return schema.getFeatureType( ftName ) != null;
    }

    @Override
    public boolean isMaxFeaturesAndStartIndexApplicable( Query[] queries ) {
        return false;
    }

    /**
     * Returns the CRS used by the shape file.
     * 
     * @return the CRS used by the shape file, never <code>null</code>
     */
    public ICRS getStorageCRS() {
        return crs;
    }

    private class FeatureIterator implements CloseableIterator<Feature> {

        private final Iterator<Pair<Integer, Long>> recIter;

        FeatureIterator( Iterator<Pair<Integer, Long>> recIter ) {
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

    @Override
    public ResourceMetadata<? extends Resource> getMetadata() {
        return metadata;
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
                        crs = new WKTParser( prj ).parseCoordinateSystem();
                    } catch ( IOException e ) {
                        String msg = "The shape datastore for '" + shpName
                                     + "' could not be initialized, because no CRS was defined.";
                        throw new ResourceInitException( msg );
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
                crs = CRSManager.getCRSRef( "CRS:84" );
            }
        }

        try {
            transformer = new GeometryTransformer( crs );
        } catch ( IllegalArgumentException e ) {
            LOG.error( "Unknown error", e );
            // } catch ( UnknownCRSException e ) {
            // LOG.error( "Unknown error", e );
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
            String msg = "The shape datastore for '" + shpName
                         + "' could not be initialized, because the .shp could not be loaded.";
            throw new ResourceInitException( msg );
        }

        String namespace = ftName.getNamespaceURI();

        try {
            dbf = new DBFReader( new RandomAccessFile( dbfFile, "r" ), encoding, ftName, shp.getGeometryType(),
                                 mappings );

            if ( generateAlphanumericIndexes ) {
                // set up index
                dbfIndex = new DBFIndex( dbf, dbfFile, shp.readEnvelopes(), mappings );
            }

            ft = dbf.getFeatureType();
        } catch ( IOException e ) {
            LOG.warn( "A dbf file was not loaded (no attributes will be available): {}.dbf", shpName );
            GeometryPropertyType geomProp = new GeometryPropertyType( new QName( namespace, "geometry",
                                                                                 ftName.getPrefix() ), 0, 1, null,
                                                                      null, shp.getGeometryType(), DIM_2_OR_3, BOTH );
            ft = new GenericFeatureType( ftName, Collections.<PropertyType> singletonList( geomProp ), false );
        }
        schema = new GenericAppSchema( new FeatureType[] { ft }, null, null, null, null, null );
    }
}
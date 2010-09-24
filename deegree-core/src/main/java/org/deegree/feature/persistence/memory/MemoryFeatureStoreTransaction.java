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

package org.deegree.feature.persistence.memory;

import static org.deegree.feature.i18n.Messages.getMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.genericxml.GenericXMLElementContent;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.utils.kvp.MissingParameterException;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.Lock;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryTransformer;
import org.deegree.geometry.linearization.GeometryLinearizer;
import org.deegree.geometry.linearization.LinearizationCriterion;
import org.deegree.geometry.linearization.NumPointsCriterion;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.primitive.Surface;
import org.deegree.gml.GMLObject;
import org.deegree.gml.GMLVersion;
import org.deegree.gml.feature.FeatureReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureStoreTransaction} implementation used by the {@link MemoryFeatureStore}.
 * 
 * @see MemoryFeatureStore
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class MemoryFeatureStoreTransaction implements FeatureStoreTransaction {

    private static final Logger LOG = LoggerFactory.getLogger( MemoryFeatureStoreTransaction.class );

    private final GeometryLinearizer linearizer = new GeometryLinearizer();

    private final LinearizationCriterion crit = new NumPointsCriterion( 20 );

    private final MemoryFeatureStore fs;

    private final StoredFeatures sf;

    /**
     * Creates a new {@link MemoryFeatureStoreTransaction} instance.
     * 
     * NOTE: This method is only supposed to be invoked by the {@link MemoryFeatureStore}.
     * 
     * @param sf
     *            invoking feature store instance, never <code>null</code>
     */
    MemoryFeatureStoreTransaction( MemoryFeatureStore fs, StoredFeatures sf ) {
        this.fs = fs;
        this.sf = sf;
    }

    @Override
    public void commit()
                            throws FeatureStoreException {
        try {
            sf.buildMaps();
        } catch ( UnknownCRSException e ) {
            throw new FeatureStoreException( e.getMessage() );
        }
        fs.storedFeatures = sf;
        fs.releaseTransaction( this );
    }

    @Override
    public FeatureStore getStore() {
        return fs;
    }

    @Override
    public int performDelete( QName ftName, OperatorFilter filter, Lock lock )
                            throws FeatureStoreException {

        String lockId = lock != null ? lock.getId() : null;
        FeatureType ft = fs.getSchema().getFeatureType( ftName );
        if ( ft == null ) {
            throw new FeatureStoreException( getMessage( "TA_OPERATION_FT_NOT_SERVED", ftName ) );
        }

        FeatureCollection fc = sf.ftToFeatures.get( ft );
        int deleted = 0;
        if ( fc != null ) {
            try {
                FeatureCollection delete = fc.getMembers( filter, sf.evaluator );

                // check if all can be deleted
                for ( Feature feature : delete ) {
                    if ( !fs.lockManager.isFeatureModifiable( feature.getId(), lockId ) ) {
                        if ( lockId == null ) {
                            throw new MissingParameterException( getMessage( "TA_DELETE_LOCKED_NO_LOCK_ID",
                                                                             feature.getId() ), "lockId" );
                        }
                        throw new InvalidParameterValueException( getMessage( "TA_DELETE_LOCKED_WRONG_LOCK_ID",
                                                                              feature.getId() ), "lockId" );
                    }
                }

                deleted = delete.size();
                for ( Feature feature : delete ) {
                    fc.remove( feature );
                    if ( lock != null ) {
                        lock.release( feature.getId() );
                    }
                }
            } catch ( FilterEvaluationException e ) {
                throw new FeatureStoreException( e.getMessage(), e );
            }
        }
        return deleted;
    }

    @Override
    public int performDelete( IdFilter filter, Lock lock )
                            throws FeatureStoreException {

        String lockId = lock != null ? lock.getId() : null;

        // check if all features can be deleted
        for ( String id : filter.getMatchingIds() ) {
            if ( !fs.lockManager.isFeatureModifiable( id, lockId ) ) {
                if ( lockId == null ) {
                    throw new MissingParameterException( getMessage( "TA_DELETE_LOCKED_NO_LOCK_ID", id ), "lockId" );
                }
                throw new InvalidParameterValueException( getMessage( "TA_DELETE_LOCKED_WRONG_LOCK_ID", id ), "lockId" );
            }
        }

        for ( String id : filter.getMatchingIds() ) {
            GMLObject obj = sf.idToObject.get( id );
            if ( obj != null ) {
                if ( obj instanceof Feature ) {
                    Feature f = (Feature) obj;
                    FeatureType ft = f.getType();
                    sf.ftToFeatures.get( ft ).remove( f );
                }
            }
            if ( lock != null ) {
                lock.release( id );
            }
        }
        return filter.getMatchingIds().size();
    }

    @Override
    public List<String> performInsert( FeatureCollection fc, IDGenMode mode )
                            throws FeatureStoreException {

        if ( fs.getStorageSRS() != null ) {
            LOG.debug( "Transforming incoming feature collection to '" + fs.getStorageSRS() + "'" );
            try {
                fc = transformGeometries( fc );
            } catch ( Exception e ) {
                e.printStackTrace();
                String msg = "Unable to transform geometries: " + e.getMessage();
                throw new FeatureStoreException( msg );
            }
            LOG.debug( "Done." );
        }

        Set<Geometry> geometries = new HashSet<Geometry>();
        Set<Feature> features = new HashSet<Feature>();
        Set<String> fids = new LinkedHashSet<String>();
        Set<String> gids = new HashSet<String>();

        long begin = System.currentTimeMillis();
        findFeaturesAndGeometries( fc, geometries, features, fids, gids );
        long elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Finding features and geometries took {} [ms]", elapsed );

        begin = System.currentTimeMillis();
        switch ( mode ) {
        case GENERATE_NEW: {
            // TODO don't alter incoming features / geometries
            for ( Feature feature : features ) {
                String newFid = "FEATURE_" + generateNewId();
                String oldFid = feature.getId();
                if ( oldFid != null ) {
                    fids.remove( oldFid );
                }
                fids.add( newFid );
                feature.setId( newFid );
            }

            for ( Geometry geometry : geometries ) {
                String newGid = "GEOMETRY_" + generateNewId();
                String oldGid = geometry.getId();
                if ( oldGid != null ) {
                    gids.remove( oldGid );
                }
                gids.add( newGid );
                geometry.setId( newGid );
            }
            break;
        }
        case REPLACE_DUPLICATE: {
            throw new FeatureStoreException( "REPLACE_DUPLICATE is not available yet." );
        }
        case USE_EXISTING: {
            // TODO don't change incoming features / geometries
            for ( Feature feature : features ) {
                if ( feature.getId() == null ) {
                    String newFid = "FEATURE_" + generateNewId();
                    feature.setId( newFid );
                    fids.add( newFid );
                }
            }

            for ( Geometry geometry : geometries ) {
                if ( geometry.getId() == null ) {
                    String newGid = "GEOMETRY_" + generateNewId();
                    geometry.setId( newGid );
                    gids.add( newGid );
                }
            }
            break;
        }
        }
        elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Id generation took {} [ms]", elapsed );

        // check if any of the features / geometries to be inserted already exists in the store
        begin = System.currentTimeMillis();
        for ( String fid : fids ) {
            if ( sf.getObjectById( fid ) != null ) {
                String msg = "Cannot insert feature '" + fid + "'. This feature already exists in the feature store.";
                throw new FeatureStoreException( msg );
            }
        }
        elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Checking for existing features took {} [ms]", elapsed );

        begin = System.currentTimeMillis();
        for ( String gid : gids ) {
            if ( sf.getObjectById( gid ) != null ) {
                String msg = "Cannot insert geometry '" + gid + "'. This geometry already exists in the feature store.";
                throw new FeatureStoreException( msg );
            }
        }
        elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Checking for existing geometries took {} [ms]", elapsed );

        begin = System.currentTimeMillis();
        sf.addFeatures( features );
        elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Adding of features took {} [ms]", elapsed );
        return new ArrayList<String>( fids );
    }

    private FeatureCollection transformGeometries( FeatureCollection fc )
                            throws IllegalArgumentException, UnknownCRSException, TransformationException {

        FeatureCollection transformedFc = new GenericFeatureCollection();
        GeometryTransformer transformer = new GeometryTransformer( fs.getStorageSRS() );
        for ( Feature feature : fc ) {
            transformedFc.add( transformGeometries( feature, transformer ) );
        }
        return transformedFc;
    }

    private Feature transformGeometries( Feature feature, GeometryTransformer transformer )
                            throws IllegalArgumentException, TransformationException, UnknownCRSException {

        // TODO Do not modify the incoming feature, but create a new one.
        for ( Property prop : feature.getProperties() ) {
            TypedObjectNode value = prop.getValue();
            if ( value != null ) {
                PropertyType pt = prop.getType();
                if ( pt instanceof GeometryPropertyType ) {
                    Geometry transformed = transformGeometry( (Geometry) value, transformer );
                    prop.setValue( transformed );
                } else if ( pt instanceof CustomPropertyType ) {
                    TypedObjectNode transformed = transformGeometries( value, transformer );
                    prop.setValue( transformed );
                }
            }
        }
        feature.getGMLProperties().setBoundedBy( null );
        return feature;
    }

    private TypedObjectNode transformGeometries( TypedObjectNode value, GeometryTransformer transformer )
                            throws IllegalArgumentException, TransformationException, UnknownCRSException {
        if ( value instanceof GenericXMLElementContent ) {
            GenericXMLElementContent generic = (GenericXMLElementContent) value;
            List<TypedObjectNode> newChildren = new ArrayList<TypedObjectNode>( generic.getChildren().size() );
            for ( int i = 0; i < generic.getChildren().size(); i++ ) {
                TypedObjectNode child = generic.getChildren().get( i );
                TypedObjectNode transformed = transformGeometries( child, transformer );
                newChildren.add( transformed );
            }
            generic.setChildren( newChildren );
        } else if ( value instanceof Geometry ) {
            value = transformGeometry( (Geometry) value, transformer );
        }
        return value;
    }

    private Geometry transformGeometry( Geometry value, GeometryTransformer transformer )
                            throws IllegalArgumentException, TransformationException, UnknownCRSException {

        Geometry transformed = value;
        if ( transformed.getCoordinateSystem() == null ) {
            transformed.setCoordinateSystem( transformer.getWrappedTargetCRS() );
        } else {
            transformed = linearizer.linearize( value, crit );
            if ( !( transformed instanceof Point && transformed.getCoordinateDimension() == 1 ) ) {
                transformed = transformer.transform( transformed, transformed.getCoordinateSystem().getWrappedCRS() );
            }
        }
        return transformed;
    }

    private String generateNewId() {
        return UUID.randomUUID().toString();
    }

    private void findFeaturesAndGeometries( Feature feature, Set<Geometry> geometries, Set<Feature> features,
                                            Set<String> fids, Set<String> gids ) {

        if ( !features.contains( feature ) ) {
            if ( feature instanceof FeatureCollection ) {
                for ( Feature member : (FeatureCollection) feature ) {
                    findFeaturesAndGeometries( member, geometries, features, fids, gids );
                }
            } else {
                if ( feature.getId() == null || !( fids.contains( feature.getId() ) ) ) {
                    features.add( feature );
                    if ( feature.getId() != null ) {
                        fids.add( feature.getId() );
                    }
                }
                for ( Property property : feature.getProperties() ) {
                    Object propertyValue = property.getValue();
                    if ( propertyValue instanceof Feature ) {
                        if ( !( propertyValue instanceof FeatureReference )
                             || ( (FeatureReference) propertyValue ).isResolved() ) {
                            findFeaturesAndGeometries( (Feature) propertyValue, geometries, features, fids, gids );
                        }
                    } else if ( propertyValue instanceof Geometry ) {
                        Geometry geom = (Geometry) propertyValue;
                        if ( !( geom instanceof Point && geom.getCoordinateDimension() == 1 ) ) {
                            if ( !geom.getCoordinateSystem().equals( fs.getStorageSRS() ) ) {
                                System.out.println( "Feature " + feature.getId() );
                                System.out.println( "Property " + property.getName() );
                                System.out.println( "Geom " + geom );
                                System.out.println( "CRS " + geom.getCoordinateSystem() );
                                throw new RuntimeException( "Untransformed geometry!?" );
                            }
                        }
                        findGeometries( (Geometry) propertyValue, geometries, gids );
                    }
                }
            }
        }
    }

    private void findGeometries( Geometry geometry, Set<Geometry> geometries, Set<String> gids ) {
        if ( geometry.getId() == null || !( gids.contains( geometry.getId() ) ) ) {
            geometries.add( geometry );
            if ( geometry.getId() != null ) {
                gids.add( geometry.getId() );
            }
        }
    }

    @Override
    public int performUpdate( QName ftName, List<Property> replacementProps, Filter filter, Lock lock )
                            throws FeatureStoreException {

        String lockId = lock != null ? lock.getId() : null;

        FeatureType ft = fs.getSchema().getFeatureType( ftName );
        if ( ft == null ) {
            throw new FeatureStoreException( getMessage( "TA_OPERATION_FT_NOT_SERVED", ftName ) );
        }

        FeatureCollection fc = sf.ftToFeatures.get( ft );
        int updated = 0;
        if ( fc != null ) {
            try {
                FeatureCollection newFc = fc.getMembers( filter, sf.evaluator );

                // check if all features can be updated
                for ( Feature feature : newFc ) {
                    if ( !fs.lockManager.isFeatureModifiable( feature.getId(), lockId ) ) {
                        if ( lockId == null ) {
                            throw new MissingParameterException( getMessage( "TA_UPDATE_LOCKED_NO_LOCK_ID",
                                                                             feature.getId() ), "lockId" );
                        }
                        throw new InvalidParameterValueException( getMessage( "TA_UPDATE_LOCKED_WRONG_LOCK_ID",
                                                                              feature.getId() ), "lockId" );
                    }
                }

                updated = newFc.size();
                for ( Feature feature : newFc ) {
                    for ( Property prop : replacementProps ) {

                        if ( prop.getValue() instanceof Geometry ) {
                            Geometry geom = (Geometry) prop.getValue();
                            if ( geom != null ) {
                                // check compatibility (CRS) for geometry replacements (CITE
                                // wfs:wfs-1.1.0-Transaction-tc7.2)
                                if ( geom.getCoordinateDimension() != ( (Geometry) prop.getValue() ).getCoordinateDimension() ) {
                                    throw new InvalidParameterValueException(
                                                                              "Cannot replace given geometry property '"
                                                                                                      + prop.getType().getName()
                                                                                                      + "' with given value (wrong dimension)." );
                                }
                                // check compatibility (geometry type) for geometry replacements (CITE
                                // wfs:wfs-1.1.0-Transaction-tc10.1)
                                if ( !( geom instanceof Surface )
                                     && prop.getName().equals(
                                                               new QName( "http://cite.opengeospatial.org/gmlsf",
                                                                          "surfaceProperty" ) ) ) {
                                    throw new InvalidParameterValueException(
                                                                              "Cannot replace given geometry property '"
                                                                                                      + prop.getType().getName()
                                                                                                      + "' with given value (wrong type)." );
                                }
                            }
                        }
                        // TODO what about multi properties, strategy for proper handling of GML version
                        feature.setPropertyValue( prop.getType().getName(), 0, prop.getValue(), GMLVersion.GML_31 );
                    }
                    if ( lock != null ) {
                        lock.release( feature.getId() );
                    }
                }
            } catch ( FilterEvaluationException e ) {
                throw new FeatureStoreException( e.getMessage(), e );
            }
        }
        return updated;
    }

    @Override
    public void rollback()
                            throws FeatureStoreException {
        fs.releaseTransaction( this );
    }
}

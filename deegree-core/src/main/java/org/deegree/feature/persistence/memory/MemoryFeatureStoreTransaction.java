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
import java.util.List;
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
import org.deegree.gml.utils.GMLObjectVisitor;
import org.deegree.gml.utils.GMLObjectWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link FeatureStoreTransaction} implementation used by the {@link MemoryFeatureStore}.
 * 
 * @see MemoryFeatureStore
 * @see StoredFeatures
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
     * @param fs
     *            invoking feature store instance, never <code>null</code>
     */
    MemoryFeatureStoreTransaction( MemoryFeatureStore fs ) {
        this.fs = fs;
        this.sf = new StoredFeatures( fs.getSchema(), fs.getStorageSRS(), fs.storedFeatures );
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

        long begin = System.currentTimeMillis();
        if ( fs.getStorageSRS() != null ) {
            LOG.debug( "Transforming incoming feature collection to '" + fs.getStorageSRS() + "'" );
            try {
                fc = transformGeometries( fc );
            } catch ( Exception e ) {
                e.printStackTrace();
                String msg = "Unable to transform geometries: " + e.getMessage();
                throw new FeatureStoreException( msg );
            }
        } else {
            LOG.debug( "Checking CRS use in feature collection" );
            checkCRS( fc );
        }
        long elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Transforming / checking geometries took {} [ms]", elapsed );

        begin = System.currentTimeMillis();
        List<Feature> features = assignIds( fc, mode );
        elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Assigning ids / finding features and geometries took {} [ms]", elapsed );

        begin = System.currentTimeMillis();
        sf.addFeatures( features );
        elapsed = System.currentTimeMillis() - begin;
        LOG.debug( "Adding of features took {} [ms]", elapsed );

        List<String> fids = new ArrayList<String>( features.size() );
        for ( Feature f : features ) {
            fids.add( f.getId() );
        }
        return new ArrayList<String>( fids );
    }

    private void checkCRS( FeatureCollection fc )
                            throws FeatureStoreException {
        GMLObjectVisitor visitor = new GMLObjectVisitor() {
            @Override
            public boolean visitGeometry( Geometry geom ) {
                if ( geom.getCoordinateSystem() != null && geom.getCoordinateDimension() != 1 ) {
                    try {
                        geom.getCoordinateSystem().getWrappedCRS();
                    } catch ( Exception e ) {
                        throw new IllegalArgumentException( e.getMessage() );
                    }
                }
                return true;
            }

            @Override
            public boolean visitFeature( Feature feature ) {
                return true;
            }
        };
        try {
            new GMLObjectWalker( visitor ).traverse( fc );
        } catch ( IllegalArgumentException e ) {
            throw new FeatureStoreException( e.getMessage() );
        }
    }

    /**
     * Assigns an id to every {@link Feature} / {@link Geometry} in the given collection.
     * 
     * @param fc
     *            feature collection, must not be <code>null</code>
     * @param mode
     *            id generation mode, must not be <code>null</code>
     * @return list of all features (including nested features) from the input collection, with ids
     */
    private List<Feature> assignIds( final FeatureCollection fc, final IDGenMode mode )
                            throws FeatureStoreException {
        final List<Feature> features = new ArrayList<Feature>( fc.size() );
        GMLObjectVisitor visitor = new GMLObjectVisitor() {
            @SuppressWarnings("synthetic-access")
            @Override
            public boolean visitGeometry( Geometry geom ) {
                String id = getGeometryId( geom, mode );
                if ( sf.getObjectById( id ) != null ) {
                    String msg = "Cannot insert geometry '" + id
                                 + "'. This geometry already exists in the feature store.";
                    throw new IllegalArgumentException( msg );
                }
                geom.setId( id );
                return true;
            }

            @SuppressWarnings("synthetic-access")
            @Override
            public boolean visitFeature( Feature feature ) {
                String id = getFeatureId( feature, mode );
                if ( sf.getObjectById( id ) != null ) {
                    String msg = "Cannot insert feature '" + id
                                 + "'. This feature already exists in the feature store.";
                    throw new IllegalArgumentException( msg );
                }
                if ( feature != fc ) {
                    feature.setId( id );
                    features.add( feature );
                }
                return true;
            }
        };
        try {
            new GMLObjectWalker( visitor ).traverse( fc );
        } catch ( IllegalArgumentException e ) {
            throw new FeatureStoreException( e.getMessage() );
        }
        return features;
    }

    private String getFeatureId( Feature feature, IDGenMode mode ) {
        String fid = feature.getId();
        switch ( mode ) {
        case GENERATE_NEW: {
            fid = "FEATURE_" + generateNewId();
            break;
        }
        case REPLACE_DUPLICATE: {
            if ( fid == null || sf.getObjectById( fid ) != null ) {
                fid = "FEATURE_" + generateNewId();
            }
        }
        case USE_EXISTING: {
            if ( fid == null ) {
                fid = "FEATURE_" + generateNewId();
            }
            break;
        }
        }
        return fid;
    }

    private String getGeometryId( Geometry geometry, IDGenMode mode ) {
        String gid = geometry.getId();
        switch ( mode ) {
        case GENERATE_NEW: {
            gid = "GEOMETRY_" + generateNewId();
            break;
        }
        case REPLACE_DUPLICATE: {
            if ( gid == null || sf.getObjectById( gid ) != null ) {
                gid = "FEATURE_" + generateNewId();
            }
        }
        case USE_EXISTING: {
            if ( gid == null ) {
                gid = "GEOMETRY_" + generateNewId();
            }
            break;
        }
        }
        return gid;
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
                FeatureCollection update = fc.getMembers( filter, sf.evaluator );

                // check if all features can be updated
                for ( Feature feature : update ) {
                    if ( !fs.lockManager.isFeatureModifiable( feature.getId(), lockId ) ) {
                        if ( lockId == null ) {
                            throw new MissingParameterException( getMessage( "TA_UPDATE_LOCKED_NO_LOCK_ID",
                                                                             feature.getId() ), "lockId" );
                        }
                        throw new InvalidParameterValueException( getMessage( "TA_UPDATE_LOCKED_WRONG_LOCK_ID",
                                                                              feature.getId() ), "lockId" );
                    }
                }

                updated = update.size();
                for ( Feature feature : update ) {
                    for ( Property replacement : replacementProps ) {
                        if ( replacement.getValue() instanceof Geometry ) {
                            Geometry geom = (Geometry) replacement.getValue();
                            if ( geom != null ) {
                                Property current = feature.getProperty( replacement.getType().getName() );
                                Geometry currentGeom = current != null ? ( (Geometry) current.getValue() ) : null;
                                // check compatibility (CRS) for geometry replacements (CITE
                                // wfs:wfs-1.1.0-Transaction-tc7.2)
                                if ( currentGeom.getCoordinateDimension() != geom.getCoordinateDimension() ) {
                                    throw new InvalidParameterValueException(
                                                                              "Cannot replace given geometry property '"
                                                                                                      + replacement.getType().getName()
                                                                                                      + "' with given value (wrong dimension)." );
                                }
                                // check compatibility (geometry type) for geometry replacements (CITE
                                // wfs:wfs-1.1.0-Transaction-tc10.1)
                                if ( !( geom instanceof Surface )
                                     && replacement.getName().equals( new QName(
                                                                                 "http://cite.opengeospatial.org/gmlsf",
                                                                                 "surfaceProperty" ) ) ) {
                                    throw new InvalidParameterValueException(
                                                                              "Cannot replace given geometry property '"
                                                                                                      + replacement.getType().getName()
                                                                                                      + "' with given value (wrong type)." );
                                }
                            }
                        }
                        // TODO what about multi properties, strategy for proper handling of GML version
                        feature.setPropertyValue( replacement.getType().getName(), 0, replacement.getValue(),
                                                  GMLVersion.GML_31 );
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
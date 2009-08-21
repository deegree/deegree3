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

package org.deegree.feature.persistence.gml;

import static org.deegree.feature.i18n.Messages.getMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.Property;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.IDGenMode;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.geometry.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class GMLMemoryStoreTransaction implements FeatureStoreTransaction {

    private static final Logger LOG = LoggerFactory.getLogger( GMLMemoryStoreTransaction.class );

    private GMLMemoryStore store;

    GMLMemoryStoreTransaction( GMLMemoryStore store ) {
        this.store = store;
    }

    @Override
    public void commit()
                            throws FeatureStoreException {
        store.releaseTransaction( this );
    }

    @Override
    public FeatureStore getStore() {
        return store;
    }

    @Override
    public int performDelete( QName ftName, OperatorFilter filter, String lockId )
                            throws FeatureStoreException {

        FeatureType ft = store.getSchema().getFeatureType( ftName );
        if ( ft == null ) {
            throw new FeatureStoreException( getMessage( "TA_OPERATION_FT_NOT_SERVED", ftName ) );
        }
        FeatureCollection fc = store.getCollection( ft );
        int deleted = 0;
        if ( fc != null ) {
            try {
                FeatureCollection newFc = fc.getMembers( filter );
                deleted = newFc.size();
                for ( Feature feature : newFc ) {
                    store.removeObject( feature.getId() );
                }
            } catch ( FilterEvaluationException e ) {
                throw new FeatureStoreException( e.getMessage(), e );
            }
        }
        return deleted;
    }

    @Override
    public int performDelete( IdFilter filter, String lockId )
                            throws FeatureStoreException {

        for ( String id : filter.getMatchingIds() ) {
            store.removeObject( id );
        }
        return filter.getMatchingIds().size();
    }

    @Override
    public List<String> performInsert( FeatureCollection fc, IDGenMode mode )
                            throws FeatureStoreException {

        Set<Geometry> geometries = new HashSet<Geometry>();
        Set<Feature> features = new HashSet<Feature>();
        Set<String> fids = new LinkedHashSet<String>();
        Set<String> gids = new LinkedHashSet<String>();
        findFeaturesAndGeometries( fc, geometries, features, fids, gids );

        switch ( mode ) {
        case GENERATE_NEW: {
            // TODO don't change incoming features / geometries
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

        // check if any of the features / geometries to be inserted already exists in the store
        for ( String fid : fids ) {
            if ( store.getObjectById( fid ) != null ) {
                String msg = "Cannot insert feature '" + fid + "'. This feature already exists in the feature store.";
                throw new FeatureStoreException( msg );
            }
        }
        for ( String gid : gids ) {
            if ( store.getObjectById( gid ) != null ) {
                String msg = "Cannot insert geometry '" + gid + "'. This geometry already exists in the feature store.";
                throw new FeatureStoreException( msg );
            }
        }

        store.addFeatures( features );
        store.addGeometriesWithId( geometries );

        return new ArrayList<String>( fids );
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
                for ( Property<?> property : feature.getProperties() ) {
                    Object propertyValue = property.getValue();
                    if ( propertyValue instanceof Feature ) {
                        findFeaturesAndGeometries( (Feature) propertyValue, geometries, features, fids, gids );
                    } else if ( propertyValue instanceof Geometry ) {
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
    public int performUpdate( QName ftName, Map<PropertyType, Object> replacementProps, Filter filter, String lockId )
                            throws FeatureStoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rollback()
                            throws FeatureStoreException {
        store.releaseTransaction( this );
        String msg = "Cannot recover pre-transaction state (not supported by this feature store). Feature store may be inconsistent!";
        throw new FeatureStoreException( msg );
    }
}

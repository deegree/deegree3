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

import static org.deegree.feature.persistence.IDGenMode.USE_EXISTING;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.IDGenMode;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.filter.Filter;
import org.deegree.filter.IdFilter;
import org.deegree.filter.OperatorFilter;
import org.deegree.geometry.Geometry;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
class GMLMemoryStoreTransaction implements FeatureStoreTransaction {

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
        throw new UnsupportedOperationException();
    }

    @Override
    public int performDelete( IdFilter filter, String lockId )
                            throws FeatureStoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> performInsert( FeatureCollection fc, IDGenMode mode )
                            throws FeatureStoreException {

        if ( mode != USE_EXISTING ) {
            throw new FeatureStoreException( "Only USE_EXISTING is currently implemented." );
        }

        Set<Geometry> geometries = new HashSet<Geometry>();
        Set<Feature> features = new HashSet<Feature>();

        findFeaturesAndGeometries( fc, geometries, features );
        
        store.addFeatures( features );
        store.addGeometriesWithId( geometries );

        return new ArrayList<String>();
    }

    private void findFeaturesAndGeometries( Feature feature, Set<Geometry> geometries, Set<Feature> features ) {

        if ( !features.contains( feature ) ) {
            if ( feature instanceof FeatureCollection ) {
                for ( Feature member : (FeatureCollection) feature ) {
                    findFeaturesAndGeometries( member, geometries, features );
                }
            } else {
                features.add( feature );
                for ( Object propertyValue : feature.getProperties() ) {
                    if ( propertyValue instanceof Feature ) {
                        findFeaturesAndGeometries( (Feature) propertyValue, geometries, features );
                    } else if ( propertyValue instanceof Geometry ) {
                        findGeometries( (Geometry) propertyValue, geometries );
                    }
                }
            }
        }
    }

    private void findGeometries( Geometry geometry, Set<Geometry> geometries ) {
        geometries.add( geometry );
        // TODO traverse further
    }

    @Override
    public int performUpdate( QName ftName, Map<PropertyType, Object> replacementProps, Filter filter, String lockId )
                            throws FeatureStoreException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rollback()
                            throws FeatureStoreException {
        throw new UnsupportedOperationException();
    }
}

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
package org.deegree.feature.persistence.osm;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Envelope;
import org.deegree.gml.GMLObject;
import org.slf4j.Logger;

public class OSMFeatureStore implements FeatureStore {

    private static final Logger LOG = getLogger( OSMFeatureStore.class );

    @Override
    public FeatureStoreTransaction acquireTransaction()
                            throws FeatureStoreException {
        throw new FeatureStoreException( "OSMFeatureStore does not support transactions." );
    }

    @Override
    public void destroy() {
        // nothing to do
    }

    @Override
    public Envelope getEnvelope( QName ftName )
                            throws FeatureStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LockManager getLockManager()
                            throws FeatureStoreException {
        throw new FeatureStoreException( "OSMFeatureStore does not support locking." );
    }

    @Override
    public GMLObject getObjectById( String id )
                            throws FeatureStoreException {
        throw new FeatureStoreException( "OSMFeatureStore#getObjectById(String) is not implemented yet." );
    }

    @Override
    public ApplicationSchema getSchema() {
        QName ftName = new QName( "http://www.deegree.org/osm", "OSMFeature", "osm" );
        List<PropertyType> pts = new ArrayList<PropertyType>();
        QName propName = new QName( "http://www.deegree.org/osm", "myprop", "osm" );
        pts.add( new SimplePropertyType( propName, 1, 1, PrimitiveType.STRING, false, false, null ) );
        FeatureType ft = new GenericFeatureType( ftName, pts, false );
        Map<String, String> prefixToNS = new HashMap<String, String>();
        prefixToNS.put( "osm", "http://www.deegree.org/osm" );
        return new ApplicationSchema( new FeatureType[] { ft }, null, prefixToNS, null );
    }

    @Override
    public void init()
                            throws FeatureStoreException {
        LOG.info( "Initializing OSMFeatureStore" );
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public FeatureResultSet query( Query query )
                            throws FeatureStoreException, FilterEvaluationException {
        throw new FeatureStoreException( "OSMFeatureStore#query(Query) is not implemented yet." );
    }

    @Override
    public FeatureResultSet query( Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        throw new FeatureStoreException( "OSMFeatureStore#query(Query[]) is not implemented yet." );
    }

    @Override
    public int queryHits( Query query )
                            throws FeatureStoreException, FilterEvaluationException {
        throw new FeatureStoreException( "OSMFeatureStore#queryHits(Query) is not implemented yet." );
    }

    @Override
    public int queryHits( Query[] queries )
                            throws FeatureStoreException, FilterEvaluationException {
        throw new FeatureStoreException( "OSMFeatureStore#queryHits(Query[]) is not implemented yet." );
    }
}
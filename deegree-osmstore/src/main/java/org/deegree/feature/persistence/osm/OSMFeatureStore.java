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

import static org.deegree.commons.tom.primitive.PrimitiveType.STRING;
import static org.deegree.cs.CRS.EPSG_4326;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.POINT;
import static org.deegree.feature.types.property.ValueRepresentation.BOTH;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.deegree.cs.CRS;
import org.deegree.feature.Feature;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.gml.GMLObject;
import org.slf4j.Logger;

public class OSMFeatureStore implements FeatureStore {

    private static final Logger LOG = getLogger( OSMFeatureStore.class );

    private static final String OSM_NS = "http://www.deegree.org/osm";

    private static FeatureType nodeFt;

    private static FeatureType wayFt;

    private static FeatureType relationFt;

    private static GeometryPropertyType nodeGeomPt;

    static {
        QName ftName = new QName( OSM_NS, "node", "osm" );
        List<PropertyType> pts = new ArrayList<PropertyType>();
        nodeGeomPt = new GeometryPropertyType( new QName( OSM_NS, "geometry", "osm" ), 1, 1, false, false, null, POINT,
                                               DIM_2, BOTH );
        pts.add( nodeGeomPt );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "highway", "osm" ), 0, 1, STRING, false, false, null ) );
        nodeFt = new GenericFeatureType( ftName, pts, false );

        // TODO create feature types
    }

    OSMFeatureStore( ApplicationSchema schema, CRS storageCRS ) throws FeatureStoreException, FileNotFoundException,
                            XMLStreamException, IOException {

        // OSMReader.getBounds();

        GeometryFactory geomFac = new GeometryFactory();

        String fid = null;
        List<Property> props = new ArrayList<Property>();
        Geometry geom = geomFac.createPoint( null, x, y, EPSG_4326 );
        props.add( new GenericProperty( nodeGeomPt, nodeGeomPt.getName(), geom ) );
        Feature node = nodeFt.newFeature( fid, props, null );

        // OSMReader.getNodes();
        // OSMReader.getWays();
        // OSMReader.getRelations();

    }

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
        return new ApplicationSchema( new FeatureType[] { nodeFt, relationFt, wayFt }, null, null, null );
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
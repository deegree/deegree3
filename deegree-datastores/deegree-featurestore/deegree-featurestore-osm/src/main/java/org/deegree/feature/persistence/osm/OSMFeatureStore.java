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
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.LINE_STRING;
import static org.deegree.feature.types.property.ValueRepresentation.BOTH;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.FeatureStoreException;
import org.deegree.feature.persistence.FeatureStoreTransaction;
import org.deegree.feature.persistence.lock.LockManager;
import org.deegree.feature.persistence.query.FeatureResultSet;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.geometry.Envelope;
import org.deegree.gml.GMLObject;
import org.slf4j.Logger;

public class OSMFeatureStore implements FeatureStore {

    private static final Logger LOG = getLogger( OSMFeatureStore.class );

    private static final String OSM_NS = "http://www.deegree.org/osm";

    public FeatureType nodeFt;

    public FeatureType wayFt;

    public FeatureType relationFt;

    public GeometryPropertyType nodeGeomPt, wayGeomPt;

    // TODO create feature types

    OSMFeatureStore() throws FileNotFoundException, XMLStreamException {

        QName ftNameWay = new QName( OSM_NS, "Way", "osm" );
        List<PropertyType> ptsway = new ArrayList<PropertyType>();
        wayGeomPt = new GeometryPropertyType( new QName( OSM_NS, "geometry", "osm" ), 1, 1, null, null, LINE_STRING,
                                              DIM_2, BOTH );
        ptsway.add( wayGeomPt );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "highway", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "amenity", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "name", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "abutters", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "aerialway", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "aeroway", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "boundary", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "bridge", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "building", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "construction", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "cutting", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "cycleway", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "disused", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "embankment", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "enforcement", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "generator:force", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "healthcare", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "historic", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "landuse", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "leisure", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "lit", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "man_made", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "military", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "natural", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "place", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "power", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "proposed", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "railway", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "route", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "shop", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "smoothness", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "source", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "sport", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "start_date", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "surface", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "tourism", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "tracktype", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "type", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "waterway", "osm" ), 0, 1, STRING, null, null ) );
        ptsway.add( new SimplePropertyType( new QName( OSM_NS, "wikipedia", "osm" ), 0, 1, STRING, null, null ) );

        wayFt = new GenericFeatureType( ftNameWay, ptsway, false );

        QName ftNameRelation = new QName( OSM_NS, "Relation", "osm" );
        List<PropertyType> ptsrelation = new ArrayList<PropertyType>();

        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "member", "osm" ), 1, 1, STRING, null, null ) );

        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "highway", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "amenity", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "name", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "abutters", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "aerialway", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "aeroway", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "boundary", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "bridge", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "building", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "construction", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "cutting", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "cycleway", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "disused", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "embankment", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "enforcement", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "generator:force", "osm" ), 0, 1, STRING, null,
                                                 null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "healthcare", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "historic", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "landuse", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "leisure", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "lit", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "man_made", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "military", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "natural", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "place", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "power", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "proposed", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "railway", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "route", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "shop", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "smoothness", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "source", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "sport", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "start_date", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "surface", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "tourism", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "tracktype", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "type", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "waterway", "osm" ), 0, 1, STRING, null, null ) );
        ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "wikipedia", "osm" ), 0, 1, STRING, null, null ) );

        relationFt = new GenericFeatureType( ftNameRelation, ptsrelation, false );

        OSMToFeature getOSM = new OSMToFeature();
        nodeFt = getOSM.nodeFt;
        getOSM.getNodes();

        /*
         * GeometryFactory geomFac = new GeometryFactory();
         * 
         * String fid = null; List<Property> props = new ArrayList<Property>(); Geometry geom = geomFac.createPoint(
         * null, x, y, EPSG_4326 ); props.add( new GenericProperty( nodeGeomPt, nodeGeomPt.getName(), geom ) ); Feature
         * node = nodeFt.newFeature( fid, props, null );
         */

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
    public void init( DeegreeWorkspace workspace ) {
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
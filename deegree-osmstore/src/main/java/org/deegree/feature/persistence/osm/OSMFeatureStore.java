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
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.LINE_STRING;
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
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
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

    private static GeometryPropertyType nodeGeomPt , wayGeomPt;

    
        
        // TODO create feature types
  

      OSMFeatureStore() throws FileNotFoundException, XMLStreamException {
      
          QName ftName = new QName( OSM_NS, "Node", "osm" );
          List<PropertyType> pts = new ArrayList<PropertyType>();
          nodeGeomPt = new GeometryPropertyType( new QName( OSM_NS, "geometry", "osm" ), 1, 1, false, false, null, POINT,
                                                 DIM_2, BOTH );
          pts.add( nodeGeomPt );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "highway", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "amenity", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "name", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "abutters", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "aerialway", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "aeroway", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "boundary", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "bridge", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "building", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "construction", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "cutting", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "cycleway", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "disused", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "embankment", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "enforcement", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "generator:force", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "healthcare", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "historic", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "landuse", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "leisure", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "lit", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "man_made", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "military", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "natural", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "place", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "power", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "proposed", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "railway", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "route", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "shop", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "smoothness", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "source", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "sport", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "start_date", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "surface", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "tourism", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "tracktype", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "type", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "waterway", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "wikipedia", "osm" ), 0, 1, STRING, false, false, null ) );
          
        /*  pts.add( new SimplePropertyType( new QName( OSM_NS, "cables", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "wires", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "voltage", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "tunnel", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "office", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "craft", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "emergency", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "geological", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "border_type", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "fenced", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "internet_access", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "area", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "motorroad", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "crossing", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "mountain_pass", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "lanes", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "layer", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "ele", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "width", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "est_width", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "incline", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "end_date", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "operator", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "opening_hours", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "wheelchair", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "tactile_paving", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "narrow", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "sac_scale", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "trail_visibility", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "mtb:scale", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "mtb:scale:uphill", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "mtb:description", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "TMC:LocationCode", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "wood", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "admin_level", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "covered", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "drive_thru", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "ford", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "access", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "vehicle", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "bicycle", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "foot", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "goods", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "hgv", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "access:lhv", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "access:bdouble", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "access:roadtrain", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "hazmat", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "agricultural", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "horse", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "motorcycle", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "atv", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "motorcar", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "motor_vehicle", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "psv", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "4wd_only", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "motorboat", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "boat", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "oneway", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "noexit", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "date_on", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "date_off", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "day_on", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "day_off", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "hour_on", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "hour_off", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "maxweight", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "maxheight", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "maxwidth", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "maxlength", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "maxspeed", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "minspeed", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "traffic_sign", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "maxstay", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "toll", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "charge", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "alt_name", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "official_name", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "int_name", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "nat_name", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "reg_name", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "loc_name", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "old_name", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "ref", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "int_ref", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "nat_ref", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "reg_ref", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "loc_ref", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "old_ref", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "ncn_ref", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "rcn_ref", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "lcn_ref", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "source_ref", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "icao", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "iata", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "place_name", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "place_number", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "postal_code", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "is_in", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "population", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "addr:housenumber", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "addr:housename", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "addr:street", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "addr:postcode", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "addr:city", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "addr:country", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "addr:hamlet", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "addr:subdistrict", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "addr:district", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "addr:province", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "addr:interpolation", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "addr:inclusion", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "note", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "fixme", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "description", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "image", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "source:name", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "source:ref", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "attribution", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "phone", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "fax", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "email", "osm" ), 0, 1, STRING, false, false, null ) );
          pts.add( new SimplePropertyType( new QName( OSM_NS, "website", "osm" ), 0, 1, STRING, false, false, null ) );
          */
          nodeFt = new GenericFeatureType( ftName, pts, false );
          
          QName ftNameWay = new QName( OSM_NS, "Way", "osm" );
          List<PropertyType> ptsway = new ArrayList<PropertyType>();
          wayGeomPt = new GeometryPropertyType( new QName( OSM_NS, "geometry", "osm" ), 1, 1, false, false, null, LINE_STRING ,
                                                 DIM_2, BOTH );
          ptsway.add( wayGeomPt );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "highway", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "amenity", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "name", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "abutters", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "aerialway", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "aeroway", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "boundary", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "bridge", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "building", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "construction", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "cutting", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "cycleway", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "disused", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "embankment", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "enforcement", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "generator:force", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "healthcare", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "historic", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "landuse", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "leisure", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "lit", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "man_made", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "military", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "natural", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "place", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "power", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "proposed", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "railway", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "route", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "shop", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "smoothness", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "source", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "sport", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "start_date", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "surface", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "tourism", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "tracktype", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "type", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "waterway", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsway.add( new SimplePropertyType( new QName( OSM_NS, "wikipedia", "osm" ), 0, 1, STRING, false, false, null ) );
         
          wayFt = new GenericFeatureType( ftNameWay, pts, false );
          
          QName ftNameRelation = new QName( OSM_NS, "Relation", "osm" );
          List<PropertyType> ptsrelation = new ArrayList<PropertyType>();
          
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "member", "osm"),  1, 1, STRING, false, false, null) );
          
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "highway", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "amenity", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "name", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "abutters", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "aerialway", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "aeroway", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "boundary", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "bridge", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "building", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "construction", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "cutting", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "cycleway", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "disused", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "embankment", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "enforcement", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "generator:force", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "healthcare", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "historic", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "landuse", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "leisure", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "lit", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "man_made", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "military", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "natural", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "place", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "power", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "proposed", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "railway", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "route", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "shop", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "smoothness", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "source", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "sport", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "start_date", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "surface", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "tourism", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "tracktype", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "type", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "waterway", "osm" ), 0, 1, STRING, false, false, null ) );
          ptsrelation.add( new SimplePropertyType( new QName( OSM_NS, "wikipedia", "osm" ), 0, 1, STRING, false, false, null ) );
         
          relationFt = new GenericFeatureType( ftNameRelation, pts, false );
          
          
      OSMToFeature getOSM = new OSMToFeature();
      
      getOSM.getNodes();
      
          /*
        GeometryFactory geomFac = new GeometryFactory();

        String fid = null;
        List<Property> props = new ArrayList<Property>();
        Geometry geom = geomFac.createPoint( null, x, y, EPSG_4326 );
        props.add( new GenericProperty( nodeGeomPt, nodeGeomPt.getName(), geom ) );
        Feature node = nodeFt.newFeature( fid, props, null ); */

        
        
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
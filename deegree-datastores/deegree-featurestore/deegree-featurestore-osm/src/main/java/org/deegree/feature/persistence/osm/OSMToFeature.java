package org.deegree.feature.persistence.osm;

import static org.deegree.commons.tom.primitive.PrimitiveType.STRING;
import static org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension.DIM_2;
import static org.deegree.feature.types.property.GeometryPropertyType.GeometryType.POINT;
import static org.deegree.feature.types.property.ValueRepresentation.BOTH;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.cs.CRSUtils;
import org.deegree.feature.Feature;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.GenericFeatureCollection;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.GenericFeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.points.Points;
import org.deegree.geometry.primitive.Point;
import org.deegree.geometry.standard.points.PointsList;

public class OSMToFeature {

    String ndref = "";

    String noderef = "";

    String wayref = "";

    String relationref = "";

    String firstndref = "";

    private static final String OSM_NS = "http://www.deegree.org/osm";

    private ArrayList<Point> nds = new ArrayList<Point>();

    public FeatureType nodeFt;

    private static FeatureType wayFt;

    private static FeatureType relationFt;

    private static GeometryPropertyType nodeGeomPt;

    private static GeometryPropertyType wayGeomPt;

    List<Property> pointProps = new ArrayList<Property>();

    GeometryFactory geomFac = new GeometryFactory();

    OSMToFeature() throws FileNotFoundException, XMLStreamException {

        QName ftName = new QName( OSM_NS, "Node", "osm" );
        List<PropertyType> pts = new ArrayList<PropertyType>();
        nodeGeomPt = new GeometryPropertyType( new QName( OSM_NS, "geometry", "osm" ), 1, 1, null, null, POINT, DIM_2,
                                               BOTH );
        pts.add( nodeGeomPt );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "highway", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "amenity", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "name", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "abutters", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "aerialway", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "aeroway", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "boundary", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "bridge", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "building", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "construction", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "cutting", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "cycleway", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "disused", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "embankment", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "enforcement", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "generator:force", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "healthcare", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "historic", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "landuse", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "leisure", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "lit", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "man_made", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "military", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "natural", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "place", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "power", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "proposed", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "railway", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "route", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "shop", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "smoothness", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "source", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "sport", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "start_date", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "surface", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "tourism", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "tracktype", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "type", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "waterway", "osm" ), 0, 1, STRING, null, null ) );
        pts.add( new SimplePropertyType( new QName( OSM_NS, "wikipedia", "osm" ), 0, 1, STRING, null, null ) );

        /*
         * pts.add( new SimplePropertyType( new QName( OSM_NS, "cables", "osm" ), 0, 1, STRING, null, null ) ); pts.add(
         * new SimplePropertyType( new QName( OSM_NS, "wires", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "voltage", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "tunnel", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "office", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "craft", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "emergency", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "geological", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "border_type", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "fenced", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "internet_access", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "area", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "motorroad", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "crossing", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "mountain_pass", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "lanes", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "layer", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "ele", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "width", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "est_width", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "incline", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "end_date", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "operator", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "opening_hours", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "wheelchair", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "tactile_paving", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "narrow", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "sac_scale", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "trail_visibility", "osm" ), 0, 1, STRING, null, null ) ); pts.add(
         * new SimplePropertyType( new QName( OSM_NS, "mtb:scale", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "mtb:scale:uphill", "osm" ), 0, 1, STRING, null, null ) ); pts.add(
         * new SimplePropertyType( new QName( OSM_NS, "mtb:description", "osm" ), 0, 1, STRING, null, null ) ); pts.add(
         * new SimplePropertyType( new QName( OSM_NS, "TMC:LocationCode", "osm" ), 0, 1, STRING, null, null ) );
         * pts.add( new SimplePropertyType( new QName( OSM_NS, "wood", "osm" ), 0, 1, STRING, null, null ) ); pts.add(
         * new SimplePropertyType( new QName( OSM_NS, "admin_level", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "covered", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "drive_thru", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "ford", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "access", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "vehicle", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "bicycle", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "foot", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "goods", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "hgv", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "access:lhv", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "access:bdouble", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "access:roadtrain", "osm" ), 0, 1, STRING, null, null ) ); pts.add(
         * new SimplePropertyType( new QName( OSM_NS, "hazmat", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "agricultural", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "horse", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "motorcycle", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "atv", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "motorcar", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "motor_vehicle", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "psv", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "4wd_only", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "motorboat", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "boat", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "oneway", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "noexit", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "date_on", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "date_off", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "day_on", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "day_off", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "hour_on", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "hour_off", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "maxweight", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "maxheight", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "maxwidth", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "maxlength", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "maxspeed", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "minspeed", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "traffic_sign", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "maxstay", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "toll", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "charge", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "alt_name", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "official_name", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "int_name", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "nat_name", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "reg_name", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "loc_name", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "old_name", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "ref", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "int_ref", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "nat_ref", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "reg_ref", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "loc_ref", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "old_ref", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "ncn_ref", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "rcn_ref", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "lcn_ref", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "source_ref", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "icao", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "iata", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "place_name", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "place_number", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "postal_code", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "is_in", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "population", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "addr:housenumber", "osm" ), 0, 1, STRING, null, null ) ); pts.add(
         * new SimplePropertyType( new QName( OSM_NS, "addr:housename", "osm" ), 0, 1, STRING, null, null ) ); pts.add(
         * new SimplePropertyType( new QName( OSM_NS, "addr:street", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "addr:postcode", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "addr:city", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "addr:country", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "addr:hamlet", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "addr:subdistrict", "osm" ), 0, 1, STRING, null, null ) ); pts.add(
         * new SimplePropertyType( new QName( OSM_NS, "addr:district", "osm" ), 0, 1, STRING, null, null ) ); pts.add(
         * new SimplePropertyType( new QName( OSM_NS, "addr:province", "osm" ), 0, 1, STRING, null, null ) ); pts.add(
         * new SimplePropertyType( new QName( OSM_NS, "addr:interpolation", "osm" ), 0, 1, STRING, null, null ) );
         * pts.add( new SimplePropertyType( new QName( OSM_NS, "addr:inclusion", "osm" ), 0, 1, STRING, null, null ) );
         * pts.add( new SimplePropertyType( new QName( OSM_NS, "note", "osm" ), 0, 1, STRING, null, null ) ); pts.add(
         * new SimplePropertyType( new QName( OSM_NS, "fixme", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "description", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "image", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "source:name", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "source:ref", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "attribution", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "phone", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "fax", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "email", "osm" ), 0, 1, STRING, null, null ) ); pts.add( new
         * SimplePropertyType( new QName( OSM_NS, "website", "osm" ), 0, 1, STRING, null, null ) );
         */
        nodeFt = new GenericFeatureType( ftName, pts, false );

    }

    public FeatureCollection getNodes()
                            throws XMLStreamException, FileNotFoundException {

        InputStream in = new FileInputStream( "/home/goerke/Desktop/map.osm" );
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader parser = inputFactory.createXMLStreamReader( in );

        List<Feature> nodes = new ArrayList<Feature>();
        while ( parser.hasNext() ) {
            int event = parser.next();

            if ( event == XMLStreamReader.START_ELEMENT && "node".equals( parser.getLocalName() ) ) {

                noderef = parser.getAttributeValue( 0 ).toString();
                String nodeid, latstr, lonstr;
                double lat, lon;
                nodeid = "node" + parser.getAttributeValue( 0 ).toString();
                String fid = nodeid;
                latstr = parser.getAttributeValue( 1 ).toString();
                lonstr = parser.getAttributeValue( 2 ).toString();
                lat = Double.parseDouble( latstr );
                lon = Double.parseDouble( lonstr );

                List<Property> pointProps = new ArrayList<Property>();
                Geometry geom = geomFac.createPoint( null, lon, lat, CRSUtils.EPSG_4326 );
                pointProps.add( new GenericProperty( nodeGeomPt, nodeGeomPt.getName(), geom ) );
                parser.next();
                parser.next();
                parser.next();
                // this.getTags();
                Feature node = nodeFt.newFeature( fid, pointProps, null, null );
                nodes.add( node );
                pointProps.clear();

            }
        }
        return new GenericFeatureCollection( null, nodes );
    }

    public void getWays()
                            throws XMLStreamException, FileNotFoundException {

        InputStream in = new FileInputStream( "/home/goerke/Desktop/map.osm" );
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader parser = inputFactory.createXMLStreamReader( in );
        while ( parser.hasNext() ) {
            int event = parser.next();

            if ( event == XMLStreamReader.START_ELEMENT && "way".equals( parser.getLocalName() ) ) {
                wayref = parser.getAttributeValue( 0 ).toString();
                String wayid;
                wayid = "way" + parser.getAttributeValue( 0 ).toString();
                String fid = wayid;

                parser.nextTag();
                while ( event == XMLStreamReader.START_ELEMENT && "nd".equals( parser.getLocalName() ) ) {
                    ndref = parser.getAttributeValue( 0 );

                    this.getWayNodes();
                    parser.nextTag();
                    parser.nextTag();

                }
                List<Property> props = new ArrayList<Property>();
                Points points = new PointsList( nds );
                Geometry geom = geomFac.createLineString( null, CRSUtils.EPSG_4326, points );
                props.add( new GenericProperty( wayGeomPt, wayGeomPt.getName(), geom ) );
                nds.clear();
                this.getTags();

                Feature way = wayFt.newFeature( fid, props, null, null );

            }

        }

    }

    public void getWayNodes()
                            throws XMLStreamException, FileNotFoundException {

        InputStream in = new FileInputStream( "/home/goerke/Desktop/map.osm" );
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader parser = inputFactory.createXMLStreamReader( in );

        while ( parser.hasNext() ) {
            int event = parser.next();
            while ( event == XMLStreamReader.START_ELEMENT && "node".equals( parser.getLocalName() )
                    && ndref.equals( parser.getAttributeValue( 0 ) ) ) {
                String latstr, lonstr;
                double lat = 0, lon = 0;

                int countnd = 0;
                if ( event == XMLStreamReader.START_ELEMENT && "node".equals( parser.getLocalName() )
                     && ndref.equals( parser.getAttributeValue( 0 ) ) ) {
                    countnd++;
                    System.out.println( "BEGINLOCATION: " + parser.getLocation() );

                    for ( int i = 0; i < countnd; i++ ) {

                        latstr = parser.getAttributeValue( 1 ).toString();
                        lonstr = parser.getAttributeValue( 2 ).toString();
                        lat = Double.parseDouble( latstr );
                        lon = Double.parseDouble( lonstr );
                        Point point;
                        point = geomFac.createPoint( null, lon, lat, CRSUtils.EPSG_4326 );
                        nds.add( point );
                        System.out.println( "" );
                        System.out.println( "LOCATIONEND: " + parser.getLocation() );
                        parser.nextTag();
                        parser.nextTag();
                    }

                }

            }
        }
    }

    public void getRelations()
                            throws XMLStreamException, FileNotFoundException {
        InputStream in = new FileInputStream( "/home/goerke/Desktop/map.osm" );
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader parser = inputFactory.createXMLStreamReader( in );
        while ( parser.hasNext() ) {
            int event = parser.next();
            List<Property> props = new ArrayList<Property>();
            if ( event == XMLStreamReader.START_ELEMENT && "relation".equals( parser.getLocalName() ) ) {
                String relationid;
                relationref = parser.getAttributeValue( 0 ).toString();
                relationid = "relation" + parser.getAttributeValue( 0 ).toString();
                String fid = relationid;
                parser.nextTag();
                while ( event == XMLStreamReader.START_ELEMENT && "member".equals( parser.getLocalName() ) ) {
                    String ref = parser.getAttributeValue( 1 ).toString();
                    String type = parser.getAttributeValue( 0 ).toString();
                    QName member = new QName( "osm", "member", "http://www.deegree.org/osm" );

                    props.add( new GenericProperty( relationFt.getPropertyDeclaration( member ), member,
                                                    new PrimitiveValue( type + ref ) ) );
                    parser.nextTag();
                    parser.nextTag();

                }
                this.getTags();
                Feature relation = relationFt.newFeature( fid, props, null, null );

            }
        }
    }

    public void getTags()
                            throws XMLStreamException, FileNotFoundException {

        String[] tagNames = { "highway", "amenity", "name", "abutters", "aerialway", "aeroway", "boundary", "bridge",
                             "building", "construction", "cutting", "cycleway", "disused", "embankment", "enforcement",
                             "generator:force", "healthcare", "historic", "landuse", "leisure", "lit", "man_made",
                             "military", "natural", "place", "power", "proposed", "railway", "route", "shop",
                             "smoothness", "source", "sport", "start_date", "surface", "tourism", "tracktype", "type",
                             "waterway", "wikipedia"
        /*
         * , "cables", "wires", "voltage", "tunnel", "office", "craft", "emergency", "geological", "border_type",
         * "fenced", "internet_access", "area", "motorroad", "crossing", "mountain_pass", "lanes", "layer", "ele",
         * "width", "est_width", "incline", "end_date", "operator", "opening_hours", "wheelchair", "tactile_paving",
         * "narrow", "sac_scale", "trail_visibility", "mtb:scale", "mtb:scale:uphill", "mtb:description",
         * "TMC:LocationCode", "wood", "admin_level", "covered", "drive_thru", "ford", "access", "vehicle", "bicycle",
         * "foot", "goods", "hgv", "access:lhv", "access:bdouble", "access:roadtrain", "hazmat", "agricultural",
         * "horse", "motorcycle", "atv", "motorcar", "motor_vehicle", "psv", "4wd_only", "motorboat", "boat", "oneway",
         * "noexit", "date_on", "date_off", "day_on", "day_off", "hour_on", "hour_off", "maxweight", "maxheight",
         * "maxwidth", "maxlength", "maxspeed", "minspeed", "traffic_sign", "maxstay", "toll", "charge", "alt_name",
         * "official_name", "int_name", "nat_name", "reg_name", "loc_name", "old_name", "ref", "int_ref", "nat_ref",
         * "reg_ref", "loc_ref", "old_ref", "ncn_ref", "rcn_ref", "lcn_ref", "source_ref", "icao", "iata", "place_name",
         * "place_number", "postal_code", "is_in", "population", "addr:housenumber", "addr:housename", "addr:street",
         * "addr:postcode", "addr:city", "addr:country", "addr:hamlet", "addr:subdistrict", "addr:district",
         * "addr:province", "addr:interpolation", "addr:inclusion", "note", "fixme", "description", "image",
         * "source:name", "source:ref", "attribution", "phone", "fax", "email", "website"
         */

        };
        InputStream in = new FileInputStream( "/home/goerke/Desktop/map.osm" );
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader parser = inputFactory.createXMLStreamReader( in );
        while ( parser.hasNext() ) {
            int event = parser.next();

            if ( event == XMLStreamReader.START_ELEMENT && "node".equals( parser.getLocalName() )
                 && noderef.equals( parser.getAttributeValue( 0 ) ) ) {
                parser.nextTag();
                parser.nextTag();
                while ( event == XMLStreamReader.START_ELEMENT && "tag".equals( parser.getLocalName() ) ) {
                    String keystr, value;
                    keystr = parser.getAttributeValue( 0 ).toString();
                    for ( int index = 0; index < tagNames.length; index++ ) {
                        if ( keystr == tagNames[index] ) {
                            QName key = new QName( "osm", keystr, "http://www.deegree.org/osm" );
                            value = parser.getAttributeValue( 1 ).toString();
                            pointProps.add( new GenericProperty( nodeFt.getPropertyDeclaration( key ), key,
                                                                 new PrimitiveValue( value ) ) );

                        }
                    }

                    parser.nextTag();
                    parser.nextTag();

                }
            }
            while ( event == XMLStreamReader.START_ELEMENT && "relation".equals( parser.getLocalName() )
                    && relationref.equals( parser.getAttributeValue( 0 ) ) ) {
                parser.nextTag();
                int countref = 0;
                for ( int i = 0; i <= countref; i++ ) {
                    if ( event == XMLStreamReader.START_ELEMENT && "member".equals( parser.getLocalName() ) ) {
                        countref++;
                    } else {
                        break;
                    }
                    parser.nextTag();

                }

                while ( event == XMLStreamReader.START_ELEMENT && "tag".equals( parser.getLocalName() ) ) {
                    String keystr, value;
                    keystr = parser.getAttributeValue( 0 ).toString();
                    for ( int index = 0; index < tagNames.length; index++ ) {
                        if ( keystr == tagNames[index] ) {
                            QName key = new QName( "osm", keystr, "http://www.deegree.org/osm" );
                            value = parser.getAttributeValue( 1 ).toString();
                            List<Property> props = new ArrayList<Property>();
                            props.add( new GenericProperty( nodeFt.getPropertyDeclaration( key ), key,
                                                            new PrimitiveValue( value ) ) );

                        }
                    }

                    parser.nextTag();
                    parser.nextTag();

                }
            }
        }
    }
}

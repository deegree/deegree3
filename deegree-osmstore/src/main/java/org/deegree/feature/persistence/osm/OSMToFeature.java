package org.deegree.feature.persistence.osm;

import static org.deegree.cs.CRS.EPSG_4326;

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
import org.deegree.feature.Feature;
import org.deegree.feature.property.GenericProperty;
import org.deegree.feature.property.Property;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.GeometryFactory;

public class OSMToFeature {

    String ndref = "";

    String noderef = "";

    String wayref = "";

    String relationref = "";

    String firstndref = "";

    private static FeatureType nodeFt;

    private static FeatureType wayFt;

    private static FeatureType relationFt;

    private static GeometryPropertyType nodeGeomPt;

    GeometryFactory geomFac = new GeometryFactory();

    public Feature getNodes()
                            throws XMLStreamException, FileNotFoundException {

        InputStream in = new FileInputStream( "/home/goerke/Desktop/map.osm" );
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader parser = inputFactory.createXMLStreamReader( in );

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
                lon = Double.parseDouble( latstr );

                List<Property> props = new ArrayList<Property>();
                Geometry geom = geomFac.createPoint( null, lon, lat, EPSG_4326 );
                props.add( new GenericProperty( nodeGeomPt, nodeGeomPt.getName(), geom ) );
                parser.next();
                parser.next();
                parser.next();
                this.getTags();

                Feature node = nodeFt.newFeature( fid, props, null );
                return node;
            }
        }
        return null;
    }
    
    public void getWays() throws XMLStreamException, FileNotFoundException
    {
        
        InputStream in = new FileInputStream( "/home/goerke/Desktop/map.osm" );
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader parser = inputFactory.createXMLStreamReader( in );
        
        while ( parser.hasNext() ) {
             int event = parser.next();
             
             if ( event == XMLStreamReader.START_ELEMENT && "way".equals( parser.getLocalName())) {
                 wayref = parser.getAttributeValue( 0 ).toString();
                 String wayid;
                 wayid = "way" + parser.getAttributeValue( 0 ).toString();

                 
                 parser.nextTag();
                 //this.askLineOrPolygon();
                 while ( event == XMLStreamReader.START_ELEMENT && "nd".equals(parser.getLocalName()) )
                 {
                     ndref = parser.getAttributeValue( 0 );
                     this.getWayNodes();
                     parser.nextTag();
                     parser.nextTag();
                     
                 }
                 List<Property> props = new ArrayList<Property>();
                 Geometry geom = geomFac.createLineString( null, crs, points )
                 props.add( new GenericProperty( wayGeomPt, wayGeomPt.getName(), geom ) );
                 this.getTags();
                 
                 }
                 
            
             }
            
        
    }
    
    public void getWayNodes() throws XMLStreamException, FileNotFoundException{
        
        
        InputStream in = new FileInputStream( "/home/goerke/Desktop/map.osm" );
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader parser = inputFactory.createXMLStreamReader( in );
        
        while ( parser.hasNext() ) {
             int event = parser.next();
             while ( event == XMLStreamReader.START_ELEMENT && "node".equals( parser.getLocalName()) && ndref.equals(parser.getAttributeValue(0)) ) {
                    String lat, lon;
                    int countnd = 0;
                    if ( event == XMLStreamReader.START_ELEMENT && "node".equals( parser.getLocalName()) && ndref.equals(parser.getAttributeValue(0)) ) {
                        countnd++;
                       System.out.println("BEGINLOCATION: "+ parser.getLocation());
                      
                    for (int i =0; i <= countnd; i++ ){

                       
                       lat = parser.getAttributeValue( 1 ).toString();
                       lon = parser.getAttributeValue( 2 ).toString();
                       writer.writeStartElement( "gml", "pos", "http://www.opengis.net/gml" );
                       writer.writeCharacters( lon + " " + lat );
                       writer.writeEndElement();
                       System.out.println("");
                       System.out.println("LOCATIONEND: "+ parser.getLocation());
                       parser.nextTag();
                       parser.nextTag();
                    }
                    
                    }
                    
                    
                    
                    
                    
                
                
             }
        }
    }
    
    public Feature getRelations()
                    throws XMLStreamException, FileNotFoundException {
        
        InputStream in = new FileInputStream( "/home/goerke/Desktop/map.osm" );
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader parser = inputFactory.createXMLStreamReader( in );
        
        while ( parser.hasNext() ) {
             int event = parser.next();
             List<Property> props = new ArrayList<Property>();
             if ( event == XMLStreamReader.START_ELEMENT && "relation".equals(parser.getLocalName())) {
                 String relationid;
                 relationref = parser.getAttributeValue( 0 ).toString();
                 relationid = "relation" + parser.getAttributeValue( 0 ).toString();
                 String fid = relationid;
                 parser.nextTag();
                 while ( event == XMLStreamReader.START_ELEMENT && "member".equals(parser.getLocalName()) )
                 { 
                   String ref = parser.getAttributeValue(1).toString();
                   String type = parser.getAttributeValue(0).toString();
                   QName member = new QName("osm", "member", "http://www.deegree.org/osm");
                   
                   props.add( new GenericProperty( relationFt.getPropertyDeclaration(member), member, null ) );
                   parser.nextTag(); 
                   parser.nextTag();
                  
                   
                 }
                 this.getTags();
                 Feature Relation = relationFt.newFeature( fid, props, null );
                 return Relation;
             }
         }    
        return null;
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
        InputStream in = new FileInputStream( "C:/Dokumente und Einstellungen/Besitzer/Desktop/map.osm" );
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
                            List<Property> props = new ArrayList<Property>();
                            props.add( new GenericProperty( nodeFt.getPropertyDeclaration( key ), key,
                                                            new PrimitiveValue( value ) ) );

                        }
                    }

                    parser.nextTag();
                    parser.nextTag();

                }
            }
            while ( event == XMLStreamReader.START_ELEMENT && "relation".equals( parser.getLocalName()) && relationref.equals(parser.getAttributeValue(0))) {
                parser.nextTag();
                int countref =0;
                for(int i = 0; i <= countref; i++){
                  if ( event == XMLStreamReader.START_ELEMENT && "member".equals( parser.getLocalName() ) ) {
                  countref++;
                  }
                  else {
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

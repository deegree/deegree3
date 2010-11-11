package org.deegree.feature.persistence.osm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.stream.*;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

public class OSMReader {

    public static void main( String args[] )
                            throws XMLStreamException, FileNotFoundException {

        InputStream in = new FileInputStream( "/home/goerke/Desktop/map(2).osm" );
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader parser = inputFactory.createXMLStreamReader( in );

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = outputFactory.createXMLStreamWriter( System.out );

        writer.setDefaultNamespace( "http://www.opengis.net/gml" );
        writer.setPrefix( "gml", "http://www.opengis.net/gml" );
        writer.setPrefix( "osm", "http://www.openstreetmap.org/osm" );
        writer.writeStartElement( "gml", "FeatureCollection", "http://www.opengis.net/gml" );

        while ( parser.hasNext() ) {
            int event = parser.next();
           
            if ( event == XMLStreamReader.START_ELEMENT && "bounds".equals( parser.getLocalName() ) ) {
                String lowerLeft, upperRight;
                lowerLeft = parser.getAttributeValue( 1 ).toString() + " " + parser.getAttributeValue( 0 ).toString();
                upperRight = parser.getAttributeValue( 3 ).toString() + " " + parser.getAttributeValue( 2 ).toString();

                writer.writeStartElement( "gml", "boundedBy", "http://www.opengis.net/gml" );
                writer.writeStartElement( "gml", "Envelope", "http://www.opengis.net/gml" );
                writer.writeAttribute( "srsName", "EPSG:4326" );
                writer.writeStartElement( "gml", "lowerCorner", "http://www.opengis.net/gml" );
                writer.writeCharacters( lowerLeft );
                writer.writeEndElement();
                writer.writeStartElement( "gml", "upperCorner", "http://www.opengis.net/gml" );
                writer.writeCharacters( upperRight );
                writer.writeEndElement();
                writer.writeEndElement();
                writer.writeEndElement();
            }
            
            if ( event == XMLStreamReader.START_ELEMENT && "node".equals( parser.getLocalName()) ) {
               
                writer.writeStartElement( "gml", "featureMember", "http://www.opengis.net/gml" );
                writer.writeStartElement( "osm", "node", "http://www.openstreetmap.org/osm" );
                String nodeid, lat, lon;
                nodeid = "n" + parser.getAttributeValue( 0 ).toString();
                lat = parser.getAttributeValue( 1 ).toString();
                lon = parser.getAttributeValue( 2 ).toString();
                writer.writeAttribute( "gml", "http://www.opengis.net/gml", "id", nodeid );
                writer.writeStartElement( "osm", "geometry", "http://www.openstreetmap.org/osm" );
                writer.writeStartElement( "gml", "Point", "http://www.opengis.net/gml" );
                writer.writeStartElement( "gml", "pos", "http://www.opengis.net/gml" );
                writer.writeCharacters( lon + " " + lat );
                writer.writeEndElement();
                writer.writeEndElement();
                writer.writeEndElement();
                parser.nextTag();
                System.out.println("parse: " + parser.getLocalName());
                while ( event == XMLStreamReader.START_ELEMENT && "tag".equals( parser.getLocalName() ) ) {
                   
                    String key, value;
                    key = parser.getAttributeValue( 0 ).toString();
                    value = parser.getAttributeValue( 1 ).toString();
                    writer.writeStartElement( "osm", key, "http://www.openstreetmap.org/osm" );
                    writer.writeCharacters( value );
                    parser.nextTag();
                    parser.nextTag();
                    writer.writeEndElement();

                }
                writer.writeEndElement();
                writer.writeEndElement();
                
            }
            
            if ( event == XMLStreamReader.START_ELEMENT && "way".equals( parser.getLocalName())) {
               
                String wayid;
                wayid = "w" + parser.getAttributeValue( 0 ).toString();

                writer.writeStartElement( "gml", "featureMember", "http://www.opengis.net/gml" );
                writer.writeStartElement( "osm", "way", "http://www.openstreetmap.org/osm" );
                writer.writeAttribute( "gml", "http://www.opengis.net/gml", "id", wayid );
                writer.writeStartElement( "osm", "geometry", "http://www.openstreetmap.org/osm" );
                writer.writeStartElement( "gml", "LineString", "http://www.opengis.net/gml" );
                parser.nextTag();
                while ( event == XMLStreamReader.START_ELEMENT && "nd".equals(parser.getLocalName()) )
                { 
                  
                  
                  parser.nextTag(); 
                }
                writer.writeEndElement();
                writer.writeEndElement();
                
                while ( event == XMLStreamReader.START_ELEMENT && "tag".equals( parser.getLocalName() ) ) {
                    
                    String key, value;
                    key = parser.getAttributeValue( 0 ).toString();
                    value = parser.getAttributeValue( 1 ).toString();
                    writer.writeStartElement( "osm", key, "http://www.openstreetmap.org/osm" );
                    writer.writeCharacters( value );
                    parser.nextTag();
                    parser.nextTag();
                    writer.writeEndElement();
                }
                
                writer.writeEndElement();
                writer.writeEndElement();
                System.out.println("");
            }
       
        
        if ( event == XMLStreamReader.START_ELEMENT && "relation".equals( parser.getLocalName())) {
            String relationid;
            relationid = "r" + parser.getAttributeValue( 0 ).toString();
            
            writer.writeStartElement( "gml", "featureMember", "http://www.opengis.net/gml" );
            writer.writeStartElement( "osm", "relation", "http://www.openstreetmap.org/osm" );
            writer.writeAttribute( "gml", "http://www.opengis.net/gml", "id", relationid );
            parser.nextTag();
            System.out.println("parser1 " + parser.getLocalName());
            while ( event == XMLStreamReader.START_ELEMENT && "member".equals(parser.getLocalName()) )
            { 
              String ref = " ";
              parser.next();
              if (event == XMLStreamReader.ATTRIBUTE && "node".equals(parser.getAttributeLocalName( 0 ))){
                  System.out.println("1");
                  
              }
              
              if (event == XMLStreamReader.ATTRIBUTE && "way".equals(parser.getAttributeLocalName( 0 ))){
                  System.out.println("2");
                  ref = "w"+parser.getAttributeValue( 1 ).toString();
                 
                 
                  }
             
              if (event == XMLStreamReader.ATTRIBUTE && "relation".equals(parser.getAttributeLocalName( 0 ))){
                  System.out.println("3");
                  ref = "r"+parser.getAttributeValue( 1 ).toString();
                  
                  }
              
              writer.writeEmptyElement( "osm", "member", "http://www.openstreetmap.org/osm" );
              writer.writeAttribute( "xlink", "http://www.w3.org/1999/xlink", "href", ref );
              parser.nextTag();
              parser.nextTag();
              parser.nextTag();
            }
            
            while ( event == XMLStreamReader.START_ELEMENT && "tag".equals( parser.getLocalName() ) ) {
                String key, value;
                key = parser.getAttributeValue( 0 ).toString();
                value = parser.getAttributeValue( 1 ).toString();
                writer.writeStartElement( "osm", key, "http://www.openstreetmap.org/osm" );
                writer.writeCharacters( value );
                parser.nextTag();
                parser.nextTag();
                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.writeEndElement();
          }
        }
        writer.writeEndDocument();

        writer.flush();
        writer.close();
    }
}

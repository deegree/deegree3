package org.deegree.feature.persistence.osm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.*;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

public class OSMReader {

    String ndref ="";
    
    	public void getBounds () throws XMLStreamException, FileNotFoundException, IOException
    	{
    		//FileWriter output= new FileWriter(new File("C:/Dokumente und Einstellungen/Besitzer/Desktop/test.gml"));
    		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter( System.out );
    		InputStream in = new FileInputStream( "C:/Dokumente und Einstellungen/Besitzer/Desktop/map.osm" );
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader parser = inputFactory.createXMLStreamReader( in );
            
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
            }  
                
                
    	}
    	public void getNodes() throws XMLStreamException, FileNotFoundException
    	{
    		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter( System.out );
    		InputStream in = new FileInputStream( "C:/Dokumente und Einstellungen/Besitzer/Desktop/map.osm" );
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader parser = inputFactory.createXMLStreamReader( in );
            
            while ( parser.hasNext() ) {
                int event = parser.next();
                
                if ( event == XMLStreamReader.START_ELEMENT && "node".equals( parser.getLocalName())) {
                	
                	
                    writer.writeStartElement( "gml", "featureMember", "http://www.opengis.net/gml" );
                    writer.writeStartElement( "osm", "node", "http://www.openstreetmap.org/osm" );
                    String nodeid, lat, lon;
                    nodeid = "node" + parser.getAttributeValue( 0 ).toString();
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

                    this.getTags();
                    writer.writeEndElement();
                    writer.writeEndElement();
                }  
            
            }  
        }
    	
    	public void getWays() throws XMLStreamException, FileNotFoundException
    	{
    		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter( System.out );
    		InputStream in = new FileInputStream( "C:/Dokumente und Einstellungen/Besitzer/Desktop/map.osm" );
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader parser = inputFactory.createXMLStreamReader( in );
            
    		while ( parser.hasNext() ) {
                 int event = parser.next();
                 
                 if ( event == XMLStreamReader.START_ELEMENT && "way".equals( parser.getLocalName())) {
                     
                     String wayid;
                     wayid = "way" + parser.getAttributeValue( 0 ).toString();

                     writer.writeStartElement( "gml", "featureMember", "http://www.opengis.net/gml" );
                     writer.writeStartElement( "osm", "way", "http://www.openstreetmap.org/osm" );
                     writer.writeAttribute( "gml", "http://www.opengis.net/gml", "id", wayid );
                     writer.writeStartElement( "osm", "geometry", "http://www.openstreetmap.org/osm" );
                     writer.writeStartElement( "gml", "LineString", "http://www.opengis.net/gml" );
                     parser.nextTag();

                     while ( event == XMLStreamReader.START_ELEMENT && "nd".equals(parser.getLocalName()) )
                     {
                    	 ndref = parser.getAttributeValue( 0 );
                    	 this.getWayNodes();
                    	 parser.nextTag();
                    	 parser.nextTag();
                     }
                     
                     this.getTags();
                     writer.writeEndElement();
                     writer.writeEndElement();
                 }    
    		}     
    	}
    	
    	public void getWayNodes() throws XMLStreamException, FileNotFoundException{
    		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter( System.out );
    		InputStream in = new FileInputStream( "C:/Dokumente und Einstellungen/Besitzer/Desktop/map.osm" );
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader parser = inputFactory.createXMLStreamReader( in );
            
    		while ( parser.hasNext() ) {
                 int event = parser.next();
                 while ( event == XMLStreamReader.START_ELEMENT && "node".equals( parser.getLocalName()) && ndref.equals(parser.getAttributeValue(0)) ) {
                	 	String lat, lon;
                		lat = parser.getAttributeValue( 1 ).toString();
                        lon = parser.getAttributeValue( 2 ).toString();
                        writer.writeStartElement( "gml", "pos", "http://www.opengis.net/gml" );
                        writer.writeCharacters( lon + " " + lat );
                        writer.writeEndElement();
                        parser.nextTag();
                        parser.nextTag();
                	
                	
                 }
    		}
    	}
    	
    	public void getRelations() throws XMLStreamException, FileNotFoundException
    	{
    		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter( System.out );
    		InputStream in = new FileInputStream( "C:/Dokumente und Einstellungen/Besitzer/Desktop/map.osm" );
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader parser = inputFactory.createXMLStreamReader( in );
            
    		while ( parser.hasNext() ) {
                 int event = parser.next();
                 
                 if ( event == XMLStreamReader.START_ELEMENT && "relation".equals(parser.getLocalName())) {
                     String relationid;
                     relationid = "relation" + parser.getAttributeValue( 0 ).toString();
                     
                     writer.writeStartElement( "gml", "featureMember", "http://www.opengis.net/gml" );
                     writer.writeStartElement( "osm", "relation", "http://www.openstreetmap.org/osm" );
                     writer.writeAttribute( "gml", "http://www.opengis.net/gml", "id", relationid );
                     parser.nextTag();
                     while ( event == XMLStreamReader.START_ELEMENT && "member".equals(parser.getLocalName()) )
                     { 
                       String ref = parser.getAttributeValue(1).toString();
                       String type = parser.getAttributeValue(0).toString();
                       
                       writer.writeEmptyElement( "osm", "member", "http://www.openstreetmap.org/osm");
                       writer.writeAttribute("xlink", "http://www.w3c.org/1999/xlink", "href", type+ref);
                       parser.nextTag(); 
                       parser.nextTag();
                     }
                     this.getTags();
                     parser.nextTag();
                     writer.writeEndElement();
                     writer.writeEndElement();
                 }
                 
    		}     
                 
                 
    	}
    	public void getTags() throws XMLStreamException, FileNotFoundException
    	{
    		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = outputFactory.createXMLStreamWriter( System.out );
    		InputStream in = new FileInputStream( "C:/Dokumente und Einstellungen/Besitzer/Desktop/map.osm" );
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader parser = inputFactory.createXMLStreamReader( in );
            
    		while ( parser.hasNext() ) {
                 int event = parser.next();
                 if ( event == XMLStreamReader.START_ELEMENT && "node".equals( parser.getLocalName())) {
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
               }
               if ( event == XMLStreamReader.START_ELEMENT && "way".equals( parser.getLocalName())) {
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
                   }
               if ( event == XMLStreamReader.START_ELEMENT && "relation".equals( parser.getLocalName())) {
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
                   }
                 
    		 }     
    	}
    	
}

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
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class OSMReader {

    String ndref ="";
    String noderef = "";
    String wayref = "";
    String relationref = "";
    String firstndref = "";
    
 
	
   XMLStreamWriter writer;
   
   public OSMReader() throws XMLStreamException, IOException{

       FileWriter output= new FileWriter(new File("/home/goerke/Desktop/test.gml"));
       XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
       writer = outputFactory.createXMLStreamWriter(output );

   }
   
    	public void getBounds () throws XMLStreamException, FileNotFoundException, IOException
    	{
    		InputStream in = new FileInputStream( "/home/goerke/Desktop/map.osm" );
    		
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
    		
    		
    		InputStream in = new FileInputStream( "/home/goerke/Desktop/map.osm" );
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader parser = inputFactory.createXMLStreamReader( in );
            
            while ( parser.hasNext() ) {
                int event = parser.next();
                
                if ( event == XMLStreamReader.START_ELEMENT && "node".equals( parser.getLocalName())) {
                	
                	noderef = parser.getAttributeValue( 0 ).toString();
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
                    parser.next();
                    parser.next();
                    parser.next();
                    this.getTags();
                    
                    writer.writeEndElement();
                    writer.writeEndElement();
                }  
            
            }  
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

                     writer.writeStartElement( "gml", "featureMember", "http://www.opengis.net/gml" );
                     writer.writeStartElement( "osm", "way", "http://www.openstreetmap.org/osm" );
                     writer.writeAttribute( "gml", "http://www.opengis.net/gml", "id", wayid );
                     writer.writeStartElement( "osm", "geometry", "http://www.openstreetmap.org/osm" );
                     writer.writeStartElement( "gml", "LineString", "http://www.opengis.net/gml" );
                     parser.nextTag();
                     //this.askLineOrPolygon();
                     while ( event == XMLStreamReader.START_ELEMENT && "nd".equals(parser.getLocalName()) )
                     {
                    	 ndref = parser.getAttributeValue( 0 );
                    	 this.getWayNodes();
                    	 parser.nextTag();
                    	 parser.nextTag();
                    	 
                     }
                     writer.writeEndElement();
                     writer.writeEndElement();
                     
                     this.getTags();
                     writer.writeEndElement();
                     writer.writeEndElement();
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
    	public void askLineOrPolygon() throws XMLStreamException, FileNotFoundException
        {
            
            InputStream in = new FileInputStream( "/home/goerke/Desktop/map.osm" );
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader parser = inputFactory.createXMLStreamReader( in );
            
            while ( parser.hasNext() ) {
                int event = parser.next();
                while ( event == XMLStreamReader.START_ELEMENT && "way".equals( parser.getLocalName()) && wayref.equals(parser.getAttributeValue(0))) {
                    parser.nextTag();
                    System.out.println(parser.getLocation());
                    if ( event == XMLStreamReader.START_ELEMENT && "nd".equals(parser.getLocalName())) {
                        firstndref = parser.getAttributeValue( 0 );
                        parser.nextTag();
                        parser.nextTag();
                    while ( event == XMLStreamReader.START_ELEMENT && "nd".equals(parser.getLocalName()) && !firstndref.equals(parser.getAttributeValue( 0 ))) {
                        ndref = parser.getAttributeValue( 0 );
                        parser.nextTag();
                        parser.nextTag();
                        System.out.println("first "+firstndref +"next "+ndref );
                        
                    }
                    if (firstndref.equals( ndref )){
                        writer.writeStartElement( "gml", "Polygon", "http://www.opengis.net/gml" );
                    }
                    else {
                           writer.writeStartElement( "gml", "LineString", "http://www.opengis.net/gml" ); 
                           System.out.println("ending: "+ parser.getLocation());;
                         }
                    parser.nextTag();
                    }
                        
                  }
                
                }
        }
    	
    	public void getRelations() throws XMLStreamException, FileNotFoundException
    	{
    		
    		InputStream in = new FileInputStream( "/home/goerke/Desktop/map.osm" );
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader parser = inputFactory.createXMLStreamReader( in );
            
    		while ( parser.hasNext() ) {
                 int event = parser.next();
                 
                 if ( event == XMLStreamReader.START_ELEMENT && "relation".equals(parser.getLocalName())) {
                     String relationid;
                     relationref = parser.getAttributeValue( 0 ).toString();
                     relationid = "relation" + parser.getAttributeValue( 0 ).toString();
                     
                     writer.writeStartElement( "gml", "featureMember", "http://www.opengis.net/gml" );
                     writer.writeStartElement( "osm", "relation", "http://www.openstreetmap.org/osm" );
                     writer.writeAttribute( "gml", "http://www.opengis.net/gml", "id", relationid );
                     parser.nextTag();
                     while ( event == XMLStreamReader.START_ELEMENT && "member".equals(parser.getLocalName()) )
                     { 
                       String ref = parser.getAttributeValue(1).toString();
                       String type = parser.getAttributeValue(0).toString();
                       parser.nextTag(); 
                       parser.nextTag();
                       writer.writeEmptyElement( "osm", "member", "http://www.openstreetmap.org/osm");
                       writer.writeAttribute("xlink", "http://www.w3c.org/1999/xlink", "href", type+ref);
                       
                     }
                     this.getTags();
                     writer.writeEndElement();
                     writer.writeEndElement();
                 }
             }    
    	}
    	public void getTags() throws XMLStreamException, FileNotFoundException
    	{
    		
    		InputStream in = new FileInputStream( "/home/goerke/Desktop/map.osm" );
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader parser = inputFactory.createXMLStreamReader( in );
    		while ( parser.hasNext() ) {
                 int event = parser.next();

                 
                 
                 if ( event == XMLStreamReader.START_ELEMENT && "node".equals( parser.getLocalName() ) && noderef.equals(parser.getAttributeValue(0)) ) {
                   parser.nextTag();
                   parser.nextTag();
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
               while ( event == XMLStreamReader.START_ELEMENT && "way".equals( parser.getLocalName()) && wayref.equals(parser.getAttributeValue(0))) {
                   
            	   parser.nextTag();
                         int countnd =0;
                         for(int i = 0; i <= countnd; i++){
                        	 if ( event == XMLStreamReader.START_ELEMENT && "nd".equals( parser.getLocalName() ) ) {
                        	 countnd++;
                        	 }
                        	 else {
                        		 break;
                        	 }
                        	 parser.nextTag();
                        	 
                         }
                        
                    
                     while ( event == XMLStreamReader.START_ELEMENT && "tag".equals( parser.getLocalName() ) ) {
                         String key, value;
                         key = parser.getAttributeValue( 0 ).toString();
                         value = parser.getAttributeValue( 1 ).toString();
                         writer.writeStartElement( "osm", key, "http://www.openstreetmap.org/osm" );
                         writer.writeCharacters( value );
                         writer.writeEndElement();
                         parser.nextTag();
                         parser.nextTag();
                         
                         

                     }
                     parser.nextTag();
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
                   String key, value;
                   key = parser.getAttributeValue( 0 ).toString();
                   value = parser.getAttributeValue( 1 ).toString();
                   writer.writeStartElement( "osm", key, "http://www.openstreetmap.org/osm" );
                   writer.writeCharacters( value );
                   writer.writeEndElement();
                   parser.nextTag();
                   parser.nextTag();
                   
                   

               		}
               parser.nextTag();
                }
                 
    		 }     
    	}
    	
}

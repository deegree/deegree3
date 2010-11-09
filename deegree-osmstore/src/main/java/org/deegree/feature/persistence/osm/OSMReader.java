package org.deegree.feature.persistence.osm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.stream.*;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

public class OSMReader {
	
	public static void main(String args[]) throws XMLStreamException, FileNotFoundException {
		
		InputStream     in      = new FileInputStream("/home/goerke/Desktop/map.osm"); 
		XMLInputFactory inputFactory = XMLInputFactory.newInstance(); 
		XMLStreamReader parser = inputFactory.createXMLStreamReader(in);
		
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLStreamWriter writer = outputFactory.createXMLStreamWriter(System.out);

        int event = parser.next();
		
        writer.writeStartDocument("1.0");
        writer.setDefaultNamespace( "http://www.opengis.net/gml" );
        writer.setPrefix( "gml", "http://www.opengis.net/gml" );
        writer.setPrefix( "osm", "http://www.openstreetmap.org/osm" );
        writer.writeStartElement("gml", "FeatureCollection", "http://www.opengis.net/gml");
        
		 while (parser.hasNext()) {
		        if (event == XMLStreamReader.START_ELEMENT
		            && "bounds".equals(parser.getLocalName())) {
		        	String lowerLeft;
		        	String upperRight;
		        	lowerLeft = parser.getAttributeValue(0).toString() + " " + parser.getAttributeValue(1).toString();
		        	upperRight = parser.getAttributeValue(2).toString() + " " + parser.getAttributeValue(3).toString();
		        	
		        	writer.writeStartElement("gml", "boundedBy", "http://www.opengis.net/gml");
		        	writer.writeStartElement( "gml", "lowerCorner", "http://www.opengis.net/gml");
		        	writer.writeCharacters( lowerLeft );
		        	writer.writeEndElement();
		        	writer.writeStartElement( "gml", "upperCorner", "http://www.opengis.net/gml");
                    writer.writeCharacters( upperRight );
                    writer.writeEndElement();
		            writer.writeEndElement();
		        }

		 }   
	/**	while (parser.hasNext()) {
			  if (event == XMLStreamReader.START_ELEMENT
		            && "node".equals(parser.getLocalName()) && parser.hasName()) {
				  String nodeid;
				  String lat,lon;
				  nodeid = parser.getAttributeValue(0).toString();
				  lat = parser.getAttributeValue(1).toString();
				  lon = parser.getAttributeValue(2).toString();
				  System.out.println(nodeid + " " + lat + " " + lon);
			  }
		}
		
		
		
	   
	    
	    

	    writer.writeEndDocument();

	    writer.flush();
	    writer.close();*/
	} 
}

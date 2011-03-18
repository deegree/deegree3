package org.deegree.feature.persistence.osm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class OSMBuilder {

	public static void main( String args[] )
    throws XMLStreamException, FileNotFoundException, IOException {
	
    FileWriter output=new FileWriter(new File("/home/goerke/Desktop/test.gml"));
    XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
    XMLStreamWriter writer = outputFactory.createXMLStreamWriter( output );
    OSMReader OSMReader;
    OSMReader = new OSMReader();
    writer.setDefaultNamespace( "http://www.opengis.net/gml" );
    writer.setPrefix( "gml", "http://www.opengis.net/gml" );
    writer.setPrefix( "osm", "http://www.openstreetmap.org/osm" );
    writer.writeStartElement( "gml", "FeatureCollection", "http://www.opengis.net/gml" );

    OSMReader.getBounds();
   // OSMReader.getNodes();
    OSMReader.getWays();
   // OSMReader.getRelations();
    
    writer.writeEndDocument();
    writer.flush();
    writer.close();
}
}

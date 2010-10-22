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
package org.deegree.feature.persistence.postgis;

import static org.deegree.commons.xml.CommonNamespaces.XSINS;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.feature.types.ApplicationSchema;
import org.deegree.feature.types.FeatureType;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.FeaturePropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.MeasurePropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class PostGISFeatureStoreConfigHelper {

    private static final String CONFIG_NS = new PostGISFeatureStoreProvider().getConfigNamespace();

    private static final String SCHEMA_LOCATION = "http://www.deegree.org/datasource/feature/postgis http://schemas.deegree.org/datasource/feature/postgis/0.6.1/postgis.xsd";

    public static void writeConfig( XMLStreamWriter writer, ApplicationSchema schema, String storageCrs,
                                    Map<String, String> namespaceHints, String connId, List<String> schemaURLs )
                            throws XMLStreamException {

        writer.setDefaultNamespace( CONFIG_NS );

        writer.writeStartElement( CONFIG_NS, "deegreeWFS" );
        writer.writeAttribute( "configVersion", "0.6.1" );
        writer.setPrefix( "xsi", XSINS );
        writer.writeAttribute( XSINS, "schemaLocation", SCHEMA_LOCATION );

        writer.writeStartElement( CONFIG_NS, "StorageCRS" );
        writer.writeCharacters( storageCrs );
        writer.writeEndElement();

        writer.writeStartElement( CONFIG_NS, "JDBCConnId" );
        writer.writeCharacters( connId );
        writer.writeEndElement();

        List<FeatureType> fts = schema.getFeatureTypes( null, false, false );
        SortedSet<String> ftNames = new TreeSet<String>();
        for ( FeatureType ft : fts ) {
            ftNames.add( ft.getName().toString() );
        }

        for ( String qName : ftNames ) {
            QName ftName = QName.valueOf( qName );
            FeatureType ft = schema.getFeatureType( ftName );
            writer.writeStartElement( CONFIG_NS, "FeatureType" );
            writer.writeAttribute( "name", getName( ftName ) );
            for ( PropertyType pt : ft.getPropertyDeclarations() ) {
                writePropertyMapping( writer, pt );
            }
            writer.writeEndElement();
        }

        writer.writeEndElement();
    }

    private static void writePropertyMapping( XMLStreamWriter writer, PropertyType pt )
                            throws XMLStreamException {
        if ( pt instanceof CodePropertyType ) {
            writePropertyMapping( writer, (CodePropertyType) pt );
        } else if ( pt instanceof CustomPropertyType ) {
            writePropertyMapping( writer, (CustomPropertyType) pt );
        } else if ( pt instanceof FeaturePropertyType ) {
            writePropertyMapping( writer, (FeaturePropertyType) pt );
        } else if ( pt instanceof GeometryPropertyType ) {
            writePropertyMapping( writer, (GeometryPropertyType) pt );
        } else if ( pt instanceof MeasurePropertyType ) {
            writePropertyMapping( writer, (MeasurePropertyType) pt );
        } else if ( pt instanceof SimplePropertyType ) {
            writePropertyMapping( writer, (SimplePropertyType) pt );
        } else {
            System.out.println( "Unhandled property type '" + pt.getClass() + "'" );
        }
    }

    private static void writePropertyMapping( XMLStreamWriter writer, CodePropertyType pt )
                            throws XMLStreamException {
        writer.writeStartElement( CONFIG_NS, "CodeProperty" );
        writeCommonAttrs( writer, pt );
        writer.writeEndElement();
    }

    private static void writePropertyMapping( XMLStreamWriter writer, CustomPropertyType pt )
                            throws XMLStreamException {
        writer.writeStartElement( CONFIG_NS, "CustomProperty" );
        writeCommonAttrs( writer, pt );
        writer.writeEndElement();
    }

    private static void writePropertyMapping( XMLStreamWriter writer, FeaturePropertyType pt )
                            throws XMLStreamException {
        writer.writeEmptyElement( CONFIG_NS, "FeatureProperty" );
        writeCommonAttrs( writer, pt );
        if ( pt.getFTName() != null ) {
            writer.writeAttribute( "type", getName( pt.getFTName() ) );
        }
    }

    private static void writePropertyMapping( XMLStreamWriter writer, GeometryPropertyType pt )
                            throws XMLStreamException {
        writer.writeStartElement( CONFIG_NS, "GeometryProperty" );
        writeCommonAttrs( writer, pt );
        writer.writeEndElement();
    }

    private static void writePropertyMapping( XMLStreamWriter writer, MeasurePropertyType pt )
                            throws XMLStreamException {
        writer.writeStartElement( CONFIG_NS, "MeasureProperty" );
        writeCommonAttrs( writer, pt );
        writer.writeEndElement();
    }

    private static void writePropertyMapping( XMLStreamWriter writer, SimplePropertyType pt )
                            throws XMLStreamException {
        writer.writeStartElement( CONFIG_NS, "SimpleProperty" );
        writeCommonAttrs( writer, pt );
        writer.writeAttribute( "type", pt.getPrimitiveType().name().toLowerCase() );
        writer.writeEndElement();
    }

    private static void writeCommonAttrs( XMLStreamWriter writer, PropertyType pt )
                            throws XMLStreamException {
        writer.writeAttribute( "name", getName( pt.getName() ) );
        if ( pt.getMinOccurs() != 1 ) {
            writer.writeAttribute( "minOccurs", "" + pt.getMinOccurs() );
        }
        if ( pt.getMaxOccurs() != 1 ) {
            if ( pt.getMaxOccurs() == -1 ) {
                writer.writeAttribute( "maxOccurs", "unbounded" );
            } else {
                writer.writeAttribute( "maxOccurs", "" + pt.getMaxOccurs() );
            }
        }
    }

    private static String getName( QName name ) {
        return name.getLocalPart();
    }

    // public static void main( String[] args )
    // throws XMLStreamException, FactoryConfigurationError, IOException, ClassCastException,
    // ClassNotFoundException, InstantiationException, IllegalAccessException {
    //
    // String schemaURL = CoreTstProperties.getProperty( "schema_inspire_addresses" );
    // if ( schemaURL == null ) {
    // return;
    // }
    //
    // ApplicationSchemaXSDDecoder adapter = new ApplicationSchemaXSDDecoder( GMLVersion.GML_32, null, schemaURL );
    // ApplicationSchema schema = adapter.extractFeatureTypeSchema();
    //
    // OutputStream os = new FileOutputStream( "/tmp/config.xml" );
    // XMLStreamWriter xmlStream = XMLOutputFactory.newInstance().createXMLStreamWriter( os );
    // xmlStream = new IndentingXMLStreamWriter( xmlStream );
    // writeConfig( xmlStream, schema, "EPSG:4258", null, "inspire", null );
    // xmlStream.close();
    // os.close();
    // }
}
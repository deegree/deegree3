//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/commons/trunk/src/org/deegree/model/feature/Feature.java $
/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 Andreas Poth  
 lat/lon GmbH 
 Aennchenstr. 19
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de


 ---------------------------------------------------------------------------*/

package org.deegree.model.gml;

import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.commons.xml.CommonNamespaces.XLNNS;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.model.feature.Feature;
import org.deegree.model.feature.Property;

public class GMLFeatureExporter {

    public static void export( XMLStreamWriter writer, Feature feature )
                            throws XMLStreamException {
        export( writer, feature, new HashSet<Feature>() );
    }

    public static void export( XMLStreamWriter writer, Feature feature, Set<Feature> exportedFeatures )
                            throws XMLStreamException {

        QName featureName = feature.getName();
        if ( featureName.getNamespaceURI() == null || featureName.getNamespaceURI().length() == 0 ) {
            writer.writeStartElement( featureName.getLocalPart() );
        } else {
            writer.writeStartElement( featureName.getNamespaceURI(), featureName.getLocalPart() );
        }
        if ( feature.getId() != null ) {
            writer.writeAttribute( GMLNS, "id", feature.getId() );
        }
        exportedFeatures.add( feature );
        for ( Property<?> prop : feature.getProperties() ) {
            export( writer, prop, exportedFeatures );
        }
        writer.writeEndElement();
    }

    private static void export( XMLStreamWriter writer, Property<?> property, Set<Feature> exportedFeatures )
                            throws XMLStreamException {

        QName propName = property.getName();

        // TODO respect property type properly
        Object value = property.getValue();

        if ( value instanceof Feature ) {
            // check if feature has already been exported (avoid cycles)
            if ( exportedFeatures.contains( value ) ) {
                if ( propName.getNamespaceURI() == null || propName.getNamespaceURI().length() == 0 ) {
                    writer.writeEmptyElement( propName.getLocalPart() );
                } else {
                    writer.writeEmptyElement( propName.getNamespaceURI(), propName.getLocalPart() );
                }
                writer.writeAttribute( XLNNS, "href", "#" + ( (Feature) value ).getId() );
            } else {
                if ( propName.getNamespaceURI() == null || propName.getNamespaceURI().length() == 0 ) {
                    writer.writeStartElement( propName.getLocalPart() );
                } else {
                    writer.writeStartElement( propName.getNamespaceURI(), propName.getLocalPart() );
                }
                export( writer, (Feature) value, exportedFeatures );
                writer.writeEndElement();
            }
        } else {
            if ( propName.getNamespaceURI() == null || propName.getNamespaceURI().length() == 0 ) {
                writer.writeStartElement( propName.getLocalPart() );
            } else {
                writer.writeStartElement( propName.getNamespaceURI(), propName.getLocalPart() );
            }
            writer.writeCharacters( value.toString() );
            writer.writeEndElement();
        }
    }
}

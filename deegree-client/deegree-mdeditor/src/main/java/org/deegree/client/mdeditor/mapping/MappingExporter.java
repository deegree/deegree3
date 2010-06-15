//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.client.mdeditor.mapping;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.client.mdeditor.model.DataGroup;
import org.deegree.client.mdeditor.model.FormField;
import org.deegree.client.mdeditor.model.mapping.MappingElement;
import org.deegree.client.mdeditor.model.mapping.MappingGroup;
import org.deegree.client.mdeditor.model.mapping.MappingInformation;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class MappingExporter {

    private static final Logger LOG = getLogger( MappingExporter.class );

    public static void export( File file, MappingInformation mapping, Map<String, FormField> formFields,
                               Map<String, List<DataGroup>> dataGroups ) {
        try {
            XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
            FileOutputStream fos = new FileOutputStream( file );

            XMLStreamWriter writer = outputFactory.createXMLStreamWriter( fos );
            writer.writeStartDocument();

            List<MappingElement> mappingElements = mapping.getMappingElements();
            Iterator<MappingElement> it = mappingElements.iterator();
            if ( it.hasNext() ) {
                MappingElement currentElement = it.next();
                int currentIndex = 0;
                while ( currentElement != null ) {
                    MappingElement nextElement = null;
                    if ( it.hasNext() ) {
                        nextElement = it.next();
                    }
                    String ffPath = currentElement.getFormFieldPath();
                    List<QName> nextSteps = new ArrayList<QName>();
                    if ( nextElement != null ) {
                        nextSteps = nextElement.getSchemaPathAsSteps( mapping.getNsContext() );
                    }

                    if ( currentElement instanceof MappingGroup ) {
                        currentIndex = writeMappingGroup( writer, nextSteps, currentIndex,
                                                          (MappingGroup) currentElement, dataGroups.get( ffPath ),
                                                          ffPath, mapping );
                    } else {
                        List<QName> currentSteps = currentElement.getSchemaPathAsSteps( mapping.getNsContext() );
                        if ( formFields.containsKey( ffPath ) && formFields.get( ffPath ) != null ) {
                            currentIndex = writeMappingElement( writer, currentSteps, nextSteps, currentIndex,
                                                                formFields.get( ffPath ).getValue(), ffPath );
                        }
                    }
                    currentElement = nextElement;
                }
            }

            writer.writeEndDocument();
            writer.close();
        } catch ( IOException e ) {
            e.printStackTrace();
        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void wtiteDataGroup( XMLStreamWriter writer, MappingInformation mapping,
                                        List<MappingElement> mappingElements, Map<String, Object> values )
                            throws XMLStreamException {
        Iterator<MappingElement> it = mappingElements.iterator();
        if ( it.hasNext() ) {
            MappingElement currentElement = it.next();
            int currentIndex = 0;
            while ( currentElement != null ) {
                MappingElement nextElement = null;
                if ( it.hasNext() ) {
                    nextElement = it.next();
                }
                String ffPath = currentElement.getFormFieldPath();
                if ( values.containsKey( ffPath ) && values.get( ffPath ) != null ) {
                    List<QName> nextSteps = new ArrayList<QName>();
                    if ( nextElement != null ) {
                        nextSteps = nextElement.getSchemaPathAsSteps( mapping.getNsContext() );
                    }
                    List<QName> currentSteps = currentElement.getSchemaPathAsSteps( mapping.getNsContext() );

                    currentIndex = writeMappingElement( writer, currentSteps, nextSteps, currentIndex,
                                                        values.get( ffPath ), ffPath );

                }
                currentElement = nextElement;
            }
        }
    }

    private static int writeMappingGroup( XMLStreamWriter writer, List<QName> nextSteps, int currentIndex,
                                          MappingGroup group, List<DataGroup> dataGroups, String ffPath,
                                          MappingInformation mapping )
                            throws XMLStreamException {
        if ( dataGroups != null ) {
            List<QName> groupSteps = group.getSchemaPathAsSteps( mapping.getNsContext() );
            for ( ; currentIndex < groupSteps.size(); currentIndex++ ) {
                QName qName = groupSteps.get( currentIndex );
                writer.writeStartElement( qName.getPrefix(), qName.getLocalPart(), qName.getNamespaceURI() );
            }
            for ( DataGroup dg : dataGroups ) {
                Map<String, Object> values = dg.getValues();
                wtiteDataGroup( writer, mapping, group.getMappingElements(), values );
            }
            for ( int i = 0; i < groupSteps.size() - currentIndex; i++ ) {
                writer.writeEndElement();
            }

        }
        return currentIndex;
    }

    private static int writeMappingElement( XMLStreamWriter writer, List<QName> currentSteps, List<QName> nextSteps,
                                            int currentIndex, Object value, String ffPath )
                            throws XMLStreamException {
        if ( value != null ) {
            for ( ; currentIndex < currentSteps.size(); currentIndex++ ) {
                QName qName = currentSteps.get( currentIndex );
                // found list of elements
                if ( "*".equals( qName.getLocalPart() ) ) {
                    writeValue( writer, currentSteps.subList( currentIndex + 1, currentSteps.size() ), value );
                    break;
                } else {
                    writer.writeStartElement( qName.getPrefix(), qName.getLocalPart(), qName.getNamespaceURI() );
                    if ( currentIndex == currentSteps.size() - 1 ) {
                        writer.writeCharacters( value.toString() );
                    }
                }
            }
        }
        return finishStepsUntilNextCommon( writer, currentSteps, nextSteps, currentIndex );
    }

    private static void writeValue( XMLStreamWriter writer, List<QName> currentSteps, Object value )
                            throws XMLStreamException {
        if ( value instanceof List<?> ) {
            for ( Object o : (List<?>) value ) {
                writeSteps( writer, currentSteps, o.toString() );
            }
        } else {
            writeSteps( writer, currentSteps, value.toString() );
        }
    }

    private static void writeSteps( XMLStreamWriter writer, List<QName> currentSteps, String value )
                            throws XMLStreamException {
        for ( QName step : currentSteps ) {
            writer.writeStartElement( step.getPrefix(), step.getLocalPart(), step.getNamespaceURI() );
        }
        writer.writeCharacters( value );
        for ( int i = 0; i < currentSteps.size(); i++ ) {
            writer.writeEndElement();
        }
    }

    private static int finishStepsUntilNextCommon( XMLStreamWriter writer, List<QName> currentSteps,
                                                   List<QName> nextSteps, int currentIndex )
                            throws XMLStreamException {
        int stepsToClose = nextSteps.size() - currentIndex;
        int equalSteps = 0;
        for ( int i = 0; i < currentSteps.size() && i < nextSteps.size(); i++ ) {
            if ( currentSteps.get( i ).equals( nextSteps.get( i ) ) ) {
                equalSteps++;
            } else {
                break;
            }
        }
        stepsToClose = currentSteps.size() - equalSteps - ( currentSteps.size() - currentIndex );
        for ( int i = 0; i < stepsToClose; i++ ) {
            writer.writeEndElement();
        }

        return equalSteps;
    }
}

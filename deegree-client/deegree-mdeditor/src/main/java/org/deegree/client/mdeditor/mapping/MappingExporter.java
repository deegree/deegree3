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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.client.mdeditor.configuration.Configuration;
import org.deegree.client.mdeditor.configuration.ConfigurationException;
import org.deegree.client.mdeditor.io.DataHandler;
import org.deegree.client.mdeditor.model.DataGroup;
import org.deegree.client.mdeditor.model.FormConfiguration;
import org.deegree.client.mdeditor.model.FormField;
import org.deegree.client.mdeditor.model.FormFieldPath;
import org.deegree.client.mdeditor.model.SelectFormField;
import org.deegree.client.mdeditor.model.mapping.MappingElement;
import org.deegree.client.mdeditor.model.mapping.MappingGroup;
import org.deegree.client.mdeditor.model.mapping.MappingInformation;
import org.jaxen.expr.NameStep;
import org.jaxen.saxpath.Axis;
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

    public static void export( File file, MappingInformation mapping, Configuration configuration, String confId,
                               Map<String, List<DataGroup>> dataGroups )
                            throws XMLStreamException, FileNotFoundException, FactoryConfigurationError,
                            ConfigurationException {
        LOG.debug( "Export dataset in file " + file.getAbsolutePath() + " selected mapping is " + mapping.toString() );
        XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter( new FileOutputStream( file ) );
        writer.writeStartDocument();

        List<MappingElement> mappingElements = mapping.getMappingElements();
        Iterator<MappingElement> it = mappingElements.iterator();
        if ( it.hasNext() ) {
            MappingElement currentElement = it.next();
            int currentIndex = 0;
            while ( currentElement != null ) {
                LOG.debug( "handle mapping element " + currentElement );
                MappingElement nextElement = null;
                if ( it.hasNext() ) {
                    nextElement = it.next();
                }
                List<NameStep> nextSteps = new ArrayList<NameStep>();
                if ( nextElement != null ) {
                    nextSteps = nextElement.getSchemaPathAsSteps();
                }
                FormConfiguration formConfiguration = configuration.getConfiguration( confId );
                if ( currentElement instanceof MappingGroup ) {
                    currentIndex = writeMappingGroup( writer, (MappingGroup) currentElement, currentIndex, nextSteps,
                                                      mapping, configuration, formConfiguration, dataGroups );
                } else {
                    FormFieldPath path = FormFieldPath.getAsPath( currentElement.getFormFieldPath() );
                    // size can not be greater than 1, if mapping is for a single mapping element
                    if ( path.getPath().size() > 0 && dataGroups.containsKey( path.getPath().get( 0 ) ) ) {
                        List<DataGroup> dgs = dataGroups.get( path.getPath().get( 0 ) );
                        if ( dgs.size() > 0 ) {
                            Map<String, Object> v = dgs.get( 0 ).getValues();
                            FormField formField = getFormField( formConfiguration, currentElement.getFormFieldPath() );
                            List<String> values = getValuesAsList( v, configuration, formField, currentElement );

                            currentIndex = writeMappingElement( writer, currentElement, currentIndex, nextSteps,
                                                                mapping, configuration, formConfiguration, values );
                        }
                    }
                }
                currentElement = nextElement;
            }
        }
        writer.writeEndDocument();
        writer.close();
    }

    private static int writeMappingGroup( XMLStreamWriter writer, MappingGroup group, int currentIndex,
                                          List<NameStep> nextSteps, MappingInformation mapping,
                                          Configuration configuration, FormConfiguration formConfiguration,
                                          Map<String, List<DataGroup>> dgs )
                            throws XMLStreamException, ConfigurationException {
        FormFieldPath path = FormFieldPath.getAsPath( group.getFormFieldPath() );
        if ( path.getPath().size() > 0 && dgs.containsKey( path.getPath().get( 0 ) ) ) {
            List<DataGroup> dataGroups = dgs.get( path.getPath().get( 0 ) );
            if ( dataGroups.size() > 0 ) {
                LOG.debug( "write mapping group" );
                List<NameStep> groupSteps = group.getSchemaPathAsSteps();
                for ( ; currentIndex < groupSteps.size(); currentIndex++ ) {
                    NameStep qName = groupSteps.get( currentIndex );
                    String prefix = qName.getPrefix();
                    String namespaceURI = mapping.getNsContext().getURI( prefix );
                    writer.writeStartElement( prefix, qName.getLocalName(), namespaceURI );
                    writer.writeNamespace( prefix, namespaceURI );
                }
                for ( DataGroup d : dataGroups ) {
                    Map<String, Object> values = d.getValues();
                    if ( values != null && values.size() > 0 ) {
                        writeDataGroup( writer, mapping, group.getMappingElements(), values, configuration,
                                        formConfiguration );
                    }
                }
                for ( int i = 0; i < groupSteps.size() - currentIndex; i++ ) {
                    writer.writeEndElement();
                }
                return finishStepsUntilNextCommon( writer, groupSteps, nextSteps, currentIndex );
            }
        }
        return currentIndex;
    }

    private static void writeDataGroup( XMLStreamWriter writer, MappingInformation mapping,
                                        List<MappingElement> mappingElements, Map<String, Object> values,
                                        Configuration configuration, FormConfiguration formConfiguration )
                            throws XMLStreamException, ConfigurationException {
        System.out.println( mappingElements );
        System.out.println( values );
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
                    List<NameStep> nextSteps = new ArrayList<NameStep>();
                    if ( nextElement != null ) {
                        nextSteps = nextElement.getSchemaPathAsSteps();
                    }

                    FormField formField = getFormField( formConfiguration, currentElement.getFormFieldPath() );
                    List<String> v = getValuesAsList( values, configuration, formField, currentElement );
                    currentIndex = writeMappingElement( writer, currentElement, currentIndex, nextSteps, mapping,
                                                        configuration, formConfiguration, v );
                }
                currentElement = nextElement;
            }
        }
    }

    private static int writeMappingElement( XMLStreamWriter writer, MappingElement currentElement, int currentIndex,
                                            List<NameStep> nextSteps, MappingInformation mapping,
                                            Configuration configuration, FormConfiguration formConfiguration,
                                            List<String> values )
                            throws XMLStreamException, ConfigurationException {
        System.out.println( values );
        System.out.println( currentIndex );
        if ( values.size() > 0 ) {
            List<NameStep> currentSteps = currentElement.getSchemaPathAsSteps();
            System.out.println( currentSteps );
            for ( ; currentIndex < currentSteps.size(); currentIndex++ ) {
                NameStep nameStep = currentSteps.get( currentIndex );
                System.out.println( nameStep.getLocalName() );
                // found list of elements
                if ( "*".equals( nameStep.getLocalName() ) ) {
                    writeList( writer, currentSteps.subList( currentIndex + 1, currentSteps.size() ), values, mapping );
                    break;
                } else if ( Axis.ATTRIBUTE == nameStep.getAxis() ) {
                    writeAttribute( writer, nameStep, values.get( 0 ), mapping );
                    break;
                } else {
                    String prefix = nameStep.getPrefix();
                    String namespaceURI = mapping.getNsContext().getURI( prefix );
                    writer.writeStartElement( prefix, nameStep.getLocalName(), namespaceURI );
                    writer.writeNamespace( prefix, namespaceURI );
                    if ( currentIndex == currentSteps.size() - 1 ) {
                        writer.writeCharacters( values.get( 0 ) );
                    }
                }
            }
            return finishStepsUntilNextCommon( writer, currentSteps, nextSteps, currentIndex );
        }
        return currentIndex;
    }

    private static void writeAttribute( XMLStreamWriter writer, NameStep nameStep, String value,
                                        MappingInformation mapping )
                            throws XMLStreamException {
        LOG.debug( "write attribute " + nameStep + ", value is " + value );
        String prefix = nameStep.getPrefix();
        String ns = mapping.getNsContext().getURI( prefix );
        if ( ns != null ) {
            writer.writeAttribute( prefix, ns, nameStep.getLocalName(), value );
        } else {
            writer.writeAttribute( nameStep.getLocalName(), value );
        }
    }

    private static void writeList( XMLStreamWriter writer, List<NameStep> currentSteps, List<String> values,
                                   MappingInformation mapping )
                            throws XMLStreamException {
        LOG.debug( "write list of values" );
        for ( String value : values ) {
            for ( NameStep step : currentSteps ) {
                String prefix = step.getPrefix();
                String namespaceURI = mapping.getNsContext().getURI( prefix );
                writer.writeStartElement( prefix, step.getLocalName(), namespaceURI );
                writer.writeNamespace( prefix, namespaceURI );
            }
            writer.writeCharacters( value );
            for ( int i = 0; i < currentSteps.size(); i++ ) {
                writer.writeEndElement();
            }
        }
    }

    private static int finishStepsUntilNextCommon( XMLStreamWriter writer, List<NameStep> currentSteps,
                                                   List<NameStep> nextSteps, int currentIndex )
                            throws XMLStreamException {
        int equalSteps = 0;
        for ( int i = 0; i < currentSteps.size() && i < nextSteps.size(); i++ ) {
            if ( isSame( currentSteps.get( i ), nextSteps.get( i ) ) ) {
                equalSteps++;
            } else {
                break;
            }
        }
        int stepsToClose = currentIndex - equalSteps;
        for ( int i = 0; i < stepsToClose; i++ ) {
            writer.writeEndElement();
        }

        return equalSteps;
    }

    private static boolean isSame( NameStep one, NameStep two ) {
        if ( one != null && two != null ) {
            if ( one.getAxis() == two.getAxis()
                 && one.getLocalName().equals( two.getLocalName() )
                 && ( ( one.getPrefix() == null && two.getPrefix() == null ) || ( one.getPrefix() != null && one.getPrefix().equals(
                                                                                                                                     two.getPrefix() ) ) ) ) {
                return true;
            }
        }
        return false;
    }

    private static FormField getFormField( FormConfiguration formConfiguration, String ffPath ) {
        FormFieldPath path = FormFieldPath.getAsPath( ffPath );
        return formConfiguration.getFormField( path );
    }

    private static List<String> getValuesAsList( Map<String, Object> v, Configuration configuration,
                                                 FormField formField, MappingElement currentElement )
                            throws ConfigurationException {
        List<String> values = new ArrayList<String>();
        if ( formField != null ) {
            Object o = v.get( formField.getPath().toString() );
            if ( o != null ) {
                if ( formField instanceof SelectFormField ) {
                    SelectFormField ff = (SelectFormField) formField;
                    if ( ff.getReferenceToCodeList() != null ) {
                        if ( o instanceof List<?> ) {
                            for ( Object value : (List<?>) o ) {
                                values.add( getCodeListValue( configuration, ff, value.toString(),
                                                              currentElement.getIndex() ) );
                            }
                        } else {
                            values.add( getCodeListValue( configuration, ff, o.toString(), currentElement.getIndex() ) );
                        }
                    } else if ( ff.getReferenceToGroup() != null ) {
                        // TODO -> Referenz globales Element
                        if ( o instanceof List<?> ) {
                            for ( Object value : (List<?>) o ) {
                                List<String> referencedValue = getReferencedValue( ff, value.toString(),
                                                                                   currentElement.getSubFormFieldPath() );
                                if ( referencedValue != null ) {
                                    values.addAll( referencedValue );
                                }
                            }
                        } else {
                            List<String> referencedValue = getReferencedValue( ff, o.toString(),
                                                                               currentElement.getSubFormFieldPath() );
                            if ( referencedValue != null ) {
                                values.addAll( referencedValue );
                            }
                        }

                    }
                } else {
                    if ( o instanceof List<?> ) {
                        for ( Object value : (List<?>) o ) {
                            values.add( value.toString() );
                        }
                    } else {
                        values.add( o.toString() );
                    }
                }
            }
        }
        return values;
    }

    private static List<String> getReferencedValue( SelectFormField selectFF, String id, String subFormFieldPath )
                            throws ConfigurationException {
        // value: referenzierte Gruppe
        String referenceToGroup = selectFF.getReferenceToGroup();
        DataGroup dataGroup = DataHandler.getInstance().getDataGroup( referenceToGroup, id );
        if ( dataGroup.getValues().containsKey( subFormFieldPath ) ) {
            Object o = dataGroup.getValues().get( subFormFieldPath );
            if ( o != null ) {
                List<String> values = new ArrayList<String>();
                if ( o instanceof List<?> ) {
                    for ( Object value : (List<?>) o ) {
                        values.add( value.toString() );
                    }
                } else {
                    values.add( o.toString() );
                }
                return values;
            }
        }
        return null;
    }

    private static String getCodeListValue( Configuration configuration, SelectFormField selectFF, String value,
                                            int index )
                            throws ConfigurationException {
        String codeListValue = configuration.getCodeListValue( selectFF.getReferenceToCodeList(), value );
        if ( index > -1 ) {
            String[] values = codeListValue.split( "," );
            if ( values.length > index ) {
                return values[index];
            }
        }
        return codeListValue;
    }

}

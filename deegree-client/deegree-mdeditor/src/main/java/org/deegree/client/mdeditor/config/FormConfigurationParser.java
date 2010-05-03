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
package org.deegree.client.mdeditor.config;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;

import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.client.mdeditor.model.CodeList;
import org.deegree.client.mdeditor.model.FormElement;
import org.deegree.client.mdeditor.model.FormField;
import org.deegree.client.mdeditor.model.FormGroup;
import org.deegree.client.mdeditor.model.INPUT_TYPE;
import org.deegree.client.mdeditor.model.InputFormField;
import org.deegree.client.mdeditor.model.SELECT_TYPE;
import org.deegree.client.mdeditor.model.SelectFormField;
import org.deegree.client.mdeditor.model.Validation;
import org.deegree.commons.xml.XMLParsingException;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class FormConfigurationParser {

    private static final Logger LOG = getLogger( FormConfigurationParser.class );

    private static final String NS = "http://www.deegree.org/igeoportal";

    private static QName FORM_CONF_ELEMENT = new QName( NS, "FormConfiguration" );

    private static QName FORM_GROUP_ELEMENT = new QName( NS, "FormGroup" );

    private static QName INPUT_FORM_ELEMENT = new QName( NS, "InputFormElement" );

    private static QName SELECT_FORM_ELEMENT = new QName( NS, "SelectFormElement" );

    private static QName CODELIST_ELEMENT = new QName( NS, "CodeList" );

    private static List<CodeList> codeLists = new ArrayList<CodeList>();

    private static List<FormGroup> formGroups = new ArrayList<FormGroup>();

    private static String layoutType;

    public static List<CodeList> getCodeLists() {
        return codeLists;
    }

    public static CodeList getCodeList( String id ) {
        if ( id == null ) {
            throw new NullPointerException();
        }
        for ( CodeList cl : codeLists ) {
            if ( id.equals( cl.getId() ) ) {
                return cl;
            }
        }
        return null;
    }

    /**
     * @return a list of all top level formGroups
     */
    public static List<FormGroup> getFormGroups() {
        return formGroups;
    }

    /**
     * @return a list of all form fields
     */
    public static Map<String, FormField> getFormElements() {
        Map<String, FormField> formElements = new HashMap<String, FormField>();
        for ( FormGroup fg : formGroups ) {
            addFormField( formElements, fg );
        }
        return formElements;
    }

    private static void addFormField( Map<String, FormField> formElements, FormGroup fg ) {
        for ( FormElement fe : fg.getFormElements() ) {
            if ( fe instanceof FormGroup ) {
                addFormField( formElements, (FormGroup) fe );
            } else if ( fe instanceof FormField ) {
                formElements.put( fe.getCompleteId(), (FormField) fe );
            }
        }
    }

    /**
     * @param grpId
     *            the id of the group to return
     * @return the form group with the given id, returns null if a grouup with the given id does not exist
     * @throws NullPointerException
     *             if grpId is null
     */
    public static FormGroup getFormGroup( String grpId ) {
        if ( grpId == null ) {
            throw new NullPointerException();
        }
        for ( FormGroup fg : formGroups ) {
            if ( grpId.equals( fg.getId() ) )
                return fg;
        }
        return null;
    }

    public String getLayoutType() {
        return layoutType;
    }

    static {
        parse();
    }

    private static void parse() {
        try {
            XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(
                                                                                             new FileReader(
                                                                                                             Configuration.getFormConfURL() ) );

            if ( xmlStream.getEventType() == START_DOCUMENT ) {
                xmlStream.nextTag();
            }
            xmlStream.require( START_ELEMENT, NS, FORM_CONF_ELEMENT.getLocalPart() );
            xmlStream.nextTag();
            if ( xmlStream.getEventType() != START_ELEMENT ) {
                throw new XMLParsingException( xmlStream, "Empty FormConfiguration" );
            }
            xmlStream.require( START_ELEMENT, NS, "layoutType" );
            layoutType = xmlStream.getElementText();
            xmlStream.nextTag();

            while ( !( xmlStream.isEndElement() && xmlStream.getName().equals( FORM_CONF_ELEMENT ) ) ) {
                QName elementName = xmlStream.getName();
                if ( FORM_GROUP_ELEMENT.equals( elementName ) ) {
                    formGroups.add( parseFormGroup( xmlStream, true ) );
                } else if ( CODELIST_ELEMENT.equals( elementName ) ) {
                    parseCodeList( xmlStream );
                }
                xmlStream.nextTag();
            }

            xmlStream.require( END_ELEMENT, NS, FORM_CONF_ELEMENT.getLocalPart() );

        } catch ( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( FactoryConfigurationError e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static FormGroup parseFormGroup( XMLStreamReader xmlStream, boolean isEntry )
                            throws XMLStreamException, IOException {

        String formGroupId = getId( xmlStream );
        if ( xmlStream.isStartElement() && FORM_GROUP_ELEMENT.equals( xmlStream.getName() ) ) {
            xmlStream.nextTag();
        }
        String label = getElementText( xmlStream, "label", formGroupId );
        String title = getElementText( xmlStream, "title", formGroupId );
        LOG.debug( "Found group with id " + formGroupId + ", title " + title + ", label " + label
                   + ". Start to parse form elements and groups." );

        FormGroup fg = new FormGroup( formGroupId, label, title );

        while ( !( xmlStream.isEndElement() && FORM_GROUP_ELEMENT.equals( xmlStream.getName() ) ) ) {
            if ( xmlStream.isStartElement() && FORM_GROUP_ELEMENT.equals( xmlStream.getName() ) ) {
                fg.addFormElement( parseFormGroup( xmlStream, false ) );
            } else if ( xmlStream.isStartElement() && INPUT_FORM_ELEMENT.equals( xmlStream.getName() ) ) {
                fg.addFormElement( parseInputFormElement( xmlStream, formGroupId ) );
            } else if ( xmlStream.isStartElement() && SELECT_FORM_ELEMENT.equals( xmlStream.getName() ) ) {
                fg.addFormElement( parseSelectFormElement( xmlStream, formGroupId ) );
            }
            xmlStream.next();
        }

        xmlStream.require( END_ELEMENT, NS, FORM_GROUP_ELEMENT.getLocalPart() );
        return fg;

    }

    private static SelectFormField parseSelectFormElement( XMLStreamReader xmlStream, String grpId )
                            throws XMLStreamException, IOException {
        String id = getId( xmlStream );
        boolean visible = getBooleanAttribute( xmlStream, "visible", true );
        xmlStream.nextTag();

        String label = getElementText( xmlStream, "label", id );
        String help = getElementText( xmlStream, "help", "Keine Hilfe verfügbar" );

        LOG.debug( "Found SelectFormElement with id " + id + "; label " + label + "; help " + help );

        xmlStream.require( START_ELEMENT, null, "selectType" );
        SELECT_TYPE selectType = getSelectType( xmlStream );

        String referenceToGroup = getElementText( xmlStream, "referenceToGroup", null );
        String referenceToCodeList = getElementText( xmlStream, "referenceToCodeList", null );
        String selectedValueAsString = getElementText( xmlStream, "selectedValue", null );
        Object selectedValue = selectedValueAsString;
        if ( SELECT_TYPE.MANY.equals( selectType ) ) {
            List<String> selValues = new ArrayList<String>();
            String[] split = selectedValueAsString.split( "," );
            for ( int i = 0; i < split.length; i++ ) {
                selValues.add( split[i].trim() );
            }
            selectedValue = selValues;
        }

        SelectFormField ff = new SelectFormField( grpId, id, label, visible, help, selectedValue, selectType,
                                                  referenceToCodeList, referenceToGroup );

        return ff;

    }

    private static InputFormField parseInputFormElement( XMLStreamReader xmlStream, String grpId )
                            throws XMLStreamException, IOException {
        String id = getId( xmlStream );
        boolean visible = getBooleanAttribute( xmlStream, "visible", true );
        xmlStream.nextTag();

        String label = getElementText( xmlStream, "label", id );
        String help = getElementText( xmlStream, "help", "Keine Hilfe verfügbar" );

        LOG.debug( "Found InputFormElement with id " + id + "; label " + label + "; help " + help );

        xmlStream.require( START_ELEMENT, null, "inputType" );
        INPUT_TYPE inputType = getInputType( xmlStream );

        String defaultValue = getElementText( xmlStream, "defaultValue", "" );

        Validation validation = null;
        if ( "Validation".equals( xmlStream.getLocalName() ) ) {
            validation = new Validation();
            xmlStream.nextTag();
            validation.setLength( getElementInteger( xmlStream, "length", Integer.MIN_VALUE ) );
            validation.setTimestampPattern( getElementText( xmlStream, "timestampPattern", null ) );
            validation.setMinValue( getElementDouble( xmlStream, "minValue", Double.MIN_VALUE ) );
            validation.setMaxValue( getElementDouble( xmlStream, "maxValue", Double.MIN_VALUE ) );
        }

        InputFormField ff = new InputFormField( grpId, id, label, visible, help, inputType, defaultValue, validation );

        return ff;
    }

    private static void parseCodeList( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        String clId = getId( xmlStream );
        CodeList cl = new CodeList( clId );

        QName code_element = new QName( NS, "Code" );
        LOG.debug( "Found CodeList with id " + clId );
        if ( xmlStream.isStartElement() && xmlStream.getName().equals( CODELIST_ELEMENT ) ) {
            xmlStream.nextTag();
        }
        while ( !( xmlStream.isEndElement() && xmlStream.getName().equals( CODELIST_ELEMENT ) ) ) {
            if ( xmlStream.isStartElement() && code_element.equals( xmlStream.getName() ) ) {
                xmlStream.nextTag();
                xmlStream.require( START_ELEMENT, null, "value" );
                String value = getElementText( xmlStream, "value", null );
                xmlStream.require( START_ELEMENT, null, "label" );
                String label = getElementText( xmlStream, "label", null );
                cl.addCode( value, label );
            }
            xmlStream.next();
        }
        xmlStream.require( END_ELEMENT, NS, CODELIST_ELEMENT.getLocalPart() );
        codeLists.add( cl );
    }

    private static INPUT_TYPE getInputType( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        String elementText = getElementText( xmlStream, "inputType", null );
        if ( "text".equals( elementText ) ) {
            return INPUT_TYPE.TEXT;
        } else if ( "string".equals( elementText ) ) {
            return INPUT_TYPE.STRING;
        } else if ( "timestamp".equals( elementText ) ) {
            return INPUT_TYPE.TIMESTAMP;
        } else if ( "int".equals( elementText ) ) {
            return INPUT_TYPE.INT;
        } else if ( "double".equals( elementText ) ) {
            return INPUT_TYPE.DOUBLE;
        }
        throw new XMLParsingException( xmlStream, "inputType " + elementText + "is not valid" );
    }

    private static SELECT_TYPE getSelectType( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        String elementText = getElementText( xmlStream, "selectType", null );
        if ( "many".equals( elementText ) ) {
            return SELECT_TYPE.MANY;
        } else if ( "one".equals( elementText ) ) {
            return SELECT_TYPE.ONE;
        }
        throw new XMLParsingException( xmlStream, "selectType " + elementText + "is not valid" );
    }

    private static String getId( XMLStreamReader xmlStream ) {
        String id = xmlStream.getAttributeValue( null, "id" );
        if ( id == null ) {
            throw new XMLParsingException( xmlStream, "id must not be null" );
        }
        return id;
    }

    private static boolean getBooleanAttribute( XMLStreamReader xmlStream, String name, boolean defaultValue ) {
        try {
            String attributeValue = xmlStream.getAttributeValue( null, name );
            if ( attributeValue != null ) {
                return Boolean.parseBoolean( attributeValue );
            }
        } catch ( Exception e ) {
            LOG.debug( "Attribute with name " + name + "is not set, return defaultValue: " + defaultValue );
        }
        return defaultValue;
    }

    private static String getElementText( XMLStreamReader xmlStream, String name, String defaultValue )
                            throws XMLStreamException {
        String s = defaultValue;
        if ( name != null && name.equals( xmlStream.getLocalName() ) ) {
            s = xmlStream.getElementText();
            xmlStream.nextTag();
        }
        return s;
    }

    private static int getElementInteger( XMLStreamReader xmlStream, String name, int defaultValue )
                            throws XMLStreamException {
        int i = defaultValue;
        if ( name != null && name.equals( xmlStream.getLocalName() ) ) {
            String element = xmlStream.getElementText();
            try {
                i = Integer.valueOf( element );
            } catch ( NumberFormatException e ) {
                LOG.info( "Found invalid integer (" + element + ") in element " + name + "return defaultValue ("
                          + defaultValue + ")" );
            }
            xmlStream.nextTag();
        }
        return i;
    }

    private static double getElementDouble( XMLStreamReader xmlStream, String name, double defaultValue )
                            throws XMLStreamException {
        double d = defaultValue;
        if ( name != null && name.equals( xmlStream.getLocalName() ) ) {
            String element = xmlStream.getElementText();
            try {
                d = Double.valueOf( element );
            } catch ( NumberFormatException e ) {
                LOG.info( "Found invalid double (" + element + ") in element " + name + "return defaultValue ("
                          + defaultValue + ")" );
            }
            xmlStream.nextTag();
        }
        return d;
    }

}

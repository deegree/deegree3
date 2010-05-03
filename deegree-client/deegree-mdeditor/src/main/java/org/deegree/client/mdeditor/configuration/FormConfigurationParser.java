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
package org.deegree.client.mdeditor.configuration;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.client.mdeditor.model.INPUT_TYPE;
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

    static final Logger LOG = getLogger( FormConfigurationParser.class );

    public static final String NS = "http://www.deegree.org/igeoportal";

    public static QName FORM_CONF_ELEMENT = new QName( NS, "FormConfiguration" );

    public static QName FORM_GROUP_ELEMENT = new QName( NS, "FormGroup" );

    public static QName INPUT_FORM_ELEMENT = new QName( NS, "InputFormElement" );

    public static QName SELECT_FORM_ELEMENT = new QName( NS, "SelectFormElement" );

    public static QName CODELIST_ELEMENT = new QName( NS, "CodeList" );

    private static JSFMainWriter writer;

    private static BeanListWriter mapWriter;

    public static void parse( XMLStreamReader xmlStream )
                            throws IOException, XMLStreamException {
        if ( xmlStream.getEventType() == START_DOCUMENT ) {
            xmlStream.nextTag();
        }
        xmlStream.require( START_ELEMENT, NS, FORM_CONF_ELEMENT.getLocalPart() );
        xmlStream.nextTag();
        if ( xmlStream.getEventType() != START_ELEMENT ) {
            throw new XMLParsingException( xmlStream, "Empty FormConfiguration" );
        }
        xmlStream.require( START_ELEMENT, NS, "layoutType" );
        String layoutType = xmlStream.getElementText();
        xmlStream.nextTag();

        writer = new JSFMainWriter( layoutType );
        mapWriter = new BeanListWriter();

        while ( !( xmlStream.isEndElement() && xmlStream.getName().equals( FORM_CONF_ELEMENT ) ) ) {
            QName elementName = xmlStream.getName();
            if ( FORM_GROUP_ELEMENT.equals( elementName ) ) {
                parseFormGroup( xmlStream, writer, true );
            } else if ( CODELIST_ELEMENT.equals( elementName ) ) {
                parseCodeList( xmlStream );
            }
            xmlStream.nextTag();
        }

        xmlStream.require( END_ELEMENT, NS, FORM_CONF_ELEMENT.getLocalPart() );
        writer.finishWriting();
        mapWriter.write();
    }

    private static void parseFormGroup( XMLStreamReader xmlStream, JSFWriter parent, boolean isEntry )
                            throws XMLStreamException, IOException {

        String formGroupId = getId( xmlStream );
        if ( xmlStream.isStartElement() && FORM_GROUP_ELEMENT.equals( xmlStream.getName() ) ) {
            xmlStream.nextTag();
        }
        String label = getElementText( xmlStream, "label", formGroupId );
        String title = getElementText( xmlStream, "title", formGroupId );
        LOG.debug( "Found group with id " + formGroupId + ", title " + title + ", label " + label
                   + ". Start to parse form elements and groups." );
        // Write beans
        BeanWriter beanWriter = new BeanWriter( formGroupId );
        beanWriter.startWriting();
        // and jsf
        FormGroupWriter jsfWriter = parent.addFormGroup( formGroupId, label, title, isEntry );

        while ( !( xmlStream.isEndElement() && FORM_GROUP_ELEMENT.equals( xmlStream.getName() ) ) ) {
            if ( xmlStream.isStartElement() && FORM_GROUP_ELEMENT.equals( xmlStream.getName() ) ) {
                parseFormGroup( xmlStream, jsfWriter, false );
            } else if ( xmlStream.isStartElement() && INPUT_FORM_ELEMENT.equals( xmlStream.getName() ) ) {
                parseInputFormElement( jsfWriter, beanWriter, xmlStream, formGroupId );
            } else if ( xmlStream.isStartElement() && SELECT_FORM_ELEMENT.equals( xmlStream.getName() ) ) {
                parseSelectFormElement( jsfWriter, beanWriter, xmlStream, formGroupId );
            }
            xmlStream.next();
        }

        xmlStream.require( END_ELEMENT, NS, FORM_GROUP_ELEMENT.getLocalPart() );

        // finish writing
        beanWriter.finishWriting();
        jsfWriter.finishWriting();
    }

    private static void parseSelectFormElement( FormGroupWriter jsfWriter, BeanWriter beanWriter,
                                                XMLStreamReader xmlStream, String grpId )
                            throws XMLStreamException, IOException {
        String id = getId( xmlStream );
        boolean visible = getBooleanAttribute( xmlStream, "visible", true );
        boolean required = getBooleanAttribute( xmlStream, "required", false );
        xmlStream.nextTag();
        LOG.debug( "Found SelectFormElement with id " + id );

        String label = getElementText( xmlStream, "label", id );
        String help = getElementText( xmlStream, "help", "Keine Hilfe verfügbar" );
        boolean helpAsLink = getBooleanAttribute( xmlStream, "showAsLink", true );

        xmlStream.require( START_ELEMENT, null, "selectType" );
        String selectType = getElementText( xmlStream, "selectType", null );

        String referenceToGroup = getElementText( xmlStream, "referenceToGroup", null );
        String referenceToCodeList = getElementText( xmlStream, "referenceToCodeList", null );
        int selectedValue = getElementInteger( xmlStream, "selectedValue", Integer.MIN_VALUE );

        beanWriter.addSelectField( id, visible, required, selectType, help, helpAsLink );
        jsfWriter.addSelectField( id, label, visible, required, help, selectType, referenceToGroup,
                                  referenceToCodeList, selectedValue );

        mapWriter.addField( grpId, id );
    }

    private static void parseInputFormElement( FormGroupWriter fgw, BeanWriter beanWriter, XMLStreamReader xmlStream,
                                               String grpId )
                            throws XMLStreamException, IOException {
        String id = getId( xmlStream );
        boolean visible = getBooleanAttribute( xmlStream, "visible", true );
        boolean required = getBooleanAttribute( xmlStream, "required", false );
        xmlStream.nextTag();
        LOG.debug( "Found InputFormElement with id " + id );
        String label = getElementText( xmlStream, "label", id );
        String help = getElementText( xmlStream, "help", "Keine Hilfe verfügbar" );
        boolean helpAsLink = getBooleanAttribute( xmlStream, "showAsLink", true );

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

        beanWriter.addInputField( id, visible, required, help, helpAsLink, inputType, defaultValue );
        fgw.addInputField( id, label, visible, help, inputType, validation );

        mapWriter.addField( grpId, id );
    }

    private static void parseCodeList( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        LOG.debug( "parse CodeList" );
        if ( xmlStream.isStartElement() && xmlStream.getName().equals( CODELIST_ELEMENT ) ) {
            xmlStream.next();
        }
        while ( !( xmlStream.isEndElement() && xmlStream.getName().equals( CODELIST_ELEMENT ) ) ) {
            xmlStream.next();
        }
        xmlStream.require( END_ELEMENT, NS, CODELIST_ELEMENT.getLocalPart() );
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

    public static String getId( XMLStreamReader xmlStream ) {
        String id = xmlStream.getAttributeValue( null, "id" );
        if ( id == null ) {
            throw new XMLParsingException( xmlStream, "id must not be null" );
        }
        return id;
    }

    public static boolean getBooleanAttribute( XMLStreamReader xmlStream, String name, boolean defaultValue ) {
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

    public static String getElementText( XMLStreamReader xmlStream, String name, String defaultValue )
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

    public static void main( String[] args )
                            throws XMLStreamException, FactoryConfigurationError, IOException {
        String file = "/home/lyn/workspace/deegree-mdeditor/resources/exampleConfiguration.xml";
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader( new FileReader( file ) );
        FormConfigurationParser.parse( reader );

        System.out.println( "finished at " + new Date( System.currentTimeMillis() ) );
    }

}

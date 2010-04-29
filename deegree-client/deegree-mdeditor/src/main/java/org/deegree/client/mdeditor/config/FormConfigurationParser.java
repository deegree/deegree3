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
import static org.deegree.client.mdeditor.configuration.FormConfigurationParser.FORM_CONF_ELEMENT;
import static org.deegree.client.mdeditor.configuration.FormConfigurationParser.FORM_GROUP_ELEMENT;
import static org.deegree.client.mdeditor.configuration.FormConfigurationParser.INPUT_FORM_ELEMENT;
import static org.deegree.client.mdeditor.configuration.FormConfigurationParser.NS;
import static org.deegree.client.mdeditor.configuration.FormConfigurationParser.SELECT_FORM_ELEMENT;
import static org.deegree.client.mdeditor.configuration.FormConfigurationParser.getBooleanAttribute;
import static org.deegree.client.mdeditor.configuration.FormConfigurationParser.getId;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.client.mdeditor.gui.FormElement;
import org.deegree.commons.xml.XMLParsingException;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class FormConfigurationParser {

    public static Map<String, FormElement> parseFormElements() {
        Map<String, FormElement> formElements = new HashMap<String, FormElement>();

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
            while ( !( xmlStream.isEndElement() && FORM_CONF_ELEMENT.equals( xmlStream.getName() ) ) ) {
                QName elementName = xmlStream.getName();
                if ( FORM_GROUP_ELEMENT.equals( elementName ) ) {
                    parseFormGroup( xmlStream, formElements );
                } else {
                    xmlStream.next();
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

        return formElements;
    }

    private static void parseFormGroup( XMLStreamReader xmlStream, Map<String, FormElement> formElements )
                            throws XMLStreamException, IOException {
        String formGroupId = getId( xmlStream );
        xmlStream.nextTag();
        while ( !( xmlStream.isEndElement() && FORM_GROUP_ELEMENT.equals( xmlStream.getName() ) ) ) {
            if ( xmlStream.isStartElement() && FORM_GROUP_ELEMENT.equals( xmlStream.getName() ) ) {
                parseFormGroup( xmlStream, formElements );
            } else if ( xmlStream.isStartElement()
                        && ( INPUT_FORM_ELEMENT.equals( xmlStream.getName() ) || SELECT_FORM_ELEMENT.equals( xmlStream.getName() ) ) ) {
                parseFormElement( xmlStream, formGroupId, formElements );
            } else {
                xmlStream.next();
            }
            xmlStream.nextTag();
        }

        xmlStream.require( END_ELEMENT, NS, FORM_GROUP_ELEMENT.getLocalPart() );
    }

    private static void parseFormElement( XMLStreamReader xmlStream, String grpId, Map<String, FormElement> formElements )
                            throws XMLStreamException, IOException {
        String id = getId( xmlStream );
        boolean visible = getBooleanAttribute( xmlStream, "visible", true );

        FormElement newFE = new FormElement( grpId, id );
        newFE.setVisibility( visible );
        newFE.setValue( "test" );
        formElements.put( newFE.getCompleteId(), newFE );
    }
}

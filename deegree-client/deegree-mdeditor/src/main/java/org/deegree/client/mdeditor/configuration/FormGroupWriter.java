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

import static org.deegree.client.mdeditor.configuration.Utils.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DecimalFormat;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.client.mdeditor.model.INPUT_TYPE;
import org.deegree.client.mdeditor.model.Validation;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class FormGroupWriter extends JSFWriter {

    private String grpId;

    private XMLStreamWriter writer;

    private String grpLabel;

    private String grpTitle;

    private static DecimalFormat df = new DecimalFormat( "#.#" );

    protected FormGroupWriter( String grpId, String label, String title, boolean isEntry ) throws XMLStreamException,
                            FileNotFoundException {
        super( isEntry );
        this.grpId = grpId;
        this.grpLabel = label;
        this.grpTitle = title;

        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        FileOutputStream fos = new FileOutputStream( "/home/lyn/workspace/deegree-mdeditor/src/main/webapp/page/form/"
                                                     + grpId + ".xhtml" );
        writer = outputFactory.createXMLStreamWriter( fos );
        writeStart();
    }

    public String getId() {
        return grpId;
    }

    public String getLabel() {
        return grpLabel;
    }

    private void writeStart()
                            throws XMLStreamException {
        startWriting( writer );

        writer.writeStartElement( H_PREF, "form", H_NS );

        writer.writeStartElement( H_PREF, "panelGrid", H_NS );
        writer.writeAttribute( "columns", "4" );

        writer.writeStartElement( F_PREF, "facet", F_NS );
        writer.writeAttribute( "name", "header" );
        writer.writeEmptyElement( H_PREF, "outputText", H_NS );
        writer.writeAttribute( "value", grpTitle );
        writer.writeEndElement();
    }

    public void finishWriting()
                            throws XMLStreamException {
        writer.writeEndElement();
        writer.writeEndElement();
        finishWriting( writer );
        writer.close();
    }

    public void addInputField( String id, String label, boolean required, String helpText, INPUT_TYPE inputType,
                               Validation validation )
                            throws XMLStreamException {
        String renderedEL = getEL( grpId, getVisibilityProp( id ) );

        writeLabel( id, label, renderedEL );

        writer.writeStartElement( H_PREF, "inputText", H_NS );
        writer.writeAttribute( "id", id );
        writer.writeAttribute( "rendered", renderedEL );
        writer.writeAttribute( "required", getEL( grpId, getRequiredProp( id ) ) );
        writer.writeAttribute( "value", getEL( grpId, beginLowerCaseId( id ) ) );
        writer.writeEmptyElement( F_PREF, "ajax", F_NS );
        writer.writeAttribute( "render", "@none" );

        // conversion
        switch ( inputType ) {
        case TIMESTAMP:
            if ( validation.getTimestampPattern() != null ) {
                writer.writeEmptyElement( F_PREF, "convertDateTime", F_NS );
                if ( validation != null && validation.getTimestampPattern() != null ) {
                    writer.writeAttribute( "pattern", validation.getTimestampPattern() );
                }
            }
            break;
        case DOUBLE:
            writer.writeEmptyElement( F_PREF, "convertNumber", F_NS );
            break;
        case INT:
            writer.writeEmptyElement( F_PREF, "convertNumber", F_NS );
            writer.writeAttribute( "integerOnly", "true" );
            writer.writeAttribute( "pattern", "#" );
            break;
        default:
            break;
        }

        // validation
        if ( validation != null ) {
            switch ( inputType ) {
            case STRING:
                if ( validation.getLength() != Integer.MIN_VALUE ) {
                    writer.writeEmptyElement( F_PREF, "validateLength", F_NS );
                    writer.writeAttribute( "render", String.valueOf( validation.getLength() ) );
                }
                break;
            case TIMESTAMP:
                // handled with conversion
                break;
            case DOUBLE:
                if ( validation.getMinValue() != Double.NaN || validation.getMaxValue() != Double.NaN ) {
                    writer.writeEmptyElement( F_PREF, "validateDoubleRange", F_NS );
                    if ( validation.getMinValue() != Double.NaN ) {
                        writer.writeAttribute( "maximum", df.format( validation.getMinValue() ) );
                    }
                    if ( validation.getMaxValue() != Double.NaN ) {
                        writer.writeAttribute( "minimum", df.format( validation.getMaxValue() ) );
                    }
                }
                break;
            case INT:
                if ( validation.getMinValue() != Double.NaN || validation.getMaxValue() != Double.NaN ) {
                    writer.writeEmptyElement( F_PREF, "validateLongRange", F_NS );
                    if ( validation.getMinValue() != Double.NaN ) {
                        writer.writeAttribute( "maximum", df.format( validation.getMinValue() ) );
                    }
                    if ( validation.getMaxValue() != Double.NaN ) {
                        writer.writeAttribute( "minimum", df.format( validation.getMaxValue() ) );
                    }
                }
                break;
            default:
                break;
            }
        }

        writer.writeEndElement();

        writeHelp( id, helpText );
        writeMsg( id, renderedEL );

    }

    public void addSelectField( String id, String label, boolean visible, boolean required, String helpText,
                                String selectType, String referenceToGroup, String referenceToCodeList,
                                int selectedValue )
                            throws XMLStreamException {
        String renderedEL = getEL( grpId, getVisibilityProp( id ) );
        writeLabel( id, label, renderedEL );

        if ( "one".equals( selectType ) ) {
            writer.writeEmptyElement( H_PREF, "selectOneListbox", H_NS );
            writer.writeAttribute( "id", id );
            writer.writeAttribute( "value", "" );
            writer.writeAttribute( "rendered", renderedEL );
        } else if ( "many".equals( selectType ) ) {
            writer.writeEmptyElement( H_PREF, "selectManyListbox", H_NS );
            writer.writeAttribute( "id", id );
            writer.writeAttribute( "value", "" );
            writer.writeAttribute( "rendered", renderedEL );
        }

        // writer.writeEmptyElement( H_PREF, "panelGroup", H_NS );
        writeHelp( id, helpText );
        writeMsg( id, renderedEL );

    }

    public FormGroupWriter addFormGroup( String id, String label, String title, boolean isEntry )
                            throws XMLStreamException, FileNotFoundException {
        writer.writeEmptyElement( H_PREF, "panelGroup", H_NS );
        writer.writeEmptyElement( UI_PREF, "include", UI_NS );
        writer.writeAttribute( "src", id + ".xhtml" );
        writer.writeEmptyElement( H_PREF, "panelGroup", H_NS );
        writer.writeEmptyElement( H_PREF, "panelGroup", H_NS );
        return new FormGroupWriter( id, label, title, isEntry );
    }

    private void writeHelp( String id, String helpText )
                            throws XMLStreamException {

        // TODO: rendered depeneds on rendered field
        // link
        writer.writeStartElement( H_PREF, "commandLink", H_NS );
        writer.writeAttribute( "value", "h" );
        writer.writeAttribute( "rendered", getEL( grpId, getHelpProp( id ) ) );

        writer.writeEmptyElement( F_PREF, "param", F_NS );
        writer.writeAttribute( "name", "text" );
        writer.writeAttribute( "value", helpText );

        writer.writeEmptyElement( F_PREF, "ajax", F_NS );
        writer.writeAttribute( "render", ":helpOutput" );
        writer.writeAttribute( "listener", "#{helpBean.updateHelp}" );
        writer.writeEndElement();

        // inline
        writer.writeEmptyElement( H_PREF, "outputText", H_NS );
        writer.writeAttribute( "value", grpLabel );
        writer.writeAttribute( "rendered", getELNot( grpId, getHelpProp( id ) ) );
    }

    private void writeLabel( String id, String label, String renderedEL )
                            throws XMLStreamException {
        writer.writeEmptyElement( H_PREF, "outputLabel", H_NS );
        writer.writeAttribute( "for", id );
        writer.writeAttribute( "value", label );
        writer.writeAttribute( "rendered", renderedEL );

    }

    private void writeMsg( String id, String renderedEL )
                            throws XMLStreamException {
        writer.writeEmptyElement( H_PREF, "message", H_NS );
        writer.writeAttribute( "for", id );
        writer.writeAttribute( "rendered", renderedEL );
    }
}

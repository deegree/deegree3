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
package org.deegree.client.mdeditor.configuration.form;

import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.deegree.commons.xml.stax.StAXParsingHelper.getAttributeValue;
import static org.deegree.commons.xml.stax.StAXParsingHelper.getAttributeValueAsBoolean;
import static org.deegree.commons.xml.stax.StAXParsingHelper.getElementTextAsDouble;
import static org.deegree.commons.xml.stax.StAXParsingHelper.getElementTextAsInteger;
import static org.deegree.commons.xml.stax.StAXParsingHelper.getRequiredText;
import static org.deegree.commons.xml.stax.StAXParsingHelper.getText;
import static org.deegree.commons.xml.stax.StAXParsingHelper.moveReaderToFirstMatch;
import static org.deegree.commons.xml.stax.StAXParsingHelper.nextElement;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.client.mdeditor.configuration.ConfigurationException;
import org.deegree.client.mdeditor.configuration.Parser;
import org.deegree.client.mdeditor.model.FormConfiguration;
import org.deegree.client.mdeditor.model.FormElement;
import org.deegree.client.mdeditor.model.FormFieldPath;
import org.deegree.client.mdeditor.model.FormGroup;
import org.deegree.client.mdeditor.model.INPUT_TYPE;
import org.deegree.client.mdeditor.model.InputFormField;
import org.deegree.client.mdeditor.model.LAYOUT_TYPE;
import org.deegree.client.mdeditor.model.ReferencedElement;
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
public class FormConfigurationParser extends Parser {

    private static final Logger LOG = getLogger( FormConfigurationParser.class );

    private static QName ROOT = new QName( NS, "FormConfiguration" );

    private static QName DATASET_CONF = new QName( NS, "DatasetConfiguration" );

    private static QName MAPPING = new QName( NS, "Mapping" );

    private static QName FORM_GROUP = new QName( NS, "FormGroup" );

    private static QName INPUT_FORM = new QName( NS, "InputFormElement" );

    private static QName SELECT_FORM = new QName( NS, "SelectFormElement" );

    private static QName REF_FORM = new QName( NS, "ReferencedFormElement" );

    private List<String> referencedGroups = new ArrayList<String>();

    private List<FormGroup> formGroups = new ArrayList<FormGroup>();

    private LAYOUT_TYPE layoutType;

    private Stack<String> path = new Stack<String>();

    FormConfiguration parseConfiguration( String formConfiguration )
                            throws ConfigurationException {
        try {

            XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader(
                                                                                             formConfiguration,
                                                                                             new FileReader(
                                                                                                             formConfiguration ) );
            if ( !moveReaderToFirstMatch( xmlStream, ROOT ) ) {
                throw new ConfigurationException( "could not parse form configuration" + xmlStream.getLocation()
                                                  + ": root element does not exist" );
            }
            layoutType = getLayoutType( xmlStream );
            LOG.debug( "Found layout type: " + layoutType );

            FormConfiguration conf = new FormConfiguration( layoutType );

            while ( !( xmlStream.isEndElement() && xmlStream.getName().equals( ROOT ) ) ) {
                QName elementName = xmlStream.getName();
                if ( DATASET_CONF.equals( elementName ) ) {
                    parseDatasetConfiguration( xmlStream, conf );
                } else if ( FORM_GROUP.equals( elementName ) ) {
                    formGroups.add( parseFormGroup( xmlStream ) );
                } else {
                    nextElement( xmlStream );
                }
            }
            updateFormGroups();
            conf.setFormGroups( formGroups );
            return conf;

        } catch ( FileNotFoundException e ) {
            LOG.debug( "could not find form configuration: " + formConfiguration, e );
            throw new ConfigurationException( "could not find form configuration: " + formConfiguration );

        } catch ( Exception e ) {
            LOG.debug( "could not parse form configuration: " + formConfiguration, e );
            throw new ConfigurationException( "could not parse form configuration: " + formConfiguration );
        }
    }

    private void parseDatasetConfiguration( XMLStreamReader xmlStream, FormConfiguration conf )
                            throws XMLStreamException, ConfigurationException {
        LOG.debug( "parse DatasetConfiguration" );
        nextElement( xmlStream );
        FormFieldPath pathToIdentifier = getAsPath( getRequiredText( xmlStream, new QName( NS, "identifier" ), true ) );
        if ( pathToIdentifier == null ) {
            throw new ConfigurationException( "path to identifier must be set!" );
        }
        conf.setPathToIdentifier( pathToIdentifier );
        conf.setPathToTitle( getAsPath( getText( xmlStream, new QName( NS, "title" ), null, true ) ) );
        conf.setPathToDescription( getAsPath( getText( xmlStream, new QName( NS, "description" ), null, true ) ) );

        if ( !moveReaderToFirstMatch( xmlStream, MAPPING ) ) {
            throw new ConfigurationException( "could not parse mapping: element does not exist" );
        }

        List<URL> mappings = parseMappings( xmlStream );
        if ( mappings.size() == 0 ) {
            throw new ConfigurationException( "form configuration does not contain at least one valid mapping" );
        }
        conf.setMappingURLs( mappings );
        nextElement( xmlStream );
    }

    private List<URL> parseMappings( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        LOG.debug( "Parse Mapping" );
        List<URL> mappings = new ArrayList<URL>();
        while ( !( xmlStream.isEndElement() && MAPPING.equals( xmlStream.getName() ) ) ) {
            if ( "mappingURL".equals( xmlStream.getLocalName() ) ) {
                String url = getText( xmlStream, new QName( NS, "mappingURL" ), null, true );
                if ( url != null ) {
                    LOG.debug( "Found mappingURL: " + url );
                    try {
                        mappings.add( resolve( url, xmlStream ) );
                    } catch ( MalformedURLException e ) {
                        LOG.debug( "Could not resolve as URL: " + url, e );
                        LOG.error( "Could not resolve as URL: " + url, e.getMessage() );
                    }
                }
            } else {
                nextElement( xmlStream );
            }
        }
        nextElement( xmlStream );
        return mappings;
    }

    private FormGroup parseFormGroup( XMLStreamReader xmlStream )
                            throws XMLStreamException, ConfigurationException {
        String formGroupId = getId( xmlStream );
        int occurence = getOccurence( xmlStream, formGroupId );
        nextElement( xmlStream );

        path.push( formGroupId );
        String label = getText( xmlStream, new QName( NS, "label" ), null, true );
        String title = getText( xmlStream, new QName( NS, "title" ), null, true );
        LOG.debug( "Found group with id " + formGroupId + ", title " + title + ", label " + label
                   + ". Start to parse form elements and groups." );

        FormGroup fg = new FormGroup( formGroupId, label, title, occurence );
        while ( !( xmlStream.isEndElement() && FORM_GROUP.equals( xmlStream.getName() ) ) ) {
            QName elementName = xmlStream.getName();
            if ( xmlStream.isStartElement() && FORM_GROUP.equals( elementName ) ) {
                fg.addFormElement( parseFormGroup( xmlStream ) );
            } else if ( xmlStream.isStartElement() && INPUT_FORM.equals( elementName ) ) {
                fg.addFormElement( parseInputFormElement( xmlStream ) );
            } else if ( xmlStream.isStartElement() && SELECT_FORM.equals( elementName ) ) {
                fg.addFormElement( parseSelectFormElement( xmlStream ) );
            } else if ( xmlStream.isStartElement() && REF_FORM.equals( elementName ) ) {
                fg.addFormElement( parseRefFormElement( xmlStream ) );
            } else {
                nextElement( xmlStream );
            }
        }
        path.pop();
        nextElement( xmlStream );
        return fg;

    }

    private FormElement parseRefFormElement( XMLStreamReader xmlStream )
                            throws ConfigurationException, XMLStreamException {
        String id = getId( xmlStream );
        boolean visible = getAttributeValueAsBoolean( xmlStream, null, "visible", true );
        nextElement( xmlStream );
        String label = getText( xmlStream, new QName( NS, "label" ), null, true );
        String help = getText( xmlStream, new QName( NS, "help" ), null, true );
        String defaultValue = getText( xmlStream, new QName( NS, "defaultValue" ), null, true );

        LOG.debug( "Found ReferencedFormElement with id " + id + "; label " + label + "; help " + help );
        String beanName = getRequiredText( xmlStream, new QName( NS, "bean-name" ), true );
        ReferencedElement re = new ReferencedElement( getPath( id ), id, label, visible, help, defaultValue, beanName );
        return re;
    }

    private SelectFormField parseSelectFormElement( XMLStreamReader xmlStream )
                            throws XMLStreamException, ConfigurationException {
        String id = getId( xmlStream );
        boolean visible = getAttributeValueAsBoolean( xmlStream, null, "visible", true );
        nextElement( xmlStream );
        String label = getText( xmlStream, new QName( NS, "label" ), null, true );
        String help = getText( xmlStream, new QName( NS, "help" ), null, true );
        String defaultValueAsString = getText( xmlStream, new QName( NS, "defaultValue" ), null, true );

        LOG.debug( "Found SelectFormElement with id " + id + "; label " + label + "; help " + help );

        xmlStream.require( START_ELEMENT, null, "selectType" );
        SELECT_TYPE selectType = getSelectType( xmlStream );

        String referenceToGroup = getText( xmlStream, new QName( NS, "referenceToGroup" ), null, true );
        if ( referenceToGroup != null ) {
            referencedGroups.add( referenceToGroup );
        }
        String referenceText = getText( xmlStream, new QName( NS, "referenceText" ), null, true );
        String referenceToCodeList = getText( xmlStream, new QName( NS, "referenceToCodeList" ), null, true );

        Object defaultValue = defaultValueAsString;
        if ( SELECT_TYPE.MANY.equals( selectType ) && defaultValueAsString != null ) {
            List<String> selValues = new ArrayList<String>();
            String[] split = defaultValueAsString.split( "," );
            for ( int i = 0; i < split.length; i++ ) {
                selValues.add( split[i].trim() );
            }
            defaultValue = selValues;
        }

        SelectFormField ff = new SelectFormField( getPath( id ), id, label, visible, help, defaultValue, selectType,
                                                  referenceToCodeList, referenceToGroup, referenceText );
        return ff;

    }

    private FormFieldPath getAsPath( String path ) {
        if ( path != null && path.length() > 0 ) {
            return new FormFieldPath( path.split( "/" ) );

        }
        return null;
    }

    private FormFieldPath getPath( String fieldId ) {
        if ( fieldId == null ) {
            return null;
        }
        FormFieldPath ffPath = new FormFieldPath();
        for ( String id : path ) {
            ffPath.addStep( id );
        }
        ffPath.addStep( fieldId );
        return ffPath;
    }

    private InputFormField parseInputFormElement( XMLStreamReader xmlStream )
                            throws XMLStreamException, ConfigurationException {
        String id = getId( xmlStream );
        boolean visible = getAttributeValueAsBoolean( xmlStream, null, "visible", true );
        int occurence = getOccurence( xmlStream, id );
        nextElement( xmlStream );

        String label = getText( xmlStream, new QName( NS, "label" ), null, true );
        String help = getText( xmlStream, new QName( NS, "help" ), null, true );
        String defaultValue = getText( xmlStream, new QName( NS, "defaultValue" ), null, true );

        LOG.debug( "Found InputFormElement with id " + id + "; label " + label + "; help " + help );

        INPUT_TYPE inputType = getInputType( xmlStream );

        Validation validation = null;
        if ( "Validation".equals( xmlStream.getLocalName() ) ) {
            validation = new Validation();
            nextElement( xmlStream );
            getElementTextAsDouble( xmlStream, new QName( NS, "maxValue" ), Double.MAX_VALUE, true );
            validation.setLength( getElementTextAsInteger( xmlStream, new QName( NS, "length" ), Integer.MIN_VALUE,
                                                           true ) );
            validation.setTimestampPattern( getText( xmlStream, new QName( NS, "timestampPattern" ), null, true ) );
            validation.setMinValue( getElementTextAsDouble( xmlStream, new QName( NS, "minValue" ), Double.MIN_VALUE,
                                                            true ) );
            validation.setMaxValue( getElementTextAsDouble( xmlStream, new QName( NS, "maxValue" ), Double.MAX_VALUE,
                                                            true ) );
        }
        InputFormField ff = new InputFormField( getPath( id ), id, label, visible, help, inputType, occurence,
                                                defaultValue, validation );
        return ff;
    }

    private int getOccurence( XMLStreamReader xmlStream, String id )
                            throws ConfigurationException {
        int occurence = 1;
        String occurenceAtt = getAttributeValue( xmlStream, "occurence" );
        if ( occurenceAtt != null ) {
            if ( !"unbounded".equals( occurenceAtt ) ) {
                try {
                    occurence = Integer.parseInt( occurenceAtt );
                } catch ( Exception e ) {
                    throw new ConfigurationException( "the attribute occurence of component with id " + id
                                                      + " is not a valid integer: " + occurenceAtt
                                                      + " nor equals unbounded" );
                }
            } else {
                occurence = Integer.MIN_VALUE;
            }
        }
        return occurence;
    }

    private INPUT_TYPE getInputType( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        String elementText = getRequiredText( xmlStream, new QName( NS, "inputType" ), true );
        if ( "text".equals( elementText ) ) {
            return INPUT_TYPE.TEXT;
        } else if ( "textarea".equals( elementText ) ) {
            return INPUT_TYPE.TEXTAREA;
        } else if ( "timestamp".equals( elementText ) ) {
            return INPUT_TYPE.TIMESTAMP;
        } else if ( "int".equals( elementText ) ) {
            return INPUT_TYPE.INT;
        } else if ( "double".equals( elementText ) ) {
            return INPUT_TYPE.DOUBLE;
        }
        throw new XMLParsingException( xmlStream, "inputType " + elementText + " is not valid" );
    }

    private SELECT_TYPE getSelectType( XMLStreamReader xmlStream )
                            throws XMLStreamException {
        String elementText = getRequiredText( xmlStream, new QName( NS, "selectType" ), true );
        if ( "many".equals( elementText ) ) {
            return SELECT_TYPE.MANY;
        } else if ( "one".equals( elementText ) ) {
            return SELECT_TYPE.ONE;
        }
        throw new XMLParsingException( xmlStream, "selectType " + elementText + "is not valid" );
    }

    private LAYOUT_TYPE getLayoutType( XMLStreamReader xmlStream )
                            throws ConfigurationException, XMLStreamException {
        if ( !moveReaderToFirstMatch( xmlStream, new QName( NS, "layoutType" ) ) ) {
            throw new ConfigurationException( "layout type is not set" );
        }
        String elementText = getRequiredText( xmlStream, new QName( NS, "layoutType" ), true );
        if ( "menu".equals( elementText ) ) {
            return LAYOUT_TYPE.MENU;
        } else if ( "tab".equals( elementText ) ) {
            return LAYOUT_TYPE.TAB;
        } else if ( "accordion".equals( elementText ) ) {
            return LAYOUT_TYPE.ACCORDION;
        } else if ( "wizard".equals( elementText ) ) {
            return LAYOUT_TYPE.WIZARD;
        }
        throw new ConfigurationException( "layoutType " + elementText + "is not valid" );
    }

    private void updateFormGroups()
                            throws ConfigurationException {
        for ( String reference : referencedGroups ) {
            boolean referenced = false;
            for ( FormGroup fg : formGroups ) {
                if ( reference.equals( fg.getId() ) ) {
                    fg.setReferenced( true );
                    referenced = true;
                    break;
                }
            }
            if ( !referenced ) {
                throw new ConfigurationException( "Referenced group " + reference + " does not exist!" );
            }
        }
    }

}

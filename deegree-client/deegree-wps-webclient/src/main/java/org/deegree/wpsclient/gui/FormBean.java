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
package org.deegree.wpsclient.gui;

import static org.deegree.client.core.utils.MessageUtils.getFacesMessage;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.UISelectItem;
import javax.faces.component.behavior.AjaxBehavior;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlForm;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlMessage;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlSelectManyCheckbox;
import javax.faces.component.html.HtmlSelectOneRadio;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;

import org.deegree.client.core.component.HtmlFieldset;
import org.deegree.client.core.component.HtmlInputBBox;
import org.deegree.client.core.component.HtmlInputFile;
import org.deegree.client.core.model.BBox;
import org.deegree.client.core.utils.MessageUtils;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.wps.client.input.type.BBoxInputType;
import org.deegree.protocol.wps.client.input.type.ComplexInputType;
import org.deegree.protocol.wps.client.input.type.InputType;
import org.deegree.protocol.wps.client.input.type.LiteralInputType;
import org.deegree.protocol.wps.client.output.type.OutputType;
import org.deegree.protocol.wps.client.param.ComplexFormat;
import org.deegree.protocol.wps.client.param.ValueWithRef;
import org.deegree.protocol.wps.client.process.Process;
import org.deegree.wpsclient.gui.component.HtmlLiteralInput;
import org.deegree.wpsclient.gui.component.HtmlSelectFormat;
import org.deegree.wpsclient.gui.converter.ComplexFormatConverter;
import org.slf4j.Logger;

/**
 * <code>FormBean</code> manages the creation of the form dependent of the selected process
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
@ManagedBean
@RequestScoped
public class FormBean {

    private static final Logger LOG = getLogger( FormBean.class );

    private HtmlForm executeForm;

    /**
     * JSF event listener to create the form dependent of the selected process before form is rendered
     */
    public void create( ComponentSystemEvent event )
                            throws AbortProcessingException {
        FacesContext fc = FacesContext.getCurrentInstance();

        ClientBean cb = (ClientBean) fc.getELContext().getELResolver().getValue( fc.getELContext(), null, "clientBean" );
        Process process = cb.getSelectedProcess();

        if ( process != null ) {
            LOG.debug( "create form for process: " + process.getId() );
            if ( executeForm == null ) {
                executeForm = new HtmlForm();
            }
            executeForm.getChildren().clear();

            try {
                addInputParams( fc, executeForm, process.getInputTypes() );
                setOutputParams( fc, executeForm, process.getOutputTypes() );
            } catch ( OWSException e ) {
                FacesMessage msg = getFacesMessage( FacesMessage.SEVERITY_WARN, "WARN.EXCEPTION", e.getMessage() );
                fc.addMessage( "WPSBean.selectProcess.EXCEPTION", msg );
            } catch ( IOException e ) {
                FacesMessage msg = getFacesMessage( FacesMessage.SEVERITY_WARN, "WARN.IOEXCEPTION", e.getMessage() );
                fc.addMessage( "WPSBean.selectProcess.IOEXCEPTION", msg );
            }
            HtmlCommandButton button = new HtmlCommandButton();
            button.setId( "executeButton" );
            button.setValue( "Execute" );
            String buttonEL = "#{executeBean.executeProcess}";
            MethodExpression action = fc.getApplication().getExpressionFactory().createMethodExpression( fc.getELContext(),
                                                                                                         buttonEL,
                                                                                                         Object.class,
                                                                                                         new Class<?>[] {} );
            button.getAttributes().put( ExecuteBean.PROCESS_ATTRIBUTE_KEY, process );
            button.setActionExpression( action );

            executeForm.getChildren().add( button );
        }
    }

    private void addInputParams( FacesContext fc, UIComponent parent, InputType[] inputs ) {
        HtmlPanelGrid inputGrid = new HtmlPanelGrid();
        inputGrid.setId( getUniqueId() );
        inputGrid.setColumns( 4 );
        inputGrid.setStyleClass( "paramBody inGrid" );
        inputGrid.setHeaderClass( "paramHeader" );
        inputGrid.setColumnClasses( "label, input, info, message" );

        HtmlOutputText headerText = new HtmlOutputText();
        ValueExpression inputTextVE = fc.getApplication().getExpressionFactory().createValueExpression( fc.getELContext(),
                                                                                                        "#{labels['inputParams']}",
                                                                                                        String.class );
        headerText.setValueExpression( "value", inputTextVE );
        inputGrid.getFacets().put( "header", headerText );

        for ( int i = 0; i < inputs.length; i++ ) {
            InputType input = inputs[i];
            String inputId = input.getId().toString();
            HtmlOutputLabel label = new HtmlOutputLabel();
            String labelId = getUniqueId();
            label.setId( labelId );
            label.setFor( inputId );
            label.setValue( input.getTitle().getString() );
            inputGrid.getChildren().add( label );

            int minOccurs = 0;
            try {
                minOccurs = Integer.parseInt( input.getMinOccurs() );
            } catch ( NumberFormatException e ) {
                // Nothing to DO (0 assumed)
            }

            int maxOccurs = 1;
            try {
                if ( "unbounded".equals( input.getMaxOccurs() ) ) {
                    maxOccurs = -1;
                } else {
                    maxOccurs = Integer.parseInt( input.getMaxOccurs() );
                }
            } catch ( NumberFormatException e ) {
                // Nothing to do (1 assumed)
            }
            switch ( input.getType() ) {
            case COMPLEX:
                if ( ( (ComplexInputType) input ).getDefaultFormat().getMimeType() != null
                     && ( (ComplexInputType) input ).getDefaultFormat().getMimeType().contains( "xml" ) ) {
                    inputGrid.getChildren().add( getXMLInput( fc, (ComplexInputType) input, minOccurs, maxOccurs ) );
                } else {
                    inputGrid.getChildren().add( getBinaryInput( fc, (ComplexInputType) input, minOccurs, maxOccurs ) );
                }
                break;
            case BBOX:
                inputGrid.getChildren().add( getBBoxInput( fc, (BBoxInputType) input, minOccurs, maxOccurs ) );
                break;
            case LITERAL:
                inputGrid.getChildren().add( getLiteralInput( fc, (LiteralInputType) input, minOccurs, maxOccurs ) );
                break;
            }
            inputGrid.getChildren().add( createInfoBt( ClientBean.IN_INFOKEY, input.getId().getCode() ) );

            // messages
            HtmlMessage msg = new HtmlMessage();
            msg.setId( getUniqueId() );
            msg.setShowSummary( true );
            msg.setShowDetail( true );
            msg.setFor( labelId );
            inputGrid.getChildren().add( msg );
        }
        parent.getChildren().add( inputGrid );
    }

    private HtmlFieldset getBinaryInput( FacesContext fc, ComplexInputType input, int minOccurs, int maxOccurs ) {
        HtmlFieldset fieldset = new HtmlFieldset();
        HtmlInputFile upload = new HtmlInputFile();
        upload.setId( input.getId().toString() );
        upload.setStyleClass( "inputField" );
        upload.setTarget( "upload" );
        String valueEL = "#{executeBean.binaryInputs['" + input.getId().toString() + "']}";
        ValueExpression valueVE = fc.getApplication().getExpressionFactory().createValueExpression( fc.getELContext(),
                                                                                                    valueEL,
                                                                                                    Object.class );

        upload.setValueExpression( "value", valueVE );
        upload.setRequired( minOccurs > 0 );

        // TODO: validation
        // FileMimeTypeValidator validator = new FileMimeTypeValidator();
        // ComplexFormat[] supportedFormats = ( (ComplexInputType) input ).getSupportedFormats();
        // for ( int j = 0; j < supportedFormats.length; j++ ) {
        // validator.addMimeType( supportedFormats[0].getMimeType() );
        // }
        // upload.addValidator( validator );
        if ( minOccurs > 0 ) {
            upload.setStyleClass( "required" );
        }
        fieldset.getChildren().add( upload );

        HtmlSelectFormat format = new HtmlSelectFormat();
        format.setId( input.getId().toString() + "_format" );
        format.setStyleClass( "selectFormatField" );
        format.setDefaultFormat( input.getDefaultFormat() );
        format.setConverter( new ComplexFormatConverter() );

        String valueFEL = "#{executeBean.complexFormats['" + input.getId().toString() + "']}";
        ValueExpression valueFVE = fc.getApplication().getExpressionFactory().createValueExpression( fc.getELContext(),
                                                                                                     valueFEL,
                                                                                                     Object.class );

        format.setValueExpression( "value", valueFVE );
        ComplexFormat[] supportedFormats = input.getSupportedFormats();
        for ( ComplexFormat complexFormat : supportedFormats ) {
            UISelectItem item = new UISelectItem();
            item.setItemLabel( complexFormat.getSchema() );
            item.setItemDescription( ComplexFormatConverter.getAsDesc( complexFormat ) );
            item.setItemValue( complexFormat );
            format.getChildren().add( item );
        }
        format.setConverter( new ComplexFormatConverter() );
        fieldset.getChildren().add( format );
        return fieldset;
    }

    private HtmlFieldset getXMLInput( FacesContext fc, ComplexInputType input, int minOccurs, int maxOccurs ) {
        HtmlFieldset fieldset = new HtmlFieldset();
        String id = input.getId().toString();
        fieldset.setId( id );
        String enableUpload = fc.getExternalContext().getInitParameter( "org.deegree.ENABLE_XML_FILEUPLOAD" );
        // FileUpload
        if ( Boolean.parseBoolean( enableUpload ) ) {
            HtmlInputFile upload = new HtmlInputFile();
            upload.setId( id + "_asFile" );
            upload.setStyleClass( "inputField" );
            upload.setTarget( "upload" );
            String valueEL = "#{executeBean.xmlInputs['" + id + "']}";
            ValueExpression valueVE = fc.getApplication().getExpressionFactory().createValueExpression( fc.getELContext(),
                                                                                                        valueEL,
                                                                                                        Object.class );

            upload.setValueExpression( "value", valueVE );
            // upload.setRequired( minOccurs > 0 );
            // if ( minOccurs > 0 ) {
            // upload.setStyleClass( "required" );
            // }

            // TODO: validation
            // FileMimeTypeValidator validator = new FileMimeTypeValidator();
            // ComplexFormat[] supportedFormats = ( (ComplexInputType) input ).getSupportedFormats();
            // for ( int j = 0; j < supportedFormats.length; j++ ) {
            // validator.addMimeType( supportedFormats[0].getMimeType() );
            // }
            // upload.addValidator( validator );
            fieldset.getChildren().add( upload );
        }

        String loadRef = fc.getExternalContext().getInitParameter( "org.deegree.LOAD_XML_REF" );
        if ( Boolean.parseBoolean( loadRef ) ) {
            HtmlPanelGrid refGrid = new HtmlPanelGrid();
            refGrid.setColumns( 1 );
            // or enter a URL as reference
            HtmlInputText text = new HtmlInputText();
            text.setId( id + "_asRef" );
            text.setStyleClass( "inputFieldText" );
            String valueTextEL = "#{executeBean.xmlRefInputs['" + id + "']}";
            ValueExpression valueTextVE = fc.getApplication().getExpressionFactory().createValueExpression( fc.getELContext(),
                                                                                                            valueTextEL,
                                                                                                            Object.class );
            text.setValueExpression( "value", valueTextVE );
            refGrid.getChildren().add( text );

            String source = fc.getExternalContext().getInitParameter( "org.deegree.XML_REF_SOURCE" );
            if ( source != null && source.trim().length() > 0 ) {
                String sourceId = "";
                String[] split = source.split( "," );
                if ( split != null && split.length > 0 ) {
                    HtmlSelectOneRadio sourceRadio = new HtmlSelectOneRadio();
                    sourceRadio.setId( id + "_radio" );
                    sourceRadio.setLayout( "pageDirection" );
                    for ( int i = 0; i < split.length; i++ ) {
                        String itemLabel = MessageUtils.getResourceText( "labels", "item_" + split[i] );
                        UISelectItem item = new UISelectItem();
                        item.setItemValue( split[i] );
                        item.setItemLabel( itemLabel );
                        sourceRadio.getChildren().add( item );
                    }
                    refGrid.getChildren().add( sourceRadio );
                    sourceId = sourceRadio.getClientId();
                }
                HtmlCommandButton loadBt = new HtmlCommandButton();
                String title = MessageUtils.getResourceText( "labels", "loadRefBt" );
                loadBt.setValue( title );
                loadBt.setOnclick( "loadReference('" + text.getClientId() + "', '" + sourceId + "'); return false;" );
                refGrid.getChildren().add( loadBt );
            }
            fieldset.getChildren().add( refGrid );
        }
        HtmlSelectFormat format = new HtmlSelectFormat();
        format.setId( input.getId().toString() + "_format" );
        format.setStyleClass( "selectFormatField" );
        format.setDefaultFormat( input.getDefaultFormat() );
        format.setConverter( new ComplexFormatConverter() );

        String valueFEL = "#{executeBean.complexFormats['" + input.getId().toString() + "']}";
        ValueExpression valueFVE = fc.getApplication().getExpressionFactory().createValueExpression( fc.getELContext(),
                                                                                                     valueFEL,
                                                                                                     Object.class );

        format.setValueExpression( "value", valueFVE );
        ComplexFormat[] supportedFormats = input.getSupportedFormats();
        for ( ComplexFormat complexFormat : supportedFormats ) {
            UISelectItem item = new UISelectItem();
            item.setItemLabel( complexFormat.getSchema() );
            item.setItemDescription( ComplexFormatConverter.getAsDesc( complexFormat ) );
            item.setItemValue( complexFormat );
            format.getChildren().add( item );
        }
        format.setConverter( new ComplexFormatConverter() );
        fieldset.getChildren().add( format );
        return fieldset;
    }

    private HtmlFieldset getBBoxInput( FacesContext fc, BBoxInputType input, int minOccurs, int maxOccurs ) {
        HtmlFieldset fieldset = new HtmlFieldset();
        HtmlInputBBox bbox = new HtmlInputBBox();
        bbox.setStyleClass( "bboxInput" );
        bbox.setId( input.getId().toString() );
        String valueEL = "#{executeBean.bboxInputs['" + input.getId().toString() + "']}";
        ValueExpression valueVE = fc.getApplication().getExpressionFactory().createValueExpression( fc.getELContext(),
                                                                                                    valueEL, BBox.class );

        bbox.setRequired( minOccurs > 0 );
        if ( minOccurs > 0 ) {
            bbox.setStyleClass( "required" );
        }
        bbox.setValueExpression( "value", valueVE );
        String[] supportedCrs = ( (BBoxInputType) input ).getSupportedCrs();
        for ( int j = 0; j < supportedCrs.length; j++ ) {
            UISelectItem crs = new UISelectItem();
            crs.setItemLabel( supportedCrs[j] );
            crs.setItemValue( supportedCrs[j] );
            bbox.getChildren().add( crs );
        }
        fieldset.getChildren().add( bbox );
        return fieldset;

    }

    private HtmlFieldset getLiteralInput( FacesContext fc, LiteralInputType input, int minOccurs, int maxOccurs ) {
        HtmlFieldset fieldset = new HtmlFieldset();
        HtmlLiteralInput literalInput = new HtmlLiteralInput();
        literalInput.setId( input.getId().toString() );
        literalInput.setStyleClass( "inputField" );
        String valueEL = "#{executeBean.literalInputs['" + input.getId().toString() + "']}";
        ValueExpression valueVE = fc.getApplication().getExpressionFactory().createValueExpression( fc.getELContext(),
                                                                                                    valueEL,
                                                                                                    Object.class );
        literalInput.setValueExpression( "value", valueVE );

        ValueWithRef[] supportedCrs = input.getSupportedUoms();
        for ( int j = 0; j < supportedCrs.length; j++ ) {
            UISelectItem uom = new UISelectItem();
            uom.setItemLabel( supportedCrs[j].getValue() );
            uom.setItemValue( supportedCrs[j].getValue() );
            literalInput.getChildren().add( uom );
        }
        if ( input.getDefaultUom() != null )
            literalInput.setDefaultUom( input.getDefaultUom().getValue() );
        if ( input.getAllowedValues() != null )
            literalInput.setAllowedValues( Arrays.asList( input.getAllowedValues() ) );
        fieldset.getChildren().add( literalInput );
        return fieldset;
    }

    private HtmlCommandButton createInfoBt( String type, String idCode ) {
        HtmlCommandButton infoBt = new HtmlCommandButton();
        infoBt.setId( getUniqueId() );
        infoBt.setImage( "resources/wpsclient/images/information_icon_small.png" );
        infoBt.setAlt( "Info" );

        ExpressionFactory ef = FacesContext.getCurrentInstance().getApplication().getExpressionFactory();
        String me = "#{clientBean.updateInfoText}";

        MethodExpression methodExpression = ef.createMethodExpression( FacesContext.getCurrentInstance().getELContext(),
                                                                       me, Object.class, new Class<?>[0] );
        infoBt.setActionExpression( methodExpression );

        UIParameter paramType = new UIParameter();
        paramType.setName( "type" );
        paramType.setValue( type );
        UIParameter param = new UIParameter();
        param.setName( "dataId" );
        param.setValue( idCode );
        infoBt.getChildren().add( paramType );
        infoBt.getChildren().add( param );

        AjaxBehavior ajaxB = new AjaxBehavior();
        List<String> render = new ArrayList<String>();
        render.add( ":infoOT" );
        ajaxB.setRender( render );
        infoBt.addClientBehavior( infoBt.getDefaultEventName(), ajaxB );
        return infoBt;
    }

    private HtmlCommandButton createAddBt( String type, String idCode ) {
        // TODO
        HtmlCommandButton addBt = new HtmlCommandButton();
        addBt.setId( getUniqueId() );
        addBt.setImage( "resources/wpsclient/images/add.png" );
        addBt.setAlt( "add" );

        AjaxBehavior ajaxB = new AjaxBehavior();
        List<String> render = new ArrayList<String>();
        render.add( ":emptyForm" );
        ajaxB.setRender( render );
        List<String> execute = new ArrayList<String>();
        execute.add( "@form" );
        ajaxB.setExecute( execute );
        addBt.addClientBehavior( addBt.getDefaultEventName(), ajaxB );

        return addBt;
    }

    private void setOutputParams( FacesContext fc, UIComponent parent, OutputType[] outputs ) {
        if ( outputs.length > 1 ) {
            HtmlPanelGrid outputGrid = new HtmlPanelGrid();
            outputGrid.setId( getUniqueId() );
            outputGrid.setStyleClass( "paramBody outGrid" );
            outputGrid.setHeaderClass( "paramHeader" );
            outputGrid.setColumns( 2 );
            HtmlOutputText headerText = new HtmlOutputText();
            headerText.setId( getUniqueId() );
            String headerTextEL = "#{labels['outputParams']}";
            ValueExpression outputTextVE = fc.getApplication().getExpressionFactory().createValueExpression( fc.getELContext(),
                                                                                                             headerTextEL,
                                                                                                             String.class );
            headerText.setValueExpression( "value", outputTextVE );
            outputGrid.getFacets().put( "header", headerText );

            HtmlSelectManyCheckbox cb = new HtmlSelectManyCheckbox();
            cb.setLayout( "pageDirection" );
            cb.setId( getUniqueId() );

            String valueEL = "#{executeBean.outputs}";
            ValueExpression valueVE = fc.getApplication().getExpressionFactory().createValueExpression( fc.getELContext(),
                                                                                                        valueEL,
                                                                                                        List.class );
            cb.setValueExpression( "value", valueVE );
            // cb.setRequired( true );
            for ( int i = 0; i < outputs.length; i++ ) {
                OutputType output = outputs[i];
                UISelectItem item = new UISelectItem();
                item.setItemLabel( output.getTitle().getString() );
                item.setItemValue( output.getId().toString() );
                if ( output.getAbstract() != null ) {
                    item.setItemDescription( output.getAbstract().toString() );
                }
                cb.getChildren().add( item );
            }

            outputGrid.getChildren().add( cb );
            // TODO!
            // outputGrid.getChildren().add( createInfoBt( "", ) );

            parent.getChildren().add( outputGrid );
        }
    }

    private static String getUniqueId() {
        return "id_" + UUID.randomUUID();
    }

    /**
     * @param executeForm
     *            the form component
     */
    public void setExecuteForm( HtmlForm executeForm ) {
        this.executeForm = executeForm;
    }

    /**
     * @return the form component
     */
    public HtmlForm getExecuteForm() {
        return executeForm;
    }

}

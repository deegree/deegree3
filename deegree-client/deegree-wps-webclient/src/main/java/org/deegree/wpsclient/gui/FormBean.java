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
import java.util.List;
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
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.MethodExpressionActionListener;

import org.deegree.client.core.component.HtmlInputBBox;
import org.deegree.client.core.component.HtmlInputFile;
import org.deegree.client.core.model.BBox;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.wps.client.input.type.BBoxInputType;
import org.deegree.protocol.wps.client.input.type.ComplexInputType;
import org.deegree.protocol.wps.client.input.type.InputType;
import org.deegree.protocol.wps.client.input.type.LiteralInputType;
import org.deegree.protocol.wps.client.output.type.OutputType;
import org.deegree.protocol.wps.client.process.Process;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
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
            MethodExpression action = fc.getApplication().getExpressionFactory().createMethodExpression(
                                                                                                         fc.getELContext(),
                                                                                                         buttonEL,
                                                                                                         null,
                                                                                                         new Class<?>[] { ActionEvent.class } );
            button.getAttributes().put( ExecuteBean.PROCESS_ATTRIBUTE_KEY, process );
            MethodExpressionActionListener listener = new MethodExpressionActionListener( action );

            button.addActionListener( listener );

            executeForm.getChildren().add( button );
        }
    }

    private void addInputParams( FacesContext fc, UIComponent parent, InputType[] inputDescription ) {
        HtmlPanelGrid inputGrid = new HtmlPanelGrid();
        inputGrid.setId( getUniqueId() );
        inputGrid.setColumns( 4 );
        inputGrid.setStyleClass( "paramBody" );
        inputGrid.setHeaderClass( "paramHeader" );
        HtmlOutputText inputText = new HtmlOutputText();
        String inputTextEL = "#{labels['inputParams']}";
        ValueExpression inputTextVE = fc.getApplication().getExpressionFactory().createValueExpression(
                                                                                                        fc.getELContext(),
                                                                                                        inputTextEL,
                                                                                                        String.class );
        inputText.setValueExpression( "value", inputTextVE );
        inputGrid.getFacets().put( "header", inputText );

        for ( int i = 0; i < inputDescription.length; i++ ) {
            InputType input = inputDescription[i];
            String inputId = input.getId().toString();
            HtmlOutputLabel label = new HtmlOutputLabel();
            String labelId = getUniqueId();
            label.setId( labelId );
            label.setFor( inputId );
            label.setValue( input.getTitle().getString() );
            inputGrid.getChildren().add( label );

            int minOccurs = 0;
            boolean isRequired = false;
            try {
                if ( Integer.parseInt( input.getMinOccurs() ) > 0 ) {
                    isRequired = true;
                }
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
            if ( input instanceof LiteralInputType ) {
                /*
                 * insert a composite component Application application = fc.getApplication(); Resource resource =
                 * application.getResourceHandler().createResource( "LiteralInput.xhtml", "wpsclient" ); UIComponent
                 * compositeComponent = application.createComponent( fc, resource ); compositeComponent.setId(
                 * "composite" ); ValueExpression value = application.getExpressionFactory().createValueExpression(
                 * fc.getELContext(), "#{executeBean.literalInputs['" + input.getId().toString() + "']}",
                 * LiteralInputType.class ); LiteralInputType lit = (LiteralInputType) input;
                 * 
                 * SimpleLiteralInput in = new SimpleLiteralInput(); in.setUom( lit.getDefaultUom().getRef() );
                 * literalInputs.put( input.getId(), in ); compositeComponent.setValueExpression( "value", value );
                 * 
                 * compositeComponent.getAttributes().put( "blub", "einTest" ); if ( lit.getDataType() != null ) {
                 * compositeComponent.getAttributes().put( "dataType", lit.getDataType() ); } if (
                 * lit.getSupportedUoms() != null ) { compositeComponent.getAttributes().put( "supportedUoms",
                 * lit.getSupportedUoms() ); } if ( lit.getAllowedValues() != null ) {
                 * compositeComponent.getAttributes().put( "allowedValues", lit.getAllowedValues() ); } if (
                 * lit.getRanges() != null ) { compositeComponent.getAttributes().put( "ranges", lit.getRanges() ); }
                 * 
                 * FaceletFactory factory = (FaceletFactory) RequestStateManager.get( fc,
                 * RequestStateManager.FACELET_FACTORY );
                 * 
                 * final UIComponent compositeRoot = application.createComponent( UIPanel.COMPONENT_TYPE );
                 * compositeRoot.setRendererType( "javax.faces.Group" );
                 * 
                 * try { Facelet f = factory.getFacelet( resource.getURL() ); f.apply( fc, compositeRoot );
                 * compositeComponent.getFacets().put( UIComponent.COMPOSITE_FACET_NAME, compositeRoot ); } catch (
                 * IOException ioex ) { throw new FaceletException( ioex ); }
                 * 
                 * inputGrid.getChildren().add( compositeComponent );
                 */

                HtmlInputText literalText = new HtmlInputText();
                literalText.setId( input.getId().toString() );
                literalText.setStyleClass( "inputField" );
                String valueEL = "#{executeBean.literalInputs['" + input.getId().toString() + "']}";
                ValueExpression valueVE = fc.getApplication().getExpressionFactory().createValueExpression(
                                                                                                            fc.getELContext(),
                                                                                                            valueEL,
                                                                                                            Object.class );

                literalText.setValueExpression( "value", valueVE );
                literalText.setRequired( isRequired );
                if ( isRequired ) {
                    literalText.setStyleClass( "required" );
                }
                inputGrid.getChildren().add( literalText );

            } else if ( input instanceof BBoxInputType ) {
                HtmlInputBBox bbox = new HtmlInputBBox();
                bbox.setId( inputId );
                String valueEL = "#{executeBean.bboxInputs['" + input.getId().toString() + "']}";
                ValueExpression valueVE = fc.getApplication().getExpressionFactory().createValueExpression(
                                                                                                            fc.getELContext(),
                                                                                                            valueEL,
                                                                                                            BBox.class );

                bbox.setRequired( isRequired );
                if ( isRequired ) {
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
                inputGrid.getChildren().add( bbox );
            } else if ( input instanceof ComplexInputType ) {
                HtmlInputFile upload = new HtmlInputFile();
                upload.setId( inputId );
                upload.setStyleClass( "inputField" );
                upload.setTarget( "upload" );
                String type = "binary";
                if ( ( (ComplexInputType) input ).getDefaultFormat().getMimeType() != null
                     && ( (ComplexInputType) input ).getDefaultFormat().getMimeType().contains( "xml" ) ) {
                    type = "xml";
                }
                String valueEL = "#{executeBean." + type + "Inputs['" + input.getId().toString() + "']}";
                ValueExpression valueVE = fc.getApplication().getExpressionFactory().createValueExpression(
                                                                                                            fc.getELContext(),
                                                                                                            valueEL,
                                                                                                            Object.class );

                upload.setValueExpression( "value", valueVE );
                upload.setRequired( isRequired );

                // FileMimeTypeValidator validator = new FileMimeTypeValidator();
                // ComplexFormat[] supportedFormats = ( (ComplexInputType) input ).getSupportedFormats();
                // for ( int j = 0; j < supportedFormats.length; j++ ) {
                // validator.addMimeType( supportedFormats[0].getMimeType() );
                // }
                // upload.addValidator( validator );
                if ( isRequired ) {
                    upload.setStyleClass( "required" );
                }
                inputGrid.getChildren().add( upload );
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

    private HtmlCommandButton createInfoBt( String type, String idCode ) {
        // info button
        HtmlCommandButton infoBt = new HtmlCommandButton();
        infoBt.setId( getUniqueId() );
        infoBt.setImage( "resources/wpsclient/images/information_icon_small.png" );

        ExpressionFactory ef = FacesContext.getCurrentInstance().getApplication().getExpressionFactory();
        String me = "#{clientBean.updateInfoText}";

        MethodExpression methodExpression = ef.createMethodExpression(
                                                                       FacesContext.getCurrentInstance().getELContext(),
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

    private void setOutputParams( FacesContext fc, UIComponent parent, OutputType[] outputs ) {
        if ( outputs.length > 1 ) {
            HtmlPanelGrid outputGrid = new HtmlPanelGrid();
            outputGrid.setId( getUniqueId() );
            outputGrid.setStyleClass( "paramBody" );
            outputGrid.setHeaderClass( "paramHeader" );
            outputGrid.setColumns( 2 );
            HtmlOutputText outputText = new HtmlOutputText();
            outputText.setId( getUniqueId() );
            String outputTextEL = "#{labels['outputParams']}";
            ValueExpression outputTextVE = fc.getApplication().getExpressionFactory().createValueExpression(
                                                                                                             fc.getELContext(),
                                                                                                             outputTextEL,
                                                                                                             String.class );
            outputText.setValueExpression( "value", outputTextVE );
            outputGrid.getFacets().put( "header", outputText );

            HtmlSelectManyCheckbox cb = new HtmlSelectManyCheckbox();
            cb.setLayout( "pageDirection" );
            cb.setId( getUniqueId() );
            String valueEL = "#{executeBean.outputs}";
            ValueExpression valueVE = fc.getApplication().getExpressionFactory().createValueExpression(
                                                                                                        fc.getELContext(),
                                                                                                        valueEL,
                                                                                                        List.class );
            cb.setValueExpression( "value", valueVE );
            cb.setRequired( true );
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

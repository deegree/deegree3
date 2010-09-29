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

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectItem;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlMessage;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlSelectManyCheckbox;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.MethodExpressionActionListener;

import org.deegree.client.core.component.HtmlInputBBox;
import org.deegree.client.core.component.HtmlInputFile;
import org.deegree.client.core.model.BBox;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.protocol.wps.client.WPSClient;
import org.deegree.protocol.wps.client.input.type.BBoxInputType;
import org.deegree.protocol.wps.client.input.type.ComplexInputType;
import org.deegree.protocol.wps.client.input.type.InputType;
import org.deegree.protocol.wps.client.input.type.LiteralInputType;
import org.deegree.protocol.wps.client.output.type.OutputType;
import org.deegree.protocol.wps.client.process.Process;

/**
 * <code>ClientBean</code> handles all selections/entries made in the GUI which leads to changes in the GUI.
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
@ManagedBean
@SessionScoped
public class ClientBean implements Serializable {

    private static final long serialVersionUID = -1434783003208250369L;

    private String url = "http://deegree3-testing.deegree.org/deegree-wps-demo/services";

    // private String url = "http://localhost:8080/deegree-wps-demo/services";

    private WPSClient wpsClient;

    private List<Process> processes = new ArrayList<Process>();

    private CodeType process;

    private Process selectedProcess;

    // private Map<CodeType, SimpleLiteralInput> literalInputs = new HashMap<CodeType, SimpleLiteralInput>();

    /**
     * change the URL of the WPS and update the list of processes
     * 
     * @param event
     * @throws AbortProcessingException
     */
    public void selectWPS( AjaxBehaviorEvent event )
                            throws AbortProcessingException {
        FacesContext fc = FacesContext.getCurrentInstance();
        processes.clear();
        try {
            URL capUrl = new URL( url + "?service=WPS&version=1.0.0&request=GetCapabilities" );
            wpsClient = new WPSClient( capUrl );
            FacesMessage msg = getFacesMessage( FacesMessage.SEVERITY_INFO, "INFO.SELECT_WPS", url );
            fc.addMessage( "WPSBean.selectWPS.SELECT_WPS", msg );
            Process[] p = wpsClient.getProcesses();
            processes.addAll( (List<Process>) Arrays.asList( p ) );
        } catch ( MalformedURLException e ) {
            FacesMessage msg = getFacesMessage( FacesMessage.SEVERITY_ERROR, "ERROR.INVALID_URL", url );
            fc.addMessage( "WPSBean.selectWPS.INVALID_URL", msg );
        } catch ( Exception e ) {
            FacesMessage msg = getFacesMessage( FacesMessage.SEVERITY_ERROR, "ERROR.INVALID_WPS", url, e.getMessage() );
            fc.addMessage( "WPSBean.selectWPS.INVALID_WPS", msg );
        }
    }

    /**
     * updates the gui, which depends on the selected process
     * 
     * @param event
     * @throws AbortProcessingException
     */
    public void selectProcess( AjaxBehaviorEvent event )
                            throws AbortProcessingException {
        FacesContext fc = FacesContext.getCurrentInstance();
        if ( wpsClient == null ) {
            FacesMessage msg = getFacesMessage( FacesMessage.SEVERITY_ERROR, "ERROR.NO_URL" );

            fc.addMessage( "WPSBean.selectProcess.NO_WPS", msg );
            return;
        }
        if ( process == null ) {
            FacesMessage msg = getFacesMessage( FacesMessage.SEVERITY_ERROR, "ERROR.NO_PROCESS" );
            fc.addMessage( "WPSBean.selectProcess.NO_Process", msg );
            return;
        }
        selectedProcess = wpsClient.getProcess( process.getCode(), process.getCodeSpace() );
        if ( selectedProcess == null ) {
            FacesMessage msg = getFacesMessage( FacesMessage.SEVERITY_WARN, "WARN.NO_PROCESS_WITH_ID", url, process );
            fc.addMessage( "WPSBean.selectProcess.NO_PROCESS_FOR_ID", msg );
            return;
        }
        createForm( selectedProcess );
        FacesMessage msg = getFacesMessage( FacesMessage.SEVERITY_INFO, "INFO.SELECT_PROCESS", selectedProcess.getId() );
        fc.addMessage( "WPSBean.selectProcess.SELECT_PROCESS", msg );

    }

    private void createForm( Process process ) {
        FacesContext fc = FacesContext.getCurrentInstance();
        UIComponent executeForm = fc.getViewRoot().findComponent( "emptyForm" );
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

    private void addInputParams( FacesContext fc, UIComponent parent, InputType[] inputDescription ) {
        HtmlPanelGrid inputGrid = new HtmlPanelGrid();
        inputGrid.setId( getUniqueId() );
        inputGrid.setColumns( 3 );
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
            HtmlOutputText label = new HtmlOutputText();
            String labelId = getUniqueId();
            label.setId( labelId );
            label.setValue( input.getTitle().getString() );
            inputGrid.getChildren().add( label );
            String minOccurs = input.getMinOccurs();
            boolean isRequired = false;
            try {
                if ( Integer.parseInt( minOccurs ) > 0 ) {
                    isRequired = true;
                }
            } catch ( NumberFormatException e ) {
                // Nothing to DO (1 assumed)
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
                upload.setId( input.getId().toString() );
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

            HtmlMessage msg = new HtmlMessage();
            msg.setId( getUniqueId() );
            msg.setShowSummary( true );
            msg.setShowDetail( true );
            msg.setFor( labelId );
            inputGrid.getChildren().add( msg );
        }
        parent.getChildren().add( inputGrid );
    }

    private void setOutputParams( FacesContext fc, UIComponent parent, OutputType[] outputs ) {
        if ( outputs.length > 1 ) {
            HtmlPanelGrid outputGrid = new HtmlPanelGrid();
            outputGrid.setId( getUniqueId() );
            outputGrid.setStyleClass( "paramBody" );
            outputGrid.setHeaderClass( "paramHeader" );
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
                UISelectItem item = new UISelectItem();
                item.setItemLabel( outputs[i].getTitle().getString() );
                item.setItemValue( outputs[i].getId().toString() );
                if ( outputs[i].getAbstract() != null ) {
                    item.setItemDescription( outputs[i].getAbstract().toString() );
                }
                cb.getChildren().add( item );
            }
            outputGrid.getChildren().add( cb );
            parent.getChildren().add( outputGrid );
        }
    }

    private static String getUniqueId() {
        return "id_" + UUID.randomUUID();
    }

    /******************* GETTER / SETTER ******************/

    /**
     * @param url
     *            the URL of the WPS
     */
    public void setUrl( String url ) {
        this.url = url;
    }

    /**
     * @return the URL of the WPS
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param process
     *            the process to execute
     */
    public void setProcess( CodeType process ) {
        this.process = process;

    }

    /**
     * @return the process to execute
     */
    public CodeType getProcess() {
        return process;
    }

    /**
     * @return a list of available processes
     */
    public List<Process> getProcesses() {
        return processes;
    }

    // public void setLiteralInputs( Map<CodeType, SimpleLiteralInput> literalInputs ) {
    // this.literalInputs = literalInputs;
    // }
    //
    // public Map<CodeType, SimpleLiteralInput> getLiteralInputs() {
    // return literalInputs;
    // }

}

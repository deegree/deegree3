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
package org.deegree.client.mdeditor.gui;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AjaxBehaviorEvent;

import org.deegree.client.mdeditor.model.DataGroup;
import org.deegree.client.mdeditor.model.FormElement;
import org.deegree.client.mdeditor.model.FormField;
import org.deegree.client.mdeditor.model.FormGroup;
import org.deegree.client.mdeditor.model.VALIDATION_TYPE;
import org.deegree.client.mdeditor.model.ValidationResult;
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
@SessionScoped
public class ValidationBean implements Serializable {

    private static final long serialVersionUID = 2442014860455198457L;

    private static final Logger LOG = getLogger( ValidationBean.class );

    private List<ValidationResult> validationResult = new ArrayList<ValidationResult>();

    public void validateDataset( AjaxBehaviorEvent event )
                            throws AbortProcessingException {
        LOG.debug( "Validate dataset" );
        validationResult.clear();
        FacesContext fc = FacesContext.getCurrentInstance();
        FormFieldBean formFieldBean = (FormFieldBean) fc.getApplication().getELResolver().getValue( fc.getELContext(),
                                                                                                    null,
                                                                                                    "formFieldBean" );
        List<FormGroup> formGroups = formFieldBean.getFormGroups();
        for ( FormGroup formGroup : formGroups ) {
            List<String> formFieldResult = null;
            if ( !formGroup.isReferenced() ) {
                if ( formGroup.getOccurence() != 1 ) {
                    List<DataGroup> dataGroups = formFieldBean.getDataGroups( formGroup.getId() );
                    // TODO
                } else {
                    formFieldResult = validateFormFields( fc, formGroup );
                }
            }

            if ( formFieldResult != null && formFieldResult.size() > 0 ) {
                validationResult.add( new ValidationResult( formGroup.getLabel(), formGroup.getId(), formFieldResult ) );
            }
        }
    }

    public List<String> validateFormFields( FacesContext fc, FormGroup formGroup ) {
        List<String> msgs = new ArrayList<String>();
        for ( FormElement fe : formGroup.getFormElements() ) {
            if ( fe instanceof FormGroup ) {
                msgs.addAll( validateFormFields( fc, (FormGroup) fe ) );
            } else {
                FormField ff = (FormField) fe;
                Map<VALIDATION_TYPE, String[]> validationMap = ff.validate();
                addValidationMsg( fc, msgs, validationMap, ff.getLabel() );
            }
        }
        return msgs;
    }

    private void addValidationMsg( FacesContext fc, List<String> msgs, Map<VALIDATION_TYPE, String[]> validationMap,
                                   String label ) {
        for ( VALIDATION_TYPE key : validationMap.keySet() ) {
            msgs.add( GuiUtils.getResourceText( fc, "mdLabels", "invalid_" + key, label, validationMap.get( key ) ) );
        }
    }

    public List<ValidationResult> getValidationResult() {
        return validationResult;
    }

}

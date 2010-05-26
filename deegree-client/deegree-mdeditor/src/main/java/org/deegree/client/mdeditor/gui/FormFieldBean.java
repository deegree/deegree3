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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AjaxBehaviorEvent;
import javax.servlet.http.HttpSession;

import org.deegree.client.mdeditor.config.ConfigurationException;
import org.deegree.client.mdeditor.config.FormConfigurationFactory;
import org.deegree.client.mdeditor.model.FormConfiguration;
import org.deegree.client.mdeditor.model.FormElement;
import org.deegree.client.mdeditor.model.FormField;
import org.deegree.client.mdeditor.model.FormFieldPath;
import org.deegree.client.mdeditor.model.FormGroup;
import org.deegree.client.mdeditor.model.FormGroupInstance;
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
public class FormFieldBean implements Serializable {

    private static final long serialVersionUID = 6057122120736649423L;

    private static final Logger LOG = getLogger( FormFieldBean.class );

    private List<FormGroup> formGroups = new ArrayList<FormGroup>();

    private Map<String, FormField> formFields = new HashMap<String, FormField>();

    public FormFieldBean() {
        forceReloaded();
    }

    public void saveValue( AjaxBehaviorEvent event )
                            throws AbortProcessingException {
        UIInput input = (UIInput) event.getSource();

        FormFieldPath path = (FormFieldPath) input.getAttributes().get( GuiUtils.FIELDPATH_ATT_KEY );

        if ( path == null ) {
            LOG.error( "Can not save value for field " + path + ": groupId or fieldId are null" );
        }

        path.resetIterator();

        String fgId = path.next();
        FormField ffToUpdate = null;
        for ( FormGroup fg : formGroups ) {
            if ( fgId.equals( fg.getId() ) ) {
                ffToUpdate = getFormField( fg.getFormElements(), path );
            }
        }
        if ( ffToUpdate != null ) {
            Object value = input.getValue();
            LOG.debug( "Update element with id " + path + ". New Value is " + value + "." );
            ffToUpdate.setValue( value );
        }
    }

    private FormField getFormField( List<FormElement> fes, FormFieldPath path ) {
        if ( path.hasNext() ) {
            String next = path.next();
            for ( FormElement fe : fes ) {
                if ( next.equals( fe.getId() ) ) {
                    if ( fe instanceof FormGroup ) {
                        return getFormField( ( (FormGroup) fe ).getFormElements(), path );
                    } else {
                        return (FormField) fe;
                    }
                }
            }
        }
        return null;
    }

    public List<FormGroup> getFormGroups() {
        return formGroups;
    }

    public FormGroup getFormGroup( String id ) {
        for ( FormGroup fg : formGroups ) {
            if ( id.equals( fg.getId() ) ) {
                return fg;
            }
        }
        return null;
    }

    public Map<String, FormField> getFormFields() {
        return formFields;
    }

    public void setFormFields( Map<String, FormField> formFields ) {
        this.formFields = formFields;
    }

    public void forceReloaded() {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) fc.getExternalContext().getSession( false );
        try {
            FormConfiguration manager = FormConfigurationFactory.getOrCreateFormConfiguration( session.getId() );
            formGroups = manager.getFormGroups();
            formFields = manager.getFormFields();
        } catch ( ConfigurationException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setValues( String grpId, FormGroupInstance fgi ) {
        for ( FormGroup fg : formGroups ) {
            if ( grpId.equals( fg.getId() ) ) {
                setValues( fg, fgi.getValues() );
            }
        }
    }

    private void setValues( FormGroup fg, Map<String, Object> values ) {
        LOG.debug( "update form group with id " + fg.getId() );
        for ( FormElement fe : fg.getFormElements() ) {
            if ( fe instanceof FormGroup ) {
                setValues( ( (FormGroup) fe ), values );
            } else if ( fe instanceof FormField ) {
                FormField ff = (FormField) fe;
                ff.setValue( values.get( ff.getPath().toString() ) );
            }
        }
    }
}

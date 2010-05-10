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

import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.deegree.client.mdeditor.controller.DatasetReader;
import org.deegree.client.mdeditor.controller.FormGroupInstanceReader;
import org.deegree.client.mdeditor.model.FormField;

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
public class LoadDatasetBean {

    private String selectedDataset;

    public List<String> getDatasets() {
        return FormGroupInstanceReader.getDatasets();
    }

    public void setSelectedDataset( String selectedDataset ) {
        this.selectedDataset = selectedDataset;
    }

    public String getSelectedDataset() {
        return selectedDataset;
    }

    public Object loadDataset() {
        FacesContext fc = FacesContext.getCurrentInstance();
        String id = selectedDataset;
        if ( selectedDataset == null || selectedDataset.length() == 0 ) {
            FacesMessage msg = GuiUtils.getFacesMessage( fc, FacesMessage.SEVERITY_FATAL, "ERROR.LOAD.INVALID_ID", id );
            fc.addMessage( "LOAD_FAILED_INVALID_ID", msg );
            return "/page/form/errorPage.xhtml";
        }

        Map<String, Object> values;
        try {
            values = DatasetReader.readDataset( id );
        } catch ( Exception e ) {
            FacesMessage msg = GuiUtils.getFacesMessage( fc, FacesMessage.SEVERITY_FATAL, "ERROR.LOAD", e.getMessage(),
                                                         id, e.getMessage() );
            fc.addMessage( "LOAD_FAILED", msg );
            return "/page/form/errorPage.xhtml";
        }

        fc.getELContext();
        FormFieldBean formfieldBean = (FormFieldBean) fc.getApplication().getELResolver().getValue( fc.getELContext(),
                                                                                                    null,
                                                                                                    "formFieldBean" );
        Map<String, FormField> formFields = formfieldBean.getFormFields();
        for ( String path : values.keySet() ) {
            if ( formFields.containsKey( path ) ) {
                formFields.get( path ).setValue( values.get( path ) );
            }
        }

        FacesMessage msg = GuiUtils.getFacesMessage( fc, FacesMessage.SEVERITY_INFO, "SUCCESS.LOAD", id );
        fc.addMessage( "LOAD_SUCCESS", msg );
        return "/page/form/successPage.xhtml";
    }

}

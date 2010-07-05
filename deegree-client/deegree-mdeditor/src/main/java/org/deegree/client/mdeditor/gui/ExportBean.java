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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AjaxBehaviorEvent;

import org.deegree.client.mdeditor.configuration.ConfigurationException;
import org.deegree.client.mdeditor.configuration.ConfigurationManager;
import org.deegree.client.mdeditor.io.DataIOException;
import org.deegree.client.mdeditor.mapping.SchemaManager;
import org.deegree.client.mdeditor.model.FormConfiguration;
import org.deegree.client.mdeditor.model.FormField;
import org.deegree.client.mdeditor.model.FormFieldPath;
import org.deegree.client.mdeditor.model.mapping.MappingInformation;
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
public class ExportBean {

    private static final Logger LOG = getLogger( ExportBean.class );

    private String selectedMapping;

    private String resultLabel;

    private String resultLink;

    public void exportDataset( AjaxBehaviorEvent event )
                            throws AbortProcessingException, ConfigurationException {
        LOG.debug( "Export dataset; id of the selected mapping: " + selectedMapping );
        FacesContext fc = FacesContext.getCurrentInstance();
        FormFieldBean formfieldBean = (FormFieldBean) fc.getApplication().getELResolver().getValue( fc.getELContext(),
                                                                                                    null,
                                                                                                    "formFieldBean" );
        Map<String, FormField> formFields = formfieldBean.getFormFields();
        FormConfiguration configuration = ConfigurationManager.getConfiguration().getSelectedFormConfiguration();

        FormFieldPath pathToIdentifier = configuration.getPathToIdentifier();
        Object value = formFields.get( pathToIdentifier.toString() ).getValue();
        String id = null;
        if ( value != null ) {
            id = String.valueOf( value );
        }

        try {
            String fileName = SchemaManager.export( id, selectedMapping, formFields, formfieldBean.getDataGroups() );

            ExternalContext ctx = FacesContext.getCurrentInstance().getExternalContext();

            resultLink = ctx.getRequestContextPath() + File.separatorChar + "download" + File.separatorChar + fileName;
            resultLabel = fileName;
        } catch ( DataIOException e ) {
            FacesMessage msg = GuiUtils.getFacesMessage( fc, FacesMessage.SEVERITY_FATAL, "ERROR.EXPORT_DATASET",
                                                         e.getMessage() );
            fc.addMessage( "EXPORT_FAILED", msg );
        }
    }

    public void setSelectedMapping( String selectedMapping ) {
        this.selectedMapping = selectedMapping;
    }

    public String getSelectedMapping() {
        return selectedMapping;
    }

    public List<MappingInformation> getMappings() {
        try {
            FormConfiguration configuration = ConfigurationManager.getConfiguration().getSelectedFormConfiguration();
            return SchemaManager.getMappings( configuration.getMappingURLs() );
        } catch ( ConfigurationException e ) {
            LOG.debug( "Could not read mappings", e );
            LOG.error( "Could not read mappings", e.getMessage() );
        }
        return new ArrayList<MappingInformation>();
    }

    public String getResultLabel() {
        return resultLabel;
    }

    public String getResultLink() {
        return resultLink;
    }

}

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.deegree.client.mdeditor.config.ConfigurationException;
import org.deegree.client.mdeditor.config.FormConfigurationFactory;
import org.deegree.client.mdeditor.controller.FormGroupHandler;
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
public class FormGroupInstanceBean implements Serializable {

    private static final long serialVersionUID = -6705118936818062292L;

    private static final Logger LOG = getLogger( FormGroupInstanceBean.class );

    private Map<String, List<FormGroupInstance>> formGroupInstances = new HashMap<String, List<FormGroupInstance>>();

    private Map<String, String> selectedInstances = new HashMap<String, String>();

    private boolean fgiLoaded = false;

    public Map<String, List<FormGroupInstance>> getFormGroupInstances() {
        if ( !fgiLoaded ) {
            loadFormGropupInstances();
            fgiLoaded = true;
        }
        return formGroupInstances;
    }

    public void setSelectedInstances( Map<String, String> selectedInstances ) {
        this.selectedInstances = selectedInstances;
    }

    public Map<String, String> getSelectedInstances() {
        return selectedInstances;
    }

    public void addSelectedInstances( String groupId, String fileName ) {
        selectedInstances.put( groupId, fileName );
    }

    public void reloadFormGroup( String grpId ) {
        LOG.debug( "Form Group with id " + grpId + " has changed. Force reload." );
        formGroupInstances.put( grpId, FormGroupHandler.getFormGroupInstances( grpId ) );
    }

    // TODO
    private void loadFormGropupInstances() {
        HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession( false );
        try {
            List<String> formGroupIds = FormConfigurationFactory.getOrCreateFormConfiguration( session.getId() ).getReferencedFormGroupIds();
            for ( String id : formGroupIds ) {
                reloadFormGroup( id );
            }
        } catch ( ConfigurationException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}

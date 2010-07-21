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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.CustomScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIForm;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;

import org.deegree.client.mdeditor.configuration.ConfigurationException;
import org.deegree.client.mdeditor.configuration.ConfigurationManager;
import org.deegree.client.mdeditor.gui.components.ListGroup;
import org.deegree.client.mdeditor.io.Utils;
import org.deegree.client.mdeditor.model.DataGroup;
import org.deegree.client.mdeditor.model.FormConfiguration;
import org.deegree.client.mdeditor.model.FormElement;
import org.deegree.client.mdeditor.model.FormField;
import org.deegree.client.mdeditor.model.FormFieldPath;
import org.deegree.client.mdeditor.model.FormGroup;
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
@CustomScoped(value = "#{selectConfigurationBean.map}")
public class EditorBean {

    private String confId;

    private String grpId;

    private UIForm form;

    private ListGroup listGroup;

    private static final Logger LOG = getLogger( EditorBean.class );

    private List<FormGroup> formGroups = new ArrayList<FormGroup>();

    private Map<String, FormField> formFields = new HashMap<String, FormField>();

    // form group id, data groups
    private Map<String, List<DataGroup>> dataGroups = new HashMap<String, List<DataGroup>>();

    public EditorBean( String confId ) throws ConfigurationException {
        this.confId = confId;
        forceReloaded();
    }

    public String getConfId() {
        return confId;
    }

    public void setGrpId( String grpId ) {
        this.grpId = grpId;
    }

    public String getGrpId() {
        return grpId;
    }

    public void setForm( UIForm form ) {
        this.form = form;
    }

    public UIForm getForm() {
        return form;
    }

    public void setListGroup( ListGroup listGroup ) {
        this.listGroup = listGroup;
    }

    public ListGroup getListGroup() {
        return listGroup;
    }

    public void load( ComponentSystemEvent event )
                            throws AbortProcessingException, ConfigurationException {
        form.getChildren().clear();
        HtmlPanelGrid grid = GuiStore.getForm( confId, grpId );
        form.getChildren().add( grid );

        Object id = grid.getAttributes().get( GuiUtils.GROUPID_ATT_KEY );
        if ( id != null ) {
            setGrpId( (String) id );
        }
    }

    /**
     * 
     * @return a map of all form fields; the key of the map contains the form field path of the form field
     */
    public Map<String, FormField> getFormFields() {
        return formFields;
    }

    /**
     * 
     * @param formFields
     *            a map of the form fields to set; the key of the map contains the form field path of the form field
     */
    public void setFormFields( Map<String, FormField> formFields ) {
        this.formFields = formFields;
    }

    /**
     * @return a list of all form groups
     */
    public List<FormGroup> getFormGroups() {
        return formGroups;
    }

    /**
     * @param id
     *            the id of the form group to return
     * @return the form group with the given id; null, if a form group with the given id does not exist
     */
    public FormGroup getFormGroup( String id ) {
        for ( FormGroup fg : formGroups ) {
            if ( id.equals( fg.getId() ) ) {
                return fg;
            }
        }
        return null;
    }

    /**
     * Reload form groups and form fields from configuration
     * 
     * @param confId
     * 
     * @throws ConfigurationException
     */
    public void forceReloaded()
                            throws ConfigurationException {
        FormConfiguration manager = ConfigurationManager.getConfiguration().getConfiguration( confId );
        formGroups = manager.getFormGroups();
        formFields = manager.getFormFields();
    }

    /**
     * @param grpId
     *            the id of the data group
     * @param dataGroup
     *            the values to set
     */
    public void setValues( String grpId, DataGroup dataGroup ) {
        for ( FormGroup fg : formGroups ) {
            if ( grpId.equals( fg.getId() ) ) {
                setValues( fg, dataGroup.getValues() );
            }
        }
    }

    /**
     * @param path
     *            the path of the form field
     * @param value
     *            the value to set
     */
    public void setValue( FormFieldPath path, Object value ) {
        FormField ffToUpdate = getFormField( path );
        if ( ffToUpdate != null ) {
            LOG.debug( "Update element with id " + path + ". New Value is " + value + "." );
            ffToUpdate.setValue( value );
        }
    }

    /**
     * Sets the values of all form fields to the default value.
     */
    public void clearFormFields() {
        for ( FormGroup fg : formGroups ) {
            fg.reset();
        }
        dataGroups.clear();
    }

    /**
     * @return a map of all data groups; the key of the map contains the form group id
     */
    public Map<String, List<DataGroup>> getDataGroups() {
        return dataGroups;
    }

    /**
     * @return a map containing all data groups and formFields as datagroups; the key of the map contains the form group
     *         id
     */
    public Map<String, List<DataGroup>> getAllDataGroups() {
        Map<String, List<DataGroup>> dgs = new HashMap<String, List<DataGroup>>();
        dgs.putAll( dataGroups );
        for ( FormGroup fg : formGroups ) {
            String grpId = fg.getId();
            if ( !dgs.containsKey( grpId ) ) {
                dgs.put( grpId, new ArrayList<DataGroup>() );
            }
            String newId = Utils.createId();
            DataGroup newDataGrp = new DataGroup( newId );
            dgs.get( grpId ).add( newDataGrp );

            // set values
            Map<String, Object> values = getValuesAsMap( getFormGroup( grpId ) );
            for ( DataGroup dg : dgs.get( grpId ) ) {
                if ( newId.equals( dg.getId() ) ) {
                    dg.setValues( values );
                    break;
                }
            }
            
        }
        return dgs;
    }

    /**
     * @param grpId
     *            the id of the form group to return
     * @return a list of the data groups of the form group with the given id; an empty list, if no data group for the
     *         form group exist
     */
    public List<DataGroup> getDataGroups( String grpId ) {
        if ( dataGroups.containsKey( grpId ) ) {
            return dataGroups.get( grpId );
        }
        return new ArrayList<DataGroup>();
    }

    /**
     * @param grpId
     *            the id of the form group
     * @param id
     *            the id of the data group
     * @return the data group with the given id assigned to the form group with the given grpId
     */
    public DataGroup getDataGroup( String grpId, String id ) {
        if ( id != null && dataGroups.containsKey( grpId ) ) {
            for ( DataGroup dg : dataGroups.get( grpId ) ) {
                if ( id.equals( dg.getId() ) ) {
                    return dg;
                }
            }
        }
        return null;
    }

    /**
     * Removes the data group with the given id assigned to the form group with the given grpId
     * 
     * @param grpId
     *            the id of the form group
     * @param id
     *            the id of the datagroup
     * */
    public void removeDataGroup( String grpId, String id ) {
        if ( id == null ) {
            return;
        }
        if ( dataGroups.containsKey( grpId ) ) {
            for ( DataGroup dg : dataGroups.get( grpId ) ) {
                if ( id.equals( dg.getId() ) ) {
                    dataGroups.get( grpId ).remove( dg );
                    break;
                }
            }
        }
    }

    /**
     * Passes the current values of the form fields assigned to the form group with the given id to the list of data
     * groups; If a data group with the given id exist, the values will be overwritten, otherwise a new data group will
     * be created.
     * 
     * @param grpId
     *            the id of the form group
     * @param id
     *            the id of the data group; if null, a new id will be created
     * @return the id of the stored data group
     */
    public String saveDataGroup( String grpId, String id ) {
        if ( !dataGroups.containsKey( grpId ) ) {
            dataGroups.put( grpId, new ArrayList<DataGroup>() );
        }
        // add new datagroup, if id is null
        String newId = id;
        if ( newId == null ) {
            newId = Utils.createId();
            DataGroup dg = new DataGroup( newId );
            dataGroups.get( grpId ).add( dg );
        }
        // set values
        Map<String, Object> values = getValuesAsMap( getFormGroup( grpId ) );
        for ( DataGroup dg : dataGroups.get( grpId ) ) {
            if ( newId.equals( dg.getId() ) ) {
                dg.setValues( values );
                break;
            }
        }
        return newId;
    }

    /**
     * Sets the values of the form fields to the values of the data group identified with the given grpId and id
     * 
     * @param grpId
     *            the id of the form group
     * @param id
     *            the id of the data group
     */
    public void resetToDataGroup( String grpId, String id ) {
        if ( id == null ) {
            return;
        }
        if ( dataGroups.containsKey( grpId ) ) {
            for ( DataGroup dg : dataGroups.get( grpId ) ) {
                if ( id.equals( dg.getId() ) ) {
                    setValues( grpId, dg );
                    break;
                }
            }
        }
    }

    /**
     * @param datagroups
     *            the data groups to put in the list of data groups
     */
    public void setDataGroups( Map<String, List<DataGroup>> datagroups ) {
        dataGroups.putAll( datagroups );
    }

    /**
     * Resets the form field with the given path.
     * 
     * @param path
     *            the path to identify the form field
     */
    public void clearFormField( FormFieldPath path ) {
        FormField formField = getFormField( path );
        if ( formField != null ) {
            formField.reset();
        }
    }

    public boolean isGlobal()
                            throws ConfigurationException {
        return ConfigurationManager.getConfiguration().isGlobal( confId );
    }

    private FormField getFormField( FormFieldPath path ) {
        FormField formField = null;
        if ( path != null ) {
            path.resetIterator();
            String fgId = path.next();
            for ( FormGroup fg : formGroups ) {
                if ( fgId.equals( fg.getId() ) ) {
                    formField = getFormField( fg.getFormElements(), path );
                }
            }
        }
        return formField;
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

    private Map<String, Object> getValuesAsMap( FormGroup fg ) {
        Map<String, Object> values = new HashMap<String, Object>();
        if ( fg != null ) {
            for ( FormElement fe : fg.getFormElements() ) {
                if ( fe instanceof FormField ) {
                    FormField ff = (FormField) fe;
                    values.put( ff.getPath().toString(), ff.getValue() );
                } else if ( fe instanceof FormGroup ) {
                    values.putAll( getValuesAsMap( (FormGroup) fe ) );
                }
            }
        }
        return values;
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

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
package org.deegree.client.mdeditor.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class FormConfiguration {

    private List<FormGroup> formGroups = new ArrayList<FormGroup>();

    private LAYOUT_TYPE layoutType;

    private FormFieldPath pathToIdentifier;

    public FormConfiguration( List<FormGroup> formGroups, LAYOUT_TYPE layoutType, FormFieldPath pathToIdentifier ) {
        this.formGroups = formGroups;
        this.layoutType = layoutType;
        this.pathToIdentifier = pathToIdentifier;
    }

    /**
     * @return a list of all top level formGroups
     */
    public List<FormGroup> getFormGroups() {
        return formGroups;
    }

    /**
     * @return the layout type
     */
    public LAYOUT_TYPE getLayoutType() {
        return layoutType;
    }

    /**
     * @return the identifer
     */
    public FormFieldPath getPathToIdentifier() {
        return pathToIdentifier;
    }

    /**
     * @return a list of all form fields
     */
    public Map<String, FormField> getFormFields() {
        Map<String, FormField> formFields = new HashMap<String, FormField>();
        for ( FormGroup fg : formGroups ) {
            addFormField( formFields, fg );
        }
        return formFields;
    }

    private void addFormField( Map<String, FormField> formFields, FormGroup fg ) {
        for ( FormElement fe : fg.getFormElements() ) {
            if ( fe instanceof FormGroup ) {
                addFormField( formFields, (FormGroup) fe );
            } else if ( fe instanceof FormField ) {
                FormField ff = (FormField) fe;
                formFields.put( ff.getPath().toString(), ff );
            }
        }
    }

    /**
     * @param id
     *            the id identifiying the top level form group
     * @return the form group assigned to the given id, null, if no form group for thi id exist
     */
    public FormGroup getFormGroup( String id ) {
        for ( FormGroup fg : formGroups ) {
            // return first!
            if ( id == null ) {
                return fg;
            } else if ( id.equals( fg.getId() ) ) {
                return fg;
            }
        }
        return null;
    }

    /**
     * @param path
     * @return
     */
    public FormField getFormField( FormFieldPath path ) {
        for ( FormGroup fg : formGroups ) {
            return getFormField( fg, path );
        }
        return null;
    }

    public FormField getFormField( FormGroup fg, FormFieldPath path ) {
        for ( FormElement fe : fg.getFormElements() ) {
            if ( fe instanceof FormField && path.equals( ( (FormField) fe ).getPath() ) ) {
                return (FormField) fe;
            } else if ( fe instanceof FormGroup ) {
                return getFormField( (FormGroup) fe, path );
            }
        }
        return null;
    }

    public List<String> getReferencedFormGroupIds() {
        List<String> ids = new ArrayList<String>();
        for ( FormGroup fg : formGroups ) {
            if ( fg.isReferenced() ) {
                ids.add( fg.getId() );
            }
        }
        return ids;
    }
}

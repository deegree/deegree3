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
package org.deegree.client.mdeditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;

import org.deegree.client.mdeditor.config.FormConfigurationParser;
import org.deegree.client.mdeditor.model.FormElement;
import org.deegree.client.mdeditor.model.FormField;
import org.deegree.client.mdeditor.model.FormGroup;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class FormElementManager {

    private static Map<String, FormField> formFields = new HashMap<String, FormField>();

    private static List<FormGroup> formGroups;

    static {
        formGroups = FormConfigurationParser.getFormGroups();
        for ( FormGroup fg : formGroups ) {
            addFormField( fg );
        }
    }

    /**
     * @return a list of all top level formGroups
     */
    public static List<FormGroup> getFormGroups() {
        return formGroups;
    }

    public static FormElement getFormField( String completeId ) {
        return formFields.get( completeId );
    }

    /**
     * @return a list of all form fields
     */
    public static Map<String, FormField> getFormFields() {
        return formFields;
    }

    private static void addFormField( FormGroup fg ) {
        for ( FormElement fe : fg.getFormElements() ) {
            if ( fe instanceof FormGroup ) {
                addFormField( (FormGroup) fe );
            } else if ( fe instanceof FormField ) {
                formFields.put( fe.getCompleteId(), (FormField) fe );
            }
        }
    }

    /**
     * @param grpId
     *            the id of the group to return
     * @return the form group with the given id, returns null if a grouup with the given id does not exist
     * @throws NullPointerException
     *             if grpId is null
     */
    public static FormGroup getFormGroup( String grpId ) {
        if ( grpId == null ) {
            throw new NullPointerException();
        }
        for ( FormGroup fg : formGroups ) {
            if ( grpId.equals( fg.getId() ) )
                return fg;
        }
        return null;
    }

    /**
     * @param fgId
     *            the id of the form group
     */
    public static List<String> getSubFormGroupIds( String fgId ) {
        return getSubFormGroupIds( getFormGroup( fgId ) );
    }

    private static List<String> getSubFormGroupIds( FormGroup fg ) {
        List<String> fgIds = new ArrayList<String>();
        for ( FormElement fe : fg.getFormElements() ) {
            System.out.println( "w" );
            if ( fe instanceof FormGroup ) {
                System.out.println( "w1" );
                fgIds.add( ( (FormGroup) fe ).getId() );
                fgIds.addAll( getSubFormGroupIds( (FormGroup) fe ) );
            }
        }
        return fgIds;
    }

}

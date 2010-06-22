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
import java.util.List;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public abstract class FormField implements FormElement {

    private String id;

    protected Object value;

    private boolean visibility;

    private String label;

    private String help;

    private FormFieldPath path;

    protected boolean valid = true;

    private String title;

    private Object defaultValue;

    private boolean required;

    public FormField( FormFieldPath path, String id, String label, boolean visible, boolean required, String help,
                      Object defaultValue ) {
        this.path = path;
        this.id = id;
        this.label = label;
        this.visibility = visible;
        this.required = required;
        this.help = help;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getId() {
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }

    public Object getValue() {
        return value;
    }

    public void setValue( Object value ) {
        this.value = value;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility( boolean visibility ) {
        this.visibility = visibility;
    }

    public void setRequired( boolean required ) {
        this.required = required;
    }

    public boolean isRequired() {
        return required;
    }

    public void setLabel( String label ) {
        this.label = label;
    }

    public String getLabel() {
        return label != null && label.length() > 0 ? label : id;
    }

    public void setHelp( String help ) {
        this.help = help;
    }

    public String getHelp() {
        return help;
    }

    public FormFieldPath getPath() {
        return path;
    }

    public boolean isValid() {
        List<VALIDATION_TYPE> validate = validate();
        if ( validate != null && validate.size() > 0 ) {
            return false;
        }
        return true;
    }

    public void setTitle( String title ) {
        this.title = title;
    }

    public String getTitle() {
        return title != null ? title : label;
    }

    public void reset() {
        setValue( getDefaultValue() );
    }

    public void setDefaultValue( Object defaultValue ) {
        this.defaultValue = defaultValue;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String toString() {
        return getPath().toString() + ": " + getValue();
    }

    public List<VALIDATION_TYPE> validate() {
        List<VALIDATION_TYPE> validationMap = new ArrayList<VALIDATION_TYPE>();
        if ( required ) {
            if ( value == null || ( value instanceof List<?> && ( (List<?>) value ).size() == 0 )
                 || ( !( value.toString().length() > 0 ) ) ) {
                validationMap.add( VALIDATION_TYPE.REQUIRED );
            }
        }
        return validationMap;
    }

}

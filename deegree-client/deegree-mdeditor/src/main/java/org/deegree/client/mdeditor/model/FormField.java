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

    private Object value;

    private boolean visibility;

    private String label;

    private String help;

    private FormFieldPath path;

    private boolean isIdentifier;

    public FormField( FormFieldPath path, String id, String label, boolean visible, String help, Object defaultValue,
                      boolean isIdentifier ) {
        this.path = path;
        this.id = id;
        this.label = label;
        this.visibility = visible;
        this.help = help;
        this.value = defaultValue;
        this.setIdentifier( isIdentifier );
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

    @Override
    public String toString() {
        return getPath().toString() + ": " + getValue();
    }

    public FormFieldPath getPath() {
        return path;
    }

    public void setIdentifier( boolean isIdentifier ) {
        this.isIdentifier = isIdentifier;
    }

    public boolean isIdentifier() {
        return isIdentifier;
    }

}

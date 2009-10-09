//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

package org.deegree.commons.types.gml;

import org.deegree.commons.types.ows.CodeType;
import org.deegree.commons.types.ows.StringOrRef;
import org.deegree.feature.Feature;
import org.deegree.geometry.Geometry;

/**
 * Version-agnostic representation of the standard properties of GML objects, such as features or geometries.
 * <p>
 * <table border="1">
 * <tr>
 * <th>Property name</th>
 * <th>Multiplicity</th>
 * <th>Description (from GML 3.1.1/3.2.1 schemas)</th>
 * </tr>
 * <tr>
 * <td><code>gml:metaDataProperty</code></td>
 * <td><code>0...*</code></td>
 * <td>Contains or refers to a metadata package that contains metadata properties. <i>NOTE: This property is deprecated
 * in GML 3.2.1.</i></td>
 * </tr>
 * <tr>
 * <td><code>gml:description</code></td>
 * <td><code>0...1</code></td>
 * <td>The value of this property is a text description of the object. <code>gml:description</code> uses
 * <code>gml:StringOrRefType</code> as its content model, so it may contain a simple text string content, or carry a
 * reference to an external description. The use of gml:description to reference an external description has been
 * deprecated and replaced by the <code>gml:descriptionReference</code> property.</td>
 * </tr>
 * <tr>
 * <td><code>gml:descriptionReference</code></td>
 * <td><code>0...1</code></td>
 * <td>The value of this property is a remote text description of the object. The <code>xlink:href</code> attribute of
 * the <code>gml:descriptionReference</code> property references the external description.</td>
 * </tr>
 * <tr>
 * <td><code>gml:identifier</code></td>
 * <td><code>0...1</code></td>
 * <td>Often, a special identifier is assigned to an object by the maintaining authority with the intention that it is
 * used in references to the object For such cases, the codeSpace shall be provided. That identifier is usually unique
 * either globally or within an application domain. <code>gml:identifier</code> is a pre-defined property for such
 * identifiers.</td>
 * </tr>
 * <tr>
 * <td><code>gml:name</code></td>
 * <td><code>0...*</code></td>
 * <td>The <code>gml:name</code> property provides a label or identifier for the object, commonly a descriptive name. An
 * object may have several names, typically assigned by different authorities. <code>gml:name</code> uses the
 * <code>gml:CodeType</code> content model. The authority for a name is indicated by the value of its (optional)
 * <code>codeSpace</code> attribute. The name may or may not be unique, as determined by the rules of the organization
 * responsible for the codeSpace. In common usage there will be one name per authority, so a processing application may
 * select the name from its preferred codeSpace.</td>
 * </tr>
 * </table>
 * </p>
 * 
 * @see Feature
 * @see Geometry
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class StandardObjectProps {

    private StringOrRef description;

    protected final CodeType[] names;

    public StandardObjectProps( StringOrRef description, CodeType[] names ) {
        this.description = description;
        this.names = names;
    }

    public StringOrRef getDescription() {
        return description;
    }

    public CodeType[] getNames() {
        return names;
    }

    @Override
    public String toString() {
        String s = "";
        for ( int i = 0; i < names.length; i++ ) {
            s += "name={" + names[i] + "}";
            if ( i != names.length - 1 ) {
                s += ',';
            }
        }
        return s;
    }
}

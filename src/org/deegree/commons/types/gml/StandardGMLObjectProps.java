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

import static org.deegree.commons.xml.CommonNamespaces.GMLNS;
import static org.deegree.feature.types.property.PrimitiveType.STRING;

import javax.xml.namespace.QName;

import org.deegree.commons.types.ows.CodeType;
import org.deegree.commons.types.ows.StringOrRef;
import org.deegree.feature.Feature;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PrimitiveType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.feature.types.property.StringOrRefPropertyType;
import org.deegree.geometry.Geometry;

/**
 * Version-agnostic representation of the standard properties that any GML object allows for.
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
public class StandardGMLObjectProps {

    /** GML 2 standard property type 'gml:description' */
    public static final SimplePropertyType<String> PT_DESCRIPTION_GML2;

    /** GML 2 standard property type 'gml:name' */
    public static final SimplePropertyType<String> PT_NAME_GML2;

    /** GML 2 standard property type 'gml:boundedBy' */
    public static final PropertyType PT_BOUNDED_BY_GML2;

    /** GML 3.0/3.1 standard property type 'gml:metaDataProperty' */
    public static final CustomPropertyType PT_META_DATA_PROPERTY_GML31;

    /** GML 3.0/3.1 standard property type 'gml:description' */
    public static final StringOrRefPropertyType PT_DESCRIPTION_GML31;

    /** GML 3.0/3.1 standard property type 'gml:name' */
    public static final CodePropertyType PT_NAME_GML31;

    static {
        PT_DESCRIPTION_GML2 = new SimplePropertyType<String>( new QName( GMLNS, "description" ), 0, 1, STRING, false,
                                                              null );
        PT_NAME_GML2 = new SimplePropertyType<String>( new QName( GMLNS, "name" ), 0, 1, STRING, false, null );

        // TODO correct this (this should be a BoundingShapeType which permits BBOX or NULL)
        PT_BOUNDED_BY_GML2 = new GeometryPropertyType( new QName( GMLNS, "boundedBy" ), 0, 1,
                                                       GeometryPropertyType.GeometryType.GEOMETRY,
                                                       GeometryPropertyType.CoordinateDimension.DIM_2, false, null );

        // TODO correct this (should be a MetaDataPropertyType)
        PT_META_DATA_PROPERTY_GML31 = new CustomPropertyType( new QName( GMLNS, "metaDataProperty" ), 0, -1, null,
                                                              false, null );
        // TODO correct this (should be a StringOrRefType)
        PT_DESCRIPTION_GML31 = new StringOrRefPropertyType( new QName( GMLNS, "description" ), 0, 1, false, null );
        PT_NAME_GML31 = new CodePropertyType( new QName( GMLNS, "name" ), 0, -1, false, null );
    }

    protected Object[] metadata;

    protected StringOrRef description;

    protected CodeType[] names;

    public StandardGMLObjectProps( Object[] metadata, StringOrRef description, CodeType[] names ) {
        if ( metadata == null ) {
            this.metadata = new Object[0];
        } else {
            this.metadata = metadata;
        }
        this.description = description;
        if ( names == null ) {
            this.names = new CodeType[0];
        } else {
            this.names = names;
        }
    }

    public Object getMetadata() {
        return description;
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

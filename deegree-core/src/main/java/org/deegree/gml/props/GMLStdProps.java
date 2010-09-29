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

package org.deegree.gml.props;

import static org.deegree.commons.tom.primitive.PrimitiveType.STRING;
import static org.deegree.commons.xml.CommonNamespaces.GML3_2_NS;
import static org.deegree.commons.xml.CommonNamespaces.GMLNS;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.StringOrRef;
import org.deegree.feature.Feature;
import org.deegree.feature.types.property.CodePropertyType;
import org.deegree.feature.types.property.CustomPropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.feature.types.property.StringOrRefPropertyType;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLObject;

/**
 * Version-agnostic representation of the standard properties that any {@link GMLObject} allows for.
 * <p>
 * The following properties exist (description taken from GML 3.1.1/3.2.1 schemas):
 * <ul>
 * <li><b><code>gml:metaDataProperty</code></b>: Contains or refers to a metadata package that contains metadata
 * properties. Has been deprecated in GML 3.2.1.</li>
 * <li><b><code>gml:description</code></b>: The value of this property is a text description of the object.
 * gml:description uses gml:StringOrRefType as its content model, so it may contain a simple text string content, or
 * carry a reference to an external description. The use of gml:description to reference an external description has
 * been deprecated and replaced by the gml:descriptionReference property.</li>
 * <li><b><code>gml:descriptionReference</code></b>: The value of this property is a remote text description of the
 * object. The xlink:href attribute of the gml:descriptionReference property references the external description.</li>
 * <li><b><code>gml:identifier</code></b>: Often, a special identifier is assigned to an object by the maintaining
 * authority with the intention that it is used in references to the object For such cases, the codeSpace shall be
 * provided. That identifier is usually unique either globally or within an application domain. gml:identifier is a
 * pre-defined property for such identifiers.</li>
 * <li><b><code>gml:name</code></b>: The gml:name property provides a label or identifier for the object, commonly a
 * descriptive name. An object may have several names, typically assigned by different authorities. gml:name uses the
 * gml:CodeType content model. The authority for a name is indicated by the value of its (optional) codeSpace attribute.
 * The name may or may not be unique, as determined by the rules of the organization responsible for the codeSpace. In
 * common usage there will be one name per authority, so a processing application may select the name from its preferred
 * codeSpace.</li>
 * </ul>
 * </p>
 * <p>
 * <table border="1">
 * <tr>
 * <th>Property name</th>
 * <th>GML 2</th>
 * <th>GML 3.0</th>
 * <th>GML 3.1</th>
 * <th>GML 3.2</th>
 * </tr>
 * <tr align="center">
 * <td><code>metaDataProperty</code></td>
 * <td><code>n/a</code></td>
 * <td><code>MetaDataPropertyType (0...*)</code></td>
 * <td><code>MetaDataPropertyType (0...*)</code></td>
 * <td><code>MetaDataPropertyType (0...*)</code></td>
 * </tr>
 * <tr align="center">
 * <td><code>description</code></td>
 * <td><code>string (0...1)</code></td>
 * <td><code>StringOrRefType (0...1)</code></td>
 * <td><code>StringOrRefType (0...1)</code></td>
 * <td><code>StringOrRefType (0...1)</code></td>
 * </tr>
 * <tr align="center">
 * <td><code>descriptionReference</code></td>
 * <td><code>n/a</code></td>
 * <td><code>n/a</code></td>
 * <td><code>n/a</code></td>
 * <td><code>ReferenceType (0...1)</code></td>
 * </tr>
 * <tr align="center">
 * <td><code>identifier</code></td>
 * <td><code>n/a</code></td>
 * <td><code>n/a</code></td>
 * <td><code>n/a</code></td>
 * <td><code>CodeWithAuthorityType (0...1)</code></td>
 * </tr>
 * <tr align="center">
 * <td><code>name</code></td>
 * <td><code>string (0...1)</code></td>
 * <td><code>CodeType (0...*)</code></td>
 * <td><code>CodeType (0...*)</code></td>
 * <td><code>CodeType (0...*)</code></td>
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
public class GMLStdProps {

    /** GML 2 standard property type 'gml:description' */
    public static final SimplePropertyType PT_DESCRIPTION_GML2;

    /** GML 2 standard property type 'gml:name' */
    public static final SimplePropertyType PT_NAME_GML2;

    /** GML 3.0/3.1 standard property type 'gml:metaDataProperty' */
    public static final CustomPropertyType PT_META_DATA_PROPERTY_GML31;

    /** GML 3.0/3.1 standard property type 'gml:description' */
    public static final StringOrRefPropertyType PT_DESCRIPTION_GML31;

    /** GML 3.0/3.1 standard property type 'gml:name' */
    public static final CodePropertyType PT_NAME_GML31;

    /** GML 3.2 standard property type 'gml:metaDataProperty' */
    public static final CustomPropertyType PT_META_DATA_PROPERTY_GML32;

    /** GML 3.2 standard property type 'gml:description' */
    public static final StringOrRefPropertyType PT_DESCRIPTION_GML32;

    /** GML 3.2 standard property type 'gml:descriptionReference' */
    public static final StringOrRefPropertyType PT_DESCRIPTION_REFERENCE_GML32;

    /** GML 3.2 standard property type 'gml:identifier' */
    public static final CodePropertyType PT_IDENTIFIER_GML32;

    /** GML 3.2 standard property type 'gml:name' */
    public static final CodePropertyType PT_NAME_GML32;

    static {
        PT_DESCRIPTION_GML2 = new SimplePropertyType( new QName( GMLNS, "description" ), 0, 1, STRING, false, false,
                                                      null );
        PT_NAME_GML2 = new SimplePropertyType( new QName( GMLNS, "name" ), 0, 1, STRING, false, false, null );

        // TODO correct this (should be a MetaDataPropertyType)
        PT_META_DATA_PROPERTY_GML31 = new CustomPropertyType( new QName( GMLNS, "metaDataProperty" ), 0, -1, null,
                                                              false, false, null );
        // TODO correct this (should be a StringOrRefType)
        PT_DESCRIPTION_GML31 = new StringOrRefPropertyType( new QName( GMLNS, "description" ), 0, 1, false, false, null );
        PT_NAME_GML31 = new CodePropertyType( new QName( GMLNS, "name" ), 0, -1, false, false, null );

        // TODO correct this (should be a MetaDataPropertyType)
        PT_META_DATA_PROPERTY_GML32 = new CustomPropertyType( new QName( GML3_2_NS, "metaDataProperty" ), 0, -1, null,
                                                              false, false, null );
        // TODO correct this (should be a StringOrRefType)
        PT_DESCRIPTION_GML32 = new StringOrRefPropertyType( new QName( GML3_2_NS, "description" ), 0, 1, false, false,
                                                            null );
        // TODO correct this (should be a ReferenceType)
        PT_DESCRIPTION_REFERENCE_GML32 = new StringOrRefPropertyType( new QName( GML3_2_NS, "descriptionReference" ),
                                                                      0, 1, false, false, null );
        PT_IDENTIFIER_GML32 = new CodePropertyType( new QName( GML3_2_NS, "identifier" ), 0, -1, false, false, null );
        PT_NAME_GML32 = new CodePropertyType( new QName( GML3_2_NS, "name" ), 0, -1, false, false, null );
    }

    protected TypedObjectNode[] metadata;

    protected StringOrRef description;

    protected CodeType identifier;

    protected CodeType[] names;

    /**
     * Creates a new {@link GMLStdProps} instance.
     * 
     * @param metadata
     *            metadata values, may be <code>null</code>
     * @param description
     *            description, may be <code>null</code>
     * @param identifier
     *            identifier, may be <code>null</code>
     * @param names
     *            names, may be <code>null</code>
     */
    public GMLStdProps( TypedObjectNode[] metadata, StringOrRef description, CodeType identifier, CodeType[] names ) {
        if ( metadata == null ) {
            this.metadata = new TypedObjectNode[0];
        } else {
            this.metadata = metadata;
        }
        this.description = description;
        this.identifier = identifier;
        if ( names == null ) {
            this.names = new CodeType[0];
        } else {
            this.names = names;
        }
    }

    /**
     * Returns the metadata values.
     * 
     * @return the metadata values, may be empty, but never <code>null</code>
     */
    public TypedObjectNode[] getMetadata() {
        return metadata;
    }

    /**
     * Returns the description.
     * 
     * @return the description, may be <code>null</code>
     */
    public StringOrRef getDescription() {
        return description;
    }

    /**
     * Returns the identifier.
     * 
     * @return the identifier, may be <code>null</code>
     */
    public CodeType getIdentifier() {
        return identifier;
    }

    /**
     * Returns the names.
     * 
     * @return the names, may be empty, but never <code>null</code>
     */
    public CodeType[] getNames() {
        return names;
    }

    @Override
    public String toString() {
        String s = "";
        for ( int i = 0; i < metadata.length; i++ ) {
            s += "metadata={" + metadata[i] + "}";
            if ( i != metadata.length - 1 ) {
                s += ',';
            }
        }
        s += ",";
        s += "description={" + description + "}";
        s += ",";
        for ( int i = 0; i < names.length; i++ ) {
            s += "name={" + names[i] + "}";
            if ( i != names.length - 1 ) {
                s += ',';
            }
        }
        s += "identifier={" + identifier + "}";
        return s;
    }
}
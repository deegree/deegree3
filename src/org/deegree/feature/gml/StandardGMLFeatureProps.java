//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.feature.gml;

import static org.deegree.commons.xml.CommonNamespaces.GMLNS;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.deegree.commons.gml.GMLVersion;
import org.deegree.commons.types.gml.StandardGMLObjectProps;
import org.deegree.commons.types.ows.CodeType;
import org.deegree.commons.types.ows.StringOrRef;
import org.deegree.feature.types.property.GeometryPropertyType;
import org.deegree.feature.types.property.PropertyType;
import org.deegree.feature.types.property.SimplePropertyType;
import org.deegree.geometry.Envelope;

/**
 * Version-agnostic representation of the standard properties that any GML feature allows for.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class StandardGMLFeatureProps extends StandardGMLObjectProps {

    /** GML 2 standard property type 'gml:description' */
    public static final SimplePropertyType PT_DESCRIPTION_GML2;

    /** GML 2 standard property type 'gml:name' */
    public static final SimplePropertyType PT_NAME_GML2;

    /** GML 2 standard property type 'gml:boundedBy' */
    public static final PropertyType PT_BOUNDED_BY_GML2;

    /** GML 3.0/3.1 standard property type 'gml:boundedBy' */
    public static final PropertyType PT_BOUNDED_BY_GML31;

    private final Envelope boundedBy;

    private final static Map<QName, PropertyType> GML2PropNameToPropType = new LinkedHashMap<QName, PropertyType>();

    private final static Map<QName, PropertyType> GML31PropNameToPropType = new LinkedHashMap<QName, PropertyType>();

    private final static Map<QName, PropertyType> GML32PropNameToPropType = new LinkedHashMap<QName, PropertyType>();

    static {
        PT_DESCRIPTION_GML2 = new SimplePropertyType( new QName( GMLNS, "description" ), 0, 1,
                                                      SimplePropertyType.PrimitiveType.STRING );
        PT_NAME_GML2 = new SimplePropertyType( new QName( GMLNS, "name" ), 0, 1,
                                               SimplePropertyType.PrimitiveType.STRING );

        // TODO correct this (this should be a BoundingShapeType which permits BBOX or NULL)
        PT_BOUNDED_BY_GML2 = new GeometryPropertyType( new QName( GMLNS, "boundedBy" ), 0, 1,
                                                       GeometryPropertyType.GeometryType.GEOMETRY,
                                                       GeometryPropertyType.CoordinateDimension.DIM_2 );

        // TODO correct this (this should be a BoundingShapeType which permits BBOX or NULL)
        PT_BOUNDED_BY_GML31 = new GeometryPropertyType( new QName( GMLNS, "boundedBy" ), 0, 1,
                                                        GeometryPropertyType.GeometryType.GEOMETRY,
                                                        GeometryPropertyType.CoordinateDimension.DIM_2 );

        // fill lookup maps
        GML2PropNameToPropType.put( PT_DESCRIPTION_GML2.getName(), PT_DESCRIPTION_GML2 );
        GML2PropNameToPropType.put( PT_NAME_GML2.getName(), PT_NAME_GML2 );
        GML2PropNameToPropType.put( PT_BOUNDED_BY_GML2.getName(), PT_BOUNDED_BY_GML2 );

        GML31PropNameToPropType.put( PT_META_DATA_PROPERTY_GML31.getName(), PT_META_DATA_PROPERTY_GML31 );
        GML31PropNameToPropType.put( PT_DESCRIPTION_GML31.getName(), PT_DESCRIPTION_GML31 );
        GML31PropNameToPropType.put( PT_NAME_GML31.getName(), PT_NAME_GML31 );
        GML31PropNameToPropType.put( PT_BOUNDED_BY_GML31.getName(), PT_BOUNDED_BY_GML31 );
    }

    StandardGMLFeatureProps( StringOrRef description, CodeType[] names, Envelope boundedBy ) {
        super( description, names );
        this.boundedBy = boundedBy;
    }

    public Envelope getBoundedBy() {
        return boundedBy;
    }

    /**
     * Returns the standard property types that any GML feature allows for.
     * 
     * @param version
     *            GML version, must not be <code>null</code>
     * @return standard GML property types, in expected order
     */
    public static Collection<PropertyType> getPropertyTypes( GMLVersion version ) {

        Collection<PropertyType> pts = null;
        switch ( version ) {
        case GML_2:
            pts = GML2PropNameToPropType.values();
            break;
        case GML_31:
            pts = GML31PropNameToPropType.values();
            break;
        case GML_32:
            pts = GML32PropNameToPropType.values();
            break;

        }
        return pts;
    }    
    
    /**
     * Returns the standard GML property type with the given name for the specified GML version.
     * 
     * @param propName
     *            qualified name of the property
     * @param version
     *            GML version, must not be <code>null</code>
     * @return standard GML property type, or <code>null</code> if no such property type exists
     */
    public static PropertyType getPropertyType( QName propName, GMLVersion version ) {

        PropertyType pt = null;
        switch ( version ) {
        case GML_2:
            pt = GML2PropNameToPropType.get( propName );
            break;
        case GML_31:
            pt = GML31PropNameToPropType.get( propName );
            break;
        case GML_32:
            pt = GML32PropNameToPropType.get( propName );
            break;

        }
        return pt;
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
        if ( boundedBy != null ) {
            if ( s.length() > 0 ) {
                s += ",";
            }
            s += "boundedBy={" + boundedBy + "}";
        }
        return s;
    }
}

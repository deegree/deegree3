//$HeadURL$
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
package org.deegree.feature.types.property;

import java.util.List;

import javax.xml.namespace.QName;

import org.deegree.geometry.Geometry;

/**
 * {@link PropertyType} that defines a property with a {@link Geometry} value.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author:$
 * 
 * @version $Revision:$, $Date:$
 */
public class GeometryPropertyType extends AbstractPropertyType<Geometry> {

    public enum GeometryType {
        /** Any kind of geometry (primitive, composite or aggregate). */
        GEOMETRY,
        /** A primitive geometry. */
        PRIMITIVE,
        /** A composite geometry. */
        COMPOSITE, POINT, CURVE, LINE_STRING, RING, LINEAR_RING, ORIENTABLE_CURVE, COMPOSITE_CURVE, SURFACE, POLYHEDRAL_SURFACE, TRIANGULATED_SURFACE, TIN, POLYGON, ORIENTABLE_SURFACE, COMPOSITE_SURFACE, SOLID, COMPOSITE_SOLID, MULTI_GEOMETRY, MULTI_POINT, MULTI_CURVE, MULTI_LINE_STRING, MULTI_SURFACE, MULTI_POLYGON, MULTI_SOLID
    }

    public enum CoordinateDimension {
        DIM_2, DIM_3, DIM_2_OR_3,
    }

    private GeometryType geomType;

    private CoordinateDimension dim;

    private final ValueRepresentation representation;

    public GeometryPropertyType( QName name, int minOccurs, int maxOccurs, GeometryType geomType,
                                 CoordinateDimension dim, boolean isAbstract,
                                 List<PropertyType<?>> substitutions, ValueRepresentation representation  ) {
        super( name, minOccurs, maxOccurs, isAbstract, substitutions );
        this.geomType = geomType;
        this.dim = dim;
        this.representation = representation;
    }

    public GeometryType getGeometryType() {
        return geomType;
    }

    public CoordinateDimension getCoordinateDimension() {
        return dim;
    }

    /**
     * Returns the allowed representation form of the value object.
     * 
     * @return the allowed representation form, never <code>null</code>
     */
    public ValueRepresentation getAllowedRepresentation() {
        return representation;
    }    
    
    @Override
    public String toString() {
        String s = "- geometry property type: '" + name + "', minOccurs=" + minOccurs + ", maxOccurs=" + maxOccurs
                   + ", geometry type: " + geomType;
        return s;
    }
}

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
package org.deegree.feature.persistence.sql.rules;

import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.feature.persistence.sql.JoinChain;
import org.deegree.feature.types.property.GeometryPropertyType.CoordinateDimension;
import org.deegree.feature.types.property.GeometryPropertyType.GeometryType;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sql.MappingExpression;
import org.deegree.geometry.Geometry;

/**
 * {@link Mapping} of {@link Geometry} particles.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GeometryMapping extends Mapping {

    private final MappingExpression mapping;

    private GeometryType type;

    private CoordinateDimension dim;

    private ICRS crs;

    private String srid;

    public GeometryMapping( PropertyName path, MappingExpression mapping, GeometryType type, CoordinateDimension dim,
                            ICRS crs, String srid, JoinChain joinedTable ) {
        super( path, joinedTable );
        this.mapping = mapping;
        this.type = type;
        this.dim = dim;
        this.crs = crs;
        this.srid = srid;
    }

    public MappingExpression getMapping() {
        return mapping;
    }

    public GeometryType getType() {
        return type;
    }

    public CoordinateDimension getDim() {
        return dim;
    }

    public ICRS getCRS() {
        return crs;
    }

    public String getSrid() {
        return srid;
    }

    @Override
    public String toString() {
        return super.toString() + ",{type=" + type + "}";
    }
}
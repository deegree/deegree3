//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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
package org.deegree.feature.persistence.sql.converter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.tom.sql.ParticleConverter;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.io.CoordinateFormatter;
import org.deegree.geometry.io.DecimalCoordinateFormatter;
import org.deegree.geometry.primitive.Point;

public class DoubleListConverter implements ParticleConverter<TypedObjectNode> {

    private final PrimitiveType pt;

    private final ParticleConverter<TypedObjectNode> geomConverter;

    // TODO
    private final CoordinateFormatter formatter = new DecimalCoordinateFormatter( 6 );

    public DoubleListConverter( PrimitiveType pt, ParticleConverter<TypedObjectNode> geomConverter ) {
        this.pt = pt;
        this.geomConverter = geomConverter;
    }

    @Override
    public String getSelectSnippet( String tableAlias ) {
        return geomConverter.getSelectSnippet( tableAlias );
    }

    @Override
    public String getSetSnippet() {
        return geomConverter.getSetSnippet();
    }

    @Override
    public PrimitiveValue toParticle( ResultSet rs, int colIndex )
                            throws SQLException {
        Geometry geom = (Geometry) geomConverter.toParticle( rs, colIndex );
        if ( geom == null ) {
            return null;
        }
        if ( geom instanceof Point ) {
            Point p = (Point) geom;
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for ( double ordinate : p.getAsArray() ) {
                if ( !first ) {
                    sb.append( " " );
                }
                sb.append( formatter.format( ordinate ) );
                first = false;
            }
            return new PrimitiveValue( sb.toString(), pt );
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void setParticle( PreparedStatement stmt, TypedObjectNode particle, int paramIndex )
                            throws SQLException {
        geomConverter.setParticle( stmt, particle, paramIndex );
    }
}
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

import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.primitive.PrimitiveType;
import org.deegree.feature.persistence.sql.expressions.JoinChain;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sql.DBField;
import org.deegree.filter.sql.MappingExpression;

/**
 * {@link Mapping} of {@link CodeType} particles.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CodeMapping extends Mapping {

    private final PrimitiveType pt;

    private final MappingExpression mapping;

    private final MappingExpression codeSpaceMapping;

    /**
     * 
     * @param path
     * @param mapping
     * @param pt
     * @param joinedTable
     * @parma codeSpaceMapping
     * @param nilMapping
     *            name of (boolean) column that stores whether the element is nilled, can be <code>null</code>
     */
    public CodeMapping( PropertyName path, MappingExpression mapping, PrimitiveType pt, JoinChain joinedTable,
                        MappingExpression codeSpaceMapping, DBField nilMapping ) {
        super( path, joinedTable, nilMapping );
        this.mapping = mapping;
        this.pt = pt;
        this.codeSpaceMapping = codeSpaceMapping;
    }

    public MappingExpression getMapping() {
        return mapping;
    }

    public PrimitiveType getType() {
        return pt;
    }

    public MappingExpression getCodeSpaceMapping() {
        return codeSpaceMapping;
    }
}
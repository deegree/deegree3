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
package org.deegree.filter;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.deegree.filter.comparison.ComparisonOperator;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.logical.LogicalOperator;
import org.deegree.filter.spatial.SpatialOperator;
import org.deegree.geometry.Geometry;

/**
 * Various static methods for performing standard tasks on {@link Filter} objects.
 * 
 * @see Filter
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Filters {

    /**
     * Returns all {@link PropertyName}s contained in the given {@link Filter} (taking nesting into account).
     * 
     * @param filter
     *            filter to be traversed, must not be <code>null</code>
     * @return {@link PropertyName}s found on any nodes of the {@link Filter}, can be empty, but never <code>null</code>
     */
    public static PropertyName[] getPropertyNames( Filter filter ) {

        List<PropertyName> propNames = null;
        switch ( filter.getType() ) {
        case OPERATOR_FILTER: {
            propNames = new LinkedList<PropertyName>();
            addPropertyNames( ( (OperatorFilter) filter ).getOperator(), propNames );
            break;
        }
        case ID_FILTER: {
            propNames = Collections.emptyList();
            break;
        }
        }
        return propNames.toArray( new PropertyName[propNames.size()] );
    }

    private static void addPropertyNames( Operator operator, List<PropertyName> propNames ) {
        Operator.Type type = operator.getType();
        switch ( type ) {
        case COMPARISON:
            ComparisonOperator compOper = (ComparisonOperator) operator;
            for ( Expression expr : compOper.getParams() ) {
                addPropertyNames( expr, propNames );
            }
            break;
        case LOGICAL:
            LogicalOperator logicalOper = (LogicalOperator) operator;
            for ( Operator param : logicalOper.getParams() ) {
                addPropertyNames( param, propNames );
            }
            break;
        case SPATIAL:
            SpatialOperator spatialOper = (SpatialOperator) operator;
            for ( Object param : spatialOper.getParams() ) {
                if ( param instanceof Expression ) {
                    addPropertyNames( (Expression) param, propNames );
                }
            }
            break;
        }
    }

    private static void addPropertyNames( Expression expr, List<PropertyName> propNames ) {
        if ( expr instanceof PropertyName ) {
            propNames.add( (PropertyName) expr );
        } else {
            for ( Expression child : expr.getParams() ) {
                addPropertyNames( child, propNames );
            }
        }
    }

    /**
     * Returns all {@link Geometry}-values contained in the given {@link Filter} (taking nesting into account).
     * 
     * @param filter
     *            filter to be traversed, must not be <code>null</code>
     * @return {@link Geometry}s found on any nodes of the {@link Filter}, can be empty, but never <code>null</code>
     */
    public static Geometry[] getGeometries( Filter filter ) {
        List<Geometry> geometries = null;
        switch ( filter.getType() ) {
        case OPERATOR_FILTER: {
            geometries = new LinkedList<Geometry>();
            addGeometries( ( (OperatorFilter) filter ).getOperator(), geometries );
            break;
        }
        case ID_FILTER: {
            geometries = Collections.emptyList();
            break;
        }
        }
        return geometries.toArray( new Geometry[geometries.size()] );
    }

    private static void addGeometries( Operator operator, List<Geometry> geometries ) {
        Operator.Type type = operator.getType();
        switch ( type ) {
        case LOGICAL:
            LogicalOperator logicalOper = (LogicalOperator) operator;
            for ( Operator param : logicalOper.getParams() ) {
                addGeometries( param, geometries );
            }
            break;
        case SPATIAL:
            SpatialOperator spatialOper = (SpatialOperator) operator;
            for ( Object param : spatialOper.getParams() ) {
                if ( param instanceof Geometry ) {
                    geometries.add( (Geometry) param );
                }
            }
            break;
        case COMPARISON:
            // nothing to do
            break;
        }
    }
}

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
package org.deegree.observation.persistence;

import static org.deegree.observation.persistence.QueryBuilder.stringSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.deegree.commons.utils.ArrayUtils;
import org.deegree.commons.utils.StringPair;
import org.deegree.filter.Expression;
import org.deegree.filter.comparison.BinaryComparisonOperator;
import org.deegree.filter.comparison.ComparisonOperator;
import org.deegree.filter.comparison.PropertyIsBetween;
import org.deegree.filter.comparison.PropertyIsNull;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.PropertyName;
import org.deegree.observation.model.Offering;
import org.deegree.protocol.sos.filter.BeginFilter;
import org.deegree.protocol.sos.filter.DurationFilter;
import org.deegree.protocol.sos.filter.EndFilter;
import org.deegree.protocol.sos.filter.ProcedureFilter;
import org.deegree.protocol.sos.filter.ResultFilter;
import org.deegree.protocol.sos.filter.TimeFilter;
import org.deegree.protocol.sos.filter.TimeInstantFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class GenericFilterConverter implements SQLFilterConverter {

    private static final Logger LOG = LoggerFactory.getLogger( GenericFilterConverter.class );

    private final DatastoreConfiguration dsConfig;

    private final TimeZone tz;

    /**
     * @param dsConfig
     * @param tz
     */
    public GenericFilterConverter( DatastoreConfiguration dsConfig, TimeZone tz ) {
        this.dsConfig = dsConfig;
        this.tz = tz;
    }

    @Override
    public void buildTimeClause( QueryBuilder q, List<TimeFilter> filters ) {
        String timeStampColumn = dsConfig.getColumnName( "timestamp" );
        final Calendar template = Calendar.getInstance( tz );

        ArrayList<String> timeFilters = new ArrayList<String>( filters.size() );

        for ( TimeFilter filter : filters ) {
            if ( filter instanceof DurationFilter ) {
                final DurationFilter durationFilter = (DurationFilter) filter;

                String beginCmp = durationFilter.isInclusiveBegin() ? ">=" : ">";
                String endCmp = durationFilter.isInclusiveEnd() ? "<=" : "<";

                timeFilters.add( String.format( "(%s %s ? AND %s %s ?)", timeStampColumn, beginCmp, timeStampColumn,
                                                endCmp ) );
                q.add( setTimestamp( durationFilter.getBegin(), template ) );
                q.add( setTimestamp( durationFilter.getEnd(), template ) );
            } else if ( filter instanceof BeginFilter ) {
                final BeginFilter beginFilter = (BeginFilter) filter;
                timeFilters.add( timeStampColumn + " " + ( beginFilter.isInclusiveBegin() ? "=" : ">" ) + " ?" );
                q.add( setTimestamp( beginFilter.getBegin(), template ) );
            } else if ( filter instanceof EndFilter ) {
                final EndFilter endFilter = (EndFilter) filter;
                timeFilters.add( timeStampColumn + " " + ( endFilter.isInclusiveEnd() ? "=" : "<" ) + " ?" );
                q.add( setTimestamp( endFilter.getEnd(), template ) );
            } else if ( filter instanceof TimeInstantFilter ) {
                final TimeInstantFilter timeInstantFilter = (TimeInstantFilter) filter;
                timeFilters.add( timeStampColumn + " = ?" );
                q.add( setTimestamp( timeInstantFilter.getInstant(), template ) );
            }
        }

        if ( timeFilters.size() > 0 ) {
            q.add( "(" ).add( ArrayUtils.join( " OR ", timeFilters ) ).add( ")" );
        }
    }

    private QueryBuilder.SetObject setTimestamp( final Date date, final Calendar template ) {
        return new QueryBuilder.SetObject() {
            public void set( PreparedStatement stmt, int i )
                                    throws SQLException {
                stmt.setTimestamp( i, new Timestamp( date.getTime() ), template );
            }
        };
    }

    @Override
    public void buildProcedureClause( QueryBuilder q, List<ProcedureFilter> filters, Offering offering ) {
        String procColumn = dsConfig.getColumnName( "procedureId" );
        if ( procColumn == null ) {
            return;
        }
        List<String> procedureFilters = new ArrayList<String>( filters.size() );
        for ( final ProcedureFilter filter : filters ) {
            final String id = offering.getProcedureIdFromHref( filter.getProcedureName() );
            procedureFilters.add( procColumn + " = ?" );
            q.add( QueryBuilder.stringSetter( id ) );
        }
        if ( procedureFilters.size() > 0 ) {
            q.add( "(" ).add( ArrayUtils.join( " OR ", procedureFilters ) ).add( ")" );
        }
    }

    @Override
    public void buildResultClause( QueryBuilder q, List<ResultFilter> filters )
                            throws FilterException {
        for ( ResultFilter rFilter : filters ) {
            ComparisonOperator op = rFilter.getOperator();
            switch ( op.getSubType() ) {
            case PROPERTY_IS_LESS_THAN:
            case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO:
            case PROPERTY_IS_GREATER_THAN:
            case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO:
            case PROPERTY_IS_EQUAL_TO:
            case PROPERTY_IS_NOT_EQUAL_TO:
                buildBinaryPropertyClause( q, op );
                break;
            case PROPERTY_IS_NULL:
                buildPropertyIsNullClause( q, op );
                break;
            case PROPERTY_IS_BETWEEN:
                buildPropertyIsBetweenClause( q, op );
                break;
            // case PROPERTY_IS_LIKE:
            // we only support numerical properties at the moment, so no need for this operation
            // buildPropertyIsLike( q, op );
            // break;
            default:
                LOG.debug( "the comparison op {} is not supported", op.getSubType().name() );
                break;
            }
        }
    }

    private void buildPropertyIsBetweenClause( QueryBuilder q, ComparisonOperator op )
                            throws FilterException {
        PropertyIsBetween p = (PropertyIsBetween) op;
        String propName, lower, upper;
        try {
            propName = ( (PropertyName) p.getExpression() ).getPropertyName();
            lower = ( (Literal<?>) p.getLowerBoundary() ).getValue().toString();
            upper = ( (Literal<?>) p.getUpperBoundary() ).getValue().toString();
        } catch ( ClassCastException ex ) {
            throw new FilterException(
                                       "Unsupported filter operation. PropertyIsBetween only supports PropertyName and Literal." );
        }
        String colName = dsConfig.getColumnName( propName );
        q.add( stringSetter( lower ) ).add( "? <" ).add( colName );
        q.add( "AND" ).add( colName ).add( " < ?" ).add( stringSetter( upper ) );
    }

    private void buildPropertyIsNullClause( QueryBuilder q, ComparisonOperator op ) {
        PropertyIsNull p = (PropertyIsNull) op;
        String propName = p.getPropertyName().getPropertyName();
        String colName = dsConfig.getColumnName( propName );
        q.add( colName + " IS NOT NULL" );
    }

    private void buildBinaryPropertyClause( QueryBuilder q, ComparisonOperator op ) {
        String compOp = null;
        switch ( op.getSubType() ) {
        case PROPERTY_IS_LESS_THAN:
            compOp = "<";
            break;
        case PROPERTY_IS_LESS_THAN_OR_EQUAL_TO:
            compOp = "<=";
            break;
        case PROPERTY_IS_GREATER_THAN:
            compOp = ">";
            break;
        case PROPERTY_IS_GREATER_THAN_OR_EQUAL_TO:
            compOp = ">=";
            break;
        case PROPERTY_IS_EQUAL_TO:
            compOp = "=";
            break;
        case PROPERTY_IS_NOT_EQUAL_TO:
            compOp = "<>";
            break;
        default:
            LOG.debug( "the comparison op {} is not supported", op.getSubType().name() );
            break;
        }
        StringPair propFilter = getSimplePropFilter( op );
        if ( compOp != null && propFilter != null ) {
            final String value = propFilter.second;
            String colName = dsConfig.getColumnName( propFilter.first );

            q.add( colName );
            q.add( compOp + " ?" );

            q.add( QueryBuilder.stringSetter( value ) );
        }
    }

    private StringPair getSimplePropFilter( ComparisonOperator op ) {
        if ( op instanceof BinaryComparisonOperator ) {
            BinaryComparisonOperator bop = (BinaryComparisonOperator) op;
            if ( bop.getParameter1().getType() == Expression.Type.PROPERTY_NAME ) {
                PropertyName pname = (PropertyName) bop.getParameter1();
                if ( bop.getParameter2().getType() == Expression.Type.LITERAL ) {
                    Literal<?> value = (Literal<?>) bop.getParameter2();
                    return new StringPair( pname.getPropertyName(), value.getValue().toString() );
                }
            }
        }
        return null;
    }

}

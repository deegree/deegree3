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
package org.deegree.protocol.sos.filter;

import java.util.LinkedList;
import java.util.List;

/**
 * This is a collection of SOS filters. The SOS spec doesn't have a single filter expression but multiple restriced
 * filter (one for time filter, one for comparsion filter, etc..).
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class FilterCollection {
    private final List<TimeFilter> timeFilters = new LinkedList<TimeFilter>();

    private final List<ProcedureFilter> procedureFilters = new LinkedList<ProcedureFilter>();

    private final List<PropertyFilter> propertyFilters = new LinkedList<PropertyFilter>();

    private final List<ResultFilter> resultFilters = new LinkedList<ResultFilter>();

    private final List<SpatialFilter> spatialFilters = new LinkedList<SpatialFilter>();

    private final List<Filter> otherFilters = new LinkedList<Filter>();

    /**
     */
    public FilterCollection() {
        // emtpy constructor
    }

    /**
     * @param filters
     */
    public FilterCollection( List<? extends Filter> filters ) {
        for ( Filter filter : filters ) {
            add( filter );
        }
    }

    /**
     * @param filters
     */
    public FilterCollection( Filter filters ) {
        add( filters );
    }

    /**
     * Add a filter to the collection.
     *
     * @param filter
     */
    public final void add( Filter filter ) {
        if ( filter == null ) {
            return;
        }
        if ( filter instanceof TimeFilter ) {
            TimeFilter timeFilter = (TimeFilter) filter;
            this.timeFilters.add( timeFilter );
        } else if ( filter instanceof PropertyFilter ) {
            PropertyFilter propertyFilter = (PropertyFilter) filter;
            this.propertyFilters.add( propertyFilter );
        } else if ( filter instanceof ProcedureFilter ) {
            ProcedureFilter procedureFilter = (ProcedureFilter) filter;
            this.procedureFilters.add( procedureFilter );
        } else if ( filter instanceof ResultFilter ) {
            ResultFilter resultFilter = (ResultFilter) filter;
            this.resultFilters.add( resultFilter );
        } else if ( filter instanceof SpatialFilter ) {
            SpatialFilter spatialFilter = (SpatialFilter) filter;
            this.spatialFilters.add( spatialFilter );
        } else {
            this.otherFilters.add( filter );
        }
    }

    /**
     * Add a filters to the collection.
     *
     * @param filters
     */
    public final void add( List<? extends Filter> filters ) {
        if ( filters == null ) {
            return;
        }
        for ( Filter filter : filters ) {
            add( filter );
        }
    }

    /**
     * @return the propertyFilter
     */
    public List<PropertyFilter> getPropertyFilter() {
        return new LinkedList<PropertyFilter>( propertyFilters );
    }

    /**
     * @return the procedureFilters
     */
    public List<ProcedureFilter> getProcedureFilter() {
        return new LinkedList<ProcedureFilter>( procedureFilters );
    }

    /**
     * @return the timeFilter
     */
    public List<TimeFilter> getTimeFilter() {
        return new LinkedList<TimeFilter>( timeFilters );
    }

    /**
     * @return the resultFilter
     */
    public List<ResultFilter> getResultFilter() {
        return new LinkedList<ResultFilter>( resultFilters );
    }

    /**
     * @return the spatialFilter
     */
    public List<SpatialFilter> getSpatialFilter() {
        return new LinkedList<SpatialFilter>( spatialFilters );
    }

    /**
     * @return all filter
     */
    public List<Filter> getAllFilter() {
        List<Filter> result = new LinkedList<Filter>( otherFilters );
        result.addAll( propertyFilters );
        result.addAll( timeFilters );
        result.addAll( resultFilters );
        result.addAll( procedureFilters );
        result.addAll( spatialFilters );
        return result;
    }

}

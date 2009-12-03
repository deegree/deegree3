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
package org.deegree.feature;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.PropertyName;
import org.deegree.filter.sort.SortProperty;
import org.deegree.gml.GMLVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utility methods for common tasks that involve {@link Feature} and {@link FeatureCollection} objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Features {

    private static final Logger LOG = LoggerFactory.getLogger( Features.class );

    /**
     * Returns a sorted {@link FeatureCollection}.
     * 
     * @param fc
     *            feature collection to be sorted, must not be <code>null</code>
     * @param sortCrits
     *            sort criteria
     * @return sorted feature collection, never <code>null</code>
     */
    public static FeatureCollection sortFc( final FeatureCollection fc, final SortProperty[] sortCrits ) {

        FeatureCollection sortedFc = fc;
        if ( sortCrits != null && sortCrits.length > 0 ) {
            SortedSet<Feature> sortedFeatures = new TreeSet<Feature>( new Comparator<Feature>() {
                @SuppressWarnings( { "unchecked", "synthetic-access" })
                @Override
                public int compare( Feature f1, Feature f2 ) {
                    int order = 0;
                    for ( SortProperty sortCrit : sortCrits ) {
                        PropertyName propName = sortCrit.getSortProperty();
                        try {
                            // TODO handle multi properties correctly
                            Object[] values1 = f1.getPropertyValues( propName, GMLVersion.GML_31 );
                            Object[] values2 = f2.getPropertyValues( propName, GMLVersion.GML_31 );
                            for ( Object value1 : values1 ) {
                                if ( value1 != null ) {
                                    for ( Object value2 : values2 ) {
                                        if ( value2 != null ) {
                                            Pair<Object, Object> comparablePair = makeComparable( value1, value2 );
                                            order = ( (Comparable<Object>) comparablePair.first ).compareTo( comparablePair.second );
                                            if ( !sortCrit.getSortOrder() ) {
                                                order *= -1;
                                            }
                                        }
                                    }
                                }
                            }
                        } catch ( Exception e ) {
                            LOG.debug( "Cannot compare values: " + e.getMessage() );
                        }
                    }
                    return order;
                }
            } );
            for ( Feature feature : fc ) {
                sortedFeatures.add( feature );
            }
            sortedFc = new GenericFeatureCollection( fc.getId(), sortedFeatures );
        }
        return sortedFc;
    }

    private static Pair<Object, Object> makeComparable( Object value1, Object value2 )
                            throws FilterEvaluationException {
        Pair<Object, Object> result = new Pair<Object, Object>( value1, value2 );
        if ( !( value1 instanceof String ) ) {
            if ( value1 instanceof Number ) {
                result = new Pair<Object, Object>( value1, new BigDecimal( value2.toString() ) );
            } else if ( value1 instanceof Date ) {
                try {
                    result = new Pair<Object, Object>( value1, DateUtils.parseISO8601Date( value2.toString() ) );
                } catch ( ParseException e ) {
                    throw new FilterEvaluationException( e.getMessage() );
                }
            }
        } else if ( !( value2 instanceof String ) ) {
            if ( value2 instanceof Number ) {
                result = new Pair<Object, Object>( new BigDecimal( value1.toString() ), value2 );
            } else if ( value2 instanceof Date ) {
                try {
                    result = new Pair<Object, Object>( DateUtils.parseISO8601Date( value1.toString() ), value2 );
                } catch ( ParseException e ) {
                    throw new FilterEvaluationException( e.getMessage() );
                }
            }
        }
        return result;
    }
}

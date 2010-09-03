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

import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.property.Property;
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
                            TypedObjectNode[] values1 = f1.evalXPath( propName, GMLVersion.GML_31 );
                            TypedObjectNode[] values2 = f2.evalXPath( propName, GMLVersion.GML_31 );
                            for ( TypedObjectNode value1 : values1 ) {
                                if ( value1 != null ) {
                                    for ( TypedObjectNode value2 : values2 ) {
                                        if ( value2 != null ) {
                                            Pair<Object, Object> comparablePair = getPrimitives( value1, value2 );
                                            order = ( (Comparable) comparablePair.first ).compareTo( comparablePair.second );
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

    /**
     * Creates a pair of {@link PrimitiveValue} instances from the given {@link TypedObjectNode} while trying to
     * preserve primitive type information.
     * 
     * @param value1
     * @param value2
     * @return
     * @throws FilterEvaluationException
     */
    private static Pair<Object, Object> getPrimitives( Object value1, Object value2 )
                            throws FilterEvaluationException {

        if ( value1 instanceof Property ) {
            value1 = ( (Property) value1 ).getValue();
        }
        if ( !( value1 instanceof PrimitiveValue ) ) {
            value1 = value1.toString();
        }
        if ( value2 instanceof Property ) {
            value2 = ( (Property) value2 ).getValue();
        }
        if ( !( value2 instanceof PrimitiveValue ) ) {
            value2 = value2.toString();
        }
        return PrimitiveValue.makeComparable( value1, value2 );
    }
}
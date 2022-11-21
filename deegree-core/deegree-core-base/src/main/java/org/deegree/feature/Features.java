/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2022 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH
 and
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.feature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.deegree.commons.tom.ElementNode;
import org.deegree.commons.tom.Reference;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.tom.TypedObjectNode;
import org.deegree.commons.tom.array.TypedObjectNodeArray;
import org.deegree.commons.tom.gml.property.Property;
import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.Pair;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.xpath.TypedObjectNodeXPathEvaluator;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.sort.SortProperty;
import org.deegree.geometry.Geometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utility methods for common tasks that involve {@link Feature} and {@link FeatureCollection} objects.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 * 
 */
public class Features {

    private static final Logger LOG = LoggerFactory.getLogger( Features.class );

    /**
     * Returns all members of the given {@link FeatureInputStream} as a {@link FeatureCollection}.
     * <p>
     * NOTE: This method should not be called for very large result sets, as it introduces the overhead of keeping all
     * created feature instances in memory. The returned collection will contain all {@link Feature}s instances from the
     * current position in the iteration sequence.
     * </p>
     * 
     * @return members as feature collection, never <code>null</code>
     */
    public static FeatureCollection toCollection( FeatureInputStream is ) {
        List<Feature> members = new ArrayList<Feature>();
        for ( Feature feature : is ) {
            members.add( feature );
        }
        is.close();
        return new GenericFeatureCollection( null, members );
    }

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

        // TODO
        final TypedObjectNodeXPathEvaluator evaluator = new TypedObjectNodeXPathEvaluator();

        FeatureCollection sortedFc = fc;
        if ( sortCrits != null && sortCrits.length > 0 ) {
            List<Feature> sortedFeatures = new ArrayList<Feature>();
            for ( Feature feature : fc ) {
                sortedFeatures.add( feature );
            }
            Collections.sort( sortedFeatures, new Comparator<Feature>() {
                @SuppressWarnings({ "unchecked", "synthetic-access" })
                @Override
                public int compare( Feature f1, Feature f2 ) {
                    int order = 0;
                    for ( SortProperty sortCrit : sortCrits ) {
                        ValueReference propName = sortCrit.getSortProperty();
                        try {
                            // TODO handle multi properties correctly
                            TypedObjectNode[] values1 = evaluator.eval( f1, propName );
                            TypedObjectNode[] values2 = evaluator.eval( f2, propName );
                            for ( TypedObjectNode value1 : values1 ) {
                                if ( value1 != null ) {
                                    for ( TypedObjectNode value2 : values2 ) {
                                        if ( value2 != null ) {
                                            Pair<Object, Object> comparablePair = getPrimitives( value1, value2 );
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
            sortedFc = new GenericFeatureCollection( fc.getId(), sortedFeatures );
        }
        return sortedFc;
    }

    /**
     * Clears out features with identical ids.
     * 
     * @param col
     * @return a new list of features
     */
    public static List<Feature> clearDuplicates( Collection<Feature> col ) {
        List<Feature> list = new ArrayList<Feature>();
        HashSet<String> ids = new HashSet<String>();
        for ( Feature f : col ) {
            if ( !ids.contains( f.getId() ) ) {
                list.add( f );
                ids.add( f.getId() );
            }
        }
        return list;
    }

    /**
     * Creates a pair of {@link PrimitiveValue} instances from the given {@link TypedObjectNode} while trying to
     * preserve primitive type information.
     * 
     * @param value1
     * @param value2
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

    /**
     * Determines all {@link Feature} and {@link Geometry} objects contained in the given {@link TypedObjectNode} and
     * their ids. Does <code>not</code> include internal referenced {@link Feature}s.
     * 
     * @param node
     *            typed object node to be scanned, can be <code>null</code>
     * @param geometries
     *            found geometry objects (not {@link Reference}s), must not be <code>null</code>
     * @param features
     *            found feature objects (not {@link Reference}s), must not be <code>null</code>
     * @param fids
     *            found feature ids, must not be <code>null</code>
     * @param gids
     *            found geometry ids, must not be <code>null</code>
     */
    public static void findFeaturesAndGeometries( TypedObjectNode node, Set<Geometry> geometries, Set<Feature> features,
                                                  Set<String> fids, Set<String> gids ) {

        if ( node instanceof ElementNode ) {
            ElementNode elNode = (ElementNode) node;
            for ( TypedObjectNode child : elNode.getChildren() ) {
                findFeaturesAndGeometries( child, geometries, features, fids, gids );
            }
        } else if ( node instanceof org.deegree.commons.tom.Object ) {
            if ( node instanceof Reference<?> ) {
                Reference<?> ref = (Reference<?>) node;
                if ( ref.isResolved() && !ref.isInternalResolved() ) {
                    node = ref.getReferencedObject();
                } else {
                    try {
                        TypedObjectNode referencedObject = ref.getReferencedObject();
                        if ( !ref.isInternalResolved() )
                            node = referencedObject;
                        else
                            return;
                    } catch ( ReferenceResolvingException e ) {
                        LOG.warn( "Unable to resolve external reference '{}'. Ignoring.", ref.getURI() );
                        return;
                    }
                }
            }

            if ( node instanceof Geometry ) {
                Geometry geometry = (Geometry) node;
                if ( geometry.getId() == null || !( gids.contains( geometry.getId() ) ) ) {
                    geometries.add( geometry );
                    if ( geometry.getId() != null ) {
                        gids.add( geometry.getId() );
                    }
                }
            } else if ( node instanceof Feature ) {
                Feature feature = (Feature) node;
                if ( feature.getId() == null || !( fids.contains( feature.getId() ) ) ) {
                    features.add( feature );
                    if ( feature.getId() != null ) {
                        fids.add( feature.getId() );
                    }
                    for ( Property property : feature.getProperties() ) {
                        findFeaturesAndGeometries( property, geometries, features, fids, gids );
                    }
                }
            }
        } else if ( node instanceof TypedObjectNodeArray<?> ) {
            TypedObjectNodeArray<?> tonArray = (TypedObjectNodeArray<?>) node;
            for ( TypedObjectNode child : tonArray.getElements() ) {
                findFeaturesAndGeometries( child, geometries, features, fids, gids );
            }
        }
    }
}

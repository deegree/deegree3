//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

 Occam Labs Schmitz & Schneider GbR
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.layer.persistence.feature;

import static java.lang.System.currentTimeMillis;
import static org.deegree.commons.utils.CollectionUtils.map;
import static org.deegree.commons.utils.math.MathUtils.round;
import static org.deegree.commons.utils.time.DateUtils.formatISO8601Date;
import static org.deegree.commons.utils.time.DateUtils.formatISO8601DateWOMS;
import static org.deegree.layer.dims.Dimension.formatDimensionValueList;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.deegree.commons.tom.primitive.PrimitiveValue;
import org.deegree.commons.utils.CollectionUtils.Mapper;
import org.deegree.feature.persistence.FeatureStore;
import org.deegree.feature.persistence.query.Query;
import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.feature.types.FeatureType;
import org.deegree.filter.Expression;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.Filters;
import org.deegree.filter.Operator;
import org.deegree.filter.OperatorFilter;
import org.deegree.filter.comparison.PropertyIsBetween;
import org.deegree.filter.comparison.PropertyIsEqualTo;
import org.deegree.filter.expression.Literal;
import org.deegree.filter.expression.ValueReference;
import org.deegree.filter.logical.And;
import org.deegree.filter.logical.Or;
import org.deegree.geometry.Envelope;
import org.deegree.layer.AbstractLayer;
import org.deegree.layer.LayerQuery;
import org.deegree.layer.dims.Dimension;
import org.deegree.layer.dims.DimensionInterval;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.style.StyleRef;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.style.utils.Styles;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class FeatureLayer extends AbstractLayer {

    private static final Logger LOG = getLogger( FeatureLayer.class );

    private FeatureStore featureStore;

    private OperatorFilter filter;

    private final QName featureType;

    public FeatureLayer( LayerMetadata md, FeatureStore featureStore, QName featureType, OperatorFilter filter ) {
        super( md );
        this.featureStore = featureStore;
        this.featureType = featureType;
        this.filter = filter;
    }

    @Override
    public FeatureLayerData mapQuery( final LayerQuery query, List<String> headers )
                            throws OWSException {
        OperatorFilter filter = this.filter;
        StyleRef style = query.getStyle( getMetadata().getName() );
        filter = Filters.and( filter, Styles.getStyleFilters( style.getStyle(), query.getScale() ) );
        filter = Filters.and( filter, query.getFilter( getMetadata().getName() ) );
        filter = Filters.and( filter, getDimensionFilter( query.getDimensions(), headers ) );

        final Envelope bbox = query.getEnvelope();

        Set<Expression> exprs = new HashSet<Expression>( Styles.getGeometryExpressions( style.getStyle() ) );

        final ValueReference geomProp;

        if ( exprs.size() == 1 && exprs.iterator().next() instanceof ValueReference ) {
            geomProp = (ValueReference) exprs.iterator().next();
        } else {
            geomProp = null;
        }

        QName ftName = featureType == null ? style.getStyle().getFeatureType() : featureType;
        if ( ftName != null && featureStore.getSchema().getFeatureType( ftName ) == null ) {
            LOG.warn( "FeatureType '" + ftName + "' is not known to the FeatureStore." );
            return null;
        }

        List<Query> queries = new LinkedList<Query>();
        Integer maxFeats = query.getRenderingOptions().getMaxFeatures().get( getMetadata().getName() );
        final int maxFeatures = maxFeats == null ? -1 : maxFeats;
        if ( featureType == null && featureStore != null ) {
            final Filter filter2 = filter;
            queries.addAll( map( featureStore.getSchema().getFeatureTypes( null, false, false ),
                                 new Mapper<Query, FeatureType>() {
                                     @Override
                                     public Query apply( FeatureType u ) {
                                         Filter fil = Filters.addBBoxConstraint( bbox, filter2, geomProp );
                                         return new Query( u.getName(), fil, round( query.getScale() ), maxFeatures,
                                                           query.getResolution() );
                                     }
                                 } ) );
        } else {
            Query fquery = new Query( featureType, Filters.addBBoxConstraint( bbox, filter, geomProp ),
                                      round( query.getScale() ), maxFeatures, query.getResolution() );
            queries.add( fquery );
        }

        if ( queries.isEmpty() ) {
            LOG.warn( "No queries were generated. Is the configuration correct?" );
            return null;
        }

        try {
            Style s = style.getStyle().filter( query.getScale() );
            FeatureInputStream features = featureStore.query( queries.toArray( new Query[queries.size()] ) );
            return new FeatureLayerData( features, maxFeatures, s );
        } catch ( FilterEvaluationException e ) {
            LOG.warn( "A filter could not be evaluated. The error was '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        } catch ( Throwable e ) {
            LOG.warn( "Data could not be fetched from the feature store. The error was '{}'.", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
        return null;
    }

    @Override
    public FeatureLayerData infoQuery( LayerQuery query, List<String> headers ) {
        return null;
    }

    /**
     * @param dims
     * @return a filter or null, if no dimensions have been requested
     * @throws OWSException
     * @throws MissingDimensionValue
     * @throws InvalidDimensionValue
     */
    private OperatorFilter getDimensionFilter( Map<String, List<?>> dims, List<String> headers )
                            throws OWSException {
        LinkedList<Operator> ops = new LinkedList<Operator>();

        Dimension<?> time = getMetadata().getDimensions().get( "time" );

        if ( time != null ) {
            final ValueReference property = new ValueReference( time.getPropertyName() );

            List<?> vals = dims.get( "time" );

            if ( vals == null ) {
                vals = time.getDefaultValue();
                if ( vals == null ) {
                    throw new OWSException( "The TIME parameter was missing.", "MissingDimensionValue", "time" );
                }
                String defVal = formatDimensionValueList( vals, true );

                headers.add( "99 Default value used: time=" + defVal + " ISO8601" );
            }

            Operator[] os = new Operator[vals.size()];
            int i = 0;
            for ( Object o : vals ) {
                if ( !time.getNearestValue() && !time.isValid( o ) ) {
                    String msg = "The value " + ( o instanceof Date ? formatISO8601DateWOMS( (Date) o ) : o.toString() )
                                 + " for dimension TIME was invalid.";
                    throw new OWSException( msg, "InvalidDimensionValue", "time" );
                }
                Date theVal = null;
                if ( o instanceof DimensionInterval<?, ?, ?> ) {
                    DimensionInterval<?, ?, ?> iv = (DimensionInterval<?, ?, ?>) o;
                    final String min = formatISO8601DateWOMS( (Date) iv.min );
                    final String max = formatISO8601DateWOMS( (Date) iv.max );
                    os[i++] = new PropertyIsBetween( property, new Literal<PrimitiveValue>( min ),
                                                     new Literal<PrimitiveValue>( max ), false, null );
                } else if ( o.toString().equalsIgnoreCase( "current" ) ) {
                    if ( !time.getCurrent() ) {
                        String msg = "The value 'current' for TIME was invalid.";
                        throw new OWSException( msg, "InvalidDimensionValue", "time" );
                    }
                    theVal = new Date( currentTimeMillis() );
                } else if ( o instanceof Date ) {
                    theVal = (Date) o;
                }
                if ( theVal != null ) {
                    if ( time.getNearestValue() ) {
                        Object nearest = time.getNearestValue( theVal );
                        if ( !nearest.equals( theVal ) ) {
                            theVal = (Date) nearest;
                            headers.add( "99 Nearest value used: time=" + formatISO8601DateWOMS( theVal ) + " "
                                         + time.getUnits() );
                        }
                    }
                    Literal<PrimitiveValue> lit = new Literal<PrimitiveValue>( formatISO8601DateWOMS( theVal ) );
                    os[i++] = new PropertyIsEqualTo( property, lit, false, null );
                }
            }
            if ( os.length > 1 ) {
                if ( !time.getMultipleValues() ) {
                    String msg = "Multiple values are not allowed for TIME.";
                    throw new OWSException( msg, "InvalidDimensionValue", "time" );
                }
                try {
                    ops.add( new Or( os ) );
                } catch ( Throwable e ) {
                    // will not happen, look at the if condition
                }
            } else {
                ops.add( os[0] );
            }
        }

        for ( String name : getMetadata().getDimensions().keySet() ) {
            Dimension<?> dim = getMetadata().getDimensions().get( name );
            final ValueReference property = new ValueReference( dim.getPropertyName() );

            List<?> vals = dims.get( name );

            if ( vals == null ) {
                vals = dim.getDefaultValue();
                if ( vals == null ) {
                    throw new OWSException( "The dimension value for " + name + " was missing.",
                                            "MissingDimensionValue", name );
                }
                String units = dim.getUnits();
                if ( name.equals( "elevation" ) ) {
                    headers.add( "99 Default value used: elevation=" + formatDimensionValueList( vals, false ) + " "
                                 + ( units == null ? "m" : units ) );
                } else if ( name.equals( "time" ) ) {
                    headers.add( "99 Default value used: time=" + formatDimensionValueList( vals, true ) + " "
                                 + ( units == null ? "ISO8601" : units ) );
                } else {
                    headers.add( "99 Default value used: DIM_" + name + "=" + formatDimensionValueList( vals, false )
                                 + " " + units );
                }
            }

            Operator[] os = new Operator[vals.size()];
            int i = 0;

            for ( Object o : vals ) {

                if ( !dim.getNearestValue() && !dim.isValid( o ) ) {
                    throw new OWSException( "The value " + o.toString() + " was not valid for dimension " + name + ".",
                                            "InvalidDimensionValue", name );
                }

                if ( o instanceof DimensionInterval<?, ?, ?> ) {
                    DimensionInterval<?, ?, ?> iv = (DimensionInterval<?, ?, ?>) o;
                    final String min;
                    if ( iv.min instanceof Date ) {
                        min = formatISO8601Date( (Date) iv.min );
                    } else {
                        min = ( (Number) iv.min ).toString();
                    }
                    final String max;
                    if ( iv.max instanceof Date ) {
                        max = formatISO8601Date( (Date) iv.max );
                    } else if ( iv.max instanceof String ) {
                        max = formatISO8601Date( new Date() );
                    } else {
                        max = ( (Number) iv.max ).toString();
                    }
                    os[i++] = new PropertyIsBetween( property, new Literal<PrimitiveValue>( min ),
                                                     new Literal<PrimitiveValue>( max ), false, null );
                } else {
                    if ( dim.getNearestValue() ) {
                        Object nearest = dim.getNearestValue( o );
                        if ( !nearest.equals( o ) ) {
                            o = nearest;
                            if ( "elevation".equals( name ) ) {
                                headers.add( "99 Nearest value used: elevation=" + o + " " + dim.getUnits() );
                            } else {
                                headers.add( "99 Nearest value used: DIM_" + name + "=" + o + " " + dim.getUnits() );
                            }
                        }
                    }
                    os[i++] = new PropertyIsEqualTo( new ValueReference( dim.getPropertyName() ),
                                                     new Literal<PrimitiveValue>( o.toString() ), false, null );
                }
            }
            if ( os.length > 1 ) {
                if ( !dim.getMultipleValues() ) {
                    throw new OWSException( "Multiple values are not allowed for ELEVATION.", "InvalidDimensionValue",
                                            "elevation" );
                }
                ops.add( new Or( os ) );
            } else {
                ops.add( os[0] );
            }
        }

        if ( ops.isEmpty() ) {
            return null;
        }
        if ( ops.size() > 1 ) {
            return new OperatorFilter( new And( ops.toArray( new Operator[ops.size()] ) ) );
        }
        return new OperatorFilter( ops.get( 0 ) );
    }

}

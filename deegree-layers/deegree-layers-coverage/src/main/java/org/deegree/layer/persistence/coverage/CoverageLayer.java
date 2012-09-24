//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.layer.persistence.coverage;

import static org.deegree.coverage.rangeset.Interval.Closure.open;
import static org.deegree.coverage.rangeset.ValueType.Void;
import static org.deegree.coverage.raster.interpolation.InterpolationType.BILINEAR;
import static org.deegree.coverage.raster.interpolation.InterpolationType.NEAREST_NEIGHBOR;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deegree.coverage.rangeset.AxisSubset;
import org.deegree.coverage.rangeset.Interval;
import org.deegree.coverage.rangeset.RangeSet;
import org.deegree.coverage.rangeset.SingleValue;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.MultiResolutionRaster;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.geometry.Envelope;
import org.deegree.layer.AbstractLayer;
import org.deegree.layer.LayerQuery;
import org.deegree.layer.dims.Dimension;
import org.deegree.layer.dims.DimensionInterval;
import org.deegree.layer.metadata.LayerMetadata;
import org.deegree.protocol.ows.exception.OWSException;
import org.deegree.rendering.r2d.context.MapOptions.Interpolation;
import org.deegree.style.StyleRef;
import org.deegree.style.se.unevaluated.Style;
import org.slf4j.Logger;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class CoverageLayer extends AbstractLayer {

    private static final Logger LOG = getLogger( CoverageLayer.class );

    private final AbstractRaster raster;

    private final MultiResolutionRaster multiraster;

    public CoverageLayer( LayerMetadata md, AbstractRaster raster, MultiResolutionRaster multiraster ) {
        super( md );
        this.raster = raster;
        this.multiraster = multiraster;
    }

    @Override
    public CoverageLayerData mapQuery( LayerQuery query, List<String> headers )
                            throws OWSException {
        try {
            Envelope bbox = query.getEnvelope();

            RangeSet filter = getDimensionFilter( query.getDimensions(), headers );

            StyleRef ref = query.getStyle();
            if ( !ref.isResolved() ) {
                ref.resolve( getMetadata().getStyles().get( ref.getName() ) );
            }
            Style style = ref.getStyle();
            // handle SLD/SE scale settings
            style = style == null ? null : style.filter( query.getScale() );

            InterpolationType interpol = NEAREST_NEIGHBOR;
            Interpolation fromRequest = query.getRenderingOptions().getInterpolation( getMetadata().getName() );
            if ( fromRequest != null ) {
                switch ( fromRequest ) {
                case BICUBIC:
                    LOG.warn( "Raster API does not support bicubic interpolation, using bilinear instead." );
                case BILINEAR:
                    interpol = BILINEAR;
                    break;
                case NEARESTNEIGHBOR:
                case NEARESTNEIGHBOUR:
                    interpol = NEAREST_NEIGHBOR;
                    break;
                }
            }

            AbstractRaster raster = this.raster;
            if ( raster == null ) {
                raster = multiraster.getRaster( query.getResolution() );
            }

            return new CoverageLayerData( raster, bbox, query.getWidth(), query.getHeight(), interpol, filter, style,
                                          getMetadata().getFeatureTypes().get( 0 ) );
        } catch ( OWSException e ) {
            throw e;
        } catch ( Throwable e ) {
            LOG.warn( "Unable to prepare rendering of raster layer: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
        return null;
    }

    @Override
    public CoverageLayerData infoQuery( LayerQuery query, List<String> headers )
                            throws OWSException {
        try {
            Envelope bbox = query.calcClickBox( query.getRenderingOptions().getFeatureInfoRadius( getMetadata().getName() ) );

            RangeSet filter = getDimensionFilter( query.getDimensions(), headers );

            StyleRef ref = query.getStyle();
            if ( !ref.isResolved() ) {
                ref.resolve( getMetadata().getStyles().get( ref.getName() ) );
            }
            Style style = ref.getStyle();
            // handle SLD/SE scale settings
            style = style == null ? null : style.filter( query.getScale() );

            AbstractRaster raster = this.raster;
            if ( raster == null ) {
                raster = multiraster.getRaster( query.getResolution() );
            }

            return new CoverageLayerData( raster, bbox, query.getWidth(), query.getHeight(),
                                          InterpolationType.NEAREST_NEIGHBOR, filter, style,
                                          getMetadata().getFeatureTypes().get( 0 ) );
        } catch ( OWSException e ) {
            throw e;
        } catch ( Throwable e ) {
            LOG.warn( "Unable to prepare feature info of raster layer: {}", e.getLocalizedMessage() );
            LOG.trace( "Stack trace:", e );
        }
        return null;
    }

    private RangeSet getDimensionFilter( Map<String, List<?>> dims, List<String> headers )
                            throws OWSException {

        LinkedList<AxisSubset> ops = new LinkedList<AxisSubset>();

        // TODO TIME, how to do this with raster API? Needs test data.
        for ( String name : getMetadata().getDimensions().keySet() ) {
            List<SingleValue<?>> singleValues = new LinkedList<SingleValue<?>>();
            List<Interval<?, ?>> intervals = new LinkedList<Interval<?, ?>>();

            Dimension<?> dim = getMetadata().getDimensions().get( name );

            List<?> vals = dims.get( name );

            if ( dims.get( name ) == null ) {
                vals = dim.getDefaultValue();

                if ( vals == null ) {
                    throw new OWSException( "The value for the " + name + " dimension is missing.",
                                            "MissingDimensionValue" );
                }
                String units = dim.getUnits();
                if ( name.equals( "elevation" ) ) {
                    headers.add( "99 Default value used: elevation=" + vals.get( 0 ) + " "
                                 + ( units == null ? "m" : units ) );
                } else {
                    headers.add( "99 Default value used: DIM_" + name + "=" + vals.get( 0 ) + " " + units );
                }
            }

            for ( Object o : vals ) {

                if ( !dim.getNearestValue() && !dim.isValid( o ) ) {
                    throw new OWSException( "The value for the " + name + " dimension is invalid: " + o.toString(),
                                            "InvalidDimensionValue" );
                }

                if ( o instanceof DimensionInterval<?, ?, ?> ) {
                    DimensionInterval<?, ?, ?> iv = (DimensionInterval<?, ?, ?>) o;
                    final String min = iv.min.toString();
                    final String max = iv.max.toString();
                    intervals.add( new Interval<String, String>( new SingleValue<String>( Void, min ),
                                                                 new SingleValue<String>( Void, max ), open, null,
                                                                 false, null ) );
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
                    singleValues.add( new SingleValue<String>( Void, o.toString() ) );
                }
            }

            if ( singleValues.size() > 1 && ( !dim.getMultipleValues() ) ) {
                throw new OWSException( "The value for ELEVATION is invalid: " + vals.toString(),
                                        "InvalidDimensionValue", "elevation" );
            }

            ops.add( new AxisSubset( BandType.BAND_0.name(), null, intervals, singleValues ) );
        }

        if ( ops.isEmpty() ) {
            return null;
        }
        return new RangeSet( ops );
    }

}

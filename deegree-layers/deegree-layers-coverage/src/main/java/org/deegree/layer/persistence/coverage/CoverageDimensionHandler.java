//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.layer.persistence.coverage;

import static org.deegree.coverage.rangeset.Interval.Closure.open;
import static org.deegree.coverage.rangeset.ValueType.Void;
import static org.deegree.coverage.raster.data.info.BandType.BAND_0;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deegree.commons.ows.exception.OWSException;
import org.deegree.coverage.rangeset.AxisSubset;
import org.deegree.coverage.rangeset.Interval;
import org.deegree.coverage.rangeset.RangeSet;
import org.deegree.coverage.rangeset.SingleValue;
import org.deegree.layer.dims.Dimension;
import org.deegree.layer.dims.DimensionInterval;

/**
 * Responsible for creating coverage range sets from dimension values/definitions.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
class CoverageDimensionHandler {

    private Map<String, Dimension<?>> layerDims;

    CoverageDimensionHandler( Map<String, Dimension<?>> layerDims ) {
        this.layerDims = layerDims;
    }

    public Dimension<?> getDimension() {
        if ( layerDims != null ) {
            for ( Dimension<?> layerDim : layerDims.values() ) {
                return layerDim;
            }
        }
        return null;
    }

    RangeSet getDimensionFilter( Map<String, List<?>> dims, List<String> headers )
                            throws OWSException {

        LinkedList<AxisSubset> ops = new LinkedList<AxisSubset>();

        // TODO TIME, how to do this with raster API? Needs test data.
        for ( String name : layerDims.keySet() ) {
            List<SingleValue<?>> singleValues = new LinkedList<SingleValue<?>>();
            List<Interval<?, ?>> intervals = new LinkedList<Interval<?, ?>>();

            Dimension<?> dim = layerDims.get( name );

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
                handleDimensionValue( dim, o, intervals, singleValues, headers, name );
            }

            if ( singleValues.size() > 1 && ( !dim.getMultipleValues() ) ) {
                throw new OWSException( "The value for ELEVATION is invalid: " + vals.toString(),
                                        "InvalidDimensionValue", "elevation" );
            }

            ops.add( new AxisSubset( BAND_0.name(), null, intervals, singleValues ) );
        }

        if ( ops.isEmpty() ) {
            return null;
        }
        return new RangeSet( ops );
    }

    private void handleDimensionValue( Dimension<?> dim, Object o, List<Interval<?, ?>> intervals,
                                       List<SingleValue<?>> singleValues, List<String> headers, String name )
                                                               throws OWSException {
        if ( !dim.getNearestValue() && !dim.isValid( o ) ) {
            throw new OWSException( "The value for the " + name + " dimension is invalid: " + o.toString(),
                                    "InvalidDimensionValue" );
        }

        // extract min/max values from extent
        Comparable extmin = null;
        Comparable extmax = null;
        for ( Object v : dim.getExtent() ) {
            if ( v instanceof DimensionInterval<?, ?, ?> ) {
                if ( extmin == null ) {
                    extmin = (Comparable<?>) ( (DimensionInterval<?, ?, ?>) v ).min;
                } else {
                    if ( extmin.compareTo( (Comparable<?>) ( (DimensionInterval<?, ?, ?>) v ).min ) > 0 ) {
                        extmin = (Comparable<?>) ( (DimensionInterval<?, ?, ?>) v ).min;
                    }
                }
                if ( extmax == null ) {
                    extmax = (Comparable<?>) ( (DimensionInterval<?, ?, ?>) v ).max;
                } else {
                    if ( extmax.compareTo( (Comparable<?>) ( (DimensionInterval<?, ?, ?>) v ).max ) < 0 ) {
                        extmax = (Comparable<?>) ( (DimensionInterval<?, ?, ?>) v ).max;
                    }
                }
            }
        }

        if ( o instanceof DimensionInterval<?, ?, ?> ) {
            DimensionInterval<?, ?, ?> iv = (DimensionInterval<?, ?, ?>) o;
            // repair min/max values to extent min/max values if applicable (raster API fails if boundary values are not
            // correct)
            final String min;
            if ( extmin.compareTo( iv.min ) > 0 ) {
                min = extmin.toString();
            } else {
                min = iv.min.toString();
            }
            final String max;
            if ( extmax.compareTo( iv.max ) < 0 ) {
                max = extmax.toString();
            } else {
                max = iv.max.toString();
            }
            intervals.add( new Interval<String, String>( new SingleValue<String>( Void, min ),
                                                         new SingleValue<String>( Void, max ), open, null, false,
                                                         null ) );
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

}

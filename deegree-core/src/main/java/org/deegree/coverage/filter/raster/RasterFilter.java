//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.coverage.filter.raster;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.deegree.coverage.filter.CoverageFilter;
import org.deegree.coverage.rangeset.AxisSubset;
import org.deegree.coverage.rangeset.Interval;
import org.deegree.coverage.rangeset.RangeSet;
import org.deegree.coverage.rangeset.SingleValue;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.BandType;

/**
 * The <code>RasterFilter</code> enables a the evaluation of bands in a raster by their values, as well as the selection
 * of specific bands in a raster.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class RasterFilter extends CoverageFilter {

    /**
     * @param raster
     * 
     */
    public RasterFilter( AbstractRaster raster ) {
        super( raster );
    }

    @Override
    public AbstractRaster apply( RangeSet sourceRangeSet, RangeSet targetRangeset ) {
        AbstractRaster result = raster();
        if ( rangeSetsAreApplicable( sourceRangeSet, targetRangeset ) ) {
            List<AxisSubset> requestedAxis = targetRangeset.getAxisDescriptions();
            if ( requestedAxis != null && !requestedAxis.isEmpty() ) {
                // create a copy so that the original rangeset will not be modified.
                List<AxisSubset> copyRA = new ArrayList<AxisSubset>( requestedAxis );
                Map<BandType, AxisSubset> referencedBands = getReferencedBands( copyRA );
                BandType[] bands = null;
                if ( referencedBands != null ) {
                    bands = referencedBands.keySet().toArray( new BandType[referencedBands.keySet().size()] );
                    // filter the bands.
                    result = raster().getSubRaster( raster().getEnvelope(), bands );
                    boolean applyData = false;
                    for ( AxisSubset ass : referencedBands.values() ) {
                        applyData = ass.hasAxisConstraints();
                        if ( applyData ) {
                            break;
                        }
                    }
                    if ( applyData ) {
                        result = applyDataFilter( result, requestedAxis, referencedBands );
                    }
                }
            }

        }
        return result;
    }

    /**
     * @param subsetRaster
     * @param axisFilter
     * @return
     */
    private AbstractRaster applyDataFilter( AbstractRaster subsetRaster, List<AxisSubset> axisFilter,
                                            Map<BandType, AxisSubset> bands ) {
        SimpleRaster simpleRaster = subsetRaster.getAsSimpleRaster();
        SimpleRaster result = simpleRaster.createCompatibleSimpleRaster();
        RasterData output = result.getRasterData();
        RasterData oldData = simpleRaster.getRasterData();
        switch ( output.getDataInfo().dataType ) {
        case BYTE:
            applyByteFilter( output, oldData, axisFilter, bands );
            break;
        case DOUBLE:
            applyDoubleFilter( output, oldData, axisFilter, bands );
            break;
        case FLOAT:
            applyFloatFilter( output, oldData, axisFilter, bands );
            break;
        case INT:
            applyIntFilter( output, oldData, axisFilter, bands );
            break;
        case SHORT:
        case USHORT:
            applyShortFilter( output, oldData, axisFilter, bands );
            break;
        default:
            break;
        }

        return result;
    }

    /**
     * @param data
     * @param oldData
     * @param axisFilter
     * @param bands
     */
    private void applyShortFilter( RasterData data, RasterData oldData, List<AxisSubset> axisFilter,
                                   Map<BandType, AxisSubset> bands ) {
        short[] result = new short[data.getBands()];
        byte[] nullPixel = data.getNullPixel( null );
        ByteBuffer bb = ByteBuffer.wrap( nullPixel );
        short[] nullVals = new short[result.length];
        for ( int i = 0; i < nullVals.length; ++i ) {
            nullVals[i] = bb.getShort();
        }

        BandType[] dataBands = data.getDataInfo().getBandInfo();
        Map<BandType, Integer> bandsWithConstraints = mapBandsToConstraints( dataBands, bands );

        for ( int y = 0; y < oldData.getRows(); ++y ) {
            for ( int x = 0; x < oldData.getColumns(); ++x ) {
                oldData.getShortPixel( x, y, result );
                // apply band filters
                if ( !bandsWithConstraints.isEmpty() ) {
                    for ( BandType b : bandsWithConstraints.keySet() ) {
                        int bnr = bandsWithConstraints.get( b );
                        short value = result[bnr];
                        if ( !isValid( value, bands.get( b ) ) ) {
                            result[bnr] = nullVals[bnr];
                        }
                    }
                }
                data.setShortPixel( x, y, result );
            }
        }
    }

    /**
     * @param data
     * @param oldData
     * @param axisFilter
     * @param bands
     */
    private void applyIntFilter( RasterData data, RasterData oldData, List<AxisSubset> axisFilter,
                                 Map<BandType, AxisSubset> bands ) {
        int[] result = new int[data.getBands()];
        byte[] nullPixel = data.getNullPixel( null );
        ByteBuffer bb = ByteBuffer.wrap( nullPixel );
        int[] nullVals = new int[result.length];
        for ( int i = 0; i < nullVals.length; ++i ) {
            nullVals[i] = bb.getInt();
        }

        BandType[] dataBands = data.getDataInfo().getBandInfo();
        Map<BandType, Integer> bandsWithConstraints = mapBandsToConstraints( dataBands, bands );

        for ( int y = 0; y < oldData.getRows(); ++y ) {
            for ( int x = 0; x < oldData.getColumns(); ++x ) {
                oldData.getIntPixel( x, y, result );
                // apply band filters
                if ( !bandsWithConstraints.isEmpty() ) {
                    for ( BandType b : bandsWithConstraints.keySet() ) {
                        int bnr = bandsWithConstraints.get( b );
                        int value = result[bnr];
                        if ( !isValid( value, bands.get( b ) ) ) {
                            result[bnr] = nullVals[bnr];
                        }
                    }
                }
                data.setIntPixel( x, y, result );
            }
        }
    }

    /**
     * @param data
     * @param oldData
     * @param axisFilter
     * @param bands
     */
    private void applyFloatFilter( RasterData data, RasterData oldData, List<AxisSubset> axisFilter,
                                   Map<BandType, AxisSubset> bands ) {
        float[] result = new float[data.getBands()];
        byte[] nullPixel = data.getNullPixel( null );
        ByteBuffer bb = ByteBuffer.wrap( nullPixel );
        float[] nullVals = new float[result.length];
        for ( int i = 0; i < nullVals.length; ++i ) {
            nullVals[i] = bb.getFloat();
        }

        BandType[] dataBands = data.getDataInfo().getBandInfo();
        Map<BandType, Integer> bandsWithConstraints = mapBandsToConstraints( dataBands, bands );

        for ( int y = 0; y < oldData.getRows(); ++y ) {
            for ( int x = 0; x < oldData.getColumns(); ++x ) {
                oldData.getFloatPixel( x, y, result );
                // apply band filters
                if ( !bandsWithConstraints.isEmpty() ) {
                    for ( BandType b : bandsWithConstraints.keySet() ) {
                        int bnr = bandsWithConstraints.get( b );
                        float value = result[bnr];
                        if ( !isValid( value, bands.get( b ) ) ) {
                            result[bnr] = nullVals[bnr];
                        }
                    }
                }
                data.setFloatPixel( x, y, result );
            }
        }
    }

    /**
     * @param data
     * @param oldData
     * @param axisFilter
     * @param bands
     */
    private void applyDoubleFilter( RasterData data, RasterData oldData, List<AxisSubset> axisFilter,
                                    Map<BandType, AxisSubset> bands ) {
        double[] result = new double[data.getBands()];
        byte[] nullPixel = data.getNullPixel( null );
        ByteBuffer bb = ByteBuffer.wrap( nullPixel );
        double[] nullVals = new double[result.length];
        for ( int i = 0; i < nullVals.length; ++i ) {
            nullVals[i] = bb.getFloat();
        }

        BandType[] dataBands = data.getDataInfo().getBandInfo();
        Map<BandType, Integer> bandsWithConstraints = mapBandsToConstraints( dataBands, bands );

        for ( int y = 0; y < oldData.getRows(); ++y ) {
            for ( int x = 0; x < oldData.getColumns(); ++x ) {
                oldData.getDoublePixel( x, y, result );
                // apply band filters
                if ( !bandsWithConstraints.isEmpty() ) {
                    for ( BandType b : bandsWithConstraints.keySet() ) {
                        int bnr = bandsWithConstraints.get( b );
                        double value = result[bnr];
                        if ( !isValid( value, bands.get( b ) ) ) {
                            result[bnr] = nullVals[bnr];
                        }
                    }
                }
                data.setDoublePixel( x, y, result );
            }
        }

    }

    /**
     * @param data
     * @param oldData
     * @param axisFilter
     * @param bands
     */
    private void applyByteFilter( RasterData data, RasterData oldData, List<AxisSubset> axisFilter,
                                  Map<BandType, AxisSubset> bands ) {
        byte[] result = new byte[data.getBands()];
        byte[] nullVal = data.getNullPixel( null );
        BandType[] dataBands = data.getDataInfo().getBandInfo();
        Map<BandType, Integer> bandsWithConstraints = mapBandsToConstraints( dataBands, bands );

        for ( int y = 0; y < oldData.getRows(); ++y ) {
            for ( int x = 0; x < oldData.getColumns(); ++x ) {
                oldData.getBytePixel( x, y, result );
                // apply band filters
                if ( !bandsWithConstraints.isEmpty() ) {
                    for ( BandType b : bandsWithConstraints.keySet() ) {
                        int bnr = bandsWithConstraints.get( b );
                        short value = (short) ( result[bnr] & 0xFF );
                        if ( !isValid( value, bands.get( b ) ) ) {
                            result[bnr] = nullVal[bnr];
                        }
                    }
                }
                data.setBytePixel( x, y, result );
            }
        }

    }

    /**
     * @param axisSubset
     */
    private <T extends Comparable<T>> boolean isValid( T value, AxisSubset axisSubset ) {
        List<Interval<?, ?>> intervals = axisSubset.getIntervals();
        List<SingleValue<?>> singleValues = axisSubset.getSingleValues();
        boolean isValid = false;
        if ( intervals != null && !intervals.isEmpty() ) {
            Iterator<Interval<?, ?>> it = intervals.iterator();
            while ( it.hasNext() && !isValid ) {
                try {
                    isValid = ( (Interval<T, ?>) it.next() ).liesWithin( value );
                } catch ( ClassCastException cc ) {
                    // mmm, this should never happen, but just to make sure.
                }
            }
        }
        if ( !isValid && singleValues != null && !singleValues.isEmpty() ) {
            Iterator<SingleValue<?>> it = singleValues.iterator();
            while ( it.hasNext() && !isValid ) {
                try {
                    isValid = ( (SingleValue<T>) it.next() ).value.equals( value );
                } catch ( ClassCastException cc ) {
                    // mmm, this should never happen, but just to make sure.
                }
            }
        }
        return isValid;

    }

    /**
     * simple raster caster
     * 
     * @return the cast
     */
    private AbstractRaster raster() {
        return ( (AbstractRaster) coverage );
    }

    /**
     * @param sourceRangeSet
     * @param targetRangeset
     * @return
     */
    private Map<BandType, AxisSubset> getReferencedBands( List<AxisSubset> requestedAxis ) {
        List<AxisSubset> copyRA = new ArrayList<AxisSubset>( requestedAxis );
        Map<BandType, AxisSubset> bands = new HashMap<BandType, AxisSubset>( requestedAxis.size() );
        for ( AxisSubset ras : requestedAxis ) {
            String name = ras.getName();
            if ( ras.getName() != null && !"".equals( ras.getName() ) ) {
                BandType refBand = BandType.fromString( name );
                if ( refBand != null ) {
                    bands.put( refBand, ras );
                    copyRA.remove( ras );
                }
            }
        }
        if ( !bands.isEmpty() ) {
            requestedAxis.removeAll( copyRA );
            return bands;
        }
        return null;
    }

    private boolean rangeSetsAreApplicable( RangeSet configuredRangeSet, RangeSet requestedRangeSet ) {
        List<AxisSubset> reqAxis = requestedRangeSet.getAxisDescriptions();
        List<AxisSubset> checked = new ArrayList<AxisSubset>( reqAxis );
        for ( AxisSubset ras : reqAxis ) {
            if ( ras.getName() != null ) {
                boolean hasMatch = false;
                Iterator<AxisSubset> it = configuredRangeSet.getAxisDescriptions().iterator();

                while ( it.hasNext() && !hasMatch ) {
                    AxisSubset as = it.next();
                    if ( as.getName().equalsIgnoreCase( ras.getName() ) ) {
                        if ( ras.match( as, true ) ) {
                            // delete the one in checked
                            checked.remove( ras );
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        return checked.isEmpty();
    }

    private Map<BandType, Integer> mapBandsToConstraints( BandType[] dataBands, Map<BandType, AxisSubset> requestedBands ) {
        Map<BandType, Integer> bandsWithConstraints = new HashMap<BandType, Integer>( dataBands.length );
        for ( int b = 0; b < dataBands.length; ++b ) {
            AxisSubset axisSubset = requestedBands.get( dataBands[b] );
            if ( axisSubset != null ) {
                if ( axisSubset.hasAxisConstraints() ) {
                    bandsWithConstraints.put( dataBands[b], b );
                }
            }
        }
        return bandsWithConstraints;
    }

}

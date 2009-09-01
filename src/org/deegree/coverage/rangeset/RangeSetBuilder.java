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

package org.deegree.coverage.rangeset;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.BandType;
import org.deegree.coverage.raster.data.DataType;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;

/**
 * The <code>RangeSetBuilder</code> class supplies methods for building rangeset definitions for coverages.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class RangeSetBuilder {

    /**
     * creates a Rangeset for a given raster by looking at the band info.
     * 
     * @param name
     *            of the rangeset
     * @param label
     *            of the rangeset
     * 
     * @param raster
     * @return a Rangeset describing the bands of the given raster.
     */
    public static RangeSet createBandRangeSetFromRaster( String name, String label, AbstractRaster raster ) {
        Envelope env = raster.getEnvelope();
        double[] min = env.getMin().getAsArray();
        double[] max = new double[min.length];

        for ( int i = 0; i < min.length; ++i ) {
            max[i] = min[i] + 0.01;
        }

        Envelope tEnv = new GeometryFactory().createEnvelope( min, max, env.getCoordinateSystem() );

        AbstractRaster subRaster = raster.getSubRaster( tEnv );
        SimpleRaster simpleRaster = subRaster.getAsSimpleRaster();
        BandType[] bandTypes = simpleRaster.getBandTypes();
        List<AxisSubset> axisDesriptions = createAxisDescriptions( bandTypes,
                                                                   simpleRaster.getRasterData().getDataType() );

        SingleValue<String> nullValue = createNullValue( simpleRaster );
        if ( name == null ) {
            name = raster.getName() == null ? "unknown" : raster.getName();
        }
        if ( label == null ) {
            label = raster.getLabel() == null ? name : raster.getLabel();
        }

        return new RangeSet( name, label, axisDesriptions, nullValue );
    }

    /**
     * @param simpleRaster
     * @return
     */
    private static SingleValue<String> createNullValue( SimpleRaster simpleRaster ) {
        RasterData rasterData = simpleRaster.getRasterData();
        DataType dataType = rasterData.getDataType();
        byte[] noData = rasterData.getNullPixel( null );
        int size = dataType.getSize();
        int bands = rasterData.getBands();
        StringBuilder sb = new StringBuilder();

        for ( int i = 0; i < bands; ++i ) {
            byte[] oneBand = new byte[size];
            System.arraycopy( noData, i * size, oneBand, 0, oneBand.length );
            ByteBuffer buffer = ByteBuffer.wrap( oneBand );
            switch ( dataType ) {
            case BYTE:
                sb.append( buffer.get() );
                break;
            case SHORT:
            case USHORT:
                sb.append( Short.valueOf( buffer.getShort() ) );
                break;
            case INT:
                sb.append( Integer.valueOf( buffer.getInt() ) );
                break;
            case FLOAT:
                sb.append( Float.valueOf( buffer.getFloat() ) );
                break;
            case DOUBLE:
                sb.append( Double.valueOf( buffer.getDouble() ) );
                break;
            case UNDEFINED:
                sb.append( new String( oneBand ) );
                break;
            }
            if ( i + 1 < bands ) {
                sb.append( "," );
            }
        }
        return new SingleValue<String>( ValueType.String, sb.toString() );
    }

    /**
     * @param bandTypes
     * @return
     */
    private static List<AxisSubset> createAxisDescriptions( BandType[] bandTypes, DataType dataType ) {
        List<AxisSubset> axis = new ArrayList<AxisSubset>( bandTypes.length );
        for ( int i = 0; i < bandTypes.length; ++i ) {
            BandType b = bandTypes[i];
            if ( b != null ) {
                String label = b.getInfo();
                String name = b.toString();
                Interval<?, String> interval = null;
                String semantic = "Min and max values were generated automatically.";
                switch ( dataType ) {
                case BYTE:
                    interval = new Interval<Byte, String>( new SingleValue<Byte>( ValueType.Byte, Byte.MIN_VALUE ),
                                                           new SingleValue<Byte>( ValueType.Byte, Byte.MAX_VALUE ),
                                                           Interval.Closure.open, semantic, false, null );
                    break;
                case SHORT:
                case USHORT:
                    interval = new Interval<Short, String>( new SingleValue<Short>( ValueType.Short, Short.MIN_VALUE ),
                                                            new SingleValue<Short>( ValueType.Short, Short.MAX_VALUE ),
                                                            Interval.Closure.open, semantic, false, null );
                    break;
                case INT:
                    interval = new Interval<Integer, String>( new SingleValue<Integer>( ValueType.Integer,
                                                                                        Integer.MIN_VALUE ),
                                                              new SingleValue<Integer>( ValueType.Integer,
                                                                                        Integer.MAX_VALUE ),
                                                              Interval.Closure.open, semantic, false, null );
                    break;
                case FLOAT:
                    interval = new Interval<Float, String>( new SingleValue<Float>( ValueType.Float, Float.MIN_VALUE ),
                                                            new SingleValue<Float>( ValueType.Float, Float.MAX_VALUE ),
                                                            Interval.Closure.open, semantic, false, null );
                    break;
                case DOUBLE:
                    interval = new Interval<Double, String>( new SingleValue<Double>( ValueType.Double,
                                                                                      Double.MIN_VALUE ),
                                                             new SingleValue<Double>( ValueType.Double,
                                                                                      Double.MAX_VALUE ),
                                                             Interval.Closure.open, semantic, false, null );
                    break;
                case UNDEFINED:
                    interval = new Interval<String, String>( new SingleValue<String>( ValueType.String, "min" ),
                                                             new SingleValue<String>( ValueType.String, "max" ),
                                                             Interval.Closure.open, semantic, false, null );
                    break;
                }
                ArrayList<Interval<?, ?>> val = new ArrayList<Interval<?, ?>>( 1 );
                val.add( interval );
                axis.add( new AxisSubset( name, label, val, null ) );
            }
        }
        return axis;
    }
}

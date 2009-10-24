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

package org.deegree.rendering.r2d.styling;

import static org.deegree.rendering.r2d.styling.RasterStyling.Overlap.RANDOM;

import java.util.HashMap;

import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.RasterDataFactory;
import org.deegree.coverage.raster.data.info.DataType;
import org.deegree.filter.function.Categorize;
import org.deegree.filter.function.Interpolate;
import org.deegree.rendering.r2d.se.unevaluated.Symbolizer;
import org.deegree.rendering.r2d.utils.Raster2RawData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <code>RasterStyling</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author <a href="mailto:andrei6200@gmail.com">Andrei Aiordachioaie</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RasterStyling implements Copyable<RasterStyling>, Styling {

    /** Default is 1. */
    public double opacity = 1;

    /***/
    public String redChannel;

    /***/
    public String greenChannel;

    /***/
    public String blueChannel;

    /***/
    public String grayChannel;

    /***/
    public HashMap<String, ContrastEnhancement> channelContrastEnhancements = new HashMap<String, ContrastEnhancement>();

    /** Default is RANDOM. */
    public Overlap overlap = RANDOM;

    /***/
    public Categorize categorize;

    /***/
    public Interpolate interpolate;

    /** Default is no contrast enhancement. */
    public ContrastEnhancement contrastEnhancement;

    /** Default is no shaded relief. */
    public ShadedRelief shaded;

    /** Default is no image outline (should be line or polygon parameterized). */
    public Symbolizer<?> imageOutline;

    /**
     * <code>ShadedRelief</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author <a href="mailto:andrei6200@gmail.com">Andrei Aiordachioaie</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public static class ShadedRelief implements Copyable<ShadedRelief> {

        static Logger LOG = LoggerFactory.getLogger( ShadedRelief.class );

        /** Default is false. */
        public boolean brightnessOnly;

        /** Default is 55. */
        public double reliefFactor = 55;

        /** Azimuth angle = Illumination direction. Default is Nord-West. */
        private final int azimuthAngle = 315;

        /** Angle of illumination source. Default is 45 degrees. */
        private final double Alt = 45;

        public ShadedRelief copy() {
            ShadedRelief copy = new ShadedRelief();

            copy.brightnessOnly = brightnessOnly;
            copy.reliefFactor = reliefFactor;

            return copy;
        }

        /**
         * Perform the hill-shading algorithm on a DEM raster. Based on algorithm presented at
         * http://edndoc.esri.com/arcobjects/9.2/net/shared/geoprocessing/spatial_analyst_tools/how_hillshade_works.htm
         * 
         * @param raster
         *            Input raster, containing a DEM, with R rows and C columns
         * @return a gray-scale raster (with bytes), with R-2 rows and C-2 columns
         */
        public AbstractRaster performHillShading( AbstractRaster raster ) {
            LOG.trace( "Performing Hill-Shading ... " );
            long start = System.nanoTime();
            int cols = raster.getColumns(), rows = raster.getRows();
            Raster2RawData data = new Raster2RawData( raster );
            RasterData shadeData = RasterDataFactory.createRasterData( cols-2, rows-2, DataType.BYTE );
            SimpleRaster hillShade = new SimpleRaster( shadeData, raster.getEnvelope(), raster.getRasterReference() );

            final double Zenith_rad = Math.toRadians( 90 - Alt );
            final double Azimuth_rad = Math.toRadians( 90 - azimuthAngle );
            final double sinZenith = Math.sin( Zenith_rad );
            final double cosZenith = Math.cos( Zenith_rad );
            double Slope_rad;
            double Aspect_rad = 0;
            byte shade = 0;
            double dx, dy;
            float m[][] = new float[3][3];

            for ( int row = 1; row < rows-1; row++ ) {
                for ( int col = 1; col < cols-1; col++ ) {
                    m[0][0] = data.get( col - 1, row - 1 );
                    m[0][1] = data.get( col, row - 1 );
                    m[0][2] = data.get( col + 1, row - 1 );
                    m[1][0] = data.get( col - 1, row );
                    m[1][1] = data.get( col, row );
                    m[1][2] = data.get( col + 1, row );
                    m[2][0] = data.get( col - 1, row + 1 );
                    m[2][1] = data.get( col, row + 1 );
                    m[2][2] = data.get( col + 1, row + 1 );

                    dx = ( ( m[0][2] + 2 * m[1][2] + m[2][2] ) - ( m[0][0] + 2 * m[1][0] + m[2][0] ) ) / 8;
                    dy = ( ( m[2][0] + 2 * m[2][1] + m[2][2] ) - ( m[0][0] + 2 * m[0][1] + m[0][2] ) ) / 8;
                    Slope_rad = Math.atan( reliefFactor * Math.sqrt( dx * dx + dy * dy ) );
                    if ( dx != 0 ) {
                        Aspect_rad = Math.atan2( dy, -dx );
                        if ( Aspect_rad < 0 )
                            Aspect_rad += Math.PI * 2;
                    }
                    if ( dx == 0 ) {
                        if ( dy > 0 )
                            Aspect_rad = Math.PI / 2;
                        else if ( dy < 0 )
                            Aspect_rad = 2 * Math.PI - Math.PI / 2;
                        else
                            Aspect_rad = 0;
                    }

                    long val = Math.round( 255.0 * ( ( cosZenith * Math.cos( Slope_rad ) ) + 
                          ( sinZenith * Math.sin( Slope_rad ) * Math.cos( Azimuth_rad - Aspect_rad ) ) ) );
                    if ( val < 0 )
                        val = 0;
                    shade = (byte) val;

                    shadeData.setByteSample( col-1, row-1, 0, shade );
                }
            }
            long end = System.nanoTime();

            LOG.trace( "Performed Hill shading in {} ms ", ( end - start ) / 1000000 );
            return hillShade;
        }

        public String toString() {
            return "ShadedRelief: { BrightnessOnly: " + brightnessOnly + ", ReliefFactor: " + reliefFactor + "}";
        }
    }

    /**
     * <code>ContrastEnhancement</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author <a href="mailto:andrei6200@gmail.com">Andrei Aiordachioaie</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public static class ContrastEnhancement implements Copyable<ContrastEnhancement> {
        /***/
        public boolean normalize;

        /***/
        public boolean histogram;

        /** Default is 1 == no gamma correction. */
        public double gamma = 1;

        public ContrastEnhancement copy() {
            ContrastEnhancement copy = new ContrastEnhancement();

            copy.normalize = normalize;
            copy.histogram = histogram;
            copy.gamma = gamma;

            return copy;
        }
    }

    /**
     * <code>Overlap</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author <a href="mailto:andrei6200@gmail.com">Andrei Aiordachioaie</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public static enum Overlap {
        /** */
        LATEST_ON_TOP,
        /** */
        EARLIEST_ON_TOP,
        /** */
        AVERAGE,
        /** */
        RANDOM
    }

    public RasterStyling copy() {
        RasterStyling copy = new RasterStyling();

        copy.opacity = opacity;
        copy.redChannel = redChannel;
        copy.greenChannel = greenChannel;
        copy.blueChannel = blueChannel;
        if ( channelContrastEnhancements != null ) {
            copy.channelContrastEnhancements = new HashMap<String, ContrastEnhancement>();
            for ( String chan : channelContrastEnhancements.keySet() ) {
                copy.channelContrastEnhancements.put( chan, channelContrastEnhancements.get( chan ).copy() );
            }
        }
        copy.overlap = overlap;
        copy.contrastEnhancement = contrastEnhancement == null ? null : contrastEnhancement.copy();
        copy.shaded = shaded == null ? null : shaded.copy();
        // should be able to share the symbolizers:
        copy.imageOutline = imageOutline;
        // ... and the functions
        copy.categorize = categorize;
        copy.interpolate = interpolate;

        return copy;
    }

}

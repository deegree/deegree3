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


/**
 * <code>RasterStyling</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
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
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public static class ShadedRelief implements Copyable<ShadedRelief> {
        /** Default is false. */
        public boolean brightnessOnly;

        /** Default is 55. */
        public double reliefFactor = 55;
        
        /** Azimuth angle = Illumination direction (Default is Nord-West) */
        private final int azimuthAngle = 315;

        public ShadedRelief copy() {
            ShadedRelief copy = new ShadedRelief();

            copy.brightnessOnly = brightnessOnly;
            copy.reliefFactor = reliefFactor;

            return copy;
        }
        
        /** Perform the hill-shading algorithm on a DEM raster. Algorithm is taken from 
         * http://edndoc.esri.com/arcobjects/9.2/net/shared/geoprocessing/spatial_analyst_tools/how_hillshade_works.htm
         * @param raster Input raster, containing a DEM
         * @return a new raster
         */
        public AbstractRaster performHillShading(AbstractRaster raster)
        {
            int cols = raster.getColumns(), rows = raster.getRows();
            Raster2RawData data = new Raster2RawData( raster );
            RasterData shadeData = RasterDataFactory.createRasterData( cols, rows, DataType.BYTE );
            SimpleRaster hillShade = new SimpleRaster(shadeData, raster.getEnvelope(), raster.getRasterReference());
            
            double Azimuth_math = 360 - azimuthAngle + 90;
            double Azimuth_rad = Azimuth_math * Math.PI / 180.0;
            double Aspect_rad = 0;
            
            for (int row = 0; row < hillShade.getRows(); row++)
                for (int col = 0; col < hillShade.getColumns(); col++)
                {
                    if (row== 0 || col == 0 || row == rows-1 || col == cols - 1)
                    {
                        shadeData.setByteSample( col, row, 0, (byte)0 );
                        continue;
                    }
                    
                    byte shade = 0;
                    double Zenith_rad = (90 - data.get( col, row )) * Math.PI / 180.0;
                    double dx = ((data.get( col+1, row-1 ) + 2*data.get( col+1, row ) + data.get( col+1, row+1 )) - 
                                 (data.get( col-1, row-1 ) + 2*data.get( col-1, row ) + data.get( col-1, row+1 ))) 
                                 / 8;
                    double dy = ((data.get( col-1, row+1 ) + 2 * data.get( col, row+1 ) + data.get( col+1, row+1 )) - 
                                 (data.get( col-1, row-1 ) + 2 * data.get( col, row-1 ) + data.get( col+1, row-1 ))) 
                                 / 8;
                    double Slope_rad = Math.atan( reliefFactor * Math.sqrt( dx*dx + dy*dy) );
                    if (dx != 0)
                    {
                        Aspect_rad = Math.atan2( dy, -dx );
                        if (Aspect_rad < 0)
                            Aspect_rad += 2 * Math.PI;
                    }
                    else
                    {
                        if (dy > 0)
                            Aspect_rad = Math.PI / 2;
                        else if (dy < 0)
                            Aspect_rad = 2 * Math.PI - Math.PI / 2;
                    }
                    
                    shade = (byte) Math.round(255.0 * ( ( Math.cos(Zenith_rad) * Math.cos(Slope_rad) ) + 
                        ( Math.sin(Zenith_rad) * Math.sin(Slope_rad) * Math.cos(Azimuth_rad - Aspect_rad) ) ));
                    if (shade < 0)
                        shade = 0;
                    shadeData.setByteSample( col, row, 0, shade );
                }
            return hillShade;
        }
        
        public String toString()
        {
            return "ShadedRelief: { BrightnessOnly: " + brightnessOnly + ", ReliefFactor: " + reliefFactor + "}";   
        }
    }

    /**
     * <code>ContrastEnhancement</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
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

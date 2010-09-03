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

import java.util.HashMap;

import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.rendering.r2d.RasterRenderingException;
import org.deegree.rendering.r2d.styling.RasterStyling.ContrastEnhancement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * <code>ChannelSelection</code>
 * 
 * TODO: move the evaluation code somewhere else
 * 
 * @author <a href="mailto:a.aiordachioaie@jacobs-university.de">Andrei Aiordachioaie</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class RasterChannelSelection implements Copyable<RasterChannelSelection> {

    private Logger LOG = LoggerFactory.getLogger( RasterChannelSelection.class );

    /** Output channel names. */
    private String redChannel, greenChannel, blueChannel, grayChannel;

    /** Contrast Enhancements for all channels. */
    public HashMap<String, ContrastEnhancement> channelContrastEnhancements = new HashMap<String, ContrastEnhancement>();

    /**
     * 
     * <code>ChannelSelectionMode</code>
     * 
     * @author <a href="mailto:a.aiordachioaie@jacobs-university.de">Andrei Aiordachioaie</a>
     * @author last edited by: $Author: aaiordachioaie$
     * 
     * @version $Revision$, $Date$
     */
    public static enum ChannelSelectionMode {
        /***/
        RGB, /***/
        GRAY, /***/
        INVALID, /***/
        NONE
    }

    /**
     * @param redChannel
     * @param greenChannel
     * @param blueChannel
     * @param grayChannel
     * @param enhancements
     */
    public RasterChannelSelection( String redChannel, String greenChannel, String blueChannel, String grayChannel,
                                   HashMap<String, ContrastEnhancement> enhancements ) {
        this.redChannel = redChannel;
        this.greenChannel = greenChannel;
        this.blueChannel = blueChannel;
        this.grayChannel = grayChannel;
        this.channelContrastEnhancements = enhancements;
    }

    public RasterChannelSelection copy() {
        RasterChannelSelection copy = new RasterChannelSelection( redChannel, greenChannel, blueChannel, grayChannel,
                                                                  channelContrastEnhancements );
        return copy;
    }

    /**
     * Compute the indexes of selected channel for a particular raster (given its channels)
     * 
     * @param bands
     *            array of information about each band
     * @return index information for all bands
     */
    public int[] evaluate( BandType[] bands ) {
        return new int[] { findChannelIndex( redChannel, bands ), findChannelIndex( greenChannel, bands ),
                          findChannelIndex( blueChannel, bands ), findChannelIndex( grayChannel, bands ) };
    }

    /**
     * Search the index of a channel in the list of bands.
     * 
     * @param cName
     *            Channel name or index, as string
     * @param bands
     *            array of band information for the current raster
     * @return index of the channel
     * @throws RasterRenderingException
     *             if the channel is not found
     */
    private int findChannelIndex( String cName, BandType[] bands )
                            throws RasterRenderingException {
        int i = -1;
        if ( cName == null ) {
            return -1;
        }
        try {
            i = Integer.parseInt( cName ) - 1;
            if ( i < 0 || i >= bands.length ) {
                LOG.error( "Cannot evaluate band '{}', raster data has only {} bands", i, bands.length );
                throw new RasterRenderingException( "Cannot evaluate band " + i + ", raster data has only "
                                                    + bands.length + " bands. " );
            }
            return i;
        } catch ( NumberFormatException e ) {
            for ( i = 0; i < bands.length; i++ ) {
                if ( bands[i].name().equals( cName ) ) {
                    return i;
                }
            }
        }

        LOG.error( "Could not evaluate band with name '{}'", cName );
        throw new RasterRenderingException( "Could not evaluate band with name '" + cName + "'" );
    }

    /**
     * @return the mode of selection
     */
    public ChannelSelectionMode getMode() {
        if ( redChannel != null && greenChannel != null && blueChannel != null && grayChannel == null ) {
            return ChannelSelectionMode.RGB;
        }
        if ( grayChannel != null && redChannel == null && greenChannel == null && blueChannel == null ) {
            return ChannelSelectionMode.GRAY;
        }
        if ( redChannel == null && greenChannel == null && blueChannel == null && grayChannel == null ) {
            return ChannelSelectionMode.NONE;
        }
        return ChannelSelectionMode.INVALID;
    }

}

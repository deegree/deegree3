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

package org.deegree.services.wpvs.config;

import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.opengl.rendering.dem.Colormap;
import org.deegree.services.jaxb.wpvs.ColormapDatasetConfig;
import org.deegree.services.jaxb.wpvs.DatasetDefinitions;
import org.slf4j.Logger;

/**
 * Retrieve the data for a Colormap from the configuration.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ColormapDataset extends Dataset<Colormap> {

    private static final Logger LOG = getLogger( ColormapDataset.class );

    private static final float[] MIN_DEFAULT = new float[] { 1, 0, 0, 1 };

    private static final float[] MAX_DEFAULT = new float[] { 0, 1, 0, 1 };

    private static final float[] HEIGHT_DEFAULT = new float[] { 0.25f, 0.11f, 0.09f, 1 };

    @Override
    public Envelope fillFromDatasetDefinitions( Envelope sceneEnvelope, double[] toLocalCRS, XMLAdapter configAdapter,
                                                DatasetDefinitions dsd ) {

        List<ColormapDatasetConfig> colormapDatsets = dsd.getColormapDataset();
        if ( !colormapDatsets.isEmpty() ) {
            sceneEnvelope = initDatasets( colormapDatsets, sceneEnvelope, toLocalCRS, configAdapter );
        } else {
            LOG.info( "No colormap dataset has been configured." );
        }
        return sceneEnvelope;
    }

    private Envelope initDatasets( List<ColormapDatasetConfig> colormapDatsets, Envelope sceneEnvelope,
                                   double[] toLocalCRS, XMLAdapter adapter ) {
        if ( colormapDatsets != null && !colormapDatsets.isEmpty() ) {
            for ( ColormapDatasetConfig dts : colormapDatsets ) {
                if ( dts != null ) {
                    if ( isUnAmbiguous( dts.getTitle() ) ) {
                        LOG.info( "The colormap dataset with name: " + dts.getName() + " and title: " + dts.getTitle()
                                  + " had multiple definitions in your service configuration." );
                    } else {
                        sceneEnvelope = handleColormapDataset( dts, sceneEnvelope, toLocalCRS, adapter );
                    }
                }
            }
        }
        return sceneEnvelope;
    }

    /**
     * @param dts
     * @param sceneEnvelope
     * @param toLocalCRS
     * @param adapter
     * @return
     */
    private Envelope handleColormapDataset( ColormapDatasetConfig dts, Envelope sceneEnvelope, double[] toLocalCRS,
                                            XMLAdapter adapter ) {
        float[] maxColor = parseColor( dts.getMaxColor(), MAX_DEFAULT );
        float[] minColor = parseColor( dts.getMinColor(), MIN_DEFAULT );
        float[] heightColor = parseColor( dts.getHeightISOColor(), HEIGHT_DEFAULT );
        double zMax = dts.getMaxZValue() == null ? sceneEnvelope.getMax().get2() : dts.getMaxZValue();
        double zMin = dts.getMinZValue() == null ? sceneEnvelope.getMin().get2() : dts.getMinZValue();
        Colormap result = new Colormap( (float) zMin, (float) zMax, minColor, maxColor, heightColor );
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Configured colormap: " + dts.getTitle() + " | " + result.toString() );
        }
        double[] min = Arrays.copyOf( sceneEnvelope.getMin().getAsArray(), 3 );
        double[] max = Arrays.copyOf( sceneEnvelope.getMax().getAsArray(), 3 );
        min[0] += toLocalCRS[0];
        min[1] += toLocalCRS[1];
        max[0] += toLocalCRS[0];
        max[1] += toLocalCRS[1];
        super.addConstraint( dts.getTitle(), result, geomFac.createEnvelope( min, max,
                                                                             sceneEnvelope.getCoordinateSystem() ) );
        return sceneEnvelope;
    }

    /**
     * @param maxColor
     * @param minDefault
     * @return
     */
    private float[] parseColor( String configColor, float[] defaultColor ) {
        if ( configColor != null && !"".equals( configColor ) ) {
            try {
                Color decode = Color.decode( configColor );
                if ( decode != null ) {
                    float[] result = decode.getRGBComponents( null );
                    if ( result[3] <= 0.0001 ) {
                        result[3] = 1;
                    }
                }
            } catch ( NumberFormatException e ) {
                LOG.warn( "Invalid color: " + configColor + " using default color: " + Arrays.toString( defaultColor )
                          + ".", e );
            }
        }
        return Arrays.copyOf( defaultColor, 4 );

    }
}

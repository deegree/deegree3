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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.protocol.wms.raster;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.LinkedList;

import org.deegree.commons.utils.Pair;
import org.deegree.coverage.raster.SimpleRaster;
import org.deegree.coverage.raster.data.RasterData;
import org.deegree.coverage.raster.data.info.BandType;
import org.deegree.coverage.raster.data.nio.ByteBufferRasterData;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.coverage.raster.io.RasterReader;
import org.deegree.feature.FeatureCollection;
import org.deegree.feature.types.FeatureType;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r2d.se.unevaluated.Style;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WMSRaster extends SimpleRaster {

    private static final Logger LOG = getLogger( WMSRaster.class );

    /**
     * @param raster
     * @param envelope
     * @param rasterReference
     */
    public WMSRaster( RasterData raster, Envelope envelope, RasterGeoReference rasterReference ) {
        super( raster, envelope, rasterReference );
    }

    /**
     * @param createdRaster
     *            to get the values from.
     */
    public WMSRaster( SimpleRaster createdRaster ) {
        this( createdRaster.getRasterData(), createdRaster.getEnvelope(), createdRaster.getRasterReference() );
    }

    /**
     * Creates a SimpleRaster with same size, DataType and InterleaveType
     * 
     * @param bands
     *            number of bands
     * @return new empty SimpleRaster
     */
    @Override
    public SimpleRaster createCompatibleSimpleRaster( BandType[] bands ) {
        SimpleRaster raster = super.createCompatibleSimpleRaster( bands );
        return new WMSRaster( raster );
    }

    /**
     * Creates a new empty writable SimpleRaster with same size, DataType and InterleaveType.
     * 
     * @return new empty SimpleRaster
     */
    @Override
    public SimpleRaster createCompatibleSimpleRaster() {
        SimpleRaster raster = super.createCompatibleSimpleRaster();
        return new WMSRaster( raster );
    }

    /**
     * Creates a new empty SimpleRaster with same DataType and InterleaveType. Size is determined by the given envelope.
     * 
     * @param rEnv
     *            The raster envelope of the new SimpleRaster.
     * @param env
     *            The boundary of the new SimpleRaster.
     * @return A new empty SimpleRaster.
     */
    @Override
    public SimpleRaster createCompatibleSimpleRaster( RasterGeoReference rEnv, Envelope env ) {
        SimpleRaster raster = super.createCompatibleSimpleRaster( rEnv, env );
        return new WMSRaster( raster );
    }

    @Override
    public SimpleRaster copy() {
        SimpleRaster result = this.createCompatibleSimpleRaster();
        result.setSubRaster( getEnvelope(), this );
        return result;
    }

    /**
     * Do a GetFeature info request on the underlying WMS client.
     * 
     * @param fi
     * @param style
     * @return the information on the underlying wms.
     */
    public Pair<FeatureCollection, LinkedList<String>> getFeatures( GetFeatureInfo fi, Style style ) {
        ByteBufferRasterData rasterData = (ByteBufferRasterData) getRasterData();
        RasterReader reader = rasterData.getReader();
        if ( reader instanceof WMSReader ) {
            // do feature info
        }
        // do a normal lookup
        return null;
    }

    public FeatureType getFeatureType() {
        return null;
    }

}

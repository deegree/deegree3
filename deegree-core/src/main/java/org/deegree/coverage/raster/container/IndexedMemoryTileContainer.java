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

package org.deegree.coverage.raster.container;

import static org.deegree.geometry.utils.GeometryUtils.createEnvelope;

import java.util.List;

import org.deegree.commons.index.QTree;
import org.deegree.coverage.ResolutionInfo;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.data.info.RasterDataInfo;
import org.deegree.coverage.raster.geom.RasterGeoReference;
import org.deegree.geometry.Envelope;

/**
 * The <code>IndexedMemoryTileContainer</code> class uses a quad tree as a spatial index.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class IndexedMemoryTileContainer implements TileContainer {

    private QTree<AbstractRaster> index;

    private Envelope domain;

    private final RasterGeoReference rasterReference;

    private RasterDataInfo rdi;

    private ResolutionInfo resolutionInfo;

    /**
     * Uses a QTree as a spatial index.
     * 
     * @param domain
     * @param rasterReference
     * @param objectsInLeaf
     */
    public IndexedMemoryTileContainer( Envelope domain, RasterGeoReference rasterReference, int objectsInLeaf ) {
        this.rasterReference = rasterReference;
        this.index = new QTree<AbstractRaster>( createEnvelope( domain ), objectsInLeaf );
        this.domain = domain;
    }

    @Override
    public Envelope getEnvelope() {
        return domain;
    }

    @Override
    public RasterGeoReference getRasterReference() {
        return this.rasterReference;
    }

    @Override
    public List<AbstractRaster> getTiles( Envelope env ) {
        return index.query( createEnvelope( env ) );
    }

    /**
     * Adds a new tile to the container.
     * 
     * @param raster
     *            new tile
     */
    public void addTile( AbstractRaster raster ) {
        if ( raster != null ) {
            if ( this.rdi == null ) {
                this.rdi = raster.getRasterDataInfo();
            }
            if ( this.resolutionInfo == null ) {
                this.resolutionInfo = raster.getResolutionInfo();
            }
            index.insert( createEnvelope( raster.getEnvelope() ), raster );
        }
    }

    /**
     * @param rasters
     */
    public void addRasterTiles( List<AbstractRaster> rasters ) {
        for ( AbstractRaster raster : rasters ) {
            addTile( raster );
        }
        // try {
        // FileWriter fw = new FileWriter( new File( "/tmp/out_tree.dot" ) );
        // GraphvizDot.startDiGraph( fw );
        // index.outputAsDot( fw, "", 0, -1 );
        // GraphvizDot.endGraph( fw );
        // fw.close();
        // } catch ( IOException e ) {
        // e.printStackTrace();
        // }

    }

    @Override
    public RasterDataInfo getRasterDataInfo() {
        return rdi;
    }

    @Override
    public ResolutionInfo getResolutionInfo() {
        return this.resolutionInfo;
    }

}

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
package org.deegree.graphics;

import org.deegree.model.coverage.grid.GridCoverage;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.ogcwebservices.wms.operation.GetMap;

/**
 * A <tt>RasterLayer</tt> represent a layer which data are contained within one single <tt>Image</tt>. The image/raster
 * is geo-referenced by a <tt>Envelope</tt> that is linked to it.
 *
 * <p>
 * ------------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @version $Revision$ $Date$
 */

class RasterLayer extends AbstractLayer {

    /**
     * The raster of the layer
     */
    protected GridCoverage raster = null;

    private GetMap request;

    /**
     * Creates a new AbstractLayer object.
     *
     * @param name
     * @param raster
     *
     * @throws Exception
     */
    RasterLayer( String name, GridCoverage raster ) throws Exception {
        super( name, raster.getCoordinateReferenceSystem() );
        setRaster( raster );
    }

    RasterLayer( String name, GridCoverage raster, GetMap request ) throws Exception {
        super( name, raster.getCoordinateReferenceSystem() );
        this.request = request;
        setRaster( raster );
    }

    /**
     * sets the coordinate reference system of the MapView. If a new crs is set all geometries of GeometryFeatures will
     * be transformed to the new coordinate reference system.
     */
    public void setCoordinatesSystem( CoordinateSystem crs )
                            throws Exception {
        // throw new NoSuchMethodError( "not implemented yet" );
    }

    /**
     * returns the image/raster that represents the layers data
     *
     * @return the image/raster that represents the layers data
     */
    public GridCoverage getRaster() {
        return raster;
    }

    GetMap getRequest() {
        return request;
    }

    /**
     * sets the image/raster that represents the layers data
     *
     * @param raster
     *            the image/raster that represents the layers data
     * @throws Exception
     *             if the bbox of the layer could not be calculated
     */
    public void setRaster( GridCoverage raster )
                            throws Exception {
        this.raster = raster;
        this.boundingbox = raster.getEnvelope();
    }

}

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

import org.deegree.graphics.sld.UserStyle;
import org.deegree.model.coverage.grid.GridCoverage;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.wms.operation.GetMap;

/**
 * Factory class for creating <tt>MapView</tt>s, <tt>Layer</tt>s and <tt>Theme</tt>s. RB: Is this class really
 * necessary? it just calls the appropriate constructors.
 *
 * <p>
 * ------------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @version $Revision$ $Date$
 */
public class MapFactory {
    /**
     * creates an empty feature layer with EPSG:4326 as default coordinate reference system. All data that will be added
     * to the layer will be converted to the EPSG:4326 coordinate reference system if no other CRS is set.
     *
     * @param name
     *            of the layer
     * @return the layer with epsg:4326 set as crs
     * @throws Exception
     */
    public static synchronized Layer createFeatureLayer( String name )
                            throws Exception {
        return new FeatureLayer( name );
    }

    /**
     * creates an empty feature layer. All data that will be added to the layer will be converted to the submitted
     * coordinate reference system if no other CRS is set.
     *
     * @param name
     *            of the layer
     * @param crs
     *            of the crs
     * @return the feature layer with given crs and name.
     * @throws Exception
     */
    public static synchronized Layer createFeatureLayer( String name, CoordinateSystem crs )
                            throws Exception {
        return new FeatureLayer( name, crs );
    }

    /**
     * creates a complete feature layer. If the CRS of the geometries contained within the submitted feature collection
     * are not the same as the submitted CRS all geometries will be converted to the submitted CRS.
     *
     * @param name
     *            of the feature layer
     * @param crs
     *            of the crs of the feature layer
     * @param fc
     *            to get the geometries from
     * @return to the Layer created from given paraemters.
     * @throws Exception
     */
    public static synchronized Layer createFeatureLayer( String name, CoordinateSystem crs, FeatureCollection fc )
                            throws Exception {
        return new FeatureLayer( name, crs, fc );
    }

    /**
     * creates a raster layer. The required CRS is contained within the <tt>GC_GridCoverage</tt> object
     *
     * @param name
     *            name of raster layer
     * @param raster
     *            the raster to create the layer from.
     * @return the raster layer.
     * @throws Exception
     */
    public static synchronized Layer createRasterLayer( String name, GridCoverage raster )
                            throws Exception {
        return new RasterLayer( name, raster );
    }

    /**
     * @param name
     * @param raster
     * @param request
     * @return the new layer
     * @throws Exception
     */
    public static synchronized Layer createRasterLayer( String name, GridCoverage raster, GetMap request )
                            throws Exception {
        return new RasterLayer( name, raster, request );
    }

    /**
     * creates a theme with a name, a Layer containing the themes data and an array of styles to be known by the
     * <tt>Theme</tt>
     *
     * @param name
     *            of the theme
     * @param layer
     *            the layer to create the theme from
     * @param styles
     *            the styles to set
     * @return the created theme from given parameters
     */
    public static synchronized Theme createTheme( String name, Layer layer, UserStyle[] styles ) {
        return new Theme( name, layer, styles );
    }

    /**
     * creates a theme with a name, a Layer containing the themes data and an array of styles to be known by the
     * <tt>Theme</tt>
     *
     * @param name
     *            of the theme
     * @param layer
     *            to use
     * @return the created theme from given parameters
     */
    public static synchronized Theme createTheme( String name, Layer layer ) {
        return new Theme( name, layer, new UserStyle[] { null } );
    }

    /**
     * creates a <tt>MapView</tt> with a name, a boundingbox describing the area coverd by the <tt>MapView</tt>, a CRS
     * and n <tt>Theme</tt>s.
     *
     * @param name
     *            of the mapview
     * @param boundingbox
     *            of the mapview
     * @param crs
     *            of the mapview
     * @param themes
     *            of the mapview
     * @param pixelsize
     *            used for rendering
     * @return the mapview created from given parameters
     * @throws Exception
     */
    public static synchronized MapView createMapView( String name, Envelope boundingbox, CoordinateSystem crs,
                                                      Theme[] themes, double pixelsize )
                            throws Exception {
        MapView mv = new MapView( name, boundingbox, crs, pixelsize );
        for ( int i = 0; i < themes.length; i++ ) {
            mv.addTheme( themes[i] );
        }

        return mv;
    }

}

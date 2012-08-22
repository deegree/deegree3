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
package org.deegree.portal.context;

import java.awt.Rectangle;
import java.net.URL;

import org.deegree.datatypes.QualifiedName;
import org.deegree.graphics.sld.StyledLayerDescriptor;
import org.deegree.graphics.transformation.GeoTransform;
import org.deegree.graphics.transformation.WorldToScreenTransform;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.model.spatialschema.Point;
import org.deegree.portal.PortalException;

/**
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class DefaultMapModelAccess implements MapModelAccess {

    private ViewContext vc = null;

    /**
     * @param vc
     */
    public DefaultMapModelAccess( ViewContext vc ) {
        this.vc = vc;
    }

    public ViewContext addCRSToLayer( String name, URL owsAddress, String type, String crsName )
                            throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewContext addFormatToLayer( String name, URL owsAddress, String type, String mimeType )
                            throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewContext addLayer( QualifiedName name, URL owsAddress, String type )
                            throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewContext addLayer( int index, QualifiedName name, URL owsAddress, String type )
                            throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewContext addLayer( int index, Layer layer )
                            throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewContext addLayer( Layer layer )
                            throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewContext addLayers( QualifiedName[] names, URL[] owsAddresses, String type )
                            throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewContext addLayers( Layer[] layers )
                            throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewContext addStyleToLayer( QualifiedName name, URL owsAddress, String type, String styleName )
                            throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewContext assignSLDToLayer( QualifiedName name, URL owsAddress, String type, StyledLayerDescriptor sld )
                            throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewContext destroyGroup( String groupName ) {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewContext groupLayers( int first, int last, String groupName, String parentGroupName )
                            throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewContext groupLayers( String[] layers, URL[] owsAddresses, String[] types, String groupName,
                                    String parentGroupName )
                            throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewContext pan( double directionDegree, double factor )
                            throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * moves the boundingbox of the map to a well known direction. Directions that are known:
     *
     * @see #PAN_NORTH
     * @see #PAN_NORTHEAST
     * @see #PAN_NORTHWEST
     * @see #PAN_SOUTH
     * @see #PAN_SOUTHEAST
     * @see #PAN_SOUTHWEST
     * @see #PAN_WEST
     * @see #PAN_EAST
     * @param direction
     *            direction the map view shall be moved to
     * @param factor
     *            factor measured in % the map view shall be moved
     * @return modified ViewContext
     * @throws PortalException
     *             will be thrown if passed direction is not valid or factor is <= 0
     * @throws ContextException
     */
    public ViewContext pan( int direction, double factor )
                            throws PortalException, ContextException {
        if ( direction < 0 || direction > 7 ) {
            throw new PortalException( "invalid pan direction: " + direction );
        }
        if ( factor <= 0 ) {
            throw new PortalException( "pan factor must be > 0 " );
        }
        factor = factor / 100.0;
        Point[] bbox = vc.getGeneral().getBoundingBox();
        double dx = ( bbox[1].getX() - bbox[0].getX() ) * factor;
        double dy = ( bbox[1].getY() - bbox[0].getY() ) * factor;
        double minx = bbox[0].getX();
        double miny = bbox[0].getY();
        double maxx = bbox[1].getX();
        double maxy = bbox[1].getY();

        if ( direction == MapModelAccess.PAN_WEST || direction == MapModelAccess.PAN_NORTHWEST
             || direction == MapModelAccess.PAN_SOUTHWEST ) {
            minx = minx - dx;
            maxx = maxx - dx;
        } else if ( direction == MapModelAccess.PAN_EAST || direction == MapModelAccess.PAN_NORTHEAST
                    || direction == MapModelAccess.PAN_SOUTHEAST ) {
            minx = minx + dx;
            maxx = maxx + dx;
        }
        if ( direction == MapModelAccess.PAN_SOUTH || direction == MapModelAccess.PAN_SOUTHEAST
             || direction == MapModelAccess.PAN_SOUTHWEST ) {
            miny = miny - dy;
            maxy = maxy - dy;
        } else if ( direction == MapModelAccess.PAN_NORTH || direction == MapModelAccess.PAN_NORTHEAST
                    || direction == MapModelAccess.PAN_NORTHWEST ) {
            miny = miny + dy;
            maxy = maxy + dy;
        }
        Envelope env = GeometryFactory.createEnvelope( minx, miny, maxx, maxy, null );
        return setMapBoundingBox( env );
    }

    /**
     * centers he map described by a ViewContext to the passed point. The behavior of this method is the same as
     *
     * @see #zoom(java.awt.Point, double) with a passed factor == 0;
     *
     * @param point
     *            point (pixel coordinates) of the current map that marks the new center of the map
     * @return modified ViewContext
     * @throws ContextException
     */
    public ViewContext recenterMap( java.awt.Point point )
                            throws ContextException {
        return zoom( point, 0 );
    }

    /**
     * centers he map described by a ViewContext to the passed point. The behavior of this method is the same as
     *
     * @see #zoom(Point, double) with a passed factor == 0;
     *
     * @param point
     *            point (map coordinates) of the current map that marks the new center of the map
     * @return modified ViewContext
     * @throws ContextException
     */
    public ViewContext recenterMap( Point point )
                            throws ContextException {
        return zoom( point, 0 );
    }

    public ViewContext removeCRSFromLayer( String name, URL owsAddress, String type, String crsName ) {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewContext removeFormatFromLayer( String name, URL owsAddress, String type, String mimeType )
                            throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewContext removeLayer( QualifiedName name, URL owsAddress, String type ) {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewContext removeStyleFromLayer( QualifiedName name, URL owsAddress, String type, String styleName ) {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewContext setCurrentMapFormat( String mimeType, URL owsAddress, String type )
                            throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewContext setLayerActiveStyle( QualifiedName name, URL owsAddress, String type, String styleName )
                            throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewContext setLayers( QualifiedName[] names, URL[] owsAddresses, String type )
                            throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewContext setLayers( Layer[] layers )
                            throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }

    public ViewContext setLayersVisible( String name, URL owsAddress, String type, boolean visible )
                            throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * sets the bounding box a map/VieContext
     *
     * @param boundingBox
     *            new boundingbox
     * @return modified ViewContext
     * @throws ContextException
     */
    public ViewContext setMapBoundingBox( Envelope boundingBox )
                            throws ContextException {

        CoordinateSystem crs = vc.getGeneral().getBoundingBox()[0].getCoordinateSystem();
        Point[] points = new Point[2];
        points[0] = GeometryFactory.createPoint( boundingBox.getMin(), crs );
        points[1] = GeometryFactory.createPoint( boundingBox.getMax(), crs );
        vc.getGeneral().setBoundingBox( points );

        return vc;
    }

    public ViewContext setMapCRS( String crs )
                            throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * sets the width and height of a map described by a ViewContext
     *
     * @param width
     *            map width measured in pixel
     * @param height
     *            map height measured in pixel
     * @return modified ViewContext
     * @throws PortalException
     *             will be thrown if width or height < 1 (even a width or height of 1 pixel is not really useful but
     *             formal it is valid);
     */
    public ViewContext setMapSize( int width, int height )
                            throws PortalException {
        if ( width < 1 ) {
            throw new PortalException( "width must be > 0" );
        }
        if ( height < 1 ) {
            throw new PortalException( "height must be > 0" );
        }
        Rectangle window = new Rectangle( 0, 0, width, height );
        vc.getGeneral().setWindow( window );
        return vc;
    }

    public ViewContext swapGroups( QualifiedName name, boolean up )
                            throws PortalException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * swaps two layers in their order. The layer identified by the passed name and OWS can be moved up or down for one
     * position. If a layer can not be moved up or down because it is already the top or bottom most layer the unchanged
     * <code>ViewContext</code> will be returned
     * <p>
     * Notice: if to layers to be swaped are served by differend OWS at least one new image-layer must be created by the
     * client.
     * </p>
     *
     * @param name
     *            name of the layer to be moved up or down in the list
     * @param owsAddress
     *            address of the OWS that serves the layer
     * @param type
     *            OWS type (WCS, WMS or WFS)
     * @param up
     *            true if layer should be moved up otherwise it will be moved down.
     * @return modified ViewContext
     * @throws PortalException
     *             will be thrown if the layer does not exists in a ViewContext.
     */
    public ViewContext swapLayers( QualifiedName name, URL owsAddress, String type, boolean up )
                            throws PortalException {

        Layer layer = vc.getLayerList().getLayer( name.getPrefixedName(), owsAddress.toExternalForm() );
        vc.getLayerList().move( layer, up );
        return vc;
    }

    /**
     * zoom in or out of the map described by a ViewContext and recenters it to the passed point.
     * <ul>
     * <li>factor > 0: zoomout</li>
     * <li>factor < 0: zoomin</li>
     * <li>factor == 0: recenter</li>
     * </ul>
     *
     * @param factor
     *            zoom factor in % of the boundingbox size
     * @param point
     *            point (pixel coordinates) of the current map that marks the new center of the map
     * @return modified ViewContext
     * @throws ContextException
     */
    public ViewContext zoom( java.awt.Point point, double factor )
                            throws ContextException {
        Point[] points = vc.getGeneral().getBoundingBox();
        Rectangle window = vc.getGeneral().getWindow();
        GeoTransform gt = new WorldToScreenTransform( points[0].getX(), points[0].getY(), points[1].getX(),
                                                      points[1].getY(), 0, 0, window.width, window.height );
        double x = gt.getSourceX( point.x );
        double y = gt.getSourceX( point.y );
        Point pt = GeometryFactory.createPoint( x, y, null );
        return zoom( pt, factor );
    }

    /**
     * zoom in or out of the map described by a ViewContext and recenters it to the passed point.
     * <ul>
     * <li>factor > 0: zoomout</li>
     * <li>factor < 0: zoomin</li>
     * <li>factor == 0: recenter</li>
     * </ul>
     *
     * @param factor
     *            zoom factor in % of the boundingbox size
     * @param point
     *            point (map coordinates) of the current map that marks the new center of the map
     * @return modified ViewContext
     * @throws ContextException
     */
    public ViewContext zoom( Point point, double factor )
                            throws ContextException {
        Point[] points = vc.getGeneral().getBoundingBox();
        double w = points[1].getX() - points[0].getX();
        double h = points[1].getY() - points[0].getY();
        factor = 1d + ( factor / 100d );
        w = w * factor;
        h = h * factor;
        double minx = point.getX() - w / 2d;
        double miny = point.getY() - h / 2d;
        double maxx = point.getX() + w / 2d;
        double maxy = point.getY() + h / 2d;
        Envelope env = GeometryFactory.createEnvelope( minx, miny, maxx, maxy, null );
        return setMapBoundingBox( env );
    }

}

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

import java.net.URL;

import org.deegree.datatypes.QualifiedName;
import org.deegree.graphics.sld.StyledLayerDescriptor;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.Point;
import org.deegree.portal.PortalException;

/**
 * <p>
 * This interface defines access to the model (
 *
 * @see org.deegree.portal.context.ViewContext ) a map based on. A concret implementation is
 *      responsible for handling the context. E.g. an implementation may adds additional
 *      capabilities like a history function.
 *      </p>
 *      <p>
 *      Several methods expects beside the name of a layer the address (URL) of the OWS that serves
 *      this layer. This is required because a ViewContext may offeres layers from more than one OWS
 *      and there is no rule that says that a layers name must be unique across several OWS. So the
 *      only way to identify a layer is to use the combination of layer name, service address and
 *      service type (maybe several services are published to the same address; e.g. deegree 2 will
 *      do this).
 *      </p>
 *      <p>
 *      Notice: All changed e.g. removeLayer just will be made to a ViewContext and not to the
 *      service providing a layer.
 *      </p>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author:wanhoff$
 *
 * @version $Revision$ $Date$
 *
 */
public interface MapModelAccess {

    /**
     * The pan north constant.
     */
    int PAN_NORTH = 0;

    /**
     * The pan north east constant.
     */
    int PAN_NORTHEAST = 1;

    /**
     * The pan north west constant.
     */
    int PAN_NORTHWEST = 2;

    /**
     * The pan south constant.
     */
    int PAN_SOUTH = 3;

    /**
     * The pan south east constant.
     */
    int PAN_SOUTHEAST = 4;

    /**
     * The pan south west constant.
     */
    int PAN_SOUTHWEST = 5;

    /**
     * The pan west constant.
     */
    int PAN_WEST = 6;

    /**
     * The pan east constant.
     */
    int PAN_EAST = 7;

    /**
     * sets the layers provided by the passed OWS to a <code>ViewContext</code>. A layer may be a
     * WMS layer, a WFS feature type or a WCS coverage.
     *
     * @param names
     *            names of the layer
     * @param owsAddresses
     *            addresses of the OWS that serves the layer. each passed layername is assigned to
     *            the OWS URL at the same index position.
     * @param type
     *            OWS type (WCS, WMS or WFS)
     * @return modified ViewContext
     * @throws PortalException
     *             will be thrown if of the layers already is registered to a ViewContext or the OWS
     *             does not serves one the layers or <code>names</names>
     *                            and <code>owsAddresses</code> are not of the same size.
     */
    public ViewContext setLayers( QualifiedName[] names, URL[] owsAddresses, String type )
                            throws PortalException;

    /**
     * sets the layers to a <code>ViewContext</code>. A layer may be a WMS layer, a WFS feature
     * type or a WCS coverage.
     *
     * @param layers
     *            layers to add
     * @return modified ViewContext
     * @throws PortalException
     *             will be thrown if of the layers already is registered to a ViewContext.
     */
    public ViewContext setLayers( Layer[] layers )
                            throws PortalException;

    /**
     * adds a layer provided by the passed OWS to the end of the layer list of a
     * <code>ViewContext</code>. A layer may be a WMS layer, a WFS feature type or a WCS
     * coverage.
     *
     * @param name
     *            name of the layer
     * @param owsAddress
     *            address of the OWS that serves the layer
     * @param type
     *            OWS type (WCS, WMS or WFS)
     * @exception PortalException
     *                will be thrown if the layer already is registered to a ViewContext or the OWS
     *                does not serves a layer with this name.
     * @return modified ViewContext
     */
    public ViewContext addLayer( QualifiedName name, URL owsAddress, String type )
                            throws PortalException;

    /**
     * adds a layer to the end of the layer list of a <code>ViewContext</code>. A layer may be a
     * WMS layer, a WFS feature type or a WCS coverage.
     *
     * @param layer
     *            layer to add
     * @throws PortalException
     *             will be thrown if the layer already is registered to a ViewContext.
     * @return modified ViewContext
     */
    public ViewContext addLayer( Layer layer )
                            throws PortalException;

    /**
     * adds a layer provided by the passed OWS to the defined index position of the layer list of a
     * <code>ViewContext</code>. A layer may be a WMS layer, a WFS feature type or a WCS
     * coverage.
     *
     * @param index
     *            index position where to insert the layer
     * @param name
     *            name of the layer
     * @param owsAddress
     *            address of the OWS that serves the layer
     * @param type
     *            OWS type (WCS, WMS or WFS)
     * @throws PortalException
     *             will be thrown if the layer already is registered to a ViewContext or the OWS
     *             does not serves a layer with this name or the index is &lt; 0 or &gt; the number
     *             of layers -1;
     * @return modified ViewContext
     */
    public ViewContext addLayer( int index, QualifiedName name, URL owsAddress, String type )
                            throws PortalException;

    /**
     * adds a layer to the defined index position of the layer list of a <code>ViewContext</code>.
     * A layer may be a WMS layer, a WFS feature type or a WCS coverage.
     *
     * @param index
     *            index position where to insert the layer
     * @param layer
     *            layer to add
     * @return modified ViewContext
     * @throws PortalException
     *             will be thrown if the layer already is registered to a ViewContext or the index
     *             is &lt; 0 or &gt; the number of layers -1;
     */
    public ViewContext addLayer( int index, Layer layer )
                            throws PortalException;

    /**
     * adds a number of layers provided by the passed OWS to the end of the layer list of a
     * <code>ViewContext</code>. A layer may be a WMS layer, a WFS feature type or a WCS
     * coverage.
     *
     * @param names
     *            names of the layer
     * @param owsAddresses
     *            addresses of the OWS's that serves the layer
     * @param type
     *            OWS type (WCS, WMS or WFS)
     * @return modified ViewContext
     * @throws PortalException
     *             will be thrown if of the layers already is registered to a ViewContext or the OWS
     *             does not serves one the layers or the OWS does not serves one the layers or
     *             <code>names</names>
     *                            and <code>owsAddresses</code> are not of the same size.
     */
    public ViewContext addLayers( QualifiedName[] names, URL[] owsAddresses, String type )
                            throws PortalException;

    /**
     * adds a number of layers to the end of the layer list of a <code>ViewContext</code>. A
     * layer may be a WMS layer, a WFS feature type or a WCS coverage.
     *
     * @param layers
     *            layers to add
     * @return modified ViewContext
     * @throws PortalException
     *             will be thrown if of the layers already is registered to a ViewContext.
     */
    public ViewContext addLayers( Layer[] layers )
                            throws PortalException;

    /**
     * removes a named layer served by the passsed OWS from a <code>ViewContext</code>. if a
     * layer with this name does not exist in a context, the unchanged <code>ViewContext</code>
     * will be returned
     *
     * @param name
     *            name of the layer to be removed
     * @param owsAddress
     *            address of the OWS that serves the layer
     * @param type
     *            OWS type (WCS, WMS or WFS)
     * @return modified ViewContext
     */
    public ViewContext removeLayer( QualifiedName name, URL owsAddress, String type );

    /**
     * swaps two layers in their order. The layer identified by the passed name and OWS can be moved
     * up or down for one position. If a layer can not be moved up or down because it is already the
     * top or bottom most layer the unchanged <code>ViewContext</code> will be returned
     * <p>
     * Notice: if to layers to be swaped are served by differend OWS at least one new image-layer
     * must be created by the client.
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
                            throws PortalException;

    /**
     * swaps to groups in their order
     *
     * @see MapModelAccess#swapLayers(QualifiedName, URL, String, boolean)
     *
     * @param name
     *            name of the group to be moved up or down
     * @param up
     *            true if a group should be moved up otherwise it will be moved down.
     * @return modified ViewContext
     * @throws PortalException
     *             will be thrown if a group with the passed name does not exist in a ViewContext
     */
    public ViewContext swapGroups( QualifiedName name, boolean up )
                            throws PortalException;

    /**
     * sets the active style (style to be used for rendering) of a layer
     *
     * @param name
     *            name of the layer a style shall be set to
     * @param owsAddress
     *            address of the OWS that serves this layer
     * @param type
     *            OWS type (WCS, WMS or WFS)
     * @param styleName
     *            name of the style to be assigned
     * @return modified ViewContext
     * @throws PortalException
     *             will be thrown if the layer or style are not known by a ViewContext or the style
     *             is not available for this layer
     */
    public ViewContext setLayerActiveStyle( QualifiedName name, URL owsAddress, String type, String styleName )
                            throws PortalException;

    /**
     * adds a named style to a layer to be available within a context.
     *
     * @param name
     *            name of the layer
     * @param owsAddress
     *            address of the OWS that serves this layer
     * @param type
     *            OWS type (WCS, WMS or WFS)
     * @param styleName
     *            name of the style to be set
     * @return modified ViewContext
     * @throws PortalException
     *             will be thrown if the layer is not known by a ViewContext or the style is not
     *             available for this layer
     */
    public ViewContext addStyleToLayer( QualifiedName name, URL owsAddress, String type, String styleName )
                            throws PortalException;

    /**
     * removes a style from the layer defined in a ViewContext. If a style with passed named is not
     * assigned to a layer the ViewContext will be returend unchanged.
     *
     * @param name
     *            name of the layer
     * @param owsAddress
     *            address of the OWS that serves this layer
     * @param type
     *            OWS type (WCS, WMS or WFS)
     * @param styleName
     *            name of the style to be removed
     * @return modified ViewContext
     */
    public ViewContext removeStyleFromLayer( QualifiedName name, URL owsAddress, String type, String styleName );

    /**
     * assignes a SLD document instead of a style to a layer. Usually this should be a SLD
     * containing one named layer or one user layer and one user style. But it also will be possible
     * to have a SLD with several layers (named layers as well as user layers). But this may cause
     * some irritations so it should be avoided.
     *
     * @param name
     *            name of the layer the SLD should be assigned to
     * @param owsAddress
     *            address of the OWS that serves this layer(s) if the SLD contains named layer(s).
     *            Otherwise the parameter can be <code>null</code>
     * @param type
     *            OWS type (WCS, WMS or WFS)
     * @param sld
     *            SLD that shall be assigned
     * @return modified ViewContext
     * @throws PortalException
     *             will be thrown if the layer is not known by a ViewContext or if the SLD contains
     *             a named layer but owsAddress is passed.
     */
    public ViewContext assignSLDToLayer( QualifiedName name, URL owsAddress, String type, StyledLayerDescriptor sld )
                            throws PortalException;

    /**
     * set a layer to be visible or invisible
     *
     * @param name
     *            name of the layer
     * @param owsAddress
     *            address of the OWS that serves this layer
     * @param type
     *            OWS type (WCS, WMS or WFS)
     * @param visible
     *            <code>true</code> if a layer shall be visible; <code>false</code> if not.
     * @return modified ViewContext
     * @throws PortalException
     *             will be thrown if the layer is not known by a ViewContext.
     *
     */
    public ViewContext setLayersVisible( String name, URL owsAddress, String type, boolean visible )
                            throws PortalException;

    /**
     * sets the CRS to be used by the map created from a ViewContext.
     *
     * @param crs
     *            name of the CRS e.g. EPSG:4326
     * @return modified ViewContext
     * @throws PortalException
     *             will be thrown if the CRS is not known by all layers available through a
     *             ViewContext
     */
    public ViewContext setMapCRS( String crs )
                            throws PortalException;

    /**
     * Adds a CRS to a layer if a ViewContext.
     *
     * @param name
     *            name of the layer
     * @param owsAddress
     *            address of the OWS that serves this layer
     * @param type
     *            OWS type (WCS, WMS or WFS)
     * @param crsName
     *            name of the CRS to be added (e.g. EPSG:4326)
     * @return modified ViewContext
     * @throws PortalException
     *             will be thrown if the layer is not known by a ViewContext or the CRS is not
     *             supported for this layer/OWS
     */
    public ViewContext addCRSToLayer( String name, URL owsAddress, String type, String crsName )
                            throws PortalException;

    /**
     * removes a CRS from the list of availabe CRS of a layer. If the layer or the CRS does not
     * exist in a ViewContext the ViewContext will be returned unchanged.
     *
     * @param name
     *            name of the layer
     * @param owsAddress
     *            address of the OWS that serves this layer
     * @param type
     *            OWS type (WCS, WMS or WFS)
     * @param crsName
     *            name of the CRS to be removed from a layers CRS list
     * @return modified ViewContext
     */
    public ViewContext removeCRSFromLayer( String name, URL owsAddress, String type, String crsName );

    /**
     * sets the bounding box a map/VieContext
     *
     * @param boundingBox
     *            new boundingbox
     * @return modified ViewContext
     * @throws ContextException
     */
    public ViewContext setMapBoundingBox( Envelope boundingBox )
                            throws ContextException;

    /**
     * sets the width and height of a map described by a ViewContext
     *
     * @param width
     *            map width measured in pixel
     * @param height
     *            map height measured in pixel
     * @return modified ViewContext
     * @throws PortalException
     *             will be thrown if width or height < 1 (even a width or height of 1 pixel is not
     *             really useful but formal it is valid);
     */
    public ViewContext setMapSize( int width, int height )
                            throws PortalException;

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
                            throws ContextException;

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
                            throws ContextException;

    /**
     * centers he map described by a ViewContext to the passed point. The behavior of this method is
     * the same as
     *
     * @see #zoom(java.awt.Point, double) with a passed factor == 0;
     *
     * @param point
     *            point (pixel coordinates) of the current map that marks the new center of the map
     * @return modified ViewContext
     * @throws ContextException
     */
    public ViewContext recenterMap( java.awt.Point point )
                            throws ContextException;

    /**
     * centers he map described by a ViewContext to the passed point. The behavior of this method is
     * the same as
     *
     * @see #zoom(Point, double) with a passed factor == 0;
     *
     * @param point
     *            point (map coordinates) of the current map that marks the new center of the map
     * @return modified ViewContext
     * @throws ContextException
     */
    public ViewContext recenterMap( Point point )
                            throws ContextException;

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
                            throws PortalException, ContextException;

    /**
     * moves a map view to a free definable direction with a given factor
     *
     * @param directionDegree
     *            direction measured in degree that map view shall be moved to
     * @param factor
     *            factor measured in % the map view shall be moved
     * @return modified ViewContext
     * @throws PortalException
     *             will be thrown if factor is <= 0
     */
    public ViewContext pan( double directionDegree, double factor )
                            throws PortalException;

    /**
     * sets the current image format a map shall be rendered. In theory OGC WMC specification allows
     * rendering of each layer in a different image format but this seems to be very complicated,
     * slow and not very useful, so deegree just supports one format for each OWS.
     *
     * @param mimeType
     *            image format
     * @param owsAddress
     *            address of the OWS the format shall be used for
     * @param type
     *            OWS type
     * @return modified ViewContext
     * @throws PortalException
     *             will be thrown if the passed mimeType is not suported by the passed OWS or one of
     *             its layers as defined in a ViewContext
     */
    public ViewContext setCurrentMapFormat( String mimeType, URL owsAddress, String type )
                            throws PortalException;

    /**
     * adds a new image format to a layer of a ViewContext. This method is useful because not every
     * image format e.g.a WMS offers must be registered to be available through a Web Map Context.
     *
     * @param name
     *            name of the layer
     * @param owsAddress
     *            address of the OWS
     * @param type
     *            OWS type (WCS, WMS or WFS)
     * @param mimeType
     *            new format to add to the layer
     * @return modified ViewContext
     * @throws PortalException
     *             will be thrown if the passed mimeType is not suported by the passed OWS
     */
    public ViewContext addFormatToLayer( String name, URL owsAddress, String type, String mimeType )
                            throws PortalException;

    /**
     * removes a format from a layer in a ViewContext.
     *
     * @see #addFormatToLayer(String, URL, String, String)
     *
     * @param name
     *            name of the layer
     * @param owsAddress
     *            address of the OWS
     * @param type
     *            OWS type (WCS, WMS or WFS)
     * @param mimeType
     *            format to be removed
     * @return modified ViewContext
     * @throws PortalException
     *             will be thrown if the removed format is the current format of the layer.
     */
    public ViewContext removeFormatFromLayer( String name, URL owsAddress, String type, String mimeType )
                            throws PortalException;

    /**
     * groups a set of layers identified by their names, the address of the OWS that provides it und
     * its type. A group has a name that must be unique and a parent that may is <code>null</code>.
     * If no parent is given the group will be located underneath the root node.
     *
     * @param layers
     *            layers to be grouped.
     * @param owsAddresses
     * @param types
     * @param groupName
     *            unique name of the group
     * @param parentGroupName
     *            name of the parent group. may be <code>null</null>
     * @return modified ViewContext
     * @throws PortalException will be thrown if one of the layers is not served by
     *                         the assigned OWS or a layer is already member of a
     *                         group
     */
    public ViewContext groupLayers( String[] layers, URL[] owsAddresses, String[] types, String groupName,
                                    String parentGroupName )
                            throws PortalException;

    /**
     * the difference to
     *
     * @see #groupLayers(String[], URL[], String[], String, String) is that the layers are
     *      identified by theri index position in a ViewContext
     *
     * @param first
     *            zero based index of the first layer to add to the group
     * @param last
     *            zero based index of the last layer to add to the group
     * @param groupName
     *            unique name of the group
     * @param parentGroupName
     *            name of the parent group. may be <code>null</null>
     * @return modified ViewContext
     * @throws PortalException will be thrown if first or last is < 0 or larger
     *                         than the total number of layers -1 or a layer is
     *                         already member of a group
     */
    public ViewContext groupLayers( int first, int last, String groupName, String parentGroupName )
                            throws PortalException;

    /**
     * destroys a group. If a group with the passed name does not exist a ViewContext will be
     * returned unchanged
     *
     * @param groupName
     *            name of the group to be removed
     * @return modified ViewContext
     */
    public ViewContext destroyGroup( String groupName );

}

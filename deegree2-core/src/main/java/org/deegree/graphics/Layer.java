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

import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Envelope;

/**
 * A Layer is a collection of <tt>Feature</tt>s or rasters building a thematic 'unit' waterways
 * or country borders for example. <tt>Feature</tt>s or raster can be added or removed from the
 * layer. A <tt>Feature</tt> or raster can e changed by a modul of the application using the layer
 * because only references to <tt>Feature</tt>s or rasters are stored within a layer.
 *
 * <p>
 * ------------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @version $Revision$ $Date$
 */

public interface Layer {
    /**
     * returns the name of the layer
     * @return the name of the layer
     */
    String getName();

    /**
     * returns the BoundingBox (Envelope) of Layer. This is the BoundingBox of the layers data. The
     * BoundingBox of the View maybe larger or smaler
     * @return the BoundingBox (Envelope) of Layer
     */
    Envelope getBoundingBox();

    /**
     * returns the coordinate reference system of the MapView
     * @return the coordinate reference system of the MapView
     *
     */
    CoordinateSystem getCoordinatesSystem();

    /**
     * sets the coordinate reference system of the MapView. If a new crs is set all geometries of
     * GeometryFeatures will be transformed to the new coordinate reference system.
     * @param crs to set
     * @throws Exception if something went wrong
     *
     */
    void setCoordinatesSystem( CoordinateSystem crs )
                            throws Exception;

    /**
     * adds an eventcontroller to the MapView that's responsible for handling events that targets the
     * map. E.g.: zooming, panning, selecting a feature etc.
     * @param obj an event controller
     */
    void addEventController( LayerEventController obj );

    /**
     * @param obj the eventcontroller to remove
     * @see Layer#addEventController(LayerEventController)
     */
    void removeEventController( LayerEventController obj );
}

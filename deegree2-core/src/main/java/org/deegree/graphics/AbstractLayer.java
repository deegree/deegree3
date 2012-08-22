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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;

/**
 * A Layer is a collection of <tt>Feature</tt>s or rasters building a thematic 'unit' waterways or country borders for
 * example. <tt>Feature</tt>s or raster can be added or removed from the layer. A <tt>Feature</tt> or raster can e
 * changed by a modul of the application using the layer because only references to <tt>Feature</tt>s or rasters are
 * stored within a layer.
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @version $Revision$ $Date$
 */

abstract class AbstractLayer implements Layer {

    /**
     * Of the layer
     */
    protected CoordinateSystem cs = null;

    /**
     * bbox of the layer
     */
    protected Envelope boundingbox = null;

    /**
     * List of controllers.
     */
    protected List<EventController> eventController = Collections.synchronizedList( new ArrayList<EventController>() );

    private String name = null;

    /**
     * creates a layer with EPSG:4326 as default coordinate system
     * 
     * @param name
     * @throws Exception
     */
    AbstractLayer( String name ) throws Exception {
        this.name = name;
        cs = CRSFactory.create( "EPSG:4326" );

    }

    /**
     * Creates a new AbstractLayer object.
     * 
     * @param name
     * @param crs
     * 
     * @throws Exception
     */
    AbstractLayer( String name, CoordinateSystem crs ) throws Exception {
        this.name = name;

        cs = crs;

        boundingbox = GeometryFactory.createEnvelope( 9E99, 9E99, -9E99, -9E99, null );
    }

    /**
     * returns the name of the layer
     * 
     * @return the name of the layer
     */
    public String getName() {
        return name;
    }

    /**
     * returns the BoundingBox (Envelope) of Layer. This is the BoundingBox of the layers data. The BoundingBox of the
     * View maybe larger or smaller
     * 
     * @return the BoundingBox (Envelope) of Layer
     */
    public Envelope getBoundingBox() {
        return boundingbox;
    }

    /**
     * returns the coordinate reference system of the MapView
     * 
     * @return the coordinate reference system of the MapView
     */
    public CoordinateSystem getCoordinatesSystem() {
        return cs;
    }

    /**
     * adds an eventcontroller to the MapView that's reponsible for handling events that targets the map. E.g.: zooming,
     * panning, selecting a feature etc.
     * 
     * @param obj
     */
    public void addEventController( LayerEventController obj ) {
        eventController.add( obj );
        obj.addLayer( this );
    }

    /**
     * @see org.deegree.graphics.AbstractLayer#addEventController(LayerEventController)
     * @param obj
     */
    public void removeEventController( LayerEventController obj ) {
        eventController.remove( obj );
        obj.removeLayer( this );
    }

    /**
     * As default the method returns <code>true</code>, but it may be overwritten by extending classes
     * 
     * @return <code>true</code> if a layer is valid and should be rendered
     */
    public boolean isValid() {
        return true;
    }
}

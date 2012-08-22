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
package org.deegree.ogcwebservices.wms.capabilities;

import org.deegree.model.spatialschema.EnvelopeImpl;
import org.deegree.model.spatialschema.Position;


/**
 * Layers may have zero or more <BoundingBox> elements that are either stated
 * explicitly or inherited from a parent Layer. Each BoundingBox states the
 * bounding rectangle of the map data in a particular spatial reference system;
 * the attribute SRS indicates which SRS applies. If the data area is shaped
 * irregularly then the BoundingBox gives the minimum enclosing rectangle.The
 * attributes minx, miny, maxx, maxy indicate the edges of the bounding box in
 * units of the specified SRS. Optional resx and resy attributes indicate the
 * spatial resolution of the data in those same units.
 * <p></p>
 * A Layer may have multiple BoundingBox element, but each one shall state a
 * different SRS. A Layer inherits any BoundingBox values defined by its parents.
 * A BoundingBox inherited from the parent Layer for a particular SRS is replaced
 * by any declaration for the same SRS in the child Layer. A BoundingBox in the
 * child for a new SRS not already declared by the parent is added to the list
 * of bounding boxes for the child Layer. A single Layer element shall not
 * contain more than one BoundingBox for the same SRS.
 * <p>----------------------------------------------------------------------</p>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @version 2002-03-01
 */
public class LayerBoundingBox extends EnvelopeImpl {

    private static final long serialVersionUID = 4865010531322434459L;

    private String sRS = null;

    private double resx = 0;

    private double resy = 0;


    /**
    * constructor initializing the class with the <LayerBoundingBox>
     * @param min
     * @param max
     * @param srs
     * @param resx
     * @param resy
    */
    public LayerBoundingBox( Position min, Position max, String srs, double resx, double resy ) {
        super( min, max );
        setSRS( srs );
        setResx( resx );
        setResy( resy );
    }

    /**
     * @return spatial resolution of the layers data in x-direction. If the resolution
     * isn't known <tt>-1</tt> will be returned.
     */
    public double getResx() {
        return resx;
    }

    /**
    * sets spatial resolution of the layers data in x-direction
    * @param resx
    */
    public void setResx( double resx ) {
        this.resx = resx;
    }

    /**
     * @return spatial resolution of the layers data in x-direction. If the resolution
     * isn't known <tt>-1</tt> will be returned.
     */
    public double getResy() {
        return resy;
    }

    /**
    * sets spatial resolution of the layers data in x-direction
    * @param resy
    */
    public void setResy( double resy ) {
        this.resy = resy;
    }

    /**
     * @return the name the spatial reference system of the bounding box
     */
    public String getSRS() {
        return sRS;
    }

    /**
     * sets the name of the spatial reference system of the bounding box
     * @param srs
     */
    public void setSRS( String srs ) {
        sRS = srs;
    }

    @Override
    public String toString() {
        String ret = null;
        ret = "resx = " + resx + "\n";
        ret += ( "resy = " + resy + "\n" );
        ret += ( "sRS = " + sRS + "\n" );
        return ret;
    }

}

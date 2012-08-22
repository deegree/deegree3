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
package org.deegree.ogcwebservices.wcs.describecoverage;

import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.EnvelopeImpl;
import org.deegree.model.spatialschema.Surface;
import org.deegree.model.spatialschema.SurfaceImpl;
import org.deegree.ogcwebservices.wcs.WCSException;

/**
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */

public class SpatialDomain implements Cloneable {

    private Envelope[] envelops = null;

    private Object[] grid = new Object[0];

    private Surface[] surface = new Surface[0];

    /**
     * @param envelops
     */
    public SpatialDomain( Envelope[] envelops ) throws WCSException {
        setEnvelops( envelops );
    }

    /**
     * @param envelops
     * @param surface
     */
    public SpatialDomain( Envelope[] envelops, Surface[] surface ) throws WCSException {
        setEnvelops( envelops );
        setSurface( surface );
    }

    /**
     * @param envelops
     * @param grid
     */
    public SpatialDomain( Envelope[] envelops, Object[] grid ) throws WCSException {
        setEnvelops( envelops );
        setGrid( grid );
    }

    /**
     * @param envelops
     * @param grid
     */
    public SpatialDomain( Envelope[] envelops, Surface[] surface, Object[] grid ) throws WCSException {
        setEnvelops( envelops );
        setGrid( grid );
        setSurface( surface );
    }

    /**
     * @return Returns the envelops.
     *
     */
    public Envelope[] getEnvelops() {
        return envelops;
    }

    /**
     * @param envelops
     *            The envelops to set.
     *
     */
    public void setEnvelops( Envelope[] envelops )
                            throws WCSException {
        if ( envelops == null ) {
            throw new WCSException( "At least one envelop must be defined for " + "a SpatialDomain!" );
        }
        this.envelops = envelops;
    }

    /**
     * @return Returns the grid.
     *
     */
    public Object[] getGrid() {
        return grid;
    }

    /**
     * @param grid
     *            The grid to set.
     *
     */
    public void setGrid( Object[] grid ) {
        if ( grid == null ) {
            grid = new Object[0];
        }
        this.grid = grid;
    }

    /**
     * @return Returns the surface.
     *
     */
    public Surface[] getSurface() {
        return surface;
    }

    /**
     * @param surface
     *            The surface to set.
     *
     */
    public void setSurface( Surface[] surface ) {
        if ( surface == null ) {
            surface = new Surface[0];
        }
        this.surface = surface;
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        try {
            Envelope[] env = new Envelope[envelops.length];
            for ( int i = 0; i < env.length; i++ ) {
                env[i] = (Envelope) ( (EnvelopeImpl) envelops[i] ).clone();
            }

            Surface[] surf = new Surface[surface.length];
            for ( int i = 0; i < surf.length; i++ ) {
                surf[i] = (Surface) ( (SurfaceImpl) surface[i] ).clone();
            }
            return new SpatialDomain( env, surf, grid );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return null;
    }

}

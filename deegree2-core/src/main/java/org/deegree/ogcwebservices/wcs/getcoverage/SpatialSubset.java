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
package org.deegree.ogcwebservices.wcs.getcoverage;

import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.wcs.WCSException;

/**
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * $Revision$, $Date$
 */

public class SpatialSubset {

    private Envelope envelope = null;

    private Object grid = null;

    /**
     * @param envelope
     * @param grid
     * @throws WCSException
     *             if one of the parameters is null
     */
    public SpatialSubset( Envelope envelope, Object grid ) throws WCSException {
        if ( envelope == null ) {
            throw new WCSException( "envelope must be <> null for SpatialSubset" );
        }
        if ( grid == null ) {
            throw new WCSException( "grid must be <> null for SpatialSubset" );
        }
        this.envelope = envelope;
        this.grid = grid;
    }

    /**
     * @return Returns the envelope.
     */
    public Envelope getEnvelope() {
        return envelope;
    }

    /**
     * @return Returns the grid.
     */
    public Object getGrid() {
        return grid;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer( 300 );
        sb.append( "envelope=(" );
        sb.append( envelope );
        sb.append( "), grid=(" );
        sb.append( grid );
        sb.append( ')' );
        return sb.toString();
    }

}

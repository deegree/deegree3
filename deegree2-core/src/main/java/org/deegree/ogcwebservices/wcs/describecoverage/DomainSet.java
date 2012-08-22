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

import org.deegree.datatypes.time.TimeSequence;
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

public class DomainSet implements Cloneable {

    private SpatialDomain spatialDomain = null;

    private TimeSequence timeSequence = null;

    /**
     * @param spatialDomain
     */
    public DomainSet( SpatialDomain spatialDomain ) throws WCSException {
        setSpatialDomain( spatialDomain );
    }

    /**
     * @param timeSequence
     */
    public DomainSet( TimeSequence timeSequence ) throws WCSException {
        setTimeSequence( timeSequence );
    }

    /**
     * @param spatialDomain
     * @param timeSequence
     */
    public DomainSet( SpatialDomain spatialDomain, TimeSequence timeSequence ) throws WCSException {
        this.spatialDomain = spatialDomain;
        this.timeSequence = timeSequence;
        if ( this.spatialDomain == null && this.timeSequence == null ) {
            throw new WCSException( "at least spatialDomain or timeSequence must " + "be <> null " );
        }
    }

    /**
     * @return Returns the spatialDomain.
     */
    public SpatialDomain getSpatialDomain() {
        return spatialDomain;
    }

    /**
     * @param spatialDomain
     *            The spatialDomain to set.
     */
    public void setSpatialDomain( SpatialDomain spatialDomain )
                            throws WCSException {
        if ( spatialDomain == null && this.timeSequence == null ) {
            throw new WCSException( "spatialDomain must be <> null because timeSequence " + "is already null" );
        }
        this.spatialDomain = spatialDomain;
    }

    /**
     * @return Returns the timeSequence.
     */
    public TimeSequence getTimeSequence() {
        return timeSequence;
    }

    /**
     * @param timeSequence
     *            The timeSequence to set.
     */
    public void setTimeSequence( TimeSequence timeSequence )
                            throws WCSException {
        if ( timeSequence == null && this.spatialDomain == null ) {
            throw new WCSException( "timeSequence must be <> null because spatialDomain " + "is already null" );
        }
        this.timeSequence = timeSequence;
    }

    /**
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        SpatialDomain spatialDomain_ = null;
        if ( spatialDomain != null ) {
            spatialDomain_ = (SpatialDomain) spatialDomain.clone();
        }
        TimeSequence timeSequence_ = null;
        if ( timeSequence != null ) {
            timeSequence_ = (TimeSequence) timeSequence.clone();
        }
        try {
            return new DomainSet( spatialDomain_, timeSequence_ );
        } catch ( Exception e ) {
        }
        return null;
    }

}

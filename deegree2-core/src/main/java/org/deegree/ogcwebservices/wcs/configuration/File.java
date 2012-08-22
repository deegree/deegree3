// $HeadURL$
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
package org.deegree.ogcwebservices.wcs.configuration;

import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.spatialschema.Envelope;

/**
 * Describes a coverage (access) available through one file. The name of the <tt>File</tt> may is
 * build from variables indicated by a leadin '$' (e.g.
 * C:/rasterdata/luftbilder/775165/$YEAR/$MONTH/$DAY/$ELEVATION/l0.5) in this case the variable
 * parts of the name can be replaced by an application with concrete values. It is in the
 * responsibility of the application to use valid values for the variables. Known variable names
 * are:
 * <ul>
 * <li>$YEAR
 * <li>$MONTH
 * <li>$DAY
 * <li>$HOUR
 * <li>$MINUTE
 * <li>$ELEVATION
 * </ul>
 *
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */

public class File extends CoverageSource {

    private String name = null;

    private Envelope envelope = null;

    /**
     * @param name
     * @param envelope
     */
    public File( CoordinateSystem crs, String name, Envelope envelope ) {
        super( crs );
        this.name = name;
        this.envelope = envelope;
    }

    /**
     * @return Returns the envelope.
     */
    public Envelope getEnvelope() {
        return envelope;
    }

    /**
     * @param envelope
     *            The envelope to set.
     */
    public void setEnvelope( Envelope envelope ) {
        this.envelope = envelope;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName( String name ) {
        this.name = name;
    }

}

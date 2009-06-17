//$HeadURL: svn+ssh://aionita@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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

package org.deegree.crs;

import java.io.File;
import java.io.IOException;

import org.deegree.crs.configuration.deegree.db.WKTParser;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.exceptions.UnknownCRSException;
import org.deegree.feature.Feature;
import org.deegree.geometry.Geometry;

/**
 * Represents the name to a {@link CRS} that is not necessarily resolved or resolvable.
 * <p>
 * Their are two aspects that this class takes care of:
 * <nl>
 * <li>In applications, coordinate system are usually identified using strings (such as 'EPSG:4326'). However, there are
 * multiple equivalent ways to encode coordinate system identifications (another one would be
 * 'urn:ogc:def:crs:EPSG::4326'). By using this class to represent a CRS, the original spelling is maintained.</li>
 * <li>A coordinate system may be specified which is not known to the {@link CRSRegistry}. However, for some operations
 * this is not a necessarily a problem, e.g. a GML document may be read and transformed into {@link Feature} and
 * {@link Geometry} objects.</li>
 * </nl>
 *
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author: ionita $
 *
 * @version $Revision: $, $Date: $
 */
public class CRS {

    /**
     * The string used to identify the coordinate system.
     */
    private String crsName;

    /**
     * The CoordinateSystem that is identified by the string.
     */
    private CoordinateSystem crs;

    /**
     * Creates a new {@link CRS} instance with a coordinate system name.
     *
     * @param crsName
     *            name of the crs (identification string) or null
     */
    public CRS( String crsName ) {
        this.crsName = crsName;
    }

    /**
     * @param crs
     */
    public CRS( CoordinateSystem crs ) {
        if ( crs != null ) {
            crsName = crs.getName();
            this.crs = crs;
        }
    }

    /**
     * @param prj
     * @throws IOException
     */
    public CRS( File prj ) throws IOException {
        crs = new WKTParser( prj.toString() ).parseCoordinateSystem();
        crsName = crs.getName();
    }

    /**
     * Returns the string that identifies the {@link CRS}.
     *
     * @return the string that identifies the coordinate system
     */
    public String getName() {
        return crsName;
    }

    /**
     * Returns the corresponding {@link CRS} object.
     *
     * @return the coordinate system, or null if the name is null
     * @throws UnknownCRSException
     */
    public CoordinateSystem getWrappedCRS()
                            throws UnknownCRSException {
        if ( crs == null && crsName != null ) {
            crs = CRSRegistry.lookup( crsName );
        }
        return crs;
    }

    /**
     * @param otherCRS
     * @return false
     */
    public boolean equals2( CRS otherCRS ) {
        return false;
    }

    @Override
    public String toString() {
        return "{name=" + crsName + ", resolved=" + ( crs != null ) + "}";
    }
}

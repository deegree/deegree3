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

package org.deegree.cs;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import org.deegree.cs.configuration.wkt.WKTParser;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.exceptions.UnknownCRSException;

/**
 * Represents the name to a {@link CRS} that is not necessarily resolved or resolvable.
 * <p>
 * Their are two aspects that this class takes care of:
 * <nl>
 * <li>In most use cases, coordinate reference system are identified using strings (such as 'EPSG:4326'). However, there
 * are multiple equivalent ways to encode coordinate reference system identifications (another one would be
 * 'urn:ogc:def:crs:EPSG::4326'). By using this class to represent a CRS, the original spelling is maintained.</li>
 * <li>A coordinate reference system may be specified which is not known to the {@link CRSRegistry}. However, for some
 * operations this is not a necessarily a problem, e.g. a GML document may be read and transformed into
 * {@link org.deegree.feature.Feature} and {@link org.deegree.geometry.Geometry} objects.</li>
 * </nl>
 * 
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author: ionita $
 * 
 * @version $Revision: $, $Date: $
 */
public class CRS implements Serializable {

    private static final long serialVersionUID = -2387578425336244509L;

    /** The commonly used geographic 'EPSG:4326', with axis order X, Y. */
    public static final CRS EPSG_4326 = new CRS( GeographicCRS.WGS84 );

    /**
     * The string used to identify the coordinate reference system.
     */
    private String crsName;

    /**
     * The CoordinateSystem that is identified by the string.
     */
    private transient CoordinateSystem crs;

    /**
     * Flag indicating, if the axis order should be swapped to x/y (EAST/NORTH; WEST/SOUTH) or the defined axis order is
     * used
     */
    private boolean forceXY;

    /**
     * Creates a new {@link CRS} instance with a coordinate reference system name.
     * 
     * @param crsName
     *            name of the crs (identification string) or null
     */
    public CRS( String crsName ) {
        this.crsName = crsName;
    }

    /**
     * Creates a new {@link CRS} instance with a coordinate reference system name.
     * 
     * @param crsName
     *            name of the crs (identification string) or null
     * @param forceXY
     *            true if the axis order of the coordinate system should be x/y (EAST/NORTH; WEST/SOUTH); false id the
     *            defined axis order should be taken
     */
    public CRS( String crsName, boolean forceXY ) {
        this( crsName );
        this.forceXY = forceXY;
    }

    /**
     * Creates a new {@link CRS} instance with the given coordinate system.
     * 
     * @param crs
     */
    public CRS( CoordinateSystem crs ) {
        if ( crs != null ) {
            crsName = crs.getCode().getOriginal();
            this.crs = crs;
        }
    }

    /**
     * Try to read a crs from the given (prj) file which is supposed to define a WKT definition.
     * 
     * @param prj
     *            a file containing a wkt representation of the coordinate system.
     * @throws IOException
     */
    public CRS( File prj ) throws IOException {
        crs = new WKTParser( prj ).parseCoordinateSystem();
        crsName = crs.getName();
    }

    /**
     * Returns the string that identifies the {@link CRS}.
     * 
     * @return the string that identifies the coordinate reference system
     */
    public String getName() {
        return crsName;
    }

    /**
     * Returns the corresponding {@link CRS} object.
     * 
     * @return the coordinate reference system, or null if the name is null
     * @throws UnknownCRSException
     */
    public CoordinateSystem getWrappedCRS()
                            throws UnknownCRSException {
        if ( crs == null && crsName != null ) {
            crs = CRSRegistry.lookup( crsName, forceXY );
        }
        return crs;
    }

    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof CRS ) {
            final CRS that = (CRS) other;
            boolean equalName = this.crsName != null && ( this.crsName.equals( that.crsName ) );
            if ( !equalName ) {
                try {
                    CoordinateSystem thisCRS = this.getWrappedCRS();
                    CoordinateSystem thatCRS = that.getWrappedCRS();
                    return thisCRS.equals( thatCRS );
                } catch ( UnknownCRSException e ) {
                    return false; // because something failed when comparing the CoordinateSystems...
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {

        try {
            if ( getWrappedCRS() != null ) {
                return getWrappedCRS().hashCode();
            }
        } catch ( UnknownCRSException e ) {
            // because something failed while retrieving the CoordinateSystem...
        }
        return crsName.hashCode();
    }

    /**
     * Returns the area of use, i.e. the domain where this {@link CRSIdentifiable} is valid.
     * 
     * @return the domain of validity (EPSG:4326 coordinates), order: minX, minY, maxX, maxY, never <code>null</code>
     *         (-180,-90,180,90) if no such information is available
     * @throws UnknownCRSException
     */
    public double[] getAreaOfUse()
                            throws UnknownCRSException {
        double[] coords = getWrappedCRS().getAreaOfUseBBox();
        return Arrays.copyOf( coords, 4 );
    }

    @Override
    public String toString() {
        return "{name=" + crsName + ", resolved=" + ( crs != null ) + "}";
    }

}

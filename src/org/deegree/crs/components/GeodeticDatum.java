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

package org.deegree.crs.components;

import org.deegree.crs.CRSCodeType;
import org.deegree.crs.CRSIdentifiable;
import org.deegree.crs.transformations.helmert.Helmert;

/**
 * A <code>GeodeticDatum</code> (aka. HorizontalDatum) holds an ellipse and a prime-meridian.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public class GeodeticDatum extends Datum {

    /**
     * The default WGS 1984 datum, with primeMeridian set to Greenwich and default (no) wgs84 conversion info.
     */
    public static final GeodeticDatum WGS84 = new GeodeticDatum( Ellipsoid.WGS84, new CRSCodeType( "6326", "EPSG" ),
                                                                 "WGS_1984" );

    private PrimeMeridian primeMeridian;

    private Ellipsoid ellipsoid;

    private Helmert toWGS84;

    /**
     * @param ellipsoid
     *            of this datum
     * @param primeMeridian
     *            to which this datum is defined.
     * @param toWGS84
     *            bursa-wolf parameters describing the transform from this datum into the wgs84 datum.
     * @param id
     *            containing all relevant id data.
     */
    public GeodeticDatum( Ellipsoid ellipsoid, PrimeMeridian primeMeridian, Helmert toWGS84, CRSIdentifiable id ) {
        super( id );
        this.ellipsoid = ellipsoid;
        this.primeMeridian = primeMeridian;
        this.toWGS84 = toWGS84;
    }

    /**
     * @param ellipsoid
     *            of this datum
     * @param primeMeridian
     *            to which this datum is defined.
     * @param id
     *            containing all relevant id data.
     */
    public GeodeticDatum( Ellipsoid ellipsoid, PrimeMeridian primeMeridian, CRSIdentifiable id ) {
        this( ellipsoid, primeMeridian, null, id );
    }

    /**
     * @param ellipsoid
     *            of this datum
     * @param primeMeridian
     *            to which this datum is defined.
     * @param toWGS84
     *            bursa-wolf parameters describing the transform from this datum into the wgs84 datum.
     * @param codes
     * @param names
     * @param versions
     * @param descriptions
     * @param areasOfUse
     */
    public GeodeticDatum( Ellipsoid ellipsoid, PrimeMeridian primeMeridian, Helmert toWGS84, CRSCodeType[] codes,
                          String[] names, String[] versions, String[] descriptions, String[] areasOfUse ) {
        this( ellipsoid, primeMeridian, toWGS84, new CRSIdentifiable( codes, names, versions, descriptions, areasOfUse ) );
    }

    /**
     * A datum with given ellipsoid and a GreenWich prime-meridian.
     * 
     * @param ellipsoid
     *            of this datum
     * @param toWGS84
     *            bursa-wolf parameters describing the transform from this datum into the wgs84 datum.
     * @param codes
     */
    public GeodeticDatum( Ellipsoid ellipsoid, Helmert toWGS84, CRSCodeType[] codes ) {
        this( ellipsoid, PrimeMeridian.GREENWICH, toWGS84, new CRSIdentifiable( codes, null, null, null, null ) );
    }

    /**
     * A datum with given ellipsoid and a prime-meridian.
     * 
     * @param ellipsoid
     *            of this datum
     * @param primeMeridian
     *            to which this datum is defined.
     * @param toWGS84
     *            bursa-wolf parameters describing the transform from this datum into the wgs84 datum.
     * @param codes
     */
    public GeodeticDatum( Ellipsoid ellipsoid, PrimeMeridian primeMeridian, Helmert toWGS84, CRSCodeType[] codes ) {
        this( ellipsoid, primeMeridian, toWGS84, new CRSIdentifiable( codes, null, null, null, null ) );
    }

    /**
     * @param ellipsoid
     *            of this datum
     * @param primeMeridian
     *            to which this datum is defined.
     * @param toWGS84
     *            bursa-wolf parameters describing the transform from this datum into the wgs84 datum.
     * @param code
     * @param name
     * @param version
     * @param description
     * @param areaOfUse
     */
    public GeodeticDatum( Ellipsoid ellipsoid, PrimeMeridian primeMeridian, Helmert toWGS84, CRSCodeType code,
                          String name, String version, String description, String areaOfUse ) {
        this( ellipsoid, primeMeridian, toWGS84, new CRSIdentifiable( new CRSCodeType[] { code },
                                                                      new String[] { name }, new String[] { version },
                                                                      new String[] { description },
                                                                      new String[] { areaOfUse } ) );
    }

    /**
     * A datum with given ellipsoid and a GreenWich prime-meridian.
     * 
     * @param ellipsoid
     *            of this datum
     * @param toWGS84
     *            bursa-wolf parameters describing the transform from this datum into the wgs84 datum.
     * @param code
     * @param name
     */
    public GeodeticDatum( Ellipsoid ellipsoid, Helmert toWGS84, CRSCodeType code, String name ) {
        this( ellipsoid, PrimeMeridian.GREENWICH, toWGS84,
              new CRSIdentifiable( new CRSCodeType[] { code }, new String[] { name }, null, null, null ) );
    }

    /**
     * A datum with given ellipsoid and a GreenWich prime-meridian, with no helmert.
     * 
     * @param ellipsoid
     *            of this datum
     * @param code
     * @param name
     */
    public GeodeticDatum( Ellipsoid ellipsoid, CRSCodeType code, String name ) {
        this( ellipsoid, PrimeMeridian.GREENWICH, null, new CRSIdentifiable( new CRSCodeType[] { code },
                                                                             new String[] { name }, null, null, null ) );
    }

    /**
     * @return the ellipsoid.
     */
    public final Ellipsoid getEllipsoid() {
        return ellipsoid;
    }

    /**
     * @return the primeMeridian.
     */
    public final PrimeMeridian getPrimeMeridian() {
        return primeMeridian;
    }

    /**
     * @param primeMeridian
     */
    public final void setPrimeMeridian( PrimeMeridian primeMeridian ) {
        this.primeMeridian = primeMeridian;
    }

    /**
     * @return the toWGS84Conversion information needed to convert this geodetic Datum into the geocentric WGS84 Datum.
     */
    public final Helmert getWGS84Conversion() {
        return toWGS84;
    }

    /**
     * 
     * @param toWGS84Conversion
     *            the transformation to be used to convert this geodetic datum into the wgs84 datum.
     */
    public final void setToWGS84( Helmert toWGS84Conversion ) {
        this.toWGS84 = toWGS84Conversion;
    }

    @Override
    public boolean equals( Object other ) {
        if ( other != null && other instanceof GeodeticDatum ) {
            GeodeticDatum that = (GeodeticDatum) other;
            return this.getPrimeMeridian().equals( that.getPrimeMeridian() )
                   && this.getEllipsoid().equals( that.getEllipsoid() )
                   && ( ( this.getWGS84Conversion() == null ) ? that.getWGS84Conversion() == null
                                                             : this.getWGS84Conversion().equals(
                                                                                                 that.getWGS84Conversion() ) )
                   && super.equals( that );
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.crs.CRSIdentifiable#toString()
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder( super.toString() );
        sb.append( "\n - Ellipsoid: " ).append( ellipsoid );
        sb.append( "\n - Primemeridian: " ).append( primeMeridian );
        if ( this.toWGS84 != null ) {
            sb.append( "\n - wgs84-conversion-info: " ).append( toWGS84 );
        }
        return sb.toString();
    }

    /**
     * Implementation as proposed by Joshua Block in Effective Java (Addison-Wesley 2001), which supplies an even
     * distribution and is relatively fast. It is created from field <b>f</b> as follows:
     * <ul>
     * <li>boolean -- code = (f ? 0 : 1)</li>
     * <li>byte, char, short, int -- code = (int)f </li>
     * <li>long -- code = (int)(f ^ (f &gt;&gt;&gt;32))</li>
     * <li>float -- code = Float.floatToIntBits(f);</li>
     * <li>double -- long l = Double.doubleToLongBits(f); code = (int)(l ^ (l &gt;&gt;&gt; 32))</li>
     * <li>all Objects, (where equals(&nbsp;) calls equals(&nbsp;) for this field) -- code = f.hashCode(&nbsp;)</li>
     * <li>Array -- Apply above rules to each element</li>
     * </ul>
     * <p>
     * Combining the hash code(s) computed above: result = 37 * result + code;
     * </p>
     * 
     * @return (int) ( result >>> 32 ) ^ (int) result;
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        // the 2.nd million th. prime, :-)
        long code = 32452843;
        if ( primeMeridian != null ) {
            code = code * 37 + primeMeridian.hashCode();
        }
        if ( ellipsoid != null ) {
            code = code * 37 + ellipsoid.hashCode();
        }
        if ( toWGS84 != null ) {
            code = code * 37 + toWGS84.hashCode();
        }
        return (int) ( code >>> 32 ) ^ (int) code;
    }

}

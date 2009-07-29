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

/**
 * The <code>Axis</code> class describe the orientation, unit and the name of a crs-axis.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public class Axis extends CRSIdentifiable {

    /**
     * Axis is pointing NORTH ( == -SOUTH) e.g Polar axis positive northwards.
     */
    public static final int AO_NORTH = 1;

    /**
     * Axis is pointing SOUTH ( == -NORTH )
     */
    public static final int AO_SOUTH = -AO_NORTH;

    /**
     * Axis is pointing WEST( == -EAST)
     */
    public static final int AO_WEST = 2;

    /**
     * Axis is pointing EAST( == -WEST) the intersection of the equator with longitude 90°E.
     */
    public static final int AO_EAST = -AO_WEST;

    /**
     * Axis is pointing UP ( == -DOWN ),
     */
    public static final int AO_UP = 3;

    /**
     * Axis is pointing DOWN ( == -UP)
     */
    public static final int AO_DOWN = -AO_UP;

    /**
     * Axis is pointing FRONT( == -BACK), e.g. the Axis through the intersection of the Greenwich meridian and equator.
     */
    public static final int AO_FRONT = 4;

    /**
     * Axis is pointing BACK ( == -FRONT) e.g. the Axis through the intersection of the opposite of the Greenwich
     * meridian and equator.
     */
    public static final int AO_BACK = -AO_FRONT;

    /**
     * Axis is pointing PERPENDICULAR to the earth's surface, which is used for a vertical axis.
     */
    public static final int AO_PERPENDICULAR = 5;

    /**
     * Axis is pointing in an OTHER direction, which is not specified.
     */
    public static final int AO_OTHER = Integer.MAX_VALUE;

    private Unit units;

    private String axisName;

    private int orientation;

    /**
     * @param units
     *            of this axis
     * @param axisName
     *            of this axis (e.g. longitude...)
     * @param orientation
     *            of the positive scale direction, one of Axis.AO*. If an unknown value is supplied AO_OTHER is assumed.
     */
    public Axis( Unit units, String axisName, final int orientation ) {
        super( new CRSCodeType[] { CRSCodeType.getUndefined() }, new String[] { axisName }, new String[] {},
               new String[] {}, new String[] {} );
        this.units = units;
        this.axisName = axisName;
        this.orientation = orientation;
        if ( orientation != AO_NORTH && orientation != AO_SOUTH && orientation != AO_WEST && orientation != AO_EAST
             && orientation != AO_UP && orientation != AO_DOWN && orientation != AO_FRONT && orientation != AO_BACK
             && orientation != AO_PERPENDICULAR ) {
            this.orientation = AO_OTHER;
        }
    }

    /**
     * Parses the given orientation and creates a valid orientation of it's non-case-sensitive version. If no conversion
     * was found, {@link #AO_OTHER} will be used.
     * 
     * @param units
     *            of the axis.
     * @param axisName
     *            of the axis.
     * @param orientation
     *            of the axis as a string for example north
     */
    public Axis( Unit units, String axisName, String orientation ) {
        super( new CRSCodeType[] { CRSCodeType.getUndefined() }, new String[] { axisName }, new String[] {},
               new String[] {}, new String[] {} );
        this.units = units;
        this.axisName = axisName;
        this.orientation = AO_OTHER;
        if ( orientation != null ) {
            String tmp = orientation.trim().toLowerCase();
            if ( tmp.contains( "north" ) ) {
                this.orientation = AO_NORTH;
            } else if ( tmp.contains( "south" ) ) {
                this.orientation = AO_SOUTH;
            } else if ( tmp.contains( "east" ) ) {
                this.orientation = AO_EAST;
            } else if ( tmp.contains( "west" ) ) {
                this.orientation = AO_WEST;
            } else if ( tmp.contains( "front" ) ) {
                this.orientation = AO_FRONT;
            } else if ( tmp.contains( "back" ) ) {
                this.orientation = AO_BACK;
            } else if ( tmp.contains( "up" ) ) {
                this.orientation = AO_UP;
            } else if ( tmp.contains( "down" ) ) {
                this.orientation = AO_DOWN;
            } else if ( tmp.contains( "perpendicular" ) ) {
                this.orientation = AO_PERPENDICULAR;
            }
        }
    }

    /**
     * An Axis with unit set to metre.
     * 
     * @param name
     *            of this axis (e.g. longitude...)
     * @param orientation
     *            of the positive scale direction, one of Axis.AO*. If an unknown value is supplied AO_OTHER is assumed.
     */
    public Axis( String name, final int orientation ) {
        this( Unit.METRE, name, orientation );
    }

    /**
     * Parses the given orientation and creates a valid orientation of it's non-case-sensitive version. If no conversion
     * was found, {@link #AO_OTHER} will be used. This axis will have metres as it's unit.
     * 
     * @param name
     *            of the axis
     * @param orientation
     *            of the axis as a string for example north
     */
    public Axis( String name, final String orientation ) {
        this( Unit.METRE, name, orientation );
    }

    // /**
    // * @return the name.
    // */
    // public final String getName() {
    // return name;
    // }

    /**
     * @return the orientation.
     */
    public final int getOrientation() {
        return orientation;
    }

    /**
     * @return the units.
     */
    public final Unit getUnits() {
        return units;
    }

    @Override
    public String toString() {
        return new StringBuilder( "name: " ).append( axisName ).append( " orientation: " ).append(
                                                                                                   getOrientationAsString() ).append(
                                                                                                                                      " units: " ).append(
                                                                                                                                                           units ).toString();
    }

    @Override
    public boolean equals( Object otherAxis ) {
        if ( otherAxis == null || !( otherAxis instanceof Axis ) ) {
            return false;
        }
        Axis other = (Axis) otherAxis;
        return this.units.equals( other.units ) && this.orientation == other.orientation;
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
        // the 2nd millionth prime, :-)
        long code = 32452843;
        if ( units != null ) {
            code = code * 37 + units.hashCode();
        }
        long tmp = Double.doubleToLongBits( orientation );
        code = code * 37 + (int) ( tmp ^ ( tmp >>> 32 ) );
        return (int) ( code >>> 32 ) ^ (int) code;
    }

    /**
     * @return an 'English' representation for the Axis Orientation, or Unknown if the given direction is not known.
     */
    public String getOrientationAsString() {
        switch ( orientation ) {
        case AO_NORTH:
            return "north";
        case AO_SOUTH:
            return "south";
        case AO_EAST:
            return "east";
        case AO_WEST:
            return "west";
        case AO_FRONT:
            return "front";
        case AO_BACK:
            return "back";
        case AO_UP:
            return "up";
        case AO_DOWN:
            return "down";
        case AO_OTHER:
            return "Other";
        case AO_PERPENDICULAR:
            return "perpendicular";
        }
        return "Unknown";
    }

}

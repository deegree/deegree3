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

package org.deegree.ogcwebservices.wms.operation;

import static java.lang.Float.parseFloat;
import static java.lang.Math.abs;

import java.util.LinkedList;

/**
 * <code>DimensionValues</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DimensionValues {

    /**
     * original value used for initializing
     */
    private String originalValue;

    /**
     * The actual values.
     */
    public LinkedList<DimensionValue> values;

    /**
     * @param val
     */
    public DimensionValues( String val ) {
        this.originalValue = val;
        values = new LinkedList<DimensionValue>();
        String[] vals = val.split( "," );
        for ( String v : vals ) {
            values.add( new DimensionValue( v ) );
        }
    }

    /**
     * @return the originalValue
     */
    public String getOriginalValue() {
        return originalValue;
    }

    /**
     * @return if multiple values are contained
     */
    public boolean hasMultipleValues() {
        return values.size() > 1;
    }

    /**
     * @param value
     * @return true, if the given single value is contained
     */
    public boolean includesValue( String value ) {
        for ( DimensionValue val : values ) {
            if ( val.value == null ) {
                // TODO use the resolution
                if ( val.low.compareTo( value ) <= 0 && val.high.compareTo( value ) >= 0 ) {
                    return true;
                }
            } else {
                if ( val.value.equals( value ) ) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @param value
     * @return true, if the given single value is contained
     */
    public boolean includesValue( float value ) {
        for ( DimensionValue val : values ) {
            if ( val.value == null ) {
                // TODO use resolution
                if ( val.lowf <= value && val.highf >= value ) {
                    return true;
                }
            } else {
                if ( abs( val.valuef - value ) <= 0.0000001 ) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * @param elev
     * @return true, if the value(s) are valid
     */
    public boolean includes( DimensionValues elev ) {
        for ( DimensionValue val : elev.values ) {
            if ( val.value != null ) {
                if ( !includesValue( parseFloat( val.value ) ) ) {
                    return false;
                }
            } else {
                if ( !includesValue( parseFloat( val.high ) ) || !includesValue( parseFloat( val.low ) ) ) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * @param value
     * @return the nearest value
     */
    public String getNearestValue( String value ) {
        String nearestHigh = null, nearestLow = null;
        for ( DimensionValue val : values ) {
            if ( val.value == null ) {
                // TODO check it for ranges
            } else {
                if ( nearestHigh != null && nearestHigh.compareTo( val.value ) > 0 && val.value.compareTo( value ) > 0 ) {
                    nearestHigh = val.value;
                    continue;
                }
                if ( nearestLow != null && nearestLow.compareTo( val.value ) < 0 && val.value.compareTo( value ) < 0 ) {
                    nearestLow = val.value;
                    continue;
                }
                if ( nearestHigh == null && val.value.compareTo( value ) > 0 ) {
                    nearestHigh = val.value;
                    continue;
                }
                if ( nearestLow == null && val.value.compareTo( value ) < 0 ) {
                    nearestLow = val.value;
                    continue;
                }
            }
        }

        if ( nearestHigh == null ) {
            return nearestLow;
        }
        if ( nearestLow == null ) {
            return nearestHigh;
        }
        try {
            float low = parseFloat( nearestLow );
            float high = parseFloat( nearestHigh );
            float val = parseFloat( value );
            if ( abs( val - low ) > abs( val - high ) ) {
                return nearestHigh;
            }
            return nearestLow;
        } catch ( NumberFormatException nfe ) {
            // TODO for time
        }
        return null; // TODO check which bound is nearer
    }

    /**
     * <code>DimensionValue</code>
     * 
     * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
     * @author last edited by: $Author$
     * 
     * @version $Revision$, $Date$
     */
    public class DimensionValue {
        /**
         *
         */
        public String value;

        /**
         *
         */
        public float valuef;

        /**
         *
         */
        public String low;

        /**
         *
         */
        public float lowf;

        /**
         *
         */
        public String high;

        /**
         *
         */
        public float highf;

        /**
         *
         */
        public String res;

        /**
         *
         */
        public float resf;

        DimensionValue( String val ) {
            if ( val.indexOf( "/" ) != -1 ) {
                String[] vs = val.split( "/" );
                low = vs[0];
                high = vs[1];
                try {
                    lowf = parseFloat( low );
                    highf = parseFloat( high );
                } catch ( NumberFormatException nfe ) {
                    // no float values then
                }
                if ( vs.length > 2 ) {
                    res = vs[2];
                    try {
                        resf = parseFloat( res );
                    } catch ( NumberFormatException nfe ) {
                        // no float value then
                    }
                }
            } else {
                value = val;
                try {
                    valuef = parseFloat( value );
                } catch ( NumberFormatException nfe ) {
                    // no float value then
                }
            }
        }
    }
}

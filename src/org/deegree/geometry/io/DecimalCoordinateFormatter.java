//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.geometry.io;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * {@link CoordinateFormatter} based on {@link DecimalFormat}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DecimalCoordinateFormatter implements CoordinateFormatter {

    private final DecimalFormat decimalFormat;

    /**
     * Creates a new {@link DecimalCoordinateFormatter} instance that uses the specified number of decimal places.
     * 
     * @param decimalPlaces
     *            number of decimal places
     */
    public DecimalCoordinateFormatter( int decimalPlaces ) {
        StringBuffer pattern = new StringBuffer( "0" );
        if ( decimalPlaces > 0 ) {
            pattern.append( "." );
            for ( int i = 0; i < decimalPlaces; i++ ) {
                pattern.append( "0" );
            }
        }
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator( '.' );
        decimalFormat = new DecimalFormat( pattern.toString(), symbols );
    }

    /**
     * Creates a new {@link DecimalCoordinateFormatter} instance from the given {@link DecimalFormat}.
     * 
     * @param decimalFormat
     *            decimalFormat to use for formatting, must not be <code>null</code>
     */
    public DecimalCoordinateFormatter( DecimalFormat decimalFormat ) {
        this.decimalFormat = decimalFormat;
    }

    @Override
    public String format( double number ) {
        return decimalFormat.format( number );
    }
}

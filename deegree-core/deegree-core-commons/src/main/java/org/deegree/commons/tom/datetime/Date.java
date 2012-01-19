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
package org.deegree.commons.tom.datetime;

import static javax.xml.bind.DatatypeConverter.printDate;

import java.util.Calendar;
import java.util.TimeZone;

import javax.xml.bind.DatatypeConverter;

/**
 * {@link TimeInstant} for representing <code>xs:date</code> values.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Date extends TimeInstant {

    /**
     * Creates a new {@link Date} instance from the given <code>xs:date</code> encoded value.
     * 
     * @param xsDate
     *            encoded date, must not be <code>null</code>
     * @throws IllegalArgumentException
     *             if parameter does not conform to lexical value space defined in XML Schema Part 2: Datatypes for
     *             <code>xs:date</code>
     */
    public Date( String xsDate ) throws IllegalArgumentException {
        super( DatatypeConverter.parseDate( xsDate ), isLocal( xsDate ) );
    }

    public Date( java.util.Date date, TimeZone tz ) {
        super( date, tz );
    }

    public Date( Calendar cal, boolean isUnknown ) {
        super( cal, isUnknown );
    }

    @Override
    public String toString() {
        return printDate( getCalendar() );
    }
}

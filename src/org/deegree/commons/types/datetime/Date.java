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
package org.deegree.commons.types.datetime;

import java.text.ParseException;

import org.deegree.commons.utils.time.DateUtils;

/**
 * Represents an <code>xs:date</code> instance.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Date implements Comparable<Date> {

    private final String isoDate;

    private final java.util.Date date;

    public Date( String isoDate ) throws ParseException {
        this.isoDate = isoDate;
        date = DateUtils.parseISO8601Date( isoDate );
    }

    @Override
    public int compareTo( Date o ) {
        return this.date.compareTo( o.date );
    }

    @Override
    public boolean equals( Object o ) {
        if ( !( o instanceof Date ) ) {
            return false;
        }
        return this.date.equals( ( (Date) o ).date );
    }

    @Override
    public String toString() {
        return isoDate;
    }
}

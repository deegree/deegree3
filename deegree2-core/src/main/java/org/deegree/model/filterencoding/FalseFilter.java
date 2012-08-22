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
package org.deegree.model.filterencoding;

import org.deegree.model.feature.Feature;

/**
 * A {@link Filter} that always evaluates to false.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class FalseFilter implements Filter {

    /**
     * Calculates the <tt>Filter</tt>'s logical value (false).
     * <p>
     *
     * @param feature
     *            (in this special case irrelevant)
     * @return false (always)
     */
    public boolean evaluate( Feature feature ) {
        return false;
    }

    public StringBuffer toXML() {
        StringBuffer sb = new StringBuffer();
        sb.append( "<ogc:Filter xmlns:ogc='http://www.opengis.net/ogc'>" );
        sb.append( "<False/>" );
        sb.append( "</ogc:Filter>\n" );
        return sb;
    }

    public StringBuffer to100XML() {
        return toXML();
    }

    public StringBuffer to110XML() {
        return toXML();
    }
}

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
package org.deegree.commons.types.ows;

/**
 * A simple text description or a reference to an external description.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class StringOrRef {

    private String string;

    private String ref;

    public StringOrRef( String s, String ref ) {
        this.string = s;
        this.ref = ref;
    }

    public String getString() {
        return string;
    }

    public String getRef() {
        return ref;
    }

    @Override
    public boolean equals( Object o ) {
        if ( o instanceof StringOrRef ) {
            StringOrRef that = (StringOrRef) o;
            if (string != null) {
                return string.equals( that.string );
            } else if (that.string != null) {
                return that.string.equals( string );
            } else {
                return true;
            }
            // TODO ref?
        } else if ( o instanceof String ) {
            return o.equals( string );
        }
        return false;
    }
    
    @Override
    // TODO clarify how PropertyIsEqualTo depends on this methods (which currently requires to return the text node)
    public String toString () {
        return string;
    }
}

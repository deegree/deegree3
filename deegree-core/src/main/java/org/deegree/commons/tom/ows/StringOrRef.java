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
package org.deegree.commons.tom.ows;

import org.deegree.commons.tom.TypedObjectNode;

/**
 * A simple text description or a reference to an external description.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class StringOrRef implements TypedObjectNode {

    private String string;

    private String ref;

    /**
     * @param s
     * @param ref
     */
    public StringOrRef( String s, String ref ) {
        this.string = s;
        this.ref = ref;
    }

    /**
     * @return the string
     */
    public String getString() {
        return string;
    }

    /**
     * @return the ref
     */
    public String getRef() {
        return ref;
    }

    @Override
    public boolean equals( Object o ) {
        if ( o instanceof StringOrRef ) {
            StringOrRef that = (StringOrRef) o;
            if ( string != null ) {
                return string.equals( that.string );
            } else if ( that.string != null ) {
                return that.string.equals( string );
            } else {
                if ( ref != null ) {
                    return ref.equals( that.ref );
                } else if ( that.ref != null ) {
                    return that.ref.equals( ref );
                } else {
                    return true;
                }
            }
            // TODO ref?
        } else if ( o instanceof String ) {
            return o.equals( string );
        }
        return false;
    }

    /**
     * Implementation as proposed by Joshua Block in Effective Java (Addison-Wesley 2001), which supplies an even
     * distribution and is relatively fast. It is created from field <b>f</b> as follows:
     * <ul>
     * <li>boolean -- code = (f ? 0 : 1)</li>
     * <li>byte, char, short, int -- code = (int)f</li>
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
        long result = 32452843;
        if ( string != null ) {
            result = result * 37 + string.hashCode();
        }
        if ( ref != null ) {
            result = result * 37 + ref.hashCode();
        }
        return (int) ( result >>> 32 ) ^ (int) result;
    }

    @Override
    // TODO clarify how PropertyIsEqualTo depends on this methods (which currently requires to return the text node)
    public String toString() {
        return string;
    }
}

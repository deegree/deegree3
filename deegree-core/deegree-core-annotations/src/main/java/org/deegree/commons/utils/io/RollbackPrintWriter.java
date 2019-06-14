//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.commons.utils.io;

import java.io.PrintWriter;
import java.util.LinkedList;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * @deprecated This class is deprecated as of version 3.4 of deegree.
 */
@Deprecated
public class RollbackPrintWriter {

    private PrintWriter out;

    private LinkedList<String> list = new LinkedList<String>();

    /**
     * @param out
     */
    public RollbackPrintWriter( PrintWriter out ) {
        this.out = out;
    }

    /**
     * 
     */
    public void println() {
        list.add( "" );
    }

    /**
     * @param s
     */
    public void println( String s ) {
        list.add( s );
    }

    /**
     * @param s
     */
    public void print( String s ) {
        // string builders?
        if ( list.isEmpty() ) {
            list.add( s );
        } else {
            s = list.getLast() + s;
            list.removeLast();
            list.add( s );
        }
    }

    /**
     * 
     */
    public void flush() {
        while ( !list.isEmpty() ) {
            out.println( list.poll() );
        }
    }

    /**
     * 
     */
    public void rollback() {
        list.clear();
    }

    /**
     * 
     */
    public void close() {
        flush();
        out.close();
    }

}

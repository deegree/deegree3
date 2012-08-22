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
package org.deegree.enterprise.control;

import java.io.BufferedReader;
import java.io.StringReader;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Administrator
 */

public class RPCWebEvent extends WebEvent {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    private RPCMethodCall mc = null;

    /** Creates a new instance of RPCWebEvent */
    public RPCWebEvent( HttpServletRequest request ) {
        super( request );
    }

    /** Creates a new instance of RPCWebEvent */
    public RPCWebEvent( HttpServletRequest request, RPCMethodCall mc ) {
        super( request );
        this.mc = mc;
    }

    /** Creates a new instance of RPCWebEvent */
    public RPCWebEvent( FormEvent parent, RPCMethodCall mc ) {
        super( (HttpServletRequest) parent.getSource() );
        this.mc = mc;
    }

    /**
     * returns the the RPC methodcall extracted from the <tt>HttpServletRequest</tt> passed to the
     * first constructor.
     */
    public RPCMethodCall getRPCMethodCall() {
        if ( mc == null ) {
            try {
                mc = getMethodCall( (ServletRequest) this.getSource() );
            } catch ( Exception e ) {
                e.printStackTrace();
            }
        }
        return mc;
    }

    /**
     * extracts the RPC method call from the
     *
     * @param request
     * @throws RPCException
     */
    private RPCMethodCall getMethodCall( ServletRequest request )
                            throws RPCException {

        StringBuffer sb = new StringBuffer( 1000 );
        try {
            BufferedReader br = request.getReader();
            String line = null;
            while ( ( line = br.readLine() ) != null ) {
                sb.append( line );
            }
            br.close();
        } catch ( Exception e ) {
            throw new RPCException( "Error reading stream from servlet\n" + e.toString() );
        }

        String s = sb.toString();
        int pos1 = s.indexOf( "<methodCall>" );
        int pos2 = s.indexOf( "</methodCall>" );
        if ( pos1 < 0 ) {
            throw new RPCException( "request doesn't contain a RPC methodCall" );
        }
        s = s.substring( pos1, pos2 + 13 );

        StringReader reader = new StringReader( s );
        RPCMethodCall mc = RPCFactory.createRPCMethodCall( reader );

        return mc;
    }
}

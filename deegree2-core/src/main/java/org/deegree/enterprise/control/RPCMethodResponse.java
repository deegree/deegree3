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

/**
 * The class encapsulates the result to a RPC. This can be an object or an instance of
 * <tt>RPCFault</tt> if an exception occured while performing a RPC
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @version $Revision$ $Date$
 */

public class RPCMethodResponse {

    private boolean fault_ = false;

    private RPCParameter[] return_ = null;

    private RPCFault fault = null;

    RPCMethodResponse( RPCParameter[] return_ ) {
        this.return_ = return_;
    }

    RPCMethodResponse( RPCFault fault ) {
        this.fault = fault;
        fault_ = true;
    }

    /**
     * returns true if the result contains a fault and not the expected data
     *
     * @return true if a fault occured
     */
    public boolean hasFault() {
        return fault_;
    }

    /**
     * returns the result of a method call as array of <tt>RPCParameter</tt>s
     *
     * @return result parameters
     */
    public RPCParameter[] getReturn() {
        return return_;
    }

    /**
     * returns the fault object if a fault occured while performing a RPC
     *
     * @return fault object
     */
    public RPCFault getFault() {
        return fault;
    }

}

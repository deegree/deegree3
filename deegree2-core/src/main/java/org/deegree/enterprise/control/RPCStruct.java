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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The class encapsulates a RPC struct.
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @version $Revision$ $Date$
 */
public class RPCStruct {

    private HashMap<String, RPCMember> members = null;

    private List<RPCMember> mem = null;

    /**
     *
     *
     */
    public RPCStruct() {
        members = new HashMap<String, RPCMember>();
        mem = new ArrayList<RPCMember>();
    }

    /**
     *
     * @param mem
     */
    public RPCStruct( RPCMember[] mem ) {
        members = new HashMap<String, RPCMember>( mem.length );
        this.mem = new ArrayList<RPCMember>( mem.length );
        for ( int i = 0; i < mem.length; i++ ) {
            members.put( mem[i].getName(), mem[i] );
            this.mem.add( mem[i] );
        }
    }

    /**
     * returns the members of the struct
     *
     * @return members of the struct
     */
    public RPCMember[] getMembers() {
        RPCMember[] m = new RPCMember[members.size()];
        return mem.toArray( m );
    }

    /**
     * returns a named member of the struct. if no member with the passed name is contained within
     * the struct <tt>null</tt> will be returned.
     *
     * @param name
     *            name of the struct member
     *
     * @return struct member
     */
    public RPCMember getMember( String name ) {
        return members.get( name );
    }

    /**
     * adds a new member to the struct
     *
     * @param member
     */
    public void addMember( RPCMember member ) {
        members.put( member.getName(), member );
    }

    /**
     * removes a member identified by its name from the struct
     *
     * @param name
     *
     * @return removed member
     */
    public RPCMember removeMember( String name ) {
        return members.remove( name );
    }

}

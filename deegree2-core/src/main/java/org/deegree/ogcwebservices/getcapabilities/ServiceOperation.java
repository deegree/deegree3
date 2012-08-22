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
package org.deegree.ogcwebservices.getcapabilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ServiceOperation
 *
 * @author Administrator
 *
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class ServiceOperation {

    private List<DCPType> dcpList;

    /**
     *
     */
    public ServiceOperation() {
        this.dcpList = new ArrayList<DCPType>();
    }

    /**
     * @param protocol
     * @return the dcptype array
     */
    public DCPType[] getDCPTypes( Protocol protocol ) {
        DCPType[] typeArray;
        List<DCPType> returnTypeList = new ArrayList<DCPType>();
        Iterator<DCPType> iterator = dcpList.iterator();
        while ( iterator.hasNext() ) {
            DCPType element = iterator.next();
            if ( element.getProtocol().equals( protocol ) ) {
                returnTypeList.add( element );
            }
        }
        typeArray = new DCPType[returnTypeList.size()];
        return returnTypeList.toArray( typeArray );
    }

    /**
     * Set all DCP types. First empyt list, then sets
     *
     * @param types
     */
    public void setDCPTypes( DCPType[] types ) {
        this.dcpList.clear();
        for ( int i = 0; i < types.length; i++ ) {
            this.addDCPType( types[i] );
        }
    }

    /**
     * @param type
     */
    public void addDCPType( DCPType type ) {
        this.dcpList.add( type );
    }

}

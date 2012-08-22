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

package org.deegree.ogcwebservices.wass.common;

import org.deegree.ogcwebservices.getcapabilities.OperationsMetadata;
import org.deegree.owscommon.OWSDomainType;

/**
 * Encapsulated data: OperationsMetadata
 *
 * Namespace: http://www.opengis.net/ows
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class OperationsMetadata_1_0 extends OperationsMetadata {

    private static final long serialVersionUID = 3587847964265446945L;

    private String extendedCapabilities = null;

    private Operation_1_0[] allOperations = null;

    /**
     * Creates new one from data.
     *
     * @param operations
     * @param parameters
     * @param constraints
     * @param extendedCapabilities
     * @param describeUser
     * @param getCapabilities
     */
    public OperationsMetadata_1_0( Operation_1_0[] operations, OWSDomainType[] parameters,
                                  OWSDomainType[] constraints, String extendedCapabilities,
                                  Operation_1_0 describeUser, Operation_1_0 getCapabilities ) {
        super( getCapabilities, parameters, constraints );
        this.allOperations = operations;
        this.describeUser = describeUser;
        this.extendedCapabilities = extendedCapabilities;
    }

    /*
     * Not specified in nrwgdi but needed by deegree to get the id (ip) from a user.
     */
    private Operation_1_0 describeUser = null;

    /**
     * @return all operations
     */
    public Operation_1_0[] getAllOperations() {
        return allOperations;
    }

    /**
     * @return Returns the describeUser operation.
     */
    public Operation_1_0 getDescribeUser() {
        return describeUser;
    }

    /**
     * @return Returns the extendedCapabilities.
     */
    public String getExtendedCapabilities() {
        return extendedCapabilities;
    }

}

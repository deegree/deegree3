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

package org.deegree.owscommon.com110;

import org.deegree.ogcwebservices.getcapabilities.DCPType;
import org.deegree.ogcwebservices.getcapabilities.Operation;
import org.deegree.owscommon.OWSMetadata;

/**
 * TODO class description
 *
 * @author <a href="mailto:mays@lat-lon.de">Judit Mays</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */
public class Operation110 extends Operation {

    private OWSDomainType110[] parameters;

    private OWSDomainType110[] constraints;

    private OWSMetadata[] metadata;

    /**
     * Creates a new <code>Operation110</code> object with information on <code>DHCP</code>,
     * <code>Parameter</code> and <code>Constraint</code>.
     *
     * @param name
     * @param dcps
     * @param parameters
     * @param constraints
     * @param metadata
     */
    public Operation110( String name, DCPType[] dcps, OWSDomainType110[] parameters, OWSDomainType110[] constraints,
                         OWSMetadata[] metadata ) {

        super( name, dcps );

        if ( parameters == null ) {
            this.parameters = new OWSDomainType110[0];
        } else {
            this.parameters = parameters;
        }

        if ( constraints == null ) {
            this.constraints = new OWSDomainType110[0];
        } else {
            this.constraints = constraints;
        }

        if ( metadata == null ) {
            this.metadata = new OWSMetadata[0];
        } else {
            this.metadata = metadata;
        }
    }

    /**
     * Returns an array of all the parameter objects. If parameters is null, an array of size 0 is
     * returned.
     *
     * @return Returns all the parameters.
     */
    public OWSDomainType110[] getParameters110() {
        return parameters;
    }

    /**
     * @param name
     * @return Returns the parameter object to the given name.
     */
    public OWSDomainType110 getParameter110( String name ) {

        for ( int i = 0; i < parameters.length; i++ ) {
            if ( name.equals( parameters[i].getName() ) ) {
                return parameters[i];
            }
        }
        return null;
    }

    /**
     * Returns an array of all the constraint objects. If constraints is null, an array of size 0 is
     * returned.
     *
     * @return Returns all the constraints.
     */
    public OWSDomainType110[] getConstraints110() {
        return constraints;
    }

    /**
     * @param name
     * @return Returns the constraint object to the given name.
     */
    public OWSDomainType110 getConstraint110( String name ) {

        for ( int i = 0; i < constraints.length; i++ ) {
            if ( name.equals( constraints[i].getName() ) ) {
                return constraints[i];
            }
        }
        return null;
    }

    /**
     * Returns an array of all the metadata objects. If metadata is null, an array of size 0 is
     * returned.
     *
     * @return Returns the metadata.
     */
    public OWSMetadata[] getMetadata110() {
        return metadata;
    }

}

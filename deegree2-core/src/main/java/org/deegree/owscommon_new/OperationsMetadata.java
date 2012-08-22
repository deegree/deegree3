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
package org.deegree.owscommon_new;

import java.util.List;

import org.deegree.datatypes.QualifiedName;

/**
 * <code>OperationsMetadata</code> stores the contents of a OperationsMetadata element according
 * to the OWS common specification version 1.0.0.
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version 2.0, $Revision$, $Date$
 *
 * @since 2.0
 */

public class OperationsMetadata {

    private List<Parameter> parameters;

    private List<DomainType> constraints;

    private List<Operation> operations;

    private List<Object> operatesOn;

    /**
     * Standard constructor that initializes all encapsulated data.
     *
     * @param parameters
     * @param constraints
     * @param operations
     * @param operatesOn
     */
    public OperationsMetadata( List<Parameter> parameters, List<DomainType> constraints, List<Operation> operations,
                               List<Object> operatesOn ) {
        this.parameters = parameters;
        this.constraints = constraints;
        this.operations = operations;
        this.operatesOn = operatesOn;
    }

    /**
     * @return Returns the constraints.
     */
    public List<DomainType> getConstraints() {
        return constraints;
    }

    /**
     * @return Returns the operations.
     */
    public List<Operation> getOperations() {
        return operations;
    }

    /**
     * @return Returns the parameters.
     */
    public List<Parameter> getParameters() {
        return parameters;
    }

    /**
     * @param name
     * @return the <code>DomainType</code> with the specified name or null, if there is no
     *         constraint with that name.
     */
    public DomainType getConstraint( QualifiedName name ) {
        for ( DomainType constraint : constraints ) {
            if ( constraint.getName().equals( name ) ) {
                return constraint;
            }
        }

        return null;
    }

    /**
     * @param name
     * @return the <code>Parameter</code> with the specified name or null, if there is no
     *         parameter with that name. This method only tests Parameters that are
     *         <code>DomainType</code>s.
     */
    public Parameter getParameter( QualifiedName name ) {
        for ( Parameter parameter : parameters ) {
            if ( parameter instanceof DomainType ) {
                if ( ( (DomainType) parameter ).getName().equals( name ) ) {
                    return parameter;
                }
            }
        }

        return null;
    }

    /**
     * @param name
     * @return the <code>Operation</code> with the specified name or null, if there is no
     *         operation with that name.
     */
    public Operation getOperation( QualifiedName name ) {
        for ( Operation operation : operations ) {
            if ( operation.getName().equals( name ) ) {
                return operation;
            }
        }

        return null;
    }

    /**
     * @return Returns the operatesOn.
     */
    public List<Object> getOperatesOn() {
        return operatesOn;
    }

}

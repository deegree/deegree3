// $HeadURL$
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.deegree.owscommon.OWSDomainType;

/**
 * Represents the <code>OperationMetadata</code> part in the capabilities document of an OGC-web
 * service according to the <code>OWS Common Implementation
 * Specification 0.3</code> (and
 * especially <code>owsOperationsMetadata.xsd</code>). As this class is abstract, it only defines
 * the <code>GetCapabilities</code> operation, which all types of <code>OWS Common</code>
 * -compliant web services must implement.
 * <p>
 * It consists of the following elements: <table border="1">
 * <tr>
 * <th>Name</th>
 * <th>Occurences</th>
 * <th>Function</th>
 * </tr>
 * <tr>
 * <td>ows:Operation</td>
 * <td align="center">2-*</td>
 * <td>Metadata for unordered list of all the (requests for) operations that this server interface
 * implements. The list of required and optional operations implemented shall be specified in the
 * Implementation Specification for this service.</td>
 * </tr>
 * <tr>
 * <td>ows:Parameter</td>
 * <td align="center">0-*</td>
 * <td>Optional unordered list of parameter valid domains that each apply to one or more operations
 * which this server interface implements. The list of required and optional parameter domain
 * limitations shall be specified in the Implementation Specification for this service.</td>
 * </tr>
 * <tr>
 * <td>ows:Constraint</td>
 * <td align="center">0-*</td>
 * <td>Optional unordered list of valid domain constraints on non-parameter quantities that each
 * apply to this server. The list of required and optional constraints shall be specified in the
 * Implementation Specification for this service.</td>
 * </tr>
 * <tr>
 * <td>ows:ExtendedCapabilities</td>
 * <td align="center">0 or 1</td>
 * <td>Individual software vendors and servers can use this element to provide metadata about any
 * additional server abilities.</td>
 * </tr>
 * </table>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public abstract class OperationsMetadata implements Serializable {

    private static final long serialVersionUID = 9004347074744748535L;

    public static final String GET_CAPABILITIES_NAME = "GetCapabilities";

    protected Operation getCapabilitiesOperation = null;

    // keys are Strings (the names of the parameters), values are
    // OWSDomainType-instances
    protected Map<String, OWSDomainType> parameters;

    // keys are Strings (the names of constrained domains), values are
    // OWSDomainType-instances
    protected Map<String, OWSDomainType> constraints;

    /**
     * Creates a new <code>OperationsMetadata</code> instance with the given configuration for the
     * getCapabilitiesOperation.
     *
     * @param getCapabilitiesOperation
     * @param parameters
     * @param constraints
     */
    public OperationsMetadata( Operation getCapabilitiesOperation, OWSDomainType[] parameters,
                               OWSDomainType[] constraints ) {
        this.getCapabilitiesOperation = getCapabilitiesOperation;
        setOperationParameter( parameters );
        setConstraints( constraints );
    }

    /**
     * @return The configuration for the <code>GetCapabilities</code> -operation.
     */
    public Operation getGetCapabilitiesOperation() {
        return getCapabilitiesOperation;
    }

    /**
     * Sets the configuration for the <code>GetCapabilities</code> -operation.
     *
     * @param getCapabilitiesOperation
     *            configuration for the <code>GetCapabilities</code> -operation to be set
     */
    public void setGetCapabilitiesOperation( Operation getCapabilitiesOperation ) {
        this.getCapabilitiesOperation = getCapabilitiesOperation;
    }

    /**
     * @return all <code>Operation</code> configurations.
     */
    public Operation[] getOperations() {
        return new Operation[] { getCapabilitiesOperation };
    }

    /**
     *
     * @return a list of parameters assigned directly to the OperationsMetadata.
     */
    public OWSDomainType[] getParameter() {
        return parameters.values().toArray( new OWSDomainType[parameters.size()] );
    }

    /**
     * adds a parameter to the OperationsMetadata
     *
     * @param parameter
     */
    public void addParameter( OWSDomainType parameter ) {
        parameters.put( parameter.getName(), parameter );
    }

    /**
     *
     *
     * @param name
     *            removes a parameter from the OperationsMetadata
     * @return the removed parameter
     */
    public OWSDomainType removeParameter( String name ) {
        return parameters.remove( name );
    }

    /**
     * sets a complete list of parameters to the OperationMetadata
     *
     * @param parameters
     */
    public void setOperationParameter( OWSDomainType[] parameters ) {
        if ( this.parameters == null ) {
            this.parameters = new HashMap<String, OWSDomainType>();
        } else {
            this.parameters.clear();
        }
        if ( parameters != null ) {
            for ( int i = 0; i < parameters.length; i++ ) {
                addParameter( parameters[i] );
            }
        }
    }

    /**
     * @return Returns the constraints.
     */
    public OWSDomainType[] getConstraints() {
        return constraints.values().toArray( new OWSDomainType[constraints.values().size()] );
    }

    /**
     * Sets the constraints of the <code>OperationMetadata</code>.
     *
     * @param constraints
     *            may be null
     */
    public void setConstraints( OWSDomainType[] constraints ) {
        if ( this.constraints == null ) {
            this.constraints = new HashMap<String, OWSDomainType>();
        } else {
            this.constraints.clear();
        }
        if ( constraints != null ) {
            for ( int i = 0; i < constraints.length; i++ ) {
                addConstraint( constraints[i] );
            }
        }
    }

    /**
     * Adds a constraint.
     *
     * @param constraint
     */
    public void addConstraint( OWSDomainType constraint ) {
        constraints.put( constraint.getName(), constraint );
    }
}

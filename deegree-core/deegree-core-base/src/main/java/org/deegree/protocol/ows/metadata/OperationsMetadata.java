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
package org.deegree.protocol.ows.metadata;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.axiom.om.OMElement;

/**
 * Encapsulates the metadata on operations of an OGC web service (as reported in the capabilities document).
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author <a href="mailto:ionita@lat-lon.de">Andrei Ionita</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class OperationsMetadata {

    private final Map<String, Operation> operationNameToMD = new HashMap<String, Operation>();

    private final List<Domain> parameters;

    private final List<Domain> constraints;

    private OMElement extendedCapabilities;

    public OperationsMetadata( List<Operation> operations, List<Domain> parameters, List<Domain> constraints,
                               Object extendedCapabilities ) {
        for ( Operation operation : operations ) {
            operationNameToMD.put( operation.getName(), operation );
        }
        this.parameters = parameters;
        this.constraints = constraints;
    }

    /**
     * Returns the metadata for all operations.
     * 
     * @return operation metadata, may be empty, but never <code>null</code>
     */
    public List<Operation> getOperation() {
        return new ArrayList<Operation>( operationNameToMD.values() );
    }

    /**
     * Returns the metadata for the specified operation name.
     * 
     * @param operationName
     *            name of the operation, can be <code>null</code>
     * @return operation metadata or <code>null</code> if no metadata for operation available
     */
    public Operation getOperation( String operationName ) {
        return operationNameToMD.get( operationName );
    }

    /**
     * Returns the endpoint {@link URL}s for the specified operation (method HTTP-GET).
     * 
     * @return endpoint URLs, can be empty, but never <code>null</code>
     */
    public List<URL> getGetUrls( String operationName ) {
        Operation operation = getOperation( operationName );
        if ( operation != null ) {
            return operation.getGetUrls();
        }
        return null;
    }

    /**
     * Returns the endpoint {@link URL}s for the specified operation (method HTTP-POST).
     * 
     * @return endpoint URLs, can be empty, but never <code>null</code>
     */
    public List<URL> getPostUrls( String operationName ) {
        Operation operation = getOperation( operationName );
        if ( operation != null ) {
            return operation.getPostUrls();
        }
        return null;
    }

    /**
     * @return parameters, may be empty but never <code>null</code>
     */
    public List<Domain> getParameters() {
        return parameters;
    }

    /**
     * @return constraints, may be empty but never <code>null</code>
     */
    public List<Domain> getConstraints() {
        return constraints;
    }

    public OMElement getExtendedCapabilities() {
        return extendedCapabilities;
    }
}

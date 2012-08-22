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
package org.deegree.ogcwebservices.wfs.capabilities;

import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.Set;

import org.deegree.i18n.Messages;

/**
 * Represents an element of type 'wfs:OperationType'.
 *
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$ $Date$
 */
public class Operation {

    /**
     * A string denoting 'Insert'
     */
    public static final String INSERT = "Insert";

    /**
     * A string denoting 'Update'
     */
    public static final String UPDATE = "Update";

    /**
     * A string denoting 'Delete'
     */
    public static final String DELETE = "Delete";

    /**
     * A string denoting 'Query'
     */
    public static final String QUERY = "Query";

    /**
     * A string denoting 'Lock'
     */
    public static final String LOCK = "Lock";

    /**
     * A string denoting 'GetGMLObject'
     */
    public static final String GET_GML_OBJECT = "GetGMLObject";

    private static final Set<String> VALID_OPERATIONS = new HashSet<String>();

    static {
        VALID_OPERATIONS.add( INSERT );
        VALID_OPERATIONS.add( UPDATE );
        VALID_OPERATIONS.add( DELETE );
        VALID_OPERATIONS.add( QUERY );
        VALID_OPERATIONS.add( LOCK );
        VALID_OPERATIONS.add( GET_GML_OBJECT );
    }

    private String operation;

    /**
     * @return the type of the operation as a <code>String</code>.
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Constructs a new OperationType.
     *
     * @param operation
     * @throws InvalidParameterException
     */
    public Operation( String operation ) throws InvalidParameterException {
        if ( VALID_OPERATIONS.contains( operation ) ) {
            this.operation = operation;
        } else {
            String msg = Messages.getMessage( "WFS_INVALID_OPERATION_TYPE", operation );
            throw new InvalidParameterException( msg );
        }
    }
}

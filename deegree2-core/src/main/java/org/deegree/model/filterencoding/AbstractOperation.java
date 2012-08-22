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
package org.deegree.model.filterencoding;

import org.w3c.dom.Element;

/**
 * Abstract superclass representing spatial_ops, comparison_ops and logical_ops entities (as defined
 * in the Filter DTD). The three different types are reflected in SpatialOperation,
 * ComparisonOperation and LogicalOperation classes.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public abstract class AbstractOperation implements Operation {

    /**
     * The underlying operator's id.
     *
     * @see OperationDefines
     */
    protected int operatorId;

    /**
     *
     * @param operatorId
     */
    AbstractOperation( int operatorId ) {
        this.operatorId = operatorId;
    }
   

    /**
     * Given a DOM-fragment, a corresponding Operation-object is built. This method recursively
     * calls other buildFromDOM () - methods to validate the structure of the DOM-fragment.
     *
     * @param element
     * @param useVersion_1_0_0
     *            true if the filter encoding 1.0.0 should be used.
     * @return operation instance
     * @throws FilterConstructionException
     *             if the structure of the DOM-fragment is invalid
     *
     */
    public static Operation buildFromDOM( Element element, boolean useVersion_1_0_0 )
                            throws FilterConstructionException {

        // check if root element's name is a known operator
        String name = element.getLocalName();
        int type = OperationDefines.getTypeByName( name );
        Operation operation = null;

        switch ( type ) {
        case OperationDefines.TYPE_SPATIAL: {
            operation = SpatialOperation.buildFromDOM( element, useVersion_1_0_0 );
            break;
        }
        case OperationDefines.TYPE_COMPARISON: {
            operation = ComparisonOperation.buildFromDOM( element, useVersion_1_0_0 );
            break;
        }
        case OperationDefines.TYPE_LOGICAL: {
            operation = LogicalOperation.buildFromDOM( element, useVersion_1_0_0 );
            break;
        }
        default: {
            throw new FilterConstructionException( "Unknown operator '" + name + "'!" );
        }
        }
        return operation;
    }

    /**
     * Returns the name of the operator.
     *
     * @return operation name
     */
    public String getOperatorName() {
        return OperationDefines.getNameById( operatorId );
    }

    /**
     * Returns the operator's id.
     *
     * @see OperationDefines
     * @return operation id
     */
    public int getOperatorId() {
        return operatorId;
    }
}

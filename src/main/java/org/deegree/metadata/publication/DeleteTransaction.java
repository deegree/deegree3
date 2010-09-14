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
package org.deegree.metadata.publication;

import javax.xml.namespace.QName;

import org.deegree.filter.Filter;
import org.deegree.protocol.csw.CSWConstants.TransactionType;

/**
 * Represents a CSW <code>Delete</code> action (part of a Transaction operation request).
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DeleteTransaction extends TransactionOperation {

    private QName typeName;

    private Filter constraint;

    /**
     * Creates a new {@link DeleteTransaction} instance.
     * 
     * @param handle
     * @param typeName
     * @param constraint
     */
    public DeleteTransaction( String handle, QName typeName, Filter constraint ) {
        super( handle );
        this.typeName = typeName;
        this.constraint = constraint;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.services.controller.csw.transaction.TransactionOperation#getType()
     */
    @Override
    public TransactionType getType() {

        return TransactionType.DELETE;
    }

    /**
     * @return the typeName
     */
    public QName getTypeName() {
        return typeName;
    }

    /**
     * @return the constraint
     */
    public Filter getConstraint() {
        return constraint;
    }

}

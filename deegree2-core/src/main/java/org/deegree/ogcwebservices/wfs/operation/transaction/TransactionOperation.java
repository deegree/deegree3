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
package org.deegree.ogcwebservices.wfs.operation.transaction;

import java.util.List;

import org.deegree.datatypes.QualifiedName;

/**
 * Abstract base class for all operations that can occur inside a {@link Transaction} request.
 *
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public abstract class TransactionOperation {

    private String handle;

    /**
     * Creates a new <code>TransactionOperation</code> instance that may be identified (in the
     * scope of a transaction) by the optional handle.
     *
     * @param handle optional identifier for the operation (for error messsages)
     */
    protected TransactionOperation( String handle ) {
        this.handle = handle;
    }

    /**
     * Returns the idenfifier of the operation.
     *
     * @return the idenfifier of the operation.
     */
    public String getHandle() {
        return this.handle;
    }

    /**
     * Returns the names of the feature types that are affected by the operation.
     *
     * @return the names of the affected feature types.
     */
    public abstract List<QualifiedName> getAffectedFeatureTypes();
}

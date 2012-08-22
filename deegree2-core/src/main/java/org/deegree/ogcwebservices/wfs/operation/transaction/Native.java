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
import org.w3c.dom.Element;

/**
 * Represents a <code>Native</code> operation as a part of a {@link Transaction} request.
 *
 * @author <a href="mailto:deshmukh@lat-lon.de">Anup Deshmukh </a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class Native extends TransactionOperation {

    private Element vendorSpecificData;

    private String vendorId;

    private boolean safeToIgnore;

    /**
     * Creates a new <code>Native</code> instance.
     *
     * @param handle
     *            optional identifier for the operation (for error messsages)
     * @param vendorSpecificData
     *            vendor specific information (as a DOM element)
     * @param vendorId
     *            vendor identifier
     * @param safeToIgnore
     *            true, if the operation may be ignored without problems, false if the surrounding
     *            request depends on it (and must fail if the native operation cannot be executed)
     */
    public Native( String handle, Element vendorSpecificData, String vendorId, boolean safeToIgnore ) {
        super( handle );
        this.vendorSpecificData = vendorSpecificData;
        this.vendorId = vendorId;
        this.safeToIgnore = safeToIgnore;
    }

    /**
     * Returns the vendor specific data that describes the operation to be performed.
     *
     * @return the vendor specific data that describes the operation to be performed.
     */
    public Element getVendorSpecificData() {
        return this.vendorSpecificData;
    }

    /**
     * Returns the vendor identifier.
     *
     * @return the vendor identifier.
     */
    public String getVendorId() {
        return this.vendorId;
    }

    /**
     * Returns whether the surrounding transaction request must fail if the operation can
     * not be executed.
     *
     * @return true, if the operation may be ignored safely, false otherwise.
     */
    public boolean isSafeToIgnore() {
        return this.safeToIgnore;
    }

    /**
     * Returns the names of the feature types that are affected by the operation.
     *
     * @return the names of the affected feature types.
     */
    @Override
    public List<QualifiedName> getAffectedFeatureTypes() {
        throw new UnsupportedOperationException( "getAffectFeatureTypes() is not supported "
                                                 + "for Native operations." );
    }
}

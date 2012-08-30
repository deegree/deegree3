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

package org.deegree.protocol.wfs.transaction;

import javax.xml.stream.XMLStreamReader;

/**
 * Represents a WFS <code>Native</code> operation (part of a {@link Transaction} request).
 * 
 * @see Transaction
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Native extends TransactionOperation {

    private String vendorId;

    private boolean safeToIgnore;

    private XMLStreamReader vendorSpecificData;

    /**
     * Creates a new {@link Native} instance.
     * 
     * @param handle
     *            identifier for the operation, can be null
     * @param vendorId
     *            vendor identifier
     * @param safeToIgnore
     *            true, if the operation may be ignored without problems, false if the surrounding request depends on it
     *            (and must fail if the native operation cannot be executed)
     * @param vendorSpecificData
     *            provides access to the XML encoded vendor specific data, cursor must point at the
     *            <code>START_ELEMENT</code> event of the <code>wfs:Native</code> element, must not be null
     */
    public Native( String handle, String vendorId, boolean safeToIgnore, XMLStreamReader vendorSpecificData ) {
        super( handle );
        this.vendorSpecificData = vendorSpecificData;
        this.vendorId = vendorId;
        this.safeToIgnore = safeToIgnore;
    }

    /**
     * Always returns {@link TransactionOperation.Type#NATIVE}.
     * 
     * @return {@link TransactionOperation.Type#NATIVE}
     */
    @Override
    public Type getType() {
        return Type.NATIVE;
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
     * Returns whether the whole transaction request should fail if the operation can not be executed.
     * 
     * @return true, if the operation may be ignored safely, false otherwise
     */
    public boolean isSafeToIgnore() {
        return this.safeToIgnore;
    }

    /**
     * Returns an <code>XMLStreamReader</code> that provides access to the vendor specific data.
     * <p>
     * NOTE: The client <b>must</b> read this stream exactly once and exactly up to the next tag event after the closing
     * element of the feature/feature collection, i.e. the END_ELEMENT of the surrounding <code>Native</code> element.
     * </p>
     * 
     * @return XML encoded vendor specific data, cursor points at the <code>START_ELEMENT</code> event of the
     *         <code>wfs:Native</code> element, never null
     */
    public XMLStreamReader getVendorSpecificData() {
        return this.vendorSpecificData;
    }
}

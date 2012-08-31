//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.protocol.wfs.transaction.action;

import static org.deegree.protocol.wfs.transaction.TransactionActionType.REPLACE;

import javax.xml.stream.XMLStreamReader;

import org.deegree.protocol.wfs.transaction.Transaction;
import org.deegree.protocol.wfs.transaction.TransactionActionType;

/**
 * A WFS <code>Replace</code> action (part of a {@link Transaction} request).
 * 
 * @see Transaction
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class Replace extends AbstractTransactionAction {

    private final XMLStreamReader xmlStream;

    /**
     * Creates a new {@link Replace} instance.
     * 
     * @param handle
     *            identifier for the operation, may be <code>null</code>
     * @param xmlStream
     *            provides access to the XML encoded replacement feature and the filter, must point at the
     *            <code>START_ELEMENT</code> event of the replacement feature, never <code>null</code>
     */
    public Replace( String handle, XMLStreamReader xmlStream ) {
        super( handle );
        this.xmlStream = xmlStream;
    }

    /**
     * Always returns {@link TransactionActionType#REPLACE}.
     * 
     * @return {@link TransactionActionType#REPLACE}
     */
    @Override
    public TransactionActionType getType() {
        return REPLACE;
    }

    /**
     * Returns an {@link XMLStreamReader} that provides access to the XML encoded replacement feature and the filter.
     * 
     * @return xml stream, never <code>null</code>, points at the <code>START_ELEMENT</code> event of the replacement
     *         feature
     */
    public XMLStreamReader getXmlStream() {
        return xmlStream;
    }
}
